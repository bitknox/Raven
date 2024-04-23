package dk.itu.raptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.beust.jcommander.JCommander;

import dk.itu.raptor.api.RaptorApi;
import dk.itu.raptor.io.commandline.CommandLineArgs;
import dk.itu.raptor.join.JoinResult;

public class Raptor {
    public static void main(String[] args) throws IOException {
        CommandLineArgs jct = new CommandLineArgs();
        JCommander commander = JCommander.newBuilder()
                .addObject(jct)
                .build();
        commander.parse(args);
        commander.setProgramName("Raptor");
        if (jct.help) {
            commander.usage();
            return;
        }

        RaptorApi api = new RaptorApi();

        long start = System.currentTimeMillis();
        api.join(jct.inputRaster, jct.inputVector, jct.parallel,
                new RaptorApi.JoinCallback() {

                    @Override
                    public void call(Stream<List<JoinResult>> result) {
                        if (!jct.ranges.isEmpty()) {
                            if (jct.ranges.size() != 2) {
                                throw new IllegalArgumentException("More than one range is not supported");
                            }
                            result = result.map(lst -> {
                                List<JoinResult> out = new ArrayList<>();
                                for (JoinResult r : lst) {
                                    if (jct.ranges.get(0) <= r.m && r.m <= jct.ranges.get(1)) {
                                        out.add(r);
                                    }
                                }
                                return out;
                            });
                        }
                        System.out.println(result.map(List::size).reduce(Integer::sum).orElse(-1));
                    }
                });

        long end = System.currentTimeMillis();
        System.out.println("joined in " + (end - start) + "ms");
    }
}
