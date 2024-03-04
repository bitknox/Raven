package dk.itu.raven.api;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.geometry.GeometryUtil;
import dk.itu.raven.geometry.Offset;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.geometry.Size;
import dk.itu.raven.io.ImageMetadata;
import dk.itu.raven.io.RasterReader;
import dk.itu.raven.io.ShapefileReader;
import dk.itu.raven.io.cache.CachedRasterStructure;
import dk.itu.raven.io.cache.RasterCache;
import dk.itu.raven.join.AbstractRavenJoin;
import dk.itu.raven.join.JoinChunk;
import dk.itu.raven.join.ParallelStreamedRavenJoin;
import dk.itu.raven.join.RavenJoin;
import dk.itu.raven.join.EmptyRavenJoin;
import dk.itu.raven.join.SpatialDataChunk;
import dk.itu.raven.join.StreamedRavenJoin;
import dk.itu.raven.ksquared.AbstractK2Raster;
import dk.itu.raven.ksquared.K2RasterBuilder;
import dk.itu.raven.ksquared.K2RasterIntBuilder;
import dk.itu.raven.util.Logger;
import dk.itu.raven.util.Logger.LogLevel;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.matrix.Matrix;

public class InternalApi {

    static java.awt.Rectangle getWindowRectangle(RasterReader rasterReader, ShapefileReader.ShapeFileBounds bounds)
            throws IOException {
        ImageMetadata imageSize = rasterReader.getImageMetadata();
        return GeometryUtil.getWindowRectangle(new Size(imageSize.getWidth(), imageSize.getHeight()), bounds);
    }

    /**
     * Creates a stream of join chunks containing the raster and rtree data.
     * 
     * @param geometries
     * @param rasterStream
     * @return a stream of the join chunks
     * @throws IOException
     */
    static Pair<Stream<JoinChunk>, java.awt.Rectangle> streamStructures(ShapefileReader featureReader,
            RasterReader rasterReader, int widthStep,
            int heightStep, boolean isCaching)
            throws IOException {
        if (isCaching)
            isCaching = rasterReader.getCacheKey().isPresent();

        // load geometries from shapefile
        Pair<List<Polygon>, ShapefileReader.ShapeFileBounds> geometries = featureReader
                .readShapefile();

        // rectangle representing the bounds of the shapefile data
        java.awt.Rectangle rect = getWindowRectangle(rasterReader, geometries.second);

        // TODO: check if it is faster to just use the original rtree
        RTree<String, Geometry> rtree = generateRTree(geometries);

        if (isCaching) {
            String key = rasterReader.getCacheKey().get();
            Logger.log("Set cache key " + key, LogLevel.DEBUG);
            RasterCache<CachedRasterStructure> cache = new RasterCache<CachedRasterStructure>(
                    rasterReader.getCacheKey().get() + widthStep + "-" + heightStep);
            Stream<SpatialDataChunk> rasterStream = rasterReader.rasterPartitionStream(rect, widthStep, heightStep,
                    Optional.of(cache));
            return new Pair<>(rasterStream.map(chunk -> {

                // if the chunk is already cached, read it from cache
                if (chunk.getCacheKey().isPresent()) {
                    Logger.log("Using cached raster structure " + chunk.getCacheKey().get(), LogLevel.DEBUG);
                    try {
                        CachedRasterStructure c = cache.readItem(chunk.getCacheKey().get());
                        return new JoinChunk(c.raster, c.offset, rtree);
                    } catch (Exception e) {
                        Logger.log("Item was in cache index, but not found on disk", LogLevel.ERROR);
                        System.exit(-1);
                    }
                }
                // cache has not been hit, generate structure
                Logger.log("matrix[0,0]: " + chunk.getMatrix().get(0, 0), LogLevel.DEBUG);
                AbstractK2Raster raster = generateRasterStructure(chunk.getMatrix());

                // write the structure to the cache
                try {
                    cache.addRasterToCache(chunk.getCacheKeyName(),
                            new CachedRasterStructure(raster, chunk.getOffset()));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // create the raster structure an potentially cache it
                return new JoinChunk(raster, chunk.getOffset(), rtree);
            }), rect);
        } else {
            Stream<SpatialDataChunk> rasterStream = rasterReader.rasterPartitionStream(rect, widthStep, heightStep,
                    Optional.empty());
            return new Pair<>(rasterStream.map(chunk -> {
                AbstractK2Raster raster = generateRasterStructure(chunk.getMatrix());
                return new JoinChunk(raster, chunk.getOffset(), rtree);
            }), rect);
        }

    }

    /**
     * Generates a k2-raster structure from the raster data
     * 
     * @param rasterData
     * @return the k2-raster
     */
    static AbstractK2Raster generateRasterStructure(Matrix rasterData) {
        AbstractK2Raster k2Raster;
        if (rasterData.getBitsUsed() > 32) {
            k2Raster = new K2RasterBuilder().build(rasterData, 2);
        } else {
            k2Raster = new K2RasterIntBuilder().build(rasterData, 2);
        }
        return k2Raster;
    }

    /**
     * Generates a R* tree from the vector data
     * 
     * @param geometries
     * @return the R* tree
     */
    static RTree<String, Geometry> generateRTree(Pair<List<Polygon>, ShapefileReader.ShapeFileBounds> geometries) {
        RTree<String, Geometry> rtree = RTree.star().maxChildren(6).create();

        // offset geometries such that they are aligned to the corner
        Offset<Double> offset = GeometryUtil.getGeometryOffset(geometries.second);
        for (Polygon geom : geometries.first) {
            geom.offset(offset.getOffsetX(), offset.getOffsetY());

            rtree = rtree.add(null, geom);
        }
        return rtree;
    }

    static AbstractRavenJoin getJoin(RasterReader rasterReader, ShapefileReader vectorReader, boolean isCaching)
            throws IOException {
        ImageMetadata metadata = rasterReader.getImageMetadata();
        Optional<RavenJoin> streamedJoin = getStreamedJoin(rasterReader, vectorReader, metadata.getWidth(),
                metadata.getHeight(), false, isCaching)
                .getRavenJoins().findFirst();
        if (streamedJoin.isPresent()) {
            return streamedJoin.get();
        } else {
            return new EmptyRavenJoin();
        }
    }

    static StreamedRavenJoin getStreamedJoin(RasterReader rasterReader, ShapefileReader vectorReader, int widthStep,
            int heightStep, boolean parallel, boolean isCaching)
            throws IOException {
        ImageMetadata metadata = rasterReader.getImageMetadata();
        Size imageSize = new Size(metadata.getWidth(), metadata.getHeight());
        var structures = streamStructures(vectorReader, rasterReader, widthStep, heightStep, isCaching);
        Stream<RavenJoin> stream = structures.first.filter(chunk -> {
            return chunk.getRtree().root().isPresent();
        }).map(chunk -> {
            return new RavenJoin(chunk.getRaster(), chunk.getRtree(), chunk.getOffset(), imageSize, structures.second);
        });
        if (parallel) {
            return new ParallelStreamedRavenJoin(stream, structures.second);
        } else {
            return new StreamedRavenJoin(stream, structures.second);
        }

    }
}
