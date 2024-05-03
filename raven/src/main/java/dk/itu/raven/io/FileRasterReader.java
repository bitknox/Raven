package dk.itu.raven.io;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.coverage.grid.io.imageio.geotiff.PixelScale;
import org.geotools.coverage.grid.io.imageio.geotiff.TiePoint;
import org.geotools.gce.geotiff.GeoTiffReader;

import dk.itu.raven.io.GeoTiff.GeoTiffMetadata;
import dk.itu.raven.io.GeoTiff.GeoTiffMetadata2CRSAdapter;
import dk.itu.raven.util.Logger;

public abstract class FileRasterReader extends RasterReader {
	File tiff;
	File tfw;
	File directory;

	CoordinateReferenceSystem crs;
	TFWFormat g2m;

	public FileRasterReader(File directory) throws IOException {
		this.directory = directory;
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

		GeoTiffMetadata metadata = readGeoTiffMetadata();

		setCRS(metadata);
		setG2M(metadata);
	}

	private GeoTiffMetadata readGeoTiffMetadata() {
		GeoTiffMetadata metadata = null;
		try {
			metadata = new GeoTiffMetadata(new GeoTiffReader(tiff));
		} catch (Exception e) {
			Logger.log("Cannot read metadata from " + tiff.getName(), Logger.LogLevel.DEBUG);
		}
		return metadata;
	}

	private void setCRS(GeoTiffMetadata metadata) {
		var adapter = new GeoTiffMetadata2CRSAdapter(null);
		try {
			crs = adapter.createCoordinateSystem(metadata);
		} catch (Exception e) {
			Logger.log("(Tiff) Cannot create CRS from metadata, using default CRS", Logger.LogLevel.DEBUG);
			crs = adapter.DefaultCRS;
		}
	}

	private void setG2M(GeoTiffMetadata metadata) throws IOException {
		if (metadata != null && metadata.hasPixelScales() && metadata.hasTiePoints()) {
			PixelScale pixelScale = metadata.getModelPixelScales();
			TiePoint[] tiePoint = metadata.getModelTiePoints();

			double sx = pixelScale.getScaleX();
			double sy = pixelScale.getScaleY();

			double tx = tiePoint[0].getValueAt(3) - tiePoint[0].getValueAt(0);
			double ty = tiePoint[0].getValueAt(4) - tiePoint[0].getValueAt(1);

			g2m = new TFWFormat(sx, 0, 0, -sy, tx, ty);
		} else if (tfw != null) {
			Logger.log("Reading TFW file because the metadata had no model to grid information", Logger.LogLevel.DEBUG);
			g2m = TFWFormat.read(tfw);
		} else {
			throw new UnsupportedOperationException("no side-car or inline TFW data found for tile: " + tiff.getPath());
		}

	}

	public TFWFormat getG2M() {
		return g2m;
	}

	public CoordinateReferenceSystem getCRS() {
		return crs;
	}

	@Override
	public Optional<String> getDirectoryName() {
		return Optional.of(directory.getName());
	}

	@Override
	public Optional<File> getDirectory() {
		return Optional.of(directory);
	}
}
