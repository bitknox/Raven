package dk.itu.raven.runner;

import java.io.IOException;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.google.gson.Gson;

import dk.itu.raven.join.JoinFilterFunctions;
import dk.itu.raven.ksquared.AbstractK2Raster;
import dk.itu.raven.util.Pair;
import dk.itu.raven.api.RavenApi;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws IOException {
        RavenApi api = new RavenApi();
        String vectorPath = args[0];
        String rasterPath = args[1];
        int numIterations = Integer.parseInt(args[2]);
        long[] times = new long[numIterations];
        BenchResult benchResult = new BenchResult("raven");
        for (int i = 0; i < numIterations; i++) {
            Pair<AbstractK2Raster, RTree<String, Geometry>> structures = api.buildStructures(vectorPath, rasterPath);
            long start = System.currentTimeMillis();
            api.join(structures.first, structures.second, JoinFilterFunctions.acceptAll());
            long end = System.currentTimeMillis();
            times[i] = end - start;
            benchResult.addEntry(times[i]);
        }

        Gson gson = new Gson();
        System.out.println(gson.toJson(benchResult));

    }

}
