package src;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;

public class Main {

    //private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        /*try {
            Server server = new Server();
            server.start();
        } catch (IOException e) {
            logger.error("Error while running server: " + Arrays.toString(e.getStackTrace()));
        }*/
        Server server = new Server();
        server.start();
    }
}
