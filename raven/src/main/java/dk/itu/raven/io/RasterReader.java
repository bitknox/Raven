package dk.itu.raven.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

import java.awt.Rectangle;

import dk.itu.raven.geometry.Offset;
import dk.itu.raven.io.cache.CachedRasterStructure;
import dk.itu.raven.io.cache.RasterCache;
import dk.itu.raven.join.SpatialDataChunk;
import dk.itu.raven.util.matrix.Matrix;

public abstract class RasterReader {
	ImageMetadata metadata;
	private Optional<String> cacheKey = Optional.empty();

	public abstract Matrix readRasters(Rectangle rect) throws IOException;

	public abstract TFWFormat getTransform() throws IOException;

	protected abstract ImageMetadata readImageMetadata() throws IOException;

	public ImageMetadata getImageMetadata() throws IOException {
		if (this.metadata == null)
			this.metadata = readImageMetadata();
		return this.metadata;
	};

	public void setCacheKey(String cacheKey) {
		this.cacheKey = Optional.of(cacheKey);
	}

	public Optional<String> getCacheKey() {
		return cacheKey;
	}

	public Stream<SpatialDataChunk> rasterPartitionStream(Rectangle rect, int widthStep, int heightStep,
			Optional<RasterCache<CachedRasterStructure>> cache)
			throws IOException {

		// Limit to image size.
		int startX = rect.x;
		int startY = rect.y;
		int endX = rect.x + rect.width;
		int endY = rect.y + rect.height;

		ArrayList<Rectangle> windows = new ArrayList<>();

		for (int y = startY; y < endY; y += heightStep) {
			for (int x = startX; x < endX; x += widthStep) {
				windows.add(new Rectangle(x, y, Math.min(endX, x + widthStep) - x, Math.min(endY, y + heightStep) - y));
			}
		}

		return windows.stream().map(w -> {
			try {
				Offset<Integer> offset = new Offset<>(w.x - rect.x, w.y - rect.y);

				SpatialDataChunk chunk = new SpatialDataChunk();
				chunk.setOffset(offset);
				String key = chunk.getCacheKeyName();
				if (cache.isPresent() && cache.get().contains(key)) {
					chunk.setCacheKey(key);
					return chunk;
				}

				chunk.setMatrix(readRasters(w));
				return chunk;
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
				return null; // unreachable
			}
		});
	}

}
