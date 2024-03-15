package dk.itu.raptor;

import java.io.IOException;
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
        Stream<JoinResult> res = api.join(jct.inputRaster,jct.inputVector);
        if (!jct.ranges.isEmpty()) {
            if (jct.ranges.size() != 2) {
                throw new IllegalArgumentException("More than one range is not supported");
            }
            res = res.filter(r -> jct.ranges.get(0) <= r.m && r.m <= jct.ranges.get(1));
        }
        System.out.println(res.count());

        long end = System.currentTimeMillis();
        System.out.println("joined in " + (end - start) + "ms");
    }
}   
