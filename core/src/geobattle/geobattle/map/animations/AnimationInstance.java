package geobattle.geobattle.map.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;

import geobattle.geobattle.map.GeoBattleMap;

// Instance of animation
public final class AnimationInstance {
    // Info about animation
    public final AnimationInfo animationInfo;

    // X coordinate of animation instance
    private double x;

    // Y coordinate of animation instance
    private double y;

    // Duration of instance's life
    public final double lifeDuration;

    // Current age
    private double age;

    public AnimationInstance(AnimationInfo animationInfo, double x, double y, double lifeDuration) {
        this.animationInfo = animationInfo;
        this.x = x;
        this.y = y;
        this.lifeDuration = lifeDuration;
        this.age = 0;
    }

    // Updates instance
    public void update(float delta) {
        age += delta;
    }

    // Draws animation
    public void draw(Batch batch, GeoBattleMap map) {
        if (age < 0 || age >= lifeDuration)
            return;

        map.drawCenteredTextureSubTiles(
                batch, x, y, 3, 3, 0,
                animationInfo.getFrame((int) (age / lifeDuration * animationInfo.getFrameCount())), Color.WHITE
        );
    }

    // Returns true if animation instance should be destroyed
    public boolean isExpired() {
        return age >= lifeDuration;
    }
}
