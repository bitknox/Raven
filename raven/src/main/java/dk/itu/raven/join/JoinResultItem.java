package dk.itu.raven.join;

import java.util.Collection;

import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.geometry.PixelRange;

public class JoinResultItem {
    public Geometry geometry;
    public Collection<PixelRange> pixelRanges;

    public JoinResultItem(Geometry geometry, Collection<PixelRange> pixelRanges) {
        this.geometry = geometry;
        this.pixelRanges = pixelRanges;
    }
}
