package geobattle.geobattle.server.implementation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import geobattle.geobattle.actionresults.AttackResult;
import geobattle.geobattle.actionresults.BuildResult;
import geobattle.geobattle.actionresults.DestroyResult;
import geobattle.geobattle.actionresults.ResearchResult;
import geobattle.geobattle.actionresults.SectorBuildResult;
import geobattle.geobattle.actionresults.StateRequestResult;
import geobattle.geobattle.actionresults.UnitBuildResult;
import geobattle.geobattle.actionresults.UpdateRequestResult;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.events.AttackEvent;
import geobattle.geobattle.events.BuildEvent;
import geobattle.geobattle.events.DestroyEvent;
import geobattle.geobattle.events.ResearchEvent;
import geobattle.geobattle.events.SectorBuildEvent;
import geobattle.geobattle.events.StateRequestEvent;
import geobattle.geobattle.events.UnitBuildEvent;
import geobattle.geobattle.game.research.ResearchType;
import geobattle.geobattle.game.units.UnitType;
import geobattle.geobattle.server.AuthInfo;
import geobattle.geobattle.actionresults.AuthorizationResult;
import geobattle.geobattle.server.Callback;
import geobattle.geobattle.server.CancelHandle;
import geobattle.geobattle.actionresults.EmailConfirmationResult;
import geobattle.geobattle.server.OSAPI;
import geobattle.geobattle.actionresults.RegistrationResult;
import geobattle.geobattle.actionresults.ResendEmailResult;
import geobattle.geobattle.server.Server;
import geobattle.geobattle.events.AuthorizationEvent;
import geobattle.geobattle.events.EmailConfirmationEvent;
import geobattle.geobattle.events.RegistrationEvent;
import geobattle.geobattle.events.ResendEmailEvent;

public final class SocketServer implements Server {
    private int masterPort;

    private String ip;

    private int port;

    private JsonParser parser;

    private OSAPI oSAPI;

    private SSLSocketFactory sslSocketFactory;

    public SocketServer(int masterPort, String ip, int port, OSAPI oSAPI) {
        this.masterPort = masterPort;
        this.ip = ip;
        this.port = port;
        this.parser = new JsonParser();
        this.oSAPI = oSAPI;
    }

    @Override
    public void setAddress(String ip, int port) {
        synchronized (this) {
            if (this.ip.equals(ip) && this.port == port)
                return;

            this.ip = ip;
            this.port = port;
            sslSocketFactory = null;
        }
    }

    @Override
    public String getIPAddress() {
        return ip;
    }

    @Override
    public int getPort() {
        return port;
    }

    public boolean requestCertificate() {
        String certificateStr = request(ip, masterPort, "{\"type\": \"SSLCertificateRequestEvent\"}");

        System.out.println("Certificate: " + certificateStr);

        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Certificate certificate = certificateFactory.generateCertificate(new ByteArrayInputStream(certificateStr.getBytes()));

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", certificate);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustManagerFactory.getTrustManagers(), null);

            sslSocketFactory = context.getSocketFactory();

            return true;
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return false;
    }

    private String requestSSL(String ip, int port, String json) {
        synchronized (this) {
            if (sslSocketFactory == null)
                requestCertificate();
        }

        SSLSocket socket;
        DataOutputStream toSocket;
        DataInputStream fromSocket;

        try {
            synchronized (this) {
                Gdx.app.log("GeoBattle", "Creating socket: " + ip + ":" + port);
                socket = (SSLSocket) sslSocketFactory.createSocket(ip, port);
                socket.startHandshake();
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
            toSocket.flush();

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

    private String request(String ip, int port, String json) {
        Socket socket;
        DataOutputStream toSocket;
        DataInputStream fromSocket;

        try {
            synchronized (this) {
                if (Gdx.app != null)
                    Gdx.app.log("GeoBattle", "Creating socket: " + ip + ":" + port);
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

        if (Gdx.app != null)
            Gdx.app.log("GeoBattle", "Data from server: " + result.toString());

        return result.toString();
    }

    @Override
    public CancelHandle register(final String playerName, final String email, final String password, final Color color, final Callback<RegistrationResult> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String resultStr = requestSSL(ip, port, new RegistrationEvent(playerName, email, password, color).toJson().toString());

                if (resultStr == null) {
                    oSAPI.showMessage("RegisterEvent failed: probable problems with connection");
                    return;
                }

                try {
                    JsonObject result = parser.parse(resultStr).getAsJsonObject();
                    callback.onResult(RegistrationResult.fromJson(result));
                } catch (Exception e) {
                    oSAPI.showMessage("RegisterEvent failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
            }
        }).start();

        return null;
    }

    @Override
    public CancelHandle login(final String playerName, final String password, final Callback<AuthorizationResult> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String resultStr = requestSSL(ip, port, new AuthorizationEvent(playerName, password).toJson().toString());

                if (resultStr == null) {
                    oSAPI.showMessage("AuthorizationEvent failed: probable problems with connection");
                    return;
                }

                try {
                    JsonObject result = parser.parse(resultStr).getAsJsonObject();
                    callback.onResult(AuthorizationResult.fromJson(result));
                } catch (Exception e) {
                    oSAPI.showMessage("AuthorizationEvent failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
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
                String resultStr = requestSSL(ip, port, new StateRequestEvent(authInfo).toJson().toString());

                if (resultStr == null) {
                    oSAPI.showMessage("StateRequestEvent failed: probable problems with connection");
                    return;
                }

                try {
                    JsonObject result = parser.parse(resultStr).getAsJsonObject();
                    callback.onResult(StateRequestResult.fromJson(result));
                } catch (Exception e) {
                    oSAPI.showMessage("StateRequestEvent failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
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
                String resultStr = requestSSL(ip, port, new BuildEvent(authInfo, type.toString(), x, y).toJson().toString());

                if (resultStr == null) {
                    oSAPI.showMessage("BuildEvent failed: probable problems with connection");
                    return;
                }

                try {
                    JsonObject result = parser.parse(resultStr).getAsJsonObject();
                    callback.onResult(BuildResult.fromJson(result));
                } catch (Exception e) {
                    oSAPI.showMessage("BuildEvent failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
            }
        }).start();

        return null;
    }

    @Override
    public CancelHandle requestSectorBuild(final AuthInfo authInfo, final int x, final int y, final Callback<SectorBuildResult> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String resultStr = requestSSL(ip, port, new SectorBuildEvent(authInfo, x, y).toJson().toString());

                if (resultStr == null) {
                    oSAPI.showMessage("SectorBuildEvent failed: probable problems with connection");
                    return;
                }

                try {
                    JsonObject result = parser.parse(resultStr).getAsJsonObject();
                    callback.onResult(SectorBuildResult.fromJson(result));
                } catch (Exception e) {
                    oSAPI.showMessage("SectorBuildEvent failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
            }
        }).start();

        return null;
    }

    @Override
    public CancelHandle requestDestroy(final AuthInfo authInfo, final int id, final Callback<DestroyResult> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String resultStr = requestSSL(ip, port, new DestroyEvent(authInfo, id).toJson().toString());

                if (resultStr == null) {
                    oSAPI.showMessage("DestroyEvent failed: probable problems with connection");
                    return;
                }

                try {
                    JsonObject result = parser.parse(resultStr).getAsJsonObject();
                    callback.onResult(DestroyResult.fromJson(result));
                } catch (Exception e) {
                    oSAPI.showMessage("DestroyEvent failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
            }
        }).start();

        return null;
    }

    @Override
    public CancelHandle requestUnitBuild(final AuthInfo authInfo, final UnitType type, final Building building, final Callback<UnitBuildResult> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String resultStr = requestSSL(ip, port, new UnitBuildEvent(authInfo, type.toString(), building.id).toJson().toString());

                if (resultStr == null) {
                    oSAPI.showMessage("UnitBuildEvent failed: probable problems with connection");
                    return;
                }

                try {
                    JsonObject result = parser.parse(resultStr).getAsJsonObject();
                    callback.onResult(UnitBuildResult.fromJson(result));
                } catch (Exception e) {
                    oSAPI.showMessage("UnitBuildResult failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
            }
        }).start();

        return null;
    }

    @Override
    public CancelHandle requestResearch(final AuthInfo authInfo, final ResearchType researchType, final Callback<ResearchResult> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String resultStr = requestSSL(ip, port, new ResearchEvent(authInfo, researchType.toString()).toJson().toString());

                if (resultStr == null) {
                    oSAPI.showMessage("ResearchEvent failed: probable problems with connection");
                    return;
                }

                try {
                    JsonObject result = parser.parse(resultStr).getAsJsonObject();
                    callback.onResult(ResearchResult.fromJson(result));
                } catch (Exception e) {
                    oSAPI.showMessage("ResearchEvent failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
            }
        }).start();

        return null;
    }

    @Override
    public CancelHandle requestAttack(final AuthInfo authInfo, final int attackerId, final int victimId, final int[] hangarIds, final int sectorId, final Callback<AttackResult> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String resultStr = requestSSL(ip, port, new AttackEvent(authInfo, attackerId, victimId, hangarIds, sectorId).toJson().toString());

                if (resultStr == null) {
                    oSAPI.showMessage("AttackEvent failed: probable problems with connection");
                    return;
                }

                try {
                    JsonObject result = parser.parse(resultStr).getAsJsonObject();
                    callback.onResult(AttackResult.fromJson(result));
                } catch (Exception e) {
                    oSAPI.showMessage("AttackEvent failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
            }
        }).start();

        return null;
    }

    @Override
    public CancelHandle requestEmailConfirmation(final String name, final int code, final Callback<EmailConfirmationResult> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String resultStr = requestSSL(ip, port, new EmailConfirmationEvent(name, code).toJson().toString());

                if (resultStr == null) {
                    oSAPI.showMessage("EmailConfirmationEvent failed: probable problems with connection");
                    return;
                }

                try {
                    JsonObject result = parser.parse(resultStr).getAsJsonObject();
                    callback.onResult(EmailConfirmationResult.fromJson(result));
                } catch (Exception e) {
                    oSAPI.showMessage("EmailConfirmationEvent failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
            }
        }).start();

        return null;
    }

    @Override
    public CancelHandle requestEmailResend(final String name, final Callback<ResendEmailResult> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String resultStr = requestSSL(ip, port, new ResendEmailEvent(name).toJson().toString());

                if (resultStr == null) {
                    oSAPI.showMessage("ResendEmailEvent failed: probable problems with connection");
                    return;
                }

                try {
                    JsonObject result = parser.parse(resultStr).getAsJsonObject();
                    callback.onResult(ResendEmailResult.fromJson(result));
                } catch (Exception e) {
                    oSAPI.showMessage("ResendEmailEvent failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
            }
        }).start();

        return null;
    }
}
