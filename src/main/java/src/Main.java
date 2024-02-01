package src;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/***
 * Glowna klasa serwera.
 */
public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    /***
     * Glowna metoda serwera. Znajduje sie tutaj wywolanie metody odpowiedzialnej za uruchomienie serwera.
     */
    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.start();
        } catch (Exception e) {
            LOGGER.error("Error while running server.");
        }
    }
}
