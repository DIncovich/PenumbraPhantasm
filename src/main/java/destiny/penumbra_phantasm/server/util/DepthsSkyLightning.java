package destiny.penumbra_phantasm.server.util;

import destiny.penumbra_phantasm.server.registry.SoundRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public class DepthsSkyLightning {
    public static final int MIN_SILHOUETTE_COUNT = 10;
    public static final int MAX_SILHOUETTE_COUNT = 15;

    public static final int FLASH_PERIOD = 300;
    public static final float FLASH_CHANCE = 0.35F;
    public static final int FLASH_DURATION_TICKS = 100;
    public static final float FLASH_MIN_RADIUS = 40F;
    public static final float FLASH_MAX_RADIUS = 100F;
    public static final float FLASH_Y = 8F;

    public static void tick(ServerLevel level) {
        if (!DarkWorldUtil.isDepths(level)) {
            return;
        }

        long skySeed = getSkySeed(level);
        long time = level.getGameTime();
        long slot = time / FLASH_PERIOD;

        for (long currentSlot = slot - 1L; currentSlot <= slot; currentSlot++) {
            FlashEvent event = createFlash(skySeed, currentSlot);
            if (event != null && event.startTick == time) {
                playThunder(level);
            }
        }
    }

    public static FlashEvent createFlash(long skySeed, long slot) {
        long seed = skySeed ^ mixSeed(slot * 341873128712L + 132897987541L);
        RandomSource random = RandomSource.create(seed);
        if (random.nextFloat() > FLASH_CHANCE) {
            return null;
        }

        long startTick = slot * FLASH_PERIOD + random.nextInt(Math.max(1, FLASH_PERIOD - FLASH_DURATION_TICKS));
        float peak = Mth.lerp(random.nextFloat(), 0.45F, 1F);
        float radius = Mth.lerp(random.nextFloat(), FLASH_MIN_RADIUS, FLASH_MAX_RADIUS);
        float azimuth = random.nextFloat() * ((float) (Math.PI * 2.0D));
        return new FlashEvent(startTick, FLASH_DURATION_TICKS, peak, radius, azimuth);
    }

    public static long getSkySeed(Level level) {
        return mixSeed(level.dimension().location().toString().hashCode());
    }

    public static int silhouetteCount(long seed) {
        RandomSource random = RandomSource.create(seed);
        return Mth.nextInt(random, MIN_SILHOUETTE_COUNT, MAX_SILHOUETTE_COUNT);
    }

    public static long mixSeed(long seed) {
        long mixed = seed;
        mixed ^= mixed >>> 33;
        mixed *= 0xff51afd7ed558ccdL;
        mixed ^= mixed >>> 33;
        mixed *= 0xc4ceb9fe1a85ec53L;
        mixed ^= mixed >>> 33;
        return mixed;
    }

    private static void playThunder(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            player.playNotifySound(SoundRegistry.DEPTHS_THUNDER.get(), SoundSource.WEATHER, 0.75F, 1.0F);
        }
    }

    public record FlashEvent(long startTick, int duration, float peak, float radius, float azimuth) {
    }
}
