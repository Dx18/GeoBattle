package geobattle.geobattle;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
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
import geobattle.geobattle.server.implementation.TileRequestPool;

public class AndroidLauncher extends AndroidApplication {
    FixedGeolocationAPI geolocationAPI;

    ExternalAPI externalAPI;

    LocationManager locationManager;

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            geolocationAPI.setLongitude((float) location.getLongitude());
            geolocationAPI.setLatitude((float) location.getLatitude());
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            String newProvider = getBestGeolocationProvider();
            locationManager.removeUpdates(locationListener);
            locationManager.requestLocationUpdates(newProvider, 0, 0, this);
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private final int PERMISSIONS_REQUEST_GEOLOCATION = 100;

    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 101;

    private final char[] APP_ID = new char[] {
            '_', '_', '_', '_', '_', '_', '_', '_', '_', '_',
            '_', '_', '_', '_', '_', '_', '_', '_', '_', '_'
    };

    private final char[] APP_CODE = new char[] {
            '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_',
            '_', '_', '_', '_', '_', '_', '_', '_', '_', '_', '_'
    };

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startGame();

        requestGeolocation();
    }

    private String getBestGeolocationProvider() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        return locationManager.getBestProvider(criteria, true);
    }

    private void requestGeolocation() {
        if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSIONS_REQUEST_GEOLOCATION);
        } else {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location initial = locationManager.getLastKnownLocation(getBestGeolocationProvider());

            if (initial != null) {
                geolocationAPI.setLongitude((float) initial.getLongitude());
                geolocationAPI.setLatitude((float) initial.getLatitude());
            }

            // locationManager.requestLocationUpdates(getBestGeolocationProvider(), 0, 0, locationListener);

            requestExternalStorage();
        }
    }

    private void requestExternalStorage() {
        if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            // startGame();
        }
    }

    private void startGame() throws SecurityException {
        geolocationAPI = new FixedGeolocationAPI(0, 0);

        OSAPI oSAPI = new OSAPI() {
            @Override
            public void showMessage(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public List<ServerAddress> getCustomServers() {
                return new ArrayList<>();
            }
        };

        externalAPI = new ExternalAPI(
                new SocketServer(11999, "78.47.182.60", 12000, oSAPI),
                geolocationAPI,
                new TileRequestPool(new String(APP_ID), new String(APP_CODE), "/", 10),
                oSAPI
        );
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        initialize(new GeoBattle(externalAPI), config);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_GEOLOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestExternalStorage();
                } else {
                    Toast.makeText(this, "Some needed permissions were not granted", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                break;
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // startGame();
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
            locationManager.requestLocationUpdates(getBestGeolocationProvider(), 0, 0, locationListener);
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
            locationManager.removeUpdates(locationListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
