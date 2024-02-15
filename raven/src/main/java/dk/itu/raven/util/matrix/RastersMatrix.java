package dk.itu.raven.util.matrix;

import java.io.IOException;

import mil.nga.tiff.Rasters;

/**
 * Matrix implementation using a Rasters from the mil.nga.tiff.Rasters library.
 */
public class RastersMatrix extends Matrix {
    private Rasters rasters;

    public RastersMatrix(Rasters rasters) {
        super(rasters.getWidth(), rasters.getHeight());
        this.rasters = rasters;
    }

    @Override
    public int getWithinRange(int r, int c) {
        return rasters.getPixelSample(0, c, r).intValue();
    }

    @Override
    protected long getWithinRangeLong(int r, int c) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getWithinRangeLong'");
    }

}
