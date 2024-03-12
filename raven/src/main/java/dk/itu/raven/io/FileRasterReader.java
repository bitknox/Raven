package dk.itu.raven.io;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;

import dk.itu.raven.geotools.GeoTiffIIOMetadataDecoder;
import dk.itu.raven.geotools.PixelScale;
import dk.itu.raven.geotools.TiePoint;

public abstract class FileRasterReader extends RasterReader {
	File tiff;
	File tfw;

	TFWFormat transform;

	public FileRasterReader(File directory) throws IOException {
		for (File file : directory.listFiles()) {

			if (file.getName().endsWith(".tif") ||
					file.getName().endsWith(".tiff")) {
				tiff = file;
			}
			if (file.getName().endsWith(".tfw")) {
				tfw = file;
			}
		}
		if (tiff == null) {
			throw new IOException("Missing tiff file");
		}
		this.setCacheKey(tiff.getName());
		if (tfw != null) {
			transform = TFWFormat.read(tfw);
		} else {

			FileImageInputStream stream = new FileImageInputStream(tiff);
			ImageReader reader = ImageIO.getImageReaders(stream).next();
			reader.setInput(stream);
			GeoTiffIIOMetadataDecoder metadata = new GeoTiffIIOMetadataDecoder(reader.getImageMetadata(0));
			for (TiePoint tiePoint : metadata.getModelTiePoints()) {
				System.out.println(tiePoint);
			}

			if (metadata.hasModelTrasformation()) {
				System.out.println(metadata.getModelTransformation());
			}

			System.out.println(metadata.getGeographicCitation().getGcsName());

			PixelScale pixelScale = metadata.getModelPixelScales();
			System.out.println(pixelScale);
			TiePoint[] tiePoint = metadata.getModelTiePoints();

			if (pixelScale == null || tiePoint == null || tiePoint.length < 1) {
				throw new UnsupportedOperationException("no side-car or inline TFW data found");
			}

			if (tiePoint[0].getValueAt(0) != 0 || tiePoint[0].getValueAt(1) != 0) {
				throw new UnsupportedOperationException("first tie point is not the top left coordinates");
			}

			transform = new TFWFormat(pixelScale.getScaleX(), 0, 0, -pixelScale.getScaleY(), tiePoint[0].getValueAt(3),
					tiePoint[0].getValueAt(4));
		}
	}

	public TFWFormat getTransform() {
		return transform;
	}

}
