package mcchat.server.networking;

import mcchat.server.packets.InfoPacket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionHandler extends Thread {

    ServerSocket watchdog;
    DataOutputStream outputStream;

    public ConnectionHandler(ServerSocket connWatchdog) {
        this.watchdog = connWatchdog;
    }

    @Override
    public void run() {
        while(true) {
            try (Socket socket = this.watchdog.accept()) {
                InfoPacket packet = new InfoPacket((byte) 0);
                this.outputStream.write(mcchat.server.packets.SerializationKt.serialize(packet));
                this.outputStream.flush();
            } catch (IOException ignored) {}
        }
    }
}


