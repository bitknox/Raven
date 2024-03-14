package dk.itu.raven.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;

import java.awt.Rectangle;

import dk.itu.raven.geometry.Offset;
import dk.itu.raven.io.cache.CachedRasterStructure;
import dk.itu.raven.io.cache.RasterCache;
import dk.itu.raven.join.SpatialDataChunk;
import dk.itu.raven.util.TreeExtensions;
import dk.itu.raven.util.matrix.Matrix;

public abstract class RasterReader {
	ImageMetadata metadata;
	private Optional<String> cacheKey = Optional.empty();

	public abstract Matrix readRasters(Rectangle rect) throws IOException;

	public abstract TFWFormat getTransform();

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

	public Stream<SpatialDataChunk> rasterPartitionStream(int widthStep, int heightStep,
			Optional<RasterCache<CachedRasterStructure>> cache, RTree<String, Geometry> rtree) throws IOException {
		return rasterPartitionStream(widthStep, heightStep, new Offset<Integer>(0, 0), cache, rtree);
	}

	public Stream<SpatialDataChunk> rasterPartitionStream(int widthStep, int heightStep, Offset<Integer> globalOffset,
			Optional<RasterCache<CachedRasterStructure>> cache, RTree<String, Geometry> rtree) throws IOException {
		ImageMetadata metadata = getImageMetadata();

		// Limit to image size.
		int startX = 0;
		int startY = 0;
		int endX = metadata.getWidth();
		int endY = metadata.getHeight();

		ArrayList<Rectangle> windows = new ArrayList<>();

		for (int y = startY; y < endY; y += heightStep) {
			for (int x = startX; x < endX; x += widthStep) {
				windows.add(new Rectangle(x, y, Math.min(endX, x + widthStep) - x, Math.min(endY, y + heightStep) - y));
			}
		}

		return windows.stream().filter(w -> {
			int x = w.x + globalOffset.getX();
			int y = w.y + globalOffset.getY();
			var rect = Geometries.rectangle(x, y, x + w.width, y + w.height);
			boolean intersects = TreeExtensions.intersectsOne(rtree.root().get(), rect);
			return intersects;
		}).map(w -> {
			try {
				Offset<Integer> offset = new Offset<>(w.x, w.y);
				offset.offset(globalOffset);

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
