package dk.itu.raven.ksquared;

import java.io.Serializable;
import java.util.List;

import dk.itu.raven.util.BitMap;
import dk.itu.raven.util.GoodArrayList;
import dk.itu.raven.util.Pair;

/**
 * K2-Raster datastructure for storing spatial raster data
 */
public class K2RasterLong implements Serializable {
    public int k;
    private long maxVal; // the maximum value stored in the matrix
    private long minVal; // the minimum value stored in the matrix
    public BitMap tree; // A tree where the i'th index is a one iff. the node with index i is internal
    // TODO: use DAC
    private long[] lMax; // stores the difference between the maximum value stored in a node and the
                        // maximum value of its parent node
    private long[] lMin; // stores the difference between the minimum value stored in a node and the
                        // minimum value of its parent node
    private int n; // the size of the matrix, always a power of k
    private int[] prefixSum; // a prefix sum of the tree

    public K2RasterLong(int k, long maxVal, long minVal, BitMap tree, long[] lMax, long[] lMin, int n, int[] prefixSum) {
        this.k = k;
        this.maxVal = maxVal;
        this.minVal = minVal;
        this.tree = tree;
        this.lMax = lMax;
        this.lMin = lMin;
        this.n = n;
        this.prefixSum = prefixSum;
    }

    /**
     * 
     * @param index the LMax index of a node of the k2 raster tree
     * @return {@code true} if the node corresponding to the given index is an
     *         internal node, {@code false} otherwise.
     */
    public boolean hasChildren(int index) {
        return tree.isSet(index);
    }

    /**
     * Gets the minimum and maximum values in the K2Raster tree
     * 
     * @return an array of length 2, where the first element is the minimum value
     *         and the second element is the maximum value
     */
    public long[] getValueRange() {
        return new long[] { minVal, maxVal };
    }

    /**
     * 
     * @param parentMax The maximum value stored in the sub-matrix corresponding to
     *                  the parent of the node with the given index.
     * @param index     the LMax index of a node of the k2 raster tree
     * @return the maximum value stored in the sub-matrix corresponding to the node
     *         with the given index
     */
    public long computeVMax(long parentMax, int index) {
        if (index == 0)
            return maxVal;
        return parentMax - lMax[index - 1];
    }

    /**
     * 
     * @param parentMax The maximum value stored in the sub-matrix corresponding to
     *                  the parent of the node with the given index.
     * @param parentMin The minimum value stored in the sub-matrix corresponding to
     *                  the parent of the node with the given index.
     * @param index     the LMax index of a node of the k2 raster tree
     * @return the minimum value stored in the sub-matrix corresponding to the node
     *         with the given index
     */
    public long computeVMin(long parentMax, long parentMin, int index) {
        if (index == 0)
            return minVal;
        if (!hasChildren(index)) {
            return computeVMax(parentMax, index);
        }
        int pref = prefixSum[index - 1];
        return parentMin + lMin[pref];
    }

    /**
     * gets the children of the node at index {@code index}
     * 
     * @param index the index of the parent node
     * @return array of indxes
     */
    public int[] getChildren(int index) {
        if (!hasChildren(index)) {
            return new int[0];
        } else {
            int numInternalNodes = prefixSum[index];
            int[] res = new int[k * k];
            for (int i = 0; i < k * k; i++) {
                res[i] = 1 + k * k * numInternalNodes + i;
            }
            return res;
        }
    }

    /**
     * 
     * @return the size of the K2Raster tree
     */
    public int getSize() {
        return this.n;
    }

    /**
     * Use of this method is discouraged for performance reasons. Use
     * {@code getWindow}
     * instead.
     * 
     * @param n      size of the matrix
     * @param r      the row to access
     * @param c      the column to access
     * @param z      only God knows what this does
     * @param maxVal the max value in the matrix
     * @return the value from the matrix at index {@code (r,c)}
     */
    private long getCell(int n, int r, int c, int z, long maxVal) {
        int nKths = (n / k);
        z = this.tree.rank(z) * k * k;
        z = z + (r / nKths) * k + (c / nKths);
        long val = lMax[z]; // LMax is 0-indexed
        maxVal = maxVal - val;
        if (!hasChildren(z + 1)) // +1 because the bitmap is 1-indexed
            return maxVal;
        return getCell(nKths, r % nKths, c % nKths, z, maxVal);
    }

    /**
     * @param r the row to access
     * @param c the column to access
     * @return the value from the matrix at index {@code (r,c)}
     */
    public long getCell(int r, int c) {
        return getCell(this.n, r, c, -1, this.maxVal);
    }

    private void getWindow(int n, int r1, int r2, int c1, int c2, int z, long maxVal, long[] out,
            IntPointer index,
            int level, List<Pair<Integer, Integer>> indexRanks) {
        int nKths = (n / k);
        Pair<Integer, Integer> indexRank = indexRanks.get(level);
        int rank = prefixSum[z + 1];
        indexRank.first = z;
        indexRank.second = rank;

        z = rank * k * k;
        int initialI = r1 / nKths;
        int lastI = r2 / nKths;
        int initialJ = c1 / nKths;
        int lastJ = c2 / nKths;

        int r1p, r2p, c1p, c2p, zp;
        long maxvalp;

        for (int i = initialI; i <= lastI; i++) {
            if (i == initialI)
                r1p = r1 % nKths;
            else
                r1p = 0;

            if (i == lastI)
                r2p = r2 % nKths;
            else
                r2p = nKths - 1;

            for (int j = initialJ; j <= lastJ; j++) {
                if (j == initialJ)
                    c1p = c1 % nKths;
                else
                    c1p = 0;

                if (j == lastJ)
                    c2p = c2 % nKths;
                else
                    c2p = nKths - 1;

                zp = z + i * k + j;

                maxvalp = maxVal - lMax[zp];
                
                if (!hasChildren(zp + 1)) {
                    int times = ((r2p - r1p) + 1) * ((c2p - c1p) + 1);
                    for (int l = 0; l < times; l++) {
                        out[index.val++] = maxvalp;
                    }
                } else {
                    getWindow(nKths, r1p, r2p, c1p, c2p, zp, maxvalp, out, index, level + 1, indexRanks);
                }

            }
        }
    }

    /**
     * Reads data from a window of the matrix given by the two points
     * {@code (r1,c1)} and {@code (r2,c2)} (inclusive on all sides)
     * 
     * @param r1 row number for the top left corner of window
     * @param r2 row number for the bottom right corner of window
     * @param c1 column number for the top left corner of window
     * @param c2 column number for the bottom right corner of window
     * @return a window of the matrix
     */
    public long[] getWindow(int r1, int r2, int c1, int c2) {
        if (r1 < 0 || r1 >= n || r2 < 0 || r2 >= n || c1 < 0 || c1 >= n || c2 < 0
                || c2 >= n)
            throw new IndexOutOfBoundsException("looked up window (" + c1 + ", " + r1 + ", " + c2 + ", " + r2
                    + ") in matrix with size (" + n + ", " + n + ")");
        int returnSize = (r2 - r1 + 1) * (c2 - c1 + 1);
        long[] out = new long[returnSize];
        int maxLevel = 1 + (int) Math.ceil(Math.log(n) / Math.log(k));
        GoodArrayList<Pair<Integer, Integer>> indexRanks = new GoodArrayList<Pair<Integer, Integer>>(maxLevel);
        for (int i = 0; i < maxLevel; i++) {
            indexRanks.set(i, new Pair<>(-1, 0));
        }
        getWindow(this.n, r1, r2, c1, c2, -1, this.maxVal, out, new IntPointer(), 0, indexRanks);

        return out;
    }

    private void searchValuesInWindow(int n, int r1, int r2, int c1, int c2, int baseX, int baseY, int z, long maxVal,
            long minVal, int vb,
            int ve, long[] out,
            IntPointer index,
            int level) {

        int cBaseX, cBaseY;

        int nKths = (n / k); // childsize
        int rank = prefixSum[z + 1];
        z = rank * k * k;
        int initialI = (r1 - baseY) / nKths;
        int lastI = (r2 - baseY) / nKths;
        int initialJ = (c1 - baseX) / nKths;
        int lastJ = (c2 - baseX) / nKths;

        int r1p, r2p, c1p, c2p, zp;
        long maxValp, minValp;

        for (int i = initialI; i <= lastI; i++) {
            cBaseY = baseY + i * nKths;

            for (int j = initialJ; j <= lastJ; j++) {
                zp = z + i * k + j;
                cBaseX = baseX + j * nKths;
                maxValp = maxVal - lMax[zp];

                if (maxValp < ve) {
                    continue;
                }

                boolean addCells = false;
                if (!hasChildren(zp + 1)) {
                    minValp = maxValp;
                    if (minValp >= vb && maxValp <= ve) {
                        addCells = true;
                        /* all cells meet the condition in this branch */
                    }
                } else {
                    // TODO: make the following work: minvalp = computeVMin(maxval, minval, zp);
                    minValp = minVal + lMin[prefixSum[zp + 1]];
                }
                if (minValp > ve) {
                    continue;
                }

                r1p = Math.max(c1, cBaseX);
                r2p = Math.min(c2, cBaseX + nKths - 1);
                c1p = Math.max(r1, cBaseY);
                c2p = Math.min(r2, cBaseY + nKths - 1);

                if (minValp >= vb && maxValp <= ve) {
                    addCells = true;
                    /* all cells meet the condition in this branch */
                } else {
                    searchValuesInWindow(nKths, r1p, r2p, c1p, c2p, cBaseX, cBaseY, zp, maxValp, minValp, vb, ve,
                            out, index,
                            level + 1);
                }

                if (addCells) {
                    System.out.println("x1: " + c1 + ", x2: " + c2 + ", y1: " + r1 + ", y2: " + r2);
                }
            }
        }
    }

    /**
     * Reads data from a window of the matrix given by the two points
     * {@code (r1,c1)} and {@code (r2,c2)}
     * 
     * @param r1 row number for the top left corner of window
     * @param r2 row number for the bottom right corner of window
     * @param c1 column number for the top left corner of window
     * @param c2 column number for the bottom right corner of window
     * @return a window of the matrix with only the values {@code v} that satisfy
     *         {@code vb <= v <= ve}
     */
    public long[] searchValuesInWindow(int r1, int r2, int c1, int c2, int thresholdLow, int thresholdHigh) {
        if (r1 < 0 || r1 >= n || r2 < 0 || r2 >= n || c1 < 0 || c1 >= n || c2 < 0
                || c2 >= n)
            throw new IndexOutOfBoundsException("looked up window (" + r1 + ", " + c1 + ", " + r2 + ", " + c2
                    + ") in matrix with size (" + n + ", " + n + ")");
        int returnSize = (r2 - r1 + 1) * (c2 - c1 + 1); // can be smaller.
        long[] out = new long[returnSize];
        searchValuesInWindow(this.n, r1, r2, c1, c2, 0, 0, -1, this.maxVal, this.minVal, thresholdLow, thresholdHigh,
                out,
                new IntPointer(), 0);

        return out;
    }
}
