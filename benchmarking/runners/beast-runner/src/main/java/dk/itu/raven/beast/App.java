package dk.itu.raven.beast;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;

import com.google.gson.Gson;

import edu.ucr.cs.bdlab.beast.JavaSpatialRDDHelper;
import edu.ucr.cs.bdlab.beast.JavaSpatialSparkContext;
import edu.ucr.cs.bdlab.beast.common.BeastOptions;
import edu.ucr.cs.bdlab.beast.geolite.IFeature;
import edu.ucr.cs.bdlab.beast.geolite.ITile;
import edu.ucr.cs.bdlab.raptor.RaptorJoinFeature;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("Beast Example");
        String name = args[0];
        String vectorPath = args[1];
        String rasterPath = args[2];
        int numIterations = Integer.parseInt(args[3]);
        BenchResult benchResult = new BenchResult(name);
        // Set Spark master to local if not already set
        if (!conf.contains("spark.master"))
            conf.setMaster("local[*]");
        SparkSession sparkSession = SparkSession.builder().config(conf).getOrCreate();
        JavaSpatialSparkContext sparkContext = new JavaSpatialSparkContext(sparkSession.sparkContext());
        sparkContext.setLogLevel("ERROR");

        JavaRDD<ITile<Integer>> treecover = sparkContext.geoTiff(rasterPath);
        JavaRDD<IFeature> countries = sparkContext.shapefile(vectorPath);

        for (int i = 0; i < numIterations; i++) {
            long start = System.currentTimeMillis();

            JavaRDD<RaptorJoinFeature<Integer>> join = JavaSpatialRDDHelper.<Integer>raptorJoin(countries, treecover,
                    new BeastOptions());
            join.count();
            long end = System.currentTimeMillis();
            benchResult.addEntry(end - start);
        }
        Gson gson = new Gson();
        System.out.println(gson.toJson(benchResult));
        sparkContext.close();
    }
}
