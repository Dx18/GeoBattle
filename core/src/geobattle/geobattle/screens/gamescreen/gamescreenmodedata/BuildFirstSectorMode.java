package geobattle.geobattle.screens.gamescreen.gamescreenmodedata;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.util.IntRect;

public final class BuildFirstSectorMode extends GameScreenModeData {
    public BuildFirstSectorMode(int pointedTileX, int pointedTileY) {
        super(pointedTileX, pointedTileY);
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, GeoBattleMap map, GameState gameState, IntRect visible) {
        int buildX = pointedTileX - Sector.SECTOR_SIZE / 2;
        int buildY = pointedTileY - Sector.SECTOR_SIZE / 2;

        Color playerColor = gameState.getCurrentPlayer().getColor();

        boolean canBuildSector = gameState.canBuildSector(buildX, buildY);
        Color mainColor = canBuildSector
                ? new Color(playerColor.r, playerColor.g, playerColor.b, 0.1f)
                : new Color(1, 0, 0, 0.05f);
        Color borderColor = canBuildSector
                ? new Color(playerColor.r, playerColor.g, playerColor.b, 1)
                : new Color(1, 0, 0, 1);

        map.drawRegionRectAdvancedSubTiles(
                buildX, buildY, Sector.SECTOR_SIZE, Sector.SECTOR_SIZE,
                mainColor, borderColor, 0x2222
        );
    }
}
