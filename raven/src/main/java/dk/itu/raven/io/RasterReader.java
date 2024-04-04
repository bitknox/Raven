package dk.itu.raven.io;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.geometry.Offset;
import dk.itu.raven.io.cache.CachedRasterStructure;
import dk.itu.raven.io.cache.RasterCache;
import dk.itu.raven.join.SpatialDataChunk;
import dk.itu.raven.util.TreeExtensions;
import dk.itu.raven.util.matrix.Matrix;

public abstract class RasterReader implements IRasterReader {
	ImageMetadata metadata;

	public abstract Matrix readRasters(Rectangle rect) throws IOException;

	protected abstract ImageMetadata readImageMetadata() throws IOException;

	public ImageMetadata getImageMetadata() throws IOException {
		if (this.metadata == null)
			this.metadata = readImageMetadata();
		return this.metadata;
	};

	public Stream<SpatialDataChunk> rasterPartitionStream(int widthStep, int heightStep,
			Optional<RasterCache<CachedRasterStructure>> cache, RTree<String, Geometry> rtree, VectorData vectorData)
			throws IOException {
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
			int x = w.x;
			int y = w.y;
			var rect = Geometries.rectangle(x, y, x + w.width, y + w.height);
			boolean intersects = TreeExtensions.intersectsOne(rtree.root().get(), rect);
			return intersects;
		}).map(w -> {
			try {
				Offset<Integer> offset = new Offset<>(w.x, w.y);
				SpatialDataChunk chunk = new SpatialDataChunk();
				chunk.setOffset(offset);
				chunk.setTree(rtree);
				if (metadata.getDirectoryName().isPresent()) {
					chunk.setName(metadata.getDirectoryName().get());

					if (cache.isPresent() && cache.get().contains(chunk.getCacheKeyName())) {
						return chunk;
					}
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

	@Override
	public Optional<String> getDirectory() {
		return Optional.empty();
	}

}
