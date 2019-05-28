package geobattle.geobattle.map;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;

import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.game.units.UnitType;

// Textures for units
public final class UnitTextures {
    // Bomber texture
    public final TextureRegion bomberTexture;

    // Spotter texture
    public final TextureRegion spotterTexture;

    // Bomber texture (team color)
    public final TextureRegion bomberTeamColorTexture;

    // Spotter texture (team color)
    public final TextureRegion spotterTeamColorTexture;

    // Texture of unit group
    public final TextureRegion unitGroupTexture;

    // Map "unit type" -> "texture of unit type"
    private HashMap<UnitType, TextureRegion> unitTypeToTexture;

    // Map "unit type" -> "team color texture of unit type"
    private HashMap<UnitType, TextureRegion> unitTypeToTeamColorTexture;

    // Creates UnitTextures
    // Unit textures must be loaded in `assetManager`
    public UnitTextures(AssetManager assetManager) {
        TextureAtlas unitsAtlas = assetManager.get(GeoBattleAssets.UNITS_ATLAS);

        for (Texture texture : unitsAtlas.getTextures())
            texture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Nearest);

        bomberTexture = unitsAtlas.findRegion(GeoBattleAssets.BOMBER);
        spotterTexture = unitsAtlas.findRegion(GeoBattleAssets.SPOTTER);

        unitTypeToTexture = new HashMap<UnitType, TextureRegion>();
        unitTypeToTexture.put(UnitType.BOMBER, bomberTexture);
        unitTypeToTexture.put(UnitType.SPOTTER, spotterTexture);

        bomberTeamColorTexture = unitsAtlas.findRegion(GeoBattleAssets.BOMBER_TEAM_COLOR);
        spotterTeamColorTexture = unitsAtlas.findRegion(GeoBattleAssets.SPOTTER_TEAM_COLOR);

        unitTypeToTeamColorTexture = new HashMap<UnitType, TextureRegion>();
        unitTypeToTeamColorTexture.put(UnitType.BOMBER, bomberTeamColorTexture);
        unitTypeToTeamColorTexture.put(UnitType.SPOTTER, spotterTeamColorTexture);

        unitGroupTexture = unitsAtlas.findRegion(GeoBattleAssets.UNIT_GROUP);
    }

    // Returns texture of unit
    public TextureRegion getTexture(UnitType unitType) {
        return unitTypeToTexture.get(unitType);
    }

    // Returns team color texture of unit
    public TextureRegion getTeamColorTexture(UnitType unitType) {
        return unitTypeToTeamColorTexture.get(unitType);
    }
}
