package com.minecraft.savagemc.hungergames.game.list.events;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.player.PlayerTeamAssignEvent;
import com.minecraft.core.bukkit.event.player.PlayerUpdateTablistEvent;
import com.minecraft.core.bukkit.listener.SoupListener;
import com.minecraft.core.bukkit.util.inventory.Selector;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.database.data.Data;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Ranking;
import com.minecraft.core.server.ServerType;
import com.minecraft.core.translation.Language;
import com.minecraft.core.util.DateUtils;
import com.minecraft.core.util.ranking.RankingFactory;
import com.minecraft.core.util.ranking.RankingTarget;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.command.ScrimCommand;
import com.minecraft.savagemc.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.savagemc.hungergames.game.Game;
import com.minecraft.savagemc.hungergames.game.list.Event;
import com.minecraft.savagemc.hungergames.game.structure.BonusFeast;
import com.minecraft.savagemc.hungergames.game.structure.Feast;
import com.minecraft.savagemc.hungergames.game.structure.Minifeast;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.list.*;
import com.minecraft.savagemc.hungergames.user.kits.list.Blink;
import com.minecraft.savagemc.hungergames.user.kits.list.Monk;
import com.minecraft.savagemc.hungergames.user.kits.list.Ninja;
import com.minecraft.savagemc.hungergames.user.pattern.DieCause;
import com.minecraft.savagemc.hungergames.util.explication.Explication;
import com.minecraft.savagemc.hungergames.util.game.GameStage;
import com.minecraft.savagemc.hungergames.util.game.Loadable;
import com.minecraft.savagemc.hungergames.util.selector.Items;
import com.minecraft.savagemc.hungergames.util.stats.StatsApplier;
import com.minecraft.savagemc.hungergames.util.templatekit.scrim.KitType;
import com.minecraft.savagemc.hungergames.util.bo3.BO3;
import com.minecraft.savagemc.hungergames.util.bo3.BO3Block;
import com.minecraft.savagemc.hungergames.util.bo3.BO3Reader;
import com.minecraft.savagemc.hungergames.util.bo3.EmptyBO3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Loadable
public class Scrim extends Event {

    // Sistema de combate
    private final Map<UUID, CombatSession> activeCombats = new HashMap<>();
    private final Map<UUID, Long> lastCombatActivity = new HashMap<>();
    private final Map<UUID, Tag> originalTags = new HashMap<>();
    private final Map<Location, UUID> privateChests = new HashMap<>();

    // Sistema de combate com cor vermelha aplicada via event handler

    // Classe interna para gerenciar sessões de combate
    private static class CombatSession {
        private final UUID player1;
        private final UUID player2;
        private final long startTime;
        private long lastActivity;

        public CombatSession(UUID player1, UUID player2) {
            this.player1 = player1;
            this.player2 = player2;
            this.startTime = System.currentTimeMillis();
            this.lastActivity = this.startTime;
        }

        public UUID getPlayer1() { return player1; }
        public UUID getPlayer2() { return player2; }
        public long getStartTime() { return startTime; }
        public long getLastActivity() { return lastActivity; }
        public void updateActivity() { this.lastActivity = System.currentTimeMillis(); }

        public boolean containsPlayer(UUID player) {
            return player1.equals(player) || player2.equals(player);
        }

        public UUID getOtherPlayer(UUID player) {
            return player1.equals(player) ? player2 : player1;
        }
    }

    private final Explication KITSET = new Explication(5, "Sejam bem-vindos a §eLegion§f!",
            "Todos irão iniciar o jogo com um kit de itens.",
            "Vocês terão §e1 minuto§f de invencibilidade para se organizarem.",
            "É §cproibido§f interferir na luta de jogadores que não fazem parte do seu time.",
            "É §cproibido§f aguardar que uma luta seja finalizada com o intuito de atacar os vencedores.",
            "Não é permitido fazer spawn traps.",
            "Aos §e30 minutos §fde partida, vocês serão teleportados para uma Arena Final.",
            "Alguns kits estarão desativados durante todo o evento.",
            "Dúvidas, reclamações e revisões de kick devem ser solicitados em nosso §9Discord§f!",
            "Iniciaremos em alguns instantes! Bom jogo e boa sorte.");

    private final Explication MINE = new Explication(5, "Sejam bem-vindos a §eLegion§f!",
            "O evento será minerado, ou seja, você deve coletar seus itens para lutar.",
            "Vocês terão §e10 minutos§f de invencibilidade para coletar recursos.",
            "É §cproibido§f aguardar que uma luta seja finalizada com o intuito de atacar os vencedores.",
            "Não é permitido fazer spawn traps.",
            "Aos §e30 minutos §fde partida, vocês serão teleportados para uma Arena Final.",
            "Aos §e40 minutos §fde partida, será aplicado efeito de §7Wither II §fem todos os jogadores.",
            "Alguns kits estarão desativados durante todo o evento.",
            "Dúvidas, reclamações e revisões de kick devem ser solicitados em nosso §9Discord§f!",
            "Iniciaremos em alguns instantes! Bom jogo e boa sorte.");

    public Scrim(HungerGames hungerGames) {
        super(hungerGames);
        setName("Legion");
        setDisplay("LEGION");
        setupVariables();
        Constants.setServerType(ServerType.CHAMPION);
        getPlugin().getBukkitFrame().registerAdapter(KitType.class, KitType::parse);
        getPlugin().getAccountLoader().addColumns(Columns.SCRIM_KILLS, Columns.SCRIM_DEATHS, Columns.SCRIM_GAMES, Columns.SCRIM_MAX_GAME_KILLS, Columns.SCRIM_RANK, Columns.SCRIM_RANK_EXP);
        getPlugin().getBukkitFrame().registerCommands(new ScrimCommand());
    }

    @Override
    public void load() {
        getPlugin().setRankingFactory(new RankingFactory(RankingTarget.SCRIM));
        super.load();
        setupKits();
    }

    @Variable(name = "champion.type")
    public KitType type = KitType.COCOA;
    @Variable(name = "champion.apply_kit")
    public boolean applyKit = true;
    @Variable(name = "champion.auto_explication")
    public boolean explicate = true;
    @Variable(name = "champion.wait_time")
    public int waitTime = 300;
    @Variable(name = "champion.exp_multiplier")
    public double multiplier = 1.0;
    @Variable(name = "champion.anti_clean_system")
    public boolean antiClean = true;
    @Variable(name = "champion.combat_mode")
    public boolean combatMode = true;
    @Variable(name = "champion.combat_timeout")
    public int combatTimeout = 10;
    @Variable(name = "champion.private_chests")
    public boolean privateChestsEnabled = true;

    // Sistema de arena final
    private boolean finalArenaActive = false;
    private boolean finalArenaPvPDisabled = false;

    // Sistema RGB para scoreboard
    private int rgbIndex = 0;
    private final String[] rgbColors = {
            "§b§l", "§3§l", "§9§l", "§1§l", "§5§l", "§d§l", "§c§l", "§6§l", "§e§l", "§a§l", "§2§l", "§8§l"
    };

    @Override
    public void handleSidebar(User user) {
        Game game = this;

        GameStage stage = game.getStage();
        String time = format(game.getTime());
        String count = getCount();

        Account account = user.getAccount();
        GameScoreboard scoreboard = user.getScoreboard();

        Language language = account.getLanguage();
        Ranking ranking = account.getRanking();

        EventMode mode = getEventMode();

        if (scoreboard == null) //Preventing NPE
            return;

        scoreboard.updateTitle(rgbColors[rgbIndex] + game.getName().toUpperCase());

        List<String> scores = new ArrayList<>();
        scores.add(" ");

        if (language == Language.PORTUGUESE) {
            if (explicate && !getExplication().isStarted())
                scores.add("§fAguardando: §7" + time);
            else if (stage == GameStage.WAITING)
                scores.add("§fIniciando em: §7" + time);
            else if (stage == GameStage.INVINCIBILITY)
                scores.add("§fInvencível por: §7" + time);
            else
                scores.add("§fTempo: §7" + time);

            scores.add("§fJogadores: §7" + count);
        } else {
            if (explicate && !getExplication().isStarted())
                scores.add("§fWaiting: §7" + time);
            else if (stage == GameStage.WAITING)
                scores.add("§fStarting in: §7" + time);
            else if (stage == GameStage.INVINCIBILITY)
                scores.add("§fInvincible for: §7" + time);
            else
                scores.add("§fGame Time: §7" + time);

            scores.add("§fPlayers: §7" + count);
        }

        if (!user.isAlive()) {
            scores.add(" ");
            scores.add(user.isVanish() ? "§cMODO VANISH" : "§7MODO ESPECTADOR");
        } else {
            Iterator<Kit> iterator = user.getKitList().stream().filter(kit -> !kit.isNone()).iterator();
            int kitCount = 0;

            if (iterator.hasNext())
                scores.add(" ");

            while (iterator.hasNext()) {

                Kit kit = iterator.next();

                if (iterator.hasNext() || kitCount != 0) {
                    kitCount++;
                    scores.add("§fKit " + kitCount + ": §a" + kit.getDisplayName());
                } else
                    scores.add("§fKit: §a" + kit.getDisplayName());
            }

            if (stage == GameStage.PLAYING || stage == GameStage.VICTORY)
                scores.add("§fKills: §a" + user.getKills());

            // Mostra informação da dupla se estiver em uma
            if (user.getDuo() != null) {
                if (language == Language.PORTUGUESE)
                    scores.add("§fDupla: §a" + user.getDuo().getPlayer().getName());
                else
                    scores.add("§fDuo: §a" + user.getDuo().getPlayer().getName());
            }
        }

        scores.add("");

        if (mode != EventMode.NONE) {
            if (language == Language.PORTUGUESE)
                scores.add("§fModo: §a" + language.translate(mode.getKey()));
            else
                scores.add("§fType: §a" + language.translate(mode.getKey()));
        }

        scores.add("§fRanking: " + ranking.getColor() + ranking.getSymbol() + " " + ranking.getColor() + ranking.getName());
        scores.add("");

        scores.add("§e§oSavagemc.com.br");
        scoreboard.updateLines(scores);
    }

    @Override
    public void start() {
        getExplication().setStarted(true);
        getExplication().setDone(true);
        getVariable("champion.auto_explication").setActive(false);
        getVariable("champion.type").setActive(false);

        if (applyKit && type == KitType.MUSHROOM) {

            Iterator<Recipe> iterator = Bukkit.getServer().recipeIterator();

            while (iterator.hasNext()) {
                Recipe recipe = iterator.next();

                if (recipe.getResult().getType() == Material.MUSHROOM_SOUP) {

                    ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
                    List<Material> ingredients = shapelessRecipe.getIngredientList().stream().map(ItemStack::getType).collect(Collectors.toList());

                    if (ingredients.contains(Material.INK_SACK) || ingredients.contains(Material.CACTUS))
                        iterator.remove();
                }
            }
        }

        super.start();
    }

    @Override
    public void execute() {
        int time = getTime();
        GameStage gameStage = getStage();

        Explication explication = getExplication();

        boolean broadcast = time != 0 && time % 30 == 0 || time != 0 && time % 5 == 0 && time <= 15 || time != 0 && time <= 5;

        if (gameStage == GameStage.WAITING) {

            boolean mayExplicate = explicate && !explication.isStarted();

            if (mayExplicate)
                broadcast = false;

            if (getPlugin().getWhitelist().isActive()) {
                setTime(waitTime);
                return;
            }

            if (broadcast)
                broadcast("hg.starting.broadcast", gameStage, time);

            if (time == 15 && !mayExplicate) {
                if (getGame().getVariables().isTeleportAll())
                    teleport(getGame().getVariables().getSpawnpoint(), 8);
            }

            if (time == 0) {

                if (mayExplicate) {
                    setTime(59);
                    explication.setStarted(true);
                    getVariable("champion.auto_explication").setActive(false);
                    explication.run();
                    return;
                }

                sync(this::start);
                return;
            }
            setTime(time - 1);
        } else if (gameStage == GameStage.INVINCIBILITY) {

            if (broadcast)
                broadcast("hg.invincibility.wears_off", gameStage, time);

            if (time == 0) {
                sync(() -> {

                    new SoupListener(getPlugin());

                    setTime(getVariables().getInvincibility());
                    setStage(GameStage.PLAYING);
                    broadcast("hg.invincibility.end");

                    Bukkit.getOnlinePlayers().forEach(player -> refreshTablist(Account.fetch(player.getUniqueId())));

                });
                return;
            }

            setTime(time - 1);
        } else if (gameStage == GameStage.PLAYING) {
            if (getVariables().isFeastSpawn() && time == getVariables().getFeast()) {

                sync(() -> {
                    if (getFeast().isSpawned()) {
                        setFeast(new Feast(getPlugin(), 300).findLocation(150));
                    }

                    getFeast().countdown();
                });

            } else if (getVariables().isExtraFeastSpawn() && time == getVariables().getExtraFeast()) {

                sync(() -> new BonusFeast(getHungerGames()).parseLocation(270, 380).build().spawn(true));

            } else if (getVariables().isMinifeastSpawn() && time % getVariables().getMiniFeastDelay() == 0) {
                sync(() -> Minifeast.fetch().findLocation(getGame().getVariables().getWorldSize() - 30).prepare().generate().alertExplorers().then(Minifeast::broadcast));
            }

            if (getVariables().isFinalArenaSpawn() && time == getVariables().getFinalArena()) {

                sync(() -> {
                    // Carrega arena final automaticamente
                    loadFinalArena();
                });
            } else if (getVariables().isMostEndGameMechanic() && time == getVariables().getMostEndGame()) {

                List<User> users = new ArrayList<>(getPlugin().getUserStorage().getAliveUsers());
                users.sort((a, b) -> Integer.compare(b.getKills(), a.getKills()));

                if (users.isEmpty()) // wtf?
                    Bukkit.shutdown();
                else if (users.size() > 1) {
                    for (int i = 1; i < users.size(); i++) {
                        new LivingUserDieEvent(users.get(i), null, false, DieCause.DIE, new ArrayList<>(), users.get(i).getPlayer().getLocation()).fire();
                    }
                } else {
                    win(Collections.singleton(users.get(0)));
                }
                users.clear();
            }
            setTime(time + 1);
            checkWin();

            // Verificar timeouts de combate a cada segundo
            if (combatMode) {
                checkCombatTimeouts();
            }
        }
    }

    private void setupVariables() {
        getVariables().setInvincibility(60);
        getVariables().setCountStats(true);
        getVariables().setSpawnRange(15);
        getVariables().setMostEndGameMechanic(false);
        getVariables().setMinimumPlayers(0);
        getVariables().setInvincibility(60);
        getVariables().setFinalArenaSpawn(true);
        getVariables().setExtraFeastSpawn(false);
        getVariables().setFinalArena(1800);
        setEventMode(EventMode.SOLO);
        setReduceTime(false);
        setArenaDisableKits(false);
        stats();

        getVariable("chat").setValue(false);
    }

    public void setupKits() {
        getKits(' ', "Anchor Urgal Stomper Demoman Endermage Launcher Phantom Switcher Fisherman Tank Timelord Achilles AntiTower Madman Poseidon Checkpoint Jumper Werewolf Blacksmith").forEach(kit -> kit.setActive(false, false));

        Blink blink = ((Blink) getKit("Blink"));

        blink.setCombatCooldown(true);
        blink.setMaxUses(3);

        Ninja ninja = ((Ninja) getKit("Ninja"));

        ninja.setCombatCooldown(true);
        ninja.setDuration(0.75);

        Monk monk = ((Monk) getKit("Monk"));
        monk.setCooldown(24);

        // Kit Toxic foi removido - funcionalidade desativada
    }

    @Override
    public void prepare(Player p) {
        if (!applyKit)
            super.prepare(p);
        else {

            User user = User.fetch(p.getUniqueId());
            Account account = user.getAccount();
            p.setExp(0);
            p.setLevel(0);
            p.spigot().setCollidesWithEntities(true);
            for (PotionEffect potionEffect : p.getActivePotionEffects())
                p.removePotionEffect(potionEffect.getType());
            if (((CraftPlayer) p).getHandle().playerConnection != null)
                p.setItemOnCursor(null);
            p.setFireTicks(0);
            p.setSaturation(3.2F);
            p.getInventory().clear();
            if (!(p.getOpenInventory().getTopInventory().getHolder() instanceof Selector.Holder))
                p.getOpenInventory().getTopInventory().clear();
            p.getInventory().setArmorContents(null);

            // Definir render distance para 4 chunks usando reflection
            try {
                // Tentar método setViewDistance se disponível
                p.getClass().getMethod("setViewDistance", int.class).invoke(p, 4);
            } catch (Exception e) {
                // Se não funcionar, tentar método alternativo
                try {
                    // Usar reflection para acessar métodos internos
                    Object handle = p.getClass().getMethod("getHandle").invoke(p);
                    Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
                    playerConnection.getClass().getMethod("a", int.class).invoke(playerConnection, 4);
                } catch (Exception ex) {
                    // Se nenhum método funcionar, ignora silenciosamente
                }
            }

            if (user.isAlive()) {
                type.apply(p);
                for (Kit kit : user.getKits())
                    kit.grant(p);
            } else {
                p.setAllowFlight(true);
                p.setFlying(true);
                Items.SPECTATOR.apply(user);
                if (!account.hasPermission(Rank.PARTNER_PLUS))
                    p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 2, false, false), true);
            }
            p.updateInventory();
            handleSidebar(user);
        }
    }

    @Override
    public void die(User user) {
        // Remove proteção de combate quando jogador morre
        if (combatMode) {
            endCombat(user.getUniqueId());
        }

        if (!applyKit)
            super.die(user);
        else {
            if (!user.isOnline())
                return;

            Account account = user.getAccount();
            Player player = user.getPlayer();

            player.setHealth(20);

            if (((CraftPlayer) player).getHandle().playerConnection != null)
                player.setItemOnCursor(null);

            run(() -> {

                if (player.getPassenger() != null)
                    player.getPassenger().leaveVehicle();

                player.setExp(0);
                player.setLevel(0);
                player.setFoodLevel(20);
                player.setSprinting(true);
                for (PotionEffect potionEffect : player.getActivePotionEffects())
                    player.removePotionEffect(potionEffect.getType());
                player.setFireTicks(0);
                player.setSaturation(3.2F);
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                player.setVelocity(new Vector());
                user.getCombatTag().addTag(null, -1);

                if (!user.isAlive()) {

                    if (account.hasPermission(Rank.VIP))
                        player.teleport(player.getLocation().clone().add(0, 0.2, 0));
                    else {
                        player.teleport(new Location(player.getWorld(), 100, 1.3, 100));
                        player.getInventory().setHeldItemSlot(4);
                    }
                    player.setAllowFlight(true);
                    player.setFlying(true);
                    player.spigot().setCollidesWithEntities(false);

                    Items.SPECTATOR.apply(user);

                    player.updateInventory();

                    user.getVictims().clear();

                    if (account.hasPermission(Rank.PARTNER_PLUS)) {
                        if (!user.isVanish())
                            Vanish.getInstance().setVanished(player, account.getRank());
                    } else
                        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 2, false, false), true);

                } else {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    player.spigot().setCollidesWithEntities(true);

                    player.getOpenInventory().getTopInventory().clear();

                    type.apply(player);

                    if (account.hasPermission(Rank.Savage)) {
                        for (Kit kit : user.getKits())
                            kit.grant(player);
                    }

                    player.updateInventory();
                }

                Bukkit.getOnlinePlayers().forEach(this::refreshVisibility);
            }, 1L);
        }
    }

    protected void stats() {
        StatsApplier.WIN.setApplier(user -> {

            if (!HungerGames.getInstance().getGame().getVariables().isCountStats())
                return;

            Account account = user.getAccount();
            user.giveCoins(120.0);

            RankingFactory rankingFactory = HungerGames.getEngine().getRankingFactory();

            if (rankingFactory != null) {
                account.addInt((int) (200 * multiplier), rankingFactory.getTarget().getExperience());
                rankingFactory.verify(account);
                user.getPlayer().sendMessage("§b+200 XP");
            }

            account.addInt(1, Columns.SCRIM_WINS);
            Bukkit.getScheduler().runTaskAsynchronously(HungerGames.getEngine(), () -> account.getDataStorage().saveTable(Tables.HUNGERGAMES));
        });

        StatsApplier.KILL.setApplier(user -> {

            if (!HungerGames.getInstance().getGame().getVariables().isCountStats())
                return;

            Account account = user.getAccount();
            user.giveCoins(40.0);

            RankingFactory rankingFactory = HungerGames.getEngine().getRankingFactory();

            if (rankingFactory != null) {
                account.addInt((int) (32 * multiplier), rankingFactory.getTarget().getExperience());
                rankingFactory.verify(account);
                user.getPlayer().sendMessage("§b+" + (32 * multiplier) + " XP");
            }

            account.addInt(1, Columns.SCRIM_KILLS);

            Data data = account.getData(Columns.SCRIM_MAX_GAME_KILLS);

            if (data.getAsInt() < user.getKills())
                data.setData(user.getKills());
        });

        StatsApplier.DEATH.setApplier(user -> {

            if (!HungerGames.getInstance().getGame().getVariables().isCountStats())
                return;

            Account account = user.getAccount();
            account.addInt(1, Columns.SCRIM_DEATHS);
            account.removeInt(5, Columns.HG_COINS);

            RankingFactory rankingFactory = HungerGames.getEngine().getRankingFactory();

            if (rankingFactory != null) {
                account.removeInt(8, rankingFactory.getTarget().getExperience());
                rankingFactory.verify(account);
                user.getPlayer().sendMessage("§4-8 XP");
            }

            Bukkit.getScheduler().runTaskAsynchronously(HungerGames.getEngine(), () -> account.getDataStorage().saveTable(Tables.HUNGERGAMES));
        });
    }

    @EventHandler
    public void onPlayerGatherDiamond(CraftItemEvent event) {

        Player player = (Player) event.getWhoClicked();
        User user = User.fetch(player.getUniqueId());

        if (!user.getCombatTag().isTagged() && !((CraftPlayer) player).getHandle().inBlock())
            return;

        for (ItemStack itemStack : event.getInventory().getContents()) {
            if (itemStack.containsEnchantment(Enchantment.DAMAGE_ALL)) {
                player.sendMessage("§cVocê não pode juntar espadas encantadas em combate.");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCheckClean(EntityDamageByEntityEvent event) {
        if (!antiClean)
            return;

        if (!(event.getDamager() instanceof Player && event.getEntity() instanceof Player))
            return;

        User damaged = User.fetch(event.getEntity().getUniqueId());
        User damager = User.fetch(event.getDamager().getUniqueId());

        if (damager.getScrimSettings().isCleanTime()) {
            damager.getScrimSettings().incrementHits();

            if (damager.getScrimSettings().getHits() >= 3) {
                damager.getScrimSettings().resetClean();
            }
        }

        if (damaged.getScrimSettings().isCleanTime()) {
            long cleanTime = damaged.getScrimSettings().getCleanTime();
            long currentTime = System.currentTimeMillis();
            long timeLeft = cleanTime - currentTime;
            
            // Debug: Log dos valores para identificar o problema
            HungerGames.getInstance().getLogger().info("DEBUG CLEAN: cleanTime=" + cleanTime + ", currentTime=" + currentTime + ", timeLeft=" + timeLeft);
            
            if (timeLeft <= 0) {
                // Clean já expirou, resetar
                damaged.getScrimSettings().resetClean();
                return;
            }
            // Converter milissegundos para segundos antes da formatação
            long timeLeftSeconds = timeLeft / 1000L;
            String timeFormatted = DateUtils.getDifferenceFormat(timeLeftSeconds);
            
            // Debug: Log do resultado da formatação
            HungerGames.getInstance().getLogger().info("DEBUG FORMAT: timeLeft=" + timeLeft + ", timeLeftSeconds=" + timeLeftSeconds + ", formatted=" + timeFormatted);
            
            damager.getPlayer().sendMessage("§cAguarde " + timeFormatted + " para entrar em combate com " + damaged.getPlayer().getName() + ".");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAntiClean(LivingUserDieEvent event) {
        if (!antiClean)
            return;

        if (event.hasKiller() && event.getKiller().isAlive()) {
            event.getKiller().getPlayer().setNoDamageTicks(160);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(LivingUserDieEvent event) {
        if (!combatMode)
            return;

        // Remove proteção de combate quando jogador morre
        endCombat(event.getUser().getUniqueId());

        // Libera todos os baús privados do jogador quando ele morre
        if (privateChestsEnabled) {
            UUID playerId = event.getUser().getUniqueId();
            privateChests.entrySet().removeIf(entry -> entry.getValue().equals(playerId));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeamAssign(PlayerTeamAssignEvent event) {
        if (!combatMode)
            return;

        Account targetAccount = event.getAccount(); // Jogador sendo visto
        Player viewer = event.getViewer(); // Jogador que está vendo

        // Se o viewer está em combate com o target, aplicar cor vermelha
        if (isInCombat(viewer.getUniqueId()) && isInCombat(targetAccount.getUniqueId())) {
            CombatSession viewerSession = activeCombats.get(viewer.getUniqueId());
            CombatSession targetSession = activeCombats.get(targetAccount.getUniqueId());

            // Verificar se são do mesmo combate
            if (viewerSession != null && targetSession != null && viewerSession == targetSession) {
                // Aplicar cor vermelha apenas no nick (sem tags)
                event.getTeam().setPrefix("§c");
                event.getTeam().setSuffix("");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCombatDamage(EntityDamageByEntityEvent event) {
        if (!combatMode || !(event.getDamager() instanceof Player && event.getEntity() instanceof Player))
            return;

        // Só funciona após a partida iniciar
        if (getStage() != GameStage.PLAYING)
            return;

        // Verifica se PvP está desabilitado durante arena final
        if (finalArenaPvPDisabled) {
            event.setCancelled(true);
            if (event.getDamager() instanceof Player) {
                Player damager = (Player) event.getDamager();
                damager.sendMessage("§cPvP está desabilitado durante a preparação da Arena Final!");
            }
            return;
        }

        User damaged = User.fetch(event.getEntity().getUniqueId());
        User damager = User.fetch(event.getDamager().getUniqueId());

        // Ignora ataques entre jogadores em dupla
        if (damager != null && damaged != null && damager.isDuo(damaged.getUniqueId())) {
            return;
        }

        // Ignora ataques se o atacante está em vanish (staff não pode atacar)
        if (damager != null && damager.isVanish()) {
            return;
        }
        // Jogadores podem atacar staff em vanish normalmente

        // Verifica se pode atacar
        if (!canAttack(damager.getUniqueId(), damaged.getUniqueId())) {
            event.setCancelled(true);

            // Se o atacante está em combate, mostra com quem está lutando
            if (isInCombat(damager.getUniqueId())) {
                CombatSession session = activeCombats.get(damager.getUniqueId());
                UUID opponentId = session.getOtherPlayer(damager.getUniqueId());
                User opponent = User.fetch(opponentId);
                if (opponent != null && opponent.isOnline()) {
                    damager.getPlayer().sendMessage("§cVocê está em combate com " + opponent.getPlayer().getName());
                } else {
                    damager.getPlayer().sendMessage("§cVocê está em combate!");
                }
            } else {
                damager.getPlayer().sendMessage("§cVocê não pode interferir nesta luta!");
            }
            return;
        }

        // Verifica se algum dos jogadores está em clean
        if (damager.getScrimSettings().isCleanTime() || damaged.getScrimSettings().isCleanTime()) {
            event.setCancelled(true);

            // Se o atacante está em clean, mostra mensagem com tempo restante
            if (damager.getScrimSettings().isCleanTime()) {
                long cleanTime = damager.getScrimSettings().getCleanTime();
                long currentTime = System.currentTimeMillis();
                long timeLeft = cleanTime - currentTime;
                
                // Debug: Log dos valores para identificar o problema
                HungerGames.getInstance().getLogger().info("DEBUG CLEAN DAMAGER: cleanTime=" + cleanTime + ", currentTime=" + currentTime + ", timeLeft=" + timeLeft);
                
                if (timeLeft <= 0) {
                    // Clean já expirou, resetar
                    damager.getScrimSettings().resetClean();
                } else {
                    // Converter milissegundos para segundos antes da formatação
                    long timeLeftSeconds = timeLeft / 1000L;
                    String timeFormatted = DateUtils.getDifferenceFormat(timeLeftSeconds);
                    
                    // Debug: Log do resultado da formatação
                    HungerGames.getInstance().getLogger().info("DEBUG FORMAT DAMAGER: timeLeft=" + timeLeft + ", timeLeftSeconds=" + timeLeftSeconds + ", formatted=" + timeFormatted);
                    
                    damager.getPlayer().sendMessage("§cVocê está em clean! Aguarde " + timeFormatted + " para poder atacar.");
                }
            }
            // Se o atacado está em clean, mostra mensagem com tempo restante
            else if (damaged.getScrimSettings().isCleanTime()) {
                long cleanTime = damaged.getScrimSettings().getCleanTime();
                long currentTime = System.currentTimeMillis();
                long timeLeft = cleanTime - currentTime;
                
                // Debug: Log dos valores para identificar o problema
                HungerGames.getInstance().getLogger().info("DEBUG CLEAN DAMAGED: cleanTime=" + cleanTime + ", currentTime=" + currentTime + ", timeLeft=" + timeLeft);
                
                if (timeLeft <= 0) {
                    // Clean já expirou, resetar
                    damaged.getScrimSettings().resetClean();
                } else {
                    // Converter milissegundos para segundos antes da formatação
                    long timeLeftSeconds = timeLeft / 1000L;
                    String timeFormatted = DateUtils.getDifferenceFormat(timeLeftSeconds);
                    
                    // Debug: Log do resultado da formatação
                    HungerGames.getInstance().getLogger().info("DEBUG FORMAT DAMAGED: timeLeft=" + timeLeft + ", timeLeftSeconds=" + timeLeftSeconds + ", formatted=" + timeFormatted);
                    
                    damager.getPlayer().sendMessage("§c" + damaged.getPlayer().getName() + " está em clean! Aguarde " + timeFormatted + " para poder atacar.");
                }
            }
            return;
        }

        // Se não estão em combate e é um ataque válido, inicia o combate
        if (!isInCombat(damager.getUniqueId()) && !isInCombat(damaged.getUniqueId())) {
            startCombatForDuos(damager.getUniqueId(), damaged.getUniqueId());
        }

        // Atualiza atividade do combate
        updateCombatActivity(damager.getUniqueId());
        updateCombatActivity(damaged.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!privateChestsEnabled) return;

        // Verifica se é um baú ou baú duplo
        if (event.getBlock().getType() == Material.CHEST || event.getBlock().getType() == Material.TRAPPED_CHEST) {
            Location location = event.getBlock().getLocation();
            UUID playerId = event.getPlayer().getUniqueId();

            // Registra o baú como privado do jogador
            privateChests.put(location, playerId);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!privateChestsEnabled) return;

        // Verifica se é um baú ou baú duplo
        if (event.getBlock().getType() == Material.CHEST || event.getBlock().getType() == Material.TRAPPED_CHEST) {
            Location location = event.getBlock().getLocation();
            UUID ownerId = privateChests.get(location);

            // Se não encontrou o dono neste baú, verifica se é parte de um baú duplo
            if (ownerId == null) {
                // Verifica os blocos adjacentes para baús duplos
                Location[] adjacentLocations = {
                        location.clone().add(1, 0, 0), // Leste
                        location.clone().add(-1, 0, 0), // Oeste
                        location.clone().add(0, 0, 1), // Sul
                        location.clone().add(0, 0, -1)  // Norte
                };

                for (Location adjLocation : adjacentLocations) {
                    if (adjLocation.getBlock().getType() == Material.CHEST || adjLocation.getBlock().getType() == Material.TRAPPED_CHEST) {
                        ownerId = privateChests.get(adjLocation);
                        if (ownerId != null) {
                            break;
                        }
                    }
                }
            }

            if (ownerId != null) {
                Player player = event.getPlayer();

                // Se não é o dono do baú
                if (!ownerId.equals(player.getUniqueId())) {
                    User playerUser = User.fetch(player.getUniqueId());
                    User owner = User.fetch(ownerId);

                    // Verifica se os jogadores estão em dupla
                    if (playerUser != null && owner != null && playerUser.isDuo(ownerId)) {
                        // Jogadores em dupla podem quebrar os baús um do outro apenas se ambos morreram
                        if (owner.isOnline() || playerUser.isOnline()) {
                            event.setCancelled(true);
                            player.sendMessage("§cVocê não pode quebrar esse baú, algum jogador da dupla ainda está vivo.");
                            return;
                        }
                        // Ambos morreram, libera o baú
                        privateChests.remove(location);
                        return;
                    }

                    // Verifica se o dono ainda está vivo
                    if (owner != null && owner.isOnline()) {
                        event.setCancelled(true);
                        player.sendMessage("§cVocê não pode quebrar esse baú, o jogador responsável está vivo ainda.");
                        return;
                    } else {
                        // Dono morreu, libera o baú
                        privateChests.remove(location);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!privateChestsEnabled) return;

        // Verifica se é um baú (simples ou duplo)
        if (event.getInventory().getHolder() instanceof org.bukkit.block.Chest) {
            org.bukkit.block.Chest chest = (org.bukkit.block.Chest) event.getInventory().getHolder();
            Location location = chest.getLocation();

            // Verifica se este baú é privado
            UUID ownerId = privateChests.get(location);
            if (ownerId != null) {
                Player player = (Player) event.getPlayer();

                // Se não é o dono do baú
                if (!ownerId.equals(player.getUniqueId())) {
                    User playerUser = User.fetch(player.getUniqueId());
                    User owner = User.fetch(ownerId);

                    // Verifica se os jogadores estão em dupla
                    if (playerUser != null && owner != null && playerUser.isDuo(ownerId)) {
                        // Jogadores em dupla podem acessar os baús um do outro enquanto estão vivos
                        return;
                    }

                    // Verifica se o dono ainda está vivo
                    if (owner != null && owner.isOnline()) {
                        event.setCancelled(true);
                        player.sendMessage("§cVocê não pode abrir esse baú, o jogador responsável está vivo ainda.");
                        return;
                    } else {
                        // Dono morreu, libera o baú
                        privateChests.remove(location);
                    }
                }
            }
        }
        // Verifica se é um baú duplo (DoubleChest)
        else if (event.getInventory().getHolder() instanceof org.bukkit.block.DoubleChest) {
            org.bukkit.block.DoubleChest doubleChest = (org.bukkit.block.DoubleChest) event.getInventory().getHolder();
            org.bukkit.block.Chest leftChest = (org.bukkit.block.Chest) doubleChest.getLeftSide();
            org.bukkit.block.Chest rightChest = (org.bukkit.block.Chest) doubleChest.getRightSide();

            // Verifica ambos os lados do baú duplo
            Location leftLocation = leftChest.getLocation();
            Location rightLocation = rightChest.getLocation();

            UUID ownerId = privateChests.get(leftLocation);
            if (ownerId == null) {
                ownerId = privateChests.get(rightLocation);
            }

            if (ownerId != null) {
                Player player = (Player) event.getPlayer();

                // Se não é o dono do baú
                if (!ownerId.equals(player.getUniqueId())) {
                    User playerUser = User.fetch(player.getUniqueId());
                    User owner = User.fetch(ownerId);

                    // Verifica se os jogadores estão em dupla
                    if (playerUser != null && owner != null && playerUser.isDuo(ownerId)) {
                        // Jogadores em dupla podem acessar os baús um do outro enquanto estão vivos
                        return;
                    }

                    // Verifica se o dono ainda está vivo
                    if (owner != null && owner.isOnline()) {
                        event.setCancelled(true);
                        player.sendMessage("§cVocê não pode abrir esse baú, o jogador responsável está vivo ainda.");
                        return;
                    } else {
                        // Dono morreu, libera ambos os baús
                        privateChests.remove(leftLocation);
                        privateChests.remove(rightLocation);
                    }
                }
            }
        }
    }

    // Event handlers para arena final (bloquear build e baldes)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlaceArenaFinal(BlockPlaceEvent event) {
        if (finalArenaActive) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cBuild está desabilitado durante a Arena Final!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreakArenaFinal(BlockBreakEvent event) {
        if (finalArenaActive) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cBuild está desabilitado durante a Arena Final!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucketEmpty(org.bukkit.event.player.PlayerBucketEmptyEvent event) {
        if (finalArenaActive) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cUso de baldes está desabilitado durante a Arena Final!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucketFill(org.bukkit.event.player.PlayerBucketFillEvent event) {
        if (finalArenaActive) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cUso de baldes está desabilitado durante a Arena Final!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDuoFriendlyFire(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player && event.getEntity() instanceof Player)) return;

        User damager = User.fetch(event.getDamager().getUniqueId());
        User damaged = User.fetch(event.getEntity().getUniqueId());

        // Verifica se os jogadores estão em dupla e cancela o ataque silenciosamente
        if (damager != null && damaged != null && damager.isDuo(damaged.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @Override
    public void checkWin() {
        // Primeiro executa a lógica padrão da classe pai
        super.checkWin();

        // Depois verifica se há uma dupla que pode ganhar
        List<User> aliveUsers = getPlugin().getUserStorage().getAliveUsers();

        if (aliveUsers.size() == 2) {
            // Verifica se os dois jogadores restantes são uma dupla
            User player1 = aliveUsers.get(0);
            User player2 = aliveUsers.get(1);

            if (player1.getDuo() != null && player1.getDuo().equals(player2)) {
                // É uma dupla! Eles ganham o jogo imediatamente
                Set<User> winners = new HashSet<>();
                winners.add(player1);
                winners.add(player2);

                sync(() -> win(winners));
            }
        }
    }

    public Explication getExplication() {
        return applyKit ? KITSET : MINE;
    }

    // Métodos para gerenciar combates
    private void startCombatForDuos(UUID player1, UUID player2) {
        if (!combatMode) return;

        User user1 = User.fetch(player1);
        User user2 = User.fetch(player2);

        if (user1 == null || user2 == null) return;

        // Verifica se os jogadores estão em dupla
        boolean player1InDuo = user1.getDuo() != null;
        boolean player2InDuo = user2.getDuo() != null;

        if (player1InDuo && player2InDuo) {
            // Ambos estão em dupla - incluir todos os membros das duas duplas
            UUID duo1Partner = user1.getDuo().getUniqueId();
            UUID duo2Partner = user2.getDuo().getUniqueId();

            if (duo1Partner != null && duo2Partner != null) {
                // Criar combate entre as duas duplas completas
                startCombatBetweenDuos(player1, duo1Partner, player2, duo2Partner);
            } else {
                // Fallback para combate individual
                startCombat(player1, player2);
            }
        } else if (player1InDuo) {
            // Apenas player1 está em dupla - incluir o parceiro dele
            UUID duo1Partner = user1.getDuo().getUniqueId();
            if (duo1Partner != null) {
                startCombatBetweenDuos(player1, duo1Partner, player2, null);
            } else {
                startCombat(player1, player2);
            }
        } else if (player2InDuo) {
            // Apenas player2 está em dupla - incluir o parceiro dele
            UUID duo2Partner = user2.getDuo().getUniqueId();
            if (duo2Partner != null) {
                startCombatBetweenDuos(player1, null, player2, duo2Partner);
            } else {
                startCombat(player1, player2);
            }
        } else {
            // Nenhum está em dupla - combate individual
            startCombat(player1, player2);
        }
    }

    private void startCombatBetweenDuos(UUID player1, UUID duo1Partner, UUID player2, UUID duo2Partner) {
        if (!combatMode) return;

        // Criar uma sessão de combate que inclui todos os jogadores
        CombatSession session = new CombatSession(player1, player2);

        // Adicionar todos os jogadores à sessão de combate
        activeCombats.put(player1, session);
        activeCombats.put(player2, session);
        lastCombatActivity.put(player1, System.currentTimeMillis());
        lastCombatActivity.put(player2, System.currentTimeMillis());

        // Adicionar parceiros se existirem
        if (duo1Partner != null) {
            activeCombats.put(duo1Partner, session);
            lastCombatActivity.put(duo1Partner, System.currentTimeMillis());
        }

        if (duo2Partner != null) {
            activeCombats.put(duo2Partner, session);
            lastCombatActivity.put(duo2Partner, System.currentTimeMillis());
        }

        // Salvar tags originais e enviar mensagens para todos
        List<UUID> allPlayers = new ArrayList<>();
        allPlayers.add(player1);
        allPlayers.add(player2);
        if (duo1Partner != null) allPlayers.add(duo1Partner);
        if (duo2Partner != null) allPlayers.add(duo2Partner);

        for (UUID playerId : allPlayers) {
            User user = User.fetch(playerId);
            if (user != null && user.isOnline()) {
                Account account = user.getAccount();
                Tag originalTag = account.getProperty("account_tag").getAs(Tag.class);
                originalTags.put(playerId, originalTag);

                // Mensagem de entrada em combate
                user.getPlayer().sendMessage("§cVocê entrou em combate!");
            }
        }

        // Forçar atualização do tablist para aplicar cor vermelha via event handler
        for (UUID playerId : allPlayers) {
            User user = User.fetch(playerId);
            if (user != null && user.isOnline()) {
                Account account = user.getAccount();
                Tag tag = account.getProperty("account_tag").getAs(Tag.class);
                PrefixType prefixType = account.getProperty("account_prefix_type").getAs(PrefixType.class);
                PlayerUpdateTablistEvent event = new PlayerUpdateTablistEvent(account, tag, prefixType);
                Bukkit.getPluginManager().callEvent(event);
            }
        }
    }

    private void startCombat(UUID player1, UUID player2) {
        if (!combatMode) return;

        CombatSession session = new CombatSession(player1, player2);
        activeCombats.put(player1, session);
        activeCombats.put(player2, session);
        lastCombatActivity.put(player1, System.currentTimeMillis());
        lastCombatActivity.put(player2, System.currentTimeMillis());

        // Salvar tags originais
        User user1 = User.fetch(player1);
        User user2 = User.fetch(player2);

        if (user1 != null && user1.isOnline()) {
            Account account1 = user1.getAccount();
            Tag originalTag1 = account1.getProperty("account_tag").getAs(Tag.class);
            originalTags.put(player1, originalTag1);

            // Mensagem de entrada em combate
            user1.getPlayer().sendMessage("§cVocê entrou em combate com " + user2.getPlayer().getName() + ".");
        }

        if (user2 != null && user2.isOnline()) {
            Account account2 = user2.getAccount();
            Tag originalTag2 = account2.getProperty("account_tag").getAs(Tag.class);
            originalTags.put(player2, originalTag2);

            // Mensagem de entrada em combate
            user2.getPlayer().sendMessage("§cVocê entrou em combate com " + user1.getPlayer().getName() + ".");
        }

        // Forçar atualização do tablist para aplicar cor vermelha via event handler
        if (user1 != null && user1.isOnline()) {
            PrefixType prefixType1 = user1.getAccount().getProperty("account_prefix_type").getAs(PrefixType.class);
            Bukkit.getPluginManager().callEvent(new PlayerUpdateTablistEvent(user1.getAccount(), originalTags.get(player1), prefixType1));
        }

        if (user2 != null && user2.isOnline()) {
            PrefixType prefixType2 = user2.getAccount().getProperty("account_prefix_type").getAs(PrefixType.class);
            Bukkit.getPluginManager().callEvent(new PlayerUpdateTablistEvent(user2.getAccount(), originalTags.get(player2), prefixType2));
        }
    }

    private void endCombat(UUID player) {
        CombatSession session = activeCombats.remove(player);
        if (session != null) {
            // Encontrar todos os jogadores que estão nesta sessão de combate
            List<UUID> playersInSession = new ArrayList<>();
            for (Map.Entry<UUID, CombatSession> entry : activeCombats.entrySet()) {
                if (entry.getValue() == session) {
                    playersInSession.add(entry.getKey());
                }
            }
            
            // Remover todos os jogadores desta sessão de combate
            for (UUID playerInSession : playersInSession) {
                activeCombats.remove(playerInSession);
                lastCombatActivity.remove(playerInSession);
                restorePlayerTag(playerInSession);
            }
            
            // Debug: Log da limpeza do combate
            HungerGames.getInstance().getLogger().info("DEBUG COMBAT END: player=" + player + ", playersInSession=" + playersInSession.size() + ", session=" + session);
        }
    }

    private void restorePlayerTag(UUID player) {
        User user = User.fetch(player);
        if (user != null && user.isOnline()) {
            Account account = user.getAccount();
            Tag originalTag = originalTags.remove(player);

            if (originalTag != null) {
                // Forçar atualização do tablist para restaurar cor original
                PrefixType prefixType = account.getProperty("account_prefix_type").getAs(PrefixType.class);
                Bukkit.getPluginManager().callEvent(new PlayerUpdateTablistEvent(account, originalTag, prefixType));

                // Mensagem de saída de combate
                user.getPlayer().sendMessage("§aVocê saiu de combate.");
            }
        }
    }

    private void updateCombatActivity(UUID player) {
        CombatSession session = activeCombats.get(player);
        if (session != null) {
            session.updateActivity();
            lastCombatActivity.put(player, System.currentTimeMillis());
            lastCombatActivity.put(session.getOtherPlayer(player), System.currentTimeMillis());
        }
    }

    private boolean isInCombat(UUID player) {
        return activeCombats.containsKey(player);
    }

    private boolean canAttack(UUID attacker, UUID target) {
        if (!combatMode) return true;

        // Se nenhum dos dois está em combate, pode atacar
        if (!isInCombat(attacker) && !isInCombat(target)) {
            return true;
        }

        // Se ambos estão em combate, verifica se são do mesmo combate
        if (isInCombat(attacker) && isInCombat(target)) {
            CombatSession attackerSession = activeCombats.get(attacker);
            CombatSession targetSession = activeCombats.get(target);
            return attackerSession == targetSession; // Mesma instância = mesmo combate
        }

        // Se apenas um está em combate, não pode atacar
        return false;
    }

    private void checkCombatTimeouts() {
        if (!combatMode) return;

        long currentTime = System.currentTimeMillis();
        long timeoutMs = combatTimeout * 1000L;

        List<UUID> toRemove = new ArrayList<>();

        for (Map.Entry<UUID, Long> entry : lastCombatActivity.entrySet()) {
            if (currentTime - entry.getValue() > timeoutMs) {
                toRemove.add(entry.getKey());
            }
        }

        for (UUID player : toRemove) {
            endCombat(player);
        }
    }

    // Limpa todos os combates ativos (útil quando o jogo termina)
    private void clearAllCombats() {
        // Restaurar todas as tags antes de limpar
        for (UUID player : originalTags.keySet()) {
            restorePlayerTag(player);
        }

        activeCombats.clear();
        lastCombatActivity.clear();
        originalTags.clear();
    }

    private void loadFinalArena() {
        try {
            // Carrega o arquivo arena_final.BO3
            File arenaFile = new File("Savage/hg/structures/arena_final.BO3");
            if (!arenaFile.exists()) {
                Bukkit.broadcastMessage("§cErro: Arquivo arena_final.BO3 não encontrado!");
                return;
            }

            // Carrega o BO3
            BO3Reader reader = new BO3Reader(arenaFile);
            BO3 arenaBO3 = reader.read();
            if (arenaBO3 instanceof EmptyBO3) {
                Bukkit.broadcastMessage("§cErro: Não foi possível carregar arena_final.BO3!");
                return;
            }

            // Localização base para spawnar a arena
            Location spawnLocation = new Location(getGame().getWorld(), 0, 10, 0);

            // Calcula o centro da arena baseado nos blocos do BO3
            Location arenaCenter = calculateArenaCenter(arenaBO3, spawnLocation);

            // Spawna a arena
            arenaBO3.spawn(spawnLocation, (loc, pattern) -> {
                Block block = loc.getBlock();

                // Remove blocos de lapis (espaços vazios)
                if (pattern.getMaterial() == Material.LAPIS_BLOCK) {
                    block.setType(Material.AIR);

                    // Limpa espaço acima
                    for (int i = 0; i < 100; i++) {
                        block = block.getRelative(BlockFace.UP);
                        if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK) {
                            block.setType(Material.AIR);
                        }
                    }
                    return false;
                }

                // Aplica o padrão do bloco
                block.setType(pattern.getMaterial());
                block.setData(pattern.getData());
                return true;
            });

            // Ativa o sistema de arena final (desabilita build e baldes)
            finalArenaActive = true;

            // Desativa PvP por 5 segundos
            finalArenaPvPDisabled = true;

            Bukkit.broadcastMessage("§e§lARENA FINAL CARREGADA!");
            Bukkit.broadcastMessage("§cBuild e uso de baldes foram desabilitados!");
            Bukkit.broadcastMessage("§ePvP desabilitado por 5 segundos!");

            // Teleporta todos os jogadores vivos para o centro da arena
            List<User> aliveUsers = getPlugin().getUserStorage().getAliveUsers();
            for (User user : aliveUsers) {
                if (user.isOnline()) {
                    Player player = user.getPlayer();

                    // Teleporta para o centro calculado da arena
                    player.teleport(arenaCenter);

                    // Mensagem para o jogador
                    player.sendMessage("§aVocê foi teleportado para a Arena Final!");
                }
            }

            // Mensagem global
            Bukkit.broadcastMessage("§6§lTodos os jogadores foram teleportados para a Arena Final!");

            // Reativa PvP após 5 segundos
            Bukkit.getScheduler().runTaskLater(HungerGames.getInstance(), () -> {
                finalArenaPvPDisabled = false;
                Bukkit.broadcastMessage("§a§lPvP reativado! A batalha final começou!");
            }, 100L); // 5 segundos = 100 ticks

        } catch (Exception e) {
            Bukkit.broadcastMessage("§cErro ao carregar arena final: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Location calculateArenaCenter(BO3 arenaBO3, Location spawnLocation) {
        if (arenaBO3.getBlocks().isEmpty()) {
            return spawnLocation; // Se não há blocos, retorna a localização base
        }

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        // Calcula os limites da arena
        for (BO3Block block : arenaBO3.getBlocks()) {
            minX = Math.min(minX, block.getX());
            maxX = Math.max(maxX, block.getX());
            minY = Math.min(minY, block.getY());
            maxY = Math.max(maxY, block.getY());
            minZ = Math.min(minZ, block.getZ());
            maxZ = Math.max(maxZ, block.getZ());
        }

        // Calcula o centro
        int centerX = (minX + maxX) / 2;
        int centerY = (minY + maxY) / 2; // Centro vertical da arena (dentro)
        int centerZ = (minZ + maxZ) / 2;

        // Retorna a localização do centro no mundo
        return spawnLocation.clone().add(centerX, centerY, centerZ);
    }


}
