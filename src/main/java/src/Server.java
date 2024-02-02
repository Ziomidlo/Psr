package src;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import src.threads.*;

import java.io.IOException;
import java.net.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/***
 * Klasa reprezentujaca serwer.
 */
class Server {

    private final Logger LOGGER = LogManager.getLogger(Server.class);
    private final ServerSocket serverSocket;
    private final Set<String> servers;
    private final Integer broadcastPort = 8081;
    private Boolean isRunning = true;

    public Server() throws IOException {
        this.serverSocket = new ServerSocket(0);
        this.servers = ConcurrentHashMap.newKeySet();
    }

    /***
     * Metoda odpowiedzialna za uruchomienie serwera. Uruchamiane sa tutaj watki dotyczace wysylania i
     * odczytywania broadcastu oraz watek odpowiedzialny za healthcheck. Znajduje sie tutaj rowniez glowna petla
     * programu, w ktorej znajduje sie nasluch na polaczenia klienckie. W przypadku zaakceptowania polaczenia klienta,
     * zostaje ono przekazane na odrebny watek serwera.
     */
    public void start() throws UnknownHostException {
        new Thread(new BroadcastSenderThread(broadcastPort, serverSocket.getLocalPort(), isRunning)).start();
        new Thread(new BroadcastListenerThread(servers, broadcastPort, isRunning)).start();
        new Thread(new HealthCheckThread(servers, serverSocket.getLocalPort(), isRunning)).start();
        LOGGER.info("Instance addrress: " + Utils.getIpAddress(serverSocket.getLocalPort()));
        Socket clientSocket;
        while (isRunning) {
            try {
                clientSocket = serverSocket.accept();
                new ServerThread(serverSocket, clientSocket, servers, isRunning).run();
            } catch (Exception ex) {
                LOGGER.error("Error while accepting client connection.");
            }
        }
    }
}
