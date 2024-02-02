package src;

import java.io.IOException;
import java.net.Socket;

/**
 * Interfejs sprawdzajacy polaczenie z serwerem
 */
public interface ServerConnectionListener {
    void onServerConnected(Socket serverSocket, ServerInfo server) throws IOException;
}

