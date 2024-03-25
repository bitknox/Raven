package dk.itu.raven.io;

import java.io.IOException;
import java.util.List;

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
        return new VectorData(polygons,
                bounds, null);
    }
}
