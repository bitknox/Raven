package dk.itu.raptor.join;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.SparkConf;
import org.locationtech.jts.geom.Geometry;

import dk.itu.raptor.util.Pair;
import edu.ucr.cs.bdlab.beast.common.BeastOptions;
import edu.ucr.cs.bdlab.beast.geolite.IFeature;
import edu.ucr.cs.bdlab.beast.geolite.ITile;
import edu.ucr.cs.bdlab.beast.geolite.RasterMetadata;
import edu.ucr.cs.bdlab.beast.io.shapefile.ShapefileFeatureReader;
import edu.ucr.cs.bdlab.raptor.IRasterReader;
import edu.ucr.cs.bdlab.raptor.Intersections;
import edu.ucr.cs.bdlab.raptor.RasterHelper;

public class RaptorJoin {
    public RaptorJoin() {

    }

    private Stream<List<PixelRange>> extractCellsBeast(Path path, Geometry[] geometries, RasterMetadata metadata) {
        Map<Integer, List<PixelRange>> ranges = new HashMap<>();
        Intersections intersections = new Intersections();
        intersections.compute(geometries, metadata, new BeastOptions());
        for (int i = 0; i < intersections.getNumIntersections(); i++) {
            List<PixelRange> rangeList = ranges.get(intersections.getTileID(i));
            if (rangeList == null) {
                rangeList = new ArrayList<>();
                ranges.put(intersections.getTileID(i), rangeList);
            }
            rangeList.add(new PixelRange(path, intersections.getTileID(i),
                    intersections.getFeatureID(i),
                    intersections.getY(i), intersections.getX1(i), intersections.getX2(i)));
        }
        return ranges.values().stream();
    }

    public Stream<JoinResult> processFlashIndices(Stream<List<PixelRange>> ranges, FileSystem fs) {
        return ranges.flatMap(list -> { // changing this to a map causes heap space issues
            List<JoinResult> results = new ArrayList<>();
            int tid = list.get(0).tid;
            Path path = list.get(0).file;
            IRasterReader<Object> reader = RasterHelper.createRasterReader(fs, path, new BeastOptions(),
                    new SparkConf());
            ITile<Object> tile = reader.readTile(tid);

            for (PixelRange range : list) {
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
            return results.stream();
        });
    }

    public Stream<List<PixelRange>> optimizeFlashIndices(Stream<List<PixelRange>> ranges) {
        return ranges.map(list -> {
            Collections.sort(list);
            return list;
        });
    }

    public Stream<Stream<List<PixelRange>>> createFlashIndices(ShapefileFeatureReader featureReader,
            Stream<Pair<Path, RasterMetadata>> metadatas) {
        List<Geometry> geometries = new ArrayList<>();
        for (IFeature feature : featureReader) {
            geometries.add(feature.getGeometry());
        }
        Geometry[] geomArray = geometries.toArray(new Geometry[0]);

        return metadatas.map(metadata -> {
            return extractCellsBeast(metadata.first, geomArray, metadata.second);
        });
    }
}
