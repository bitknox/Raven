package dk.itu.raven.util.matrix;

import java.io.IOException;

/**
 * Matrix implementation using a 2D array.
 */
public class ArrayMatrix extends Matrix {
    private int[][] m;

    public ArrayMatrix(int[][] m, int width, int height) {
        super(width, height);
        this.m = m;
    }

    @Override
    public int getWithinRange(int r, int c) {
        return m[r][c];
    }

    @Override
    protected long getWithinRangeLong(int r, int c) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getWithinRangeLong'");
    }

}
