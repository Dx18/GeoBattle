package geobattle.geobattle;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import java.util.ArrayList;
import java.util.List;

import geobattle.geobattle.server.ExternalAPI;
import geobattle.geobattle.server.OSAPI;
import geobattle.geobattle.server.ServerAddress;
import geobattle.geobattle.server.implementation.FixedGeolocationAPI;
import geobattle.geobattle.server.implementation.SocketServer;

// Android launcher for GeoBattle
public class AndroidLauncher extends AndroidApplication {
    // Location listener
    private class GeoBattleLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            geolocationAPI.setLongitude((float) location.getLongitude());
            geolocationAPI.setLatitude((float) location.getLatitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    }

    // API of geolocation
    FixedGeolocationAPI geolocationAPI;

    // External API
    ExternalAPI externalAPI;

    // Location manager for GPS
    LocationManager gpsLocationManager;

    // Location listener for GPS
    LocationListener gpsLocationListener;

    // Location manager for network
    LocationManager networkLocationManager;

    // Location listener for network
    LocationListener networkLocationListener;

    // Geolocation permission request
    private final int PERMISSIONS_REQUEST_GEOLOCATION = 100;

    // Game settings
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = getSharedPreferences("default", Context.MODE_PRIVATE);

        startGame();

        requestGeolocation();
    }

    // Requests geolocation permission
    private void requestGeolocation() {
        if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSIONS_REQUEST_GEOLOCATION);
        } else {
            gpsLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            networkLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            Location initial = gpsLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (initial == null)
                initial = gpsLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (initial != null) {
                geolocationAPI.setLongitude((float) initial.getLongitude());
                geolocationAPI.setLatitude((float) initial.getLatitude());
            }
        }
    }

    private void startGame() throws SecurityException {
        geolocationAPI = new FixedGeolocationAPI(0, 0);

        OSAPI oSAPI = new OSAPI() {
            @Override
            public void showMessage(final String message) {}

            @Override
            public void saveValue(String key, String value) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(key, value);
                editor.commit();
            }

            @Override
            public String loadValue(String key, String def) {
                return settings.getString(key, def);
            }

            @Override
            public String loadCertificate(String name) {
                return null;
            }

            @Override
            public void saveCertificate(String name, String certificate) {}

            @Override
            public List<ServerAddress> getCustomServers() {
                return new ArrayList<>();
            }
        };

        externalAPI = new ExternalAPI(
                "82.146.61.124",
                new int[] { 12000 },
                11998,
                new SocketServer(11999, null, 0),
                geolocationAPI,
                null,
                3,
                oSAPI
        );
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;
        initialize(new GeoBattle(externalAPI, BuildConfig.VERSION_NAME), config);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_GEOLOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestGeolocation();
                } else {
                    Toast.makeText(this, "Some needed permissions were not granted", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                break;
            default:
                Toast.makeText(this, "Unknown activity result with request code " + requestCode, Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
            gpsLocationListener = new GeoBattleLocationListener();
            gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsLocationListener);
            networkLocationListener = new GeoBattleLocationListener();
            networkLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkLocationListener);
        } catch (SecurityException e) {
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        try {
            super.onPause();
            gpsLocationManager.removeUpdates(gpsLocationListener);
            networkLocationManager.removeUpdates(networkLocationListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
