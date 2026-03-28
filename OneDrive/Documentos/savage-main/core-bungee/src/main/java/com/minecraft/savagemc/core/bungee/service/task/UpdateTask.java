package com.minecraft.savagemc.core.bungee.service.task;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.bungee.event.list.update.UpdateEvent;
import com.minecraft.savagemc.core.server.type.ServerType;
import com.minecraft.savagemc.core.util.Util;

import java.util.Arrays;
import java.util.List;

public class UpdateTask implements Runnable {

    private final List<String> customMessages = Arrays.asList(
            "&6Achou algum hacker? &eUtilize &b/report <jogador> <motivo>",
            "&eVeja suas estatísticas utlizando &b/stats",
            "&eConsiga &6ranks &eem nosso site &bwww.savagemc.net",
            "&6Evite ser banido! &eConfira nossas regras &bsavagemc.net/regras",
            "&6Entre em nosso Discord! &eE fique por dentro de &dpromoções e novidades &bdiscord.gg/savagemc",
            "&6Cansou de achar xitado? &eVenha participar de nossa equipe &bsavagemc.net/aplicar",
            "&6Está tudo bem? &eO CVV realiza apoio emocional e prevenção ao suicídio, atendendo de forma voluntária todos que precisam conversar &bhttps://www.cvv.org.br/",
            "&6Cansado da mesma &cvelha skin&6? &eConfira nossas opções em &b/skin",
            "&eUtilize &b/block &epara bloquear jogadores indesejados."
    );

    private final int messageSize = customMessages.size();

    private final int delay = 120; // 120 segundos

    int time = delay, index = 0;

    @Override
    public void run() {
        /* Chamando evento de atualização */
        new UpdateEvent().call();

        /* Mensagens Automáticas */
        time--;

        if (time == 0) {
            Savage.getAccountController()
                    .filter(account -> !account.inServer(ServerType.AUTH))
                    .forEach(account -> account.send(Constant.SERVER_PREFIX + Util.color(customMessages.get(index))));

            time = delay;
            index++;

            if (index >= messageSize)
                index = 0;
        }
    }
}
