package upmsp.cli;

import picocli.CommandLine.Command;
import java.util.concurrent.Callable;

@Command(subcommands = {
        Optimize.class
})
public class App implements Callable<Void> {

    @Override
    public Void call() throws Exception {
        return null;
    }
}
