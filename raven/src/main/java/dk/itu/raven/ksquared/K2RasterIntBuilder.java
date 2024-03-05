package dk.itu.raven.ksquared;

import java.util.ArrayList;
import java.util.List;

import dk.itu.raven.util.BitMap;
import dk.itu.raven.util.GoodIntArrayList;
import dk.itu.raven.util.IntArrayWrapper;
import dk.itu.raven.util.PrimitiveArrayWrapper;

public class K2RasterIntBuilder extends AbstractK2RasterBuilder {
    // intermediate datastructures
    private List<GoodIntArrayList> vMax;
    private List<GoodIntArrayList> vMin;

    @Override
    protected void init(int maxLevel) {
        vMax = new ArrayList<GoodIntArrayList>(maxLevel);
        vMin = new ArrayList<GoodIntArrayList>(maxLevel);
        for (int i = 0; i < maxLevel; i++) {
            vMax.add(new GoodIntArrayList());
            vMin.add(new GoodIntArrayList());

        }
    }

    @Override
    protected long getMatrixVal(int r, int c) {
        return m.get(r, c);
    }

    @Override
    protected void setVMax(int level, int index, long val) {
        vMax.get(level).set(pMax[level], (int) val);
    }

    @Override
    protected void setVMin(int level, int index, long val) {
        vMin.get(level).set(pMin[level], (int) val);
    }

    @Override
    protected IntArrayWrapper getWrapper(int size) {
        return new IntArrayWrapper(new int[size]);
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
    protected K2RasterInt getK2Raster(long maxVal, long minVal, BitMap tree, PrimitiveArrayWrapper lMax,
            PrimitiveArrayWrapper lMin, IntRank prefixSum) {
        // IntArrayWrapper casts are safe, since they originate from this class (and are
        // therfore defined as IntArrayWrapper originally)
        // int casts are safe, becaus ethis class should only be used if the matrix
        // contains only ints, using this class on matrices containing longs will
        // truncate them
        return new K2RasterInt(k, (int) maxVal, (int) minVal, tree, (IntArrayWrapper) lMax, (IntArrayWrapper) lMin, n,
                prefixSum);
    }

}
