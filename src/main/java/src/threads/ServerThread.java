package src.threads;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import src.DTO;
import src.Utils;
import src.enums.OperationType;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Set;

/***
 * Watek odpowiedzialny za przetwarzanie poszczegolnych zadan wysylanych do obecnej instancji, zarowno z
 * innych instancji jak i od klientow.
 */
public class ServerThread implements Runnable {

    private final Logger LOGGER = LogManager.getLogger(ServerThread.class);
    private final ServerSocket serverSocket;
    private final Socket clientSocket;
    private final ObjectInputStream reader;
    private final ObjectOutputStream writer;
    private Set<String> servers;
    private Boolean isRunning;

    private final String FILES_PATH = "C:\\server_files\\";

    public ServerThread(ServerSocket serverSocket, Socket clientSocket, Set<String> servers,
                        Boolean isRunning) throws IOException {
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
        this.writer = new ObjectOutputStream(clientSocket.getOutputStream());
        this.reader = new ObjectInputStream(clientSocket.getInputStream());
        this.servers = servers;
        this.isRunning = isRunning;
    }

    /***
     * Glowna metoda watku.
     */
    @Override
    public void run() {
        try {
            processOperation(receiveData());
            closeClientConnection();
        } catch (Exception ex) {
//            LOGGER.error("Error while processing client request.");
        }
    }

    /***
     * Metoda odpowiedzialna za ustalenie jaka operacja powinna zostac wykonana.
     * @param dto Obiekt DTO otrzymywany od klienta/innej instancji.
     */
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
                isRunning = Boolean.FALSE;
                sendSimplifiedResponse(true);
            }
        }
    }

    /***
     * Metoda odpowiedzialna za odczytanie i zwrocenie pliku z instacji. W przypadku jego braku, odpytywane sa inne
     * instancje.
     * @param dto DTO.
     */
    private void readFile(DTO dto) throws IOException {
        File file = new File(FILES_PATH + dto.getKey());
        byte[] data = null;
        try {
            data = Files.readAllBytes(file.toPath());
        } catch (NoSuchFileException ex) {}
        if (!dto.isDataFromAnotherInstance() && (data == null || data.length == 0)) {
            for (String server : servers) {
                if (server.equals(Utils.getIpAddress(serverSocket.getLocalPort()))) continue;
                data = readFileAtAnotherInstance(dto.getKey(), server);
                if (data != null && data.length != 0) break;
            }
        }
        sendResponse(new DTO(OperationType.READ, data));
    }

    /***
     * Metoda odpowiedzialna za zwrocenie listy plikow na instancji.
     */
    private void readAllFiles() throws IOException {
        File file = new File(FILES_PATH);
        String[] fileNames = file.list();
        byte[] fileNamesAsBytes = StringUtils.join(fileNames, ";").getBytes();
        sendResponse(new DTO(OperationType.READ_ALL, fileNamesAsBytes));
    }

    /***
     * Metoda odpowiedzialna za utworzenie pliku na instancji. W przypadku gdy brakuje na niej miejsca,
     * plik tworzony jest na innej. Ponadto, tworzona jest kopia zapasowa pliku na innej instancji.
     * @param dto DTO.
     */
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

    /***
     * Metoda odpowiedzialna za usuniecie pliku z instancji. Plik usuwany jest rowniez z innych instancji,
     * gdzie przechowywany jest jako kopia zapasowa.
     * @param dto DTO.
     */
    private void deleteFile(DTO dto) throws IOException {
        File file = new File(FILES_PATH + dto.getKey());
        try {
            Files.delete(file.toPath());
        } catch (NoSuchFileException ex) {
            if (dto.isDataFromAnotherInstance()) sendSimplifiedResponse(false);
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

    /***
     * Metoda odpowiedzialna za odczytanie pliku z innej instancji.
     * @param key Klucz przyporzadkowany do pliku.
     * @param server IP i port instancji.
     * @return Plik w postaci tablicy bajtow, lub pusta tablica bajtow w przypadku jego braku na wskazanej instancji.
     */
    private byte[] readFileAtAnotherInstance(String key, String server) {
        String [] serverParts = server.split(":");
        try (Socket serverSocket = new Socket(serverParts[0], Integer.parseInt(serverParts[1]));
             ObjectInputStream reader = new ObjectInputStream(serverSocket.getInputStream());
             ObjectOutputStream writer = new ObjectOutputStream(serverSocket.getOutputStream())) {
            writer.writeObject(new DTO(OperationType.READ, key, true));
            writer.flush();
            DTO dto = (DTO) reader.readObject();
            return dto.getData();
        } catch (Exception ex) {
            LOGGER.error("Error while reading file at another instance.");
            return new byte[0];
        }
    }

    /***
     * Metoda odpowiedzialna za stworzenie pliku na innej instancji.
     * @param key Klucz przyporzadkowany do pliku.
     * @param data Plik w postaci tablicy bajtow.
     * @param server IP i port instancji.
     * @return true - zapis pomyslny, false - zapis niepomyslny.
     */
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
            LOGGER.error("Error while creating file at another instance.");
            return false;
        }
    }

    /***
     * Metoda odpowiedzialna za usuniecie pliku na innej instancji.
     * @param key Klucz przyporzadkowany do pliku.
     * @param server IP i port instancji.
     * @return true - usuniecie pomyslne, false - usuniecie niepomyslne.
     */
    private boolean deleteFileAtAnotherInstance(String key, String server) {
        String [] serverParts = server.split(":");
        try (Socket serverSocket = new Socket(serverParts[0], Integer.parseInt(serverParts[1]));
             ObjectInputStream reader = new ObjectInputStream(serverSocket.getInputStream());
             ObjectOutputStream writer = new ObjectOutputStream(serverSocket.getOutputStream())) {
            writer.writeObject(new DTO(OperationType.DELETE, key, true));
            writer.flush();
            return reader.readBoolean();
        } catch (Exception ex) {
            LOGGER.error("Error while deleting file at another instance.");
            return false;
        }
    }

    /***
     * Metoda odpowiedzialna za zwrocenie ilosci dostepnych bajtow przestrzeni dyskowej instancji.
     * @return Ilosc dostepnych bajtow miejsca na dysku.
     */
    private Long getAvailableBytes() {
        return new File(FILES_PATH).getUsableSpace();
    }

    /***
     * Metoda odpowiedzialna za otrzymanie danych od klienta/innej instancji.
     * @return Obiekt DTO.
     */
    private DTO receiveData() throws IOException, ClassNotFoundException {
        return (DTO) reader.readObject();
    }

    /***
     * Metoda odpowiedzialna za wyslanie odpowiedzi.
     * @param dto Obiekt DTO.
     */
    private void sendResponse(DTO dto) throws IOException {
        writer.writeObject(dto);
         writer.flush();
    }

    /***
     * Metoda odpowiedzialna za wyslanie odpowiedzi w postaci uproszczonej (true/false zamiast obiektu DTO).
     * @param response Odpowiedz - true/false.
     */
    private void sendSimplifiedResponse(boolean response) throws IOException {
        writer.writeBoolean(response);
        writer.flush();
    }

    /***
     * Metoda odpowiedzialna za zamkniecie polaczenia z klientem.
     */
    private void closeClientConnection() throws IOException {
        clientSocket.close();
        reader.close();
        writer.close();
        Thread.currentThread().interrupt();
    }

    /***
     * Metoda odpowiedzialna za zamkniecie instancji serwera.
     */
    private void closeServer() throws IOException {
        serverSocket.close();
        closeClientConnection();
    }
}
