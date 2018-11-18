package mcchat.server.networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionWatchdog {
    public ConnectionWatchdog() {
        try (ServerSocket watchdog = new ServerSocket(1502)) {
            while (true) {
                try (Socket socket = watchdog.accept()) {
                    System.out.println("Connection arrived!");
                    new Thread(new ConnectionHandler(socket)).start();
                } catch (IOException ignored) {}
            }
        } catch (final IOException ignored) {}
    }
}
