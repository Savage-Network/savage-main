package com.minecraft.arcade.eventos.listeners;

import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.arcade.eventos.Eventos;
import com.minecraft.arcade.eventos.settings.EventEmbeddedConfig;
import com.minecraft.arcade.eventos.utils.EventUiTheme;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Menu de espectador: teleporte para vivos §8(§70§8-§77§8) §7e volta ao lobby no slot §c8§7.
 */
@IgnoreEvent
public class SpectatorMenuListener implements Listener {

    private static final int SLOT_LOBBY = 8;
    private static final int SIZE = 9;

    private final Eventos plugin;

    public SpectatorMenuListener() {
        this(Eventos.getPlugin());
    }

    public SpectatorMenuListener(Eventos plugin) {
        this.plugin = plugin;
    }

    /** Abre o menu para o jogador (espectador). Slots 0-7 = cabeças dos vivos para teleportar. */
    public void openMenu(Player espectador) {
        List<UUID> vivos = new ArrayList<>(plugin.getEventManager().getVivos());
        List<UUID> shown = new ArrayList<>();
        for (int i = 0; i < vivos.size() && shown.size() < SLOT_LOBBY; i++) {
            UUID uuid = vivos.get(i);
            Player target = Bukkit.getPlayer(uuid);
            if (target != null && target.isOnline()) {
                shown.add(uuid);
            }
        }
        Inventory inv = Bukkit.createInventory(new SpectatorMenuHolder(shown), SIZE, EventUiTheme.SPECTATOR_MENU_TITLE);
        for (int i = 0; i < shown.size() && i < SLOT_LOBBY; i++) {
            Player target = Bukkit.getPlayer(shown.get(i));
            if (target != null) {
                inv.setItem(i, criarCabeca(target.getName()));
            }
        }

        inv.setItem(SLOT_LOBBY, criarItemLobby());

        espectador.openInventory(inv);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getItem() == null || e.getItem().getType() != Material.COMPASS) return;
        Player p = e.getPlayer();
        if (plugin.getEventManager().getState() != com.minecraft.arcade.eventos.managers.EventState.EM_JOGO) return;
        if (!plugin.getEventManager().isEspectador(p.getUniqueId())) return;
        e.setCancelled(true);
        openMenu(p);
    }

    private static ItemStack criarCabeca(String nomeJogador) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setOwner(nomeJogador);
            meta.setDisplayName("§a§l" + nomeJogador);
            List<String> lore = new ArrayList<>();
            lore.add("§8Eventos §7· §8vivo");
            lore.add("");
            lore.add("§7Teleporta até este jogador");
            lore.add("§7que ainda está vivo na arena.");
            lore.add("");
            lore.add("§eClique para teleportar!");
            meta.setLore(lore);
        }
        skull.setItemMeta(meta);
        return skull;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (!(e.getInventory().getHolder() instanceof SpectatorMenuHolder)) return;
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= e.getInventory().getSize()) return;

        SpectatorMenuHolder holder = (SpectatorMenuHolder) e.getInventory().getHolder();
        if (slot == SLOT_LOBBY) {
            p.closeInventory();
            String servidor = plugin.getVariablesManager().get("event.lobby_server", EventEmbeddedConfig.LOBBY_SERVER);
            plugin.getBungeeManager().enviarParaServidor(p, servidor);
            p.sendMessage(EventUiTheme.CHAT_PREFIX + "§7Conectando ao lobby §f" + servidor + "§7...");
            return;
        }
        if (slot < holder.getPlayers().size()) {
            UUID targetId = holder.getPlayers().get(slot);
            Player target = Bukkit.getPlayer(targetId);
            if (target != null && target.isOnline()) {
                p.closeInventory();
                p.teleport(target.getLocation());
                p.sendMessage(EventUiTheme.CHAT_PREFIX + "§aTeleportado para §f" + target.getName() + "§a.");
            }
        }
    }

    private ItemStack criarItemLobby() {
        ItemStack item = new ItemStack(Material.NETHER_STAR, 1);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lVoltar ao lobby");
            List<String> lore = new ArrayList<>();
            lore.add("§8Eventos · espectador");
            lore.add("");
            lore.add("§7Volta ao servidor de lobby");
            lore.add("§7através do BungeeCord.");
            lore.add("");
            lore.add("§eClique para conectar!");
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    /** Holder que guarda a lista de UUIDs dos jogadores vivos (ordem dos slots 0 a 7). */
    public static class SpectatorMenuHolder implements InventoryHolder {
        private final List<UUID> players;

        public SpectatorMenuHolder(List<UUID> players) {
            this.players = players != null ? new ArrayList<>(players) : new ArrayList<>();
        }

        public List<UUID> getPlayers() {
            return players;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
