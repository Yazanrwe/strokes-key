package com.example.keystrokes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeystrokesMod implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		KeystrokesConfig.load();

		KeyBinding open = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.keystrokes.edit", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.keystrokes"));

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (open.wasPressed()) {
				if (mc.currentScreen == null) mc.setScreen(new EditScreen());
			}
		});

		HudRenderCallback.EVENT.register((ctx, tick) -> {
			if (!MinecraftClient.getInstance().options.hudHidden) KeystrokesRenderer.render(ctx);
		});
	}
}
