package dk.itu.raven.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.davidmoten.rtree2.geometry.Geometries;

import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.MatrixReader;
import dk.itu.raven.io.MockedShapefileReader;
import dk.itu.raven.io.RasterReader;
import dk.itu.raven.io.TFWFormat;
import dk.itu.raven.join.JoinFilterFunctions;
import dk.itu.raven.join.JoinResult;
import dk.itu.raven.join.JoinResultItem;
import dk.itu.raven.join.RavenJoin;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.util.matrix.RandomMatrix;

public class RavenJoinIntegrationTest {
    @Test
    public void testRavenJoinWithFilterCorrectness() throws IOException {
        int width = 2000;
        int height = 2000;
        int filterLow = 50;
        int filterHigh = 950;
        Matrix mat = new RandomMatrix(width, height, 1000);
        List<Polygon> polygons = Arrays.asList(new Polygon(Arrays.asList(Geometries.point(-5, -5),
                Geometries.point(-5, height + 5), Geometries.point(width + 5, height + 5),
                Geometries.point(width + 5, -5))));

        boolean[][] expected = new boolean[width][height];
        boolean[][] actual = new boolean[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                expected[x][y] = mat.get(y, x) >= filterLow && mat.get(y, x) <= filterHigh;
            }
        }

        RasterReader rasterReader = new MatrixReader(mat, new TFWFormat(1, 0, 0, -1, 0, 0));
        MockedShapefileReader shapefileReader = new MockedShapefileReader(polygons);

        RavenJoin join = InternalApi.getJoin(rasterReader, shapefileReader);
        JoinResult result = join.join(JoinFilterFunctions.rangeFilter(filterLow, filterHigh)).asMemoryAllocatedResult();

        for (JoinResultItem item : result) {
            for (PixelRange range : item.pixelRanges) {
                int y = range.row;
                for (int x = range.x1; x <= range.x2; x++) {
                    actual[x][y] = true;
                }
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                assertEquals(expected[x][y], actual[x][y]);
            }
        }
    }

    @Test
    public void testRavenJoinWithFilterCorrectness2() throws IOException {
        int width = 2000;
        int height = 2000;
        int filterLow = 50;
        int filterHigh = 950;
        Matrix mat = new RandomMatrix(width, height, 1000);
        List<Polygon> polygons = Arrays.asList(new Polygon(Arrays.asList(Geometries.point(100, 100),
                Geometries.point(100, height - 100), Geometries.point(width - 100, height - 100),
                Geometries.point(width - 100, 100))));

        boolean[][] expected = new boolean[width][height];
        boolean[][] actual = new boolean[width][height];

        // the following assumes that pixels at the bottom and right edge of the polygon
        // are not joined
        for (int x = 100; x < width - 100; x++) {
            for (int y = 100; y < height - 100; y++) {
                expected[x][y] = mat.get(y, x) >= filterLow && mat.get(y, x) <= filterHigh;
            }
        }

        RasterReader rasterReader = new MatrixReader(mat, new TFWFormat(1, 0, 0, -1, 0, 0));
        MockedShapefileReader shapefileReader = new MockedShapefileReader(polygons);

        RavenJoin join = InternalApi.getJoin(rasterReader, shapefileReader);
        JoinResult result = join.join(JoinFilterFunctions.rangeFilter(filterLow, filterHigh)).asMemoryAllocatedResult();

        for (JoinResultItem item : result) {
            for (PixelRange range : item.pixelRanges) {
                int y = range.row;
                for (int x = range.x1; x <= range.x2; x++) {
                    actual[x][y] = true;
                }
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                assertEquals(expected[x][y], actual[x][y]);
            }
        }
    }
}
