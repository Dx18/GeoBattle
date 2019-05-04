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

    public ExternalAPI(Server server, GeolocationAPI geolocationAPI, TileRequestPool tileRequestPool) {
        this.server = server;
        this.geolocationAPI = geolocationAPI;
        this.tileRequestPool = tileRequestPool;
    }
}
