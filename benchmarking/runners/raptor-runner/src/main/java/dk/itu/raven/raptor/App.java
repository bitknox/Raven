package dk.itu.raven.raptor;

import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.List;

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
            // locate tiles
            File dir = new File(jct.inputRaster);
            List<File> files = Arrays.asList(dir.listFiles());

            files.stream().map(f -> {
                try {
                    // find tiff file in the directory
                    if (!f.isDirectory()) {
                        return null;
                    }
                    File[] tiffFiles = f.listFiles((dir1, name) -> name.endsWith(".tif"));

                    if (tiffFiles.length == 0) {
                        return null;
                    }
                    return api.join(tiffFiles[0].getAbsolutePath(), jct.inputVector);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }).map(res -> {
                if (res == null) {
                    return 0l;
                }
                if (jct.filterLow == Integer.MIN_VALUE && jct.filterHigh == Integer.MAX_VALUE) {
                    return res.count();
                } else {
                    return res.filter(f -> f.m >= jct.filterLow && f.m <= jct.filterHigh).count();
                }
            }).count();

            long end = System.currentTimeMillis();
            long time = end - start;
            benchResult.addEntry(time);
        }

        Gson gson = new Gson();
        System.out.println(gson.toJson(benchResult));
    }
}
