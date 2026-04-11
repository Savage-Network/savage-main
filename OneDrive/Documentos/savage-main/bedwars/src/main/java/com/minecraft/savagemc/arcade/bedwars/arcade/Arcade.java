package com.minecraft.savagemc.arcade.bedwars.arcade;

import com.grinderwolf.swm.plugin.SWMPlugin;
import com.minecraft.savagemc.arcade.bedwars.BedWars;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.event.Event;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.event.phase.EventPhase;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.top.enums.TopType;
import com.minecraft.savagemc.arcade.bedwars.menu.custom.team.TeamSelectorMenu;
import com.minecraft.savagemc.arcade.bedwars.menu.game.play.PlayAgainMenu;
import com.minecraft.savagemc.arcade.bedwars.menu.game.teleport.TeleportMenu;
import com.minecraft.savagemc.arcade.bedwars.structure.team.Team;
import com.minecraft.savagemc.arcade.bedwars.user.User;
import com.minecraft.savagemc.arcade.bedwars.user.context.UserContext;
import com.minecraft.savagemc.core.bukkit.user.combat.Combat;
import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.tag.Tag;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.api.redirect.Redirect;
import com.minecraft.savagemc.core.arcade.ArcadeHolder;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.arcade.room.Room;
import com.minecraft.savagemc.core.arcade.room.custom.RoomCustom;
import com.minecraft.savagemc.core.arcade.room.map.Map;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.arcade.room.slot.Slot;
import com.minecraft.savagemc.core.arcade.room.type.Type;
import com.minecraft.savagemc.core.arcade.route.ArcadeRouteContext;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import com.minecraft.savagemc.core.arcade.route.state.ArcadeState;
import com.minecraft.savagemc.core.arcade.slime.SlimeWorldController;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.savagemc.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.savagemc.core.bukkit.api.title.TitleAnimation;
import com.minecraft.savagemc.core.bukkit.manager.Manager;
import com.minecraft.savagemc.core.bukkit.manager.list.TagManager;
import com.minecraft.savagemc.core.bukkit.menu.server.arcade.mode.bedwars.BedWarsMenu;
import com.minecraft.savagemc.core.member.list.bedwars.BedMember;
import com.minecraft.savagemc.core.member.list.bedwars.objects.enums.item.BedWarsItem;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.stats.BedStats;
import com.minecraft.savagemc.core.server.type.ServerType;
import com.minecraft.savagemc.core.util.Util;
import com.minecraft.savagemc.core.util.list.DateUtil;
import com.minecraft.savagemc.core.util.list.TimeUtil;
import com.minecraft.savagemc.core.util.list.bukkit.BukkitUtil;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.minecraft.savagemc.core.util.list.bukkit.BukkitUtil.getItemAmount;

public abstract class Arcade extends ArcadeHolder {

    private final File mapsDirectory;

    @Getter
    private final Manager manager;

    public Arcade(String mapsDirectory, Integer minRooms, Integer maxRooms, ArcadeCategory category) {
        super(mapsDirectory, minRooms, maxRooms, category);

        this.mapsDirectory = new File(BedWars.getInstance().getDataFolder().getAbsolutePath() + "/maps");

        this.manager = BukkitCore.getManager();
    }

    @Override
    public boolean load() {
        Savage.getLogger().info("[" + getName() + "] Iniciando jogo...");

        if (!loadMaps(mapsDirectory)) return false;

        List<Map> availableMaps = getMaps();

        if (availableMaps.isEmpty()) {
            Savage.getLogger().warning("[" + getName() + "] Não há mapas disponíveis para este jogo.");
            return false;
        }

        int numRooms = getMinRooms(),
                casualRooms = (int) Math.floor(numRooms * 0.7), // 70% das salas serão casual,
                rankedRooms = numRooms - casualRooms;

        List<Type> availableTypes = new ArrayList<>();

        for (int i = 0; i < casualRooms; i++)
            availableTypes.add(Type.CASUAL);

        for (int i = 0; i < rankedRooms; i++)
            availableTypes.add(Type.COMPETITIVE);

        Collections.shuffle(availableTypes); // embaralha a lista para que os modos sejam distribuídos aleatoriamente

        int mapIndex = 0;
        for (Type type : availableTypes) {
            Map map = availableMaps.get(mapIndex);

            if (setupRoom(map, getCategory().getSlots().get(0), type) == null) return false;

            mapIndex = (mapIndex + 1) % availableMaps.size();
        }

        Savage.getLogger().info("[" + getName() + "] Jogo iniciado com sucesso.");
        return true;
    }

    @Override
    public void unload() {
        Savage.getLogger().info("[" + getName() + "] Desligando dados...");

        getRooms().clear();

        Savage.getLogger().info("[" + getName() + "] O jogo foi interrompido.");
    }

    @Override
    public Arena setupRoom(Map map, Slot slot, Type type) {
        Instant now = Instant.now();

        // Gerando ID da sala
        int id = (getId().getAndIncrement() + 1);

        Arena arena = new Arena(id, this, map, slot, type);

        Savage.getLogger().info("[" + getName() + "/" + id + "] Iniciando arena...");

        String templateName = getName().toLowerCase() + "-" + map.getName().toLowerCase(),
                worldName = templateName + "-" + id;

        handleArenaData(templateName, worldName, arena, id, now);

        return arena;
    }

    public void removeArena(Arena root) {
        root.getGenerators().clear();
        root.getTeams().clear();

        getRooms().remove(root);
    }

    public Arena copyArena(Arena root) {
        Instant now = Instant.now();

        int id = root.getId();

        Map map = root.getMap();

        Arena arena = new Arena(id, this, map, root.getSlot(), root.getType());

        Savage.getLogger().info("[" + getName() + "/" + id + "] Copiando arena...");

        String[] mapSplit = map.getName().split(" ");

        String mapName = mapSplit.length > 1 ? mapSplit[0] + "-" + mapSplit[1] : map.getName();

        String templateName = getName().toLowerCase() + "-" + mapName.toLowerCase(),
                worldName = templateName + "-" + id;

        handleArenaData(templateName, worldName, arena, id, now);

        return arena;
    }

    protected void handleArenaData(String template, String world, Arena arena, int id, Instant now) {
        ArcadeCategory arcade = arena.getArcade().getCategory();
        Slot slot = arena.getSlot();

        SlimeWorldController.cloneWorldFromTemplate(SWMPlugin.getInstance(),
                template,
                world, () -> {
                    List<Team> teamList = arcade.name().contains("VERSUS")
                            ? arena.buildTeams(ChatColor.RED, ChatColor.BLUE)
                            : (slot.equals(Slot.TRIO) || slot.equals(Slot.QUARTET)
                            ? arena.buildTeams(ChatColor.RED, ChatColor.BLUE, ChatColor.GREEN, ChatColor.YELLOW)
                            : arena.buildTeams());

                    // Definir o número correto de jogadores por time baseado no slot
                    int playersPerTeam = slot.equals(Slot.SOLO) ? 1 : slot.equals(Slot.DUO) ? 2 : slot.equals(Slot.TRIO) ? 3 : 4;
                    teamList.forEach(team -> team.setMaxPlayers(playersPerTeam));

                    // Calcular minPlayers e maxPlayers
                    int minPlayers, maxPlayers;
                    
                    if (arcade.name().contains("VERSUS")) {
                        // Para VERSUS: 2 times, então min e max são baseados no número de jogadores por time
                        minPlayers = playersPerTeam * 2; // Ex: 2v2 = 4 players min
                        maxPlayers = playersPerTeam * 2; // Ex: 2v2 = 4 players max
                    } else {
                        // Para modos normais (8 times)
                        minPlayers = slot.equals(Slot.SOLO) ? 4 : slot.equals(Slot.DUO) ? 8 : slot.equals(Slot.TRIO) ? 6 : 12;
                        maxPlayers = slot.equals(Slot.DUO) ? 16 : slot.equals(Slot.TRIO) ? 12 : slot.equals(Slot.QUARTET) ? 16 : 8;
                    }

                    arena.getTeams().addAll(teamList);

                    arena.setMinPlayers(minPlayers);
                    arena.setMaxPlayers(maxPlayers);

                    arena.handleBorder();

                    handleEntities(arena);

                    getRooms().add(arena);

                    Savage.getLogger().info("[" + getName() + "/" + id + "] Arena iniciada com sucesso. " +
                            "(Tempo médio: " + Util.formatInstant(now) + ")");
                });
    }

    public void checkAndGenerateArenas() {
        int totalArenas = getRooms().size();
        int occupiedArenas = (int) getRooms().stream().filter(room -> !room.isAvailable()).count();

        if (occupiedArenas >= (totalArenas / 2)) {
            final int NUM_ARENAS_TO_GENERATE = 6;

            final int CASUAL_ROOMS = 3; // Quantidade fixa de salas CASUAL
            final int COMPETITIVE_ROOMS = 3; // Quantidade fixa de salas COMPETITIVO

            List<Type> availableTypes = new ArrayList<>();

            // Adiciona as salas de CASUAL
            for (int i = 0; i < CASUAL_ROOMS; i++)
                availableTypes.add(Type.CASUAL);

            // Adiciona as salas de COMPETITIVO
            for (int i = 0; i < COMPETITIVE_ROOMS; i++)
                availableTypes.add(Type.COMPETITIVE);

            Collections.shuffle(availableTypes); // Embaralha a lista para distribuição aleatória

            Savage.getLogger().info("[" + getName() + "] Gerando +" + NUM_ARENAS_TO_GENERATE + " arenas...");

            int loaded = 0;
            for (Type type : availableTypes) {
                Map randomMap = getRandomMap();

                if (randomMap != null) {
                    setupRoom(randomMap, randomMap.getArcade().getSlots().get(0), type);
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

        System.out.println("[Search] Iniciando busca para " + sender);
        System.out.println("[Search] Redirect: " + (redirect != null ? "SIM" : "NÃO"));

        // Se há redirecionamento, buscar a arena diretamente
        if (redirect != null) {
            Arena arena = findRedirectedArena(route);
            System.out.println("[Search] Arena redirecionada: " + (arena != null ? arena.getIdentifier() : "NULL"));
            return arena;
        }

        // Filtra as arenas conforme a rota e informações do jogador
        Stream<Arena> stream = getFilteredArenas(route);
        
        long totalArenas = stream.count();
        System.out.println("[Search] Total de arenas filtradas: " + totalArenas);
        
        // Recria o stream pois count() o consome
        stream = getFilteredArenas(route);

        // Filtra pela lógica de reconexão e reservas
        Arena result = stream.filter(arena -> isArenaValidForPlayer(arena, route, account, sender))
                .findFirst()
                .orElse(null);
                
        System.out.println("[Search] Arena encontrada: " + (result != null ? result.getIdentifier() : "NULL"));
        return result;
    }

    // Busca uma arena específica caso exista um redirecionamento
    private Arena findRedirectedArena(ArcadeRouteContext route) {
        return getRooms().stream()
                .filter(room -> room.isCategory(route.getArcade())
                        && room.getId() == route.getRoomId()
                        && room.isFiltered(route.getMapId()))
                .map(room -> (Arena) room)
                .findFirst()
                .orElse(null);
    }

    // Filtra as arenas com base nos parâmetros da rota
    private Stream<Arena> getFilteredArenas(ArcadeRouteContext route) {
        Stream<Arena> stream = getRooms().stream()
                .map(room -> (Arena) room)
                .filter(arena -> arena.getSlot().equals(route.getSlot()));

        if (route.getType() != null) {
            stream = stream.filter(arena -> arena.getType().equals(route.getType()));
        }

        if (route.hasMap()) {
            stream = stream.filter(arena -> arena.isFiltered(route.getMapId()));
        }

        return stream;
    }

    // Verifica se uma arena é válida para o jogador com base nas reservas e condições de reconexão
    private boolean isArenaValidForPlayer(Arena arena, ArcadeRouteContext route, Account account, UUID sender) {
        System.out.println("[Search] Filtrando " + arena.getIdentifier() + " para " + sender);
        System.out.println("[Search]   - Fase: " + arena.getPhase());
        System.out.println("[Search]   - Disponível: " + arena.isAvailable());
        System.out.println("[Search]   - Jogadores: " + arena.getPlayers().size() + "/" + arena.getMaxPlayers());

        // Verifica reconexão
        if (account.hasReconnect() && arena.getId() == route.getRoomId() && arena.getMap().getId() == route.getMapId()) {
            System.out.println("[Search]   - ACEITA (Reconexão)");
            return true;
        }

        // Verifica se a arena está disponível
        if (!arena.isAvailable()) {
            System.out.println("[Search]   - REJEITADA (Não disponível)");
            return false;
        }

        // Se for dono de uma party e a rota estiver vinculada, tenta adicionar reservas
        if (account.isPartyOwner() && route.isLinked() && !arena.hasReservations(route.getLink())) {
            arena.setReservations(route.getLink());
            System.out.println("[PT-Route] " + arena.getIdentifier() + " recebeu +" + route.getLink().size() + " reservas. " +
                    "Total de reservas: " + arena.getTotalReservations());
        }

        // Verifica se o jogador é dono da party e está reservado na arena
        if (account.isPartyOwner() && arena.isReserved() && arena.hasReservation(sender)) {
            Savage.getLogger().info("[PT-Route] O dono " + account.getNickname() + " entrou em " + arena.getIdentifier() + ".");
            System.out.println("[Search]   - ACEITA (Dono da party reservado)");
            return true;
        }

        // Se a arena estiver reservada para o jogador e não estiver cheia, permite a entrada
        if (arena.isReserved() && arena.hasReservation(sender) && !arena.isFull()) {
            arena.removeReservation(sender);
            System.out.println("[PT-Route] " + account.getNickname() + " entrou em " + arena.getIdentifier() + ".");
            System.out.println("[Search]   - ACEITA (Reservado)");
            return true;
        }

        // Caso contrário, a arena deve estar disponível
        boolean available = arena.isAvailable();
        System.out.println("[Search]   - " + (available ? "ACEITA" : "REJEITADA") + " (Disponibilidade final)");
        return available;
    }

    /**
     * Unused method
     */
    @Override
    public Room setupRoom(Map map, Slot slot) {
        return null;
    }

    public void load(User user) {
        handleSidebar(user);
        handleHotbar(user);
        handleTabList(user);
    }

    public void handleTabList(User user) {
        Player player = user.getPlayer();
        if (player != null) {
            player.setPlayerListHeaderFooter(new TextComponent(Constant.SERVER_TAB_HEADER),
                    new TextComponent(Constant.SERVER_TAB_FOOTER));
        }
    }

    public void handleSidebar(User user) {
        Arena arena = user.getArena();

        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.setTitle((arena.isType(Type.CASUAL) ? "§6" : arena.isCustom() ? "§b" : "§5") + "§lBED WARS");

        sidebar.addRow("date", "§7" + DateUtil.getCurrentDate() + " §8" + arena.getIdentifier()
                + (arena.isCustom() ? " (C)" : ""));

        sidebar.blankRow();

        // Renderizar scoreboard quando o jogo iniciou
        if (arena.isPhase(RoomPhase.PLAYING)) {
            Event event = arena.getEvent();

            if (event != null && event.isNotPhase(EventPhase.SUDDEN_DEATH)) {
                sidebar.addRow("next_event", "§ePróximo evento:");
                sidebar.addRow("event", " " + event.getPhase().getName() + " em §a" + TimeUtil.time(event.getTime()));

                sidebar.blankRow();
            }

            arena.renderSidebarTeams(user);

        } else {
            // Renderizar scoreboard quando o jogo ainda não iniciou

            sidebar.addRow("map", "Mapa: §a" + arena.getMap().getName());
            sidebar.addRow("mode", "Modo: §a" + arena.getArcade().getCategory().getName());

            sidebar.blankRow();
            arena.renderSidebarTime(sidebar);

            sidebar.addRow("players", "Jogadores: §a" + arena.getTotalPlayers() + "/" + arena.getMaxPlayers());
        }

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        
        // Atualizar tag com o time do jogador no BedWars
        Team team = user.getTeam();
        String teamSuffix = "";
        
        if (team != null) {
            // Pegar a primeira letra do nome do time
            String teamInitial = team.getName().substring(0, 1).toUpperCase();
            // Formato: [A] para Azul, [V] para Vermelho, etc
            teamSuffix = " " + team.getColor() + "[" + teamInitial + "]";
            
            // Se tiver clan, adicionar também
            if (user.getAccount().hasClan() && user.getAccount().getToggle().isAllowClanTag()) {
                teamSuffix += " " + user.getAccount().getClanTag();
            }
        }
        
        TagManager.updateTagWithSuffix(user.getAccount(), user.getAccount().getTag(), teamSuffix);
    }

    public void updateSidebar(User user) {
        // Método vazio - não atualiza mais as estatísticas na sidebar
    }

    public void handleHotbar(User user) {
        Player player = user.getAccount().player();

        Arena arena = user.getArena();

        PlayerInventory inv = player.getInventory();

        if (arena.isPhase(RoomPhase.PLAYING)) {
            player.clearInventory();

            if (user.isPlayer()) {
                if (user.inState(ArcadeState.ALIVE)) {
                    Team team = user.getTeam();

                    BukkitUtil.sendLeatherArmor(player, team.getRgb());

                    inv.setItem(0, new Item(Material.WOOD_SWORD).unbreakable());

                    if (team.hasSword())
                        team.getSword().applyEnchantment(player);

                    UserContext context = user.getContext();

                    BedWarsItem armor = context.getArmorItem();

                    if (armor != null) {
                        String name = armor.name().split("_")[1].toUpperCase();

                        inv.setLeggings(Item.of(Material.getMaterial(name + "_LEGGINGS")).unbreakable());
                        inv.setBoots(Item.of(Material.getMaterial(name + "_BOOTS")).unbreakable());
                    }

                    if (team.hasArmor())
                        team.getArmor().applyEnchantment(player);

                    if (context.getPickaxeItem() != null)
                        inv.addItem(Item.fromStack(context.getPickaxeItem().getStack()).unbreakable());

                    if (context.getAxeItem() != null)
                        inv.addItem(Item.fromStack(context.getAxeItem().getStack()).unbreakable());

                    // Haste
                    if (team.hasHaste())
                        Savage.getPlatform().runSync(() -> team.getHaste().applyEnchantment(player), 5L);

                    // Shears
                    if (context.isPurchasedShears())
                        inv.addItem(new Item(Material.SHEARS).unbreakable());
                } else
                    sendDeathHotbar(player, arena);
            }

        } else {
            if (user.isPlayer() && arena.isPreGame()) {
                player.clearInventory();

                inv.setItem(0, new Item(Material.EMERALD)
                        .name("§aMenu do " + ServerType.BEDWARS.getName())
                        .interact(event -> new BedWarsMenu(event.getPlayer()).handle()));

                // Itens da sala customizada
                if (arena.isCustom()) {
                    RoomCustom custom = arena.getCustom();

                    inv.setItem(custom.isAuthor(player.getUniqueId()) ? 3 : 4, Item.of(Material.NAME_TAG, "§aSelecionar time")
                            .interact(event -> new TeamSelectorMenu(event.getPlayer()).handle()));

                    if (custom.isAuthor(player.getUniqueId()))
                        inv.setItem(5, Item.of(Material.BREWING_STAND_ITEM, "§aGerenciar sala §7(/menu)")
                                .interact(event -> event.getPlayer().performCommand("menu")));
                }

                inv.setItem(8, Item.of(Material.ACACIA_DOOR_ITEM, "§cVoltar ao lobby")
                        .interact(event -> event.getPlayer().performCommand("lobby")));

            } else if (arena.isPhase(RoomPhase.ENDING))
                sendDeathHotbar(player, arena);
        }
    }

    protected void sendDeathHotbar(Player player, Arena arena) {
        PlayerInventory inv = player.getInventory();

        if (arena.isPhase(RoomPhase.PLAYING))
            inv.setItem(0, Item.of(Material.COMPASS, "§aTeleportador")
                    .interact(event -> new TeleportMenu(event.getPlayer()).handle()));

        inv.setItem(7, Item.of(Material.PAPER, "§aJogar novamente")
                .interact(event -> new PlayAgainMenu(event.getPlayer()).handle()));

        inv.setItem(8, Item.of(Material.ACACIA_DOOR_ITEM, "§cVoltar ao lobby")
                .interact(event -> event.getPlayer().performCommand("lobby")));
    }

    public void handleDeath(Arena arena, User user, User killer, DeathCause cause) {
        Player player = user.getAccount().player();

        UserContext context = user.getContext();

        boolean hasKiller = killer != null;

        player.clearPotionEffects();

        if (player.getHealth() < player.getMaxHealth())
            player.setHealth(player.getMaxHealth());

        /* Restaurar Cooldown */
        BukkitCore.getManager().getCooldown().resetCooldown(player);

        /* Rebaixar as Picaretas / Machados */
        context.downGradePickaxe(player);
        context.downGradeAxe(player);

        user.setCombat((Combat) null);
        user.setDeaths();

        /* Caso for o abate final */
        final boolean finalKill = arena.isFinalKill(user);

        if (hasKiller) {
            Account account = killer.getAccount();

            killer.setCombat((Combat) null);
            killer.setKills();

            /* Pegar minérios e enviar para o Killer */
            List<ItemStack> ores = new ArrayList<>();

            for (ItemStack content : player.getInventory().getContents()) {
                if (content != null && content.isOre())
                    ores.add(content);
            }

            Player target = account.player();

            if (!ores.isEmpty()) {
                ores.forEach(ore -> target.getInventory().addItem(ore));

                int iron = getItemAmount(player, Material.IRON_INGOT),
                        gold = getItemAmount(player, Material.GOLD_INGOT),
                        diamond = getItemAmount(player, Material.DIAMOND),
                        emerald = getItemAmount(player, Material.EMERALD);

                if (iron > 0)
                    target.sendMessage("§f+" + iron + " Ferros!");

                if (gold > 0)
                    target.sendMessage("§6+" + gold + " Ouros!");

                if (diamond > 0)
                    target.sendMessage("§b+" + diamond + " Diamantes!");

                if (emerald > 0)
                    target.sendMessage("§a+" + emerald + " Esmeraldas!");
            }

            if (finalKill) {
                BedMember member = killer.getMember();

                int xp = Savage.RANDOM.ints(7, 15).findFirst().orElse(7) * 2;

                if (arena.isType(Type.CASUAL))
                    member.addLevelXp(xp);
                else
                    member.addEloXp(xp);

                killer.setXpGain(xp);
                killer.setFinalKill();

                arena.verifyTop(TopType.FINAL_KILL, killer.getAccount().getId(), context.getFinalKill());

                account.send("§b+" + xp + " XP §7(Kill Final)");
                
                // Executar efeito de kill final se o jogador tiver um colecionável equipado
                member.getActiveCollectibles().stream()
                    .filter(collectible -> collectible.getType() == BedCollectibleType.FINAL_KILL)
                    .findFirst()
                    .ifPresent(collectible -> {
                        try {
                            // Tentar executar o método execute(Location) se existir
                            java.lang.reflect.Method executeMethod = collectible.getClass().getMethod("execute", Location.class);
                            executeMethod.invoke(collectible, player.getLocation());
                        } catch (Exception e) {
                            // Fallback para o efeito de raio padrão
                            if (collectible.getName().equalsIgnoreCase("Raio")) {
                                player.getWorld().strikeLightningEffect(player.getLocation());
                            }
                        }
                    });
            }

            updateSidebar(killer);
        }

        user.setState(finalKill ? ArcadeState.DEAD : ArcadeState.RESPAWN);

        if (finalKill) {
            Team team = user.getTeam();

            TitleAnimation.sendDefeatFrame(player, "", TimeUnit.SECONDS.toMillis(5));

            if (arena.getTeamUsers(team).isEmpty()) {
                arena.send("",
                        "§f§lTIME ELIMINADO §f» §cO " + team.getColor() + "Time " + team.getName() + "§c foi eliminado.",
                        "");

                arena.getUsers().forEach(target -> CompletableFuture.runAsync(() -> arena.updateSidebarTeams(target)));
            }

            // Verificar se resta apenas 1 time vivo (ou 1 jogador em solo)
            if (arena.getAliveTeams().size() == 1) {
                arena.setWinner(arena.getAliveTeams().get(0));
                arena.setPhase(RoomPhase.ENDING);
                return;
            }

            user.setTag(Tag.SPECTATOR);
            TagManager.updateTag(user.getAccount());

            handleHotbar(user);
        }

        player.playSound(Sound.BLAZE_DEATH);

        sendDeathMessage(arena, user, killer, cause);

        /* Alterar Visibilidade */
        arena.hideAndShow(user);
        
        // Atualizar visibilidade para todos os jogadores da arena
        arena.getUsers().forEach(target -> {
            if (!target.equals(user)) {
                arena.hideAndShow(target);
            }
        });
    }

    /* Death Messages */
    private final List<String> voidByOtherMessages = Arrays.asList(
            "foi jogado no void por", "foi arremessado direto pro void por"
    ), voidMessages = Arrays.asList(
            "caiu no void", "sucumbiu no vazio", "tropeçou e caiu no void", "tentou nadar no limbo"
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
                voidByOtherMessage = voidByOtherMessages.get(Savage.RANDOM.nextInt(voidByOtherMessages.size()));

        arena.send(user.getTeam().getColor() + account.getNickname() + " §7" +
                (!cause.equals(DeathCause.VOID) ? (hasKiller ? normalMessage + " " + killer.getTeam().getColor() + killer.getAccount().getNickname() : "morreu")
                        : (hasKiller ? voidByOtherMessage + " " + killer.getTeam().getColor() + killer.getAccount().getNickname() : voidMessage))
                + "§7." + (hasKiller && finalKill ? " §f§lKILL FINAL!" : "")
        );
    }

    public enum DeathCause {
        PLAYER, VOID, NONE
    }

    public void updateStatisticalData(Team winner, List<Team> losers) {
        if (winner != null) {
            for (UUID id : winner.getMembers()) {
                User user = (User) User.of(id);

                if (user == null) continue;

                BedMember member = user.getMember();

                Arena arena = user.getArena();

                BedStats stats = member.getStats(arena.getArcade().getCategory(), arena.getType());

                int gainXp = Savage.RANDOM.ints(40, 140).findFirst().orElse(35);

                System.out.println("[Arcade] Jogador " + user.getPlayer().getName() + " ganhou! Wins antes: " + stats.getWins());
                stats.setWins();
                stats.setWinStreak();
                System.out.println("[Arcade] Jogador " + user.getPlayer().getName() + " - Wins depois: " + stats.getWins() + ", WinStreak: " + stats.getWinStreak());

                if (arena.isType(Type.COMPETITIVE))
                    member.addEloXp(gainXp);
                else
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

                    BedMember member = user.getMember();

                    Arena arena = user.getArena();

                    int lostXp = Savage.RANDOM.ints(30, 45).findFirst().orElse(25);

                    handleDefeatStats(user);

                    if (arena.isType(Type.COMPETITIVE))
                        member.removeEloXp(lostXp);
                    else
                        member.removeLevelXp(lostXp);

                }
            }
        }
    }

    public void handleDefeatStats(User user) {
        BedMember member = user.getMember();

        Arena arena = user.getArena();

        BedStats stats = member.getStats(arena.getArcade().getCategory(), arena.getType());

        int lostXp = Savage.RANDOM.ints(30, 45).findFirst().orElse(25);

        stats.setDefeats();
        stats.setWinStreak(0);

        member.updateStats(stats);
    }

    public void handleEntities(Arena arena) {
        for (Team team : arena.getTeamList()) {
            Location shopNpcLocation = arena.getLocation("npc_shop_" + team.getTypeName().toLowerCase());

            if (shopNpcLocation != null) {
                HologramServer hologram = manager.getHologram().spawnServer("npc_shop_" + team.getTypeName().toLowerCase(), shopNpcLocation);

                hologram.setText(Arrays.asList(
                        team.getColor() + "§lLOJA",
                        "§7Clique para comprar!"
                ));
            }

            Location upgradeNpcLocation = arena.getLocation("npc_upgrade_" + team.getTypeName().toLowerCase());

            if (upgradeNpcLocation != null) {
                HologramServer hologram = manager.getHologram().spawnServer("npc_upgrade_" + team.getTypeName().toLowerCase(), upgradeNpcLocation);

                hologram.setText(Arrays.asList(
                        team.getColor() + "§lMELHORIAS",
                        "§7Clique para ver!"
                ));
            }
            
            // Criar apenas um holograma por ilha (no primeiro baú encontrado)
            List<Location> chestLocations = findChestsInIsland(team);
            
            if (!chestLocations.isEmpty()) {
                Location firstChestLocation = chestLocations.get(0);
                HologramServer chestHologram = manager.getHologram().spawnServer(
                        "chest_hologram_" + team.getTypeName().toLowerCase(), 
                        firstChestLocation.clone().add(0.5, 1.2, 0.5));
                
                chestHologram.setText(Arrays.asList(
                        "§eBata aqui",
                        "§ePara depositar"
                ));
            }
        }
    }
    
    private List<Location> findChestsInIsland(Team team) {
        List<Location> chests = new ArrayList<>();
        Location base = team.getBase();
        if (base == null) return chests;
        
        // Procurar baús em um raio de 15 blocos da base
        int radius = 15;
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location loc = base.clone().add(x, y, z);
                    org.bukkit.block.Block block = loc.getBlock();
                    
                    // Procurar tanto baú normal quanto ender chest
                    if (block.getType() == org.bukkit.Material.CHEST || 
                        block.getType() == org.bukkit.Material.ENDER_CHEST) {
                        // Verificar se está na ilha do time
                        if (team.inIsland(loc)) {
                            chests.add(loc);
                        }
                    }
                }
            }
        }
        
        return chests;
    }
}
