package com.minecraft.savagemc.core.util.level;

public class LevelCalculator {

    /**
     * Calcula o XP necessário para subir de um nível específico para o próximo
     * 
     * @param currentLevel O nível atual do jogador
     * @return XP necessário para o próximo nível
     */
    public static int getXpForNextLevel(int currentLevel) {
        if (currentLevel < 10) {
            return 500;
        } else if (currentLevel < 20) {
            return 1000;
        } else if (currentLevel < 30) {
            return 2000;
        } else if (currentLevel < 40) {
            return 3000;
        } else if (currentLevel < 50) {
            return 5000;
        } else {
            // A partir do nível 50, aumenta 1k a cada 10 níveis
            int bracket = (currentLevel / 10) - 4; // 50-59 = bracket 1, 60-69 = bracket 2, etc
            return 5000 + (bracket * 1000);
        }
    }

    /**
     * Calcula o XP total necessário para alcançar um determinado nível
     * 
     * @param targetLevel O nível alvo
     * @return XP total necessário
     */
    public static long getTotalXpForLevel(int targetLevel) {
        long totalXp = 0;
        
        for (int level = 0; level < targetLevel; level++) {
            totalXp += getXpForNextLevel(level);
        }
        
        return totalXp;
    }

    /**
     * Calcula o nível baseado no XP total
     * 
     * @param totalXp XP total do jogador
     * @return O nível atual do jogador
     */
    public static int getLevelFromXp(long totalXp) {
        int level = 0;
        long xpAccumulated = 0;
        
        while (xpAccumulated + getXpForNextLevel(level) <= totalXp) {
            xpAccumulated += getXpForNextLevel(level);
            level++;
        }
        
        return level;
    }

    /**
     * Calcula o XP atual dentro do nível (progresso para o próximo nível)
     * 
     * @param totalXp XP total do jogador
     * @return XP atual no nível
     */
    public static int getCurrentLevelXp(long totalXp) {
        int level = getLevelFromXp(totalXp);
        long xpForCurrentLevel = getTotalXpForLevel(level);
        
        return (int) (totalXp - xpForCurrentLevel);
    }

    /**
     * Calcula a porcentagem de progresso no nível atual
     * 
     * @param totalXp XP total do jogador
     * @return Porcentagem de 0 a 100
     */
    public static double getLevelProgress(long totalXp) {
        int level = getLevelFromXp(totalXp);
        int currentXp = getCurrentLevelXp(totalXp);
        int xpNeeded = getXpForNextLevel(level);
        
        return (currentXp * 100.0) / xpNeeded;
    }

    /**
     * Formata o nível com cor baseado na faixa
     * 
     * @param level O nível do jogador
     * @return String formatada com cor
     */
    public static String getFormattedLevel(int level) {
        if (level < 10) {
            return "§7[" + level + "§7]"; // Cinza
        } else if (level < 20) {
            return "§f[" + level + "§f]"; // Branco
        } else if (level < 30) {
            return "§a[" + level + "§a]"; // Verde
        } else if (level < 40) {
            return "§b[" + level + "§b]"; // Azul claro
        } else if (level < 50) {
            return "§e[" + level + "§e]"; // Amarelo
        } else if (level < 60) {
            return "§6[" + level + "§6]"; // Laranja
        } else if (level < 70) {
            return "§c[" + level + "§c]"; // Vermelho
        } else if (level < 80) {
            return "§5[" + level + "§5]"; // Roxo
        } else if (level < 90) {
            return "§d[" + level + "§d]"; // Rosa
        } else if (level < 100) {
            return "§9[" + level + "§9]"; // Azul escuro
        } else {
            return "§4[" + level + "§4]"; // Vermelho escuro
        }
    }

    /**
     * Formata a barra de progresso visual
     * 
     * @param totalXp XP total do jogador
     * @param barLength Tamanho da barra
     * @return String com a barra de progresso
     */
    public static String getProgressBar(long totalXp, int barLength) {
        double progress = getLevelProgress(totalXp);
        int filled = (int) ((progress / 100.0) * barLength);
        
        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append("█");
            } else if (i == filled) {
                bar.append("§7");
            }
            if (i >= filled) {
                bar.append("█");
            }
        }
        
        return bar.toString();
    }
}
