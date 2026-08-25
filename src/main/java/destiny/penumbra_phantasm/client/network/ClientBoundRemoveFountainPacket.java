package destiny.penumbra_phantasm.client.network;

import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundRemoveFountainPacket {
    public final BlockPos fountainPos;

    public ClientBoundRemoveFountainPacket(BlockPos fountainPos) {
        this.fountainPos = fountainPos;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(fountainPos);
    }

    public static ClientBoundRemoveFountainPacket decode(FriendlyByteBuf buffer) {
        return new ClientBoundRemoveFountainPacket(buffer.readBlockPos());
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) {
                return;
            }
            level.getCapability(CapabilityRegistry.DARK_FOUNTAIN).ifPresent(cap -> {
                DarkFountain fountain = cap.darkFountains.remove(fountainPos);
                if (fountain == null) {
                    return;
                }
                if (fountain.windSound != null) {
                    fountain.windSound.stopSound();
                }
                if (fountain.darknessSound != null) {
                    fountain.darknessSound.stopSound();
                }
            });
        });
        return true;
    }
}
