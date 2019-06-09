package geobattle.geobattle.screens.gamescreen.gamescreenmodedata;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.util.IntRect;

// Sector building
public final class BuildSectorMode extends GameScreenModeData {
    public BuildSectorMode(int pointedTileX, int pointedTileY) {
        super(pointedTileX, pointedTileY);
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, GeoBattleMap map, GameState gameState, IntRect visible) {
        Sector sector = gameState.getCurrentPlayer().getAllSectors().next();

        int buildX = pointedTileX - ((pointedTileX - sector.x) % Sector.SECTOR_SIZE + Sector.SECTOR_SIZE) % Sector.SECTOR_SIZE;
        int buildY = pointedTileY - ((pointedTileY - sector.y) % Sector.SECTOR_SIZE + Sector.SECTOR_SIZE) % Sector.SECTOR_SIZE;

        Color playerColor = gameState.getCurrentPlayer().getColor();

        boolean canBuildSector = gameState.canBuildSector(buildX, buildY, null);
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
