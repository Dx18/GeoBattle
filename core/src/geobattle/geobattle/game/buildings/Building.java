package geobattle.geobattle.game.buildings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.google.gson.JsonObject;

import geobattle.geobattle.map.BuildingTextures;
import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.map.animations.Animations;

// Base class for all buildings
public abstract class Building {
    // X coordinate of building
    public final int x;

    // Y coordinate of building
    public final int y;

    // ID of building
    public final int id;

    // ID of player
    public final int playerId;

    // ID of sector this building belongs to
    public final int sectorId;

    // Type of building
    private final BuildingType buildingType;

    protected Building(int x, int y, int id, int playerId, int sectorId, BuildingType buildingType) {
        this.buildingType = buildingType;
        this.x = x;
        this.y = y;
        this.id = id;
        this.playerId = playerId;
        this.sectorId = sectorId;
    }

    // Creates building using BuildingParams
    public Building(BuildingParams params, BuildingType buildingType) {
        this.buildingType = buildingType;
        this.x = params.x;
        this.y = params.y;
        this.id = params.id;
        this.playerId = params.playerId;
        this.sectorId = params.sectorId;
    }

    // Returns width of building
    public int getSizeX() {
        return buildingType.sizeX;
    }

    // Returns height of building
    public int getSizeY() {
        return buildingType.sizeY;
    }

    // Returns type of building
    public BuildingType getBuildingType() {
        return buildingType;
    }

    // Draws building
    public void draw(Batch batch, GeoBattleMap map, BuildingTextures buildingTextures, Animations animations, Color color, boolean drawIcons) {
        if (drawIcons) {
            map.drawTexture(
                    batch, x, y, getSizeX(), getSizeY(),
                    buildingTextures.getIconTexture(buildingType), color
            );
        } else {
            map.drawTexture(
                    batch, x - 0.5, y - 0.5,
                    getSizeX() + 1, getSizeY() + 1,
                    buildingTextures.getTexture(buildingType), Color.WHITE
            );
            map.drawTexture(
                    batch, x - 0.5, y - 0.5,
                    getSizeX() + 1, getSizeY() + 1,
                    buildingTextures.getTeamColorTexture(buildingType), color
            );
        }
    }

    // Creates building from JSON
    public static Building fromJson(JsonObject object) {
        String type = object.getAsJsonPrimitive("type").getAsString();
        BuildingType buildingType = BuildingType.from(type);
        if (buildingType == null)
            throw new IllegalArgumentException(
                    String.format("Invalid type of building: %s", type)
            );

        int x = object.getAsJsonPrimitive("x").getAsInt();
        int y = object.getAsJsonPrimitive("y").getAsInt();
        int id = object.getAsJsonPrimitive("id").getAsInt();
        int playerId = object.getAsJsonPrimitive("playerId").getAsInt();
        int sectorId = object.getAsJsonPrimitive("sectorId").getAsInt();

        BuildingParams params = new BuildingParams(x, y, id, playerId, sectorId);

        switch (buildingType) {
            case RESEARCH_CENTER: return ResearchCenter.fromJson(object, params);
            case TURRET: return Turret.fromJson(object, params);
            case GENERATOR: return Generator.fromJson(object, params);
            case MINE: return Mine.fromJson(object, params);
            case HANGAR: return Hangar.fromJson(object, params);
        }

        return null;
    }

    // Creates building from building type and some parameters
    public static Building from(BuildingType buildingType, BuildingParams params) {
        switch (buildingType) {
            case RESEARCH_CENTER: return new ResearchCenter(params);
            case TURRET: return new Turret(params);
            case GENERATOR: return new Generator(params);
            case MINE: return new Mine(params);
            case HANGAR: return new Hangar(params);
        }
        return null;
    }

    // Returns parameters of building
    public BuildingParams getParams() {
        return new BuildingParams(x, y, id, playerId, sectorId);
    }
}
