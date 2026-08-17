package destiny.penumbra_phantasm.mixin;

import destiny.penumbra_phantasm.server.egg.EggRoomUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
	private void penumbraPhantasm$blockEggRoomSprint(boolean sprinting, CallbackInfo ci) {
		if (!sprinting) {
			return;
		}
		LivingEntity self = (LivingEntity) (Object) this;
		if (self instanceof Player && EggRoomUtil.isEggRoom(self.level())) {
			ci.cancel();
		}
	}
}
