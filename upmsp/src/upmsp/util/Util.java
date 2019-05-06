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
     * @param special some informative String to print after the row.
     */
    public static void safePrintMoveStatistics(PrintStream output, Move move, String special) {
        if (output != null) {
            output.printf("| %-21s | %12s | %7.2f%% | %7.2f%% | %7.2f%% | %7.2f%% |\n",
                    move.name,
                    longToString(move.getNIters()),
                    100.0 * (move.getNImprovements() / (double) move.getNIters()),
                    100.0 * (move.getNSideways() / (double) move.getNIters()),
                    100.0 * (move.getNAccepts() / (double) move.getNIters()),
                    100.0 * (move.getNRejects() / (double) move.getNIters()));
        }
    }


    /**
     * Prints the current solution status after checking that the PrintStream is not null.
     *
     * @param output       the output stream.
     * @param nIters       the current iteration number.
     * @param bestSolution the best solution object.
     * @param solution     the current solution object.
     * @param special      some informative String to print after the row.
     */
    public static void safePrintStatus(PrintStream output, long nIters, Solution bestSolution, Solution solution, long timeMillis, String special) {
        if (output != null) {
            output.printf("| %s %10s | %13d | %13d | %12.2f |\n",
                    special,
                    longToString(nIters),
                    bestSolution.getCost(),
                    solution.getCost(),
                    timeMillis / 1000.0);
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
