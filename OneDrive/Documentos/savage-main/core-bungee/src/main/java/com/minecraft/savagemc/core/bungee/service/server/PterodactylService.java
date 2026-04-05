package com.minecraft.savagemc.core.bungee.service.server;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.util.list.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PterodactylService {

    private static final String API_URL = "https://dev.savagemc.net/api/application";
    private static final String API_KEY = "ptlc_Rb1d2KOoLyv";
    
    private static final Map<String, String> SERVER_MAP = new HashMap<>();
    
    static {
        SERVER_MAP.put("proxy", "734d91c9-324b-4413-9a60-30a21b2cdd49");
        SERVER_MAP.put("lobbybw", "643a3ac8-5ec8-4f57-9fcf-05099494e540");
        SERVER_MAP.put("lobbyhg", "3e05173b-472f-4b9b-a860-cd0196170522");
        SERVER_MAP.put("bedwars", "c3f8f732-f4bf-418f-9416-e922c370a8a4");
        SERVER_MAP.put("duels", "f1660a7d-9603-427b-8e86-c7d569f673c6");
        SERVER_MAP.put("eventos", "b77f7636-8b0f-4f14-92bf-4a376d9defeb");
        SERVER_MAP.put("builds", "99903462-53bb-4ed7-9466-38dbc2378aeb");
        SERVER_MAP.put("auth", "7c31b89c-706b-4b0d-9533-c67a710cfa97");
        SERVER_MAP.put("lobbyduels", "ec9cd6c1-721d-400f-95df-c267b35551ee");
        SERVER_MAP.put("lobbypvp", "1982427a-01d4-499e-82a9-6750be502d37");
        SERVER_MAP.put("pvp", "aef757bf-0f23-4814-906c-49566c710e9f");
        SERVER_MAP.put("lobby", "84b6f085-6ede-4f9b-9791-1f49d4bd9d17");
    }

    public CompletableFuture<Boolean> startServer(String serverName) {
        return sendPowerSignal(serverName, "start");
    }

    public CompletableFuture<Boolean> stopServer(String serverName) {
        return sendPowerSignal(serverName, "stop");
    }

    public CompletableFuture<Boolean> restartServer(String serverName) {
        return sendPowerSignal(serverName, "restart");
    }

    public CompletableFuture<String> getServerStatus(String serverName) {
        String uuid = SERVER_MAP.get(serverName.toLowerCase());
        if (uuid == null) {
            return CompletableFuture.completedFuture("UNKNOWN");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                String response = HttpRequest.get(API_URL + "/servers/" + uuid + "/resources")
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body();
                
                if (response.contains("\"state\":\"running\"")) return "ONLINE";
                if (response.contains("\"state\":\"starting\"")) return "STARTING";
                if (response.contains("\"state\":\"stopping\"")) return "STOPPING";
                if (response.contains("\"state\":\"offline\"")) return "OFFLINE";
                return "UNKNOWN";
            } catch (Exception e) {
                Savage.getLogger().warning("[Pterodactyl] Erro ao obter status: " + e.getMessage());
                return "ERROR";
            }
        });
    }

    private CompletableFuture<Boolean> sendPowerSignal(String serverName, String signal) {
        String uuid = SERVER_MAP.get(serverName.toLowerCase());
        if (uuid == null) {
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = "{\"signal\":\"" + signal + "\"}";
                HttpRequest.post(API_URL + "/client/servers/" + uuid + "/power")
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .send(json)
                    .body();
                return true;
            } catch (Exception e) {
                Savage.getLogger().warning("[Pterodactyl] Erro ao enviar sinal '" + signal + "': " + e.getMessage());
                return false;
            }
        });
    }

    public String getServerUUID(String serverName) {
        return SERVER_MAP.get(serverName.toLowerCase());
    }

    public boolean isValidServer(String serverName) {
        return SERVER_MAP.containsKey(serverName.toLowerCase());
    }

    public Map<String, String> getAvailableServers() {
        return SERVER_MAP;
    }
}
