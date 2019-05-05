package geobattle.geobattle.map;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;

import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.game.buildings.BuildingType;

// Textures of buildings
public final class BuildingTextures {
    // Texture of beacon
    public final TextureRegion beaconTexture;

    // Texture of research center
    public final TextureRegion researchCenterTexture;

    // Texture of turret
    public final TextureRegion turretTexture;

    // Texture of generator
    public final TextureRegion generatorTexture;

    // Texture of mine
    public final TextureRegion mineTexture;

    // Texture of hangar
    public final TextureRegion hangarTexture;

    // Team color texture of beacon
    public final TextureRegion beaconTeamColorTexture;

    // Team color texture of research center
    public final TextureRegion researchCenterTeamColorTexture;

    // Team color texture of turret
    public final TextureRegion turretTeamColorTexture;

    // Team color texture of generator
    public final TextureRegion generatorTeamColorTexture;

    // Team color texture of mine
    public final TextureRegion mineTeamColorTexture;

    // Team color texture of hangar
    public final TextureRegion hangarTeamColorTexture;

    // Map "building type" -> "texture of building type"
    private final HashMap<BuildingType, TextureRegion> buildingTypeToTexture;

    // Map "building type" -> "team color texture of building type"
    private final HashMap<BuildingType, TextureRegion> buildingTypeToTeamColorTexture;

    // Creates BuildingTextures
    // Building textures must be loaded in `assetManager`
    public BuildingTextures(AssetManager assetManager) {
        TextureAtlas buildingsAtlas = assetManager.get(GeoBattleAssets.BUILDINGS_ATLAS);

        for (Texture texture : buildingsAtlas.getTextures())
            texture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Nearest);

        beaconTexture = buildingsAtlas.findRegion(GeoBattleAssets.BEACON);
        researchCenterTexture = buildingsAtlas.findRegion(GeoBattleAssets.RESEARCH_CENTER);
        turretTexture = buildingsAtlas.findRegion(GeoBattleAssets.TURRET);
        generatorTexture = buildingsAtlas.findRegion(GeoBattleAssets.GENERATOR);
        mineTexture = buildingsAtlas.findRegion(GeoBattleAssets.MINE);
        hangarTexture = buildingsAtlas.findRegion(GeoBattleAssets.HANGAR);

        buildingTypeToTexture = new HashMap<BuildingType, TextureRegion>();
        buildingTypeToTexture.put(BuildingType.RESEARCH_CENTER, researchCenterTexture);
        buildingTypeToTexture.put(BuildingType.TURRET, turretTexture);
        buildingTypeToTexture.put(BuildingType.GENERATOR, generatorTexture);
        buildingTypeToTexture.put(BuildingType.MINE, mineTexture);
        buildingTypeToTexture.put(BuildingType.HANGAR, hangarTexture);

        beaconTeamColorTexture = buildingsAtlas.findRegion(GeoBattleAssets.BEACON_TEAM_COLOR);
        researchCenterTeamColorTexture = buildingsAtlas.findRegion(GeoBattleAssets.RESEARCH_CENTER_TEAM_COLOR);
        turretTeamColorTexture = buildingsAtlas.findRegion(GeoBattleAssets.TURRET_TEAM_COLOR);
        generatorTeamColorTexture = buildingsAtlas.findRegion(GeoBattleAssets.GENERATOR_TEAM_COLOR);
        mineTeamColorTexture = buildingsAtlas.findRegion(GeoBattleAssets.MINE_TEAM_COLOR);
        hangarTeamColorTexture = buildingsAtlas.findRegion(GeoBattleAssets.HANGAR_TEAM_COLOR);

        buildingTypeToTeamColorTexture = new HashMap<BuildingType, TextureRegion>();
        buildingTypeToTeamColorTexture.put(BuildingType.RESEARCH_CENTER, researchCenterTeamColorTexture);
        buildingTypeToTeamColorTexture.put(BuildingType.TURRET, turretTeamColorTexture);
        buildingTypeToTeamColorTexture.put(BuildingType.GENERATOR, generatorTeamColorTexture);
        buildingTypeToTeamColorTexture.put(BuildingType.MINE, mineTeamColorTexture);
        buildingTypeToTeamColorTexture.put(BuildingType.HANGAR, hangarTeamColorTexture);
    }

    // Returns texture of building
    public TextureRegion getTexture(BuildingType buildingType) {
        return buildingTypeToTexture.get(buildingType);
    }

    // Returns team color texture of building
    public TextureRegion getTeamColorTexture(BuildingType buildingType) {
        return buildingTypeToTeamColorTexture.get(buildingType);
    }
}
