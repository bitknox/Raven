package dk.itu.raven.ksquared;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import dk.itu.raven.util.BitMap;

import java.util.Random;

public class PrefixSumArrayTest {
    @Test
    public void testPrefixSumCorrectness() {
        int length = 10000;
        Random r = new Random(42);
        BitMap bitmap = new BitMap(length);
        int[] full = new int[length + 1];
        bitmap.unset(0);
        for (int i = 1; i < length; i++) {
            full[i] = full[i - 1];
            if (r.nextInt(2) == 1) {
                bitmap.set(i);
                full[i]++;
            } else {
                bitmap.unset(i);
            }
        }

        IntRank compressed = new IntRank(bitmap.getMap(), bitmap.size());

        for (int i = 2; i < length; i++) {
            assertEquals(full[i], compressed.rank(i),
                    "tree-size: " + bitmap.size() + ", index: " + i);
        }
    }
}