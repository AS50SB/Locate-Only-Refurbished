package org.eablo;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ModConfig {
    // 动态获取 Fabric 配置目录
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("locateo.properties");
    private static final Properties properties = new Properties();
    
    public static boolean locateoEnabled = true;
    public static boolean locateonlyEnabled = true;
    public static boolean loEnabled = true;
    
    public static boolean originalLocateoEnabled = true;
    public static boolean originalLocateonlyEnabled = true;
    public static boolean originalLoEnabled = true;
    
    private static MinecraftServer serverInstance = null;

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
                properties.load(in);
                locateoEnabled = Boolean.parseBoolean(properties.getProperty("locateo.enabled", "true"));
                locateonlyEnabled = Boolean.parseBoolean(properties.getProperty("locateonly.enabled", "true"));
                loEnabled = Boolean.parseBoolean(properties.getProperty("lo.enabled", "true"));
            } catch (IOException e) {
                LocateOnly.LOGGER.error("Failed to load config", e);
            }
        } else {
            save();
        }
        saveOriginalValues();
    }

    public static void saveOriginalValues() {
        originalLocateoEnabled = locateoEnabled;
        originalLocateonlyEnabled = locateonlyEnabled;
        originalLoEnabled = loEnabled;
    }

    public static void save() {
        properties.setProperty("locateo.enabled", String.valueOf(locateoEnabled));
        properties.setProperty("locateonly.enabled", String.valueOf(locateonlyEnabled));
        properties.setProperty("lo.enabled", String.valueOf(loEnabled));
        try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
            properties.store(out, "LocateOnly Mod Configuration");
        } catch (IOException e) {
            LocateOnly.LOGGER.error("Failed to save config", e);
        }
    }

    public static boolean isCommandEnabled(String commandName) {
        return switch (commandName) {
            case "locateo" -> locateoEnabled;
            case "locateonly" -> locateonlyEnabled;
            case "lo" -> loEnabled;
            default -> true;
        };
    }

    public static void setCommandEnabled(String commandName, boolean enabled) {
        boolean currentValue = isCommandEnabled(commandName);
        if (currentValue == enabled) {
            LocateOnly.LOGGER.info("Command {} is already {}", commandName, enabled);
            return;
        }
        
        switch (commandName) {
            case "locateo" -> locateoEnabled = enabled;
            case "locateonly" -> locateonlyEnabled = enabled;
            case "lo" -> loEnabled = enabled;
        }
        save();
        saveOriginalValues();
        reloadCommands();
    }

    public static void reloadCommands() {
        if (serverInstance == null) {
            serverInstance = LocateOnly.getServer();
        }
        
        if (serverInstance == null) {
            LocateOnly.LOGGER.warn("No server instance available, cannot reload");
            return;
        }
        
        LocateOnly.LOGGER.info("Configuration changed, queuing command reload...");
        
        // 必须在服务端主线程执行重载任务
        serverInstance.execute(() -> {
            try {
                var consoleSource = serverInstance.createCommandSourceStack().withSuppressedOutput();
                serverInstance.getCommands().performPrefixedCommand(consoleSource, "reload");
                LocateOnly.LOGGER.debug("Reload command executed silently on main thread");
            } catch (Exception e) {
                LocateOnly.LOGGER.error("Failed to reload: {}", e.getMessage());
            }
        });
    }

    public static void setServerInstance(MinecraftServer server) {
        serverInstance = server;
        if (server != null) {
            LocateOnly.LOGGER.info("Server instance cached for reload");
        }
    }
}