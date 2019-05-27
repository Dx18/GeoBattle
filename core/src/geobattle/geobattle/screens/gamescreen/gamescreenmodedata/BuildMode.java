package geobattle.geobattle.screens.gamescreen.gamescreenmodedata;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.Iterator;

import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.map.BuildingTextures;
import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.util.GeoBattleMath;
import geobattle.geobattle.util.IntRect;

public final class BuildMode extends GameScreenModeData {
    private BuildingType buildingType;

    private final BuildingTextures buildingTextures;

    public BuildMode(int pointedTileX, int pointedTileY, BuildingType buildingType, BuildingTextures buildingTextures) {
        super(pointedTileX, pointedTileY);
        this.buildingType = buildingType;
        this.buildingTextures = buildingTextures;
    }

    public void setBuildingType(BuildingType buildingType) {
        this.buildingType = buildingType;
    }

    @Override
    public void draw(Batch batch, GeoBattleMap map, GameState gameState, IntRect visible) {
        int buildX = pointedTileX - buildingType.sizeX / 2;
        int buildY = pointedTileY - buildingType.sizeY / 2;

        Color color = gameState.canBuildBuilding(buildingType, buildX, buildY)
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

        Iterator<Building> intersected = gameState.getCurrentPlayer().getBuildingsInRect(
                pointedTileX - buildingType.sizeX / 2 - 1,
                pointedTileY - buildingType.sizeY / 2 - 1,
                buildingType.sizeX + 2,
                buildingType.sizeY + 2
        );
        while (intersected.hasNext()) {
            Building next = intersected.next();

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

    public BuildingType getBuildingType() {
        return buildingType;
    }
}
