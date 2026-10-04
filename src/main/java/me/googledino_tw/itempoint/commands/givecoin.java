package me.googledino_tw.itempoint.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class givecoin implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "此指令只能由玩家執行。");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("itemcoin.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.YELLOW + "用法: /givecoin <每個可兌換日幣數量> [張數]");
            return true;
        }

        int value;
        try {
            value = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "數量必須是整數。");
            return true;
        }
        if (value <= 0) {
            player.sendMessage(ChatColor.RED + "數量必須大於 0。");
            return true;
        }

        int count = 1;
        if (args.length >= 2) {
            try {
                count = Math.max(1, Integer.parseInt(args[1]));
            } catch (NumberFormatException ignored) {
                // 保持 1
            }
        }

        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "日幣兌換 右鍵");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.RED + "每個可兌換 " + ChatColor.GOLD + value + ChatColor.RED + " 日幣");
        meta.setLore(lore);
        item.setItemMeta(meta);
        item.setAmount(Math.min(64, count));
        player.getInventory().addItem(item);

        return true;
    }
}
