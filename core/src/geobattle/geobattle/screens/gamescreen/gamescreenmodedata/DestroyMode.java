package geobattle.geobattle.screens.gamescreen.gamescreenmodedata;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.util.IntRect;

public final class DestroyMode extends GameScreenModeData {
    private Building pointedBuilding;

    private final GameState gameState;

    public DestroyMode(int pointedTileX, int pointedTileY, GameState gameState) {
        super(pointedTileX, pointedTileY);
        this.gameState = gameState;
    }

    public Building getPointedBuilding() {
        return pointedBuilding;
    }

    @Override
    public void setPointedTile(int x, int y, boolean fromTransition) {
        super.setPointedTile(x, y, fromTransition);
        pointedBuilding = gameState.getCurrentPlayer().getBuilding(x, y);
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, GeoBattleMap map, GameState gameState, IntRect visible) {
        if (pointedBuilding != null) {
            map.drawRegionRectSubTiles(
                    pointedBuilding.x, pointedBuilding.y,
                    pointedBuilding.getSizeX(), pointedBuilding.getSizeY(),
                    new Color(1, 0, 0, 0)
            );
        }
    }
}
