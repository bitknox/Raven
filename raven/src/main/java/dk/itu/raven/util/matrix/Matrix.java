package dk.itu.raven.util.matrix;

import java.io.IOException;

import dk.itu.raven.geometry.Size;

/*
This class helps by wrapping different forms of matrix data
and provides the added utility that values outside the range of the matrix will have value 0.
This helps reduce the complexity of the K2Raster implementation.
*/

public abstract class Matrix {
    protected int width, height;
    protected int bitsUsed;
    protected int[] sampleSize;

    public Matrix(int width, int height, int bitsUsed) {
        this.width = width;
        this.height = height;
        this.bitsUsed = bitsUsed;
        this.sampleSize = new int[] { bitsUsed };
    }

    public static final long filler = 0;

    public int get(int r, int c) {
        if (c < 0 || c >= width || r < 0 || r >= height)
            return (int) filler;
        try {
            return getWithinRange(r, c);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
            return -1;
        }
    }

    public long getLong(int r, int c) {
        if (c < 0 || c >= width || r < 0 || r >= height)
            return filler;
        try {
            return getWithinRangeLong(r, c);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
            return -1;
        }
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Size getSize() {
        return new Size(this.width, this.height);
    }

    public void print() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(getLong(i, j) + " ");
            }
            System.out.println();
        }
    }

    protected abstract int getWithinRange(int r, int c) throws IOException;

    protected long getWithinRangeLong(int r, int c) throws IOException {
        return getWithinRange(r, c);
    }

    public int getBitsUsed() {
        return this.bitsUsed;
    }

    public int[] getSampleSize() {
        return this.sampleSize;
    }

    public boolean overlaps(int x, int y) {
        return (x < width && y < height);
    }
}
