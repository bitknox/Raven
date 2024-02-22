package dk.itu.raven.join;

import java.util.List;

public abstract class JoinFilterFunctions {
    public static IRasterFilterFunction acceptAll() {
        return new IRasterFilterFunction() {
            @Override
            public boolean containsWithin(long lo, long hi) {
                return true;
            }

            @Override
            public boolean containsOutside(long lo, long hi) {
                return false;
            }
        };
    }

    public static IRasterFilterFunction rangeFilter(long lo, long hi) {
        return new IRasterFilterFunction() {
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

    public static IRasterFilterFunction multiSampleRangeFilter(List<Long> ranges, int[] sampleSize, int totalBits) {
        return new IRasterFilterFunction() {
            @Override
            public boolean containsWithin(long lo, long hi) {
                boolean lowerMet = false; // signifies that we are allowed to set all further lom's to 0
                boolean higherMet = false; // signifies that we are allowed to set all further him's to their maximum
                                           // possible value
                boolean eitherMet = false; // signifies that we can have either the higherMet OR the lowerMet set to
                                           // true, but not both
                int bitsRemaining = totalBits;
                for (int i = 0; i < sampleSize.length; i++) {
                    long mask = (1 << sampleSize[i]) - 1; // all 1s, the length of it is equal to sampleSize[i]

                    long lom = lowerMet ? 0 : (lo >> (bitsRemaining - sampleSize[i])) & mask;
                    long him = higherMet ? mask : (hi >> (bitsRemaining - sampleSize[i])) & mask;

                    long min = ranges.get(2 * i);
                    long max = ranges.get(2 * i + 1);

                    // the first check is for when we can set the value of either the lom or the him
                    // and one of them is satisfied anyways

                    // the second check is for when we don't have eitherMet set to true. In that
                    // case, we need to either have higherMet set to true or be able to satisfy it
                    // anyways. Similarly, we also need to have lowerMet set to true or be able to
                    // satisfy it anyway.
                    if (!((eitherMet && (lom <= max || min <= him))
                            || ((lom <= max || lowerMet) && (min <= him || higherMet)))) {
                        return false;
                    } else if (!((lom <= max || lowerMet) && (min <= him || higherMet))) {
                        // this is for when we are forced to choose one of the options given by
                        // eitherMet (namely we need to choose between setting lowerMet and higherMet)
                        eitherMet = false;
                        if (lom > max) {
                            lowerMet = true;
                        } else {
                            higherMet = true;
                        }
                    } else if (eitherMet && (him - 1 >= min || lom + 1 <= max)) {
                        // here, we have eitherMet set to true, and we are able to satisfy at least one
                        // of the conditions necessary to set one of lowerMet or higherMet. In this
                        // case, we can set them both to true (one because its condition is met and the
                        // other because of eitherSet)
                        eitherMet = false;
                        lowerMet = true;
                        higherMet = true;
                    }

                    // recompute the lom and him, as the lowerMet and higherMet flags might have
                    // changed.
                    lom = lowerMet ? 0 : (lo >> (bitsRemaining - sampleSize[i])) & mask;
                    him = higherMet ? mask : (hi >> (bitsRemaining - sampleSize[i])) & mask;

                    if (him - lom >= 2 && him - 1 >= min && lom + 1 <= max) {
                        // we have enough room from min and max that we can set both flags to true
                        eitherMet = false;
                        lowerMet = true;
                        higherMet = true;
                    } else if (him - lom == 1 && him - 1 >= min && lom + 1 <= max) {
                        // we can only set exactly one of them to true
                        if (lowerMet) {
                            higherMet = true;
                        } else if (higherMet) {
                            lowerMet = true;
                        } else {
                            eitherMet = true;
                        }
                    } else if (him > lom && him - 1 >= min) {
                        // we can only set exactly higherMet, not lowerMet
                        higherMet = true;
                    } else if (him > lom && lom + 1 <= max) {
                        // we can only set exactly lowerMet, not higherMet
                        lowerMet = true;
                    }

                    bitsRemaining -= sampleSize[i];
                }
                return true;
            }

            @Override
            public boolean containsOutside(long lo, long hi) {
                boolean differenceBefore = false; // signifies that an earlier sample has satisfied lom < him. This
                                                  // means that both all 0s and all 1s are possible for the rest of the
                                                  // samples.

                int bitsRemaining = totalBits;
                for (int i = 0; i < sampleSize.length; i++) {
                    long mask = (1 << sampleSize[i]) - 1;
                    long lom = (lo >> (bitsRemaining - sampleSize[i])) & mask;
                    long him = (hi >> (bitsRemaining - sampleSize[i])) & mask;
                    long min = ranges.get(2 * i);
                    long max = ranges.get(2 * i + 1);

                    // first part means that the range within the currect sample has some member not
                    // overlapping with the target range.
                    // second part means that there was an earlier sample where lom < him. this
                    // means that unless the target range for the current sample accepts both all 0s
                    // and all 1s, there is some number that is non-overlapping.
                    if (lom < min || him > max || (differenceBefore && (min != 0 || max != mask)))
                        return true;
                    if (lom < him)
                        differenceBefore = true;

                    bitsRemaining -= sampleSize[i];
                }
                return false;
            }
        };
    }
}
