package geobattle.geobattle.screens.loginscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.I18NBundle;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.actionresults.AuthorizationResult;
import geobattle.geobattle.actionresults.RegistrationResult;
import geobattle.geobattle.screens.BackButtonProcessor;
import geobattle.geobattle.screens.emailconfirmationscreen.EmailConfirmationScreen;
import geobattle.geobattle.screens.settingsscreen.SettingsScreen;
import geobattle.geobattle.server.Callback;
import geobattle.geobattle.server.ExternalAPI;

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

    // Texture of background
    private final Texture background;

    // Sprite batch used for drawing background
    private final SpriteBatch batch;

    public LoginScreen(ExternalAPI externalAPI, AssetManager assetManager, GeoBattle game) {
        this.externalAPI = externalAPI;
        this.assetManager = assetManager;
        this.game = game;

        background = assetManager.get(GeoBattleAssets.MAIN_MENU_BACKGROUND);
        batch = new SpriteBatch();
    }

    // Shows screen
    @Override
    public void show() {
        guiStage = new Stage();
        gui = new LoginScreenGUI(assetManager, this, guiStage);

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

    // Renders login screen
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
            }, null);
    }

    private void onAuthorizationResult(AuthorizationResult result) {
        if (result != null)
            result.apply(game, null);
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
            }, null);
    }

    private void onRegistrationResult(RegistrationResult result) {
        if (result != null)
            result.apply(game, null);
    }

    public void onSettings() {
        game.setScreen(new SettingsScreen(externalAPI, assetManager, game));
    }

    public void onEmailConfirmation() {
        game.setScreen(new EmailConfirmationScreen(externalAPI, assetManager, game, null));
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
