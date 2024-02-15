package dk.itu.raven.util.matrix;

import java.awt.image.Raster;
import java.io.IOException;

public class AwtRasterMatrix extends Matrix {
    private Raster raster;

    public AwtRasterMatrix(Raster raster) {
        super(raster.getWidth(), raster.getHeight());
        this.raster = raster;
    }

    @Override
    protected int getWithinRange(int r, int c) throws IOException {
        return raster.getSample(c, r, 0);
    }
    
}
