package com.polypheides.resolutionscaler.integration;

import com.polypheides.resolutionscaler.ResolutionScaler;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class PrsSodiumConfig implements ConfigEntryPoint {

    @Override
    public void registerConfigEarly(ConfigBuilder builder) {
        // No-op
    }

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        var page = builder.createOptionPage().setName(Component.literal("Scale"));

        page.addOptionGroup(builder.createOptionGroup()
            .addOption(
                builder.createIntegerOption(Identifier.parse("resolutionscaler:scale"))
                    .setName(Component.literal("Resolution Scale"))
                    .setTooltip(Component.literal("Adjust the resolution scaling (render target percentage). Lower values dramatically improve performance at the cost of visual quality."))
                    .setRange(10, 200, 10)
                    .setDefaultValue(100)
                    .setImpact(OptionImpact.HIGH)
                    .setStorageHandler(ResolutionScaler::saveConfig)
                    .setValueFormatter((value) -> {
                        int w = 1920;
                        int h = 1080;
                        net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
                        if (client != null && client.getWindow() != null) {
                            w = client.getWindow().getWidth();
                            h = client.getWindow().getHeight();
                        }
                        return Component.literal(value + "% (" + ResolutionScaler.scale(w, value / 100.0) + "x" + ResolutionScaler.scale(h, value / 100.0) + ")");
                    })
                    .setBinding(
                        // Setter
                        (value) -> {
                            ResolutionScaler.SCALE = value / 100.0f;
                            ResolutionScaler.saveConfig();
                            ResolutionScaler.resizeTarget();
                        },
                        // Getter
                        () -> (int) (ResolutionScaler.SCALE * 100)
                    )
            )
        );

        builder.registerOwnModOptions()
               .setName("PRS")
               .addPage(page);
    }
}
