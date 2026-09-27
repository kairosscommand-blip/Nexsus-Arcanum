package com.nexuscraft.nexusarcanum;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/** Parses config.yml's tunables. Same "config drives shared numbers, EnchantCatalog/EnchantTier
 *  hold the fixed game-balance data" split NexusWands already established (compare WandsConfig's
 *  own comment about wood/core numbers staying fixed in code). Every number here is a shared knob
 *  that applies the same way regardless of which specific enchantment ends up rolled. */
final class ArcanumConfig {

    private final Plugin plugin;

    boolean enabled = true;

    // -- Resonance scoring --
    double maxResonanceScore = 20.0;
    double chiseledSlotPoints = 0.25;
    double symmetryPairPoints = 0.5;
    /** Even a table with zero bookshelves still offers *something*, matching vanilla's own
     *  always-offers-1-3 behavior at zero shelves. */
    double minResonanceFraction = 0.05;

    // -- Tier thresholds (resonance fraction, 0.0-1.0, at or above which a tier becomes reachable
    //    at all -- COMMON is always reachable). --
    double uncommonThreshold = 0.15;
    double rareThreshold = 0.35;
    double epicThreshold = 0.65;
    double mythicThreshold = 0.90;

    // -- Level-cost curve (replaces vanilla's flat 1/15/30-at-15-shelves curve) --
    int levelCostCeiling = 50;
    /** The three offer slots' base fraction of the current ceiling, low/mid/high, same "three
     *  buttons" shape vanilla itself shows. */
    double[] slotLevelFractions = {0.3, 0.6, 1.0};

    // -- Multi-enchant chance at the two highest tiers (EnchantTier's own maxSimultaneousGrants
    //    is the hard cap; these are the odds of actually reaching it rather than falling back to
    //    one fewer). --
    double epicSecondEnchantChance = 0.5;
    double mythicSecondEnchantChance = 0.8;
    double mythicThirdEnchantChance = 0.35;

    // -- Curses (both real vanilla curses are in EnchantCatalog at MYTHIC; this is the on/off
    //    switch plus how often one actually gets appended on top of a Mythic offer). --
    boolean allowCurses = false;
    double mythicCurseChance = 0.15;

    ArcanumConfig(Plugin plugin) {
        this.plugin = plugin;
    }

    void load(Logger log) {
        FileConfiguration c = plugin.getConfig();

        enabled = c.getBoolean("enabled", true);

        ConfigurationSection resonance = c.getConfigurationSection("resonance");
        if (resonance != null) {
            maxResonanceScore = Math.max(1, resonance.getDouble("max-score", maxResonanceScore));
            chiseledSlotPoints = Math.max(0, resonance.getDouble("chiseled-slot-points", chiseledSlotPoints));
            symmetryPairPoints = Math.max(0, resonance.getDouble("symmetry-pair-points", symmetryPairPoints));
            minResonanceFraction = clamp01(resonance.getDouble("min-fraction", minResonanceFraction));
        }

        ConfigurationSection thresholds = c.getConfigurationSection("tier-thresholds");
        if (thresholds != null) {
            uncommonThreshold = clamp01(thresholds.getDouble("uncommon", uncommonThreshold));
            rareThreshold = clamp01(thresholds.getDouble("rare", rareThreshold));
            epicThreshold = clamp01(thresholds.getDouble("epic", epicThreshold));
            mythicThreshold = clamp01(thresholds.getDouble("mythic", mythicThreshold));
        }

        ConfigurationSection cost = c.getConfigurationSection("level-cost");
        if (cost != null) {
            levelCostCeiling = Math.max(1, cost.getInt("ceiling", levelCostCeiling));
            java.util.List<Double> fractions = cost.getDoubleList("slot-fractions");
            if (fractions.size() == 3) {
                slotLevelFractions = new double[]{
                        clamp01(fractions.get(0)), clamp01(fractions.get(1)), clamp01(fractions.get(2))
                };
            }
        }

        ConfigurationSection multi = c.getConfigurationSection("multi-enchant");
        if (multi != null) {
            epicSecondEnchantChance = clamp01(multi.getDouble("epic-second-chance", epicSecondEnchantChance));
            mythicSecondEnchantChance = clamp01(multi.getDouble("mythic-second-chance", mythicSecondEnchantChance));
            mythicThirdEnchantChance = clamp01(multi.getDouble("mythic-third-chance", mythicThirdEnchantChance));
        }

        ConfigurationSection curses = c.getConfigurationSection("curses");
        if (curses != null) {
            allowCurses = curses.getBoolean("allowed", allowCurses);
            mythicCurseChance = clamp01(curses.getDouble("mythic-chance", mythicCurseChance));
        }

        if (!enabled) {
            log.info("NexusArcanum is disabled via config.yml.");
        }
    }

    /** Which tier a given resonance fraction can reach, at or above the configured thresholds --
     *  COMMON is always reachable, higher tiers require progressively more resonance. */
    EnchantTier tierCeilingFor(double resonanceFraction) {
        if (resonanceFraction >= mythicThreshold) {
            return EnchantTier.MYTHIC;
        }
        if (resonanceFraction >= epicThreshold) {
            return EnchantTier.EPIC;
        }
        if (resonanceFraction >= rareThreshold) {
            return EnchantTier.RARE;
        }
        if (resonanceFraction >= uncommonThreshold) {
            return EnchantTier.UNCOMMON;
        }
        return EnchantTier.COMMON;
    }

    private static double clamp01(double value) {
        return Math.max(0, Math.min(1, value));
    }
}
