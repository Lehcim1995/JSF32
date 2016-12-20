package Protocols;

import calculate.Edge;
import calculate.KochManager;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * Created by michel on 20-12-2016.
 */
public class ClientProtocol implements ProtcolNames
{
    KochManager kochManager;
    private State state = State.WAITING;
    private boolean recivingmode = true;
    public ClientProtocol(KochManager kochManager)
    {
        this.kochManager = kochManager;
    }

    public String processInput(String theInput, InputStream inputStream)
    {

        String theOutput = null;

        switch (state)
        {
            case WAITING:
                if (theInput.equals(SERVERWAITNG))
                {
                    theOutput = 8 + "";
                    state = State.WAITFORLEVELCONFORMATION;
                    break;
                }

                theOutput = "huh?";
                break;
            case WAITFORLEVELCONFORMATION:
                if (theInput.equals(MODETYPEASKING))
                {
                    theOutput = CONFORMATION;
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

                try (ObjectInputStream strm = new ObjectInputStream(inputStream))
                {
                    while (true)
                    {
                        Edge e = (Edge) strm.readObject();
                        kochManager.AddEdge(e);
                        kochManager.DrawEdge(e);
                    }
                }
                catch (EOFException eofex)
                {

                }
                catch (IOException | ClassNotFoundException e)
                {
                    e.printStackTrace();
                }


                kochManager.drawEdges();

                break;
            case DONE_RECEIVING:
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
        DONE_RECEIVING
    }
}
