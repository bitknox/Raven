package dk.itu.raven.join;

import java.util.Collection;

import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.join.results.IResult;

public class JoinResultItem {
    public Geometry geometry;
    public Collection<IResult> pixelRanges;

    public JoinResultItem(Geometry geometry, Collection<IResult> pixelRanges) {
        this.geometry = geometry;
        this.pixelRanges = pixelRanges;
    }
}
