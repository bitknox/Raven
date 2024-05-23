package dk.itu.raven.join;

import java.io.File;
import java.util.Optional;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.geometry.Offset;
import dk.itu.raven.io.TFWFormat;
import dk.itu.raven.util.matrix.Matrix;

public class SpatialDataChunk {

    private Matrix matrix;
    private RTree<Object, Geometry> tree;

    private String name;
    private Offset<Integer> offset;
    private CoordinateReferenceSystem crs;
    private TFWFormat g2m;
    private Optional<File> directory;

    public SpatialDataChunk() {
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }

    public void setTree(RTree<Object, Geometry> tree) {
        this.tree = tree;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setDirectory(Optional<File> directory) {
        this.directory = directory;
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public RTree<Object, Geometry> getTree() {
        return tree;
    }

    public Offset<Integer> getOffset() {
        return offset;
    }

    public CoordinateReferenceSystem getCrs() {
        return crs;
    }

    public TFWFormat getG2m() {
        return g2m;
    }

    public String getName() {
        return name;
    }

    public Optional<File> getDirectory() {
        return this.directory;
    }

    public String getCacheKeyName() {
        return name + "-" + offset.getX() + "-" + offset.getY() + ".raven";
    }
}
