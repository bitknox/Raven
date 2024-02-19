package dk.itu.raven;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.ksquared.AbstractK2Raster;

public class JoinChunk {
    private AbstractK2Raster raster;
    private RTree<String, Geometry> rtree;

    public JoinChunk(AbstractK2Raster raster, RTree<String, Geometry> rtree) {
        this.raster = raster;
        this.rtree = rtree;
    }

    public AbstractK2Raster getRaster() {
        return raster;
    }

    public RTree<String, Geometry> getRtree() {
        return rtree;
    }
}
