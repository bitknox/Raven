package dk.itu.raven.io;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.geometry.MismatchedDimensionException;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.ConcatenatedTransform;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Point;

import dk.itu.raven.geometry.FeatureGeometry;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.util.Pair;

public class ShapefileReader {

	private TFWFormat transform;
	private CoordinateReferenceSystem crs;
	private File file;
	protected ShapeFileBounds bounds;

	public ShapefileReader(String path, TFWFormat transform, CoordinateReferenceSystem crs) {
		this.file = new File(path);
		this.transform = transform;
		this.crs = crs;
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
			maxX = Double.MIN_VALUE;
			maxY = Double.MIN_VALUE;
		}
	}

	public ShapeFileBounds getBounds() {
		return this.bounds;
	}

	public Pair<List<FeatureGeometry>, ShapeFileBounds> readShapefile()
			throws IOException {
		FileDataStore myData = FileDataStoreFinder.getDataStore(file);
		SimpleFeatureSource source = myData.getFeatureSource();
		bounds.reset();
		List<FeatureGeometry> features = new ArrayList<>();
		FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures();

		MathTransform w2g = calculateTransform(myData.getSchema().getCoordinateReferenceSystem());

		try (FeatureIterator<SimpleFeature> featuresItr = collection.features()) {
			while (featuresItr.hasNext()) {
				SimpleFeature feature = featuresItr.next();
				Geometry geom = (Geometry) feature.getDefaultGeometry();
				geom = JTS.transform(geom, w2g);
				extractGeometries(geom, features);
			}
		} catch (MismatchedDimensionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			myData.dispose();
		}
		return new Pair<>(features, bounds);
	}

	private void extractGeometries(Geometry geometry,
			List<FeatureGeometry> features) {
		if (geometry.getNumGeometries() > 1) {
			for (int i = 0; i < geometry.getNumGeometries(); i++) {
				Geometry geom = geometry.getGeometryN(i);
				extractGeometries(geom, features);
			}
		} else {
			features.add(new FeatureGeometry(geometry));
		}
	}

	private void createPolygons(Coordinate[] coordinates,
			List<Polygon> features) {
		List<Point> points = new ArrayList<>();
		double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
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
				maxX = Double.MIN_VALUE;
				maxY = Double.MIN_VALUE;
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

	private MathTransform calculateTransform(CoordinateReferenceSystem source) {
		MathTransform transform = null;
		AffineTransform g2w = this.transform.getAffineTransform();

		try {
			g2w.invert();
		} catch (java.awt.geom.NoninvertibleTransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (crs == null) {
			return ProjectiveTransform.create(g2w);
		}

		try {
			transform = CRS.findMathTransform(source, crs, true);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ConcatenatedTransform.create(transform, ProjectiveTransform.create(g2w));
	}
}
