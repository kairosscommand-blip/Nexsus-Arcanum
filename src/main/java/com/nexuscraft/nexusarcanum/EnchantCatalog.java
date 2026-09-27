package com.nexuscraft.nexusarcanum;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Every real vanilla enchantment this plugin can offer, with three fixed, hand-authored facts
 * about each one: which {@link EnchantTier} it belongs to (this project's own original rarity
 * scheme, not vanilla's internal weights), which {@link EnchantCategory} item slots it's willing
 * to land on, and which other enchantments it can never share an item with. All three are fixed
 * catalog data, not config -- same "game-balance data lives in code, shared tunables live in
 * config.yml" split NexusWands already established for {@code WandWood}/{@code WandCore}.
 *
 * <p>Two real vanilla enchantments are deliberately included even though a plain vanilla table
 * never offers them at all ({@code Enchantment#isTreasure()} enchantments are normally
 * loot/trading/fishing-only): {@code MENDING} and the other five treasure enchantments. This is a
 * deliberate design choice for the "massive overhaul," not an oversight -- a sufficiently
 * resonant table (see {@link ResonanceScanner}) can offer a treasure enchantment at Epic/Mythic
 * tier, giving high-effort bookshelf arrangements a real, tangible payoff beyond "the same offers,
 * just cheaper." Both real curses ({@code VANISHING_CURSE}/{@code BINDING_CURSE}) are included
 * too, gated off by default via {@code ArcanumConfig#allowCurses}.
 */
final class EnchantCatalog {

    private static final Map<Enchantment, EnchantTier> TIERS = new HashMap<>();
    private static final Map<Enchantment, Set<EnchantCategory>> CATEGORIES = new HashMap<>();
    private static final Map<Enchantment, Set<Enchantment>> CONFLICTS = new HashMap<>();
    private static final Map<Material, EnchantCategory> MATERIAL_CATEGORY = new HashMap<>();

    private static final Set<EnchantCategory> ARMOR_AND_ELYTRA =
            EnumSet.of(EnchantCategory.HELMET, EnchantCategory.CHESTPLATE, EnchantCategory.LEGGINGS,
                    EnchantCategory.BOOTS, EnchantCategory.ELYTRA);
    private static final Set<EnchantCategory> TOOLS =
            EnumSet.of(EnchantCategory.PICKAXE, EnchantCategory.SHOVEL, EnchantCategory.AXE,
                    EnchantCategory.HOE, EnchantCategory.SHEARS);
    private static final Set<EnchantCategory> EVERYTHING = EnumSet.allOf(EnchantCategory.class);

    static {
        // -- Armor --
        tier(Enchantment.PROTECTION, EnchantTier.COMMON, ARMOR_AND_ELYTRA);
        tier(Enchantment.FIRE_PROTECTION, EnchantTier.COMMON, ARMOR_AND_ELYTRA);
        tier(Enchantment.BLAST_PROTECTION, EnchantTier.COMMON, ARMOR_AND_ELYTRA);
        tier(Enchantment.PROJECTILE_PROTECTION, EnchantTier.COMMON, ARMOR_AND_ELYTRA);
        tier(Enchantment.FEATHER_FALLING, EnchantTier.COMMON, EnumSet.of(EnchantCategory.BOOTS));
        tier(Enchantment.THORNS, EnchantTier.UNCOMMON, ARMOR_AND_ELYTRA);
        tier(Enchantment.RESPIRATION, EnchantTier.COMMON, EnumSet.of(EnchantCategory.HELMET));
        tier(Enchantment.AQUA_AFFINITY, EnchantTier.UNCOMMON, EnumSet.of(EnchantCategory.HELMET));
        tier(Enchantment.DEPTH_STRIDER, EnchantTier.UNCOMMON, EnumSet.of(EnchantCategory.BOOTS));
        tier(Enchantment.FROST_WALKER, EnchantTier.EPIC, EnumSet.of(EnchantCategory.BOOTS));
        tier(Enchantment.SOUL_SPEED, EnchantTier.EPIC, EnumSet.of(EnchantCategory.BOOTS));
        tier(Enchantment.SWIFT_SNEAK, EnchantTier.MYTHIC, EnumSet.of(EnchantCategory.LEGGINGS));

        // -- Weapon (sword only, matching real vanilla -- axes don't take these at the table) --
        tier(Enchantment.SHARPNESS, EnchantTier.COMMON, EnumSet.of(EnchantCategory.SWORD));
        tier(Enchantment.SMITE, EnchantTier.COMMON, EnumSet.of(EnchantCategory.SWORD));
        tier(Enchantment.BANE_OF_ARTHROPODS, EnchantTier.COMMON, EnumSet.of(EnchantCategory.SWORD));
        tier(Enchantment.KNOCKBACK, EnchantTier.COMMON, EnumSet.of(EnchantCategory.SWORD));
        tier(Enchantment.FIRE_ASPECT, EnchantTier.UNCOMMON, EnumSet.of(EnchantCategory.SWORD));
        tier(Enchantment.LOOTING, EnchantTier.RARE, EnumSet.of(EnchantCategory.SWORD));
        tier(Enchantment.SWEEPING_EDGE, EnchantTier.UNCOMMON, EnumSet.of(EnchantCategory.SWORD));

        // -- Tools --
        tier(Enchantment.EFFICIENCY, EnchantTier.COMMON, TOOLS);
        tier(Enchantment.SILK_TOUCH, EnchantTier.RARE, TOOLS);
        tier(Enchantment.FORTUNE, EnchantTier.RARE, TOOLS);
        // Unbreaking applies far more broadly in real vanilla than just tools -- weapons, armor,
        // bows, tridents, crossbows, fishing rods, elytra all take it too.
        tier(Enchantment.UNBREAKING, EnchantTier.COMMON, EVERYTHING);

        // -- Bow --
        tier(Enchantment.POWER, EnchantTier.COMMON, EnumSet.of(EnchantCategory.BOW));
        tier(Enchantment.PUNCH, EnchantTier.UNCOMMON, EnumSet.of(EnchantCategory.BOW));
        tier(Enchantment.FLAME, EnchantTier.UNCOMMON, EnumSet.of(EnchantCategory.BOW));
        tier(Enchantment.INFINITY, EnchantTier.RARE, EnumSet.of(EnchantCategory.BOW));

        // -- Fishing rod --
        tier(Enchantment.LUCK_OF_THE_SEA, EnchantTier.COMMON, EnumSet.of(EnchantCategory.FISHING_ROD));
        tier(Enchantment.LURE, EnchantTier.UNCOMMON, EnumSet.of(EnchantCategory.FISHING_ROD));

        // -- Trident --
        tier(Enchantment.LOYALTY, EnchantTier.COMMON, EnumSet.of(EnchantCategory.TRIDENT));
        tier(Enchantment.IMPALING, EnchantTier.UNCOMMON, EnumSet.of(EnchantCategory.TRIDENT));
        tier(Enchantment.RIPTIDE, EnchantTier.RARE, EnumSet.of(EnchantCategory.TRIDENT));
        tier(Enchantment.CHANNELING, EnchantTier.RARE, EnumSet.of(EnchantCategory.TRIDENT));

        // -- Crossbow --
        tier(Enchantment.MULTISHOT, EnchantTier.RARE, EnumSet.of(EnchantCategory.CROSSBOW));
        tier(Enchantment.PIERCING, EnchantTier.UNCOMMON, EnumSet.of(EnchantCategory.CROSSBOW));
        tier(Enchantment.QUICK_CHARGE, EnchantTier.UNCOMMON, EnumSet.of(EnchantCategory.CROSSBOW));

        // -- Universal treasure --
        tier(Enchantment.MENDING, EnchantTier.EPIC, EVERYTHING);

        // -- Curses (config-gated at ArcanumConfig#allowCurses) --
        tier(Enchantment.VANISHING_CURSE, EnchantTier.MYTHIC, EVERYTHING);
        tier(Enchantment.BINDING_CURSE, EnchantTier.MYTHIC, ARMOR_AND_ELYTRA);

        // -- Conflicts (explicit pairs, not pure grouping -- Riptide excludes both Loyalty and
        //    Channeling, but Loyalty and Channeling don't conflict with *each other* in real
        //    vanilla, so this can't be modeled as "everyone in one bucket excludes everyone else"). --
        conflict(Enchantment.SHARPNESS, Enchantment.SMITE);
        conflict(Enchantment.SHARPNESS, Enchantment.BANE_OF_ARTHROPODS);
        conflict(Enchantment.SMITE, Enchantment.BANE_OF_ARTHROPODS);
        conflict(Enchantment.PROTECTION, Enchantment.FIRE_PROTECTION);
        conflict(Enchantment.PROTECTION, Enchantment.BLAST_PROTECTION);
        conflict(Enchantment.PROTECTION, Enchantment.PROJECTILE_PROTECTION);
        conflict(Enchantment.FIRE_PROTECTION, Enchantment.BLAST_PROTECTION);
        conflict(Enchantment.FIRE_PROTECTION, Enchantment.PROJECTILE_PROTECTION);
        conflict(Enchantment.BLAST_PROTECTION, Enchantment.PROJECTILE_PROTECTION);
        conflict(Enchantment.DEPTH_STRIDER, Enchantment.FROST_WALKER);
        conflict(Enchantment.FORTUNE, Enchantment.SILK_TOUCH);
        conflict(Enchantment.INFINITY, Enchantment.MENDING);
        conflict(Enchantment.RIPTIDE, Enchantment.LOYALTY);
        conflict(Enchantment.RIPTIDE, Enchantment.CHANNELING);
        conflict(Enchantment.MULTISHOT, Enchantment.PIERCING);

        // -- Material -> category --
        category(EnchantCategory.SWORD, Material.WOODEN_SWORD, Material.STONE_SWORD, Material.GOLDEN_SWORD,
                Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD);
        category(EnchantCategory.AXE, Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE,
                Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE);
        category(EnchantCategory.PICKAXE, Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE,
                Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE);
        category(EnchantCategory.SHOVEL, Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL,
                Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL);
        category(EnchantCategory.HOE, Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE,
                Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE);
        category(EnchantCategory.BOW, Material.BOW);
        category(EnchantCategory.CROSSBOW, Material.CROSSBOW);
        category(EnchantCategory.TRIDENT, Material.TRIDENT);
        category(EnchantCategory.FISHING_ROD, Material.FISHING_ROD);
        category(EnchantCategory.SHEARS, Material.SHEARS);
        category(EnchantCategory.HELMET, Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET,
                Material.IRON_HELMET, Material.GOLDEN_HELMET, Material.DIAMOND_HELMET,
                Material.NETHERITE_HELMET, Material.TURTLE_HELMET);
        category(EnchantCategory.CHESTPLATE, Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE,
                Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.DIAMOND_CHESTPLATE,
                Material.NETHERITE_CHESTPLATE);
        category(EnchantCategory.LEGGINGS, Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS,
                Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.DIAMOND_LEGGINGS,
                Material.NETHERITE_LEGGINGS);
        category(EnchantCategory.BOOTS, Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS,
                Material.IRON_BOOTS, Material.GOLDEN_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS);
        category(EnchantCategory.ELYTRA, Material.ELYTRA);
        category(EnchantCategory.BOOK, Material.BOOK);
    }

    private EnchantCatalog() {
    }

    private static void tier(Enchantment enchantment, EnchantTier tier, Set<EnchantCategory> categories) {
        TIERS.put(enchantment, tier);
        CATEGORIES.put(enchantment, categories);
    }

    private static void conflict(Enchantment a, Enchantment b) {
        CONFLICTS.computeIfAbsent(a, k -> new HashSet<>()).add(b);
        CONFLICTS.computeIfAbsent(b, k -> new HashSet<>()).add(a);
    }

    private static void category(EnchantCategory category, Material... materials) {
        for (Material material : materials) {
            MATERIAL_CATEGORY.put(material, category);
        }
    }

    static EnchantTier tierOf(Enchantment enchantment) {
        return TIERS.getOrDefault(enchantment, EnchantTier.COMMON);
    }

    static Set<Enchantment> allEnchantments() {
        return TIERS.keySet();
    }

    static List<Enchantment> byTier(EnchantTier tier) {
        return TIERS.entrySet().stream()
                .filter(e -> e.getValue() == tier)
                .map(Map.Entry::getKey)
                .toList();
    }

    /** Null if this material isn't a real enchantable item this catalog knows about. */
    static EnchantCategory categoryOf(Material material) {
        return MATERIAL_CATEGORY.get(material);
    }

    static boolean appliesToCategory(Enchantment enchantment, EnchantCategory category) {
        Set<EnchantCategory> categories = CATEGORIES.get(enchantment);
        return categories != null && categories.contains(category);
    }

    static boolean conflictsWith(Enchantment candidate, Set<Enchantment> alreadyChosen) {
        Set<Enchantment> candidateConflicts = CONFLICTS.get(candidate);
        if (candidateConflicts == null) {
            return false;
        }
        for (Enchantment chosen : alreadyChosen) {
            if (candidateConflicts.contains(chosen)) {
                return true;
            }
        }
        return false;
    }
}
