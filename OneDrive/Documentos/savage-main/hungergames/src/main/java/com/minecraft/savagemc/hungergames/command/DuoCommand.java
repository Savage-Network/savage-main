package com.minecraft.savagemc.hungergames.command;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.game.Game;
import com.minecraft.savagemc.hungergames.game.list.Event;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import com.minecraft.savagemc.hungergames.util.game.GameStage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DuoCommand implements Assistance, BukkitInterface, VariableStorage {

    public DuoCommand() {
        loadVariables();
    }

    @Variable(name = "event.command.duo", permission = Rank.SECONDARY_MOD)
    private boolean duo = false;

    @Command(name = "duo", platform = Platform.PLAYER, aliases = "dupla")
    public void execute(Context<CommandSender> context) {
        User user = getUser(context.getUniqueId());
        // Verifica se o comando está habilitado OU se o modo do evento é DUO
        Game game = HungerGames.getInstance().getGame();
        boolean isDuoMode = game instanceof Event && ((Event) game).getEventMode() == Event.EventMode.DUOS;
        
        if (!duo && !isDuoMode) {
            context.sendMessage("§cO modo dupla não está habilitado nesta sala.");
            return;
        }

        if (context.argsCount() == 0) {
            sendUsage(context);
            if (user.getAccount().hasPermission(Rank.TRIAL_MODERATOR)) {
                context.sendMessage("§c* /" + context.getLabel() + " ver <nick>");
                context.sendMessage("§c* /" + context.getLabel() + " verduplas");
                return;
            }
            return;
        }

        Argument argument = Argument.get(context.getArg(0));

        if (argument == null) {
            // Se não é um comando específico, trata como convite direto
            if (context.argsCount() == 1) {
                // /dupla <nick> - convida diretamente
                String targetName = context.getArg(0);
                Player target = Bukkit.getPlayer(targetName);
                
                if (target == null) {
                    context.sendMessage("§cJogador não encontrado.");
                    return;
                }
                
                User targetUser = User.fetch(target.getUniqueId());
                
                if (targetUser == null) {
                    context.sendMessage("§cJogador não encontrado.");
                    return;
                }
                
                if (user.getUniqueId().equals(targetUser.getUniqueId())) {
                    context.sendMessage("§cVocê não pode convidar a si mesmo.");
                    return;
                }
                
                if (user.getDuo() != null) {
                    context.sendMessage("§cVocê já possui uma dupla.");
                    return;
                }
                
                if (targetUser.getDuo() != null) {
                    context.sendMessage("§cO jogador alvo já possui uma dupla.");
                    return;
                }
                
                if (!targetUser.isAlive()) {
                    context.sendMessage("§cO jogador alvo precisa estar vivo para você convidá-lo como sua dupla.");
                    return;
                }
                
                user.getRequests().add(targetUser.getUniqueId());
                
                context.sendMessage("§aVocê convidou " + target.getName() + " para ser sua dupla.");
                target.sendMessage("§a" + user.getPlayer().getName() + " lhe convidou para ser a dupla dele.");
                
                TextComponent component = new TextComponent(TextComponent.fromLegacyText("§b§lCLIQUE AQUI §epara aceitar o convite de dupla."));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dupla aceitar " + user.getPlayer().getName()));
                
                target.spigot().sendMessage(component);
                return;
            } else {
                context.info("no_function", context.getArg(0).toLowerCase());
                return;
            }
        }

        if (context.argsCount() == 0) {
            sendUsage(context);
            if (user.getAccount().hasPermission(Rank.TRIAL_MODERATOR)) {
                context.sendMessage("§c* /" + context.getLabel() + " ver <nick>");
                context.sendMessage("§c* /" + context.getLabel() + " verduplas");
                return;
            }
            return;
        }

        try {
            argument.getExecutor().execute(context);
        } catch (Exception e) {
            context.sendMessage("§cOcorreu um erro ao executar o comando. Tente novamente mais tarde.");
            e.printStackTrace();
        }
    }

    private void sendUsage(Context<CommandSender> context) {
        context.sendMessage("§cUso do /" + context.getLabel() + ":");
        context.sendMessage("§c* /" + context.getLabel() + " convidar <player>");
        context.sendMessage("§c* /" + context.getLabel() + " aceitar <player>");
        context.sendMessage("§c* /" + context.getLabel() + " sair");
    }

    @AllArgsConstructor
    @Getter
    public enum Argument implements Assistance {

        CONVIDAR("convidar", 2, context -> {

            if (context.argsCount() < 2) {
                context.sendMessage("§cVocê precisa informar alguém para ser sua dupla.");
                context.sendMessage("§c/dupla convidar <nick>");
                return;
            }

            String playerName = context.getArg(1);
            if (playerName == null || playerName.trim().isEmpty()) {
                context.sendMessage("§cNome do jogador inválido.");
                return;
            }

            Player target = Bukkit.getPlayer(playerName);

            if (target == null) {
                context.info("target.not_found");
                return;
            }

            User senderUser = User.fetch(context.getUniqueId());
            User targetUser = User.fetch(target.getUniqueId());

            if (senderUser.getRequests().contains(target.getUniqueId())) {
                context.sendMessage("§cVocê já convidou este jogador anteriormente.");
                return;
            }

            if (senderUser.getDuo() != null) {
                context.sendMessage("§cVocê já possui uma dupla.");
                return;
            }

            if (targetUser.getDuo() != null) {
                context.sendMessage("§cO jogador alvo já possui uma dupla.");
                return;
            }

            if (!senderUser.isAlive()) {
                context.sendMessage("§cVocê precisa estar vivo para fazer isso.");
                return;
            }

            if (senderUser.getPlayer() == target) {
                context.sendMessage("§cVocê não pode convidar a si mesmo.");
                return;
            }

            if (!targetUser.isAlive()) {
                context.sendMessage("§cO jogador alvo precisa estar vivo para você convidá-lo como sua dupla.");
                return;
            }

            senderUser.getRequests().add(targetUser.getUniqueId());

            context.sendMessage("§aVocê convidou " + target.getName() + " para ser sua dupla.");
            target.sendMessage("§a" + senderUser.getPlayer().getName() + " lhe convidou para ser a dupla dele.");

            TextComponent component = new TextComponent(TextComponent.fromLegacyText("§b§lCLIQUE AQUI §epara aceitar o convite de dupla."));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dupla aceitar " + senderUser.getPlayer().getName()));

            target.spigot().sendMessage(component);
        }),

        ACEITAR("aceitar", 2, context -> {

            if (context.argsCount() < 2) {
                context.sendMessage("§cVocê precisa informar o nick do player que te convidou.");
                context.sendMessage("§c/dupla aceitar <nick>");
                return;
            }

            String playerName = context.getArg(1);
            if (playerName == null || playerName.trim().isEmpty()) {
                context.sendMessage("§cNome do jogador inválido.");
                return;
            }

            Player target = Bukkit.getPlayer(playerName);

            if (target == null) {
                context.info("target.not_found");
                return;
            }

            User senderUser = User.fetch(context.getUniqueId());
            User targetUser = User.fetch(target.getUniqueId());

            if (senderUser.getDuo() != null) {
                context.sendMessage("§cVocê já possui uma dupla.");
                return;
            }

            if (targetUser.getDuo() != null) {
                context.sendMessage("§cO jogador alvo já possui uma dupla.");
                return;
            }

            if (!targetUser.getRequests().contains(senderUser.getUniqueId())) {
                context.sendMessage("§cO jogador alvo não lhe convidou para ser a dupla dele.");
                return;
            }

            targetUser.setDuo(senderUser);
            senderUser.setDuo(targetUser);

            context.sendMessage("§aVocê aceitou o convite de dupla de " + target.getName() + ".");
            target.sendMessage("§a" + senderUser.getPlayer().getName() + " aceitou seu convite de dupla.");

            targetUser.getRequests().remove(senderUser.getUniqueId());
            senderUser.getRequests().remove(targetUser.getUniqueId());
            
            // Atualiza a scoreboard de ambos os jogadores
            HungerGames.getInstance().getGame().handleSidebar(senderUser);
            HungerGames.getInstance().getGame().handleSidebar(targetUser);
        }),

        SAIR("sair", 0, context -> {

            User senderUser = User.fetch(context.getUniqueId());
            User duoUser = senderUser.getDuo();

            if (duoUser.isAlive()) {
                context.sendMessage("§cVocê não pode sair com o sua dupla viva.");
                return;
            }

            Game game = HungerGames.getInstance().getGame();

            GameStage stage = game.getStage();

            if (stage == GameStage.WAITING || stage == GameStage.INVINCIBILITY) {
                context.sendMessage("§cVocê só pode sair da dupla após a sala iniciar.");
                return;
            }


            if (duoUser == null) {
                context.sendMessage("§cVocê não está em uma dupla.");
                return;
            }

            duoUser.setDuo(null);
            duoUser.getPlayer().sendMessage("§aSeu parceiro desfez a dupla.");
            senderUser.setDuo(null);
            context.sendMessage("§aAgora você não está mais em uma dupla.");
            
            // Atualiza a scoreboard de ambos os jogadores
            HungerGames.getInstance().getGame().handleSidebar(senderUser);
            HungerGames.getInstance().getGame().handleSidebar(duoUser);
        }),

        VERDUPLAS("verduplas", 0, context -> {

            User user = User.fetch(context.getUniqueId());

            if (!user.getAccount().hasPermission(Rank.TRIAL_MODERATOR)) {
                context.info("command.insufficient_permission");
                return;
            }

            List<String> duos = Bukkit.getOnlinePlayers().stream()
                    .map(player -> User.fetch(player.getUniqueId()))
                    .filter(u -> u.getDuo() != null && u.getUniqueId().compareTo(u.getDuo().getUniqueId()) < 0) // Evita duplicação
                    .map(u -> "§a" + u.getPlayer().getName() + "§f e §a" + u.getDuo().getPlayer().getName()) // Nomes verdes
                    .collect(Collectors.toList());

            if (duos.isEmpty()) {
                context.sendMessage("§cNenhuma dupla encontrada no momento.");
                return;
            }

            context.sendMessage("§aLista de duplas online:");
            duos.forEach(context::sendMessage);
        }),
        VER("ver", 1, context -> {

            User user = User.fetch(context.getUniqueId());

            if (!user.getAccount().hasPermission(Rank.TRIAL_MODERATOR)) {
                context.info("command.insufficient_permission");
                return;
            }

            Player target = Bukkit.getPlayer(context.getArg(1));

            if (target == null) {
                context.info("target.not_found");
                return;
            }

            User targetUser = User.fetch(target.getUniqueId());

            context.sendMessage((targetUser.getDuo() == null ? "§cO jogador alvo não possui nenhuma dupla." : "§aA dupla de " + target.getName() + " é " + targetUser.getDuo().getName()));
        });

        private final String argument;
        private final int minimumArgs;
        private final Executor executor;

        public static Argument get(String key) {
            return Arrays.stream(values()).filter(c -> c.getArgument() != null && c.getArgument().equalsIgnoreCase(key)).findFirst().orElse(null);
        }
    }

    private interface Executor {
        void execute(Context<CommandSender> context);
    }

    @Completer(name = "duo")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return getOnlineNicknames(context);

        return Collections.emptyList();
    }
}
