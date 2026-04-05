package com.minecraft.savagemc.arcade.aquatic.arcade;

import com.grinderwolf.swm.plugin.SWMPlugin;
import com.minecraft.savagemc.arcade.aquatic.Aquatic;
import com.minecraft.savagemc.arcade.aquatic.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.aquatic.arcade.item.ItemList;
import com.minecraft.savagemc.arcade.aquatic.arcade.team.Team;
import com.minecraft.savagemc.arcade.aquatic.menu.perk.PerkMenu;
import com.minecraft.savagemc.arcade.aquatic.perk.Perk;
import com.minecraft.savagemc.arcade.aquatic.user.User;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.tag.Tag;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.api.redirect.Redirect;
import com.minecraft.savagemc.core.arcade.ArcadeHolder;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.arcade.room.map.Map;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.arcade.room.slot.Slot;
import com.minecraft.savagemc.core.arcade.route.ArcadeRouteContext;
import com.minecraft.savagemc.core.arcade.route.state.ArcadeState;
import com.minecraft.savagemc.core.arcade.slime.SlimeWorldController;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.savagemc.core.bukkit.api.title.TitleAnimation;
import com.minecraft.savagemc.core.bukkit.manager.list.TagManager;
import com.minecraft.savagemc.core.bukkit.menu.server.arcade.mode.aquatic.AquaticMenu;
import com.minecraft.savagemc.core.event.IgnoreEvent;
import com.minecraft.savagemc.core.member.context.stats.ArcadeStats;
import com.minecraft.savagemc.core.member.list.aquatic.AquaticMember;
import com.minecraft.savagemc.core.server.type.ServerType;
import com.minecraft.savagemc.core.util.Util;
import com.minecraft.savagemc.core.util.list.DateUtil;
import com.minecraft.savagemc.core.util.list.bukkit.BukkitUtil;
import com.minecraft.savagemc.core.util.list.bukkit.ColorUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@IgnoreEvent
public abstract class Arcade extends ArcadeHolder {

    private final File mapsFile;

    public Arcade(String mapsDirectory, Integer minRooms, Integer maxRooms, ArcadeCategory category) {
        super(mapsDirectory, minRooms, maxRooms, category);

        this.mapsFile = new File(Aquatic.getInstance().getDataFolder().getAbsolutePath() + "/maps");
    }

    @Override
    public boolean load() {
        Savage.getLogger().info("Iniciando " + getName() + "...");

        if (!loadMaps(mapsFile)) return false;

        List<Map> availableMaps = getMaps();

        if (availableMaps.isEmpty()) {
            Savage.getLogger().warning("Nenhum mapa disponível para " + getName() + ".");
            return false;
        }

        int numRooms = getMinRooms();
        int mapIndex = 0;

        for (int i = 0; i < numRooms; i++) {
            Map map = availableMaps.get(mapIndex);

            if (setupRoom(map, getCategory().getSlots().get(0)) == null) return false;

            mapIndex = (mapIndex + 1) % availableMaps.size();
        }

        Savage.getLogger().info("Modo de jogo " + getName() + " iniciado com sucesso.");
        return true;
    }

    @Override
    public void unload() {
        getRooms().clear();
    }

    @Override
    public Arena setupRoom(Map map, Slot slot) {
        Instant now = Instant.now();

        int id = (getId().getAndIncrement() + 1);

        Arena arena = new Arena(id, this, map, slot);

        Savage.getLogger().info("[" + getName() + "/" + id + "] Criando arena...");

        String[] mapSplit = map.getName().split(" ");

        String mapName = mapSplit.length > 1 ? mapSplit[0] + "-" + mapSplit[1] : map.getName();

        String templateName = getName().toLowerCase() + "-" + mapName.toLowerCase(),
                worldName = templateName + "-" + id;

        handleArenaData(arena, id, templateName, worldName, now);

        return arena;
    }

    public void unloadArena(Arena arena) {
        arena.getProperties().clear();

        getRooms().remove(arena);
    }

    public void copyArena(Arena source) {
        Instant now = Instant.now();

        int id = source.getId();

        Map map = source.getMap();
        Slot slot = source.getSlot();

        Arena arena = new Arena(id, this, map, slot);

        arena.setType(source.getType());
        arena.getProperties().putAll(source.getProperties());

        Savage.getLogger().info("[" + getName() + "/" + id + "] Copiando arena...");

        String[] mapSplit = map.getName().split(" ");

        String mapName = mapSplit.length > 1 ? mapSplit[0] + "-" + mapSplit[1] : map.getName();

        String templateName = getName().toLowerCase() + "-" + mapName.toLowerCase(),
                worldName = templateName + "-" + id;

        handleArenaData(arena, id, templateName, worldName, now);
    }

    protected void handleArenaData(Arena arena, int id, String templateName, String worldName, Instant now) {
        ArcadeCategory arcade = arena.getArcade().getCategory();
        Slot slot = arena.getSlot();

        SlimeWorldController.cloneWorldFromTemplate(SWMPlugin.getInstance(), templateName, worldName, () -> {
            List<Team> teamList = !arcade.equals(ArcadeCategory.AQUATIC_FREE) ? arena.buildTeams()
                    : new ArrayList<>();

            teamList.forEach(team -> {
                team.setRemainingLives(3);

                team.setMaxPlayers(slot.getMaxPlayers() / 2);
            });

            int minPlayers = arcade.equals(ArcadeCategory.AQUATIC_FREE) ? 300
                    : arcade.equals(ArcadeCategory.AQUATIC_DUO) ? 12
                    : 6;

            int maxPlayers = arcade.equals(ArcadeCategory.AQUATIC_FREE) ? 300
                    : arcade.equals(ArcadeCategory.AQUATIC_DUO) ? 16
                    : 8;

            arena.setMinPlayers(minPlayers);
            arena.setMaxPlayers(maxPlayers);

            arena.getTeams().addAll(teamList);

            arena.handleBorder();

            getRooms().add(arena);

            Savage.getLogger().info("[" + getName() + "/" + id + "] Arena criada com sucesso. (Tempo médio: " + Util.formatInstant(now) + ")");
        });
    }

    public void checkAndGenerateArenas() {
        int totalArenas = getRooms().size();
        int occupiedArenas = (int) getRooms().stream().filter(room -> !room.isAvailable()).count();

        if (occupiedArenas >= (totalArenas / 2)) {
            final int NUM_ARENAS_TO_GENERATE = 3;

            Savage.getLogger().info("[" + getName() + "] Gerando " + NUM_ARENAS_TO_GENERATE + " arenas...");

            int loaded = 0;
            for (int i = 0; i < NUM_ARENAS_TO_GENERATE; i++) {
                Map randomMap = getRandomMap();

                if (randomMap != null) {
                    setupRoom(randomMap, randomMap.getArcade().getSlots().get(0));
                    loaded++;
                }
            }

            if (loaded > 0)
                Savage.getLogger().info("[" + getName() + "] " + loaded + " arenas foram geradas.");
            else
                Savage.getLogger().warning("[" + getName() + "] Não foi possível gerar novas arenas!");
        }
    }

    @Override
    public Arena findBestArena(UUID sender, ArcadeRouteContext route) {
        Account account = Savage.getAccountData().of(sender);

        Redirect redirect = Savage.getRedirectData().of(sender);

        if (redirect != null) {
            return (Arena) getRooms().stream()
                    .filter(room -> room.isCategory(route.getArcade()) && room.getId() == route.getRoomId() && room.isFiltered(route.getMapId()))
                    .findFirst()
                    .orElse(null);
        }

        Stream<Arena> stream = getRooms().stream().map(room -> (Arena) room)
                .filter(arena -> arena.isAvailable() && arena.getSlot().equals(route.getSlot()))
                .collect(Collectors.toList()).stream();

        if (route.getType() != null)
            stream = stream.filter(arena -> arena.getType().equals(route.getType()));

        if (route.hasMap())
            stream = stream.filter(arena -> arena.isFiltered(route.getMapId()));

        return stream.filter(arena -> {
            System.out.println("[Search] Filtrando " + arena.getIdentifier() + " para " + sender);

            if (account.isPartyOwner() && route.isLinked() && !arena.hasReservations(route.getLink())) {
                arena.setReservations(route.getLink());

                System.out.println("[PT-Route] " + arena.getIdentifier() + " recebeu +" + route.getLink().size() + " reservas. " +
                        "Total de reservas: " + arena.getTotalReservations());
            }

            if (account.isPartyOwner() && arena.isReserved() && arena.hasReservation(sender)) {
                Savage.getLogger().info("[PT-Route] O dono " + account.getNickname() + " entrou em " + arena.getIdentifier() + ".");
                return true;
            }

            if (arena.isReserved() && arena.hasReservation(sender) && !arena.isFull()) {
                arena.removeReservation(sender);

                System.out.println("[PT-Route] " + account.getNickname() + " entrou em " + arena.getIdentifier() + ".");
                return true;
            }

            return arena.isAvailable();
        }).findFirst().orElse(null);
    }

    public void load(User user) {
        handleSidebar(user);
        handleHotbar(user);
    }

    public void handleSidebar(User user) {
        Arena arena = user.getArena();

        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.setTitle("WATER FIGHT");

        sidebar.addRow("date", "§7" + DateUtil.getCurrentDate() + " §8" + arena.getIdentifier());
        sidebar.blankRow();

        // Renderizar scoreboard quando o jogo iniciou
        if (arena.isPhase(RoomPhase.PLAYING)) {
            arena.handleSidebarTeams(user);

            sidebar.blankRow();
            sidebar.addRow("finalKills", "Kills finais: §a" + Util.formatNumber(user.getFinalKill()));
        } else {
            // Renderizar scoreboard quando o jogo ainda não iniciou

            sidebar.addRow("map", "Mapa: §a" + arena.getMap().getName());
            sidebar.addRow("mode", "Modo: §a" + arena.getArcade().getCategory().getName());

            sidebar.blankRow();
            arena.handleTimeSidebar(sidebar);

            sidebar.addRow("players", "Players: §a" + arena.getTotalPlayers() + "/" + arena.getMaxPlayers());
            sidebar.addRow("winstreak", "Winstreak: §7" + Util.formatNumber(user.getMember().getTotalWinstreak(getCategory())));
        }

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagManager.updateTag(user.getAccount());
    }

    public void updateSidebar(User user) {

    }

    public void handleHotbar(User user) {
        Player player = user.getAccount().player();

        Arena arena = user.getArena();

        PlayerInventory inv = player.getInventory();

        if (arena.isPhase(RoomPhase.PLAYING)) {
            player.clearInventory();

            if (user.isPlayer() && user.inState(ArcadeState.ALIVE)) {
                Team team = user.getTeam();

                BukkitUtil.sendLeatherArmor(player, team.getRgb());

                inv.setItem(0, Item.of(Material.WOOD_SWORD)
                        .flags(ItemFlag.values())
                        .unbreakable());

                inv.setItem(1, Item.of(Material.WOOL, ColorUtil.getIdByColor(team.getColor())).amount(64));

                // Adicionando itens do perk
                if (user.hasPerk()) {
                    Perk perk = user.getPerk();

                    perk.handle(player);

                    perk.getItems().forEach(inv::addItem);
                }

            } else if (user.inState(ArcadeState.DEAD)) {
                sendDeathHotbar(player, arena);
            }

        } else {
            if (user.isPlayer() && arena.isPreGame()) {
                player.clearInventory();

                inv.setItem(0, Item.of(Material.STORAGE_MINECART, "§aSelecionar perk")
                        .interact(event -> new PerkMenu(event.getPlayer()).handle()));

                inv.setItem(4, Item.of(Material.EMERALD, "§aMenu do " + ServerType.AQUATIC.getName())
                        .interact(event -> new AquaticMenu(event.getPlayer()).handle()));

                inv.setItem(8, Item.of(Material.ACACIA_DOOR_ITEM, "§cVoltar ao lobby")
                        .interact(event -> event.getPlayer().performCommand("lobby")));
            }

            if (arena.isPhase(RoomPhase.ENDING))
                sendDeathHotbar(player, arena);
        }
    }

    protected void sendDeathHotbar(Player player, Arena arena) {
        PlayerInventory inv = player.getInventory();

        if (arena.isPhase(RoomPhase.PLAYING))
            inv.setItem(0, Item.of(Material.COMPASS, "§aTeleportar")
                    .interact(event -> {
                    }));

        inv.setItem(7, Item.of(Material.PAPER, "§aJogar novamente")
                .interact(event -> event.getPlayer().performCommand("playagain")));

        inv.setItem(8, Item.of(Material.ACACIA_DOOR_ITEM, "§cVoltar ao lobby")
                .interact(event -> event.getPlayer().performCommand("lobby")));
    }

    public void handleDeath(Arena arena, User user, User killer, DeathCause cause) {
        Player player = user.getAccount().player();

        boolean hasKiller = killer != null;

        player.clearPotionEffects();

        if (player.getHealth() < player.getMaxHealth())
            player.setHealth(player.getMaxHealth());

        /* Restaurar Cooldown */
        BukkitCore.getManager().getCooldown().resetCooldown(player);

        user.setCombat((Player) null);
        user.setDeaths();

        Team team = user.getTeam();

        team.setRemainingLives(team.getRemainingLives() - 1);

        sendDeathMessage(arena, user, killer, cause);

        /* Caso for o abate final */
        final boolean finalKill = arena.isFinalKill(user);

        if (hasKiller) {
            Account account = killer.getAccount();

            Player killerPlayer = account.player();

            killer.setCombat((Player) null);
            killer.setKills();

            if (finalKill) {
                AquaticMember member = killer.getMember();

                int xp = Savage.RANDOM.ints(7, 15).findFirst().orElse(7) * 2;

                member.addEloXp(xp);

                killer.setXpGain(xp);

                account.send("§b+" + xp + " XP §7(Kill Final)");
            }

            // Enviar item aleatório
            Item randomItem = ItemList.getRandomItem();

            if (BukkitUtil.isInventoryFull(killerPlayer))
                account.send("§cO seu inventário está cheio! Não há como receber itens extras.");
            else {
                account.send("§aParabéns! Você recebeu um item extra.");

                killerPlayer.getInventory().addItem(randomItem);
            }

            updateSidebar(killer);
        }

        user.setState(finalKill ? ArcadeState.DEAD : ArcadeState.RESPAWN);

        if (finalKill) {
            TitleAnimation.sendDefeatFrame(player, "", TimeUnit.SECONDS.toMillis(5));

            arena.send("",
                    "§f§lTIME ELIMINADO §f» §cO " + team.getColor() + "Time " + team.getName() + "§c foi eliminado.",
                    "");

            arena.getUsers().forEach(target -> CompletableFuture.runAsync(() -> arena.updateSidebarTeams(target)));

            if (arena.getAliveUsers().size() == 1) {
                arena.setWinner(arena.getTeamList().stream().filter(search -> !search.equals(team)).findFirst().orElse(null));

                arena.setPhase(RoomPhase.ENDING);
                return;
            }

            user.setTag(Tag.SPECTATOR);

            handleHotbar(user);
        }

        player.playSound(Sound.BLAZE_DEATH);

        arena.getUsers().forEach(arena::updateSidebarTeams);

        /* Alterar Visibilidade */
        arena.hideAndShow(user);
    }

    /* Death Messages */
    private final List<String> voidByOtherMessages = Arrays.asList(
            "foi jogado no void por", "foi arremessado direto pro void por"
    ), voidMessages = Arrays.asList(
            "caiu no void", "sucumbiu no vazio", "tropeçou e caiu no void", "tentou nadar no limbo"
    ), waterMessages = Arrays.asList(
            "se afogou", "caiu na água"
    ), waterOtherMessages = Arrays.asList(
            "foi afogado por", "foi empurrado na água por"
    ), normalMessages = Arrays.asList(
            "teve sua cabeça arrancada por", "foi humilhado por", "não aguentou", "foi morto por"
    );

    public void sendDeathMessage(Arena arena, User user, User killer, DeathCause cause) {
        if (cause.equals(DeathCause.NONE)) return;

        boolean hasKiller = killer != null;

        if (hasKiller) {
            Account account = killer.getAccount();

            account.sound(Sound.SUCCESSFUL_HIT);
        }

        Account account = user.getAccount();

        boolean finalKill = arena.isFinalKill(user);

        String normalMessage = normalMessages.get(Savage.RANDOM.nextInt(normalMessages.size())),
                voidMessage = voidMessages.get(Savage.RANDOM.nextInt(voidMessages.size())),
                voidByOtherMessage = voidByOtherMessages.get(Savage.RANDOM.nextInt(voidByOtherMessages.size())),
                waterMessage = waterMessages.get(Savage.RANDOM.nextInt(waterMessages.size())),
                waterByOtherMessage = waterOtherMessages.get(Savage.RANDOM.nextInt(waterOtherMessages.size()));

        String killerName = "";

        if (killer != null)
            killerName = killer.getTeam().getColor() + killer.getAccount().getNickname();

        arena.send(user.getTeam().getColor() + account.getNickname() + " §7" +
                (cause.equals(DeathCause.PLAYER)
                        ? (hasKiller ? normalMessage + " " + killerName : "morreu")
                        : cause.equals(DeathCause.WATER)
                        ? (hasKiller ? waterByOtherMessage + " " + killerName : waterMessage)
                        : (hasKiller ? voidByOtherMessage + " " + killerName : voidMessage))
                + "§7." + (hasKiller && finalKill ? " §f§lKILL FINAL!" : "")
        );
    }

    public enum DeathCause {
        PLAYER, VOID, WATER, NONE
    }

    public void updateStatisticalData(Team winner, List<Team> losers) {
        if (winner != null) {
            for (UUID id : winner.getMembers()) {
                User user = (User) User.of(id);

                if (user == null) continue;

                AquaticMember member = user.getMember();

                Arena arena = user.getArena();

                ArcadeStats stats = member.getStats(arena.getArcade().getCategory());

                int gainXp = Savage.RANDOM.ints(40, 140).findFirst().orElse(35);

                stats.setWins();
                stats.setWinStreak();

                member.addLevelXp(gainXp);

                member.updateStats(stats);

                user.setXpGain(user.getXpGain() + gainXp);
                user.getAccount().send("§b+" + gainXp + " XP");
            }
        }

        if (losers != null && !losers.isEmpty()) {
            for (Team loser : losers) {
                for (UUID id : loser.getMembers()) {
                    User user = (User) User.of(id);

                    if (user == null) continue;

                    AquaticMember member = user.getMember();

                    Arena arena = user.getArena();

                    ArcadeStats stats = member.getStats(arena.getArcade().getCategory());

                    int lostXp = Savage.RANDOM.ints(30, 45).findFirst().orElse(25);

                    stats.setDefeats();
                    stats.setWinStreak(0);

                    member.removeLevelXp(lostXp);

                    member.updateStats(stats);
                }
            }
        }
    }
}
