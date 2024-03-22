package dk.itu.raven.ksquared;

import dk.itu.raven.ksquared.dac.LongDAC;
import dk.itu.raven.util.BitMap;
import dk.itu.raven.util.LongArrayWrapper;
import dk.itu.raven.util.PrimitiveArrayWrapper;

/**
 * K2-Raster datastructure for storing spatial raster data
 */
public class K2Raster extends AbstractK2Raster {
    public K2Raster(int k, long maxVal, long minVal, BitMap tree, LongArrayWrapper lMax, LongArrayWrapper lMin, int n,
            IntRank prefixSum) {
        super(k, minVal, maxVal, tree, n, prefixSum, new LongDAC(lMin), new LongDAC(lMax));
    }

    @Override
    public PrimitiveArrayWrapper getWindow(int r1, int r2, int c1, int c2) {
        PrimitiveArrayWrapper out = new LongArrayWrapper(new long[getSize(r1, r2, c1, c2)]);
        getWindow(r1, r2, c1, c2, out);
        return out;
    }
}
