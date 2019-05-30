package geobattle.geobattle.server.implementation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;

import geobattle.geobattle.map.GeoBattleCamera;
import geobattle.geobattle.map.TileCounter;
import geobattle.geobattle.map.TileTree;
import geobattle.geobattle.server.MapRenderer;

// Renderer of real map
public final class RealMapRenderer implements MapRenderer {
    // Tree of tiles
    private final TileTree tiles;

    // Counter of tiles
    private final TileCounter counter;

    // Tile request pool
    private final TileRequestPool tileRequestPool;

    // X offset of map
    private final int xOffset;

    // Y offset of map
    private final int yOffset;

    public RealMapRenderer(final int xOffset, final int yOffset, TileRequestPool tileRequestPool) {
        this.tiles = new TileTree(xOffset, yOffset);
        this.counter = new TileCounter();
        this.tileRequestPool = tileRequestPool;
        this.xOffset = xOffset;
        this.yOffset = yOffset;

        tileRequestPool.setOnLoadListener(new TileRequestPool.TileRequestCallback() {
            @Override
            public void onLoad(final Pixmap pixmap, final int x, final int y, final int zoomLevel) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        tiles.setTile(pixmap, counter, xOffset, yOffset, x, y, zoomLevel);
                    }
                });
            }
        });
    }

    @Override
    public void drawAndReduceTiles(Batch batch, int xOffset, int yOffset, GeoBattleCamera camera) {
        final int zoomLevel = 20 - Math.max(1, (int)MathUtils.log2(camera.viewportWidth));

        final int startX = camera.getTileStartX(zoomLevel, xOffset);
        final int startY = camera.getTileStartY(zoomLevel, yOffset);
        final int endX = camera.getTileEndX(zoomLevel, xOffset);
        final int endY = camera.getTileEndY(zoomLevel, yOffset);

        for (int x = startX; x <= endX; x += (1 << (19 - zoomLevel)))
            for (int y = startY; y <= endY; y += (1 << (19 - zoomLevel))) {
                Texture tile = tiles.getTile(x, y, zoomLevel, xOffset, yOffset, tileRequestPool, counter);

                if (tile != null)
                    batch.draw(
                            tile,
                            x,
                            y,
                            1 << (19 - zoomLevel),
                            1 << (19 - zoomLevel)
                    );
            }

        if (counter.getLoadedCount() > 90) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    tiles.reduceTiles(
                            startX, startY,
                            endX, endY,
                            zoomLevel, 2,
                            counter
                    );
                }
            });
        }
    }

    @Override
    public void dispose() {
        tiles.dispose();
    }
}
