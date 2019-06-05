package geobattle.geobattle.screens.selectserverscreen;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.screens.BackButtonProcessor;
import geobattle.geobattle.server.ServerAddress;
import geobattle.geobattle.util.GeoBattleMath;

public final class SelectServerScreen implements Screen {
    private final GeoBattle game;

    private final Stage guiStage;

    private final SelectServerScreenGUI gui;

    private final ArrayList<ServerAddress> customServers;

    // Texture of background
    private final Texture background;

    // Sprite batch used for drawing background
    private final SpriteBatch batch;

    // Rectangle for background drawing
    private final Rectangle backgroundDrawRect;

    public SelectServerScreen(AssetManager assetManager, GeoBattle game) {
        this.game = game;

        customServers = loadCustomServers();

        guiStage = new Stage();
        gui = new SelectServerScreenGUI(assetManager, this, guiStage);

        background = assetManager.get(GeoBattleAssets.MAIN_MENU_BACKGROUND);
        batch = new SpriteBatch();

        backgroundDrawRect = new Rectangle(
                0, 0,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight()
        );

        game.setNetworkState(0);
    }

    @Override
    public void show() {
        InputMultiplexer input = new InputMultiplexer();
        input.addProcessor(new BackButtonProcessor(new Runnable() {
            @Override
            public void run() {
                saveCustomServers(customServers);
                game.switchToMainMenuScreen();
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
    }

    public I18NBundle getI18NBundle() {
        return game.getI18NBundle();
    }

    public ArrayList<ServerAddress> getOfficialServers() {
        ArrayList<ServerAddress> result = new ArrayList<ServerAddress>();
        result.add(new ServerAddress("Main server", "82.146.61.124", 12000));
        return result;
    }

    public ArrayList<ServerAddress> getCustomServers() {
        return customServers;
    }

    private ArrayList<ServerAddress> loadCustomServers() {
        try {
            JsonArray serversJson = new JsonParser().parse(game.getExternalAPI().oSAPI.loadValue("customServers", "[]")).getAsJsonArray();

            boolean someIgnored = false;

            ArrayList<ServerAddress> servers = new ArrayList<ServerAddress>();
            for (JsonElement serverJson : serversJson) {
                try {
                    servers.add(ServerAddress.fromJson(serverJson.getAsJsonObject()));
                } catch (Exception e) {
                    someIgnored = true;
                }
            }

            if (someIgnored)
                saveCustomServers(servers);

            return servers;
        } catch (Exception e) {
            game.getExternalAPI().oSAPI.saveValue("customServers", "[]");
            return new ArrayList<ServerAddress>();
        }
    }

    private void saveCustomServers(ArrayList<ServerAddress> servers) {
        JsonArray serversJson = new JsonArray();
        for (ServerAddress server : servers)
            serversJson.add(server.toJson());
        game.getExternalAPI().oSAPI.saveValue("customServers", serversJson.toString());
    }

    public void onServerSelected(ServerAddress item) {
        game.getExternalAPI().server.setAddress(item.ip, item.port);
        saveCustomServers(customServers);
        game.switchToLoginScreen();
    }

    public ServerAddress onCreateServerAddress(String name, String ip, String port) {
        name = name.trim();

        if (name.isEmpty()) {
            game.showMessage(game.getI18NBundle().get("typeServerName"));
            return null;
        }

        ip = ip.trim();

        if (ip.isEmpty()) {
            game.showMessage(game.getI18NBundle().get("typeServerIp"));
            return null;
        }

        port = port.trim();

        if (port.isEmpty()) {
            game.showMessage(game.getI18NBundle().get("typeServerPort"));
            return null;
        }

        try {
            int parsedPort = Integer.parseInt(port);

            return new ServerAddress(name, ip, parsedPort);
        } catch (NumberFormatException e) {
            game.showMessage(game.getI18NBundle().get("typeServerPort"));
            return null;
        }
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
