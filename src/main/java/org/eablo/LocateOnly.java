package org.eablo;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocateOnly implements ModInitializer {
    public static final String MOD_ID = "locateo";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static MinecraftServer serverInstance = null;

    @Override
    public void onInitialize() {
        LOGGER.info("LocateOnly mod initializing for Minecraft 26.2...");
        ModConfig.load();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            serverInstance = server;
            ModConfig.setServerInstance(server);
            LOGGER.info("Server instance cached for reload");
        });

        // 服务器停止时清理缓存，防止内存泄漏
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            serverInstance = null;
            ModConfig.setServerInstance(null);
            LOGGER.info("Server instance cleared");
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LocateOnlyCommands.register(dispatcher);
        });

        LOGGER.info("LocateOnly mod initialization complete.");
    }
    
    public static MinecraftServer getServer() {
        return serverInstance;
    }
}