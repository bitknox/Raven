package dk.itu.raven.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;

import java.awt.Rectangle;

import dk.itu.raven.geometry.Offset;
import dk.itu.raven.join.SpatialDataChunk;
import dk.itu.raven.util.matrix.Matrix;

public abstract class RasterReader {
	ImageMetadata metadata;

	public abstract Matrix readRasters(Rectangle rect) throws IOException;

	public abstract TFWFormat getTransform() throws IOException;

	protected abstract ImageMetadata readImageMetadata() throws IOException;

	public ImageMetadata getImageMetadata() throws IOException {
		if (this.metadata == null)
			this.metadata = readImageMetadata();
		return this.metadata;
	};

	public Stream<SpatialDataChunk> rasterPartitionStream(Rectangle rect, int widthStep, int heightStep)
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
				SpatialDataChunk chunk = new SpatialDataChunk();
				chunk.setMatrix(readRasters(w));
				Offset<Integer> offset = new Offset<>(w.x - rect.x, w.y - rect.y);
				chunk.setOffset(offset);
				return chunk;
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
				return null; // unreachable
			}
		});
	}

}
