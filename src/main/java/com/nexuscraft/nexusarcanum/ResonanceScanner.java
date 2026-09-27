package com.nexuscraft.nexusarcanum;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ChiseledBookshelf;
import org.bukkit.inventory.ItemStack;

/**
 * Computes a table's total enchanting power from the real blocks actually surrounding it. Three
 * real, additive sources, each documented separately since each is a different kind of "real":
 *
 * <p><b>Shelf count</b> -- the same real vanilla ring vanilla's own enchanting table checks: the
 * 8 compass directions around the table, at 2 blocks out, across both the table's own level and
 * one block above, but only counted when the 1-block-inward gap on that same direction/level is
 * air (this is the well-documented "needs a walkway of air between the table and the shelf" rule
 * -- reproduced here as the commonly-described simplified shape of vanilla's real scan, not
 * decompiled source, so treat the exact position set as best-recollection like every other
 * game-data detail this stub tree flags). Capped at vanilla's own real ceiling of 15.
 *
 * <p><b>Chiseled bonus</b> -- a real, current (1.20+) block, {@code Material.CHISELED_BOOKSHELF},
 * that on a real server does NOT contribute to the vanilla table's own power at all (only plain
 * {@code Material.BOOKSHELF} does -- confirmed real vanilla behavior, not a guess). NexusArcanum
 * explicitly fills that gap: any chiseled bookshelf found at the same ring positions (same
 * air-gap rule) contributes based on how many of its real 6 slots hold something, via the real
 * {@code ChiseledBookshelf} block state's {@code Inventory}.
 *
 * <p><b>Symmetry bonus</b> -- original design, not a real vanilla mechanic: the four opposing
 * direction pairs (N/S, E/W, NE/SW, NW/SE) are compared, and a pair "matches" when both sides
 * contributed the same real shelf count. A fully round, deliberate build matches all four; a
 * lopsided pile of shelves in one corner matches none.
 */
final class ResonanceScanner {

    private static final int[][] DIRECTIONS = {
            {0, -1}, {0, 1}, {1, 0}, {-1, 0},
            {1, -1}, {-1, -1}, {1, 1}, {-1, 1},
    };
    /** Indices into {@link #DIRECTIONS} for the four opposing pairs used by the symmetry bonus. */
    private static final int[][] OPPOSING_PAIRS = {{0, 1}, {2, 3}, {4, 7}, {5, 6}};

    private final ArcanumConfig config;

    ResonanceScanner(ArcanumConfig config) {
        this.config = config;
    }

    Resonance scan(Block table) {
        int[] shelfCountByDirection = new int[DIRECTIONS.length];
        int chiseledFilledSlots = 0;

        for (int i = 0; i < DIRECTIONS.length; i++) {
            int dx = DIRECTIONS[i][0];
            int dz = DIRECTIONS[i][1];
            if (!isAirGap(table, dx, dz)) {
                continue;
            }
            for (int dy = 0; dy <= 1; dy++) {
                Block shelfBlock = table.getRelative(dx * 2, dy, dz * 2);
                Material type = shelfBlock.getType();
                if (type == Material.BOOKSHELF) {
                    shelfCountByDirection[i]++;
                } else if (type == Material.CHISELED_BOOKSHELF) {
                    chiseledFilledSlots += countFilledSlots(shelfBlock);
                }
            }
        }

        int rawShelfCount = 0;
        for (int count : shelfCountByDirection) {
            rawShelfCount += count;
        }
        int shelfCount = Math.min(15, rawShelfCount);

        int symmetricPairs = 0;
        for (int[] pair : OPPOSING_PAIRS) {
            if (shelfCountByDirection[pair[0]] == shelfCountByDirection[pair[1]]) {
                symmetricPairs++;
            }
        }

        double score = shelfCount
                + chiseledFilledSlots * config.chiseledSlotPoints
                + symmetricPairs * config.symmetryPairPoints;
        double fraction = config.maxResonanceScore > 0 ? score / config.maxResonanceScore : 0.0;

        return new Resonance(shelfCount, chiseledFilledSlots, symmetricPairs, score, fraction);
    }

    /** The real vanilla "walkway of air" requirement: both the table's own level and one above,
     *  one block in from the table on this direction, must be air for the shelf two blocks out
     *  on this direction to count at all. */
    private boolean isAirGap(Block table, int dx, int dz) {
        return table.getRelative(dx, 0, dz).getType() == Material.AIR
                && table.getRelative(dx, 1, dz).getType() == Material.AIR;
    }

    private int countFilledSlots(Block chiseledBookshelfBlock) {
        if (!(chiseledBookshelfBlock.getState() instanceof ChiseledBookshelf chiseled)) {
            return 0;
        }
        int filled = 0;
        // Real chiseled bookshelves have exactly 6 slots; this stub's generic Inventory doesn't
        // enforce that size, so this deliberately only ever looks at indices 0-5.
        for (int slot = 0; slot < 6; slot++) {
            ItemStack item = chiseled.getInventory().getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                filled++;
            }
        }
        return filled;
    }
}
