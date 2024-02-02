package src.threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import src.ServerConnectionListener;
import src.ServerInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

/***
 * Wątek odpowiedzialny za nasłuchiwanie broadcastu w kliencie.
 */
public class ClientBroadcastListenerThread implements Runnable {

    private final Logger LOGGER = LogManager.getLogger(ClientBroadcastListenerThread.class);
    private final Integer broadcastPort;
    private final Boolean isRunning;

    private ServerConnectionListener serverConnectionListener;


    public ClientBroadcastListenerThread(Integer broadcastPort, Boolean isRunning) {
        this.broadcastPort = broadcastPort;
        this.isRunning = isRunning;
    }

    /***
     * Glówna metoda wątku.
     */
    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(broadcastPort)) {
            byte[] buffer = new byte[1024];
            while (isRunning) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String serverInfo = new String(packet.getData(), 0, packet.getLength());
                ServerInfo server = ServerInfo.parse(serverInfo);

                connectToServer(server);
            }
        } catch (Exception e) {
            LOGGER.error("Error while listening for broadcast.");
        }
    }

    private static Socket socket;

    /**
     * * metoda połaczenia sie z serwerem
     * @param server parametr przechowujacy informacje o serwerze
     */
    private void connectToServer(ServerInfo server) {
        try {
            if (socket == null || socket.isClosed()) {
                socket = new Socket(server.getIpAddress(), server.getPort());

                if (serverConnectionListener != null) {
                    serverConnectionListener.onServerConnected(socket, server);
                }
            }

        } catch (IOException e) {
            LOGGER.error("Error while connecting to the server.", e);
        }
    }

    public void setServerConnectionListener(ServerConnectionListener listener) {
        this.serverConnectionListener = listener;
    }

}
