package destiny.penumbra_phantasm.client.render.textbox;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.capability.SoulCapability;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;

public final class DarkWorldTextBox {
	private DarkWorldTextBox() {
	}

	public static void render(GuiGraphics graphics, TextBoxWriter writer, int screenWidth, int screenHeight) {
		Minecraft minecraft = Minecraft.getInstance();
		int scale = screenWidth >= TextBoxMetrics.BOX_WIDTH * 2 && screenHeight >= TextBoxMetrics.BOX_HEIGHT * 2 + 8 ? 2 : 1;
		int boxW = TextBoxMetrics.BOX_WIDTH * scale;
		int boxH = TextBoxMetrics.BOX_HEIGHT * scale;
		int originX = (screenWidth - boxW) / 2;
		int originY = screenHeight - boxH;

		PoseStack pose = graphics.pose();
		pose.pushPose();
		pose.translate(originX, originY, 0);
		pose.scale(scale, scale, 1);

		RenderSystem.enableBlend();
		graphics.blit(TextBoxMetrics.TEXTURE, 0, 0, 0, 0, TextBoxMetrics.BOX_WIDTH, TextBoxMetrics.BOX_HEIGHT,
				TextBoxMetrics.TEXTURE_SIZE, TextBoxMetrics.TEXTURE_SIZE);

		float t = (float) (Util.getMillis() % TextBoxMetrics.GLOW_PERIOD_MS) / TextBoxMetrics.GLOW_PERIOD_MS;
		float jewelAlpha = 1f - Mth.sin(t * Mth.PI);
		graphics.setColor(1f, 1f, 1f, jewelAlpha);
		blitJewel(graphics, 0, 0, false, false);
		blitJewel(graphics, TextBoxMetrics.BOX_WIDTH, 0, true, false);
		blitJewel(graphics, 0, TextBoxMetrics.BOX_HEIGHT, false, true);
		blitJewel(graphics, TextBoxMetrics.BOX_WIDTH, TextBoxMetrics.BOX_HEIGHT, true, true);
		graphics.setColor(1f, 1f, 1f, 1f);

		Font font = minecraft.font;
		List<String> lines = writer.visibleLines();
		for (int i = 0; i < lines.size(); i++) {
			drawGridLine(graphics, font, lines.get(i), TextBoxMetrics.TEXT_ORIGIN_X,
					TextBoxMetrics.TEXT_ORIGIN_Y + i * TextBoxMetrics.VSPACE);
		}

		if (writer.isChoosing()) {
			drawChoices(graphics, font, writer);
		}

		pose.popPose();
		RenderSystem.disableBlend();
	}

	private static void blitJewel(GuiGraphics graphics, int x, int y, boolean flipX, boolean flipY) {
		int size = TextBoxMetrics.JEWEL_SIZE;
		int destX = flipX ? x - size : x;
		int destY = flipY ? y - size : y;
		float uOffset = flipX ? size : 0;
		float vOffset = flipY ? TextBoxMetrics.JEWEL_V + size : TextBoxMetrics.JEWEL_V;
		int uWidth = flipX ? -size : size;
		int vHeight = flipY ? -size : size;
		graphics.blit(TextBoxMetrics.TEXTURE, destX, destY, size, size, uOffset, vOffset, uWidth, vHeight,
				TextBoxMetrics.TEXTURE_SIZE, TextBoxMetrics.TEXTURE_SIZE);
	}

	private static void drawGridLine(GuiGraphics graphics, Font font, String text, int x, int y) {
		drawGridLine(graphics, font, text, x, y, 0xFFFFFFFF);
	}

	private static void drawGridLine(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
		Style style = TextBoxWriter.FONT_STYLE;
		int cursor = x;
		for (int i = 0; i < text.length(); i++) {
			String ch = String.valueOf(text.charAt(i));
			Component component = Component.literal(ch).withStyle(style);
			graphics.drawString(font, component, cursor + 1, y + 1, 0xFF111133, false);
			graphics.drawString(font, component, cursor, y, color, false);
			cursor += TextBoxMetrics.HSPACE;
		}
	}

	private static void drawChoices(GuiGraphics graphics, Font font, TextBoxWriter writer) {
		int yesW = writer.yesLabel().getString().length() * TextBoxMetrics.HSPACE;
		int noW = writer.noLabel().getString().length() * TextBoxMetrics.HSPACE;
		int yesX = TextBoxMetrics.BOX_WIDTH / 4 - yesW / 2;
		int noX = TextBoxMetrics.BOX_WIDTH * 3 / 4 - noW / 2;
		int y = (TextBoxMetrics.BOX_HEIGHT - 8) / 2;
		int choice = writer.choiceIndex();
		int yesColor = choice == 0 ? TextBoxMetrics.CHOICE_SELECTED_COLOR : 0xFFFFFFFF;
		int noColor = choice == 1 ? TextBoxMetrics.CHOICE_SELECTED_COLOR : 0xFFFFFFFF;
		drawGridLine(graphics, font, writer.yesLabel().getString(), yesX, y, yesColor);
		drawGridLine(graphics, font, writer.noLabel().getString(), noX, y, noColor);

		int soulSize = TextBoxMetrics.SOUL_SIZE;
		int soulX;
		int soulY = (TextBoxMetrics.BOX_HEIGHT - soulSize) / 2;
		if (choice < 0) {
			soulX = TextBoxMetrics.BOX_WIDTH / 2 - soulSize / 2;
		} else {
			int labelX = choice == 0 ? yesX : noX;
			soulX = labelX - soulSize - TextBoxMetrics.CHOICE_SOUL_GAP;
		}
		int soulType = 1;
		if (Minecraft.getInstance().player != null) {
			soulType = Minecraft.getInstance().player.getCapability(CapabilityRegistry.SOUL)
					.map(cap -> Mth.clamp(cap.soulType, 1, 7)).orElse(1);
		}
		ResourceLocation soul = new ResourceLocation(PenumbraPhantasm.MODID, "textures/misc/soul_shatter/soul_" + soulType + ".png");
		graphics.blit(soul, soulX, soulY, soulSize, soulSize, 0, 0, 15, 15, 15, 15);
	}
}
