package com.minecraft.savagemc.hungergames.game.list.events;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.bukkit.event.player.PlayerTeamAssignEvent;
import com.minecraft.savagemc.core.bukkit.event.player.PlayerUpdateTablistEvent;
import com.minecraft.savagemc.core.bukkit.util.variable.object.Variable;
import com.minecraft.savagemc.core.server.type.ServerType;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.game.list.Event;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.util.game.Loadable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

@Loadable
public class ClanxClan extends Event {

    public ClanxClan(HungerGames hungerGames) {
        super(hungerGames);
        setName("Clan x Clan");

        getVariables().setFinalArenaSpawn(true);
        getVariables().setInvincibility(300);
        getVariables().setFinalArena(1800);
        getVariables().setMostEndGameMechanic(false);
        getVariables().setMinimumPlayers(4); // Mínimo 2v2
        setArenaDisableKits(false);
        
        // Definir server type para clanxclan
        Savage.getServerData().setServerType(ServerType.HUNGERGAMES_CLANXCLAN);

        try {
            getVariable("hg.respawn").setValue(false);
            getVariable("hg.late_join").setValue(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        super.start();
        updateClanTablist();
    }

    public void updateClanTablist() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            Account account = Savage.getAccountController().of(player.getUniqueId());
            if (account != null) {
                PlayerUpdateTablistEvent event = new PlayerUpdateTablistEvent(account, account.getTag(), account.getTagPrefix());
                Bukkit.getPluginManager().callEvent(event);
            }
        });
    }

    @Override
    public void load() {
        super.load();
        // Ativar kits para clan battles
        getKits(' ', "Achilles Timelord Stomper Demoman Endermage AntiTower Launcher Phantom Switcher Fisherman Cultivator Camel Tank Blink Rider Berserker Checkpoint Chameleon Boxer Monk Digger Gladiator").forEach(kit -> kit.setActive(true, false));
        
        // Verificar se jogadores têm clan para participar
        Bukkit.getOnlinePlayers().forEach(player -> {
            Account account = Savage.getAccountController().of(player.getUniqueId());
            if (account != null && !account.hasClan()) {
                player.sendMessage("§cVocê precisa estar em um clan para participar de Clan x Clan!");
            }
        });
    }

    @Variable(name = "hg.clanxclan.required_clan")
    public boolean requireClan = true;

    @Variable(name = "hg.clanxclan.max_players_per_clan")
    public int maxPlayersPerClan = 4;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        // Verificar se jogador tem clan
        Player player = event.getPlayer();
        Account account = Savage.getAccountController().of(player.getUniqueId());
        
        if (account != null && requireClan && !account.hasClan()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§cVocê precisa estar em um clan para entrar no servidor Clan x Clan!");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateClanTablist();
    }

    @Override
    public void die(User user) {
        super.die(user);

        // Jogadores que morrem são enviados ao lobby
        if (!user.isAlive() && user.isOnline()) {
            ServerType lobbyServer = ServerType.HUB_HUNGERGAMES;
            
            if (user.getAccount() != null) {
                user.getAccount().redirect(lobbyServer);
            } else {
                user.getPlayer().kickPlayer("§cVocê morreu. Tente novamente na próxima!");
            }
        }
    }

    @EventHandler
    public void onPlayerUpdateTablist(PlayerTeamAssignEvent event) {
        User user = User.fetch(event.getViewer().getUniqueId());

        if (user != null && user.isAlive()) {
            Account account = user.getAccount();
            if (account != null && account.hasClan()) {
                // Mostrar tag do clan no tablist
                String clanTag = account.getClanTag();
                event.getTeam().setPrefix(clanTag + " ");
            }
        }
    }
}
