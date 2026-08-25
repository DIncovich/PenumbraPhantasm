package destiny.penumbra_phantasm.server.network;

import destiny.penumbra_phantasm.server.egg.EggRoomManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundEggRoomReadyPacket {
	public ServerBoundEggRoomReadyPacket() {
	}

	public void encode(FriendlyByteBuf buffer) {
	}

	public static ServerBoundEggRoomReadyPacket decode(FriendlyByteBuf buffer) {
		return new ServerBoundEggRoomReadyPacket();
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if (player != null) {
				EggRoomManager.onClientReady(player);
			}
		});
		return true;
	}
}
