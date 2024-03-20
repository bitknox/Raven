package dk.itu.raven.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.geometry.Offset;
import dk.itu.raven.io.cache.CachedRasterStructure;
import dk.itu.raven.io.cache.RasterCache;
import dk.itu.raven.join.SpatialDataChunk;

public class MultiFileRasterReader implements IRasterReader {

	private Stream<ImageIORasterReader> readers;
	private TFWFormat g2m = new TFWFormat(0, 0, 0, 0, Integer.MAX_VALUE, Integer.MIN_VALUE);
	private ImageMetadata metadata;
	private CoordinateReferenceSystem crs;
	private String cacheKey;

	public MultiFileRasterReader(File directory) throws IOException {
		cacheKey = directory.getName() + "-" + "cache";
		// find all tiff files in directory and subdirectories
		List<File> files = Arrays.asList(directory.listFiles());
		ImageIORasterReader reader = new ImageIORasterReader(files.get(0));
		this.g2m = reader.getG2M();
		this.crs = reader.getCRS();
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
				TFWFormat transform = reader.getG2M();

				Offset<Integer> offset = new Offset<Integer>(
						(int) ((transform.topLeftX - this.g2m.topLeftX) / transform.pixelLengthX),
						(int) ((transform.topLeftY - this.g2m.topLeftY) / transform.pixelLengthY));

				System.out.println("Offset: " + offset.getX() + " " + offset.getY());

				return reader.rasterPartitionStream(widthStep, heightStep,
						offset, cache, rtree).parallel();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}).reduce(Stream::concat).orElse(Stream.empty());
	}

	public TFWFormat getG2M() {
		return g2m;
	}

	public CoordinateReferenceSystem getCRS() {
		return crs;
	}

	public ImageMetadata getImageMetadata() {
		// we assume that all the tiff files must have the same metadata
		return metadata;
	}
}
