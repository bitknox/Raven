package dk.itu.raven.join;

public class Window {
    public int minX;
    public int maxX;
    public int minY;
    public int maxY;

    public Window() {
        this.minX = Integer.MAX_VALUE;
        this.maxX = Integer.MIN_VALUE;
        this.minY = Integer.MAX_VALUE;
        this.maxY = Integer.MIN_VALUE;
    }
}
