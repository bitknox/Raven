package dk.itu.raven.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.io.cache.CachedRasterStructure;
import dk.itu.raven.io.cache.RasterCache;
import dk.itu.raven.join.SpatialDataChunk;

public class MultiFileRasterReader implements IRasterReader {

	private Stream<ImageIORasterReader> readers;
	private String cacheKey;
	private ImageMetadata metadata;

	public MultiFileRasterReader(File directory) throws IOException {
		List<File> files = Arrays.asList(directory.listFiles());
		ImageIORasterReader reader = new ImageIORasterReader(files.get(0));
		this.metadata = reader.getImageMetadata();
		Stream<ImageIORasterReader> singleStream = Stream.of(reader);
		Stream<ImageIORasterReader> stream = files.subList(1, files.size()).stream().map(f -> {
			if (f.isDirectory()) {
				try {
					return new ImageIORasterReader(f);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		});

		this.readers = Stream.concat(singleStream, stream);
	}

	public Optional<String> getCacheKey() {
		return Optional.of(cacheKey);
	}

	// TODO: update signature to take a list of all polygons instead of an RTREE
	public Stream<SpatialDataChunk> rasterPartitionStream(int widthStep, int heightStep,
			Optional<RasterCache<CachedRasterStructure>> cache, RTree<String, Geometry> rtree) throws IOException {
		return readers.map(reader -> {
			try {

				// TODO: use reader.getCRS() and reader.getG2M() to remove non-overlapping
				// TODO: vector data and transform the vector data the correct crs

				reader.getCRS();
				reader.getG2M();

				return reader.rasterPartitionStream(widthStep, heightStep,
						cache, rtree).parallel();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}).reduce(Stream::concat).orElse(Stream.empty());
	}

	@Override
	public ImageMetadata getImageMetadata() throws IOException {
		return this.metadata;
	}
}
