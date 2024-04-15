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
        File rasterFile = new File(inputRaster);
        Path vectorPath = new Path(new File(inputVector).getAbsolutePath());

        FileSystem fs = FileSystem.newInstance(new Configuration());

        RaptorJoin join = new RaptorJoin();
        ShapefileFeatureReader featureReader = new ShapefileFeatureReader();
        List<File> files = Arrays.asList(rasterFile.listFiles());

        List<RasterMetadata> metadatas = new ArrayList<>();
        List<IRasterReader<Object>> readers = new ArrayList<>();

        for (File file : files) {
            if (!file.isDirectory()) {
                break;
            }
            File[] tiffFiles = file.listFiles((dir1, name) -> name.endsWith(".tif"));

            if (tiffFiles.length == 0) {
                break;
            }
            Path rasterPath = new Path(tiffFiles[0].getAbsolutePath());
            IRasterReader<Object> reader = RasterHelper.createRasterReader(fs, rasterPath, new BeastOptions(),
                    new SparkConf());
            readers.add(reader);
            RasterMetadata metadata = reader.metadata();
            metadatas.add(metadata);
        }

        featureReader.initialize(vectorPath, new BeastOptions());
        Stream<List<PixelRange>> stream = join.createFlashIndices(featureReader, metadatas.stream());
        stream = join.optimizeFlashIndices(stream);
        Stream<JoinResult> res = join.processFlashIndices(stream, readers);

        return res;
    }
}
