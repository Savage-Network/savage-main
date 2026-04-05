package com.minecraft.savagemc.core.bungee.service.security;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.bungee.service.webhook.DiscordWebhookService;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class AntiDDoSService implements Listener {

    private final DiscordWebhookService webhookService = new DiscordWebhookService();
    
    private final Map<String, ConnectionTracker> ipTracker = new ConcurrentHashMap<>();
    private final Map<String, ConnectionTracker> nameTracker = new ConcurrentHashMap<>();
    private final Map<String, Long> blacklisted = new ConcurrentHashMap<>();
    private final Map<String, String> ipCountryCache = new ConcurrentHashMap<>();
    private final Set<String> suspiciousNicksBlocked = ConcurrentHashMap.newKeySet();
    
    private static final int MAX_CONNECTIONS_PER_IP = 3;
    private static final int MAX_CONNECTIONS_PER_SECOND = 10;
    private static final long BLACKLIST_DURATION = TimeUnit.MINUTES.toMillis(15);
    private static final long CLEANUP_INTERVAL = TimeUnit.MINUTES.toMillis(5);
    private static final long WINDOW_SIZE = TimeUnit.SECONDS.toMillis(1);

    private static final Set<String> BLOCKED_COUNTRIES = new HashSet<>(Arrays.asList(
        "RU", "CN", "IN", "ID", "VN", "PH", "IR", "PK", "BD", "KZ",
        "UA", "RO", "BG", "TR",
        "NG", "KE", "ZA"
    ));
    
    private static final Set<String> VERIFICATION_COUNTRIES = new HashSet<>(Arrays.asList(
        "US", "DE", "NL"
    ));
    
    private static final List<Pattern> SUSPICIOUS_NICK_PATTERNS = Arrays.asList(
        Pattern.compile(".*spam.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*bot_.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*teste.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*bombadesavage.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*savagebomba.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*ddos.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*ataque.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*nigg.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*preto.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*macaco.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*[0-9]{5,}.*"),
        Pattern.compile("^[0-9]+$"),
        Pattern.compile(".*(.)\\1{4,}.*"),
        Pattern.compile(".*[A-Z].*[0-9].*[A-Z].*[0-9].*")
    );

    public AntiDDoSService() {
        ProxyServer.getInstance().getScheduler().schedule(
            ProxyServer.getInstance().getPluginManager().getPlugin("Core"),
            this::cleanup,
            CLEANUP_INTERVAL,
            CLEANUP_INTERVAL,
            TimeUnit.MILLISECONDS
        );
    }

    @EventHandler
    public void onPreLogin(PreLoginEvent event) {
        PendingConnection connection = event.getConnection();
        String ip = connection.getAddress().getAddress().getHostAddress();
        String name = connection.getName();

        // Verificar whitelist primeiro
        if (isWhitelisted(name, ip)) {
            return;
        }

        if (isBlacklisted(ip)) {
            event.setCancelled(true);
            event.setCancelReason(TextComponent.fromLegacyText("§c§lANTIDDOS!\n\n§cVocê foi detectado como uma ameaça, se você acha que isso é um erro, contate a administração\n\n§cdiscord.gg/savage"));
            logBlockedConnection(ip, name, "IP_BLACKLISTED");
            webhookService.sendAlert("🚫 IP Bloqueado", "Nick: ##" + name + "##\nIP: `" + ip + "`\nMotivo: IP na blacklist", 15158332);
            return;
        }

        if (isSuspiciousNickname(name)) {
            event.setCancelled(true);
            event.setCancelReason(TextComponent.fromLegacyText("§c§lANTIDDOS!\n\n§cVocê foi detectado como uma ameaça, se você acha que isso é um erro, contate a administração\n\n§cdiscord.gg/savage"));
            suspiciousNicksBlocked.add(name + "|" + ip + "|" + System.currentTimeMillis());
            logBlockedConnection(ip, name, "SUSPICIOUS_NICK");
            webhookService.sendAlert("🚫 Nick Suspeito Bloqueado", "Nick: ##" + name + "##\nIP: `" + ip + "`\nMotivo: Padrão suspeito detectado", 15158332);
            return;
        }

        String country = getCountryFromIP(ip);
        
        if (country != null && BLOCKED_COUNTRIES.contains(country)) {
            event.setCancelled(true);
            event.setCancelReason(TextComponent.fromLegacyText("§c§lANTIDDOS!\n\n§cVocê foi detectado como uma ameaça, se você acha que isso é um erro, contate a administração\n\n§cdiscord.gg/savage"));
            logBlockedConnection(ip, name, "COUNTRY_BLOCKED: " + country);
            webhookService.sendAlert("🚫 País Bloqueado", "Nick: ##" + name + "##\nIP: `" + ip + "`\nPaís: " + country, 15158332);
            return;
        }

        boolean needsVerification = country != null && VERIFICATION_COUNTRIES.contains(country);

        ConnectionTracker ipStats = ipTracker.computeIfAbsent(ip, k -> new ConnectionTracker());
        ConnectionTracker nameStats = nameTracker.computeIfAbsent(name, k -> new ConnectionTracker());

        if (checkRateLimit(ipStats, ip, "IP")) {
            event.setCancelled(true);
            event.setCancelReason(TextComponent.fromLegacyText("§c§lANTIDDOS!\n\n§cVocê foi detectado como uma ameaça, se você acha que isso é um erro, contate a administração\n\n§cdiscord.gg/savage"));
            webhookService.sendAlert("⚠️ Rate Limit Excedido", "Nick: ##" + name + "##\nIP: `" + ip + "`\nMotivo: Muitas conexões por segundo", 16776960);
            detectAttack(ip, name);
            return;
        }
        
        if (checkRateLimit(nameStats, name, "Nome")) {
            event.setCancelled(true);
            event.setCancelReason(TextComponent.fromLegacyText("§c§lANTIDDOS!\n\n§cVocê foi detectado como uma ameaça, se você acha que isso é um erro, contate a administração\n\n§cdiscord.gg/savage"));
            webhookService.sendAlert("⚠️ Rate Limit Excedido (Nick)", "Nick: ##" + name + "##\nIP: `" + ip + "`\nMotivo: Muitas tentativas com mesmo nick", 16776960);
            detectAttack(ip, name);
            return;
        }

        if (ipStats.getConcurrentConnections() >= MAX_CONNECTIONS_PER_IP) {
            blacklist(ip);
            event.setCancelled(true);
            event.setCancelReason(TextComponent.fromLegacyText("§c§lANTIDDOS!\n\n§cVocê foi detectado como uma ameaça, se você acha que isso é um erro, contate a administração\n\n§cdiscord.gg/savage"));
            webhookService.sendAlert("⚠️ IP Blacklisted", "Nick: ##" + name + "##\nIP: `" + ip + "`\nMotivo: Múltiplas conexões simultâneas excedidas", 16776960);
            return;
        }

        ipStats.recordConnection();
        nameStats.recordConnection();

        if (needsVerification) {
            Savage.getLogger().info("[AntiDDoS] Jogador " + name + " (" + ip + ") do país " + country + " em verificação.");
        }
    }

    private boolean checkRateLimit(ConnectionTracker tracker, String identifier, String type) {
        long now = System.currentTimeMillis();
        tracker.cleanOldEntries(now - WINDOW_SIZE);
        
        if (tracker.getRequestsInWindow() >= MAX_CONNECTIONS_PER_SECOND) {
            Savage.getLogger().warning("[AntiDDoS] Rate limit excedido para " + type + ": " + identifier);
            return true;
        }
        
        tracker.recordRequest();
        return false;
    }

    private void blacklist(String ip) {
        blacklisted.put(ip, System.currentTimeMillis() + BLACKLIST_DURATION);
        Savage.getLogger().warning("[AntiDDoS] IP blacklisted: " + ip);
        webhookService.sendAlert("🔒 IP Adicionado à Blacklist", "IP: `" + ip + "`\nDuração: 15 minutos", 15158332);
    }

    private boolean isWhitelisted(String name, String ip) {
        try {
            // Verificar se o nick está na whitelist
            if (Savage.getRedis().getDefault("whitelist:nick:" + name.toLowerCase()) != null) {
                return true;
            }
            // Verificar se o IP está na whitelist
            if (Savage.getRedis().getDefault("whitelist:ip:" + ip) != null) {
                return true;
            }
        } catch (Exception e) {
            Savage.getLogger().warning("[AntiDDoS] Erro ao verificar whitelist: " + e.getMessage());
        }
        return false;
    }

    private boolean isBlacklisted(String ip) {
        Long expiry = blacklisted.get(ip);
        if (expiry == null) return false;
        if (System.currentTimeMillis() > expiry) {
            blacklisted.remove(ip);
            return false;
        }
        return true;
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        blacklisted.entrySet().removeIf(entry -> entry.getValue() < now);
        ipTracker.values().forEach(t -> t.cleanOldEntries(now - WINDOW_SIZE));
        nameTracker.values().forEach(t -> t.cleanOldEntries(now - WINDOW_SIZE));
        Savage.getLogger().info("[AntiDDoS] Cleanup executado. IPs blacklistados ativos: " + blacklisted.size());
    }

    private boolean isSuspiciousNickname(String name) {
        if (name == null || name.length() < 3 || name.length() > 16) return true;
        
        for (Pattern pattern : SUSPICIOUS_NICK_PATTERNS) {
            if (pattern.matcher(name).matches()) {
                return true;
            }
        }
        return false;
    }

    private String getCountryFromIP(String ip) {
        if (ipCountryCache.containsKey(ip)) {
            return ipCountryCache.get(ip);
        }
        
        try {
            URL url = new URL("http://ip-api.com/json/" + ip + "?fields=countryCode");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            String json = response.toString();
            String countryCode = null;
            if (json.contains("countryCode")) {
                int start = json.indexOf("countryCode\":\"") + 14;
                int end = json.indexOf("\"", start);
                if (start > 13 && end > start) {
                    countryCode = json.substring(start, end);
                }
            }
            
            ipCountryCache.put(ip, countryCode);
            if (ipCountryCache.size() > 10000) {
                ipCountryCache.clear();
            }
            return countryCode;
        } catch (Exception e) {
            return null;
        }
    }

    private void detectAttack(String ip, String name) {
        long recentConnections = ipTracker.values().stream()
            .filter(t -> t.getRequestsInWindow() > MAX_CONNECTIONS_PER_SECOND / 2)
            .count();
            
        if (recentConnections > 5) {
            webhookService.sendAlert("🚨 ATAQUE DDoS DETECTADO", 
                "IPs suspeitos: " + recentConnections + "\n" +
                "Último IP: `" + ip + "`\n" +
                "Último Nick: ##" + name + "##\n" +
                "Ação: Blacklist automática ativada", 16711680);
            
            Savage.getLogger().warning("[AntiDDoS] ATAQUE DETECTADO! " + recentConnections + " IPs suspeitos.");
        }
    }

    private void logBlockedConnection(String ip, String name, String reason) {
        Savage.getLogger().info("[AntiDDoS] Conexão bloqueada - IP: " + ip + " | Nick: " + name + " | Motivo: " + reason);
    }

    public void registerConnection(String ip) {
        ConnectionTracker tracker = ipTracker.get(ip);
        if (tracker != null) {
            tracker.incrementConcurrent();
        }
    }

    public void unregisterConnection(String ip) {
        ConnectionTracker tracker = ipTracker.get(ip);
        if (tracker != null) {
            tracker.decrementConcurrent();
        }
    }

    private static class ConnectionTracker {
        private final Map<Long, Integer> requests = new ConcurrentHashMap<>();
        private int concurrentConnections = 0;

        void recordRequest() {
            long second = System.currentTimeMillis() / 1000;
            requests.merge(second, 1, Integer::sum);
        }

        void recordConnection() {
            recordRequest();
        }

        int getRequestsInWindow() {
            return requests.values().stream().mapToInt(Integer::intValue).sum();
        }

        void cleanOldEntries(long cutoff) {
            long cutoffSecond = cutoff / 1000;
            requests.entrySet().removeIf(entry -> entry.getKey() < cutoffSecond);
        }

        synchronized void incrementConcurrent() {
            concurrentConnections++;
        }

        synchronized void decrementConcurrent() {
            concurrentConnections = Math.max(0, concurrentConnections - 1);
        }

        synchronized int getConcurrentConnections() {
            return concurrentConnections;
        }
    }
}
