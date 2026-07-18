package com.polypheides.resolutionscaler.mixin;

import com.mojang.renderpearl.backend.opengl.DirectStateAccess;
import com.polypheides.resolutionscaler.ResolutionScaler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// GlCommandEncoder is package-private, so we use string targets.
@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlCommandEncoder")
public class GlCommandEncoderMixin {

    /**
     * Redirect the blitFrameBuffers call inside presentTexture to use the full
     * scaled render target as source and the full swapchain as destination.
     *
     * Vanilla computes:
     * copyWidth = Math.min(swapchainWidth, textureWidth) — clips supersampled
     * textures
     * copyHeight = Math.min(swapchainHeight, textureHeight) — clips supersampled
     * textures
     * blit src: (0,0) → (copyWidth, copyHeight)
     * blit dst: (0, destY) → (copyWidth, copyHeight + destY)
     *
     * We override to:
     * blit src: (0,0) → (scaledWidth, scaledHeight) — full scaled render target
     * blit dst: (0,0) → (swapchainWidth, swapchainHeight) — full screen
     * filter: GL_LINEAR (9729) instead of GL_NEAREST (9728) for smooth scaling
     */
    @Redirect(
        method = "presentTexture",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/renderpearl/backend/opengl/DirectStateAccess;blitFrameBuffers(IIIIIIIIIIII)V"
        )
    )
    private void redirectBlitFrameBuffers(
            DirectStateAccess dsa,
            int source, int dest,
            int srcX0, int srcY0, int srcX1, int srcY1,
            int dstX0, int dstY0, int dstX1, int dstY1,
            int mask, int filter) {
        // Calculate scaled source dimensions and exact swapchain destination dimensions.
        // We override Vanilla's scaling boundaries which clip scaled render targets.

        int swapW = dstX1;
        int swapH = dstY1 - dstY0;
        int scaledWidth = srcX1;
        int scaledHeight = srcY1;

        // The most reliable way to get the true dimensions is from the window directly
        net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
        if (client != null && client.getWindow() != null) {
            swapW = client.getWindow().getWidth();
            swapH = client.getWindow().getHeight();
            scaledWidth = ResolutionScaler.scale(swapW);
            scaledHeight = ResolutionScaler.scale(swapH);
        }

        // Blit from full scaled render target to full swapchain with linear filtering
        dsa.blitFrameBuffers(
                source, dest,
                0, 0, scaledWidth, scaledHeight, // src: full scaled render target
                0, 0, swapW, swapH, // dst: full swapchain (no Y offset)
                mask,
                9729 // GL_LINEAR for smooth scaling
        );
    }
}
