package dk.itu.raven;

import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.ShapefileReader;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.matrix.Matrix;

public class SpatialDataChunk {
    private Matrix matrix;
    private Pair<Iterable<Polygon>, ShapefileReader.ShapeFileBounds> geometries;
    private java.awt.Rectangle offset;

    public SpatialDataChunk() {
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }

    public void setGeometries(Pair<Iterable<Polygon>, ShapefileReader.ShapeFileBounds> geometries) {
        this.geometries = geometries;
    }

    public void setOffset(java.awt.Rectangle offset) {
        this.offset = offset;
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public Pair<Iterable<Polygon>, ShapefileReader.ShapeFileBounds> getGeometries() {
        return geometries;
    }

    public java.awt.Rectangle getOffset() {
        return offset;
    }
}
