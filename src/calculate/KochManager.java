/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calculate;

import Protocols.ClientProtocol;
import Threads.ReadDrawEdge;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import jsf31kochfractalfx.JSF31KochFractalFX;
import timeutil.TimeStamp;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static Protocols.ClientProtocol.State.RECIEVING;

/**
 * @author michel
 */
public class KochManager {

    private KochFractal koch; //De fractal, niet echt meer nodig in dit geval maar ik laat het er toch in.
    private List<Edge> edgeList; // De gezamelijke lijst van edges voor de fractal
    private JSF31KochFractalFX application; // de  applicatie zelf

    private timeutil.TimeStamp calts; // timestap voor het berekenen van de fractal

    private final int SIDES = 3;
    private final String LEVELSTRING = "------------LEVEL ";
    private final String LEVELSTART = " START-------------";
    private final String LEVELEND = " DONE--------------";

    public KochManager(JSF31KochFractalFX application) {
        this.application = application;
        koch = new KochFractal();
        edgeList = new ArrayList<>();
    }

    public synchronized void changeLevel(int currentLevel) {

        String hostName = "localhost";
        int portNumber = 1337;

        try (
                Socket socket = new Socket(hostName, portNumber);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
        ) {
            BufferedReader stdIn =
                    new BufferedReader(new InputStreamReader(System.in));
            String fromServer;
            String fromUser;
            ClientProtocol clientProtocol = new ClientProtocol(this);

            while ((fromServer = in.readLine()) != null) {
                System.out.println("Server: " + fromServer);
                if (fromServer.equals("Bye."))
                    break;

                fromUser = clientProtocol.processInput(fromServer, socket.getInputStream());
                if (fromUser != null) {
                    System.out.println("Client: " + fromUser);
                    out.println(fromUser);
                }

                if (clientProtocol.getState() == RECIEVING)
                {
                    clientProtocol.processInput(null, socket.getInputStream());
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
        }

    }

    public void RequestDraw() {
        application.requestDrawEdges();
    }

    public synchronized void drawEdges() // tekent alle edges in de lijst
    {
        System.out.println("Draw Start " + koch.getLevel());
        timeutil.TimeStamp ts2 = new TimeStamp();
        ts2.setBegin();
        application.clearKochPanel();

        for (Edge edge : edgeList) {
            application.drawEdge(edge);
        }

        System.out.println("Draw End " + koch.getLevel());
        System.out.println(LEVELSTRING + koch.getLevel() + LEVELEND);
        ts2.setEnd();
        application.setTextDraw(ts2.toString());
    }

    public synchronized void DrawEdge(final Edge edge)// voor het toevoegen van 1 edge
    {
        Platform.runLater(new Runnable() // dit is zodat het op de gui thread wordt uitgevoert, als je dit weghaald werkt het niet.
        {
            @Override
            public void run() {
                application.drawEdge(edge);
            }
        });
    }

    public synchronized void AddEdge(Edge edge)// voor het toevoegen van 1 edge
    {
        edgeList.add(edge);
    }

    //synchronized betekent dat hij deze methode "Locked" totdat hij klaar is, en dan mag een andere thread hier mee verder.
    public synchronized void AddEdges(List<Edge> edges)// voor het toevoegen van meerdere edges
    {
        edgeList.addAll(edges);
    }

    public void CalculatingDone() {
        Platform.runLater(new Runnable() // dit is zodat het op de gui thread wordt uitgevoert, als je dit weghaald werkt het niet.
        {
            @Override
            public void run() {
                calts.setEnd(); // stop de timestamp
                application.setTextCalc(calts.toString());
            }
        });
    }

    public JSF31KochFractalFX getApplication() {
        return application;
    }
}
