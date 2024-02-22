package dk.itu.raven.join;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

public class JoinFilterFunctionsTests {
    @Test
    public void testMultiSampleRangeFilterExhaustive() {
        int bits = 2;
        // used to quickly verify that the function gives correct results
        int[] prefixSumWithin = new int[(1 << (3 * bits)) + 1];
        int[] prefixSumOutside = new int[(1 << (3 * bits)) + 1];
        // go through all possible search ranges using the allotted bits
        for (long lo1 = 0; lo1 < 1 << bits; lo1++) {
            for (long hi1 = lo1; hi1 < 1 << bits; hi1++) {
                for (long lo2 = 0; lo2 < 1 << bits; lo2++) {
                    for (long hi2 = lo2; hi2 < 1 << bits; hi2++) {
                        for (long lo3 = 0; lo3 < 1 << bits; lo3++) {
                            for (long hi3 = lo3; hi3 < 1 << bits; hi3++) {
                                IRasterFilterFunction function = JoinFilterFunctions.multiSampleRangeFilter(
                                        Arrays.asList(lo1, hi1, lo2, hi2, lo3, hi3), new int[] { bits, bits, bits },
                                        bits * 3);
                                prefixSumWithin[0] = 0;
                                prefixSumOutside[0] = 0;

                                // compute a prefix sum array for the number of matches for both methods in the
                                // function for all possible numbers using the allotted bits
                                for (int num = 0; num < 1 << (3 * bits); num++) {
                                    int first = num >> (2 * bits);
                                    int second = (num >> (bits)) & ((1 << bits) - 1);
                                    int third = num & ((1 << bits) - 1);
                                    prefixSumWithin[num + 1] = prefixSumWithin[num];
                                    prefixSumOutside[num + 1] = prefixSumOutside[num];
                                    if (first <= hi1 && first >= lo1 &&
                                            second <= hi2 && second >= lo2 &&
                                            third <= hi3 && third >= lo3) {
                                        prefixSumWithin[num + 1]++;
                                    } else {
                                        prefixSumOutside[num + 1]++;
                                    }
                                }

                                // go through all possible lo and hi combinations using the allotted bits
                                for (int lo = 0; lo < 1 << (3 * bits); lo++) {
                                    for (int hi = lo; hi < 1 << (3 * bits); hi++) {
                                        // the function says there are some matching numbers within the range, check
                                        // that
                                        // this is true
                                        if (function.containsWithin(lo, hi)) {
                                            assertTrue(prefixSumWithin[lo] != prefixSumWithin[hi + 1]);
                                        }
                                        // there are some matching numbers within the the range, check that the function
                                        // agrees
                                        if (prefixSumWithin[lo] != prefixSumWithin[hi + 1]) {
                                            assertTrue(function.containsWithin(lo, hi));
                                        }
                                        // the function says there are some non-matching numbers within the range, check
                                        // that
                                        // this is true
                                        if (function.containsOutside(lo, hi)) {
                                            assertTrue(prefixSumOutside[lo] != prefixSumOutside[hi + 1]);
                                        }
                                        // there are some non-matching numbers within the the range, check that the
                                        // function
                                        // agrees
                                        if (prefixSumOutside[lo] != prefixSumOutside[hi + 1]) {
                                            assertTrue(function.containsOutside(lo, hi));
                                        }
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
        int[] samples = new int[] { 4, 4, 4 };
        int totalBits = 12;
        // 0000-0010 0001-0001 1000-1010
        List<Long> ranges = Arrays.asList(0L, 2L, 1L, 1L, 8L, 10L);

        IRasterFilterFunction function = JoinFilterFunctions.multiSampleRangeFilter(ranges, samples, totalBits);

        // 0011 0000 0000
        int hi = (3 << 8) + (0 << 4) + (0);
        // 0000 0000 0000
        int lo = (0 << 8) + (0 << 4) + (0);

        // range contains (for example) 0010 0001 1000
        int example = (2 << 8) + (1 << 4) + (8);

        assertTrue(lo <= example && example <= hi);
        assertTrue(function.containsWithin(lo, hi));

        // for example 0011 0000 0000, which does not satisfy the first range
        assertTrue(function.containsOutside(lo, hi));
    }

    @Test
    public void testMultiSampleRangeFilterRandomWithConstantRanges() {
        int bits = 6;
        int tests = 10_000_000;

        long lo1 = 0;
        long hi1 = (1 << bits) - 1;
        long lo2 = 0;
        long hi2 = 0;
        long lo3 = 14;
        long hi3 = 17;

        // compute prefix sums for both numbers within and outside the range
        int[] prefixSumWithin = new int[(1 << (3 * bits)) + 1];
        int[] prefixSumOutside = new int[(1 << (3 * bits)) + 1];

        prefixSumWithin[0] = 0;
        prefixSumOutside[0] = 0;
        for (int num = 0; num < 1 << (3 * bits); num++) {
            prefixSumWithin[num + 1] = prefixSumWithin[num];
            prefixSumOutside[num + 1] = prefixSumOutside[num];
            int first = num >> (2 * bits);
            int second = (num >> (bits)) & ((1 << bits) - 1);
            int third = num & ((1 << bits) - 1);
            if (first <= hi1 && first >= lo1 &&
                    second <= hi2 && second >= lo2 &&
                    third <= hi3 && third >= lo3) {
                prefixSumWithin[num + 1]++;
            } else {
                prefixSumOutside[num + 1]++;
            }
        }

        List<Long> ranges = Arrays.asList(lo1, hi1, lo2, hi2, lo3, hi3);
        Random r = new Random(42);

        IRasterFilterFunction function = JoinFilterFunctions.multiSampleRangeFilter(ranges,
                new int[] { bits, bits, bits }, 3 * bits);

        // generate random lo and hi values and check that the function outputs the
        // correct numbers for all of them
        for (int test = 0; test < tests; test++) {
            int lo = r.nextInt();
            if (lo < 0)
                lo *= -1;
            int hi = r.nextInt();
            if (hi < 0)
                hi *= -1;
            lo %= (1 << (3 * bits));
            hi %= (1 << (3 * bits));
            if (lo > hi) {
                int temp = lo;
                lo = hi;
                hi = temp;
            }

            // the function says there are some matching numbers within the range, check
            // that
            // this is true
            if (function.containsWithin(lo, hi)) {
                assertTrue(prefixSumWithin[lo] != prefixSumWithin[hi + 1]);
            }
            // there are some matching numbers within the the range, check that the function
            // agrees
            if (prefixSumWithin[lo] != prefixSumWithin[hi + 1]) {
                assertTrue(function.containsWithin(lo, hi));
            }
            // the function says there are some non-matching numbers within the range, check
            // that
            // this is true
            if (function.containsOutside(lo, hi)) {
                assertTrue(prefixSumOutside[lo] != prefixSumOutside[hi + 1]);
            }
            // there are some non-matching numbers within the the range, check that the
            // function
            // agrees
            if (prefixSumOutside[lo] != prefixSumOutside[hi + 1]) {
                assertTrue(function.containsOutside(lo, hi));
            }

        }
    }
}
