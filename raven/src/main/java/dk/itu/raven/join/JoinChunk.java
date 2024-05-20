package dk.itu.raven.join;

import java.io.File;
import java.util.Optional;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.geometry.Offset;
import dk.itu.raven.ksquared.AbstractK2Raster;

public class JoinChunk {

    private AbstractK2Raster raster;
    private Offset<Integer> offset;

    private RTree<Object, Geometry> rtree;
    private Optional<File> directory;

    public JoinChunk(AbstractK2Raster raster, Offset<Integer> offset,
            RTree<Object, Geometry> rtree, Optional<File> directory) {
        this.raster = raster;
        this.offset = offset;
        this.rtree = rtree;
        this.directory = directory;
    }

    public AbstractK2Raster getRaster() {
        return raster;
    }

    public Offset<Integer> getOffset() {
        return offset;
    }

    public RTree<Object, Geometry> getRtree() {
        return rtree;
    }

    public Optional<File> getDirectory() {
        return this.directory;
    }
}
