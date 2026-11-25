package com.example.keystrokes;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class EditScreen extends Screen {
	private static final float MIN = 0.5f, MAX = 4.0f;

	private boolean dragging;
	private int offX, offY;
	private ScaleSlider slider;

	public EditScreen() {
		super(Text.literal("Keystrokes"));
	}

	private static double toValue(float scale) {
		return (scale - MIN) / (MAX - MIN);
	}

	@Override
	protected void init() {
		KeystrokesConfig cfg = KeystrokesConfig.get();
		slider = new ScaleSlider(width / 2 - 100, height - 52, 200, 20);
		addDrawableChild(slider);
		addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> {
			cfg.x = 10; cfg.y = 10; cfg.scale = 1.0f;
			slider.sync();
		}).dimensions(width / 2 - 100, height - 28, 98, 20).build());
		addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
				.dimensions(width / 2 + 2, height - 28, 98, 20).build());
	}

	private int bw() { return (int) (KeystrokesRenderer.WIDTH * KeystrokesConfig.get().scale); }
	private int bh() { return (int) (KeystrokesRenderer.HEIGHT * KeystrokesConfig.get().scale); }

	private boolean inside(double mx, double my) {
		KeystrokesConfig c = KeystrokesConfig.get();
		return mx >= c.x && mx <= c.x + bw() && my >= c.y && my <= c.y + bh();
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (super.mouseClicked(mx, my, button)) return true;
		if (button == 0 && inside(mx, my)) {
			dragging = true;
			offX = (int) mx - KeystrokesConfig.get().x;
			offY = (int) my - KeystrokesConfig.get().y;
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
		if (dragging) {
			KeystrokesConfig c = KeystrokesConfig.get();
			c.x = MathHelper.clamp((int) mx - offX, 0, Math.max(0, width - bw()));
			c.y = MathHelper.clamp((int) my - offY, 0, Math.max(0, height - bh()));
			return true;
		}
		return super.mouseDragged(mx, my, button, dx, dy);
	}

	@Override
	public boolean mouseReleased(double mx, double my, int button) {
		dragging = false;
		return super.mouseReleased(mx, my, button);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double h, double v) {
		KeystrokesConfig c = KeystrokesConfig.get();
		c.scale = MathHelper.clamp(c.scale + (float) v * 0.1f, MIN, MAX);
		slider.sync();
		return true;
	}

	@Override
	public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
		// no blur/dim, keep the overlay clearly visible
	}

	@Override
	public void render(DrawContext ctx, int mx, int my, float delta) {
		super.render(ctx, mx, my, delta);
		KeystrokesConfig c = KeystrokesConfig.get();
		ctx.drawBorder(c.x - 2, c.y - 2, bw() + 4, bh() + 4, 0xFFFFFFFF);
		ctx.drawCenteredTextWithShadow(textRenderer, "Drag to move  |  Scroll to resize", width / 2, 10, 0xFFFFFFFF);
	}

	@Override
	public void removed() {
		KeystrokesConfig.save();
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private class ScaleSlider extends SliderWidget {
		ScaleSlider(int x, int y, int w, int h) {
			super(x, y, w, h, Text.empty(), toValue(KeystrokesConfig.get().scale));
			updateMessage();
		}

		void sync() {
			this.value = MathHelper.clamp(toValue(KeystrokesConfig.get().scale), 0, 1);
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			setMessage(Text.literal("Size: " + String.format("%.2f", KeystrokesConfig.get().scale)));
		}

		@Override
		protected void applyValue() {
			KeystrokesConfig.get().scale = (float) (MIN + value * (MAX - MIN));
		}
	}
}
