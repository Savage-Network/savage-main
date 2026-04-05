package com.minecraft.savagemc.lobby.architect.list.main;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.savagemc.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.savagemc.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.savagemc.core.bukkit.event.type.update.type.list.AsyncUpdateEvent;
import com.minecraft.savagemc.core.bukkit.manager.list.TagManager;
import com.minecraft.savagemc.core.server.type.ServerType;
import com.minecraft.savagemc.core.util.Util;
import com.minecraft.savagemc.core.util.list.DateUtil;
import com.minecraft.savagemc.lobby.architect.Architect;
import com.minecraft.savagemc.lobby.user.User;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import java.util.List;

public class MainArchitect extends Architect {

    public MainArchitect() {
        super(ServerType.HUB);
    }

    @Override
    public void handleSidebar(User user) {
        Account account = user.getAccount();

        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.addRow("date", "§7" + DateUtil.getCurrentDate() + " §8" + getId());

        sidebar.blankRow();
        sidebar.addRow("rank", "Rank: " + account.getRank().getColoredName());
        sidebar.blankRow();

        sidebar.addRow("clan", "Clan: §7" + (account.hasClan() ? account.getClanName() : "Nenhum"));
        sidebar.blankRow();

        sidebar.addRow("lobby", "Sala: §7#" + Savage.getServerId());
        sidebar.addRow("players", "Jogadores: §a" + Util.formatNumber(Savage.getServerData().getOnlinePlayers()));

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagManager.updateTag(account);
        
        // Forçar atualização adicional após 5 ticks para garantir que o jogador veja seu próprio nick
        org.bukkit.Bukkit.getScheduler().runTaskLater(
            com.minecraft.savagemc.core.bukkit.BukkitCore.getInstance(), 
            () -> {
                if (account.player() != null && account.player().isOnline()) {
                    TagManager.updateTag(account);
                }
            }, 
            5L
        );
    }

    @Override
    public void handleEntities() {
        List<Location> locations = getLocationsByKey("npc_bw", "npc_aquatic", "npc_duels", "npc_pvp");

        if (locations.isEmpty()) return;

        handleServerNpc(ServerType.HUB_BEDWARS, locations.get(0),
                "ewogICJ0aW1lc3RhbXAiIDogMTYxNDE5NDQ0ODIzNiwKICAicHJvZmlsZUlkIiA6ICJiNWRkZTVmODJlYjM0OTkzYmMwN2Q0MGFiNWY2ODYyMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJsdXhlbWFuIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzE2ZjdiZTcxNTFhZjFlY2ZjY2I5ZmU0YjNiYjI3NDIyM2FkMjhjNDU3ZTM0OWY2YWJkZmE4MDkxNDAwNjA2ZmUiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                "RwLKVCV46AczgaMsIc/2yrLXGoKQdp/k1ESvfl4Ri92UpP04jatqY6KiNGIDbJjFCJmoU1h0Bj127iHEpGHb1mwf3h/+/7hSLiKN0CABi8J9aMDsFcsIwA4YaP0jsdlflHx7vzZmOA87aIN75rVRaVA4qWCe7mHevqHpVcIz3R3p1Uh+A2CAZ7SkSE8+HACUUek4R3T4magGYIlH0g3mlw96/BWCVbkViXxVJYoSmovwX0BYHfIRtjZILc2OPB+I3bpCOQ+WB7YbF8KloD4eyFJYC7dg+n/X9oCDXSKPD6yofQlvavvoyH1QSEtmidJQYDrJWR4KtslpTKCaJibQ3ywV969tpanYxl0QXfTleIVYaALwHkRZ11DUY78laOJP+q/PJ9/XGAaYL01J/e2DBUbaHxlqsn0vYka0H8T9MJ5YcCGFv6q6p1QFRY7gCTifcsZ+vfMmTHAitPAabBSuOcA1FLwBQIzJTYjsnT1etY+TYTPmJxOC3f1dqwjMttF9H6pIsBBUdWIzpsmdh1xw/iemBRYjwtWoIR9v2SG4QruHgqHKIPBZnmk1aG5ymmIjD4NNH8EqXPF8cg3US8IbM7jbQ+8AO+TgrryZYSEwJqDNW6x7eTMVWM3WcHXnEoAjJ6B2ScHGB0glQDpE4yhn1TxDwY+DiL1Hgx4U04JXX0I=");

        handleServerNpc(ServerType.HUB_AQUATIC, locations.get(1),
                "ewogICJ0aW1lc3RhbXAiIDogMTcyNjM3NzQ5NDc2OSwKICAicHJvZmlsZUlkIiA6ICJjMTJkMmY5ZWJhZGI0ZTllYTIxZmM2M2M3YWY3M2E5NSIsCiAgInByb2ZpbGVOYW1lIiA6ICJEcmVhbXlOZW9uIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RkNTU0OWYzNTczYWM2ZTRjYWQ0MDdjNTRhZWZjMjZkOTAwOTMzZmQ3MmQ5NGIxOTA4NTkyMzg5YTM0Yzg1MzEiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                "WCKPDLGRBjI6Mf0OlbDAjwbaxKlEiG9tZSGc5sVvYFfBlTsEYU6Ehdu9wbeNUX6h2T6Y/v8f98r5GSKuLhNTsSM2M2Ldx5NiGkmZrIWQKCE4fAUR24ozfojFStSuRlnpH0oz2FGzzNI9Tw39EXKBViEK79RqB83dhu/FIWq912AcGL4Y0mddC/HWPKbqsK5vfbur2C4/7FX/WNFmsMY6bOPT0o1+2BSJlDlDpGYffWFT7iUV8K9kJ6Alz4XPN0Cu5BMgQ7s94xa8JbxQ1BO4u77fANVKjXDRUwlp3GSuoS+InzicQeLselIq/Gw80L6YdkiHV5IWjaLVs3egMGK8Qdpp+H9Y6xqtyOknJfV71meshgc6nqGHb30ctqQOdaFZc5HNFGf08pKhcY4ZLZdv2CSzQ5kqNzv7CMh+iySrD1LCEk8xQvWQtgtS8vJ7uGTsrLsHbHCGnqF8N1IFyp8HJxgWxX4zuUj8bsHShtMK5mcXRolS39TbuL1ni8kWGOMajGHXlYsZNphgb6EDxGIt4gZhnrPOXVsnJ+Uu0JuynZ/Ak50qctealEEXFJVQZ6XLHVZzlqBjaToGfk7yHCYlQXYwrtQ6Ew/yHDM9JGoeZvqSMuAD96qd3NKXeMAQmcjH4PkpK8wzOsahrYOSeh6wnVJsoYiKpJQKqIyfFcY/tXg=");

        handleServerNpc(ServerType.HUB_DUELS, locations.get(2),
                "ewogICJ0aW1lc3RhbXAiIDogMTY4NzEzMTcwODY1OCwKICAicHJvZmlsZUlkIiA6ICI2NDU4Mjc0MjEyNDg0MDY0YTRkMDBlNDdjZWM4ZjcyZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaDNtMXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzY1N2VmNmU4NjNhZmViYjVjZmM5YmQ0YTFlYWMzZDdmMmVlNmVlZjM1YzE4ZWQyZjQ3MmMxMDE3Y2YwOGNkZCIKICAgIH0KICB9Cn0=",
                "RKh+hREnrO/wW1erBWI/+Ws9+u/3g6dP2iWLxewkFQ7vPchVlYe6XA+NZpORu0eVCVF/2tN9fNQnWWKqO746gyWXKe67LXtbZZGE7JOObIZWr3toTeQxrI87SmPU32jsgSEEIfeMhmaFxzaeSUdJMPcdhsRo9sf4C4ZqGBGVaEaHUwPAyxTi1j3ix6EBQRORjQv1fhHECSjRXr/d3Go5bUWbgw0Kr6+XNJrTsTNnHPPglAamH2OQCfcr2CUKa4dxVBUwXdntDu6ef84fU1RWed7ASSUi99EBMgPmkxhEw4IcI7+mRlFXtk3iivclU2Cz/NRWkZg29ucsx1EqAEuVCGPC+E1bst2Y1H6uwZw5/8CIqwKfqdkV7k0w9mTPj7rMJxksmH3MZyxqWxFnrlO5OA/D9i5H6bEryUxpeG2pygLI/uzUJAfZnOBi8lMi7p+OUKyySA8OwBlHDcWruzXzC3KwswlZiT4jnrZllfCQydzM1si5CI5n4DOHltlMzt90tzn0C6cKR24FhiWhBNDoVxs1CSjiiSL0eUQPhaDPndDA8ww0zi/MG4HOjhXq0/QKs9F7GE+HW3Woj1PgR1JZCIqlnG6T9bw313pVyvcOz4hlSpL+HKDAji8WbMP1qSDtXFfwfbK1f5osDNJpIAU3GscQzW+yWtuI4OOdpXhzOjc=");

        handleServerNpc(ServerType.HUB_PVP, locations.get(3),
                "ewogICJ0aW1lc3RhbXAiIDogMTczNDU0NzI1MzE2OCwKICAicHJvZmlsZUlkIiA6ICJlNzM4MTYzZTYwM2M0MTFkOTg4MzNiYzkyZTI4Y2IyYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJYZW5va3JhdGVzUml0dmEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzZmZDdmM2ZiMGU2NGIwZWEzZWY5ZWRmN2U5MzRkMzczNjZjZjE4MTBjNzY5ZGI1ZTIxZWY2Yzk5NTk2ODMyYiIKICAgIH0KICB9Cn0=",
                "d54fZN/2uPkAOtpxH/K0PTsXx7yvhXjOREgVLXMKE/NUoFWq05v2XqnuKg0uW536NUItbQxhbOnJzPTxFCaj62ffHIF9bVoD1x1h5DH7eqwVkQMxMmb0nVrmAO0jJMSC7Np+fZ+QYpW1cVVsZDkWzryDerqOoXmJgMnWmduny81TmDMJtJH628F/O86WG3enNqSNI+DdXWXWXKxZiAMb2LLULvnSqk0dMkt3PMMObJXYzdXaJh6mud4Hd/5vBrPEcYwTIkmOh/YSxc7xKuHo24rDMddCm2WR324ouehiE/EpansKYNMe8NJOnVIiTZ6lc7kHXmptGfDSrsv0dYnkpgVT4po1fvWaxn06Vfsu7l3k8r6ef31depW/v+ZCaF752GURsxgZ6i7nqoaF7GVLXhFXxL84dCZGGi15DWcCckd/gb7AluKlYAz+NqVSFPKaiA5+Rnm6AKs0d+68PQvatIYRQ9DKaADsgEm0C/uIEFHVgY8BcdplldLhrsSbIDIZKXh93kvJT+CXhrGNRGruHd4V0NWWjEwsAHi4Oqt6dsMZuOimnCOQmkHnvAq41ae1+OVgpg6aC9bcOOvQAzU7RfiQrk1B2d09oBNZyf6Es59PYHrdWOZmhOTRoQFBRz0qkzSGhgbphp4dpn0uiNnb2XipxnfAsl1J9W7o5Ta0eZ0=");

    }

    @Override
    public void chat(Player player, String message) {
        User user = (User) User.of(player.getUniqueId());

        Account account = user.getAccount();

        Savage.getAccountController().list().forEach(target -> {
            TextComponent text = new TextComponent(account.getTag().getByPrefix(target.getTagPrefix()) + account.getNickname() + ": ");

            text.addExtra(account.isAllowColoredChat() ? "§f" + Util.color(message) : "§7" + message);

            target.send(text);
        });
    }

    @Override
    public Vector getSlimeJump(Vector direction) {
        return direction.multiply(2.1).setY(0.6);
    }

    @EventHandler
    public void onAsyncCounterUpdate(AsyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (ServerType server : ServerType.list(ServerType::isLobby)) {
                if (server.equals(ServerType.HUB)) continue;

                HologramServer hologram = BukkitCore.getManager().getHologram().getServer(server.name().toLowerCase());

                if (hologram != null) {
                    int onlinePlayers = Savage.getServerData().getOnlinePlayers(server);

                    ServerType realServer = server.getRealServerOfLobby();

                    if (realServer != null)
                        onlinePlayers += Savage.getServerData().getOnlinePlayers(realServer);

                    hologram.setText(1, "§e" + Util.formatNumber(onlinePlayers) + " jogando.");
                }
            }
        }
    }
}


