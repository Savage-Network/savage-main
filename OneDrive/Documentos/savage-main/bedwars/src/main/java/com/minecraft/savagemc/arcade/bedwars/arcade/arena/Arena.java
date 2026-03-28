package com.minecraft.savagemc.arcade.bedwars.arcade.arena;

import com.google.gson.JsonObject;
import com.minecraft.savagemc.arcade.bedwars.arcade.Arcade;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.ArenaContext;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.event.Event;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.event.phase.EventPhase;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.top.Top;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.top.enums.TopCategory;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.top.enums.TopType;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.top.user.TopUser;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.phase.ArenaPhaseExecutor;
import com.minecraft.savagemc.arcade.bedwars.menu.shop.ShopMenu;
import com.minecraft.savagemc.arcade.bedwars.menu.upgrade.UpgradeMenu;
import com.minecraft.savagemc.arcade.bedwars.structure.generator.objects.level.GeneratorLevel;
import com.minecraft.savagemc.arcade.bedwars.structure.generator.objects.type.forge.ForgeGenerator;
import com.minecraft.savagemc.arcade.bedwars.structure.generator.objects.type.ore.list.DiamondGenerator;
import com.minecraft.savagemc.arcade.bedwars.structure.generator.objects.type.ore.list.EmeraldGenerator;
import com.minecraft.savagemc.arcade.bedwars.structure.team.Team;
import com.minecraft.savagemc.arcade.bedwars.structure.generator.objects.type.ore.OreGenerator;
import com.minecraft.savagemc.arcade.bedwars.structure.team.objects.forge.Forge;
import com.minecraft.savagemc.arcade.bedwars.user.User;
import com.minecraft.savagemc.arcade.bedwars.user.context.UserContext;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.tag.Tag;
import com.minecraft.savagemc.core.api.reconnect.Reconnect;
import com.minecraft.savagemc.core.arcade.room.Room;
import com.minecraft.savagemc.core.arcade.room.map.Map;
import com.minecraft.savagemc.core.arcade.room.map.area.Cuboid;
import com.minecraft.savagemc.core.arcade.room.map.location.SyntheticLocation;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.arcade.room.slot.Slot;
import com.minecraft.savagemc.core.arcade.room.team.preset.TeamPresetType;
import com.minecraft.savagemc.core.arcade.room.type.Type;
import com.minecraft.savagemc.core.arcade.route.state.ArcadeState;
import com.minecraft.savagemc.core.arcade.slime.SlimeWorldController;
import com.minecraft.savagemc.core.bukkit.api.firework.FireworkApi;
import com.minecraft.savagemc.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.savagemc.core.bukkit.api.npc.type.client.NpcClient;
import com.minecraft.savagemc.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.savagemc.core.member.list.bedwars.BedMember;
import com.minecraft.savagemc.core.member.list.bedwars.objects.enums.BedOre;
import com.minecraft.savagemc.core.member.list.bedwars.objects.enums.BedShop;
import com.minecraft.savagemc.core.member.list.bedwars.objects.enums.BedSkin;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.SellerCollectible;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.list.Default;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.list.Mirror;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.list.Random;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.profile.ProfileType;
import com.minecraft.savagemc.core.util.list.TimeUtil;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
public class Arena extends Room {

    private final ArenaContext context;

    public Arena(int id, Arcade arcade, Map map, Slot slot, Type type) {
        super(id, arcade, map, slot, type);

        this.context = new ArenaContext();
    }

    public void unload() {
        getUsers().forEach(user -> user.getAccount().redirectToHub());

        if (getPlayers().isEmpty()) {
            Arcade arcade = getArcade();

            SlimeWorldController.unloadWorld(getWorld(), () -> arcade.removeArena(this));
        }
    }

    public void reload(Player author) {
        Arcade arcade = getArcade();

        Arena arena = arcade.setupRoom(getMap(), getSlot(), getType());

        arena.setCustom(getCustom());

        Savage.getPlatform().runSync(() -> {
            if (arena.getWorld() != null) {
                send("§b" + author.getName() + "§e reiniciou a sala.");

                getUsers().forEach(user -> {
                    user.setArena(arena);

                    arena.join(user.getPlayer());
                });

                if (getPlayers().isEmpty())
                    SlimeWorldController.unloadWorld(getWorld(), () -> arcade.removeArena(this));
            }
        }, 10L);
    }

    @Override
    public void join(Player player) {
        User user = (User) User.of(player.getUniqueId());

        UserContext context = user.getContext();

        Arcade arcade = user.getArcade();

        Account account = user.getAccount();

        user.setTag(!user.isPlayer() ? Tag.SPECTATOR : account.getTag());

        getPlayers().add(player);

        player.refresh();
        player.getEnderChest().clear();

        Location spawn = getLocation("spawn");

        user.setTeam(null);

        if (user.isPlayer()) {
            /* Lidar com reconexão */
            if (context.hasReconnect()) {
                Reconnect reconnect = context.getReconnect();

                Team team = getTeamList().stream()
                        .filter(search -> search.getCodeId().equalsIgnoreCase(reconnect.getTeamId()))
                        .findFirst()
                        .orElse(null);

                if (team != null) {
                    team.setBedExpiresAt(-1L);

                    user.setTeam(team);

                    Tag tag = Tag.of(team.getTypeName());

                    if (tag == null || !team.hasBed())
                        tag = Tag.SPECTATOR;

                    team.getMembers().add(player.getUniqueId());

                    player.teleport(team.getBase().clone().add(0, 20, 0));

                    if (team.hasBed())
                        user.setState(ArcadeState.RESPAWN);
                    else {
                        user.setState(ArcadeState.DEAD);

                        player.sendMessage("§cO seu time foi eliminado.");

                        account.title("§c§lVOCÊ MORREU!", "§eO seu time foi eliminado!");
                    }

                    user.setTag(tag);

                    handleEntities(user);
                }

                Savage.getReconnectData().delete(player.getUniqueId());
            } else {
                user.setState(ArcadeState.ALIVE);

                player.teleport(spawn);
            }

            Tag tag = user.getTag();

            if (context.hasReconnect())
                getUsers().forEach(this::updateSidebarTeams);

            if (!user.inState(ArcadeState.DEAD))
                send(tag.getColor() + (tag.ordinal() >= Tag.MIDIA_PLUS.ordinal() ? "§o" : "") + account.getNickname()
                        + (!context.hasReconnect()
                        ? "§e entrou na sala. (§b" + getTotalPlayers() + "§e/§b" + getMaxPlayers() + "§e)"
                        : "§e reconectou."));

        } else {
            handleVanish(player);

            player.teleport(spawn);
        }

        hideAndShow(user);

        arcade.load(user);

        account.setRoute(this, "", user.getState(), user.getJoin());
    }

    @Override
    public void quit(Player player) {
        User user = (User) User.of(player.getUniqueId());

        if (user.isPlayer() && !user.inState(ArcadeState.DEAD)) {
            Team team = user.getTeam();

            if (team != null) {
                team.getMembers().remove(player.getUniqueId());

                if (isPhase(RoomPhase.PLAYING)) {
                    Savage.getPlatform().runSync(() -> {
                        boolean hasReconnect = Savage.getReconnectData().of(player.getUniqueId()) != null;

                        send(team.getColor() + player.getName() + "§e desconectou." +
                                (hasReconnect ? " Quebre a cama dele para evitar que renasça." : ""));

                        // Tem reconexão/cama, e os membros ficaram vazios, adicionar expiração para a cama
                        if (team.getMembers().isEmpty() && team.hasBed()) {
                            if (hasReconnect) {
                                team.setBedExpiresAt(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1));
                            } else {
                                team.destructBed();

                                getArcade().handleDefeatStats(user);
                            }

                            getUsers().forEach(this::updateSidebarTeams);
                        }
                    }, 3L);
                }
            }
        }

        getPlayers().remove(player);
    }

    public void handlePulse() {
        if (isPhase(RoomPhase.WAITING) && canBeCount())
            setPhase(RoomPhase.STARTING);

        if (isPhase(RoomPhase.STARTING)) {
            // Pode contabilizar
            if (canBeCount()) {
                if (isFull() && getTime() > 10)
                    setTime(10);

                setTime(getTime() - 1);

                final int time = getTime();

                if (time == 10) {
                    title("§c§l" + getTime(), "§csegundos!");

                    sound(Sound.CLICK);
                }

                if (time <= 5 && time >= 1) {
                    send("§eA partida inicia em §c" + time + "§e segundo" + (time > 1 ? "s" : "") + "!");
                    title((time <= 3 ? "§a" : "§c") + time, "§ePrepare-se!");

                    sound(Sound.CLICK);
                }

                if (time <= 0)
                    setPhase(RoomPhase.PLAYING);
            } else if (!isCustom())
                setPhase(RoomPhase.WAITING);
        }

        if (isPhase(RoomPhase.PLAYING)) {
            setFullTime(getFullTime() + 1);

            /* Verificar camas */
            checkTeamBeds();

            if (getTotalPlayers() > 1) {
                /* Atualizar geradores */
                updateGenerators();

                /* Gerenciando eventos */
                Event event = getEvent();

                if (context.isCounterEnabled()) {
                    if (event != null) {
                        int eventTime = event.getTime();

                        if (eventTime > 0)
                            event.setTime(eventTime - 1);

                        if (eventTime == 0) {
                            event.handle(this);

                            updateEventSidebar(event);
                        } else
                            updateEventSidebar(event);
                    }
                }

                /* Verificando estado dos jogadores */
                for (Player player : getPlayers()) {
                    User user = (User) User.of(player.getUniqueId());

                    if (user == null || !user.isPlayer() || !user.inState(ArcadeState.RESPAWN) || user.getRespawnTime() < 0)
                        continue;

                    Account account = user.getAccount();

                    Arcade arcade = user.getArena().getArcade();

                    int time = user.getRespawnTime();

                    user.setRespawnTime(time - 1);

                    if (time >= 1 && time <= 5) {
                        account.title("§c§lVOCÊ MORREU!", "§eRenascendo em §c" + time + "§e segundo" + (time > 1 ? "s" : "") + "!",
                                60, 100);

                        account.send("§c" + time + "...");
                        account.sound(Sound.WOOD_CLICK);
                    }

                    if (time == 0) {
                        final ArcadeState state = user.getState();

                        if (!state.equals(ArcadeState.ALIVE))
                            account.sound(Sound.LEVEL_UP);

                        user.setState(ArcadeState.ALIVE);

                        if (user.inState(ArcadeState.ALIVE))
                            arcade.handleHotbar(user);

                        hideAndShow(user);

                        Location spawn = user.getTeam().getBase();

                        if (spawn != null)
                            player.teleport(spawn);

                        account.title("", "§aRenasceu!");
                    }
                }
            } else {
                send("§4§lAVISO: §cNão há jogadores suficientes para continuar a partida!");

                Team winner = getTeamList().stream().filter(team -> !team.getPlayers().isEmpty()).findFirst().orElse(null);

                setWinner(winner);
                setPhase(RoomPhase.ENDING);
            }
        }

        if (isPhase(RoomPhase.ENDING)) {
            setTime(getTime() - 1);

            if (getTime() <= 0 || getPlayers().isEmpty())
                setPhase(RoomPhase.RESETTING);

            if (hasWinner())
                getWinner().getPlayers().forEach(player -> Savage.getPlatform().runSync(() -> FireworkApi.random(player.getLocation())));
        }

        getAllUsers().forEach(user -> updateSidebarTime(user.getSidebar()));
    }

    protected void updateEventSidebar(Event event) {
        EventPhase phase = event.getPhase();

        // Atualizar na scoreboard
        getAllUsers().forEach(user -> {
            Sidebar sidebar = user.getSidebar();

            if (sidebar != null)
                sidebar.updateRow("event", " " + phase.getName() + " em §a" + TimeUtil.time(event.getTime()));
        });
    }

    public void chat(ChatType type, User user, String message) {
        Account account = user.getAccount();

        BedMember member = user.getMember();

        String rank = (member.isProfile(ProfileType.RANKED) ? member.getElo().getChat() : member.getLevelId()) + " ";

        Team team = user.getTeam();

        String prefix = !user.inState(ArcadeState.ALIVE) ? "§7[MORTO] " : "";

        if (Objects.requireNonNull(type) == ChatType.TEAM) {
            List<User> usersToSend = !user.inState(ArcadeState.DEAD)
                    ? getTeamUsers(team)
                    : getAllUsers().stream().filter(search -> !search.inState(ArcadeState.ALIVE)).collect(Collectors.toList());

            usersToSend.forEach(teamMember -> {
                Player player = teamMember.getPlayer();

                if (player != null) {
                    player.sendMessage((!user.inState(ArcadeState.DEAD) ? team.getBrackets() + " " : "")
                            + prefix + rank + account.getTag().getByPrefix(teamMember.getAccount().getTagPrefix())
                            + account.getNickname() + ": §f" + message);
                }
            });

        } else {
            List<User> usersToSend = !user.inState(ArcadeState.DEAD)
                    ? getUsers()
                    : getAllUsers().stream().filter(search -> !search.inState(ArcadeState.ALIVE)).collect(Collectors.toList());

            usersToSend.forEach(target -> {
                Player player = target.getPlayer();

                if (player != null)
                    player.sendMessage(
                            (!isSlot(Slot.SOLO) ? "§6[G] " : "") +
                                    (team != null && isPhase(RoomPhase.PLAYING) ?
                                            (!user.inState(ArcadeState.DEAD) ? team.getBrackets() + " " : "") : "")
                                    + prefix + rank + account.getTag().getByPrefix(target.getAccount().getTagPrefix())
                                    + account.getNickname() + ": §f" + message
                    );
            });
        }
    }

    public enum ChatType {
        TEAM, GLOBAL
    }

    public Arcade getArcade() {
        return (Arcade) super.getArcade();
    }

    @Override
    public void setPhase(RoomPhase phase) {
        if (isPhase(phase)) return;

        super.setPhase(phase);
        ArenaPhaseExecutor.execute(this, phase);
    }

    @Override
    public boolean canBeCount() {
        return context.isCounterEnabled() && getTotalPlayers() >= getMinPlayers();
    }

    public boolean isProtectedArea(Location center) {
        // Verificando se está tentando na região dos geradores
        for (OreGenerator generator : getGenerators()) {
            Location location = generator.getLocation();

            if (location != null && center.getWorld().equals(center.getWorld()) && center.distance(location) <= 3)
                return true;
        }

        // Verificando se está tentando no spawn de algum time
        JsonObject area = getMap().getConfig().get("area_protection").getAsJsonObject();

        int frontAndBack = area.get("frontAndBack").getAsInt(),
                up = area.get("up").getAsInt(),
                side = area.get("side").getAsInt();

        for (Team team : getTeamList()) {
            Location base = team.getBase();

            SyntheticLocation synthetic = getSynthetic(team.getTypeName().toLowerCase() + "_spawn");

            boolean axisX = synthetic != null && synthetic.isAxisX();

            if (Cuboid.isProtectedArea(base, center, frontAndBack, up, side, axisX))
                return true;
        }

        return false;
    }

    public void handleEntities(User user) {
        Player player = user.getPlayer();

        BedMember member = user.getMember();

        Account account = user.getAccount();

        Team userTeam = user.getTeam();

        /* Iniciando NPCs das outras ilhas */
        for (Team team : getTeamList()) {
            if (team.equals(userTeam)) continue;

            BedSkin skin = BedSkin.of(team.getName());

            handleTeamNpc(player, team, skin.getValue(), skin.getSignature());
        }

        /* Iniciando NPCs da ilha do jogador */
        String value, signature;

        SellerCollectible seller = (SellerCollectible) member.getActiveCollectible(BedCollectibleType.SELLER_SKIN);

        if (seller != null && seller.isNotEqual(Default.class)) {
            if (!seller.isNotEqual(Random.class)) {
                seller = (SellerCollectible) member.getRandomCollectible(BedCollectibleType.SELLER_SKIN);

                if (seller == null)
                    seller = (SellerCollectible) member.getCollectible(BedCollectibleType.SELLER_SKIN, "Default");
            }

            value = seller.isNotEqual(Mirror.class) ? seller.getValue() : account.getSkin().getValue();
            signature = seller.isNotEqual(Mirror.class) ? seller.getSignature() : account.getSkin().getSignature();
        } else {
            BedSkin bedSkin = BedSkin.of(userTeam.getName());

            value = bedSkin.getValue();
            signature = bedSkin.getSignature();
        }

        handleTeamNpc(player, userTeam, value, signature);
    }

    protected void handleTeamNpc(Player player, Team team, String value, String signature) {
        Location shopLocation = getLocation("npc_shop_" + team.getTypeName().toLowerCase());

        if (shopLocation != null) {
            NpcClient shop = getArcade().getManager().getNpc().spawnClient(player, shopLocation, value, signature);

            shop.setAction((target, action) -> {
                if (ableToInteract(target))
                    new ShopMenu(target, BedShop.FAVORITE).handle();
            });

            shop.display();
        }

        Location upgradeLocation = getLocation("npc_upgrade_" + team.getTypeName().toLowerCase());

        if (upgradeLocation != null) {
            NpcClient upgrade = getArcade().getManager().getNpc().spawnClient(player, upgradeLocation, value, signature);

            upgrade.setAction((target, action) -> {
                if (ableToInteract(target))
                    new UpgradeMenu(target).handle();
            });

            upgrade.display();
        }
    }

    protected boolean ableToInteract(Player player) {
        User user = (User) User.of(player.getUniqueId());

        return user != null && user.isPlayer() && user.inState(ArcadeState.ALIVE);
    }

    public Event getEvent() {
        return context.getEvent();
    }

    public List<Team> getTeamList() {
        return getTeams().stream().map(team -> (Team) team).collect(Collectors.toList());
    }

    @Override
    public Team buildTeam(TeamPresetType preset) {
        Location location = getLocation(preset.name().toLowerCase() + "_spawn");

        if (location == null)
            location = getLocation(preset.getName().toLowerCase() + "_spawn");

        return new Team(preset, this, location, getSlot().getMaxPlayers() / 2);
    }

    public List<Team> buildTeams() {
        return Arrays.stream(TeamPresetType.values())
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .map(this::buildTeam)
                .collect(Collectors.toList());
    }

    public List<Team> buildTeams(ChatColor... colors) {
        List<Team> teamList = buildTeams();

        if (colors == null || colors.length == 0)
            return teamList;

        Set<ChatColor> colorSet = new HashSet<>(Arrays.asList(colors));

        return teamList.stream()
                .filter(team -> colorSet.contains(team.getColor()))
                .collect(Collectors.toList());
    }

    public Team getTeam(Location bedLocation) {
        return getTeamList().stream().filter(team -> team.isYourBed(bedLocation)).findFirst().orElse(null);
    }

    public List<Team> getAliveTeams() {
        return getTeamList().stream().filter(team -> !team.isDead()).collect(Collectors.toList());
    }

    public List<User> getAllUsers() {
        return User.getList().stream().filter(user -> isPlayer(user.getAccount().getId()))
                .map(user -> (User) user).collect(Collectors.toList());
    }

    public List<User> getUsers() {
        return User.getList().stream()
                .filter(user -> user != null && user.isPlayer() && isPlayer(user.getAccount().getId()))
                .map(user -> (User) user)
                .collect(Collectors.toList());
    }

    public List<User> getAliveUsers() {
        return getUsers().stream().filter(user -> user.inState(ArcadeState.ALIVE)).collect(Collectors.toList());
    }

    public List<User> getAliveUsers(Team team) {
        return getAliveUsers().stream().filter(user -> user.getTeam() != null && user.getTeam().equals(team)).collect(Collectors.toList());
    }

    public List<User> getTeamUsers(Team team) {
        return getAliveUsers().stream().filter(user -> user.getTeam().equals(team)).collect(Collectors.toList());
    }

    @Override
    public int getTotalPlayers() {
        return getUsers().size();
    }

    public boolean hasTeamsWithBed() {
        return getTeamList().stream().anyMatch(Team::hasBed);
    }

    /**
     * Filtrar visibilidade dos jogadores conforme o estado atual.
     *
     * @param user Usuário a ser filtrado
     */
    public void hideAndShow(User user) {
        Player mainPlayer = user.getAccount().player();

        // 1º passo: Escondendo todos os jogadores
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target == null || target.equals(mainPlayer)) continue;

            target.hidePlayer(mainPlayer);
            mainPlayer.hidePlayer(target);
        }

        // 2º passo: Realizar filtragem e ver para quem mostrar o "mainPlayer"
        for (Player targetPlayer : getPlayers()) {
            if (targetPlayer == null || mainPlayer.equals(targetPlayer))
                continue;

            User target = (User) User.of(targetPlayer.getUniqueId());

            if (target == null || !target.getArena().equals(user.getArena())) continue;

            if (user.isVanish()) {
                mainPlayer.showPlayer(targetPlayer);

                if (target.isVanish())
                    targetPlayer.showPlayer(mainPlayer);
            }

            if (user.isSpectator() && !target.isVanish()) {
                mainPlayer.showPlayer(targetPlayer);

                if (target.isSpectator())
                    targetPlayer.showPlayer(mainPlayer);
            }

            if (user.isPlayer()) {
                if (isPhase(RoomPhase.ENDING) && target.isPlayer()) {
                    mainPlayer.showPlayer(targetPlayer);
                    targetPlayer.showPlayer(mainPlayer);
                }

                if (target.isPlayer() && !target.inState(ArcadeState.ALIVE) && !user.inState(ArcadeState.ALIVE))
                    mainPlayer.showPlayer(targetPlayer);

                if (target.isPlayer() && target.inState(ArcadeState.ALIVE))
                    mainPlayer.showPlayer(targetPlayer);

                if (user.inState(ArcadeState.ALIVE))
                    targetPlayer.showPlayer(mainPlayer);
            }
        }
    }

    public void renderSidebarTeams(User user) {
        Sidebar sidebar = user.getSidebar();

        Team userTeam = user.getTeam();

        for (Team team : getTeamList()) {

            sidebar.addRow(team.getCodeId(), team.getDisplay() + ": "
                    + team.getBedName(getAliveUsers(team).size())
                    + (user.isPlayer() && team.equals(userTeam) && !user.inState(ArcadeState.DEAD)
                    ? " §7(VOCÊ)" : ""));
        }
    }

    public void updateSidebarTeams(User user) {
        Sidebar sidebar = user.getSidebar();

        Team userTeam = user.getTeam();

        for (Team team : getTeamList()) {

            sidebar.updateRow(team.getCodeId(), team.getDisplay() + ": "
                    + team.getBedName(getAliveUsers(team).size())
                    + (user.isPlayer() && team.equals(userTeam) && !user.inState(ArcadeState.DEAD)
                    ? " §7(VOCÊ)" : ""));
        }
    }

    public void renderSidebarTime(Sidebar sidebar) {
        String prefix = isPhase(RoomPhase.STARTING)
                ? "Inicia em §a"
                : isPhase(RoomPhase.ENDING)
                ? "§cFim de jogo!"
                : "§7Aguardando...";

        String suffix = isPhase(RoomPhase.STARTING)
                ? TimeUtil.formatTime(getTime())
                : "";

        sidebar.addRow("time", prefix + suffix);
        sidebar.blankRow();
    }

    public void updateSidebarTime(Sidebar sidebar) {
        String prefix = isPhase(RoomPhase.STARTING)
                ? "Inicia em §a"
                : isPhase(RoomPhase.ENDING)
                ? "§cFim de jogo!"
                : "§7Aguardando...";

        String suffix = isPhase(RoomPhase.STARTING)
                ? TimeUtil.formatTime(getTime())
                : "";

        sidebar.updateRow("time", prefix + suffix);
        sidebar.updateRow("players", "Jogadores: §a" + getTotalPlayers() + "/" + getMaxPlayers());
    }

    public Top getTop(TopType type) {
        return context.getTop(type);
    }

    public boolean inTop(User user) {
        return context.getTops().stream().anyMatch(top -> top.inTop(user.getAccount().getId()));
    }

    public void verifyTop(TopType type, UUID id, int value) {
        Top top = getTop(type);

        if (top != null)
            top.setTop(id, value);
    }

    public TopUser getTopUser(TopType type, UUID id) {
        Top top = getTop(type);

        if (top == null) {
            Savage.Savage.Logger().warning("O top " + type.name() + " é nulo.");
            return null;
        }

        return top.getUser(id);
    }

    public TopUser getTopUser(TopType type, TopCategory category) {
        Top top = getTop(type);

        if (top == null) {
            Savage.Savage.Logger().warning("O top " + type.name() + " é nulo.");
            return null;
        }

        return top.getUser(category);
    }

    public boolean isFinalKill(User user) {
        Team team = user.getTeam();

        return !team.hasBed() && getAliveUsers(team).size() <= 1;
    }

    /* Lidando com o sistema de geradores */
    public List<OreGenerator> getGenerators() {
        return context.getGenerators();
    }

    public void spawnGenerators() {
        // Geradores de Diamante / Esmeralda global
        getLocationsByName("ore_generator_" + BedOre.DIAMOND.name().toLowerCase())
                .forEach(signed -> getGenerators().add(new DiamondGenerator(getTotalGenerators(BedOre.DIAMOND) + 1, this)));

        getLocationsByName("ore_generator_" + BedOre.EMERALD.name().toLowerCase())
                .forEach(signed -> getGenerators().add(new EmeraldGenerator(getTotalGenerators(BedOre.EMERALD) + 1, this)));

        // Geradores de Ferro / Ouro para os times
        for (Team team : getTeamList()) {
            if (team == null) continue;

            Forge forge = team.getForge();

            if (forge != null)
                forge.spawn();
        }
    }

    public void checkTeamBeds() {
        for (Team team : getTeamList()) {
            if (team.hasExpired()) {
                Savage.getPlatform().runSync(() -> {
                    team.destructBed();

                    Reconnect reconnect = Savage.getReconnectData().of(getIdentifier(), team.getCodeId());

                    if (reconnect != null)
                        Savage.getReconnectData().delete(reconnect.getSender());

                    getUsers().forEach(this::updateSidebarTeams);
                });

                send("§cO time " + team.getColoredName() + "§c foi eliminado.");
            }
        }
    }

    public void updateGenerators() {
        // Geradores dos times
        for (Team team : getTeamList()) {
            if (team == null) continue;

            Forge forge = team.getForge();

            for (ForgeGenerator generator : forge.getGenerators()) {
                if (generator != null && generator.hasPendentUpdate()) {
                    generator.resetTime();

                    Savage.getPlatform().runSync(generator::spawn);
                }
            }
        }

        // Geradores globais
        for (OreGenerator generator : getGenerators()) {
            HologramServer hologram = generator.getHologram();

            if (hologram == null) continue;

            generator.setTime(generator.getTime() - 1);

            int time = generator.getTime();

            if (time <= 0) {
                Savage.getPlatform().runSync(generator::spawn);

                generator.setTime(generator.getUpdateTime());
            }

            int lastRow = hologram.getRows().size() - 2;

            if (time <= 0)
                time = 1;

            hologram.setText(0, "§eNível §c" + generator.getLevel().getTag());
            hologram.setText(lastRow, "§eGera em §c" + time + "§e segundo" + (time > 1 ? "s" : ""));
        }
    }

    public void upgradeGenerators(BedOre ore, int updateTime) {
        for (OreGenerator generator : getGenerators(ore)) {
            GeneratorLevel next = GeneratorLevel.values()[generator.getLevel().ordinal() + 1];

            generator.setLevel(next);

            generator.setUpdateTime(updateTime);
            generator.setTime(updateTime);
        }
    }

    public GeneratorLevel getGeneratorsLevel(BedOre ore) {
        OreGenerator generator = getGenerators(ore).stream().findFirst().orElse(null);

        return generator != null ? generator.getLevel() : GeneratorLevel.NONE;
    }

    public List<OreGenerator> getGenerators(BedOre ore) {
        return getGenerators().stream().filter(generator -> generator.getOre().equals(ore)).collect(Collectors.toList());
    }

    public OreGenerator getGenerator(BedOre ore, int id) {
        return getGenerators(ore).stream().filter(generator -> generator.getId() == id).findFirst().orElse(null);
    }

    public int getTotalGenerators() {
        return getGenerators().size();
    }

    public int getTotalGenerators(BedOre ore) {
        return getGenerators(ore).size();
    }
}
