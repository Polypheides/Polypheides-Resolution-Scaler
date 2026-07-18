package com.polypheides.resolutionscaler.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    private RenderTarget mainRenderTarget;

    /**
     * Prevents resize() from firing every frame.
     * Since our mainRenderTarget width/height is scaled, it no longer matches the raw windowRenderState,
     * causing Vanilla to trigger a resize every tick.
     * We cache the last raw window dimensions and only resize when the window actually changes.
     */
    private int lastResizedForWidth = -1;
    private int lastResizedForHeight = -1;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resize(II)V"))
    private void redirectResizeInRender(GameRenderer instance, int width, int height) {
        if (width != lastResizedForWidth || height != lastResizedForHeight) {
            lastResizedForWidth = width;
            lastResizedForHeight = height;
            instance.resize(width, height);
        }
    }
}
