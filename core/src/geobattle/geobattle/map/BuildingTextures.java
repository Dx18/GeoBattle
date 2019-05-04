package geobattle.geobattle.map;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;

import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.game.buildings.BuildingType;

// Textures of buildings
public final class BuildingTextures {
    // Texture of command center
    public final Texture commandCenterTexture;

    // Texture of beacon
    public final Texture beaconTexture;

    // Texture of research center
    public final Texture researchCenterTexture;

    // Texture of turret
    public final Texture turretTexture;

    // Texture of generator
    public final Texture generatorTexture;

    // Texture of mine
    public final Texture mineTexture;

    // Texture of hangar
    public final Texture hangarTexture;

    // Team color texture of command center
    public final Texture commandCenterTeamColorTexture;

    // Team color texture of beacon
    public final Texture beaconTeamColorTexture;

    // Team color texture of research center
    public final Texture researchCenterTeamColorTexture;

    // Team color texture of turret
    public final Texture turretTeamColorTexture;

    // Team color texture of generator
    public final Texture generatorTeamColorTexture;

    // Team color texture of mine
    public final Texture mineTeamColorTexture;

    // Team color texture of hangar
    public final Texture hangarTeamColorTexture;

    // Map "building type" -> "texture of building type"
    private final HashMap<BuildingType, Texture> buildingTypeToTexture;

    // Map "building type" -> "team color texture of building type"
    private final HashMap<BuildingType, Texture> buildingTypeToTeamColorTexture;

    // Creates BuildingTextures
    // Building textures must be loaded in `assetManager`
    public BuildingTextures(AssetManager assetManager) {
        commandCenterTexture = assetManager.get(GeoBattleAssets.COMMAND_CENTER);
        beaconTexture = assetManager.get(GeoBattleAssets.BEACON);
        researchCenterTexture = assetManager.get(GeoBattleAssets.RESEARCH_CENTER);
        turretTexture = assetManager.get(GeoBattleAssets.TURRET);
        generatorTexture = assetManager.get(GeoBattleAssets.GENERATOR);
        mineTexture = assetManager.get(GeoBattleAssets.MINE);
        hangarTexture = assetManager.get(GeoBattleAssets.HANGAR);

        buildingTypeToTexture = new HashMap<BuildingType, Texture>();
        buildingTypeToTexture.put(BuildingType.RESEARCH_CENTER, researchCenterTexture);
        buildingTypeToTexture.put(BuildingType.TURRET, turretTexture);
        buildingTypeToTexture.put(BuildingType.GENERATOR, generatorTexture);
        buildingTypeToTexture.put(BuildingType.MINE, mineTexture);
        buildingTypeToTexture.put(BuildingType.HANGAR, hangarTexture);

        for (Texture texture : buildingTypeToTexture.values())
            texture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Nearest);

        commandCenterTeamColorTexture = assetManager.get(GeoBattleAssets.COMMAND_CENTER_TEAM_COLOR);
        beaconTeamColorTexture = assetManager.get(GeoBattleAssets.BEACON_TEAM_COLOR);
        researchCenterTeamColorTexture = assetManager.get(GeoBattleAssets.RESEARCH_CENTER_TEAM_COLOR);
        turretTeamColorTexture = assetManager.get(GeoBattleAssets.TURRET_TEAM_COLOR);
        generatorTeamColorTexture = assetManager.get(GeoBattleAssets.GENERATOR_TEAM_COLOR);
        mineTeamColorTexture = assetManager.get(GeoBattleAssets.MINE_TEAM_COLOR);
        hangarTeamColorTexture = assetManager.get(GeoBattleAssets.HANGAR_TEAM_COLOR);

        buildingTypeToTeamColorTexture = new HashMap<BuildingType, Texture>();
        buildingTypeToTeamColorTexture.put(BuildingType.RESEARCH_CENTER, researchCenterTeamColorTexture);
        buildingTypeToTeamColorTexture.put(BuildingType.TURRET, turretTeamColorTexture);
        buildingTypeToTeamColorTexture.put(BuildingType.GENERATOR, generatorTeamColorTexture);
        buildingTypeToTeamColorTexture.put(BuildingType.MINE, mineTeamColorTexture);
        buildingTypeToTeamColorTexture.put(BuildingType.HANGAR, hangarTeamColorTexture);

        for (Texture texture : buildingTypeToTeamColorTexture.values())
            texture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Nearest);
    }

    // Returns texture of building
    public Texture getTexture(BuildingType buildingType) {
        return buildingTypeToTexture.get(buildingType);
    }

    // Returns team color texture of building
    public Texture getTeamColorTexture(BuildingType buildingType) {
        return buildingTypeToTeamColorTexture.get(buildingType);
    }
}
