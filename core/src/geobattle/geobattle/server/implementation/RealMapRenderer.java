package geobattle.geobattle.server.implementation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;

import geobattle.geobattle.GeoBattleConst;
import geobattle.geobattle.map.TileCounter;
import geobattle.geobattle.map.TileTree;
import geobattle.geobattle.server.MapRenderer;
import geobattle.geobattle.util.CoordinateConverter;
import geobattle.geobattle.util.IntRect;

// Renderer of real map
public final class RealMapRenderer implements MapRenderer {
    // Tree of tiles
    private final TileTree tiles;

    // Counter of tiles
    private final TileCounter counter;

    // Tile request pool
    private final TileRequestPool tileRequestPool;

    public RealMapRenderer(TileRequestPool tileRequestPool) {
        this.tiles = new TileTree();
        this.counter = new TileCounter();
        this.tileRequestPool = tileRequestPool;

        tileRequestPool.setOnLoadListener(new TileRequestPool.TileRequestCallback() {
            @Override
            public void onLoad(final Pixmap pixmap, final int x, final int y, final int zoomLevel) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        tiles.setTile(pixmap, counter, x, y, zoomLevel);
                    }
                });
            }
        });
    }

    @Override
    public void drawAndReduceTiles(Batch batch, int xOffset, int yOffset, IntRect visibleRect) {
        final int zoomLevel = 20 - Math.max(1, (int)MathUtils.log2(
                CoordinateConverter.subTilesToRealWorld(visibleRect.width, GeoBattleConst.SUBDIVISION)
        ));

//        final int startX = camera.getTileStartX(zoomLevel, xOffset);
//        final int startY = camera.getTileStartY(zoomLevel, yOffset);
//        final int endX = camera.getTileEndX(zoomLevel, xOffset);
//        final int endY = camera.getTileEndY(zoomLevel, yOffset);

        int startX = (int) CoordinateConverter.subTilesToRealWorld(visibleRect.x, GeoBattleConst.SUBDIVISION);
        startX -= startX % (1 << (TileTree.MAX_ZOOM_LEVEL - zoomLevel));

        int startY = (int) CoordinateConverter.subTilesToRealWorld(visibleRect.y, GeoBattleConst.SUBDIVISION);
        startY -= startY % (1 << (TileTree.MAX_ZOOM_LEVEL - zoomLevel));

        int endX = (int) CoordinateConverter.subTilesToRealWorld(visibleRect.x + visibleRect.width, GeoBattleConst.SUBDIVISION);
        endX -= endX % (1 << (TileTree.MAX_ZOOM_LEVEL - zoomLevel));

        int endY = (int) CoordinateConverter.subTilesToRealWorld(visibleRect.y + visibleRect.height, GeoBattleConst.SUBDIVISION);
        endY -= endY % (1 << (TileTree.MAX_ZOOM_LEVEL - zoomLevel));

        final IntRect finalVisibleRect = new IntRect(
                startX, startY,
                endX - startX + (1 << (TileTree.MAX_ZOOM_LEVEL - zoomLevel)),
                endY - startY + (1 << (TileTree.MAX_ZOOM_LEVEL - zoomLevel))
        );

        tileRequestPool.setVisibleData(finalVisibleRect, zoomLevel);

        int middle = startX + (endX - startX) / 3 - (endX - startX) / 3 % (1 << (19 - zoomLevel));

        for (int x = middle; x >= startX; x -= (1 << (19 - zoomLevel)))
            for (int y = startY; y <= endY; y += (1 << (19 - zoomLevel))) {
                Texture tile = tiles.getTile(x, y, zoomLevel, tileRequestPool, counter);

                if (tile != null)
                    batch.draw(
                            tile,
                            x - xOffset,
                            y - yOffset,
                            1 << (19 - zoomLevel),
                            1 << (19 - zoomLevel)
                    );
            }

        for (int x = endX; x > middle; x -= (1 << (19 - zoomLevel)))
            for (int y = startY; y <= endY; y += (1 << (19 - zoomLevel))) {
                Texture tile = tiles.getTile(x, y, zoomLevel, tileRequestPool, counter);

                if (tile != null)
                    batch.draw(
                            tile,
                            x - xOffset,
                            y - yOffset,
                            1 << (19 - zoomLevel),
                            1 << (19 - zoomLevel)
                    );
            }

        if (counter.getLoadedCount() > 90) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    tiles.reduceTiles(
                            finalVisibleRect,
                            zoomLevel, new TileTree.ReduceTilesOptions(2),
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
