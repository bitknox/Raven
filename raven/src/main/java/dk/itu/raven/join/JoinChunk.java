package dk.itu.raven.join;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.geometry.Offset;
import dk.itu.raven.ksquared.AbstractK2Raster;

public class JoinChunk {
    private AbstractK2Raster raster;
    private Offset<Integer> offset;
    private Offset<Integer> globalOffset;
    private RTree<String, Geometry> rtree;

    public JoinChunk(AbstractK2Raster raster, Offset<Integer> offset, Offset<Integer> globalOffset,
            RTree<String, Geometry> rtree) {
        this.raster = raster;
        this.offset = offset;
        this.globalOffset = globalOffset;
        this.rtree = rtree;
    }

    public AbstractK2Raster getRaster() {
        return raster;
    }

    public Offset<Integer> getOffset() {
        return offset;
    }

    public Offset<Integer> getGlobalOffset() {
        return globalOffset;
    }

    public RTree<String, Geometry> getRtree() {
        return rtree;
    }
}
