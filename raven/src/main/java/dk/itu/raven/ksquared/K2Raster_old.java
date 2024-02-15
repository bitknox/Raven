package dk.itu.raven.ksquared;

import dk.itu.raven.util.BitMap;
import dk.itu.raven.util.IntArrayWrapper;
import dk.itu.raven.util.PrimitiveArrayWrapper;

/**
 * K2-Raster datastructure for storing spatial raster data
 */
public class K2Raster_old extends AbstractK2Raster {
    public K2Raster_old(int k, int maxVal, int minVal, BitMap tree, int[] lMax, int[] lMin, int n, int[] prefixSum) {
        super(k, minVal, maxVal, tree, n, prefixSum,new IntArrayWrapper(lMin),new IntArrayWrapper(lMax));
    }

    @Override
    public PrimitiveArrayWrapper getWindow(int r1, int r2, int c1, int c2) {
        PrimitiveArrayWrapper out = new IntArrayWrapper(new int[getSize(r1, r2, c1, c2)]);
        getWindow(r1, r2, c1, c2, out);
        return out;
    }

    @Override
    public PrimitiveArrayWrapper searchValuesInWindow(int r1, int r2, int c1, int c2, int thresholdLow,
            int thresholdHigh) {
        PrimitiveArrayWrapper out = new IntArrayWrapper(new int[getSize(r1, r2, c1, c2)]);
        searchValuesInWindow(r1, r2, c1, c2, thresholdLow, thresholdHigh, out);
        return out;
    }
}
