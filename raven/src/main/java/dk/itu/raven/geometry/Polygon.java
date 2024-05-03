package dk.itu.raven.geometry;

import java.util.Iterator;
import java.util.List;

import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.locationtech.jts.geom.Coordinate;

import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Point;
import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.io.TFWFormat;

/**
 * A polygon is a sequence of points that are connected by straight lines.
 * The last point is connected to the first point.
 */
public class Polygon implements Geometry, Iterator<Point>, Iterable<Point> {
    private Rectangle mbr;
    private double[] points;
    private int currentIteratorIndex;

    public Polygon(List<Point> points) {
        this.points = new double[points.size() * 2];
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        int idx = 0;
        for (Point p : points) {
            this.points[idx++] = p.x();
            this.points[idx++] = p.y();
            minX = Math.min(minX, p.x());
            maxX = Math.max(maxX, p.x());
            minY = Math.min(minY, p.y());
            maxY = Math.max(maxY, p.y());
        }
        this.mbr = Geometries.rectangle(minX, minY, maxX, maxY);
    }

    public Polygon(Coordinate[] coordinates) {
        this.points = new double[coordinates.length * 2];
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        int idx = 0;
        for (Coordinate coord : coordinates) {
            this.points[idx++] = coord.x;
            this.points[idx++] = coord.y;
            minX = Math.min(minX, coord.x);
            maxX = Math.max(maxX, coord.x);
            minY = Math.min(minY, coord.y);
            maxY = Math.max(maxY, coord.y);
            // this.points.add(p);
        }
        this.mbr = Geometries.rectangle(minX, minY, maxX, maxY);
    }

    public Polygon(Coordinate[] coordinates, TFWFormat tfw) {
        this.points = new double[coordinates.length * 2];
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        int idx = 0;
        for (Coordinate coord : coordinates) {
            this.points[idx++] = coord.x;
            this.points[idx++] = coord.y;
            minX = Math.min(minX, coord.x);
            maxX = Math.max(maxX, coord.x);
            minY = Math.min(minY, coord.y);
            maxY = Math.max(maxY, coord.y);
        }
        this.mbr = Geometries.rectangle(minX, minY, maxX, maxY);
    }

    public Polygon(double[] points, int endIndex, Rectangle mbr) {
        this.points = new double[endIndex];
        System.arraycopy(points, 0, this.points, 0, endIndex);
        this.mbr = mbr;
    }

    public void offset(double dx, double dy) {
        for (int i = 0; i < points.length; i += 2) {
            points[i] += dx;
            points[i + 1] += dy;
        }
        this.mbr = Geometries.rectangle(mbr.x1() + dx, mbr.y1() + dy, mbr.x2() + dx, mbr.y2() + dy);
    }

    private double distanceSimple(Rectangle r) {
        return mbr().distance(r);
    }

    @Override
    public double distance(Rectangle r) {
        return distanceSimple(r); // a more complex method might be needed here
    }

    @Override
    public Rectangle mbr() {
        return this.mbr;
    }

    private boolean intersectsSimple(Rectangle r) {
        return mbr().intersects(r);
    }

    @Override
    public boolean intersects(Rectangle r) {
        return intersectsSimple(r); // a more complex method might be needed here
    }

    @Override
    public boolean isDoublePrecision() {
        return true;
    }

    @Override
    public Iterator<Point> iterator() {
        this.currentIteratorIndex = 1;
        return this;
    }

    @Override
    public boolean hasNext() {
        return currentIteratorIndex <= this.points.length;
    }

    @Override
    public Point next() {
        Point point = Geometries.point(this.points[currentIteratorIndex++], this.points[currentIteratorIndex++]);
        return point;
    }

    public Point getFirst() {
        Point point = Geometries.point(this.points[0], this.points[1]);
        return point;
    }

    public int size() {
        return this.points.length;
    }

    public Point getPoint(int index) {
        if (index > this.points.length)
            throw new IndexOutOfBoundsException(index);
        index = (index * 2) % this.points.length;
        Point point = Geometries.point(this.points[index], this.points[index + 1]);// when accessing the point one
                                                                                   // past the last, give the
                                                                                   // first point instead
        return point;
    }

    public Coordinate[] getCoordinates() {
        Coordinate[] coords = new Coordinate[this.points.length + 1];
        for (int i = 0; i < this.points.length; i += 2) {
            Point point = Geometries.point(this.points[i], this.points[i + 1]);
            coords[i] = new Coordinate(point.x(), point.y());
        }
        Point point = Geometries.point(this.points[0], this.points[1]);
        coords[this.points.length] = new Coordinate(point.x(), point.y());
        return coords;
    }

    public Polygon transform(MathTransform transform) throws TransformException {
        double[] transformedPoints = new double[points.length];
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < points.length; i += 2) {
            double[] point = new double[] { points[i], points[i + 1] };
            transform.transform(point, 0, point, 0, 1);
            transformedPoints[i] = point[0];
            transformedPoints[i + 1] = point[1];
            minX = Math.min(minX, point[0]);
            maxX = Math.max(maxX, point[0]);
            minY = Math.min(minY, point[1]);
            maxY = Math.max(maxY, point[1]);
        }
        return new Polygon(transformedPoints, transformedPoints.length, Geometries.rectangle(minX, minY, maxX, maxY));
    }
}
