package com.polypheides.resolutionscaler.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SkyRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SkyRenderer.class)
public class SkyRendererMixin {

    /**
     * Redirects internal field accesses to `this.renderTarget` inside SkyRenderer
     * to ensure it always renders into the currently active main render target.
     */
    @Redirect(method = {
            "renderSkyDisc",
            "renderDarkDisc",
            "renderSun",
            "renderMoon",
            "renderStars",
            "renderSunriseAndSunset",
            "renderEndSky",
            "renderEndFlash"
    }, at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;", opcode = org.objectweb.asm.Opcodes.GETFIELD))
    private RenderTarget redirectGetRenderTarget(SkyRenderer instance) {
        return Minecraft.getInstance().gameRenderer.mainRenderTarget();
    }
}
