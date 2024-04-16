package dk.itu.raven.join;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.Test;

import dk.itu.raven.api.RavenApi;
import dk.itu.raven.geometry.PixelRange;

public class StreamedRavenJoinTest {
    public class Point {
        int x, y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Point))
                return false;
            Point other = (Point) obj;
            return x == other.x && y == other.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    @Test
    public void testAllApproaches() throws IOException {
        RavenApi ravenApi = new RavenApi();
        String rasterPath = "src/test/java/dk/itu/raven/data/wildfires";
        String vectorPath = "src/test/java/dk/itu/raven/data/cb_2018_us_state_500k/cb_2018_us_state_500k.shp";
        AbstractRavenJoin inMemoryJoin = ravenApi.getJoin(rasterPath, vectorPath, false, 2, 1, 8);
        AbstractRavenJoin streamedJoin = ravenApi.getStreamedJoin(rasterPath, vectorPath, 200, 200, false, false, 2, 1,
                8);
        AbstractRavenJoin parallelJoin = ravenApi.getStreamedJoin(rasterPath, vectorPath, 200, 200, true, false, 2, 1,
                8);

        IJoinResult inMemoryResult = inMemoryJoin.join().asMemoryAllocatedResult();
        IJoinResult streamedResult = streamedJoin.join().asMemoryAllocatedResult();
        IJoinResult parallelResult = parallelJoin.join().asMemoryAllocatedResult();

        Set<Point> inMemorySet = new HashSet<>();
        Set<Point> streamedSet = new HashSet<>();
        Set<Point> parallelSet = new HashSet<>();

        for (JoinResultItem item : inMemoryResult) {
            for (PixelRange range : item.pixelRanges) {
                int y = range.row;
                for (int x = range.x1; x <= range.x2; x++) {
                    inMemorySet.add(new Point(x, range.row));
                    assertTrue(x >= 0 && x < 1052 && y >= 0 && y < 784, "InMemory x: " + x + " y: " + y);
                }
            }
        }

        for (JoinResultItem item : streamedResult) {
            for (PixelRange range : item.pixelRanges) {
                int y = range.row;
                for (int x = range.x1; x <= range.x2; x++) {
                    assertTrue(inMemorySet.contains(new Point(x, range.row)));
                    streamedSet.add(new Point(x, range.row));
                    assertTrue(x >= 0 && x < 1052 && y >= 0 && y < 784, "Streamed x: " + x + " y: " + y);
                }
            }
        }

        for (JoinResultItem item : parallelResult) {
            for (PixelRange range : item.pixelRanges) {
                int y = range.row;
                for (int x = range.x1; x <= range.x2; x++) {
                    assertTrue(inMemorySet.contains(new Point(x, range.row)));
                    parallelSet.add(new Point(x, range.row));
                    assertTrue(x >= 0 && x < 1052 && y >= 0 && y < 784, "Parallel x: " + x + " y: " + y);
                }
            }
        }

        assertEquals(inMemorySet.size(), streamedSet.size());
        assertEquals(inMemorySet.size(), parallelSet.size());
    }
}
