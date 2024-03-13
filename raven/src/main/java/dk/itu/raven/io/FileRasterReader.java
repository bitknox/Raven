package dk.itu.raven.io;

import java.io.File;
import java.io.IOException;

import org.geotools.api.data.DataSourceException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.coverage.grid.io.imageio.geotiff.GeoTiffIIOMetadataDecoder;
import org.geotools.coverage.grid.io.imageio.geotiff.PixelScale;
import org.geotools.coverage.grid.io.imageio.geotiff.TiePoint;
import org.geotools.gce.geotiff.GeoTiffReader;

public abstract class FileRasterReader extends RasterReader {
	File tiff;
	File tfw;

	CoordinateReferenceSystem crs;
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

		GeoTiffReader gtiffreader = null;
		GeoTiffIIOMetadataDecoder metadata = null;
		try {
			gtiffreader = new GeoTiffReader(tiff);
			metadata = gtiffreader.getMetadata();
			this.crs = gtiffreader.getCoordinateReferenceSystem();
		} catch (DataSourceException e) {
			// TODO: handle exception
		}

		if (tfw != null) {
			transform = TFWFormat.read(tfw);
		} else if (metadata != null) {

			// TODO: Use this for reprojection

			PixelScale pixelScale = metadata.getModelPixelScales();
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

	public CoordinateReferenceSystem getCRS() {
		return crs;
	}
}
