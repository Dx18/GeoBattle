package geobattle.geobattle.server.implementation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import geobattle.geobattle.util.IntRect;

// Pool for tile requesting
public final class TileRequestPool {
    // Tile request
    public static class TileRequest {
        // X coordinate of tile request (real world)
        public final int x;

        // Y coordinate of tile request (real world)
        public final int y;

        // Zoom level
        public final int zoomLevel;

        public TileRequest(int x, int y, int zoomLevel) {
            this.x = x;
            this.y = y;
            this.zoomLevel = zoomLevel;
        }
    }

    // Callback for tile loading
    public interface TileRequestCallback {
        void onLoad(Pixmap pixmap, int x, int y, int zoomLevel);
    }

    // Invokes on load
    private TileRequestCallback onLoad;

    // Count of loading tiles
    private AtomicInteger loadingCount;

    // Requests
    private Stack<TileRequest> requests;

    // Visible tile (real world). May be null
    private IntRect visible;

    // Level of zoom. May be null
    private Integer zoomLevel;

    // App ID
    private final String appId;

    // App code
    private final String appCode;

    // Path to cache
    private final String cachePath;

    // Max loading count
    private final int maxLoadingCount;

    // Reads pixmap from input stream
    public TileRequestPool(String appId, String appCode, String cachePath, int maxLoadingCount) {
        loadingCount = new AtomicInteger();
        requests = new Stack<TileRequest>();
        visible = null;
        zoomLevel = null;
        this.appId = appId;
        this.appCode = appCode;
        this.cachePath = cachePath;
        this.maxLoadingCount = maxLoadingCount;
    }

    public synchronized void setOnLoadListener(TileRequestCallback onLoad) {
        this.onLoad = onLoad;
    }

    private static Pixmap readPixmap(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        int read;
        byte[] buffer = new byte[2048];
        while ((read = inputStream.read(buffer)) >= 0)
            result.write(buffer, 0, read);

        byte[] finalResult = result.toByteArray();
        return new Pixmap(finalResult, 0, finalResult.length);
    }

    // Reads pixmap from cache. Does not create a new thread
    private Pixmap readFromCache(TileRequest tileRequest) {
        String fileName = String.format(
                Locale.US,
                "%d_%d_%d.png",
                tileRequest.x,
                tileRequest.y,
                tileRequest.zoomLevel
        );

        File file = new File(cachePath, fileName);

        Gdx.app.log("GeoBattle", "Loading from " + fileName);

        if (!file.exists())
            return null;

        try {
            InputStream fromFile = new FileInputStream(file);
            return readPixmap(fromFile);
        } catch (FileNotFoundException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Writes pixmap to cache. Does not create a new thread
    private void writeToCache(Pixmap pixmap, TileRequest tileRequest) {
        String fileName = String.format(
                Locale.US,
                "%d_%d_%d.png",
                tileRequest.x,
                tileRequest.y,
                tileRequest.zoomLevel
        );

        File file = new File(cachePath, fileName);

        Gdx.app.log("GeoBattle", "Writing to " + fileName);

        try {
            PixmapIO.writePNG(new FileHandle(file), pixmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Requests tile from cache
    private Pixmap requestCache(final TileRequest tileRequest) {
        return readFromCache(tileRequest);
    }

    // Requests tile via HTTP
    private Pixmap requestHTTP(final TileRequest tileRequest) {
        final int size = (1 << (19 - tileRequest.zoomLevel));

        String urlString = String.format(
                Locale.US,
                "https://1.base.maps.api.here.com/maptile/2.1/maptile/newest/normal.day/%d/%d/%d/512/jpg?app_id=%s&app_code=%s&lg=rus&ppi=250",
                tileRequest.zoomLevel,
                tileRequest.x / size,
                (1 << tileRequest.zoomLevel) - 1 - tileRequest.y / size,
                appId,
                appCode
        );

        HttpURLConnection connection = null;
        InputStream fromConnection = null;
        try {
            URL url = new URL(urlString);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            fromConnection = connection.getInputStream();
            return readPixmap(fromConnection);
        } catch (IOException e) {
            // e.printStackTrace();
            return null;
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }

    // Invokes when tile is loaded
    private void onLoad(Pixmap pixmap, TileRequest tileRequest) {
        if (onLoad != null)
            onLoad.onLoad(pixmap, tileRequest.x, tileRequest.y, tileRequest.zoomLevel);
        synchronized (this) {
            loadingCount.decrementAndGet();
            next();
        }
    }

    // Requests next tile
    private void next() {
        synchronized (this) {
            if (requests.isEmpty())
                return;
        }
        if (loadingCount.get() >= maxLoadingCount)
            return;

        final TileRequest next;
        synchronized (this) {
            next = requests.pop();
        }
        if (
                visible == null || zoomLevel == null ||
                Math.abs(zoomLevel - next.zoomLevel) <= 2 &&
                next.x >= visible.x &&
                next.x < visible.x + visible.width &&
                next.y >= visible.y &&
                next.y < visible.y + visible.height
        ) {
            loadingCount.incrementAndGet();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Pixmap result = requestCache(next);
                    if (result == null) {
                        result = requestHTTP(next);
                        if (result != null)
                            writeToCache(result, next);
                    }
                    onLoad(result, next);
                }
            }).start();
        }
    }

    // Puts tile request
    public void put(TileRequest request) {
        synchronized (this) {
            requests.push(request);
            if (loadingCount.get() < maxLoadingCount)
                next();
        }
    }
}
