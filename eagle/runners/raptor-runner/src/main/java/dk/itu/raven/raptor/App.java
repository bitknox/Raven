package dk.itu.raven.raptor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
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
        commander.setProgramName("Raptor Runner");
        if (jct.help) {
            commander.usage();
            return;
        }

        BenchResult benchResult = new BenchResult("Local Raptor Benchmark");
        benchResult.addLabel("Vector: " + benchResult.formatPath(jct.inputVector));
        benchResult.addLabel("Raster: " + benchResult.formatPath(jct.inputRaster));

        RaptorApi api = new RaptorApi();
        for (int i = 0; i < jct.iterations; i++) {
            System.err.println("Running iteration " + (i+1) + " of " + jct.iterations + " iterations.");
            long start = System.currentTimeMillis();

            api.join(jct.inputRaster, jct.inputVector, jct.parallel, new RaptorApi.JoinCallback() {

                @Override
                public void call(Stream<List<JoinResult>> result) {
                    if (!(jct.filterLow == Integer.MIN_VALUE && jct.filterHigh == Integer.MAX_VALUE)) {
                        result = result.map(lst -> {
                            List<JoinResult> out = new ArrayList<>();
                            for (JoinResult r : lst) {
                                if (jct.filterLow <= r.m && r.m <= jct.filterHigh) {
                                    out.add(r);
                                }
                            }
                            return out;
                        });
                    }
                    result.map(List::size).reduce(Integer::sum).orElse(-1);
                }
            });

            long end = System.currentTimeMillis();
            long time = end - start;
            System.err.println("    Iteration " + (i+1) + " took " + time + "ms.");
            benchResult.addEntry(time);
        }

        Gson gson = new Gson();
        System.out.println(gson.toJson(benchResult));
    }
}
