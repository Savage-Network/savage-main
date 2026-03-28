package com.minecraft.savagemc.lobby.architect.list.aquatic;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.skin.Skin;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.savagemc.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.savagemc.core.bukkit.manager.list.TagManager;
import com.minecraft.savagemc.core.member.context.elo.Elo;
import com.minecraft.savagemc.core.member.list.aquatic.AquaticMember;
import com.minecraft.savagemc.core.server.type.ServerType;
import com.minecraft.savagemc.core.util.Util;
import com.minecraft.savagemc.core.util.list.DateUtil;
import com.minecraft.savagemc.lobby.architect.Architect;
import com.minecraft.savagemc.lobby.user.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AquaticArchitect extends Architect {

    public AquaticArchitect() {
        super(ServerType.HUB_AQUATIC);
    }

    @Override
    public void join(Player player) {
        UUID id = player.getUniqueId();

        AquaticMember member = Savage.getAquaticData().of(id, true);

        if (member == null)
            member = Savage.getAquaticData().save(new AquaticMember(id, player.getName()));

        Savage.getMemberController().save(member);
        Savage.getAquaticData().cancelExpiration(id);

        Location statsLocation = getLocation("npc_stats");

        if (statsLocation != null) {

            HologramClient details = BukkitCore.getManager().getHologram().spawnClient(player, "stats-details",
                    statsLocation.clone().add(0, 2.3, 0));

            int xp = member.getLevelXp(), minXp = member.getMinLevelXp();

            details.setText(Arrays.asList(
                    "Seu nível: " + member.getLevelId(),
                    "§f[" + Util.createProgressBar(ChatColor.GREEN, ChatColor.DARK_GRAY, Constant.SQUARE_SYMBOL, xp, minXp, 10) + "§f] " +
                            "§7(" + Util.formatNumberWithLetter(xp) + "/" + Util.formatNumberWithLetter(minXp) + ")",
                    "",
                    "Partidas jogadas: §a" + Util.formatNumber(member.getTotalMatches()),
                    "Vitórias: §a" + Util.formatNumber(member.getTotalWins()),
                    "",
                    "Kills: §a" + Util.formatNumber(member.getTotalKills()),
                    "Winstreak: §a" + Util.formatNumber(member.getTotalWinstreak())
            ));

            details.spawnTo(player);
        }

        super.join(player);
    }

    @Override
    public void quit(Player player) {
        super.quit(player);

        Savage.getAquaticData().startExpiration(player.getUniqueId());
    }

    @Override
    public void handleSidebar(User user) {
        AquaticMember member = user.getMember(AquaticMember.class);

        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.setTitle("WATER FIGHT");

        sidebar.addRow("date", "§7" + DateUtil.getCurrentDate() + " §8" + getId());
        sidebar.blankRow();

        sidebar.addRow("level", "Seu nível: " + member.getLevelId());
        sidebar.addRow("progress", " §8[" + Util.createProgressBar(
                ChatColor.GREEN, ChatColor.GRAY, Constant.SQUARE_SYMBOL, member.getLevelXp(), member.getMinLevelXp(), 10, false) + "§8]"
                + " §7(" + Util.formatNumberWithLetter(member.getLevelXp()) + "/" + Util.formatNumberWithLetter(member.getMinLevelXp()) + ")");
        sidebar.blankRow();

        sidebar.addRow("solo", "§eSolo:");
        sidebar.addRow("solo_wins", " Vitórias: §a" + Util.formatNumber(member.getTotalWins(ArcadeCategory.AQUATIC_SOLO)));
        sidebar.addRow("solo_winstreak", " Winstreak: §a" + Util.formatNumber(member.getTotalWinstreak(ArcadeCategory.AQUATIC_SOLO)));

        sidebar.addRow("duo", "§eDuplas:");
        sidebar.addRow("duo_wins", " §fVitórias: §a" + Util.formatNumber(member.getTotalWins(ArcadeCategory.AQUATIC_DUO)));
        sidebar.addRow("duo_winstreak", " §fWinstreak: §a" + Util.formatNumber(member.getTotalWinstreak(ArcadeCategory.AQUATIC_DUO)));

        sidebar.blankRow();
        sidebar.addRow("players", "Jogadores: §a" + Util.formatNumber(Savage.getServerData().getOnlinePlayers()));

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagManager.updateTag(user.getAccount());
    }

    @Override
    public void handleEntities() {
        List<Location> list = getLocationsByKey("npc_stats", "npc_solo", "npc_duo", "npc_flood", "npc_free", "npc_unknown");

        if (list.isEmpty()) return;

        handleStatsNpc(list.get(0));

        handleArcadeNpc(ArcadeCategory.AQUATIC_SOLO, list.get(1),
                "ewogICJ0aW1lc3RhbXAiIDogMTY1ODE4MTU1MjMyNywKICAicHJvZmlsZUlkIiA6ICI1MjhlYzVmMmEzZmM0MDA0YjYwY2IwOTA5Y2JiMjdjYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJQdWxpenppIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzQ4ODYxZDEzZjA0MzI2NDg0ZjZhMWFhZGVmMzQzYWJjYmNiM2ZkNzllZmU3NWYzZTQyYjFkNjI0NTc0MzllMGIiCiAgICB9CiAgfQp9",
                "YRlZSwQtXUVxtdFofPa8hnYT84u8HHjpTojbTV1cDjVEU5PwhwRSynK5CYO02RpIPK9oYQ6EaJ3fN9L6/753jeAeo1AofFfDbQmsR0pP8AioxEGNLEvpT9djvRaNvb0INYpZVOVqTcHS7EuhrUsKTKOukNSYG/WbhzoOrG89iAECC9/P37InFfNk2EpwJM0nbS7C2ngAsqDE9HKbmmFEMWGeVdfz5tmZ0qXDoIreE+pd95owSrITE+7FG839y0AH+STsZ/wdQ6mPKx56eaCwdXqHSUlkK4j6Z/xutgAowWkUwssnybiv71MgXkyoptBlhJZRf1iawMVyqHT+0Ienuhf5UZG0VEmWZx5s0zUcaeHjacEVCaj6kGt3tiW9JK3rK7bZlS8NxjM0r7Vxsp2kUnxgCyvwEBLS/ZJTRIgEcLioUjhVmWEU9CHUtW97un02WV7RQ+j1ByQlI2ngb+dmDhcVVHRsj7gVtgkXM9crL48AVwW5JpETz+JJ9G9SO1yywNYgKvzy8gZkDnoxKT6e7LQamb7YVOCwNWPxkLFV08Iibafed6MNolUEENq903mWabxeaCIM8NzhQU2VdpkHPA85BoMqW7CU7NA8cWqgCu7YrIkh05GTIFvqVNX42jDd8vkVydR1GH6+6g/1kRZt/EizOYk5ck1ZKQ1LiPNbsOg=");

        handleArcadeNpc(ArcadeCategory.AQUATIC_DUO, list.get(2),
                "eyJ0aW1lc3RhbXAiOjE1ODMwMTY5NzU2NjAsInByb2ZpbGVJZCI6IjUyOGUxZmNhZWZlNjQzMGRhMGNhNDBjNDczZWVhOGI4IiwicHJvZmlsZU5hbWUiOiJ3YXRlciIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjg3YWZhMDcxMmM0NjhiM2I2NWFlNzNmNjAzZjljM2RiMWMxMWVkYzc0MWY1MDkwODNlMGZjYmU3ZTNkYzg4ZiIsIm1ldGFkYXRhIjp7Im1vZGVsIjoic2xpbSJ9fX19",
                "Uva/noDdxRTb9pLQ5XRkBm0Qe+uz3FfVwOYS8zd9+NaYbRdT5eZxnfMkaZFJrppv7Adf3Y7/b7vmmn4hkeIudWn/Mdh8SvT3rlkopZDSnkxnsFS7btXKaqD4X/4PjloRaa2ppf4EyPa/jpHC6awxA8UCzgLyCOnNyq3KTqBcjpFZtzfrFn2FSzvCq7q5lAYlL3lBr/wQFUn2/QgoTRnPssussR2i/qa0uHMc0KePYH0D67wdYtZ3/ps3GFDyeUzzMAJfHBej4mXe01GbjDJIlaBTJKSwRKRdwFY5KBxunAdELds9FRMJXYxfcbOoPoXLwhfIXj1QrlXrJt1OAgZoJKj8aHZlKWk7n3/qpVK1fmbqlCZVuuJnCRKHI3tWQHX5Ygjr1xuH25bcuRnQhPc4ybsQFreqjtpxTxrozkWeXxIuvYLgaSEpxQ3hJcFPBcDW9/i/sxqNpO1dqOMgeHr6aOUCCNVWlU8x2yxLLmL6UCVckjmGANquGRIVAqIp5VX6I1cDsd6f4fxID+Rjj5ul18CaDvbUwGS2ItN3b9Xb+bmvDexS8DUbRXShjKGnK+uCpjzwTy5ADPDc+xA10pMFM9+Q0JeCVMuj1t/uZ6T5ricoGpfKoafYtpmlIHkFuTXsR6G0lYYrh/kLo0yeVf7wKZVKWs03mKYIbkAOrh84Thg=");

        handleArcadeNpc(ArcadeCategory.AQUATIC_FLOOD, list.get(3),
                "ewogICJ0aW1lc3RhbXAiIDogMTYwMDI4MTM3Mjk5NywKICAicHJvZmlsZUlkIiA6ICJjZGM5MzQ0NDAzODM0ZDdkYmRmOWUyMmVjZmM5MzBiZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJSYXdMb2JzdGVycyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xZGNmNjJlMTlhYzMxYTIyMjJmMzI4MTI1MzM4ODc5NGMzOTFjOTJhYzVkZDJkYTMwOTZlMjUyZTE3NTE0YWI4IgogICAgfQogIH0KfQ==",
                "lgIxG1fr0YlOmawQI9wP+gb1tTbuPxqn3tsEhdsgJNdracntrmnPBi3LZdPi+P97XtdmHhiGhY+V27RqKifFZkWcfv5jz8qRcqS19xTYr6nKQ0eRxT6JcdIizBp1xRK2xhY+rK7EisX02cAT9RY83RuD49Shl6Tw9C3wOXMqjumZc5PRdaHuZnhooHbGy1tX4tU4lowPDYJ7kfpfaldb9ldb2oiaTXJ2hZm2ltVy+2yRDnldmscDf6zf8h++rzE0Ge/Fv+cAzqBcVRV5PUH/y+xvvUVtb9r2j58ys4enqK55QRVwteJaFMvwMKn5FMay3rse6b4aAcZDVuBxkqHP2hHU+yzLYfp2RDwE3Jp6c0bV/cMNktVvzqYcAI7pDBgB+3gJYyLVBmKL4MzouRnnNDoJShSWb6dlLEEMzATVDFmil9n74mish3+vO9nub72O1/JwuPLsPwsd7v8acxxbHU75uezhLXgejtt5089na8W6TCKR+mwPrdn/rA74pyQtozOFTh/sxPpLCSqts0gd3epJGdUXuqWqweMRNXd+moMNplPMJ0/BMimGELxCenXoCGePjUiv/aP+B6GwsvLXknGZ992Ne1XoAn37085gXd2h/+tn8pdLt+mVNG2t23ebLzcmt46WNLWS40cAf2QTT4pFJ8KrIHg4TPs6iO4VzYY=");

        handleArcadeNpc(ArcadeCategory.AQUATIC_FREE, list.get(4),
                "ewogICJ0aW1lc3RhbXAiIDogMTYzNDE1NTk1MjkyNiwKICAicHJvZmlsZUlkIiA6ICIwYTUzMDU0MTM4YWI0YjIyOTVhMGNlZmJiMGU4MmFkYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJQX0hpc2lybyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iY2VlOGNiNmIxODBmYzFkOTdjNTgyOTY4MTM1NTM1OGFhNTlmOWZiYzNmZDY0NGJiZDAzM2YwODgxNWZlNjAiCiAgICB9CiAgfQp9",
                "eeeosCY6qeGWdapAL5g52gqcwlpZ89qwV3PFNWrukf1ECxf28ygtbL166RuwTGlQVy91tQ2PGMj+nrAB9s9ru4sJ/LMjMJVUW0UMaczXU0INZQl7zfK9EgEGjwQxhHZQ16Wq1fdF82Dl30jlHWLh3AMmG1liBkCFhCkk6b/jzK+ZNDCqQ08enkkbDMxs/TBQPvJ6lFjkRQZ8sW6ker2XGgQU54+SDQhTOy/Wfe5hUSJf2EIRV+tLRbA+6n9R3y0o1vGswyICSI8rv+eiZSNB5j7LfaMI9Om4nKjtWXGc3YXdiDiA+VK00zqvfsEAUgmkXwW63hMJnbM2fXrdrheBTWFIDH/CsJ5jbWZHLHqmwyKigHvHayShhjCZ6Wz53Qh0q9tehOMlpguxm4ILh9Fg1KRwDgb2O7gnWw4owOp4WjU0fTno6cQSz6FLn2rGdJ1dU/XkCt9EyXCK7iVmQ3wUOSBKEfeTwiEiQRcNcHB6UkpRqZZESvLNSnt/7w9dhLqZmTeVZy5p8aCnrv4IgFt8pvE8vlYMPsa20XhOP42RPsfR8eXUMnf47Ri94/FGKLL2sYD88pMNzTc7NaVrR67Hj5l/etByxeWPhFMN3OSOFx+3Rh1dXBEtb6hEo0t9KpXIjC2Teb6IjVHKO319NCKkX5E+5rBNGgpsqm7s4v7UUT0=");

        handleArcadeNpc(ArcadeCategory.NONE, list.get(5), (player, action) -> {
                },
                Skin.unknown().getValue(),
                Skin.unknown().getSignature());
    }

    @Override
    public void chat(Player player, String message) {
        User user = (User) User.of(player.getUniqueId());

        Account account = user.getAccount();

        AquaticMember member = user.getMember(AquaticMember.class);

        if (member == null) {
            player.sendMessage("§cNão foi possível recolher os seus dados. Contate um administrador!");
            return;
        }

        Elo elo = member.getElo();

        // Enviar apenas para jogadores no mesmo servidor (lobby aquatic)
        player.getServer().getOnlinePlayers().forEach(targetPlayer -> {
            Account targetAccount = Savage.getAccountController().of(targetPlayer.getUniqueId());
            if (targetAccount != null) {
                TextComponent text = new TextComponent(elo.getChat()
                        + " " + account.getTag().getByPrefix(targetAccount.getTagPrefix()) + account.getNickname() + ": ");

                text.addExtra(account.isAllowColoredChat() ? "§f" + Util.color(message) : "§7" + message);

                targetAccount.send(text);
            }
        });
    }

    @Override
    public Vector getSlimeJump(Vector direction) {
        return direction.multiply(1.1).setY(0.2);
    }
}
