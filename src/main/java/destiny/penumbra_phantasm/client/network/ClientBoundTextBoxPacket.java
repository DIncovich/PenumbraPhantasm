package destiny.penumbra_phantasm.client.network;

import net.minecraft.network.FriendlyByteBuf;
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
}
