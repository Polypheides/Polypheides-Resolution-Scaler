package com.polypheides.resolutionscaler.integration;

import com.polypheides.resolutionscaler.ResolutionScaler;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatterImpls;
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
                    .setValueFormatter(ControlValueFormatterImpls.percentage())
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
