/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calculate;

import Threads.ReadDrawEdge;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import jsf31kochfractalfx.ConsoleGenerator;
import jsf31kochfractalfx.JSF31KochFractalFX;
import timeutil.TimeStamp;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.*;

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
        int edges = 0;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("File");
        File file = fileChooser.showOpenDialog(application.getStage());

        //

        String fileName = file.getName();
        String fileNameWithoutTmp = fileName.substring(0, fileName.lastIndexOf(ConsoleGenerator.TMP_POSTFIX));
        if (fileName.endsWith(".tmp")) {
            boolean fileAvailable = false;

            try {
                Path path = file.toPath().getParent();
                WatchService watchService = FileSystems.getDefault().newWatchService();
                path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);

                do {

                    try {
                        WatchKey key = watchService.take();

                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind kind = event.kind();

                            if (kind == OVERFLOW) {
                                continue;
                            }

                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path name = ev.context();


                            System.out.println("Comparing " + name.toString() + " to " + fileNameWithoutTmp);
                            if (name.toString().equals(fileNameWithoutTmp)) {
                                fileAvailable = true;
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (!fileAvailable);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        application.clearKochPanel();

        ReadDrawEdge future = new ReadDrawEdge(new File(fileNameWithoutTmp), koch, this);

        ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(1);
        threadPoolExecutor.submit(future);

        threadPoolExecutor.shutdown();

        /*try {
            edgeList = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }*/
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
