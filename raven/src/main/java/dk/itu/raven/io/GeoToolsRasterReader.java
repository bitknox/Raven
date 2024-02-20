package dk.itu.raven.io;

import java.awt.Dimension;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;

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

import dk.itu.raven.SpatialDataChunk;
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

            transform = new TFWFormat(pixelScale.getScaleX(), 0, 0, -pixelScale.getScaleY(), tiePoint[0].getValueAt(3),
                    tiePoint[0].getValueAt(4));
        }
    }

    @Override
    public Matrix readRasters(Rectangle rect) throws IOException {
        GeoTiffReader reader = new GeoTiffReader(tiff);

        GeneralParameterValue[] params = createWindowParams(reader, (int) rect.x1(), (int) rect.y1(),
                (int) (rect.x2() - rect.x1()), (int) (rect.y2() - rect.y1()));

        GridCoverage2D coverage = reader.read(params);

        RenderedImage image = coverage.getRenderedImage();
        Raster raster = image.getData();

        return new AwtRasterMatrix(raster);
    }

    @Override
    public Stream<SpatialDataChunk> streamRasters(Rectangle rect) throws IOException {

        GeoTiffReader reader = new GeoTiffReader(tiff);
        int imageWidth = reader.getOriginalGridRange().getSpan(0);
        int imageHeight = reader.getOriginalGridRange().getSpan(1);

        int widthStep = 200;
        int heightStep = 200;

        int startX = 0;
        int startY = 0;
        int endX = imageWidth;
        int endY = imageHeight;

        ArrayList<GridGeometry2D> windows = new ArrayList<>();

        for (int y = startY; y < endY; y += heightStep) {
            for (int x = startX; x < endX; x += widthStep) {
                // GeneralParameterValue[] window = createWindowParams(reader, x, y, widthStep,
                // heightStep);
                windows.add(new GridGeometry2D(
                        new GridEnvelope2D(new java.awt.Rectangle(x, y, widthStep, heightStep)),
                        new GridEnvelope2D(new java.awt.Rectangle(x, y, widthStep, heightStep))));
            }
        }

        return windows.stream().map(w -> {
            try {
                GridCoverage2D coverage;
                java.awt.Rectangle bounds = w.getGridRange2D().getBounds();
                GeneralParameterValue[] params = new GeneralParameterValue[1];
                // Define a GridGeometry in order to reduce the output
                final ParameterValue<GridGeometry2D> gg = AbstractGridFormat.READ_GRIDGEOMETRY2D.createValue();
                final GeneralBounds envelope = reader.getOriginalEnvelope();
                final Dimension dim = new Dimension();
                dim.setSize(widthStep, heightStep);
                final java.awt.Rectangle rasterArea = ((GridEnvelope2D) reader.getOriginalGridRange());
                rasterArea.setSize(dim);
                rasterArea.setLocation(bounds.x, bounds.y);
                final GridEnvelope2D range = new GridEnvelope2D(rasterArea);
                var test = new GridGeometry2D(range, envelope);
                gg.setValue(test);
                params[0] = gg;
                coverage = reader.read(params);
                RenderedImage image = coverage.getRenderedImage();
                Raster raster = image.getData();
                SpatialDataChunk chunk = new SpatialDataChunk();
                chunk.setMatrix(new AwtRasterMatrix(raster));
                chunk.setOffset(bounds);
                return chunk;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    private GeneralParameterValue[] createWindowParams(GeoTiffReader reader, int x, int y, int width, int height) {
        GeneralParameterValue[] params = new GeneralParameterValue[1];
        // Define a GridGeometry in order to reduce the output
        final ParameterValue<GridGeometry2D> gg = AbstractGridFormat.READ_GRIDGEOMETRY2D.createValue();
        final GeneralBounds envelope = reader.getOriginalEnvelope();

        int rectX = (int) Math.max(x, 0.0);
        int rectY = (int) Math.max(y, 0.0);
        int rectWidth = (int) Math.ceil(Math.min(width, reader.getOriginalGridRange().getSpan(0) - x));
        int rectHeight = (int) Math.ceil(Math.min(height, reader.getOriginalGridRange().getSpan(1)) - y);

        final GridEnvelope2D range = new GridEnvelope2D(new java.awt.Rectangle(rectX, rectY, rectWidth, rectHeight));
        gg.setValue(new GridGeometry2D(range, envelope));
        params[0] = gg;
        return params;
    }

    @Override
    public ImageMetadata readImageMetadata() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'readImageMetadata'");
    }

}
