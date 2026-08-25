package destiny.penumbra_phantasm.server.item;

import destiny.penumbra_phantasm.client.network.ClientBoundTextBoxPacket;
import destiny.penumbra_phantasm.server.registry.PacketHandlerRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class EggItem extends Item {
	public EggItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!DarkWorldUtil.isDarkWorld(level)) {
			return InteractionResultHolder.pass(stack);
		}
		if (player instanceof ServerPlayer serverPlayer) {
			PacketHandlerRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
					new ClientBoundTextBoxPacket(ClientBoundTextBoxPacket.USED_EGG));
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
