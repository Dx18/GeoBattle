package geobattle.geobattle.screens.settingsscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.I18NBundle;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.screens.BackButtonProcessor;
import geobattle.geobattle.util.GeoBattleMath;

public final class SettingsScreen implements Screen {
    // Stage where all GUI is
    private Stage guiStage;

    // GUI
    private SettingsScreenGUI gui;

    // Reference to game
    private final GeoBattle game;

    // Background texture
    private Texture background;

    // Sprite batch for drawing background
    private SpriteBatch batch;

    // Rectangle for background drawing
    private final Rectangle backgroundDrawRect;

    public SettingsScreen(AssetManager assetManager, GeoBattle game) {
        this.game = game;
        guiStage = new Stage();
        gui = new SettingsScreenGUI(assetManager, this, game.getExternalAPI().oSAPI, guiStage);

        background = assetManager.get(GeoBattleAssets.SETTINGS_BACKGROUND, Texture.class);
        batch = new SpriteBatch();

        backgroundDrawRect = new Rectangle(
                0, 0,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight()
        );
    }

    // Shows screen
    @Override
    public void show() {
        InputMultiplexer input = new InputMultiplexer();
        input.addProcessor(new BackButtonProcessor(new Runnable() {
            @Override
            public void run() {
                game.switchToMainMenuScreen();
            }
        }));
        input.addProcessor(guiStage);

        Gdx.input.setInputProcessor(input);
        Gdx.input.setCatchBackKey(true);

        game.setMessagePad(20, true);
    }

    public I18NBundle getI18NBundle() {
        return game.getI18NBundle();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        batch.begin();

        batch.draw(
                background,
                backgroundDrawRect.x, backgroundDrawRect.y,
                backgroundDrawRect.width, backgroundDrawRect.height
        );

        batch.end();

        guiStage.act();
        guiStage.draw();
    }

    public void onSaveSettings() {
        game.setMusicVolume(
                Float.parseFloat(game.getExternalAPI().oSAPI.loadValue("musicVolume", "0.5"))
        );
        game.setSoundVolume(
                Float.parseFloat(game.getExternalAPI().oSAPI.loadValue("soundVolume", "0.5"))
        );
        game.getExternalAPI().tileRequestPool.setMapQuality(
                game.getExternalAPI().oSAPI.loadValue("mapQuality", "mapQualityHigh")
        );
        game.switchToMainMenuScreen();
    }

    @Override
    public void resize(int width, int height) {
        guiStage.getViewport().update(width, height);
        guiStage.getViewport().apply();
        gui.reset(this);

        batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);

        backgroundDrawRect.set(GeoBattleMath.scaleToCover(
                background.getWidth(), background.getHeight(),
                width, height
        ));
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
