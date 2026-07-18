package com.polypheides.resolutionscaler.mixin;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.polypheides.resolutionscaler.ResolutionScaler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(RenderTarget.class)
public class RenderTargetMixin {
    @ModifyVariable(method = "createBuffers", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private int modifyWidth(int width) {
        if ((Object) this instanceof MainTarget) {
            return ResolutionScaler.scale(width);
        }
        return width;
    }

    @ModifyVariable(method = "createBuffers", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private int modifyHeight(int height) {
        if ((Object) this instanceof MainTarget) {
            return ResolutionScaler.scale(height);
        }
        return height;
    }
}
