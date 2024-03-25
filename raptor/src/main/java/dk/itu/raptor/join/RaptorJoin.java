package dk.itu.raptor.join;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.locationtech.jts.geom.Geometry;

import edu.ucr.cs.bdlab.beast.common.BeastOptions;
import edu.ucr.cs.bdlab.beast.geolite.IFeature;
import edu.ucr.cs.bdlab.beast.geolite.ITile;
import edu.ucr.cs.bdlab.beast.geolite.RasterMetadata;
import edu.ucr.cs.bdlab.beast.io.shapefile.ShapefileFeatureReader;
import edu.ucr.cs.bdlab.raptor.IRasterReader;
import edu.ucr.cs.bdlab.raptor.Intersections;

public class RaptorJoin {
    public RaptorJoin() {

    }

    private Stream<List<PixelRange>> extractCellsBeast(int rid, Geometry[] geometries, RasterMetadata metadata) {
        Map<Integer, List<PixelRange>> ranges = new HashMap<>();
        Intersections intersections = new Intersections();
        intersections.compute(geometries, metadata, new BeastOptions());
        for (int i = 0; i < intersections.getNumIntersections(); i++) {
            List<PixelRange> rangeList = ranges.get(intersections.getTileID(i));
            if (rangeList == null) {
                rangeList = new ArrayList<>();
                ranges.put(intersections.getTileID(i), rangeList);
            }
            rangeList.add(new PixelRange(rid, intersections.getTileID(i), intersections.getFeatureID(i),
                    intersections.getY(i), intersections.getX1(i), intersections.getX2(i)));
        }
        return ranges.values().stream();
    }

    public Stream<JoinResult> processFlashIndices(Stream<List<PixelRange>> ranges,
            List<IRasterReader<Object>> rasterReaders) {
        return ranges.flatMap(list -> {
            List<JoinResult> results = new ArrayList<>();
            int tid = list.get(0).tid;
            int rid = list.get(0).rid;
            ITile<Object> tile = rasterReaders.get(rid).readTile(tid);

            for (PixelRange range : list) {
                for (int x = range.x1; x <= range.x2; x++) {
                    Object m = tile.getPixelValue(x, range.y);
                    results.add(
                            new JoinResult(range.gid, range.rid, x, range.y,
                                    tile.numComponents() == 1 ? (int) m
                                            : (((int[]) m)[0] << 16) + (((int[]) m)[1] << 8) + (((int[]) m)[2])));
                }
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

    public Stream<List<PixelRange>> createFlashIndices(ShapefileFeatureReader featureReader,
            Stream<RasterMetadata> metadatas) {
        List<Geometry> geometries = new ArrayList<>();
        for (IFeature feature : featureReader) {
            geometries.add(feature.getGeometry());
        }
        final AtomicInteger counter = new AtomicInteger();
        return metadatas.flatMap(metadata -> {
            return extractCellsBeast(counter.getAndIncrement(), geometries.toArray(new Geometry[0]), metadata);
        });
    }
}
