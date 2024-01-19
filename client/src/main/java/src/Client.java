package src;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import src.enums.OperationType;
import src.threads.BroadcastListenerThread;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/***
 * Klasa reprezentujaca klienta.
 */
public class Client {

    private static final Logger LOGGER = LogManager.getLogger(Client.class);
    private static Boolean isRunning;

    public static void main(String[] args) {

        Set<ServerInfo> availableServers = new HashSet<>();
        int broadcastPort = 8081;

        Thread broadcastListenerThread = new Thread(new BroadcastListenerThread(availableServers, broadcastPort));
        broadcastListenerThread.start();

        // Wątek wybierający serwer z dostępnych
        Thread serverChooserThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                displayAvailableServers(availableServers);
                System.out.println("Wybierz numer serwera (0 - odśwież, -1 - wyjście):");
                int choice = scanner.nextInt();

                if (choice == 0) {
                    continue;
                } else if (choice == -1) {
                    broadcastListenerThread.interrupt();
                    break;
                }

                ServerInfo selectedServer = getServerByChoice(availableServers, choice);
                if (selectedServer != null) {
                    // Nawiązanie połączenia z wybranym serwerem
                    connectToServer(selectedServer);
                    System.out.println("Połączono z serwerem!" + selectedServer);
                    break;
                } else {
                    System.out.println("Nieprawidłowy numer serwera. Spróbuj ponownie.");
                }
            }
        });
        serverChooserThread.start();
    }

    private static ServerInfo getServerByChoice(Set<ServerInfo> availableServers, int choice) {
        int index = 1;
        for (ServerInfo server : availableServers) {
            if (index == choice) {
                return server;
            }
            index++;
        }
        return null;
    }

    private static void displayAvailableServers(Set<ServerInfo> availableServers) {
        System.out.println("Dostępne serwery:");
        int index = 1;
        for (ServerInfo server : availableServers) {
            System.out.println(index++ + ". " + server);
        }
        System.out.println("0. Odśwież");
        System.out.println("-1. Wyjście");
    }

    private static void connectToServer(ServerInfo server) {
            //Tu będzie dodawanie wczytywanie i uruchamianie plików.
    }
}
