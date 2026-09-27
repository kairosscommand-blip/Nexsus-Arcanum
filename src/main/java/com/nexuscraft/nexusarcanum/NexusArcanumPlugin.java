package com.nexuscraft.nexusarcanum;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

/**
 * NexusArcanum -- a massive overhaul of the vanilla enchanting table itself: real bookshelf
 * placement, extended to reward a real 1.20+ chiseled bookshelf (a block vanilla's own table
 * completely ignores) and a balanced/symmetric arrangement, drives both a level-cost ceiling well
 * beyond vanilla's flat 30 and a five-tier rarity system that can grant more than one real vanilla
 * enchantment from a single offer at the top two tiers. Every enchantment this plugin ever grants
 * is a real, ordinary {@code org.bukkit.enchantments.Enchantment} -- nothing here is a custom
 * effect system, so anything else that reads an item's real enchantments (an anvil, a loot table,
 * another plugin) sees exactly what it would from a normal enchant.
 *
 * <p>Deliberately scoped away from both siblings it could be confused with: NexusEnchants is 189
 * custom item-bound effects applied outside the table entirely (several anvil-bound by how they
 * were built), and NexusWands is chat-spoken spellcasting -- neither one touches the actual
 * table-and-bookshelf ritual a player stands in front of, which is exactly what this plugin
 * overhauls instead. A standalone plugin, no shared compiled dependency with either.
 */
public final class NexusArcanumPlugin extends JavaPlugin {

    private ArcanumConfig config;
    private ResonanceScanner scanner;
    private OfferPlanner planner;
    private OfferCache cache;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.config = new ArcanumConfig(this);
        config.load(getLogger());

        this.scanner = new ResonanceScanner(config);
        this.planner = new OfferPlanner(config, new Random());
        this.cache = new OfferCache();

        getServer().getPluginManager().registerEvents(new EnchantPrepareListener(config, scanner, planner, cache), this);
        getServer().getPluginManager().registerEvents(new EnchantApplyListener(config, cache), this);

        var command = getCommand("nexusarcanum");
        if (command != null) {
            command.setExecutor(new ArcanumCommand(config, scanner, this::reload));
        }

        getLogger().info("NexusArcanum enabled -- the enchanting table has been overhauled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("NexusArcanum disabled.");
    }

    private void reload() {
        reloadConfig();
        config.load(getLogger());
    }
}
