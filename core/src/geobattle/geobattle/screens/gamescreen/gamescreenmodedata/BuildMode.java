package geobattle.geobattle.screens.gamescreen.gamescreenmodedata;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.util.IntRect;

public final class BuildMode extends GameScreenModeData {
    private BuildingType buildingType;

    public BuildMode(int pointedTileX, int pointedTileY, BuildingType buildingType) {
        super(pointedTileX, pointedTileY);
        this.buildingType = buildingType;
    }

    public void setBuildingType(BuildingType buildingType) {
        this.buildingType = buildingType;
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, GeoBattleMap map, GameState gameState, IntRect visible) {

    }

    @Override
    public void draw(Batch batch, GeoBattleMap map, GameState gameState, IntRect visible) {
        int buildX = pointedTileX - buildingType.sizeX / 2;
        int buildY = pointedTileY - buildingType.sizeY / 2;

        Color color = gameState.canBuildBuilding(buildingType, buildX, buildY)
                ? new Color(0, 1, 0, 0.6f)
                : new Color(1, 0, 0, 0.3f);

        map.drawTexture(
                batch, buildX, buildY, buildingType.sizeX, buildingType.sizeY, 0.5f,
                map.getBuildingTextures().getTexture(buildingType), color
        );
    }

    public BuildingType getBuildingType() {
        return buildingType;
    }
}
