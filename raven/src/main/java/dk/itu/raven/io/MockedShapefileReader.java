package dk.itu.raven.io;

import java.io.IOException;
import java.util.List;

import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.util.Pair;

public class MockedShapefileReader extends ShapefileReader {
    private List<Polygon> polygons;

    public MockedShapefileReader(List<Polygon> polygons) {
        super("", null);
        for (Polygon p : polygons) {
            this.bounds.updateBounds(p.mbr().x1(), p.mbr().y1(), p.mbr().x2(), p.mbr().y2());
        }
        this.polygons = polygons;
    }

    @Override
    public Pair<List<Polygon>, ShapeFileBounds> readShapefile() throws IOException {
        return new Pair<List<Polygon>, ShapefileReader.ShapeFileBounds>(polygons, bounds);
    }
}
