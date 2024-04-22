package dk.itu.raptor.join;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.SparkConf;
import org.locationtech.jts.geom.Geometry;

import dk.itu.raptor.util.Pair;
import edu.ucr.cs.bdlab.beast.common.BeastOptions;
import edu.ucr.cs.bdlab.beast.geolite.ITile;
import edu.ucr.cs.bdlab.beast.geolite.RasterMetadata;
import edu.ucr.cs.bdlab.raptor.IRasterReader;
import edu.ucr.cs.bdlab.raptor.Intersections;
import edu.ucr.cs.bdlab.raptor.RasterHelper;

public class RaptorJoin {
    public RaptorJoin() {

    }

    private Stream<List<PixelRange>> extractCellsBeast(Path path, Geometry[] geometries, RasterMetadata metadata) {
        List<List<PixelRange>> ranges = new ArrayList<>();
        for (int i = 0; i < metadata.numTiles(); i++) {
            ranges.add(new ArrayList<>());
        }
        Intersections intersections = new Intersections();
        intersections.compute(geometries, metadata, new BeastOptions());
        for (int i = 0; i < intersections.getNumIntersections(); i++) {
            ranges.get(intersections.getTileID(i)).add(new PixelRange(path, intersections.getTileID(i),
                    intersections.getFeatureID(i),
                    intersections.getY(i), intersections.getX1(i), intersections.getX2(i)));
        }
        return ranges.stream();
    }

    public Stream<List<JoinResult>> processFlashIndices(Stream<List<PixelRange>> stream, FileSystem fs) {
        return stream.map(ranges -> {
            if (ranges.isEmpty()) {
                return new ArrayList<>();
            }
            List<JoinResult> results = new ArrayList<>();
            Path path = ranges.get(0).file;
            IRasterReader<Object> reader = RasterHelper.createRasterReader(fs, path, new BeastOptions(),
                    new SparkConf());
            int tid = ranges.get(0).tid;
            ITile<Object> tile = reader.readTile(tid);
            for (var range : ranges) {
                if (range.tid != tid) {
                    tid = range.tid;
                    tile = reader.readTile(tid);
                }

                for (int x = range.x1; x <= range.x2; x++) {
                    Object m = tile.getPixelValue(x, range.y);
                    results.add(
                            new JoinResult(range.gid, x, range.y,
                                    tile.numComponents() == 1 ? (int) m
                                            : (((int[]) m)[0] << 16) + (((int[]) m)[1] << 8) + (((int[]) m)[2])));
                }
            }
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return results;
        });
    }

    public void optimizeFlashIndices(Stream<List<PixelRange>> ranges) {
        // Collections.sort(ranges);
    }

    public Stream<List<PixelRange>> createFlashIndices(Geometry[] geomArray,
            Pair<Path, RasterMetadata> metadata) {
        return extractCellsBeast(metadata.first, geomArray, metadata.second);
    }
}
