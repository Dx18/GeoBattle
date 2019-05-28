package geobattle.geobattle.screens.mainmenuscreen;

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

public final class MainMenuScreen implements Screen {
    private final GeoBattle game;

    private final Stage guiStage;

    private final MainMenuScreenGUI gui;

    private final Texture background;

    private final Texture title;

    private final SpriteBatch batch;

    // Rectangle for background drawing
    private final Rectangle backgroundDrawRect;

    // Rectangle for title drawing
    private final Rectangle titleDrawRect;

    public MainMenuScreen(AssetManager assetManager, GeoBattle game) {
        this.game = game;

        guiStage = new Stage();
        gui = new MainMenuScreenGUI(assetManager, this, guiStage);

        background = assetManager.get(GeoBattleAssets.MAIN_MENU_BACKGROUND);
        title = assetManager.get(GeoBattleAssets.MAIN_MENU_TITLE);
        batch = new SpriteBatch();

        backgroundDrawRect = new Rectangle(
                0, 0,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight()
        );
        titleDrawRect = new Rectangle(
                0, Gdx.graphics.getHeight() * 2 / 3f,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight()
        );

        game.setNetworkState(0);
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

    public void onHelp() {
        game.switchToHelpScreen();
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

        game.setMessagePad(20, true);
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
        batch.draw(
                title,
                titleDrawRect.x, titleDrawRect.y + Gdx.graphics.getHeight() * 2 / 3f,
                titleDrawRect.width, titleDrawRect.height
        );

        batch.end();

        guiStage.act();
        guiStage.draw();
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

        titleDrawRect.set(GeoBattleMath.scaleToFit(
                title.getWidth(), title.getHeight(),
                width, height / 3f
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
