package geobattle.geobattle.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.HashMap;
import java.util.Iterator;

import geobattle.geobattle.GeoBattleConst;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.PlayerState;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.game.buildings.Hangar;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.game.units.Unit;
import geobattle.geobattle.screens.gamescreen.GameScreenMode;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.BuildFirstSectorMode;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.BuildMode;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.BuildSectorMode;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.DestroyMode;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.GameScreenModeData;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.NormalMode;
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

    // Data of screen mode
    private GameScreenModeData screenModeData;

    // Tile where player pointed to
    private IntPoint pointedTile;

    // Shape renderer
    private ShapeRenderer shapeRenderer;

    // Saved modes of game screen
    private HashMap<GameScreenMode, GameScreenModeData> screenModes;

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

        this.pointedTile = new IntPoint((int) geolocation.x, (int) geolocation.y);

        this.screenModes = new HashMap<GameScreenMode, GameScreenModeData>();
        this.screenModes.put(GameScreenMode.BUILD_FIRST_SECTOR, new BuildFirstSectorMode((int) geolocation.x, (int) geolocation.y));
        this.screenModes.put(GameScreenMode.BUILD_SECTOR, new BuildSectorMode((int) geolocation.x, (int) geolocation.y));
        this.screenModes.put(GameScreenMode.NORMAL, new NormalMode((int) geolocation.x, (int) geolocation.y, this.gameState));
        this.screenModes.put(GameScreenMode.BUILD, new BuildMode((int) geolocation.x, (int) geolocation.y, BuildingType.GENERATOR));
        this.screenModes.put(GameScreenMode.DESTROY, new DestroyMode((int) geolocation.x, (int) geolocation.y, this.gameState));

        setScreenMode(GameScreenMode.NORMAL);

        // Resetting zoom of camera and moving it to initial point
        camera.resetZoom();
        camera.position.set(0, 0, 0);

        this.shapeRenderer = new ShapeRenderer();
    }

    // Sets screen mode
    public void setScreenMode(GameScreenMode mode) {
        screenModeData = screenModes.get(mode);
        screenModeData.setPointedTile(pointedTile.x, pointedTile.y);
    }

    // Returns building where player points to
    public Building getPointedBuilding() {
        if (screenModeData instanceof NormalMode)
            return ((NormalMode) screenModeData).getPointedBuilding();
        else if (screenModeData instanceof DestroyMode)
            return ((DestroyMode) screenModeData).getPointedBuilding();
        return null;
    }

    // Returns sector where player points to
    public Sector getPointedSector() {
        if (screenModeData instanceof NormalMode)
            return ((NormalMode) screenModeData).getPointedSector();
        return null;
    }

    public void setPointedTileSubTiles(int x, int y) {
        screenModeData.setPointedTile(x, y);
        pointedTile = new IntPoint(x, y);
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

    public int getPointedTileX() {
        return pointedTile.x;
    }

    public int getPointedTileY() {
        return pointedTile.y;
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

    public void drawRegionRectSubTiles(int x, int y, int width, int height, Color color) {
        drawRegionRectAdvanced(
                CoordinateConverter.subTilesToWorld(x, xOffset, GeoBattleConst.SUBDIVISION),
                CoordinateConverter.subTilesToWorld(y, yOffset, GeoBattleConst.SUBDIVISION),
                CoordinateConverter.subTilesToRealWorld(width, GeoBattleConst.SUBDIVISION),
                CoordinateConverter.subTilesToRealWorld(height, GeoBattleConst.SUBDIVISION),
                color,
                new Color(color.r, color.g, color.b, 1),
                0x1111
        );
    }

    public void drawRegionRectAdvancedSubTiles(int x, int y, int width, int height, Color mainColor, Color borderColor, int borderInfo) {
        drawRegionRectAdvanced(
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
    public void drawRegionRect(float x, float y, float width, float height, Color color) {
        drawRegionRectAdvanced(x, y, width, height, color, new Color(color.r, color.g, color.b, 1), 0x1111);
    }

    public void drawRegionRectAdvanced(float x, float y, float width, float height, Color mainColor, Color borderColor, int borderInfo) {
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

        shapeRenderer.setColor(mainColor);

        shapeRenderer.rect(x, y, width, height);

        shapeRenderer.setColor(borderColor);

        // Top border
        switch ((borderInfo >> TOP) & BORDER_TYPE_MASK) {
            case SOLID:
                shapeRenderer.rect(x - borderSize / 2, y + height - borderSize / 2, borderSize + width, borderSize);
                break;
            case DASHED:
                float dashCoord = 0;
                while (dashCoord < borderSize + width) {
                    shapeRenderer.rect(x - borderSize / 2 + dashCoord, y + height - borderSize / 2, Math.min(dashSize, borderSize + width - dashCoord), borderSize);
                    dashCoord += dashSize + dashGapSize;
                }
                break;
        }

        // Right border
        switch ((borderInfo >> RIGHT) & BORDER_TYPE_MASK) {
            case SOLID:
                shapeRenderer.rect(x + width - borderSize / 2, y - borderSize / 2, borderSize, borderSize + height);
                break;
            case DASHED:
                float dashCoord = 0;
                while (dashCoord < borderSize + height) {
                    shapeRenderer.rect(x + width - borderSize / 2, y - borderSize / 2 + dashCoord, borderSize, Math.min(dashSize, borderSize + height - dashCoord));
                    dashCoord += dashSize + dashGapSize;
                }
                break;
        }

        // Bottom border
        switch ((borderInfo >> BOTTOM) & BORDER_TYPE_MASK) {
            case SOLID:
                shapeRenderer.rect(x - borderSize / 2, y - borderSize / 2, borderSize + width, borderSize);
                break;
            case DASHED:
                float dashCoord = 0;
                while (dashCoord < borderSize + width) {
                    shapeRenderer.rect(x - borderSize / 2 + dashCoord, y - borderSize / 2, Math.min(dashSize, borderSize + width - dashCoord), borderSize);
                    dashCoord += dashSize + dashGapSize;
                }
                break;

        }

        // Left border
        switch ((borderInfo >> LEFT) & BORDER_TYPE_MASK) {
            case SOLID:
                shapeRenderer.rect(x - borderSize / 2, y - borderSize / 2, borderSize, borderSize + height);
                break;
            case DASHED:
                float dashCoord = 0;
                while (dashCoord < borderSize + height) {
                    shapeRenderer.rect(x - borderSize / 2, y - borderSize / 2 + dashCoord, borderSize, Math.min(dashSize, borderSize + height - dashCoord));
                    dashCoord += dashSize + dashGapSize;
                }
                break;
        }
    }

    private void drawSectorsAndSelections(IntRect visible) {
        // Drawing sectors...
        Iterator<PlayerState> players = gameState.getPlayers();
        while (players.hasNext()) {
            PlayerState player = players.next();
            Color playerSectorColor = player.getColor().cpy();
            playerSectorColor.a = 0.2f;

            Iterator<Sector> sectors = player.getAllSectors();
            while (sectors.hasNext()) {
                Sector next = sectors.next();

                if (!GeoBattleMath.tileRectanglesIntersect(
                        visible.x, visible.y,
                        visible.width, visible.height,
                        next.x, next.y,
                        Sector.SECTOR_SIZE, Sector.SECTOR_SIZE
                ))
                    continue;

                drawRegionRectSubTiles(
                        next.x, next.y, Sector.SECTOR_SIZE, Sector.SECTOR_SIZE, playerSectorColor
                );
            }
        }
    }

    private void drawBuildings(Batch batch, IntRect visible) {
        int visibleTiles = Math.min(
                CoordinateConverter.realWorldToSubTiles(camera.viewportWidth, GeoBattleConst.SUBDIVISION),
                CoordinateConverter.realWorldToSubTiles(camera.viewportHeight, GeoBattleConst.SUBDIVISION)
        );

        if (visibleTiles >= 400)
            return;

        boolean drawIcons = visibleTiles >= 100;

        // Draws buildings
        Iterator<PlayerState> players = gameState.getPlayers();
        while (players.hasNext()) {
            PlayerState player = players.next();
            Iterator<Sector> sectors = player.getAllSectors();
            while (sectors.hasNext()) {
                Sector nextSector = sectors.next();

                nextSector.drawBeacon(batch, this, buildingTextures, player.getColor(), drawIcons);

                Iterator<Building> buildings = nextSector.getAllBuildings();
                while (buildings.hasNext()) {
                    Building nextBuilding = buildings.next();

                    if (!GeoBattleMath.tileRectanglesIntersect(
                            visible.x, visible.y,
                            visible.width, visible.height,
                            nextBuilding.x, nextBuilding.y,
                            nextBuilding.getSizeX(), nextBuilding.getSizeY()
                    ))
                        continue;

                    nextBuilding.draw(batch, this, buildingTextures, player.getColor(), drawIcons);
                }
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

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

        IntRect visible = new IntRect(
                CoordinateConverter.worldToSubTiles(camera.position.x - camera.viewportWidth / 2, xOffset, GeoBattleConst.SUBDIVISION),
                CoordinateConverter.worldToSubTiles(camera.position.y - camera.viewportHeight / 2, yOffset, GeoBattleConst.SUBDIVISION),
                CoordinateConverter.worldToSubTiles(camera.position.x + camera.viewportWidth / 2, xOffset, GeoBattleConst.SUBDIVISION),
                CoordinateConverter.worldToSubTiles(camera.position.y + camera.viewportHeight / 2, yOffset, GeoBattleConst.SUBDIVISION)
        );

        batch.end();
        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        drawSectorsAndSelections(visible);

        if (screenModeData != null)
            screenModeData.draw(shapeRenderer, this, gameState, visible);

        // Drawing player
        Vector2 playerCoords = GeoBattleMath.latLongToMercator(geolocationAPI.getCurrentCoordinates());
        drawRegionRect(
                CoordinateConverter.realWorldToWorld(playerCoords.x, xOffset) - 0.05f,
                CoordinateConverter.realWorldToWorld(playerCoords.y, yOffset) - 0.05f,
                0.1f, 0.1f, Color.CYAN
        );

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.begin();

        drawBuildings(batch, visible);

        if (screenModeData != null)
            screenModeData.draw(batch, this, gameState, visible);

        // Draws units (on top of buildings)
        Iterator<PlayerState> players = gameState.getPlayers();
        while (players.hasNext()) {
            PlayerState player = players.next();
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

        batch.end();
        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (screenModeData != null)
            screenModeData.drawOverlay(shapeRenderer, this, gameState, visible);

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.begin();
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
            for (int y = startY; y <= endY; y += (1 << (19 - zoomLevel))) {
                shapes.rect(
                        x,
                        y,
                        1 << (19 - zoomLevel),
                        1 << (19 - zoomLevel)
                );
            }
    }

    // Disposes map
    public void dispose() {
        tiles.dispose();
    }

    // Sets selected type of building
    public void setSelectedBuildingType(BuildingType selectedBuildingType) {
        if (screenModeData instanceof BuildMode)
            ((BuildMode) screenModeData).setBuildingType(selectedBuildingType);
    }

    public BuildingType getSelectedBuildingType() {
        if (screenModeData instanceof BuildMode)
            return ((BuildMode) screenModeData).getBuildingType();
        return null;
    }

    public TileCounter getTileCounter() {
        return tileCounter;
    }

    public BuildingTextures getBuildingTextures() {
        return buildingTextures;
    }
}
