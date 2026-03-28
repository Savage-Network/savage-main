package com.minecraft.savagemc.core.bungee.command.single;

import com.minecraft.savagemc.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.mojang.MojangApi;
import com.minecraft.savagemc.core.backend.database.redis.message.types.account.AccountNickChangeMessage;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.core.util.list.Validator;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FakeCommand implements CommandInheritor {

    private final List<String> RANDOM_NAMES = Arrays.asList(
            "JoaoZika_47", "Brav0Tezão", "XablauPikao21", "ZezinDaQuebra", "CachorroLokoZ",
            "FogueteDoido12", "MlkDoSertao87", "ZicaDoBagui21", "TropaSombraZZ", "CoringaBRv",
            "JorginTopZera", "RatoLocoBR", "MlkDoBonde77", "SombraTigre21", "VagabundoDoMorro",
            "Lendario_15", "MacacoAtomic", "AzulaoBR_v2", "VyytorZK", "FuraOlho99",
            "CapitaoM4to", "FuscaoPreto_77", "FantasmaRoxo", "BrisaSincera", "PegasusBR99",
            "TigreNegroZX", "TonhaoDoRole", "VerdaoDoMorumbi", "PanteraBrava44", "GaludoDaRoça",
            "FuracaoBR_01", "LeoDoValeXX", "Skullzinho123", "JucaTatu_vip", "PirataRoxo",
            "BRNinja_42", "ZePorrada_", "TigreManco88", "UrubuNervoso", "RaposaVeloz_",
            "JacareDoPantano", "CaveiraDoMorrao", "BrasilBala_", "PatraoMalucoX", "AranhaFumante",
            "DragaoZord_v2", "TropinhaMonstro", "CabraMacho_", "Pantheraz_", "GoianoBruto_7"
    );

    @Command(name = "fake", aliases = {"nick"}, rank = RankType.SAVAGE, runAsync = true)
    public void fake(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cComo usar: /" + context.getLabel() + ":",
                    "§c* /" + context.getLabel() + " (apelido)",
                    "§c* /" + context.getLabel() + " (reset)",
                    "§c* /" + context.getLabel() + " (random)");
            return;
        }

        String nickName = args[0];

        /* Ver a lista de nomes */
        if (nickName.equalsIgnoreCase("list")) {
            List<Account> fakeList = Savage.getAccountController().filter(Account::isUsingFake);

            if (fakeList.isEmpty()) {
                account.send("§cNão há jogadores com nomes falsos.");
                return;
            }

            TextComponent builder = new TextComponent("§aLista de fakes:");

            int index = 1;
            boolean end = false;
            for (Account faker : fakeList) {
                if (index >= fakeList.size()) end = true;

                TextComponent name = new TextComponent("§f" + faker.getNickname());

                name.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7" + faker.getName())));

                builder.addExtra(name);
                builder.addExtra("§r" + (end ? "." : ", "));

                index++;
            }

            account.send(builder);
            return;
        }

        /* Restaurar para o nick original */
        if (nickName.equalsIgnoreCase("#") || nickName.equalsIgnoreCase("reset")) {
            if (!account.isUsingFake()) {
                account.send("§cVocê não está usando fake.");
                return;
            }

            account.setFake("");

            new AccountNickChangeMessage(account, account.getNickname()).send();
            return;
        }

        /* Nome Aleatório */
        if (nickName.equalsIgnoreCase("random")) {
            do nickName = RANDOM_NAMES.get(Savage.RANDOM.nextInt(RANDOM_NAMES.size()));

            while (isNotValidName(nickName));
        }

        if (account.hasCooldown(Constant.NICK_CHANGE_COOLDOWN_KEY)) {
            account.send("§cAguarde " + account.getFormattedCooldown(Constant.NICK_CHANGE_COOLDOWN_KEY) + " para trocar de nick novamente.");
            return;
        }

        if (isNotValidName(nickName)) {
            account.send("§cOps! O nome solicitado não é válido.");
            return;
        }

        account.setFake(nickName);

        new AccountNickChangeMessage(account, nickName).send();

        if (!account.hasRank(RankType.STUDIO))
            account.setCooldown(Constant.NICK_CHANGE_COOLDOWN_KEY, TimeUnit.SECONDS.toMillis(15));
    }

    protected boolean isNotValidName(String name) {
        return !Validator.isNickname(name) || (Savage.MOJANG_API.isPremium(name).equals(MojangApi.ResponseCode.DONE)
                || Savage.getAccountController().isPresent(account -> account.getNickname().equalsIgnoreCase(name)));
    }
}
