package upmsp.util;

import upmsp.*;
import upmsp.algorithm.neighborhood.*;
import upmsp.model.solution.*;

import java.io.*;

/**
 * Simple class with some util methods.
 *
 * @author Tulio Toffolo
 * @author Andre L. Maravilha
 */
public class Util {

    /**
     * Convert a Long number to a readable string.
     *
     * @param value long number.
     * @return the "human-friendly" (readable) number as a String.
     */
    public static String longToString(long value) {
        return value >= 1e11 ? String.format("%.0fG", value / 1e9)
          : value >= 1e7 ? String.format("%.0fM", value / 1e6)
          : (value >= 1e4) ? String.format("%.0fK", value / 1e3)
          : Long.toString(value);
    }

    /**
     * Calls printf after checking if the PrintStream is not null.
     *
     * @param output the output stream.
     * @param format the String with the format.
     * @param args   "printf" arguments.
     */
    public static void safePrintf(PrintStream output, String format, Object... args) {
        if (output != null) {
            output.printf(format, args);
        }
    }

    /**
     * Prints the statistics of a Move using the table stable, after checking that the PrintStream is not null.
     *
     * @param output  the output stream.
     * @param move    the Move considered.
     * @param nIters  number of iterations.
     */
    public static void safePrintMoveStatistics(PrintStream output, Move move, long nIters) {
        if (output != null) {
            Move.Stats stats = move.getStats();
            output.printf("| %-21s | %12s (%6.2f%%) | %7.2f%% | %7.2f%% | %7.2f%% | %7.2f%% | %7.2f%% |\n",
                    move.name,
                    longToString(stats.getCalls()),
                    100.0 *(stats.getCalls() / (double) nIters),
                    100.0 * (stats.getImprovements() / (double) stats.getCalls()),
                    100.0 * (stats.getSideways() / (double) stats.getCalls()),
                    100.0 * (stats.getWorsens() / (double) stats.getCalls()),
                    100.0 * (stats.getAccepts() / (double) stats.getCalls()),
                    100.0 * (stats.getRejects() / (double) stats.getCalls()));
        }
    }


    /**
     * Prints the current solution status after checking that the PrintStream is not null.
     *
     * @param output the output stream.
     * @param previousIncumbent previous best solution object (or null if incumbent has not changed).
     * @param currentIncumbent the best solution object.
     * @param currentSolution the current solution object.
     * @param nIters the current iteration number.
     * @param timeNano elapsed time (in nanoseconds)
     * @param special some informative String to print after the row.
     */
    public static void safePrintStatus(PrintStream output, Solution previousIncumbent, Solution currentIncumbent, Solution currentSolution, long nIters, long timeNano, String special) {
        if (output != null) {
            if (previousIncumbent != null) {
                output.printf("| %s %10s | %13d | %12.2f%% | %12.2f |\n", special, longToString(nIters), currentIncumbent.getCost(),
                        100.0 * ((previousIncumbent.getCost() - currentIncumbent.getCost()) / (double) previousIncumbent.getCost()),
                        timeNano / 1e9);
            } else {
                output.printf("| %s %10s | %13d |           --- | %12.2f |\n", special, longToString(nIters), currentIncumbent.getCost(),
                        timeNano / 1e9);
            }
        }
    }

    /**
     * Prints the text maintaining the table style, after checking that the PrintStream is not null.
     *
     * @param output  the output stream.
     * @param text    text to print inside the table.
     */
    public static void safePrintText(PrintStream output, String text) {
        if (output != null) {
            output.printf("%s\n", text);
        }
    }

}
