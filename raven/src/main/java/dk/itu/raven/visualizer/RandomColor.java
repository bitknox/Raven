package dk.itu.raven.visualizer;

import java.awt.Color;
import java.util.Random;

public class RandomColor extends Color {
    Random r;

    public RandomColor() {
        super(0);
        this.r = new Random();
    }

    @Override
    public int getRGB() {
        return 0xff000000 + r.nextInt(0x00ffffff);
    }
}
