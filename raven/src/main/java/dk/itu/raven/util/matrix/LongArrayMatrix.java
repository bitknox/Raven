package dk.itu.raven.util.matrix;

/**
 * Matrix implementation using a 2D array.
 */
public class LongArrayMatrix extends Matrix {
    private long[][] m;

    public LongArrayMatrix(long[][] m, int width, int height) {
        super(width, height);
        this.m = m;
    }

    @Override
    public int getWithinRange(int r, int c) {
        return (int) getWithinRangeLong(r, c);
    }

    @Override
    public long getWithinRangeLong(int r, int c) {
        return m[r][c];
    }
}
