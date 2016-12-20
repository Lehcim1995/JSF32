package jsf31kochfractalfx;

import Threads.ServerThread;
import calculate.Edge;
import calculate.KochFractal;
import timeutil.TimeStamp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.*;

public class ConsoleGenerator implements Observer {

    private List<Edge> edges = new ArrayList<>();

    public static void main(String[] args) {
        int portNumber = 1337;
        boolean listening = true;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (listening) {
                new ServerThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        edges.add(((Edge) arg));
    }
}
