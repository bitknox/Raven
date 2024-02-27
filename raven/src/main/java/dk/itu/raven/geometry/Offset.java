package dk.itu.raven.geometry;

public class Offset<T extends Number> {
    private T offsetX, offsetY;

    public Offset(T offsetX, T offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public T getOffsetX() {
        return offsetX;
    }

    public T getOffsetY() {
        return offsetY;
    }
}
