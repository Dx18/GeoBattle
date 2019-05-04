package geobattle.geobattle.map;

// Counter for tiles
public final class TileCounter {
    // Count of requested tiles
    private int requestedCount;

    // Count of loaded tiles
    private int loadedCount;

    // Constructor
    // Sets all fields to 0
    public TileCounter() {
        requestedCount = 0;
        loadedCount = 0;
    }

    // Increments requested count
    public synchronized void onRequest() {
        requestedCount++;
    }

    // Decrements requested count
    public synchronized void onCancelRequest() {
        requestedCount--;
    }

    // Decrements requested count and increments loaded count
    public synchronized void onLoad() {
        requestedCount--;
        loadedCount++;
    }

    // Decrements loaded count
    public synchronized void onUnload() {
        loadedCount--;
    }

    // Returns requested count
    public synchronized int getRequestedCount() {
        return requestedCount;
    }

    // Returns loaded count
    public synchronized int getLoadedCount() {
        return loadedCount;
    }

    // Returns total count of loaded tiles
    public synchronized int getTotalCount() {
        return requestedCount + loadedCount;
    }
}
