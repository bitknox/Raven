package dk.itu.raven.ksquared;

import java.util.ArrayList;
import java.util.List;

import dk.itu.raven.util.BitMap;
import dk.itu.raven.util.GoodLongArrayList;
import dk.itu.raven.util.LongArrayWrapper;
import dk.itu.raven.util.PrimitiveArrayWrapper;

public class K2RasterBuilder extends AbstractK2RasterBuilder {

    protected List<GoodLongArrayList> vMax;
    protected List<GoodLongArrayList> vMin;

    @Override
    protected void init(int maxLevel) {
        vMax = new ArrayList<GoodLongArrayList>(maxLevel);
        vMin = new ArrayList<GoodLongArrayList>(maxLevel);
        for (int i = 0; i < maxLevel; i++) {
            vMax.add(new GoodLongArrayList());
            vMin.add(new GoodLongArrayList());
        }
    }

    @Override
    protected long getMatrixVal(int r, int c) {
        return m.getLong(r, c);
    }

    @Override
    protected void setVMax(int level, int index, long val) {
        vMax.get(level).set(pMax[level], val);
    }

    @Override
    protected void setVMin(int level, int index, long val) {
        vMin.get(level).set(pMin[level], val);
    }

    @Override
    protected LongArrayWrapper getWrapper(int size) {
        return new LongArrayWrapper(new long[size]);
    }

    @Override
    protected void killVMin() {
        vMin = null;
    }

    @Override
    protected void killVMax() {
        vMax = null;
    }

    @Override
    protected long getVMax(int level, int index) {
        return vMax.get(level).get(index);
    }

    @Override
    protected long getVMin(int level, int index) {
        return vMin.get(level).get(index);
    }

    @Override
    protected K2Raster getK2Raster(long maxVal, long minVal, BitMap tree, PrimitiveArrayWrapper lMax,
            PrimitiveArrayWrapper lMin, int[] prefixSum, java.awt.Rectangle rect) {
        return new K2Raster(k, maxVal, minVal, tree, (LongArrayWrapper) lMax, (LongArrayWrapper) lMin, n, prefixSum,
                rect);
    }

}
