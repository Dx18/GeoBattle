package geobattle.geobattle.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;

import java.util.Iterator;
import java.util.Locale;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.game.buildings.Sector;
import geobattle.geobattle.map.GeoBattleCamera;
import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.screens.BackButtonProcessor;
import geobattle.geobattle.server.AuthInfo;
import geobattle.geobattle.server.ExternalAPI;

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

    // Constructor
    public GameScreen(ExternalAPI externalAPI, GameState gameState, AssetManager assetManager, AuthInfo authInfo, GeoBattle game) {
        this.assetManager = assetManager;
        this.gameState = gameState;
        this.game = game;

        // Map rendering stuff
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        camera = new GeoBattleCamera(width, height);
        tilesStage = new Stage(new ScalingViewport(Scaling.stretch, width, height, camera), new SpriteBatch(8191));
        map = new GeoBattleMap(
                externalAPI.tileRequestPool,
                externalAPI.geolocationAPI,
                camera,
                gameState,
                assetManager,
                game.getSoundVolume()
        );
        tilesStage.addActor(map);

        this.gameEvents = new GameEvents(externalAPI.server, externalAPI.oSAPI, gameState, authInfo, this, map, game);
        map.setSelectedBuildingType(BuildingType.GENERATOR);

        this.debugMode = true;

        tilesStage.setDebugAll(false);
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

        game.setMessagePad(Gdx.graphics.getHeight() * 2 / 3f, true);
    }

    public void switchTo(GameScreenMode mode) {
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

    // Returns camera
    public GeoBattleCamera getCamera() {
        return camera;
    }

    public GameEvents getGameEvents() {
        return gameEvents;
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
            int count = 0;
            Iterator<Building> buildings = gameState.getCurrentPlayer().getAllBuildings();
            while (buildings.hasNext())
                if (buildings.next().getBuildingType() == selectedBuildingType)
                    count++;

            gui.maxBuildingCountLabel.setText(String.format(
                    Locale.US,
                    "%d / %d",
                    count, selectedBuildingType.maxCount
            ));
        }

        gui.onBuildingSelected(map.getPointedBuilding());

        if (debugMode) {
            gui.debugInfo.setText(String.format(
                    Locale.US,
                    "%d FPS\nPointed tile: %s, %s\nPointed object: %s\nLoading tiles:%d\nLoaded tiles: %d\nMemory used: %d MB",
                    Gdx.graphics.getFramesPerSecond(),
                    map.getPointedTile() == null ? "<null>" : map.getPointedTile().x,
                    map.getPointedTile() == null ? "<null>" : map.getPointedTile().y,
                    map.getPointedBuilding() == null
                            ? (map.getPointedSector() == null ? "<null>" : "sector")
                            : map.getPointedBuilding().getBuildingType().toString(),
                    map.getTileCounter().getRequestedCount(),
                    map.getTileCounter().getLoadedCount(),
                    Gdx.app.getJavaHeap() >> 20
            ));
        } else {
            gui.debugInfo.setText("");
        }

        if (gameState.getCurrentPlayer().getResearchCenters().hasNext())
            gui.researchDialog.unlockButtons();
        else
            gui.researchDialog.lockButtons();

        tilesStage.draw();

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
    private class GeoBattleGestureListener implements GestureDetector.GestureListener {
        @Override
        public boolean touchDown(float x, float y, int pointer, int button) { return false; }
        @Override
        public boolean tap(float x, float y, int count, int button) {
            Vector3 point = new Vector3();
            camera.getPickRay(x, y).getEndPoint(point, 0);

            map.setPointedTile(point.x, point.y, false);
            Building pointed = map.getPointedBuilding();

            return true;
        }
        @Override
        public boolean longPress(float x, float y) { return false; }
        @Override
        public boolean fling(float velocityX, float velocityY, int button) { return false; }

        // Moves camera around
        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            camera.translate(
                    -camera.viewportWidth * deltaX / Gdx.graphics.getWidth(),
                    camera.viewportHeight * deltaY / Gdx.graphics.getHeight()
            );

            return true;
        }

        @Override
        public boolean panStop(float x, float y, int pointer, int button) { return false; }
        @Override
        public boolean zoom(float initialDistance, float distance) { return false; }
        @Override
        public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) { return false; }
        @Override
        public void pinchStop() {}
    }
}
