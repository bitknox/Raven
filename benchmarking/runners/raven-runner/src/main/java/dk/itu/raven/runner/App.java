package dk.itu.raven.runner;

import org.geotools.data.shapefile.shp.ShapefileReader;

import dk.itu.raven.join.RavenJoin;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {

        String vectorPath = args[0];
        String rasterPath = args[1];
        int numIterations = Integer.parseInt(args[2]);

        for (int i = 0; i < numIterations; i++) {

        }

    }

}
