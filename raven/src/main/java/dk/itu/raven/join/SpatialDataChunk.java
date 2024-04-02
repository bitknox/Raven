package dk.itu.raven.join;

import java.util.Optional;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.geometry.Offset;
import dk.itu.raven.io.TFWFormat;
import dk.itu.raven.util.matrix.Matrix;

public class SpatialDataChunk {
    private Matrix matrix;
    private RTree<String, Geometry> tree;
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

    public void setTree(RTree<String, Geometry> tree) {
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

    public Matrix getMatrix() {
        return matrix;
    }

    public RTree<String, Geometry> getTree() {
        return tree;
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
