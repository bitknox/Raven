package dk.itu.raven.io;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.SpatialDataChunk;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.util.matrix.RastersMatrix;
import mil.nga.tiff.FileDirectory;
import mil.nga.tiff.ImageWindow;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TIFFImage;
import mil.nga.tiff.TiffReader;

public class MilRasterReader extends FileRasterReader {

	public MilRasterReader(File directory) throws IOException {
		super(directory);
		if (tfw == null)
			throw new IOException("no TFW file found");
	}

	@Override
	public Matrix readRasters(Rectangle rect) throws IOException {
		TIFFImage image = TiffReader.readTiff(tiff);
		FileDirectory directory = image.getFileDirectory();
		int imageWidth = directory.getImageWidth().intValue();
		int imageHeight = directory.getImageHeight().intValue();
		Rasters rasters;

		int minX = (int) Math.max(rect.x1(), 0.0);
		int minY = (int) Math.max(rect.y1(), 0.0);
		int maxX = (int) Math.ceil(Math.min(rect.x2(), imageWidth));
		int maxY = (int) Math.ceil(Math.min(rect.y2(), imageHeight));

		ImageWindow window = new ImageWindow(minX, minY, maxX, maxY);
		rasters = directory.readRasters(window);

		Matrix matrix = new RastersMatrix(rasters);

		return matrix;
	}

	@Override
	public Stream<SpatialDataChunk> streamRasters(Rectangle rect) throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'streamRasters'");
	}

}
