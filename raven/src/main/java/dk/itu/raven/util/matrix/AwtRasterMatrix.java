package dk.itu.raven.util.matrix;

import java.awt.image.Raster;
import java.io.IOException;

import dk.itu.raven.util.Logger;
import dk.itu.raven.util.Logger.LogLevel;

public class AwtRasterMatrix extends Matrix {
    private Raster raster;
    private int numberOfBands;

    public AwtRasterMatrix(Raster raster) {
        super(raster.getWidth(), raster.getHeight(), 0);
        this.raster = raster;

        this.sampleSize = raster.getSampleModel().getSampleSize();
        numberOfBands = raster.getNumBands();

        Logger.log("bits:", LogLevel.DEBUG);
        for (int bits : sampleSize) {
            this.bitsUsed += bits;
            Logger.log("  " + bits, LogLevel.DEBUG);
        }

        if (this.bitsUsed > 64) {
            throw new UnsupportedOperationException("The total bits per pixel is greater than 64");
        }
    }

    @Override
    protected long getWithinRangeLong(int r, int c) throws IOException {

        long color = raster.getSample(c, r, 0);
        for (int i = 1; i < numberOfBands; i++) {
            color <<= sampleSize[i - 1];
            color += raster.getSample(c, r, i);
        }

        return color;
    }

    @Override
    protected int getWithinRange(int r, int c) throws IOException {
        return (int) getWithinRangeLong(r, c);
    }

}
