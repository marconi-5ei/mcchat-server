package mcchat.server.networking;

import java.io.IOException;
import java.net.ServerSocket;

public class ConnectionWatchdog {
    public ConnectionWatchdog() {
        try (ServerSocket watchdog = new ServerSocket(1502)) {
            new ConnectionHandler(watchdog).start();
            System.out.println("New handler thread dispatched.");
        } catch (final IOException ignored) {}
    }

    public static void main(String[] args) {
        new ConnectionWatchdog();
    }
}

