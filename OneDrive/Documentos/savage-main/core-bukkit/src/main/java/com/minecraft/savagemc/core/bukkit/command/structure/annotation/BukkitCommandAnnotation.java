package com.minecraft.savagemc.core.bukkit.command.structure.annotation;

import com.minecraft.savagemc.core.bukkit.command.structure.annotation.CommandTarget;
import com.minecraft.savagemc.core.bukkit.command.structure.annotation.CommandType;
import com.minecraft.savagemc.core.bukkit.command.structure.annotation.CommandUsage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BukkitCommandAnnotation {
    String name();
    String[] aliases() default {};
    String description() default "";
    CommandTarget target();
    String usage();
    CommandType type();
    boolean async() default false;
}
