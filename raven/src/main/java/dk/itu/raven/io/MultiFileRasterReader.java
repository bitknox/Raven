package dk.itu.raven.io;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.io.cache.CachedRasterStructure;
import dk.itu.raven.io.cache.RasterCache;
import dk.itu.raven.join.SpatialDataChunk;
import dk.itu.raven.util.matrix.Matrix;

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
		for (File file : directory.listFiles()) {
			if (file.getName().endsWith(".tif") || file.getName().endsWith(".tiff")) {
				// create a new FileRasterReader for each tiff file
				RasterReader r = new ImageIORasterReader(file);
				TFWFormat transform = r.getTransform();

				// check if the tile is the top left tile so far.
				if (transform.topLeftX < this.transform.topLeftX && transform.topLeftY > this.transform.topLeftY) {
					this.transform = transform;
				}

			}
		}
	}

	public Stream<SpatialDataChunk> rasterPartitionStream(int widthStep, int heightStep,
			Optional<RasterCache<CachedRasterStructure>> cache, RTree<String, Geometry> rtree) throws IOException {
		ArrayList<RasterWindow> windows = new ArrayList<>();
		for (RasterReader reader : readers) {
			// Create tiles for each raster file, and add them to the windows list
			// we cannot rely on the rasterReader partitionStream method, as this will
			// create joinchunks
			// which will not be able to be used in the parallel join.
			ImageMetadata metadata = reader.getImageMetadata();
			TFWFormat transform = reader.getTransform();

			int startX = (int) transform.topLeftX;
			int startY = (int) transform.topLeftY;
			int endX = metadata.getWidth();
			int endY = metadata.getHeight();
			for (int y = startY; y < endY; y += heightStep) {
				for (int x = startX; x < endX; x += widthStep) {
					// calculate the offset
					RasterWindow window = new RasterWindow(reader,
							new Rectangle(x, y, Math.min(endX, x + widthStep) - x, Math.min(endY, y + heightStep) - y), null);
					windows.add(window);
				}
			}

		}

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
