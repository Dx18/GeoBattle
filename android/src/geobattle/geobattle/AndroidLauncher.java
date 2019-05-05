package geobattle.geobattle;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import geobattle.geobattle.server.ExternalAPI;
import geobattle.geobattle.server.OSAPI;
import geobattle.geobattle.server.implementation.FixedGeolocationAPI;
import geobattle.geobattle.server.implementation.SocketServer;
import geobattle.geobattle.server.implementation.TileRequestPool;

public class AndroidLauncher extends AndroidApplication {
    FixedGeolocationAPI geolocationAPI;

    ExternalAPI externalAPI;

    LocationManager locationManager;

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            geolocationAPI.setLongitude((float)location.getLongitude());
            geolocationAPI.setLatitude((float)location.getLatitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            Location initial = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (initial != null)
                geolocationAPI = new FixedGeolocationAPI(
                        (float)initial.getLongitude(),
                        (float)initial.getLatitude()
                );
            else
                geolocationAPI = new FixedGeolocationAPI(0, 0);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, locationListener);
        } catch (SecurityException e) {
            finish();
        }

        OSAPI oSAPI = new OSAPI() {
            @Override
            public void showMessage(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }
                });
            }
        };

        externalAPI = new ExternalAPI(
                new SocketServer("78.47.182.60", 12000, oSAPI),
                geolocationAPI,
                new TileRequestPool("D66l1mTahRCaumS4HXmh", "7uAxlwVnWG_2fKJ4yxApbw", "/", 10),
                oSAPI
        );

        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        initialize(new GeoBattle(externalAPI), config);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } catch (SecurityException e) {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }
}
