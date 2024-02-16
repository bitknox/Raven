package dk.itu.raven.util.matrix;

import mil.nga.tiff.Rasters;

/**
 * Matrix implementation using a Rasters from the mil.nga.tiff.Rasters library.
 */
public class RastersMatrix extends Matrix {
    private Rasters rasters;

    public RastersMatrix(Rasters rasters) {
        super(rasters.getWidth(), rasters.getHeight(), 0);
        for (int bits : rasters.getBitsPerSample()) {
            this.bitsUsed += bits;
        }
        this.rasters = rasters;
    }

    @Override
    public int getWithinRange(int r, int c) {
        return (int) getWithinRangeLong(r, c);

    }

    @Override
    public long getWithinRangeLong(int r, int c) {
        long color = rasters.getPixelSample(0, c, r).intValue();
        for (int i = 1; i < rasters.getSamplesPerPixel(); i++) {
            color <<= rasters.getBitsPerSample().get(i);
            color += rasters.getPixelSample(i, c, r).intValue();
        }
        return color;
    }

}
