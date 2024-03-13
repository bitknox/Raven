package dk.itu.raven.geometry;

import java.util.Iterator;

import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Point;
import com.github.davidmoten.rtree2.geometry.Rectangle;

public class FeatureGeometry implements Geometry, Iterator<Point>, Iterable<Point> {
    org.locationtech.jts.geom.Geometry feature;

    @Override
    public Iterator<Point> iterator() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'iterator'");
    }

    @Override
    public boolean hasNext() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasNext'");
    }

    @Override
    public Point next() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'next'");
    }

    @Override
    public double distance(Rectangle arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'distance'");
    }

    @Override
    public boolean intersects(Rectangle arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'intersects'");
    }

    @Override
    public boolean isDoublePrecision() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isDoublePrecision'");
    }

    @Override
    public Rectangle mbr() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mbr'");
    }

}
