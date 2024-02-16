package dk.itu.raven.util;

public class LongArrayWrapper extends PrimitiveArrayWrapper {
    private long[] data;
    public LongArrayWrapper(long[] data) {
        this.data = data;
    }
    public long get(int index) {
        return data[index];
    }

    @Override
    public void set(int index, long val) {
        data[index] = val;
    }

    @Override
    public int length() {
        return data.length;
    }
}
