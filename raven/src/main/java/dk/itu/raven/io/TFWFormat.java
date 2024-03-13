package dk.itu.raven.io;

import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Point;

/**
 * The TFWFormat is used to represent the transformation from pixel to
 * coordinate (included in the geotiff format).
 */
public class TFWFormat {

	double pixelLengthX, rotationY, rotationX, pixelLengthYNegative, pixelLengthY, topLeftX, topLeftY,
			inveresePixelLengthX, inveresePixelLengthY;

	public TFWFormat(double pixelLengthX, double rotationY, double rotationX, double pixelLengthYNegative,
			double topLeftX, double topLeftY) {
		this.pixelLengthX = pixelLengthX;
		this.rotationY = rotationY;
		this.rotationX = rotationX;
		this.pixelLengthYNegative = pixelLengthYNegative;
		this.pixelLengthY = pixelLengthYNegative;
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		this.inveresePixelLengthX = 1.0 / pixelLengthX;
		this.inveresePixelLengthY = 1.0 / pixelLengthY;
	}

	public TFWFormat(AffineTransform transformation) {
		pixelLengthX = transformation.getScaleX();
		rotationY = transformation.getShearY();
		rotationX = transformation.getShearX();
		pixelLengthYNegative = transformation.getScaleY();
		pixelLengthY = -pixelLengthYNegative;
		topLeftX = transformation.getTranslateX();
		topLeftY = transformation.getTranslateY();
		inveresePixelLengthX = 1.0 / pixelLengthX;
		inveresePixelLengthY = 1.0 / pixelLengthY;
	}

	public AffineTransform getAffineTransform() {
		return new AffineTransform(pixelLengthX, rotationY, rotationX, pixelLengthY, topLeftX,
				topLeftY);
	}

	public TFWFormat() {
		this.topLeftX = Integer.MAX_VALUE;
		this.topLeftY = Integer.MAX_VALUE;
	}

	static TFWFormat read(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		double pixelLengthX = Double.parseDouble(br.readLine());
		double rotationY = Double.parseDouble(br.readLine());
		double rotationX = Double.parseDouble(br.readLine());
		double pixelLengthYNegative = Double.parseDouble(br.readLine());
		double topLeftX = Double.parseDouble(br.readLine());
		double topLeftY = Double.parseDouble(br.readLine());
		br.close();
		return new TFWFormat(pixelLengthX, rotationY, rotationX, pixelLengthYNegative, topLeftX, topLeftY);
	}

	public Point transformFromPixelToCoordinate(double x, double y) {
		double xCoordinate = topLeftX + (pixelLengthX * x);
		double yCoordinate = topLeftY + (pixelLengthY * y);
		return Geometries.point(xCoordinate, yCoordinate);
	}

	public Point transFromCoordinateToPixel(double lat, double lon) {
		double xPixel = (lat - topLeftX) * inveresePixelLengthX;
		double yPixel = (lon - topLeftY) * inveresePixelLengthY;
		return Geometries.point(xPixel, yPixel);
	}

	public void applyOther(TFWFormat other) {
		this.pixelLengthX = other.pixelLengthX;
		this.rotationY = other.rotationY;
		this.rotationX = other.rotationX;
		this.pixelLengthYNegative = other.pixelLengthYNegative;
		this.pixelLengthY = other.pixelLengthY;
		this.topLeftX = other.topLeftX;
		this.topLeftY = other.topLeftY;
		this.inveresePixelLengthX = other.inveresePixelLengthX;
		this.inveresePixelLengthY = other.inveresePixelLengthY;
	}

	@Override
	public String toString() {
		return "TFWFormat [pixelLengthX=" + pixelLengthX + ", rotationY=" + rotationY + ", rotationX=" + rotationX
				+ ", pixelLengthYNegative=" + pixelLengthYNegative + ", pixelLengthY=" + pixelLengthY + ", topLeftX="
				+ topLeftX + ", topLeftY=" + topLeftY + ", inveresePixelLengthX=" + inveresePixelLengthX
				+ ", inveresePixelLengthY=" + inveresePixelLengthY + "]";
	}
}
