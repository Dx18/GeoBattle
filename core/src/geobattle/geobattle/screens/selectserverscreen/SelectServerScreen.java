package geobattle.geobattle.screens.selectserverscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;

import geobattle.geobattle.GeoBattle;

public final class SelectServerScreen implements Screen {
    private final GeoBattle game;

    private final Stage guiStage;

    private final SelectServerScreenGUI gui;

    public SelectServerScreen(AssetManager assetManager, GeoBattle game) {
        this.game = game;

        guiStage = new Stage();
        gui = new SelectServerScreenGUI(assetManager, this, guiStage);

        guiStage.setDebugAll(true);
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
