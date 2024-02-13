package dk.itu.raven.io;

import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import org.geotools.api.parameter.GeneralParameterValue;
import org.geotools.api.parameter.ParameterValue;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.geotiff.GeoTiffIIOMetadataDecoder;
import org.geotools.coverage.grid.io.imageio.geotiff.PixelScale;
import org.geotools.coverage.grid.io.imageio.geotiff.TiePoint;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.GeneralBounds;

import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.util.matrix.AwtRasterMatrix;
import dk.itu.raven.util.matrix.Matrix;

public class GeoToolsRasterReader extends FileRasterReader {

    public GeoToolsRasterReader(File directory) throws IOException {
        super(directory);
        if (tfw == null) {
            GeoTiffReader reader = new GeoTiffReader(tiff);
			GeoTiffIIOMetadataDecoder metadata = reader.getMetadata();
			PixelScale pixelScale = metadata.getModelPixelScales();
			TiePoint[] tiePoint = metadata.getModelTiePoints();

			if (tiePoint[0].getValueAt(0) != 0 || tiePoint[0].getValueAt(1) != 0) {
				throw new UnsupportedOperationException("first tie point is not the top left coordinates");
			}

			transform = new TFWFormat(pixelScale.getScaleX(), 0, 0, -pixelScale.getScaleY(), tiePoint[0].getValueAt(3),tiePoint[0].getValueAt(4));
        }
    }

    @Override
    public Matrix readRasters(Rectangle rect) throws IOException {
        GeoTiffReader reader = new GeoTiffReader(tiff);

        GeneralParameterValue[] params = new GeneralParameterValue[1];
        // Define a GridGeometry in order to reduce the output
        final ParameterValue<GridGeometry2D> gg = AbstractGridFormat.READ_GRIDGEOMETRY2D.createValue();
        final GeneralBounds envelope = reader.getOriginalEnvelope();

        int minX = (int) Math.max(rect.x1(), 0.0);
		int minY = (int) Math.max(rect.y1(), 0.0);
		int maxX = (int) Math.ceil(Math.min(rect.x2(), reader.getOriginalGridRange().getSpan(0)));
		int maxY = (int) Math.ceil(Math.min(rect.y2(), reader.getOriginalGridRange().getSpan(1)));

        final GridEnvelope2D range = new GridEnvelope2D(new java.awt.Rectangle(minX,minY,maxX,maxY));
        gg.setValue(new GridGeometry2D(range, envelope));
        params[0] = gg;

        GridCoverage2D coverage = reader.read(null);

        RenderedImage image = coverage.getRenderedImage();
        Raster raster = image.getData();

        return new AwtRasterMatrix(raster);
    }

}
