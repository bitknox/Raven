package dk.itu.raven.join;

import java.util.Optional;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import dk.itu.raven.geometry.Offset;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.ShapefileReader;
import dk.itu.raven.io.TFWFormat;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.matrix.Matrix;

public class SpatialDataChunk {
    private Matrix matrix;
    private Pair<Iterable<Polygon>, ShapefileReader.ShapeFileBounds> geometries;
    private Optional<String> cacheKey = Optional.empty();

    private Offset<Integer> offset;
    private CoordinateReferenceSystem crs;
    private TFWFormat g2m;

    public SpatialDataChunk() {
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = Optional.of(cacheKey);
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }

    public void setGeometries(Pair<Iterable<Polygon>, ShapefileReader.ShapeFileBounds> geometries) {
        this.geometries = geometries;
    }

    public void setOffset(Offset<Integer> offset) {
        this.offset = offset;
    }

    public void setCrs(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    public void setG2m(TFWFormat g2m) {
        this.g2m = g2m;
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public Pair<Iterable<Polygon>, ShapefileReader.ShapeFileBounds> getGeometries() {
        return geometries;
    }

    public Offset<Integer> getOffset() {
        return offset;
    }

    public String getCacheKeyName() {
        return offset.getX() + "-" + offset.getY() + ".raven";
    }

    public Optional<String> getCacheKey() {
        return cacheKey;
    }

    public CoordinateReferenceSystem getCrs() {
        return crs;
    }

    public TFWFormat getG2m() {
        return g2m;
    }
}
