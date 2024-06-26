package dk.itu.raven.io;

import java.awt.geom.NoninvertibleTransformException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;

import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.cache.CachedRasterStructure;
import dk.itu.raven.io.cache.RasterCache;
import dk.itu.raven.join.SpatialDataChunk;
import dk.itu.raven.util.Logger;
import dk.itu.raven.util.Logger.LogLevel;

public class MultiFileRasterReader implements IRasterReader {

    private Stream<ImageIORasterReader> readers;
    private ImageMetadata metadata;
    private File directory;

    public MultiFileRasterReader(File directory) throws IOException {
        this.directory = directory;
        List<File> files = Arrays.asList(directory.listFiles());
        ImageIORasterReader reader = new ImageIORasterReader(files.get(0));
        this.metadata = reader.getImageMetadata();
        Stream<ImageIORasterReader> singleStream = Stream.of(reader);
        Stream<ImageIORasterReader> stream = files.subList(1, files.size()).stream().map(f -> {
            if (f.isDirectory()) {
                try {
                    return new ImageIORasterReader(f);
                } catch (IOException e) {
                    Logger.log("Could not create reader for" + f.getAbsolutePath() + ". Skipping Image",
                            LogLevel.ERROR);
                    e.printStackTrace();
                }
            }
            return null;
        });

        this.readers = Stream.concat(singleStream, stream);
    }

    public Stream<SpatialDataChunk> rasterPartitionStream(int widthStep, int heightStep,
            Optional<RasterCache<CachedRasterStructure>> cache, RTree<Object, Geometry> rtree, VectorData vectorData)
            throws IOException {
        return readers.filter(r -> r != null)
                .map(reader -> {
                    try {
                        CoordinateReferenceSystem targetCRS = reader.getCRS();
                        TFWFormat g2m = reader.getG2M();
                        MathTransform transform = Reprojector.calculateFullTransform(vectorData.getCRS(), targetCRS,
                                g2m);
                        MathTransform inverseTransform = transform.inverse();

                        double[] topLeftPixel = new double[]{0, 0};
                        double[] bottomRightPixel = new double[]{reader.getImageMetadata().getWidth(),
                            reader.getImageMetadata().getHeight()};
                        double[] topLeftLatLong = new double[2];
                        double[] bottomRightLatLong = new double[2];

                        inverseTransform.transform(topLeftPixel, 0, topLeftLatLong, 0, 1);
                        inverseTransform.transform(bottomRightPixel, 0, bottomRightLatLong, 0, 1);

                        Logger.log("Top left: " + topLeftLatLong[0] + ", " + topLeftLatLong[1], LogLevel.DEBUG);
                        Logger.log("Bottom right: " + bottomRightLatLong[0] + ", " + bottomRightLatLong[1],
                                LogLevel.DEBUG);

                        Rectangle bounds = Geometries.rectangle(topLeftLatLong[0], bottomRightLatLong[1],
                                bottomRightLatLong[0], topLeftLatLong[1]);

                        Logger.log("Bounds: " + bounds.x1() + ", " + bounds.y1() + ", " + bounds.x2() + ", "
                                + bounds.y2(), Logger.LogLevel.INFO);

                        Iterable<Entry<Object, Geometry>> overlapping = rtree.search(bounds);
                        if (!overlapping.iterator().hasNext()) {
                            Logger.log("No overlapping data found, skipping image", LogLevel.DEBUG);
                            return null;
                        }
                        RTree<Object, Geometry> rtree2 = RTree.star().maxChildren(6).create();

                        for (Entry<Object, Geometry> entry : overlapping) {
                            rtree2 = rtree2.add(null, ((Polygon) entry.geometry()).transform(transform));
                        }

                        Logger.log(rtree2.root().get().geometry().mbr(), LogLevel.DEBUG);

                        return reader.rasterPartitionStream(widthStep, heightStep,
                                cache, rtree2, null).parallel();
                    } catch (IOException e) {
                        Logger.log("Unable to read raster data, skipping image",
                                LogLevel.ERROR);
                    } catch (TransformException e) {
                        Logger.log("Unable to transform vector data to pixel CRS, skipping image",
                                LogLevel.ERROR);
                    } catch (NoninvertibleTransformException e) {
                        Logger.log("Unable to transform from image CRS to pixel coordinates, skipping image",
                                LogLevel.ERROR);
                    } catch (FactoryException e) {
                        Logger.log("Unable to transform from vector CRS to raster CRS, skipping image",
                                LogLevel.ERROR);
                    }
                    return null;
                }).filter(c -> c != null).reduce(Stream::concat).orElse(Stream.empty());
    }

    @Override
    public ImageMetadata getImageMetadata() throws IOException {
        return this.metadata;
    }

    @Override
    public Optional<String> getDirectoryName() {
        return Optional.of(directory.getName());
    }

    @Override
    public Optional<File> getDirectory() {
        return Optional.of(directory);
    }
}
