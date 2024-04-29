package dk.itu.raven.join.results;

import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.Optional;

public class PixelValue implements IResult {
    public long value;
    public int x, y;

    public PixelValue(long value, int x, int y) {
        this.value = value;
        this.x = x;
        this.y = y;
    }

    @Override
    public Iterator<Pixel> iterator() {
        return new Iterator<Pixel>() {
            private boolean hasNext = true;

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public Pixel next() {
                hasNext = false;
                return new IResult.Pixel(x, y, Optional.of(value));
            }
        };
    }

    @Override
    public void draw(Graphics2D graphics) {
        graphics.drawLine(x, y, x, y);
    }
}
