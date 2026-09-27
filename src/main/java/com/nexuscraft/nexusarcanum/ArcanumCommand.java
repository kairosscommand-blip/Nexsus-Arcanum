package com.nexuscraft.nexusarcanum;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.Locale;

/** {@code /nexusarcanum reload|inspect} -- reload is admin-only (double-checked here, not just
 *  left to plugin.yml's permission default, same convention this whole family follows); inspect
 *  is open to any player and shows the real resonance breakdown of whatever enchanting table
 *  they're looking straight at, the same ray-traced "what am I looking at" idiom NexusWands'
 *  TargetResolver already established. */
final class ArcanumCommand implements CommandExecutor {

    private static final double INSPECT_RANGE_BLOCKS = 6.0;

    private final ArcanumConfig config;
    private final ResonanceScanner scanner;
    private final Runnable reload;

    ArcanumCommand(ArcanumConfig config, ResonanceScanner scanner, Runnable reload) {
        this.config = config;
        this.scanner = scanner;
        this.reload = reload;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> handleReload(sender);
            case "inspect" -> handleInspect(sender);
            default -> sendUsage(sender);
        }
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§7Usage: /nexusarcanum <reload|inspect>");
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("nexusarcanum.admin")) {
            sender.sendMessage("§cYou don't have permission to do that.");
            return;
        }
        reload.run();
        sender.sendMessage("§aNexusArcanum reloaded.");
    }

    private void handleInspect(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly a player can inspect a table.");
            return;
        }
        Block table = resolveLookedAtTable(player);
        if (table == null) {
            player.sendMessage("§7Look straight at an enchanting table first.");
            return;
        }

        Resonance resonance = scanner.scan(table);
        player.sendMessage("§5=== Table Resonance ===");
        player.sendMessage("§7Bookshelves: §f" + resonance.shelfCount() + "§7/15");
        player.sendMessage("§7Chiseled bookshelf slots filled: §f" + resonance.chiseledFilledSlots());
        player.sendMessage("§7Symmetric direction pairs: §f" + resonance.symmetricPairs() + "§7/4");
        player.sendMessage("§7Total score: §f" + String.format(Locale.ROOT, "%.2f", resonance.score())
                + " §7(§f" + String.format(Locale.ROOT, "%.0f%%", resonance.clampedFraction() * 100)
                + "§7 of max)");
        player.sendMessage("§7Tier ceiling at full cost: §f" + config.tierCeilingFor(resonance.clampedFraction()).colored());
    }

    private Block resolveLookedAtTable(Player player) {
        Location eye = player.getEyeLocation();
        if (eye == null || eye.getWorld() == null) {
            return null;
        }
        RayTraceResult result = eye.getWorld().rayTraceBlocks(eye, eye.getDirection(), INSPECT_RANGE_BLOCKS);
        Block hit = result == null ? null : result.getHitBlock();
        return hit != null && hit.getType() == Material.ENCHANTING_TABLE ? hit : null;
    }
}
