package com.nexuscraft.nexusarcanum;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Fires the instant a player clicks one of the three offer buttons. Looks up the exact
 * {@link OfferPlan} {@link EnchantPrepareListener} cached for this button, clears vanilla's own
 * chosen enchantment(s), and grants that plan's enchantments instead -- so what's granted always
 * matches what the offer showed. If no cached plan is found for some reason (a genuine edge case:
 * the offer view somehow never re-fired for this player), this fails open and leaves vanilla's
 * own choice untouched rather than granting nothing at all -- same "never crash, fail open"
 * discipline this whole plugin family uses everywhere.
 */
final class EnchantApplyListener implements Listener {

    private final ArcanumConfig config;
    private final OfferCache cache;

    EnchantApplyListener(ArcanumConfig config, OfferCache cache) {
        this.config = config;
        this.cache = cache;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        if (!config.enabled) {
            return;
        }
        Player player = event.getEnchanter();
        OfferPlan plan = cache.get(player, event.whichButton());
        if (plan == null || plan.isEmpty()) {
            return;
        }

        event.getEnchantsToAdd().clear();
        event.getEnchantsToAdd().putAll(plan.grants());
        event.setExpLevelCost(plan.cost());

        playFlourish(player, plan.tier());

        boolean gotCurse = plan.grantedEnchantments().stream().anyMatch(Enchantment::isCursed);
        if (gotCurse) {
            player.sendMessage("§8A cold chill runs through the ritual -- something dark rode along with that enchantment.");
        }
        player.sendMessage("§5The table resonates: §f" + plan.tier().colored() + "§f offer granted.");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player != null) {
            cache.clear(player);
        }
    }

    private void playFlourish(Player player, EnchantTier tier) {
        Location location = player.getLocation();
        if (location == null || location.getWorld() == null) {
            return;
        }
        Sound sound = tier.ordinal() >= EnchantTier.EPIC.ordinal()
                ? Sound.ENTITY_PLAYER_LEVELUP
                : Sound.BLOCK_ENCHANTMENT_TABLE_USE;
        Particle particle = tier.ordinal() >= EnchantTier.EPIC.ordinal() ? Particle.PORTAL : Particle.ANGRY_VILLAGER;
        location.getWorld().playSound(location, sound, 0.9f, 1.0f);
        location.getWorld().spawnParticle(particle, location, 12, 0.5, 0.7, 0.5);
    }
}
