package dk.itu.raven.join.results;

import java.util.Optional;
import java.awt.Graphics2D;

public interface IResult extends Iterable<IResult.Pixel> {
    public class Pixel {
        public Optional<Long> value;
        public int x, y;

        public Pixel(int x, int y, Optional<Long> value) {
            this.x = x;
            this.y = y;
            this.value = value;
        }
    }

    public void draw(Graphics2D graphics);
}
