package dk.itu.raven.io.cache;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import dk.itu.raven.io.serialization.Serializer;
import dk.itu.raven.util.Logger;

public class RasterCache<T> {
	private Path path;
	private Set<String> cache = new HashSet<>();

	// Build an index of the raster files in the cache
	public RasterCache(String parentDir, String path) {
		Path p = Paths.get(parentDir, path);
		this.path = p;
		File dir = p.toFile();
		Logger.log("RasterCache path: " + this.path.toString(), Logger.LogLevel.DEBUG);
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

	@SuppressWarnings("unchecked")
	public T readItem(String cacheKey) throws ClassNotFoundException, IOException {
		return (T) Serializer.deserialize(path + "/" + cacheKey);
	}

}
