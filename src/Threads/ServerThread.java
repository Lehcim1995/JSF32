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
            out.println(outputLine);

            while ((inputLine = in.readLine()) != null) {
                outputLine = serverProtocol.processInput(inputLine, socket.getOutputStream());
                out.println(outputLine);
                if (outputLine.equals("Bye"))
                    break;
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}