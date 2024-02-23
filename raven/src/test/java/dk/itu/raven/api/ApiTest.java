package dk.itu.raven.api;

import static dk.itu.raven.api.InternalApi.buildStructures;
import static dk.itu.raven.api.InternalApi.streamStructures;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static dk.itu.raven.api.InternalApi.getJoin;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.geometry.Size;
import dk.itu.raven.io.ImageMetadata;
import dk.itu.raven.io.MatrixReader;
import dk.itu.raven.io.MockedShapefileReader;
import dk.itu.raven.io.RasterReader;
import dk.itu.raven.io.ShapefileReader;
import dk.itu.raven.io.TFWFormat;
import dk.itu.raven.join.AbstractJoinResult;
import dk.itu.raven.join.AbstractRavenJoin;
import dk.itu.raven.join.JoinResult;
import dk.itu.raven.join.RavenJoin;
import dk.itu.raven.ksquared.AbstractK2Raster;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.util.matrix.RandomMatrix;

public class ApiTest {
    @Test
    public void offsetSurroundingPolygonTest() throws IOException {
        Polygon p = new Polygon(Arrays.asList(Geometries.point(-10, -10), Geometries.point(20, -10),
                Geometries.point(20, 20), Geometries.point(-10, 20), Geometries.point(-10, -10), Geometries.point(0, 0),
                Geometries.point(10, 0), Geometries.point(10, 10), Geometries.point(0, 10), Geometries.point(0, 0)));
        Matrix m = new RandomMatrix(10, 10, 10, 10);

        ShapefileReader vectorReader = new MockedShapefileReader(List.of(p));
        RasterReader rasterReader = new MatrixReader(m, new TFWFormat(1, 0, 0, -1, 0, 0));
        RavenJoin join = getJoin(rasterReader, vectorReader);
        JoinResult result = join.join().asMemoryAllocatedResult();

        int sum = 0;
        for (var item : result) {
            sum += item.pixelRanges.size();
        }

        assertEquals(0, sum);
    }

    @Test
    public void offsetRasterCoverPolygonTest() throws IOException {
        Polygon p = new Polygon(Arrays.asList(Geometries.point(5, 5), Geometries.point(10, 5), Geometries.point(10, 10),
                Geometries.point(5, 10)));
        Matrix m = new RandomMatrix(10, 15, 15, 10);

        ShapefileReader vectorReader = new MockedShapefileReader(List.of(p));
        RasterReader rasterReader = new MatrixReader(m, new TFWFormat(1, 0, 0, -1, 0, 0));
        RavenJoin join = getJoin(rasterReader, vectorReader);
        JoinResult result = join.join().asMemoryAllocatedResult();

        int sum = 0;
        for (var item : result) {
            sum += item.pixelRanges.size();
            for (PixelRange range : item.pixelRanges) {
                assertEquals(5, range.x1);
                assertEquals(10, range.x2);
            }
        }

        assertEquals(5, sum);
    }

    @Test
    public void offsetPolygonCoverRasterTest() throws IOException {
        Polygon p = new Polygon(
                Arrays.asList(Geometries.point(-5, -5), Geometries.point(15, -5), Geometries.point(15, 15),
                        Geometries.point(-5, 15)));
        Matrix m = new RandomMatrix(10, 10, 10, 10);

        ShapefileReader vectorReader = new MockedShapefileReader(List.of(p));
        RasterReader rasterReader = new MatrixReader(m, new TFWFormat(1, 0, 0, -1, 0, 0));
        RavenJoin join = getJoin(rasterReader, vectorReader);
        JoinResult result = join.join().asMemoryAllocatedResult();

        int sum = 0;
        for (var item : result) {
            sum += item.pixelRanges.size();
            for (PixelRange range : item.pixelRanges) {
                assertEquals(0, range.x1);
                assertEquals(9, range.x2);
            }
        }

        assertEquals(10, sum);
    }
}
