package geobattle.geobattle.map;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;

// Camera for `GeoBattleMap`
public final class GeoBattleCamera extends OrthographicCamera {
    // Initial scale of camera (tiles / screen (by width))
    private static final float INITIAL_SCALE = 10;

    // Min width of camera
    private float minCameraWidth;

    // Min height of camera
    private float minCameraHeight;

    // Creates camera
    // Sets size of camera
    // Resets zoom
    public GeoBattleCamera(int width, int height) {
        resize(width, height);
        resetZoom();
    }

    // Resets zoom of camera to default
    public void resetZoom() {
        viewportWidth = minCameraWidth * INITIAL_SCALE;
        viewportHeight = minCameraHeight * INITIAL_SCALE;
    }

    // Resizes camera
    // `width` and `height` - new size of screen
    public void resize(int width, int height) {
        minCameraWidth = 0.5f;
        minCameraHeight = (float)height / width * minCameraWidth;

        viewportHeight = minCameraHeight * viewportWidth / minCameraWidth;
    }

    // Zooms camera
    private void zoom(float delta, float zoomFactor) {
        final float factor = 1 - (1 - zoomFactor) * delta;

        viewportWidth = MathUtils.clamp(viewportWidth * factor, minCameraWidth, 1 << 19);
        viewportHeight = viewportWidth * minCameraHeight / minCameraWidth;
    }

    // Zooms in
    public void zoomIn(float delta) {
        zoom(delta, 0.5f);
    }

    // Zooms out
    public void zoomOut(float delta) {
        zoom(delta, 2f);
    }

    // Returns X of first tile to render
    public int getTileStartX(int zoomLevel, int xOffset) {
        return (((MathUtils.floor(position.x - viewportWidth / 2) + xOffset) >> (19 - zoomLevel)) << (19 - zoomLevel)) - xOffset;
    }

    // Returns Y of first tile to render
    public int getTileStartY(int zoomLevel, int yOffset) {
        return (((MathUtils.floor(position.y - viewportHeight / 2) + yOffset) >> (19 - zoomLevel)) << (19 - zoomLevel)) - yOffset;
    }

    // Returns X of last tile to render
    public int getTileEndX(int zoomLevel, int xOffset) {
        return (((MathUtils.floor(position.x + viewportWidth / 2) + xOffset) >> (19 - zoomLevel)) << (19 - zoomLevel)) - xOffset;
    }

    // Returns Y of last tile to render
    public int getTileEndY(int zoomLevel, int yOffset) {
        return (((MathUtils.floor(position.y + viewportHeight / 2) + yOffset) >> (19 - zoomLevel)) << (19 - zoomLevel)) - yOffset;
    }
}
