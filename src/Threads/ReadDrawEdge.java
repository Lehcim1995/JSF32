package Threads;

import calculate.Edge;
import calculate.KochFractal;
import calculate.KochManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import timeutil.TimeStamp;

import java.io.*;
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

        if (file.getName().endsWith(".json")) {
            readJson(file.getAbsolutePath(), executorService);
        } else {
            readBinary(file.getAbsolutePath(), executorService);
        }

        System.out.println("Read End " + koch.getLevel());
        ts2.setEnd();
        System.out.println(ts2.toString());

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

    private void readJson(String filename, ExecutorService executorService) {
        Gson gson = new Gson();
        String json = "";
        JsonObject jsonObject = null;

        TimeStamp ts = new TimeStamp();
        ts.setBegin();

        try (FileReader fileReader = new FileReader(filename)) {
            try (BufferedReader reader = new BufferedReader(fileReader)) {
                StringBuilder stringBuilder = new StringBuilder();
                String line = null;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                json = stringBuilder.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ts.setEnd();

        System.out.println("JSON read " + ts.toString());

        ts.init();
        ts.setBegin();

        jsonObject = gson.fromJson(json, JsonObject.class);

        koch.setLevel(jsonObject.get("level").getAsInt());

        edges = koch.getNrOfEdges();

        edgeList.clear();


        JsonArray jsonArray = jsonObject.getAsJsonArray("edges");
        jsonArray.forEach(jsonElement -> {
            Edge e = gson.fromJson(jsonElement, Edge.class);
            edgeList.add(e);
            Edge drawEdge = new Edge(e.X1, e.Y1, e.X2, e.Y2, Color.BEIGE);

            //executorService.submit(new EdgeDrawer(drawEdge));

        });

        ts.setEnd();

        System.out.println("JSON deserialize " + ts.toString());
    }

    private void readBinary(String filename, ExecutorService executorService) {
        try (FileInputStream inputStream = new FileInputStream(filename)) {
            try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
                try (ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream)) {
                    koch.setLevel(objectInputStream.readInt());

                    edges = koch.getNrOfEdges();
                    edgeList.clear();

                    for (int i = 0; i < edges; i++) {
                        Edge e = (Edge) objectInputStream.readObject();
                        edgeList.add(e);

                        Edge drawEdge = new Edge(e.X1, e.Y1, e.X2, e.Y2, Color.BEIGE);

                        executorService.submit(new EdgeDrawer(drawEdge));
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class EdgeDrawer implements Runnable {

        private Edge edge;

        public EdgeDrawer(Edge edge) {
            this.edge = edge;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(0, 1);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            Platform.runLater(() -> {
                kochManager.DrawEdge(edge);
            });
        }
    }
}
