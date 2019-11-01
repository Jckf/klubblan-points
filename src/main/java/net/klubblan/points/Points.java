package net.klubblan.points;

import net.klubblan.points.listeners.BlockListener;
import net.klubblan.points.listeners.InventoryListener;
import net.klubblan.points.listeners.PlayerListener;
import net.klubblan.points.listeners.WorldListener;
import org.bukkit.Material;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class Points extends JavaPlugin
{
    protected final Map<UUID, Integer> advancementPoints = new HashMap<UUID, Integer>();

    protected final Map<UUID, Integer> itemPoints = new HashMap<UUID, Integer>();

    protected ScoreboardManager sbManager;

    protected Scoreboard scoreboard;

    protected Objective objective;

    @Override
    public void onEnable()
    {
        // Create config file if none is present.
        this.saveDefaultConfig();

        // Load missing values from default file.
        this.getConfig().options().copyDefaults(true);

        for (final Material material : Material.values()) {
            final String key = this.getMaterialConfigKey(material);

            this.getConfig().set(key + ".points", this.getConfig().getInt(key + ".points"));
            this.getConfig().set(key + ".max_items", this.getConfig().getInt(key + ".max_items"));
        }

        final Iterator<Advancement> advancementIterator = this.getServer().advancementIterator();
        while (advancementIterator.hasNext()) {
            final Advancement advancement = advancementIterator.next();

            final String key = this.getAdvancementConfigKey(advancement);

            this.getConfig().set(key, this.getConfig().getInt(key));
        }

        this.sbManager = this.getServer().getScoreboardManager();
        this.scoreboard = this.sbManager.getMainScoreboard();
        this.objective = this.scoreboard.getObjective("points");

        if (this.objective == null) {
            this.objective = this.scoreboard.registerNewObjective("points", "dummy", "Points");
            this.objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        }

        this.registerEvents(new WorldListener(this));
        this.registerEvents(new PlayerListener(this));
        this.registerEvents(new BlockListener(this));
        this.registerEvents(new InventoryListener(this));

        // I'm a cheap bastard who uses the config as data storage. Write it to disk once every minute.
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new ConfigSaver(this), 0, 20 * 60);
    }

    @Override
    public void onDisable()
    {
        this.saveConfig();
    }

    public String getMaterialConfigKey(Material material)
    {
        return "materials." + material.name().toLowerCase();
    }

    public String getAdvancementConfigKey(Advancement advancement)
    {
        return "advancements." + advancement.getKey().getKey().replace("/", ".");
    }

    public void setAdvancementPoints(Player player, int points)
    {
        this.advancementPoints.put(player.getUniqueId(), points);

        this.getConfig().set("points." + player.getUniqueId().toString() + ".advancements", points);

        this.updateScoreboard(player);
    }

    public int getAdvancementPoints(Player player)
    {
        if (!this.advancementPoints.containsKey(player.getUniqueId())) {
            this.advancementPoints.put(player.getUniqueId(), this.getConfig().getInt("points." + player.getUniqueId().toString() + ".advancements"));
        }

        return this.advancementPoints.get(player.getUniqueId());
    }

    public void incrementAdvancementPoints(Player player, int points)
    {
        this.setAdvancementPoints(player, this.getAdvancementPoints(player) + points);
    }

    public void setItemPoints(Player player, int points)
    {
        this.itemPoints.put(player.getUniqueId(), points);

        this.getConfig().set("points." + player.getUniqueId().toString() + ".items", points);

        this.updateScoreboard(player);
    }

    public int getItemPoints(Player player)
    {
        if (!this.itemPoints.containsKey(player.getUniqueId())) {
            this.itemPoints.put(player.getUniqueId(), this.getConfig().getInt("points." + player.getUniqueId().toString() + ".items"));
        }

        return this.itemPoints.get(player.getUniqueId());
    }

    public void incrementItemPoints(Player player, int points)
    {
        this.setItemPoints(player, this.getItemPoints(player) + points);
    }

    public int getPoints(Player player)
    {
        return this.getAdvancementPoints(player) + this.getItemPoints(player);
    }

    public void updateScoreboard(Player player)
    {
        this.objective.getScore(player).setScore(this.getPoints(player));
    }

    private void registerEvents(final Listener listener)
    {
        this.getServer().getPluginManager().registerEvents(listener, this);
    }
}
