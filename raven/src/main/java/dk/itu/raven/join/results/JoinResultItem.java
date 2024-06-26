package dk.itu.raven.join.results;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

import com.github.davidmoten.rtree2.geometry.Geometry;

public class JoinResultItem {
    public Geometry geometry;
    public Optional<File> file;
    public Collection<IResult> pixelRanges;

    public JoinResultItem(Geometry geometry, Collection<IResult> pixelRanges, Optional<File> file) {
        this.geometry = geometry;
        this.pixelRanges = pixelRanges;
        this.file = file;
    }
}
