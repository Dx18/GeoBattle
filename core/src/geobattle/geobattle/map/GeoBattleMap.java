package geobattle.geobattle.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.GeoBattleConst;
import geobattle.geobattle.actionresults.MatchBranch;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.PlayerState;
import geobattle.geobattle.game.SoundInstance;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.game.buildings.Hangar;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.game.units.Unit;
import geobattle.geobattle.game.units.UnitGroup;
import geobattle.geobattle.game.units.UnitGroupState;
import geobattle.geobattle.map.animations.AnimationInstance;
import geobattle.geobattle.map.animations.Animations;
import geobattle.geobattle.screens.gamescreen.GameScreenMode;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.BuildFirstSectorMode;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.BuildMode;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.BuildSectorMode;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.DestroyMode;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.GameScreenModeData;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.NormalMode;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.SelectHangarsMode;
import geobattle.geobattle.screens.gamescreen.gamescreenmodedata.SelectSectorMode;
import geobattle.geobattle.server.MapRenderer;
import geobattle.geobattle.util.CoordinateConverter;
import geobattle.geobattle.util.GeoBattleMath;
import geobattle.geobattle.util.IntPoint;
import geobattle.geobattle.util.IntRect;
import geobattle.geobattle.util.QuadTree;
import geobattle.geobattle.util.ReadOnlyArrayList;

// Class for game and map rendering
public class GeoBattleMap extends Actor {
    // Tile API
    // private TileRequestPool tileRequestPool;

    // Geolocation API
    // private GeolocationAPI geolocationAPI;

    // Game
    private final GeoBattle game;

    // Renderer of map
    private MapRenderer mapRenderer;

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

    // Textures of sector states
    private SectorStateTextures sectorStateTextures;

    // Animations
    private Animations animations;

    // Data of screen mode
    private GameScreenModeData screenModeData;

    // Tile where player pointed to
    private IntPoint pointedTile;

    // Shape renderer
    private ShapeRenderer shapeRenderer;

    // Saved modes of game screen
    private HashMap<GameScreenMode, GameScreenModeData> screenModes;

    // Available animations
    private ArrayList<AnimationInstance> animationInstances;

    // Sounds (available)
    private Sounds sounds;

    // Sound instances
    private HashMap<Long, SoundInstance> soundInstances;

    // Constructor
    public GeoBattleMap(
            GeoBattleCamera camera, GameState gameState,
            AssetManager assetManager,
            MapRenderer mapRenderer,
            GeoBattle game
    ) {
        this.game = game;

        Vector2 geolocation = GeoBattleMath.latLongToMercator(
                this.game.getExternalAPI().geolocationAPI.getCurrentCoordinates()
        );

        this.mapRenderer = mapRenderer;

        this.xOffset = (int) geolocation.x;
        this.yOffset = (int) geolocation.y;
        this.camera = camera;
        this.gameState = gameState;

        this.buildingTextures = new BuildingTextures(assetManager);
        this.unitTextures = new UnitTextures(assetManager);
        this.sectorStateTextures = new SectorStateTextures(assetManager);
        this.animations = new Animations(assetManager);
        this.sounds = new Sounds(assetManager);

        this.animationInstances = new ArrayList<AnimationInstance>();
        this.soundInstances = new HashMap<Long, SoundInstance>();

        this.pointedTile = new IntPoint((int) geolocation.x, (int) geolocation.y);

        this.screenModes = new HashMap<GameScreenMode, GameScreenModeData>();
        this.screenModes.put(GameScreenMode.BUILD_FIRST_SECTOR, new BuildFirstSectorMode(0, 0, game));
        this.screenModes.put(GameScreenMode.BUILD_SECTOR, new BuildSectorMode(0, 0, game));
        this.screenModes.put(GameScreenMode.NORMAL, new NormalMode(0, 0, this.gameState));
        this.screenModes.put(GameScreenMode.BUILD, new BuildMode(0, 0, BuildingType.GENERATOR, buildingTextures));
        this.screenModes.put(GameScreenMode.DESTROY, new DestroyMode(0, 0, this.gameState));
        this.screenModes.put(GameScreenMode.SELECT_HANGARS, new SelectHangarsMode(0, 0, this.gameState));
        this.screenModes.put(GameScreenMode.SELECT_SECTOR, new SelectSectorMode(0, 0, this.gameState));

        setScreenMode(GameScreenMode.NORMAL, false);

        // Resetting zoom of camera and moving it to initial point
        camera.resetZoom();
        camera.position.set(0, 0, 0);

        this.shapeRenderer = new ShapeRenderer();
    }

    // Sets screen mode
    public void setScreenMode(GameScreenMode mode, boolean fromTransition) {
        screenModeData = screenModes.get(mode);
        screenModeData.setPointedTile(pointedTile.x, pointedTile.y, fromTransition);
    }

    public GameScreenModeData getScreenModeData(GameScreenMode mode) {
        return screenModes.get(mode);
    }

    // Returns building where player points to
    public Building getPointedBuilding() {
        if (screenModeData instanceof NormalMode)
            return ((NormalMode) screenModeData).getPointedBuilding();
        if (screenModeData instanceof DestroyMode)
            return ((DestroyMode) screenModeData).getPointedBuilding();
        return null;
    }

    // Returns sector where player points to
    public Sector getPointedSector() {
        if (screenModeData instanceof NormalMode)
            return ((NormalMode) screenModeData).getPointedSector();
        if (screenModeData instanceof SelectSectorMode)
            return ((SelectSectorMode) screenModeData).getPointedSector();
        return null;
    }

    public void setPointedTileSubTiles(int x, int y, boolean fromTransition) {
        screenModeData.setPointedTile(x, y, fromTransition);
        pointedTile = new IntPoint(x, y);
    }

    // Sets pointed tile
    public void setPointedTile(float worldX, float worldY, boolean fromTransition) {
        setPointedTileSubTiles(
                CoordinateConverter.worldToSubTiles(worldX, xOffset, GeoBattleConst.SUBDIVISION),
                CoordinateConverter.worldToSubTiles(worldY, yOffset, GeoBattleConst.SUBDIVISION),
                fromTransition
        );
    }

    // Returns tile where player points
    public IntPoint getPointedTile() {
        return pointedTile.clone();
    }

    // Moves camera to player geolocation
    public void moveToPlayer() {
        Vector2 coords = GeoBattleMath.latLongToMercator(
                game.getExternalAPI().geolocationAPI.getCurrentCoordinates()
        );
        camera.position.set(
                CoordinateConverter.realWorldToWorld(coords.x, xOffset),
                CoordinateConverter.realWorldToWorld(coords.y, yOffset),
                0
        );
    }

    // Moves camera to player's base (or does nothing if it does not exist)
    public void moveToBase() {
        IntPoint centerPoint = gameState.getCurrentPlayer().getCenterPoint();

        if (centerPoint != null)
            camera.position.set(
                    CoordinateConverter.subTilesToWorld(centerPoint.x, xOffset, GeoBattleConst.SUBDIVISION),
                    CoordinateConverter.subTilesToWorld(centerPoint.y, yOffset, GeoBattleConst.SUBDIVISION),
                    0
            );
    }

    // Moves camera to specific player's base (or does nothing if it does not exist)
    public void moveToBase(int playerId) {
        PlayerState player = gameState.getPlayer(playerId);

        if (player == null)
            return;

        IntPoint centerPoint = player.getCenterPoint();

        if (centerPoint != null)
            camera.position.set(
                    CoordinateConverter.subTilesToWorld(centerPoint.x, xOffset, GeoBattleConst.SUBDIVISION),
                    CoordinateConverter.subTilesToWorld(centerPoint.y, yOffset, GeoBattleConst.SUBDIVISION),
                    0
            );
    }

    // Updates map
    @Override
    public void act(float delta) {
        for (int animationInstance = 0; animationInstance < animationInstances.size();) {
            AnimationInstance current = animationInstances.get(animationInstance);
            current.update(delta);
            if (current.isExpired())
                animationInstances.remove(animationInstance);
            else
                animationInstance++;
        }
    }

    // Draws texture on map
    public void drawTexture(Batch batch, double subTileX, double subTileY, double subTileWidth, double subTileHeight, TextureRegion texture, Color color) {
        if (texture == null)
            return;

        float x = CoordinateConverter.subTilesToWorld(subTileX, xOffset, GeoBattleConst.SUBDIVISION);
        float y = CoordinateConverter.subTilesToWorld(subTileY, yOffset, GeoBattleConst.SUBDIVISION);
        float width = CoordinateConverter.subTilesToRealWorld(subTileWidth, GeoBattleConst.SUBDIVISION);
        float height = CoordinateConverter.subTilesToRealWorld(subTileHeight, GeoBattleConst.SUBDIVISION);

        Color prev = batch.getColor().cpy();
        batch.setColor(color);

        batch.draw(texture, x, y, width, height);

        batch.setColor(prev);
    }

    // Draws centered texture on map
    public void drawCenteredTexture(Batch batch, double subTileX, double subTileY, double subTileWidth, double subTileHeight, double direction, TextureRegion texture, Color color) {
        if (texture == null)
            return;

        float width = CoordinateConverter.subTilesToRealWorld(subTileWidth, GeoBattleConst.SUBDIVISION);
        float height = CoordinateConverter.subTilesToRealWorld(subTileHeight, GeoBattleConst.SUBDIVISION);
        float x = CoordinateConverter.subTilesToWorld(subTileX, xOffset, GeoBattleConst.SUBDIVISION);
        float y = CoordinateConverter.subTilesToWorld(subTileY, yOffset, GeoBattleConst.SUBDIVISION);

        Color prev = batch.getColor().cpy();
        batch.setColor(color);

        batch.draw(
                texture, x - width / 2, y - height / 2,
                width / 2, height / 2, width, height, 1, 1, (float) Math.toDegrees(direction)
        );

        batch.setColor(prev);
    }

    // Draws region rect in sub-tile coordinates
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

    // Draws advanced region rect in sub-tile coordinates
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

    // Draws region rect in world coordinates
    public void drawRegionRect(float x, float y, float width, float height, Color color) {
        drawRegionRectAdvanced(x, y, width, height, color, new Color(color.r, color.g, color.b, 1), 0x1111);
    }

    // Draws advanced region rect in sub-tile coordinates
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

    // Draws multiple textures in one square
    private void drawMultipleTexturesSubTiles(Batch batch, ArrayList<TextureRegion> textures, int x, int y, int squareSize, Color color) {
        int inRow = 1;
        while (inRow * inRow < textures.size())
            inRow++;

        double textureSize = (double) squareSize / inRow;
        for (int index = 0; index < textures.size(); index++) {
            double textureX = x + (double) squareSize * (index % textures.size()) / inRow;
            double textureY = y + (double) squareSize * (index / textures.size()) / inRow;

            drawCenteredTexture(
                    batch, textureX + textureSize / 2, textureY + textureSize / 2,
                    textureSize, textureSize, 0, textures.get(index), color
            );
        }
    }

    // Draws sectors
    private void drawSectors(IntRect visible) {
        if (Math.min(visible.width, visible.height) < 3000) {
            Color playerSectorColor = new Color(1, 1, 1, 0.2f);
            Iterator<PlayerState> players = gameState.getPlayers();
            while (players.hasNext()) {
                PlayerState player = players.next();
                playerSectorColor.set(player.getColor());
                playerSectorColor.a = 0.2f;

                ReadOnlyArrayList<Sector> sectors = player.getAllSectors();
                for (int sectorIndex = 0; sectorIndex < sectors.size(); sectorIndex++) {
                    Sector next = sectors.get(sectorIndex);

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
        } else {
            Color playerSectorColor = new Color(1, 1, 1, 0);
            Iterator<PlayerState> players = gameState.getPlayers();
            while (players.hasNext()) {
                PlayerState player = players.next();
                playerSectorColor.set(player.getColor());
                playerSectorColor.a = 0;

                int minSectorX = player.getMinSectorX();
                int minSectorY = player.getMinSectorY();
                int maxSectorX = player.getMaxSectorX();
                int maxSectorY = player.getMaxSectorY();

                int width = maxSectorX - minSectorX + Sector.SECTOR_SIZE;
                int height = maxSectorY - minSectorY + Sector.SECTOR_SIZE;

                if (!GeoBattleMath.tileRectanglesIntersect(
                        visible.x, visible.y,
                        visible.width, visible.height,
                        minSectorX, minSectorY, width, height
                ))
                    continue;

                drawRegionRectSubTiles(
                        minSectorX, minSectorY, width, height, playerSectorColor
                );
            }
        }
    }

    // Draws states of sectors
    private void drawSectorStates(Batch batch, IntRect visible) {
        if (Math.min(visible.width, visible.height) < 100)
            return;
        if (Math.min(visible.width, visible.height) >= 400)
            return;

        final int padding = Sector.SECTOR_SIZE / 3;

        Iterator<PlayerState> players = gameState.getPlayers();
        while (players.hasNext()) {
            PlayerState player = players.next();

            ReadOnlyArrayList<Sector> sectors = player.getAllSectors();
            for (int sectorIndex = 0; sectorIndex < sectors.size(); sectorIndex++) {
                Sector next = sectors.get(sectorIndex);

                if (!GeoBattleMath.tileRectanglesIntersect(
                        visible.x, visible.y,
                        visible.width, visible.height,
                        next.x, next.y,
                        Sector.SECTOR_SIZE, Sector.SECTOR_SIZE
                ))
                    continue;

                ArrayList<TextureRegion> toDraw = new ArrayList<TextureRegion>();
                if (next.isBlocked())
                    toDraw.add(sectorStateTextures.blocked);
                if (next.getEnergy() < 0)
                    toDraw.add(sectorStateTextures.noEnergy);

                drawMultipleTexturesSubTiles(
                        batch, toDraw,
                        next.x + padding, next.y + padding,
                        Sector.SECTOR_SIZE - 2 * padding,
                        new Color(1, 1, 1, 0.6f)
                );
            }
        }
    }

    // Draws buildings
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
            ReadOnlyArrayList<Sector> sectors = player.getAllSectors();
            for (int sectorIndex = 0; sectorIndex < sectors.size(); sectorIndex++) {
                Sector nextSector = sectors.get(sectorIndex);

                if (!GeoBattleMath.tileRectanglesIntersect(
                        visible.x, visible.y,
                        visible.width, visible.height,
                        nextSector.x, nextSector.y,
                        Sector.SECTOR_SIZE, Sector.SECTOR_SIZE
                ))
                    continue;

                nextSector.drawBeacon(batch, this, buildingTextures, player.getColor(), drawIcons);

                ReadOnlyArrayList<Building> buildings = nextSector.getAllBuildings();
                for (int building = 0; building < buildings.size(); building++) {
                    Building nextBuilding = buildings.get(building);

                    if (!GeoBattleMath.tileRectanglesIntersect(
                            visible.x, visible.y,
                            visible.width, visible.height,
                            nextBuilding.x, nextBuilding.y,
                            nextBuilding.getSizeX(), nextBuilding.getSizeY()
                    ))
                        continue;

                    nextBuilding.draw(batch, this, buildingTextures, animations, player.getColor(), drawIcons);
                }
            }
        }
    }

    // Draws units
    private void drawUnits(Batch batch, IntRect visible) {
        int visibleTiles = Math.min(
                CoordinateConverter.realWorldToSubTiles(camera.viewportWidth, GeoBattleConst.SUBDIVISION),
                CoordinateConverter.realWorldToSubTiles(camera.viewportHeight, GeoBattleConst.SUBDIVISION)
        );

        if (visibleTiles >= 400) {
            IntRect mapRect = new IntRect(0, 0, 1 << 23, 1 << 23);
            int maxDistance = visibleTiles / 50;

            Iterator<PlayerState> players = gameState.getPlayers();
            while (players.hasNext()) {
                PlayerState player = players.next();
                Color color = new Color(player.getColor());
                color.a = 0.6f;
                ReadOnlyArrayList<Building>[] buildings = player.getAllBuildings();
                QuadTree<UnitGroup> units = new QuadTree<UnitGroup>(16, mapRect);
                for (int sector = 0; sector < buildings.length; sector++) {
                    for (int buildingIndex = 0; buildingIndex < buildings[sector].size(); buildingIndex++) {
                        Building building = buildings[sector].get(buildingIndex);
                        if (!(building instanceof Hangar))
                            continue;

                        Hangar nextHangar = (Hangar) building;

                        if (nextHangar.units.getState() instanceof UnitGroupState.Idle)
                            continue;

                        if (!GeoBattleMath.tileRectangleContains(
                                visible, (int) nextHangar.units.x, (int) nextHangar.units.y
                        ))
                            continue;

                        units.insertAsPoint(
                                nextHangar.units,
                                new IntPoint((int) nextHangar.units.x, (int) nextHangar.units.y),
                                null
                        );
                    }
                }

                while (true) {
                    HashSet<UnitGroup> maybeGroup = units.queryByRect(mapRect, 1);
                    if (maybeGroup.size() == 0)
                        break;

                    UnitGroup group = maybeGroup.iterator().next();
                    Queue<UnitGroup> groups = new Queue<UnitGroup>();
                    groups.addLast(group);

                    ArrayList<UnitGroup> component = new ArrayList<UnitGroup>();
                    while (!groups.isEmpty()) {
                        UnitGroup currentGroup = groups.removeFirst();
                        units.removeAsPoint(new IntPoint((int) currentGroup.x, (int) currentGroup.y));
                        component.add(currentGroup);

                        HashSet<UnitGroup> nextGroups = units.queryByRect(new IntRect(
                                (int) currentGroup.x - maxDistance,
                                (int) currentGroup.y - maxDistance,
                                maxDistance * 2 + 1,
                                maxDistance * 2 + 1
                        ));

                        for (UnitGroup nextGroup : nextGroups) {
                            groups.addLast(nextGroup);
                            units.removeAsPoint(new IntPoint((int) nextGroup.x, (int) nextGroup.y));
                        }
                    }

                    int minX = Integer.MAX_VALUE;
                    int minY = Integer.MAX_VALUE;
                    int maxX = Integer.MIN_VALUE;
                    int maxY = Integer.MIN_VALUE;

                    for (UnitGroup componentGroup : component) {
                        if ((int) componentGroup.x < minX)
                            minX = (int) componentGroup.x;
                        if ((int) componentGroup.y < minY)
                            minY = (int) componentGroup.y;
                        if ((int) componentGroup.x > maxX)
                            maxX = (int) componentGroup.x;
                        if ((int) componentGroup.y > maxY)
                            maxY = (int) componentGroup.y;
                    }

                    int size = Math.max(maxX - minX + 1 + 2 * maxDistance, maxY - minY + 1 + 2 * maxDistance);

                    drawCenteredTexture(
                            batch,
                            (minX + maxX) / 2.0, (minY + maxY) / 2.0,
                            size, size, 0, unitTextures.unitGroupTexture, color
                    );
                }
            }
        } else {
            boolean drawIcons = visibleTiles >= 100;

            Iterator<PlayerState> players = gameState.getPlayers();
            while (players.hasNext()) {
                PlayerState player = players.next();
                ReadOnlyArrayList<Building>[] buildings = player.getAllBuildings();
                for (int sector = 0; sector < buildings.length; sector++) {
                    for (int buildingIndex = 0; buildingIndex < buildings[sector].size(); buildingIndex++) {
                        Building building = buildings[sector].get(buildingIndex);
                        if (!(building instanceof Hangar))
                            continue;
                        Hangar nextHangar = (Hangar) building;

                        if (drawIcons) {
                            if (!(nextHangar.units.getState() instanceof UnitGroupState.Idle)) {
                                drawCenteredTexture(
                                        batch, nextHangar.units.x, nextHangar.units.y,
                                        6, 6, 0, unitTextures.unitGroupTexture,
                                        player.getColor()
                                );
                            }
                        } else {
                            Iterator<Unit> units = nextHangar.units.getAllUnits();
                            while (units.hasNext()) {
                                Unit next = units.next();

                                if (next == null)
                                    continue;

                                int unitSize = Math.max(next.getSizeX(), next.getSizeY()) * 3;

                                if (!GeoBattleMath.tileRectanglesIntersect(
                                        visible.x, visible.y,
                                        visible.width, visible.height,
                                        (int) next.x - unitSize / 2, (int) next.y - unitSize / 2,
                                        unitSize, unitSize
                                ))
                                    continue;

                                next.draw(batch, this, unitTextures, player.getColor());
                            }
                        }
                    }
                }
            }
        }
    }

    // Returns rectangle visible by camera
    public IntRect getVisibleRect() {
        return new IntRect(
                CoordinateConverter.worldToSubTiles(camera.position.x - camera.viewportWidth / 2, xOffset, GeoBattleConst.SUBDIVISION) - 2,
                CoordinateConverter.worldToSubTiles(camera.position.y - camera.viewportHeight / 2, yOffset, GeoBattleConst.SUBDIVISION) - 2,
                CoordinateConverter.realWorldToSubTiles(camera.viewportWidth, GeoBattleConst.SUBDIVISION) + 4,
                CoordinateConverter.realWorldToSubTiles(camera.viewportHeight, GeoBattleConst.SUBDIVISION) + 4
        );
    }

    // Calculates distance from camera to point on a map
    private double getDistanceTo(double subTileX, double subTileY) {
        int zoomLevel = 20 - Math.max(1, (int)MathUtils.log2(camera.viewportWidth));

        int cameraSubTileX = CoordinateConverter.worldToSubTiles(camera.position.x, xOffset, GeoBattleConst.SUBDIVISION);
        int cameraSubTileY = CoordinateConverter.worldToSubTiles(camera.position.y, yOffset, GeoBattleConst.SUBDIVISION);

        double cameraHeight = zoomLevel >= 15
                ? 100.0 / (1 << (zoomLevel - 15))
                : 100.0 * (1 << (15 - zoomLevel));

        return Math.sqrt(
                Math.pow(cameraSubTileX - subTileX, 2) +
                Math.pow(cameraSubTileY - subTileY, 2) +
                Math.pow(cameraHeight, 2)
        );
    }

    // Calculates sound volume for point on the map
    private float getSoundVolumeAt(double subTileX, double subTileY) {
        return getDistanceTo(subTileX, subTileY) <= 100 ? (float) ((getDistanceTo(subTileX, subTileY) / -100 + 1) * game.getSoundVolume()) : 0;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        for (Map.Entry<Long, SoundInstance> soundInstance : soundInstances.entrySet()) {
            long soundId = soundInstance.getKey();
            SoundInstance sound = soundInstance.getValue();

            sound.sound.setVolume(soundId, getSoundVolumeAt(sound.x, sound.y));
        }

        mapRenderer.drawAndReduceTiles(batch, xOffset, yOffset, camera);

        IntRect visible = getVisibleRect();

        batch.end();
        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        drawSectors(visible);

        if (screenModeData != null)
            screenModeData.draw(shapeRenderer, this, gameState, visible);

        // Drawing player
        Vector2 playerCoords = GeoBattleMath.latLongToMercator(game.getExternalAPI().geolocationAPI.getCurrentCoordinates());
        drawRegionRect(
                CoordinateConverter.realWorldToWorld(playerCoords.x, xOffset) - 0.05f,
                CoordinateConverter.realWorldToWorld(playerCoords.y, yOffset) - 0.05f,
                0.1f, 0.1f, Color.BLUE
        );

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.begin();

        drawBuildings(batch, visible);

        for (AnimationInstance animationInstance : animationInstances)
            animationInstance.draw(batch, this);

        drawUnits(batch, visible);

        drawSectorStates(batch, visible);

        if (screenModeData != null)
            screenModeData.draw(batch, this, gameState, visible);

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
        mapRenderer.dispose();
    }

    // Sets selected type of building
    public void setSelectedBuildingType(BuildingType selectedBuildingType) {
        if (screenModeData instanceof BuildMode)
            ((BuildMode) screenModeData).setBuildingType(selectedBuildingType);
    }

    // Returns selected building type
    public BuildingType getSelectedBuildingType() {
        if (screenModeData instanceof BuildMode)
            return ((BuildMode) screenModeData).getBuildingType();
        return null;
    }

    // Handles event
    public void handleEvent(GeoBattleMapEvent event) {
        event.match(
                new MatchBranch<GeoBattleMapEvent.BombDropped>() {
                    @Override
                    public void onMatch(GeoBattleMapEvent.BombDropped bombDropped) {
                        animationInstances.add(new AnimationInstance(
                                animations.explosion, bombDropped.x, bombDropped.y, 2
                        ));
                        long soundId = sounds.explosion.play(game.getSoundVolume());
                        sounds.explosion.setVolume(soundId, getSoundVolumeAt(bombDropped.x, bombDropped.y));
                    }
                }
        );
    }

    // Plays sound of shots
    public long playShotsSound(double x, double y) {
        long soundId = sounds.shots.play(game.getSoundVolume());
        sounds.shots.setLooping(soundId, true);

        soundInstances.put(soundId, new SoundInstance(sounds.shots, soundId, x, y));

        return soundId;
    }

    // Stops sound of shots
    public void stopShotsSound(long soundId) {
        if (soundInstances.containsKey(soundId)) {
            sounds.shots.stop(soundId);
            soundInstances.remove(soundId);
        }
    }

    // Returns X offset of map
    public int getXOffset() {
        return xOffset;
    }

    // Returns Y offset of map
    public int getYOffset() {
        return yOffset;
    }
}
