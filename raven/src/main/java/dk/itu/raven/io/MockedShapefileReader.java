package dk.itu.raven.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.davidmoten.rtree2.Entries;
import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.geometry.Polygon;

public class MockedShapefileReader extends ShapefileReader {
    private List<Polygon> polygons;

    public MockedShapefileReader(List<Polygon> polygons) {
        super("");

        this.polygons = polygons;
    }

    @Override
    public VectorData readShapefile()
            throws IOException {
        List<Entry<String, Geometry>> entries = new ArrayList<>();
        for (Polygon poly : polygons) {
            entries.add(Entries.entry(null, poly));
        }
        return new VectorData(entries,
                bounds, null);
    }
}
