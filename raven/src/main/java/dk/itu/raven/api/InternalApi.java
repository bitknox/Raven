package dk.itu.raven.api;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;

import dk.itu.raven.geometry.Size;
import dk.itu.raven.io.IRasterReader;
import dk.itu.raven.io.ImageMetadata;
import dk.itu.raven.io.ShapefileReader;
import dk.itu.raven.io.VectorData;
import dk.itu.raven.io.cache.CacheOptions;
import dk.itu.raven.io.cache.CachedRasterStructure;
import dk.itu.raven.io.cache.RasterCache;
import dk.itu.raven.io.commandline.ResultType;
import dk.itu.raven.join.AbstractRavenJoin;
import dk.itu.raven.join.EmptyRavenJoin;
import dk.itu.raven.join.JoinChunk;
import dk.itu.raven.join.ParallelStreamedRavenJoin;
import dk.itu.raven.join.RavenJoin;
import dk.itu.raven.join.SpatialDataChunk;
import dk.itu.raven.join.StreamedRavenJoin;
import dk.itu.raven.join.results.IResultCreator;
import dk.itu.raven.join.results.PixelRangeCreator;
import dk.itu.raven.join.results.PixelRangeValueCreator;
import dk.itu.raven.join.results.PixelValueCreator;
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
            int heightStep, CacheOptions cacheOptions, int kSize, int rTreeMinChildren, int rTreeMaxChildren,
            IResultCreator resultCreator)
            throws IOException {
        if (cacheOptions.isCaching) {
            cacheOptions.isCaching = rasterReader.getDirectoryName().isPresent();
        }

        // load geometries from shapefile
        VectorData geometries = featureReader.readShapefile();

        RTree<Object, Geometry> rtree = generateRTree(geometries.getFeatures(), rTreeMinChildren, rTreeMaxChildren);

        if (cacheOptions.isCaching) {
            // create a cache for the raster structures
            // the cache key is the name of the dataset directory and the width and height
            // step
            RasterCache<CachedRasterStructure> cache = new RasterCache<>(
                    cacheOptions.getCacheDir(),
                    rasterReader.getDirectoryName().get() + "-" + widthStep + "-" + heightStep + "-k" + kSize);
            Stream<SpatialDataChunk> rasterStream = rasterReader.rasterPartitionStream(widthStep, heightStep,
                    Optional.of(cache), rtree, geometries);
            return rasterStream.map(chunk -> {
                // if the chunk is already cached, read it from cache
                if (cache.contains(chunk.getCacheKeyName())) {
                    Logger.log("Using cached raster structure " + chunk.getCacheKeyName(), LogLevel.DEBUG);
                    try {
                        CachedRasterStructure c = cache.readItem(chunk.getCacheKeyName());
                        c.raster.setResultCreator(resultCreator);
                        return new JoinChunk(c.raster, c.offset, chunk.getTree(), chunk.getDirectory());
                    } catch (Exception e) {
                        Logger.log("Item was in cache index, but not found on disk", LogLevel.ERROR);
                        System.exit(-1);
                    }
                }
                // cache has not been hit, generate structure
                Logger.log("matrix[0,0]: " + chunk.getMatrix().get(0, 0), LogLevel.DEBUG);
                AbstractK2Raster raster = generateRasterStructure(chunk.getMatrix(), kSize);
                raster.setResultCreator(resultCreator);

                // write the structure to the cache
                try {
                    cache.addRasterToCache(chunk.getCacheKeyName(),
                            new CachedRasterStructure(raster, chunk.getOffset(), chunk.getName()));
                } catch (IOException e) {
                    Logger.log("Failed to write to cache: " + e.getMessage(), LogLevel.ERROR);
                }

                // create the raster structure an potentially cache it
                return new JoinChunk(raster, chunk.getOffset(), chunk.getTree(), chunk.getDirectory());
            });
        } else {
            Stream<SpatialDataChunk> rasterStream = rasterReader.rasterPartitionStream(widthStep, heightStep,
                    Optional.empty(), rtree, geometries);
            return rasterStream.map(chunk -> {
                AbstractK2Raster raster = generateRasterStructure(chunk.getMatrix(), kSize);
                raster.setResultCreator(resultCreator);
                return new JoinChunk(raster, chunk.getOffset(), chunk.getTree(), chunk.getDirectory());
            });
        }

    }

    /**
     * Generates a k2-raster structure from the raster data
     *
     * @param rasterData
     * @return the k2-raster
     */
    static AbstractK2Raster generateRasterStructure(Matrix rasterData, int k) {
        AbstractK2Raster k2Raster;
        if (rasterData.getBitsUsed() > 32) {
            k2Raster = new K2RasterBuilder().build(rasterData, k);
        } else {
            k2Raster = new K2RasterIntBuilder().build(rasterData, k);
        }
        return k2Raster;
    }

    /**
     * Generates a R* tree from the vector data
     *
     * @param geometries the geometires that should be included in the R-tree
     * @param minChildren minimum children, this parameter is passed on to the
     * R-tree
     * @param maxChildren maximum children, this parameter is passed on to the
     * R-tree
     * @return the R* tree
     */
    public static RTree<Object, Geometry> generateRTree(List<Entry<Object, Geometry>> geometries, int minChildren,
            int maxChildren) {
        RTree<Object, Geometry> rtree = RTree.star().minChildren(minChildren).maxChildren(maxChildren)
                .create(geometries);
        return rtree;
    }

    static AbstractRavenJoin getJoin(IRasterReader rasterReader, ShapefileReader vectorReader,
            CacheOptions cacheOptions,
            int kSize, int rTreeMinChildren, int rTreeMaxChildren, IResultCreator resultCreator)
            throws IOException {
        ImageMetadata metadata = rasterReader.getImageMetadata();
        Optional<RavenJoin> streamedJoin = getStreamedJoin(rasterReader, vectorReader, metadata.getWidth(),
                metadata.getHeight(), false, cacheOptions, kSize, rTreeMinChildren, rTreeMaxChildren, resultCreator)
                .getRavenJoins().findFirst();
        if (streamedJoin.isPresent()) {
            return streamedJoin.get();
        } else {
            return new EmptyRavenJoin();
        }
    }

    static StreamedRavenJoin getStreamedJoin(IRasterReader rasterReader, ShapefileReader vectorReader, int widthStep,
            int heightStep, boolean parallel, CacheOptions cacheOptions, int kSize, int rTreeMinChildren,
            int rTreeMaxChildren, IResultCreator resultCreator)
            throws IOException {
        ImageMetadata metadata = rasterReader.getImageMetadata();
        Size imageSize = new Size(metadata.getWidth(), metadata.getHeight());
        var structures = streamStructures(vectorReader, rasterReader, widthStep, heightStep, cacheOptions, kSize,
                rTreeMinChildren, rTreeMaxChildren, resultCreator);
        Stream<RavenJoin> stream = structures.filter(chunk -> {
            return chunk.getRtree().root().isPresent();
        }).map(chunk -> {
            return new RavenJoin(chunk.getRaster(), chunk.getRtree(), chunk.getOffset(),
                    imageSize, chunk.getDirectory());
        });
        if (parallel) {
            return new ParallelStreamedRavenJoin(stream);
        } else {
            return new StreamedRavenJoin(stream);
        }
    }

    static public IResultCreator getResultCreator(ResultType type) {
        switch (type) {
            case RANGE -> {
                return new PixelRangeCreator();
            }
            case RANGEVALUE -> {
                return new PixelRangeValueCreator();
            }
            case VALUE -> {
                return new PixelValueCreator();
            }
        }
        return null;
    }
}
