package org.example;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class HomeRenameListener implements Listener {

    private final TestPlugin plugin;

    public HomeRenameListener(TestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        Integer homeId = plugin.getRenameHome().get(player.getUniqueId());
        if (homeId == null) return;

        // We ARE handling chat → cancel it
        event.setCancelled(true);

        String message = event.getMessage().trim();

        // CANCEL
        if (message.equalsIgnoreCase("cancel")) {
            plugin.getRenameHome().remove(player.getUniqueId());
            player.sendMessage("§7Naming cancelled.");
            return;
        }

        // VALIDATION
        if (message.length() < 2) {
            player.sendMessage("§cName too short.");
            return;
        }

        if (message.length() > 16) {
            player.sendMessage("§cName too long (max 16 characters).");
            return;
        }

        if (message.matches("\\d+")) {
            player.sendMessage("§cName cannot be only numbers.");
            return;
        }

        // SUCCESS
        plugin.getRenameHome().remove(player.getUniqueId());

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            String path = player.getUniqueId() + ".homes." + homeId + ".name";
            plugin.getConfig().set(path, message);
            plugin.saveConfig();
            player.sendMessage("§aHome renamed to §f" + message);
        });
    }
}
