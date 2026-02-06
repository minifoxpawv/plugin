package org.example;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class MenuKeybindListener implements Listener {

    private final TestPlugin plugin;

    public MenuKeybindListener(TestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        event.setCancelled(true); // block offhand swap
        player.performCommand("menu");
    }
}


