package dk.itu.raven.io.cache;

public class CacheOptions {
	public boolean isCaching;
	private String cacheDir;

	public CacheOptions(String cacheDir, boolean isCaching) {
		this.cacheDir = cacheDir;
		this.isCaching = isCaching;
	}

	public String getCacheDir() {
		if (cacheDir == null) {
			throw new IllegalArgumentException("Cache directory is not set");
		}
		return cacheDir;
	}
}
