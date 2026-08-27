package org.eablo;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ModConfigClient {

    public static void openGui() {
        boolean initialLocateo = ModConfig.locateoEnabled;
        boolean initialLocateonly = ModConfig.locateonlyEnabled;
        boolean initialLo = ModConfig.loEnabled;
        
        Minecraft.getInstance().execute(() -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setTitle(Component.translatable("locateo.config.title"))
                    .setSavingRunnable(() -> {
                        boolean changed = ModConfig.locateoEnabled != initialLocateo ||
                                          ModConfig.locateonlyEnabled != initialLocateonly ||
                                          ModConfig.loEnabled != initialLo;
                        
                        if (changed) {
                            ModConfig.save();
                            ModConfig.saveOriginalValues();
                            ModConfig.reloadCommands();
                            
                            if (Minecraft.getInstance().player != null) {
                                Minecraft.getInstance().player.sendSystemMessage(Component.translatable("locateo.config.saved_and_reloaded"));
                            }
                        } else {
                            if (Minecraft.getInstance().player != null) {
                                Minecraft.getInstance().player.sendSystemMessage(Component.translatable("locateo.config.no_changes"));
                            }
                            LocateOnly.LOGGER.info("GUI config saved with no changes, reload skipped");
                        }
                    });

            ConfigCategory general = builder.getOrCreateCategory(Component.translatable("locateo.config.category.general"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("locateo.config.command.locateo"), ModConfig.locateoEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> ModConfig.locateoEnabled = newValue)
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("locateo.config.command.locateonly"), ModConfig.locateonlyEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> ModConfig.locateonlyEnabled = newValue)
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("locateo.config.command.lo"), ModConfig.loEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> ModConfig.loEnabled = newValue)
                    .build());

            // 适配 26.2 版本的 GUI 调用方式
            Minecraft.getInstance().gui.setScreen(builder.build());
        });
    }
}