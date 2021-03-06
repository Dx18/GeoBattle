package geobattle.geobattle.server.implementation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Locale;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import geobattle.geobattle.GeoBattleConst;
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

    // Visible tiles (real world). May be null
    private IntRect visible;

    // Level of zoom. May be null
    private Integer zoomLevel;

    // IP address of tile server
    private final String tileServerIp;

    // Port of tile server
    private final int tileServerPort;

    // Path to cache
    private final String cachePath;

    // Max loading count
    private final int maxLoadingCount;

    // Quality of map
    private String mapQuality;

    // Map "Map quality name" -> "Map quality resolution"
    private static final HashMap<String, String> MAP_QUALITY_TABLE;

    static {
        MAP_QUALITY_TABLE = new HashMap<String, String>();
        MAP_QUALITY_TABLE.put("mapQualityLow", "256");
        MAP_QUALITY_TABLE.put("mapQualityHigh", "512");
    }

    public TileRequestPool(String tileServerIp, int tileServerPort, String cachePath, int maxLoadingCount) {
        loadingCount = new AtomicInteger();
        requests = new Stack<TileRequest>();
        visible = null;
        zoomLevel = null;
        this.tileServerIp = tileServerIp;
        this.tileServerPort = tileServerPort;
        this.cachePath = cachePath;
        this.maxLoadingCount = maxLoadingCount;
        this.mapQuality = GeoBattleConst.DEFAULT_MAP_QUALITY;
    }

    // Sets rect of visible tiles
    public synchronized void setVisibleData(IntRect visible, Integer zoomLevel) {
        this.visible = visible;
        this.zoomLevel = zoomLevel;
    }

    public synchronized void setOnLoadListener(TileRequestCallback onLoad) {
        this.onLoad = onLoad;
    }

    // Sets quality of map
    public synchronized void setMapQuality(String mapQuality) {
        this.mapQuality = mapQuality;
    }

    // Reads pixmap from input stream
    private static Pixmap readPixmap(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        int read;
        byte[] buffer = new byte[2048];
        while ((read = inputStream.read(buffer)) >= 0)
            result.write(buffer, 0, read);

        byte[] finalResult = result.toByteArray();

        try {
            return new Pixmap(finalResult, 0, finalResult.length);
        } catch (GdxRuntimeException e) {
            return null;
        }
    }

    // Reads pixmap from cache. Does not create a new thread
    private Pixmap readFromCache(TileRequest tileRequest) {
        if (cachePath == null)
            return null;

        String fileName = String.format(
                Locale.US,
                "%d_%d_%d_%s.png",
                tileRequest.x,
                tileRequest.y,
                tileRequest.zoomLevel,
                MAP_QUALITY_TABLE.get(mapQuality)
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
        if (cachePath == null)
            return;

        String fileName = String.format(
                Locale.US,
                "%d_%d_%d_%s.png",
                tileRequest.x,
                tileRequest.y,
                tileRequest.zoomLevel,
                MAP_QUALITY_TABLE.get(mapQuality)
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
    private Pixmap requestSocket(final TileRequest tileRequest) {
        final int size = (1 << (19 - tileRequest.zoomLevel));

        Socket socket = null;
        DataOutputStream toSocket;
        DataInputStream fromSocket;

        try {
            socket = new Socket();
            socket.setSoTimeout(4000);
            Gdx.app.log("GeoBattle", "Creating socket for tiles: " + tileServerIp + ":" + tileServerPort);
            socket.connect(new InetSocketAddress(tileServerIp, tileServerPort), 2000);
            toSocket = new DataOutputStream(socket.getOutputStream());
            fromSocket = new DataInputStream(socket.getInputStream());

            byte[] sendBytes;
            try {
                sendBytes = String.format(
                        Locale.US,
                        "{ \"x\": %d, \"y\": %d, \"zoomLevel\": %d, \"size\": \"%s\" }#",
                        tileRequest.x / size,
                        (1 << tileRequest.zoomLevel) - 1 - tileRequest.y / size,
                        tileRequest.zoomLevel,
                        MAP_QUALITY_TABLE.get(mapQuality)
                ).getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }

            toSocket.write(sendBytes);
            return readPixmap(fromSocket);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Invokes when tile is loaded
    private void onLoad(Pixmap pixmap, TileRequest tileRequest) {
        if (onLoad != null)
            onLoad.onLoad(pixmap, tileRequest.x, tileRequest.y, tileRequest.zoomLevel);
    }

    // Requests next tile
    private void next() {
        synchronized (this) {
            if (loadingCount.get() >= maxLoadingCount)
                return;
            while (!requests.isEmpty()) {
                final TileRequest next = requests.pop();
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
                                result = requestSocket(next);
                                if (result != null)
                                    writeToCache(result, next);
                            }
                            onLoad(result, next);
                            loadingCount.decrementAndGet();
                            next();
                        }
                    }).start();
                    break;
                } else
                    onLoad(null, next);
            }
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
