package dk.itu.raven.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.geometry.Offset;
import dk.itu.raven.io.cache.CachedRasterStructure;
import dk.itu.raven.io.cache.RasterCache;
import dk.itu.raven.join.SpatialDataChunk;

public class MultiFileRasterReader implements IRasterReader {

	private Stream<RasterReader> readers;
	private TFWFormat transform = new TFWFormat(0, 0, 0, 0, Integer.MAX_VALUE, Integer.MIN_VALUE);
	private ImageMetadata metadata;
	private String cacheKey;

	public MultiFileRasterReader(File directory) throws IOException {
		cacheKey = directory.getName() + "-" + "cache";
		// find all tiff files in directory and subdirectories
		List<File> files = Arrays.asList(directory.listFiles());
		ImageIORasterReader reader = new ImageIORasterReader(files.get(0));
		this.transform = reader.getTransform();
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

	public Stream<SpatialDataChunk> rasterPartitionStream(int widthStep, int heightStep,
			Optional<RasterCache<CachedRasterStructure>> cache, RTree<String, Geometry> rtree) throws IOException {
		return readers.map(reader -> {
			try {
				TFWFormat transform = reader.getTransform();
				return reader.rasterPartitionStream(widthStep, heightStep,
						new Offset<Integer>((int) (this.transform.topLeftX - transform.topLeftX),
								(int) (this.transform.topLeftY - transform.topLeftY)),
						cache, rtree).parallel();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}).reduce(Stream::concat).orElse(Stream.empty());
	}

	public TFWFormat getTransform() {
		return transform;
	}

	public ImageMetadata getImageMetadata() {
		// we assume that all the tiff files must have the same metadata
		return metadata;
	}
}
