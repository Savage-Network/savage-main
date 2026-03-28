package com.minecraft.savagemc.core.api.party;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.party.connection.PartyConnection;
import com.minecraft.savagemc.core.api.party.member.PartyMember;
import com.minecraft.savagemc.core.api.party.type.PartyType;
import com.minecraft.savagemc.core.arcade.route.ArcadeRouteContext;
import com.minecraft.savagemc.core.backend.database.redis.message.types.route.arcade.ArenaPartyWarpMessage;
import com.minecraft.savagemc.core.util.list.StringUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
@Setter
public class Party {

    private final String identifier;

    private UUID authorId;
    private PartyType type;

    private final List<PartyMember> members;

    private int slots;

    public Party(Account author, PartyType type, int slots) {
        this.identifier = StringUtil.generateLetterCode(5) + Savage.getPartyData().list().size() + 1;
        this.authorId = author.getId();

        this.type = type;
        this.slots = slots;

        this.members = new CopyOnWriteArrayList<>(Collections.singletonList(new PartyMember(authorId)));

        author.setParty(identifier);

        Savage.getPartyData().save(this);
    }

    public void save() {
        Savage.getPartyData().update(this);
    }

    public void disband() {
        for (Account member : getOnlineMembers()) {
            if (member == null) continue;

            member.resetParty();
        }

        /* Limpar Dados */
        Savage.getPartyData().clearData(this);

        send("§cA party acabou.");

        Savage.getPartyData().delete(this);
    }

    public void redirect(ArcadeRouteContext route) {
        if (route.getMaxPlayers() == 2) {
            PartyMember member = getMembers().get(Savage.RANDOM.nextInt(getMembersCount()));

            while (member.getId().equals(authorId))
                member = getMembers().get(Savage.RANDOM.nextInt(getMembersCount()));

            Account account = member.getAccount();

            if (account != null && account.isOnline()) {

                if (!account.inServer(route.getArcade().getServer())) {
                    account.sendGlobal("§aEnviando para " + route.getArenaIdentifier() + "...");

                    account.setArcadeRoute(route);

                    account.redirect(route);
                } else {
                    /* Caso já esteja no servidor, solicitar busca da arena e enviá-lo. */
                    new ArenaPartyWarpMessage(account, route).send();
                }
            }

        } else {
            for (PartyMember member : getMembers()) {
                if (member == null) continue;

                Account account = member.getAccount();

                if (account != null && account.isOnline()) {
                    if (account.getId().equals(authorId)) continue;

                    if (!account.inServer(route.getArcade().getServer())) {
                        account.sendGlobal("§aEnviando para " + route.getArenaIdentifier() + "...");

                        account.setArcadeRoute(route);

                        account.redirect(route);
                    } else {
                        /* Caso já esteja no servidor, solicitar busca da arena e enviá-lo. */
                        new ArenaPartyWarpMessage(account, route).send();
                    }
                }
            }
        }
    }

    /* --------------------------
         Métodos Utilitários
    ---------------------------- */
    public void transfer(Account account) {
        this.authorId = account.getId();

        send(account.getColoredName() + "§e é o novo dono da party.");
        save();
    }

    public boolean isAuthor(UUID uuid) {
        return this.authorId.equals(uuid);
    }

    public Account getAuthor() {
        return Savage.getAccountController().of(this.authorId);
    }

    public void send(String message) {
        getOnlineMembers().forEach(member -> member.send(Constant.PARTY_CHAT_PREFIX + "§7" + message));
    }

    public boolean isType(PartyType type) {
        return this.type.equals(type);
    }

    public void setType(PartyType type) {
        this.type = type;
        save();
    }

    public void setSlots(int slots) {
        this.slots = slots;
        save();
    }

    public boolean isFull() {
        return members.size() >= slots;
    }

    /* --------------------------
           Gerenciamento de Membros
        ---------------------------- */
    public List<Account> getOnlineMembers() {
        return Savage.getAccountController().filter(account -> account.isOnline() && isMember(account.getId()));
    }

    public List<UUID> getMembersId() {
        return this.members.stream().map(PartyMember::getId).collect(Collectors.toList());
    }

    public int getMembersCount() {
        return this.members.size();
    }

    public boolean isMember(UUID uuid) {
        return this.members.stream().anyMatch(member -> member.getId().equals(uuid));
    }

    public PartyMember getMember(UUID uuid) {
        return getMembers().stream().filter(member -> member.getId().equals(uuid)).findFirst().orElse(null);
    }

    public void disconnect(Account account) {
        PartyMember member = getMember(account.getId());

        account.resetParty();

        /* Atualizando membro */
        member.setExpiresAt(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(3));

        int indexOf = this.members.indexOf(member);
        this.members.set(indexOf, member);

        send(account.getColoredName() + "§c desconectou-se e será removido em 3 minutos.");
        save();

        Savage.getPartyData().disconnect(new PartyConnection(identifier, account.getId()));
    }

    public void reconnect(Account account) {
        PartyMember member = getMember(account.getId());

        if (isFull())
            setSlots(slots + 1);

        account.setParty(identifier);

        /* Atualizando Membro */
        member.setExpiresAt(-1L);

        int indexOf = this.members.indexOf(member);
        this.members.set(indexOf, member);

        send(account.getColoredName() + "§a reconectou.");
        save();
    }

    public void add(Account member) {
        if (isMember(member.getId())) return;

        send(member.getColoredName() + "§a entrou na party.");

        this.members.add(new PartyMember(member.getId()));
        member.setParty(identifier);

        save();
    }

    public void kick(Account member) {
        if (!isMember(member.getId())) return;

        send(member.getColoredName() + "§c foi expulso da party.");

        this.members.removeIf(partyMember -> partyMember.getId().equals(member.getId()));
        member.resetParty();

        save();
    }

    public void remove(Account member) {
        if (!isMember(member.getId())) return;

        send(member.getColoredName() + "§c saiu da party.");

        this.members.removeIf(partyMember -> partyMember.getId().equals(member.getId()));
        member.resetParty();

        save();
    }
}
