/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calculate;

import Threads.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import jsf31kochfractalfx.JSF31KochFractalFX;
import timeutil.TimeStamp;

/**
 *
 * @author michel
 */
public class KochManager
{

    private KochFractal koch; //De fractal, niet echt meer nodig in dit geval maar ik laat het er toch in.
    private ArrayList<Edge> edgeList; // De gezamelijke lijst van edges voor de fractal
    private JSF31KochFractalFX application; // de  applicatie zelf

    private timeutil.TimeStamp calts; // timestap voor het berekenen van de fractal
    private CyclicBarrier bar;
    private ExecutorService pool;

    private int count = 0; // voor het bijhouden van hoeveel threads er klaar zijn
    
    private final int SIDES = 3;
    private final String LEVELSTRING = "------------LEVEL ";
    private final String LEVELSTART = " START-------------";
    private final String LEVELEND = " DONE--------------";

    private Task bottom;
    private Task left;
    private Task right;


    

    public KochManager(JSF31KochFractalFX application)
    {
        this.application = application;
        koch = new KochFractal();
        edgeList = new ArrayList<>();
    }

    public synchronized void changeLevel(int currentLevel)
    {
        if (bottom != null || left != null || right != null)
        {
            bottom.cancel();
            left.cancel();
            right.cancel();
            application.UnbindAll();
        }

        calts = new TimeStamp();  //zet timestamp voor bereken fractals
        calts.setBegin(); //start de timestamp
        koch.setLevel(currentLevel); // zet level van de fractal
        edgeList.clear(); //clear list
        System.out.println(LEVELSTRING + koch.getLevel() + LEVELSTART);
        
        pool = Executors.newFixedThreadPool(SIDES);//Only 3 threads
        bar = new CyclicBarrier(SIDES); //Set en barrier die de treads stillhoud totdat er een bepaald aantal keer de await is aangeroepen

        bottom = new GenerateEdge(koch.getLevel(), this, GenerateEdge.BOTTOM); // maak meerdere callables aan, dit zijn eidegenlijk gewoon de threads
        left = new GenerateEdge(koch.getLevel(), this, GenerateEdge.LEFT);
        right = new GenerateEdge(koch.getLevel(), this, GenerateEdge.RIGHT);

        application.SetBind(bottom, GenerateEdge.BOTTOM);
        application.SetBind(left, GenerateEdge.LEFT);
        application.SetBind(right, GenerateEdge.RIGHT);

        pool.submit(bottom); //voeg de Callables toe aan de threadpool
        pool.submit(left);
        pool.submit(right);

        pool.shutdown(); // de pool afsluiten want de pool hoeft maar 3 tasks te doen

        application.setTextNrEdges(koch.getNrOfEdges() + ""); // en zet de  edges goed, dit kan ik verplaatsen tot nadat de fractal klaar is met berekenen maar het werkt hiero ook
    }

    public CyclicBarrier getBar()
    {
        return bar;
    }
    
    public void RequestDraw()
    {
        application.requestDrawEdges();
    }
    
    public synchronized void drawEdges() // tekent alle edges in de lijst
    {
        System.out.println("Draw Start " + koch.getLevel());
        timeutil.TimeStamp ts2 = new TimeStamp();
        ts2.setBegin();
        application.clearKochPanel();

        for (Edge edge : edgeList)
        {
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
            public void run()
            {
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

    public void CalculatingDone()
    {
        Platform.runLater(new Runnable() // dit is zodat het op de gui thread wordt uitgevoert, als je dit weghaald werkt het niet.
        {
            @Override
            public void run()
            {
                calts.setEnd(); // stop de timestamp
                application.setTextCalc(calts.toString());
            }
        });
    }
}
