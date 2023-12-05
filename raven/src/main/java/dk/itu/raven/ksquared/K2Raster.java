package dk.itu.raven.ksquared;

import java.util.ArrayList;
import java.util.List;

import dk.itu.raven.util.BitMap;
import dk.itu.raven.util.GoodArrayList;
import dk.itu.raven.util.GoodIntArrayList;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.util.Logger;

public class K2Raster {
    public static final int k = 2;

    private int maxval;
    private int minval;
    public BitMap Tree;
    // public DAC LMax;
    // public DAC LMin;
    private int[] LMax;
    private int[] LMin;
    // private int[] VMaxList;
    // private int[] VMinList;

    private int n;
    private int[] prefixsum;
    private int original_n, original_m;

    /**
     * bulds a K^2 Raster data-structure for an n*m matrix (meaning a 2-dimensional
     * array with {@code n} rows and {@code m} columns)
     * 
     * @param M the raw matrix data
     */
    public K2Raster(Matrix M) {
        int n = M.getHeight();
        int m = M.getWidth();

        // ensures n is a power of k even if the n from the input is not
        this.original_n = n;
        this.original_m = m;
        int real_n = 1;
        while (real_n < n || real_n < m) {
            real_n *= k;
        }
        this.n = real_n;

        int maxLevel = 1 + (int) Math.ceil(Math.log(Math.max(n, m)) / Math.log(k));
        List<BitMap> T = new ArrayList<>(maxLevel);
        List<GoodIntArrayList> Vmax = new ArrayList<GoodIntArrayList>(maxLevel);
        List<GoodIntArrayList> Vmin = new ArrayList<GoodIntArrayList>(maxLevel);
        List<GoodIntArrayList> parentMax = new ArrayList<>(maxLevel);
        List<GoodIntArrayList> parentMin = new ArrayList<>(maxLevel);
        int pmax[] = new int[maxLevel];
        int pmin[] = new int[maxLevel];
        int bitsSet[] = new int[maxLevel];
        for (int i = 0; i < maxLevel; i++) {
            T.add(new BitMap(40));
            Vmax.add(new GoodIntArrayList());
            Vmin.add(new GoodIntArrayList());
            parentMax.add(new GoodIntArrayList());
            parentMin.add(new GoodIntArrayList());
        }

        int[] res = Build(M, this.n, original_n, original_m, 1, 0, 0, T, Vmin, Vmax, pmax, pmin, parentMax, parentMin,
                0, 0, bitsSet);
        maxval = res[0];
        minval = res[1];
        Vmax.get(0).set(0, maxval);
        Vmin.get(0).set(0, minval);

        int minSize = 0;
        for (GoodIntArrayList p : Vmin) {
            minSize += p.size();
        }

        Logger.log("minSize: " + minSize);

        int size_max = 0;
        int size_min = 0;
        for (int i = 1; i < maxLevel; i++) {
            size_max += pmax[i];
            size_min += pmin[i];
        }

        Logger.log("size_max: " + size_max);
        Logger.log("size_min: " + size_min);

        int[] LMaxList = new int[size_max+1];
        int[] LMinList = new int[size_min+1];

        // VMinList = new int[size_min+1];
        // VMaxList = new int[size_max+1];
        // int[] VMaxParList = new int[size_max+1];
        // int[] VMinParList = new int[size_max+1];

        Tree = new BitMap(Math.max(1, size_max));
        int bitmapIndex = 0;

        for (int i = 0; i < maxLevel - 1; i++) {
            for (int j = 0; j < pmax[i]; j++) {
                if (T.get(i).isSet(j)) {
                    Tree.set(++bitmapIndex);
                } else {
                    Tree.unset(++bitmapIndex);
                }
            }
        }

        if (maxval != minval) {
            Tree.set(0);
        } else {
            Tree.unset(0);
        }

        prefixsum = new int[Tree.size()+1];
        prefixsum[0] = 0;
        for (int i = 1; i <= Tree.size(); i++) {
            prefixsum[i] = prefixsum[i - 1] + Tree.getOrZero(i);
        }

        int imax = 0, imin = 0;
        // int imin2 = 1;

        for (int i = 1; i < maxLevel - 1; i++)
            for (int j = 0; j < pmin[i]; j++) {
                int par = parentMin.get(i).get(j);
                int vminCur = Vmin.get(i).get(j);
                int vMinLast = Vmin.get(i - 1).get(par);
                // VMinList[imin++] = vminCur;
                // VMinParList[imin] = vMinLast;
                LMinList[imin++] = Math.abs(vminCur - vMinLast);
            }
            
        for (int i = 1; i < maxLevel; i++) {
            // int vMinIndex = 0;
            for (int j = 0; j < pmax[i]; j++) {
                int par = parentMax.get(i).get(j);
                int vMaxCur = Vmax.get(i).get(j);
                int vMaxLast = Vmax.get(i - 1).get(par);
                // VMaxList[imax++] = vMaxCur;
                // VMaxParList[imax] = vMaxLast;
                LMaxList[imax++] = Math.abs(vMaxLast - vMaxCur);
                // if (T.get(i).isSet(j)) {
                //     int minpar = parentMin.get(i).get(vMinIndex);
                //     int vminCur = Vmin.get(i).get(vMinIndex);
                //     int vMinLast = Vmin.get(i - 1).get(minpar);
                //     VMinParList[imin] = vMinLast;
                //     VMinList[imin++] = Vmin.get(i).get(vMinIndex++);
                //     LMinList[imin2++] = Math.abs(vminCur - vMinLast);
                // } else {
                //     VMinParList[imin] = VMaxParList[imin]; // probably wrong
                //     VMinList[imin++] = vMaxCur;
                // }
            }
        }
        

        // LMax = new DAC(LMaxList);
        // LMin = new DAC(LMinList);
        LMax = LMaxList;
        LMin = LMinList;

        // for (int i = 1; i < Tree.size()-1; i++) {
        //     Logger.log(Tree.get(i));
        //     Logger.log(VMinList[i]);
        //     Logger.log(VMinParList[i]);
        //     Logger.log(computeVMin(VMaxParList[i], VMinParList[i], i));
        //     Logger.log();
        //     if (VMinList[i] != computeVMin(VMaxParList[i], VMinParList[i], i)) {
        //         Logger.log("BOGUS: " + i);
        //     }
        // }
        
        // for (int i = 1; i < Tree.size(); i++) {
        //     Logger.log(Tree.get(i));
        //     Logger.log(VMaxList[i]);
        //     Logger.log(computeVMax(VMaxParList[i], i));

        //     if (VMaxList[i] != computeVMax(VMaxParList[i], i)) {
        //         Logger.log("BOGUS: " + i);
        //     }
        // }

        // for (int i = 1; i < Tree.size(); i++) {
        //     if (prefixsum[i] != Tree.rank(i)) {
        //         Logger.log("BOGUS Tree " + prefixsum[i] + ", " + Tree.rank(i));
        //     }
        // }
    }

    /**
     * Gets the minimum and maximum values in the K2Raster tree
     * 
     * @return an array of length 2, where the first element is the minimum value
     *         and the second element is the maximum value
     */
    public int[] getValueRange() {
        return new int[] { minval, maxval };
    }

    public int computeVMax(int parentMax, int index) {
        if (index == 0) return maxval;
        return parentMax - LMax[index];
        // return VMaxList[index];
    }
    
    public int computeVMin(int parentMax, int parentMin, int index) {
        if (index == 0) return minval;

        if (Tree.getOrZero(index) == 0) {
            return computeVMax(parentMax, index); // convert LMax index to the corresponding LMin index
        }
        return parentMin + LMin[prefixsum[index]];
        // Logger.log(Tree.size());
        // Logger.log(prefixsum.length);
        // Logger.log(VMaxList.length);
        // Logger.log(VMinList.length);
        // int prefix = prefixsum[index];
        // return VMinList[prefix];
    }

    /**
     * gets the children of the node at index {@code index}
     * 
     * @param index the index of the parent node
     * @return array of indxes
     */
    public int[] getChildren(int index) {
        if (!Tree.isSet(index)) {
            return new int[0];
        } else {
            int numInternalNodes = prefixsum[index];
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

    private static int[] Build(Matrix M, int n, int original_n, int original_m, int level, int row, int column,
            List<BitMap> T,
            List<GoodIntArrayList> Vmin, List<GoodIntArrayList> Vmax, int[] pmax, int[] pmin,
            List<GoodIntArrayList> parentsMax, List<GoodIntArrayList> parentsMin, int parentMax, int parentMin,
            int[] bitsSet) {

        int min, max;
        min = Integer.MAX_VALUE;
        max = 0;
        boolean lastlevel = n == k;
        int nKths = n / k;
        
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                int child = pmax[level];
                if (lastlevel) {
                    int matrix_value;
                    matrix_value = M.get(row + i, column + j);

                    if (min > matrix_value) {
                        min = matrix_value;
                    }
                    if (max < matrix_value) {
                        max = matrix_value;
                    }
                    T.get(level).unset(child);
                    Vmax.get(level).set(child, matrix_value);
                    parentsMax.get(level).set(child, parentMax);
                    pmax[level]++;
                } else {
                    int[] res = Build(M, nKths, original_n, original_m, level + 1, row + i * nKths, column + j * nKths,
                            T, Vmin, Vmax, pmax,
                            pmin, parentsMax, parentsMin,
                            T.get(level).size(), bitsSet[level], bitsSet);
                    // Logger.log(child);
                    int childMax = res[0];
                    int childMin = res[1];
                    Vmax.get(level).set(child, childMax);
                    parentsMax.get(level).set(child, parentMax);
                    pmax[level]++;
                    if (childMin != childMax) {
                        Vmin.get(level).set(pmin[level], childMin);
                        parentsMin.get(level).set(pmin[level], parentMin);
                        pmin[level]++;
                        T.get(level).set(child);
                        bitsSet[level]++;
                    } else {
                        // Vmin.get(level).set(pmin[level], childMin);
                        // pmin[level]++;
                        T.get(level).unset(child);
                    }
                    if (min > childMin) {
                        min = childMin;
                    }
                    if (max < childMax) {
                        max = childMax;
                    }
                }
            }
        }

        if (min == max) {
            pmax[level] = pmax[level] - k * k;
            T.get(level).setSize(pmax[level]);
        }

        return new int[] { max, min };
    }

    /**
     * Use of this method is discouraged for performance reasons. Use
     * {@code getWindow}
     * instead.
     * 🤬
     * 
     * @param n      size of the matrix
     * @param r      the row to access
     * @param c      the column to access
     * @param z      only God knows what this does
     * @param maxval the max value in the matrix
     * @return the value from the matrix at index {@code (r,c)}
     */
    private int getCell(int n, int r, int c, int z, int maxval) {
        int nKths = (n / k);
        z = this.Tree.rank(z) * k * k;
        z = z + (r / nKths) * k + (c / nKths);
        int val = LMax[z]; // 😡
        maxval = maxval - val;
        // maxval = VMaxList[z];
        if (z >= Tree.size() || Tree.getOrZero(z + 1) == 0) // 😡
            return maxval;
        return getCell(nKths, r % nKths, c % nKths, z, maxval);
    }

    /**
     * @param r the row to access
     * @param c the column to access
     * @return the value from the matrix at index {@code (r,c)}
     */
    public int getCell(int r, int c) {
        return getCell(this.n, r, c, -1, this.maxval);
    }

    private void getWindow(int n, int r1, int r2, int c1, int c2, int z, int maxval, int[] out,
            IntPointer index,
            int level, List<Pair<Integer, Integer>> indexRanks) {
        int nKths = (n / k);
        Pair<Integer, Integer> indexRank = indexRanks.get(level);
        int rank = (indexRank.second + this.Tree.rank(indexRank.first + 1, z));
        indexRank.first = z;
        indexRank.second = rank;

        z = rank * k * k;
        int initialI = r1 / nKths;
        int lastI = r2 / nKths;
        int initialJ = c1 / nKths;
        int lastJ = c2 / nKths;

        int r1p, r2p, c1p, c2p, maxvalp, zp;

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

                maxvalp = maxval - LMax[zp];
                // maxvalp = VMaxList[zp];
                if (zp + 1 >= Tree.size() || Tree.getOrZero(zp + 1) == 0) {
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
     * {@code (r1,c1)} and {@code (r2,c2)}
     * 
     * @param r1 row number for the top left corner of window
     * @param r2 row number for the bottom right corner of window
     * @param c1 column number for the top left corner of window
     * @param c2 column number for the bottom right corner of window
     * @return a window of the matrix
     */
    public int[] getWindow(int r1, int r2, int c1, int c2) {
        if (r1 < 0 || r1 >= original_n || r2 < 0 || r2 >= original_n || c1 < 0 || c1 >= original_m || c2 < 0
                || c2 >= original_m)
            throw new IndexOutOfBoundsException("looked up window (" + r1 + ", " + c1 + ", " + r2 + ", " + c2
                    + ") in matrix with size (" + original_n + ", " + original_m + ")");
        int returnSize = (r2 - r1 + 1) * (c2 - c1 + 1);
        int[] out = new int[returnSize];
        int maxLevel = 1 + (int) Math.ceil(Math.log(n) / Math.log(k));
        GoodArrayList<Pair<Integer, Integer>> indexRanks = new GoodArrayList<Pair<Integer, Integer>>(maxLevel);
        for (int i = 0; i < maxLevel; i++) {
            indexRanks.set(i, new Pair<>(-1, 0));
        }
        getWindow(this.n, r1, r2, c1, c2, -1, this.maxval, out, new IntPointer(), 0, indexRanks);

        return out;
    }

    private void searchValuesInWindow(int n, int r1, int r2, int c1, int c2, int z, int maxval, int minval, int vb,
            int ve, int[] out,
            IntPointer index,
            int level, List<Pair<Integer, Integer>> indexRanks) {
        int nKths = (n / k);
        Pair<Integer, Integer> indexRank = indexRanks.get(level);
        int rank = (indexRank.second + this.Tree.rank(indexRank.first + 1, z));
        indexRank.first = z;
        indexRank.second = rank;

        z = rank * k * k;
        int initialI = r1 / nKths;
        int lastI = r2 / nKths;
        int initialJ = c1 / nKths;
        int lastJ = c2 / nKths;

        int r1p, r2p, c1p, c2p, maxvalp, minvalp, zp;

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

                maxvalp = maxval - LMax[zp];
                // maxvalp = VMaxList[zp];
                if (zp + 1 >= Tree.size() || Tree.getOrZero(zp + 1) == 0) {
                    minvalp = maxvalp;
                    if (minvalp >= vb && maxvalp <= ve) {

                        /* all cells meet the condition in this branch */
                        // int times = ((r2p - r1p) + 1) * ((c2p - c1p) + 1);
                        // for (int l = 0; l < times; l++) {
                        // out[index.index++] = maxvalp;
                        // }
                    }
                } else {
                    minvalp = LMin[this.Tree.rank(zp)];
                    // minvalp = VMinList[prefixsum[zp]];
                    if (minvalp >= vb && maxvalp <= ve) {
                        /* all cells meet the condition in this branch */
                        // int times = ((r2p - r1p) + 1) * ((c2p - c1p) + 1);
                        // for (int l = 0; l < times; l++) {
                        // out[index.index++] = maxvalp;
                        // }
                    }
                    if (minvalp < vb && maxvalp > ve) {
                        searchValuesInWindow(nKths, r1p, r2p, c1p, c2p, zp, maxvalp, minvalp, vb, ve, out, index,
                                level + 1,
                                indexRanks);
                    }
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
     * @return a window of the matrix
     */
    public int[] searchValuesInWindow(int r1, int r2, int c1, int c2, int thresholdLow, int thresholdHigh) {
        if (r1 < 0 || r1 >= original_n || r2 < 0 || r2 >= original_n || c1 < 0 || c1 >= original_m || c2 < 0
                || c2 >= original_m)
            throw new IndexOutOfBoundsException("looked up window (" + r1 + ", " + c1 + ", " + r2 + ", " + c2
                    + ") in matrix with size (" + original_n + ", " + original_m + ")");
        int returnSize = (r2 - r1 + 1) * (c2 - c1 + 1); // can be smaller.
        int[] out = new int[returnSize];
        int maxLevel = 1 + (int) Math.ceil(Math.log(n) / Math.log(k));
        GoodArrayList<Pair<Integer, Integer>> indexRanks = new GoodArrayList<Pair<Integer, Integer>>(maxLevel);
        for (int i = 0; i < maxLevel; i++) {
            indexRanks.set(i, new Pair<>(-1, 0));
        }
        searchValuesInWindow(this.n, r1, r2, c1, c2, -1, this.maxval, this.minval, thresholdLow, thresholdHigh, out,
                new IntPointer(), 0,
                indexRanks);

        return out;
    }
}
