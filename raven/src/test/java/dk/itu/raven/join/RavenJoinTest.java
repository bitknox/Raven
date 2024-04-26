package dk.itu.raven.join;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Point;

import dk.itu.raven.geometry.Offset;
import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.geometry.Size;
import dk.itu.raven.ksquared.AbstractK2Raster;
import dk.itu.raven.ksquared.K2RasterBuilder;
import dk.itu.raven.util.Pair;
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
            join.setFunction(JoinFilterFunctions.acceptAll());
            Collection<PixelValue> ranges = join.extractCellsPolygon(poly, 0, new Square(0, 0, k2.getSize()),
                    k2.getValueRange().first, k2.getValueRange().second, rect,
                    false);

            assertTrue(ranges.stream().anyMatch(pr -> pr.y == 2 && pr.x == 0));
            assertTrue(ranges.stream().anyMatch(pr -> pr.y == 2 && pr.x == 1));
            assertFalse(ranges.stream().anyMatch(pr -> pr.y == 2 && pr.x == 2));
            assertTrue(ranges.stream().anyMatch(pr -> pr.y == 3 && pr.x == 0));
            assertTrue(ranges.stream().anyMatch(pr -> pr.y == 3 && pr.x == 2));
            assertTrue(ranges.stream().anyMatch(pr -> pr.y == 2 && pr.x == 17));
            assertTrue(ranges.stream().anyMatch(pr -> pr.y == 2 && pr.x == 19));
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
            join.setFunction(JoinFilterFunctions.acceptAll());
            Collection<PixelValue> ranges = join.extractCellsPolygon(poly, 0, new Square(0, 0, k2.getSize()),
                    k2.getValueRange().first, k2.getValueRange().second, rect, false);

            assertEquals(9, ranges.size());
            assertTrue(ranges.stream().anyMatch(pr -> pr.y == 1));
            int j;
            for (j = 1; j < 10; j++) {
                final int k = j;
                assertTrue(ranges.stream().anyMatch(pr -> pr.x == k && pr.y == k));
            }

            for (j = 1; j < ranges.size(); j++) {
                final int k = j;
                assertTrue(ranges.stream().anyMatch(pr -> pr.y == k && pr.x == k && pr.x == k));
            }
            assertFalse(ranges.stream().anyMatch(pr -> pr.y == 10 && pr.x == 10 && pr.x == 9));
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
            for (PixelValue range : res.get(0).pixelRanges) {
                actual[range.x][range.y] = true;
            }

            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    assertEquals(expected[i][j], actual[i][j], "index: " + i + ", " + j);
                }
            }

            boolean[][] actual2 = new boolean[16][16];
            for (PixelValue range : res.get(1).pixelRanges) {
                actual[range.x][range.y] = true;
            }

            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    assertEquals(expected2[i][j], actual2[i][j], "index: " + i + ", " + j);
                }
            }
        }
    }

    private Pair<Integer, Integer> getMinMax(List<PixelRange> ranges, int offsetX, int start, int end) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (PixelRange range : ranges) {
            if (range.row < start)
                continue;
            if (range.row > end)
                break;
            min = Math.min(min, range.x1 - offsetX);
            max = Math.max(max, range.x2 - offsetX);
        }
        return new Pair<Integer, Integer>(min, max);
    }

    private int countRanges(List<PixelRange> ranges, int start, int end) {
        int count = 0;
        for (PixelRange range : ranges) {
            if (range.row < start)
                continue;
            if (range.row > end)
                break;
            count++;
        }
        return count;
    }

    @Test
    public void testRangeLimitTreeBuilding() {
        List<PixelRange> ranges = new ArrayList<>();
        // int n = 2048;
        int n = 2187;
        // int k = 2;
        int k = 3;
        int min = n;
        int max = 0;
        Random r = new Random(42);
        int offsetX = r.nextInt(n);
        for (int i = 0; i < n; i++) {
            if (r.nextDouble() > 0.5)
                continue;
            int x1 = r.nextInt(n);
            int x2 = r.nextInt(n);
            if (x1 > x2) {
                int temp = x1;
                x1 = x2;
                x2 = temp;
            }
            min = Math.min(min, x1);
            max = Math.max(max, x2);

            ranges.add(new PixelRange(i, x1, x2));

        }
        RangeExtremes[] tree = RavenJoin.buildRangeLimitTree(new Offset<>(offsetX, 0), n, k, ranges).first;
        int idx = tree.length - 1;
        for (int i = 1; i <= n; i *= k) {
            for (int j = 0; j < n / i; j++) {
                Pair<Integer, Integer> expected = getMinMax(ranges, offsetX, n - (j + 1) * i, n - j * i - 1);
                int expectedMin = expected.first;
                int expectedMax = expected.second;
                int actualMax = tree[idx].x2;
                int actualMin = tree[idx].x1;
                idx--;
                assertEquals(expectedMax, actualMax);
                assertEquals(expectedMin, actualMin);
            }
        }
    }

    @Test
    public void testRangePrefixsum() {
        List<PixelRange> ranges = new ArrayList<>();
        // int n = 2048;
        int n = 2187;
        // int k = 2;
        int k = 3;
        int min = n;
        int max = 0;
        Random r = new Random(42);
        for (int i = 0; i < n; i++) {
            if (r.nextDouble() > 0.5)
                continue;
            int x1 = r.nextInt(n);
            int x2 = r.nextInt(n);
            if (x1 > x2) {
                int temp = x1;
                x1 = x2;
                x2 = temp;
            }
            min = Math.min(min, x1);
            max = Math.max(max, x2);

            ranges.add(new PixelRange(i, x1, x2));
        }

        int[] prefixsum = RavenJoin.buildRangeLimitTree(new Offset<>(0, 0), n, k, ranges).second;
        for (int start = 0; start < ranges.size(); start++) {
            for (int end = start + 1; end < ranges.size(); end++) {
                assertEquals(countRanges(ranges, start, end), prefixsum[end + 1] - prefixsum[start],
                        "start: " + start + ", end: " + end);
            }
        }
    }
}
