package com.polypheides.resolutionscaler.mixin;

import com.mojang.renderpearl.backend.vulkan.VulkanGpuSurface;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageBlit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VulkanGpuSurface.class)
public abstract class VulkanGpuSurfaceMixin {
    @Shadow private int swapchainWidth;
    @Shadow private int swapchainHeight;

    @Redirect(
        method = "blitFromTexture",
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/vulkan/VK12;vkCmdBlitImage(Lorg/lwjgl/vulkan/VkCommandBuffer;JIJILorg/lwjgl/vulkan/VkImageBlit$Buffer;I)V",
            remap = false
        )
    )
    private void redirectVkCmdBlitImage(VkCommandBuffer commandBuffer, long srcImage, int srcImageLayout, long dstImage, int dstImageLayout, VkImageBlit.Buffer pRegions, int filter) {
        // Override srcOffsets to the actual size of the scaled RenderTarget,
        // preventing Vanilla from clipping the source region when supersampling.
        int scaledWidth = com.polypheides.resolutionscaler.ResolutionScaler.scale(this.swapchainWidth);
        int scaledHeight = com.polypheides.resolutionscaler.ResolutionScaler.scale(this.swapchainHeight);
        pRegions.srcOffsets(1).x(scaledWidth);
        pRegions.srcOffsets(1).y(scaledHeight);

        // Minecraft flips the Y axis. We map the destination to the full unscaled swapchain.
        pRegions.dstOffsets(0).y(this.swapchainHeight);
        pRegions.dstOffsets(1).x(this.swapchainWidth);
        pRegions.dstOffsets(1).y(0);

        // We use VK_FILTER_LINEAR (1) instead of VK_FILTER_NEAREST (0) for smooth upscaling!
        VK12.vkCmdBlitImage(java.util.Objects.requireNonNull(commandBuffer), srcImage, srcImageLayout, dstImage, dstImageLayout, pRegions, 1);
    }
}
