package dk.itu.raptor;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.SparkConf;
import org.locationtech.jts.geom.Polygon;

import com.beust.jcommander.JCommander;

import dk.itu.raptor.io.commandline.CommandLineArgs;
import dk.itu.raptor.join.RasterMetadata;
// import dk.itu.raptor.join.RasterMetadata;
import edu.ucr.cs.bdlab.beast.common.BeastOptions;
import edu.ucr.cs.bdlab.beast.geolite.IFeature;
import edu.ucr.cs.bdlab.beast.geolite.ITile;
import edu.ucr.cs.bdlab.beast.io.shapefile.ShapefileFeatureReader;
import edu.ucr.cs.bdlab.beast.io.tiff.AbstractTiffTile;
import edu.ucr.cs.bdlab.beast.io.tiff.ITiffReader;
import edu.ucr.cs.bdlab.beast.io.tiff.TiffRaster;
import edu.ucr.cs.bdlab.raptor.GeoKeyEntry;
import edu.ucr.cs.bdlab.raptor.GeoTiffMetadata;
import edu.ucr.cs.bdlab.raptor.IRasterReader;
import edu.ucr.cs.bdlab.raptor.RaptorJoin;
import edu.ucr.cs.bdlab.raptor.RasterHelper;

public class Raptor {
    public static void main(String[] args) throws IOException {
        CommandLineArgs jct = new CommandLineArgs();
        JCommander commander = JCommander.newBuilder()
                .addObject(jct)
                .build();
        commander.parse(args);
        commander.setProgramName("Raptor");
        if (jct.help) {
            commander.usage();
            return;
        }

        Path rasterPath = new Path(new File(jct.inputRaster).getAbsolutePath());
        Path vectorPath = new Path(new File(jct.inputVector).getAbsolutePath());
        FileSystem fs = rasterPath.getParent().getFileSystem(new Configuration());

        IRasterReader<Object> reader = RasterHelper.createRasterReader(fs, rasterPath, new BeastOptions(),
                null);

        Point2D.Double point = new Point2D.Double();
        reader.metadata().gridToModel(20.0, 20.0, point);
        System.out.println(point.x + ", " + point.y);

        ITiffReader tiffReader = ITiffReader.openFile(fs, rasterPath);
        TiffRaster raster = new TiffRaster(tiffReader, 0);

        AbstractTiffTile tile = raster.getTile(0);

        RaptorJoin join;

        // RasterMetadata rmd = new RasterMetadata(raster, metaData);

        List<Polygon> features = new ArrayList<>();
        try (ShapefileFeatureReader featureReader = new ShapefileFeatureReader()) {
            featureReader.initialize(new Path(jct.inputVector), new BeastOptions());
            for (IFeature feature : featureReader) {

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
