package dk.itu.raven.raptor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.beust.jcommander.JCommander;
import com.google.gson.Gson;

import dk.itu.raptor.api.RaptorApi;
import dk.itu.raptor.join.JoinResult;

public class App {
    public static void main(String[] args) throws IOException {
        CommandLineArgs jct = new CommandLineArgs();
        JCommander commander = JCommander.newBuilder()
                .addObject(jct)
                .build();
        commander.parse(args);

        BenchResult benchResult = new BenchResult("Local Raptor Benchmark");
        benchResult.addLabel("Vector: " + benchResult.formatPath(jct.inputVector));
        benchResult.addLabel("Raster: " + benchResult.formatPath(jct.inputRaster));

        RaptorApi api = new RaptorApi();
        for (int i = 0; i < jct.iterations; i++) {
            long start = System.currentTimeMillis();

            Stream<JoinResult> res = api.join(jct.inputRaster, jct.inputVector);
            if (jct.filterLow == Integer.MIN_VALUE && jct.filterHigh == Integer.MAX_VALUE) {
                res.count();
            } else {
                res.filter(f -> f.m >= jct.filterLow && f.m <= jct.filterHigh).count();
            }
            long end = System.currentTimeMillis();
            long time = end - start;
            benchResult.addEntry(time);
        }

        Gson gson = new Gson();
        System.out.println(gson.toJson(benchResult));
    }
}
