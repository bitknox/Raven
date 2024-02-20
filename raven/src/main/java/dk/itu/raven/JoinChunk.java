package dk.itu.raven;

import org.checkerframework.checker.units.qual.t;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.ksquared.AbstractK2Raster;

public class JoinChunk {
    private AbstractK2Raster raster;
    private java.awt.Rectangle offset;
    private RTree<String, Geometry> rtree;

    public JoinChunk(AbstractK2Raster raster, java.awt.Rectangle offset, RTree<String, Geometry> rtree) {
        this.raster = raster;
        this.offset = offset;
        this.rtree = rtree;
    }

    public AbstractK2Raster getRaster() {
        return raster;
    }

    public java.awt.Rectangle getOffset() {
        return offset;
    }

    public RTree<String, Geometry> getRtree() {
        return rtree;
    }
}
