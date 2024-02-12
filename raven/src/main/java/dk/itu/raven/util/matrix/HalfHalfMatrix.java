package dk.itu.raven.util.matrix;

public class HalfHalfMatrix extends Matrix {
	public int[][] M;

	public HalfHalfMatrix(int width, int height) {
		super(width, height);
		M = new int[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (i > width / 2) {
					M[i][j] = 1;
				} else {
					M[i][j] = 0;
				}
			}
		}
	}

	@Override
	public int getWithinRange(int r, int c) {
		return M[r][c];
	}

}
