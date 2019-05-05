package geobattle.geobattle.screens.settingsscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.screens.loginscreen.LoginScreen;
import geobattle.geobattle.server.ExternalAPI;

public final class SettingsScreen implements Screen {
    // External API
    private final ExternalAPI externalAPI;

    // Asset manager
    private final AssetManager assetManager;

    // Stage where all GUI is
    private Stage guiStage;

    // GUI
    private SettingsScreenGUI gui;

    // Reference to game
    private final GeoBattle game;

    public SettingsScreen(ExternalAPI externalAPI, AssetManager assetManager, GeoBattle game) {
        this.externalAPI = externalAPI;
        this.assetManager = assetManager;
        this.game = game;
    }

    // Shows screen
    @Override
    public void show() {
        guiStage = new Stage();
        gui = new SettingsScreenGUI(assetManager, this, guiStage);

        Gdx.input.setInputProcessor(guiStage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        guiStage.act();

        guiStage.draw();
    }

    // Saves IP address and port
    public void setAddress(String ip, int port) {
        externalAPI.server.setAddress(ip, port);
    }

    // Returns IP address of server
    public String getIPAddress() {
        return externalAPI.server.getIPAddress();
    }

    // Returns port of server
    public int getPort() {
        return externalAPI.server.getPort();
    }

    public void exit() {
        game.setScreen(new LoginScreen(externalAPI, assetManager, game));
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
