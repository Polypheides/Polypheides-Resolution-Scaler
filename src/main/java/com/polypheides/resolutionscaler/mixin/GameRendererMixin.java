package com.polypheides.resolutionscaler.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.polypheides.resolutionscaler.ResolutionScaler;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Mutable
    @Shadow
    private RenderTarget mainRenderTarget;

    private RenderTarget nativeTarget;

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void onRenderLevelHead(CallbackInfo ci) {
        if (ResolutionScaler.scaledRenderTarget != null) {
            this.nativeTarget = this.mainRenderTarget;
            this.mainRenderTarget = ResolutionScaler.scaledRenderTarget;
        }
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void onRenderLevelReturn(CallbackInfo ci) {
        if (ResolutionScaler.scaledRenderTarget != null && this.nativeTarget != null) {
            // Restore the native high-res target for the UI
            this.mainRenderTarget = this.nativeTarget;

            // In Minecraft 26.3, blitAndBlendToTexture(dstColor, dstDepth) blits THIS
            // texture to the arguments.
            // So we call it on the scaled target, passing the native target's textures as
            // the destination!
            ResolutionScaler.scaledRenderTarget.blitAndBlendToTexture(
                    java.util.Objects.requireNonNull(this.mainRenderTarget.getColorTextureView()),
                    java.util.Objects.requireNonNull(this.mainRenderTarget.getDepthTextureView()));
        }
    }
}
