package Protocols;

import calculate.Edge;
import calculate.KochManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * Created by michel on 20-12-2016.
 */
public class ClientProtocol implements ProtcolNames
{
    private KochManager kochManager;
    private State state = State.WAITING;
    private int level;
    private boolean all;
    public ClientProtocol(KochManager kochManager, int level, boolean all)
    {
        this.kochManager = kochManager;
        this.level = level;
        this.all = all;
    }

    public String processInput(String theInput, InputStream inputStream)
    {

        String theOutput = null;

        switch (state)
        {
            case WAITING:
                if (theInput.equals(SERVERWAITNG))
                {
                    theOutput = String.valueOf(level);
                    state = State.WAITFORLEVELCONFORMATION;
                    break;
                }

                theOutput = "huh?";
                break;
            case WAITFORLEVELCONFORMATION:
                if (theInput.equals(MODETYPEASKING))
                {
                    theOutput = all ? CONFORMATION : NEGATIVE;
                    state = State.WAITFORMODECONFORMATION;
                    break;
                }

                theOutput = "huh?";
                state = State.WAITING;

                break;
            case WAITFORMODECONFORMATION:
                if (theInput.equals(STARTSENDING))
                {
                    theOutput = "yay";
                    state = State.RECIEVING;
                    break;
                }

                theOutput = "huh?";
                state = State.WAITING;
                break;
            case RECIEVING:

                try {
                    ObjectInputStream strm = new ObjectInputStream(inputStream);
                    int edges = strm.readInt();

                    for (int i = 0; i < edges; i++) {
                        Edge e = (Edge) strm.readObject();
                        kochManager.AddEdge(e);
                        kochManager.DrawEdge(e);
                    }
                }
                catch (IOException | ClassNotFoundException e)
                {
                    e.printStackTrace();
                }

                state = State.DONE_RECEIVING;
                break;
            case DONE_RECEIVING:
                theOutput = CLIENTDONE;

                state = State.DONE;
                break;
        }

        return theOutput;
    }

    public State getState()
    {
        return state;
    }

    public enum State
    {
        WAITING,
        WAITFORLEVELCONFORMATION,
        WAITFORMODECONFORMATION,
        RECIEVING,
        DONE_RECEIVING,
        DONE
    }
}
