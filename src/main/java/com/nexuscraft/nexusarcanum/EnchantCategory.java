package com.nexuscraft.nexusarcanum;

/** Which kind of item slot a real vanilla enchantment can land on. {@link EnchantCatalog} maps
 *  both a {@code Material} to the category (or categories -- a helmet is armor, nothing double-
 *  counts) it belongs to, and each {@code Enchantment} to which categories it's willing to land
 *  on, the same "config/catalog drives, code doesn't hardcode per-call" split this whole plugin
 *  family already uses everywhere else -- just as fixed catalog data here instead of config.yml,
 *  matching NexusWands' own precedent of keeping wood/core game-balance data fixed in code. */
enum EnchantCategory {
    SWORD,
    AXE,
    PICKAXE,
    SHOVEL,
    HOE,
    BOW,
    CROSSBOW,
    TRIDENT,
    FISHING_ROD,
    SHEARS,
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS,
    ELYTRA,
    /** A plain {@code Material.BOOK} placed in the table -- the vanilla "enchant a blank book
     *  into an enchanted book" path. Never {@code ENCHANTED_BOOK} itself: vanilla's table only
     *  ever accepts a plain book as input, the same way it never accepts an already-enchanted
     *  tool as input either. */
    BOOK,
}
