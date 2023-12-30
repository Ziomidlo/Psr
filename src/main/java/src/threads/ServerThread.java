package src.threads;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import src.DTO;
import src.Utils;
import src.enums.OperationType;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Set;

public class ServerThread implements Runnable {

    private final Logger LOGGER = LogManager.getLogger(ServerThread.class);
    private final ServerSocket serverSocket;
    private final Socket clientSocket;
    private final ObjectInputStream reader;
    private final ObjectOutputStream writer;
    private Set<String> servers;
    private Boolean isRunning;

    private final String FILES_PATH = "C:\\";

    public ServerThread(ServerSocket serverSocket, Socket clientSocket, Set<String> servers,
                        boolean isRunning) throws IOException {
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
        this.reader = new ObjectInputStream(clientSocket.getInputStream());
        this.writer = new ObjectOutputStream(clientSocket.getOutputStream());
        this.servers = servers;
        this.isRunning = isRunning;
    }

    @Override
    public void run() {
        try {
            processOperation(receiveData());
            closeClientConnection();
        } catch (Exception ex) {}
    }

    private void processOperation(DTO dto) throws Exception {
        if (dto == null || dto.getOperation() == null) throw new Exception("Incorrect DTO");
        switch (dto.getOperation()) {
            case READ -> readFile(dto);
            case READ_ALL -> readAllFiles();
            case SAVE -> createFile(dto);
            case DELETE -> deleteFile(dto);
            case LIST_OF_SERVERS -> sendResponse(new DTO(OperationType.LIST_OF_SERVERS, String.join(";", servers).getBytes()));
            case HEALTH_CHECK -> sendSimplifiedResponse(true);
            case CLOSE_SERVER -> {
                closeServer();
                isRunning = false;
                sendSimplifiedResponse(true);
            }
        }
    }

    private void readFile(DTO dto) throws IOException {
        File file = new File(FILES_PATH + dto.getKey());
        byte[] data = Files.readAllBytes(file.toPath());
        if (!dto.isDataFromAnotherInstance() && (data == null || data.length == 0)) {
            for (String server : servers) {
                if (server.equals(Utils.getIpAddress(serverSocket.getLocalPort()))) continue;
                data = readFileAtAnotherInstance(dto.getKey(), server);
                if (data != null && data.length != 0) break;
            }
        }
        sendResponse(new DTO(OperationType.READ, data));
    }

    private void readAllFiles() throws IOException {
        File file = new File(FILES_PATH);
        String[] fileNames = file.list();
        byte[] fileNamesAsBytes = StringUtils.join(fileNames, ";").getBytes();
        sendResponse(new DTO(OperationType.READ_ALL, fileNamesAsBytes));
    }

    private void createFile(DTO dto) throws IOException {
        String alternativeServerIpAddress = "";
        if (getAvailableBytes() >= dto.getData().length) {
            File file = new File(FILES_PATH + dto.getKey());
            Files.write(file.toPath(), dto.getData());
        } else if (!dto.isDataFromAnotherInstance()) {
            for (String server : servers) {
                if (server.equals(Utils.getIpAddress(serverSocket.getLocalPort()))) continue;
                alternativeServerIpAddress = server;
                if (createFileAtAnotherInstance(dto.getKey(), dto.getData(), server)) break;
            }
        } else {
            sendResponse(new DTO(OperationType.SAVE));
            return;
        }
        if (!dto.isDataFromAnotherInstance()) {
            for (String server : servers) {
                if (server.equals(Utils.getIpAddress(serverSocket.getLocalPort())) || server.equals(alternativeServerIpAddress)) continue;
                if (createFileAtAnotherInstance(dto.getKey(), dto.getData(), server)) break;
            }
        }
        sendResponse(new DTO(OperationType.SAVE, dto.getKey()));
    }

    private void deleteFile(DTO dto) throws IOException {
        File file = new File(FILES_PATH + dto.getKey());
        try {
            Files.delete(file.toPath());
        } catch (NoSuchFileException ex) {
            sendSimplifiedResponse(false);
        }
        if (!dto.isDataFromAnotherInstance()) {
            for (String server : servers) {
                if (server.equals(Utils.getIpAddress(serverSocket.getLocalPort()))) continue;
                if (deleteFileAtAnotherInstance(dto.getKey(), server)) break;
            }
        } else {
            sendSimplifiedResponse(true);
        }
        sendResponse(new DTO(OperationType.DELETE));
    }

    private byte[] readFileAtAnotherInstance(String key, String server) {
        String [] serverParts = server.split(":");
        try (Socket serverSocket = new Socket(serverParts[0], Integer.parseInt(serverParts[1]));
             ObjectInputStream reader = new ObjectInputStream(serverSocket.getInputStream());
             ObjectOutputStream writer = new ObjectOutputStream(serverSocket.getOutputStream())) {
            writer.writeObject(new DTO(OperationType.READ, key, true));
            writer.flush();
            return reader.readAllBytes();
        } catch (Exception ex) {
            ex.printStackTrace();
            return new byte[0];
        }
    }

    private boolean createFileAtAnotherInstance(String key, byte[] data, String server) {
        String [] serverParts = server.split(":");
        try (Socket serverSocket = new Socket(serverParts[0], Integer.parseInt(serverParts[1]));
             ObjectInputStream reader = new ObjectInputStream(serverSocket.getInputStream());
             ObjectOutputStream writer = new ObjectOutputStream(serverSocket.getOutputStream())) {
            writer.writeObject(new DTO(OperationType.SAVE, key, data, true));
            writer.flush();
            DTO dto = (DTO) reader.readObject();
            return !dto.getKey().isBlank();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean deleteFileAtAnotherInstance(String key, String server) {
        String [] serverParts = server.split(":");
        try (Socket serverSocket = new Socket(serverParts[0], Integer.parseInt(serverParts[1]));
             ObjectInputStream reader = new ObjectInputStream(serverSocket.getInputStream());
             ObjectOutputStream writer = new ObjectOutputStream(serverSocket.getOutputStream())) {
            writer.writeObject(new DTO(OperationType.DELETE, key));
            writer.flush();
            return reader.readBoolean();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private Long getAvailableBytes() {
        return new File(FILES_PATH).getUsableSpace();
    }

    private DTO receiveData() throws IOException, ClassNotFoundException {
        return (DTO) reader.readObject();
    }

    private void sendResponse(DTO dto) throws IOException {
        writer.writeObject(dto);
        writer.flush();
    }

    private void sendSimplifiedResponse(boolean response) throws IOException {
        writer.writeBoolean(response);
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
