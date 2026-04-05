package com.minecraft.savagemc.core.api.clan;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.clan.member.ClanMember;
import com.minecraft.savagemc.core.api.clan.metadata.ClanMetadata;
import com.minecraft.savagemc.core.api.clan.metadata.emblem.ClanEmblem;
import com.minecraft.savagemc.core.api.clan.role.ClanRole;
import com.minecraft.savagemc.core.backend.database.redis.message.types.account.AccountClanChangeMessage;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
public class Clan {

    private final UUID id;
    private UUID leader;

    private String name, tag;

    private ClanMetadata metadata;

    public Clan(Account leader, UUID id, String name, String tag) {
        this.id = id;

        this.name = name;
        this.tag = tag;

        this.leader = leader.getId();

        ClanMetadata metadata = new ClanMetadata();

        metadata.getMembers().put(leader.getId(), new ClanMember(leader, ClanRole.LEADER));

        this.metadata = metadata;
    }

    protected void save(String... fields) {
        for (String field : fields) {
            Savage.getClanData().update(this, field);
        }
    }

    public void disband() {
        Savage.getLogger().info("Deletando o clan " + name + " (" + tag + ")...");

        for (UUID id : metadata.getMembers().keySet()) {
            Account account = Savage.getAccountData().of(id);

            if (account == null) continue;

            account.setClan(Constant.DEFAULT_ID);

            new AccountClanChangeMessage(account).send();
        }

        send("§7O clan foi desfeito.");

        Savage.getClanData().delete(this);

        Savage.getLogger().info("O clan " + name + " (" + tag + ") foi deletado!");
    }

    public void send(String message) {
        getOnlineMembers().forEach(account -> account.send(Constant.CLAN_CHAT_PREFIX + message));
    }

    public void saveMetadata(ClanMetadata metadata) {
        this.metadata = metadata;
        save("context");
    }

    /* Clan Methods */
    public void setName(String name) {
        this.name = name;
        save("name");
    }

    public void setTag(String tag) {
        this.tag = tag;
        save("tag");
    }

    public boolean isLeader(UUID leader) {
        return this.leader.equals(leader);
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
        save("leader");
    }

    /* Member Methods */
    public Map<UUID, ClanMember> getMembers() {
        return metadata.getMembers();
    }

    public boolean isMember(UUID id) {
        return getMembers().containsKey(id);
    }

    public ClanMember getMember(UUID id) {
        return getMembers().get(id);
    }

    public List<Account> getOnlineMembers() {
        return Savage.getAccountData().filteredList(account -> account.isOnline() && isMember(account.getId()));
    }

    public void addMember(Account member) {
        if (isMember(member.getId())) return;

        send("§7" + member.getName() + " entrou.");

        member.setClan(id);

        getMembers().put(member.getId(), new ClanMember(member, ClanRole.MEMBER));
        saveMetadata(metadata);
    }

    public void removeMember(Account member) {
        if (!isMember(member.getId())) return;

        send("§7" + member.getName() + " saiu.");

        member.setClan(Constant.DEFAULT_ID);

        getMembers().remove(member.getId());
        saveMetadata(metadata);
    }

    public void updateMember(ClanMember member) {
        if (!isMember(member.getId())) return;

        getMembers().put(member.getId(), member);
        saveMetadata(metadata);
    }

    /* Emblems Methods */
    public Set<ClanEmblem> getEmblems() {
        return metadata.getEmblems();
    }

    public boolean hasEmblem(ClanEmblem emblem) {
        return getEmblems().contains(emblem);
    }

    public void setEmblem(ClanEmblem emblem) {
        getEmblems().add(emblem);
        saveMetadata(metadata);
    }

    /* Power Methods */
    public int getPower() {
        return metadata.getPower();
    }

    public void setPower(int power) {
        metadata.setPower(power);
        saveMetadata(metadata);
    }

    public void addPower(int power) {
        if (power > 0)
            setPower(getPower() + power);
    }
}
