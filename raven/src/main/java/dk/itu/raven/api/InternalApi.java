package dk.itu.raven.api;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.geometry.Size;
import dk.itu.raven.io.IRasterReader;
import dk.itu.raven.io.ImageMetadata;
import dk.itu.raven.io.ShapefileReader;
import dk.itu.raven.io.VectorData;
import dk.itu.raven.io.cache.CachedRasterStructure;
import dk.itu.raven.io.cache.RasterCache;
import dk.itu.raven.join.AbstractRavenJoin;
import dk.itu.raven.join.EmptyRavenJoin;
import dk.itu.raven.join.JoinChunk;
import dk.itu.raven.join.ParallelStreamedRavenJoin;
import dk.itu.raven.join.RavenJoin;
import dk.itu.raven.join.SpatialDataChunk;
import dk.itu.raven.join.StreamedRavenJoin;
import dk.itu.raven.ksquared.AbstractK2Raster;
import dk.itu.raven.ksquared.K2RasterBuilder;
import dk.itu.raven.ksquared.K2RasterIntBuilder;
import dk.itu.raven.util.Logger;
import dk.itu.raven.util.Logger.LogLevel;
import dk.itu.raven.util.matrix.Matrix;

public class InternalApi {

    /**
     * Creates a stream of join chunks containing the raster and rtree data.
     * 
     * @param geometries
     * @param rasterStream
     * @return a stream of the join chunks
     * @throws IOException
     */
    static Stream<JoinChunk> streamStructures(ShapefileReader featureReader,
            IRasterReader rasterReader, int widthStep,
            int heightStep, boolean isCaching)
            throws IOException {
        if (isCaching)
            isCaching = rasterReader.getDirectory().isPresent();

        // load geometries from shapefile
        VectorData geometries = featureReader.readShapefile();

        // TODO: check if it is faster to just use the original rtree
        RTree<String, Geometry> rtree = generateRTree(geometries.getFeatures());

        if (isCaching) {
            // create a cache for the raster structures
            // the cache key is the name of the dataset directory and the width and height
            // step
            RasterCache<CachedRasterStructure> cache = new RasterCache<CachedRasterStructure>(
                    rasterReader.getDirectory().get() + "-" + widthStep + "-" + heightStep);
            Stream<SpatialDataChunk> rasterStream = rasterReader.rasterPartitionStream(widthStep, heightStep,
                    Optional.of(cache), rtree, geometries);
            return rasterStream.map(chunk -> {
                // if the chunk is already cached, read it from cache
                if (cache.contains(chunk.getCacheKeyName())) {
                    Logger.log("Using cached raster structure " + chunk.getCacheKeyName(), LogLevel.DEBUG);
                    try {
                        CachedRasterStructure c = cache.readItem(chunk.getCacheKeyName());
                        return new JoinChunk(c.raster, c.offset, chunk.getTree());
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
                            new CachedRasterStructure(raster, chunk.getOffset(), chunk.getName()));
                } catch (IOException e) {
                    Logger.log("Failed to write to cache: " + e.getMessage(), LogLevel.ERROR);
                }

                // create the raster structure an potentially cache it
                return new JoinChunk(raster, chunk.getOffset(), chunk.getTree());
            });
        } else {
            Stream<SpatialDataChunk> rasterStream = rasterReader.rasterPartitionStream(widthStep, heightStep,
                    Optional.empty(), rtree, geometries);
            return rasterStream.map(chunk -> {
                AbstractK2Raster raster = generateRasterStructure(chunk.getMatrix());
                return new JoinChunk(raster, chunk.getOffset(), chunk.getTree());
            });
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
    static RTree<String, Geometry> generateRTree(List<Polygon> geometries) {
        RTree<String, Geometry> rtree = RTree.star().maxChildren(6).create();
        for (Polygon polygon : geometries) {
            rtree = rtree.add(null, polygon);
        }
        return rtree;
    }

    static AbstractRavenJoin getJoin(IRasterReader rasterReader, ShapefileReader vectorReader, boolean isCaching)
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

    static StreamedRavenJoin getStreamedJoin(IRasterReader rasterReader, ShapefileReader vectorReader, int widthStep,
            int heightStep, boolean parallel, boolean isCaching)
            throws IOException {
        ImageMetadata metadata = rasterReader.getImageMetadata();
        Size imageSize = new Size(metadata.getWidth(), metadata.getHeight());
        var structures = streamStructures(vectorReader, rasterReader, widthStep, heightStep, isCaching);
        Stream<RavenJoin> stream = structures.filter(chunk -> {
            return chunk.getRtree().root().isPresent();
        }).map(chunk -> {
            return new RavenJoin(chunk.getRaster(), chunk.getRtree(), chunk.getOffset(),
                    imageSize);
        });
        if (parallel) {
            return new ParallelStreamedRavenJoin(stream);
        } else {
            return new StreamedRavenJoin(stream);
        }

    }
}
