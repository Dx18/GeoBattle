package geobattle.geobattle.game;

import com.badlogic.gdx.audio.Sound;

public class SoundInstance {
    public final Sound sound;

    public final long soundId;

    public final double x;

    public final double y;

    public SoundInstance(Sound sound, long soundId, double x, double y) {
        this.sound = sound;
        this.soundId = soundId;
        this.x = x;
        this.y = y;
    }
}
