package com.minecraft.savagemc.core;

import com.minecraft.savagemc.core.api.mojang.MojangApi;
import com.minecraft.savagemc.core.backend.data.list.api.*;
import com.minecraft.savagemc.core.backend.data.list.server.RouteData;
import com.minecraft.savagemc.core.backend.data.list.server.SeasonData;
import com.minecraft.savagemc.core.backend.data.list.server.ServerData;
import com.minecraft.savagemc.core.backend.data.list.user.AccountData;
import com.minecraft.savagemc.core.backend.data.list.user.arcade.list.AquaticData;
import com.minecraft.savagemc.core.backend.data.list.user.arcade.list.BedWarsData;
import com.minecraft.savagemc.core.backend.data.list.user.arcade.list.DuelsData;
import com.minecraft.savagemc.core.backend.data.list.user.arcade.list.PvPData;
import com.minecraft.savagemc.core.backend.database.postgres.PostgresDatabase;
import com.minecraft.savagemc.core.backend.database.DatabaseCredentials;
import com.minecraft.savagemc.core.backend.database.redis.RedisDatabase;
import com.minecraft.savagemc.core.controller.list.AccountController;
import com.minecraft.savagemc.core.controller.list.CollectibleController;
import com.minecraft.savagemc.core.controller.list.MemberController;
import com.minecraft.savagemc.core.server.Server;
import com.minecraft.savagemc.core.server.type.ServerType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.minecraft.savagemc.core.account.context.objects.rank.Rank;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.util.list.gson.RankAdapter;
import com.minecraft.savagemc.core.util.list.gson.RankTypeAdapter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Logger;

public class Savage {

    @Getter
    @Setter
    private static Platform platform;

    @Getter
    @Setter
    private static Logger logger;

    @Getter
    @Setter
    private static JavaPlugin javaPlugin;

    /* Database */
    @Getter
    @Setter
    private static PostgresDatabase postgres;

    @Getter
    @Setter
    private static RedisDatabase redis;

    @Getter
    @Setter
    private static ServerData serverData;

    @Getter
    @Setter
    private static SeasonData seasonData;

    @Getter
    @Setter
    private static ReportData reportData;

    @Getter
    @Setter
    private static AccountData accountData;

    @Getter
    @Setter
    private static RouteData routeData;

    @Getter
    @Setter
    private static ClanData clanData;

    @Getter
    @Setter
    private static ArcadeData arcadeData;

    @Getter
    @Setter
    private static DuelsData duelsData;

    @Getter
    @Setter
    private static PvPData pvpData;

    @Getter
    @Setter
    private static BedWarsData bedWarsData;

    @Getter
    @Setter
    private static AquaticData aquaticData;

    @Getter
    @Setter
    private static PartyData partyData;

    @Getter
    @Setter
    private static PunishmentData punishmentData;

    @Getter
    @Setter
    private static RedirectData redirectData;

    @Getter
    @Setter
    private static ReconnectData reconnectData;

    @Getter
    @Setter
    private static SkinCacheData skinCacheData;

    @Getter
    @Setter
    private static ServerType serverType = ServerType.BUNGEE;

    @Getter
    @Setter
    private static int serverId = 0, serverPort = 25565;

    /* Controllers */
    @Getter
    private final static AccountController accountController = new AccountController();

    @Getter
    private final static MemberController memberController = new MemberController();

    @Getter
    private final static CollectibleController collectibleController = new CollectibleController();

    /* Util Variables */
    public static Gson GSON = new GsonBuilder()
            .registerTypeAdapter(RankType.class, new RankTypeAdapter())
            .registerTypeAdapter(Rank.class, new RankAdapter())
            .create();
    public static JsonParser PARSER = new JsonParser();

    public static Random RANDOM = new Random();

    public static MojangApi MOJANG_API = new MojangApi();

    public static Server getLocalServer() {
        return serverData.local();
    }

    public static boolean isUntouchable(UUID id) {
        return Constant.DEVELOPERS_IDS.contains(id);
    }

    public static void load(Platform platform, ServerData serverData, Logger logger) {
        logger.info("Iniciando Core...");

        /* Configure Server */
        setPlatform(platform);
        setLogger(logger);

        // ServerData é setado imediatamente para evitar NPE no onEnable
        setServerData(serverData);

        TimeZone.setDefault(TimeZone.getTimeZone("GMT-3"));

        /* Configure Database */
        PostgresDatabase postgres = new PostgresDatabase(
                DatabaseCredentials.builder()
                        .host("198.1.195.80")
                        .port(5432)
                        .database("savagemc")
                        .user("savagemc")
                        .password("savagemcnaochorax")
                        .build());

        postgres.load();

        RedisDatabase redis = new RedisDatabase(
                DatabaseCredentials.builder()
                        .host("198.1.195.80")
                        .port(6379)
                        .build());
        redis.load();

        setPostgres(postgres);
        setRedis(redis);

        /* Datas */
        setSeasonData(new SeasonData(postgres));

        setRouteData(new RouteData(redis));
        setArcadeData(new ArcadeData(redis));
        setReportData(new ReportData(redis));
        setClanData(new ClanData(postgres, redis));
        setPartyData(new PartyData(redis));
        setPunishmentData(new PunishmentData(postgres));
        setRedirectData(new RedirectData(redis));
        setReconnectData(new ReconnectData(redis));
        setSkinCacheData(new SkinCacheData(redis));

        setAccountData(new AccountData(postgres, redis));

        /* Arcade Datas */
        setDuelsData(new DuelsData(postgres, redis));
        setPvpData(new PvPData(postgres, redis));
        setBedWarsData(new BedWarsData(postgres, redis));
        setAquaticData(new AquaticData(postgres, redis));

        logger.info("Core iniciado com sucesso.");
    }

    public static void unload() {
        getAccountController().getCache().clear();
        getMemberController().getCache().clear();
        getCollectibleController().getCache().clear();
    }

    public static void scan(String title, Instant started) {
        logger.info("[Scan/" + title + "] Examined with " + Duration.between(started, Instant.now()).toMillis() + " ms!");
    }

    public static void message(String... texts) {
        for (String text : texts)
            logger.info(text);
    }
}
