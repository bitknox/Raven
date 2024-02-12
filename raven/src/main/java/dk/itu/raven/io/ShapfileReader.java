package dk.itu.raven.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.data.Query;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.Property;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.GeometryAttributeImpl;
import org.locationtech.jts.geom.Coordinate;

import com.github.davidmoten.rtree2.geometry.Point;
import com.github.davidmoten.rtree2.geometry.Geometries;

import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.util.Pair;

public class ShapfileReader {

	private TFWFormat transform;
	private ShapeFileBounds bounds;

	public ShapfileReader(TFWFormat transform) {
		this.transform = transform;
		this.bounds = new ShapeFileBounds();
	}

	public class ShapeFileBounds {
		public double minx, miny, maxx, maxy;

		public ShapeFileBounds() {
			this.reset();
		}

		public void updateBounds(double x1, double y1, double x2, double y2) {
			minx = Math.min(minx, x1);
			miny = Math.min(miny, y1);
			maxx = Math.max(maxx, x2);
			maxy = Math.max(maxy, y2);
		}

		public void reset() {
			minx = Double.MAX_VALUE;
			miny = Double.MAX_VALUE;
			maxx = Double.MIN_VALUE;
			maxy = Double.MIN_VALUE;
		}
	}

	public Pair<Iterable<Polygon>, ShapeFileBounds> readShapefile(String path) throws IOException {
		File file = new File(path);
		FileDataStore myData = FileDataStoreFinder.getDataStore(file);
		SimpleFeatureSource source = myData.getFeatureSource();
		bounds.reset();
		List<Polygon> features = new ArrayList<>();
		FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures();
		try (FeatureIterator<SimpleFeature> featuresItr = collection.features()) {
			while (featuresItr.hasNext()) {
				SimpleFeature feature = featuresItr.next();
				extractGeometries(((GeometryAttributeImpl) feature.getProperty("the_geom")).getValue(), features);
			}
			return new Pair<>(features, bounds);
		}
	}

	private void extractGeometries(Geometry geometry, List<Polygon> features) {
		if (geometry.getNumGeometries() > 1) {
			for (int i = 0; i < geometry.getNumGeometries(); i++) {
				Geometry geom = geometry.getGeometryN(i);
				extractGeometries(geom, features);
			}
		} else {
			createPolygons(geometry.getCoordinates(), features);
		}
	}

	private void createPolygons(Coordinate[] coordinates, List<Polygon> features) {
		List<Point> points = new ArrayList<>();
		double minx = Double.MAX_VALUE, miny = Double.MAX_VALUE;
		double maxx = Double.MIN_VALUE, maxy = Double.MIN_VALUE;
		Coordinate start = coordinates[0];
		Point p;
		for (int i = 0; i < coordinates.length; i++) {
			Coordinate coord = coordinates[i];
			if (start.x == coord.x && start.y == coord.y && points.size() > 0) {
				this.bounds.updateBounds(minx, miny, maxx, maxy);
				features.add(new Polygon(points, Geometries.rectangle(minx, miny, maxx, maxy)));
				points = new ArrayList<>();
				minx = Double.MAX_VALUE;
				miny = Double.MAX_VALUE;
				maxx = Double.MIN_VALUE;
				maxy = Double.MIN_VALUE;
				if (i + 1 < coordinates.length) {
					start = coordinates[i + 1];
				}
			} else {
				p = transform.transFromCoordinateToPixel(coord.x, coord.y);
				minx = Math.min(minx, p.x());
				maxx = Math.max(maxx, p.x());
				miny = Math.min(miny, p.y());
				maxy = Math.max(maxy, p.y());
				points.add(p);
			}
		}
	}
}
