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

import dk.itu.raptor.api.RaptorApi;
import dk.itu.raptor.io.commandline.CommandLineArgs;
import dk.itu.raptor.join.JoinResult;

import edu.ucr.cs.bdlab.beast.common.BeastOptions;
import edu.ucr.cs.bdlab.beast.geolite.RasterMetadata;
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

        // RasterMetadata rmd = new RasterMetadata(raster, metaData);
        RaptorApi api = new RaptorApi();

        long start = System.currentTimeMillis();
        Stream<JoinResult> res = api.join(rasterPath, vectorPath, Stream.of(metadata), readers);
        if (!jct.ranges.isEmpty()) {
            if (jct.ranges.size() != 2) {
                throw new IllegalArgumentException("More than one range is not supported");
            }
            res = res.filter(r -> jct.ranges.get(0) <= r.m && r.m <= jct.ranges.get(1));
        }
        System.out.println(res.count());

        long end = System.currentTimeMillis();
        System.out.println("joined in " + (end - start) + "ms");
    }
}
