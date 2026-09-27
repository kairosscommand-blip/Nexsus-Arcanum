package com.nexuscraft.nexusarcanum;

import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Fires every time the enchanting-table view recomputes its three offers. This is where
 * NexusArcanum's whole overhaul actually shows up to the player: real vanilla's own offers are
 * discarded outright and replaced with three freshly-planned {@link OfferPlan}s built from this
 * table's real {@link Resonance}, one per slot, cached by {@link OfferCache} so the matching
 * {@code EnchantItemEvent} later grants exactly what was shown here.
 */
final class EnchantPrepareListener implements Listener {

    private final ArcanumConfig config;
    private final ResonanceScanner scanner;
    private final OfferPlanner planner;
    private final OfferCache cache;

    EnchantPrepareListener(ArcanumConfig config, ResonanceScanner scanner, OfferPlanner planner, OfferCache cache) {
        this.config = config;
        this.scanner = scanner;
        this.planner = planner;
        this.cache = cache;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrepare(PrepareItemEnchantEvent event) {
        if (!config.enabled) {
            return;
        }
        ItemStack item = event.getItem();
        Player player = event.getEnchanter();
        Block table = event.getEnchantBlock();
        if (item == null || player == null || table == null) {
            return;
        }

        Resonance resonance = scanner.scan(table);
        Map<Enchantment, Integer> alreadyOnItem = item.getEnchantments();

        EnchantmentOffer[] offers = event.getOffers();
        OfferPlan[] plans = new OfferPlan[offers.length];
        for (int slot = 0; slot < offers.length; slot++) {
            OfferPlan plan = planner.plan(item.getType(), slot, resonance, alreadyOnItem);
            plans[slot] = plan;
            offers[slot] = plan.isEmpty() ? null : new EnchantmentOffer(plan.headline(), plan.headlineLevel(), plan.cost());
        }
        cache.put(player, plans);
    }
}
