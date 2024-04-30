package dk.itu.raven.join.results;

import java.util.Iterator;
import java.util.Optional;

public class PixelRangeValue extends PixelRange {
    long value;

    public PixelRangeValue(int row, int x1, int x2, long value) {
        super(row, x1, x2);
        this.value = value;
    }

    @Override
    public Iterator<Pixel> iterator() {
        return new Iterator<Pixel>() {
            private int x = x1;

            @Override
            public boolean hasNext() {
                return x <= x2;
            }

            @Override
            public Pixel next() {
                return new IResult.Pixel(x++, row, Optional.of(value));
            }
        };
    }
}
