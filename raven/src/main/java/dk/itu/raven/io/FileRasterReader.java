package dk.itu.raven.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;

import org.geotools.coverage.grid.io.imageio.geotiff.GeoTiffIIOMetadataDecoder;
import org.geotools.coverage.grid.io.imageio.geotiff.PixelScale;
import org.geotools.coverage.grid.io.imageio.geotiff.TiePoint;
import org.geotools.gce.geotiff.GeoTiffReader;

import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.SpatialDataChunk;

public abstract class FileRasterReader implements RasterReader {
	File tiff;
	File tfw;

	ImageMetadata metadata;
	TFWFormat transform;

	public FileRasterReader(File directory) throws IOException {
		for (File file : directory.listFiles()) {

			if (file.getName().endsWith(".tif") ||
					file.getName().endsWith(".tiff")) {
				tiff = file;
			}
			if (file.getName().endsWith(".tfw")) {
				tfw = file;
			}
		}
		if (tiff == null) {
			throw new IOException("Missing tiff file");
		}

		if (tfw != null) {
			transform = TFWFormat.read(tfw);
		} else {
			GeoTiffReader reader = new GeoTiffReader(tiff);
			GeoTiffIIOMetadataDecoder metadata = reader.getMetadata();
			PixelScale pixelScale = metadata.getModelPixelScales();
			TiePoint[] tiePoint = metadata.getModelTiePoints();

			if (pixelScale == null || tiePoint == null || tiePoint.length < 1) {
				throw new UnsupportedOperationException("no side-car or inline TFW data found");
			}

			if (tiePoint[0].getValueAt(0) != 0 || tiePoint[0].getValueAt(1) != 0) {
				throw new UnsupportedOperationException("first tie point is not the top left coordinates");
			}

			transform = new TFWFormat(pixelScale.getScaleX(), 0, 0, -pixelScale.getScaleY(), tiePoint[0].getValueAt(3),
					tiePoint[0].getValueAt(4));
		}

		this.metadata = readImageMetadata();
	}

	public TFWFormat getTransform() {
		return transform;
	}

	protected abstract ImageMetadata readImageMetadata() throws IOException;

	public ImageMetadata getImageMetadata() {
		return this.metadata;
	};

	public Stream<SpatialDataChunk> rasterPartitionStream(Rectangle rect, int widthStep, int heightStep)
			throws IOException {
		ImageMetadata imageSize = getImageMetadata();

		// Limit to image size.
		int startX = (int) Math.max(rect.x1(), 0);
		int startY = (int) Math.max(rect.y1(), 0);
		int endX = (int) Math.ceil(Math.min(imageSize.getWidth(), rect.x2()));
		int endY = (int) Math.ceil(Math.min(imageSize.getHeight(), rect.y2()));

		ArrayList<Rectangle> windows = new ArrayList<>();

		for (int y = startY; y < endY; y += heightStep) {
			for (int x = startX; x < endX; x += widthStep) {
				windows.add(Geometries.rectangle(x, y, Math.min(endX, x + widthStep), Math.min(endY, y + heightStep)));
			}
		}

		return windows.stream().map(w -> {
			try {
				SpatialDataChunk chunk = new SpatialDataChunk();
				chunk.setMatrix(readRasters(w));
				java.awt.Rectangle offset = new java.awt.Rectangle((int) w.x1(), (int) w.y1(), (int) (w.x2() - w.x1()),
						(int) (w.y2() - w.y1()));
				chunk.setOffset(offset);
				return chunk;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		});
	}
}
