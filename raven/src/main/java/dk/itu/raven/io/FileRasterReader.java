package dk.itu.raven.io;

import java.io.File;
import java.io.IOException;

// TODO: Support inline tfw.
public abstract class FileRasterReader implements RasterReader {
	File tiff;
	File tfw;

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
		}
	}

	public TFWFormat getTransform() throws IOException {
		return transform;
	}
}
