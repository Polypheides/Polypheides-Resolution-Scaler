package com.polypheides.resolutionscaler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
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

    public static int scale(int value, double scaleFactor) {
        return (int) Math.round(value * scaleFactor);
    }

    public static int scale(int value) {
        return scale(value, SCALE);
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
                    Minecraft client = Minecraft.getInstance();
                    if (client != null && client.getWindow() != null && client.gameRenderer != null) {
                        client.gameRenderer.resize(client.getWindow().getWidth(), client.getWindow().getHeight());
                    }
                });

        System.out.println("Polypheides Resolution Scaler initialized with SCALE: " + SCALE);

        // HACK: Force a full render pipeline re-init on the first client tick.
        // This fixes broken 3D scaling when launching in windowed/non-maximized mode.
        boolean[] needsNudge = { true };
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (needsNudge[0] && client.gameRenderer != null && client.getWindow() != null) {
                needsNudge[0] = false;
                float saved = SCALE;
                // Nudge: +1% normally, -1% if at max (200%)
                SCALE = saved >= 2.0f ? saved - 0.01f : saved + 0.01f;
                client.gameRenderer.resize(client.getWindow().getWidth(), client.getWindow().getHeight());
                // Restore saved value
                SCALE = saved;
                client.gameRenderer.resize(client.getWindow().getWidth(), client.getWindow().getHeight());
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
