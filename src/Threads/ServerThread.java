package Threads;

import Protocols.ServerProtocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by michel on 20-12-2016.
 */
public class ServerThread extends Thread {
    private Socket socket = null;

    public ServerThread(Socket socket) {
        super("ServerThread");
        this.socket = socket;
    }

    public void run() {

        try (
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream()))
        ) {
            String inputLine, outputLine;
            ServerProtocol serverProtocol = new ServerProtocol();
            outputLine = serverProtocol.processInput(null, socket.getOutputStream());
            System.out.println("Server: " + outputLine);
            out.println(outputLine);

            while ((inputLine = in.readLine()) != null) {
                System.out.println("Client: " + inputLine);
                outputLine = serverProtocol.processInput(inputLine, socket.getOutputStream());

                if (outputLine == null) {
                    break;
                }

                System.out.println("Server: " + outputLine);
                out.println(outputLine);

            }
            socket.close();
            System.out.println("Socket closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}