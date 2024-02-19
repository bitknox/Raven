package dk.itu.raven;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;

import com.beust.jcommander.JCommander;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.api.RavenApi;
import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.ShapfileReader;
import dk.itu.raven.io.commandline.CommandLineArgs;
import dk.itu.raven.join.JoinFilterFunctions;
import dk.itu.raven.join.RasterFilterFunction;
import dk.itu.raven.join.RavenJoin;
import dk.itu.raven.ksquared.AbstractK2Raster;
import dk.itu.raven.util.Logger;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.visualizer.Visualizer;
import dk.itu.raven.visualizer.VisualizerOptions;
import dk.itu.raven.visualizer.VisualizerOptionsBuilder;
import static dk.itu.raven.util.Logger.LogLevel;

/**
 * Main class for the raven application
 * 
 */
public class Raven {

    public static void main(String[] args) throws IOException {
        CommandLineArgs jct = new CommandLineArgs();
        JCommander commander = JCommander.newBuilder()
                .addObject(jct)
                .build();
        commander.parse(args);
        commander.setProgramName("Raven");
        if (jct.help) {
            commander.usage();
            return;
        }

        Logger.setLogLevel(jct.verbose);

        RavenApi api = new RavenApi();

        Pair<Pair<Iterable<Polygon>, ShapfileReader.ShapeFileBounds>, Matrix> data = api.createReaders(jct.inputVector,
                jct.inputRaster);

        // Build k2-raster structure
        long startBuildNano = System.nanoTime();
        AbstractK2Raster k2Raster = api.generateRasterStructure(data.second);
        long endBuildNano = System.nanoTime();
        Logger.log("Build time: " + (endBuildNano - startBuildNano) / 1000000 + "ms", LogLevel.INFO);
        Logger.log("Done Building Raster", LogLevel.INFO);
        Logger.log(k2Raster.tree.size(), LogLevel.DEBUG);

        int w = data.second.getWidth();
        int h = data.second.getHeight();
        int[] sampleSize = data.second.getSampleSize();
        int bitsUsed = data.second.getBitsUsed();
        data.second = null;

        RasterFilterFunction function;

        if (jct.ranges.size() == 2) {
            if (sampleSize.length > 1) {
                Logger.log("WARNING: only one range was given, but more than one raster sample exists ("
                        + sampleSize.length + ")", LogLevel.WARNING);
            }
            long lo = jct.ranges.get(0);
            long hi = jct.ranges.get(1);
            function = JoinFilterFunctions.rangeFilter(lo, hi);
        } else if (jct.ranges.size() == sampleSize.length * 2) {
            Logger.log("using multiSampleRangeFilter", LogLevel.DEBUG);
            function = JoinFilterFunctions.multiSampleRangeFilter(jct.ranges, sampleSize, bitsUsed);
        } else if (jct.ranges.size() == 0) {
            function = JoinFilterFunctions.rangeFilter(Integer.MIN_VALUE, Integer.MAX_VALUE);
        } else {
            throw new IllegalArgumentException(
                    "The number of provided search ranges does not match the number of raster samples");
        }

        // create a R* tree with
        RTree<String, Geometry> rtree = api.generateRTree(data.first);
        Logger.log("Done Building rtree", LogLevel.INFO);

        // construct and compute the join
        RavenJoin join = new RavenJoin(k2Raster, rtree);
        long startJoinNano = System.nanoTime();

        List<Pair<Geometry, Collection<PixelRange>>> results = api
                .join(api.streamBuildStructures(jct.inputVector, jct.inputRaster))
                .collect(ArrayList::new, (l, x) -> l.addAll(x), (l, r) -> l.addAll(r));

        // List<Pair<Geometry, Collection<PixelRange>>> result = join.join(function);
        long endJoinNano = System.nanoTime();
        Logger.log("Join time: " + (endJoinNano - startJoinNano) / 1000000 + "ms", LogLevel.INFO);

        // Visualize the result
        if (jct.outputPath != null) {
            Visualizer visual = new Visualizer(w, h);
            VisualizerOptionsBuilder builder = new VisualizerOptionsBuilder();

            builder.setOutputPath(jct.outputPath);
            builder.setUseOutput(true);

            VisualizerOptions options = builder.build();

            visual.drawResult(results, data.first.first, options);
        }

        Logger.log("Done joining", LogLevel.INFO);
    }

}