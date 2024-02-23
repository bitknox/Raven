package dk.itu.raven.ksquared;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import dk.itu.raven.util.BitMap;
import dk.itu.raven.util.Logger;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.PrimitiveArrayWrapper;
import dk.itu.raven.util.matrix.Matrix;

public abstract class AbstractK2RasterBuilder {
    protected List<BitMap> t;
    protected int[] pMax;
    protected int[] pMin;
    protected Matrix m;
    protected int n;
    protected int k;

    public AbstractK2Raster build(Matrix m, int k) {
        Rectangle rect = new Rectangle(0, 0, 0, 0);
        return build(m, k, rect);
    }

    /**
     * bulds a K^2 Raster data-structure for an n*m matrix (meaning a 2-dimensional
     * array with {@code n} rows and {@code m} columns)
     * 
     * @param m the raw matrix data
     */
    public AbstractK2Raster build(Matrix m, int k, Rectangle rect) {
        this.k = k;

        int h = m.getHeight();
        int w = m.getWidth();

        this.m = m;

        // ensures n is a power of k even if the n from the input is not
        int maxLevel = 1;
        int real_h = 1;
        while (real_h < h || real_h < w) {
            real_h *= k;
            maxLevel++;
        }
        this.n = real_h;

        t = new ArrayList<>(maxLevel);
        pMax = new int[maxLevel];
        pMin = new int[maxLevel];
        for (int i = 0; i < maxLevel; i++) {
            t.add(new BitMap(40));
        }
        init(maxLevel);

        Pair<Long, Long> res = new Pair<Long, Long>(0L, 0L);
        buildK2(this.n, 1, 0, 0, res);
        m = null;
        long maxVal = res.first;
        long minVal = res.second;
        setVMax(0, 0, maxVal);
        setVMin(0, 0, minVal);

        int size_max = 0;
        int size_min = 0;
        for (int i = 1; i < maxLevel; i++) {
            size_max += pMax[i];
            size_min += pMin[i];
        }

        Logger.log("size_max: " + size_max, Logger.LogLevel.DEBUG);
        Logger.log("size_min: " + size_min, Logger.LogLevel.DEBUG);

        PrimitiveArrayWrapper LMaxList = getWrapper(size_max + 1);
        PrimitiveArrayWrapper LMinList = getWrapper(size_min + 1);

        BitMap tree = new BitMap(Math.max(1, size_max));
        int bitmapIndex = 0;

        for (int i = 0; i < maxLevel - 1; i++) {
            for (int j = 0; j < pMax[i]; j++) {
                if (t.get(i).isSet(j)) {
                    tree.set(++bitmapIndex);
                } else {
                    tree.unset(++bitmapIndex);
                }
            }
        }

        pMax[0] = 1;
        if (maxVal != minVal) { // the root of the k2 raster tree is not a leaf
            tree.set(0);
            t.get(0).set(0);
            pMin[0] = 1;
        } else { // the root of the k2 raster tree is a leaf
            tree.unset(0);
            t.get(0).unset(0);
            pMin[0] = 0;
        }

        int[] prefixSum = new int[size_max + 1];
        prefixSum[0] = 0;
        for (int i = 1; i < size_max + 1; i++) {
            prefixSum[i] = prefixSum[i - 1] + tree.getOrZero(i);
        }

        int imax = 0, imin = 0;

        // compute LMin using the VMin computed in Build
        for (int i = 0; i < maxLevel - 2; i++) {
            int internalNodeCount = 0;
            int innerInternalNodeCount = 0;
            for (int j = 0; j < pMax[i]; j++) {
                if (t.get(i).isSet(j)) {
                    int start = internalNodeCount * k * k;
                    for (int l = start; l < start + k * k; l++) {
                        if (t.get(i + 1).isSet(l)) {
                            LMinList.set(imin++, Math.abs(
                                    getVMin(i + 1, innerInternalNodeCount) - getVMin(i, internalNodeCount)));
                            innerInternalNodeCount++;
                        }
                    }
                    internalNodeCount++;
                }
            }
        }
        killVMin();
        pMin = null;

        // compute LMax using the VMax computed in Build
        for (int i = 0; i < maxLevel - 1; i++) {
            int internalNodeCount = 0;
            for (int j = 0; j < pMax[i]; j++) {
                if (t.get(i).isSet(j)) {
                    int start = internalNodeCount * k * k;
                    for (int l = start; l < start + k * k; l++) {
                        LMaxList.set(imax++, Math.abs(getVMax(i, j) - getVMax(i + 1, l)));
                    }
                    internalNodeCount++;
                }
            }
        }

        killVMax();
        t = null;
        pMax = null;

        // TODO: use DAC
        PrimitiveArrayWrapper lMax = LMaxList;
        PrimitiveArrayWrapper lMin = LMinList;

        return getK2Raster(maxVal, minVal, tree, lMax, lMin, prefixSum, rect);
    }

    protected void buildK2(int n, int level, int row, int column, Pair<Long, Long> res) {
        long minVal = Integer.MAX_VALUE;
        long maxVal = 0;

        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                if (n == k) { // last level
                    long matrixVal = getMatrixVal(row + i, column + j);
                    if (minVal > matrixVal) {
                        minVal = matrixVal;
                    }
                    if (maxVal < matrixVal) {
                        maxVal = matrixVal;
                    }
                    setVMax(level, pMax[level], matrixVal);
                    pMax[level]++;
                } else {
                    buildK2(n / k, level + 1, row + i * (n / k), column + j * (n / k), res);
                    setVMax(level, pMax[level], res.first);
                    if (!res.first.equals(res.second)) {
                        setVMin(level, pMax[level], res.second);
                        pMin[level]++;
                        t.get(level).set(pMax[level]);
                    } else {
                        t.get(level).unset(pMax[level]);
                    }

                    pMax[level]++;
                    if (minVal > res.second) {
                        minVal = res.second;
                    }
                    if (maxVal < res.first) {
                        maxVal = res.first;
                    }
                }
            }
        }
        if (minVal == maxVal) {
            pMax[level] -= k * k;
        }

        res.first = maxVal;
        res.second = minVal;
    }

    protected abstract long getMatrixVal(int r, int c);

    protected abstract void setVMax(int level, int index, long val);

    protected abstract long getVMax(int level, int index);

    protected abstract void setVMin(int level, int index, long val);

    protected abstract long getVMin(int level, int index);

    protected abstract void init(int maxLevel);

    protected abstract PrimitiveArrayWrapper getWrapper(int size);

    protected abstract void killVMin();

    protected abstract void killVMax();

    protected abstract AbstractK2Raster getK2Raster(long maxVal, long minVal, BitMap tree, PrimitiveArrayWrapper lMax,
            PrimitiveArrayWrapper lMin, int[] prefixSum, Rectangle rasterWindow);
}
