package src;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;

/***
 * Klasa zawierajaca metody pomocnicze.
 */
public class Utils {

    private static final Logger LOGGER = LogManager.getLogger(Utils.class);

    /***
     * Metoda odpowiedzialna za zwrocenie adresu IP instancji.
     * @param serverPort Port.
     * @return IP + port.
     */
    public static String getIpAddress(Integer serverPort) {
        try {
            return InetAddress.getLocalHost().getHostAddress() + ":" + serverPort;
        } catch (UnknownHostException e) {
            LOGGER.error("Error while getting IP address.");
            return "";
        }
    }
}
