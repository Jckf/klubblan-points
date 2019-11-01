package net.klubblan.points.listeners;

import net.klubblan.points.Points;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class InventoryListener implements Listener
{
    private final Points plugin;

    public InventoryListener(final Points plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void enderChestHandler(final InventoryCloseEvent event)
    {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();

        if (!inventory.getType().equals(InventoryType.ENDER_CHEST)) {
            return;
        }

        final HashMap<Material, Integer> materialMap = new HashMap<>();

        for (final ItemStack stack : inventory.getContents()) {
            if (stack == null) {
                continue;
            }

            final Material material = stack.getType();

            if (!materialMap.containsKey(material)) {
                materialMap.put(material, 0);
            }

            materialMap.put(material, materialMap.get(material) + stack.getAmount());
        }

        int points = 0;

        for (Material material : materialMap.keySet()) {
            final int value = this.plugin.getConfig().getInt(this.plugin.getMaterialConfigKey(material) + ".points");

            if (value == 0) {
                continue;
            }

            final int maxItems = this.plugin.getConfig().getInt(this.plugin.getMaterialConfigKey(material) + ".max_items");

            final int items = materialMap.get(material);

            points += Math.min(items, maxItems) * value;
        }

        this.plugin.setItemPoints(player, points);
    }
}
