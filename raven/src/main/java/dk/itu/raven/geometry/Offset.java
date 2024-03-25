package dk.itu.raven.geometry;

import java.io.Serializable;

public class Offset<T extends Number> implements Serializable {
    private T offsetX, offsetY;

    public Offset(T offsetX, T offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public T getX() {
        return offsetX;
    }

    public T getY() {
        return offsetY;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Offset))
            return false;
        Offset<T> other = (Offset<T>) obj;
        return offsetX.equals(other.offsetX) && offsetY.equals(other.offsetY);

    }

    public void offset(T x, T y) {
        offsetX = add(offsetX, x);
        offsetY = add(offsetY, y);
    }

    public void offset(Offset<T> offset) {
        offsetX = add(offsetX, offset.offsetX);
        offsetY = add(offsetY, offset.offsetY);
    }

    private T add(T a, T b) {
        if (a instanceof Integer) {
            return (T) Integer.valueOf(((Number) a).intValue() + ((Number) b).intValue());
        } else if (a instanceof Double) {
            return (T) Double.valueOf(((Number) a).doubleValue() + ((Number) b).doubleValue());
        } else if (a instanceof Long) {
            return (T) Long.valueOf(((Number) a).longValue() + ((Number) b).longValue());
        } else {
            return (T) Float.valueOf(((Number) a).floatValue() + ((Number) b).floatValue());
        }
    }

    @Override
    public String toString() {
        return "Offset [offsetX=" + offsetX + ", offsetY=" + offsetY + "]";
    }
}
