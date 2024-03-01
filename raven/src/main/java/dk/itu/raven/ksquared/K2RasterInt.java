package dk.itu.raven.ksquared;

import dk.itu.raven.util.BitMap;
import dk.itu.raven.util.IntArrayWrapper;
import dk.itu.raven.util.PrimitiveArrayWrapper;

/**
 * K2-Raster datastructure for storing spatial raster data
 */
public class K2RasterInt extends AbstractK2Raster {
    public K2RasterInt(int k, int maxVal, int minVal, BitMap tree, IntArrayWrapper lMax, IntArrayWrapper lMin, int n,
            int[] prefixSum) {
        super(k, minVal, maxVal, tree, n, prefixSum, lMin, lMax);
    }

    @Override
    public PrimitiveArrayWrapper getWindow(int r1, int r2, int c1, int c2) {
        PrimitiveArrayWrapper out = new IntArrayWrapper(new int[getSize(r1, r2, c1, c2)]);
        getWindow(r1, r2, c1, c2, out);
        return out;
    }
}
