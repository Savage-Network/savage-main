package com.minecraft.savagemc.core.account.context;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.account.context.objects.auth.AuthMetadata;
import com.minecraft.savagemc.core.account.context.objects.block.Block;
import com.minecraft.savagemc.core.account.context.objects.clan.ClanMetadata;
import com.minecraft.savagemc.core.account.context.objects.cosmetic.CosmeticEntry;
import com.minecraft.savagemc.core.account.context.objects.fake.Fake;
import com.minecraft.savagemc.core.account.context.objects.friend.FriendMetadata;
import com.minecraft.savagemc.core.account.context.objects.clantag.ClanTag;
import com.minecraft.savagemc.core.account.context.objects.medal.Medal;
import com.minecraft.savagemc.core.account.context.objects.permission.Permission;
import com.minecraft.savagemc.core.account.context.objects.punishment.PunishmentHistory;
import com.minecraft.savagemc.core.account.context.objects.rank.info.RankInfo;
import com.minecraft.savagemc.core.account.context.objects.restrict.Restrict;
import com.minecraft.savagemc.core.account.context.objects.route.RouteContext;
import com.minecraft.savagemc.core.account.context.objects.skin.SkinMetadata;
import com.minecraft.savagemc.core.account.context.objects.tag.Tag;
import com.minecraft.savagemc.core.account.context.objects.toggle.Toggle;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class AccountContext {

    /* Data List */
    private RouteContext route = RouteContext.builder().build();

    private RankInfo rankInfo = new RankInfo();

    private Fake fake = new Fake();
    private SkinMetadata skin = new SkinMetadata();

    private FriendMetadata friend = new FriendMetadata();
    private Toggle toggle = new Toggle();

    private AuthMetadata auth = new AuthMetadata();
    private ClanMetadata clan = new ClanMetadata();

    private PunishmentHistory history = new PunishmentHistory(false);

    private Tag tag = Tag.MEMBER;
    private Medal medal = Medal.NONE;
    private ClanTag clanTag = ClanTag.NONE;

    private UUID lastMessage = Constant.DEFAULT_ID;
    private String partyIdentifier = "...";

    private int level = 1, golds = 0;
    private long lastLogin = System.currentTimeMillis();

    private String ipAddress = "127.0.0.1";
    private boolean premium = false, online = false;

    private final List<CosmeticEntry> collectibles = new ArrayList<>();
    private final List<String> activeCollectibles = new ArrayList<>();

    private final List<Permission> permissions = new ArrayList<>();
    private final List<Restrict> restricts = new ArrayList<>();

    private final List<Medal> medals = new ArrayList<>(Collections.singletonList(Medal.NONE));
    private final List<Block> blocks = new ArrayList<>();

    private final Map<String, Long> cooldown = new ConcurrentHashMap<>();

    /* Permanent Info */
    private final long createdAt = System.currentTimeMillis();
}
