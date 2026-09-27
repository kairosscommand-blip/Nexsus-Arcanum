package com.nexuscraft.nexusarcanum;

import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

/**
 * What one offer slot actually resolves to: the headline enchantment+level shown on the button
 * (all a real {@code EnchantmentOffer} can display -- see that stub's own comment), the full set
 * of real vanilla enchantments actually granted if the player clicks it (which can be more than
 * one at Epic/Mythic -- the "massive overhaul" part), the tier it rolled, and its lapis/XP cost.
 * Built by {@link OfferPlanner}, cached by {@link OfferCache}, consumed by
 * {@code EnchantApplyListener} at the exact moment the player clicks.
 */
record OfferPlan(EnchantTier tier, Enchantment headline, int headlineLevel, int cost, Map<Enchantment, Integer> grants) {

    static OfferPlan empty(int cost) {
        return new OfferPlan(EnchantTier.COMMON, null, 0, cost, Map.of());
    }

    boolean isEmpty() {
        return headline == null || grants.isEmpty();
    }

    List<Enchantment> grantedEnchantments() {
        return List.copyOf(grants.keySet());
    }
}
