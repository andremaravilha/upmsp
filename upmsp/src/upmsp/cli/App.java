package upmsp.cli;

import picocli.CommandLine.Command;
import java.util.concurrent.Callable;

/**
 * Entry point for the command line interface.
 *
 * @author Andre L. Maravilha
 */
@Command(subcommands = {
        Optimize.class,
        Analyze.class
})
public class App implements Callable<Void> {

    @Override
    public Void call() throws Exception {
        return null;
    }
}
