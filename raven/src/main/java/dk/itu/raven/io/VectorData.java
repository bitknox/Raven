package dk.itu.raven.io;

import java.util.List;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.io.ShapefileReader.ShapeFileBounds;

public class VectorData {
    private List<Entry<String, Geometry>> features;
    private ShapeFileBounds bounds;
    private CoordinateReferenceSystem crs;

    public VectorData(List<Entry<String, Geometry>> features, ShapeFileBounds bounds, CoordinateReferenceSystem crs) {
        this.features = features;
        this.bounds = bounds;
        this.crs = crs;
    }

    public List<Entry<String, Geometry>> getFeatures() {
        return features;
    }

    public ShapeFileBounds getBounds() {
        return bounds;
    }

    public CoordinateReferenceSystem getCRS() {
        return crs;
    }
}
