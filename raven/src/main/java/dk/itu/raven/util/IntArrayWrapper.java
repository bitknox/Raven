package dk.itu.raven.util;

public class IntArrayWrapper extends PrimitiveArrayWrapper {
    private int[] data;
    public IntArrayWrapper(int[] data) {
        this.data = data;
    }
    public long get(int index) {
        return data[index];
    }
    @Override
    public void set(int index, long val) {
        data[index] = (int) val;
    }

    @Override
    public int length() {
        return data.length;
    }
}
