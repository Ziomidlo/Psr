package src.threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Set;

public class BroadcastListenerThread implements Runnable {

    private final Logger LOGGER = LogManager.getLogger(BroadcastListenerThread.class);
    private Set<String> servers;
    private final Integer broadcastPort;
    private Boolean isRunning;

    public BroadcastListenerThread(Set<String> servers, Integer broadcastPort,
                                   boolean isRunning) {
        this.servers = servers;
        this.broadcastPort = broadcastPort;
        this.isRunning = isRunning;
    }

    @Override
    public void run() {
        byte[] data;
        int previousServerNumber;
        while (isRunning) {
            previousServerNumber = servers.size();
            data = receiveBroadcastData();
            processOperation(data);
            if (servers.size() != previousServerNumber) {
                System.out.println("Liczba instancji serwerów: " + servers.size());
            }
        }
    }

    private byte[] receiveBroadcastData() {
        byte[] receiveData = new byte[21];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try (DatagramSocket socket = new DatagramSocket(broadcastPort)) {
            socket.receive(receivePacket);
            return receivePacket.getData();
        } catch (IOException e) {
            //LOGGER.error("");
            //e.printStackTrace();
        }
        return null;
    }

    private void processOperation(byte[] dataBytes) {
        if (dataBytes == null) return;
        String dataAsString = new String(dataBytes).trim();
        if (dataAsString.isEmpty()) return;
        servers.add(dataAsString);
    }
}
