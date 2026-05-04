package com.minecraft.arcade.eventos.managers;

import com.minecraft.arcade.eventos.Eventos;
import com.minecraft.arcade.eventos.settings.EventEmbeddedConfig;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Envia jogadores para outros servidores via BungeeCord e sinaliza reinício.
 * Requer bungeecord: true no spigot.yml e canal BungeeCord no plugin.yml.
 */
public class BungeeManager {

    private static final String CHANNEL = "BungeeCord";
    private static final String SUBCHANNEL_RESTART = "EventosRestart";

    private final Eventos plugin;

    public BungeeManager(Eventos plugin) {
        this.plugin = plugin;
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
    }

    /**
     * Envia o jogador para o servidor especificado (ex: lobby).
     */
    public void enviarParaServidor(Player player, String nomeServidor) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("Connect");
            out.writeUTF(nomeServidor);
        } catch (IOException e) {
            plugin.getLogger().warning("Erro ao montar mensagem BungeeCord: " + e.getMessage());
            return;
        }
        player.sendPluginMessage(plugin, CHANNEL, b.toByteArray());
    }

    /**
     * Sinaliza ao BungeeCord que este servidor deve ser reiniciado.
     * Deve ser chamado enquanto ainda houver jogadores online (a mensagem vai pelo canal de um deles).
     * Um plugin no BungeeCord pode escutar o subchannel e reiniciar este servidor/apagar mapa.
     */
    public void sinalizarReinicioServidor() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF(SUBCHANNEL_RESTART);
            out.writeUTF(plugin.getVariablesManager().get("event.nome_servidor_eventos", EventEmbeddedConfig.NOME_SERVIDOR_EVENTOS));
        } catch (IOException e) {
            plugin.getLogger().warning("Erro ao sinalizar reinício: " + e.getMessage());
            return;
        }
        byte[] data = b.toByteArray();
        for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            p.sendPluginMessage(plugin, CHANNEL, data);
            break; // Um jogador basta para enviar ao proxy
        }
    }
}
