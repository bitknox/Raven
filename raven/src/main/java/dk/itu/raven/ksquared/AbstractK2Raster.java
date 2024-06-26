package dk.itu.raven.ksquared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import dk.itu.raven.geometry.Offset;
import dk.itu.raven.join.IRasterFilterFunction;
import dk.itu.raven.join.RangeExtremes;
import dk.itu.raven.join.results.IResult;
import dk.itu.raven.join.results.IResultCreator;
import dk.itu.raven.join.results.PixelRange;
import dk.itu.raven.join.results.PixelValueCreator;
import dk.itu.raven.ksquared.dac.AbstractDAC;
import dk.itu.raven.util.BitMap;
import dk.itu.raven.util.GoodArrayList;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.PrimitiveArrayWrapper;

public abstract class AbstractK2Raster implements Serializable {
    public final int k;
    protected long minVal;// the maximum value stored in the matrix
    protected long maxVal;// the minimum value stored in the matrix
    public BitMap tree; // A tree where the i'th index is a one iff. the node with index i is internal
    protected final int n; // the size of the matrix, always a power of k
    protected IntRank prefixSum; // a prefix sum of the tree

    protected AbstractDAC lMin;// stores the difference between the minimum value stored in a node and the
    // minimum value of its parent node
    protected AbstractDAC lMax;// stores the difference between the maximum value stored in a node and the
    // maximum value of its parent node

    protected transient IResultCreator resultCreator;

    public void setResultCreator(IResultCreator resultCreator) {
        this.resultCreator = resultCreator;
    }

    public IResultCreator getResultCreator() {
        return resultCreator;
    }

    public AbstractK2Raster(int k, long minVal, long maxVal, BitMap tree, int n, IntRank prefixSum,
            AbstractDAC lMin, AbstractDAC lMax) {
        this.resultCreator = new PixelValueCreator();
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
        return tree.isSet2(index);
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
        // the -1 is caused by lMax being 0-indexed and not including the root
        if (index == 0)
            return maxVal;
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

    public long computeVMin(long parentMax, long parentMin, int index, long vMax) {
        if (index == 0)
            return minVal;
        if (!hasChildren(index)) {
            return vMax;
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
            IntPointer index) {
        final int nKths = (n / k); // childsize
        final int rank = treeRank(z);
        final int initialI = r1 / nKths;
        final int lastI = r2 / nKths;
        final int initialJ = c1 / nKths;
        final int lastJ = c2 / nKths;

        z = rank * k * k;

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
                    getWindow(nKths, r1p, r2p, c1p, c2p, zp, maxValp, out, index);
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
        getWindow(this.n, r1, r2, c1, c2, -1, this.maxVal, out, new IntPointer());

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
                    minValp = computeVMin(maxVal, minVal, zp + 1, maxValp);
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

    public void searchValuesInRanges(List<PixelRange> ranges,
            List<IResult> out, Offset<Integer> offset, int r1,
            int r2, int c1, int c2, RangeExtremes[] rangeLimits, int[] rangePrefixsum, IRasterFilterFunction function) {
        searchValuesInRanges(ranges, out, offset, r1, r2, c1, c2, rangeLimits, rangePrefixsum, function, -1, 0,
                0, 0, this.minVal, this.maxVal, this.n);
    }

    public void searchValuesInRanges(List<PixelRange> ranges,
            List<IResult> out, Offset<Integer> offset, int r1, int r2, int c1, int c2, RangeExtremes[] rangeLimits,
            int[] rangePrefixsum, IRasterFilterFunction function, int z, int treeIndex, int baseX, int baseY,
            long minVal, long maxVal, int n) {
        searchValuesInRanges(ranges, out, offset, r1, r2, c1, c2, rangeLimits, rangePrefixsum, z, treeIndex, function,
                baseX, baseY, minVal, maxVal, n, new Offset<Integer>(baseX, baseY));
    }

    private void searchValuesInRanges(List<PixelRange> ranges, List<IResult> out, Offset<Integer> offset, int r1,
            int r2, int c1, int c2, RangeExtremes[] rangeLimits, int[] rangePrefixsum, int z, int treeIndex,
            IRasterFilterFunction function, int baseX, int baseY, long minVal, long maxVal, int n,
            Offset<Integer> treeOffset) {
        final int nKths = (n / k); // childsize
        final int rank = treeRank(z);
        final int initialI = r1 / nKths;
        final int lastI = r2 / nKths;
        final int initialJ = c1 / nKths;
        final int lastJ = c2 / nKths;
        z = rank * k * k;

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
                boolean hasChildren = hasChildren(zp + 1);
                int baseXp = baseX + j * nKths;
                int baseYp = baseY + i * nKths;

                int xValue1 = c1p + baseXp + offset.getX();
                int xValue2 = c2p + baseXp + offset.getX();

                int treeIndexp = treeIndex * k + i + 1;
                if (!hasChildren) {
                    minValp = maxValp;
                    if (!function.containsOutside(minValp, maxValp)) {
                        addCells = true;
                        /* all cells meet the condition in this branch */
                    }
                } else {
                    minValp = computeVMin(maxVal, minVal, zp + 1, maxValp);
                    if (!function.containsOutside(minValp, maxValp)) {
                        addCells = true;
                        /* all cells meet the condition in this branch */
                    } else {
                        boolean containsNoRanges = rangeLimits[treeIndexp].x1 > c2p + baseXp - treeOffset.getX()
                                || rangeLimits[treeIndexp].x2 < c1p + baseXp - treeOffset.getX();
                        if (containsNoRanges || !function.containsWithin(minValp, maxValp)) {
                            continue;
                        } else {
                            searchValuesInRanges(ranges, out, offset, r1p, r2p, c1p, c2p, rangeLimits, rangePrefixsum,
                                    zp, treeIndexp, function, baseXp, baseYp, minValp, maxValp, nKths, treeOffset);
                        }
                    }
                }
                if (addCells) {
                    int rStart = r1p + baseYp - treeOffset.getY();
                    int rEnd = r2p + baseYp - treeOffset.getY();
                    if (resultCreator.hasValues() && hasChildren) {
                        getWithinRanges(ranges, out, offset, r1p, r2p, c1p, c2p, rangeLimits, rangePrefixsum, zp,
                                treeIndexp, baseXp, baseYp, maxValp, nKths, treeOffset);
                    } else {
                        for (int l = rangePrefixsum[rStart]; l < rangePrefixsum[rEnd + 1]; l++) {
                            PixelRange range = ranges.get(l);

                            if (range.x2 < xValue1 || range.x1 > xValue2) {
                                continue;
                            }

                            resultCreator.createResults(new PixelRange(range.row,
                                    Math.max(range.x1, xValue1),
                                    Math.min(range.x2, xValue2)), maxValp, out);

                        }
                    }
                }
            }
        }
    }

    public void getWithinRanges(List<PixelRange> ranges,
            List<IResult> out, Offset<Integer> offset, int r1, int r2, int c1, int c2, RangeExtremes[] rangeLimits,
            int[] rangePrefixsum, int z, int treeIndex, int baseX, int baseY,
            long maxVal, int n) {
        getWithinRanges(ranges, out, offset, r1, r2, c1, c2, rangeLimits, rangePrefixsum, z, treeIndex,
                baseX, baseY, maxVal, n, new Offset<Integer>(baseX, baseY));
    }

    private void getWithinRanges(List<PixelRange> ranges, List<IResult> out, Offset<Integer> offset, int r1,
            int r2, int c1, int c2, RangeExtremes[] rangeLimits, int[] rangePrefixsum, int z, int treeIndex,
            int baseX, int baseY, long maxVal, int n, Offset<Integer> treeOffset) {
        final int nKths = (n / k); // childsize
        final int rank = treeRank(z);
        final int initialI = r1 / nKths;
        final int lastI = r2 / nKths;
        final int initialJ = c1 / nKths;
        final int lastJ = c2 / nKths;
        z = rank * k * k;

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

                boolean addCells = false;
                int baseXp = baseX + j * nKths;
                int baseYp = baseY + i * nKths;

                int xValue1 = c1p + baseXp + offset.getX();
                int xValue2 = c2p + baseXp + offset.getX();

                int treeIndexp = treeIndex * k + i + 1;
                if (!hasChildren(zp + 1)) {
                    maxValp = computeVMax(maxVal, zp + 1);
                    addCells = true;
                } else {
                    boolean containsNoRanges = rangeLimits[treeIndexp].x1 > c2p + baseXp - treeOffset.getX()
                            || rangeLimits[treeIndexp].x2 < c1p + baseXp - treeOffset.getX();
                    if (containsNoRanges) {
                        continue;
                    } else {
                        maxValp = computeVMax(maxVal, zp + 1);
                        getWithinRanges(ranges, out, offset, r1p, r2p, c1p, c2p, rangeLimits, rangePrefixsum,
                                zp, treeIndexp, baseXp, baseYp, maxValp, nKths, treeOffset);
                    }
                }
                if (addCells) {
                    int rStart = r1p + baseYp - treeOffset.getY();
                    int rEnd = r2p + baseYp - treeOffset.getY();
                    for (int l = rangePrefixsum[rStart]; l < rangePrefixsum[rEnd + 1]; l++) {
                        PixelRange range = ranges.get(l);

                        if (range.x2 < xValue1 || range.x1 > xValue2) {
                            continue;
                        }

                        int x1 = Math.max(range.x1, c1p + baseXp + offset.getX());
                        int x2 = Math.min(range.x2, c2p + baseXp + offset.getX());
                        resultCreator.createResults(new PixelRange(range.row, x1, x2), maxValp, out);
                    }
                }
            }
        }
    }
}
