package dk.itu.raven.io;

import java.io.File;
import java.io.IOException;

import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.util.Logger;
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
	public ImageMetadata readImageMetadata() throws IOException {
		long start = System.currentTimeMillis();
		TIFFImage image = TiffReader.readTiff(tiff);
		long end = System.currentTimeMillis();
		Logger.log("Read tiff in " + (end - start) + "ms", Logger.LogLevel.INFO);
		FileDirectory directory = image.getFileDirectory();
		int imageWidth = directory.getImageWidth().intValue();
		int imageHeight = directory.getImageHeight().intValue();

		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'readImageMetadata'");
	}
}
