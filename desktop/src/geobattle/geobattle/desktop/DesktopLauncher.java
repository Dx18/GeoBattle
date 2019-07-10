package geobattle.geobattle.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.server.ExternalAPI;
import geobattle.geobattle.server.OSAPI;
import geobattle.geobattle.server.ServerAddress;
import geobattle.geobattle.server.implementation.FixedGeolocationAPI;
import geobattle.geobattle.server.implementation.SocketServer;
import geobattle.geobattle.server.implementation.TileRequestPool;

public class DesktopLauncher {
    private static ExternalAPI createExternalAPI(String path, String cachePath) {
        File file = new File(path);
        Scanner scanner = null;
        try {
            scanner = new Scanner(new FileInputStream(file));
            scanner.useLocale(Locale.US);
            String appId = scanner.next();
            String appCode = scanner.next();
            float longitude = scanner.nextFloat();
            float latitude = scanner.nextFloat();

            OSAPI oSAPI = new OSAPI() {
                @Override
                public void showMessage(String message) {
                    try {
                        Runtime.getRuntime().exec("/usr/bin/notify-send", new String[] { message });
                        Gdx.app.log("GeoBattle", "Failed to send message, so: " + message);
                    } catch (IOException e) {
                        // Fallback
                        Gdx.app.log("GeoBattle", "Failed to send message, so: " + message);
                    }
                }

                @Override
                public void saveValue(String key, String value) {

                }

                @Override
                public String loadValue(String key, String def) {
                    return def;
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
                    return new ArrayList<ServerAddress>();
                }
            };

            return new ExternalAPI(
                    new SocketServer(11999, "78.47.182.60", 12000),
                    new FixedGeolocationAPI(longitude, latitude),
                    new TileRequestPool("82.146.61.124", 11998, cachePath, 3),
                    oSAPI
            );
        } catch (FileNotFoundException e) {
            System.out.println("File tileAPI not found. You should add it to ./android/assets/tileAPI");
            return null;
        } finally {
            if (scanner != null)
                scanner.close();
        }
    }

	public static void main (String[] arg) {
        System.out.print("Type user name: ");

        String userName = new Scanner(System.in).nextLine();

        String cachePath = String.format(
                "/home/%s/.geobattle/cache/",
                userName
        );

        System.out.println(String.format("Cache path: %s", cachePath));

        ExternalAPI externalAPI = createExternalAPI("tileAPI", null);

        if (externalAPI == null)
            return;

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1920 / 2;
		config.height = 1080;
		new LwjglApplication(new GeoBattle(externalAPI), config);
	}
}
