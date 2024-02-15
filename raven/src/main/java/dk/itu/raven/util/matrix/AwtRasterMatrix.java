package dk.itu.raven.util.matrix;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.IOException;

public class AwtRasterMatrix extends Matrix {
    private Raster raster;
    int bytesPerPixel;
    int numberOfBands;
    int sampleSize[];


    public AwtRasterMatrix(Raster raster) {
        super(raster.getWidth(), raster.getHeight());
        this.raster = raster;

        switch (raster.getSampleModel().getDataType()) {
            case DataBuffer.TYPE_BYTE:
                bytesPerPixel = 1;
                break;
            case DataBuffer.TYPE_DOUBLE:
                bytesPerPixel = 8;
                break;
            case DataBuffer.TYPE_FLOAT:
                bytesPerPixel = 4;
                break;
            case DataBuffer.TYPE_INT:
                bytesPerPixel = 4;
                break;
            case DataBuffer.TYPE_SHORT:
                bytesPerPixel = 2;
                break;
            case DataBuffer.TYPE_USHORT:
                bytesPerPixel = 2;
                break;
        
            default:
                bytesPerPixel = 0;
                break;
        }

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
