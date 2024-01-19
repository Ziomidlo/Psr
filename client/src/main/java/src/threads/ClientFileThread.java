package src.threads;

import src.DTO;
import src.enums.OperationType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/***
 * Wątek odpowiedzialny za interakcje z plikami.
 */
public class ClientFileThread implements Runnable {

    private final ClientCommunicationThread communicationThread;
    private Boolean isRunning;

    public ClientFileThread(ClientCommunicationThread communicationThread) {
        this.communicationThread = communicationThread;
    }

    @Override
    public void run() {
        while (isRunning) {
            // Implementacja logiki operacji na plikach
        }
    }



    public void sendFile(File file) throws IOException {
        byte[] data = Files.readAllBytes(file.toPath());
        DTO dto = new DTO(OperationType.SAVE, file.getName(), data);
        communicationThread.sendRequest(dto);
    }

    public void deleteFile(String fileName) throws IOException {
        DTO dto = new DTO(OperationType.DELETE, fileName);
        communicationThread.sendRequest(dto);
    }

    public void readFile(String fileName) throws IOException, ClassNotFoundException {
        DTO dto = new DTO(OperationType.READ, fileName);
        communicationThread.sendRequest(dto);
        DTO response = communicationThread.receiveResponse();
    }
}
