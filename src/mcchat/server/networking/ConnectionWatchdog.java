package mcchat.server.networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionWatchdog {
    public ConnectionWatchdog() {
        try (ServerSocket watchdog = new ServerSocket(1502)) {
            while (true) {
                try (Socket socket = watchdog.accept()) {
                    new ConnectionHandler(socket).start();
                } catch (IOException ignored) {
                }
            }
        } catch (final IOException ignored) {
        }
    }

    public static void main(String[] args) {
        new ConnectionWatchdog();
    }
}

