package net.klubblan.points.listeners;

import net.klubblan.points.Points;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

public class WorldListener implements Listener {
    private final Points plugin;

    public WorldListener(final Points plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void initHandler(WorldInitEvent event)
    {
        event.getWorld().setKeepSpawnInMemory(false);
    }
}
