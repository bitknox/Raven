package dk.itu.raven;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.geotools.util.logging.Logging;

import com.beust.jcommander.JCommander;

import dk.itu.raven.api.RavenApi;
import dk.itu.raven.io.IRasterReader;
import dk.itu.raven.io.ImageMetadata;
import dk.itu.raven.io.MultiFileRasterReader;
import dk.itu.raven.io.ShapefileReader;
import dk.itu.raven.io.cache.CacheOptions;
import dk.itu.raven.io.commandline.CommandLineArgs;
import dk.itu.raven.io.commandline.ResultType;
import dk.itu.raven.join.AbstractRavenJoin;
import dk.itu.raven.join.IRasterFilterFunction;
import dk.itu.raven.join.JoinFilterFunctions;
import dk.itu.raven.join.results.IJoinResult;
import dk.itu.raven.util.Logger;
import dk.itu.raven.util.Logger.LogLevel;
import dk.itu.raven.visualizer.RandomColor;
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
        JCommander commander = JCommander.newBuilder().addObject(jct).build();
        commander.parse(args);
        commander.setProgramName("Raven");
        if (jct.help) {
            commander.usage();
            return;
        }
        // Set geotools logging to severe to avoid spamming the console
        Logging.getLogger("org.geotools").setLevel(Level.SEVERE);
        Logger.setLogLevel(jct.verbose);

        RavenApi api = new RavenApi();
        IRasterReader rasterReader = new MultiFileRasterReader(new File(jct.inputRaster));
        ImageMetadata metadata = rasterReader.getImageMetadata();
        ShapefileReader shapefileReader = api.createShapefileReader(jct.inputVector);
        CacheOptions cacheOptions = new CacheOptions(jct.cacheDir, jct.isCaching);

        // Set the fraction size for the DAC
        api.setDACFraction(jct.dacFractionSize);

        IRasterFilterFunction function = JoinFilterFunctions.acceptAll();

        if (jct.resultType.equals(ResultType.NONE)) {
            Logger.log("WARNING: The \"None\" result type is only meant to be used for testing.", LogLevel.WARNING);
        }

        if (jct.ranges.size() == 2) {
            if (metadata.getSamplesPerPixel() > 1) {
                Logger.log("WARNING: only one range was given, but more than one raster sample exists ("
                        + metadata.getSamplesPerPixel() + ")", LogLevel.WARNING);
            }
            long lo = jct.ranges.get(0);
            long hi = jct.ranges.get(1);
            function = JoinFilterFunctions.rangeFilter(lo, hi);
        } else if (jct.ranges.size() == metadata.getSamplesPerPixel() * 2) {
            Logger.log("using multiSampleRangeFilter", LogLevel.DEBUG);
            function = JoinFilterFunctions.multiSampleRangeFilter(jct.ranges,
                    metadata.getBitsPerSample(),
                    metadata.getTotalBitsPerPixel());
        } else if (jct.ranges.size() == 0) {
            function = JoinFilterFunctions.rangeFilter(Integer.MIN_VALUE,
                    Integer.MAX_VALUE);
        } else {
            throw new IllegalArgumentException(
                    "The number of provided search ranges does not match the number of raster samples");
        }

        long startJoinNano = System.nanoTime();
        AbstractRavenJoin join;
        if (jct.streamed) {
            join = api.getStreamedJoin(jct.inputRaster, jct.inputVector, jct.tileSize,
                    jct.tileSize, jct.parallel,
                    cacheOptions, jct.kSize, jct.rTreeMinChildren,
                    jct.rTreeMaxChildren, jct.resultType);
        } else {
            join = api.getJoin(jct.inputRaster, jct.inputVector, cacheOptions,
                    jct.kSize, jct.rTreeMinChildren, jct.rTreeMaxChildren, jct.resultType);
        }
        IJoinResult result = join.join(function);

        if (jct.outputPath != null) {
            result = result.asMemoryAllocatedResult(); // this allows the visualizer to draw the result while still
            // allowing us to consume the stream and time the join
        } else {
            result.count(); // count will still force the stream to be executed, so the timing of
            // the function will work
        }

        long endJoinNano = System.nanoTime();
        Logger.log("Join time: " + (endJoinNano - startJoinNano) / 1000000 + "ms",
                LogLevel.INFO);
        Logger.log("Done joining", LogLevel.INFO);

        // Visualize the result
        if (jct.outputPath != null) {
            // FIXME: This is a hack to get the visualizer to work with multifile raster
            Visualizer visual = new Visualizer(metadata.getWidth(), metadata.getHeight());
            VisualizerOptionsBuilder builder = new VisualizerOptionsBuilder();

            builder.setOutputPath(jct.outputPath);
            builder.setOutputFormat(jct.outputExtension);
            builder.setUseOutput(true);
            builder.setCropToVector(jct.cropToVector);
            builder.setPrimaryColor(new RandomColor());
            builder.setDrawFeatures(true);
            builder.setSecondaryColor(Color.BLACK);
            builder.setUseOriginalColours(true);

            VisualizerOptions options = builder.build();

            visual.drawResult(result, shapefileReader, options);
            Logger.log("Done visualizing", LogLevel.INFO);
        }
    }

}
