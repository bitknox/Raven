package dk.itu.raven;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.api.RavenApi;
import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.ShapfileReader;
import dk.itu.raven.io.commandline.CommandLineArgs;
import dk.itu.raven.join.RavenJoin;
import dk.itu.raven.ksquared.AbstractK2Raster;
import dk.itu.raven.util.Logger;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.matrix.Matrix;
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

        Pair<Pair<Iterable<Polygon>, ShapfileReader.ShapeFileBounds>, Matrix> data = api
                .createReaders(jct.inputVector, jct.inputRaster);

        // Build k2-raster structure
        long startBuildNano = System.nanoTime();
        AbstractK2Raster k2Raster = api.generateRasterStructure(data.second);
        long endBuildNano = System.nanoTime();
        Logger.log("Build time: " + (endBuildNano - startBuildNano) / 1000000 + "ms", Logger.LogLevel.INFO);
        Logger.log("Done Building Raster", Logger.LogLevel.INFO);
        Logger.log(k2Raster.tree.size(), Logger.LogLevel.DEBUG);

        int w = data.second.getWidth();
        int h = data.second.getHeight();
        int[] sampleSize = data.second.getSampleSize();
        data.second = null;

        

        // create a R* tree with
        RTree<String, Geometry> rtree = api.generateRTree(data.first);
        Logger.log("Done Building rtree", Logger.LogLevel.INFO);

        // construct and compute the join
        RavenJoin join = new RavenJoin(k2Raster, rtree);
        long startJoinNano = System.nanoTime();
        List<Pair<Geometry, Collection<PixelRange>>> result = join.join();
        long endJoinNano = System.nanoTime();
        Logger.log("Build time: " + (endJoinNano - startJoinNano) / 1000000 + "ms", Logger.LogLevel.INFO);

        // Visualize the result
        if (jct.outputPath != null) {
            Visualizer visual = new Visualizer(w, h);
            VisualizerOptionsBuilder builder = new VisualizerOptionsBuilder();

            builder.setOutputPath(jct.outputPath);
            builder.setUseOutput(true);

            VisualizerOptions options = builder.build();

            visual.drawResult(result, data.first.first, options);
        }

        Logger.log("Done joining", Logger.LogLevel.INFO);
    }

}