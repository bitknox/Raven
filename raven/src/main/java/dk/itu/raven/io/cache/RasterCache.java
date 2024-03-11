package dk.itu.raven.io.cache;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import dk.itu.raven.io.serialization.Serializer;

public class RasterCache<T> {
	private String path;
	private Set<String> cache = new HashSet<>();

	// Build an index of the raster files in the cache
	public RasterCache(String path) {
		this.path = path;
		File dir = new File(path);

		if (!dir.exists()) {
			dir.mkdirs();
			return;
		}

		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				cache.add(child.getName());
			}
		} else {
			throw new IllegalArgumentException("The path provided is not a directory");
		}
	}

	public boolean contains(String cacheKey) {
		return cache.contains(cacheKey);
	}

	public void addRasterToCache(String cacheKey, T rasterStructure) throws IOException {
		cache.add(cacheKey);
		Serializer.serialize(path + "/" + cacheKey, rasterStructure);
	}

	public T readItem(String cacheKey) throws ClassNotFoundException, IOException {
		return (T) Serializer.deserialize(path + "/" + cacheKey);
	}

}