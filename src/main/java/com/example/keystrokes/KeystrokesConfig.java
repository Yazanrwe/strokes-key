package com.example.keystrokes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class KeystrokesConfig {
	public int x = 10;
	public int y = 10;
	public float scale = 1.0f;

	private static KeystrokesConfig inst;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("keystrokes.json");

	public static KeystrokesConfig get() {
		if (inst == null) load();
		return inst;
	}

	public static void load() {
		try {
			if (Files.exists(FILE)) inst = GSON.fromJson(Files.readString(FILE), KeystrokesConfig.class);
		} catch (Exception ignored) {}
		if (inst == null) inst = new KeystrokesConfig();
	}

	public static void save() {
		try {
			Files.writeString(FILE, GSON.toJson(get()));
		} catch (IOException ignored) {}
	}
}
