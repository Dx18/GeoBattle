package geobattle.geobattle.server;

import com.badlogic.gdx.math.Vector2;

// API for geolocation
public interface GeolocationAPI {
    // Returns current coordinates of player
    Vector2 getCurrentCoordinates();
}
