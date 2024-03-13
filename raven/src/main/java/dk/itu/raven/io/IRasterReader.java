package dk.itu.raven.io;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.io.cache.CachedRasterStructure;
import dk.itu.raven.io.cache.RasterCache;
import dk.itu.raven.join.SpatialDataChunk;

public interface IRasterReader {

	public TFWFormat getTransform();

	public Optional<String> getCacheKey();

	public ImageMetadata getImageMetadata() throws IOException;

	public CoordinateReferenceSystem getCRS();

	public Stream<SpatialDataChunk> rasterPartitionStream(int widthStep, int heightStep,
			Optional<RasterCache<CachedRasterStructure>> cache, RTree<String, Geometry> rtree) throws IOException;
}