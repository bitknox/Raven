package dk.itu.raven.join;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import dk.itu.raven.api.RavenApi;
import dk.itu.raven.io.cache.CacheOptions;
import dk.itu.raven.io.commandline.ResultType;
import dk.itu.raven.join.results.IJoinResult;
import dk.itu.raven.join.results.IResult;
import dk.itu.raven.join.results.JoinResultItem;

public class StreamedRavenJoinTest {

    public class Point {

        int x, y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Point)) {
                return false;
            }
            Point other = (Point) obj;
            return x == other.x && y == other.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    @ParameterizedTest
    @MethodSource("dk.itu.raven.Util#getResultTypes")
    public void testAllApproaches(ResultType type) throws IOException {
        RavenApi ravenApi = new RavenApi();
        String rasterPath = "src/test/java/dk/itu/raven/data/wildfires";
        String vectorPath = "src/test/java/dk/itu/raven/data/cb_2018_us_state_500k/cb_2018_us_state_500k.shp";
        AbstractRavenJoin inMemoryJoin = ravenApi.getJoin(rasterPath, vectorPath, new CacheOptions(null, false), 2, 1,
                8, type);
        AbstractRavenJoin streamedJoin = ravenApi.getStreamedJoin(rasterPath, vectorPath, 200, 200, false,
                new CacheOptions(null, false), 2, 1,
                8, type);
        AbstractRavenJoin parallelJoin = ravenApi.getStreamedJoin(rasterPath, vectorPath, 200, 200, true,
                new CacheOptions(null, false), 2, 1,
                8, type);

        IJoinResult inMemoryResult = inMemoryJoin.join().asMemoryAllocatedResult();
        IJoinResult streamedResult = streamedJoin.join().asMemoryAllocatedResult();
        IJoinResult parallelResult = parallelJoin.join().asMemoryAllocatedResult();

        Set<Point> inMemorySet = new HashSet<>();
        Set<Point> streamedSet = new HashSet<>();
        Set<Point> parallelSet = new HashSet<>();

        for (JoinResultItem item : inMemoryResult) {
            for (IResult range : item.pixelRanges) {
                for (IResult.Pixel pixel : range) {
                    int y = pixel.y;
                    inMemorySet.add(new Point(pixel.x, y));
                    assertTrue(pixel.x >= 0 && pixel.x < 1052 && y >= 0 && y < 784,
                            "InMemory x: " + pixel.x + " y: " + y);
                }
            }
        }

        for (JoinResultItem item : streamedResult) {
            for (IResult range : item.pixelRanges) {
                for (IResult.Pixel pixel : range) {
                    int y = pixel.y;
                    int x = pixel.x;
                    assertTrue(inMemorySet.contains(new Point(x, y)));
                    streamedSet.add(new Point(x, y));
                    assertTrue(x >= 0 && x < 1052 && y >= 0 && y < 784, "Streamed x: " + x + " y: " + y);
                }
            }
        }

        for (JoinResultItem item : parallelResult) {
            for (IResult range : item.pixelRanges) {
                for (IResult.Pixel pixel : range) {
                    int y = pixel.y;
                    int x = pixel.x;
                    assertTrue(inMemorySet.contains(new Point(x, y)));
                    parallelSet.add(new Point(x, y));
                    assertTrue(x >= 0 && x < 1052 && y >= 0 && y < 784, "Parallel x: " + x + " y: " + y);
                }
            }
        }

        assertEquals(inMemorySet.size(), streamedSet.size());
        assertEquals(inMemorySet.size(), parallelSet.size());
        assertEquals(inMemorySet.size(), 824230);
    }
}
