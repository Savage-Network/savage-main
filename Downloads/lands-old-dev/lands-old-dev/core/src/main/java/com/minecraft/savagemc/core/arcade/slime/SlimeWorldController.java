package com.minecraft.savagemc.core.arcade.slime;

import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.util.list.FileUtil;
import com.minecraft.savagemc.core.util.list.Validator;
import com.minecraft.savagemc.core.util.list.WorldUtil;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import com.grinderwolf.swm.plugin.SWMPlugin;
import com.grinderwolf.swm.plugin.config.ConfigManager;
import com.grinderwolf.swm.plugin.config.WorldData;
import com.grinderwolf.swm.plugin.config.WorldsConfig;
import lombok.SneakyThrows;
import org.bukkit.*;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class SlimeWorldController {

    private static final Map<String, SlimeWorld> templateCache = new ConcurrentHashMap<>();

    public static void cloneWorldFromTemplate(SWMPlugin plugin, String templateWorld, String worldName) {
        cloneWorldFromTemplate(plugin, templateWorld, worldName, () -> {
        });
    }

    public static void cloneWorldFromTemplate(SWMPlugin plugin, String template, String name, SlimeAction afterGenerate) {
        SlimeLoader loader = plugin.getLoader(Constant.SLIME_FILE_LOADER);

        if (loader == null) {
            plugin.Savage.Logger().info("Loader \"" + Constant.SLIME_FILE_LOADER + "\" não encontrado.");
            return;
        }

        WorldsConfig config = ConfigManager.getWorldConfig();

        WorldData worldData = config.getWorlds().get(template);

        if (worldData == null) {
            plugin.Savage.Logger().info("Mundo \"" + template + "\" não encontrado.");
            return;
        }

        if (template.equalsIgnoreCase(name)) {
            plugin.Savage.Logger().info("Os mundos não podem ter os nomes iguais. (" + template + "/" + name + ")");
            return;
        }

        // Gerar mundo
        plugin.Savage.Logger().info("Gerando mundo \"" + name + "\" de \"" + template + "\"...");

        generateWorld(plugin, loader, template, name, afterGenerate, 3); // Tente gerar o mundo até 3 vezes
    }

    private static void generateWorld(SWMPlugin plugin, SlimeLoader loader, String template, String name, SlimeAction afterGenerate, int attempts) {
        if (attempts <= 0) {
            plugin.Savage.Logger().info("Não foi possível gerar o mundo \"" + name + "\" após " + attempts + " tentativas.");
            return;
        }

        try {
            Instant start = Instant.now();

            /* Configurando Mapa */
            SlimeWorld templateWorld = templateCache.get(template);

            if (templateWorld == null) {
                // Configurando propriedades
                SlimePropertyMap properties = new SlimePropertyMap();

                properties.setString(SlimeProperties.WORLD_TYPE, "flat");
                properties.setString(SlimeProperties.DIFFICULTY, "normal");

                properties.setBoolean(SlimeProperties.ALLOW_ANIMALS, false);
                properties.setBoolean(SlimeProperties.ALLOW_MONSTERS, false);

                templateWorld = plugin.loadWorld(loader, template, true, properties);

                if (templateWorld == null) {
                    plugin.Savage.Logger().info("O template \"" + template + "\" não foi encontrado.");
                    return;
                }

                templateCache.put(template, templateWorld);
            }

            SlimeWorld slimeWorld = templateWorld.clone(name, loader);

            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    plugin.generateWorld(slimeWorld);

                    World bukkitWorld = Bukkit.getWorld(slimeWorld.getName());

                    if (bukkitWorld != null) {
                        // Verifica se o mundo foi carregado com sucesso
                        if (bukkitWorld.getBlockAt(0, 0, 0) != null) { // Verifica se o bloco (0, 0, 0) existe
                            // Localizações estão consistentes, execute a ação
                            WorldUtil.setup(bukkitWorld);

                            afterGenerate.run();

                            plugin.Savage.Logger().info("Mundo \"" + name + "\" carregado e gerado em "
                                    + Duration.between(start, Instant.now()).toMillis() + "ms.");
                        } else {
                            plugin.Savage.Logger().info("Não foi possível executar a ação do mundo " + name);
                            generateWorld(plugin, loader, template, name, afterGenerate, attempts - 1); // Tente novamente
                        }
                    } else {
                        plugin.Savage.Logger().info("Não foi possível executar a ação do mundo " + name);
                        generateWorld(plugin, loader, template, name, afterGenerate, attempts - 1); // Tente novamente
                    }

                } catch (Exception e) {
                    plugin.Savage.Logger().log(Level.WARNING, "Não foi possível gerar o mundo \"" + name + "\" (1x)", e);
                    generateWorld(plugin, loader, template, name, afterGenerate, attempts - 1); // Tente novamente
                }
            });
        } catch (Exception e) {
            plugin.Savage.Logger().log(Level.WARNING, "Não foi possível gerar o mundo \"" + name + "\" (2x)", e);
            generateWorld(plugin, loader, template, name, afterGenerate, attempts - 1); // Tente novamente
        }
    }

    @SneakyThrows
    public static void removeWorldsFromLoader(String loaderName) {
        SlimeLoader loader = SWMPlugin.getInstance().getLoader(loaderName);

        if (loader == null) {
            Savage.Savage.Logger().info("Loader não encontrado!");
            return;
        }

        try {
            loader.listWorlds().forEach(world -> {
                try {
                    loader.deleteWorld(world);
                } catch (Exception e) {
                    Savage.Savage.Logger().log(Level.WARNING, "Não foi possível deletar o mundo \"" + world + "\".", e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public static void removeWorldsFromFileLoader() {
        SlimeLoader loader = SWMPlugin.getInstance().getLoader(Constant.SLIME_FILE_LOADER);

        if (loader == null) {
            Savage.Savage.Logger().info("Loader não encontrado!");
            return;
        }

        try {
            for (String worldName : loader.listWorlds()) {
                if (!Validator.hasNumberInString(worldName)) continue;

                World world = Bukkit.getWorld(worldName);

                if (world != null) {
                    Bukkit.unloadWorld(world, false);
                    FileUtil.delete(world.getWorldFolder());
                }

                loader.deleteWorld(worldName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unloadWorld(World world) {
        unloadWorld(world, () -> {
        });
    }

    public static void unloadWorld(World world, SlimeAction afterUnloadWorld) {
        SlimeLoader loader = SWMPlugin.getInstance().getLoader(Constant.SLIME_FILE_LOADER);

        if (loader == null) {
            Savage.Savage.Logger().log(Level.WARNING, "Não foi possível encontrar o loader \"Hikari\".");
            return;
        }

        if (world == null) return;

        String worldName = world.getName();
        File file = world.getWorldFolder();

        try {
            Bukkit.unloadWorld(worldName, false);
            FileUtil.delete(file);

            loader.deleteWorld(worldName);

        } catch (Exception e) {
            Savage.Savage.Logger().log(Level.WARNING, "Ocorreu um erro ao encerrar o mundo \"" + (worldName) + "\"...", e);
        }

        if (Bukkit.getWorld(worldName) == null) {
            Savage.Savage.Logger().info("[SlimeWorld] Mundo \"" + worldName + "\" deletado! Executando ação...");

            afterUnloadWorld.run();
        } else {
            Savage.Savage.Logger().info("[SlimeWorld] Não foi possível deletar o mundo \"" + worldName + "\".");
        }
    }

    public interface SlimeAction {
        void run();
    }
}
