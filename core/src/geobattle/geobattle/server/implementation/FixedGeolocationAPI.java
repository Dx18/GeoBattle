package geobattle.geobattle.server.implementation;

import com.badlogic.gdx.math.Vector2;

import geobattle.geobattle.server.GeolocationAPI;

// Geolocation API where geolocation is fixed
public class FixedGeolocationAPI implements GeolocationAPI {
    // Fixed longitude
    private float longitude;

    // Fixed latitude
    private float latitude;

    public FixedGeolocationAPI(float longitude, float latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    // Returns current coordinates
    @Override
    public Vector2 getCurrentCoordinates() {
        return new Vector2(longitude, latitude);
    }

    // Returns longitude
    public float getLongitude() {
        return longitude;
    }

    // Sets longitude
    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    // Returns latitude
    public float getLatitude() {
        return latitude;
    }

    // Sets latitude
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }
}
