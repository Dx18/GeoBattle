package geobattle.geobattle.screens.emailconfirmationscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;

import java.util.Locale;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.game.actionresults.MatchBranch;
import geobattle.geobattle.server.Callback;
import geobattle.geobattle.server.EmailConfirmationResult;
import geobattle.geobattle.server.ExternalAPI;
import geobattle.geobattle.server.ResendEmailResult;

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
        });
    }

    private void onEmailConfirmationResult(EmailConfirmationResult result) {
        result.match(
                new MatchBranch<EmailConfirmationResult.EmailConfirmed>() {
                    @Override
                    public void onMatch(EmailConfirmationResult.EmailConfirmed emailConfirmed) {
                        game.onAuthInfoObtained(emailConfirmed.authInfo);
                    }
                },
                new MatchBranch<EmailConfirmationResult.WrongCode>() {
                    @Override
                    public void onMatch(EmailConfirmationResult.WrongCode wrongCode) {
                        externalAPI.oSAPI.showMessage(String.format(
                                Locale.US,
                                "Wrong code. Tries left: %d",
                                wrongCode.triesLeft
                        ));
                        if (wrongCode.triesLeft <= 0)
                            game.switchToLoginScreen();
                    }
                },
                new MatchBranch<EmailConfirmationResult.DoesNotExist>() {
                    @Override
                    public void onMatch(EmailConfirmationResult.DoesNotExist doesNotExist) {
                        externalAPI.oSAPI.showMessage("Player with same name already confirmed email or does not exist");
                    }
                },
                new MatchBranch<EmailConfirmationResult.MalformedJson>() {
                    @Override
                    public void onMatch(EmailConfirmationResult.MalformedJson malformedJson) {
                        externalAPI.oSAPI.showMessage("Cannot confirm email: JSON request is not well-formed. Probable bug. Tell the developers");
                    }
                },
                new MatchBranch<EmailConfirmationResult.IncorrectData>() {
                    @Override
                    public void onMatch(EmailConfirmationResult.IncorrectData incorrectData) {
                        externalAPI.oSAPI.showMessage("Cannot confirm email: value of field in request is not valid. Probable bug. Tell the developers");
                    }
                }
        );
    }

    public void onResend(String playerName) {
        playerName = playerName.trim();
        if (playerName.isEmpty())
            return;

        externalAPI.server.requestEmailResend(playerName, new Callback<ResendEmailResult>() {
            @Override
            public void onResult(ResendEmailResult result) {

            }
        });
    }

    private void onResendEmailResult(ResendEmailResult result) {
        result.match(
                new MatchBranch<ResendEmailResult.EmailResent>() {
                    @Override
                    public void onMatch(ResendEmailResult.EmailResent emailResent) {
                        externalAPI.oSAPI.showMessage("Check your email");
                    }
                },
                new MatchBranch<ResendEmailResult.DoesNotExist>() {
                    @Override
                    public void onMatch(ResendEmailResult.DoesNotExist doesNotExist) {
                        externalAPI.oSAPI.showMessage("Player with same name already confirmed email or does not exist");
                    }
                },
                new MatchBranch<ResendEmailResult.MalformedJson>() {
                    @Override
                    public void onMatch(ResendEmailResult.MalformedJson malformedJson) {
                        externalAPI.oSAPI.showMessage("Cannot resend email: JSON request is not well-formed. Probable bug. Tell the developers");
                    }
                },
                new MatchBranch<ResendEmailResult.IncorrectData>() {
                    @Override
                    public void onMatch(ResendEmailResult.IncorrectData incorrectData) {
                        externalAPI.oSAPI.showMessage("Cannot resend email: value of field in request is not valid. Probable bug. Tell the developers");
                    }
                }
        );
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
