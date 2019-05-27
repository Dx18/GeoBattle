package geobattle.geobattle.map;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import geobattle.geobattle.GeoBattleAssets;

// Textures of sector states
public class SectorStateTextures {
    // Sector is blocked
    public final TextureRegion blocked;

    // Sector has no energy
    public final TextureRegion noEnergy;

    public SectorStateTextures(AssetManager assetManager) {
        TextureAtlas sectorStatesAtlas = assetManager.get(GeoBattleAssets.SECTOR_STATES_ATLAS);

        blocked = sectorStatesAtlas.findRegion(GeoBattleAssets.SECTOR_STATE_BLOCKED);
        noEnergy = sectorStatesAtlas.findRegion(GeoBattleAssets.SECTOR_STATE_NO_ENERGY);
    }
}
