package src.threads;

import src.DTO;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientCommunicationThread implements Runnable {

    private final Socket serverSocket;
    private final ObjectInputStream reader;
    private final ObjectOutputStream writer;
    
    private final String clientName;
    private Boolean isRunning = true;

    public ClientCommunicationThread(Socket serverSocket, String clientName) throws IOException {
        this.serverSocket = serverSocket;
        this.reader = new ObjectInputStream(serverSocket.getInputStream());
        this.writer = new ObjectOutputStream(serverSocket.getOutputStream());
        this.clientName = clientName;
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                DTO receivedData = receiveResponse();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void sendRequest(DTO dto) throws IOException {
        writer.writeObject(dto);
        writer.flush();
    }

    public DTO receiveResponse() throws IOException, ClassNotFoundException {
        return (DTO) reader.readObject();
    }

    public void closeConnection() throws IOException {
        serverSocket.close();
        reader.close();
        writer.close();
    }

    public void isRunning(boolean b) {
    }
}
