package com.minecraft.savagemc.core.backend.data.list.server;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.server.Server;
import com.minecraft.savagemc.core.server.payload.ServerPayload;
import com.minecraft.savagemc.core.server.type.ServerType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecraft.savagemc.core.util.Util.sum;

public abstract class ServerData {

    private final String SERVER_KEY_FORMAT = "server:";

    public abstract int getPort();

    public void start(int maxPlayers) {
        ServerType serverType = Savage.getServerType();
        
        if (serverType == null) {
            Savage.getLogger().warning("[Server] ServerType não está definido. Não foi possível iniciar o servidor.");
            return;
        }

        // Evitar criar servidores com a mesma porta e tipo
        Server existingServer = of(getPort());
        if (existingServer != null && existingServer.getType() != null && existingServer.getType().equals(serverType)) return;

        int possibleId = getServerList(server -> server.getType() != null && server.getType().equals(serverType)).size() + 1; // Example: 0 servers -> 0 + 1 = 1 possible ID

        Server server = new Server(possibleId, getPort(), serverType, new ServerPayload(maxPlayers));

        Savage.setServerId(possibleId);
        Savage.setServerPort(getPort());

        Savage.getRedis().save(SERVER_KEY_FORMAT + server.getIdentifier(), server);
    }

    public void stop() {
        Server server = local();

        if (server != null)
            Savage.getRedis().delete(SERVER_KEY_FORMAT + server.getIdentifier());
        else
            Savage.getLogger().info("[Server] Não foi possível encerrar o servidor. Servidor não encontrado!");
    }

    public void refresh(Server server) {
        Savage.getRedis().update(SERVER_KEY_FORMAT + server.getIdentifier(), server);
    }

    public void updatePlayerCounter(int players) {
        Server server = local();

        if (server != null) {
            ServerPayload payload = server.getPayload();

            payload.setHeartBeat(System.currentTimeMillis());
            payload.setPlayers(players);

            this.refresh(server);
        }
    }

    /* Manager Servers */
    public List<Server> getServerList() {
        return Savage.getRedis().loadAll(SERVER_KEY_FORMAT, Server.class);
    }

    public List<Server> getServerList(Predicate<Server> predicate) {
        return getServerList().stream().filter(predicate).collect(Collectors.toList());
    }

    public Server of(Predicate<Server> predicate) {
        return getServerList(predicate).stream().findFirst().orElse(null);
    }

    public Server of(int port) {
        return of(server -> server.getPort() == port);
    }

    public Server of(ServerType serverType) {
        List<Server> serverList = getServerList(server -> server.getType() != null && 
            (server.getType().equals(serverType) || 
            (server.getIdentifier() != null && server.getIdentifier().replace("-", "_").equalsIgnoreCase(serverType.name()))));

        Optional<Server> bestServer = serverList.stream().filter(server -> !server.getPayload().isFull() && !server.isDead()).findFirst();

        return bestServer.orElse(null);
    }

    public Server of(String identifier) {
        return of(server -> server.getIdentifier().equalsIgnoreCase(identifier));
    }

    public Server findByNameAndId(String nameAndId) {
        return of(server -> server.getNameAndId().equalsIgnoreCase(nameAndId));
    }

    public Server local() {
        return of(getPort());
    }

    public boolean isNeedFindAnother(Server server) {
        return server.isArcade() && server.getPayload().getPlayers() >= 100;
    }

    public int getOnlinePlayers() {
        // Retornar total de jogadores em todos os servidores
        return sum(getServerList(server -> server.getPayload() != null && !server.isDead())
                .stream()
                .map(server -> server.getPayload().getPlayers())
                .collect(Collectors.toList()));
    }

    public int getOnlinePlayers(ServerType... serverTypes) {
        List<ServerType> list = Arrays.asList(serverTypes);

        return sum(getServerList(server -> server.getPayload() != null && list.contains(server.getType()))
                .stream()
                .map(server -> server.getPayload().getPlayers())
                .collect(Collectors.toList()));
    }

    // Métodos adicionais para compatibilidade
    public ServerType getServerType() {
        return Savage.getServerType();
    }

    public void broadcastMessage(String message) {
        getServerList().forEach(server -> {
            // Enviar mensagem para todos os jogadores no servidor
            // Implementação específica depende do sistema de mensageria
        });
    }
}
