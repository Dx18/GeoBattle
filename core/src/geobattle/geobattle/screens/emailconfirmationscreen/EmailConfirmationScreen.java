package geobattle.geobattle.screens.emailconfirmationscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.actionresults.EmailConfirmationResult;
import geobattle.geobattle.actionresults.ResendEmailResult;
import geobattle.geobattle.server.Callback;
import geobattle.geobattle.server.ExternalAPI;

public final class EmailConfirmationScreen implements Screen {
    private final ExternalAPI externalAPI;

    private final AssetManager assetManager;

    private final GeoBattle game;

    private final Stage guiStage;

    private final EmailConfirmationScreenGUI gui;

    public EmailConfirmationScreen(ExternalAPI externalAPI, AssetManager assetManager, GeoBattle game, String name) {
        this.externalAPI = externalAPI;
        this.assetManager = assetManager;
        this.game = game;
        guiStage = new Stage();
        gui = new EmailConfirmationScreenGUI(assetManager, this, guiStage, name);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(guiStage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

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
