package dk.itu.raven.util.matrix;

import mil.nga.tiff.Rasters;

/**
 * Matrix implementation using a Rasters from the mil.nga.tiff.Rasters library.
 */
public class RastersMatrix extends Matrix {
    private Rasters rasters;

    public RastersMatrix(Rasters rasters) {
        super(rasters.getWidth(), rasters.getHeight(), 0);
        int numSamples = rasters.getSamplesPerPixel();
        this.sampleSize = new int[numSamples];
        for (int idx = 0; idx < numSamples; idx++) {
            this.sampleSize[idx] = rasters.getBitsPerSample().get(idx);
            this.bitsUsed += this.sampleSize[idx];
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
            color <<= rasters.getBitsPerSample().get(i - 1);
            color += rasters.getPixelSample(i, c, r).intValue();
        }
        return color;
    }

}
