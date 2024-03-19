package dk.itu.raven.ksquared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import dk.itu.raven.util.BitMap;
import dk.itu.raven.util.GoodLongArrayList;
import dk.itu.raven.util.LongArrayWrapper;
import dk.itu.raven.util.PrimitiveArrayWrapper;

public class DAC implements Serializable {
    int numValues;
    int levels;
    int vByteBitSize;
    int originalBitSize;
    int[] blockSizes;
    int[] blockSizesPrefixSum;
    List<long[]> A;
    List<IntRank> B;

    private long maxValue;

    private static final int FACT_RANK = 20;

    public DAC(PrimitiveArrayWrapper values) {
        this.numValues = values.length();

        this.A = new ArrayList<>();
        this.B = new ArrayList<>();

        this.blockSizes = this.optimize(values, FACT_RANK);
        blockSizesPrefixSum = new int[blockSizes.length + 1];
        blockSizesPrefixSum[0] = 0;
        for (int i = 1; i <= blockSizes.length; i++) {
            blockSizesPrefixSum[i] = blockSizesPrefixSum[i - 1] + blockSizes[i - 1];
        }
        this.levels = blockSizes.length;
        this.originalBitSize = values.length() * Math.max(1, Basics.bits(maxValue));
        this.vByteBitSize = 0;
        for (int i = 0; i < values.length(); i++) {
            this.vByteBitSize += 8 * Math.ceil(Math.log(values.get(i) + 1) / (Math.log(2) * 7.0));
        }

        for (int level = 0; level < levels; level++) {
            int blockSize = blockSizes[level];
            long[] Ai = new long[values.length()];
            long[] Aj = new long[values.length()];

            for (int i = 0; i < values.length(); i++) {
                long x = values.get(i);
                Ai[i] = x & ((1L << blockSize) - 1); // TODO: if blockSize is 64 this breaks
                Aj[i] = x >> blockSize;
            }

            BitMap continuation_bits = new BitMap(Aj.length);
            for (int i = 0; i < Aj.length; i++) {
                if (Aj[i] > 0) {
                    continuation_bits.set(i);
                } else {
                    continuation_bits.unset(i);
                }
            }

            this.A.add(Ai);
            if (level < this.levels - 1) {
                this.B.add(new IntRank(continuation_bits.getMap(), continuation_bits.size()));
            }

            GoodLongArrayList vals = new GoodLongArrayList();

            for (int i = 0; i < Aj.length; i++) {
                if (Aj[i] > 0) {
                    vals.add(Aj[i]);
                }
            }

            values = new LongArrayWrapper(vals);
        }
    }

    public long get(int index) {
        long item = 0;

        for (int level = 0; level < this.A.size(); level++) {
            item += this.A.get(level)[index] << this.blockSizesPrefixSum[level];
            if (level >= this.B.size()) {
                break;
            }
            if (this.B.get(level).get(index)) {
                index = this.B.get(level).rank(index) - 1;
            } else {
                break;
            }
        }

        return item;
    }

    private int[] optimize(PrimitiveArrayWrapper array, int FACT_RANK) {
        this.maxValue = 0;
        TreeMap<Integer, Long> hist = new TreeMap<>();
        for (int i = 0; i < array.length(); i++) {
            long val = array.get(i);
            int bitLength = Math.max(1, Basics.bits(val));
            Long count = hist.get(bitLength);
            if (count == null) {
                count = 0L;
            }
            hist.put(bitLength, count + 1);
            if (val > this.maxValue) {
                this.maxValue = val;
            }
        }
        int m;
        if (maxValue == 0) {
            m = 0;
        } else {
            m = (int) (Math.log(maxValue) / Math.log(2));
        }
        long[] fc = new long[m + 2];
        // Basics.bits(m)

        for (var entry : hist.entrySet()) {
            int i = entry.getKey();
            long c = entry.getValue();
            fc[i - 1] = c;
        }

        for (int i = 1; i < fc.length; i++) {
            fc[i] = fc[i - 1] + fc[i];
        }

        long[] s = new long[m + 1];
        int[] l = new int[m + 1];
        int[] b = new int[m + 1];

        for (int t = m; t >= 0; t--) {
            long minSize = Long.MAX_VALUE;
            int minPos = m;

            for (int i = m - 1; i >= t + 1; i--) {
                long currentSize = s[i] + (fc[m + 1] - fc[t]) * (i - t + 1) + (fc[m + 1] - fc[t]) / FACT_RANK;
                if (minSize > currentSize) {
                    minSize = currentSize;
                    minPos = i;
                }
            }

            if (minSize < (fc[m + 1] - fc[t]) * (m - t + 1)) {
                s[t] = minSize;
                l[t] = l[minPos] + 1;
                b[t] = minPos - t;
            } else {
                s[t] = (fc[m + 1] - fc[t]) * (m - t + 1);
                l[t] = 1;
                b[t] = (m - t + 1);
            }
        }

        int L = l[0];
        int[] kvalues = new int[L];
        int t = 0;

        for (int k = 0; k < L; k++) {
            kvalues[k] = b[t];
            t += b[t];
        }

        return kvalues;
    }
}
