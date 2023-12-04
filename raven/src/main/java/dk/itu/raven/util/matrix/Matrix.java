package dk.itu.raven.util.matrix;

import java.io.IOException;

public abstract class Matrix {
    protected int width, height;

    public Matrix(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int get(int r, int c) {
        if (r < 0 || r >= width || c < 0 || c >= height)
            return 0;
        try {
            return getWithinRange(r, c);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
            return 0;
        }
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    protected abstract int getWithinRange(int r, int c) throws IOException;
}
