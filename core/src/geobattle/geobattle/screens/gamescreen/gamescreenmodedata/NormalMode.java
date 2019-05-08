package geobattle.geobattle.screens.gamescreen.gamescreenmodedata;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.util.IntRect;

public final class NormalMode extends GameScreenModeData {
    private Building pointedBuilding;

    private Sector pointedSector;

    private final GameState gameState;

    public NormalMode(int pointedTileX, int pointedTileY, GameState gameState) {
        super(pointedTileX, pointedTileY);
        this.gameState = gameState;
    }

    public Building getPointedBuilding() {
        return pointedBuilding;
    }

    public Sector getPointedSector() {
        return pointedSector;
    }

    @Override
    public void setPointedTile(int x, int y) {
        super.setPointedTile(x, y);
        pointedBuilding = null;
        pointedSector = null;

        pointedBuilding = gameState.getCurrentPlayer().getBuilding(x, y);
        if (pointedBuilding == null)
            pointedSector = gameState.getCurrentPlayer().getSector(x, y);
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, GeoBattleMap map, GameState gameState, IntRect visible) {
        if (pointedBuilding != null) {
            map.drawRegionRectSubTiles(
                    pointedBuilding.x, pointedBuilding.y,
                    pointedBuilding.getSizeX(), pointedBuilding.getSizeY(),
                    new Color(0, 1, 0, 0)
            );
        } else if (pointedSector != null) {
            map.drawRegionRectSubTiles(
                    pointedSector.x + Sector.SECTOR_SIZE / 2 - Sector.BEACON_SIZE / 2 - 1,
                    pointedSector.y + Sector.SECTOR_SIZE / 2 - Sector.BEACON_SIZE / 2 - 1,
                    Sector.BEACON_SIZE + 2, Sector.BEACON_SIZE + 2,
                    new Color(0, 1, 0, 0)
            );
        }
    }

    @Override
    public void draw(Batch batch, GeoBattleMap map, GameState gameState, IntRect visible) {

    }
}
