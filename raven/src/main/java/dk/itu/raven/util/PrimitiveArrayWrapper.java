package dk.itu.raven.util;

import java.io.Serializable;

public abstract class PrimitiveArrayWrapper implements Serializable {
    public abstract long get(int index);

    public abstract void set(int index, long val);

    public abstract int length();
}
