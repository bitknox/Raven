package dk.itu.raven.visualizer;

import java.awt.Color;

public class LengthColor extends Color {

    private int[] lengthList = new int[2049];

    private int length(int number) {
        int i = 0;
        while (number != 1) {
            i++;
            number >>= 1;

        }
        return i;
    }

    public LengthColor() {
        super(0);
        for (int i = 1; i <= 2048; i++) {
            lengthList[i] = length(i);
        }
    }

    @Override
    public int getRGB() {
        return 0;
    }

    public int getRGB(int length) {
        int value = (int) (225 - 18 * lengthList[length]);
        return 0xff000000 + (value << 16) + (value << 8) + value;
    }
}
