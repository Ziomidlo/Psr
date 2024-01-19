package src;

/***
 * Klasa odpowiadająca za otrzymanie adresów ip oraz portu serwerów do wyboru.
 */
public class ServerInfo {
    private String ipAddress;
    private int port;

    public ServerInfo(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public static ServerInfo parse(String serverInfoString) {
        try {
            String[] parts = serverInfoString.split(":");
            if (parts.length == 2) {
                String ipAddress = parts[0];
                int port = Integer.parseInt(parts[1]);
                return new ServerInfo(ipAddress, port);
            } else {
                System.err.println("Invalid serverInfo format: " + serverInfoString);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number in serverInfo: " + serverInfoString);
        }
        return null;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return ipAddress + ":" + port;
    }
}

