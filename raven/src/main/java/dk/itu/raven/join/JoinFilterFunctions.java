package dk.itu.raven.join;

import java.util.List;

public abstract class JoinFilterFunctions {
    public static RasterFilterFunction rangeFilter(long lo, long hi) {
        return new RasterFilterFunction() {
            @Override
            public boolean containsWithin(long lo2, long hi2) {
                return lo2 <= hi && lo <= hi2;
            }

            @Override
            public boolean containsOutside(long lo2, long hi2) {
                return lo2 < lo || hi < hi2;
            }
        };
    }

    // TODO: Explain this with detailed comments. & Test this function
    public static RasterFilterFunction multiSampleRangeFilter(List<Long> ranges, int[] sampleSize, int totalBits) {
        return new RasterFilterFunction() {
            @Override
            public boolean containsWithin(long lo, long hi) {
                int bitsRemaining = totalBits;
                for (int i = 0; i < sampleSize.length; i++) {
                    long mask = (1 << sampleSize[i]) - 1;
                    long lom = (lo >> (bitsRemaining - sampleSize[i])) & mask;
                    long him = (hi >> (bitsRemaining - sampleSize[i])) & mask;
                    long min = ranges.get(2 * i);
                    long max = ranges.get(2 * i + 1);
                    // do logic
                    if (!(lom <= max && min <= him))
                        return false; //
                    if ((him - 1 >= lom) && lom <= (max - 1) && min <= him)
                        return true; //

                    bitsRemaining -= sampleSize[i];
                }
                return true;
            }

            // TODO: Test this function
            @Override
            public boolean containsOutside(long lo, long hi) {
                int bitsRemaining = totalBits;
                for (int i = 0; i < sampleSize.length; i++) {
                    long mask = (1 << sampleSize[i]) - 1;
                    long lom = (lo >> (bitsRemaining - sampleSize[i])) & mask;
                    long him = (hi >> (bitsRemaining - sampleSize[i])) & mask;
                    long min = ranges.get(2 * i);
                    long max = ranges.get(2 * i + 1);

                    // Check if the entire range is outside the specified criteria
                    if (him < min || lom > max) {
                        return true;
                    }

                    // Check if part of the range is outside the specified criteria
                    if ((him - 1 >= min && him - 1 <= max) || (lom <= max - 1 && lom >= min)) {
                        return true;
                    }

                    bitsRemaining -= sampleSize[i];
                }
                return false;
            }
        };
    }
}
