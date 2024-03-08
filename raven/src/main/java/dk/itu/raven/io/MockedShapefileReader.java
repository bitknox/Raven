package dk.itu.raven.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.davidmoten.rtree2.Entries;
import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.util.Pair;

public class MockedShapefileReader extends ShapefileReader {
    private List<Polygon> polygons;

    public MockedShapefileReader(List<Polygon> polygons) {
        super("", null);

        this.polygons = polygons;
    }

    @Override
    public Pair<List<Polygon>, ShapeFileBounds> readShapefile() throws IOException {
        return new Pair<List<Polygon>, ShapefileReader.ShapeFileBounds>(polygons, bounds);
    }
}
