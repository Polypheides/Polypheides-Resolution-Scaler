package com.polypheides.resolutionscaler.mixin;

import com.polypheides.resolutionscaler.ResolutionScaler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlobalSettingsUniform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GlobalSettingsUniform.class)
public class GlobalSettingsUniformMixin {

    @ModifyVariable(method = "update", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private int modifyWidth(int width) {
        if (ResolutionScaler.scaledRenderTarget != null) {
            return Minecraft.getInstance().getWindow().getWidth();
        }
        return width;
    }

    @ModifyVariable(method = "update", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private int modifyHeight(int height) {
        if (ResolutionScaler.scaledRenderTarget != null) {
            return Minecraft.getInstance().getWindow().getHeight();
        }
        return height;
    }
}
