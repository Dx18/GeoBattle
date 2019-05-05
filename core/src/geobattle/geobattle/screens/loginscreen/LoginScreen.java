package geobattle.geobattle.screens.loginscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.game.actionresults.MatchBranch;
import geobattle.geobattle.screens.settingsscreen.SettingsScreen;
import geobattle.geobattle.server.AuthorizationResult;
import geobattle.geobattle.server.Callback;
import geobattle.geobattle.server.ExternalAPI;
import geobattle.geobattle.server.RegistrationResult;

// Login screen
public final class LoginScreen implements Screen {
    // External API
    private final ExternalAPI externalAPI;

    // Asset manager
    private final AssetManager assetManager;

    // Stage where all GUI is
    private Stage guiStage;

    // GUI
    private LoginScreenGUI gui;

    // Reference to game
    private final GeoBattle game;

    public LoginScreen(ExternalAPI externalAPI, AssetManager assetManager, GeoBattle game) {
        this.externalAPI = externalAPI;
        this.assetManager = assetManager;
        this.game = game;
    }

    // Shows screen
    @Override
    public void show() {
        guiStage = new Stage();
        gui = new LoginScreenGUI(assetManager, this, guiStage);

        Gdx.input.setInputProcessor(guiStage);
    }

    // Renders login screen
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        guiStage.act();

        guiStage.draw();
    }

    // Invokes when player tries to login
    public void onLogin() {
        String userName = gui.loginUserName.getText().trim();
        String password = gui.loginPassword.getText();

        if (!userName.isEmpty() && !password.isEmpty())
            externalAPI.server.login(userName, password, new Callback<AuthorizationResult>() {
                @Override
                public void onResult(final AuthorizationResult result) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            onAuthorizationResult(result);
                        }
                    });
                }
            });
    }

    private void onAuthorizationResult(AuthorizationResult result) {
        result.match(
                new MatchBranch<AuthorizationResult.Success>() {
                    @Override
                    public void onMatch(AuthorizationResult.Success success) {
                        game.onAuthInfoObtained(success.authInfo);
                    }
                },
                new MatchBranch<AuthorizationResult.PairNotFound>() {
                    @Override
                    public void onMatch(AuthorizationResult.PairNotFound pairNotFound) {
                        externalAPI.oSAPI.showMessage("Cannot login: pair not found");
                        // Gdx.app.log("GeoBattle", "Cannot login: pair not found");
                    }
                }
        );
    }

    // Invokes when player tries to register
    public void onRegister() {
        String userName = gui.registerUserName.getText().trim();
        String email = gui.registerEmail.getText().trim();
        String password = gui.registerPassword.getText();
        String repeatPassword = gui.registerRepeatPassword.getText();
        Color color = gui.pickColorResult.getColor();

        if (!userName.isEmpty() && !email.isEmpty() && !password.isEmpty() && password.equals(repeatPassword))
            externalAPI.server.register(userName, email, password, color, new Callback<RegistrationResult>() {
                @Override
                public void onResult(final RegistrationResult result) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            onRegistrationResult(result);
                        }
                    });
                }
            });
    }

    private void onRegistrationResult(RegistrationResult result) {
        result.match(
                new MatchBranch<RegistrationResult.Success>() {
                    @Override
                    public void onMatch(RegistrationResult.Success success) {
                        game.onAuthInfoObtained(success.authInfo);
                    }
                },
                new MatchBranch<RegistrationResult.InvalidEmail>() {
                    @Override
                    public void onMatch(RegistrationResult.InvalidEmail invalidEmail) {
                        externalAPI.oSAPI.showMessage("Cannot register: invalid email");
                        // Gdx.app.log("GeoBattle", "Cannot register: invalid email");
                    }
                },
                new MatchBranch<RegistrationResult.EmailExists>() {
                    @Override
                    public void onMatch(RegistrationResult.EmailExists emailExists) {
                        externalAPI.oSAPI.showMessage("Cannot register: player with same email exists");
                        // Gdx.app.log("GeoBattle", "Cannot register: player with same email exists");
                    }
                },
                new MatchBranch<RegistrationResult.InvalidNameLength>() {
                    @Override
                    public void onMatch(RegistrationResult.InvalidNameLength invalidNameLength) {
                        externalAPI.oSAPI.showMessage("Cannot register: invalid name length");
                        // Gdx.app.log("GeoBattle", "Cannot register: invalid name length");
                    }
                },
                new MatchBranch<RegistrationResult.InvalidPasswordLength>() {
                    @Override
                    public void onMatch(RegistrationResult.InvalidPasswordLength invalidPasswordLength) {
                        externalAPI.oSAPI.showMessage("Cannot register: invalid password length");
                        // Gdx.app.log("GeoBattle", "Cannot register: invalid password length");
                    }
                },
                new MatchBranch<RegistrationResult.InvalidNameSymbols>() {
                    @Override
                    public void onMatch(RegistrationResult.InvalidNameSymbols invalidNameSymbols) {
                        externalAPI.oSAPI.showMessage("Cannot register: invalid symbols in name");
                        // Gdx.app.log("GeoBattle", "Cannot register: invalid symbols in name");
                    }
                },
                new MatchBranch<RegistrationResult.NameExists>() {
                    @Override
                    public void onMatch(RegistrationResult.NameExists nameExists) {
                        externalAPI.oSAPI.showMessage("Cannot register: player with same name exists");
                        // Gdx.app.log("GeoBattle", "Cannot register: player with same name exists");
                    }
                }
        );
    }

    public void onSettings() {
        game.setScreen(new SettingsScreen(externalAPI, assetManager, game));
    }

    // Invokes when window is resized
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
