package dk.itu.raven.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.davidmoten.rtree2.geometry.Geometries;

import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.IRasterReader;
import dk.itu.raven.io.MatrixReader;
import dk.itu.raven.io.MockedShapefileReader;
import dk.itu.raven.io.TFWFormat;
import dk.itu.raven.io.cache.CacheOptions;
import dk.itu.raven.join.AbstractRavenJoin;
import dk.itu.raven.join.JoinFilterFunctions;
import dk.itu.raven.join.JoinResult;
import dk.itu.raven.join.JoinResultItem;
import dk.itu.raven.util.matrix.ArrayMatrix;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.util.matrix.RandomMatrix;

public class RavenJoinIntegrationTest {
    @Test
    public void testRavenJoinWithFilter() throws IOException {
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

        IRasterReader rasterReader = new MatrixReader(mat, new TFWFormat(1, 0, 0, -1, 0, 0));
        MockedShapefileReader shapefileReader = new MockedShapefileReader(polygons);

        AbstractRavenJoin join = InternalApi.getJoin(rasterReader, shapefileReader, new CacheOptions(null, false));
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
    public void testRavenJoinWithFilter2() throws IOException {
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

        IRasterReader rasterReader = new MatrixReader(mat, new TFWFormat(1, 0, 0, -1, 0, 0));
        MockedShapefileReader shapefileReader = new MockedShapefileReader(polygons);

        AbstractRavenJoin join = InternalApi.getJoin(rasterReader, shapefileReader, new CacheOptions(null, false));
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
    public void testRavenJoinWithSampleFilter() throws IOException {
        int width = 2000;
        int height = 2000;

        long filterLow1 = 0;
        long filterHigh1 = 10;

        long filterLow2 = 10;
        long filterHigh2 = 255;

        long filterLow3 = 20;
        long filterHigh3 = 128;

        Matrix mat1 = new RandomMatrix(width, height, 0xff);
        Matrix mat2 = new RandomMatrix(width, height, 0xff);
        Matrix mat3 = new RandomMatrix(width, height, 0xff);
        List<Polygon> polygons = Arrays.asList(new Polygon(Arrays.asList(Geometries.point(-5, -5),
                Geometries.point(-5, height + 5), Geometries.point(width + 5, height + 5),
                Geometries.point(width + 5, -5))));

        int[][] m = new int[width][height];
        boolean[][] expected = new boolean[width][height];

        // combines the three random matrices into one matrix and builds the expected
        // result at the same time
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                m[x][y] = (mat1.get(y, x) << 16) + (mat2.get(y, x) << 8) + (mat3.get(y, x));
                expected[x][y] = mat1.get(y, x) >= filterLow1 && mat1.get(y, x) <= filterHigh1 &&
                        mat2.get(y, x) >= filterLow2 && mat2.get(y, x) <= filterHigh2 &&
                        mat3.get(y, x) >= filterLow3 && mat3.get(y, x) <= filterHigh3;
            }
        }

        Matrix mat = new ArrayMatrix(m, width, height);

        boolean[][] actual = new boolean[width][height];

        IRasterReader rasterReader = new MatrixReader(mat, new TFWFormat(1, 0, 0, -1, 0, 0));
        MockedShapefileReader shapefileReader = new MockedShapefileReader(polygons);

        AbstractRavenJoin join = InternalApi.getJoin(rasterReader, shapefileReader, new CacheOptions(null, false));
        JoinResult result = join
                .join(JoinFilterFunctions.multiSampleRangeFilter(
                        Arrays.asList(filterLow1, filterHigh1, filterLow2, filterHigh2, filterLow3, filterHigh3),
                        new int[] { 8, 8, 8 }, 24))
                .asMemoryAllocatedResult();

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
                assertEquals(expected[x][y], actual[x][y], "index: " + x + ", " + y);
            }
        }
    }
}
