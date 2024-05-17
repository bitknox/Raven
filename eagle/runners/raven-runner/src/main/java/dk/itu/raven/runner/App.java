package dk.itu.raven.runner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.geotools.util.logging.Logging;

import com.beust.jcommander.JCommander;
import com.google.gson.Gson;

import dk.itu.raven.api.RavenApi;
import dk.itu.raven.io.IRasterReader;
import dk.itu.raven.io.ImageMetadata;
import dk.itu.raven.io.MultiFileRasterReader;
import dk.itu.raven.io.cache.CacheOptions;
import dk.itu.raven.join.AbstractRavenJoin;
import dk.itu.raven.join.JoinFilterFunctions;

import org.apache.commons.io.FileUtils;

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
            join = switch (jct.joinType) {
                case STREAMED ->
                    api.getStreamedJoin(jct.inputRaster, jct.inputVector, jct.tileSize, jct.tileSize, false,
                    cacheOptions, jct.kSize, jct.rTreeMinChildren, jct.rTreeMaxChildren, jct.resultType);
                case PARALLEL ->
                    api.getStreamedJoin(jct.inputRaster, jct.inputVector, jct.tileSize, jct.tileSize, true,
                    cacheOptions, jct.kSize, jct.rTreeMinChildren, jct.rTreeMaxChildren, jct.resultType);
                default ->
                    api.getJoin(jct.inputRaster, jct.inputVector, cacheOptions, jct.kSize, jct.rTreeMinChildren,
                    jct.rTreeMaxChildren, jct.resultType);
            };
            if (jct.filterLow == null && jct.filterHigh == null) {
                // join.join(JoinFilterFunctions.acceptAll()).count();
                join.count();
            } else if (jct.filterLow.size() == 1 && jct.filterHigh.size() == 1) {
                // join.join(JoinFilterFunctions.rangeFilter(jct.filterLow.get(0), jct.filterHigh.get(0))).count();
                join.count();
            } else {
                IRasterReader rasterReader = new MultiFileRasterReader(new File(jct.inputRaster));
                ImageMetadata metadata = rasterReader.getImageMetadata();
                List<Long> ranges = new ArrayList<>();
                if (jct.filterLow.size() != jct.filterHigh.size()) {
                    throw new IllegalArgumentException("length of filter low list is different from length of filter max list");
                }
                for (int j = 0; j < jct.filterLow.size(); j++) {
                    ranges.add(jct.filterLow.get(j));
                    ranges.add(jct.filterHigh.get(j));
                }
                join.join(JoinFilterFunctions.multiSampleRangeFilter(ranges, metadata.getBitsPerSample(), metadata.getTotalBitsPerPixel())).count();
            }
            long end = System.currentTimeMillis();
            long time = end - start;
            System.err.println("    Iteration " + (i + 1) + " took " + time + "ms.");
            benchResult.addEntry(time);
            FileUtils.deleteDirectory(new File(jct.cacheDir));
        }

        Gson gson = new Gson();
        System.out.println(gson.toJson(benchResult));

    }

}
