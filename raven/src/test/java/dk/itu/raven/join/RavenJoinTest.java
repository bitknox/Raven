package dk.itu.raven.join;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Point;

import dk.itu.raven.geometry.FeatureGeometry;
import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.geometry.Size;
import dk.itu.raven.ksquared.AbstractK2Raster;
import dk.itu.raven.ksquared.K2RasterBuilder;
import dk.itu.raven.util.matrix.ArrayMatrix;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.util.matrix.RandomMatrix;

public class RavenJoinTest {
    @Test
    public void testExtractCellsPolygon() {
        List<Point> points = new ArrayList<>();
        points.add(Geometries.point(0, 0));
        points.add(Geometries.point(10, 10));
        points.add(Geometries.point(20, 0));
        points.add(Geometries.point(20, 30));
        points.add(Geometries.point(10, 20));
        points.add(Geometries.point(0, 30));
        Polygon poly = new Polygon(points);

        java.awt.Rectangle rect = new java.awt.Rectangle(0, 0, 20, 30);
        Matrix matrix = new RandomMatrix(20, 30, 2);

        for (int i = 2; i <= 10; i++) {
            AbstractK2Raster k2 = new K2RasterBuilder().build(matrix, i);
            RavenJoin join = new RavenJoin(k2, null, new Size(20, 30));
            Collection<PixelRange> ranges = join.extractCellsPolygon(poly, 0, rect, false);

            assertTrue(ranges.stream().anyMatch(pr -> pr.row == 2 && pr.x1 == 0 && pr.x2 == 1));
            assertFalse(ranges.stream().anyMatch(pr -> pr.row == 2 && pr.x1 == 2));
            assertTrue(ranges.stream().anyMatch(pr -> pr.row == 3 && pr.x1 == 0 && pr.x2 == 2));
            assertTrue(ranges.stream().anyMatch(pr -> pr.row == 2 && pr.x1 == 17 && pr.x2 == 19));
        }
    }

    @Test
    public void testExtractCellsPolygonWithLine() {
        List<Point> points = new ArrayList<>();
        points.add(Geometries.point(1, 1));
        points.add(Geometries.point(10, 10));
        Polygon poly = new Polygon(points);
        java.awt.Rectangle rect = new java.awt.Rectangle(0, 0, 11, 11);
        Matrix matrix = new RandomMatrix(10, 10, 2);
        for (int i = 2; i <= 10; i++) {
            AbstractK2Raster k2 = new K2RasterBuilder().build(matrix, i);
            RavenJoin join = new RavenJoin(k2, null, new Size(11, 11));
            Collection<PixelRange> ranges = join.extractCellsPolygon(poly, 0, rect, false);

            assertEquals(9, ranges.size());
            assertTrue(ranges.stream().anyMatch(pr -> pr.row == 1));

            int j = 1;
            for (PixelRange range : ranges) {
                assertEquals(new PixelRange(j, j, j), range);
                j++;
            }

            for (j = 1; j < ranges.size(); j++) {
                final int k = j;
                assertTrue(ranges.stream().anyMatch(pr -> pr.row == k && pr.x1 == k && pr.x2 == k));
            }
            assertFalse(ranges.stream().anyMatch(pr -> pr.row == 10 && pr.x1 == 10 && pr.x2 == 9));
        }
    }

    @Test
    public void testRavenJoin() {
        int[][] matrix = new int[16][16];
        int fillValue = 42; // You can change this to any integer value
        for (int i = 0; i < 16; i++)
            for (int j = 0; j < 16; j++)
                matrix[i][j] = fillValue;
        matrix[6][6] = 0;
        for (int k = 2; k <= 10; k++) {
            AbstractK2Raster k2 = new K2RasterBuilder().build(new ArrayMatrix(matrix, 16, 16), k);

            RTree<String, Geometry> rtree = RTree.star().maxChildren(6).create();
            Polygon p = new Polygon(new Coordinate[] { new Coordinate(1, 1), new Coordinate(3, 1), new Coordinate(3, 3),
                    new Coordinate(1, 3) });
            Polygon p2 = new Polygon(
                    new Coordinate[] { new Coordinate(5, 5), new Coordinate(10, 5), new Coordinate(10, 10),
                            new Coordinate(5, 10) });
            PixelRange[] expectedRanges = new PixelRange[] { new PixelRange(1, 1, 2), new PixelRange(2, 1, 2) };
            PixelRange[] expectedRanges2 = new PixelRange[] { new PixelRange(5, 5, 9), new PixelRange(6, 5, 5),
                    new PixelRange(6, 7, 9), new PixelRange(7, 5, 9), new PixelRange(8, 5, 9),
                    new PixelRange(9, 5, 9) };
            rtree = rtree.add(null, p);
            rtree = rtree.add(null, p2);

            RavenJoin join = new RavenJoin(k2, rtree, new Size(16, 16));
            JoinResult res = join.join(JoinFilterFunctions.rangeFilter(42, 42)).asMemoryAllocatedResult();

            assertEquals(res.get(0).geometry, p);
            assertEquals(res.get(1).geometry, p2);

            boolean[][] expected = new boolean[16][16];
            for (PixelRange range : expectedRanges) {
                for (int x = range.x1; x <= range.x2; x++) {
                    expected[x][range.row] = true;
                }
            }

            boolean[][] expected2 = new boolean[16][16];
            for (PixelRange range : expectedRanges2) {
                for (int x = range.x1; x <= range.x2; x++) {
                    expected2[x][range.row] = true;
                }
            }

            boolean[][] actual = new boolean[16][16];
            for (PixelRange range : res.get(0).pixelRanges) {
                for (int x = range.x1; x <= range.x2; x++) {
                    actual[x][range.row] = true;
                }
            }

            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    assertEquals(expected[i][j], actual[i][j], "index: " + i + ", " + j);
                }
            }

            boolean[][] actual2 = new boolean[16][16];
            for (PixelRange range : res.get(1).pixelRanges) {
                for (int x = range.x1; x <= range.x2; x++) {
                    actual2[x][range.row] = true;
                }
            }

            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    assertEquals(expected2[i][j], actual2[i][j], "index: " + i + ", " + j);
                }
            }
        }
    }
}
