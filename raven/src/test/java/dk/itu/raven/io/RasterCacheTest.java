package dk.itu.raven.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dk.itu.raven.io.cache.RasterCache;

public class RasterCacheTest {

	private static final String CACHE_DIR = "./test-cache-lmao";

	// Test for the RasterCache class
	@BeforeAll
	public static void setUp() throws IOException {
		new File(CACHE_DIR).mkdirs();
		new File(CACHE_DIR + "/" + "in-cache").createNewFile();
	}

	@Test
	public void testRasterCache() throws IOException, ClassNotFoundException {
		RasterCache<String> rc = new RasterCache<String>(CACHE_DIR);
		// test that this item already exists in the cache
		String inCache = "in-cache";
		// test that this item does not exist in the cache
		String notInCache = "not-in-cache";
		// test that this item is not in the cache and then add it to the cache
		String addToCache = "add-to-cache";

		assertTrue(rc.contains(inCache));
		assertFalse(rc.contains(notInCache));

		assertFalse(rc.contains(addToCache));

		rc.addRasterToCache(addToCache, "content");

		assertTrue(rc.contains(addToCache));

		assertEquals("content", rc.readItem(addToCache));
	}

	@AfterAll
	public static void tearDown() {
		File file = new File(CACHE_DIR);
		for (File f : file.listFiles()) {
			f.delete();
		}
		file.delete();
	}
}
