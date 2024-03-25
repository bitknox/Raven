package dk.itu.raven.io;

import java.awt.Rectangle;
import java.io.IOException;

import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.NoSuchAuthorityCodeException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.CRS;

import dk.itu.raven.util.matrix.ArrayMatrix;
import dk.itu.raven.util.matrix.Matrix;

public class MatrixReader extends RasterReader {
	public Matrix matrix;
	public TFWFormat transform;

	public MatrixReader(Matrix matrix, TFWFormat transform) {
		this.matrix = matrix;
		this.transform = transform;
	}

	@Override
	public Matrix readRasters(Rectangle rect) throws IOException {
		// only return values in the rectangle
		int[][] values = new int[rect.width][rect.height];
		for (int i = rect.x; i < rect.x + rect.width; i++) {
			for (int j = rect.y; j < rect.y + rect.height; j++) {
				values[i - rect.x][j - rect.y] = matrix.get(j, i);
			}
		}
		Matrix arrayMatrix = new ArrayMatrix(values, rect.width, rect.height);

		return arrayMatrix;
	}

	/**
	 * Returns metadata about the matrix instead of a file. Used for testing.
	 */
	@Override
	protected ImageMetadata readImageMetadata() throws IOException {
		return new ImageMetadata(matrix.getWidth(), matrix.getHeight(), matrix.getBitsUsed(),
				matrix.getSampleSize());
	}

	@Override
	public CoordinateReferenceSystem getCRS() {
		// TODO Auto-generated method stub
		try {
			return CRS.decode("EPSG:4326");
		} catch (NoSuchAuthorityCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public TFWFormat getG2M() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getG2M'");
	}
}
