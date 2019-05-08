package geobattle.geobattle.screens.gamescreen.gamescreenmodedata;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import geobattle.geobattle.game.GameState;
import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.util.IntRect;

public abstract class GameScreenModeData {
    protected int pointedTileX;

    protected int pointedTileY;

    public GameScreenModeData(int pointedTileX, int pointedTileY) {
        this.pointedTileX = pointedTileX;
        this.pointedTileY = pointedTileY;
    }

    public void setPointedTile(int x, int y) {
        this.pointedTileX = x;
        this.pointedTileY = y;
    }

    public abstract void draw(ShapeRenderer shapeRenderer, GeoBattleMap map, GameState gameState, IntRect visible);

    public abstract void draw(Batch batch, GeoBattleMap map, GameState gameState, IntRect visible);
}
