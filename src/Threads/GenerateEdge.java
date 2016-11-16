/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Threads;

import calculate.Edge;
import calculate.KochFractal;
import calculate.KochManager;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;

/**
 *
 * @author michel
 */
public class GenerateEdge extends Task<ArrayList<Edge>> implements Observer
{
    private KochFractal koch; // nieuwe fractal
    private ArrayList<Edge> edgeList; //list van edges van de onderkant
    private KochManager km; //kochmanager
    private int edges; //aantal edges die hij nodig heeft
    private boolean isDone; // of hij klaar is
    private int count; //aantal edges
    private final int side; //de zijde van de fractal
    
    public final static int BOTTOM = 0; //Mooie statics voor het aangeven van de zijde
    public final static int LEFT = 1;
    public final static int RIGHT = 2;

    public GenerateEdge(int level, KochManager km, int side)
    {
        this.koch = new KochFractal(); // maak nieuwe fractal
        koch.setLevel(level); // zet hem  op de juiste  level
        koch.addObserver(this); // voeg zichzelf toe als observer
        edgeList = new ArrayList<>(); //maak lijst
        this.km = km;
        edges = this.koch.getNrOfEdges() / 3; // zet het aantal edges op aantaledges / 3 omdat er 3 zijdes zijn en je wilt er maar 1 hebben
        count = 0; // count is 0
        this.side = side; 
    }

    @Override
    public void update(Observable o, Object o1) // de observer pattern, wordt aangeroepen wanneer hij een edge binnenkrijgt
    {

        Edge e = (Edge)o1; //voeg de edge  toe aan zijn eigen lijst
        Edge e1 = new Edge(e.X1, e.Y1, e.X2, e.Y2, Color.BEIGE );


        try
        {
            Thread.sleep( koch.getLevel() < 8 ? 1 : 0, koch.getLevel() < 8 ? 0 : 1 );
        }
        catch (InterruptedException ex)
        {
            //e2.printStackTrace();
        }

        km.DrawEdge(e1);
        edgeList.add(e);
        count++; //voeg edge toe
        updateProgress(count, edges);
        updateMessage("NR of edges : " + count);
        //km.AddEdge(e); //voeg edge toe in de kochmanager
    }

    @Override
    public ArrayList<Edge> call() throws Exception
    {
        switch(side) //selecteerd de goede zijde
        {
            case BOTTOM:
                koch.generateBottomEdge(); // start generaton
                break;
            case LEFT:
                koch.generateLeftEdge(); // start generaton
                break;
            case RIGHT:
                koch.generateRightEdge(); // start generaton
                break;
        }
        
//        while (!isDone) // wanneer hij niet klaar is of niet interrupted is kijk dan of hij klaar is
//        {
//            isDone = count == edges; // jij wilt dat hij klaar is wanneer de list evengroot is als de aantal edges die hij nodig heeft           
//        }
        
        km.AddEdges(edgeList); //Voegt edges toe aan de kochmanger lijst
        System.out.println("Thread " + side + " waiting");
        if (km.getBar().await() == 0)//Roept de cyclic barrier aan, dit blijft wachten todat hij 3 keer is aangeroepen, de await geeft 0 terug wanneer het de laatste await is, zo weet je dat hij de laatste is
        {
            km.RequestDraw(); // wanneer het de laatste is weet je dat alles klaar is en kan je gaan tekenen
            km.CalculatingDone();
        }
        
        System.out.println("Thread " + side + " done");
        return edgeList;//dit is niet echt nodig maarja
    }

    @Override
    public void cancelled()
    {
        super.cancelled();
        koch.cancel();
        System.out.println("Cancelled");
    }
}
