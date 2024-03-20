package dk.itu.raven.io;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.referencing.CRS;
import org.geotools.referencing.CRS.AxisOrder;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.referencing.operation.transform.ConcatenatedTransform;
import org.geotools.referencing.operation.transform.ProjectiveTransform;

import dk.itu.raven.util.Logger;

public class Reprojector {

    private static final MathTransform reverseAxesTransform = new AffineTransform2D(0, 1, 1, 0, 0, 0);

    static public MathTransform calculateMathTransform(CoordinateReferenceSystem sourceCRS,
            CoordinateReferenceSystem targetCRS) {
        MathTransform t = null;
        try {
            t = CRS.findMathTransform(sourceCRS, targetCRS, true);
        } catch (FactoryException e) {
            Logger.log("Could not calculate math transform", Logger.LogLevel.ERROR);
            e.printStackTrace();
        }
        if (CRS.getAxisOrder(sourceCRS) == AxisOrder.NORTH_EAST)
            t = ConcatenatedTransform.create(reverseAxesTransform, t);
        if (CRS.getAxisOrder(targetCRS) == AxisOrder.NORTH_EAST)
            t = ConcatenatedTransform.create(t, reverseAxesTransform);
        return t;
    }

    static public MathTransform calculateFullTransform(CoordinateReferenceSystem source,
            CoordinateReferenceSystem target,
            TFWFormat tfwFormat) {
        MathTransform m2g = model2Grid(tfwFormat);

        MathTransform transform = Reprojector.calculateMathTransform(source, target);

        return ConcatenatedTransform.create(transform, m2g);
    }

    static public MathTransform calculateFullTransform(CoordinateReferenceSystem source,
            CoordinateReferenceSystem target) {
        return Reprojector.calculateMathTransform(source, target);
    }

    static public MathTransform model2Grid(TFWFormat tfwFormat) {
        AffineTransform affine = tfwFormat.getAffineTransform();
        try {
            affine.invert();
        } catch (NoninvertibleTransformException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ProjectiveTransform.create(affine);
    }
}
