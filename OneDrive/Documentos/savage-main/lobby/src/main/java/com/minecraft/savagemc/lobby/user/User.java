package com.minecraft.savagemc.lobby.user;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.arcade.route.join.Join;
import com.minecraft.savagemc.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.savagemc.core.bukkit.user.UserModel;
import com.minecraft.savagemc.core.member.Member;
import com.minecraft.savagemc.lobby.Lobby;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public class User extends UserModel {

    public User(UUID id) {
        super(id, Join.PLAYER);
    }

    @Override
    public void handle() {
        Player player = getAccount().player();

        if (player != null) {
            setSidebar(new Sidebar(player, Constant.SERVER_TITLE));

            Lobby.getLoader().getArchitect().join(player);
        }
    }

    public <T extends Member> T getMember(Class<T> generic) {
        return Savage.getMemberController().of(getAccount().getId(), generic);
    }
}


