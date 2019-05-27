package geobattle.geobattle.screens.mainmenuscreen;

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

public final class MainMenuScreen implements Screen {
    private final GeoBattle game;

    private final Stage guiStage;

    private final MainMenuScreenGUI gui;

    private final Texture background;

    private final Texture title;

    private final SpriteBatch batch;

    public MainMenuScreen(AssetManager assetManager, GeoBattle game) {
        this.game = game;

        guiStage = new Stage();
        gui = new MainMenuScreenGUI(assetManager, this, guiStage);

        background = assetManager.get(GeoBattleAssets.MAIN_MENU_BACKGROUND);
        title = assetManager.get(GeoBattleAssets.MAIN_MENU_TITLE);
        batch = new SpriteBatch();
    }

    public I18NBundle getI18NBundle() {
        return game.getI18NBundle();
    }

    public void onPlay() {
        game.switchToLoginScreen();
//        game.switchToSelectServerScreen();
    }

    public void onSettings() {
        game.switchToSettingsScreen();
    }

    public void onExit() {
        Gdx.app.exit();
    }

    @Override
    public void show() {
        InputMultiplexer input = new InputMultiplexer();
        input.addProcessor(new BackButtonProcessor(new Runnable() {
            @Override
            public void run() {
                game.dispose();
                Gdx.app.exit();
            }
        }));
        input.addProcessor(guiStage);

        Gdx.input.setInputProcessor(input);
        Gdx.input.setCatchBackKey(true);

        game.setMessagePad(Gdx.graphics.getHeight() / 3f, false);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        float screenRatio = (float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight();
        float textureRatio = (float) background.getWidth() / background.getHeight();

        float titleScreenRatio = 3 * screenRatio;
        float titleTextureRatio = (float) title.getWidth() / title.getHeight();

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

        float titleX, titleY, titleWidth, titleHeight;
        if (titleScreenRatio >= titleTextureRatio) {
            float scale = (float) Gdx.graphics.getHeight() / 3 / title.getHeight();

            titleX = (Gdx.graphics.getWidth() - title.getWidth() * scale) / 2;
            titleY = Gdx.graphics.getHeight() * 2 / 3f;
            titleWidth = title.getWidth() * scale;
            titleHeight = Gdx.graphics.getHeight() / 3f;
        } else {
            float scale = (float) Gdx.graphics.getWidth() / title.getWidth();

            titleX = 0;
            titleY = Gdx.graphics.getHeight() * 2 / 3f + (Gdx.graphics.getHeight() / 3f - title.getHeight() * scale) / 2;
            titleWidth = Gdx.graphics.getWidth();
            titleHeight = title.getHeight() * scale;
        }

        batch.begin();
        batch.draw(background, x, y, width, height);
        batch.draw(title, titleX, titleY, titleWidth, titleHeight);
        batch.end();

        guiStage.act();
        guiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        guiStage.getViewport().update(width, height);
        gui.reset(this);
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
