package dk.itu.raven.runner;

import java.io.IOException;

import com.google.gson.Gson;

import dk.itu.raven.runner.BenchResult;

//import com.github.bitknox.Raven;
import dk.itu.raven.api.RavenApi;
import dk.itu.raven.join.AbstractRavenJoin;

/**
 * Bootstrap raven and run the benchmark
 */
public class App {
    public static void main(String[] args) throws IOException {
        RavenApi api = new RavenApi();
        String vectorPath = args[0];
        String rasterPath = args[1];
        String type = args[2];
        int partitionSize = Integer.parseInt(args[3]);
        int numIterations = Integer.parseInt(args[4]);

        BenchResult benchResult = new BenchResult("Raven Benchmark");
        benchResult.addLabel("Vector: "+ benchResult.formatPath(vectorPath));
        benchResult.addLabel("Raster: "+ benchResult.formatPath(rasterPath));
        benchResult.addLabel("Type: "+ type);
        benchResult.addLabel("Partition Size: "+ partitionSize);
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
            long time = end - start;
            benchResult.addEntry(time);
        }

        Gson gson = new Gson();
        System.out.println(gson.toJson(benchResult));

    }

}
