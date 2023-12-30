package src.threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import src.Utils;

import java.io.IOException;
import java.net.*;

public class BroadcastSenderThread implements Runnable {

    private final Logger LOGGER = LogManager.getLogger(BroadcastSenderThread.class);
    private final InetAddress address;
    private final Integer broadcastPort;
    private final Integer serverPort;
    private final Boolean isRunning;

    public BroadcastSenderThread(Integer broadcastPort, Integer serverPort,
                                 Boolean isRunning) throws UnknownHostException {
        this.address = InetAddress.getByName("255.255.255.255");
        this.broadcastPort = broadcastPort;
        this.serverPort = serverPort;
        this.isRunning = isRunning;
    }

    @Override
    public void run() {
        String ipAddress = Utils.getIpAddress(serverPort);
        if (ipAddress.isEmpty()) {
            LOGGER.error("");
        } else {
            while (isRunning) {
                sendBroadcast(ipAddress);
            }
        }
    }

    private void sendBroadcast(String ipAddress) {
        byte [] data = ipAddress.getBytes();
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.send(new DatagramPacket(data, data.length, address, broadcastPort));
        } catch (IOException e) {
            LOGGER.error("");
            e.printStackTrace();
        }
    }
}
