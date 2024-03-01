package dk.itu.raven.api;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.geometry.GeometryUtil;
import dk.itu.raven.geometry.Offset;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.geometry.Size;
import dk.itu.raven.io.ImageMetadata;
import dk.itu.raven.io.RasterReader;
import dk.itu.raven.io.ShapefileReader;
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
        Rectangle rect = Geometries.rectangle(bounds.minX, bounds.minY, bounds.maxX, bounds.maxY);
        ImageMetadata imageSize = rasterReader.getImageMetadata();
        int startX = (int) Math.max(rect.x1(), 0);
        int startY = (int) Math.max(rect.y1(), 0);
        int endX = (int) Math.ceil(Math.min(imageSize.getWidth(), rect.x2()));
        int endY = (int) Math.ceil(Math.min(imageSize.getHeight(), rect.y2()));
        return new java.awt.Rectangle(startX, startY, endX - startX, endY - startY);
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
            int heightStep)
            throws IOException {
        // load geometries from shapefile
        Pair<List<Polygon>, ShapefileReader.ShapeFileBounds> geometries = featureReader
                .readShapefile();

        // rectangle representing the bounds of the shapefile data
        java.awt.Rectangle rect = getWindowRectangle(rasterReader, geometries.second);

        Stream<SpatialDataChunk> rasterStream = rasterReader.rasterPartitionStream(rect, widthStep, heightStep);
        // TODO: check if it is faster to just use the original rtree
        RTree<String, Geometry> rtree = generateRTree(geometries);
        return new Pair<>(rasterStream.map(chunk -> {
            Logger.log("matrix[0,0]: " + chunk.getMatrix().get(0, 0), LogLevel.DEBUG);
            return new JoinChunk(generateRasterStructure(chunk.getMatrix()), chunk.getOffset(), rtree);
        }), rect);

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

    static AbstractRavenJoin getJoin(RasterReader rasterReader, ShapefileReader vectorReader) throws IOException {
        ImageMetadata metadata = rasterReader.getImageMetadata();
        Optional<RavenJoin> streamedJoin = getStreamedJoin(rasterReader, vectorReader, metadata.getWidth(),
                metadata.getHeight(), false)
                .getRavenJoins().findFirst();
        if (streamedJoin.isPresent()) {
            return streamedJoin.get();
        } else {
            return new EmptyRavenJoin();
        }
    }

    static StreamedRavenJoin getStreamedJoin(RasterReader rasterReader, ShapefileReader vectorReader, int widthStep,
            int heightStep, boolean parallel)
            throws IOException {
        ImageMetadata metadata = rasterReader.getImageMetadata();
        Size imageSize = new Size(metadata.getWidth(), metadata.getHeight());
        var structures = streamStructures(vectorReader, rasterReader, widthStep, heightStep);
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
