package dk.itu.raven.io;

import java.io.IOException;
import java.util.stream.Stream;

import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.SpatialDataChunk;
import dk.itu.raven.util.matrix.ArrayMatrix;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.util.matrix.RandomMatrix;

public class GeneratorRasterReader implements IRasterReader {
	private int width;
	private int height;
	private int maxValue;
	private long seed;
	private TFWFormat transform;

	public GeneratorRasterReader(int width, int height, long seed, int maxValue, TFWFormat transform) {
		this.width = width;
		this.height = height;
		this.transform = transform;
		this.maxValue = maxValue;
	}

	@Override
	public Matrix readRasters(Rectangle rect) throws IOException {
		Matrix randomMatrix = new RandomMatrix(seed, width, height, maxValue);
		int width = (int) rect.x2() - (int) rect.x1();
		int height = (int) rect.y2() - (int) rect.y1();

		int[][] m = new int[height][width];

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				m[i][j] = randomMatrix.get(i, j);
			}
		}
		return new ArrayMatrix(m, width, height);

		// return randomMatrix;
	}

	@Override
	public TFWFormat getTransform() throws IOException {
		return transform;
	}

	@Override
	public Stream<SpatialDataChunk> rasterPartitionStream(Rectangle rect, int widthStep, int heightStep)
			throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'streamRasters'");
	}
}
