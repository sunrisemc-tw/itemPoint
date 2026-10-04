package me.googledino_tw.itempoint;

import me.googledino_tw.itempoint.commands.givecoin;
import me.googledino_tw.itempoint.events.rightclick;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ItemPoint extends JavaPlugin {

    private static ItemPoint instance;

    public static ItemPoint get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        if (getCommand("givecoin") != null) {
            getCommand("givecoin").setExecutor(new givecoin());
        }

        // Register event listener
        Bukkit.getPluginManager().registerEvents(new rightclick(), this);
    }

    @Override
    public void onDisable() {
        instance = null;
    }
}
