package src.threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import src.DTO;
import src.Utils;
import src.enums.OperationType;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
        DTO dto = new DTO(OperationType.HEALTH_CHECK);
        while (isRunning) {
            for (String server : servers) {
                String [] serverParts = server.split(":");
                if (server.equals(Utils.getIpAddress(serverPort))) continue;
                try (Socket serverSocket = new Socket(serverParts[0], Integer.parseInt(serverParts[1]));
                     ObjectInputStream reader = new ObjectInputStream(serverSocket.getInputStream());
                     ObjectOutputStream writer = new ObjectOutputStream(serverSocket.getOutputStream())) {
                    writer.writeObject(dto);
                    writer.flush();
                    if (!reader.readBoolean()) servers.remove(server);
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
}
