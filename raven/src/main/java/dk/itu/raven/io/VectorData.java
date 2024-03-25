package dk.itu.raven.io;

import java.util.List;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.ShapefileReader.ShapeFileBounds;

public class VectorData {
    private List<Polygon> features;
    private ShapeFileBounds bounds;
    private CoordinateReferenceSystem crs;

    public VectorData(List<Polygon> features, ShapeFileBounds bounds, CoordinateReferenceSystem crs) {
        this.features = features;
        this.bounds = bounds;
        this.crs = crs;
    }

    public List<Polygon> getFeatures() {
        return features;
    }

    public ShapeFileBounds getBounds() {
        return bounds;
    }

    public CoordinateReferenceSystem getCRS() {
        return crs;
    }
}
