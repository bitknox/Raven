package dk.itu.raven.runner;

import java.io.IOException;
import java.util.logging.Level;

import com.beust.jcommander.JCommander;
import com.google.gson.Gson;
import org.geotools.util.logging.Logging;

//import com.github.bitknox.Raven;
import dk.itu.raven.api.RavenApi;
import dk.itu.raven.join.AbstractRavenJoin;
import dk.itu.raven.join.JoinFilterFunctions;

/**
 * Bootstrap raven and run the benchmark
 */
public class App {
    public static void main(String[] args) throws IOException {
        RavenApi api = new RavenApi();
        CommandLineArgs jct = new CommandLineArgs();
        JCommander commander = JCommander.newBuilder()
                .addObject(jct)
                .build();
        commander.parse(args);

        // Suppress geotools logging
        Logging.getLogger("org.geotools").setLevel(Level.SEVERE);

        BenchResult benchResult = new BenchResult("Raven Benchmark");
        benchResult.addLabel("Vector: " + benchResult.formatPath(jct.inputVector));
        benchResult.addLabel("Raster: " + benchResult.formatPath(jct.inputRaster));
        benchResult.addLabel("Type: " + jct.joinType);
        if (jct.joinType != JoinType.SEQUENTIAL) {
            benchResult.addLabel("Partition Size: " + jct.tileSize);
        }
        for (int i = 0; i < jct.iterations; i++) {
            long start = System.currentTimeMillis();
            AbstractRavenJoin join;
            if (jct.joinType.equals(JoinType.STREAMED)) {
                join = api.getStreamedJoin(jct.inputRaster, jct.inputVector, jct.tileSize, jct.tileSize, false, jct.cached);
            } else if (jct.joinType.equals(JoinType.PARALLEL)) {
                join = api.getStreamedJoin(jct.inputRaster, jct.inputVector, jct.tileSize, jct.tileSize, true, jct.cached);
            } else {
                join = api.getJoin(jct.inputRaster, jct.inputVector, jct.cached);
            }
            if (jct.filterLow == Integer.MIN_VALUE && jct.filterHigh == Integer.MAX_VALUE) {
                join.join(JoinFilterFunctions.acceptAll()).count();
            } else {
                join.join(JoinFilterFunctions.rangeFilter(jct.filterLow, jct.filterHigh)).count();
            }
            long end = System.currentTimeMillis();
            long time = end - start;
            benchResult.addEntry(time);
        }

        Gson gson = new Gson();
        System.out.println(gson.toJson(benchResult));

    }

}
