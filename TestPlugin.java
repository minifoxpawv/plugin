package org.example;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class TestPlugin extends JavaPlugin implements Listener {

    private final Map<UUID, Integer> deleteConfirm = new HashMap<>();
    private final Map<UUID, Integer> renameHome = new HashMap<>();

    public Map<UUID, Integer> getDeleteConfirm() {
        return deleteConfirm;
    }

    public Map<UUID, Integer> getRenameHome() {
        return renameHome;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new HomeMenuListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MenuKeybindListener(this), this);
        Bukkit.getPluginManager().registerEvents(new HomeRenameListener(this), this);

        getLogger().info("TestPlugin enabled!");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        deleteConfirm.remove(id);
        renameHome.remove(id);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only!");
            return true;
        }

        /* ---------- /home <1-9> ---------- */
        if (command.getName().equalsIgnoreCase("home")) {
            if (args.length != 1) {
                player.sendMessage("§cUsage: /home <1-9>");
                return true;
            }

            int homeId;
            try {
                homeId = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid number!");
                return true;
            }

            if (homeId < 1 || homeId > 9) {
                player.sendMessage("§cHome must be between 1 and 9!");
                return true;
            }

            String path = player.getUniqueId() + ".homes." + homeId;
            if (!getConfig().contains(path)) {
                player.sendMessage("§cHome not set!");
                return true;
            }

            String worldName = getConfig().getString(path + ".world");
            if (worldName == null || Bukkit.getWorld(worldName) == null) {
                player.sendMessage("§cThat world no longer exists!");
                return true;
            }

            Location home = new Location(
                    Bukkit.getWorld(worldName),
                    getConfig().getDouble(path + ".x"),
                    getConfig().getDouble(path + ".y"),
                    getConfig().getDouble(path + ".z"),
                    (float) getConfig().getDouble(path + ".yaw"),
                    (float) getConfig().getDouble(path + ".pitch")
            );

            player.teleport(home);
            player.sendMessage("§aTeleported!");
            return true;
        }

        /* ---------- /menu ---------- */
        if (command.getName().equalsIgnoreCase("menu")) {
            Inventory menu = Bukkit.createInventory(player, 54, "§8Homes");

            for (int i = 0; i < 9; i++) {
                int homeId = i + 1;
                String basePath = player.getUniqueId() + ".homes." + homeId;

                ItemStack bed;
                ItemMeta meta;

                if (getConfig().contains(basePath)) {
                    bed = new ItemStack(Material.GREEN_BED);
                    meta = bed.getItemMeta();
                    String name = getConfig().getString(basePath + ".name", "Home " + homeId);
                    meta.setDisplayName("§a" + name);

                    meta.setLore(List.of("§7Click to teleport", "§7Shift-click to rename"));
                } else {
                    bed = new ItemStack(Material.RED_BED);
                    meta = bed.getItemMeta();
                    meta.setDisplayName("§cSet Home " + homeId);
                    meta.setLore(List.of("§7Click to set"));
                }

                bed.setItemMeta(meta);
                menu.setItem(36 + i, bed);

                ItemStack delete = new ItemStack(Material.BARRIER);
                ItemMeta dm = delete.getItemMeta();
                dm.setDisplayName("§cDelete Home " + homeId);
                delete.setItemMeta(dm);
                menu.setItem(45 + i, delete);
            }

            player.openInventory(menu);
            return true;
        }

        /* ---------- /homeconfirm confirm ---------- */
        if (command.getName().equalsIgnoreCase("homeconfirm")) {
            Integer homeId = deleteConfirm.remove(player.getUniqueId());
            if (homeId == null) {
                player.sendMessage("§7Nothing to confirm.");
                return true;
            }

            if (args.length == 0 || !args[0].equalsIgnoreCase("confirm")) {
                player.sendMessage("§7Cancelled.");
                return true;
            }

            getConfig().set(player.getUniqueId() + ".homes." + homeId, null);
            saveConfig();
            player.sendMessage("§aHome deleted!");
            return true;
        }

        return true;
    }
}
