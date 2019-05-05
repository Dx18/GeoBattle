package geobattle.geobattle.server;

import geobattle.geobattle.server.implementation.TileRequestPool;

// External API which includes server, geolocation API and tile API
public class ExternalAPI {
    // Server
    public final Server server;

    // Geolocation API
    public final GeolocationAPI geolocationAPI;

    // Tile request pool
    public final TileRequestPool tileRequestPool;

    // OS API which contains some OS-specific functions
    public final OSAPI oSAPI;

    public ExternalAPI(Server server, GeolocationAPI geolocationAPI, TileRequestPool tileRequestPool, OSAPI oSAPI) {
        this.server = server;
        this.geolocationAPI = geolocationAPI;
        this.tileRequestPool = tileRequestPool;
        this.oSAPI = oSAPI;
    }
}
