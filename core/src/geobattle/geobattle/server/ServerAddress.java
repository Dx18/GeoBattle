package geobattle.geobattle.server;

import com.google.gson.JsonObject;

// Address of GeoBattle server
public final class ServerAddress {
    // Name of server
    public String name;

    // IP of server
    public String ip;

    // Port of server
    public int port;

    public ServerAddress(String name, String ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    // Sets values
    public void set(ServerAddress serverAddress) {
        name = serverAddress.name;
        ip = serverAddress.ip;
        port = serverAddress.port;
    }

    // Creates ServerAddress from JSON
    public static ServerAddress fromJson(JsonObject object) {
        String name = object.getAsJsonPrimitive("name").getAsString();
        String ip = object.getAsJsonPrimitive("ip").getAsString();
        int port = object.getAsJsonPrimitive("port").getAsInt();
        return new ServerAddress(name, ip, port);
    }

    // Converts ServerAddress to JSON
    public JsonObject toJson() {
        JsonObject result = new JsonObject();
        result.addProperty("name", name);
        result.addProperty("ip", ip);
        result.addProperty("port", port);
        return result;
    }
}
