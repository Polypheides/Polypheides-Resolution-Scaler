package com.polypheides.resolutionscaler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ResolutionScaler implements ClientModInitializer {
    public static float SCALE = 1.0f;
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(),
            "resolutionscaler.json");
    private static final Gson GSON = new Gson();

    public static OptionInstance<Double> RESOLUTION_OPTION;

    // Our dedicated low-res canvas for the 3D world
    public static RenderTarget scaledRenderTarget;

    public static int scale(int value, double scaleFactor) {
        return Math.max(1, (int) Math.round(value * scaleFactor));
    }

    public static int scale(int value) {
        return scale(value, SCALE);
    }

    public static void resizeTarget() {
        Minecraft client = Minecraft.getInstance();
        if (client != null && client.getWindow() != null) {
            int w = client.getWindow().getWidth();
            int h = client.getWindow().getHeight();
            int scaledW = scale(w);
            int scaledH = scale(h);

            if (scaledRenderTarget == null && client.level != null) {
                scaledRenderTarget = new com.mojang.blaze3d.pipeline.TextureTarget(
                        "ResolutionScaler",
                        scaledW,
                        scaledH,
                        com.mojang.renderpearl.api.GpuFormat.RGBA8_UNORM,
                        com.mojang.renderpearl.api.GpuFormat.D24_UNORM_S8_UINT);
            } else if (scaledRenderTarget != null
                    && (scaledRenderTarget.width != scaledW || scaledRenderTarget.height != scaledH)) {
                // In modern versions, resize takes (width, height)
                scaledRenderTarget.resize(scaledW, scaledH);
            }
        }
    }

    @Override
    public void onInitializeClient() {
        loadConfig();

        RESOLUTION_OPTION = new OptionInstance<>(
                "Resolution Scale",
                OptionInstance.noTooltip(),
                (caption, value) -> {
                    int w = 1920;
                    int h = 1080;
                    Minecraft client = Minecraft.getInstance();
                    if (client != null && client.getWindow() != null) {
                        w = client.getWindow().getWidth();
                        h = client.getWindow().getHeight();
                    }
                    return Component.literal("Res: " + Math.round(value * 100.0) + "% (" + scale(w, value) + "x"
                            + scale(h, value) + ")");
                },
                new OptionInstance.IntRange(10, 200).xmap(v -> v / 100.0, v -> (int) (Math.round(v * 100.0)), true),
                java.util.Objects.requireNonNull(Codec.doubleRange(0.1, 2.0)),
                (double) SCALE,
                (newValue) -> {
                    SCALE = newValue.floatValue();
                    saveConfig();
                    if (Minecraft.getInstance().level != null) {
                        resizeTarget();
                    }
                });

        System.out.println("Polypheides Resolution Scaler initialized with SCALE: " + SCALE);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Ensure target exists, but only when we are actually in a world!
            if (scaledRenderTarget == null && client.level != null) {
                resizeTarget();
            }
        });
    }

    public static void loadConfig() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                com.google.gson.JsonElement element = com.google.gson.JsonParser.parseReader(reader);
                if (element != null && element.isJsonObject()) {
                    JsonObject json = element.getAsJsonObject();
                    if (json.has("scale")) {
                        SCALE = json.get("scale").getAsFloat();
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to load Resolution Scaler config: " + e.getMessage());
            }
        } else {
            saveConfig();
        }
    }

    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            JsonObject json = new JsonObject();
            json.addProperty("scale", SCALE);
            GSON.toJson(json, writer);
        } catch (Exception e) {
            System.err.println("Failed to save Resolution Scaler config: " + e.getMessage());
        }
    }
}
