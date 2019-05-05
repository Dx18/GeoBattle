package geobattle.geobattle.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import java.util.Locale;

import geobattle.geobattle.server.implementation.TileRequestPool;

// Tree of tiles
public class TileTree {
    // Zoom level of current tile tree ([0; 19])
    private int zoomLevel;

    // X coordinate of tile tree
    private int x;

    // Y coordinate of tile tree
    private int y;

    // Texture of tile tree. May be null
    private Texture texture;

    // Is texture requested
    private boolean textureRequested;

    // Four subtrees. May be null
    private TileTree[] subtrees;

    // Constructor
    public TileTree(int zoomLevel, int x, int y, int xOffset, int yOffset) {
        this.zoomLevel = zoomLevel;
        this.x = (x + xOffset) - (x + xOffset) % (1 << (19 - zoomLevel)) - xOffset;
        this.y = (y + yOffset) - (y + yOffset) % (1 << (19 - zoomLevel)) - yOffset;

        Gdx.app.debug("GeoBattle", String.format(
                Locale.US,
                "Created TileTree with x = %d, y = %d, zoomLevel = %d",
                this.x,
                this.y,
                this.zoomLevel
        ));
    }

    // Constructor with xOffset and yOffset
    public TileTree(int xOffset, int yOffset) {
        this(0, 0, 0, xOffset, yOffset);
    }

    public void setTile(Pixmap tile, TileCounter tileCounter, int xOffset, int yOffset, int x, int y, int zoomLevel) {
        if (this.x + xOffset == x && this.y + yOffset == y && this.zoomLevel == zoomLevel) {
            setTile(tile, tileCounter);
        } else if (this.zoomLevel < zoomLevel) {
            int xNorm = x % getSize() / (getSize() >> 1);
            int yNorm = y % getSize() / (getSize() >> 1);

            if (xNorm < 0 || xNorm > 1 || yNorm < 0 || yNorm > 1)
                return;

            int index = yNorm * 2 + xNorm;
            subtrees[index].setTile(tile, tileCounter, xOffset, yOffset, x, y, zoomLevel);
        }
    }

    // Sets texture of tile and increments tree counter if needed
    private void setTile(Pixmap tile, TileCounter tileCounter) {
        if (tile == null && texture != null) {
            texture.dispose();
            texture = null;
            tileCounter.onUnload();
            textureRequested = false;
        } else if (tile != null && texture == null) {
            texture = new Texture(tile);
            tile.dispose();
            tileCounter.onLoad();
            textureRequested = false;
        } else if (tile != null) {
            texture.dispose();
            texture = new Texture(tile);
            tile.dispose();
            textureRequested = false;
        }
    }

    // Removes texture of tile and decrements tree counter if needed
//    private void removeTexture(TileCounter tileCounter) {
//        if (textureRequest != null) {
//            Gdx.app.log("GeoBattle", "Canceling request...");
//            tileCounter.onCancelRequest();
//            textureRequest.cancel();
//            textureRequest = null;
//        } else if (texture != null) {
//            Gdx.app.log("GeoBattle", "Disposing texture...");
//            tileCounter.onUnload();
//            texture.dispose();
//            texture = null;
//        }
//    }

    // Returns size of tile tree in tiles. Depends on zoom level
    public int getSize() {
        return 1 << (19 - zoomLevel);
    }

    // Returns texture of tile at specified location
    public Texture getTile(int x, int y, int zoomLevel, final int xOffset, final int yOffset, final TileRequestPool tileRequestPool, final TileCounter tileCounter) {
        if (this.zoomLevel == zoomLevel) {
            if (texture == null && !textureRequested) {
                tileRequestPool.put(new TileRequestPool.TileRequest(this.x + xOffset, this.y + yOffset, zoomLevel));
                tileCounter.onRequest();
                textureRequested = true;
            }
            return texture;
        } else if (this.zoomLevel < zoomLevel) {
            int xNorm = (x + xOffset) % getSize() / (getSize() >> 1);
            int yNorm = (y + yOffset) % getSize() / (getSize() >> 1);

            if (xNorm < 0 || xNorm > 1 || yNorm < 0 || yNorm > 1)
                return null;

            int index = yNorm * 2 + xNorm;
            if (subtrees == null)
                subtrees = new TileTree[4];
            if (subtrees[index] == null)
                subtrees[index] = new TileTree(this.zoomLevel + 1, x, y, xOffset, yOffset);

            return subtrees[index].getTile(x, y, zoomLevel, xOffset, yOffset, tileRequestPool, tileCounter);
        } else {
            return null;
        }
    }

    // Reduces number of tiles. Removes invisible ones
    public void reduceTiles(int startX, int startY, int endX, int endY, int zoomLevel, int zoomLevelDelta, TileCounter tileCounter) {
        if (
                Math.abs(zoomLevel - this.zoomLevel) > zoomLevelDelta ||
                startX > x + getSize() ||
                endX < x ||
                startY > y + getSize() ||
                endY < y
        ) {
            setTile(null, tileCounter);
        }

        if (subtrees == null)
            return;

        for (int index = 0; index < 4; index++)
            if (subtrees[index] != null)
                subtrees[index].reduceTiles(startX, startY, endX, endY, zoomLevel, zoomLevelDelta, tileCounter);
    }

    // Disposes tile tree
    public void dispose() {
        if (subtrees != null)
            for (TileTree subtree : subtrees)
                if (subtree != null)
                    subtree.dispose();

        if (texture != null)
            texture.dispose();
    }
}
