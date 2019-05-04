package geobattle.geobattle.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// Loading screen
public class LoadingScreen implements Screen {
    // Font for text
    private BitmapFont textFont;

    // Batch for font
    private SpriteBatch fontBatch;

    // Initializes font and batch
    @Override
    public void show() {
        textFont = new BitmapFont();
        fontBatch = new SpriteBatch();
    }

    // Renders "Loading..." text
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        fontBatch.begin();

        textFont.draw(fontBatch, "Loading...", 40, 100);

        fontBatch.end();
    }

    @Override
    public void resize(int width, int height) {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
    @Override
    public void dispose() {}
}
