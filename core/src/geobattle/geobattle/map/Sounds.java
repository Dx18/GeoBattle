package geobattle.geobattle.map;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;

import geobattle.geobattle.GeoBattleAssets;

// Available sounds
public class Sounds {
    // Sound of shots
    public final Sound shots;

    // Sound of explosion
    public final Sound explosion;

    public Sounds(AssetManager assetManager) {
        shots = assetManager.get(GeoBattleAssets.SOUND_SHOTS);
        explosion = assetManager.get(GeoBattleAssets.SOUND_EXPLOSION);
    }
}
