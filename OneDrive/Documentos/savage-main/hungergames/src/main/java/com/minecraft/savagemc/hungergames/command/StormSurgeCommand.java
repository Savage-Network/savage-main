package com.minecraft.savagemc.hungergames.command;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.command.BukkitFrame;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.util.DateUtils;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StormSurgeCommand implements Assistance, BukkitInterface {

    private final Pattern pattern = Pattern.compile("[a-zA-Z0-9_]{0,32}=[a-zA-Z0-9_]{0,32}");

    @Command(name = "stormsurge", platform = Platform.BOTH, rank = Rank.EVENT_MOD, usage = "{label} <effect> <seconds> <amplifier> <predicate>", aliases = "aplicarmarédetempestade")
    public void handleCommand(Context<CommandSender> context, String effect, String secondsRaw, int amplifier, String raw) {

        PotionEffectType potionEffectType = PotionEffectType.getByName(effect);

        if (potionEffectType == null) {
            context.info("object.not_found", "Effect");
            return;
        }

        Class<User> userClass = User.class;

        if (!pattern.matcher(raw).matches()) {
            context.info("command.variable.failed_to_convert");
            return;
        }

        long seconds;
        try {
            seconds = DateUtils.parseDateDiff(secondsRaw, true);
        } catch (Exception e) {
            context.info("invalid_time", "y,m,d,min,s");
            return;
        }

        seconds = (seconds - System.currentTimeMillis()) / 1000;

        if (seconds <= 0) {
            context.info("command.number_negative");
            return;
        }

        String[] splitted = raw.split("=");
        String fieldString = splitted[0];

        try {

            Field field = userClass.getDeclaredField(fieldString);

            BukkitFrame bukkitFrame = getPlugin().getBukkitFrame();

            if (!bukkitFrame.getAdapterMap().containsKey(field.getType()))
                return;

            Object val = bukkitFrame.getAdapterMap().get(field.getType()).convert(splitted[1]);

            if (val == null) {
                System.out.println("Value is null! Tried to parse: [" + splitted[1] + "]");
                context.info("command.variable.failed_to_convert");
                return;
            }

            int affected = 0;

            field.setAccessible(true);

            for (User user : getPlugin().getUserStorage().getUsers()) {

                if (!user.isOnline())
                    continue;

                if (field.get(user).equals(val)) {
                    affected++;
                    user.getPlayer().addPotionEffect(new PotionEffect(potionEffectType, (((int) seconds) * 20), amplifier), true);
                }
            }

            context.info("command.stormsurge.apply_successful", potionEffectType.getName().toLowerCase(), amplifier, seconds, affected, field.getName() + "=" + val);
        } catch (Exception e) {
            if (e instanceof NoSuchFieldException)
                context.sendMessage("§cNão foi encontrado nenhum campo com este nome.");
            else {
                e.printStackTrace();
                context.info("command.variable.failed_to_convert");
            }
        }
    }

    @Completer(name = "stormserge")
    public List<String> handleComplete(Context<CommandSender> context) {

        if (context.argsCount() == 1)
            return Arrays.stream(PotionEffectType.values()).map(c -> c.getClass().getName().toLowerCase()).filter(name -> startsWith(name, context.getArg(0))).collect(Collectors.toList());
        else if (context.argsCount() == 2)
            return Stream.of("15s", "30s", "1m", "3m", "5m", "10m", "30m", "1h").filter(c -> startsWith(c, context.getArg(1))).collect(Collectors.toList());
        else if (context.argsCount() == 4) {

            List<String> response = new ArrayList<>();
            Class<User> userClass = User.class;

            for (Field field : userClass.getDeclaredFields()) {

                if ((field.getModifiers() & Modifier.TRANSIENT) != 0)
                    continue;

                if (startsWith(field.getName(), context.getArg(3)))
                    response.add(field.getName() + "=");
            }

            return response;
        }
        return Collections.emptyList();
    }
}
