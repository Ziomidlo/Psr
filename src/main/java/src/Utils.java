package src;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Utils {

    private static final Logger LOGGER = LogManager.getLogger(Utils.class);

    public static String getIpAddress(Integer serverPort) {
        try {
            return InetAddress.getLocalHost().getHostAddress() + ":" + serverPort;
        } catch (UnknownHostException e) {
            LOGGER.error("");
            e.printStackTrace();
            return "";
        }
    }
}
