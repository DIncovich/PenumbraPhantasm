package destiny.penumbra_phantasm.client.render.textbox;

import net.minecraft.network.chat.Component;

public final class TextBoxLine {
	public final Component text;
	public final Runnable onBegin;
	public char waitAfterChar;
	public float waitUnits;

	public TextBoxLine(Component text, Runnable onBegin) {
		this.text = text;
		this.onBegin = onBegin;
	}

	public TextBoxLine(Component text) {
		this(text, null);
	}
}
