package com.minecraft.arcade.eventos.managers;

import com.minecraft.arcade.eventos.Eventos;
import com.minecraft.arcade.eventos.settings.EventEmbeddedConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia grupos (times) no evento. Grupos são formados por /grupo <nick>:
 * quem convida é o líder; o jogador convidado entra no grupo do líder.
 * O "clan" usado no EventManager é o líder do grupo (UUID em string).
 */
public class ClanEventManager {

    private final Eventos plugin;
    /** Líder do grupo -> membros (incluindo o próprio líder) */
    private final Map<UUID, Set<UUID>> leaderToMembers = new ConcurrentHashMap<>();
    /** Jogador -> líder do seu grupo */
    private final Map<UUID, UUID> playerToLeader = new ConcurrentHashMap<>();

    public ClanEventManager(Eventos plugin) {
        this.plugin = plugin;
    }

    public boolean isEventosClansAtivos() {
        return plugin.getVariablesManager().getBoolean("event.eventos_clans", EventEmbeddedConfig.EVENTOS_CLANS);
    }

    public int getMaxJogadoresPorClan() {
        return plugin.getVariablesManager().getInt("event.max_por_clan", EventEmbeddedConfig.MAX_JOGADORES_POR_CLAN);
    }

    /**
     * Adiciona o jogador 'toAdd' ao grupo de quem convida ('inviter').
     * Se inviter não tem grupo, inviter vira líder e forma um novo grupo.
     */
    public boolean addToGroup(Player inviter, Player toAdd) {
        if (!isEventosClansAtivos()) return false;
        int max = getMaxJogadoresPorClan();
        UUID inviterId = inviter.getUniqueId();
        UUID toAddId = toAdd.getUniqueId();

        UUID leader = playerToLeader.get(inviterId);
        if (leader == null) {
            leader = inviterId;
            Set<UUID> set = ConcurrentHashMap.newKeySet();
            set.add(leader);
            leaderToMembers.put(leader, set);
            playerToLeader.put(leader, leader);
        }

        Set<UUID> members = leaderToMembers.get(leader);
        if (members.size() >= max && !members.contains(toAddId)) {
            inviter.sendMessage("§cSeu grupo já tem o máximo de " + max + " jogadores.");
            return false;
        }

        // Remove toAdd do grupo anterior, se tiver
        UUID oldLeader = playerToLeader.remove(toAddId);
        if (oldLeader != null && !oldLeader.equals(leader)) {
            Set<UUID> oldSet = leaderToMembers.get(oldLeader);
            if (oldSet != null) {
                oldSet.remove(toAddId);
                if (oldSet.isEmpty()) {
                    leaderToMembers.remove(oldLeader);
                }
            }
        }

        members.add(toAddId);
        playerToLeader.put(toAddId, leader);
        return true;
    }

    /**
     * Remove o jogador do grupo (sair do grupo).
     */
    public void leaveGroup(Player player) {
        UUID uuid = player.getUniqueId();
        UUID leader = playerToLeader.remove(uuid);
        if (leader != null) {
            Set<UUID> members = leaderToMembers.get(leader);
            if (members != null) {
                members.remove(uuid);
                if (members.isEmpty()) {
                    leaderToMembers.remove(leader);
                } else if (leader.equals(uuid)) {
                    // Líder saiu: dissolve o grupo (outros perdem o grupo)
                    for (UUID m : members) {
                        playerToLeader.remove(m);
                    }
                    leaderToMembers.remove(leader);
                }
            }
        }
    }

    /** Retorna o ID do time (líder em string) para o EventManager. */
    public String getClan(UUID uuid) {
        UUID leader = playerToLeader.get(uuid);
        return leader != null ? leader.toString() : null;
    }

    public Set<UUID> getMembrosClan(String teamId) {
        if (teamId == null) return Collections.emptySet();
        try {
            UUID leader = UUID.fromString(teamId);
            Set<UUID> set = leaderToMembers.get(leader);
            return set != null ? Collections.unmodifiableSet(set) : Collections.emptySet();
        } catch (IllegalArgumentException e) {
            return Collections.emptySet();
        }
    }

    public Collection<String> getClansNoEvento() {
        Set<String> ids = new HashSet<>();
        for (UUID leader : leaderToMembers.keySet()) {
            ids.add(leader.toString());
        }
        return ids;
    }

    public void sairDoEvento(UUID uuid) {
        // Não remove do grupo ao sair do evento; grupo persiste até /grupo sair
    }

    public void limpar() {
        leaderToMembers.clear();
        playerToLeader.clear();
    }

    /** Nomes dos membros do grupo do jogador (para mensagens). */
    public List<String> getNomesDoGrupo(Player player) {
        String teamId = getClan(player.getUniqueId());
        if (teamId == null) return Collections.singletonList(player.getName());
        Set<UUID> membros = getMembrosClan(teamId);
        List<String> nomes = new ArrayList<>();
        for (UUID u : membros) {
            Player p = Bukkit.getPlayer(u);
            nomes.add(p != null ? p.getName() : Bukkit.getOfflinePlayer(u).getName());
        }
        return nomes;
    }
}
