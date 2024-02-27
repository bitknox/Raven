package dk.itu.raven.geometry;

import dk.itu.raven.io.ShapefileReader.ShapeFileBounds;

public abstract class GeometryUtil {
    public static Offset<Double> getGeometryOffset(ShapeFileBounds bounds) {
        double offsetX = bounds.minX > 0 ? -bounds.minX : 0;
        double offsetY = bounds.minY > 0 ? -bounds.minY : 0;
        return new Offset<Double>(offsetX, offsetY);
    }
}
