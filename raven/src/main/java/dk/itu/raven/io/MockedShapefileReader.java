package dk.itu.raven.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dk.itu.raven.geometry.FeatureGeometry;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.util.Pair;

public class MockedShapefileReader extends ShapefileReader {
    private List<FeatureGeometry> polygons;

    public MockedShapefileReader(List<Polygon> polygons) {
        super("", null, null);

        List<FeatureGeometry> featureGeometries = new ArrayList<FeatureGeometry>();
        for (Polygon polygon : polygons) {
            featureGeometries.add(new FeatureGeometry(polygon));
        }

        this.polygons = featureGeometries;
    }

    @Override
    public Pair<List<FeatureGeometry>, ShapeFileBounds> readShapefile()
            throws IOException {
        return new Pair<List<FeatureGeometry>, ShapefileReader.ShapeFileBounds>(polygons,
                bounds);
    }
}
