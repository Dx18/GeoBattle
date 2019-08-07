package geobattle.geobattle.server;

import geobattle.geobattle.server.implementation.TileRequestPool;

// External API which includes server, geolocation API and tile API
public class ExternalAPI {
    // IP of official server
    public final String officialServerIp;

    // Ports of official server
    public final int[] officialServerPorts;

    // Port of official tile server
    public final int officialServerTilePort;

    // Server
    public final Server server;

    // Geolocation API
    public final GeolocationAPI geolocationAPI;

    // Tile request pool
    public final TileRequestPool tileRequestPool;

    // OS API which contains some OS-specific functions
    public final OSAPI oSAPI;

    public ExternalAPI(String officialServerIp, int[] officialServerPorts, int officialServerTilePort, Server server, GeolocationAPI geolocationAPI, String tileCachePath, int tileMaxLoadingCount, OSAPI oSAPI) {
        this.officialServerIp = officialServerIp;
        this.officialServerPorts = officialServerPorts;
        this.officialServerTilePort = officialServerTilePort;
        this.server = server;
        this.geolocationAPI = geolocationAPI;
        this.tileRequestPool = new TileRequestPool(officialServerIp, officialServerTilePort, tileCachePath, tileMaxLoadingCount);
        this.oSAPI = oSAPI;
    }
}
