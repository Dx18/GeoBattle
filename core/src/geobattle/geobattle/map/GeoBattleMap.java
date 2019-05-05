package geobattle.geobattle.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.ArrayList;
import java.util.Iterator;

import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.GeoBattleConst;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.PlayerState;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.game.buildings.Hangar;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.game.units.Unit;
import geobattle.geobattle.screens.gamescreen.GameScreenMode;
import geobattle.geobattle.server.GeolocationAPI;
import geobattle.geobattle.server.implementation.TileRequestPool;
import geobattle.geobattle.util.CoordinateConverter;
import geobattle.geobattle.util.GeoBattleMath;
import geobattle.geobattle.util.IntPoint;
import geobattle.geobattle.util.IntRect;

// Class for game and map rendering
public class GeoBattleMap extends Actor {
    // Tile API
    private TileRequestPool tileRequestPool;

    // Geolocation API
    private GeolocationAPI geolocationAPI;

    // Tree of tiles
    private TileTree tiles;

    // Counter for tiles. Used for removing unnecessary tiles
    private TileCounter tileCounter;

    // X tile offset
    private int xOffset;

    // Y tile offset
    private int yOffset;

    // State of game
    private GameState gameState;

    // Reference to rendering camera
    private GeoBattleCamera camera;

    // Textures of buildings
    private BuildingTextures buildingTextures;

    // Textures of units
    private UnitTextures unitTextures;

    // Current screenMode of game screen
    private GameScreenMode screenMode;

    // Single-pixel white texture
    private Texture colorTexture;

    // Tile where player pointed to
    private IntPoint pointedTile;

    // Current building type which selected in build screen mode
    private BuildingType selectedBuildingType;

    // Building where player points to
    private Building pointedBuilding;

    // Sector where player points to
    private Sector pointedSector;

    // Constructor
    public GeoBattleMap(
            TileRequestPool tileRequestPool, GeolocationAPI geolocationAPI,
            GeoBattleCamera camera, GameState gameState,
            AssetManager assetManager
    ) {
        Vector2 geolocation = GeoBattleMath.latLongToMercator(
                geolocationAPI.getCurrentCoordinates()
        );

        // Initializing all fields
        this.tileRequestPool = tileRequestPool;
        this.tileRequestPool.setOnLoadListener(new TileRequestPool.TileRequestCallback() {
            @Override
            public void onLoad(final Pixmap pixmap, final int x, final int y, final int zoomLevel) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        tiles.setTile(pixmap, tileCounter, xOffset, yOffset, x, y, zoomLevel);
                    }
                });
            }
        });
        this.geolocationAPI = geolocationAPI;
        this.tiles = new TileTree((int) geolocation.x, (int) geolocation.y);
        this.tileCounter = new TileCounter();

        this.xOffset = (int) geolocation.x;
        this.yOffset = (int) geolocation.y;
        this.camera = camera;
        this.gameState = gameState;

        this.buildingTextures = new BuildingTextures(assetManager);
        this.unitTextures = new UnitTextures(assetManager);

        this.screenMode = GameScreenMode.NORMAL;

        setPointedTileSubTiles((int) geolocation.x, (int) geolocation.y);

        // Resetting zoom of camera and moving it to initial point
        camera.resetZoom();
        camera.position.set(0, 0, 0);

        this.colorTexture = assetManager.get(GeoBattleAssets.COLOR);
    }

    // Sets screenMode of map rendering
    public void setScreenMode(GameScreenMode screenMode) {
        this.screenMode = screenMode;
    }

    // Returns building where player points to
    public Building getPointedBuilding() {
        return pointedBuilding;
    }

    // Returns sector where player points to
    public Sector getPointedSector() {
        return pointedSector;
    }

    public void setPointedTileSubTiles(int x, int y) {
        pointedTile = new IntPoint(x, y);

        pointedBuilding = null;
        pointedSector = null;

        PlayerState currentPlayer = gameState.getCurrentPlayer();

        pointedBuilding = currentPlayer.getBuilding(pointedTile.x, pointedTile.y);

        if (pointedBuilding != null)
            return;

        Iterator<Sector> sectors = currentPlayer.getAllSectors();
        while (sectors.hasNext()) {
            Sector next = sectors.next();
            if (GeoBattleMath.tileRectangleContains(
                    next.x, next.y, Sector.SECTOR_SIZE, Sector.SECTOR_SIZE,
                    pointedTile.x, pointedTile.y
            )) {
                pointedSector = next;
                break;
            }
        }
    }

    // Sets pointed tile
    public void setPointedTile(float worldX, float worldY) {
        setPointedTileSubTiles(
                CoordinateConverter.worldToSubTiles(worldX, xOffset, GeoBattleConst.SUBDIVISION),
                CoordinateConverter.worldToSubTiles(worldY, yOffset, GeoBattleConst.SUBDIVISION)
        );
    }

    // Returns tile where player points
    public IntPoint getPointedTile() {
        return pointedTile.clone();
    }

    // Sets real world position of camera
    // `x` and `y` - position of camera in real world
    public void moveToPlayer() {
        Vector2 coords = GeoBattleMath.latLongToMercator(
                geolocationAPI.getCurrentCoordinates()
        );
        camera.position.set(
                CoordinateConverter.realWorldToWorld(coords.x, xOffset),
                CoordinateConverter.realWorldToWorld(coords.y, yOffset),
                0
        );
    }

    // Updates map
    @Override
    public void act(float delta) {}

    // Draws region of tiles
    private void drawTiles(Batch batch, int startX, int startY, int endX, int endY, int zoomLevel) {
        for (int x = startX; x <= endX; x += (1 << (19 - zoomLevel)))
            for (int y = startY; y <= endY; y += (1 << (19 - zoomLevel))) {
                Texture tile = tiles.getTile(x, y, zoomLevel, xOffset, yOffset, tileRequestPool, tileCounter);

                if (tile != null)
                    batch.draw(
                            tile,
                            x,
                            y,
                            1 << (19 - zoomLevel),
                            1 << (19 - zoomLevel)
                    );
            }
    }

    public void drawTexture(Batch batch, int subTileX, int subTileY, int subTileWidth, int subTileHeight, float subTilePadding, TextureRegion texture, Color color) {
        if (texture == null)
            return;

        float padding = CoordinateConverter.subTilesToRealWorld(subTilePadding, GeoBattleConst.SUBDIVISION);

        float x = CoordinateConverter.subTilesToWorld(subTileX, xOffset, GeoBattleConst.SUBDIVISION) - padding;
        float y = CoordinateConverter.subTilesToWorld(subTileY, yOffset, GeoBattleConst.SUBDIVISION) - padding;
        float width = CoordinateConverter.subTilesToRealWorld(subTileWidth, GeoBattleConst.SUBDIVISION) + padding * 2;
        float height = CoordinateConverter.subTilesToRealWorld(subTileHeight, GeoBattleConst.SUBDIVISION) + padding * 2;

        Color prev = batch.getColor().cpy();
        batch.setColor(color);

        batch.draw(texture, x, y, width, height);

        batch.setColor(prev);
    }

    private void drawNormalBuilding(Batch batch, Building building, PlayerState player) {
        if (pointedBuilding == building) {
            drawRegionRectSubTiles(
                    batch,
                    building.x, building.y,
                    building.getSizeX(), building.getSizeY(),
                    new Color(0, 1, 0, 0)
            );
        }

        building.draw(batch, this, buildingTextures, player.getColor());
    }

    private void drawToBeBuiltBuilding(Batch batch) {
        if (selectedBuildingType == null)
            return;

        int x = pointedTile.x - selectedBuildingType.sizeX / 2 - 1;
        int y = pointedTile.y - selectedBuildingType.sizeY / 2 - 1;
        int width = selectedBuildingType.sizeX + 2;
        int height = selectedBuildingType.sizeY + 2;

        Iterator<Sector> sectors = gameState.getCurrentPlayer().getSectorsInRect(x, y, width, height);
        boolean canBeBuilt = false;
        while (sectors.hasNext()) {
            Sector next = sectors.next();

            if (next.containsRect(x, y, width, height))
                canBeBuilt = true;
        }

        ArrayList<IntRect> intersections = new ArrayList<IntRect>(16);
        Iterator<Building> buildings = gameState.getCurrentPlayer().getBuildingsInRect(x, y, width, height);
        while (buildings.hasNext()) {
            Building next = buildings.next();

            intersections.add(GeoBattleMath.getTileRectangleIntersection(
                    x, y, width, height,
                    next.x, next.y, next.getSizeX(), next.getSizeY()
            ));
        }

        if (intersections.size() > 0)
            canBeBuilt = false;

        if (selectedBuildingType.maxCount != Integer.MAX_VALUE) {
            int count = 0;
            Iterator<Building> allBuildings = gameState.getCurrentPlayer().getAllBuildings();
            while (allBuildings.hasNext()) {
                if (allBuildings.next().getBuildingType() == selectedBuildingType) {
                    count++;
                    if (count >= selectedBuildingType.maxCount) {
                        canBeBuilt = false;
                        break;
                    }
                }
            }
        }

        if (gameState.getResources() < selectedBuildingType.cost)
            canBeBuilt = false;

        Color green = new Color(0, 1, 0, 0.6f);
        Color red = new Color(1, 0, 0, 0.3f);

        drawTexture(
                batch,
                pointedTile.x - selectedBuildingType.sizeX / 2,
                pointedTile.y - selectedBuildingType.sizeY / 2,
                selectedBuildingType.sizeX, selectedBuildingType.sizeY,
                0.5f,
                buildingTextures.getTexture(selectedBuildingType),
                canBeBuilt ? green : red
        );

        for (IntRect intersection : intersections) {
            drawRegionRectSubTiles(
                    batch, intersection.x, intersection.y,
                    intersection.width, intersection.height, red
            );
        }
    }

    private void drawToBeDestroyedBuilding(Batch batch) {
        if (pointedBuilding != null)
            pointedBuilding.drawColorless(
                    batch, this, buildingTextures,
                    new Color(1, 0, 0, 0.6f)
            );
    }

    private void drawNormalSector(Batch batch, Sector sector, PlayerState player) {
        sector.draw(batch, this, buildingTextures, player.getColor());

        if (pointedSector == sector) {
            drawRegionRectSubTiles(
                    batch,
                    sector.x + Sector.SECTOR_SIZE / 2 - Sector.BEACON_SIZE / 2 - 1,
                    sector.y + Sector.SECTOR_SIZE / 2 - Sector.BEACON_SIZE / 2 - 1,
                    Sector.BEACON_SIZE + 2, Sector.BEACON_SIZE + 2,
                    new Color(0, 1, 0, 0)
            );
        }
    }

    private void drawUnit(Batch batch, Unit unit, Texture texture, Color color) {
        float sizeX = unit.getSizeX() / (float) (1 << GeoBattleConst.SUBDIVISION);
        float sizeY = unit.getSizeY() / (float) (1 << GeoBattleConst.SUBDIVISION);
        float x = CoordinateConverter.subTilesToWorld(unit.x, xOffset, GeoBattleConst.SUBDIVISION);
        float y = CoordinateConverter.subTilesToWorld(unit.y, yOffset, GeoBattleConst.SUBDIVISION);

//        Gdx.app.log("GeoBattle", "Drawing unit at " + x + ", " + y);

        TextureRegion region = new TextureRegion(texture);
        batch.draw(
                region, x - sizeX / 2, y - sizeY / 2,
                sizeX / 2, sizeY / 2, sizeX, sizeY, 1, 1, (float) unit.direction
        );
    }

    private void drawNormalUnit(Batch batch, Unit unit, Color teamColor) {
        drawUnit(batch, unit, unitTextures.getTexture(unit.getUnitType()), Color.WHITE);
        drawUnit(batch, unit, unitTextures.getTeamColorTexture(unit.getUnitType()), teamColor);
    }

    public void drawRegionRectSubTiles(Batch batch, int x, int y, int width, int height, Color color) {
        drawRegionRectAdvanced(
                batch,
                CoordinateConverter.subTilesToWorld(x, xOffset, GeoBattleConst.SUBDIVISION),
                CoordinateConverter.subTilesToWorld(y, yOffset, GeoBattleConst.SUBDIVISION),
                CoordinateConverter.subTilesToRealWorld(width, GeoBattleConst.SUBDIVISION),
                CoordinateConverter.subTilesToRealWorld(height, GeoBattleConst.SUBDIVISION),
                color,
                new Color(color.r, color.g, color.b, 1),
                0x1111
        );
    }

    public void drawRegionRectAdvancedSubTiles(Batch batch, int x, int y, int width, int height, Color mainColor, Color borderColor, int borderInfo) {
        drawRegionRectAdvanced(
                batch,
                CoordinateConverter.subTilesToWorld(x, xOffset, GeoBattleConst.SUBDIVISION),
                CoordinateConverter.subTilesToWorld(y, yOffset, GeoBattleConst.SUBDIVISION),
                CoordinateConverter.subTilesToRealWorld(width, GeoBattleConst.SUBDIVISION),
                CoordinateConverter.subTilesToRealWorld(height, GeoBattleConst.SUBDIVISION),
                mainColor,
                borderColor,
                borderInfo
        );
    }

    // Draws rect on map
    public void drawRegionRect(Batch batch, float x, float y, float width, float height, Color color) {
        drawRegionRectAdvanced(batch, x, y, width, height, color, new Color(color.r, color.g, color.b, 1), 0x1111);
    }

    public void drawRegionRectAdvanced(Batch batch, float x, float y, float width, float height, Color mainColor, Color borderColor, int borderInfo) {
        final int BORDER_TYPE_SIZE = 4;
        final int BORDER_TYPE_MASK = (1 << BORDER_TYPE_SIZE) - 1;
        final int TOP = 0;
        final int RIGHT = TOP + BORDER_TYPE_SIZE;
        final int BOTTOM = RIGHT + BORDER_TYPE_SIZE;
        final int LEFT = BOTTOM + BORDER_TYPE_SIZE;
        final int SOLID = 1;
        final int DASHED = 2;

        // Width of border in pixels
        final int BORDER_SIZE_PX = 5;
        final int DASH_SIZE_PX = 25;
        final int DASH_GAP_SIZE_PX = 5;

        // Width of stroke in world
        float borderSize = camera.viewportWidth / Gdx.graphics.getWidth() * BORDER_SIZE_PX;
        float dashSize = camera.viewportWidth / Gdx.graphics.getWidth() * DASH_SIZE_PX;
        float dashGapSize = camera.viewportWidth / Gdx.graphics.getWidth() * DASH_GAP_SIZE_PX;

        Color prev = batch.getColor().cpy();

        batch.setColor(mainColor);
        batch.draw(colorTexture, x, y, width, height);

        batch.setColor(borderColor);

        // Top border
        switch ((borderInfo >> TOP) & BORDER_TYPE_MASK) {
            case SOLID:
                batch.draw(colorTexture, x - borderSize / 2, y + height - borderSize / 2, borderSize + width, borderSize);
                break;
            case DASHED:
                float dashCoord = 0;
                while (dashCoord < borderSize + width) {
                    batch.draw(colorTexture, x - borderSize / 2 + dashCoord, y + height - borderSize / 2, Math.min(dashSize, borderSize + width - dashCoord), borderSize);
                    dashCoord += dashSize + dashGapSize;
                }
                break;
        }

        // Right border
        switch ((borderInfo >> RIGHT) & BORDER_TYPE_MASK) {
            case SOLID:
                batch.draw(colorTexture, x + width - borderSize / 2, y - borderSize / 2, borderSize, borderSize + height);
                break;
            case DASHED:
                float dashCoord = 0;
                while (dashCoord < borderSize + height) {
                    batch.draw(colorTexture, x + width - borderSize / 2, y - borderSize / 2 + dashCoord, borderSize, Math.min(dashSize, borderSize + height - dashCoord));
                    dashCoord += dashSize + dashGapSize;
                }
                break;
        }

        // Bottom border
        switch ((borderInfo >> BOTTOM) & BORDER_TYPE_MASK) {
            case SOLID:
                batch.draw(colorTexture, x - borderSize / 2, y - borderSize / 2, borderSize + width, borderSize);
                break;
            case DASHED:
                float dashCoord = 0;
                while (dashCoord < borderSize + width) {
                    batch.draw(colorTexture, x - borderSize / 2 + dashCoord, y - borderSize / 2, Math.min(dashSize, borderSize + width - dashCoord), borderSize);
                    dashCoord += dashSize + dashGapSize;
                }
                break;

        }

        // Left border
        switch ((borderInfo >> LEFT) & BORDER_TYPE_MASK) {
            case SOLID:
                batch.draw(colorTexture, x - borderSize / 2, y - borderSize / 2, borderSize, borderSize + height);
                break;
            case DASHED:
                float dashCoord = 0;
                while (dashCoord < borderSize + height) {
                    batch.draw(colorTexture, x - borderSize / 2, y - borderSize / 2 + dashCoord, borderSize, Math.min(dashSize, borderSize + height - dashCoord));
                    dashCoord += dashSize + dashGapSize;
                }
                break;
        }

        batch.setColor(prev);
    }

    private void drawRegionAroundBuilding(Batch batch, Building building, int regionSizeX, int regionSizeY, Color color) {
        float buildingX = CoordinateConverter.subTilesToWorld(building.x, xOffset, GeoBattleConst.SUBDIVISION);
        float buildingY = CoordinateConverter.subTilesToWorld(building.y, yOffset, GeoBattleConst.SUBDIVISION);
        float buildingSizeX = CoordinateConverter.subTilesToRealWorld(building.getSizeX(), GeoBattleConst.SUBDIVISION);
        float buildingSizeY = CoordinateConverter.subTilesToRealWorld(building.getSizeY(), GeoBattleConst.SUBDIVISION);

        float regionWorldSizeX = CoordinateConverter.subTilesToRealWorld(regionSizeX, GeoBattleConst.SUBDIVISION);
        float regionWorldSizeY = CoordinateConverter.subTilesToRealWorld(regionSizeY, GeoBattleConst.SUBDIVISION);

        drawRegionRect(
                batch,
                buildingX + buildingSizeX / 2 - regionWorldSizeX / 2,
                buildingY + buildingSizeY / 2 - regionWorldSizeY / 2,
                regionWorldSizeX, regionWorldSizeY, color
        );
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // FIXME: 22.02.19 Zoom level should use screen info instead of camera viewport
        final int zoomLevel = 20 - Math.max(1, (int)MathUtils.log2(camera.viewportWidth));

        final int startX = camera.getTileStartX(zoomLevel, xOffset);
        final int startY = camera.getTileStartY(zoomLevel, yOffset);
        final int endX = camera.getTileEndX(zoomLevel, xOffset);
        final int endY = camera.getTileEndY(zoomLevel, yOffset);

        drawTiles(batch, startX, startY, endX, endY, zoomLevel);

        // If there are too many tiles
        if (tileCounter.getLoadedCount() > 90) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    tiles.reduceTiles(
                            startX, startY,
                            endX, endY,
                            zoomLevel, 2,
                            tileCounter
                    );
                }
            });
        }

        // Draws territory
        for (PlayerState player : gameState.getPlayers()) {
            Iterator<Sector> sectors = player.getAllSectors();
            while (sectors.hasNext()) {
                Sector next = sectors.next();

                drawNormalSector(batch, next, player);
            }
        }

        // Draws buildings
        for (PlayerState player : gameState.getPlayers()) {
            Iterator<Sector> sectors = player.getAllSectors();
            while (sectors.hasNext()) {
                Sector nextSector = sectors.next();

                Iterator<Building> buildings = nextSector.getAllBuildings();
                while (buildings.hasNext()) {
                    Building nextBuilding = buildings.next();

                    if (screenMode == GameScreenMode.DESTROY && nextBuilding == pointedBuilding)
                        drawToBeDestroyedBuilding(batch);
                    else
                        drawNormalBuilding(batch, nextBuilding, player);
                }
            }
        }

        // Draws units (on top of buildings)
        for (PlayerState player : gameState.getPlayers()) {
            Iterator<Hangar> hangars = player.getHangars();
            while (hangars.hasNext()) {
                Hangar nextHangar = hangars.next();

                Iterator<Unit> units = nextHangar.units.getAllUnits();
                while (units.hasNext()) {
                    Unit next = units.next();

                    if (next != null)
                        drawNormalUnit(batch, next, player.getColor());
                }
            }
        }

        if (screenMode == GameScreenMode.BUILD) {
            // Draws building preview
            drawToBeBuiltBuilding(batch);
        } else if (screenMode == GameScreenMode.BUILD_FIRST_SECTOR) {
            Color mainColor = new Color(gameState.getCurrentPlayer().getColor());
            mainColor.a = 0.1f;
            Color borderColor = new Color(gameState.getCurrentPlayer().getColor());
            borderColor.a = 0.6f;

            drawRegionRectAdvanced(
                    batch,
                    CoordinateConverter.subTilesToWorld(pointedTile.x - Sector.SECTOR_SIZE / 2, xOffset, GeoBattleConst.SUBDIVISION),
                    CoordinateConverter.subTilesToWorld(pointedTile.y - Sector.SECTOR_SIZE / 2, yOffset, GeoBattleConst.SUBDIVISION),
                    CoordinateConverter.subTilesToRealWorld(Sector.SECTOR_SIZE, GeoBattleConst.SUBDIVISION),
                    CoordinateConverter.subTilesToRealWorld(Sector.SECTOR_SIZE, GeoBattleConst.SUBDIVISION),
                    mainColor, borderColor, 0x2222
            );
        } else if (screenMode == GameScreenMode.BUILD_SECTOR) {
            Iterator<Sector> sectors = gameState.getCurrentPlayer().getAllSectors();

            boolean isNeighbour = false;
            boolean exists = false;

            Sector sector = gameState.getCurrentPlayer().getAllSectors().next();

            int newSectorX = pointedTile.x - ((pointedTile.x - sector.x) % Sector.SECTOR_SIZE + Sector.SECTOR_SIZE) % Sector.SECTOR_SIZE;
            int newSectorY = pointedTile.y - ((pointedTile.y - sector.y) % Sector.SECTOR_SIZE + Sector.SECTOR_SIZE) % Sector.SECTOR_SIZE;

            while (sectors.hasNext()) {
                Sector next = sectors.next();

                if (
                        Math.abs(next.x - newSectorX) == Sector.SECTOR_SIZE && next.y == newSectorY ||
                        Math.abs(next.y - newSectorY) == Sector.SECTOR_SIZE && next.x == newSectorX
                )
                    isNeighbour = true;
                if (next.x == newSectorX && next.y == newSectorY)
                    exists = true;
            }

            Color mainColor;
            Color borderColor;
            if (isNeighbour && !exists) {
                mainColor = new Color(gameState.getCurrentPlayer().getColor());
                mainColor.a = 0.1f;
                borderColor = new Color(gameState.getCurrentPlayer().getColor());
                borderColor.a = 0.6f;
            } else {
                mainColor = new Color(1, 0, 0, 0.05f);
                borderColor = new Color(1, 0, 0, 0.3f);
            }

            drawRegionRectAdvancedSubTiles(
                    batch, newSectorX, newSectorY,
                    Sector.SECTOR_SIZE, Sector.SECTOR_SIZE,
                    mainColor, borderColor, 0x2222
            );
        }

        // Draws player
        Vector2 playerCoords = GeoBattleMath.latLongToMercator(geolocationAPI.getCurrentCoordinates());
        drawRegionRect(
                batch,
                CoordinateConverter.realWorldToWorld(playerCoords.x, xOffset) - 0.05f,
                CoordinateConverter.realWorldToWorld(playerCoords.y, yOffset) - 0.05f,
                0.1f, 0.1f, Color.CYAN
        );
    }

    // Draws debug
    @Override
    public void drawDebug(ShapeRenderer shapes) {
        // FIXME: 08.03.19 Zoom level should use screen info instead of camera viewport
        final int zoomLevel = 20 - Math.max(1, (int)MathUtils.log2(camera.viewportWidth));

        final int startX = camera.getTileStartX(zoomLevel, xOffset);
        final int startY = camera.getTileStartY(zoomLevel, yOffset);
        final int endX = camera.getTileEndX(zoomLevel, xOffset);
        final int endY = camera.getTileEndY(zoomLevel, yOffset);

        shapes.setColor(Color.GREEN);
        shapes.set(ShapeRenderer.ShapeType.Line);

        for (int x = startX; x <= endX; x += (1 << (19 - zoomLevel)))
            for (int y = startY; y <= endY; y += (1 << (19 - zoomLevel)))
                shapes.rect(
                        x,
                        y,
                        1 << (19 - zoomLevel),
                        1 << (19 - zoomLevel)
                );
    }

    // Disposes map
    public void dispose() {
        tiles.dispose();
    }

    // Sets selected type of building
    public void setSelectedBuildingType(BuildingType selectedBuildingType) {
        this.selectedBuildingType = selectedBuildingType;
    }

    public TileCounter getTileCounter() {
        return tileCounter;
    }
}
