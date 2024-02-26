package dk.itu.raven.geometry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    private List<Point> points;
    private int currentIteratorIndex;

    public Polygon(List<Point> points) {
        this.points = points;
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
        for (Point p : points) {
            minX = Math.min(minX, p.x());
            maxX = Math.max(maxX, p.x());
            minY = Math.min(minY, p.y());
            maxY = Math.max(maxY, p.y());
        }
        this.mbr = Geometries.rectangle(minX, minY, maxX, maxY);
    }

    public Polygon(Coordinate[] coordinates) {
        this.points = new ArrayList<>();
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
        for (Coordinate coord : coordinates) {
            Point p = Geometries.point(coord.x, coord.y);
            minX = Math.min(minX, p.x());
            maxX = Math.max(maxX, p.x());
            minY = Math.min(minY, p.y());
            maxY = Math.max(maxY, p.y());
            this.points.add(p);
        }
        this.mbr = Geometries.rectangle(minX, minY, maxX, maxY);
    }

    public Polygon(Coordinate[] coordinates, TFWFormat tfw) {
        this.points = new ArrayList<>();
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
        for (Coordinate coord : coordinates) {
            Point p = tfw.transFromCoordinateToPixel(coord.x, coord.y);
            minX = Math.min(minX, p.x());
            maxX = Math.max(maxX, p.x());
            minY = Math.min(minY, p.y());
            maxY = Math.max(maxY, p.y());
            this.points.add(p);
        }
        this.mbr = Geometries.rectangle(minX, minY, maxX, maxY);
    }

    public Polygon(List<Point> points, Rectangle mbr) {
        this.points = points;
        this.mbr = mbr;
    }

    public void offset(double dx, double dy) {
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            points.set(i, Geometries.point(p.x() + dx, p.y() + dy));
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
        return currentIteratorIndex <= this.points.size();
    }

    @Override
    public Point next() {
        return this.points.get((currentIteratorIndex++) % this.points.size());
    }

    public Point getFirst() {
        return points.get(0);
    }

    public int size() {
        return this.points.size();
    }

    public Point getPoint(int index) {
        if (index > this.points.size())
            throw new IndexOutOfBoundsException(index);
        return this.points.get(index % this.points.size()); // when accessing the point one past the last, give the
                                                            // first point instead
    }
}
