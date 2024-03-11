package dk.itu.raptor.join;

public class JoinResult {
    public int rid, x, y;
    public long gid;
    public int m;

    public JoinResult(long gid, int rid, int x, int y, int m) {
        this.gid = gid;
        this.rid = rid;
        this.x = x;
        this.y = y;
        this.m = m;
    }
}
