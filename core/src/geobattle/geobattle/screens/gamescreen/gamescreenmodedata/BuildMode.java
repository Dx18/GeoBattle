package geobattle.geobattle.screens.gamescreen.gamescreenmodedata;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.map.BuildingTextures;
import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.util.GeoBattleMath;
import geobattle.geobattle.util.IntRect;
import geobattle.geobattle.util.ReadOnlyArrayList;

// Building mode
public final class BuildMode extends GameScreenModeData {
    // Selected type of building
    private BuildingType buildingType;

    // Textures of buildings
    private final BuildingTextures buildingTextures;

    public BuildMode(int pointedTileX, int pointedTileY, BuildingType buildingType, BuildingTextures buildingTextures) {
        super(pointedTileX, pointedTileY);
        this.buildingType = buildingType;
        this.buildingTextures = buildingTextures;
    }

    // Sets type of building
    public void setBuildingType(BuildingType buildingType) {
        this.buildingType = buildingType;
    }

    @Override
    public void draw(Batch batch, GeoBattleMap map, GameState gameState, IntRect visible) {
        int buildX = pointedTileX - buildingType.sizeX / 2;
        int buildY = pointedTileY - buildingType.sizeY / 2;

        Color color = gameState.canBuildBuilding(buildingType, buildX, buildY, null)
                ? new Color(0, 1, 0, 0.6f)
                : new Color(1, 0, 0, 0.3f);

        map.drawTexture(
                batch, buildX - 0.5, buildY - 0.5,
                buildingType.sizeX + 1, buildingType.sizeY + 1,
                buildingTextures.getTexture(buildingType), color
        );
    }

    @Override
    public void drawOverlay(ShapeRenderer shapeRenderer, GeoBattleMap map, GameState gameState, IntRect visible) {
        Color color = new Color(1, 0, 0, 0.3f);

        ReadOnlyArrayList<Building>[] buildings = gameState.getCurrentPlayer().getAllBuildings();
        for (int sector = 0; sector < buildings.length; sector++) {
            for (int buildingIndex = 0; buildingIndex < buildings[sector].size(); buildingIndex++) {
                Building next = buildings[sector].get(buildingIndex);

                if (!GeoBattleMath.tileRectanglesIntersect(
                        pointedTileX - buildingType.sizeX / 2 - 1,
                        pointedTileY - buildingType.sizeY / 2 - 1,
                        buildingType.sizeX + 2,
                        buildingType.sizeY + 2,
                        next.x, next.y, next.getSizeX(), next.getSizeY()
                ))
                        continue;

                IntRect intersection = GeoBattleMath.getTileRectangleIntersection(
                        pointedTileX - buildingType.sizeX / 2 - 1,
                        pointedTileY - buildingType.sizeY / 2 - 1,
                        buildingType.sizeX + 2,
                        buildingType.sizeY + 2,
                        next.x, next.y, next.getSizeX(), next.getSizeY()
                );

                map.drawRegionRectSubTiles(
                        intersection.x, intersection.y,
                        intersection.width, intersection.height,
                        color
                );
            }
        }
    }

    // Returns type of building
    public BuildingType getBuildingType() {
        return buildingType;
    }
}
