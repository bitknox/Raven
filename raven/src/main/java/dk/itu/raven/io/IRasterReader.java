package dk.itu.raven.io;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.io.cache.CachedRasterStructure;
import dk.itu.raven.io.cache.RasterCache;
import dk.itu.raven.join.SpatialDataChunk;

public interface IRasterReader {

	public ImageMetadata getImageMetadata() throws IOException;

	public Stream<SpatialDataChunk> rasterPartitionStream(int widthStep, int heightStep,
			Optional<RasterCache<CachedRasterStructure>> cache, RTree<String, Geometry> rtree, VectorData vectorData)
			throws IOException;

	public Optional<String> getDirectoryName();

	public Optional<File> getDirectory();
}