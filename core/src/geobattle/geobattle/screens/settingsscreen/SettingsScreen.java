package geobattle.geobattle.screens.settingsscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.I18NBundle;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.screens.BackButtonProcessor;
import geobattle.geobattle.server.ExternalAPI;

public final class SettingsScreen implements Screen {
    // External API
    private final ExternalAPI externalAPI;

    // Asset manager
    private final AssetManager assetManager;

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

    public SettingsScreen(ExternalAPI externalAPI, AssetManager assetManager, GeoBattle game) {
        this.externalAPI = externalAPI;
        this.assetManager = assetManager;
        this.game = game;

        background = assetManager.get(GeoBattleAssets.SETTINGS_BACKGROUND, Texture.class);
        batch = new SpriteBatch();
    }

    // Shows screen
    @Override
    public void show() {
        guiStage = new Stage();
        gui = new SettingsScreenGUI(assetManager, this, externalAPI.oSAPI, guiStage);

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
    }

    public I18NBundle getI18NBundle() {
        return game.getI18NBundle();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        float screenRatio = (float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight();
        float textureRatio = (float) background.getWidth() / background.getHeight();

        float x, y, width, height;
        if (screenRatio >= textureRatio) {
            float scale = (float) Gdx.graphics.getHeight() / background.getHeight();

            x = (background.getWidth() * scale - Gdx.graphics.getWidth()) / 2;
            y = 0;
            width = background.getWidth() * scale;
            height = Gdx.graphics.getHeight();
        } else {
            float scale = (float) Gdx.graphics.getWidth() / background.getWidth();

            x = 0;
            y = (background.getHeight() * scale - Gdx.graphics.getHeight()) / 2;
            width = Gdx.graphics.getWidth();
            height = background.getHeight() * scale;
        }

        batch.begin();
        batch.draw(background, x, y, width, height);
        batch.end();

        guiStage.act();

        guiStage.draw();
    }

    public void onSaveSettings() {
        externalAPI.server.setAddress(
                externalAPI.oSAPI.loadValue("ip", "78.47.182.60"),
                Integer.parseInt(externalAPI.oSAPI.loadValue("port", "12000"))
        );
        game.setMusicVolume(
                Float.parseFloat(externalAPI.oSAPI.loadValue("musicVolume", "0.5"))
        );
        game.switchToMainMenuScreen();
    }

    @Override
    public void resize(int width, int height) {

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
