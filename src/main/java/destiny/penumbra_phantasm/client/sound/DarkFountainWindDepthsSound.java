package destiny.penumbra_phantasm.client.sound;

import destiny.penumbra_phantasm.server.fountain.DarkFountain;
import net.minecraft.sounds.SoundEvent;

public class DarkFountainWindDepthsSound extends DarkFountainSound<DarkFountain>{
    private static final float VOLUME_MIN = 0.0F;
    private static final float VOLUME_MAX = 0.25F;

    public DarkFountainWindDepthsSound(DarkFountain fountain, SoundEvent soundEvent) {
        super(fountain, soundEvent, 64, 96);
        this.looping = true;
        this.volume = VOLUME_MIN;
    }

    @Override
    public void tick()
    {
        if(getDistanceFromSource3d() <= 96)
            fadeIn();
        else
            fadeOut();

        super.tick();
    }

    @Override
    public boolean canStartSilent()
    {
        return true;
    }

    private void fadeIn()
    {
        if(this.volume < VOLUME_MAX)
            this.volume += 0.01F;
    }

    private void fadeOut()
    {
        if(this.volume > VOLUME_MIN)
            this.volume -= 0.01F;
    }
}
