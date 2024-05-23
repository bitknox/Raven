package dk.itu.raven.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.davidmoten.rtree2.geometry.Geometries;

import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.IRasterReader;
import dk.itu.raven.io.MatrixReader;
import dk.itu.raven.io.MockedShapefileReader;
import dk.itu.raven.io.TFWFormat;
import dk.itu.raven.io.cache.CacheOptions;
import dk.itu.raven.io.commandline.ResultType;
import dk.itu.raven.join.AbstractRavenJoin;
import dk.itu.raven.join.JoinFilterFunctions;
import dk.itu.raven.join.results.IResult;
import dk.itu.raven.join.results.IResult.Pixel;
import dk.itu.raven.join.results.IResultCreator;
import dk.itu.raven.join.results.JoinResult;
import dk.itu.raven.join.results.JoinResultItem;
import dk.itu.raven.util.matrix.ArrayMatrix;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.util.matrix.RandomMatrix;

public class RavenJoinIntegrationTest {

    @ParameterizedTest
    @MethodSource("dk.itu.raven.Util#getResultTypes")
    public void testRavenJoinWithFilter(ResultType type) throws IOException {
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
        IResultCreator resultCreator = InternalApi.getResultCreator(type);

        AbstractRavenJoin join = InternalApi.getJoin(rasterReader, shapefileReader, new CacheOptions(null, false), 2, 1,
                8, resultCreator);
        JoinResult result = join.join(JoinFilterFunctions.rangeFilter(filterLow, filterHigh)).asMemoryAllocatedResult();

        for (JoinResultItem item : result) {
            for (IResult value : item.pixelRanges) {
                for (Pixel pixel : value) {
                    int y = pixel.y;
                    int x = pixel.x;
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

    @ParameterizedTest
    @MethodSource("dk.itu.raven.Util#getResultTypes")
    public void testRavenJoinWithFilter2(ResultType type) throws IOException {
        int width = 2000;
        int height = 2000;
        int filterLow = 50;
        int filterHigh = 950;
        Matrix mat = new RandomMatrix(4, width, height, 1000);
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
        IResultCreator resultCreator = InternalApi.getResultCreator(type);

        AbstractRavenJoin join = InternalApi.getJoin(rasterReader, shapefileReader, new CacheOptions(null, false), 2, 1,
                8, resultCreator);
        JoinResult result = join.join(JoinFilterFunctions.rangeFilter(filterLow, filterHigh)).asMemoryAllocatedResult();

        for (JoinResultItem item : result) {
            for (IResult value : item.pixelRanges) {
                for (Pixel pixel : value) {
                    int y = pixel.y;
                    int x = pixel.x;
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

    @ParameterizedTest
    @MethodSource("dk.itu.raven.Util#getResultTypes")
    public void testRavenJoinWithSampleFilter(ResultType type) throws IOException {
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
                expected[x][y] = mat1.get(y, x) >= filterLow1 && mat1.get(y, x) <= filterHigh1
                        && mat2.get(y, x) >= filterLow2 && mat2.get(y, x) <= filterHigh2
                        && mat3.get(y, x) >= filterLow3 && mat3.get(y, x) <= filterHigh3;
            }
        }

        Matrix mat = new ArrayMatrix(m, width, height);

        boolean[][] actual = new boolean[width][height];

        IRasterReader rasterReader = new MatrixReader(mat, new TFWFormat(1, 0, 0, -1, 0, 0));
        MockedShapefileReader shapefileReader = new MockedShapefileReader(polygons);
        IResultCreator resultCreator = InternalApi.getResultCreator(type);

        AbstractRavenJoin join = InternalApi.getJoin(rasterReader, shapefileReader, new CacheOptions(null, false), 2, 1,
                8, resultCreator);
        JoinResult result = join
                .join(JoinFilterFunctions.multiSampleRangeFilter(
                        Arrays.asList(filterLow1, filterHigh1, filterLow2, filterHigh2, filterLow3, filterHigh3),
                        new int[]{8, 8, 8}, 24))
                .asMemoryAllocatedResult();

        for (JoinResultItem item : result) {
            for (IResult value : item.pixelRanges) {
                for (Pixel pixel : value) {
                    int y = pixel.y;
                    int x = pixel.x;
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
