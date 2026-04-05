package com.minecraft.savagemc.core.bukkit.api.permission.injector;

import com.minecraft.savagemc.core.bukkit.api.permission.injector.loaders.*;
import lombok.val;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;

public class RegExpMatcher implements PermissionMatcher {

    private final Pattern rangeExpression = Pattern.compile("(\\d+)-(\\d+)");

    public boolean isMatches(String expression, String permission) {
        try {
            if (rangeExpression.matcher(expression).matches()) {
                String[] parts = expression.split("-");
                int start = Integer.parseInt(parts[0]);
                int end = Integer.parseInt(parts[1]);
                
                String[] permParts = permission.split("\\.");
                if (permParts.length > 0) {
                    try {
                        int permNum = Integer.parseInt(permParts[permParts.length - 1]);
                        return permNum >= start && permNum <= end;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
