package net.klubblan.points.listeners;

import net.klubblan.points.Points;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockListener implements Listener
{
    private final Points plugin;

    public BlockListener(final Points plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void enderChestProtectionHandler(BlockBreakEvent event)
    {
        if (!event.getBlock().getType().equals(Material.ENDER_CHEST)) {
            return;
        }

        event.setCancelled(true);
    }
}
