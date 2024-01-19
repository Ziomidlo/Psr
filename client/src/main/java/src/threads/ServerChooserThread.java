package src.threads;

import src.ServerInfo;

import java.util.List;

public class ServerChooserThread extends Thread {
    private List<ServerInfo> availableServers;
    private ServerInfo chosenServer;

    public ServerChooserThread(List<ServerInfo> availableServers) {
        this.availableServers = availableServers;
    }

    public ServerInfo getChosenServer() {
        return chosenServer;
    }

    @Override
    public void run() {
        if (!availableServers.isEmpty()) {
            chosenServer = availableServers.get(0);
            System.out.println("Automatically chosen server: " + chosenServer);
        } else {
            System.out.println("No available servers found.");
        }
    }
}

