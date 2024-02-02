package src;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import src.threads.ClientBroadcastListenerThread;
import src.threads.ClientCommunicationThread;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/***
 * Klasa reprezentująca klienta.
 */
public class Client implements ServerConnectionListener {

    private static final Logger LOGGER = LogManager.getLogger(Client.class);
    private static Boolean isRunning;
    private static Socket serverSocket;
    private static ClientCommunicationThread communicationThread;
    private static boolean communicationThreadStarted = false;

    @Override
    public void onServerConnected(Socket serverSocket, ServerInfo server) throws IOException {
        System.out.println("Nawiązano połączenie z serwerem: " + server);
        communicationThread = new ClientCommunicationThread(serverSocket);
        Thread thread = new Thread(communicationThread);
        thread.start();
        communicationThreadStarted = true;
    }

    public static void main(String[] args) {
        isRunning = true;
        ClientBroadcastListenerThread broadcastListenerThread = new ClientBroadcastListenerThread(8081, isRunning);
        broadcastListenerThread.setServerConnectionListener(new Client());
        Thread broadcastThread = new Thread(broadcastListenerThread);
        broadcastThread.start();

        try {
            Scanner scanner = new Scanner(System.in);
            while (isRunning) {
                if (communicationThreadStarted) {
                printMenu();
                int choice = scanner.nextInt();

                    switch (choice) {
                        case 1:
                            createFile();
                            break;
                        case 2:
                            deleteFile();
                            break;
                        case 3:
                            readFile();
                            break;
                        case 4:
                            broadcastThread.interrupt();
                            closeConnection();
                            isRunning = false;
                            System.out.println("Pomyslnie zakonczono prace klienta.");
                            break;
                        default:
                            System.out.println("Niepoprawny wybór. Wybierz ponownie.");
                    }
                } else {
                    System.out.println("Czekam na połączenie z serwerem...");
                    Thread.sleep(1000);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error in the client main loop.", e);
        }
    }

    private static void printMenu() {
        System.out.println("Wybierz opcję:");
        System.out.println("1 - Utwórz nowy plik");
        System.out.println("2 - Usuń plik");
        System.out.println("3 - Odczytaj plik");
        System.out.println("4 - Wyjdź");
    }

    private static void createFile() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Podaj nazwę pliku do stworzenia: ");
        String fileName = scanner.nextLine();

        System.out.println("Podaj zawartość pliku: ");
        byte[] fileData = scanner.nextLine().getBytes();

        communicationThread.createFile(fileName, fileData);
    }

    private static void deleteFile() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Podaj nazwę pliku do usunięcia:");
        String fileName = scanner.nextLine();

        communicationThread.deleteFile(fileName);
    }

    private static void readFile() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Podaj nazwę pliku do odczytu:");
        String fileName = scanner.nextLine();

        communicationThread.readFile(fileName);
    }

    private static void closeConnection() throws IOException {
        communicationThread.closeConnection();
    }


}
