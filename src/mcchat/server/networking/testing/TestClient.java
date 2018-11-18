package mcchat.server.networking.testing;

//Simple client used purely for testing purposes
//RUN THIS IN A SEPARATE THREAD / IDE INSTANCE!!! DO IT FOR YOUR SAKE!

import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

public class TestClient {

    private DataOutputStream os;
    private BufferedReader is;

    TestClient(){
        try(Socket s = new Socket("localhost", 1502)){
            this.os = new DataOutputStream(s.getOutputStream());
            this.is = new BufferedReader(new InputStreamReader(s.getInputStream()));
            System.out.println(is.read()); //Server output
        } catch (IOException ignored) {}
    }

}
