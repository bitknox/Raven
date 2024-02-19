package dk.itu.raven.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.JoinChunk;
import dk.itu.raven.SpatialDataChunk;
import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.FileRasterReader;
import dk.itu.raven.io.GeoToolsRasterReader;
import dk.itu.raven.io.ShapfileReader;
import dk.itu.raven.io.TFWFormat;
import dk.itu.raven.join.RavenJoin;
import dk.itu.raven.ksquared.AbstractK2Raster;
import dk.itu.raven.ksquared.K2RasterBuilder;
import dk.itu.raven.ksquared.K2RasterIntBuilder;
import dk.itu.raven.util.Logger;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.Logger.LogLevel;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.join.RasterFilterFunction;

/**
 * Public API for interacting with the raven library safely.
 * Only the methods in this class are intended to be used by the user.
 * However, the user is free to use the other classes in the library if they
 * wish.
 */
public class RavenApi {

	/**
	 * Joins the vector and raster data using the k2-raster, rtrees and a filter
	 * function to filter the results
	 * 
	 * @param k2Raster the k2-raster data
	 * @param rtree    the rtree data
	 * @param features the vector data
	 * @param filter   a filter function to filter the results
	 * @return a list of pairs of geometries and pixel ranges
	 */
	public List<Pair<Geometry, Collection<PixelRange>>> join(AbstractK2Raster k2Raster, RTree<String, Geometry> rtree,
			Iterable<Polygon> features, RasterFilterFunction filter) {
		return new RavenJoin(k2Raster, rtree).join(filter);
	}

	/**
	 * Joins the vector and raster data using the k2-raster and rtree
	 * 
	 * @param k2Raster
	 * @param rtree
	 * @param features
	 * @return a list of pairs of geometries and pixel ranges
	 */
	public List<Pair<Geometry, Collection<PixelRange>>> join(AbstractK2Raster k2Raster, RTree<String, Geometry> rtree,
			Iterable<Polygon> features) {
		return new RavenJoin(k2Raster, rtree).join();

	}

	/**
	 * Builds the data structures needed for join operation
	 * 
	 * @param vectorPath path to the vector data
	 * @param rasterPath path to the raster data
	 * @return a pair of the k2-raster and the rtree
	 * @throws IOException
	 */

	public Pair<AbstractK2Raster, RTree<String, Geometry>> buildStructures(String vectorPath, String rasterPath)
			throws IOException {
		Pair<Pair<Iterable<Polygon>, ShapfileReader.ShapeFileBounds>, Matrix> data = createReaders(vectorPath,
				rasterPath);
		AbstractK2Raster k2Raster = generateRasterStructure(data.second);
		RTree<String, Geometry> rtree = generateRTree(data.first);
		return new Pair<>(k2Raster, rtree);
	}

	public Stream<List<Pair<Geometry, Collection<PixelRange>>>> join(Stream<JoinChunk> stream) {
		return stream.map(chunk -> {
			if (!chunk.getRtree().root().isPresent())
				return new ArrayList<>();
			return new RavenJoin(chunk.getRaster(), chunk.getRtree()).join();
		});
	}

	public Stream<JoinChunk> streamBuildStructures(String vectorPath, String rasterPath) throws IOException {
		Pair<Pair<Iterable<Polygon>, ShapfileReader.ShapeFileBounds>, Stream<SpatialDataChunk>> data = createStreamers(
				vectorPath, rasterPath);
		// TODO: check if it is faster to just use the original rtree
		RTree<String, Geometry> rtree = generateRTree(data.first);
		Stream<SpatialDataChunk> streamRasters = data.second;
		return streamRasters.map(chunk -> {
			// RTree<String, Geometry> rtree2 = RTree.star().maxChildren(6).create();
			// for (var entry :
			// rtree.search(Geometries.rectangle(chunk.getOffset().getMinX(),
			// chunk.getOffset().getMinY(),
			// chunk.getOffset().getMaxX(), chunk.getOffset().getMaxY()))) {
			// rtree2.add(entry);
			// }
			Logger.log("matrix[0,0]: " + chunk.getMatrix().get(0, 0), LogLevel.DEBUG);
			return new JoinChunk(generateRasterStructure(chunk.getMatrix()), rtree);
		});

	}

	/**
	 * Reads the vector and stream raster data and returns the geometries and raster
	 * data
	 * 
	 * @param vectorPath
	 * @param rasterPath
	 * @return a pair of the geometries and a stream of the raster data
	 * @throws IOException
	 */
	public Pair<Pair<Iterable<Polygon>, ShapfileReader.ShapeFileBounds>, Stream<SpatialDataChunk>> createStreamers(
			String vectorPath,
			String rasterPath) throws IOException {
		// Read geo raster file
		FileRasterReader rasterReader = new GeoToolsRasterReader(new File(rasterPath));

		// get getTiff transform (used to transform from (lat, lon) to pixel coordinates
		// in shapefileReader)
		TFWFormat format = rasterReader.getTransform();

		ShapfileReader featureReader = new ShapfileReader(format);

		// load geometries from shapefile
		Pair<Iterable<Polygon>, ShapfileReader.ShapeFileBounds> geometries = featureReader
				.readShapefile(vectorPath);

		// rectangle representing the bounds of the shapefile data
		Rectangle rect = Geometries.rectangle(geometries.second.minX, geometries.second.minY,
				geometries.second.maxX,
				geometries.second.maxY);

		Stream<SpatialDataChunk> rasterData = rasterReader.streamRasters(rect);

		return new Pair<>(geometries, rasterData);
	}

	/**
	 * Reads the vector and raster data and returns the geometries and raster data
	 * 
	 * @param vectorPath
	 * @param rasterPath
	 * @return a pair of the geometries and the raster data
	 * @throws IOException
	 */
	public Pair<Pair<Iterable<Polygon>, ShapfileReader.ShapeFileBounds>, Matrix> createReaders(String vectorPath,
			String rasterPath) throws IOException {
		// Read geo raster file
		FileRasterReader rasterReader = new GeoToolsRasterReader(new File(rasterPath));

		// get getTiff transform (used to transform from (lat, lon) to pixel coordinates
		// in shapefileReader)
		TFWFormat format = rasterReader.getTransform();

		ShapfileReader featureReader = new ShapfileReader(format);

		// load geometries from shapefile
		Pair<Iterable<Polygon>, ShapfileReader.ShapeFileBounds> geometries = featureReader
				.readShapefile(vectorPath);

		// rectangle representing the bounds of the shapefile data
		Rectangle rect = Geometries.rectangle(geometries.second.minX, geometries.second.minY,
				geometries.second.maxX,
				geometries.second.maxY);

		Matrix rasterData = rasterReader.readRasters(rect);

		return new Pair<>(geometries, rasterData);
	}

	/**
	 * Generates a k2-raster structure from the raster data
	 * 
	 * @param rasterData
	 * @return the k2-raster
	 */
	public AbstractK2Raster generateRasterStructure(Matrix rasterData) {
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
	public RTree<String, Geometry> generateRTree(Pair<Iterable<Polygon>, ShapfileReader.ShapeFileBounds> geometries) {
		// create a R* tree with
		RTree<String, Geometry> rtree = RTree.star().maxChildren(6).create();

		// offset geometries such that they are aligned to the corner
		double offsetX = geometries.second.minX > 0 ? -geometries.second.minX : 0;
		double offsetY = geometries.second.minY > 0 ? -geometries.second.minY : 0;
		for (Polygon geom : geometries.first) {
			geom.offset(offsetX, offsetY);

			rtree = rtree.add(null, geom);
		}
		return rtree;
	}
}
