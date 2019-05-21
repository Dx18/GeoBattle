package geobattle.geobattle.server;

// Address of GeoBattle server
public final class ServerAddress {
    // Name of server
    public final String name;

    // IP of server
    public final String ip;

    // Port of server
    public final int port;

    public ServerAddress(String name, String ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }
}
