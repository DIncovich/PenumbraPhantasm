package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.client.render.textbox.DarkWorldDialogue;
import destiny.penumbra_phantasm.client.render.textbox.TextBoxMetrics;
import destiny.penumbra_phantasm.client.render.textbox.TextBoxScript;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundTextBoxPacket {
	public static final String TREE_FRONT = "tree_front";
	public static final String TREE_FRONT_GONE = "tree_front_gone";
	public static final String TREE_BEHIND = "tree_behind";
	public static final String TREE_BEHIND_GONE = "tree_behind_gone";
	public static final String RECEIVED_EGG = "received_egg";
	public static final String THEN_NEEDNT = "then_neednt";
	public static final String USED_EGG = "used_egg";

	public final String scriptId;

	public ClientBoundTextBoxPacket(String scriptId) {
		this.scriptId = scriptId;
	}

	public ClientBoundTextBoxPacket(FriendlyByteBuf buf) {
		this.scriptId = buf.readUtf();
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeUtf(scriptId);
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> ClientBoundPacketHandler.openTextBox(scriptId));
		return true;
	}

	public static TextBoxScript createScript(String id) {
		TextBoxScript script = new TextBoxScript().id(id);
		switch (id) {
			case TREE_FRONT -> script.line(Component.translatable("textbox.penumbra_phantasm.egg.he_is_behind"));
			case TREE_FRONT_GONE -> script.line(Component.translatable("textbox.penumbra_phantasm.egg.it_is_a_tree"));
			case TREE_BEHIND -> script
					.speed(TextBoxMetrics.CHARS_PER_TICK_FAST)
					.line(Component.translatable("textbox.penumbra_phantasm.egg.man_here"))
					.waitAfter(',', TextBoxMetrics.WAIT_AFTER_WELL)
					.line(Component.translatable("textbox.penumbra_phantasm.egg.offered"))
					.choices();
			case TREE_BEHIND_GONE -> script
					.speed(TextBoxMetrics.CHARS_PER_TICK_FAST)
					.line(Component.translatable("textbox.penumbra_phantasm.egg.no_man"))
					.waitAfter(',', TextBoxMetrics.WAIT_AFTER_WELL);
			case RECEIVED_EGG -> script.line(Component.translatable("textbox.penumbra_phantasm.egg.received"), ClientBoundTextBoxPacket::playEggAcquire);
			case THEN_NEEDNT -> script.line(Component.translatable("textbox.penumbra_phantasm.egg.neednt"));
			case USED_EGG -> script.line(Component.translatable("textbox.penumbra_phantasm.egg.used"), ClientBoundTextBoxPacket::playEggAcquire);
			default -> script.line(Component.literal(id));
		}
		return script;
	}

	private static void playEggAcquire() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			player.level().playSound(player, player.blockPosition(), SoundRegistry.EGG_ACQUIRE.get(), SoundSource.PLAYERS, 1f, 1f);
		}
	}
}
