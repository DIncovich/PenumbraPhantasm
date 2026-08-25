package destiny.penumbra_phantasm.server.network;

import destiny.penumbra_phantasm.server.egg.EggRoomManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundTextBoxPacket {
	public static final String INTERACT = "interact";

	public final String scriptId;
	public final boolean yes;

	public ServerBoundTextBoxPacket(String scriptId, boolean yes) {
		this.scriptId = scriptId;
		this.yes = yes;
	}

	public ServerBoundTextBoxPacket(FriendlyByteBuf buf) {
		this.scriptId = buf.readUtf();
		this.yes = buf.readBoolean();
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeUtf(scriptId);
		buf.writeBoolean(yes);
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if (player != null) {
				if (INTERACT.equals(scriptId)) {
					EggRoomManager.tryInteract(player);
				} else {
					EggRoomManager.handleTextBoxChoice(player, scriptId, yes);
				}
			}
		});
		return true;
	}
}
