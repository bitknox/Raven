package dk.itu.raven.ksquared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.join.IRasterFilterFunction;
import dk.itu.raven.ksquared.dac.AbstractDAC;
import dk.itu.raven.util.BitMap;
import dk.itu.raven.util.GoodArrayList;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.PrimitiveArrayWrapper;

public abstract class AbstractK2Raster implements Serializable {
    public int k;
    protected long minVal;// the maximum value stored in the matrix
    protected long maxVal;// the minimum value stored in the matrix
    public BitMap tree; // A tree where the i'th index is a one iff. the node with index i is internal
    protected int n; // the size of the matrix, always a power of k
    protected IntRank prefixSum; // a prefix sum of the tree

    protected AbstractDAC lMin;// stores the difference between the minimum value stored in a node and the
    // minimum value of its parent node
    protected AbstractDAC lMax;// stores the difference between the maximum value stored in a node and the
    // maximum value of its parent node

    public AbstractK2Raster(int k, long minVal, long maxVal, BitMap tree, int n, IntRank prefixSum,
            AbstractDAC lMin, AbstractDAC lMax) {
        this.k = k;
        this.minVal = minVal;
        this.maxVal = maxVal;
        this.tree = tree;
        this.n = n;
        this.prefixSum = prefixSum;
        this.lMax = lMax;
        this.lMin = lMin;
    }

    private int treeRank(int idx) {
        return prefixSum.rank(idx + 1);
    }

    /**
     * 
     * @param index the LMax index of a node of the k2 raster tree
     * @return {@code true} if the node corresponding to the given index is an
     *         internal node, {@code false} otherwise.
     */
    public boolean hasChildren(int index) {
        if (index == 0)
            return minVal != maxVal;
        return tree.isSet(index);
    }

    /**
     * Gets the minimum and maximum values in the K2Raster tree
     * 
     * @return an array of length 2, where the first element is the minimum value
     *         and the second element is the maximum value
     */
    public Pair<Long, Long> getValueRange() {
        return new Pair<Long, Long>(minVal, maxVal);
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
        // the -1 is caused by lMax being 0-indexed and not including the root
        return parentMax - lMax.get(index - 1);
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

        int rank = treeRank(index - 1);
        return parentMin + lMin.get(rank - 1);
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
            int numInternalNodes = prefixSum.rank(index);
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
    protected long getCell(int n, int r, int c, int z, long maxVal) {
        int nKths = (n / k);
        z = treeRank(z) * k * k;
        z = z + (r / nKths) * k + (c / nKths);
        long val = lMax.get(z); // LMax is 0-indexed
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

    private void getWindow(int n, int r1, int r2, int c1, int c2, int z, long maxVal, PrimitiveArrayWrapper out,
            IntPointer index,
            int level, List<Pair<Integer, Integer>> indexRanks) {
        int nKths = (n / k);
        Pair<Integer, Integer> indexRank = indexRanks.get(level);
        int rank = treeRank(z);
        indexRank.first = z;
        indexRank.second = rank;

        z = rank * k * k;
        int initialI = r1 / nKths;
        int lastI = r2 / nKths;
        int initialJ = c1 / nKths;
        int lastJ = c2 / nKths;

        int r1p, r2p, c1p, c2p, zp;
        long maxValp;

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

                maxValp = computeVMax(maxVal, zp + 1);

                if (!hasChildren(zp + 1)) {
                    int times = ((r2p - r1p) + 1) * ((c2p - c1p) + 1);
                    for (int l = 0; l < times; l++) {
                        out.set(index.val++, maxValp);
                    }
                } else {
                    getWindow(nKths, r1p, r2p, c1p, c2p, zp, maxValp, out, index, level + 1, indexRanks);
                }

            }
        }
    }

    protected int getSize(int r1, int r2, int c1, int c2) {
        if (r1 < 0 || r1 >= n || r2 < 0 || r2 >= n || c1 < 0 || c1 >= n || c2 < 0
                || c2 >= n)
            throw new IndexOutOfBoundsException("looked up window (" + c1 + ", " + r1 + ", " + c2 + ", " + r2
                    + ") in matrix with size (" + n + ", " + n + ")");
        return (r2 - r1 + 1) * (c2 - c1 + 1);
    }

    protected PrimitiveArrayWrapper getWindow(int r1, int r2, int c1, int c2, PrimitiveArrayWrapper out) {
        int maxLevel = 1 + (int) Math.ceil(Math.log(n) / Math.log(k));
        GoodArrayList<Pair<Integer, Integer>> indexRanks = new GoodArrayList<Pair<Integer, Integer>>(maxLevel);
        for (int i = 0; i < maxLevel; i++) {
            indexRanks.set(i, new Pair<>(-1, 0));
        }
        getWindow(this.n, r1, r2, c1, c2, -1, this.maxVal, out, new IntPointer(), 0, indexRanks);

        return out;
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
    public abstract PrimitiveArrayWrapper getWindow(int r1, int r2, int c1, int c2);

    protected void searchValuesInWindow(int n, int r1, int r2, int c1, int c2, IRasterFilterFunction function,
            long maxVal, long minVal, int z, List<PixelRange> out, int baseX, int baseY) {
        int nKths = (n / k); // childsize
        int rank = treeRank(z);
        z = rank * k * k;
        int initialI = r1 / nKths;
        int lastI = r2 / nKths;
        int initialJ = c1 / nKths;
        int lastJ = c2 / nKths;

        int r1p, r2p, c1p, c2p, zp;
        long maxValp, minValp;

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
                maxValp = computeVMax(maxVal, zp + 1);

                boolean addCells = false;
                int baseXp = baseX + j * nKths;
                int baseYp = baseY + i * nKths;
                if (!hasChildren(zp + 1)) {
                    minValp = maxValp;
                    if (!function.containsOutside(minValp, maxValp)) {
                        addCells = true;
                        /* all cells meet the condition in this branch */
                    }
                } else {
                    minValp = computeVMin(maxVal, minVal, zp + 1);
                    if (!function.containsOutside(minValp, maxValp)) {
                        addCells = true;
                        /* all cells meet the condition in this branch */
                    } else {
                        if (!function.containsWithin(minValp, maxValp)) {
                            continue;
                        } else {
                            searchValuesInWindow(nKths, r1p, r2p, c1p, c2p, function, maxValp, minValp, zp,
                                    out, baseXp, baseYp);
                        }
                    }
                }

                if (addCells) {
                    for (int r = r1p + baseYp; r <= r2p + baseYp; r++) {
                        out.add(new PixelRange(r, c1p + baseXp, c2p + baseXp));
                    }
                }
            }
        }
    }

    public void searchValuesInWindow(int r1, int r2, int c1, int c2, IRasterFilterFunction function,
            List<PixelRange> out) {
        searchValuesInWindow(this.n, r1, r2, c1, c2, function,
                this.maxVal, this.minVal, -1, out,
                0, 0);
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
    public List<PixelRange> searchValuesInWindow(int r1, int r2, int c1, int c2, IRasterFilterFunction function) {
        List<PixelRange> out = new ArrayList<>();
        searchValuesInWindow(r1, r2, c1, c2, function, out);
        return out;
    }

    public void searchValuesInRanges(Map<Integer, List<PixelRange>> ranges, List<PixelRange> out, int r1, int r2,
            int c1,
            int c2, IRasterFilterFunction function) {
        searchValuesInRanges(ranges, out, r1, r2, c1, c2, -1, function, 0, 0, this.minVal, this.maxVal, this.n);
    }

    private void searchValuesInRanges(Map<Integer, List<PixelRange>> ranges, List<PixelRange> out, int r1, int r2,
            int c1,
            int c2, int z, IRasterFilterFunction function, int baseX, int baseY, long minVal, long maxVal, int n) {
        int nKths = (n / k); // childsize
        int rank = treeRank(z);
        z = rank * k * k;
        int initialI = r1 / nKths;
        int lastI = r2 / nKths;
        int initialJ = c1 / nKths;
        int lastJ = c2 / nKths;

        int r1p, r2p, c1p, c2p, zp;
        long maxValp, minValp;

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
                maxValp = computeVMax(maxVal, zp + 1);

                boolean addCells = false;
                int baseXp = baseX + j * nKths;
                int baseYp = baseY + i * nKths;
                if (!hasChildren(zp + 1)) {
                    minValp = maxValp;
                    if (!function.containsOutside(minValp, maxValp)) {
                        addCells = true;
                        /* all cells meet the condition in this branch */
                    }
                } else {
                    minValp = computeVMin(maxVal, minVal, zp + 1);
                    if (!function.containsOutside(minValp, maxValp)) {
                        addCells = true;
                        /* all cells meet the condition in this branch */
                    } else {
                        if (!function.containsWithin(minValp, maxValp)) {
                            continue;
                        } else {
                            searchValuesInRanges(ranges, out, r1p, r2p, c1p, c2p, zp, function, baseXp, baseYp, minValp,
                                    maxValp, nKths);
                        }
                    }
                }

                if (addCells) {
                    for (int r = r1p + baseYp; r <= r2p + baseYp; r++) {
                        if (ranges.containsKey(r)) {
                            for (PixelRange range : ranges.get(r)) {
                                if (range.x2 < c1p + baseXp || range.x1 > c2p + baseXp)
                                    continue;
                                out.add(new PixelRange(r, Math.max(range.x1, c1p + baseXp),
                                        Math.min(range.x2, c2p + baseXp)));
                            }
                        }
                    }
                }
            }
        }
    }
}
