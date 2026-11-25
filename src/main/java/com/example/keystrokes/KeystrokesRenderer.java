package com.example.keystrokes;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class KeystrokesRenderer {
	// Unscaled size of the whole overlay (keyboard + mouse)
	public static final int WIDTH = 216;
	public static final int HEIGHT = 103;

	private static final int P = 21;   // key pitch
	private static final int G = 3;    // gap between keys
	private static final int K = P - G; // key height (18)

	// Colors (ARGB) - taken from the reference image
	private static final int BORDER = 0xFFB3E7FF;
	private static final int IDLE = 0xE600427F;
	private static final int PRESSED = 0xFF194BDC;
	private static final int MOUSE_FILL = 0xD005345E;
	private static final int TEXT = 0xFFFFFFFF;

	private record Key(String label, int row, float col, float w, int code) {}

	private static final List<Key> KEYS = new ArrayList<>();

	private static void row(int row, float start, String letters) {
		for (int i = 0; i < letters.length(); i++) {
			char c = letters.charAt(i);
			KEYS.add(new Key(String.valueOf(c), row, start + i, 1f, c)); // GLFW codes for 0-9/A-Z == ASCII
		}
	}

	static {
		KEYS.add(new Key("Esc", 0, 0f, 1f, GLFW.GLFW_KEY_ESCAPE));
		row(0, 1f, "12345");

		KEYS.add(new Key("Tab", 1, 0f, 1.5f, GLFW.GLFW_KEY_TAB));
		row(1, 1.5f, "QWERT");

		KEYS.add(new Key("Caps", 2, 0f, 1.85f, GLFW.GLFW_KEY_CAPS_LOCK));
		row(2, 1.85f, "ASDFG");

		KEYS.add(new Key("", 3, 0f, 2.2f, GLFW.GLFW_KEY_LEFT_SHIFT)); // shift icon drawn manually
		row(3, 2.2f, "ZXCVB");

		KEYS.add(new Key("^", 4, 0f, 1.85f, GLFW.GLFW_KEY_LEFT_CONTROL));
		KEYS.add(new Key("Alt", 4, 1.85f, 1.2f, GLFW.GLFW_KEY_LEFT_ALT));
		KEYS.add(new Key("", 4, 3.05f, 4.0f, GLFW.GLFW_KEY_SPACE));
	}

	public static void render(DrawContext ctx) {
		MinecraftClient mc = MinecraftClient.getInstance();
		KeystrokesConfig cfg = KeystrokesConfig.get();
		MatrixStack m = ctx.getMatrices();
		long win = mc.getWindow().getHandle();
		boolean active = mc.currentScreen == null || mc.currentScreen instanceof EditScreen;

		m.push();
		m.translate(cfg.x, cfg.y, 0);
		m.scale(cfg.scale, cfg.scale, 1f);

		for (Key k : KEYS) {
			boolean down = active && InputUtil.isKeyPressed(win, k.code);
			int x = Math.round(k.col * P);
			int y = k.row * P;
			int w = Math.round(k.w * P) - G;
			rbox(ctx, x, y, w, K, BORDER, down ? PRESSED : IDLE);
			if (k.code == GLFW.GLFW_KEY_LEFT_SHIFT) diamond(ctx, x + w / 2, y + K / 2);
			else if (!k.label.isEmpty()) label(ctx, mc.textRenderer, k.label, x, y, w, K);
		}

		renderMouse(ctx, win, active);
		m.pop();
	}

	private static void renderMouse(DrawContext ctx, long win, boolean active) {
		boolean l = active && GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		boolean r = active && GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
		boolean mid = active && GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;

		int x0 = Math.round(7.7f * P);
		int w = Math.round(2.6f * P);
		int y0 = Math.round(0.4f * P);
		int h = Math.round(3.9f * P);
		float cx = x0 + w / 2f, cy = y0 + h / 2f, rx = w / 2f, ry = h / 2f;
		int divY = y0 + Math.round(0.43f * h);

		// side buttons
		rbox(ctx, x0 - 4, y0 + 40, 6, 6, BORDER, MOUSE_FILL);
		rbox(ctx, x0 - 4, y0 + 48, 6, 7, BORDER, MOUSE_FILL);

		// body: outline then fill
		ellipse(ctx, cx, cy, rx, ry, BORDER, -1e9f, 1e9f, -1e9f, 1e9f);
		ellipse(ctx, cx, cy, rx - 1, ry - 1, MOUSE_FILL, -1e9f, 1e9f, -1e9f, 1e9f);

		// pressed buttons
		if (l) ellipse(ctx, cx, cy, rx - 1, ry - 1, PRESSED, -1e9f, divY - 0.5f, -1e9f, cx - 0.5f);
		if (r) ellipse(ctx, cx, cy, rx - 1, ry - 1, PRESSED, -1e9f, divY - 0.5f, cx + 1.5f, 1e9f);

		// divider lines
		float dy = (divY - cy) / ry;
		float half = rx * (float) Math.sqrt(Math.max(0, 1 - dy * dy));
		ctx.fill(Math.round(cx - half), divY, Math.round(cx + half), divY + 1, BORDER);
		ctx.fill((int) cx, y0 + 1, (int) cx + 1, divY, BORDER);

		// scroll wheel
		rbox(ctx, (int) cx - 4, divY - 9, 9, 19, BORDER, mid ? PRESSED : MOUSE_FILL);
	}

	/** Rounded box with 1px outline. */
	private static void rbox(DrawContext ctx, int x, int y, int w, int h, int outline, int fill) {
		rr(ctx, x, y, w, h, outline, 2);
		rr(ctx, x + 1, y + 1, w - 2, h - 2, fill, 1);
	}

	private static void rr(DrawContext ctx, int x, int y, int w, int h, int c, int r) {
		if (r >= 2 && w > 4 && h > 4) {
			ctx.fill(x + 2, y, x + w - 2, y + h, c);
			ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, c);
			ctx.fill(x, y + 2, x + w, y + h - 2, c);
		} else {
			ctx.fill(x + 1, y, x + w - 1, y + h, c);
			ctx.fill(x, y + 1, x + w, y + h - 1, c);
		}
	}

	/** Ellipse drawn at 2x resolution so the curve stays smooth. */
	private static void ellipse(DrawContext ctx, float cx, float cy, float rx, float ry, int color,
								float yMin, float yMax, float xMin, float xMax) {
		MatrixStack m = ctx.getMatrices();
		m.push();
		m.scale(0.5f, 0.5f, 1f);
		int y0 = (int) Math.ceil((cy - ry) * 2);
		int y1 = (int) Math.floor((cy + ry) * 2);
		for (int yy = y0; yy < y1; yy++) {
			float fy = (yy + 0.5f) / 2f;
			if (fy < yMin || fy > yMax) continue;
			float d = (fy - cy) / ry;
			float hh = 1 - d * d;
			if (hh <= 0) continue;
			float half = rx * (float) Math.sqrt(hh);
			float lf = Math.max(cx - half, xMin);
			float rf = Math.min(cx + half, xMax);
			int xl = Math.round(lf * 2), xr = Math.round(rf * 2);
			if (xr > xl) ctx.fill(xl, yy, xr, yy + 1, color);
		}
		m.pop();
	}

	private static void diamond(DrawContext ctx, int cx, int cy) {
		for (int dy = -4; dy <= 4; dy++) {
			int half = 4 - Math.abs(dy);
			ctx.fill(cx - half, cy + dy, cx + half + 1, cy + dy + 1, TEXT);
		}
	}

	private static void label(DrawContext ctx, TextRenderer tr, String s, int x, int y, int w, int h) {
		int tw = tr.getWidth(s);
		float sc = tw > w - 4 ? (w - 4f) / tw : 1f;
		MatrixStack m = ctx.getMatrices();
		m.push();
		m.translate(x + w / 2f, y + h / 2f - 4f * sc, 0);
		m.scale(sc, sc, 1f);
		ctx.drawText(tr, s, -tw / 2, 0, TEXT, false);
		m.pop();
	}
}
