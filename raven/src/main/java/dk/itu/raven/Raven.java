package dk.itu.raven;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.beust.jcommander.JCommander;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.api.RavenApi;
import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.FileRasterReader;
import dk.itu.raven.io.ImageMetadata;
import dk.itu.raven.io.ShapfileReader;
import dk.itu.raven.io.commandline.CommandLineArgs;
import dk.itu.raven.join.JoinFilterFunctions;
import dk.itu.raven.join.RasterFilterFunction;
import dk.itu.raven.util.Logger;
import dk.itu.raven.util.Logger.LogLevel;
import dk.itu.raven.util.Pair;
import dk.itu.raven.visualizer.Visualizer;
import dk.itu.raven.visualizer.VisualizerOptions;
import dk.itu.raven.visualizer.VisualizerOptionsBuilder;

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

        FileRasterReader rasterReader = api.createRasterReader(jct.inputRaster);
        ShapfileReader shapefileReader = api.createShapefileReader(jct.inputVector, rasterReader.getTransform());

        ImageMetadata metadata = rasterReader.getImageMetadata();

        int totalBits = 0;
        for (int bits : metadata.getBitsPerSample()) {
            totalBits += bits;
        }

        RasterFilterFunction function = JoinFilterFunctions.acceptAll();

        if (jct.ranges.size() == 2) {
            if (metadata.getBitsPerSample().length > 1) {
                Logger.log("WARNING: only one range was given, but more than one raster sample exists ("
                        + metadata.getBitsPerSample().length + ")", LogLevel.WARNING);
            }
            long lo = jct.ranges.get(0);
            long hi = jct.ranges.get(1);
            function = JoinFilterFunctions.rangeFilter(lo, hi);
        } else if (jct.ranges.size() == metadata.getBitsPerSample().length * 2) {
            Logger.log("using multiSampleRangeFilter", LogLevel.DEBUG);
            function = JoinFilterFunctions.multiSampleRangeFilter(jct.ranges, metadata.getBitsPerSample(),
                    totalBits);
        } else if (jct.ranges.size() == 0) {
            function = JoinFilterFunctions.rangeFilter(Integer.MIN_VALUE, Integer.MAX_VALUE);
        } else {
            throw new IllegalArgumentException(
                    "The number of provided search ranges does not match the number of raster samples");
        }

        Pair<Pair<Iterable<Polygon>, ShapfileReader.ShapeFileBounds>, Stream<SpatialDataChunk>> data = api
                .streamData(shapefileReader, rasterReader, jct.tileSize, jct.tileSize);

        long startJoinNano = System.nanoTime();
        List<Pair<Geometry, Collection<PixelRange>>> results = api
                .join(api.streamStructures(data.first, data.second), function).parallel()
                .collect(ArrayList::new, (l, x) -> l.addAll(x), (l, r) -> l.addAll(r));
        long endJoinNano = System.nanoTime();

        Logger.log("Streamed Join time: " + (endJoinNano - startJoinNano) / 1000000 + "ms", LogLevel.INFO);

        // Visualize the result
        if (jct.outputPath != null) {

            Visualizer visual = new Visualizer(metadata.getWidth(), metadata.getHeight());
            VisualizerOptionsBuilder builder = new VisualizerOptionsBuilder();

            builder.setOutputPath(jct.outputPath);
            builder.setUseOutput(true);

            VisualizerOptions options = builder.build();

            visual.drawResult(results, data.first.first, options);
        }

        Logger.log("Done joining", LogLevel.INFO);
    }

}