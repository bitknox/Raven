package dk.itu.raven.geometry;

import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Point;
import com.github.davidmoten.rtree2.geometry.Rectangle;

public class FeatureGeometry
        implements com.github.davidmoten.rtree2.geometry.Geometry, Iterator<Coordinate>, Iterable<Coordinate> {
    private Geometry feature;
    private Coordinate[] coordinates;
    private Rectangle mbr;
    private int currentIteratorIndex;

    public FeatureGeometry(Geometry geometry) {
        this.feature = geometry;
        setCoordinates(geometry);
        setMbr(geometry);
    }

    public FeatureGeometry(Polygon polygon) {
        Geometry feature = new GeometryFactory().createPolygon(polygon.getCoordinates());
        setCoordinates(feature);
        setMbr(feature);
    }

    public FeatureGeometry(Coordinate[] coordinates) {
        this.feature = new GeometryFactory().createPolygon(coordinates);
        setCoordinates(feature);
        setMbr(feature);
    }

    public FeatureGeometry(List<Point> points) {
        Coordinate[] coordinates = new Coordinate[points.size() + 1];
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            coordinates[i] = new Coordinate(p.x(), p.y());
        }
        coordinates[points.size()] = new Coordinate(points.get(0).x(), points.get(0).y());
        this.feature = new GeometryFactory().createPolygon(coordinates);
        setCoordinates(feature);
        setMbr(feature);
    }

    @Override
    public Iterator<Coordinate> iterator() {
        currentIteratorIndex = 1;
        return this;
    }

    @Override
    public boolean hasNext() {
        return currentIteratorIndex <= coordinates.length;
    }

    @Override
    public Coordinate next() {
        return coordinates[currentIteratorIndex++
                % coordinates.length];
    }

    @Override
    public double distance(Rectangle r) {
        return mbr.distance(r);
    }

    @Override
    public Rectangle mbr() {
        return mbr;
    }

    @Override
    public boolean intersects(Rectangle r) {
        return mbr.intersects(r);
    }

    @Override
    public boolean isDoublePrecision() {
        return true;
    }

    public Coordinate getFirst() {
        return coordinates[0];
    }

    public Coordinate getPoint(int index) {
        return coordinates[index % coordinates.length];
    }

    public int size() {
        return coordinates.length;
    }

    private void setMbr(Geometry feature) {
        Envelope envelope = feature.getEnvelopeInternal();
        this.mbr = Geometries.rectangle(envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());
    }

    private void setCoordinates(Geometry feature) {
        this.coordinates = feature.getCoordinates();
    }
}
