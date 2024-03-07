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
    private List<Entry<String, Geometry>> polygons;

    public MockedShapefileReader(List<Polygon> polygons) {
        super("", null);
        List<Entry<String, Geometry>> entries = new ArrayList<>();
        for (Polygon p : polygons) {
            this.bounds.updateBounds(p.mbr().x1(), p.mbr().y1(), p.mbr().x2(), p.mbr().y2());
            entries.add(Entries.entry("", p));
        }
        this.polygons = entries;
    }

    @Override
    public Pair<List<Entry<String, Geometry>>, ShapeFileBounds> readShapefile() throws IOException {
        return new Pair<List<Entry<String, Geometry>>, ShapefileReader.ShapeFileBounds>(polygons, bounds);
    }
}
