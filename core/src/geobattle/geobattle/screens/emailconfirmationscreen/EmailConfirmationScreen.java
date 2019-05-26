package geobattle.geobattle.screens.emailconfirmationscreen;

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
import geobattle.geobattle.actionresults.EmailConfirmationResult;
import geobattle.geobattle.actionresults.ResendEmailResult;
import geobattle.geobattle.screens.BackButtonProcessor;
import geobattle.geobattle.server.Callback;
import geobattle.geobattle.server.ExternalAPI;

public final class EmailConfirmationScreen implements Screen {
    private final ExternalAPI externalAPI;

    private final AssetManager assetManager;

    private final GeoBattle game;

    private final Stage guiStage;

    private final EmailConfirmationScreenGUI gui;

    private final Texture background;

    private final SpriteBatch batch;

    public EmailConfirmationScreen(ExternalAPI externalAPI, AssetManager assetManager, GeoBattle game, String name) {
        this.externalAPI = externalAPI;
        this.assetManager = assetManager;
        this.game = game;
        guiStage = new Stage();
        gui = new EmailConfirmationScreenGUI(assetManager, this, guiStage, name);

        background = assetManager.get(GeoBattleAssets.MAIN_MENU_BACKGROUND);
        batch = new SpriteBatch();
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

    public void onConfirm(String playerName, int code) {
        playerName = playerName.trim();
        if (playerName.isEmpty() || code < 0 || code >= 10000)
            return;

        externalAPI.server.requestEmailConfirmation(playerName, code, new Callback<EmailConfirmationResult>() {
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

        externalAPI.server.requestEmailResend(playerName, new Callback<ResendEmailResult>() {
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
