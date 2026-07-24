package com.polypheides.resolutionscaler.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.FilterMode;
import com.polypheides.resolutionscaler.ResolutionScaler;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.OptionalDouble;

@SuppressWarnings("null")
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

            RenderSystem.assertOnRenderThread();
            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
                    this.mainRenderTarget.getColorTexture(),
                    ((GameRenderer) (Object) this).gameRenderState().guiRenderState.clearColorOverride,
                    this.mainRenderTarget.getDepthTexture(),
                    0.0);
        }
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void onRenderLevelReturn(CallbackInfo ci) {
        if (ResolutionScaler.scaledRenderTarget != null && this.nativeTarget != null) {
            // Restore the native high-res target for the UI
            this.mainRenderTarget = this.nativeTarget;

            // Opaque blit from scaled target → native target using TRACY_BLIT (no alpha
            // blending)
            RenderSystem.assertOnRenderThread();
            try (RenderPass renderPass = RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(
                            () -> "Blit scaled resolution",
                            this.mainRenderTarget.getColorTextureView(),
                            Optional.empty(),
                            this.mainRenderTarget.getDepthTextureView(),
                            OptionalDouble.empty())) {
                renderPass.setPipeline(RenderPipelines.TRACY_BLIT);
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.bindTexture("InSampler",
                        ResolutionScaler.scaledRenderTarget.getColorTextureView(),
                        RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
                renderPass.draw(3, 1, 0, 0);
            }
        }
    }
}
