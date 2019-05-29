package geobattle.geobattle.server.implementation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
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

import geobattle.geobattle.GeoBattle;
import geobattle.geobattle.actionresults.AttackResult;
import geobattle.geobattle.actionresults.AuthorizationResult;
import geobattle.geobattle.actionresults.BuildResult;
import geobattle.geobattle.actionresults.DestroyResult;
import geobattle.geobattle.actionresults.EmailConfirmationResult;
import geobattle.geobattle.actionresults.RegistrationResult;
import geobattle.geobattle.actionresults.ResearchResult;
import geobattle.geobattle.actionresults.ResendEmailResult;
import geobattle.geobattle.actionresults.SectorBuildResult;
import geobattle.geobattle.actionresults.StateRequestResult;
import geobattle.geobattle.actionresults.UnitBuildResult;
import geobattle.geobattle.actionresults.UpdateRequestResult;
import geobattle.geobattle.events.AttackEvent;
import geobattle.geobattle.events.AuthorizationEvent;
import geobattle.geobattle.events.BuildEvent;
import geobattle.geobattle.events.DestroyEvent;
import geobattle.geobattle.events.EmailConfirmationEvent;
import geobattle.geobattle.events.RegistrationEvent;
import geobattle.geobattle.events.ResearchEvent;
import geobattle.geobattle.events.ResendEmailEvent;
import geobattle.geobattle.events.SectorBuildEvent;
import geobattle.geobattle.events.StateRequestEvent;
import geobattle.geobattle.events.UnitBuildEvent;
import geobattle.geobattle.events.UpdateRequestEvent;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.BuildingType;
import geobattle.geobattle.game.research.ResearchType;
import geobattle.geobattle.game.units.UnitType;
import geobattle.geobattle.server.AuthInfo;
import geobattle.geobattle.server.Callback;
import geobattle.geobattle.server.CancelHandle;
import geobattle.geobattle.server.Server;
import geobattle.geobattle.util.FrequencyQueue;

public final class SocketServer implements Server {
    private static final int TRIES_COUNT = 7;

    private int masterPort;

    private String ip;

    private int port;

    private JsonParser parser;

    private SSLSocketFactory sslSocketFactory;

    private FrequencyQueue fails;

    private Runnable onFail;

    private GeoBattle game;

    private CancelHandle registrationEvent;

    private CancelHandle authorizationEvent;

    private CancelHandle stateRequestEvent;

    private CancelHandle updateRequestEvent;

    private CancelHandle buildEvent;

    private CancelHandle sectorBuildEvent;

    private CancelHandle destroyEvent;

    private CancelHandle unitBuildEvent;

    private CancelHandle researchEvent;

    private CancelHandle attackEvent;

    private CancelHandle emailConfirmationEvent;

    private CancelHandle emailResendEvent;

    public SocketServer(int masterPort, String ip, int port) {
        this.masterPort = masterPort;
        this.ip = ip;
        this.port = port;
        this.parser = new JsonParser();
        this.fails = new FrequencyQueue(TRIES_COUNT, false);
    }

    @Override
    public void setGame(GeoBattle game) {
        this.game = game;
    }

    @Override
    public void setOnFailListener(Runnable onFail) {
        this.onFail = onFail;
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

        if (certificateStr == null)
            return false;

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

            if (Thread.interrupted())
                return false;

            synchronized (this) {
                sslSocketFactory = context.getSocketFactory();
            }

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
        boolean sslSocketFactoryExists;
        synchronized (this) {
            sslSocketFactoryExists = sslSocketFactory != null;
        }
        if (!sslSocketFactoryExists && !requestCertificate())
            return null;

        SSLSocket socket;
        DataOutputStream toSocket;
        DataInputStream fromSocket;

        try {
            SSLSocketFactory sslSocketFactory;
            synchronized (this) {
                sslSocketFactory = this.sslSocketFactory;
            }
            Gdx.app.log("GeoBattle", "Creating socket: " + ip + ":" + port);
            socket = (SSLSocket) sslSocketFactory.createSocket(ip, port);
            socket.startHandshake();
            socket.setSoTimeout(2000);
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

        if (Thread.interrupted())
            return null;

        return result.toString();
    }

    private String request(String ip, int port, String json) {
        Socket socket;
        DataOutputStream toSocket;
        DataInputStream fromSocket;

        try {
            socket = new Socket(ip, port);
            Gdx.app.log("GeoBattle", "Creating socket: " + ip + ":" + port);
            socket.setSoTimeout(2000);
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

        if (Thread.interrupted())
            return null;

        return result.toString();
    }

    private void onFail() {
        fails.add(true);
        if (game != null)
            game.setNetworkState((float) fails.getFalseCount() / TRIES_COUNT);
        if (fails.getTrueCount() >= TRIES_COUNT && onFail != null) {
            onFail.run();
            fails = new FrequencyQueue(TRIES_COUNT, false);
        }
    }

    private void onSuccess() {
        fails.add(false);
        if (game != null) {
            Gdx.app.log("GeoBattle", "Setting network state: " + ((float) fails.getFalseCount() / TRIES_COUNT));
            game.setNetworkState((float) fails.getFalseCount() / TRIES_COUNT);
        }
    }

    @Override
    public synchronized void cancelRegistrationEvent() {
        if (registrationEvent != null) {
            registrationEvent.cancel();
            registrationEvent = null;
        }
    }

    @Override
    public synchronized void onRegistrationEvent(final String playerName, final String email, final String password, final Color color, final Callback<RegistrationResult> callback, final Runnable failCallback) {
        if (registrationEvent != null)
            return;

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String ip;
                int port;
                synchronized (SocketServer.this) {
                    ip = SocketServer.this.ip;
                    port = SocketServer.this.port;
                }

                String resultStr = requestSSL(ip, port, new RegistrationEvent(playerName, email, password, color).toJson().toString());

                synchronized (SocketServer.this) {
                    registrationEvent = null;
                }

                if (resultStr == null) {
                    if (failCallback == null) {
                        game.showMessage(game.getI18NBundle().get("networkProblems"));
                        game.getExternalAPI().oSAPI.showMessage("RegisterEvent failed: probable problems with connection");
                    } else
                        failCallback.run();
                    return;
                }

                try {
                    RegistrationResult result = RegistrationResult.fromJson(parser.parse(resultStr).getAsJsonObject());
                    if (result == null)
                        throw new IllegalArgumentException("Given type of RegistrationResult is unknown");
                    callback.onResult(result);
                } catch (Exception e) {
                    game.getExternalAPI().oSAPI.showMessage("RegisterEvent failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
            }
        });

        thread.start();

        registrationEvent = new CancelHandle() {
            @Override
            public void cancel() {
                thread.interrupt();
            }
        };

    }

    @Override
    public void cancelAuthorizationEvent() {
        if (authorizationEvent != null) {
            authorizationEvent.cancel();
            authorizationEvent = null;
        }
    }

    @Override
    public synchronized void onAuthorizationEvent(final String playerName, final String password, final Callback<AuthorizationResult> callback, final Runnable failCallback) {
        if (authorizationEvent != null)
            return;

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String ip;
                int port;
                synchronized (SocketServer.this) {
                    ip = SocketServer.this.ip;
                    port = SocketServer.this.port;
                }

                String resultStr = requestSSL(ip, port, new AuthorizationEvent(playerName, password).toJson().toString());

                synchronized (SocketServer.this) {
                    authorizationEvent = null;
                }

                if (resultStr == null) {
                    if (failCallback == null) {
                        game.showMessage(game.getI18NBundle().get("networkProblems"));
                        game.getExternalAPI().oSAPI.showMessage("AuthorizationEvent failed: probable problems with connection");
                    } else
                        failCallback.run();
                    return;
                }

                try {
                    AuthorizationResult result = AuthorizationResult.fromJson(parser.parse(resultStr).getAsJsonObject());
                    if (result == null)
                        throw new IllegalArgumentException("Given type of AuthorizationResult is unknown");
                    callback.onResult(result);
                } catch (Exception e) {
                    game.getExternalAPI().oSAPI.showMessage("AuthorizationEvent failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
            }
        });

        thread.start();

        authorizationEvent = new CancelHandle() {
            @Override
            public void cancel() {
                thread.interrupt();
            }
        };

    }

    @Override
    public void invalidatePlayerToken(int playerId, String playerToken, final Runnable failCallback) {
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();

    }

    @Override
    public synchronized void cancelStateRequestEvent() {
        if (stateRequestEvent != null) {
            stateRequestEvent.cancel();
            stateRequestEvent = null;
        }
    }

    @Override
    public synchronized void onStateRequestEvent(final AuthInfo authInfo, final Callback<StateRequestResult> callback, final Runnable failCallback) {
        if (stateRequestEvent != null)
            return;

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String ip;
                int port;
                synchronized (SocketServer.this) {
                    ip = SocketServer.this.ip;
                    port = SocketServer.this.port;
                }

                String resultStr = requestSSL(ip, port, new StateRequestEvent(authInfo).toJson().toString());

                synchronized (SocketServer.this) {
                    stateRequestEvent = null;
                }

                if (resultStr == null) {
                    onFail();
                    if (failCallback == null) {
                        game.showMessage(game.getI18NBundle().get("networkProblems"));
                        game.getExternalAPI().oSAPI.showMessage("StateRequestEvent failed: probable problems with connection");
                    } else
                        failCallback.run();
                    return;
                }
                onSuccess();

                try {
                    StateRequestResult result = StateRequestResult.fromJson(parser.parse(resultStr).getAsJsonObject());
                    if (result == null)
                        throw new IllegalArgumentException("Given type of StateRequestResult is unknown");
                    callback.onResult(result);
                } catch (Exception e) {
                    game.getExternalAPI().oSAPI.showMessage("StateRequestEvent failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
            }
        });

        thread.start();

        stateRequestEvent = new CancelHandle() {
            @Override
            public void cancel() {
                thread.interrupt();
            }
        };

    }

    @Override
    public synchronized void cancelUpdateRequestEvent() {
        if (updateRequestEvent != null) {
            updateRequestEvent.cancel();
            updateRequestEvent = null;
        }
    }

    @Override
    public synchronized void onUpdateRequestEvent(final AuthInfo authInfo, final double lastUpdateTime, final Callback<UpdateRequestResult> callback, final Runnable failCallback) {
        if (updateRequestEvent != null)
            return;

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String ip;
                int port;
                synchronized (SocketServer.this) {
                    ip = SocketServer.this.ip;
                    port = SocketServer.this.port;
                }

                String resultStr = requestSSL(ip, port, new UpdateRequestEvent(authInfo, lastUpdateTime).toJson().toString());

                synchronized (SocketServer.class) {
                    updateRequestEvent = null;
                }

                if (resultStr == null) {
                    onFail();
                    if (failCallback == null)
                        game.getExternalAPI().oSAPI.showMessage("UpdateRequestEvent failed: probable problems with connection");
                    else
                        failCallback.run();
                    return;
                }
                onSuccess();

                try {
                    UpdateRequestResult result = UpdateRequestResult.fromJson(parser.parse(resultStr).getAsJsonObject());
                    if (result == null)
                        throw new IllegalArgumentException("Given type of UpdateRequestResult is unknown");
                    callback.onResult(result);
                } catch (Exception e) {
                    game.getExternalAPI().oSAPI.showMessage("UpdateRequestEvent failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
            }
        });

        thread.start();

        updateRequestEvent = new CancelHandle() {
                @Override
                public void cancel() {
                    thread.interrupt();
                }
            };

    }

    @Override
    public synchronized void cancelBuildEvent() {
        if (buildEvent != null) {
            buildEvent.cancel();
            buildEvent = null;
        }
    }

    @Override
    public synchronized void onBuildEvent(final AuthInfo authInfo, final BuildingType type, final int x, final int y, final Callback<BuildResult> callback, final Runnable failCallback) {
        if (buildEvent != null)
            return;

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String ip;
                int port;
                synchronized (SocketServer.this) {
                    ip = SocketServer.this.ip;
                    port = SocketServer.this.port;
                }

                String resultStr = requestSSL(ip, port, new BuildEvent(authInfo, type.toString(), x, y).toJson().toString());

                synchronized (SocketServer.this) {
                    buildEvent = null;
                }

                if (resultStr == null) {
                    onFail();
                    if (failCallback == null) {
                        game.showMessage(game.getI18NBundle().get("networkProblems"));
                        game.getExternalAPI().oSAPI.showMessage("BuildEvent failed: probable problems with connection");
                    } else
                        failCallback.run();
                    return;
                }
                onSuccess();

                try {
                    BuildResult result = BuildResult.fromJson(parser.parse(resultStr).getAsJsonObject());
                    if (result == null)
                        throw new IllegalArgumentException("Given type of BuildResult is unknown");
                    callback.onResult(result);
                } catch (Exception e) {
                    game.getExternalAPI().oSAPI.showMessage("BuildEvent failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
            }
        });

        thread.start();

        buildEvent = new CancelHandle() {
                @Override
                public void cancel() {
                    thread.interrupt();
                }
            };

    }

    @Override
    public synchronized void cancelSectorBuildEvent() {
        if (sectorBuildEvent != null) {
            sectorBuildEvent.cancel();
            sectorBuildEvent = null;
        }
    }

    @Override
    public synchronized void onSectorBuildEvent(final AuthInfo authInfo, final int x, final int y, final Callback<SectorBuildResult> callback, final Runnable failCallback) {
        if (sectorBuildEvent != null)
            return;

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String ip;
                int port;
                synchronized (SocketServer.this) {
                    ip = SocketServer.this.ip;
                    port = SocketServer.this.port;
                }

                String resultStr = requestSSL(ip, port, new SectorBuildEvent(authInfo, x, y).toJson().toString());

                synchronized (SocketServer.this) {
                    sectorBuildEvent = null;
                }

                if (resultStr == null) {
                    onFail();
                    if (failCallback == null) {
                        game.showMessage(game.getI18NBundle().get("networkProblems"));
                        game.getExternalAPI().oSAPI.showMessage("SectorBuildEvent failed: probable problems with connection");
                    } else
                        failCallback.run();
                    return;
                }
                onSuccess();

                try {
                    SectorBuildResult result = SectorBuildResult.fromJson(parser.parse(resultStr).getAsJsonObject());
                    if (result == null)
                        throw new IllegalArgumentException("Given type of SectorBuildResult is unknown");
                    callback.onResult(result);
                } catch (Exception e) {
                    game.getExternalAPI().oSAPI.showMessage("SectorBuildEvent failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
            }
        });

        thread.interrupt();

        sectorBuildEvent = new CancelHandle() {
            @Override
            public void cancel() {
                thread.interrupt();
            }
        };

    }

    @Override
    public synchronized void cancelDestroyEvent() {
        if (destroyEvent != null) {
            destroyEvent.cancel();
            destroyEvent = null;
        }
    }

    @Override
    public synchronized void onDestroyEvent(final AuthInfo authInfo, final int id, final Callback<DestroyResult> callback, final Runnable failCallback) {
        if (destroyEvent != null)
            destroyEvent = null;

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String ip;
                int port;
                synchronized (SocketServer.this) {
                    ip = SocketServer.this.ip;
                    port = SocketServer.this.port;
                }

                String resultStr = requestSSL(ip, port, new DestroyEvent(authInfo, id).toJson().toString());

                synchronized (SocketServer.this) {
                    destroyEvent = null;
                }

                if (resultStr == null) {
                    onFail();
                    if (failCallback == null) {
                        game.showMessage(game.getI18NBundle().get("networkProblems"));
                        game.getExternalAPI().oSAPI.showMessage("DestroyEvent failed: probable problems with connection");
                    } else
                        failCallback.run();
                    return;
                }
                onSuccess();

                try {
                    DestroyResult result = DestroyResult.fromJson(parser.parse(resultStr).getAsJsonObject());
                    if (result == null)
                        throw new IllegalArgumentException("Given type of DestroyResult is unknown");
                    callback.onResult(result);
                } catch (Exception e) {
                    game.getExternalAPI().oSAPI.showMessage("DestroyEvent failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
            }
        });

        thread.start();

        destroyEvent = new CancelHandle() {
            @Override
            public void cancel() {
                thread.interrupt();
            }
        };

    }

    @Override
    public synchronized void cancelUnitBuildEvent() {
        if (unitBuildEvent != null) {
            unitBuildEvent.cancel();
            unitBuildEvent = null;
        }
    }

    @Override
    public synchronized void onUnitBuildEvent(final AuthInfo authInfo, final UnitType type, final Building building, final Callback<UnitBuildResult> callback, final Runnable failCallback) {
        if (unitBuildEvent != null)
            return;

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String ip;
                int port;
                synchronized (SocketServer.this) {
                    ip = SocketServer.this.ip;
                    port = SocketServer.this.port;
                }

                String resultStr = requestSSL(ip, port, new UnitBuildEvent(authInfo, type.toString(), building.id).toJson().toString());

                synchronized (SocketServer.this) {
                    unitBuildEvent = null;
                }

                if (resultStr == null) {
                    onFail();
                    if (failCallback == null) {
                        game.showMessage(game.getI18NBundle().get("networkProblems"));
                        game.getExternalAPI().oSAPI.showMessage("UnitBuildEvent failed: probable problems with connection");
                    } else
                        failCallback.run();
                    return;
                }
                onSuccess();

                try {
                    UnitBuildResult result = UnitBuildResult.fromJson(parser.parse(resultStr).getAsJsonObject());
                    if (result == null)
                        throw new IllegalArgumentException("Given type of UnitBuildResult is unknown");
                    callback.onResult(result);
                } catch (Exception e) {
                    game.getExternalAPI().oSAPI.showMessage("UnitBuildResult failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
            }
        });

        thread.start();

        unitBuildEvent = new CancelHandle() {
            @Override
            public void cancel() {
                thread.interrupt();
            }
        };

    }

    @Override
    public synchronized void cancelResearchEvent() {
        if (researchEvent != null) {
            researchEvent.cancel();
            researchEvent = null;
        }
    }

    @Override
    public synchronized void onResearchEvent(final AuthInfo authInfo, final ResearchType researchType, final Callback<ResearchResult> callback, final Runnable failCallback) {
        if (researchEvent != null)
            return;

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String ip;
                int port;
                synchronized (SocketServer.this) {
                    ip = SocketServer.this.ip;
                    port = SocketServer.this.port;
                }

                String resultStr = requestSSL(ip, port, new ResearchEvent(authInfo, researchType.toString()).toJson().toString());

                synchronized (SocketServer.this) {
                    researchEvent = null;
                }

                if (resultStr == null) {
                    onFail();
                    if (failCallback == null) {
                        game.showMessage(game.getI18NBundle().get("networkProblems"));
                        game.getExternalAPI().oSAPI.showMessage("ResearchEvent failed: probable problems with connection");
                    } else
                        failCallback.run();
                    return;
                }
                onSuccess();

                try {
                    ResearchResult result = ResearchResult.fromJson(parser.parse(resultStr).getAsJsonObject());
                    if (result == null)
                        throw new IllegalArgumentException("Given type of ResearchResult is unknown");
                    callback.onResult(result);
                } catch (Exception e) {
                    game.getExternalAPI().oSAPI.showMessage("ResearchEvent failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
            }
        });

        thread.start();

        researchEvent = new CancelHandle() {
            @Override
            public void cancel() {
                thread.interrupt();
            }
        };

    }

    @Override
    public synchronized void cancelAttackEvent() {
        if (attackEvent != null) {
            attackEvent.cancel();
            attackEvent = null;
        }
    }

    @Override
    public synchronized void onAttackEvent(final AuthInfo authInfo, final int attackerId, final int victimId, final int[] hangarIds, final int sectorId, final Callback<AttackResult> callback, final Runnable failCallback) {
        if (attackEvent != null)
            return;

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String ip;
                int port;
                synchronized (SocketServer.this) {
                    ip = SocketServer.this.ip;
                    port = SocketServer.this.port;
                }

                String resultStr = requestSSL(ip, port, new AttackEvent(authInfo, attackerId, victimId, hangarIds, sectorId).toJson().toString());

                synchronized (SocketServer.this) {
                    attackEvent = null;
                }

                if (resultStr == null) {
                    onFail();
                    if (failCallback == null) {
                        game.showMessage(game.getI18NBundle().get("networkProblems"));
                        game.getExternalAPI().oSAPI.showMessage("AttackEvent failed: probable problems with connection");
                    } else
                        failCallback.run();
                    return;
                }
                onSuccess();

                try {
                    AttackResult result = AttackResult.fromJson(parser.parse(resultStr).getAsJsonObject());
                    if (result == null)
                        throw new IllegalArgumentException("Given type of AttackResult is unknown");
                    callback.onResult(result);
                } catch (Exception e) {
                    game.getExternalAPI().oSAPI.showMessage("AttackEvent failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
            }
        });

        thread.start();

        attackEvent = new CancelHandle() {
            @Override
            public void cancel() {
                thread.interrupt();
            }
        };

    }

    @Override
    public synchronized void cancelEmailConfirmationEvent() {
        if (emailConfirmationEvent != null) {
            emailConfirmationEvent.cancel();
            emailConfirmationEvent = null;
        }
    }

    @Override
    public synchronized void onEmailConfirmationEvent(final String name, final int code, final Callback<EmailConfirmationResult> callback, final Runnable failCallback) {
        if (emailConfirmationEvent != null)
            return;

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String ip;
                int port;
                synchronized (SocketServer.this) {
                    ip = SocketServer.this.ip;
                    port = SocketServer.this.port;
                }

                String resultStr = requestSSL(ip, port, new EmailConfirmationEvent(name, code).toJson().toString());

                synchronized (SocketServer.this) {
                    emailConfirmationEvent = null;
                }

                if (resultStr == null) {
                    if (failCallback == null) {
                        game.showMessage(game.getI18NBundle().get("networkProblems"));
                        game.getExternalAPI().oSAPI.showMessage("EmailConfirmationEvent failed: probable problems with connection");
                    } else
                        failCallback.run();
                    return;
                }

                try {
                    EmailConfirmationResult result = EmailConfirmationResult.fromJson(parser.parse(resultStr).getAsJsonObject());
                    if (result == null)
                        throw new IllegalArgumentException("Given type of EmailConfirmationResult is unknown");
                    callback.onResult(result);
                } catch (Exception e) {
                    game.getExternalAPI().oSAPI.showMessage("EmailConfirmationEvent failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
            }
        });

        thread.start();

        emailConfirmationEvent = new CancelHandle() {
            @Override
            public void cancel() {
                thread.interrupt();
            }
        };

    }

    @Override
    public synchronized void cancelEmailResendEvent() {
        if (emailResendEvent != null) {
            emailResendEvent.cancel();
            emailResendEvent = null;
        }
    }

    @Override
    public synchronized void onEmailResendEvent(final String name, final Callback<ResendEmailResult> callback, final Runnable failCallback) {
        if (emailResendEvent != null)
            return;

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String ip;
                int port;
                synchronized (SocketServer.this) {
                    ip = SocketServer.this.ip;
                    port = SocketServer.this.port;
                }

                String resultStr = requestSSL(ip, port, new ResendEmailEvent(name).toJson().toString());

                synchronized (SocketServer.this) {
                    emailResendEvent = null;
                }

                if (resultStr == null) {
                    if (failCallback == null) {
                        game.showMessage(game.getI18NBundle().get("networkProblems"));
                        game.getExternalAPI().oSAPI.showMessage("ResendEmailEvent failed: probable problems with connection");
                    } else
                        failCallback.run();
                    return;
                }

                try {
                    ResendEmailResult result = ResendEmailResult.fromJson(parser.parse(resultStr).getAsJsonObject());
                    if (result == null)
                        throw new IllegalArgumentException("Given type of EmailResendResult is unknown");
                    callback.onResult(result);
                } catch (Exception e) {
                    game.getExternalAPI().oSAPI.showMessage("ResendEmailEvent failed: " + e.getClass().getName() + ", see GeoBattleError for details");
                    Gdx.app.error("GeoBattleError", e.getClass().getName() + ": " + e.getMessage() + ". Server returned: " + resultStr);
                }
            }
        });

        thread.start();

        emailResendEvent = new CancelHandle() {
            @Override
            public void cancel() {
                thread.interrupt();
            }
        };

    }
}
