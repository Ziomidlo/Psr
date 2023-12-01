package src.threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import src.DTO;
import src.enums.OperationType;

import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;

public class HealthCheckThread implements Runnable {

    private final Logger LOGGER = LogManager.getLogger(HealthCheckThread.class);
    private Set<String> servers;
    private Integer serverPort;
    private Boolean isRunning;

    public HealthCheckThread(Set<String> servers, Integer serverPort, Boolean isRunning) {
        this.servers = servers;
        this.serverPort = serverPort;
        this.isRunning = isRunning;
    }

    @Override
    public void run() {
        while (isRunning) {
            for (String server : servers) {
                String [] serverParts = server.split(":");
                if (server.equals(getIpAddress())) continue;
                try (Socket serverSocket = new Socket(serverParts[0], Integer.parseInt(serverParts[1]));
                     ObjectOutputStream writer = new ObjectOutputStream(serverSocket.getOutputStream())) {
                    writer.writeObject(null);
                    writer.flush();
                } catch (Exception ex) {
                    servers.remove(server);
                }
            }
            try {
                Thread.sleep(5000);
            } catch (Exception ex) {
                LOGGER.error("");
                ex.printStackTrace();
            }
        }
    }

    private String getIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress() + ":" + serverPort;
        } catch (UnknownHostException e) {
            LOGGER.error("");
            e.printStackTrace();
            return null;
        }
    }
}
