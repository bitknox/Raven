package dk.itu.raven.api;

import static dk.itu.raven.api.InternalApi.getJoin;
import static dk.itu.raven.api.InternalApi.getStreamedJoin;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.davidmoten.rtree2.geometry.Geometries;

import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.IRasterReader;
import dk.itu.raven.io.MatrixReader;
import dk.itu.raven.io.MockedShapefileReader;
import dk.itu.raven.io.ShapefileReader;
import dk.itu.raven.io.TFWFormat;
import dk.itu.raven.io.cache.CacheOptions;
import dk.itu.raven.io.commandline.ResultType;
import dk.itu.raven.join.AbstractRavenJoin;
import dk.itu.raven.join.JoinResult;
import dk.itu.raven.join.results.IResult;
import dk.itu.raven.join.results.IResultCreator;
import dk.itu.raven.join.results.IResult.Pixel;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.util.matrix.RandomMatrix;

public class ApiTest {
    @ParameterizedTest
    @MethodSource("parametersProvider")
    public void offsetSurroundingPolygonTest(int streamed, ResultType type) throws IOException {
        Polygon p = new Polygon(Arrays.asList(Geometries.point(-10, -10), Geometries.point(20, -10),
                Geometries.point(20, 20), Geometries.point(-10, 20), Geometries.point(-10, -10), Geometries.point(0, 0),
                Geometries.point(10, 0), Geometries.point(10, 10), Geometries.point(0, 10), Geometries.point(0, 0)));
        Matrix m = new RandomMatrix(10, 10, 10, 10);

        ShapefileReader vectorReader = new MockedShapefileReader(List.of(p));
        IRasterReader rasterReader = new MatrixReader(m, new TFWFormat(1, 0, 0, -1, 0, 0));
        JoinResult result = getResult(streamed, type, vectorReader, rasterReader);

        int sum = 0;
        for (var item : result) {
            sum += item.pixelRanges.size();
        }

        assertEquals(0, sum);
    }

    @ParameterizedTest
    @MethodSource("parametersProvider")
    public void offsetRasterCoverPolygonTest(int streamed, ResultType type) throws IOException {
        Polygon p = new Polygon(Arrays.asList(Geometries.point(5, 5), Geometries.point(10, 5), Geometries.point(10, 10),
                Geometries.point(5, 10)));
        Matrix m = new RandomMatrix(10, 15, 15, 10);

        ShapefileReader vectorReader = new MockedShapefileReader(List.of(p));
        IRasterReader rasterReader = new MatrixReader(m, new TFWFormat(1, 0, 0, -1, 0, 0));

        JoinResult result = getResult(streamed, type, vectorReader, rasterReader);

        assertIncluded(15, 15, 5, 5, 9, 9, result);
    }

    @ParameterizedTest
    @MethodSource("parametersProvider")
    public void offsetPolygonCoverRasterTest(int streamed, ResultType type) throws IOException {
        Polygon p = new Polygon(
                Arrays.asList(Geometries.point(-5, -5), Geometries.point(15, -5), Geometries.point(15, 15),
                        Geometries.point(-5, 15)));
        Matrix m = new RandomMatrix(10, 10, 10, 10);

        ShapefileReader vectorReader = new MockedShapefileReader(List.of(p));
        IRasterReader rasterReader = new MatrixReader(m, new TFWFormat(1, 0, 0, -1, 0, 0));
        JoinResult result = getResult(streamed, type, vectorReader, rasterReader);

        assertIncluded(10, 10, 0, 0, 9, 9, result);
    }

    @ParameterizedTest
    @MethodSource("parametersProvider")
    public void offsetTopLeftPolygonTest(int streamed, ResultType type) throws IOException {
        Polygon p = new Polygon(Arrays.asList(Geometries.point(-5, -5), Geometries.point(5, -5), Geometries.point(5, 5),
                Geometries.point(-5, 5)));
        Matrix m = new RandomMatrix(10, 10, 10, 10);

        ShapefileReader vectorReader = new MockedShapefileReader(List.of(p));
        IRasterReader rasterReader = new MatrixReader(m, new TFWFormat(1, 0, 0, -1, 0, 0));
        JoinResult result = getResult(streamed, type, vectorReader, rasterReader);

        assertIncluded(10, 10, 0, 0, 4, 4, result);
    }

    @ParameterizedTest
    @MethodSource("parametersProvider")
    public void offsetBottomRightPolygonTest(int streamed, ResultType type) throws IOException {
        Polygon p = new Polygon(Arrays.asList(Geometries.point(5, 5), Geometries.point(15, 5), Geometries.point(15, 15),
                Geometries.point(5, 15)));
        Matrix m = new RandomMatrix(10, 10, 10, 10);

        ShapefileReader vectorReader = new MockedShapefileReader(List.of(p));
        IRasterReader rasterReader = new MatrixReader(m, new TFWFormat(1, 0, 0, -1, 0, 0));
        JoinResult result = getResult(streamed, type, vectorReader, rasterReader);

        assertIncluded(10, 10, 5, 5, 9, 9, result);
    }

    @ParameterizedTest
    @MethodSource("parametersProvider")
    public void offsetTopRightPolygonTest(int streamed, ResultType type) throws IOException {
        Polygon p = new Polygon(Arrays.asList(Geometries.point(5, -5), Geometries.point(15, -5),
                Geometries.point(15, 5), Geometries.point(5, 5)));
        Matrix m = new RandomMatrix(10, 10, 10, 10);

        ShapefileReader vectorReader = new MockedShapefileReader(List.of(p));
        IRasterReader rasterReader = new MatrixReader(m, new TFWFormat(1, 0, 0, -1, 0, 0));
        JoinResult result = getResult(streamed, type, vectorReader, rasterReader);

        assertIncluded(10, 10, 5, 0, 9, 4, result);
    }

    @ParameterizedTest
    @MethodSource("parametersProvider")
    public void offsetBottomLeftPolygonTest(int streamed, ResultType type) throws IOException {
        Polygon p = new Polygon(Arrays.asList(Geometries.point(-5, 5), Geometries.point(5, 5), Geometries.point(5, 15),
                Geometries.point(-5, 15)));
        Matrix m = new RandomMatrix(10, 10, 10, 10);

        ShapefileReader vectorReader = new MockedShapefileReader(List.of(p));
        IRasterReader rasterReader = new MatrixReader(m, new TFWFormat(1, 0, 0, -1, 0, 0));
        JoinResult result = getResult(streamed, type, vectorReader, rasterReader);

        assertIncluded(10, 10, 0, 5, 4, 9, result);
    }

    @ParameterizedTest
    @MethodSource("parametersProvider")
    public void offsetOutsideRightPolygonTest(int streamed, ResultType type) throws IOException {
        Polygon p = new Polygon(Arrays.asList(Geometries.point(10, 5), Geometries.point(15, 5),
                Geometries.point(15, 10), Geometries.point(10, 10)));
        Matrix m = new RandomMatrix(10, 10, 10, 10);

        ShapefileReader vectorReader = new MockedShapefileReader(List.of(p));
        IRasterReader rasterReader = new MatrixReader(m, new TFWFormat(1, 0, 0, -1, 0, 0));
        JoinResult result = getResult(streamed, type, vectorReader, rasterReader);

        int sum = 0;
        for (var item : result) {
            sum += item.pixelRanges.size();
        }

        assertEquals(0, sum);
    }

    @ParameterizedTest
    @MethodSource("parametersProvider")
    public void offsetOutsideLeftPolygonTest(int streamed, ResultType type) throws IOException {
        Polygon p = new Polygon(Arrays.asList(Geometries.point(-5, 5), Geometries.point(0, 5),
                Geometries.point(0, 10), Geometries.point(-5, 10)));
        Matrix m = new RandomMatrix(10, 10, 10, 10);

        ShapefileReader vectorReader = new MockedShapefileReader(List.of(p));
        IRasterReader rasterReader = new MatrixReader(m, new TFWFormat(1, 0, 0, -1, 0, 0));
        JoinResult result = getResult(streamed, type, vectorReader, rasterReader);

        int sum = 0;
        for (var item : result) {
            sum += item.pixelRanges.size();
        }

        assertEquals(0, sum);
    }

    @ParameterizedTest
    @MethodSource("parametersProvider")
    public void offsetOutsideBottomPolygonTest(int streamed, ResultType type) throws IOException {
        Polygon p = new Polygon(Arrays.asList(Geometries.point(5, 10), Geometries.point(10, 10),
                Geometries.point(10, 15), Geometries.point(5, 15)));
        Matrix m = new RandomMatrix(10, 10, 10, 10);

        ShapefileReader vectorReader = new MockedShapefileReader(List.of(p));
        IRasterReader rasterReader = new MatrixReader(m, new TFWFormat(1, 0, 0, -1, 0, 0));
        JoinResult result = getResult(streamed, type, vectorReader, rasterReader);

        int sum = 0;
        for (var item : result) {
            sum += item.pixelRanges.size();
        }

        assertEquals(0, sum);
    }

    @ParameterizedTest
    @MethodSource("parametersProvider")
    public void perfectMatchPolygonTest(int streamed, ResultType type) throws IOException {
        Polygon p = new Polygon(Arrays.asList(Geometries.point(0, 0), Geometries.point(10, 0), Geometries.point(10, 10),
                Geometries.point(0, 10)));
        Matrix m = new RandomMatrix(10, 10, 10, 10);

        ShapefileReader vectorReader = new MockedShapefileReader(List.of(p));
        IRasterReader rasterReader = new MatrixReader(m, new TFWFormat(1, 0, 0, -1, 0, 0));
        JoinResult result = getResult(streamed, type, vectorReader, rasterReader);

        assertIncluded(10, 10, 0, 0, 9, 9, result);
    }

    private void assertIncluded(int width, int height, int minX, int minY, int maxX, int maxY, JoinResult result) {
        int sum = 0;
        boolean[][] pixelIncluded = new boolean[width][height];
        for (var item : result) {
            for (IResult value : item.pixelRanges) {
                for (Pixel pixel : value) {
                    sum++;
                    pixelIncluded[pixel.x][pixel.y] = true;
                }
            }
        }

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                assertTrue(pixelIncluded[x][y], "pixel " + x + ", " + y + "should be included");
            }
        }

        assertEquals((maxX - minX + 1) * (maxY - minY + 1), sum);
    }

    private JoinResult getResult(int streamed, ResultType type, ShapefileReader vectorReader,
            IRasterReader rasterReader)
            throws IOException {
        IResultCreator resultCreator = InternalApi.getResultCreator(type);
        AbstractRavenJoin join;
        if (streamed == 0) {
            join = getStreamedJoin(rasterReader, vectorReader, 4, 4, false, new CacheOptions(null, false), 2, 1, 8,
                    resultCreator);
        } else if (streamed == 1) {
            join = getStreamedJoin(rasterReader, vectorReader, 4, 4, true, new CacheOptions(null, false), 2, 1, 8,
                    resultCreator);
        } else {
            join = getJoin(rasterReader, vectorReader, new CacheOptions(null, false), 2, 1, 8, resultCreator);
        }
        JoinResult result = join.join().asMemoryAllocatedResult();
        return result;
    }

    static Stream<Object[]> parametersProvider() {
        return Stream.of(
                new Object[] { 0, ResultType.VALUE },
                new Object[] { 0, ResultType.RANGE },
                new Object[] { 0, ResultType.RANGEVALUE },
                new Object[] { 1, ResultType.VALUE },
                new Object[] { 1, ResultType.RANGE },
                new Object[] { 1, ResultType.RANGEVALUE },
                new Object[] { 2, ResultType.VALUE },
                new Object[] { 2, ResultType.RANGE },
                new Object[] { 2, ResultType.RANGEVALUE });
    }
}
