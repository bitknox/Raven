package dk.itu.raptor.join;

public class JoinResult {
    int rid, x, y;
    long gid;
    Object m;

    public JoinResult(long gid, int rid, int x, int y, Object m) {
        this.gid = gid;
        this.rid = rid;
        this.x = x;
        this.y = y;
        this.m = m;
    }
}
