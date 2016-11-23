package jsf31kochfractalfx;

import calculate.Edge;
import calculate.KochFractal;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

public class ConsoleGenerator implements Observer {

    private List<Edge> edges = new ArrayList<>();

    public ConsoleGenerator() {

        System.out.println("Level?");
        Scanner scanner = new Scanner(System.in);

        int level = scanner.nextInt();

        System.out.println("File?");

        String filename = scanner.next();

        if (filename.isEmpty()) {
            filename = "default_" + level;
        }

        KochFractal fractal = new KochFractal();
        fractal.setLevel(level);

        fractal.addObserver(this);

        System.out.println("Generating " + System.currentTimeMillis());

        fractal.generateLeftEdge();
        fractal.generateBottomEdge();
        fractal.generateRightEdge();

        System.out.println("Writing " + System.currentTimeMillis());

        try (FileOutputStream outputStream = new FileOutputStream(filename)) {
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
                objectOutputStream.writeInt(level);
                for (Edge edge : edges) {
                    objectOutputStream.writeObject(edge);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Done " + System.currentTimeMillis());
    }

    public static void main(String[] args) {
        new ConsoleGenerator();
    }

    @Override
    public void update(Observable o, Object arg) {
        edges.add(((Edge) arg));
    }
}
