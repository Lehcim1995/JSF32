package Threads;

import calculate.Edge;
import calculate.KochFractal;
import calculate.KochManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import timeutil.TimeStamp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReadDrawEdge extends Task<List<Edge>> {

    private File file;
    private KochFractal koch;
    private KochManager kochManager;

    private List<Edge> edgeList = new ArrayList<>();
    private int edges = 0;

    public ReadDrawEdge(File file, KochFractal koch, KochManager kochManager) {
        this.file = file;
        this.koch = koch;
        this.kochManager = kochManager;
    }

    @Override
    protected List<Edge> call() throws Exception {
        System.out.println("Read Start " + koch.getLevel());
        timeutil.TimeStamp ts2 = new TimeStamp();
        ts2.setBegin();

        ExecutorService executorService = Executors.newFixedThreadPool(8);

        try (FileInputStream inputStream = new FileInputStream(file)) {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
                koch.setLevel(objectInputStream.readInt());

                edges = koch.getNrOfEdges();
                edgeList.clear();

                for (int i = 0; i < edges; i++) {
                    Edge e = (Edge) objectInputStream.readObject();
                    edgeList.add(e);

                    Edge drawEdge = new Edge(e.X1, e.Y1, e.X2, e.Y2, Color.BEIGE);

                    executorService.submit(new EdgeDrawer(drawEdge));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("Read End " + koch.getLevel());
        ts2.setEnd();

        executorService.submit(() -> {
            kochManager.AddEdges(edgeList);

            Platform.runLater(() -> {
                kochManager.getApplication().setTextCalc(ts2.toString());

                kochManager.getApplication().setTextNrEdges(edges + "");
                kochManager.getApplication().setTextLevel("Level: " + koch.getLevel());
                kochManager.getApplication().requestDrawEdges();
            });
        });

        executorService.shutdown();

        return edgeList;
    }

    private class EdgeDrawer implements Runnable {

        private Edge edge;

        public EdgeDrawer(Edge edge) {
            this.edge = edge;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(0,1);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            Platform.runLater(() -> {
                kochManager.DrawEdge(edge);
            });
        }
    }
}
