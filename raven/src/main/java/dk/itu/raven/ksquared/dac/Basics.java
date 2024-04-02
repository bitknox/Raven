package dk.itu.raven.ksquared.dac;

public abstract class Basics {
    public static int bits(long n) {
        int b = 0;
        while (n > 0) {
            b++;
            n >>= 1;
        }
        return b;
    }
}
