package destiny.penumbra_phantasm.client.render.overlay;

import destiny.penumbra_phantasm.server.capability.ScreenAnimationCapability;
import destiny.penumbra_phantasm.server.registry.CapabilityRegistry;
import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import destiny.penumbra_phantasm.server.util.DarkWorldUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.common.util.LazyOptional;

public class DepthsEntryOverlay {
    public static int lastTick = -1;

    public static final IGuiOverlay OVERLAY = ((gui, guiGraphics, partialTick, width, height) -> {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) return;

        if (!DarkWorldUtil.isDepths(player.level())) return;

        ScreenAnimationCapability cap;
        LazyOptional<ScreenAnimationCapability> lazyCapability = player.getCapability(CapabilityRegistry.SCREEN_ANIMATION);
        if(lazyCapability.isPresent() && lazyCapability.resolve().isPresent())
            cap = lazyCapability.resolve().get();
        else return;

        int ticker = cap.depthsEntryTicker;
        if (ticker < 0)
            return;

        if(lastTick == -1 || ticker == 0)
            lastTick = 0;

        if (ticker == 2 && lastTick != 2) {
            player.playSound(SoundRegistry.DEPTHS_ENTER.get(), 0.75f, 1f);
        }

        lastTick = ticker;
    });
}
