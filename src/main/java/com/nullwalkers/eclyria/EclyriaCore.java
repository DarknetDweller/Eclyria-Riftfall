package com.nullwalkers.eclyria;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.Chunk;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Entity;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class EclyriaCore extends JavaPlugin implements CommandExecutor, TabCompleter, Listener {
    private Connection connection;
    private NamespacedKey itemKey;
    private NamespacedKey mobKey;
    private NamespacedKey collectionKey;
    private NamespacedKey abilityKey;
    private final Map<UUID, Long> abilityCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> manaRegenTick = new ConcurrentHashMap<>();
    private final Set<UUID> priestAccess = ConcurrentHashMap.newKeySet();
    private final Map<UUID, String> storageOwners = new ConcurrentHashMap<>();
    private final Map<UUID, String> activeRifts = new ConcurrentHashMap<>();
    private final Map<UUID, Long> ambienceCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> itemBrowserPages = new ConcurrentHashMap<>();
    private final Set<String> classes = Set.of("vanguard", "arcanist", "ranger", "luminary");
    private final Map<String, String> itemMaterials = Map.ofEntries(
            Map.entry("rift_shard", "AMETHYST_SHARD"),
            Map.entry("aetherite", "PRISMARINE_CRYSTALS"),
            Map.entry("voidglass", "ECHO_SHARD"),
            Map.entry("rift_ingot", "PLAYER_HEAD"),
            Map.entry("rift_helm", "PLAYER_HEAD"),
            Map.entry("verdant_core", "PLAYER_HEAD"),
            Map.entry("aether_blade", "NETHERITE_SWORD"),
            Map.entry("rift_bow", "BOW"),
            Map.entry("healing_tonic", "HONEY_BOTTLE"),
            Map.entry("stormcaller_staff", "BLAZE_ROD"),
            Map.entry("glacier_orb", "SNOWBALL"),
            Map.entry("emberheart", "FIRE_CHARGE"),
            Map.entry("thornlash_whip", "VINE"),
            Map.entry("shadowstep_dagger", "IRON_SWORD"),
            Map.entry("sunfire_charm", "GLOWSTONE_DUST"),
            Map.entry("tidal_scepter", "TRIDENT"),
            Map.entry("soul_anchor", "SOUL_LANTERN"),
            Map.entry("gravity_maul", "ANVIL"),
            Map.entry("phoenix_feather", "FEATHER"),
            Map.entry("frostbite_blade", "DIAMOND_SWORD"),
            Map.entry("arcane_focus", "AMETHYST_SHARD"),
            Map.entry("void_reaper", "NETHERITE_HOE"),
            Map.entry("verdant_buckler", "SHIELD"),
            Map.entry("thunder_rune", "LIGHTNING_ROD"),
            Map.entry("bloodmoon_amulet", "REDSTONE"),
            Map.entry("astral_compass", "RECOVERY_COMPASS"),
            Map.entry("riftsurge_crystal", "END_CRYSTAL"),
            Map.entry("echoing_horn", "GOAT_HORN"),
            Map.entry("crystal_bomb", "FIREWORK_ROCKET"),
            Map.entry("warding_totem", "TOTEM_OF_UNDYING"),
            Map.entry("mana_flask", "HONEY_BOTTLE"),
            Map.entry("starfall_lance", "NETHERITE_AXE"),
            Map.entry("mirror_shard", "GLASS_PANE"),
            Map.entry("runic_gauntlet", "GOLDEN_CARROT"),
            Map.entry("dreamweaver", "PHANTOM_MEMBRANE"),
            Map.entry("abyssal_crown", "PLAYER_HEAD"),
            Map.entry("verdantite_ore", "EMERALD_ORE"),
            Map.entry("verdantite_ingot", "PLAYER_HEAD"),
            Map.entry("emberstone_ore", "NETHER_GOLD_ORE"),
            Map.entry("emberstone_ingot", "PLAYER_HEAD"),
            Map.entry("voidsilver_ore", "LAPIS_ORE"),
            Map.entry("voidsilver_ingot", "PLAYER_HEAD"),
            Map.entry("stormsteel_ore", "COPPER_ORE"),
            Map.entry("stormsteel_ingot", "PLAYER_HEAD"),
            Map.entry("frostquartz_ore", "DIAMOND_ORE"),
            Map.entry("frostquartz_ingot", "PLAYER_HEAD"),
            Map.entry("soulcopper_ore", "REDSTONE_ORE"),
            Map.entry("soulcopper_ingot", "PLAYER_HEAD"),
            Map.entry("suniron_ore", "GOLD_ORE"),
            Map.entry("suniron_ingot", "PLAYER_HEAD"),
            Map.entry("duskcoal_ore", "COAL_ORE"),
            Map.entry("duskcoal_ingot", "PLAYER_HEAD"),
            Map.entry("aetherium_ore", "IRON_ORE"),
            Map.entry("aetherium_ingot", "PLAYER_HEAD"),
            Map.entry("riftglass_ore", "NETHER_QUARTZ_ORE"),
            Map.entry("riftglass_ingot", "PLAYER_HEAD")
    );

    private static final Map<String, Integer> ABILITY_COSTS = Map.ofEntries(
        Map.entry("stormcaller_staff",24),Map.entry("glacier_orb",18),Map.entry("emberheart",22),Map.entry("thornlash_whip",16),
        Map.entry("shadowstep_dagger",20),Map.entry("sunfire_charm",20),Map.entry("tidal_scepter",18),Map.entry("soul_anchor",25),
        Map.entry("gravity_maul",28),Map.entry("phoenix_feather",30),Map.entry("frostbite_blade",16),Map.entry("arcane_focus",12),
        Map.entry("void_reaper",26),Map.entry("verdant_buckler",18),Map.entry("thunder_rune",20),Map.entry("bloodmoon_amulet",24),
        Map.entry("astral_compass",14),Map.entry("riftsurge_crystal",32),Map.entry("echoing_horn",18),Map.entry("crystal_bomb",22),
        Map.entry("warding_totem",28),Map.entry("starfall_lance",30),Map.entry("mirror_shard",20),Map.entry("runic_gauntlet",18),
        Map.entry("dreamweaver",24),Map.entry("abyssal_crown",35));

    private static final Map<Material, String> RIFT_ORES = Map.ofEntries(
        Map.entry(Material.EMERALD_ORE, "verdantite_ore"),
        Map.entry(Material.NETHER_GOLD_ORE, "emberstone_ore"),
        Map.entry(Material.LAPIS_ORE, "voidsilver_ore"),
        Map.entry(Material.COPPER_ORE, "stormsteel_ore"),
        Map.entry(Material.DIAMOND_ORE, "frostquartz_ore"),
        Map.entry(Material.REDSTONE_ORE, "soulcopper_ore"),
        Map.entry(Material.GOLD_ORE, "suniron_ore"),
        Map.entry(Material.COAL_ORE, "duskcoal_ore"),
        Map.entry(Material.IRON_ORE, "aetherium_ore"),
        Map.entry(Material.NETHER_QUARTZ_ORE, "riftglass_ore")
    );
    private static final Map<String, String> ORE_REGIONS = Map.ofEntries(
        Map.entry("verdantite_ore", "verdant"),
        Map.entry("emberstone_ore", "ember"),
        Map.entry("voidsilver_ore", "void"),
        Map.entry("stormsteel_ore", "ember"),
        Map.entry("frostquartz_ore", "void"),
        Map.entry("soulcopper_ore", "ember"),
        Map.entry("suniron_ore", "ember"),
        Map.entry("duskcoal_ore", "verdant"),
        Map.entry("aetherium_ore", "void"),
        Map.entry("riftglass_ore", "void")
    );

    @Override
    public org.bukkit.generator.ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        String theme = id;
        if (theme == null || theme.isBlank()) {
            String lower = worldName == null ? "" : worldName.toLowerCase(Locale.ROOT);
            if (lower.contains("ember")) theme = "ember";
            else if (lower.contains("void")) theme = "void";
            else theme = "verdant";
        }
        return new RiftChunkGenerator(theme);
    }

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("Multiverse-Core") == null
                || !Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core")) {
            getLogger().severe("Multiverse-Core is required. Install and enable it before Eclyria: Riftfall.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        saveDefaultConfig();
        saveResource("messages.yml", false);
        itemKey = new NamespacedKey(this, "item_id");
        mobKey = new NamespacedKey(this, "mob_id");
        collectionKey = new NamespacedKey(this, "collection_id");
        abilityKey = new NamespacedKey(this, "ability_id");
        try {
            openDatabase();
        } catch (SQLException ex) {
            getLogger().severe("Could not open SQLite database: " + ex.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Objects.requireNonNull(getCommand("eclyria")).setExecutor(this);
        Objects.requireNonNull(getCommand("eclyria")).setTabCompleter(this);
        getServer().getPluginManager().registerEvents(this, this);
        setupWorld();
        registerRecipes();
        Bukkit.getScheduler().runTaskTimer(this, this::regenerateMana, 20L, 20L);
        Bukkit.getScheduler().runTaskTimer(this, this::playRiftAmbience, 100L, 100L);
        getLogger().info("Eclyria: Riftfall enabled. RPG menus, regions, collections and crafting are ready.");
    }

    @Override
    public void onDisable() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException ex) {
            getLogger().warning("Could not close database cleanly: " + ex.getMessage());
        }
    }

    private void openDatabase() throws SQLException {
        File dir = getDataFolder();
        if (!dir.exists() && !dir.mkdirs()) throw new SQLException("Could not create plugin data directory");
        connection = DriverManager.getConnection("jdbc:sqlite:" + new File(dir, "eclyria.db").getAbsolutePath());
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS profiles (
                  uuid TEXT PRIMARY KEY,
                  player_name TEXT NOT NULL,
                  class_id TEXT NOT NULL DEFAULT 'vanguard',
                  level INTEGER NOT NULL DEFAULT 1,
                  xp INTEGER NOT NULL DEFAULT 0,
                  updated_at INTEGER NOT NULL
                )
                """);
        }
    }

    private synchronized void ensureProfile(OfflinePlayer player) throws SQLException {
        String name = player.getName() == null ? player.getUniqueId().toString() : player.getName();
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR IGNORE INTO profiles(uuid, player_name, class_id, level, xp, updated_at) VALUES(?, ?, 'vanguard', 1, 0, ?)")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, name);
            ps.setLong(3, System.currentTimeMillis());
            ps.executeUpdate();
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE profiles SET player_name=?, updated_at=? WHERE uuid=?")) {
            ps.setString(1, name);
            ps.setLong(2, System.currentTimeMillis());
            ps.setString(3, player.getUniqueId().toString());
            ps.executeUpdate();
        }
    }

    private synchronized Profile getProfile(OfflinePlayer player) throws SQLException {
        ensureProfile(player);
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT player_name, class_id, level, xp FROM profiles WHERE uuid=?")) {
            ps.setString(1, player.getUniqueId().toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Profile missing after insert");
                return new Profile(rs.getString("player_name"), rs.getString("class_id"),
                        rs.getInt("level"), rs.getLong("xp"));
            }
        }
    }

    private synchronized void setClass(OfflinePlayer player, String classId) throws SQLException {
        ensureProfile(player);
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE profiles SET class_id=?, updated_at=? WHERE uuid=?")) {
            ps.setString(1, classId);
            ps.setLong(2, System.currentTimeMillis());
            ps.setString(3, player.getUniqueId().toString());
            ps.executeUpdate();
        }
    }

    private synchronized void addXp(OfflinePlayer player, long amount) throws SQLException {
        ensureProfile(player);
        Profile p = getProfile(player);
        long total = Math.max(0, p.xp + amount);
        int level = 1;
        long remaining = total;
        while (level < 100 && remaining >= xpForNextLevel(level)) {
            remaining -= xpForNextLevel(level);
            level++;
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE profiles SET xp=?, level=?, updated_at=? WHERE uuid=?")) {
            ps.setLong(1, total);
            ps.setInt(2, level);
            ps.setLong(3, System.currentTimeMillis());
            ps.setString(4, player.getUniqueId().toString());
            ps.executeUpdate();
        }
    }

    private long xpForNextLevel(int level) {
        return 100L + (level - 1L) * 50L;
    }

    private record Profile(String name, String classId, int level, long xp) {}

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private void tell(CommandSender sender, String message) {
        sender.sendMessage(color("&8[&5Eclyria&8] &r" + message));
    }

    private boolean isAdmin(CommandSender sender) {
        if (sender.hasPermission("eclyria.admin")) return true;
        tell(sender, "&cYou need eclyria.admin permission.");
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 && sender instanceof Player player) {
            if (!player.hasPermission("eclyria.use")) return deny(sender);
            openMainMenu(player);
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            tell(sender, "&dECLYRIA: RIFTFALL &7Alpha 2 &8| &fCreator: &bDarknet_Dweller &8| &fTeam: &bNullWalkers");
            tell(sender, "&f/eclyria profile [player] &7- View an RPG profile");
            tell(sender, "&f/eclyria class <class> &7- Select your class");
            tell(sender, "&f/eclyria xp <player> <amount> &7- Admin: add/remove XP");
            tell(sender, "&f/eclyria item give <player> <id> [amount] &7- Admin: give a resource");
            tell(sender, "&f/eclyria rift start <tier> &7- Admin: start test rift state");
            tell(sender, "&f/eclyria rift stop &7- Admin: stop test rift state");
            tell(sender, "&f/eclyria rift travel <verdant|ember|void> &7- Travel to a custom rift");
            tell(sender, "&f/eclyria gui &7- Open the Eclyria menu");
            tell(sender, "&f/eclyria priest &7- Admin: place the Divine Priest NPC");
            tell(sender, "&f/eclyria mana &7- View mana and Divine Revelation");
            tell(sender, "&f/eclyria dialogue revelation &7- Open conditional priest dialogue");
            tell(sender, "&f/eclyria item give <player> <id> [amount] &7- Give an RPG item");
            tell(sender, "&f/eclyria world setup &7- Generate three custom rifts and ore veins");
            tell(sender, "&f/eclyria forge &7- Open Rift Forge (or right-click a Smithing Table)");
            tell(sender, "&f/eclyria items &7- Admin item browser");
            tell(sender, "&f/eclyria mob list|spawn <id> [player] &7- Mob testing (admin)");
            tell(sender, "&f/eclyria world info|setup|setspawn <region> &7- World tools");
            tell(sender, "&f/eclyria debug | reload &7- Diagnostics and config reload");
            return true;
        }
        try {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "items" -> {
                    if (!(sender instanceof Player player)) { tell(sender,"&cPlayers only."); return true; }
                    if (!isAdmin(sender)) return true;
                    openItemBrowser(player);
                }
                case "forge" -> {
                    if (!(sender instanceof Player player)) { tell(sender,"&cPlayers only."); return true; }
                    openRiftForge(player);
                }
                case "gui" -> {
                    if (!(sender instanceof Player player)) {
                        tell(sender, "&cThis command can only be used in-game.");
                        return true;
                    }
                    if (!player.hasPermission("eclyria.use")) return deny(sender);
                    openMainMenu(player);
                }
                case "priest" -> {
                    if (!isAdmin(sender)) return true;
                    if (!(sender instanceof Player player)) { tell(sender,"&cRun this command in game."); return true; }
                    Villager priest = (Villager) player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
                    priest.setCustomName("Divine Priest"); priest.setCustomNameVisible(true); priest.setProfession(Villager.Profession.CLERIC); priest.setInvulnerable(true); priest.setAI(false); priest.setPersistent(true);
                    tell(player,"&aDivine Priest placed. Players can right-click him to open Divine Revelation.");
                }
                case "mana" -> {
                    if (!(sender instanceof Player player)) { tell(sender,"&cPlayers only."); return true; }
                    openDivineMenu(player);
                }
                case "dialogue" -> {
                    if (!(sender instanceof Player player)) { tell(sender, "&cThis dialogue can only be opened by a player."); return true; }
                    if (args.length < 2) { tell(sender, "&cUsage: /eclyria dialogue revelation"); return true; }
                    String dialogueId = args[1].toLowerCase(Locale.ROOT);
                    if (dialogueId.equals("revelation") || dialogueId.equals("priest")) {
                        String path = "divine." + player.getUniqueId();
                        if (getConfig().getInt(path + ".mana-level", 0) > 0) {
                            tell(player, "&dPriest: &fYour revelation is already awakened, " + player.getName() + ".");
                            openDivineMenu(player);
                            return true;
                        }
                        priestAccess.add(player.getUniqueId());
                        tell(player, "&dDivine Priest: &fYour soul stands before the Rift. Offer one Diamond Block to receive your revelation.");
                        openDivineMenu(player);
                        return true;
                    }
                    tell(player, "&cUnknown dialogue. Available: revelation");
                }
                case "mob" -> {
                    if (!isAdmin(sender)) return true;
                    if (args.length == 2 && args[1].equalsIgnoreCase("list")) {
                        tell(sender, "&dEclyria mobs: &friftling, rift_stalker, verdant_sentinel, rift_guardian, void_wraith, crystal_brute, ash_hound, aether_mage");
                        return true;
                    }
                    if (args.length >= 3 && args[1].equalsIgnoreCase("spawn")) {
                        String id = args[2].toLowerCase(Locale.ROOT);
                        Player target;
                        if (args.length >= 4) target = Bukkit.getPlayerExact(args[3]);
                        else target = sender instanceof Player p ? p : null;
                        if (target == null) {
                            tell(sender, "&cSpecify an online player: /eclyria mob spawn <id> <player>");
                            return true;
                        }
                        if (!spawnEclyriaMob(id, target.getLocation(), target)) {
                            tell(sender, "&cUnknown mob. Use /eclyria mob list.");
                            return true;
                        }
                        tell(sender, "&aSpawned &f" + title(id.replace('_', ' ')) + " &anear &f" + target.getName() + "&a.");
                        return true;
                    }
                    tell(sender, "&cUsage: /eclyria mob list | spawn <id> [player]");
                }
                case "world" -> {
                    if (!isAdmin(sender)) return true;
                    if (args.length == 1 || args[1].equalsIgnoreCase("info")) {
                        World world = getRiftfallWorld();
                        tell(sender, "&aHub: &f" + (world == null ? "not loaded" : world.getName()));
                        for (String key : List.of("verdant", "ember", "void")) {
                            World riftWorld = Bukkit.getWorld(riftWorldName(key));
                            tell(sender, "&7" + title(key) + " Rift: &f" + (riftWorld == null ? "not loaded" : riftWorld.getName()));
                        }
                    } else if (args[1].equalsIgnoreCase("setup")) {
                        setupWorld();
                        tell(sender, "&aChecked the hub and Multiverse-managed Rift worlds. Newly created Rift worlds use the Eclyria procedural terrain generator.");
                    } else if (args[1].equalsIgnoreCase("regenerate-rifts")) {
                        tell(sender, "&cAutomatic regeneration is disabled to protect builds. Back up a Rift world and regenerate it through Multiverse-Core only when you intend to replace that world's terrain.");
                    } else if (args[1].equalsIgnoreCase("setspawn") && args.length == 3 && sender instanceof Player player) {
                        String region = args[2].toLowerCase(Locale.ROOT);
                        if (!Set.of("havenfall", "verdant", "ember", "void", "riftlands").contains(region)) {
                            tell(sender, "&cRegions: havenfall, verdant, ember, void, riftlands");
                            return true;
                        }
                        Location loc = player.getLocation();
                        String path = "regions." + region + ".spawn";
                        getConfig().set(path + ".world", loc.getWorld().getName());
                        getConfig().set(path + ".x", loc.getX());
                        getConfig().set(path + ".y", loc.getY());
                        getConfig().set(path + ".z", loc.getZ());
                        getConfig().set(path + ".yaw", loc.getYaw());
                        getConfig().set(path + ".pitch", loc.getPitch());
                        if (Set.of("verdant", "ember", "void").contains(region)) {
                            String riftPath = "regions.rifts." + region + ".spawn";
                            getConfig().set(riftPath + ".world", loc.getWorld().getName());
                            getConfig().set(riftPath + ".x", loc.getX());
                            getConfig().set(riftPath + ".y", loc.getY());
                            getConfig().set(riftPath + ".z", loc.getZ());
                            getConfig().set(riftPath + ".yaw", loc.getYaw());
                            getConfig().set(riftPath + ".pitch", loc.getPitch());
                        }
                        saveConfig();
                        tell(sender, "&aSaved &f" + title(region) + "&a spawn at your current location.");
                    } else {
                        tell(sender, "&cUsage: /eclyria world info | setup | regenerate-rifts | setspawn <havenfall|verdant|ember|void>");
                    }
                }
                case "profile" -> {
                    if (!sender.hasPermission("eclyria.use")) return deny(sender);
                    OfflinePlayer target;
                    if (args.length >= 2) {
                        target = Bukkit.getOfflinePlayer(args[1]);
                    } else if (sender instanceof Player p) {
                        target = p;
                    } else {
                        tell(sender, "&cConsole usage: /eclyria profile <player>");
                        return true;
                    }
                    Profile profile = getProfile(target);
                    tell(sender, "&dProfile: &f" + profile.name + " &8| &dClass: &f" + title(profile.classId)
                            + " &8| &dLevel: &f" + profile.level + " &8| &dXP: &f" + profile.xp);
                }
                case "class" -> {
                    if (!(sender instanceof Player player)) {
                        tell(sender, "&cOnly players can select their own class.");
                        return true;
                    }
                    if (args.length != 2 || !classes.contains(args[1].toLowerCase(Locale.ROOT))) {
                        tell(sender, "&cChoose: vanguard, arcanist, ranger, luminary");
                        return true;
                    }
                    setClass(player, args[1].toLowerCase(Locale.ROOT));
                    tell(sender, "&aYour class is now &f" + title(args[1]) + "&a.");
                }
                case "xp" -> {
                    if (!isAdmin(sender)) return true;
                    if (args.length != 3) {
                        tell(sender, "&cUsage: /eclyria xp <player> <amount>");
                        return true;
                    }
                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                    long amount = Long.parseLong(args[2]);
                    if (Math.abs(amount) > 1_000_000_000L) {
                        tell(sender, "&cAmount is too large.");
                        return true;
                    }
                    addXp(target, amount);
                    Profile p = getProfile(target);
                    tell(sender, "&aUpdated &f" + p.name + "&a: level &f" + p.level + "&a, total XP &f" + p.xp);
                }
                case "item" -> {
                    if (!isAdmin(sender)) return true;
                    if (args.length < 4 || !args[1].equalsIgnoreCase("give")) {
                        tell(sender, "&cUsage: /eclyria item give <player> <rift_shard|aetherite|voidglass> [amount]");
                        return true;
                    }
                    Player target = Bukkit.getPlayerExact(args[2]);
                    if (target == null) {
                        tell(sender, "&cThat player must be online.");
                        return true;
                    }
                    String id = args[3].toLowerCase(Locale.ROOT);
                    if (!itemMaterials.containsKey(id)) {
                        tell(sender, "&cUnknown item. Available: rift_shard, aetherite, voidglass");
                        return true;
                    }
                    int amount = args.length >= 5 ? Integer.parseInt(args[4]) : 1;
                    if (amount < 1 || amount > 64) {
                        tell(sender, "&cAmount must be between 1 and 64.");
                        return true;
                    }
                    ItemStack item = createRpgItem(id, amount);
                    if (item == null) {
                        tell(sender, "&cUnknown or invalid item id. Use /eclyria items.");
                        return true;
                    }
                    Map<Integer, ItemStack> overflow = target.getInventory().addItem(item);
                    overflow.values().forEach(left -> target.getWorld().dropItemNaturally(target.getLocation(), left));
                    tell(sender, "&aGave &f" + amount + "x " + id + " &ato &f" + target.getName() + "&a.");
                }
                case "rift" -> {
                    if (args.length == 3 && args[1].equalsIgnoreCase("travel")) {
                        if (!(sender instanceof Player player)) { tell(sender, "&cPlayers only."); return true; }
                        String target = args[2].toLowerCase(Locale.ROOT);
                        String key = switch (target) {
                            case "verdant", "1" -> "verdant";
                            case "ember", "ashen", "2" -> "ember";
                            case "void", "3" -> "void";
                            default -> "";
                        };
                        if (key.isEmpty()) { tell(sender, "&cUse /eclyria rift travel <verdant|ember|void>"); return true; }
                        Location destination = readSpawn("rifts." + key);
                        if (destination == null || destination.getWorld() == null) { tell(sender, "&cRifts are not set up yet. Ask an admin to run /eclyria world setup."); return true; }
                        player.teleport(destination);
                        tell(player, "&5Entering the " + title(key) + " Rift...");
                    } else if (args.length == 3 && args[1].equalsIgnoreCase("start")) {
                        if (!isAdmin(sender)) return true;
                        if (!(sender instanceof Player player)) {
                            tell(sender, "&cRun this command in-game to start a test rift.");
                            return true;
                        }
                        String tier = args[2].toLowerCase(Locale.ROOT);
                        if (!Set.of("1", "2", "3", "4", "5", "easy", "normal", "hard").contains(tier)) {
                            tell(sender, "&cTier options: 1-5, easy, normal, hard");
                            return true;
                        }
                        activeRifts.put(player.getUniqueId(), tier);
                        tell(sender, "&5Test rift state started &7(tier " + tier + "). &cRift dungeons are outside the Alpha 2 GUI/world scope.");
                    } else if (args.length == 2 && args[1].equalsIgnoreCase("stop")) {
                        if (!isAdmin(sender)) return true;
                        if (sender instanceof Player player) activeRifts.remove(player.getUniqueId());
                        else activeRifts.clear();
                        tell(sender, "&aTest rift state stopped.");
                    } else {
                        tell(sender, "&cUsage: /eclyria rift travel <verdant|ember|void> | /eclyria rift start <tier> | /eclyria rift stop");
                    }
                }
                case "debug" -> {
                    if (!isAdmin(sender)) return true;
                    tell(sender, "&aVersion: &f" + getDescription().getVersion() + " &8| &aOnline: &f" + Bukkit.getOnlinePlayers().size());
                    tell(sender, "&aDatabase: &f" + (connection != null && !connection.isClosed() ? "connected" : "disconnected")
                            + " &8| &aActive test rifts: &f" + activeRifts.size());
                    tell(sender, "&aWorlds: &f" + Bukkit.getWorlds().size());
                }
                case "reload" -> {
                    if (!isAdmin(sender)) return true;
                    reloadConfig();
                    tell(sender, "&aConfiguration reloaded. Database and active rift state were not reset.");
                }
                default -> tell(sender, "&cUnknown command. Use /eclyria help");
            }
        } catch (NumberFormatException ex) {
            tell(sender, "&cInvalid number format.");
        } catch (SQLException ex) {
            getLogger().severe("Database operation failed: " + ex.getMessage());
            tell(sender, "&cA database error occurred. Check the server console.");
        } catch (Exception ex) {
            getLogger().warning("Command failed: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            tell(sender, "&cCommand failed safely. Check the server console.");
        }
        return true;
    }

    private World getRiftfallWorld() {
        return Bukkit.getWorld(getConfig().getString("world.name", "EclyriaRiftfall"));
    }

    private void setupWorld() {
        String worldName = getConfig().getString("world.name", "EclyriaRiftfall");
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        boolean folderExisted = worldFolder.exists();
        World world = Bukkit.getWorld(worldName);
        if (world == null) world = Bukkit.createWorld(new WorldCreator(worldName));
        if (world == null) {
            getLogger().severe("Could not load or create hub world '" + worldName + "'. Check the world name and server logs.");
            return;
        }
        world.setAutoSave(true);
        if (!getConfig().getBoolean("world.generated.havenfall", false)) {
            buildHavenfall(world);
            getConfig().set("world.generated.havenfall", true);
            saveConfig();
            getLogger().info("Generated Havenfall hub in " + worldName + ". No Rift terrain was placed in the hub world.");
        } else {
            getLogger().info("Loaded existing hub world folder '" + worldName + "'. Existing hub preserved.");
        }
        if (getConfig().getConfigurationSection("regions.havenfall.spawn") == null) {
            int y = world.getHighestBlockYAt(0, 0) + 1;
            if (!folderExisted && y < 90) y = 91;
            saveSpawn("havenfall", new Location(world, 0.5, Math.max(y, 2), 8.5));
            saveConfig();
        }
        Location hubSpawn = readSpawn("havenfall");
        if (hubSpawn != null && hubSpawn.getWorld() != null) world.setSpawnLocation(hubSpawn);
        ensureRiftWorld("verdant");
        ensureRiftWorld("ember");
        ensureRiftWorld("void");
    }

    private String riftWorldName(String key) {
        return getConfig().getString("rifts.worlds." + key, switch (key) {
            case "verdant" -> "Rift_Verdant";
            case "ember" -> "Rift_Ember";
            default -> "Rift_Void";
        });
    }

    private String riftKey(World world) {
        if (world == null) return null;
        for (String key : List.of("verdant", "ember", "void")) {
            if (world.getName().equalsIgnoreCase(riftWorldName(key))) return key;
        }
        return null;
    }

    private void ensureRiftWorld(String key) {
        String name = riftWorldName(key);
        World world = Bukkit.getWorld(name);
        boolean createdNewWorld = false;
        if (world == null) {
            File folder = new File(Bukkit.getWorldContainer(), name);
            String command;
            if (folder.exists()) {
                command = "mv load " + name;
            } else {
                command = "mv create " + name + " normal -g EclyriaCore:" + key;
                createdNewWorld = true;
            }
            boolean dispatched = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            world = Bukkit.getWorld(name);
            if (!dispatched || world == null) {
                getLogger().severe("Could not create/load Rift world '" + name + "' through Multiverse-Core. Tried: /" + command);
                return;
            }
        }
        world.setAutoSave(true);
        if (createdNewWorld || getConfig().getConfigurationSection("regions.rifts." + key + ".spawn") == null
                || !name.equals(getConfig().getString("regions.rifts." + key + ".spawn.world", name))) {
            Location surface = world.getHighestBlockAt(0, 0).getLocation().add(0.5, 1.0, 0.5);
            if (surface.getBlockY() < world.getMinHeight() + 2) surface.setY(world.getMinHeight() + 2);
            saveSpawn("rifts." + key, surface);
            saveSpawn(key, surface);
            saveConfig();
        }
        Location spawn = readSpawn("rifts." + key);
        if (spawn != null && spawn.getWorld() == world) world.setSpawnLocation(spawn);
        getLogger().info("Rift world ready: " + name + " (Eclyria procedural generator configured for newly created worlds; existing chunks are unchanged).");
    }

    private void generateAllRifts(World world) {
        // Three hand-built-by-code procedural arenas; no downloaded map or external setup is required.
        buildRiftIsland(world, "verdant", 300, Material.MOSS_BLOCK, Material.MOSSY_STONE_BRICKS,
                Material.OAK_LEAVES, Material.GLOWSTONE, Material.MOSS_CARPET, 1);
        buildRiftIsland(world, "ember", -300, Material.BLACKSTONE, Material.MAGMA_BLOCK,
                Material.BASALT, Material.SHROOMLIGHT, Material.CRIMSON_NYLIUM, 2);
        buildRiftIsland(world, "void", 600, Material.CRYING_OBSIDIAN, Material.PURPLE_STAINED_GLASS,
                Material.POLISHED_BLACKSTONE_BRICKS, Material.END_ROD, Material.SCULK, 3);
        world.setAutoSave(true);
        getLogger().info("Generated the Verdant, Ember and Void Rifts in " + world.getName());
    }

    private void buildRiftIsland(World world, String key, int centerX, Material surface, Material rim,
                                 Material accent, Material light, Material detail, int theme) {
        // Reject item-only materials before any world edits; Bukkit setType requires a block material.
        if (surface == null || rim == null || accent == null || light == null || detail == null
                || !surface.isBlock() || !rim.isBlock() || !accent.isBlock() || !light.isBlock() || !detail.isBlock()) {
            getLogger().severe("Skipped rift '" + key + "': all island materials must be blocks.");
            return;
        }
        int centerZ = 0;
        int baseY = 82;
        int radius = 54;
        // Clear only the intended island's volume so re-generation is predictable.
        for (int x = -radius - 3; x <= radius + 3; x++) {
            for (int z = -radius - 3; z <= radius + 3; z++) {
                double d = Math.sqrt(x * x + z * z);
                if (d <= radius + 3) {
                    for (int y = 48; y <= 125; y++) world.getBlockAt(centerX + x, y, centerZ + z).setType(Material.AIR, false);
                }
            }
        }
        // A shaped floating landmass with rough, deterministic ridges and a tapered underside.
        for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++) {
            double d = Math.sqrt(x * x + z * z);
            double wave = Math.sin((x + theme * 7) * 0.19) * 2.2 + Math.cos((z - theme * 5) * 0.17) * 2.0
                    + Math.sin((x + z) * 0.11) * 1.4;
            double edge = radius - d + wave;
            if (edge < 0) continue;
            int topY = baseY + (int) Math.round(wave + Math.max(0, 7 - d * 0.12));
            int depth = Math.max(5, (int) (17 * (1.0 - d / (radius + 1.0))));
            for (int y = topY - depth; y <= topY; y++) {
                Material block = y == topY ? (d > radius - 6 ? rim : surface)
                        : (y < topY - depth + 2 ? Material.DEEPSLATE : (theme == 2 ? Material.BASALT : Material.STONE));
                world.getBlockAt(centerX + x, y, centerZ + z).setType(block, false);
            }
            if (d < radius - 8 && ((x * 31 + z * 17 + theme * 13) & 127) == 0) {
                world.getBlockAt(centerX + x, topY + 1, centerZ + z).setType(detail, false);
            }
        }
        // Safe arrival dais, distinct per rift.
        int y = baseY + 10;
        Material floor = theme == 1 ? Material.MOSSY_STONE_BRICKS : theme == 2 ? Material.POLISHED_BLACKSTONE : Material.PURPUR_BLOCK;
        for (int x = -5; x <= 5; x++) for (int z = -5; z <= 5; z++) {
            world.getBlockAt(centerX + x, y, z).setType(Math.abs(x) == 5 || Math.abs(z) == 5 ? rim : floor, false);
            world.getBlockAt(centerX + x, y + 1, z).setType(Material.AIR, false);
            world.getBlockAt(centerX + x, y + 2, z).setType(Material.AIR, false);
        }
        for (int[] c : new int[][]{{-9,-9},{-9,9},{9,-9},{9,9}}) {
            for (int yy = y + 1; yy <= y + 5; yy++) world.getBlockAt(centerX + c[0], yy, c[1]).setType(accent, false);
            world.getBlockAt(centerX + c[0], y + 6, c[1]).setType(light, false);
        }
        // Landmark at the center makes each area immediately recognizable.
        for (int yy = y + 1; yy <= y + 8; yy++) {
            world.getBlockAt(centerX - 2, yy, 0).setType(accent, false);
            world.getBlockAt(centerX + 2, yy, 0).setType(accent, false);
        }
        for (int x = -2; x <= 2; x++) {
            world.getBlockAt(centerX + x, y + 1, 0).setType(rim, false);
            world.getBlockAt(centerX + x, y + 8, 0).setType(rim, false);
        }
        for (int x = -1; x <= 1; x++) for (int yy = y + 2; yy <= y + 7; yy++) {
            world.getBlockAt(centerX + x, yy, 0).setType(theme == 1 ? Material.LIME_STAINED_GLASS : theme == 2 ? Material.ORANGE_STAINED_GLASS : Material.PURPLE_STAINED_GLASS, false);
        }
        // Hand-designed route, ruins, dungeon entrance and boss arena.
        buildRiftPath(world, centerX, y, theme, surface, rim, detail);
        buildRiftRuins(world, centerX, y, theme, accent, rim, light);
        buildRiftDungeon(world, centerX, y, theme, surface, accent, rim);
        buildRiftBossArena(world, centerX, y, theme, rim, accent, light);
        generateRiftOres(world, centerX, theme);
        Location spawn = new Location(world, centerX + 0.5, y + 1, 8.5, 180, 0);
        saveSpawn("rifts." + key, spawn);
        saveSpawn(key, spawn);
        if (theme == 1) saveSpawn("verdant", spawn);
        if (theme == 2) saveSpawn("ember", spawn);
        if (theme == 3) saveSpawn("void", spawn);
    }

    private void generateRiftOres(World world, int centerX, int theme) {
        String[] ids = switch (theme) {
            case 1 -> new String[]{"verdantite_ore", "frostquartz_ore", "duskcoal_ore"};
            case 2 -> new String[]{"emberstone_ore", "stormsteel_ore", "soulcopper_ore", "suniron_ore"};
            default -> new String[]{"voidsilver_ore", "aetherium_ore", "riftglass_ore", "frostquartz_ore"};
        };
        Material[] blocks = {Material.EMERALD_ORE, Material.NETHER_GOLD_ORE, Material.LAPIS_ORE, Material.COPPER_ORE,
                Material.DIAMOND_ORE, Material.REDSTONE_ORE, Material.GOLD_ORE, Material.COAL_ORE, Material.IRON_ORE, Material.NETHER_QUARTZ_ORE};
        Map<String, Material> materials = new HashMap<>();
        String[] all = {"verdantite_ore", "emberstone_ore", "voidsilver_ore", "stormsteel_ore", "frostquartz_ore", "soulcopper_ore", "suniron_ore", "duskcoal_ore", "aetherium_ore", "riftglass_ore"};
        for (int i = 0; i < all.length; i++) materials.put(all[i], blocks[i]);
        Random random = new Random(4100L + theme);
        for (String id : ids) {
            Material ore = materials.get(id);
            for (int vein = 0; vein < 11; vein++) {
                int dx = random.nextInt(81) - 40, dz = random.nextInt(81) - 40;
                int y = 57 + random.nextInt(22);
                for (int n = 0; n < 3 + random.nextInt(3); n++) {
                    int x = centerX + dx + random.nextInt(3) - 1, z = dz + random.nextInt(3) - 1, yy = y + random.nextInt(3) - 1;
                    Block block = world.getBlockAt(x, yy, z);
                    if (block.getType() == Material.STONE || block.getType() == Material.DEEPSLATE || block.getType() == Material.BASALT) block.setType(ore, false);
                }
            }
        }
    }

    @EventHandler
    public void onRiftOreBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        World world = block.getWorld();
        String key = riftKey(world);
        if (key == null) return;
        String oreId = RIFT_ORES.get(block.getType());
        if (oreId == null || !oreBelongsToRift(oreId, key)) return;
        event.setDropItems(false);
        event.setExpToDrop(2 + new Random().nextInt(4));
        block.setType(Material.AIR, false);
        ItemStack drop = createRpgItem(oreId, 1 + new Random().nextInt(2));
        if (drop != null) world.dropItemNaturally(block.getLocation(), drop);
        Player miner = event.getPlayer();
        miner.playSound(block.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, .8f, .7f + new Random().nextFloat());
        world.spawnParticle(Particle.CRIT, block.getLocation().add(.5,.5,.5), 12, .25,.25,.25,.08);
    }

    private boolean oreBelongsToRift(String oreId, String key) {
        return switch (key) {
            case "verdant" -> Set.of("verdantite_ore", "duskcoal_ore", "stormsteel_ore").contains(oreId);
            case "ember" -> Set.of("emberstone_ore", "stormsteel_ore", "soulcopper_ore", "suniron_ore").contains(oreId);
            case "void" -> Set.of("voidsilver_ore", "frostquartz_ore", "aetherium_ore", "riftglass_ore").contains(oreId);
            default -> false;
        };
    }

    @EventHandler
    public void onRiftChunkPopulate(ChunkPopulateEvent event) {
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();
        String key = riftKey(world);
        if (key == null) return;
        Random random = new Random(((long) chunk.getX() << 32) ^ chunk.getZ() ^ world.getSeed());
        List<String> ids = switch (key) {
            case "verdant" -> List.of("verdantite_ore", "duskcoal_ore", "stormsteel_ore");
            case "ember" -> List.of("emberstone_ore", "stormsteel_ore", "soulcopper_ore", "suniron_ore");
            default -> List.of("voidsilver_ore", "frostquartz_ore", "aetherium_ore", "riftglass_ore");
        };
        Map<String, Material> materials = new HashMap<>();
        RIFT_ORES.forEach((material, id) -> materials.put(id, material));
        int minY = Math.max(world.getMinHeight() + 8, 8);
        int maxY = Math.min(world.getMaxHeight() - 32, 72);
        if (maxY <= minY) return;
        int veins = 3 + random.nextInt(4);
        for (int vein = 0; vein < veins; vein++) {
            String id = ids.get(random.nextInt(ids.size()));
            Material ore = materials.get(id);
            if (ore == null || !ore.isBlock()) continue;
            int x = (chunk.getX() << 4) + random.nextInt(16);
            int y = minY + random.nextInt(maxY - minY);
            int z = (chunk.getZ() << 4) + random.nextInt(16);
            int size = 2 + random.nextInt(4);
            for (int n = 0; n < size; n++) {
                int bx = x + random.nextInt(3) - 1;
                int by = y + random.nextInt(3) - 1;
                int bz = z + random.nextInt(3) - 1;
                if ((bx >> 4) != chunk.getX() || (bz >> 4) != chunk.getZ()) continue;
                if (by < world.getMinHeight() || by >= world.getMaxHeight()) continue;
                Block target = world.getBlockAt(bx, by, bz);
                if (target.getType() == Material.STONE || target.getType() == Material.DEEPSLATE || target.getType() == Material.TUFF) {
                    target.setType(ore, false);
                }
            }
        }
        // Small, infrequent surface ruin; vanilla cave generation remains responsible for caves.
        if (random.nextInt(90) == 0) placeSmallRiftRuin(world, chunk, key, random);
    }

    private void placeSmallRiftRuin(World world, Chunk chunk, String key, Random random) {
        int x = (chunk.getX() << 4) + 8;
        int z = (chunk.getZ() << 4) + 8;
        int y = world.getHighestBlockYAt(x, z);
        if (y < world.getMinHeight() + 3 || y > world.getMaxHeight() - 12) return;
        Material wall = switch (key) {
            case "verdant" -> Material.MOSSY_STONE_BRICKS;
            case "ember" -> Material.BLACKSTONE;
            default -> Material.POLISHED_BLACKSTONE_BRICKS;
        };
        Material accent = switch (key) {
            case "verdant" -> Material.MOSS_BLOCK;
            case "ember" -> Material.MAGMA_BLOCK;
            default -> Material.CRYING_OBSIDIAN;
        };
        for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
            if (Math.abs(dx) == 3 || Math.abs(dz) == 3) {
                world.getBlockAt(x + dx, y + 1, z + dz).setType(wall, false);
                if (random.nextBoolean()) world.getBlockAt(x + dx, y + 2, z + dz).setType(wall, false);
            }
        }
        for (int[] corner : new int[][]{{-3,-3},{-3,3},{3,-3},{3,3}}) {
            world.getBlockAt(x + corner[0], y + 2, z + corner[1]).setType(accent, false);
        }
        world.getBlockAt(x, y + 1, z).setType(key.equals("verdant") ? Material.MOSS_CARPET : key.equals("ember") ? Material.SHROOMLIGHT : Material.END_ROD, false);
    }

    private void buildRiftPath(World world, int centerX, int y, int theme, Material surface, Material rim, Material detail) {
        Material path = theme == 1 ? Material.MOSSY_COBBLESTONE : theme == 2 ? Material.BLACKSTONE : Material.POLISHED_BLACKSTONE_BRICKS;
        // A curved, branching route with occasional marker pillars.
        for (int step = 0; step < 38; step++) {
            int z = -12 - step * 2;
            int bend = (int)Math.round(Math.sin(step * 0.31) * 7.0);
            for (int w = -2; w <= 2; w++) {
                int x = centerX + bend + w;
                int zz = z;
                int top = y;
                if (world.getBlockAt(x, top, zz).getType().isAir()) {
                    world.getBlockAt(x, top, zz).setType(path, false);
                    world.getBlockAt(x, top - 1, zz).setType(surface, false);
                }
            }
            if (step % 7 == 0) {
                int x = centerX + bend + 4;
                for (int h = 1; h <= 4; h++) world.getBlockAt(x, y + h, z).setType(rim, false);
                world.getBlockAt(x, y + 5, z).setType(detail, false);
                world.getBlockAt(x, y + 6, z).setType(theme == 1 ? Material.GLOWSTONE : theme == 2 ? Material.SHROOMLIGHT : Material.END_ROD, false);
            }
        }
    }

    private void buildRiftRuins(World world, int cx, int y, int theme, Material accent, Material rim, Material light) {
        int[][] sites = {{-28,-16},{28,-18},{-26,25},{27,27}};
        Material brick = theme == 1 ? Material.MOSSY_STONE_BRICKS : theme == 2 ? Material.POLISHED_BLACKSTONE_BRICKS : Material.PURPUR_BLOCK;
        for (int[] site : sites) {
            int x0 = cx + site[0], z0 = site[1];
            for (int x=-5;x<=5;x++) for(int z=-4;z<=4;z++) {
                if (Math.abs(x)==5 || Math.abs(z)==4) {
                    for (int h=0;h<3;h++) if ((x+z+h+theme)%4!=0) world.getBlockAt(x0+x,y+h,z0+z).setType(brick,false);
                } else if ((x*z+theme)%11==0) world.getBlockAt(x0+x,y,z0+z).setType(rim,false);
            }
            for (int[] col : new int[][]{{-4,-3},{4,-3},{-4,3},{4,3}}) {
                for(int h=1;h<=5;h++) world.getBlockAt(x0+col[0],y+h,z0+col[1]).setType(accent,false);
                world.getBlockAt(x0+col[0],y+6,z0+col[1]).setType(light,false);
            }
            world.getBlockAt(x0,y+1,z0).setType(theme==1?Material.EMERALD_BLOCK:theme==2?Material.MAGMA_BLOCK:Material.AMETHYST_BLOCK,false);
        }
    }

    private void buildRiftDungeon(World world, int cx, int y, int theme, Material floor, Material accent, Material rim) {
        // Sunken, roofed mini-dungeon with corridor, side chambers and a reward altar.
        int x0 = cx + 13, z0 = -37, base = y - 5;
        Material wall = theme == 1 ? Material.MOSSY_STONE_BRICKS : theme == 2 ? Material.BLACKSTONE : Material.POLISHED_BLACKSTONE_BRICKS;
        for (int x= -9;x<=9;x++) for(int z=-13;z<=13;z++) {
            boolean border = Math.abs(x)==9 || Math.abs(z)==13;
            world.getBlockAt(x0+x,base,z0+z).setType(border?rim:floor,false);
            world.getBlockAt(x0+x,base-1,z0+z).setType(wall,false);
            for(int h=1;h<=4;h++) world.getBlockAt(x0+x,base+h,z0+z).setType(border?wall:Material.AIR,false);
            if (border) world.getBlockAt(x0+x,base+5,z0+z).setType(wall,false);
        }
        // Corridor ribs and three chamber altars.
        for (int z=-10;z<=10;z+=5) for(int x : new int[]{-5,5}) for(int h=1;h<=3;h++) world.getBlockAt(x0+x,base+h,z0+z).setType(accent,false);
        for (int[] altar : new int[][]{{-5,-7},{5,0},{0,8}}) {
            world.getBlockAt(x0+altar[0],base+1,z0+altar[1]).setType(theme==1?Material.EMERALD_BLOCK:theme==2?Material.MAGMA_BLOCK:Material.CRYING_OBSIDIAN,false);
            world.getBlockAt(x0+altar[0],base+2,z0+altar[1]).setType(theme==1?Material.LANTERN:theme==2?Material.SOUL_LANTERN:Material.END_ROD,false);
        }
        // Entrance stair and frame.
        for(int step=0;step<6;step++) {
            int zz=z0+14+step;
            for(int x=-2;x<=2;x++) world.getBlockAt(x0+x,base+step,zz).setType(floor,false);
        }
        for(int h=0;h<=5;h++) { world.getBlockAt(x0-3,base+h,z0+14).setType(rim,false); world.getBlockAt(x0+3,base+h,z0+14).setType(rim,false); }
    }

    private void buildRiftBossArena(World world, int cx, int y, int theme, Material rim, Material accent, Material light) {
        int x0=cx-30, z0=-34, radius=13;
        Material floor=theme==1?Material.MOSSY_STONE_BRICKS:theme==2?Material.BLACKSTONE:Material.POLISHED_BLACKSTONE_BRICKS;
        for(int x=-radius;x<=radius;x++) for(int z=-radius;z<=radius;z++) {
            if(x*x+z*z<=radius*radius) {
                world.getBlockAt(x0+x,y,z0+z).setType((Math.abs(x)==radius||Math.abs(z)==radius)?rim:floor,false);
                world.getBlockAt(x0+x,y+1,z0+z).setType(Material.AIR,false);
                if(x*x+z*z>(radius-2)*(radius-2)) world.getBlockAt(x0+x,y+1,z0+z).setType(rim,false);
            }
        }
        for(int a=0;a<8;a++) {
            double angle=Math.PI*2*a/8.0; int x=x0+(int)Math.round(Math.cos(angle)*10), z=z0+(int)Math.round(Math.sin(angle)*10);
            for(int h=1;h<=5;h++) world.getBlockAt(x,y+h,z).setType(accent,false);
            world.getBlockAt(x,y+6,z).setType(light,false);
        }
        world.getBlockAt(x0,y+1,z0).setType(theme==1?Material.EMERALD_BLOCK:theme==2?Material.MAGMA_BLOCK:Material.CRYING_OBSIDIAN,false);
        world.getBlockAt(x0,y+2,z0).setType(theme==1?Material.BEACON:theme==2?Material.SHROOMLIGHT:Material.END_ROD,false);
    }

    private void spawnRegionMobs() {
        for (String key : List.of("verdant", "ember", "void")) {
            World world = Bukkit.getWorld(riftWorldName(key));
            if (world == null) continue;
            String id = switch (key) {
                case "verdant" -> "verdant_sentinel";
                case "ember" -> "ash_hound";
                default -> "void_wraith";
            };
            for (Player player : world.getPlayers()) {
                if (new Random().nextDouble() > .12) continue;
                long nearby = player.getNearbyEntities(28, 20, 28).stream()
                        .filter(e -> e instanceof LivingEntity le && le.getPersistentDataContainer().has(mobKey, PersistentDataType.STRING)).count();
                if (nearby < 5) spawnEclyriaMob(id, player.getLocation().add(5, 0, 5), player);
            }
        }
    }

    private void playRiftAmbience() {
        spawnRegionMobs();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld() == null) continue;
            long now=System.currentTimeMillis();
            if (now-ambienceCooldowns.getOrDefault(player.getUniqueId(),0L)<9000L) continue;
            String region = riftKey(player.getWorld());
            if (region == null && player.getWorld().getName().equalsIgnoreCase(getConfig().getString("world.name", "EclyriaRiftfall"))) region = "havenfall";
            if (region==null) continue;
            ambienceCooldowns.put(player.getUniqueId(),now);
            Location at=player.getLocation().add(0,1,0);
            switch(region) {
                case "verdant" -> { player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,at,4,.8,.6,.8,.01); player.getWorld().playSound(player.getLocation(),Sound.BLOCK_AMETHYST_BLOCK_CHIME,.18f,.7f); }
                case "ember" -> { player.getWorld().spawnParticle(Particle.FLAME,at,5,.7,.5,.7,.015); player.getWorld().spawnParticle(Particle.SMOKE,at,3,.6,.4,.6,.01); player.getWorld().playSound(player.getLocation(),Sound.BLOCK_FIRE_AMBIENT,.22f,.65f); }
                case "void" -> { player.getWorld().spawnParticle(Particle.PORTAL,at,12,.8,.8,.8,.15); player.getWorld().playSound(player.getLocation(),Sound.BLOCK_PORTAL_AMBIENT,.18f,.55f); }
                default -> { player.getWorld().spawnParticle(Particle.END_ROD,at,3,.5,.5,.5,.01); player.getWorld().playSound(player.getLocation(),Sound.BLOCK_BEACON_AMBIENT,.12f,1.4f); }
            }
        }
    }

    private void saveSpawn(String region, Location loc) {
        String path = "regions." + region + ".spawn";
        getConfig().set(path + ".world", loc.getWorld().getName());
        getConfig().set(path + ".x", loc.getX());
        getConfig().set(path + ".y", loc.getY());
        getConfig().set(path + ".z", loc.getZ());
        getConfig().set(path + ".yaw", loc.getYaw());
        getConfig().set(path + ".pitch", loc.getPitch());
    }

    private Location readSpawn(String region) {
        String path = "regions." + region + ".spawn";
        String worldName = getConfig().getString(path + ".world", getConfig().getString("world.name", "EclyriaRiftfall"));
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, getConfig().getDouble(path + ".x"), getConfig().getDouble(path + ".y", 91),
                getConfig().getDouble(path + ".z"), (float) getConfig().getDouble(path + ".yaw"), (float) getConfig().getDouble(path + ".pitch"));
    }

    private void buildHavenfall(World world) {
        int y = 90;
        for (int x = -18; x <= 18; x++) {
            for (int z = -18; z <= 18; z++) {
                double d = Math.sqrt(x * x + z * z);
                if (d <= 18) {
                    Material top = d > 15 ? Material.POLISHED_DEEPSLATE : (d > 12 ? Material.DEEPSLATE_TILES : Material.STONE_BRICKS);
                    world.getBlockAt(x, y, z).setType(top, false);
                    world.getBlockAt(x, y - 1, z).setType(d > 15 ? Material.DEEPSLATE : Material.STONE, false);
                    if (d > 16 && ((x + z) % 3 == 0)) world.getBlockAt(x, y + 1, z).setType(Material.SOUL_LANTERN, false);
                }
            }
        }
        // Central portal plaza and a decorative portal frame (not a Nether portal).
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
            world.getBlockAt(x, y, z).setType((Math.abs(x) == 3 || Math.abs(z) == 3) ? Material.POLISHED_BLACKSTONE : Material.PURPLE_STAINED_GLASS, false);
        }
        for (int yy = y + 1; yy <= y + 6; yy++) {
            world.getBlockAt(-2, yy, 0).setType(Material.OBSIDIAN, false);
            world.getBlockAt(2, yy, 0).setType(Material.OBSIDIAN, false);
        }
        for (int x = -2; x <= 2; x++) {
            world.getBlockAt(x, y + 1, 0).setType(Material.OBSIDIAN, false);
            world.getBlockAt(x, y + 6, 0).setType(Material.OBSIDIAN, false);
        }
        for (int x = -1; x <= 1; x++) for (int yy = y + 2; yy <= y + 5; yy++) {
            world.getBlockAt(x, yy, 0).setType(Material.PURPLE_STAINED_GLASS, false);
        }
        // Four corner beacons make the spawn hub easy to recognize.
        for (int[] c : new int[][]{{-10,-10},{-10,10},{10,-10},{10,10}}) {
            for (int yy = y + 1; yy <= y + 4; yy++) world.getBlockAt(c[0], yy, c[1]).setType(Material.POLISHED_BLACKSTONE_BRICKS, false);
            world.getBlockAt(c[0], y + 5, c[1]).setType(Material.SOUL_LANTERN, false);
        }
        world.setSpawnLocation(0, y + 1, 8);
    }

    private void buildRegionPlatform(World world, int centerX, Material floor, Material edge, String label) {
        int y = 90;
        for (int x = -8; x <= 8; x++) for (int z = -8; z <= 8; z++) {
            double d = Math.sqrt(x * x + z * z);
            if (d <= 8) {
                world.getBlockAt(centerX + x, y, z).setType(d > 6 ? edge : floor, false);
                world.getBlockAt(centerX + x, y - 1, z).setType(Material.STONE, false);
            }
        }
        for (int yy = y + 1; yy <= y + 3; yy++) {
            world.getBlockAt(centerX - 6, yy, 0).setType(edge, false);
            world.getBlockAt(centerX + 6, yy, 0).setType(edge, false);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (entity instanceof Villager villager && "Divine Priest".equals(villager.getCustomName())) {
            event.setCancelled(true);
            priestAccess.add(event.getPlayer().getUniqueId());
            openDivineMenu(event.getPlayer());
        }
    }

    @EventHandler
    public void onRiftForgeInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.SMITHING_TABLE) return;
        event.setCancelled(true);
        openRiftForge(event.getPlayer());
    }

    @EventHandler
    public void onAbilityUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack hand = event.getItem();
        if (hand == null || !hand.hasItemMeta()) return;
        String id = hand.getItemMeta().getPersistentDataContainer().get(abilityKey, PersistentDataType.STRING);
        if (id == null) return;
        event.setCancelled(true);
        if (id.equals("mana_flask")) {
            setMana(player, Math.min(getMaxMana(player), getMana(player) + 45));
            if (hand.getAmount() <= 1) player.getInventory().setItemInMainHand(null); else hand.setAmount(hand.getAmount()-1);
            tell(player, "&bMana Flask used. Mana: " + getMana(player) + "/" + getMaxMana(player)); return;
        }
        int cost = ABILITY_COSTS.getOrDefault(id, 0);
        long now = System.currentTimeMillis();
        long until = abilityCooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (until > now) { tell(player, "&cAbility cooling down for " + ((until-now+999)/1000) + "s."); return; }
        if (getMana(player) < cost) { tell(player, "&bNot enough mana. Need " + cost + " mana."); return; }
        setMana(player, getMana(player)-cost);
        abilityCooldowns.put(player.getUniqueId(), now + getCooldown(id)*1000L);
        castAbility(player, id);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, .8f, 1.2f);
        tell(player, "&d" + title(id.replace('_',' ')) + " &7used. &bMana: " + getMana(player) + "/" + getMaxMana(player));
    }

    private int itemTier(String id) {
        return switch (id) {
            case "arcane_focus", "thornlash_whip", "mana_flask" -> 1;
            case "glacier_orb", "emberheart", "sunfire_charm", "tidal_scepter", "frostbite_blade",
                    "astral_compass", "echoing_horn" -> 2;
            case "stormcaller_staff", "shadowstep_dagger", "soul_anchor", "verdant_buckler",
                    "thunder_rune", "crystal_bomb", "mirror_shard", "runic_gauntlet" -> 3;
            case "gravity_maul", "phoenix_feather", "void_reaper", "bloodmoon_amulet",
                    "warding_totem", "dreamweaver", "starfall_lance" -> 4;
            case "riftsurge_crystal", "abyssal_crown" -> 5;
            default -> 1;
        };
    }

    private int getCooldown(String id) {
        return switch(id) { case "arcane_focus" -> 3; case "glacier_orb","frostbite_blade","runic_gauntlet" -> 5; case "emberheart","tidal_scepter","thunder_rune","crystal_bomb","echoing_horn" -> 7; case "stormcaller_staff","shadowstep_dagger","void_reaper","starfall_lance","gravity_maul","riftsurge_crystal" -> 9; case "phoenix_feather" -> 30; case "warding_totem","dreamweaver","mirror_shard","soul_anchor","bloodmoon_amulet","abyssal_crown" -> 12; default -> 8; };
    }

    private void castAbility(Player p, String id) {
        Location loc=p.getLocation();
        switch(id) {
            case "shadowstep_dagger", "astral_compass" -> { Location to=loc.clone().add(loc.getDirection().multiply(id.equals("astral_compass")?5:4)); to.setY(Math.max(to.getY(), p.getWorld().getHighestBlockYAt(to)+1)); p.teleport(to); p.getWorld().spawnParticle(Particle.PORTAL, loc, 45, .6,.8,.6,.2); }
            case "sunfire_charm", "phoenix_feather", "dreamweaver" -> { p.setHealth(Math.min(p.getAttribute(Attribute.MAX_HEALTH).getValue(), p.getHealth()+ (id.equals("phoenix_feather")?10:6))); p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,100, id.equals("phoenix_feather")?1:0)); p.getWorld().spawnParticle(Particle.HEART, loc.add(0,1,0), 12,.7,.6,.7,.1); }
            case "soul_anchor", "verdant_buckler", "warding_totem", "mirror_shard" -> { p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, id.equals("warding_totem")?160:100, id.equals("warding_totem")?1:0)); if(id.equals("verdant_buckler")) p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,100,0)); p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc.add(0,1,0), 35,.8,.8,.8,.1); }
            case "mana_flask" -> setMana(p, getMaxMana(p));
            default -> { double radius = Set.of("gravity_maul","void_reaper","riftsurge_crystal","abyssal_crown","starfall_lance","crystal_bomb").contains(id)?6:4; int damage = Set.of("riftsurge_crystal","abyssal_crown","starfall_lance","void_reaper").contains(id)?12:7; for(Entity e:p.getNearbyEntities(radius, radius, radius)) if(e instanceof LivingEntity le && e!=p) { le.damage(damage,p); if(id.equals("glacier_orb")||id.equals("frostbite_blade")) le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,80,1)); if(id.equals("thornlash_whip")||id.equals("gravity_maul")) le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,50,2)); if(id.equals("emberheart")) le.setFireTicks(80); } p.getWorld().spawnParticle(id.equals("glacier_orb")||id.equals("frostbite_blade")?Particle.SNOWFLAKE:Particle.PORTAL, loc.add(0,1,0), 65, radius/2,.8,radius/2,.15); }
        }
    }

    private int getMana(Player p) { return getConfig().getInt("divine."+p.getUniqueId()+".mana", 0); }
    private int getMaxMana(Player p) { int level=getConfig().getInt("divine."+p.getUniqueId()+".mana-level",0); return level==0?0:50+level*25; }
    private void setMana(Player p,int amount) { getConfig().set("divine."+p.getUniqueId()+".mana", Math.max(0,Math.min(getMaxMana(p),amount))); saveConfig(); }
    private void regenerateMana() { for(Player p:Bukkit.getOnlinePlayers()) { int max=getMaxMana(p), current=getMana(p); if(max>0 && current<max && System.currentTimeMillis()-manaRegenTick.getOrDefault(p.getUniqueId(),0L)>3000) { getConfig().set("divine."+p.getUniqueId()+".mana",Math.min(max,current+2)); manaRegenTick.put(p.getUniqueId(),System.currentTimeMillis()); } } saveConfig(); }
    private void grantRevelation(Player p) { String path="divine."+p.getUniqueId(); if(getConfig().getInt(path+".mana-level",0)>0) { tell(p,"&dYour revelation has already been granted."); return; } getConfig().set(path+".mana-level",1); getConfig().set(path+".mana",75); getConfig().set(path+".rank","Initiate of the Rift"); getConfig().set(path+".revealed",true); saveConfig(); p.getWorld().spawnParticle(Particle.END_ROD,p.getLocation().add(0,1,0),50,.7,1,.7,.08); p.playSound(p.getLocation(),Sound.UI_TOAST_CHALLENGE_COMPLETE,1,1); tell(p,"&dThe priest has revealed your divine path. Rank: &fInitiate of the Rift &7| &bMana Level 1"); }
    private void openDivineMenu(Player p) { Inventory inv=Bukkit.createInventory(null,27,color("&0Eclyria | Divine Revelation")); for(int i=0;i<27;i++) inv.setItem(i,menuItem(Material.BLACK_STAINED_GLASS_PANE,"&0 ")); String path="divine."+p.getUniqueId(); int level=getConfig().getInt(path+".mana-level",0); inv.setItem(11,menuItem(Material.NETHER_STAR,"&dDivine Revelation",level==0?new String[]{"&7The priest may awaken your mana.","&eRight-click the Divine Priest"}:new String[]{"&7Your divine path is awakened.","&dRank: &f"+getConfig().getString(path+".rank","Initiate of the Rift"),"&bMana Level: &f"+level})); inv.setItem(13,menuItem(Material.LAPIS_LAZULI,"&bMana Reserve","&7Mana: &f"+getMana(p)+"/&f"+getMaxMana(p),"&7Regenerates slowly over time.")); if(level>0) inv.setItem(15,menuItem(Material.EXPERIENCE_BOTTLE,"&bMeditate for Mana","&7Restore mana to maximum.","&eClick to meditate")); else inv.setItem(15,menuItem(Material.ENCHANTED_BOOK,"&dReceive Divine Revelation","&7Speak with the priest to awaken your mana.","&eRight-click the Divine Priest")); inv.setItem(22,menuItem(Material.ARROW,"&eBack to Menu")); p.openInventory(inv); }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (getConfig().getBoolean("world.send-new-players-to-havenfall", true) && !event.getPlayer().hasPlayedBefore()) {
            Bukkit.getScheduler().runTask(this, () -> {
                if (!event.getPlayer().isOnline()) return;
                Location spawn = readSpawn("havenfall");
                if (spawn != null) event.getPlayer().teleport(spawn.clone().add(0, 1, 8));
            });
        }
    }

    private ItemStack menuItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color(name));
        if (lore.length > 0) meta.setLore(Arrays.stream(lore).map(this::color).toList());
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createRpgItem(String id, int amount) {
        String normalized = id.toLowerCase(Locale.ROOT);
        Material material = Material.matchMaterial(itemMaterials.getOrDefault(normalized, "PAPER"));
        if (material == null) return null;
        ItemStack item;
        if (Set.of("rift_ingot", "rift_helm", "verdant_core", "abyssal_crown", "verdantite_ingot", "emberstone_ingot", "voidsilver_ingot", "stormsteel_ingot", "frostquartz_ingot", "soulcopper_ingot", "suniron_ingot", "duskcoal_ingot", "aetherium_ingot", "riftglass_ingot").contains(normalized)) {
            item = texturedHead(normalized, amount);
        } else {
            item = new ItemStack(material, Math.max(1, Math.min(64, amount)));
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        String name = switch (normalized) {
            case "rift_shard" -> "&5Rift Shard";
            case "aetherite" -> "&bAetherite Crystal";
            case "voidglass" -> "&8Voidglass";
            case "rift_ingot" -> "&dRiftforged Ingot";
            case "rift_helm" -> "&5Riftbound Visage";
            case "verdant_core" -> "&aVerdant Core";
            case "aether_blade" -> "&bAether Blade";
            case "rift_bow" -> "&5Riftstring Bow";
            case "healing_tonic" -> "&cVitality Tonic";
            case "stormcaller_staff" -> "&bStormcaller Staff"; case "glacier_orb" -> "&fGlacier Orb";
            case "emberheart" -> "&6Emberheart"; case "thornlash_whip" -> "&aThornlash";
            case "shadowstep_dagger" -> "&8Shadowstep Dagger"; case "sunfire_charm" -> "&eSunfire Charm";
            case "tidal_scepter" -> "&3Tidal Scepter"; case "soul_anchor" -> "&5Soul Anchor";
            case "gravity_maul" -> "&8Gravity Maul"; case "phoenix_feather" -> "&cPhoenix Feather";
            case "frostbite_blade" -> "&bFrostbite Blade"; case "arcane_focus" -> "&dArcane Focus";
            case "void_reaper" -> "&5Void Reaper"; case "verdant_buckler" -> "&2Verdant Buckler";
            case "thunder_rune" -> "&eThunder Rune"; case "bloodmoon_amulet" -> "&4Bloodmoon Amulet";
            case "astral_compass" -> "&9Astral Compass"; case "riftsurge_crystal" -> "&dRiftsurge Crystal";
            case "echoing_horn" -> "&6Echoing Horn"; case "crystal_bomb" -> "&dCrystal Bomb";
            case "warding_totem" -> "&aWarding Totem"; case "mana_flask" -> "&bMana Flask";
            case "starfall_lance" -> "&eStarfall Lance"; case "mirror_shard" -> "&fMirror Shard";
            case "runic_gauntlet" -> "&6Runic Gauntlet"; case "dreamweaver" -> "&dDreamweaver";
            case "abyssal_crown" -> "&5Abyssal Crown";
            case "verdantite_ore" -> "&aVerdantite Ore"; case "verdantite_ingot" -> "&6Verdantite Ingot";
            case "emberstone_ore" -> "&aEmberstone Ore"; case "emberstone_ingot" -> "&6Emberstone Ingot";
            case "voidsilver_ore" -> "&aVoidsilver Ore"; case "voidsilver_ingot" -> "&6Voidsilver Ingot";
            case "stormsteel_ore" -> "&aStormsteel Ore"; case "stormsteel_ingot" -> "&6Stormsteel Ingot";
            case "frostquartz_ore" -> "&aFrostquartz Ore"; case "frostquartz_ingot" -> "&6Frostquartz Ingot";
            case "soulcopper_ore" -> "&aSoulcopper Ore"; case "soulcopper_ingot" -> "&6Soulcopper Ingot";
            case "suniron_ore" -> "&aSuniron Ore"; case "suniron_ingot" -> "&6Suniron Ingot";
            case "duskcoal_ore" -> "&aDuskcoal Ore"; case "duskcoal_ingot" -> "&6Duskcoal Ingot";
            case "aetherium_ore" -> "&aAetherium Ore"; case "aetherium_ingot" -> "&6Aetherium Ingot";
            case "riftglass_ore" -> "&aRiftglass Ore"; case "riftglass_ingot" -> "&6Riftglass Ingot";
            default -> "&f" + title(normalized.replace('_', ' '));
        };
        List<String> lore = switch (normalized) {
            case "rift_shard" -> List.of("&7A fractured remnant of the Rift.", "&8Crafting material", "&5Rarity: Uncommon");
            case "aetherite" -> List.of("&7A crystal humming with old magic.", "&8Crafting material", "&bRarity: Rare");
            case "voidglass" -> List.of("&7Dark glass that drinks in the light.", "&8Crafting material", "&5Rarity: Rare");
            case "rift_ingot" -> List.of("&7Metal refined around a Rift shard.", "&8Used to craft Riftbound equipment", "&dRarity: Epic");
            case "rift_helm" -> List.of("&7A cosmetic helm shaped by the Rift.", "&8Equip in your helmet slot", "&5Rarity: Epic");
            case "verdant_core" -> List.of("&7A living seed from an ancient guardian.", "&8A key ingredient for rare crafts", "&aRarity: Rare");
            case "aether_blade" -> List.of("&7A balanced blade charged with Aether.", "&bDamage: 9", "&8Forged at an ordinary crafting table", "&bRarity: Rare");
            case "rift_bow" -> List.of("&7A bow strung with condensed Rift energy.", "&5Rift-touched ammunition", "&8Forged at an ordinary crafting table", "&5Rarity: Epic");
            case "healing_tonic" -> List.of("&7A restorative brew for dangerous journeys.", "&cRestores health when consumed", "&aRarity: Common");
            case "stormcaller_staff" -> List.of("&7Calls a burst of lightning around you.", "&bAbility: Storm Burst", "&dCost: 24 mana | Cooldown: 8s");
            case "glacier_orb" -> List.of("&7Unleashes a chilling pulse.", "&bAbility: Frost Nova", "&dCost: 18 mana | Cooldown: 6s");
            case "emberheart" -> List.of("&7A heart of living flame.", "&6Ability: Flame Wave", "&dCost: 22 mana | Cooldown: 7s");
            case "thornlash_whip" -> List.of("&7Vines lash nearby foes.", "&aAbility: Briar Snare", "&dCost: 16 mana | Cooldown: 5s");
            case "shadowstep_dagger" -> List.of("&7Slip through a fold in shadow.", "&8Ability: Shadowstep", "&dCost: 20 mana | Cooldown: 8s");
            case "sunfire_charm" -> List.of("&7A small sun sealed in gold.", "&eAbility: Radiant Mend", "&dCost: 20 mana | Cooldown: 10s");
            case "tidal_scepter" -> List.of("&7Commands a surge of water magic.", "&3Ability: Tidal Pulse", "&dCost: 18 mana | Cooldown: 7s");
            case "soul_anchor" -> List.of("&7Steadies the soul against the void.", "&5Ability: Soul Guard", "&dCost: 25 mana | Cooldown: 12s");
            case "gravity_maul" -> List.of("&7Bends gravity into a crushing field.", "&8Ability: Gravity Well", "&dCost: 28 mana | Cooldown: 12s");
            case "phoenix_feather" -> List.of("&7A feather warm with second chances.", "&cAbility: Phoenix Renewal", "&dCost: 30 mana | Cooldown: 30s");
            case "frostbite_blade" -> List.of("&7Its edge steals heat from the air.", "&bAbility: Frost Strike", "&dCost: 16 mana | Cooldown: 5s");
            case "arcane_focus" -> List.of("&7Focuses raw arcane energy.", "&dAbility: Arcane Bolt", "&dCost: 12 mana | Cooldown: 3s");
            case "void_reaper" -> List.of("&7Harvests the silence between worlds.", "&5Ability: Void Sweep", "&dCost: 26 mana | Cooldown: 9s");
            case "verdant_buckler" -> List.of("&7A living shield that mends its bearer.", "&aAbility: Rooted Ward", "&dCost: 18 mana | Cooldown: 10s");
            case "thunder_rune" -> List.of("&7A rune etched with stormlight.", "&eAbility: Chain Spark", "&dCost: 20 mana | Cooldown: 7s");
            case "bloodmoon_amulet" -> List.of("&7Gathers strength beneath a crimson moon.", "&4Ability: Bloodmoon Surge", "&dCost: 24 mana | Cooldown: 12s");
            case "astral_compass" -> List.of("&7Points toward hidden astral currents.", "&9Ability: Astral Leap", "&dCost: 14 mana | Cooldown: 5s");
            case "riftsurge_crystal" -> List.of("&7A volatile pocket of Rift energy.", "&dAbility: Rift Surge", "&dCost: 32 mana | Cooldown: 15s");
            case "echoing_horn" -> List.of("&7Its note shakes the air around you.", "&6Ability: Echo Shockwave", "&dCost: 18 mana | Cooldown: 8s");
            case "crystal_bomb" -> List.of("&7A volatile crystal charge.", "&dAbility: Crystal Detonation", "&dCost: 22 mana | Cooldown: 8s");
            case "warding_totem" -> List.of("&7Raises a brief protective blessing.", "&aAbility: Warding Aura", "&dCost: 28 mana | Cooldown: 20s");
            case "mana_flask" -> List.of("&7A sealed reserve of pure mana.", "&bAbility: Restore 45 mana", "&aConsumable");
            case "starfall_lance" -> List.of("&7Channels a falling star into a strike.", "&eAbility: Starfall", "&dCost: 30 mana | Cooldown: 12s");
            case "mirror_shard" -> List.of("&7Reflects a sliver of incoming force.", "&fAbility: Mirror Guard", "&dCost: 20 mana | Cooldown: 12s");
            case "runic_gauntlet" -> List.of("&7Runes amplify the wearer's next strike.", "&6Ability: Rune Smash", "&dCost: 18 mana | Cooldown: 6s");
            case "dreamweaver" -> List.of("&7Folds a dream into a healing mist.", "&dAbility: Dream Veil", "&dCost: 24 mana | Cooldown: 12s");
            case "abyssal_crown" -> List.of("&7A crown that whispers from below.", "&5Ability: Abyssal Pulse", "&dCost: 35 mana | Cooldown: 18s");
            case "verdantite_ore" -> List.of("&7A rare deposit found in the Verdant rift.", "&8Mine it to obtain raw verdantite.", "&dEclyria custom ore");
            case "verdantite_ingot" -> List.of("&7Refined verdantite metal from the Verdant rift.", "&8Used in Eclyria equipment crafting.", "&dEclyria custom material");
            case "emberstone_ore" -> List.of("&7A rare deposit found in the Ember rift.", "&8Mine it to obtain raw emberstone.", "&dEclyria custom ore");
            case "emberstone_ingot" -> List.of("&7Refined emberstone metal from the Ember rift.", "&8Used in Eclyria equipment crafting.", "&dEclyria custom material");
            case "voidsilver_ore" -> List.of("&7A rare deposit found in the Void rift.", "&8Mine it to obtain raw voidsilver.", "&dEclyria custom ore");
            case "voidsilver_ingot" -> List.of("&7Refined voidsilver metal from the Void rift.", "&8Used in Eclyria equipment crafting.", "&dEclyria custom material");
            case "stormsteel_ore" -> List.of("&7A rare deposit found in the Ember rift.", "&8Mine it to obtain raw stormsteel.", "&dEclyria custom ore");
            case "stormsteel_ingot" -> List.of("&7Refined stormsteel metal from the Ember rift.", "&8Used in Eclyria equipment crafting.", "&dEclyria custom material");
            case "frostquartz_ore" -> List.of("&7A rare deposit found in the Void rift.", "&8Mine it to obtain raw frostquartz.", "&dEclyria custom ore");
            case "frostquartz_ingot" -> List.of("&7Refined frostquartz metal from the Void rift.", "&8Used in Eclyria equipment crafting.", "&dEclyria custom material");
            case "soulcopper_ore" -> List.of("&7A rare deposit found in the Ember rift.", "&8Mine it to obtain raw soulcopper.", "&dEclyria custom ore");
            case "soulcopper_ingot" -> List.of("&7Refined soulcopper metal from the Ember rift.", "&8Used in Eclyria equipment crafting.", "&dEclyria custom material");
            case "suniron_ore" -> List.of("&7A rare deposit found in the Ember rift.", "&8Mine it to obtain raw suniron.", "&dEclyria custom ore");
            case "suniron_ingot" -> List.of("&7Refined suniron metal from the Ember rift.", "&8Used in Eclyria equipment crafting.", "&dEclyria custom material");
            case "duskcoal_ore" -> List.of("&7A rare deposit found in the Verdant rift.", "&8Mine it to obtain raw duskcoal.", "&dEclyria custom ore");
            case "duskcoal_ingot" -> List.of("&7Refined duskcoal metal from the Verdant rift.", "&8Used in Eclyria equipment crafting.", "&dEclyria custom material");
            case "aetherium_ore" -> List.of("&7A rare deposit found in the Void rift.", "&8Mine it to obtain raw aetherium.", "&dEclyria custom ore");
            case "aetherium_ingot" -> List.of("&7Refined aetherium metal from the Void rift.", "&8Used in Eclyria equipment crafting.", "&dEclyria custom material");
            case "riftglass_ore" -> List.of("&7A rare deposit found in the Void rift.", "&8Mine it to obtain raw riftglass.", "&dEclyria custom ore");
            case "riftglass_ingot" -> List.of("&7Refined riftglass metal from the Void rift.", "&8Used in Eclyria equipment crafting.", "&dEclyria custom material");
            default -> List.of("&7An item from the lands of Eclyria.");
        };
        meta.setDisplayName(color(name));
        if (ABILITY_COSTS.containsKey(normalized) || normalized.equals("mana_flask")) {
            int tier = itemTier(normalized);
            String rating = "&6Rating: &e" + tier + "/5 " + "★".repeat(tier) + "☆".repeat(5-tier);
            List<String> upgradedLore = new ArrayList<>(lore);
            upgradedLore.add("&7");
            upgradedLore.add(rating);
            upgradedLore.add("&eHow to use: &fRight-click");
            if (!normalized.equals("mana_flask")) {
                upgradedLore.add("&7Requires unlocked mana and sufficient reserve.");
                upgradedLore.add("&7Cooldown: &f" + getCooldown(normalized) + "s");
            } else {
                upgradedLore.add("&7Restores up to 45 mana, then consumes itself.");
            }
            lore = upgradedLore;
        }
        meta.setLore(lore.stream().map(this::color).toList());
        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, normalized);
        if (ABILITY_COSTS.containsKey(normalized) || normalized.equals("mana_flask")) meta.getPersistentDataContainer().set(abilityKey, PersistentDataType.STRING, normalized);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack texturedHead(String itemId, int amount) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, Math.max(1, Math.min(64, amount)));
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        String skinName = switch (itemId) {
            case "rift_ingot" -> "MHF_Blaze";
            case "rift_helm" -> "MHF_Enderman";
            case "verdant_core" -> "MHF_OakLog";
            case "abyssal_crown" -> "MHF_Wither";
            case "verdantite_ingot" -> "MHF_OakLog";
            case "emberstone_ingot" -> "MHF_Blaze";
            case "voidsilver_ingot" -> "MHF_Squid";
            case "stormsteel_ingot" -> "MHF_IronGolem";
            case "frostquartz_ingot" -> "MHF_SnowGolem";
            case "soulcopper_ingot" -> "MHF_CaveSpider";
            case "suniron_ingot" -> "MHF_Gold";
            case "duskcoal_ingot" -> "MHF_Creeper";
            case "aetherium_ingot" -> "MHF_Enderman";
            case "riftglass_ingot" -> "MHF_Wither";
            default -> "MHF_Question";
        };
        OfflinePlayer skin = Bukkit.getOfflinePlayer(skinName);
        meta.setOwningPlayer(skin);
        head.setItemMeta(meta);
        return head;
    }

    private void registerRecipes() {
        registerRecipe("rift_ingot", new String[]{"ABA", "BCB", "ABA"}, Map.of('A', Material.AMETHYST_SHARD, 'B', Material.IRON_INGOT, 'C', Material.ECHO_SHARD));
        registerRecipe("rift_helm", new String[]{"ABA", "BCB", " B "}, Map.of('A', Material.OBSIDIAN, 'B', Material.NETHERITE_SCRAP, 'C', Material.AMETHYST_SHARD));
        registerRecipe("verdant_core", new String[]{"ABA", "BCB", "ABA"}, Map.of('A', Material.MOSS_BLOCK, 'B', Material.GOLD_NUGGET, 'C', Material.HEART_OF_THE_SEA));
        registerRecipe("aether_blade", new String[]{"  A", " AB", "C  "}, Map.of('A', Material.PRISMARINE_CRYSTALS, 'B', Material.AMETHYST_SHARD, 'C', Material.STICK));
        registerRecipe("rift_bow", new String[]{" AB", "C B", " AB"}, Map.of('A', Material.ECHO_SHARD, 'B', Material.STRING, 'C', Material.STICK));
        registerRecipe("healing_tonic", new String[]{" A ", " B ", " C "}, Map.of('A', Material.GHAST_TEAR, 'B', Material.HONEY_BOTTLE, 'C', Material.GLASS_BOTTLE));
        registerRecipe("stormcaller_staff", new String[]{"  A", " B ", "C  "}, Map.of('A', Material.LIGHTNING_ROD, 'B', Material.BLAZE_ROD, 'C', Material.AMETHYST_SHARD));
        registerRecipe("glacier_orb", new String[]{"ABA", "BCB", "ABA"}, Map.of('A', Material.SNOW_BLOCK, 'B', Material.ICE, 'C', Material.DIAMOND));
        registerRecipe("emberheart", new String[]{"ABA", "BCB", "ABA"}, Map.of('A', Material.BLAZE_POWDER, 'B', Material.MAGMA_CREAM, 'C', Material.FIRE_CHARGE));
        registerRecipe("thornlash_whip", new String[]{"A  ", " BA", "  B"}, Map.of('A', Material.VINE, 'B', Material.STICK));
        registerRecipe("shadowstep_dagger", new String[]{" A ", " B ", " C "}, Map.of('A', Material.ENDER_PEARL, 'B', Material.IRON_INGOT, 'C', Material.STICK));
        registerRecipe("sunfire_charm", new String[]{"ABA", "BCB", "ABA"}, Map.of('A', Material.GLOWSTONE_DUST, 'B', Material.GOLD_INGOT, 'C', Material.SUNFLOWER));
        registerRecipe("tidal_scepter", new String[]{"  A", " B ", "C  "}, Map.of('A', Material.HEART_OF_THE_SEA, 'B', Material.PRISMARINE_CRYSTALS, 'C', Material.STICK));
        registerRecipe("soul_anchor", new String[]{"ABA", "BCB", "ABA"}, Map.of('A', Material.SOUL_SAND, 'B', Material.SOUL_LANTERN, 'C', Material.ECHO_SHARD));
        registerRecipe("gravity_maul", new String[]{"ABA", " B ", " B "}, Map.of('A', Material.OBSIDIAN, 'B', Material.IRON_INGOT));
        registerRecipe("phoenix_feather", new String[]{"ABA", "BCB", "ABA"}, Map.of('A', Material.FEATHER, 'B', Material.BLAZE_POWDER, 'C', Material.GHAST_TEAR));
        registerRecipe("frostbite_blade", new String[]{" A ", " A ", " B "}, Map.of('A', Material.BLUE_ICE, 'B', Material.STICK));
        registerRecipe("arcane_focus", new String[]{" A ", " B ", " C "}, Map.of('A', Material.AMETHYST_SHARD, 'B', Material.BLAZE_ROD, 'C', Material.STICK));
        registerRecipe("void_reaper", new String[]{" AA", " BA", "B  "}, Map.of('A', Material.ECHO_SHARD, 'B', Material.NETHERITE_INGOT));
        registerRecipe("verdant_buckler", new String[]{"ABA", "ABA", " A "}, Map.of('A', Material.MOSS_BLOCK, 'B', Material.IRON_INGOT));
        registerRecipe("thunder_rune", new String[]{"ABA", "BCB", "ABA"}, Map.of('A', Material.COPPER_INGOT, 'B', Material.REDSTONE, 'C', Material.LIGHTNING_ROD));
        registerRecipe("bloodmoon_amulet", new String[]{"ABA", "BCB", "ABA"}, Map.of('A', Material.REDSTONE, 'B', Material.GOLD_NUGGET, 'C', Material.RED_DYE));
        registerRecipe("astral_compass", new String[]{" A ", "ABA", " A "}, Map.of('A', Material.AMETHYST_SHARD, 'B', Material.COMPASS));
        registerRecipe("riftsurge_crystal", new String[]{"ABA", "BCB", "ABA"}, Map.of('A', Material.AMETHYST_SHARD, 'B', Material.CRYING_OBSIDIAN, 'C', Material.END_CRYSTAL));
        registerRecipe("echoing_horn", new String[]{" AA", " BA", "B  "}, Map.of('A', Material.GOAT_HORN, 'B', Material.COPPER_INGOT));
        registerRecipe("crystal_bomb", new String[]{"ABA", "BCB", "ABA"}, Map.of('A', Material.AMETHYST_SHARD, 'B', Material.GUNPOWDER, 'C', Material.TNT));
        registerRecipe("warding_totem", new String[]{"ABA", "BCB", "ABA"}, Map.of('A', Material.GOLD_INGOT, 'B', Material.MOSS_BLOCK, 'C', Material.TOTEM_OF_UNDYING));
        registerRecipe("mana_flask", new String[]{" A ", " B ", " C "}, Map.of('A', Material.LAPIS_LAZULI, 'B', Material.HONEY_BOTTLE, 'C', Material.GLASS_BOTTLE));
        registerRecipe("starfall_lance", new String[]{"  A", " B ", "C  "}, Map.of('A', Material.NETHER_STAR, 'B', Material.BLAZE_ROD, 'C', Material.NETHERITE_INGOT));
        registerRecipe("mirror_shard", new String[]{"ABA", "BCB", "ABA"}, Map.of('A', Material.GLASS_PANE, 'B', Material.IRON_INGOT, 'C', Material.AMETHYST_SHARD));
        registerRecipe("runic_gauntlet", new String[]{"ABA", "BCB", "ABA"}, Map.of('A', Material.GOLD_INGOT, 'B', Material.LEATHER, 'C', Material.EXPERIENCE_BOTTLE));
        registerRecipe("dreamweaver", new String[]{"ABA", "BCB", "ABA"}, Map.of('A', Material.PHANTOM_MEMBRANE, 'B', Material.STRING, 'C', Material.DIAMOND));
        registerRecipe("abyssal_crown", new String[]{"ABA", "BCB", "ABA"}, Map.of('A', Material.CRYING_OBSIDIAN, 'B', Material.GOLD_INGOT, 'C', Material.WITHER_SKELETON_SKULL));
    }

    private void registerRecipe(String id, String[] shape, Map<Character, Material> ingredients) {
        ItemStack result = createRpgItem(id, 1);
        if (result == null) return;
        NamespacedKey key = new NamespacedKey(this, "recipe_" + id);
        Bukkit.removeRecipe(key);
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(shape);
        ingredients.forEach(recipe::setIngredient);
        Bukkit.addRecipe(recipe);
    }

    private File storageFile(Player player) {
        return new File(new File(getDataFolder(), "storage"), player.getUniqueId() + ".yml");
    }

    private void openStorageMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, color("&0Eclyria | Personal Storage"));
        File file = storageFile(player);
        if (file.exists()) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            for (int slot = 0; slot < 54; slot++) {
                ItemStack item = yaml.getItemStack("slots." + slot);
                if (item != null) inv.setItem(slot, item);
            }
        }
        storageOwners.put(player.getUniqueId(), player.getUniqueId().toString());
        player.openInventory(inv);
    }

    @EventHandler
    public void onStorageClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        String title = ChatColor.stripColor(event.getView().getTitle());
        if (title == null || !title.equals("Eclyria | Personal Storage")) return;
        storageOwners.remove(player.getUniqueId());
        File file = storageFile(player);
        File parent = file.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            getLogger().warning("Could not create personal storage folder for " + player.getName());
            return;
        }
        YamlConfiguration yaml = new YamlConfiguration();
        Inventory inv = event.getInventory();
        for (int slot = 0; slot < 54; slot++) if (inv.getItem(slot) != null) yaml.set("slots." + slot, inv.getItem(slot));
        try { yaml.save(file); } catch (Exception ex) { getLogger().warning("Could not save storage for " + player.getName() + ": " + ex.getMessage()); }
    }

    private void openSkinsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, color("&0Eclyria | Skins"));
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, menuItem(Material.BLACK_STAINED_GLASS_PANE, "&0 "));
        inv.setItem(10, cosmeticHead("MHF_Enderman", "&5Rift Visage", "rift_helm", "&7Equip as a cosmetic helmet."));
        inv.setItem(13, cosmeticHead("MHF_OakLog", "&aVerdant Crown", "verdant_core", "&7A forest-themed head cosmetic."));
        inv.setItem(16, cosmeticHead("MHF_Blaze", "&6Ember Mask", "rift_ingot", "&7A fiery head cosmetic."));
        inv.setItem(31, menuItem(Material.ARROW, "&eBack to Menu"));
        player.openInventory(inv);
    }

    private ItemStack cosmeticHead(String skin, String name, String id, String lore) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(skin));
        meta.setDisplayName(color(name));
        meta.setLore(List.of(color(lore), color("&eClick to equip")));
        meta.getPersistentDataContainer().set(collectionKey, PersistentDataType.STRING, id);
        head.setItemMeta(meta);
        return head;
    }

    private void openCollectionsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, color("&0Eclyria | Collections"));
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, menuItem(Material.BLACK_STAINED_GLASS_PANE, "&0 "));
        String[] ids = {"rift_shard", "aetherite", "voidglass", "rift_ingot", "rift_helm", "verdant_core", "aether_blade", "rift_bow", "healing_tonic",
            "stormcaller_staff","glacier_orb","emberheart","thornlash_whip","shadowstep_dagger","sunfire_charm","tidal_scepter","soul_anchor","gravity_maul","phoenix_feather","frostbite_blade","arcane_focus","void_reaper","verdant_buckler","thunder_rune","bloodmoon_amulet","astral_compass","riftsurge_crystal","echoing_horn","crystal_bomb","warding_totem","mana_flask","starfall_lance","mirror_shard","runic_gauntlet","dreamweaver","abyssal_crown"};
        int found = 0;
        for (String id : ids) if (hasCollected(player, id)) found++;
        inv.setItem(4, menuItem(Material.EMERALD, "&aEclyria Collection", "&7Discovered items: &f" + found + "/" + ids.length, "&7Collect items in your inventory to register them."));
        Material[] icons = {Material.AMETHYST_SHARD, Material.PRISMARINE_CRYSTALS, Material.ECHO_SHARD, Material.PLAYER_HEAD, Material.PLAYER_HEAD, Material.PLAYER_HEAD, Material.NETHERITE_SWORD, Material.BOW, Material.HONEY_BOTTLE};
        for (int i = 0; i < ids.length; i++) {
            boolean has = hasCollected(player, ids[i]);
            ItemStack item = createRpgItem(ids[i], 1);
            if (item == null) item = new ItemStack(icons[i]);
            ItemMeta meta = item.getItemMeta();
            meta.setLore(List.of(color(has ? "&aDiscovered" : "&cNot discovered"), color("&8ID: " + ids[i])));
            item.setItemMeta(meta);
            inv.setItem(10 + i, item);
        }
        inv.setItem(31, menuItem(Material.ARROW, "&eBack to Menu"));
        player.openInventory(inv);
    }

    private boolean hasCollected(Player player, String id) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || !item.hasItemMeta()) continue;
            String found = item.getItemMeta().getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
            if (id.equals(found)) return true;
        }
        return false;
    }

    private void openRecipeMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, color("&0Eclyria | Crafting"));
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, menuItem(Material.BLACK_STAINED_GLASS_PANE, "&0 "));
        String[] ids = itemMaterials.keySet().toArray(new String[0]);
        Arrays.sort(ids);
        for (int i = 0; i < ids.length; i++) {
            ItemStack result = createRpgItem(ids[i], 1);
            if (result != null) {
                ItemMeta meta = result.getItemMeta();
                List<String> lore = new ArrayList<>(meta.hasLore() ? meta.getLore() : List.of());
                lore.add(color("&eRecipe registered at crafting table"));
                meta.setLore(lore);
                result.setItemMeta(meta);
                inv.setItem(10 + i, result);
            }
        }
        inv.setItem(31, menuItem(Material.ARROW, "&eBack to Menu"));
        player.openInventory(inv);
    }

    private void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, color("&0Eclyria &8| &5Riftfall"));
        for (int i = 0; i < inv.getSize(); i++) {
            boolean border = i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8;
            inv.setItem(i, menuItem(border ? Material.BLACK_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE, "&0 "));
        }
        inv.setItem(4, menuItem(Material.NETHER_STAR, "&d&lECLYRIA: RIFTFALL", "&7Your adventure begins in Havenfall.", "&8RPG Adventure"));
        inv.setItem(10, menuItem(Material.PLAYER_HEAD, "&dPlayer Profile", "&7View your class, level and XP."));
        inv.setItem(11, menuItem(Material.IRON_CHESTPLATE, "&cChoose Class", "&7Select your adventurer class."));
        inv.setItem(12, menuItem(Material.BOOK, "&bCharacter Stats", "&7View your current progression."));
        inv.setItem(13, menuItem(Material.ENCHANTED_BOOK, "&5Skills & Abilities", "&7Skill menu foundation.", "&8Skill progression arrives in a later update."));
        inv.setItem(14, menuItem(Material.FILLED_MAP, "&aWorld Map", "&7View regions and travel between them."));
        inv.setItem(15, menuItem(Material.WRITABLE_BOOK, "&eQuest Journal", "&7Review your adventure objectives.", "&8Quest system is not active in Alpha 2."));
        inv.setItem(16, menuItem(Material.KNOWLEDGE_BOOK, "&5Item Browser", "&7Browse resources, weapons and gear."));
        inv.setItem(19, menuItem(Material.CHEST, "&6Personal Storage", "&7A private 54-slot vault.", "&eClick to open"));
        inv.setItem(21, menuItem(Material.PLAYER_HEAD, "&dSkin & Cosmetic Menu", "&7Equip collectible head cosmetics."));
        inv.setItem(23, menuItem(Material.EMERALD, "&aCollections", "&7Track collected Eclyria items."));
        inv.setItem(27, menuItem(Material.CRAFTING_TABLE, "&eCrafting & Recipes", "&7View registered item recipes."));
        inv.setItem(25, menuItem(Material.SPAWNER, "&cMob Bestiary", "&7View the current test mobs.", "&7Admins can click a mob to spawn it."));
        inv.setItem(30, menuItem(Material.LAPIS_LAZULI, "&bMana & Divine Revelation", "&7Mana: &f" + getMana(player), "&7Rank: &d" + getConfig().getString("divine." + player.getUniqueId() + ".rank", "Unrevealed"), "&eVisit the priest to unlock your revelation."));
        inv.setItem(20, menuItem(Material.COMPASS, "&aReturn to Havenfall", "&7Return to the central sanctuary."));
        inv.setItem(22, menuItem(Material.COMPARATOR, "&6Settings", "&7Toggle GUI click sounds."));
        inv.setItem(24, menuItem(Material.PAPER, "&fHelp & Controls", "&7View available Alpha 2 actions."));
        if (player.hasPermission("eclyria.admin")) inv.setItem(31, menuItem(Material.COMMAND_BLOCK, "&cAdmin Dashboard", "&7World setup and region spawn tools."));
        inv.setItem(49, menuItem(Material.BARRIER, "&cClose Menu"));
        player.openInventory(inv);
    }

    private void openClassMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, color("&0Eclyria &8| &cChoose Class"));
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, menuItem(Material.BLACK_STAINED_GLASS_PANE, "&0 "));
        String current = "vanguard";
        try { current = getProfile(player).classId; } catch (SQLException ignored) { }
        inv.setItem(10, menuItem(Material.IRON_CHESTPLATE, "&cVanguard", "&7A sturdy frontline defender.", "&7Role: &fDefense", current.equals("vanguard") ? "&aCurrently selected" : "&eClick to select"));
        inv.setItem(12, menuItem(Material.BLAZE_POWDER, "&6Arcanist", "&7A scholar of volatile magic.", "&7Role: &fMagic", current.equals("arcanist") ? "&aCurrently selected" : "&eClick to select"));
        inv.setItem(14, menuItem(Material.FEATHER, "&aRanger", "&7A precise ranged combatant.", "&7Role: &fRanged", current.equals("ranger") ? "&aCurrently selected" : "&eClick to select"));
        inv.setItem(16, menuItem(Material.GLOWSTONE, "&eLuminary", "&7A beacon of radiant power.", "&7Role: &fSupport", current.equals("luminary") ? "&aCurrently selected" : "&eClick to select"));
        inv.setItem(31, menuItem(Material.ARROW, "&eBack to Menu"));
        player.openInventory(inv);
    }

    private void openStatsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, color("&0Eclyria &8| &bCharacter Stats"));
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, menuItem(Material.BLACK_STAINED_GLASS_PANE, "&0 "));
        try {
            Profile p = getProfile(player);
            inv.setItem(11, menuItem(Material.PLAYER_HEAD, "&f" + p.name, "&7Adventurer identity"));
            inv.setItem(13, menuItem(Material.NETHER_STAR, "&dLevel " + p.level, "&7Total XP: &f" + p.xp, "&7XP to next level: &f" + Math.max(0, (long) p.level * 100 - p.xp)));
            inv.setItem(15, menuItem(Material.IRON_CHESTPLATE, "&cClass: " + title(p.classId), "&7Class selection is available in the menu."));
        } catch (SQLException ex) {
            inv.setItem(13, menuItem(Material.BARRIER, "&cStats unavailable", "&7Check the server console."));
        }
        inv.setItem(22, menuItem(Material.ARROW, "&eBack to Menu"));
        player.openInventory(inv);
    }

    private void openSkillsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, color("&0Eclyria &8| &5Skills"));
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, menuItem(Material.BLACK_STAINED_GLASS_PANE, "&0 "));
        inv.setItem(11, menuItem(Material.AMETHYST_SHARD, "&cCombat Skills", "&7Combat abilities are not active yet."));
        inv.setItem(13, menuItem(Material.ENCHANTED_BOOK, "&5Class Mastery", "&7Your class is saved in your profile.", "&7Skill trees will be added in a later update."));
        inv.setItem(15, menuItem(Material.EXPERIENCE_BOTTLE, "&aProgression", "&7Current level and XP are shown in Character Stats."));
        inv.setItem(31, menuItem(Material.ARROW, "&eBack to Menu"));
        player.openInventory(inv);
    }

    private void openQuestMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, color("&0Eclyria &8| &eQuest Journal"));
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, menuItem(Material.BLACK_STAINED_GLASS_PANE, "&0 "));
        inv.setItem(11, menuItem(Material.WRITABLE_BOOK, "&eNo Active Quests", "&7The quest system is not enabled in Alpha 2.", "&7This journal will display objectives in a future update."));
        inv.setItem(15, menuItem(Material.MAP, "&bExplore the World", "&7Visit Havenfall, Verdant Reach and Riftlands.", "&eClick to open the world map"));
        inv.setItem(22, menuItem(Material.ARROW, "&eBack to Menu"));
        player.openInventory(inv);
    }

    private void openSettingsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, color("&0Eclyria &8| &6Settings"));
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, menuItem(Material.BLACK_STAINED_GLASS_PANE, "&0 "));
        boolean sounds = getConfig().getBoolean("player-settings." + player.getUniqueId() + ".gui-sounds", true);
        inv.setItem(13, menuItem(sounds ? Material.NOTE_BLOCK : Material.BARRIER, "&6GUI Click Sounds", "&7Status: " + (sounds ? "&aEnabled" : "&cDisabled"), "&eClick to toggle"));
        inv.setItem(22, menuItem(Material.ARROW, "&eBack to Menu"));
        player.openInventory(inv);
    }

    private void openHelpMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, color("&0Eclyria &8| &fHelp"));
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, menuItem(Material.BLACK_STAINED_GLASS_PANE, "&0 "));
        inv.setItem(10, menuItem(Material.COMPASS, "&aWorld Travel", "&7Use World Map to visit configured region spawns."));
        inv.setItem(12, menuItem(Material.PLAYER_HEAD, "&dCharacter", "&7Profile, class and stats menus are available."));
        inv.setItem(14, menuItem(Material.KNOWLEDGE_BOOK, "&5Item Browser", "&7Browse the current resource item identities."));
        inv.setItem(16, menuItem(Material.COMMAND_BLOCK, "&cAdmin Tools", "&7Admins can set region spawns from the dashboard."));
        inv.setItem(22, menuItem(Material.ARROW, "&eBack to Menu"));
        player.openInventory(inv);
    }

    private void openProfileMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, color("&0Eclyria &8| &dProfile"));
        try {
            Profile p = getProfile(player);
            inv.setItem(11, menuItem(Material.PLAYER_HEAD, "&f" + p.name, "&7Your adventurer profile"));
            inv.setItem(13, menuItem(Material.NETHER_STAR, "&dLevel " + p.level, "&7Total XP: &f" + p.xp, "&7Class: &f" + title(p.classId)));
            inv.setItem(15, menuItem(Material.WRITABLE_BOOK, "&bClass: " + title(p.classId), "&7Use /eclyria class <class> to change."));
        } catch (SQLException ex) {
            inv.setItem(13, menuItem(Material.BARRIER, "&cProfile unavailable", "&7Check the server console."));
        }
        inv.setItem(22, menuItem(Material.ARROW, "&eBack"));
        player.openInventory(inv);
    }

    private void openRegionsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, color("&0Eclyria &8| &bRegions"));
        inv.setItem(10, menuItem(Material.BEACON, "&dHavenfall", "&7The central sanctuary", "&eClick to travel"));
        inv.setItem(12, menuItem(Material.MOSS_BLOCK, "&aVerdant Reach", "&7Overgrown ruins, winding paths and a buried shrine", "&eClick to travel"));
        inv.setItem(14, menuItem(Material.MAGMA_BLOCK, "&6Ember Rift", "&7Scorched ruins, a sunken dungeon and boss arena", "&eClick to travel"));
        inv.setItem(16, menuItem(Material.CRYING_OBSIDIAN, "&5Void Rift", "&7A fractured wasteland and ancient arena", "&eClick to travel"));
        inv.setItem(22, menuItem(Material.ARROW, "&eBack"));
        player.openInventory(inv);
    }

    private void openRiftForge(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, color("&0Eclyria | Rift Forge"));
        for (int i=0;i<54;i++) inv.setItem(i, menuItem(Material.BLACK_STAINED_GLASS_PANE, "&0 "));
        String[] ids = {"verdantite_ingot","emberstone_ingot","voidsilver_ingot","stormsteel_ingot","frostquartz_ingot","soulcopper_ingot","suniron_ingot","duskcoal_ingot","aetherium_ingot","riftglass_ingot"};
        for (int i=0;i<ids.length;i++) {
            String oreId = ids[i].replace("_ingot", "_ore");
            ItemStack output = createRpgItem(ids[i], 1);
            ItemMeta meta = output.getItemMeta();
            List<String> lore = new ArrayList<>(meta.hasLore()?meta.getLore():List.of());
            lore.add(color("&eCost: 4x " + title(oreId.replace('_',' '))));
            lore.add(color("&aClick to forge")); meta.setLore(lore); output.setItemMeta(meta);
            int[] forgeSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21};
            inv.setItem(forgeSlots[i], output);
        }
        inv.setItem(31, menuItem(Material.ANVIL, "&6Rift Refinement", "&7Select an ingot above.", "&7Cost: 4 matching raw ores", "&7Output: 1 refined ingot"));
        inv.setItem(49, menuItem(Material.ARROW,"&eBack to Menu"));
        player.openInventory(inv);
    }

    private boolean consumeRpgItems(Player player, String id, int amount) {
        int total=0;
        for (ItemStack stack: player.getInventory().getStorageContents()) {
            if (stack==null || !stack.hasItemMeta()) continue;
            String found=stack.getItemMeta().getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
            if (id.equals(found)) total += stack.getAmount();
        }
        if (total<amount) return false;
        int remaining=amount;
        for (int slot=0;slot<player.getInventory().getStorageContents().length && remaining>0;slot++) {
            ItemStack stack=player.getInventory().getItem(slot);
            if (stack==null || !stack.hasItemMeta()) continue;
            String found=stack.getItemMeta().getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
            if (!id.equals(found)) continue;
            int take=Math.min(remaining,stack.getAmount()); remaining-=take;
            if (take==stack.getAmount()) player.getInventory().setItem(slot,null); else stack.setAmount(stack.getAmount()-take);
        }
        return true;
    }

    private void openItemBrowser(Player player) {
        if (!player.hasPermission("eclyria.admin")) { tell(player, "&cThe item browser is admin-only."); return; }
        int page = Math.max(0, itemBrowserPages.getOrDefault(player.getUniqueId(), 0));
        Inventory inv = Bukkit.createInventory(null, 54, color("&0Eclyria | Item Browser"));
        for (int i=0;i<54;i++) inv.setItem(i, menuItem(Material.BLACK_STAINED_GLASS_PANE, "&0 "));
        List<String> ids = new ArrayList<>(itemMaterials.keySet()); Collections.sort(ids);
        int start = page * 45;
        for (int i=0;i<45 && start+i<ids.size();i++) {
            ItemStack item=createRpgItem(ids.get(start+i),1);
            if (item!=null) inv.setItem(i,item);
        }
        if (page>0) inv.setItem(48, menuItem(Material.ARROW,"&ePrevious Page"));
        inv.setItem(49, menuItem(Material.BARRIER, "&eBack"));
        if (start+45<ids.size()) inv.setItem(50, menuItem(Material.ARROW,"&eNext Page"));
        inv.setItem(52, menuItem(Material.PAPER,"&7Page " + (page+1),"&7Items: " + ids.size()));
        player.openInventory(inv);
    }

    private void openMobBestiary(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, color("&0Eclyria &8| &cBestiary"));
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, menuItem(Material.BLACK_STAINED_GLASS_PANE, "&0 "));
        inv.setItem(10, menuItem(Material.ENDERMITE_SPAWN_EGG, "&5Riftling", "&7Small creature from unstable rifts.", "&7ID: &friftling", player.hasPermission("eclyria.admin") ? "&eClick to spawn nearby" : "&8Admin spawn only"));
        inv.setItem(12, menuItem(Material.HUSK_SPAWN_EGG, "&cRift Stalker", "&7A fast hostile wanderer.", "&7ID: &frift_stalker", player.hasPermission("eclyria.admin") ? "&eClick to spawn nearby" : "&8Admin spawn only"));
        inv.setItem(14, menuItem(Material.MOSS_BLOCK, "&aVerdant Sentinel", "&7A guardian of overgrown ruins.", "&7ID: &fverdant_sentinel", player.hasPermission("eclyria.admin") ? "&eClick to spawn nearby" : "&8Admin spawn only"));
        inv.setItem(16, menuItem(Material.CRYING_OBSIDIAN, "&5Rift Guardian", "&7An elite guardian for testing.", "&7ID: &frift_guardian", player.hasPermission("eclyria.admin") ? "&eClick to spawn nearby" : "&8Admin spawn only"));
        inv.setItem(19, menuItem(Material.VEX_SPAWN_EGG, "&dVoid Wraith", "&7A drifting rift spirit.", "&7ID: &fvoid_wraith", player.hasPermission("eclyria.admin") ? "&eClick to spawn nearby" : "&8Admin spawn only"));
        inv.setItem(21, menuItem(Material.RAVAGER_SPAWN_EGG, "&6Crystal Brute", "&7A heavily armoured crystal beast.", "&7ID: &fcrystal_brute", player.hasPermission("eclyria.admin") ? "&eClick to spawn nearby" : "&8Admin spawn only"));
        inv.setItem(23, menuItem(Material.WOLF_SPAWN_EGG, "&cAsh Hound", "&7A fast hunter from the scorched edges.", "&7ID: &fash_hound", player.hasPermission("eclyria.admin") ? "&eClick to spawn nearby" : "&8Admin spawn only"));
        inv.setItem(25, menuItem(Material.EVOKER_SPAWN_EGG, "&bAether Mage", "&7A spellcaster guarding ancient relics.", "&7ID: &faether_mage", player.hasPermission("eclyria.admin") ? "&eClick to spawn nearby" : "&8Admin spawn only"));
        inv.setItem(31, menuItem(Material.ARROW, "&eBack to Menu"));
        player.openInventory(inv);
    }

    private boolean spawnEclyriaMob(String id, Location origin, Player target) {
        EntityType type;
        String display;
        double health;
        double damage;
        double speed;
        switch (id.toLowerCase(Locale.ROOT)) {
            case "riftling" -> { type = EntityType.ENDERMITE; display = "&5Riftling"; health = 16; damage = 3; speed = 0.32; }
            case "rift_stalker" -> { type = EntityType.HUSK; display = "&cRift Stalker"; health = 30; damage = 5; speed = 0.29; }
            case "verdant_sentinel" -> { type = EntityType.ZOMBIE; display = "&aVerdant Sentinel"; health = 48; damage = 8; speed = 0.27; }
            case "rift_guardian" -> { type = EntityType.ZOMBIE; display = "&5&lRift Guardian"; health = 100; damage = 12; speed = 0.30; }
            case "void_wraith" -> { type = EntityType.VEX; display = "&dVoid Wraith"; health = 26; damage = 7; speed = 0.34; }
            case "crystal_brute" -> { type = EntityType.RAVAGER; display = "&6Crystal Brute"; health = 140; damage = 16; speed = 0.23; }
            case "ash_hound" -> { type = EntityType.WOLF; display = "&cAsh Hound"; health = 38; damage = 8; speed = 0.34; }
            case "aether_mage" -> { type = EntityType.EVOKER; display = "&bAether Mage"; health = 75; damage = 10; speed = 0.25; }
            default -> { return false; }
        }
        Location spawnAt = origin.clone().add(1.5, 0, 1.5);
        LivingEntity mob = (LivingEntity) origin.getWorld().spawnEntity(spawnAt, type);
        mob.setCustomName(color(display + " &8[&c" + (int) health + " HP&8]"));
        mob.setCustomNameVisible(true);
        mob.setPersistent(true);
        mob.setRemoveWhenFarAway(false);
        mob.getPersistentDataContainer().set(mobKey, PersistentDataType.STRING, id.toLowerCase(Locale.ROOT));
        if (mob.getAttribute(Attribute.MAX_HEALTH) != null) mob.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
        mob.setHealth(health);
        if (mob.getAttribute(Attribute.ATTACK_DAMAGE) != null) mob.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        if (mob.getAttribute(Attribute.MOVEMENT_SPEED) != null) mob.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
        EntityEquipment equipment = mob.getEquipment();
        if (equipment != null) {
            if (id.equalsIgnoreCase("verdant_sentinel")) {
                equipment.setHelmet(new ItemStack(Material.MOSS_BLOCK));
                equipment.setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
            } else if (id.equalsIgnoreCase("rift_guardian")) {
                equipment.setHelmet(new ItemStack(Material.CRYING_OBSIDIAN));
                equipment.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
                equipment.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
                equipment.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
                mob.setGlowing(true);
                mob.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
            } else if (id.equalsIgnoreCase("rift_stalker")) {
                equipment.setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
            }
            equipment.setHelmetDropChance(0.0f);
            equipment.setChestplateDropChance(0.0f);
            equipment.setLeggingsDropChance(0.0f);
            equipment.setBootsDropChance(0.0f);
            equipment.setItemInMainHandDropChance(0.0f);
        }
        return true;
    }

    private void openAdminMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, color("&0Eclyria &8| &cAdmin"));
        inv.setItem(10, menuItem(Material.GRASS_BLOCK, "&aSet Havenfall Spawn", "&7Save your current position."));
        inv.setItem(12, menuItem(Material.MOSS_BLOCK, "&aSet Verdant Spawn", "&7Save your current position."));
        inv.setItem(14, menuItem(Material.CRYING_OBSIDIAN, "&5Set Riftlands Spawn", "&7Save your current position."));
        inv.setItem(16, menuItem(Material.STRUCTURE_BLOCK, "&eWorld Info", "&7Print loaded world status to chat."));
        inv.setItem(22, menuItem(Material.ARROW, "&eBack"));
        player.openInventory(inv);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = ChatColor.stripColor(event.getView().getTitle());
        if (title == null || !title.startsWith("Eclyria")) return;
        if (title.equals("Eclyria | Personal Storage")) return;
        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) return;
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;
        String name = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        if (name == null) return;
        if (getConfig().getBoolean("player-settings." + player.getUniqueId() + ".gui-sounds", true)) {
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.45f, 1.15f);
        }
        switch (title) {
            case "Eclyria | Divine Revelation" -> {
                if (name.equals("Back to Menu")) { openMainMenu(player); return; }
                if (name.equals("Receive Divine Revelation")) {
                    if (!priestAccess.remove(player.getUniqueId())) { tell(player, "&cSpeak with the Divine Priest first."); return; }
                    if (!player.isOp()) {
                        int sacrifice = 0;
                        for (ItemStack stack : player.getInventory().getStorageContents()) if (stack != null && stack.getType() == Material.DIAMOND_BLOCK) sacrifice += stack.getAmount();
                        if (sacrifice < 1) { tell(player, "&cThe priest requires one Diamond Block as a sacrifice."); priestAccess.add(player.getUniqueId()); return; }
                        for (int slot = 0; slot < player.getInventory().getStorageContents().length; slot++) {
                            ItemStack stack = player.getInventory().getItem(slot);
                            if (stack != null && stack.getType() == Material.DIAMOND_BLOCK) {
                                if (stack.getAmount() == 1) player.getInventory().setItem(slot, null); else stack.setAmount(stack.getAmount() - 1);
                                break;
                            }
                        }
                    }
                    grantRevelation(player); openDivineMenu(player); return;
                }
                if (name.equals("Meditate for Mana")) { setMana(player, getMaxMana(player)); tell(player, "&bYour mana has been fully restored."); openDivineMenu(player); return; }
            }
            case "Eclyria | Riftfall" -> {
                switch (name) {
                    case "Player Profile" -> openProfileMenu(player);
                    case "Choose Class" -> openClassMenu(player);
                    case "Character Stats" -> openStatsMenu(player);
                    case "Skills & Abilities" -> openSkillsMenu(player);
                    case "World Map" -> openRegionsMenu(player);
                    case "Quest Journal" -> openQuestMenu(player);
                    case "Item Browser" -> openItemBrowser(player);
                    case "Personal Storage" -> openStorageMenu(player);
                    case "Skin & Cosmetic Menu" -> openSkinsMenu(player);
                    case "Collections" -> openCollectionsMenu(player);
                    case "Crafting & Recipes" -> openRecipeMenu(player);
                    case "Mob Bestiary" -> openMobBestiary(player);
                    case "Mana & Divine Revelation" -> openDivineMenu(player);
                    case "Return to Havenfall" -> teleportRegion(player, "havenfall");
                    case "Settings" -> openSettingsMenu(player);
                    case "Help & Controls" -> openHelpMenu(player);
                    case "Admin Dashboard" -> { if (player.hasPermission("eclyria.admin")) openAdminMenu(player); }
                    case "Close Menu" -> player.closeInventory();
                }
            }
            case "Eclyria | Profile", "Eclyria | Character Stats", "Eclyria | Skills", "Eclyria | Quest Journal", "Eclyria | Help" -> {
                if (name.equals("Back") || name.equals("Back to Menu")) { openMainMenu(player); return; }
                if (title.endsWith("Quest Journal") && name.equals("Explore the World")) openRegionsMenu(player);
            }
            case "Eclyria | Choose Class" -> {
                if (name.equals("Back to Menu")) { openMainMenu(player); return; }
                String id = name.toLowerCase(Locale.ROOT);
                if (classes.contains(id)) {
                    try {
                        setClass(player, id);
                        tell(player, "&aYour class is now &f" + title(id) + "&a.");
                        openClassMenu(player);
                    } catch (Exception ex) {
                        tell(player, "&cCould not save your class. Check the console.");
                        getLogger().warning("Could not update class for " + player.getName() + ": " + ex.getMessage());
                    }
                }
            }
            case "Eclyria | Skins" -> {
                if (name.equals("Back to Menu")) { openMainMenu(player); return; }
                ItemStack clicked = event.getCurrentItem();
                if (clicked.getType() == Material.PLAYER_HEAD && clicked.hasItemMeta()) {
                    String id = clicked.getItemMeta().getPersistentDataContainer().get(collectionKey, PersistentDataType.STRING);
                    if (id != null) {
                        ItemStack cosmetic = createRpgItem(id, 1);
                        if (cosmetic != null && cosmetic.getType() == Material.PLAYER_HEAD) {
                            ItemStack old = player.getInventory().getHelmet();
                            if (old != null && old.getType() != Material.AIR) player.getInventory().addItem(old).values().forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left));
                            player.getInventory().setHelmet(cosmetic);
                            tell(player, "&aCosmetic equipped.");
                        }
                    }
                }
            }
            case "Eclyria | Collections" -> {
                if (name.equals("Back to Menu")) { openMainMenu(player); return; }
            }
            case "Eclyria | Crafting" -> {
                if (name.equals("Back to Menu")) { openMainMenu(player); return; }
            }
            case "Eclyria | Regions" -> {
                if (name.equals("Back")) { openMainMenu(player); return; }
                if (name.equals("Havenfall")) teleportRegion(player, "havenfall");
                else if (name.equals("Verdant Reach")) teleportRegion(player, "verdant");
                else if (name.equals("Ember Rift")) teleportRegion(player, "ember");
                else if (name.equals("Void Rift")) teleportRegion(player, "void");
                else if (name.equals("Riftlands")) teleportRegion(player, "riftlands");
            }
            case "Eclyria | Item Browser" -> {
                if (name.equals("Back")) { openMainMenu(player); return; }
                if (name.equals("Next Page")) { itemBrowserPages.merge(player.getUniqueId(),1,Integer::sum); openItemBrowser(player); return; }
                if (name.equals("Previous Page")) { itemBrowserPages.compute(player.getUniqueId(),(u,p)->Math.max(0,(p==null?0:p)-1)); openItemBrowser(player); return; }
                if (!player.hasPermission("eclyria.admin")) return;
                ItemStack clicked = event.getCurrentItem();
                String id = clicked.getItemMeta().getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
                if (id != null) {
                    HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(createRpgItem(id, 1));
                    overflow.values().forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left));
                    tell(player, "&aTest item granted: &f" + id + "&a.");
                }
            }
            case "Eclyria | Rift Forge" -> {
                if (name.equals("Back to Menu")) { openMainMenu(player); return; }
                ItemStack clicked=event.getCurrentItem();
                String id=clicked.getItemMeta().getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
                if (id==null || !id.endsWith("_ingot")) return;
                String oreId=id.replace("_ingot","_ore");
                if (!consumeRpgItems(player, oreId, 4)) { tell(player,"&cYou need 4 " + title(oreId.replace('_',' ')) + " to forge this ingot."); return; }
                HashMap<Integer, ItemStack> overflow=player.getInventory().addItem(createRpgItem(id,1));
                overflow.values().forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(),left));
                player.playSound(player.getLocation(),Sound.BLOCK_ANVIL_USE,.8f,1.2f);
                tell(player,"&aForged &f"+title(id.replace('_',' '))+"&a.");
            }
            case "Eclyria | Settings" -> {
                if (name.equals("Back to Menu")) { openMainMenu(player); return; }
                if (name.equals("GUI Click Sounds")) {
                    String path = "player-settings." + player.getUniqueId() + ".gui-sounds";
                    boolean enabled = !getConfig().getBoolean(path, true);
                    getConfig().set(path, enabled);
                    saveConfig();
                    openSettingsMenu(player);
                }
            }
            case "Eclyria | Bestiary" -> {
                if (name.equals("Back to Menu")) { openMainMenu(player); return; }
                if (!player.hasPermission("eclyria.admin")) {
                    tell(player, "&7These creatures are view-only in the Alpha 2 bestiary.");
                    return;
                }
                String id = switch (name) {
                    case "Riftling" -> "riftling";
                    case "Rift Stalker" -> "rift_stalker";
                    case "Verdant Sentinel" -> "verdant_sentinel";
                    case "Rift Guardian" -> "rift_guardian";
                    case "Void Wraith" -> "void_wraith";
                    case "Crystal Brute" -> "crystal_brute";
                    case "Ash Hound" -> "ash_hound";
                    case "Aether Mage" -> "aether_mage";
                    default -> "";
                };
                if (!id.isEmpty() && spawnEclyriaMob(id, player.getLocation(), player)) {
                    tell(player, "&aSpawned test mob: &f" + title(id.replace('_', ' ')));
                }
            }
            case "Eclyria | Admin" -> {
                if (name.equals("Back")) { openMainMenu(player); return; }
                if (!player.hasPermission("eclyria.admin")) return;
                if (name.equals("Set Havenfall Spawn")) savePlayerSpawn(player, "havenfall");
                else if (name.equals("Set Verdant Spawn")) savePlayerSpawn(player, "verdant");
                else if (name.equals("Set Riftlands Spawn")) savePlayerSpawn(player, "riftlands");
                else if (name.equals("World Info")) {
                    World world = getRiftfallWorld();
                    tell(player, "&aWorld: &f" + (world == null ? "not loaded" : world.getName()));
                }
            }
        }
    }

    private void savePlayerSpawn(Player player, String region) {
        saveSpawn(region, player.getLocation());
        saveConfig();
        tell(player, "&aSaved " + title(region) + " spawn.");
        openAdminMenu(player);
    }

    private void teleportRegion(Player player, String region) {
        Location loc = readSpawn(region);
        if (loc == null) {
            tell(player, "&cThat region spawn is not configured. Ask an admin to run /eclyria world setup.");
            return;
        }
        player.closeInventory();
        player.teleport(loc);
        tell(player, "&aTravelled to &f" + title(region) + "&a.");
    }

    private boolean deny(CommandSender sender) {
        tell(sender, "&cYou do not have permission.");
        return true;
    }

    private String title(String s) {
        if (s == null || s.isBlank()) return "Unknown";
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1).toLowerCase(Locale.ROOT);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return filter(List.of("help", "gui", "world", "profile", "class", "xp", "item", "mob", "rift", "debug", "reload", "priest", "mana", "dialogue", "forge", "items"), args[0]);
        if (args.length == 2 && args[0].equalsIgnoreCase("dialogue")) return filter(List.of("revelation", "priest"), args[1]);
        if (args.length == 2 && args[0].equalsIgnoreCase("world")) return filter(List.of("info", "setup", "setspawn", "regenerate-rifts"), args[1]);
        if (args.length == 3 && args[0].equalsIgnoreCase("world") && args[1].equalsIgnoreCase("setspawn")) return filter(List.of("havenfall", "verdant", "ember", "void", "riftlands"), args[2]);
        if (args.length == 2 && args[0].equalsIgnoreCase("class")) return filter(new ArrayList<>(classes), args[1]);
        if (args.length == 2 && args[0].equalsIgnoreCase("item")) return filter(List.of("give"), args[1]);
        if (args.length == 2 && args[0].equalsIgnoreCase("rift")) return filter(List.of("start", "stop"), args[1]);
        if (args.length == 2 && args[0].equalsIgnoreCase("mob")) return filter(List.of("list", "spawn"), args[1]);
        if (args.length == 3 && args[0].equalsIgnoreCase("mob") && args[1].equalsIgnoreCase("spawn")) return filter(List.of("riftling", "rift_stalker", "verdant_sentinel", "rift_guardian"), args[2]);
        if (args.length == 4 && args[0].equalsIgnoreCase("mob") && args[1].equalsIgnoreCase("spawn")) return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[3]);
        if (args.length == 3 && args[0].equalsIgnoreCase("item") && args[1].equalsIgnoreCase("give")) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[2]);
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("item") && args[1].equalsIgnoreCase("give")) return filter(new ArrayList<>(itemMaterials.keySet()), args[3]);
        if (args.length == 3 && args[0].equalsIgnoreCase("rift") && args[1].equalsIgnoreCase("start")) return filter(List.of("1", "2", "3", "4", "5", "easy", "normal", "hard"), args[2]);
        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String input) {
        String needle = input.toLowerCase(Locale.ROOT);
        return options.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(needle)).sorted().toList();
    }
}
