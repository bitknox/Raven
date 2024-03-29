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

    @Override
    public String toString() {
        return "Offset [offsetX=" + offsetX + ", offsetY=" + offsetY + "]";
    }
}
