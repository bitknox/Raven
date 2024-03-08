package dk.itu.raptor.join;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

// import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import edu.ucr.cs.bdlab.beast.io.tiff.AbstractIFDEntry;
import edu.ucr.cs.bdlab.beast.io.tiff.TiffRaster;
import edu.ucr.cs.bdlab.raptor.GeoTiffMetadata;
import edu.ucr.cs.bdlab.raptor.GeoTiffMetadata2CRSAdapter;

public class RasterMetadata {
    int columns, rows;
    int tileWidth, tileHeight;
    AffineTransform g2w;
    AffineTransform w2g;
    CoordinateReferenceSystem crs;

    public RasterMetadata(int columns, int rows, int tileWidth, int tileHeight, AffineTransform g2w,
            AffineTransform w2g, CoordinateReferenceSystem crs) {
        this.columns = columns;
        this.rows = rows;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.g2w = g2w;
        this.w2g = w2g;
        this.crs = crs;
    }

    public RasterMetadata(TiffRaster raster, GeoTiffMetadata metadata) {
        this.columns = raster.getWidth();
        this.rows = raster.getHeight();
        this.tileWidth = raster.getTileWidth();
        this.tileHeight = raster.getTileHeight();
        this.g2w = metadata.getModelTransformation();
        GeoTiffMetadata2CRSAdapter adapter = new GeoTiffMetadata2CRSAdapter(null);
        // metadata.getModelTransformation()
        // AbstractIFDEntry[] entries = raster.getEntries();
        // for (AbstractIFDEntry entry : entries) {
        // System.out.println(entry.tag);
        // entry.
        // }
        this.w2g = (AffineTransform) metadata.getModelTransformation().clone();
        try {
            this.crs = adapter.createCoordinateSystem(metadata);
            this.w2g.invert();
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
