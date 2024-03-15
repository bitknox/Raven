package dk.itu.raven.io.GeoTiff;

import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.checkerframework.checker.units.qual.g;
import org.geotools.coverage.grid.io.imageio.geotiff.GeoKeyEntry;
import org.geotools.coverage.grid.io.imageio.geotiff.GeoTiffIIOMetadataDecoder;
import org.geotools.coverage.grid.io.imageio.geotiff.PixelScale;
import org.geotools.coverage.grid.io.imageio.geotiff.TiePoint;
import org.geotools.gce.geotiff.GeoTiffReader;

/**
 * This class provides required data-structure to store meta-data of GeoTiff
 * files. Specifically it stores the required tags found under
 * GeoKeyDirectoryTag.
 *
 * This class is designed based on GeoTiffIIOMetadataDecoder from GeoTools
 * library.
 */
public class GeoTiffMetadata {

    /** Get entries to tag */
    private final GeoTiffReader reader;

    private final GeoTiffIIOMetadataDecoder metadata;

    private final Map<Integer, GeoKeyEntry> geoKeys;

    private int geoKeyDirVersion;

    private int geoKeyRevision;

    private int geoKeyMinorRevision;

    private final PixelScale pixelScale;

    private final TiePoint[] tiePoints;

    private final double noData;

    private final AffineTransform modelTransformation;
    public static final int TAG_GEO_KEY_DIRECTORY = 34735;

    public GeoTiffMetadata(GeoTiffReader reader) throws IOException {
        this.metadata = reader.getMetadata();
        this.reader = reader;

        geoKeys = new HashMap<>();

        for (var geoKey : metadata.getGeoKeys()) {
            geoKeys.put(geoKey.getKeyID(), geoKey);
        }

        pixelScale = calculatePixelScales();
        modelTransformation = calculateModelTransformation();
        tiePoints = calculateTiePoints();
        noData = calculateNoData();
    }

    public static final int TIFFTAG_NODATA = 42113;

    private double calculateNoData() throws IOException {
        return metadata.getNoData();
    }

    public static final int TAG_MODEL_TRANSFORMATION = 34264;

    /**
     * Gets the model tie points from the appropriate TIFFField
     *
     * <p>
     * Attention, for the moment we support only 2D baseline transformations.
     *
     * @return the transformation, or null if not found
     */
    private AffineTransform calculateModelTransformation() throws IOException {
        return metadata.getModelTransformation();
    }

    public static final int TAG_MODEL_TIE_POINT = 33922;

    private TiePoint[] calculateTiePoints() throws IOException {
        return metadata.getModelTiePoints();
    }

    public static final int TAG_MODEL_PIXEL_SCALE = 33550;

    private PixelScale calculatePixelScales() throws IOException {
        return metadata.getModelPixelScales();
    }

    /**
     * Gets the version of the GeoKey directory. This is typically a value of 1 and
     * can be used to
     * check that the data is of a valid format.
     * 
     * @return the directory version index as an integer
     */
    public int getGeoKeyDirectoryVersion() {
        // now get the value from the correct TIFFShort location
        return geoKeyDirVersion;
    }

    /**
     * Gets the revision number of the GeoKeys in this metadata.
     * 
     * @return the revision as an integer
     */
    public int getGeoKeyRevision() {
        // Get the value from the correct TIFFShort
        return geoKeyRevision;
    }

    /**
     * Gets the minor revision number of the GeoKeys in this metadata.
     * 
     * @return the minor revision as an integer
     */
    public int getGeoKeyMinorRevision() {
        // Get the value from the correct TIFFShort
        return geoKeyMinorRevision;
    }

    /**
     * Gets a GeoKey value as a String. This implementation should be
     * &quot;quiet&quot; in the sense
     * that it should not throw any exceptions but only return null in the event
     * that the data
     * organization is not as expected.
     *
     * @param keyID The numeric ID of the GeoKey
     * @return A string representing the value, or null if the key was not found.
     * @throws IOException if an error happens while reading the given key from the
     *                     file
     */
    public String getGeoKey(final int keyID) throws IOException {
        return metadata.getGeoKey(keyID);
    }

    /**
     * Gets a record containing the four TIFFShort values for a geokey entry. For
     * more information
     * see the GeoTIFFWritingUtilities specification.
     *
     * @param keyID the ID of the key to read
     * @return the record with the given keyID, or null if none is found
     */
    public GeoKeyEntry getGeoKeyRecord(int keyID) {
        return geoKeys.get(keyID);
    }

    public AffineTransform getModelTransformation() {
        return modelTransformation;
    }

    /**
     * Return the GeoKeys.
     * 
     * @return the collection of all model keys
     */
    public Collection<GeoKeyEntry> getGeoKeys() {
        return geoKeys.values();
    }

    /**
     * Gets the model pixel scales from the correct TIFFField
     * 
     * @return the model pixel scales
     */
    public PixelScale getModelPixelScales() {
        return pixelScale;
    }

    /**
     * Gets the model tie points from the appropriate TIFFField
     *
     * @return the tie points, or null if not found
     */
    public TiePoint[] getModelTiePoints() {
        return tiePoints;
    }

    /**
     * Tells me if the underlying metdata contains ModelTiepointTag tag for
     * {@link TiePoint}.
     *
     * @return true if ModelTiepointTag is present, false otherwise.
     */
    public boolean hasTiePoints() {
        return tiePoints != null && tiePoints.length > 0;
    }

    /**
     * Tells me if the underlying metdata contains ModelTransformationTag tag for
     * {@link
     * AffineTransform} that map from Raster Space to World Space.
     *
     * @return true if ModelTransformationTag is present, false otherwise.
     */
    public boolean hasModelTrasformation() {
        return modelTransformation != null;
    }

    /**
     * Tells me if the underlying metadata contains ModelTiepointTag tag for
     * {@link TiePoint}.
     *
     * @return true if ModelTiepointTag is present, false otherwise.
     */
    public boolean hasPixelScales() {
        if (pixelScale == null) {
            return false;
        } else {
            final double[] values = pixelScale.getValues();
            for (double value : values) {
                if (Double.isInfinite(value) || Double.isNaN(value)) {
                    return false;
                }
            }
            return true;
        }
    }
}