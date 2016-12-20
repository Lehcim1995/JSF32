package Protocols;

import calculate.Edge;
import calculate.KochFractal;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by michel on 20-12-2016.
 */
public class ServerProtocol implements ProtcolNames, Observer
{
    @Override
    public void update(java.util.Observable o, Object arg)
    {
        Edge e = (Edge) arg;
        try {
            edgeQueue.put(e);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    public enum State
    {
        WAITING,
        WAITFORLEVEL,
        WAITNGMODE,
        SENDING_All_EDGES,
        SENDINGEDGE,
        IMDONE
    }

    private State state = State.WAITING;
    private boolean SendAll;
    private int level;
    private KochFractal fracttal;
    private BlockingQueue<Edge> edgeQueue = new LinkedBlockingQueue<>();

    private ExecutorService threadPool = Executors.newFixedThreadPool(3);

    public String processInput(String theInput, OutputStream stream) {
        String theOutput = null;

        switch (state)
        {
            case WAITING:
                theOutput = SERVERWAITNG;
                state = State.WAITFORLEVEL;
                break;
            case WAITFORLEVEL:
                try
                {
                    level = Integer.parseInt(theInput);
                    if (fracttal == null)
                    {
                        fracttal = new KochFractal();
                        fracttal.addObserver(this);
                    }
                    fracttal.setLevel(level);
                }
                catch (NumberFormatException NFE)
                {
                    theOutput = WRONGNUMBER;
                    break;
                }

                theOutput = MODETYPEASKING;
                state = State.WAITNGMODE;
                break;
            case WAITNGMODE:
                SendAll = (theInput.equals(CONFORMATION));

                theOutput = STARTSENDING;

                if (SendAll)
                {
                    state = State.SENDING_All_EDGES;
                    break;
                }
                state = State.SENDINGEDGE;

                break;
            case SENDING_All_EDGES:

                fracttal.generateBottomEdge();
                fracttal.generateLeftEdge();
                fracttal.generateRightEdge();

                try {
                    ObjectOutputStream objstream = new ObjectOutputStream(stream);
                    objstream.writeInt(fracttal.getNrOfEdges());
                    for (Edge e : edgeQueue)
                    {
                        objstream.writeObject(e);
                    }
                    objstream.flush();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                theOutput = SENDINGDONE;
                state = State.IMDONE;

                break;
            case SENDINGEDGE:


                threadPool.submit(() -> {
                    KochFractal kochFractal = new KochFractal();
                    kochFractal.setLevel(level);
                    kochFractal.addObserver(ServerProtocol.this);
                    kochFractal.generateBottomEdge();
                });
                threadPool.submit(() -> {
                    KochFractal kochFractal = new KochFractal();
                    kochFractal.setLevel(level);
                    kochFractal.addObserver(ServerProtocol.this);
                    kochFractal.generateRightEdge();
                });
                threadPool.submit(() -> {
                    KochFractal kochFractal = new KochFractal();
                    kochFractal.setLevel(level);
                    kochFractal.addObserver(ServerProtocol.this);
                    kochFractal.generateLeftEdge();
                });

                try {
                    ObjectOutputStream objstream = new ObjectOutputStream(stream);
                    objstream.writeInt(fracttal.getNrOfEdges());
                    System.out.println("Edges: " + fracttal.getNrOfEdges());
                    for (int i = 0; i < fracttal.getNrOfEdges(); i++) {
                        Edge e = edgeQueue.take();
                        objstream.writeObject(e);
                    }
                    objstream.flush();
                }
                catch (IOException | InterruptedException e)
                {
                    e.printStackTrace();
                }

                theOutput = SENDINGDONE;
                state = State.IMDONE;

                break;
            case IMDONE:

                state = State.WAITING;
                break;
        }

        return theOutput;
    }
}
