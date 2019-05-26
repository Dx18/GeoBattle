package geobattle.geobattle.server;

import java.util.List;

public interface OSAPI {
    void showMessage(String message);

    void saveValue(String key, String value);

    String loadValue(String key, String def);

    String loadCertificate(String name);

    void saveCertificate(String name, String certificate);

    List<ServerAddress> getCustomServers();
}
