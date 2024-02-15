package dk.itu.raven.ksquared;

import java.util.ArrayList;
import java.util.List;

import dk.itu.raven.util.BitMap;
import dk.itu.raven.util.GoodLongArrayList;
import dk.itu.raven.util.Logger;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.matrix.Matrix;

public class K2RasterBuilder {
    // intermediate datastructures
    private List<GoodLongArrayList> vMax;
    private List<GoodLongArrayList> vMin;
    private List<BitMap> t;
    private int[] pMax;
    private int[] pMin;
    private Matrix m;
    private int n;
    private int k;

    /**
     * bulds a K^2 Raster data-structure for an n*m matrix (meaning a 2-dimensional
     * array with {@code n} rows and {@code m} columns)
     * 
     * @param m the raw matrix data
     */
    public K2Raster build(Matrix m, int k) {
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
        vMax = new ArrayList<GoodLongArrayList>(maxLevel);
        vMin = new ArrayList<GoodLongArrayList>(maxLevel);
        pMax = new int[maxLevel];
        pMin = new int[maxLevel];
        for (int i = 0; i < maxLevel; i++) {
            t.add(new BitMap(40));
            vMax.add(new GoodLongArrayList());
            vMin.add(new GoodLongArrayList());

        }

        Pair<Long,Long> res = new Pair<Long,Long>(0L,0L);
        buildK2(this.n, 1, 0, 0, res);
        m = null;
        long maxVal = res.first;
        long minVal = res.second;
        vMax.get(0).set(0, maxVal);
        vMin.get(0).set(0, minVal);

        int size_max = 0;
        int size_min = 0;
        for (int i = 1; i < maxLevel; i++) {
            size_max += pMax[i];
            size_min += pMin[i];
        }

        Logger.log("size_max: " + size_max);
        Logger.log("size_min: " + size_min);

        long[] LMaxList = new long[size_max + 1];
        long[] LMinList = new long[size_min + 1];

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
                            LMinList[imin++] = Math.abs(
                                    vMin.get(i + 1).get(innerInternalNodeCount) - vMin.get(i).get(internalNodeCount));
                            innerInternalNodeCount++;
                        }
                    }
                    internalNodeCount++;
                }
            }
        }
        vMin = null;
        pMin = null;

        // compute LMax using the VMax computed in Build
        for (int i = 0; i < maxLevel - 1; i++) {
            int internalNodeCount = 0;
            for (int j = 0; j < pMax[i]; j++) {
                if (t.get(i).isSet(j)) {
                    int start = internalNodeCount * k * k;
                    for (int l = start; l < start + k * k; l++) {
                        LMaxList[imax++] = Math.abs(vMax.get(i).get(j) - vMax.get(i + 1).get(l));
                    }
                    internalNodeCount++;
                }
            }
        }

        vMax = null;
        t = null;
        pMax = null;

        // TODO: use DAC
        long[] lMax = LMaxList;
        long[] lMin = LMinList;

        return new K2Raster(k, maxVal, minVal, tree, lMax, lMin, n, prefixSum);
    }

    private void buildK2(int n, int level, int row, int column, Pair<Long, Long> res) {
        long minVal = Integer.MAX_VALUE;
        long maxVal = 0;

        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                if (n == k) { // last level
                    long matrixVal = m.getLong(row + i, column + j);
                    if (minVal > matrixVal) {
                        minVal = matrixVal;
                    }
                    if (maxVal < matrixVal) {
                        maxVal = matrixVal;
                    }
                    vMax.get(level).set(pMax[level], matrixVal);
                    pMax[level]++;
                } else {
                    buildK2(n / k, level + 1, row + i * (n / k), column + j * (n / k), res);
                    vMax.get(level).set(pMax[level], res.first);
                    if (!res.first.equals(res.second)) {
                        vMin.get(level).set(pMin[level], res.second);
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
}
