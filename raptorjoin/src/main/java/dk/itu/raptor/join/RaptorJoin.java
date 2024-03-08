package dk.itu.raptor.join;

import java.util.stream.Stream;

import edu.ucr.cs.bdlab.beast.geolite.IFeature;
import edu.ucr.cs.bdlab.beast.io.shapefile.ShapefileFeatureReader;

public class RaptorJoin {

    private Stream<PixelRange> extractCellsRaven(IFeature feature, RasterMetadata metadata) {
        return null;
    }

    private Stream<PixelRange> extractCellsBeast(IFeature feature, RasterMetadata metadata) {
        return null;
    }

    public Stream<PixelRange> createFlashIndices(ShapefileFeatureReader featureReader,
            Stream<RasterMetadata> metadata) {
        return null;
    }
}
