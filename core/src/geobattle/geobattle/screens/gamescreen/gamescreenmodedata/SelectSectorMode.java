package geobattle.geobattle.screens.gamescreen.gamescreenmodedata;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.Iterator;

import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.PlayerState;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.util.IntRect;

public final class SelectSectorMode extends GameScreenModeData {
    private Sector pointedSector;

    private int owningPlayerId;

    private final GameState gameState;

    public SelectSectorMode(int pointedTileX, int pointedTileY, GameState gameState) {
        super(pointedTileX, pointedTileY);
        this.gameState = gameState;
        owningPlayerId = -1;
    }

    public Sector getPointedSector() {
        return pointedSector;
    }

    public int getOwningPlayerId() {
        return owningPlayerId;
    }

    @Override
    public void setPointedTile(int x, int y, boolean fromTransition) {
        super.setPointedTile(x, y, fromTransition);

        if (fromTransition)
            return;

        pointedSector = null;
        owningPlayerId = -1;
        Iterator<PlayerState> players = gameState.getPlayers();
        while (players.hasNext()) {
            PlayerState nextPlayer = players.next();
            if (nextPlayer.getPlayerId() == gameState.getPlayerId())
                continue;
            pointedSector = nextPlayer.getSector(x, y);
            if (pointedSector != null) {
                owningPlayerId = nextPlayer.getPlayerId();
                break;
            }
        }
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, GeoBattleMap map, GameState gameState, IntRect visible) {
        if (pointedSector != null) {
            map.drawRegionRectSubTiles(
                    pointedSector.x + Sector.SECTOR_SIZE / 2 - Sector.BEACON_SIZE / 2 - 1,
                    pointedSector.y + Sector.SECTOR_SIZE / 2 - Sector.BEACON_SIZE / 2 - 1,
                    Sector.BEACON_SIZE + 2, Sector.BEACON_SIZE + 2,
                    new Color(1, 0, 0, 0)
            );
        }
    }
}
