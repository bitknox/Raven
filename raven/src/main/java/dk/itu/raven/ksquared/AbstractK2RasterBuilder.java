package dk.itu.raven.ksquared;

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
    private int k2;
    private int[] nKths;

    /**
     * bulds a K^2 Raster data-structure for an n*m matrix (meaning a 2-dimensional
     * array with {@code n} rows and {@code m} columns)
     * 
     * @param m the raw matrix data
     */
    public AbstractK2Raster build(Matrix m, int k) {
        this.k = k;
        this.k2 = k * k;

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
        nKths = new int[maxLevel];
        int tempN = n;
        for (int i = 0; i < maxLevel; i++) {
            int nk = tempN / k;
            nKths[i] = nk;
            tempN = nk;
            t.add(new BitMap(40));
        }
        init(maxLevel);

        Pair<Long, Long> res = new Pair<Long, Long>(0L, 0L);
        if (this.n == 1) {
            res.first = getMatrixVal(0, 0);
            res.second = getMatrixVal(0, 0);
        } else {
            buildK2(this.n, 1, 0, 0, res);
        }
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
        pMax[0] = 1;
        if (maxVal != minVal) { // the root of the k2 raster tree is not a leaf
            // tree.set(0);
            t.get(0).set(0);
            pMin[0] = 1;
        } else { // the root of the k2 raster tree is a leaf
            // tree.unset(0);
            t.get(0).unset(0);
            pMin[0] = 0;
        }

        // TODO: build prefix sum array at the same time as the tree
        int imax = 0, imin = 0;
        // builds the LMax list at the same time as the concatinated tree
        for (int i = 0; i < maxLevel - 1; i++) {
            int internalNodeCount = 0;
            for (int j = 0; j < pMax[i]; j++) {
                if (t.get(i).isSet(j)) {
                    int start = internalNodeCount * k2;
                    for (int l = start; l < start + k2; l++) {
                        LMaxList.set(imax++, Math.abs(getVMax(i, j) - getVMax(i + 1, l)));
                    }
                    internalNodeCount++;
                    // tree.set(bitmapIndex++);
                } else {
                    // tree.unset(bitmapIndex++);
                }
            }
            t.get(i).setSize(pMax[i]);
            tree.concat(t.get(i));
        }

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

        tree.unset(0);

        // the +1 is caused by the rank being 0-indexed, while the tree is 1-indexed
        IntRank prefixSum = new IntRank(tree.getMap(), bitmapIndex + 1);

        // compute LMin using the VMin computed in Build

        killVMin();
        killVMax();
        pMin = null;
        pMax = null;
        t = null;

        // TODO: use DAC
        PrimitiveArrayWrapper lMax = LMaxList;
        PrimitiveArrayWrapper lMin = LMinList;

        return getK2Raster(maxVal, minVal, tree, lMax, lMin, prefixSum);
    }

    protected void buildK2(int n, int level, int row, int column, Pair<Long, Long> res) {
        long minVal = Integer.MAX_VALUE;
        long maxVal = 0;
        int nKths = this.nKths[level - 1];

        if (n == k) { // last level
            for (int newRow = row; newRow < row + n; newRow += nKths) {
                for (int newColumn = column; newColumn < column + n; newColumn += nKths) {
                    long matrixVal = getMatrixVal(newRow, newColumn);
                    minVal = minVal > matrixVal ? matrixVal : minVal;
                    maxVal = maxVal < matrixVal ? matrixVal : maxVal;
                    setVMax(level, pMax[level], matrixVal);
                    pMax[level]++;
                }
            }
        } else {
            for (int newRow = row; newRow < row + n; newRow += nKths) {
                for (int newColumn = column; newColumn < column + n; newColumn += nKths) {
                    if (!m.overlaps(newColumn, newRow)) {
                        res.first = Matrix.filler;
                        res.second = Matrix.filler;
                    } else {
                        buildK2(nKths, level + 1, newRow, newColumn, res);
                    }
                    setVMax(level, pMax[level], res.first);
                    if (!res.first.equals(res.second)) {
                        setVMin(level, pMax[level], res.second);
                        pMin[level]++;
                        t.get(level).set(pMax[level]);
                    } else {
                        t.get(level).unset(pMax[level]);
                    }

                    pMax[level]++;
                    minVal = minVal > res.second ? res.second : minVal;
                    maxVal = maxVal < res.first ? res.first : maxVal;
                }
            }
        }

        pMax[level] -= minVal == maxVal ? k2 : 0;

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
            PrimitiveArrayWrapper lMin, IntRank prefixSum);
}
