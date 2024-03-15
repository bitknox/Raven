package dk.itu.raptor.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.SparkConf;

import dk.itu.raptor.join.JoinResult;
import dk.itu.raptor.join.PixelRange;
import dk.itu.raptor.join.RaptorJoin;
import edu.ucr.cs.bdlab.beast.common.BeastOptions;
import edu.ucr.cs.bdlab.beast.geolite.RasterMetadata;
import edu.ucr.cs.bdlab.beast.io.shapefile.ShapefileFeatureReader;
import edu.ucr.cs.bdlab.raptor.IRasterReader;
import edu.ucr.cs.bdlab.raptor.RasterHelper;

public class RaptorApi {
    public Stream<JoinResult> join(String inputRaster, String inputVector) throws IOException {
        Path rasterPath = new Path(new File(inputRaster).getAbsolutePath());
        Path vectorPath = new Path(new File(inputVector).getAbsolutePath());

        FileSystem fs = rasterPath.getParent().getFileSystem(new Configuration());

        RaptorJoin join = new RaptorJoin();
        try (ShapefileFeatureReader featureReader = new ShapefileFeatureReader();
                IRasterReader<Object> reader = RasterHelper.createRasterReader(fs, rasterPath, new BeastOptions(),
                        new SparkConf())) {
            List<IRasterReader<Object>> readers = new ArrayList<>();
            readers.add(reader);

            RasterMetadata metadata = reader.metadata();
            featureReader.initialize(vectorPath, new BeastOptions());
            Stream<List<PixelRange>> stream = join.createFlashIndices(featureReader, Stream.of(metadata));
            stream = join.optimizeFlashIndices(stream);
            Stream<JoinResult> res = join.processFlashIndices(stream, readers);

            return res;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
            return Stream.empty();
        }
    }
}
