package com.polypheides.resolutionscaler.mixin;

import com.polypheides.resolutionscaler.ResolutionScaler;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VideoSettingsScreen.class)
public class VideoSettingsScreenMixin {

    @Inject(method = "displayOptions", at = @At("RETURN"), cancellable = true)
    private static void injectResolutionOption(Options options, CallbackInfoReturnable<OptionInstance<?>[]> cir) {
        OptionInstance<?>[] original = cir.getReturnValue();
        if (original != null) {
            OptionInstance<?>[] modified = new OptionInstance<?>[original.length + 1];
            System.arraycopy(original, 0, modified, 0, original.length);
            modified[original.length] = ResolutionScaler.RESOLUTION_OPTION;
            cir.setReturnValue(modified);
        }
    }
}
