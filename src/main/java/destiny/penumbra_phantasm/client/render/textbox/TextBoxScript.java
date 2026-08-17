package destiny.penumbra_phantasm.client.render.textbox;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class TextBoxScript {
	public final List<TextBoxLine> lines = new ArrayList<>();
	public boolean hasChoices;
	public Component yesLabel = Component.translatable("gui.penumbra_phantasm.textbox.yes");
	public Component noLabel = Component.translatable("gui.penumbra_phantasm.textbox.no");
	public String id = "";
	public float charsPerTick = TextBoxMetrics.CHARS_PER_TICK;

	public TextBoxScript id(String id) {
		this.id = id;
		return this;
	}

	public TextBoxScript speed(float charsPerTick) {
		this.charsPerTick = charsPerTick;
		return this;
	}

	public TextBoxScript line(Component text) {
		lines.add(new TextBoxLine(text));
		return this;
	}

	public TextBoxScript line(Component text, Runnable onBegin) {
		lines.add(new TextBoxLine(text, onBegin));
		return this;
	}

	public TextBoxScript waitAfter(char ch, float units) {
		if (!lines.isEmpty()) {
			TextBoxLine last = lines.get(lines.size() - 1);
			last.waitAfterChar = ch;
			last.waitUnits = units;
		}
		return this;
	}

	public TextBoxScript choices() {
		this.hasChoices = true;
		return this;
	}
}
