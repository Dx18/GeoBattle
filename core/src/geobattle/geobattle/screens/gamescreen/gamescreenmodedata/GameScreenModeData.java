package geobattle.geobattle.screens.gamescreen.gamescreenmodedata;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import geobattle.geobattle.game.GameState;
import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.util.IntRect;

// Data of game screen mode
public abstract class GameScreenModeData {
    // X of pointed tile
    protected int pointedTileX;

    // Y of pointed tile
    protected int pointedTileY;

    public GameScreenModeData(int pointedTileX, int pointedTileY) {
        this.pointedTileX = pointedTileX;
        this.pointedTileY = pointedTileY;
    }

    // Sets pointed tile
    public void setPointedTile(int x, int y, boolean fromTransition) {
        this.pointedTileX = x;
        this.pointedTileY = y;
    }

    // Returns X of pointed tile
    public int getPointedTileX() {
        return pointedTileX;
    }

    // Returns Y of pointed tile
    public int getPointedTileY() {
        return pointedTileY;
    }

    // Draws using ShapeRenderer
    public void draw(ShapeRenderer shapeRenderer, GeoBattleMap map, GameState gameState, IntRect visible) {}

    // Draws using Batch
    public void draw(Batch batch, GeoBattleMap map, GameState gameState, IntRect visible) {}

    // Draws overlay using ShapeRenderer
    public void drawOverlay(ShapeRenderer shapeRenderer, GeoBattleMap map, GameState gameState, IntRect visible) {}
}
