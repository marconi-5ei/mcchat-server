package mcchat.server.networking;

import mcchat.server.packets.InfoPacket;
import mcchat.server.packets.SerializationKt;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ConnectionHandler extends Thread {

    Socket connection;

    public ConnectionHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        try(DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())){
            outputStream.write(SerializationKt.serialize(new InfoPacket((byte) 0)));
            outputStream.flush();
        } catch (IOException ignored) {}
    }
}


