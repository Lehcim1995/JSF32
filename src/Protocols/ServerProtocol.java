package Protocols;

import calculate.Edge;
import calculate.KochFractal;
import javafx.beans.Observable;
import javafx.scene.paint.Color;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

/**
 * Created by michel on 20-12-2016.
 */
public class ServerProtocol implements ProtcolNames, Observer
{
    @Override
    public void update(java.util.Observable o, Object arg)
    {
        Edge e = (Edge) arg;
        edgelist.add(e);
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
    private List<Edge> edgelist = new ArrayList<>();

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

                try (ObjectOutputStream objstream = new ObjectOutputStream(stream))
                {
                    for (Edge e : edgelist)
                    {
                        objstream.writeObject(e);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                theOutput = "ALL";
                state = State.IMDONE;

                break;
            case SENDINGEDGE:

                theOutput = "a";
                state = State.IMDONE;
                break;
            case IMDONE:

                theOutput = SENDINGDONE;
                state = State.WAITING;
                break;
        }

        return theOutput;
    }
}
