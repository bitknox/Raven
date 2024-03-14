package dk.itu.raptor.join;

public class PixelRange implements Comparable<PixelRange> {
    int rid, tid, y, x1, x2;
    long gid;

    public PixelRange(int rid, int tid, long gid, int y, int x1, int x2) {
        this.rid = rid;
        this.tid = tid;
        this.gid = gid;
        this.y = y;
        this.x1 = x1;
        this.x2 = x2;
    }

    @Override
    public int compareTo(PixelRange o) {
        if (y == o.y) {
            if (gid == o.gid) {
                if (x1 == o.x1) {
                    return 0;
                }
                return x1 - o.x1;
            }
            // return (int) (gid - o.gid);
            if (gid < o.gid)
                return -1;
            return 1;
        }
        return y - o.y;
    }
}
