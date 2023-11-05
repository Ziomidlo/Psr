package src;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

class ServerThread implements Runnable {

    private final Logger logger = LogManager.getLogger(ServerThread.class);
    private final ServerSocket serverSocket;
    private final Socket clientSocket;
    private final ObjectInputStream reader;
    private final ObjectOutputStream writer;
    private final Marshaller marshaller;

    public ServerThread(ServerSocket serverSocket, Socket clientSocket, Marshaller marshaller) throws IOException {
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
        this.reader = new ObjectInputStream(clientSocket.getInputStream());
        this.writer = new ObjectOutputStream(clientSocket.getOutputStream());
        this.marshaller = marshaller;
    }

    @Override
    public void run() {

        //closeClientConnection();
    }

    private void closeClientConnection() throws IOException {
        clientSocket.close();
        reader.close();
        writer.close();
        Thread.currentThread().interrupt();
    }

    private void closeServer() throws IOException {
        serverSocket.close();
        closeClientConnection();
    }
}
