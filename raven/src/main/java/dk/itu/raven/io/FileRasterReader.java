package dk.itu.raven.io;

import java.io.File;
import java.io.IOException;

import org.geotools.api.data.DataSourceException;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.coverage.grid.io.imageio.geotiff.PixelScale;
import org.geotools.coverage.grid.io.imageio.geotiff.TiePoint;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.referencing.CRS;

import dk.itu.raven.io.GeoTiff.GeoTiffMetadata;
import dk.itu.raven.io.GeoTiff.GeoTiffMetadata2CRSAdapter;

public abstract class FileRasterReader extends RasterReader {
	File tiff;
	File tfw;

	CoordinateReferenceSystem crs;

	TFWFormat g2m;
	TFWFormat g2w;

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
		GeoTiffMetadata metadata = null;
		var adapter = new GeoTiffMetadata2CRSAdapter(null);
		try {
			gtiffreader = new GeoTiffReader(tiff);
			metadata = new GeoTiffMetadata(gtiffreader);
			this.crs = adapter.createCoordinateSystem(metadata);
			// metadata = gtiffreader.getMetadata();
			// this.crs = gtiffreader.getCoordinateReferenceSystem();
		} catch (DataSourceException e) {
			// TODO: handle exception
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (tfw != null) {
			g2w = TFWFormat.read(tfw);
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

			g2w = new TFWFormat(pixelScale.getScaleX(), 0, 0, -pixelScale.getScaleY(), tiePoint[0].getValueAt(3),
					tiePoint[0].getValueAt(4));
		}

		try {
			MathTransform mt = CRS.findMathTransform(crs, adapter.DefaultCRS, true);
			double[] tl = new double[2];
			double[] pixelXY = new double[2];
			double[] ref = new double[2];
			mt.transform(new double[] { g2w.topLeftX, g2w.topLeftY }, 0, tl, 0, 1);
			mt.transform(new double[] { 0, 0 }, 0, ref, 0, 1);
			mt.transform(new double[] { g2w.pixelLengthX, g2w.pixelLengthY }, 0, pixelXY, 0, 1);
			g2m = new TFWFormat(pixelXY[0] - ref[0], 0, 0, pixelXY[1] - ref[1], tl[0],
					tl[1]);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public TFWFormat getG2M() {
		return g2m;
	}

	public TFWFormat getG2W() {
		return g2w;
	}

	public CoordinateReferenceSystem getCRS() {
		return crs;
	}
}
