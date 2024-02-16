package dk.itu.raven.util.matrix;

import java.awt.image.Raster;
import java.io.IOException;

public class AwtRasterMatrix extends Matrix {
    private Raster raster;
    private int numberOfBands;

    public AwtRasterMatrix(Raster raster) {
        super(raster.getWidth(), raster.getHeight(),0);
        this.raster = raster;

        this.sampleSize = raster.getSampleModel().getSampleSize();
        numberOfBands = raster.getNumBands();

        for (int bits : sampleSize) {
            this.bitsUsed += bits;
        }

        if (this.bitsUsed > 64) {
            throw new UnsupportedOperationException("The total bits per pixel is greater than 64");
        }
    }

    @Override
    protected long getWithinRangeLong(int r, int c) throws IOException {

        long color = raster.getSample(c, r, 0);
        for (int i = 1; i < numberOfBands; i++) {
            color <<= sampleSize[i];
            color += raster.getSample(c, r, i);
        }

        return color;
    }

    @Override
    protected int getWithinRange(int r, int c) throws IOException {
        return raster.getSample(c, r, 0);
    }
    
}
