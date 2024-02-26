package dk.itu.raven.api;

import static dk.itu.raven.api.InternalApi.getJoin;
import static dk.itu.raven.api.InternalApi.getStreamedJoin;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.github.davidmoten.rtree2.geometry.Geometries;

import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.MatrixReader;
import dk.itu.raven.io.MockedShapefileReader;
import dk.itu.raven.io.RasterReader;
import dk.itu.raven.io.ShapefileReader;
import dk.itu.raven.io.TFWFormat;
import dk.itu.raven.join.AbstractRavenJoin;
import dk.itu.raven.join.JoinResult;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.util.matrix.RandomMatrix;

public class ApiTest {
    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    public void offsetSurroundingPolygonTest(int streamed) throws IOException {
        Polygon p = new Polygon(Arrays.asList(Geometries.point(-10, -10), Geometries.point(20, -10),
                Geometries.point(20, 20), Geometries.point(-10, 20), Geometries.point(-10, -10), Geometries.point(0, 0),
                Geometries.point(10, 0), Geometries.point(10, 10), Geometries.point(0, 10), Geometries.point(0, 0)));
        Matrix m = new RandomMatrix(10, 10, 10, 10);

        ShapefileReader vectorReader = new MockedShapefileReader(List.of(p));
        RasterReader rasterReader = new MatrixReader(m, new TFWFormat(1, 0, 0, -1, 0, 0));
        JoinResult result = getResult(streamed, vectorReader, rasterReader);

        int sum = 0;
        for (var item : result) {
            sum += item.pixelRanges.size();
        }

        assertEquals(0, sum);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    public void offsetRasterCoverPolygonTest(int streamed) throws IOException {
        Polygon p = new Polygon(Arrays.asList(Geometries.point(5, 5), Geometries.point(10, 5), Geometries.point(10, 10),
                Geometries.point(5, 10)));
        Matrix m = new RandomMatrix(10, 15, 15, 10);

        ShapefileReader vectorReader = new MockedShapefileReader(List.of(p));
        RasterReader rasterReader = new MatrixReader(m, new TFWFormat(1, 0, 0, -1, 0, 0));

        JoinResult result = getResult(streamed, vectorReader, rasterReader);

        assertIncluded(15, 15, 5, 5, 9, 9, result);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    public void offsetPolygonCoverRasterTest(int streamed) throws IOException {
        Polygon p = new Polygon(
                Arrays.asList(Geometries.point(-5, -5), Geometries.point(15, -5), Geometries.point(15, 15),
                        Geometries.point(-5, 15)));
        Matrix m = new RandomMatrix(10, 10, 10, 10);

        ShapefileReader vectorReader = new MockedShapefileReader(List.of(p));
        RasterReader rasterReader = new MatrixReader(m, new TFWFormat(1, 0, 0, -1, 0, 0));
        JoinResult result = getResult(streamed, vectorReader, rasterReader);

        assertIncluded(10, 10, 0, 0, 9, 9, result);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    public void offsetTopLeftPolygonTest(int streamed) throws IOException {
        Polygon p = new Polygon(Arrays.asList(Geometries.point(-5, -5), Geometries.point(5, -5), Geometries.point(5, 5),
                Geometries.point(-5, 5)));
        Matrix m = new RandomMatrix(10, 10, 10, 10);

        ShapefileReader vectorReader = new MockedShapefileReader(List.of(p));
        RasterReader rasterReader = new MatrixReader(m, new TFWFormat(1, 0, 0, -1, 0, 0));
        JoinResult result = getResult(streamed, vectorReader, rasterReader);

        assertIncluded(10, 10, 0, 0, 4, 4, result);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    public void offsetBottomRightPolygonTest(int streamed) throws IOException {
        Polygon p = new Polygon(Arrays.asList(Geometries.point(5, 5), Geometries.point(15, 5), Geometries.point(15, 15),
                Geometries.point(5, 15)));
        Matrix m = new RandomMatrix(10, 10, 10, 10);

        ShapefileReader vectorReader = new MockedShapefileReader(List.of(p));
        RasterReader rasterReader = new MatrixReader(m, new TFWFormat(1, 0, 0, -1, 0, 0));
        JoinResult result = getResult(streamed, vectorReader, rasterReader);

        assertIncluded(10, 10, 5, 5, 9, 9, result);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    public void offsetTopRightPolygonTest(int streamed) throws IOException {
        Polygon p = new Polygon(Arrays.asList(Geometries.point(5, -5), Geometries.point(15, -5),
                Geometries.point(15, 5), Geometries.point(5, 5)));
        Matrix m = new RandomMatrix(10, 10, 10, 10);

        ShapefileReader vectorReader = new MockedShapefileReader(List.of(p));
        RasterReader rasterReader = new MatrixReader(m, new TFWFormat(1, 0, 0, -1, 0, 0));
        JoinResult result = getResult(streamed, vectorReader, rasterReader);

        assertIncluded(10, 10, 5, 0, 9, 4, result);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    public void offsetBottomLeftPolygonTest(int streamed) throws IOException {
        Polygon p = new Polygon(Arrays.asList(Geometries.point(-5, 5), Geometries.point(5, 5), Geometries.point(5, 15),
                Geometries.point(-5, 15)));
        Matrix m = new RandomMatrix(10, 10, 10, 10);

        ShapefileReader vectorReader = new MockedShapefileReader(List.of(p));
        RasterReader rasterReader = new MatrixReader(m, new TFWFormat(1, 0, 0, -1, 0, 0));
        JoinResult result = getResult(streamed, vectorReader, rasterReader);

        assertIncluded(10, 10, 0, 5, 4, 9, result);
    }

    private void assertIncluded(int width, int height, int minX, int minY, int maxX, int maxY, JoinResult result) {
        int sum = 0;
        boolean[][] pixelIncluded = new boolean[width][height];
        for (var item : result) {
            for (PixelRange range : item.pixelRanges) {
                for (int x = range.x1; x <= range.x2; x++) {
                    sum++;
                    pixelIncluded[x][range.row] = true;
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

    private JoinResult getResult(int streamed, ShapefileReader vectorReader, RasterReader rasterReader)
            throws IOException {
        AbstractRavenJoin join;
        if (streamed == 0) {
            join = getStreamedJoin(rasterReader, vectorReader, 4, 4, false);
        } else if (streamed == 1) {
            join = getStreamedJoin(rasterReader, vectorReader, 4, 4, true);
        } else {
            join = getJoin(rasterReader, vectorReader);
        }
        JoinResult result = join.join().asMemoryAllocatedResult();
        return result;
    }
}
