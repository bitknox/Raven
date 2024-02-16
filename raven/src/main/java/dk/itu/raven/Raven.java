package dk.itu.raven;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.CommandLineArgs;
import dk.itu.raven.io.FileRasterReader;
import dk.itu.raven.io.GeoToolsRasterReader;
import dk.itu.raven.io.ShapfileReader;
import dk.itu.raven.io.TFWFormat;
import dk.itu.raven.join.RavenJoin;
import dk.itu.raven.ksquared.AbstractK2Raster;
import dk.itu.raven.ksquared.K2Raster;
import dk.itu.raven.ksquared.K2RasterBuilder;
import dk.itu.raven.ksquared.K2RasterIntBuilder;
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

        Logger.setDebug(jct.verbose);

        // Read geo raster file
        FileRasterReader rasterReader = new GeoToolsRasterReader(new File(jct.inputRaster));

        // get getTiff transform (used to transform from (lat, lon) to pixel coordinates
        // in shapefileReader)
        TFWFormat format = rasterReader.getTransform();

        // create a R* tree with
        RTree<String, Geometry> rtree = RTree.star().maxChildren(6).create();

        ShapfileReader featureReader = new ShapfileReader(format);

        // load geometries from shapefile
        Pair<Iterable<Polygon>, ShapfileReader.ShapeFileBounds> geometries = featureReader
                .readShapefile(jct.inputVector);

        // rectangle representing the bounds of the shapefile data
        Rectangle rect = Geometries.rectangle(geometries.second.minX, geometries.second.minY,
                geometries.second.maxX,
                geometries.second.maxY);

        Matrix rasterData = rasterReader.readRasters(rect);

        // offset geometries such that they are aligned to the corner
        double offsetX = geometries.second.minX > 0 ? -geometries.second.minX : 0;
        double offsetY = geometries.second.minY > 0 ? -geometries.second.minY : 0;
        for (Polygon geom : geometries.first) {
            geom.offset(offsetX, offsetY);

            rtree = rtree.add(null, geom);
        }

        // Build k2-raster structure
        long startBuildNano = System.nanoTime();
        AbstractK2Raster k2Raster;
        if (rasterData.getBitsUsed() > 32) {
            k2Raster = new K2RasterBuilder().build(rasterData,2);
        } else {
            k2Raster = new K2RasterIntBuilder().build(rasterData,2);
        }
        long endBuildNano = System.nanoTime();
        Logger.log("Build time: " + (endBuildNano - startBuildNano) / 1000000 + "ms");

        Logger.log("Done Building Raster");
        Logger.log(k2Raster.tree.size());

        Logger.log("Done Building rtree");

        // construct and compute the join
        RavenJoin join = new RavenJoin(k2Raster, rtree);
        long startJoinNano = System.nanoTime();
        List<Pair<Geometry, Collection<PixelRange>>> result = join.join(jct.minValue, jct.maxValue);
        long endJoinNano = System.nanoTime();
        System.out.println("Build time: " + (endJoinNano - startJoinNano) / 1000000 + "ms");

        // Visualize the result
        if (jct.outputPath != null) {
            Visualizer visual = new Visualizer(rasterData.getWidth(), rasterData.getHeight());
            VisualizerOptionsBuilder builder = new VisualizerOptionsBuilder();

            builder.setOutputPath(jct.outputPath);
            builder.setUseOutput(true);

            VisualizerOptions options = builder.build();

            visual.drawResult(result, geometries.first, options);
        }

        Logger.log("Done joining");
    }
}