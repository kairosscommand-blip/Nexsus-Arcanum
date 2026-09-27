package com.nexuscraft.nexusarcanum;

/**
 * NexusArcanum's own five-tier rarity scheme -- an original tiering, not a byte-for-byte clone of
 * vanilla's own internal enchantment weights (which this project was never able to verify against
 * a real jar anyway). {@link #maxSimultaneousGrants} is the "massive overhaul" part: a Common,
 * Uncommon, or Rare offer still nets exactly one enchantment like vanilla always has, but an Epic
 * offer can land two at once and a Mythic offer up to three, all still real vanilla
 * {@code Enchantment} objects and all still checked against {@link EnchantCatalog}'s conflict
 * rules so nothing mutually-exclusive ever lands together.
 */
enum EnchantTier {

    COMMON("Common", "§7", 1),
    UNCOMMON("Uncommon", "§a", 1),
    RARE("Rare", "§9", 1),
    EPIC("Epic", "§5", 2),
    MYTHIC("Mythic", "§6", 3),
    ;

    private final String displayName;
    private final String colorCode;
    private final int maxSimultaneousGrants;

    EnchantTier(String displayName, String colorCode, int maxSimultaneousGrants) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.maxSimultaneousGrants = maxSimultaneousGrants;
    }

    String displayName() {
        return displayName;
    }

    String colored() {
        return colorCode + displayName;
    }

    int maxSimultaneousGrants() {
        return maxSimultaneousGrants;
    }
}
