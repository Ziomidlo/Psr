package src.threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Set;

/***
 * Wątek nasluchujacy na otrzymanie broadcastu.
 */
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

    /***
     * Glowna metoda watku.
     */
    @Override
    public void run() {
        byte[] data;
        int previousServerNumber;
        while (isRunning) {
            previousServerNumber = servers.size();
            data = receiveBroadcastData();
            processOperation(data);
            if (servers.size() != previousServerNumber) {
                LOGGER.info("Number of server instances: " + servers.size());
            }
        }
    }

    /***
     * Metoda odpowiedzialna za odbior danych z broadcastu.
     * @return Tablica bajtow z danymi.
     */
    private byte[] receiveBroadcastData() {
        byte[] receiveData = new byte[21];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try (DatagramSocket socket = new DatagramSocket(broadcastPort)) {
            socket.receive(receivePacket);
            return receivePacket.getData();
        } catch (IOException e) {}
        return null;
    }

    /***
     * Metoda odpowiedzialna za przetworzenie otrzymanych danych i ewentualny zapis instancji.
     * @param dataBytes Otrzymane dane.
     */
    private void processOperation(byte[] dataBytes) {
        if (dataBytes == null) return;
        String dataAsString = new String(dataBytes).trim();
        if (dataAsString.isEmpty()) return;
        servers.add(dataAsString);
    }
}
