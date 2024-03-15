package dk.itu.raven.raptor;

import java.io.IOException;

import com.beust.jcommander.JCommander;
import com.google.gson.Gson;

import dk.itu.raptor.api.RaptorApi;

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
            api.join(jct.inputRaster, jct.inputVector).count();
            long end = System.currentTimeMillis();
            long time = end - start;
            benchResult.addEntry(time);
        }

        Gson gson = new Gson();
        System.out.println(gson.toJson(benchResult));
    }
}
