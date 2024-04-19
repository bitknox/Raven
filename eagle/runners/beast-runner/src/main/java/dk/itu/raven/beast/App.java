package dk.itu.raven.beast;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;

import com.beust.jcommander.JCommander;
import com.google.gson.Gson;

import edu.ucr.cs.bdlab.beast.JavaSpatialRDDHelper;
import edu.ucr.cs.bdlab.beast.JavaSpatialSparkContext;
import edu.ucr.cs.bdlab.beast.common.BeastOptions;
import edu.ucr.cs.bdlab.beast.geolite.IFeature;
import edu.ucr.cs.bdlab.beast.geolite.ITile;
import edu.ucr.cs.bdlab.raptor.RaptorJoinFeature;

/**
 * 
 *
 */
public class App {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("Beast Example");
        CommandLineArgs jct = new CommandLineArgs();
        JCommander commander = JCommander.newBuilder()
                .addObject(jct)
                .build();
        commander.parse(args);

        BenchResult benchResult = new BenchResult("Beast Join");
        benchResult.addLabel("Vector: " + benchResult.formatPath(jct.inputVector));
        benchResult.addLabel("Raster: " + benchResult.formatPath(jct.inputRaster));
        // Set Spark master to local if not already set
        if (!conf.contains("spark.master"))
            conf.setMaster("local[*]");
        SparkSession sparkSession = SparkSession.builder().config(conf).getOrCreate();
        JavaSpatialSparkContext sparkContext = new JavaSpatialSparkContext(sparkSession.sparkContext());
        sparkContext.setLogLevel("ERROR");

        JavaRDD<ITile<Integer>> treecover = sparkContext.geoTiff(jct.inputRaster);
        JavaRDD<IFeature> countries = sparkContext.shapefile(jct.inputVector);

        for (int i = 0; i < jct.iterations; i++) {
            long start = System.currentTimeMillis();
            JavaRDD<RaptorJoinFeature<Integer>> join = JavaSpatialRDDHelper.<Integer>raptorJoin(countries, treecover,
                    new BeastOptions());
            if (jct.filterLow == Integer.MIN_VALUE && jct.filterHigh == Integer.MAX_VALUE) {
                join.count();
            } else {
                join.filter(f -> f.m() >= jct.filterLow && f.m() <= jct.filterHigh).count();
            }
            long end = System.currentTimeMillis();
            benchResult.addEntry(end - start);
        }
        Gson gson = new Gson();
        System.out.println(gson.toJson(benchResult));
        sparkContext.close();
    }
}
