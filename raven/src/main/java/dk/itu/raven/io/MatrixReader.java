package dk.itu.raven.io;

import java.io.IOException;
import java.util.stream.Stream;

import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.SpatialDataChunk;
import dk.itu.raven.util.matrix.ArrayMatrix;
import dk.itu.raven.util.matrix.Matrix;

public class MatrixReader implements RasterReader {
	public Matrix matrix;
	public TFWFormat transform;

	public MatrixReader(Matrix matrix, TFWFormat transform) {
		this.matrix = matrix;
		this.transform = transform;
	}

	@Override
	public Matrix readRasters(Rectangle rect) throws IOException {
		// only return values in the rectangle
		int width = (int) Math.ceil(rect.x2()) - (int) rect.x1();
		int height = (int) Math.ceil(rect.y2()) - (int) rect.y1();
		int[][] values = new int[width][height];
		for (int i = (int) rect.x1(); i < (int) rect.x2(); i++) {
			for (int j = (int) rect.y1(); j < (int) rect.y2(); j++) {
				values[i - (int) rect.x1()][j - (int) rect.y1()] = matrix.get(i, j);
			}
		}
		Matrix arrayMatrix = new ArrayMatrix(values, width, height);

		return arrayMatrix;
	}

	@Override
	public TFWFormat getTransform() throws IOException {
		return transform;
	}

	@Override
	public Stream<SpatialDataChunk> rasterPartitionStream(Rectangle rect, int widthStep, int heightStep)
			throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'rasterPartitionStream'");
	}
}
