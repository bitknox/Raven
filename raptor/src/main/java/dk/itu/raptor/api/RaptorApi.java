package dk.itu.raptor.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.SparkConf;
import org.locationtech.jts.geom.Geometry;

import dk.itu.raptor.join.JoinResult;
import dk.itu.raptor.join.PixelRange;
import dk.itu.raptor.join.RaptorJoin;
import dk.itu.raptor.util.Pair;
import edu.ucr.cs.bdlab.beast.common.BeastOptions;
import edu.ucr.cs.bdlab.beast.geolite.IFeature;
import edu.ucr.cs.bdlab.beast.geolite.RasterMetadata;
import edu.ucr.cs.bdlab.beast.io.shapefile.ShapefileFeatureReader;
import edu.ucr.cs.bdlab.raptor.IRasterReader;
import edu.ucr.cs.bdlab.raptor.RasterHelper;

public class RaptorApi {

    public Stream<List<JoinResult>> join(Path rasterPath, Geometry[] geomArray) throws IOException {
        RaptorJoin join = new RaptorJoin();
        FileSystem fs = FileSystem.newInstance(new Configuration());

        IRasterReader<Object> reader = RasterHelper.createRasterReader(fs, rasterPath, new BeastOptions(),
                new SparkConf());
        RasterMetadata metadata = reader.metadata();
        Stream<List<PixelRange>> ranges = join.createFlashIndices(geomArray, new Pair<>(rasterPath, metadata));
        join.optimizeFlashIndices(ranges);
        Stream<List<JoinResult>> res = join.processFlashIndices(ranges, fs);

        fs.close();

        return res;
    }

    public static interface JoinCallback {
        public void call(Stream<List<JoinResult>> result);
    }

    public void join(String inputRaster, String inputVector, boolean parallel, JoinCallback callback)
            throws IOException {
        File rasterFile = new File(inputRaster);
        Path vectorPath = new Path(new File(inputVector).getAbsolutePath());

        ShapefileFeatureReader featureReader = new ShapefileFeatureReader();
        featureReader.initialize(vectorPath, new BeastOptions());

        List<Geometry> geometries = new ArrayList<>();
        for (IFeature feature : featureReader) {
            geometries.add(feature.getGeometry());
        }
        Geometry[] geomArray = geometries.toArray(new Geometry[0]);

        List<File> files = Arrays.asList(rasterFile.listFiles());

        Stream<File> stream = files.stream();

        if (parallel) {
            stream = stream.parallel();
        }

        stream.forEach(f -> {
            System.out.println(f.getAbsolutePath());
            if (!f.isDirectory()) {
                return;
            }
            File[] tiffFiles = f.listFiles((dir1, name) -> name.endsWith(".tif"));

            if (tiffFiles.length == 0) {
                return;
            }
            Path rasterPath = new Path(tiffFiles[0].getAbsolutePath());
            try {
                callback.call(join(rasterPath, geomArray));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        featureReader.close();
    }
}
