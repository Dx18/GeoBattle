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
import geobattle.geobattle.server.implementation.TileRequestPool;

public class AndroidLauncher extends AndroidApplication {
    private class GeoBattleLocationListener implements  LocationListener {
        private final String provider;

        public GeoBattleLocationListener(String provider) {
            this.provider = provider;
        }

        @Override
        public void onLocationChanged(Location location) {
            geolocationAPI.setLongitude((float) location.getLongitude());
            geolocationAPI.setLatitude((float) location.getLatitude());
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
    }

    FixedGeolocationAPI geolocationAPI;

    ExternalAPI externalAPI;

    LocationManager gpsLocationManager;

    LocationListener gpsLocationListener;

    LocationManager networkLocationManager;

    LocationListener networkLocationListener;

    private final int PERMISSIONS_REQUEST_GEOLOCATION = 100;

    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 101;

    private final char[] APP_ID = new char[] {
            'D', '6', '6', 'l', '1', 'm', 'T', 'a', 'h', 'R',
            'C', 'a', 'u', 'm', 'S', '4', 'H', 'X', 'm', 'h'
    };

    private final char[] APP_CODE = new char[] {
            '7', 'u', 'A', 'x', 'l', 'w', 'V', 'n', 'W', 'G', '_',
            '2', 'f', 'K', 'J', '4', 'y', 'x', 'A', 'p', 'b', 'w'
    };

    private SharedPreferences settings;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = getSharedPreferences("default", Context.MODE_PRIVATE);

        startGame();

        requestGeolocation();
    }

//    private String getBestGeolocationProvider() {
//        Criteria criteria = new Criteria();
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);
//
//        return locationManager.getBestProvider(criteria, true);
//    }

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
            } else {
                geolocationAPI.setLongitude(44.432085f);
                geolocationAPI.setLatitude(48.649366f);
            }

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
                        // Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

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
            public void saveCertificate(String name, String certificate) {

            }

            @Override
            public List<ServerAddress> getCustomServers() {
                return new ArrayList<>();
            }
        };

        externalAPI = new ExternalAPI(
                new SocketServer(11999, oSAPI.loadValue("ip", "78.47.182.60"), Integer.parseInt(oSAPI.loadValue("port", "12000")), oSAPI),
                geolocationAPI,
                new TileRequestPool(new String(APP_ID), new String(APP_CODE), null, 10),
                oSAPI
        );
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;
        initialize(new GeoBattle(externalAPI), config);
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
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestExternalStorage();
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
            gpsLocationListener = new GeoBattleLocationListener(LocationManager.GPS_PROVIDER);
            gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsLocationListener);
            networkLocationListener = new GeoBattleLocationListener(LocationManager.NETWORK_PROVIDER);
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
