package dk.itu.raven.api;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.JoinChunk;
import dk.itu.raven.SpatialDataChunk;
import dk.itu.raven.geometry.GeometryUtil;
import dk.itu.raven.geometry.Offset;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.geometry.Size;
import dk.itu.raven.io.FileRasterReader;
import dk.itu.raven.io.ImageIORasterReader;
import dk.itu.raven.io.ImageMetadata;
import dk.itu.raven.io.ShapefileReader;
import dk.itu.raven.io.TFWFormat;
import dk.itu.raven.join.ParallelStreamedRavenJoin;
import dk.itu.raven.join.RavenJoin;
import dk.itu.raven.join.StreamedRavenJoin;
import dk.itu.raven.ksquared.AbstractK2Raster;
import dk.itu.raven.ksquared.K2RasterBuilder;
import dk.itu.raven.ksquared.K2RasterIntBuilder;
import dk.itu.raven.util.Logger;
import dk.itu.raven.util.Logger.LogLevel;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.matrix.Matrix;

/**
 * Public API for interacting with the raven library safely.
 * Only the methods in this class are intended to be used by the user.
 * However, the user is free to use the other classes in the library if they
 * wish.
 */
public class RavenApi {

	/**
	 * constructs a RavenJoin object
	 * 
	 * @param rasterPath
	 * @param vectorPath
	 * @return
	 * @throws IOException
	 */
	public RavenJoin getJoin(String rasterPath, String vectorPath) throws IOException {
		FileRasterReader rasterReader = createRasterReader(rasterPath);
		ShapefileReader vectorReader = createShapefileReader(vectorPath, rasterReader.getTransform());

		ImageMetadata metadata = rasterReader.getImageMetadata();
		Size imageSize = new Size(metadata.getWidth(), metadata.getHeight());
		Pair<AbstractK2Raster, RTree<String, Geometry>> structures = buildStructures(vectorReader, rasterReader);
		return new RavenJoin(structures.first, structures.second, imageSize);
	}

	/**
	 * Joins the vector and raster data using the k2-raster and rtree as a stream
	 * 
	 * @param k2Raster
	 * @param rtree
	 * @param features
	 * @return a stream of results of the join
	 */
	public StreamedRavenJoin getStreamedJoin(String rasterPath, String vectorPath,
			int widthStep, int heightStep, boolean parallel) throws IOException {
		FileRasterReader rasterReader = createRasterReader(rasterPath);
		ShapefileReader vectorReader = createShapefileReader(vectorPath, rasterReader.getTransform());

		ImageMetadata metadata = rasterReader.getImageMetadata();
		Size imageSize = new Size(metadata.getWidth(), metadata.getHeight());

		var pair = streamData(vectorReader, rasterReader, widthStep, heightStep);
		var stream = streamStructures(pair.first, pair.second).filter(chunk -> {
			return chunk.getRtree().root().isPresent();
		}).map(chunk -> {
			return new RavenJoin(chunk.getRaster(), chunk.getRtree(), chunk.getOffset(), imageSize);
		});
		if (parallel) {
			return new ParallelStreamedRavenJoin(stream);
		} else {
			return new StreamedRavenJoin(stream);
		}
	}

	/**
	 * Reads the raster data and returns a FileRasterReader
	 * 
	 * @param rasterPath
	 * @return the raster reader
	 * @throws IOException
	 */
	public FileRasterReader createRasterReader(String rasterPath) throws IOException {
		return new ImageIORasterReader(new File(rasterPath));
	}

	/**
	 * Reads the shapefile data and returns a ShapefileReader
	 * 
	 * @param vectorPath the path to the shapefile
	 * @param transform  the transform to use for the shapefile
	 * @return the shapefile reader
	 */
	public ShapefileReader createShapefileReader(String vectorPath, TFWFormat transform) {
		return new ShapefileReader(vectorPath, transform);
	}

	/**
	 * Builds the data structures needed for join operation
	 * 
	 * @param vectorPath path to the vector data
	 * @param rasterPath path to the raster data
	 * @return a pair of the k2-raster and the rtree
	 * @throws IOException
	 */
	private Pair<AbstractK2Raster, RTree<String, Geometry>> buildStructures(ShapefileReader featureReader,
			FileRasterReader rasterReader)
			throws IOException {
		Pair<Pair<List<Polygon>, ShapefileReader.ShapeFileBounds>, Matrix> data = readData(featureReader,
				rasterReader);
		AbstractK2Raster k2Raster = generateRasterStructure(data.second);
		RTree<String, Geometry> rtree = generateRTree(data.first);
		return new Pair<>(k2Raster, rtree);
	}

	/**
	 * Creates a stream of join chunks containing the raster and rtree data.
	 * 
	 * @param geometries
	 * @param rasterStream
	 * @return a stream of the join chunks
	 * @throws IOException
	 */
	private Stream<JoinChunk> streamStructures(Pair<List<Polygon>, ShapefileReader.ShapeFileBounds> geometries,
			Stream<SpatialDataChunk> rasterStream)
			throws IOException {
		// TODO: check if it is faster to just use the original rtree
		RTree<String, Geometry> rtree = generateRTree(geometries);
		return rasterStream.map(chunk -> {
			// List<Polygon> polygons = new ArrayList<>();
			// for (int i = 0; i < geometries.first.size(); i++) {
			// polygons.add(geometries.first.get(i).copy());
			// }

			// RTree<String, Geometry> rtree = generateRTree(
			// new Pair<List<Polygon>, ShapefileReader.ShapeFileBounds>(polygons,
			// geometries.second));

			Logger.log("matrix[0,0]: " + chunk.getMatrix().get(0, 0), LogLevel.DEBUG);
			return new JoinChunk(generateRasterStructure(chunk.getMatrix()), chunk.getOffset(), rtree);
		});

	}

	/**
	 * Reads the vector and raster data and returns the geometries and raster data
	 * 
	 * @param vectorPath
	 * @param rasterPath
	 * @return a pair of the geometries and the raster data
	 * @throws IOException
	 */
	private Pair<Pair<List<Polygon>, ShapefileReader.ShapeFileBounds>, Matrix> readData(
			ShapefileReader featureReader,
			FileRasterReader rasterReader) throws IOException {

		// load geometries from shapefile
		Pair<List<Polygon>, ShapefileReader.ShapeFileBounds> geometries = featureReader
				.readShapefile();

		// rectangle representing the bounds of the shapefile data
		Rectangle rect = Geometries.rectangle(geometries.second.minX, geometries.second.minY,
				geometries.second.maxX,
				geometries.second.maxY);

		Matrix rasterData = rasterReader.readRasters(rect);

		return new Pair<>(geometries, rasterData);
	}

	/**
	 * Reads the vector and stream raster data and returns the geometries and raster
	 * data
	 * 
	 * @param vectorPath
	 * @param rasterReader
	 * @return a pair of the geometries and a stream of the raster data
	 * @throws IOException
	 */
	private Pair<Pair<List<Polygon>, ShapefileReader.ShapeFileBounds>, Stream<SpatialDataChunk>> streamData(
			ShapefileReader featureReader, FileRasterReader rasterReader, int widthStep, int heightStep)
			throws IOException {
		// load geometries from shapefile
		Pair<List<Polygon>, ShapefileReader.ShapeFileBounds> geometries = featureReader
				.readShapefile();

		// rectangle representing the bounds of the shapefile data
		Rectangle rect = Geometries.rectangle(geometries.second.minX, geometries.second.minY,
				geometries.second.maxX,
				geometries.second.maxY);

		Stream<SpatialDataChunk> rasterData = rasterReader.rasterPartitionStream(rect, widthStep, heightStep);

		return new Pair<>(geometries, rasterData);
	}

	/**
	 * Generates a k2-raster structure from the raster data
	 * 
	 * @param rasterData
	 * @return the k2-raster
	 */
	private AbstractK2Raster generateRasterStructure(Matrix rasterData) {
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
	private RTree<String, Geometry> generateRTree(Pair<List<Polygon>, ShapefileReader.ShapeFileBounds> geometries) {
		// create a R* tree with
		RTree<String, Geometry> rtree = RTree.star().maxChildren(6).create();

		// offset geometries such that they are aligned to the corner
		Offset<Double> offset = GeometryUtil.getGeometryOffset(geometries.second);
		for (Polygon geom : geometries.first) {
			geom.offset(offset.getOffsetX(), offset.getOffsetY());

			rtree = rtree.add(null, geom);
		}
		return rtree;
	}
}
