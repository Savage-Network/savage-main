package com.minecraft.savagemc.core.bungee.listener.api;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.party.Party;
import com.minecraft.savagemc.core.api.party.connection.PartyConnection;
import com.minecraft.savagemc.core.api.party.member.PartyMember;
import com.minecraft.savagemc.core.backend.data.list.api.PartyData;
import com.minecraft.savagemc.core.bungee.event.list.update.UpdateEvent;
import com.minecraft.savagemc.core.controller.list.AccountController;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PartyListener implements Listener {

    private final AccountController accountController = Savage.getAccountController();
    private final PartyData partyData = Savage.getPartyData();

    @EventHandler
    public void onPartiesConnections(UpdateEvent event) {
        CompletableFuture.runAsync(() -> {
            for (Party party : partyData.list()) {
                if (party == null) continue;

                Iterator<PartyMember> iterator = party.getMembers().iterator();

                while (iterator.hasNext()) {
                    PartyMember member = iterator.next();

                    if (member.hasExpired()) {
                        Account account = member.getAccount();

                        boolean changedOwner = false;

                        iterator.remove();
                        party.save();

                        if (partyData.hasConnection(member.getId()))
                            partyData.cancelConnection(member.getId());

                        account.resetParty();

                        party.send(account.getColoredName() + "§c foi removido da party.");

                        if (party.isAuthor(member.getId())) {
                            List<Account> onlineMembers = party.getOnlineMembers();

                            if (!onlineMembers.isEmpty()) {
                                changedOwner = true;

                                party.setAuthorId(onlineMembers.get(0).getId());
                                party.save();
                            }
                        }

                        if (changedOwner) {
                            Account author = party.getAuthor();

                            party.send(author.getColoredName() + "§e é o novo dono da party.");

                            Savage.getLogger().info(author.getNickname() + " virou dono de uma party.");
                        }
                    }
                }
            }
        });
    }

    @EventHandler
    public void onReconnect(PostLoginEvent event) {
        Account account = accountController.of(event.getPlayer().getUniqueId());

        if (account != null && partyData.hasConnection(account.getId())) {
            PartyConnection connection = partyData.getConnection(account.getId());

            Party party = partyData.of(connection.getPartyIdentifier());

            partyData.cancelConnection(account.getId());

            if (party != null) {
                party.reconnect(account);

                Savage.getLogger().info(account.getNickname() + " reconectou-se na party de " + party.getAuthor().getNickname() + ".");
            } else
                account.send("§cA sua party antiga não foi encontrada.");
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();

        Account account = accountController.of(player.getUniqueId());

        if (account != null && account.hasParty()) {
            Party party = account.getParty();

            if (party == null) {
                Savage.getLogger().warning("Não foi possível encontrar a party de " + account.getNickname() + ".");
                return;
            }

            if (party.getMembersCount() == 1) {
                party.disband();

                Savage.getLogger().info("A party de " + account.getNickname() + " foi desfeita.");
            } else {
                party.disconnect(account);

                Savage.getLogger().info(account.getNickname() + " desconectou-se da party de " + party.getAuthor().getNickname() + ".");
            }
        }
    }
}
