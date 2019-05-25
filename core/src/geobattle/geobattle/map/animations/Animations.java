package geobattle.geobattle.map.animations;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

import geobattle.geobattle.GeoBattleAssets;

// Available game animations
public final class Animations {
    // Bomb explosion
    public final AnimationInfo explosion;

    // Turret flash
    public final AnimationInfo turretFlash;

    public Animations(AssetManager assetManager) {
        explosion = new AnimationInfo(
                assetManager.get(GeoBattleAssets.ANIMATION_EXPLOSION, Texture.class),
                GeoBattleAssets.ANIMATION_EXPLOSION_FRAME_WIDTH,
                GeoBattleAssets.ANIMATION_EXPLOSION_FRAME_HEIGHT,
                GeoBattleAssets.ANIMATION_EXPLOSION_FRAME_COUNT
        );
        turretFlash = new AnimationInfo(
                assetManager.get(GeoBattleAssets.ANIMATION_TURRET_FLASH, Texture.class),
                GeoBattleAssets.ANIMATION_TURRET_FLASH_FRAME_WIDTH,
                GeoBattleAssets.ANIMATION_TURRET_FLASH_FRAME_HEIGHT,
                GeoBattleAssets.ANIMATION_TURRET_FLASH_FRAME_COUNT
        );
    }
}
