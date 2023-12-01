package src.threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import src.DTO;
import src.OperationService;
import src.enums.OperationType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.stream.Collectors;

public class ServerThread implements Runnable {

    private final Logger LOGGER = LogManager.getLogger(ServerThread.class);
    private final ServerSocket serverSocket;
    private final Socket clientSocket;
    private final ObjectInputStream reader;
    private final ObjectOutputStream writer;
    private final OperationService operationService;
    private Set<String> servers;
    private Boolean isRunning;

    public ServerThread(ServerSocket serverSocket, Socket clientSocket, Set<String> servers,
                        boolean isRunning) throws IOException {
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
        this.reader = new ObjectInputStream(clientSocket.getInputStream());
        this.writer = new ObjectOutputStream(clientSocket.getOutputStream());
        this.operationService = OperationService.getInstance();
        this.servers = servers;
        this.isRunning = isRunning;
    }

    @Override
    public void run() {
        try {
            processOperation(receiveData());
            closeClientConnection();
        } catch (Exception ex) {
        }
    }

    private void processOperation(DTO dto) throws Exception {
        if (dto == null || dto.getOperation() == null) throw new Exception("Incorrect DTO");
        switch (dto.getOperation()) {
            case LIST_OF_SERVERS -> {
                sendData(new DTO(OperationType.LIST_OF_SERVERS, String.join(";", servers).getBytes()));
            }
            case HEALTH_CHECK -> {
                sendData(dto);
            }
            case CLOSE_SERVER -> {
                closeServer();
                isRunning = false;
                sendData(dto);
            }
        }
    }

    private DTO receiveData() throws IOException, ClassNotFoundException {
        return (DTO) reader.readObject();
    }

    private void sendData(DTO dto) throws IOException {
        writer.writeObject(dto);
        writer.flush();
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
