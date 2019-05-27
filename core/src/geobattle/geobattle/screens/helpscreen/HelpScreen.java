package geobattle.geobattle.screens.helpscreen;

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

public final class HelpScreen implements Screen {
    private final GeoBattle game;

    private final Stage guiStage;

    private final HelpScreenGUI gui;

    // Background texture
    private Texture background;

    // Sprite batch for drawing background
    private SpriteBatch batch;

    public HelpScreen(AssetManager assetManager, GeoBattle game) {
        this.game = game;

        background = assetManager.get(GeoBattleAssets.MAIN_MENU_BACKGROUND, Texture.class);
        batch = new SpriteBatch();

        guiStage = new Stage();
        gui = new HelpScreenGUI(assetManager, this, guiStage);
    }

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
