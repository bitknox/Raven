package dk.itu.raven.join.intersection;

public interface IntersectionIndex extends Iterable<Integer> {
    public void addIntersection(int x);

    public int getCount(int x);
}
