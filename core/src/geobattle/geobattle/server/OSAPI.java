package geobattle.geobattle.server;

import java.util.List;

public interface OSAPI {
    void showMessage(String message);

    List<ServerAddress> getCustomServers();
}
