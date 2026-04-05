/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.util.templatekit;

import java.util.HashSet;
import java.util.Set;

public class TemplateKitStorage {

    private static final Set<TemplateKit> templateKits = new HashSet<>();

    public static TemplateKit registerTemplateKit(TemplateKit templateKit) {
        templateKits.add(templateKit);
        return templateKit;
    }

    public static void deleteTemplateKit(TemplateKit templateKit) {
        templateKits.remove(templateKit);
    }

    public static Set<TemplateKit> getTemplateKits() {
        return templateKits;
    }

    public static TemplateKit getTemplateKit(String name) {
        return templateKits.stream().filter(c -> c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static int clear() {
        int returnInt = templateKits.size();
        templateKits.clear();
        return returnInt;
    }
}
