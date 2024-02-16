package dk.itu.raven.util.matrix;

/*
Used for debugging
*/

public class HalfHalfMatrix extends Matrix {
	public int[][] m;

	public HalfHalfMatrix(int width, int height) {
		super(width, height,32);
		m = new int[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (i > width / 2) {
					m[i][j] = 1;
				} else {
					m[i][j] = 0;
				}
			}
		}
	}

	@Override
	public int getWithinRange(int r, int c) {
		return m[r][c];
	}

}
