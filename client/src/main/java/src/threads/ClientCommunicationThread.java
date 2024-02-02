package src.threads;

import src.DTO;
import src.enums.OperationType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientCommunicationThread implements Runnable {

    private final Socket serverSocket;
    private final ObjectInputStream reader;
    private final ObjectOutputStream writer;

    private DTO receivedData;


    private Boolean isRunning = true;

    /**
     * Klasa reprezentująca wątek komunikacyjny klienta.
     */

    public ClientCommunicationThread(Socket serverSocket) throws IOException {
        this.serverSocket = serverSocket;
        this.reader = new ObjectInputStream(serverSocket.getInputStream());
        this.writer = new ObjectOutputStream(serverSocket.getOutputStream());
    }

    /**
     * Metoda uruchamiana w wątku. Odbiera odpowiedzi od serwera w pętli.
     * Obsługuje wyjątki związane z wejściem/wyjściem i klasą nieznalezioną.
     */
    @Override
    public void run() {
        try {
            while (isRunning) {
                receivedData = receiveResponse();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metoda wysyłająca żądanie odczytu pliku do serwera, odbierająca odpowiedź i wyświetlająca zawartość pliku na konsoli.
     * Obsługuje wyjątki związane z wejściem/wyjściem.
     *
     * @param fileName Nazwa pliku do odczytu.
     */
    public void readFile(String fileName) {
        try {
            DTO request = new DTO(OperationType.READ, fileName);
            writer.writeObject(request);
            writer.flush();

            DTO response = receiveResponse();

            if (response.getOperation() == OperationType.READ && response.getData() != null) {
                byte[] fileData = response.getData();

                String fileContent = new String(fileData, StandardCharsets.UTF_8);

                System.out.println("Zawartość pliku:");
                System.out.println(fileContent);
            } else {
                System.out.println("Błąd podczas odczytu pliku.");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Błąd podczas komunikacji z serwerem.");
        }
    }


    /**
     * Metoda wysyłająca żądanie utworzenia pliku do serwera, odbierająca odpowiedź i wyświetlająca komunikat o powodzeniu.
     * Obsługuje wyjątki związane z wejściem/wyjściem.
     *
     * @param fileName Nazwa pliku do utworzenia.
     * @param data     Dane do zapisania w pliku.
     */
    public void createFile(String fileName, byte[] data) {
        try {
            DTO dto = new DTO(OperationType.SAVE, fileName, data);

            sendRequest(dto);


//            if(receivedData.getOperation() ==  OperationType.SAVE) {
                System.out.println("Plik stworzony pomyślnie.");
//            } else {
//                System.out.println("Błąd podczas tworzenia pliku.");
//            }
            refresh();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Metoda wysyłająca żądanie usunięcia pliku do serwera, odbierająca odpowiedź i wyświetlająca komunikat o powodzeniu.
     * Obsługuje wyjątki związane z wejściem/wyjściem i klasą nieznalezioną.
     *
     * @param fileName Nazwa pliku do usunięcia.
     */
    public void deleteFile(String fileName) {
        try {
            DTO request = new DTO(OperationType.DELETE, fileName);
            writer.writeObject(request);
            writer.flush();

//            if(reader != null ){
//                receivedData = (DTO) reader.readObject();
//            } else {
//                receivedData = null;
//            }

//            if (receivedData.getOperation() == OperationType.DELETE) {
                // Operacja usunięcia pliku zakończona sukcesem
                System.out.println("Plik usunięty pomyślnie.");
//            } else {
//                System.out.println("Błąd podczas usuwania pliku.");
//            }
            refresh();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Błąd podczas komunikacji z serwerem.");
        }
    }

    /**
     * Metoda wysyłająca żądanie do serwera.
     * Obsługuje wyjątek związany z wejściem/wyjściem.
     *
     * @param dto Obiekt DTO reprezentujący żądanie.
     * @throws IOException Wyjątek związany z wejściem/wyjściem.
     */
    public void sendRequest(DTO dto) throws IOException {
        writer.writeObject(dto);
        writer.flush();
    }

    /**
     * Metoda odbierająca odpowiedź od serwera.
     * Obsługuje wyjątki związane z wejściem/wyjściem i klasą nieznalezioną.
     *
     * @return Obiekt DTO reprezentujący odpowiedź.
     * @throws IOException            Wyjątek związany z wejściem/wyjściem.
     * @throws ClassNotFoundException Wyjątek klasa nieznaleziona.
     */

    public DTO receiveResponse() throws IOException, ClassNotFoundException {
        if (reader != null && reader.available() > 0) {
            return (DTO) reader.readObject();
        } else {
            return null;
        }
    }
    /**
     * Metoda zamykająca połączenie z serwerem.
     * Obsługuje wyjątek związany z wejściem/wyjściem.
     *
     * @throws IOException Wyjątek związany z wejściem/wyjściem.
     */

    public void refresh() throws IOException {
        isRunning = false;
        serverSocket.close();
    }

    public void closeConnection() throws IOException {
        serverSocket.close();
        reader.close();
        writer.close();
        if (!Thread.currentThread().isInterrupted()) {
            Thread.currentThread().interrupt();
        }
    }



}
