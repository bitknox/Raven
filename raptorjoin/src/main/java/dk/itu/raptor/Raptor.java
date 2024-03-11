package dk.itu.raptor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.beust.jcommander.JCommander;

import dk.itu.raptor.io.commandline.CommandLineArgs;
import dk.itu.raptor.join.JoinResult;
import dk.itu.raptor.join.PixelRange;
import dk.itu.raptor.join.RaptorJoin;
// import dk.itu.raptor.join.RasterMetadata;
import edu.ucr.cs.bdlab.beast.common.BeastOptions;
import edu.ucr.cs.bdlab.beast.geolite.RasterMetadata;
import edu.ucr.cs.bdlab.beast.io.shapefile.ShapefileFeatureReader;
import edu.ucr.cs.bdlab.raptor.IRasterReader;
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

        IRasterReader<Object> reader = RasterHelper.createRasterReader(fs, rasterPath, new BeastOptions());
        List<IRasterReader<Object>> readers = new ArrayList<>();
        readers.add(reader);

        RasterMetadata metadata = reader.metadata();
        RaptorJoin join = new RaptorJoin();

        // RasterMetadata rmd = new RasterMetadata(raster, metaData);

        long start = System.currentTimeMillis();

        try (ShapefileFeatureReader featureReader = new ShapefileFeatureReader()) {
            featureReader.initialize(vectorPath, new BeastOptions());
            Stream<List<PixelRange>> stream = join.createFlashIndices(featureReader, Stream.of(metadata));
            stream = join.optimizeFlashIndices(stream);
            if (jct.parallel) {
                stream = stream.parallel();
            }
            Stream<JoinResult> res = join.processFlashIndices(stream, readers);
            if (!jct.ranges.isEmpty()) {
                if (jct.ranges.size() != 2) {
                    throw new IllegalArgumentException("More than one range is not supported");
                }
                res = res.filter(r -> jct.ranges.get(0) <= r.m && r.m <= jct.ranges.get(1));
            }
            System.out.println(res.count());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        long end = System.currentTimeMillis();
        System.out.println("joined in " + (end - start) + "ms");
    }
}
