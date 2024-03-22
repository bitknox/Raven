package dk.itu.raven.join;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Point;

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
        AbstractK2Raster k2 = new K2RasterBuilder().build(matrix, 2);
        RavenJoin join = new RavenJoin(k2, null, new Size(20, 30));
        Collection<PixelRange> ranges = join.extractCellsPolygon(poly, 0, rect, false);

        assertTrue(ranges.stream().anyMatch(pr -> pr.row == 2 && pr.x1 == 0 && pr.x2 == 1));
        assertFalse(ranges.stream().anyMatch(pr -> pr.row == 2 && pr.x1 == 2));
        assertTrue(ranges.stream().anyMatch(pr -> pr.row == 3 && pr.x1 == 0 && pr.x2 == 2));
        assertTrue(ranges.stream().anyMatch(pr -> pr.row == 2 && pr.x1 == 17 && pr.x2 == 19));
    }

    @Test
    public void testExtractCellsPolygonWithLine() {
        List<Point> points = new ArrayList<>();
        points.add(Geometries.point(1, 1));
        points.add(Geometries.point(10, 10));
        Polygon poly = new Polygon(points);
        java.awt.Rectangle rect = new java.awt.Rectangle(0, 0, 11, 11);
        Matrix matrix = new RandomMatrix(10, 10, 2);
        AbstractK2Raster k2 = new K2RasterBuilder().build(matrix, 2);
        RavenJoin join = new RavenJoin(k2, null, new Size(11, 11));
        Collection<PixelRange> ranges = join.extractCellsPolygon(poly, 0, rect, false);

        assertEquals(9, ranges.size());
        assertTrue(ranges.stream().anyMatch(pr -> pr.row == 1));

        int i = 1;
        for (PixelRange range : ranges) {
            assertEquals(new PixelRange(i, i, i), range);
            i++;
        }

        for (i = 1; i < ranges.size(); i++) {
            final int j = i;
            assertTrue(ranges.stream().anyMatch(pr -> pr.row == j && pr.x1 == j && pr.x2 == j));
        }
        assertFalse(ranges.stream().anyMatch(pr -> pr.row == 10 && pr.x1 == 10 && pr.x2 == 9));
    }

    @RepeatedTest(100)
    public void testCombineLists() {
        Random r = new Random();
        Matrix matrix = new RandomMatrix(100, 100, 100);
        int lo = 25;
        int hi = 75;
        AbstractK2Raster k2Raster = new K2RasterBuilder().build(matrix, 2);
        RavenJoin join = new RavenJoin(k2Raster, null, new Size(100, 100));
        JoinResult def = new JoinResult();
        JoinResult prob = new JoinResult();
        List<PixelRange> initialDef = new ArrayList<>();
        def.add(new JoinResultItem(null, new ArrayList<>()));
        prob.add(new JoinResultItem(null, new ArrayList<>()));
        for (int i = 0; i < matrix.getHeight(); i++) {
            int start = r.nextInt(25);
            int end = 75 + r.nextInt(25);
            PixelRange range = new PixelRange(i, start, end);

            if (i % 2 == 0) {
                def.get(0).pixelRanges.add(range);
                initialDef.add(range);
            } else {
                prob.get(0).pixelRanges.add(range);
            }
        }

        join.combineLists(def, prob, JoinFilterFunctions.rangeFilter(lo, hi));

        for (PixelRange range : initialDef) {
            assertTrue(def.get(0).pixelRanges.contains(range));
        }

        HashSet<Long> seen = new HashSet<>();
        // check that all ranges in def are within the range of lo and hi
        for (int j = 1; j < def.size(); j++) {
            JoinResultItem item = def.get(j);
            for (PixelRange range : item.pixelRanges) {
                for (int i = range.x1; i <= range.x2; i++) {
                    long hash = range.row;
                    hash <<= 32;
                    hash += i;
                    seen.add(hash);
                    int val = matrix.get(range.row, i);
                    assertTrue(val >= lo && val <= hi);
                }
            }
        }

        // check that all pixels in prob with a value in the range are present in def
        for (PixelRange range : prob.get(0).pixelRanges) {
            for (int i = range.x1; i <= range.x2; i++) {
                int val = matrix.get(range.row, i);
                if (val <= hi && val >= lo) {
                    long hash = range.row;
                    hash <<= 32;
                    hash += i;
                    assertTrue(seen.contains(hash));
                }
            }
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

        AbstractK2Raster k2 = new K2RasterBuilder().build(new ArrayMatrix(matrix, 16, 16), 2);

        RTree<String, Geometry> rtree = RTree.star().maxChildren(6).create();
        Polygon p = new Polygon(new Coordinate[] { new Coordinate(1, 1), new Coordinate(3, 1), new Coordinate(3, 3),
                new Coordinate(1, 3) });
        Polygon p2 = new Polygon(new Coordinate[] { new Coordinate(5, 5), new Coordinate(10, 5), new Coordinate(10, 10),
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

    @Test
    public void testCombineListExtremePixelRanges() {
        Matrix matrix = new RandomMatrix(64, 64, 100);
        AbstractK2Raster k2Raster = new K2RasterBuilder().build(matrix, 2);
        RavenJoin join = new RavenJoin(k2Raster, null, new Size(64, 64));
        JoinResult def = new JoinResult();
        JoinResult prob = new JoinResult();
        List<PixelRange> initialDef = new ArrayList<>();
        def.add(new JoinResultItem(null, new ArrayList<>()));
        prob.add(new JoinResultItem(null, new ArrayList<>()));
        for (int i = 0; i < matrix.getHeight(); i++) {
            int start = 0;
            int end = 63;
            PixelRange range = new PixelRange(i, start, end);

            if (i % 2 == 0) {
                def.get(0).pixelRanges.add(range);
                initialDef.add(range);
            } else {
                prob.get(0).pixelRanges.add(range);
            }
        }

        IRasterFilterFunction function = JoinFilterFunctions.rangeFilter(0, 50);

        join.combineLists(def, prob, function);

        for (PixelRange range : initialDef) {
            assertTrue(def.get(0).pixelRanges.contains(range));
        }

        boolean[][] actual = new boolean[64][64];
        for (JoinResultItem item : def) {
            for (PixelRange range : item.pixelRanges) {
                for (int x = range.x1; x <= range.x2; x++) {
                    actual[x][range.row] = function.containsWithin(matrix.get(range.row, x), matrix.get(range.row, x));
                }
            }
        }

        for (PixelRange range : prob.get(0).pixelRanges) {
            for (int x = range.x1; x <= range.x2; x++) {
                if (function.containsWithin(matrix.get(range.row, x), matrix.get(range.row, x))) {
                    assertTrue(actual[x][range.row], "index: " + x + ", " + range.row);
                }
            }
        }
    }
}
