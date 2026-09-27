package com.nexuscraft.nexusarcanum;

/** One table's computed enchanting power, broken down into where each point came from --
 *  surfaced as-is by {@code /nexusarcanum inspect} so a player can see exactly what to add next,
 *  rather than a single opaque number. See {@link ResonanceScanner}'s own comment for how each
 *  field is computed. */
record Resonance(int shelfCount, int chiseledFilledSlots, int symmetricPairs, double score, double fraction) {

    double clampedFraction() {
        return Math.max(0.0, Math.min(1.0, fraction));
    }
}
