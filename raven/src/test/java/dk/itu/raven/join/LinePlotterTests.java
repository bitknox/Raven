package dk.itu.raven.join;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.util.Assert;

import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Point;

import dk.itu.raven.ksquared.IntPointer;

public class LinePlotterTests {

    // @Test
    public void testAllDiagonals() {

        for (int deg = 0; deg < 360; deg += 5) {
            ArrayList<ArrayList<Integer>> matrix = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                ArrayList<Integer> list = new ArrayList<>();
                for (int j = 0; j < 20; j++) {
                    list.add(0);
                }
                matrix.add(list);

            }
            double rad1 = Math.toRadians(deg);
            double rad2 = Math.toRadians(deg + 180);
            int x1 = (int) Math.floor((Math.cos(rad1) * 10));
            int y1 = (int) (Math.sin(rad1) * 10);
            int x2 = (int) Math.floor((Math.cos(rad2) * 10));
            int y2 = (int) (Math.sin(rad2) * 10);
            System.out.println(x1 + " " + y1 + " " + x2 + " " + y2);
            LinePlotter.plotLine((x, y) -> {
                matrix.get(y + 10).set(x + 10, 1);
            }, x1, y1, x2, y2, -10, -10, 10, 9);

            for (int i = 0; i < 20; i++) {
                for (int j = 0; j < 20; j++) {
                    int val = matrix.get(i).get(j);
                    if (val == 0) {
                        System.out.print("-");
                    } else {
                        System.out.print("#");
                    }
                }
                System.out.println();
            }

            System.out.println();
        }

    }

    @Test
    public void testConnectedLines() {

        Random r = new Random(6);
        for (int num = 0; num < 10; num++) {
            ArrayList<ArrayList<Integer>> matrix = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                ArrayList<Integer> list = new ArrayList<>();
                for (int j = 0; j < 20; j++) {
                    list.add(0);
                }
                matrix.add(list);

            }

            int x1 = r.nextInt(20);
            int y1 = r.nextInt(20);
            int x2 = r.nextInt(20);
            int y2 = r.nextInt(20);
            int x3 = r.nextInt(20);
            int y3 = r.nextInt(20);

            int maxY1 = Math.max(y1, y2);
            int minY1 = Math.min(y1, y2);
            int maxY2 = Math.max(y2, y3);
            int minY2 = Math.min(y2, y3);

            System.out.println("x1 " + x1 + " y1 " + y1);
            System.out.println("x2 " + x2 + " y2 " + y2);
            System.out.println("x3 " + x3 + " y3 " + y3);

            LinePlotter.plotLine((x, y) -> {
                matrix.get(y).set(x, matrix.get(y).get(x) + 5);
            }, x1, y1, x2, y2, 0, minY1 + 1, 20, maxY1);
            LinePlotter.plotLine((x, y) -> {
                matrix.get(y).set(x, matrix.get(y).get(x) + 1);
            }, x2, y2, x3, y3, 0, minY2 + 1, 20, maxY2);

            for (int i = 0; i < 20; i++) {
                for (int j = 0; j < 20; j++) {
                    int val = matrix.get(i).get(j);
                    if (val == 0) {

                        System.out.print("-");
                    } else {

                        System.out.print(val);
                    }
                }
                System.out.print(i);
                System.out.println();
            }

            System.out.println();
        }

    }

    // @Test
    public void testSpeed() {
        Random r = new Random();

        for (int j = 0; j < 10; j++) {
            ArrayList<Point> points = new ArrayList<>();

            for (int i = 0; i < 31_000_000; i++) {
                points.add(Geometries.point(r.nextDouble() * 100, r.nextDouble() * 100));
            }

            long start = System.currentTimeMillis();

            IntPointer sum = new IntPointer();
            Point old = points.get(0);
            for (int i = 1; i < points.size(); i++) {
                Point next = points.get(i);
                double a = (next.y() - old.y());
                double aInv = 1.0 / a;
                double b = (old.x() - next.x());
                double c = a * old.x() + b * old.y();

                int minY = (int) Math.round(Math.min(old.y(), next.y()));
                int maxY = (int) Math.round(Math.max(old.y(), next.y()));

                // // compute all intersections between the line segment and horizontal pixel
                // lines
                for (int y = minY; y < maxY; y++) {
                    double x = (c - b * (y + 0.5)) * aInv;
                    int ix = (int) Math.floor(x);
                    sum.val += ix;
                }

                old = next;
            }

            long end = System.currentTimeMillis();

            System.out.println("normal " + (end - start));

            start = System.currentTimeMillis();

            old = points.get(0);
            for (int i = 1; i < points.size(); i++) {
                Point next = points.get(i);

                int minY = (int) Math.round(Math.min(old.y(), next.y()));
                int maxY = (int) Math.round(Math.max(old.y(), next.y()));

                int x0 = (int) (old.x());
                int y0 = (int) (old.y());
                int x1 = (int) (next.x());
                int y1 = (int) (next.y());

                LinePlotter.plotLine((x, y) -> {
                    int ix = x;
                    int yx = y;
                    sum.val += ix;
                }, x0, y0, x1, y1, 0, minY, 100,
                        maxY - 1);

                old = next;

            }

            end = System.currentTimeMillis();

            System.out.println("other " + (end - start));

        }
    }

    // @Test
    public void testCorrectness() {
        Random r = new Random(4);
        for (int num = 0; num < 10; num++) {
            final int number = num;

            double x0 = r.nextDouble() * 100;
            double y0 = r.nextDouble() * 100;
            double x1 = r.nextDouble() * 100;
            double y1 = r.nextDouble() * 100;

            double dy = (y1 - y0);
            double c = (y0 * (x1 - x0) - dy * x0);

            double ap = (y0 - y1);
            // double aInv = 1.0 / a;
            double bp = (x1 - x0);
            double cp = ap * x1 + bp * y1;

            double a = -dy;
            double aInv = 1.0 / a;
            double b = (x1 - x0);

            int minY = (int) Math.round(Math.min(y0, y1));
            int maxY = (int) Math.round(Math.max(y0, y1));

            ArrayList<Integer> result = new ArrayList<>();
            ArrayList<Integer> expected = new ArrayList<>();

            expected.add((int) Math.floor((c - b * (minY + 0.5)) * aInv));
            expected.add((int) Math.floor((c - b * ((maxY - 1) + 0.5)) * aInv));

            int x0i = (int) Math.floor(x0);
            int y0i = (int) Math.round(y0);
            int x1i = (int) Math.floor(x1);
            int y1i = (int) Math.round(y1);

            System.out.println(
                    "minY: " + minY + " maxY: " + maxY + " x0 " + x0 + " y0 " + y0 + " x1 " + x1 + " y1 " + y1);

            LinePlotter.plotLine((x, y) -> {
                System.out.println("x: " + x + " y: " + y + " minY: " + minY + " maxY: " + maxY);
                if (y == minY) {
                    result.add(x);
                }
                if (y == maxY) {
                    result.add(x);
                }
            }, x0i, y0i, x1i, y1i, 0, minY, 100,
                    maxY);

            expected.sort((a1, a2) -> a2 - a1);
            result.sort((a1, a2) -> a2 - a1);
            System.out.println(expected.size());
            System.out.println(result.size());
            Assert.equals(expected.get(0), result.get(0),
                    "1) NUM: " + number + " x0 " + x0 + " y0 " + y0 + " x1 " + x1 + " y1 " + y1);

            Assert.equals(expected.get(1), result.get(1),
                    "2) NUM: " + number + "x0 " + x0 + " y0 " + y0 + " x1 " + x1 + " y1 " + y1);
        }
    }

    // @Test
    public void testDirection() {
        double x0 = 0.7;
        double y0 = 0.7;
        double x1 = 10.4;
        double y1 = 10.4;

        int x0i = (int) Math.floor(x0);
        int y0i = (int) Math.round(y0);
        int x1i = (int) Math.floor(x1);
        int y1i = (int) Math.round(y1);

        int minY = 4;
        int maxY = 10;

        LinePlotter.plotLine((x, y) -> {
            System.out.println("x: " + x + " y: " + y);
        }, x0i, y0i, x1i, y1i, 0, minY, 100,
                maxY);

        LinePlotter.plotLine((x, y) -> {
            System.out.println("x: " + x + " y: " + y);
        }, x1i, y1i, x0i, y0i, 0, minY, 100,
                maxY);

    }
}
