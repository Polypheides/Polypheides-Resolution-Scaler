package com.polypheides.resolutionscaler.mixin;

import com.polypheides.resolutionscaler.ResolutionScaler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;


@Mixin(targets = "com.mojang.renderpearl.frontend.FrontendRenderPass")
public class FrontendRenderPassMixin {

    @org.spongepowered.asm.mixin.Shadow(remap = false)
    private com.mojang.renderpearl.api.commands.RenderPass.RenderArea renderArea;

    private boolean isMainTarget() {
        if (this.renderArea == null)
            return false;
        net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
        if (client == null || client.gameRenderer == null)
            return false;

        return this.renderArea.width() == client.gameRenderer.mainRenderTarget().width &&
                this.renderArea.height() == client.gameRenderer.mainRenderTarget().height;
    }

    @ModifyVariable(method = "enableScissor(IIII)V", at = @At("HEAD"), ordinal = 0, argsOnly = true, remap = false)
    private int modifyScissorX(int x) {
        return isMainTarget() ? (int) Math.round(x * ResolutionScaler.SCALE) : x;
    }

    @ModifyVariable(method = "enableScissor(IIII)V", at = @At("HEAD"), ordinal = 1, argsOnly = true, remap = false)
    private int modifyScissorY(int y) {
        return isMainTarget() ? (int) Math.round(y * ResolutionScaler.SCALE) : y;
    }

    @ModifyVariable(method = "enableScissor(IIII)V", at = @At("HEAD"), ordinal = 2, argsOnly = true, remap = false)
    private int modifyScissorWidth(int width) {
        return isMainTarget() ? (int) Math.round(width * ResolutionScaler.SCALE) : width;
    }

    @ModifyVariable(method = "enableScissor(IIII)V", at = @At("HEAD"), ordinal = 3, argsOnly = true, remap = false)
    private int modifyScissorHeight(int height) {
        return isMainTarget() ? (int) Math.round(height * ResolutionScaler.SCALE) : height;
    }

    @org.spongepowered.asm.mixin.injection.Redirect(method = "enableScissor(IIII)V", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass$RenderArea;width()I"), remap = false)
    private int redirectRenderAreaWidth(com.mojang.renderpearl.api.commands.RenderPass.RenderArea area) {
        return isMainTarget() ? Integer.MAX_VALUE : area.width();
    }

    @org.spongepowered.asm.mixin.injection.Redirect(method = "enableScissor(IIII)V", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass$RenderArea;height()I"), remap = false)
    private int redirectRenderAreaHeight(com.mojang.renderpearl.api.commands.RenderPass.RenderArea area) {
        return isMainTarget() ? Integer.MAX_VALUE : area.height();
    }
}
