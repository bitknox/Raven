package dk.itu.raven;

import dk.itu.raven.geometry.Offset;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.ShapefileReader;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.matrix.Matrix;

public class SpatialDataChunk {
    private Matrix matrix;
    private Pair<Iterable<Polygon>, ShapefileReader.ShapeFileBounds> geometries;
    private Offset<Integer> offset;

    public SpatialDataChunk() {
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

    public Matrix getMatrix() {
        return matrix;
    }

    public Pair<Iterable<Polygon>, ShapefileReader.ShapeFileBounds> getGeometries() {
        return geometries;
    }

    public Offset<Integer> getOffset() {
        return offset;
    }
}
