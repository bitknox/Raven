package dk.itu.raven.util.matrix;

import java.awt.image.Raster;
import java.io.IOException;

public class AwtRasterMatrix extends Matrix {
    private Raster raster;
    int numberOfBands;
    int sampleSize[];

    public AwtRasterMatrix(Raster raster) {
        super(raster.getWidth(), raster.getHeight());
        this.raster = raster;

        sampleSize = raster.getSampleModel().getSampleSize();
        numberOfBands = raster.getNumBands();

        int totalBits = 0;
        for (int bits : sampleSize) {
            totalBits += bits;
        }

        if (totalBits > 64) {
            throw new UnsupportedOperationException("The total bits per pixel is greater than 64");
        }
    }

    @Override
    protected long getWithinRangeLong(int r, int c) throws IOException {

        long color = raster.getSample(r, c, 0);
        for (int i = 1; i < numberOfBands; i++) {
            color <<= sampleSize[i];
            color += raster.getSample(r, c, i);
        }

        return color;
    }

    @Override
    protected int getWithinRange(int r, int c) throws IOException {
        return raster.getSample(r, c, 0);
    }
    
}
