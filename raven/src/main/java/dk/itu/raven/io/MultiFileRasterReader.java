package dk.itu.raven.io;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.io.cache.CachedRasterStructure;
import dk.itu.raven.io.cache.RasterCache;
import dk.itu.raven.join.SpatialDataChunk;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.io.FileRasterReader;

public class MultiFileRasterReader {

	private List<RasterReader> readers = new ArrayList<RasterReader>();
	private TFWFormat transform = new TFWFormat(0, 0, 0, 0, Integer.MAX_VALUE, Integer.MIN_VALUE);

	// TODO: Figure out if reading metadata from all files synchronously is the best
	// approach,
	// or if we should just transform the metadata for the vector data to the same
	// coordinate
	// system as the raster data file.
	public MultiFileRasterReader(File directory) throws IOException {
		// find all tiff files in directory and subdirectories

		TFWFormat topLeft = Arrays.asList(directory.listFiles()).stream().map(f -> {
			if (f.isDirectory()) {
				try {
					return new ImageIORasterReader(f).getTransform();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}).filter(f -> f != null).collect(new TFWCollector());

	}

	public Matrix readRasters(Rectangle rect) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public TFWFormat getTransform() throws IOException {
		return transform;
	}

	protected ImageMetadata readImageMetadata() throws IOException {
		// we assume that all the tiff files must have the same metadata
		return readers.get(0).readImageMetadata();
	}

}
