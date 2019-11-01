package net.klubblan.points.listeners;

import net.klubblan.points.Points;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.advancement.Advancement;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;

public class PlayerListener implements Listener
{
    private final Points plugin;

    private final Configuration config;

    private boolean silentJoinOp;

    private boolean silentJoinEveryone;

    private List<String> isolateEnvironments;

    public PlayerListener(final Points plugin)
    {
        this.plugin = plugin;

        this.config = this.plugin.getConfig();

        this.silentJoinOp = this.config.getBoolean("silent_join.op");
        this.silentJoinEveryone = this.config.getBoolean("silent_join.everyone");

        this.isolateEnvironments = this.config.getStringList("isolate_environments");
    }

    private void i(final String message)
    {
        this.plugin.getLogger().info(message);
    }

    @EventHandler
    public void joinIsolationHandler(final PlayerJoinEvent event)
    {
        this.isolationHandler(event.getPlayer());
    }

    @EventHandler
    public void teleportIsolationHandler(final PlayerTeleportEvent event)
    {
        switch (event.getCause()) {
            case PLUGIN:
            case COMMAND:
            case UNKNOWN:
                return;
        }

        this.isolationHandler(event.getPlayer());
    }

    private void isolationHandler(final Player player)
    {
        final World world = player.getLocation().getWorld();

        this.i("Running isolation check for " + player.getName() + " in " + world.getName() + " (" + world.getUID() + ").");

        if (player.isOp()) {
            this.i("Is op. Ignoring.");
            return;
        }

        final String environmentName = world.getEnvironment().name().toLowerCase();

        if (!this.isolateEnvironments.contains(environmentName)) {
            this.i("Environment " + environmentName + " is not isolated.");
            return;
        }

        final String worldOwner = this.config.getString("worlds." + world.getUID() + ".owner");

        final String playerUid = player.getUniqueId().toString();

        if (playerUid.equals(worldOwner)) {
            this.i("World belongs to player.");
            return;
        }

        this.i("Player " + playerUid + " is not owner " + (worldOwner == null ? "null" : worldOwner) + ".");

        for (final World checkWorld : this.plugin.getServer().getWorlds()) {
            this.i("Checking " + checkWorld.getName() + ".");

            if (!checkWorld.getEnvironment().equals(world.getEnvironment())) {
                this.i("Incorrect environment.");
                continue;
            }

            final String checkOwner = this.config.getString("worlds." + checkWorld.getUID() + ".owner");

            if (checkOwner == null) {
                this.i("Nobody owns this world.");
                continue;
            }

            if (checkOwner.equals(playerUid)) {
                this.i("Moving to " + checkWorld.getName() + ".");
                player.teleport(checkWorld.getSpawnLocation());
                return;
            }

            this.i("Player " + playerUid + " is not owner " + checkOwner + ".");
        }

        this.i("Creating new world.");

        final WorldCreator creator = new WorldCreator(player.getName() + "_" + world.getName());
        creator.copy(world);

        final World newWorld = this.plugin.getServer().createWorld(creator);

        this.config.set("worlds." + newWorld.getUID() + ".owner", playerUid);

        player.teleport(newWorld.getSpawnLocation());
    }

    @EventHandler
    public void firstJoinHandler(PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();

        if (player.hasPlayedBefore()) {
            return;
        }

        player.getInventory().addItem(new ItemStack(Material.ENDER_CHEST));
    }

    @EventHandler
    public void silentJoinHandler(final PlayerJoinEvent event)
    {
        if (event.getPlayer().isOp()) {
            if (!this.silentJoinOp) {
                return;
            }
        } else {
            if (!this.silentJoinEveryone) {
                return;
            }
        }

        this.i("Silent join.");

        event.setJoinMessage(null);
    }

    @EventHandler
    public void recalculateAdvancementsHandler(PlayerLoginEvent event)
    {
        final Player player = event.getPlayer();

        int points = 0;

        final Iterator<Advancement> advancementIterator = this.plugin.getServer().advancementIterator();
        while (advancementIterator.hasNext()) {
            final Advancement advancement = advancementIterator.next();

            if (player.getAdvancementProgress(advancement).isDone()) {
                points += this.config.getInt(this.plugin.getAdvancementConfigKey(advancement));
            }
        }

        this.plugin.setAdvancementPoints(player, points);
    }

    @EventHandler
    public void advancementHandler(final PlayerAdvancementDoneEvent event)
    {
        final Player player = event.getPlayer();

        final String advancementKey = this.plugin.getAdvancementConfigKey(event.getAdvancement());

        final int points = this.config.getInt(advancementKey);

        this.i("Player " + player.getName() + " finished advancement " + advancementKey + " worth " + points + " points.");

        if (points != 0) {
            this.plugin.incrementAdvancementPoints(player, points);
        }
    }
}
