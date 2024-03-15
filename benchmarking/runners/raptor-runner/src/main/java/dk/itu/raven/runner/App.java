package dk.itu.raven.runner;

import com.beust.jcommander.JCommander;

import dk.itu.raptor.api.RaptorApi;
import dk.itu.raptor.join.RaptorJoin;

import org.apache.hadoop.fs.Path;

public class App {
    public static void main(String[] args) {
        CommandLineArgs jct = new CommandLineArgs();
        JCommander commander = JCommander.newBuilder()
                .addObject(jct)
                .build();
        commander.parse(args);

        RaptorApi api = new RaptorApi();

        api.join(inputRaster, inputVector);
    }
}
