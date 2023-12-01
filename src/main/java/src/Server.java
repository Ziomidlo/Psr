package src;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import src.threads.BroadcastListenerThread;
import src.threads.BroadcastSenderThread;
import src.threads.HealthCheckThread;
import src.threads.ServerThread;

import java.io.IOException;
import java.net.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

    public void start() throws UnknownHostException {
        new Thread(new BroadcastSenderThread(broadcastPort, serverSocket.getLocalPort(), isRunning)).start();
        new Thread(new BroadcastListenerThread(servers, broadcastPort, isRunning)).start();
        new Thread(new HealthCheckThread(servers, serverSocket.getLocalPort(), isRunning)).start();
        Socket clientSocket;
        while (isRunning) {
            try {
                clientSocket = serverSocket.accept();
                new ServerThread(serverSocket, clientSocket, servers, isRunning).run();
            } catch (Exception ex) {
                LOGGER.error("");
                ex.printStackTrace();
            }
        }
    }
}
