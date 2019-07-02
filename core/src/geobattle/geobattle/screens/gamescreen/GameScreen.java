package geobattle.geobattle.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;

import java.util.Iterator;
import java.util.Locale;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.GeoBattleConst;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.PlayerState;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.map.GeoBattleCamera;
import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.screens.BackButtonProcessor;
import geobattle.geobattle.server.AuthInfo;
import geobattle.geobattle.server.implementation.RealMapRenderer;
import geobattle.geobattle.tutorial.Tutorial;
import geobattle.geobattle.util.CoordinateConverter;
import geobattle.geobattle.util.GeoBattleMath;
import geobattle.geobattle.util.IntPoint;

// Game screen
public final class GameScreen implements Screen {
    // State of game
    private final GameState gameState;

    // Manager of assets
    private final AssetManager assetManager;

    // Stage which holds map
    private Stage tilesStage;

    // Map
    private GeoBattleMap map;

    // Camera for tiles
    private GeoBattleCamera camera;

    // Stage which holds GUI
    private Stage guiStage;

    // User interface
    private GameScreenGUI gui;

    // Current mode
    private GameScreenMode mode;

    // Game instance
    private GeoBattle game;

    // Game events
    private GameEvents gameEvents;

    // Debug mode
    private boolean debugMode;

    // Current tutorial
    private Tutorial tutorial;

    // Sprite batch for player data
    private SpriteBatch playerDataSpriteBatch;

    // Font for player data
    private final BitmapFont font;

    // Constructor
    public GameScreen(GameState gameState, AssetManager assetManager, AuthInfo authInfo, GeoBattle game) {
        this.assetManager = assetManager;
        this.gameState = gameState;
        this.game = game;

        // Map rendering stuff
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        camera = new GeoBattleCamera(width, height);
        tilesStage = new Stage(new ScalingViewport(Scaling.stretch, width, height, camera), new SpriteBatch(8191));

        Vector2 geolocation = GeoBattleMath.latLongToMercator(
                this.game.getExternalAPI().geolocationAPI.getCurrentCoordinates()
        );

        map = new GeoBattleMap(
                camera, gameState, assetManager,
                new RealMapRenderer((int) geolocation.x, (int) geolocation.y, game.getExternalAPI().tileRequestPool),
                game
        );
        tilesStage.addActor(map);

        this.gameEvents = new GameEvents(gameState, authInfo, this, map, game);
        map.setSelectedBuildingType(BuildingType.GENERATOR);

        this.debugMode = true;

        tilesStage.setDebugAll(false);

        playerDataSpriteBatch = new SpriteBatch();

        font = new BitmapFont(Gdx.files.internal("mapFont/font-36.fnt"));
        font.setUseIntegerPositions(false);
    }

    public I18NBundle getI18NBundle() {
        return game.getI18NBundle();
    }

    // Initializes game screen
    @Override
    public void show() {
        // GUI stuff
        guiStage = new Stage();
        gui = new GameScreenGUI(assetManager, this, guiStage);

        switchTo(gameState.getCurrentPlayer().getSectorCount() == 0
                ? GameScreenMode.BUILD_FIRST_SECTOR
                : GameScreenMode.NORMAL
        );

        onSelectTutorial(null);

        if (mode == GameScreenMode.BUILD_FIRST_SECTOR)
            gui.showTutorialQuestionDialog(assetManager, this);

        // Input handling
        InputMultiplexer input = new InputMultiplexer();
        input.addProcessor(new BackButtonProcessor(new Runnable() {
            @Override
            public void run() {
                gui.showExitDialog(GameScreen.this);
            }
        }));
        input.addProcessor(guiStage);
        input.addProcessor(new GestureDetector(new GeoBattleGestureListener()));
        Gdx.input.setInputProcessor(input);

        // guiStage.setDebugAll(true);

        game.setMessagePad(250, true);
    }

    public void switchTo(GameScreenMode mode) {
        if (this.mode != null) {
            switch (this.mode) {
                case BUILD:
                    game.getExternalAPI().server.cancelBuildEvent();
                    break;
                case BUILD_FIRST_SECTOR:
                case BUILD_SECTOR:
                    game.getExternalAPI().server.cancelSectorBuildEvent();
                    break;
                case DESTROY:
                    game.getExternalAPI().server.cancelDestroyEvent();
                    break;
                case SELECT_HANGARS:
                case SELECT_SECTOR:
                    game.getExternalAPI().server.cancelAttackEvent();
                    break;
            }
        }

        if (mode == null)
            return;

        this.mode = mode;
        map.setScreenMode(mode, true);
        gui.setMode(mode);
    }

    public void switchToNormalMode() {
        switchTo(GameScreenMode.NORMAL);
    }

    public void onBuildSectorMode() {
        if (mode == GameScreenMode.NORMAL)
            switchTo(GameScreenMode.BUILD_SECTOR);
        else if (mode == GameScreenMode.BUILD_SECTOR)
            switchTo(GameScreenMode.NORMAL);
    }

    // Invokes when user wants to switch build mode
    public void onBuildMode() {
        if (mode == GameScreenMode.NORMAL)
            switchTo(GameScreenMode.BUILD);
        else if (mode == GameScreenMode.BUILD)
            switchTo(GameScreenMode.NORMAL);
    }

    // Invokes when user wants to switch destroy mode
    public void onDestroyMode() {
        if (mode == GameScreenMode.NORMAL)
            switchTo(GameScreenMode.DESTROY);
        else if (mode == GameScreenMode.DESTROY)
            switchTo(GameScreenMode.NORMAL);
    }

    public void onSelectHangarsMode() {
        if (mode == GameScreenMode.NORMAL || mode == GameScreenMode.SELECT_SECTOR)
            switchTo(GameScreenMode.SELECT_HANGARS);
        else if (mode == GameScreenMode.SELECT_HANGARS)
            switchTo(GameScreenMode.NORMAL);
    }

    public void onSelectSectorMode() {
        if (mode == GameScreenMode.NORMAL || mode == GameScreenMode.SELECT_HANGARS)
            switchTo(GameScreenMode.SELECT_SECTOR);
        else if (mode == GameScreenMode.SELECT_SECTOR)
            switchTo(GameScreenMode.NORMAL);
    }

    // Invokes when player wants to move to its geolocation
    public void onMoveToPlayer() {
        map.moveToPlayer();
    }

    public void onMoveToBase(int playerId) {
        map.moveToBase(playerId);
    }

    // Invokes when player wants to move to its base
    public void onMoveToBase() {
        map.moveToBase();
    }

    public void onTutorialMessage() {
        if (tutorial != null && tutorial.getCurrent() != null)
            gui.showTutorialMessage(this, tutorial.getCurrent());
    }

    // Sets tutorial
    public void onSelectTutorial(Tutorial tutorial) {
        if (this.tutorial != null && this.tutorial.getCurrent() != null)
            this.tutorial.getCurrent().onEnd(this, gui, gameState);

        this.tutorial = tutorial;
        gui.tutorialMessageButton.setVisible(this.tutorial != null);
        if (this.tutorial != null && this.tutorial.getCurrent() != null) {
            this.tutorial.getCurrent().onBegin(this, gui, gameState);
            gui.showTutorialMessage(this, this.tutorial.getCurrent());
        }
    }

    // Returns camera
    public GeoBattleCamera getCamera() {
        return camera;
    }

    public GameEvents getGameEvents() {
        return gameEvents;
    }

    public GameScreenGUI getGUI() {
        return gui;
    }

    // Handles keyboard input
    private void handleInput(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1))
            guiStage.setDebugAll(!guiStage.getActors().first().getDebug());

        if (Gdx.input.isKeyJustPressed(Input.Keys.F2))
            debugMode = !debugMode;

        final float speed = camera.viewportWidth * delta;

        if (Gdx.input.isKeyPressed(Input.Keys.A))
            camera.position.x -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.D))
            camera.position.x += speed;
        if (Gdx.input.isKeyPressed(Input.Keys.W))
            camera.position.y += speed;
        if (Gdx.input.isKeyPressed(Input.Keys.S))
            camera.position.y -= speed;

        if (Gdx.input.isKeyPressed(Input.Keys.Q))
            camera.zoomIn(delta);
        if (Gdx.input.isKeyPressed(Input.Keys.E))
            camera.zoomOut(delta);

        if (Gdx.input.isKeyJustPressed(Input.Keys.B))
            onBuildMode();
        if (Gdx.input.isKeyJustPressed(Input.Keys.N))
            onDestroyMode();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            game.onExitGame(gameEvents.authInfo);
    }

    // Renders game screen
    @Override
    public void render(float delta) {
        gameEvents.onTick(delta);

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        handleInput(delta);

        tilesStage.act(delta);
        guiStage.act(delta);

        if (!gui.isTutorialMessageShown() && tutorial != null && tutorial.getCurrent() != null && tutorial.getCurrent().update(this, gui, gameState)) {
            tutorial.getCurrent().onEnd(this, gui, gameState);
            tutorial.nextStep();
            if (tutorial.getCurrent() != null) {
                tutorial.getCurrent().onBegin(this, gui, gameState);
                gui.showTutorialMessage(this, tutorial.getCurrent());
            } else {
                gui.tutorialMessageButton.setVisible(false);
            }
        }

        gui.resourcesLabel.setText(String.valueOf((int) gameState.getResources()));
        if (map.getPointedSector() != null) {
            Sector pointedSector = map.getPointedSector();
            gui.sectorInfo.setVisible(true);
            gui.energyLabel.setText(String.valueOf(pointedSector.getEnergy()));
            gui.healthLabel.setText(String.valueOf((int) pointedSector.getHealth()));
            gui.nameLabel.setText(gameState.getPlayer(pointedSector.playerId).getName());
        } else {
            gui.sectorInfo.setVisible(false);
        }

        if (mode != GameScreenMode.NORMAL)
            gui.currentModeLabel.setText(getI18NBundle().get(String.format("mode%s", mode.toString())));
        else
            gui.currentModeLabel.setText("");

        BuildingType selectedBuildingType = map.getSelectedBuildingType();

        if (mode != GameScreenMode.BUILD || selectedBuildingType.maxCount == Integer.MAX_VALUE) {
            gui.maxBuildingCountLabel.setText("");
        } else {
            gui.maxBuildingCountLabel.setText(String.format(
                    Locale.US,
                    "%d / %d",
                    gameState.getCurrentPlayer().getCount(selectedBuildingType),
                    selectedBuildingType.maxCount
            ));
        }

        gui.onBuildingSelected(map.getPointedBuilding());

//        if (debugMode) {
//            gui.debugInfo.setText(String.format(
//                    Locale.US,
//                    "%d FPS\nPointed tile: %s, %s\nPointed object: %s\nLoading tiles:%d\nLoaded tiles: %d\nMemory used: %d MB",
//                    Gdx.graphics.getFramesPerSecond(),
//                    map.getPointedTile() == null ? "<null>" : map.getPointedTile().x,
//                    map.getPointedTile() == null ? "<null>" : map.getPointedTile().y,
//                    map.getPointedBuilding() == null
//                            ? (map.getPointedSector() == null ? "<null>" : "sector")
//                            : map.getPointedBuilding().getBuildingType().toString(),
//                    map.getTileCounter().getRequestedCount(),
//                    map.getTileCounter().getLoadedCount(),
//                    Gdx.app.getJavaHeap() >> 20
//            ));
//        } else {
//            gui.debugInfo.setText("");
//        }

        if (gameState.getCurrentPlayer().getCount(BuildingType.RESEARCH_CENTER) > 0) {
            gui.researchDialog.setResearchInfo(gameState.getCurrentPlayer().getResearchInfo());
            gui.researchDialog.unlockButtons((int) gameState.getResources());
        } else
            gui.researchDialog.lockButtons();

        tilesStage.draw();

        playerDataSpriteBatch.begin();

        Iterator<PlayerState> players = gameState.getPlayers();
        while (players.hasNext()) {
            PlayerState player = players.next();
            IntPoint centerPoint = player.getCenterPoint();
            if (centerPoint != null) {
                float worldX = CoordinateConverter.subTilesToWorld(centerPoint.x, map.getXOffset(), GeoBattleConst.SUBDIVISION);
                float worldY = CoordinateConverter.subTilesToWorld(centerPoint.y, map.getYOffset(), GeoBattleConst.SUBDIVISION);

                int screenX = (int) ((worldX - (camera.position.x - camera.viewportWidth / 2)) / camera.viewportWidth * Gdx.graphics.getWidth());
                int screenY = (int) ((worldY - (camera.position.y - camera.viewportHeight / 2)) / camera.viewportHeight * Gdx.graphics.getHeight());

                if (
                        screenX >= Gdx.graphics.getWidth() / 6 &&
                        screenX <= Gdx.graphics.getWidth() * 5 / 6 &&
                        screenY >= Gdx.graphics.getHeight() / 6 &&
                        screenY <= Gdx.graphics.getHeight() * 5 / 6
                ) {
                    screenX = (int) (Gdx.graphics.getWidth() / 2 + (screenX - Gdx.graphics.getWidth() / 2) * 1.5f);
                    screenY = (int) (Gdx.graphics.getHeight() / 2 + (screenY - Gdx.graphics.getHeight() / 2) * 1.5f);

                    font.setColor(player.getColor());
                    font.draw(
                            playerDataSpriteBatch, player.getName(), screenX,
                            screenY + font.getCapHeight() / 2, 0,
                            Align.center, false
                    );
                }
            }
        }

        playerDataSpriteBatch.end();

        guiStage.draw();
    }

    // Invokes when window is resized
    @Override
    public void resize(int width, int height) {
        camera.resize(width, height);
        guiStage.getViewport().update(width, height);
        gui.reset(this);
    }

    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
    @Override
    public void dispose() {
        map.dispose();
    }

    public void onExit() {
        game.switchToLoginScreen();
    }

    // Gesture listener for game screen
    private class GeoBattleGestureListener extends GestureDetector.GestureAdapter {
        // Camera's initial viewport width at the beginning of zoom gesture
        private Float initialViewportWidth;

        // Camera's initial viewport height at the beginning of zoom gesture
        private Float initialViewportHeight;

        @Override
        public boolean tap(float x, float y, int count, int button) {
            Vector3 point = new Vector3();
            camera.getPickRay(x, y).getEndPoint(point, 0);

            map.setPointedTile(point.x, point.y, false);

            return true;
        }

        // Moves camera around
        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            camera.translate(
                    -camera.viewportWidth * deltaX / Gdx.graphics.getWidth(),
                    camera.viewportHeight * deltaY / Gdx.graphics.getHeight()
            );

            camera.fix(map.getXOffset(), map.getYOffset());

            return true;
        }

        @Override
        public boolean zoom(float initialDistance, float distance) {
            if (initialViewportWidth == null || initialViewportHeight == null) {
                initialViewportWidth = camera.viewportWidth;
                initialViewportHeight = camera.viewportHeight;
            }

            float scale = initialDistance / distance;
            camera.viewportWidth = initialViewportWidth * scale;
            camera.viewportHeight = initialViewportHeight * scale;

            camera.fix(map.getXOffset(), map.getYOffset());

            return true;
        }

        @Override
        public void pinchStop() {
            initialViewportWidth = null;
            initialViewportHeight = null;
        }
    }
}
