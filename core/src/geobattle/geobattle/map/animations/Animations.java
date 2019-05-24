package geobattle.geobattle.map.animations;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

import geobattle.geobattle.GeoBattleAssets;

// Available game animations
public final class Animations {
    // Bomb explosion
    public final AnimationInfo explosion;

    public Animations(AssetManager assetManager) {
        explosion = new AnimationInfo(
                assetManager.get(GeoBattleAssets.ANIMATION_EXPLOSION, Texture.class),
                GeoBattleAssets.ANIMATION_EXPLOSION_FRAME_WIDTH,
                GeoBattleAssets.ANIMATION_EXPLOSION_FRAME_HEIGHT,
                GeoBattleAssets.ANIMATION_EXPLOSION_FRAME_COUNT
        );
    }
}
