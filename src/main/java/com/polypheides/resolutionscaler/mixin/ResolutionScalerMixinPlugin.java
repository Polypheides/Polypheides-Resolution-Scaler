package com.polypheides.resolutionscaler.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ResolutionScalerMixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null; // default
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("GlCommandEncoderMixin")) {
            // Instead of Class.forName which can fail in Mixin plugins due to early
            // classloading,
            // we safely check if the .class file exists in the classpath resources.
            boolean hasOpenGL = this.getClass()
                    .getResource("/com/mojang/renderpearl/backend/opengl/GlCommandEncoder.class") != null;
            if (!hasOpenGL) {
                System.out.println(
                        "[Resolution Scaler] OpenGL engine (RenderPearl) not found. Disabling GlCommandEncoderMixin.");
                return false;
            }
            return true;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
