package dk.itu.raven.join;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.checkerframework.checker.units.qual.radians;
import org.junit.jupiter.api.Test;

import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.api.RavenApi;
import dk.itu.raven.geometry.PixelRange;

public class StreamedRavenJoinTest {
    @Test
    public void testAllApproaches() throws IOException {
        RavenApi ravenApi = new RavenApi();
        String rasterPath = "src/test/java/dk/itu/raven/data/wildfires";
        String vectorPath = "src/test/java/dk/itu/raven/data/cb_2018_us_state_500k/cb_2018_us_state_500k.shp";
        AbstractRavenJoin inMemoryJoin = ravenApi.getJoin(rasterPath, vectorPath);
        AbstractRavenJoin streamedJoin = ravenApi.getStreamedJoin(rasterPath, vectorPath, 200, 200, false);
        AbstractRavenJoin parallelJoin = ravenApi.getStreamedJoin(rasterPath, vectorPath, 200, 200, true);

        AbstractJoinResult inMemoryResult = inMemoryJoin.join().asMemoryAllocatedResult();
        AbstractJoinResult streamedResult = streamedJoin.join().asMemoryAllocatedResult();
        AbstractJoinResult parallelResult = parallelJoin.join().asMemoryAllocatedResult();

        Set<PixelRange> set = new HashSet<>();
        int inMemoryCount = 0, streamedCount = 0, parallelCount = 0;

        for (JoinResultItem item : inMemoryResult) {
            for (PixelRange range : item.pixelRanges) {
                set.add(range);
                inMemoryCount++;
            }
        }

        for (JoinResultItem item : streamedResult) {
            for (PixelRange range : item.pixelRanges) {
                assertTrue(set.contains(range));
                streamedCount++;
            }
        }

        for (JoinResultItem item : parallelResult) {
            for (PixelRange range : item.pixelRanges) {
                assertTrue(set.contains(range));
                parallelCount++;
            }
        }

        assertEquals(inMemoryCount, streamedCount);
        assertEquals(inMemoryCount, parallelCount);
    }
}
