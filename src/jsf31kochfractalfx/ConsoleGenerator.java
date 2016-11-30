package jsf31kochfractalfx;

import calculate.Edge;
import calculate.KochFractal;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import timeutil.TimeStamp;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
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

        TimeStamp timeStamp = new TimeStamp();
        timeStamp.setBegin("Generate start");

        fractal.generateLeftEdge();
        fractal.generateBottomEdge();
        fractal.generateRightEdge();

        timeStamp.setEnd("Generate end");

        System.out.println("Generate " + timeStamp.toString());

        timeStamp.init();





        //Binary
        try (ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream())
        {
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream))
            {
                objectOutputStream.writeInt(level);

                for (Edge edge : edges)
                {
                    objectOutputStream.writeObject(edge);
                }
            }

            timeStamp.setBegin("Writing start");
            File f = new File(filename);
            f.delete();

            FileChannel fc = new RandomAccessFile(f, "rw").getChannel();

            byte[] mymem = byteOutputStream.toByteArray(); // in bytes
            long buffer = 8 * mymem.length; // buffer size
            System.out.println("Buffer bytes : " + buffer);
            MappedByteBuffer mem = fc.map(FileChannel.MapMode.READ_WRITE, 0, buffer);

            mem.put(mymem);

            fc.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        timeStamp.setEnd("Writing end");

        System.out.println("Writing " + timeStamp.toString());

        timeStamp.init();

        timeStamp.setBegin("Serializing JSON start");

        Gson gson = new Gson();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("level", level);
        jsonObject.add("edges", gson.toJsonTree(edges));
        String json = gson.toJson(jsonObject);

        timeStamp.setEnd("Serializing JSON end");

        System.out.println("Serializing JSON " + timeStamp.toString());

        timeStamp.init();

        timeStamp.setBegin("Writing JSON start");

        try (FileOutputStream outputStream = new FileOutputStream(filename + ".json")) {
            try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {
                try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(bufferedOutputStream)) {
                    outputStreamWriter.write(json);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        timeStamp.setEnd("Writing JSON end");

        System.out.println("Writing JSON " + timeStamp.toString());
    }

    public static void main(String[] args) {
        new ConsoleGenerator();
    }

    @Override
    public void update(Observable o, Object arg) {
        edges.add(((Edge) arg));
    }
}
