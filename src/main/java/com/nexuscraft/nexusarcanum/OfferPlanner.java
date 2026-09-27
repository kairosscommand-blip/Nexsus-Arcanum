package com.nexuscraft.nexusarcanum;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Builds one {@link OfferPlan} per offer slot -- the actual weighted-roll heart of the "massive
 * overhaul": which tier this slot reaches, which real vanilla enchantment(s) it grants from that
 * tier's pool (filtered by {@link EnchantCatalog} to what the item's category can take, what
 * isn't already on the item, and what doesn't conflict with anything else being granted this same
 * offer), how many levels each lands at, and whether a curse rides along.
 */
final class OfferPlanner {

    /** Fixed roll weights among the tiers reachable at a given resonance -- higher tiers stay
     *  rare even once "unlocked," the ceiling only makes them possible, never guaranteed. */
    private static final Map<EnchantTier, Integer> TIER_ROLL_WEIGHTS = Map.of(
            EnchantTier.COMMON, 50,
            EnchantTier.UNCOMMON, 30,
            EnchantTier.RARE, 15,
            EnchantTier.EPIC, 4,
            EnchantTier.MYTHIC, 1
    );

    private final ArcanumConfig config;
    private final Random random;

    OfferPlanner(ArcanumConfig config, Random random) {
        this.config = config;
        this.random = random;
    }

    OfferPlan plan(Material material, int slotIndex, Resonance resonance, Map<Enchantment, Integer> alreadyOnItem) {
        int cost = Math.max(1, (int) Math.round(config.levelCostCeiling * slotFraction(slotIndex, resonance)));

        EnchantCategory category = EnchantCatalog.categoryOf(material);
        if (category == null) {
            return OfferPlan.empty(cost);
        }

        EnchantTier rolledTier = rollTier(slotIndex, resonance);
        EnchantTier resolvedTier = rolledTier;
        List<Enchantment> pool = candidatesForTier(resolvedTier, category, alreadyOnItem.keySet());
        while (pool.isEmpty() && resolvedTier != EnchantTier.COMMON) {
            resolvedTier = EnchantTier.values()[resolvedTier.ordinal() - 1];
            pool = candidatesForTier(resolvedTier, category, alreadyOnItem.keySet());
        }
        if (pool.isEmpty()) {
            return OfferPlan.empty(cost);
        }

        int grantCount = grantCountFor(resolvedTier);
        Map<Enchantment, Integer> grants = new HashMap<>();
        List<Enchantment> shuffled = new ArrayList<>(pool);
        java.util.Collections.shuffle(shuffled, random);
        for (Enchantment candidate : shuffled) {
            if (grants.size() >= grantCount) {
                break;
            }
            if (EnchantCatalog.conflictsWith(candidate, grants.keySet())
                    || EnchantCatalog.conflictsWith(candidate, alreadyOnItem.keySet())) {
                continue;
            }
            grants.put(candidate, rollLevel(candidate, slotIndex, resonance));
        }

        if (grants.isEmpty()) {
            return OfferPlan.empty(cost);
        }

        if (config.allowCurses && resolvedTier == EnchantTier.MYTHIC && random.nextDouble() < config.mythicCurseChance) {
            rollCurse(category, grants);
        }

        Enchantment headline = grants.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        int headlineLevel = headline != null ? grants.get(headline) : 0;

        return new OfferPlan(resolvedTier, headline, headlineLevel, cost, Map.copyOf(grants));
    }

    private double slotFraction(int slotIndex, Resonance resonance) {
        double effective = Math.max(config.minResonanceFraction, resonance.clampedFraction());
        double[] fractions = config.slotLevelFractions;
        double slotWeight = fractions[Math.max(0, Math.min(fractions.length - 1, slotIndex))];
        return Math.max(0.0, Math.min(1.0, effective * slotWeight));
    }

    private EnchantTier rollTier(int slotIndex, Resonance resonance) {
        EnchantTier ceiling = config.tierCeilingFor(slotFraction(slotIndex, resonance));
        int totalWeight = 0;
        Map<EnchantTier, Integer> reachable = new HashMap<>();
        for (Map.Entry<EnchantTier, Integer> entry : TIER_ROLL_WEIGHTS.entrySet()) {
            if (entry.getKey().ordinal() <= ceiling.ordinal()) {
                reachable.put(entry.getKey(), entry.getValue());
                totalWeight += entry.getValue();
            }
        }
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (Map.Entry<EnchantTier, Integer> entry : reachable.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) {
                return entry.getKey();
            }
        }
        return EnchantTier.COMMON;
    }

    private int grantCountFor(EnchantTier tier) {
        int max = tier.maxSimultaneousGrants();
        int count = 1;
        if (max >= 2 && tier == EnchantTier.EPIC && random.nextDouble() < config.epicSecondEnchantChance) {
            count = 2;
        } else if (max >= 2 && tier == EnchantTier.MYTHIC) {
            if (random.nextDouble() < config.mythicSecondEnchantChance) {
                count = 2;
                if (max >= 3 && random.nextDouble() < config.mythicThirdEnchantChance) {
                    count = 3;
                }
            }
        }
        return count;
    }

    private List<Enchantment> candidatesForTier(EnchantTier tier, EnchantCategory category, Set<Enchantment> alreadyOn) {
        List<Enchantment> candidates = new ArrayList<>();
        for (Enchantment enchantment : EnchantCatalog.byTier(tier)) {
            if (enchantment.isCursed()) {
                continue;
            }
            if (!EnchantCatalog.appliesToCategory(enchantment, category)) {
                continue;
            }
            if (alreadyOn.contains(enchantment)) {
                continue;
            }
            candidates.add(enchantment);
        }
        return candidates;
    }

    private int rollLevel(Enchantment enchantment, int slotIndex, Resonance resonance) {
        int maxLevel = enchantment.getMaxLevel();
        if (maxLevel <= 1) {
            return maxLevel;
        }
        double slotStrength = slotFraction(slotIndex, resonance);
        double levelRoll = Math.max(0.0, Math.min(1.0, random.nextDouble() * 0.5 + slotStrength * 0.5));
        int level = 1 + (int) Math.round((maxLevel - 1) * levelRoll);
        return Math.max(1, Math.min(maxLevel, level));
    }

    private void rollCurse(EnchantCategory category, Map<Enchantment, Integer> grants) {
        List<Enchantment> curses = new ArrayList<>();
        if (EnchantCatalog.appliesToCategory(Enchantment.VANISHING_CURSE, category)) {
            curses.add(Enchantment.VANISHING_CURSE);
        }
        if (EnchantCatalog.appliesToCategory(Enchantment.BINDING_CURSE, category)
                && !grants.containsKey(Enchantment.BINDING_CURSE)) {
            curses.add(Enchantment.BINDING_CURSE);
        }
        if (curses.isEmpty()) {
            return;
        }
        Enchantment curse = curses.get(random.nextInt(curses.size()));
        grants.put(curse, 1);
    }
}
