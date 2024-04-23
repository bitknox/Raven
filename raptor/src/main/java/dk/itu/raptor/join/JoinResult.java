package dk.itu.raptor.join;

public class JoinResult {
    public int x, y;
    public long gid;
    public int m;

    public JoinResult(long gid, int x, int y, int m) {
        this.gid = gid;
        this.x = x;
        this.y = y;
        this.m = m;
    }
}
