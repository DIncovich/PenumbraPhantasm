package destiny.penumbra_phantasm.client.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundSoulSyncPacket {
	public boolean seenIntro;
	public boolean diedWithSoulHearth;
	public int soulType;
	public int determination;
	public int connectionLevel;
	public int eggRoomManGone;
	public int eggObtained;

	public ClientBoundSoulSyncPacket(boolean seenIntro, boolean diedWithSoulHearth, int soulType, int determination, int connectionLevel, int eggRoomManGone, int eggObtained) {
		this.seenIntro = seenIntro;
		this.diedWithSoulHearth = diedWithSoulHearth;
		this.soulType = soulType;
		this.determination = determination;
		this.connectionLevel = connectionLevel;
		this.eggRoomManGone = eggRoomManGone;
		this.eggObtained = eggObtained;
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeBoolean(this.seenIntro);
		buffer.writeBoolean(this.diedWithSoulHearth);
		buffer.writeInt(this.soulType);
		buffer.writeInt(this.determination);
		buffer.writeInt(this.connectionLevel);
		buffer.writeInt(this.eggRoomManGone);
		buffer.writeInt(this.eggObtained);
	}

	public static ClientBoundSoulSyncPacket decode(FriendlyByteBuf buffer) {
		return new ClientBoundSoulSyncPacket(buffer.readBoolean(), buffer.readBoolean(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt());
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> ClientBoundPacketHandler.syncSoulStuff(seenIntro, diedWithSoulHearth, soulType, determination, connectionLevel, eggRoomManGone, eggObtained));
		return true;
	}
}
