package geobattle.geobattle.screens.emailconfirmationscreen;

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
import geobattle.geobattle.actionresults.EmailConfirmationResult;
import geobattle.geobattle.actionresults.ResendEmailResult;
import geobattle.geobattle.screens.BackButtonProcessor;
import geobattle.geobattle.server.Callback;
import geobattle.geobattle.util.GeoBattleMath;

public final class EmailConfirmationScreen implements Screen {
    private final GeoBattle game;

    private final Stage guiStage;

    private final EmailConfirmationScreenGUI gui;

    private final Texture background;

    private final SpriteBatch batch;

    // Rectangle for background drawing
    private final Rectangle backgroundDrawRect;

    public EmailConfirmationScreen(AssetManager assetManager, GeoBattle game, String name) {
        this.game = game;
        guiStage = new Stage();
        gui = new EmailConfirmationScreenGUI(assetManager, this, guiStage, name);

        background = assetManager.get(GeoBattleAssets.MAIN_MENU_BACKGROUND);
        batch = new SpriteBatch();

        backgroundDrawRect = new Rectangle(
                0, 0,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight()
        );
    }

    @Override
    public void show() {
        InputMultiplexer input = new InputMultiplexer();
        input.addProcessor(new BackButtonProcessor(new Runnable() {
            @Override
            public void run() {
                game.switchToLoginScreen();
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

    public void onConfirm(String playerName, int code) {
        playerName = playerName.trim();
        if (playerName.isEmpty() || code < 0 || code >= 10000)
            return;

        game.getExternalAPI().server.requestEmailConfirmation(playerName, code, new Callback<EmailConfirmationResult>() {
            @Override
            public void onResult(final EmailConfirmationResult result) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onEmailConfirmationResult(result);
                    }
                });
            }
        }, null);
    }

    private void onEmailConfirmationResult(EmailConfirmationResult result) {
        if (result != null)
            result.apply(game, null);
    }

    public void onResend(String playerName) {
        playerName = playerName.trim();
        if (playerName.isEmpty())
            return;

        game.getExternalAPI().server.requestEmailResend(playerName, new Callback<ResendEmailResult>() {
            @Override
            public void onResult(final ResendEmailResult result) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        onResendEmailResult(result);
                    }
                });
            }
        }, null);
    }

    private void onResendEmailResult(ResendEmailResult result) {
        if (result != null)
            result.apply(game, null);
    }

    public void onReturn() {
        game.switchToLoginScreen();
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
