package com.minecraft.savagemc.core.bukkit.api.sidebar.row;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.scoreboard.Score;

@Getter
@Setter
@RequiredArgsConstructor
public class SidebarRow {

    private final String field;
    private String prefix;
    private String suffix;

    private Score score;

    private int slot;

    public SidebarRow(String field, String prefix) {
        this.field = field;

        this.prefix = prefix;
        this.suffix = "";
    }

    // Métodos adicionais para compatibilidade
    public void setSlot(int slot) {
        this.slot = slot;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public int getSlot() {
        return slot;
    }

    // Métodos adicionais para compatibilidade
    public String getField() {
        return field;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }
}
