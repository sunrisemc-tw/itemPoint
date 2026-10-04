package me.googledino_tw.itempoint.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 手持「日幣兌換 右鍵」金錠右鍵即可兌換成日幣（PlayerPoints）。
 *
 * 修正重點：
 *  - 只處理「主手 + 右鍵（空氣/方塊）」，避免左鍵或其他手誤觸。
 *  - 名稱比對忽略顏色碼，材質限定 GOLD_INGOT。
 *  - 兌換金額從 lore「每個可兌換 N 日幣」解析，找不到數字就不動作。
 *  - 先給點數、成功才扣物品，避免物品消失但沒拿到日幣。
 *  - Folia / Canvas 相容：事件本身在玩家 region thread 執行，僅呼叫 PlayerPoints API（非同步 DB），
 *    不使用 Bukkit 排程器，plugin.yml 標記 folia-supported。
 */
public class rightclick implements Listener {

    private static final String ITEM_NAME = "日幣兌換 右鍵";
    private static final Pattern NUMBER = Pattern.compile("(\\d+)");
    /** 同一玩家 250ms 內只處理一次，避免一次點擊因主/副手或方塊+空氣事件重複觸發。 */
    private static final long COOLDOWN_MS = 250L;

    private final Map<UUID, Long> lastUse = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.GOLD_INGOT || !item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        String displayName = meta.getDisplayName();
        if (displayName == null || !ChatColor.stripColor(displayName).equals(ITEM_NAME)) {
            return;
        }

        int amount = parseAmount(meta.getLore());
        if (amount <= 0) {
            return;
        }

        Player player = event.getPlayer();
        long now = System.currentTimeMillis();
        Long last = lastUse.get(player.getUniqueId());
        if (last != null && now - last < COOLDOWN_MS) {
            return;
        }
        lastUse.put(player.getUniqueId(), now);

        if (!givePoints(player, amount)) {
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "日幣" + ChatColor.GRAY + "] "
                    + ChatColor.RED + "兌換失敗，請稍後再試或通知管理員。");
            return;
        }

        // 給點成功後才扣物品
        int remaining;
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            remaining = item.getAmount();
        } else {
            player.getInventory().setItemInMainHand(null);
            remaining = 0;
        }

        player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "日幣" + ChatColor.GRAY + "] "
                + ChatColor.WHITE + "兌換成功 " + ChatColor.GOLD + "+" + amount);

        org.bukkit.Bukkit.getLogger().info("[itemPoint] " + player.getName() + " 兌換 " + ITEM_NAME
                + " -> +" + amount + " 日幣（剩 " + remaining + "）");
    }

    private boolean givePoints(Player player, int amount) {
        try {
            return org.black_ixx.playerpoints.PlayerPoints.getInstance()
                    .getAPI()
                    .give(player.getUniqueId(), amount);
        } catch (Throwable ignored) {
            // PlayerPoints 直接 API 不可用時退回 console 指令
            return org.bukkit.Bukkit.dispatchCommand(
                    org.bukkit.Bukkit.getConsoleSender(),
                    "points give " + player.getName() + " " + amount);
        }
    }

    private int parseAmount(List<String> lore) {
        if (lore == null) {
            return 0;
        }
        for (String line : lore) {
            if (line == null) {
                continue;
            }
            String plain = ChatColor.stripColor(line);
            Matcher matcher = NUMBER.matcher(plain);
            if (plain.contains("兌換") && matcher.find()) {
                try {
                    return Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }
}
