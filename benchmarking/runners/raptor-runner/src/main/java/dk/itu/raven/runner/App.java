package dk.itu.raven.runner;

import com.beust.jcommander.JCommander;

import dk.itu.raptor.join.RaptorJoin;

public class App {
    public static void main(String[] args) {
        CommandLineArgs jct = new CommandLineArgs();
        JCommander commander = JCommander.newBuilder()
                .addObject(jct)
                .build();
        commander.parse(args);

        RaptorJoin join = new RaptorJoin();
    }
}
