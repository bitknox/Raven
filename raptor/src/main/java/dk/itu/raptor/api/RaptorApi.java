package dk.itu.raptor.api;

import java.util.List;
import java.util.stream.Stream;

import org.apache.hadoop.fs.Path;

import dk.itu.raptor.join.JoinResult;
import dk.itu.raptor.join.PixelRange;
import dk.itu.raptor.join.RaptorJoin;
import edu.ucr.cs.bdlab.beast.common.BeastOptions;
import edu.ucr.cs.bdlab.beast.geolite.RasterMetadata;
import edu.ucr.cs.bdlab.beast.io.shapefile.ShapefileFeatureReader;
import edu.ucr.cs.bdlab.raptor.IRasterReader;

public class RaptorApi {
    public Stream<JoinResult> join(Path vectorPath, Path rasterPath,
            Stream<RasterMetadata> metadatas, List<IRasterReader<Object>> readers) {
        RaptorJoin join = new RaptorJoin();
        try (ShapefileFeatureReader featureReader = new ShapefileFeatureReader()) {
            featureReader.initialize(vectorPath, new BeastOptions());
            Stream<List<PixelRange>> stream = join.createFlashIndices(featureReader, metadatas);
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
