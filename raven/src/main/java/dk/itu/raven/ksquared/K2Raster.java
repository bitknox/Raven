package dk.itu.raven.ksquared;

import dk.itu.raven.util.BitMap;
import dk.itu.raven.util.LongArrayWrapper;
import dk.itu.raven.util.PrimitiveArrayWrapper;

/**
 * K2-Raster datastructure for storing spatial raster data
 */
public class K2Raster extends AbstractK2Raster {
    public K2Raster(int k, long maxVal, long minVal, BitMap tree, long[] lMax, long[] lMin, int n, int[] prefixSum) {
        super(k, minVal, maxVal, tree, n, prefixSum,new LongArrayWrapper(lMin),new LongArrayWrapper(lMax));
    }

    @Override
    public PrimitiveArrayWrapper getWindow(int r1, int r2, int c1, int c2) {
        PrimitiveArrayWrapper out = new LongArrayWrapper(new long[getSize(r1, r2, c1, c2)]);
        getWindow(r1, r2, c1, c2, out);
        return out;
    }

    @Override
    public PrimitiveArrayWrapper searchValuesInWindow(int r1, int r2, int c1, int c2, int thresholdLow,
            int thresholdHigh) {
        PrimitiveArrayWrapper out = new LongArrayWrapper(new long[getSize(r1, r2, c1, c2)]);
        searchValuesInWindow(r1, r2, c1, c2, thresholdLow, thresholdHigh, out);
        return out;
    }
}
