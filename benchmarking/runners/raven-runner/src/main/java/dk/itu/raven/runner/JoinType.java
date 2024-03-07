package dk.itu.raven.runner;

public enum JoinType {
    SEQUENTIAL("Sequential"),
    STREAMED("Streamed"),
    PARALLEL("Parallel");

    private String label;

    private JoinType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
