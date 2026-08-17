package destiny.penumbra_phantasm.mixin;

import destiny.penumbra_phantasm.server.egg.EggRoomUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@ModifyArg(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V"), index = 3)
	private boolean penumbraPhantasm$eggRoomSkipOcclusion(boolean isSpectator) {
		Level level = Minecraft.getInstance().level;
		return isSpectator || (level != null && EggRoomUtil.isEggRoom(level));
	}
}
