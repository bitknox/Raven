package dk.itu.raven.geometry;

import java.io.IOException;

import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.io.ShapefileReader;
import dk.itu.raven.io.ShapefileReader.ShapeFileBounds;

public abstract class GeometryUtil {
    public static Offset<Double> getGeometryOffset(ShapeFileBounds bounds) {
        double offsetX = bounds.minX > 0 ? -bounds.minX : 0;
        double offsetY = bounds.minY > 0 ? -bounds.minY : 0;
        return new Offset<Double>(offsetX, offsetY);
    }

    public static java.awt.Rectangle getWindowRectangle(Size imageSize,
            ShapefileReader.ShapeFileBounds bounds) throws IOException {
        Rectangle rect = Geometries.rectangle(bounds.minX, bounds.minY, bounds.maxX, bounds.maxY);
        int startX = (int) Math.max(rect.x1(), 0);
        int startY = (int) Math.max(rect.y1(), 0);
        int endX = (int) Math.ceil(Math.min(imageSize.width, rect.x2()));
        int endY = (int) Math.ceil(Math.min(imageSize.height, rect.y2()));
        return new java.awt.Rectangle(startX, startY, endX - startX, endY - startY);
    }
}
