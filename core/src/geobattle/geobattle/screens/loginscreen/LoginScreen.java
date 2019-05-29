package geobattle.geobattle.screens.loginscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
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
import geobattle.geobattle.util.GeoBattleMath;

// Login screen
public final class LoginScreen implements Screen {
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

    // Rectangle for background drawing
    private final Rectangle backgroundDrawRect;

    public LoginScreen(AssetManager assetManager, GeoBattle game) {
        this.assetManager = assetManager;
        this.game = game;

        guiStage = new Stage();
        gui = new LoginScreenGUI(assetManager, this, guiStage);

        background = assetManager.get(GeoBattleAssets.MAIN_MENU_BACKGROUND);
        batch = new SpriteBatch();

        backgroundDrawRect = new Rectangle(
                0, 0,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight()
        );

        game.setNetworkState(0);
    }

    // Shows screen
    @Override
    public void show() {
        InputMultiplexer input = new InputMultiplexer();
        input.addProcessor(new BackButtonProcessor(new Runnable() {
            @Override
            public void run() {
                game.switchToSelectServerScreen();
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

    // Invokes when player tries to login
    public void onLogin() {
        String userName = gui.loginUserName.getText().trim();

        if (userName.isEmpty()) {
            game.showMessage(game.getI18NBundle().get("typeUserName"));
            return;
        }

        String password = gui.loginPassword.getText();

        if (password.isEmpty()) {
            game.showMessage(game.getI18NBundle().get("typePassword"));
            return;
        }

        game.getExternalAPI().server.onAuthorizationEvent(userName, password, new Callback<AuthorizationResult>() {
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

        if (userName.isEmpty()) {
            game.showMessage(game.getI18NBundle().get("typeUserName"));
            return;
        }

        String email = gui.registerEmail.getText().trim();

        if (email.isEmpty()) {
            game.showMessage(game.getI18NBundle().get("typeEmail"));
            return;
        }

        String password = gui.registerPassword.getText();

        if (password.isEmpty()) {
            game.showMessage(game.getI18NBundle().get("typePassword"));
            return;
        }

        String repeatPassword = gui.registerRepeatPassword.getText();

        if (repeatPassword.isEmpty()) {
            game.showMessage(game.getI18NBundle().get("typeRepeatPassword"));
            return;
        }

        if (!password.equals(repeatPassword)) {
            game.showMessage(game.getI18NBundle().get("passwordsDoNotMatch"));
            return;
        }

        Color color = gui.pickColorResult.getColor();

        game.getExternalAPI().server.onRegistrationEvent(userName, email, password, color, new Callback<RegistrationResult>() {
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
        game.setScreen(new SettingsScreen(assetManager, game));
    }

    public void onEmailConfirmation() {
        game.setScreen(new EmailConfirmationScreen(assetManager, game, null));
    }

    // Invokes when window is resized
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
