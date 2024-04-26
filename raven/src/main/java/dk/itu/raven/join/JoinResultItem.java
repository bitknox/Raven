package dk.itu.raven.join;

import java.util.Collection;

import com.github.davidmoten.rtree2.geometry.Geometry;

public class JoinResultItem {
    public Geometry geometry;
    public Collection<PixelValue> pixelRanges;

    public JoinResultItem(Geometry geometry, Collection<PixelValue> pixelRanges) {
        this.geometry = geometry;
        this.pixelRanges = pixelRanges;
    }
}
