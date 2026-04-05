package com.minecraft.savagemc.hungergames.game.team;

import com.minecraft.core.translation.Language;
import com.minecraft.savagemc.hungergames.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Team {

    private int id;
    private String name;
    private ChatColor chatColor;
    private Color color;
    private final List<User> members = new ArrayList<>();

    public boolean isMember(User user) {
        return equals(user.getTeam());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return id == team.id;
    }

    public String printMembers() {

        Iterator<User> iterator = getMembers().iterator();

        StringBuilder stringBuilder = new StringBuilder();

        while (iterator.hasNext()) {

            User user = iterator.next();

            stringBuilder.append(user.getAccount().getRank().getDefaultTag().getFormattedColor()).append(user.getName());

            if (iterator.hasNext())
                stringBuilder.append("§f, ");
        }

        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return chatColor + Language.PORTUGUESE.translate(name);
    }
}
