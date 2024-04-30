package dk.itu.raven.runner;

import java.io.IOException;
import java.util.logging.Level;

import com.beust.jcommander.JCommander;
import com.google.gson.Gson;
import org.geotools.util.logging.Logging;

//import com.github.bitknox.Raven;
import dk.itu.raven.api.RavenApi;
import dk.itu.raven.io.cache.CacheOptions;
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
        commander.setProgramName("Raven Runner");
        if (jct.help) {
            commander.usage();
            return;
        }

        api.setDACFraction(jct.dacFractionSize);

        CacheOptions cacheOptions = new CacheOptions(jct.cacheDir, jct.cached);

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
            System.err.println("Running iteration " + (i + 1) + " of " + jct.iterations + " iterations.");
            long start = System.currentTimeMillis();
            AbstractRavenJoin join;
            if (jct.joinType.equals(JoinType.STREAMED)) {
                join = api.getStreamedJoin(jct.inputRaster, jct.inputVector, jct.tileSize, jct.tileSize, false,
                        cacheOptions, jct.kSize, jct.rTreeMinChildren, jct.rTreeMaxChildren, jct.resultType);
            } else if (jct.joinType.equals(JoinType.PARALLEL)) {
                join = api.getStreamedJoin(jct.inputRaster, jct.inputVector, jct.tileSize, jct.tileSize, true,
                        cacheOptions, jct.kSize, jct.rTreeMinChildren, jct.rTreeMaxChildren, jct.resultType);
            } else {
                join = api.getJoin(jct.inputRaster, jct.inputVector, cacheOptions, jct.kSize, jct.rTreeMinChildren,
                        jct.rTreeMaxChildren, jct.resultType);
            }
            if (jct.filterLow == Integer.MIN_VALUE && jct.filterHigh == Integer.MAX_VALUE) {
                join.join(JoinFilterFunctions.acceptAll()).count();
            } else {
                join.join(JoinFilterFunctions.rangeFilter(jct.filterLow, jct.filterHigh)).count();
            }
            long end = System.currentTimeMillis();
            long time = end - start;
            System.err.println("    Iteration " + (i + 1) + " took " + time + "ms.");
            benchResult.addEntry(time);
        }

        Gson gson = new Gson();
        System.out.println(gson.toJson(benchResult));

    }

}
