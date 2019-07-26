package geobattle.geobattle.map;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import geobattle.geobattle.server.implementation.TileRequestPool;
import geobattle.geobattle.util.GeoBattleMath;
import geobattle.geobattle.util.IntRect;

// Tree of tiles
public final class TileTree {
    // Options for tiles reducing
    public static final class ReduceTilesOptions {
        // Zoom delta for tile texture keeping
        public final int textureKeepZoomLevelDelta;

        public ReduceTilesOptions(int textureKeepZoomLevelDelta) {
            this.textureKeepZoomLevelDelta = textureKeepZoomLevelDelta;
        }

        // Returns true if texture should be kept
        public boolean shouldKeepTexture(int zoomLevel, IntRect visibleRect, TileTree tree) {
            return Math.abs(tree.zoomLevel - zoomLevel) <= textureKeepZoomLevelDelta &&
                    GeoBattleMath.tileRectanglesIntersect(
                            visibleRect.x, visibleRect.y, visibleRect.width, visibleRect.height,
                            tree.x, tree.y, tree.getSize(), tree.getSize()
                    );
        }
    }

    // Max level of zoom
    public static final int MAX_ZOOM_LEVEL = 19;

    // Zoom level of current tree
    private final int zoomLevel;

    // X of current tree. Divisible by size of region current tree representing
    private final int x;

    // Y of current tree. Divisible by size of region current tree representing
    private final int y;

    // Tile texture of current tree
    private Texture texture;

    // Whether tile texture of current tree is requested
    private boolean textureRequested;

    // Top left tree with coordinates `(x, y + getSize() / 2)` and size equal to `getSize() / 2`
    private TileTree topLeftTree;

    // Top right tree with coordinates (x + getSize() / 2, y + getSize() / 2) and size equal to `getSize() / 2`
    private TileTree topRightTree;

    // Bottom right tree with coordinates (x + getSize() / 2, y) and size equal to `getSize() / 2`
    private TileTree bottomRightTree;

    // Bottom left tree with coordinates (x, y) and size equal to `getSize() / 2`
    private TileTree bottomLeftTree;

    public TileTree(int zoomLevel, int x, int y) {
        this.zoomLevel = zoomLevel;
        this.x = x - x % (1 << (MAX_ZOOM_LEVEL - zoomLevel));
        this.y = y - y % (1 << (MAX_ZOOM_LEVEL - zoomLevel));
    }

    // Creates root of tile tree
    public TileTree() {
        this(0, 0, 0);
    }

    // Returns size of current tree
    public int getSize() {
        return 1 << (MAX_ZOOM_LEVEL - zoomLevel);
    }

    // Sets tile of current tree
    private void setTile(Pixmap tile, TileCounter tileCounter) {
        if (textureRequested) {
            if (tile == null) {
                if (texture != null) {
                    texture.dispose();
                    texture = null;
                    tileCounter.onUnload();
                }
            } else if (texture == null) {
                texture = new Texture(tile);
                tile.dispose();
                tileCounter.onLoad();
            } else {
                texture.dispose();
                texture = new Texture(tile);
                tile.dispose();
            }

            textureRequested = false;
        }
    }

    // Sets tile of subtree or current tree
    public void setTile(Pixmap tile, TileCounter tileCounter, int x, int y, int zoomLevel) {
        if (zoomLevel < 0 || zoomLevel > MAX_ZOOM_LEVEL)
            return;
        if (x < 0 || x > (1 << MAX_ZOOM_LEVEL))
            return;
        if (y < 0 || y > (1 << MAX_ZOOM_LEVEL))
            return;

        if (this.zoomLevel < zoomLevel) {
            int xNorm = x % getSize() / (getSize() >> 1);
            int yNorm = y % getSize() / (getSize() >> 1);

            if (xNorm < 0 || xNorm > 1 || yNorm < 0 || yNorm > 1)
                return;

            TileTree subTree = getSubTree(xNorm, yNorm);
            if (subTree != null)
                subTree.setTile(tile, tileCounter, x, y, zoomLevel);
        } else if (this.zoomLevel == zoomLevel && this.x == x && this.y == y) {
            setTile(tile, tileCounter);
        }
    }

    // Returns subtree using normalized X and Y (either 0 or 1)
    // If subtree does not exist this method creates it
    // If zoom level is max allowed or one of coordinates is not normalized this method returns null
    private TileTree getSubTree(int localNormX, int localNormY) {
        if (zoomLevel == MAX_ZOOM_LEVEL)
            return null;
        if (localNormX < 0 || localNormX > 1 || localNormY < 0 || localNormY > 1)
            return null;

        if (localNormX == 0) {
            if (localNormY == 0) {
                if (bottomLeftTree == null)
                    bottomLeftTree = new TileTree(zoomLevel + 1, x, y);
                return bottomLeftTree;
            } else {
                if (topLeftTree == null)
                    topLeftTree = new TileTree(zoomLevel + 1, x, y + (getSize() >> 1));
                return topLeftTree;
            }
        } else {
            if (localNormY == 0) {
                if (bottomRightTree == null)
                    bottomRightTree = new TileTree(zoomLevel + 1, x + (getSize() >> 1), y);
                return bottomRightTree;
            } else {
                if (topRightTree == null)
                    topRightTree = new TileTree(zoomLevel + 1, x + (getSize() >> 1), y + (getSize() >> 1));
                return topRightTree;
            }
        }
    }

    // Returns tile by coordinates and zoom level
    // If tile is not yet loaded this method tries to load it
    public Texture getTile(int x, int y, int zoomLevel, TileRequestPool tileRequestPool, TileCounter tileCounter) {
        if (zoomLevel < 0 || zoomLevel > MAX_ZOOM_LEVEL)
            return null;
        if (x < 0 || x > (1 << MAX_ZOOM_LEVEL))
            return null;
        if (y < 0 || y > (1 << MAX_ZOOM_LEVEL))
            return null;

        if (this.zoomLevel < zoomLevel) {
            int xNorm = x % getSize() / (getSize() >> 1);
            int yNorm = y % getSize() / (getSize() >> 1);

            if (xNorm < 0 || xNorm > 1 || yNorm < 0 || yNorm > 1)
                return null;

            TileTree subTree = getSubTree(xNorm, yNorm);
            if (subTree != null)
                return subTree.getTile(x, y, zoomLevel, tileRequestPool, tileCounter);
        } else if (this.zoomLevel == zoomLevel && this.x == x && this.y == y) {
            if (texture == null && !textureRequested) {
                textureRequested = true;
                tileRequestPool.put(new TileRequestPool.TileRequest(x, y, zoomLevel));
                tileCounter.onRequest();
            }
            return texture;
        }

        return null;
    }

    // Reduces number of tiles loaded
    public void reduceTiles(IntRect visibleRect, int zoomLevel, ReduceTilesOptions options, TileCounter tileCounter) {
        if (!options.shouldKeepTexture(zoomLevel, visibleRect, this))
            setTile(null, tileCounter);

        if (topLeftTree != null)
            topLeftTree.reduceTiles(visibleRect, zoomLevel, options, tileCounter);
        if (topRightTree != null)
            topRightTree.reduceTiles(visibleRect, zoomLevel, options, tileCounter);
        if (bottomRightTree != null)
            bottomRightTree.reduceTiles(visibleRect, zoomLevel, options, tileCounter);
        if (bottomLeftTree != null)
            bottomLeftTree.reduceTiles(visibleRect, zoomLevel, options, tileCounter);
    }

    // Disposes current tree and subtrees
    public void dispose() {
        if (topLeftTree != null)
            topLeftTree.dispose();
        if (topRightTree != null)
            topRightTree.dispose();
        if (bottomRightTree != null)
            bottomRightTree.dispose();
        if (bottomLeftTree != null)
            bottomLeftTree.dispose();

        if (texture != null)
            texture.dispose();
    }
}
