package dk.itu.raven.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Point;

import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.util.Logger;

public class ShapefileReader {

	private File file;
	protected ShapeFileBounds bounds;

	public ShapefileReader(String path) {
		this.file = new File(path);
		this.bounds = new ShapeFileBounds();
	}

	public class ShapeFileBounds {
		public double minX, minY, maxX, maxY;

		public ShapeFileBounds() {
			this.reset();
		}

		public void updateBounds(double x1, double y1, double x2, double y2) {
			minX = Math.min(minX, x1);
			minY = Math.min(minY, y1);
			maxX = Math.max(maxX, x2);
			maxY = Math.max(maxY, y2);
		}

		public void reset() {
			minX = Double.MAX_VALUE;
			minY = Double.MAX_VALUE;
			maxX = Double.NEGATIVE_INFINITY;
			maxY = Double.NEGATIVE_INFINITY;
		}
	}

	public ShapeFileBounds getBounds() {
		return this.bounds;
	}

	public VectorData readShapefile()
			throws IOException {
		FileDataStore myData = FileDataStoreFinder.getDataStore(file);
		SimpleFeatureSource source = myData.getFeatureSource();
		bounds.reset();
		List<Polygon> features = new ArrayList<>();
		FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures();

		CoordinateReferenceSystem crs = source.getSchema().getCoordinateReferenceSystem();
		if (crs == null) {
			try {
				Logger.log("(Vector) Cannot create CRS from metadata, using default CRS", Logger.LogLevel.DEBUG);
				crs = CRS.decode("EPSG:4326", true);
			} catch (Exception e) {
				Logger.log("Could not decode EPSG:4326 exception(" + e + ")", Logger.LogLevel.ERROR);
				e.printStackTrace();
			}
		}

		try (FeatureIterator<SimpleFeature> featuresItr = collection.features()) {
			while (featuresItr.hasNext()) {
				SimpleFeature feature = featuresItr.next();
				Geometry geom = (Geometry) feature.getDefaultGeometry();
				extractGeometries(geom, features);
			}
		} finally {
			myData.dispose();
		}
		return new VectorData(features, bounds, crs);
	}

	private void extractGeometries(Geometry geometry,
			List<Polygon> features) {
		if (geometry.getNumGeometries() > 1) {
			for (int i = 0; i < geometry.getNumGeometries(); i++) {
				Geometry geom = geometry.getGeometryN(i);
				extractGeometries(geom, features);
			}
		} else {
			createPolygons(geometry.getCoordinates(), features);
		}
	}

	private void createPolygons(Coordinate[] coordinates,
			List<Polygon> features) {
		List<Point> points = new ArrayList<>();
		double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
		double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
		Coordinate start = coordinates[0];
		Point p;

		for (int i = 0; i < coordinates.length; i++) {
			Coordinate coord = coordinates[i];
			if (start.x == coord.x && start.y == coord.y && points.size() > 0) {
				this.bounds.updateBounds(minX, minY, maxX, maxY);
				features
						.add(new Polygon(points, Geometries.rectangle(minX, minY, maxX, maxY)));
				points = new ArrayList<>();
				minX = Double.MAX_VALUE;
				minY = Double.MAX_VALUE;
				maxX = Double.NEGATIVE_INFINITY;
				maxY = Double.NEGATIVE_INFINITY;
				if (i + 1 < coordinates.length) {
					start = coordinates[i + 1];
				}
			} else {
				p = Geometries.point(coord.x, coord.y);
				minX = Math.min(minX, p.x());
				maxX = Math.max(maxX, p.x());
				minY = Math.min(minY, p.y());
				maxY = Math.max(maxY, p.y());
				points.add(p);
			}
		}
	}
}
