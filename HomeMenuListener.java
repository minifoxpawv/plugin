package org.example;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.md_5.bungee.api.chat.*;

public class HomeMenuListener implements Listener {

    private final TestPlugin plugin;

    public HomeMenuListener(TestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals("§8Homes")) return;
        if (event.getClick() == ClickType.SWAP_OFFHAND) {
            event.setCancelled(true);
            return;
        }


        int slot = event.getRawSlot();
        if (slot >= event.getView().getTopInventory().getSize()) return;

        event.setCancelled(true);

        /* DELETE BUTTONS */
        if (slot >= 45 && slot <= 53) {
            int homeId = slot - 44;
            String base = player.getUniqueId() + ".homes." + homeId;

            if (!plugin.getConfig().contains(base)) {
                player.sendMessage("§cHome does not exist!");
                return;
            }

            plugin.getDeleteConfirm().put(player.getUniqueId(), homeId);
            player.closeInventory();
            sendDeleteConfirm(player);
            return;
        }

        /* HOME BUTTONS */
        if (slot < 36 || slot > 44) return;

        int homeId = slot - 35;
        String base = player.getUniqueId() + ".homes." + homeId;

        // SHIFT + LEFT CLICK = RENAME
        if (event.getClick() == ClickType.SHIFT_LEFT
                && plugin.getConfig().contains(base)) {

            plugin.getRenameHome().put(player.getUniqueId(), homeId);
            player.closeInventory();
            player.sendMessage("§eType new name (or cancel)");
            return;
        }


        if (plugin.getConfig().contains(base)) {
            String worldName = plugin.getConfig().getString(base + ".world");
            if (worldName == null || Bukkit.getWorld(worldName) == null) {
                player.sendMessage("§cWorld missing!");
                return;
            }

            Location home = new Location(
                    Bukkit.getWorld(worldName),
                    plugin.getConfig().getDouble(base + ".x"),
                    plugin.getConfig().getDouble(base + ".y"),
                    plugin.getConfig().getDouble(base + ".z"),
                    (float) plugin.getConfig().getDouble(base + ".yaw"),
                    (float) plugin.getConfig().getDouble(base + ".pitch")
            );

            player.teleport(home);

            String name = plugin.getConfig().getString(base + ".name", "Home " + homeId);
            player.sendMessage("§aTeleported to " + name);

            player.closeInventory();
            return;
        }

        Location loc = player.getLocation();
        plugin.getConfig().set(base + ".world", loc.getWorld().getName());
        plugin.getConfig().set(base + ".x", loc.getX());
        plugin.getConfig().set(base + ".y", loc.getY());
        plugin.getConfig().set(base + ".z", loc.getZ());
        plugin.getConfig().set(base + ".yaw", loc.getYaw());
        plugin.getConfig().set(base + ".pitch", loc.getPitch());
        plugin.saveConfig();

        plugin.getRenameHome().put(player.getUniqueId(), homeId);
        player.closeInventory();
        player.sendMessage("§aHome set! Name it.");
    }

    private void sendDeleteConfirm(Player player) {
        TextComponent yes = new TextComponent("§a[CONFIRM]");
        yes.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/homeconfirm confirm"));

        TextComponent no = new TextComponent(" §c[CANCEL]");
        no.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/homeconfirm cancel"));

        player.spigot().sendMessage(new ComponentBuilder("§cDelete home? ").append(yes).append(no).create());
    }
}
