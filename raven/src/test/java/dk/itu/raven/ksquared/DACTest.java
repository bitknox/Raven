package dk.itu.raven.ksquared;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

import dk.itu.raven.util.IntArrayWrapper;
import dk.itu.raven.util.LongArrayWrapper;

public class DACTest {
    @Test
    public void DACSameAsOriginal() {
        Random r = new Random(42);
        int[] list = new int[1000000];
        for (int i = 0; i < list.length; i++) {
            list[i] = r.nextInt(1000);
        }

        DAC dac = new DAC(new IntArrayWrapper(list));

        for (int i = 0; i < list.length; i++) {
            assertEquals(list[i], dac.get(i), "Index: " + i);
        }
    }

    @Test
    public void DacLongTest() {
        Random r = new Random(42);
        long[] list = new long[1000000];
        for (int i = 0; i < list.length; i++) {
            list[i] = Math.abs(r.nextLong());
            if (list[i] == Long.MIN_VALUE)
                list[i] = 0;
        }

        DAC dac = new DAC(new LongArrayWrapper(list));

        for (int i = 0; i < list.length; i++) {
            assertEquals(list[i], dac.get(i));
        }
    }
}
