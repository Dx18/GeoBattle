package geobattle.geobattle.screens.gamescreen.gamescreenmodedata;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.GeoBattleConst;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.util.CoordinateConverter;
import geobattle.geobattle.util.GeoBattleMath;
import geobattle.geobattle.util.IntPoint;
import geobattle.geobattle.util.IntRect;

// Sector building
public final class BuildSectorMode extends GameScreenModeData {
    // Game instance
    private final GeoBattle game;

    public BuildSectorMode(int pointedTileX, int pointedTileY, GeoBattle game) {
        super(pointedTileX, pointedTileY);
        this.game = game;
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, GeoBattleMap map, GameState gameState, IntRect visible) {
        Sector sector = gameState.getCurrentPlayer().getAllSectors().get(0);

        int buildX = pointedTileX - ((pointedTileX - sector.x) % Sector.SECTOR_SIZE + Sector.SECTOR_SIZE) % Sector.SECTOR_SIZE;
        int buildY = pointedTileY - ((pointedTileY - sector.y) % Sector.SECTOR_SIZE + Sector.SECTOR_SIZE) % Sector.SECTOR_SIZE;

        Color playerColor = gameState.getCurrentPlayer().getColor();

        Vector2 playerPosition = GeoBattleMath.latLongToMercator(
                game.getExternalAPI().geolocationAPI.getCurrentCoordinates()
        );

        boolean canBuildSector = gameState.canBuildSector(buildX, buildY, null, new IntPoint(
                CoordinateConverter.realWorldToSubTiles(playerPosition.x, GeoBattleConst.SUBDIVISION),
                CoordinateConverter.realWorldToSubTiles(playerPosition.y, GeoBattleConst.SUBDIVISION)
        ));
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
