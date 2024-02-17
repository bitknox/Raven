package dk.itu.raven.join;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

public class JoinFilterFunctionsTests {
    private class FenwickTree {
        int[] arr;
        int size;

        public FenwickTree(int size) {
            this.arr = new int[size + 1];
            this.size = size;
        }

        public int query(int idx) {
            if (idx == -1)
                return 0;
            idx++;
            int sum = 0;

            while (idx > 0) {
                sum += arr[idx];
                idx -= idx & (-idx);
            }

            return sum;
        }

        public void update(int idx, int value) {
            idx++;
            while (idx <= size) {
                arr[idx] += value;
                idx += idx & (-idx);
            }
        }

        public void clear() {
            this.arr = new int[this.size + 1];
        }
    }

    @Test
    public void testMultiSampleRangeFilterExhaustive() {
        int bits = 2;
        FenwickTree treeWithin = new FenwickTree(1 << (3 * bits));
        FenwickTree treeOutside = new FenwickTree(1 << (3 * bits));
        for (long lo1 = 0; lo1 < 1 << bits; lo1++) {
            for (long hi1 = lo1; hi1 < 1 << bits; hi1++) {
                for (long lo2 = 0; lo2 < 1 << bits; lo2++) {
                    for (long hi2 = lo2; hi2 < 1 << bits; hi2++) {
                        for (long lo3 = 0; lo3 < 1 << bits; lo3++) {
                            for (long hi3 = lo3; hi3 < 1 << bits; hi3++) {
                                RasterFilterFunction function = JoinFilterFunctions.multiSampleRangeFilter(
                                        Arrays.asList(lo1, hi1, lo2, hi2, lo3, hi3), new int[] { bits, bits, bits },
                                        bits * 3);
                                treeWithin.clear();
                                treeOutside.clear();
                                for (int i = 0; i < 1 << (3 * bits); i++) {
                                    int first = i >> (2 * bits);
                                    int second = (i >> (bits)) & ((1 << bits) - 1);
                                    int third = i & ((1 << bits) - 1);
                                    if (first <= hi1 && first >= lo1 &&
                                            second <= hi2 && second >= lo2 &&
                                            third <= hi3 && third >= lo3) {
                                        treeWithin.update(i, 1);
                                    } else {
                                        treeOutside.update(i, 1);
                                    }
                                }

                                for (int lo = 0; lo < 1 << (3 * bits); lo++) {
                                    for (int hi = lo; hi < 1 << (3 * bits); hi++) {
                                        if (function.containsWithin(lo, hi)) {
                                            assertTrue(treeWithin.query(lo - 1) != treeWithin.query(hi));
                                        }
                                        if (treeWithin.query(lo - 1) != treeWithin.query(hi)) {
                                            if (!(function.containsWithin(lo, hi))) {
                                                System.err.println("lo: " + lo + ", hi: " + hi);
                                                for (long stop : Arrays.asList(lo1, hi1, lo2, hi2, lo3, hi3)) {
                                                    System.err.println(stop);
                                                }
                                            }
                                            assertTrue(function.containsWithin(lo, hi));
                                        }
                                        // TODO:
                                        // if (function.containsOutside(lo, hi)) {
                                        //     assertTrue(treeOutside.query(lo - 1) != treeOutside.query(hi));
                                        // }
                                        // if (treeOutside.query(lo - 1) != treeOutside.query(hi)) {
                                        //     assertTrue(function.containsOutside(lo, hi));
                                        // }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test 
    public void testMultiSampleRangeFilterCustom() {
        int[] samples = new int[] {4,4,4};
        int totalBits = 12;
        // 0000-0010    0001-0001   1000-1010
        List<Long> ranges = Arrays.asList(0L,2L,1L,1L,8L,10L);

        RasterFilterFunction function = JoinFilterFunctions.multiSampleRangeFilter(ranges, samples, totalBits);

        // 0011 0000 0000
        int hi = (3<<8) + (0<<4) + (0); 
        // 0000 0000 0000
        int lo = (0<<8) + (0<<4) + (0);

        
        // range contains (for example) 0010 0001 1000
        int example = (2<<8) + (1<<4) + (8);

        assertTrue(lo <= example && example <= hi);
        assertTrue(function.containsWithin(lo, hi));
    }

    @Test
    public void testMultiSampleRangeFilterRandom() {
        int bits = 8;
        int tests = 100_000;

        for (int test = 0; test < tests; test++) {
            // TODO:
        }
    }

    @Test
    public void testMultiSampleRangeFilterRandomWithConstantRanges() {
        int bits = 5;
        int tests = 10_000_000;

        long lo1 = 0;
        long hi1 = (1<<bits)-1;
        long lo2 = 0;
        long hi2 = 0;
        long lo3 = 14;
        long hi3 = 17;

        FenwickTree treeWithin = new FenwickTree(1<<(3*bits));
        FenwickTree treeOutside = new FenwickTree(1<<(3*bits));

        // TODO: use fenwick with range updates
        for (int num = 0; num < 1<<(3*bits); num++) {
            int first = num >> (2 * bits);
            int second = (num >> (bits)) & ((1 << bits) - 1);
            int third = num & ((1 << bits) - 1);
            if (first <= hi1 && first >= lo1 &&
                    second <= hi2 && second >= lo2 &&
                    third <= hi3 && third >= lo3) {
                treeWithin.update(num, 1);
            } else {
                treeOutside.update(num, 1);
            }
        }

        List<Long> ranges = Arrays.asList(lo1,hi1,lo2,hi2,lo3,hi3);
        Random r = new Random(42);

        RasterFilterFunction function = JoinFilterFunctions.multiSampleRangeFilter(ranges, new int[] {bits,bits,bits}, 3*bits);

        for (int test = 0; test < tests; test++) {

            int lo = r.nextInt();
            if (lo < 0) lo *= -1;
            int hi = r.nextInt();
            if (hi < 0) hi *= -1;
            lo %= (1<<(3*bits));
            hi %= (1<<(3*bits));
            if (lo > hi) {
                int temp = lo;
                lo = hi;
                hi = temp;
            }

            if (function.containsWithin(lo, hi)) {
                assertTrue(treeWithin.query(lo - 1) != treeWithin.query(hi));
            }
            if (treeWithin.query(lo - 1) != treeWithin.query(hi)) {
                assertTrue(function.containsWithin(lo, hi));
            }
            // TODO:
            // if (function.containsOutside(lo, hi)) {
            //     if (!(treeOutside.query(lo - 1) != treeOutside.query(hi))) {
            //         System.err.println("lo: " + lo + ", hi: " + hi);
            //         for (long stop : Arrays.asList(lo1, hi1, lo2, hi2, lo3, hi3)) {
            //             System.err.println(stop);
            //         }
            //     }
            //     assertTrue(treeOutside.query(lo - 1) != treeOutside.query(hi));
            // }
            // if (treeOutside.query(lo - 1) != treeOutside.query(hi)) {
            //     assertTrue(function.containsOutside(lo, hi));
            // }

        }
    }
}
