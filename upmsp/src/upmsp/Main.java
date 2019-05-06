package upmsp;

import picocli.CommandLine;
import upmsp.cli.App;

import java.util.Locale;

/**
 * This class is the Main class of the program.
 *
 * @author Andre L. Maravilha
 */
public class Main {

    public static void main(String[] args) {

        // Set locale
        Locale.setDefault(new Locale("en-US"));

        // Parse arguments and run
        CommandLine cli = new CommandLine(new App());
        cli.parseWithHandler(new CommandLine.RunAll(), args);
    }

}
