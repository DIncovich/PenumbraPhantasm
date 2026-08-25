package destiny.penumbra_phantasm.client.network;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientBoundEggRoomCoverPacket(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeResourceKey(dimension);
		buffer.writeVarInt(chunkX);
		buffer.writeVarInt(chunkZ);
	}

	public static ClientBoundEggRoomCoverPacket decode(FriendlyByteBuf buffer) {
		ResourceKey<Level> dimension = buffer.readResourceKey(Registries.DIMENSION);
		int chunkX = buffer.readVarInt();
		int chunkZ = buffer.readVarInt();
		return new ClientBoundEggRoomCoverPacket(dimension, chunkX, chunkZ);
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> ClientBoundPacketHandler.openEggRoomCover(dimension, chunkX, chunkZ));
		return true;
	}
}
