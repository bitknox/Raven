package dk.itu.raven.geometry;

import java.util.Objects;

/**
 * Stores a range of continuous pixels in a compressed format
 */
public class PixelRange {
    public int row;
    public int x1, x2;

    public PixelRange(int row, int x1, int x2) {
        this.row = row;
        this.x1 = x1;
        this.x2 = x2;
    }

    public void translate(int dx, int dy) {
        this.row += dy;
        this.x1 += dx;
        this.x2 += dx;
    }

    @Override
    public String toString() {
        return String.format("PixelRange(row=%d, x1=%d, x2=%d)", row, x1, x2);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PixelRange))
            return false;
        PixelRange pixelRange = (PixelRange) other;
        return this.row == pixelRange.row && this.x1 == pixelRange.x1 && this.x2 == pixelRange.x2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, x1, x2);
    }
}
