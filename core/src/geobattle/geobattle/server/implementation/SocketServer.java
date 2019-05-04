package geobattle.geobattle.server.implementation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import geobattle.geobattle.game.actionresults.BuildResult;
import geobattle.geobattle.game.actionresults.DestroyResult;
import geobattle.geobattle.game.actionresults.SectorBuildResult;
import geobattle.geobattle.game.actionresults.StateRequestResult;
import geobattle.geobattle.game.actionresults.UnitBuildResult;
import geobattle.geobattle.game.actionresults.UpdateRequestResult;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.game.events.BuildEvent;
import geobattle.geobattle.game.events.DestroyEvent;
import geobattle.geobattle.game.events.StateRequestEvent;
import geobattle.geobattle.game.units.UnitType;
import geobattle.geobattle.server.AuthInfo;
import geobattle.geobattle.server.AuthorizationResult;
import geobattle.geobattle.server.Callback;
import geobattle.geobattle.server.CancelHandle;
import geobattle.geobattle.server.RegistrationResult;
import geobattle.geobattle.server.Server;
import geobattle.geobattle.server.events.AuthorizationEvent;
import geobattle.geobattle.server.events.RegistrationEvent;

public final class SocketServer implements Server {
    private String ip;

    private int port;

    private JsonParser parser;

    public SocketServer(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.parser = new JsonParser();
    }

    @Override
    public void setAddress(String ip, int port) {
        synchronized (this) {
            this.ip = ip;
            this.port = port;
        }
    }

    private String request(String json) {
        Socket socket;
        DataOutputStream toSocket;
        DataInputStream fromSocket;

        try {
            synchronized (this) {
                socket = new Socket(ip, port);
            }
            socket.setSoTimeout(5000);
            toSocket = new DataOutputStream(socket.getOutputStream());
            fromSocket = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        byte[] sendBytes;
        try {
            sendBytes = (json + "#").getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder result = new StringBuilder();

        try {
            toSocket.write(sendBytes);

            byte[] buffer = new byte[1024];
            while (true) {
                int read = fromSocket.read(buffer);
                if (read < 0)
                    break;

                result.append(new String(buffer, 0, read));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        try {
            toSocket.close();
            fromSocket.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gdx.app.log("GeoBattle", "Data from server: " + result.toString());

        return result.toString();
    }

    @Override
    public CancelHandle register(final String playerName, final String email, final String password, final Color color, final Callback<RegistrationResult> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String resultStr = request(new RegistrationEvent(playerName, email, password, color).toJson().toString());

                if (resultStr == null)
                    return;

                JsonObject result = parser.parse(resultStr).getAsJsonObject();
                callback.onResult(RegistrationResult.fromJson(result));
            }
        }).start();

        return null;
    }

    @Override
    public CancelHandle login(final String playerName, final String password, final Callback<AuthorizationResult> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String resultStr = request(new AuthorizationEvent(playerName, password).toJson().toString());

                if (resultStr == null)
                    return;

                JsonObject result = parser.parse(resultStr).getAsJsonObject();
                callback.onResult(AuthorizationResult.fromJson(result));
            }
        }).start();

        return null;
    }

    @Override
    public CancelHandle invalidatePlayerToken(int playerId, String playerToken) {
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();

        return null;
    }

    @Override
    public CancelHandle requestState(final AuthInfo authInfo, final Callback<StateRequestResult> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String resultStr = request(new StateRequestEvent(authInfo).toJson().toString());

                if (resultStr == null)
                    return;

                JsonObject result = parser.parse(resultStr).getAsJsonObject();
                callback.onResult(StateRequestResult.fromJson(result));
            }
        }).start();

        return null;
    }

    @Override
    public CancelHandle requestUpdate(AuthInfo authInfo, Callback<UpdateRequestResult> callback) {
        return null;
    }

    @Override
    public CancelHandle requestBuild(final AuthInfo authInfo, final BuildingType type, final int x, final int y, final Callback<BuildResult> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String resultStr = request(new BuildEvent(authInfo, type.toString(), x, y).toJson().toString());

                if (resultStr == null)
                    return;

                JsonObject result = parser.parse(resultStr).getAsJsonObject();
                callback.onResult(BuildResult.fromJson(result));
            }
        }).start();

        return null;
    }

    @Override
    public CancelHandle requestSectorBuild(AuthInfo authInfo, int x, int y, Callback<SectorBuildResult> callback) {
        return null;
    }

    @Override
    public CancelHandle requestDestroy(final AuthInfo authInfo, final int id, final Callback<DestroyResult> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String resultStr = request(new DestroyEvent(authInfo, id).toJson().toString());

                if (resultStr == null)
                    return;

                JsonObject result = parser.parse(resultStr).getAsJsonObject();
                callback.onResult(DestroyResult.fromJson(result));
            }
        }).start();

        return null;
    }

    @Override
    public CancelHandle requestUnitBuild(AuthInfo authInfo, UnitType type, Building building, Callback<UnitBuildResult> callback) {
        return null;
    }
}
