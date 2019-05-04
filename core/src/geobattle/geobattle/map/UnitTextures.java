package geobattle.geobattle.map;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;

import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.game.units.UnitType;

// Textures for units
public final class UnitTextures {
    // Bomber texture
    public final Texture bomberTexture;

    // Spotter texture
    public final Texture spotterTexture;

    // Bomber texture (team color)
    public final Texture bomberTeamColorTexture;

    // Spotter texture (team color)
    public final Texture spotterTeamColorTexture;

    // Map "unit type" -> "texture of unit type"
    private HashMap<UnitType, Texture> unitTypeToTexture;

    // Map "unit type" -> "team color texture of unit type"
    private HashMap<UnitType, Texture> unitTypeToTeamColorTexture;

    // Creates UnitTextures
    // Unit textures must be loaded in `assetManager`
    public UnitTextures(AssetManager assetManager) {
        bomberTexture = assetManager.get(GeoBattleAssets.BOMBER);
        spotterTexture = assetManager.get(GeoBattleAssets.SPOTTER);

        unitTypeToTexture = new HashMap<UnitType, Texture>();
        unitTypeToTexture.put(UnitType.BOMBER, bomberTexture);
        unitTypeToTexture.put(UnitType.SPOTTER, spotterTexture);

        for (Texture texture : unitTypeToTexture.values())
            texture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Nearest);

        bomberTeamColorTexture = assetManager.get(GeoBattleAssets.BOMBER_TEAM_COLOR);
        spotterTeamColorTexture = assetManager.get(GeoBattleAssets.SPOTTER_TEAM_COLOR);

        unitTypeToTeamColorTexture = new HashMap<UnitType, Texture>();
        unitTypeToTeamColorTexture.put(UnitType.BOMBER, bomberTeamColorTexture);
        unitTypeToTeamColorTexture.put(UnitType.SPOTTER, spotterTeamColorTexture);

        for (Texture texture : unitTypeToTeamColorTexture.values())
            texture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Nearest);
    }

    // Returns texture of unit
    public Texture getTexture(UnitType unitType) {
        return unitTypeToTexture.get(unitType);
    }

    // Returns team color texture of unit
    public Texture getTeamColorTexture(UnitType unitType) {
        return unitTypeToTeamColorTexture.get(unitType);
    }
}
