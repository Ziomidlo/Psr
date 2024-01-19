package src.threads;

import src.ServerInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Set;

/***
 * Wątek nasłuchujący broadcastów w klasie klienta.
 */
public class BroadcastListenerThread implements Runnable {
    private final Set<ServerInfo> availableServers;
    private final int broadcastPort;

    public BroadcastListenerThread(Set<ServerInfo> availableServers, int broadcastPort) {
        this.availableServers = availableServers;
        this.broadcastPort = broadcastPort;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(broadcastPort)) {
            byte[] receiveData = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(packet);
                String serverInfo = new String(packet.getData(), 0, packet.getLength());
                ServerInfo info = ServerInfo.parse(serverInfo);
                availableServers.add(info);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

