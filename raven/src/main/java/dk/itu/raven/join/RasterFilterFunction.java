package dk.itu.raven.join;

public interface RasterFilterFunction {
    public boolean containsWithin(long lo, long hi);
    public boolean containsOutside(long lo, long hi);
}