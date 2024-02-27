package dk.itu.raven.runner;

import java.io.IOException;

import com.google.gson.Gson;

//import com.github.bitknox.Raven;
import dk.itu.raven.api.RavenApi;
import dk.itu.raven.join.AbstractRavenJoin;

/**
 * Bootstrap raven and run the benchmark
 */
public class App {
    public static void main(String[] args) throws IOException {
        RavenApi api = new RavenApi();
        String name = args[0];
        String vectorPath = args[1];
        String rasterPath = args[2];
        int numIterations = Integer.parseInt(args[3]);
        String type = args[4];
        int partitionSize = Integer.parseInt(args[5]);
        long[] times = new long[numIterations];
        BenchResult benchResult = new BenchResult(name);
        for (int i = 0; i < numIterations; i++) {
            long start = System.currentTimeMillis();
            AbstractRavenJoin join;
            if (type.equals("stream")) {
                join = api.getStreamedJoin(rasterPath, vectorPath, partitionSize, partitionSize, false);
            } else if (type.equals("parallel")) {
                join = api.getStreamedJoin(rasterPath, vectorPath, partitionSize, partitionSize, true);
            } else {
                join = api.getJoin(rasterPath, vectorPath);
            }
            join.join().count();
            long end = System.currentTimeMillis();
            times[i] = end - start;
            benchResult.addEntry(times[i]);
        }

        Gson gson = new Gson();
        System.out.println(gson.toJson(benchResult));

    }

}
