package com.nexuscraft.nexusarcanum;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Bridges {@code PrepareItemEnchantEvent} (where the three {@link OfferPlan}s are computed and
 * shown) to {@code EnchantItemEvent} (where the player has clicked one and it's time to actually
 * grant it) -- real vanilla's own client recomputes offers on every inventory change, so this
 * always holds each player's most recently shown three plans, keyed by player. In-memory only, on
 * purpose: an enchanting-table offer that's still open when the server restarts isn't a state
 * worth persisting, same reasoning as NexusWands' FocusStore.
 */
final class OfferCache {

    private final Map<UUID, OfferPlan[]> plans = new HashMap<>();

    void put(Player player, OfferPlan[] slots) {
        plans.put(player.getUniqueId(), slots);
    }

    OfferPlan get(Player player, int slotIndex) {
        OfferPlan[] slots = plans.get(player.getUniqueId());
        if (slots == null || slotIndex < 0 || slotIndex >= slots.length) {
            return null;
        }
        return slots[slotIndex];
    }

    void clear(Player player) {
        plans.remove(player.getUniqueId());
    }
}
