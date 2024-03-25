package dk.itu.raven.join;

import java.util.Optional;

import dk.itu.raven.geometry.Offset;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.ShapefileReader;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.matrix.Matrix;

public class SpatialDataChunk {
    private Matrix matrix;
    private Pair<Iterable<Polygon>, ShapefileReader.ShapeFileBounds> geometries;
    private Optional<String> cacheKey = Optional.empty();

    private Offset<Integer> offset;
    private Offset<Integer> globalOffset;

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

    public void setGlobalOffset(Offset<Integer> globalOffset) {
        this.globalOffset = globalOffset;
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

    public Offset<Integer> getGlobalOffset() {
        return this.globalOffset;
    }

    public String getCacheKeyName() {
        return offset.getX() + "-" + offset.getY() + ".raven";
    }

    public Optional<String> getCacheKey() {
        return cacheKey;
    }
}
