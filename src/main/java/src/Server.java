package src;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import src.enums.ConnectionType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;

class Server {

    //private final Logger logger = LogManager.getLogger(Server.class);
    /*private final Socket connectionSocket;
    private final ObjectInputStream objectInputStream;
    private final ObjectOutputStream objectOutputStream;

    private final String exchangeHost = "localhost";
    private final Integer exchangePort = 8080;

    public Server() throws IOException {
        this.connectionSocket = new Socket(exchangeHost, exchangePort);
        this.objectOutputStream = new ObjectOutputStream(connectionSocket.getOutputStream());
        this.objectInputStream = new ObjectInputStream(connectionSocket.getInputStream());
    }

    public void start() throws IOException {
        try {
            connectToExchange();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error while connecting to exchange: " + Arrays.toString(e.getStackTrace()));
            return;
        }
        while (!connectionSocket.isClosed()) {
            System.out.printf("");
        }
    }

    private void connectToExchange() throws IOException, ClassNotFoundException {
        logger.info("Connecting to exchange with host: " + exchangeHost + " and port: " + exchangePort);
        DTO dto = new DTO(ConnectionType.SERVER);
        objectOutputStream.writeObject(dto);
        objectOutputStream.flush();
        DTO dtoResponse = (DTO) objectInputStream.readObject();
        if (dtoResponse != null && dtoResponse.getConnectionType().equals(ConnectionType.EXCHANGE))
            logger.info("Connected to exchange with host: " + exchangeHost + " and port: " + exchangePort);
        else throw new IOException("Incorrect connection type");
    }*/

    public void start() {
        /*try {
            broadcastToOtherServers();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            try {
                list();
            } catch (SocketException e) {
                System.out.println("x");
            }
        }*/
        MulticastSocket multicastSocket;
        try {
            multicastSocket = new MulticastSocket(8080);
            InetAddress group = InetAddress.getByName("255.255.255.255");
            multicastSocket.joinGroup(group);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        /*try {
            socket = new DatagramSocket(8080);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }*/
        byte [] data = new byte[256];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        try {
            multicastSocket.receive(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("x");
    }

    private void broadcastToOtherServers() throws IOException {
        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);
        byte [] bytes = "TEST".getBytes();
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, InetAddress.getByName("255.255.255.255"), 8080);
        socket.send(packet);
        socket.close();
    }

    private void list() throws SocketException {
        List<InetAddress> addresses = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            networkInterface.getInterfaceAddresses().stream()
                    .map(a -> a.getBroadcast())
                    .filter(Objects::nonNull)
                    .forEach(addresses::add);
        }
        System.out.println("test2");
    }
}
