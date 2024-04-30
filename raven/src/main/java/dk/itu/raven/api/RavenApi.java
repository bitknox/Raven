package dk.itu.raven.api;

import java.io.File;
import java.io.IOException;

import dk.itu.raven.io.IRasterReader;
import dk.itu.raven.io.MultiFileRasterReader;
import dk.itu.raven.io.ShapefileReader;
import dk.itu.raven.io.cache.CacheOptions;
import dk.itu.raven.io.commandline.ResultType;
import dk.itu.raven.join.AbstractRavenJoin;
import dk.itu.raven.join.StreamedRavenJoin;
import dk.itu.raven.join.results.IResultCreator;
import dk.itu.raven.join.results.PixelRangeCreator;
import dk.itu.raven.join.results.PixelValueCreator;
import dk.itu.raven.ksquared.dac.AbstractDAC;

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
	public AbstractRavenJoin getJoin(String rasterPath, String vectorPath, CacheOptions cacheOptions, int kSize,
			int rTreeMinChildren, int rTreeMaxChildren, ResultType resultType) throws IOException {
		IRasterReader rasterReader = createRasterReader(rasterPath);
		ShapefileReader vectorReader = createShapefileReader(vectorPath);

		return InternalApi.getJoin(rasterReader, vectorReader, cacheOptions, kSize, rTreeMinChildren, rTreeMaxChildren,
				getResultCreator(resultType));
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
			int widthStep, int heightStep, boolean parallel, CacheOptions cacheOptions, int kSize, int rTreeMinChildren,
			int rTreeMaxChildren, ResultType resultType) throws IOException {
		IRasterReader rasterReader = createRasterReader(rasterPath);
		ShapefileReader vectorReader = createShapefileReader(vectorPath);

		return InternalApi.getStreamedJoin(rasterReader, vectorReader, widthStep, heightStep, parallel, cacheOptions,
				kSize, rTreeMinChildren, rTreeMaxChildren, getResultCreator(resultType));
	}

	/**
	 * Reads the raster data and returns a FileRasterReader
	 * 
	 * @param rasterPath
	 * @return the raster reader
	 * @throws IOException
	 */
	public IRasterReader createRasterReader(String rasterPath) throws IOException {
		return new MultiFileRasterReader(new File(rasterPath));
	}

	/**
	 * Reads the shapefile data and returns a ShapefileReader
	 * 
	 * @param vectorPath the path to the shapefile
	 * @param transform  the transform to use for the shapefile
	 * @return the shapefile reader
	 */
	public ShapefileReader createShapefileReader(String vectorPath) {
		return new ShapefileReader(vectorPath);
	}

	public void setDACFraction(int size) {
		AbstractDAC.FACT_RANK = size;
	}

	private IResultCreator getResultCreator(ResultType type) {
		switch (type) {
			case RANGE:
				return new PixelRangeCreator();
			case RANGEVALUE:
				return new PixelRangeCreator();
			case VALUE:
				return new PixelValueCreator();
		}
		return null;
	}
}
