package upmsp;

import upmsp.algorithm.constructive.*;
import upmsp.algorithm.heuristic.*;
import upmsp.algorithm.neighborhood.*;
import upmsp.model.*;
import upmsp.model.solution.*;
import upmsp.util.*;

import java.io.*;
import java.util.*;

/**
 * This class is the Main class of the program, responsible of parsing the input, instantiating moves and heuristics and
 * printing the results.
 *
 * @author Tulio Toffolo
 */
public class Main {

    // region solver parameters and default values

    public static long startTimeMillis = System.currentTimeMillis();

    public static String algorithm = "sa";
    public static String inFile;
    public static String outFile = null;

    public static long seed = 0;
    public static long maxIters = ( long ) 1e8;
    public static long timeLimit = 70 * 1000;

    public static int bestKnown = Integer.MAX_VALUE;

    // SA (Simulated Annealing)
    public static double alpha = 0.99;
    public static int saMax = ( int ) 1e7;
    public static double t0 = 1;

    // Neighborhoods
    public static boolean neighborhoods[];

    static {
        neighborhoods = new boolean[6 * 4];
        for (int i = 0; i < neighborhoods.length; i++)
            neighborhoods[i] = true;
    }

    // endregion solver parameters and default values

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     * @throws IOException if any IO error occurs.
     */
    public static void main(String[] args) throws IOException {
        Locale.setDefault(new Locale("en-US"));
        readArgs(args);

        Problem problem = new Problem(inFile);
        Random random = new Random(seed);

        Heuristic solver;
        switch (algorithm) {
            case "sa":
                solver = new SA(problem, random, alpha, t0, saMax);
                break;
            default:
                System.exit(-1);
                return;
        }

        // adding moves (neighborhoods)
        createNeighborhoods(problem, random, solver);

        System.out.printf("Instance....: %s\n", inFile);
        System.out.printf("Algorithm...: %s\n", solver);
        System.out.printf("Other params: maxIters=%s, seed=%d, timeLimit=%.2fs\n\n", Util.longToString(maxIters), seed, timeLimit / 1000.0);
        System.out.printf("    /--------------------------------------------------------\\\n");
        System.out.printf("    | %8s | %8s | %8s | %8s | %10s | %s\n", "Iter", "RDP(%)", "S*", "S'", "Time", "");
        System.out.printf("    |----------|----------|----------|----------|------------|\n");

        // re-starting time counting (after reading files)
        startTimeMillis = System.currentTimeMillis();

        // generating initial solution
        Solution solution = SimpleConstructive.randomSolution(problem, random);
        Util.safePrintStatus(System.out, 0, solution, solution, "s0");
        assert solution.validate(System.err);

        // running stochastic local search
        if (solver.getMoves().size() > 0)
            solution = solver.run(solution, timeLimit, maxIters, System.out);
        solution.validate(System.err);

        System.out.printf("    \\--------------------------------------------------------/\n\n");

        System.out.printf("Neighborhoods statistics (values in %%):\n\n");
        System.out.printf("    /----------------------------------------------------------------\\\n");
        System.out.printf("    | %-18s | %8s | %8s | %8s | %8s |\n", "Move", "Improvs.", "Sideways", "Accepts", "Rejects");
        System.out.printf("    |--------------------|----------|----------|----------|----------|\n");
        for (Move move : solver.getMoves())
            Util.safePrintMoveStatistics(System.out, move, "");
        System.out.printf("    \\----------------------------------------------------------------/\n\n");

        if (bestKnown != Integer.MAX_VALUE)
            System.out.printf("Best RDP..........: %.4f%%\n", 100 * ( double ) (solution.getCost() - bestKnown) / ( double ) bestKnown);
        System.out.printf("Best makespan.....: %d\n", solution.getCost());
        System.out.printf("N. of Iterations..: %d\n", solver.getNIters());
        System.out.printf("Total runtime.....: %.2fs\n", (System.currentTimeMillis() - startTimeMillis) / 1000.0);

        solution.write(outFile);
    }

    private static void createNeighborhoods(Problem problem, Random random, Heuristic solver) {
        int index = -1;

        if (neighborhoods[++index]) solver.addMove(new Shift(problem, random, 1, true));
        if (neighborhoods[++index]) solver.addMove(new Shift(problem, random, 1, false));
        if (neighborhoods[++index]) solver.addMove(new ShiftSmart(problem, random, 1, true));
        if (neighborhoods[++index]) solver.addMove(new ShiftSmart(problem, random, 1, false));

        if (neighborhoods[++index]) solver.addMove(new SimpleSwap(problem, random, 1, true));
        if (neighborhoods[++index]) solver.addMove(new SimpleSwap(problem, random, 1, false));
        if (neighborhoods[++index]) solver.addMove(new SimpleSwapSmart(problem, random, 1, true));
        if (neighborhoods[++index]) solver.addMove(new SimpleSwapSmart(problem, random, 1, false));

        if (neighborhoods[++index]) solver.addMove(new Swap(problem, random, 1, true));
        if (neighborhoods[++index]) solver.addMove(new Swap(problem, random, 1, false));
        if (neighborhoods[++index]) solver.addMove(new SwapSmart(problem, random, 1, true));
        if (neighborhoods[++index]) solver.addMove(new SwapSmart(problem, random, 1, false));

        if (neighborhoods[++index]) solver.addMove(new Switch(problem, random, 1, true));
        if (neighborhoods[++index]) solver.addMove(new Switch(problem, random, 1, false));
        if (neighborhoods[++index]) solver.addMove(new SwitchSmart(problem, random, 1, true));
        if (neighborhoods[++index]) solver.addMove(new SwitchSmart(problem, random, 1, false));

        if (neighborhoods[++index]) solver.addMove(new TaskMove(problem, random, 1, true));
        if (neighborhoods[++index]) solver.addMove(new TaskMove(problem, random, 1, false));
        if (neighborhoods[++index]) solver.addMove(new TaskMoveSmart(problem, random, 1, true));
        if (neighborhoods[++index]) solver.addMove(new TaskMoveSmart(problem, random, 1, false));

        if (neighborhoods[++index]) solver.addMove(new TwoShift(problem, random, 1, true));
        if (neighborhoods[++index]) solver.addMove(new TwoShift(problem, random, 1, false));
        if (neighborhoods[++index]) solver.addMove(new TwoShiftSmart(problem, random, 1, true));
        if (neighborhoods[++index]) solver.addMove(new TwoShiftSmart(problem, random, 1, false));

    }

    /**
     * Prints the program usage.
     */
    public static void printUsage() {
        System.out.println("Usage: java -jar upmsp.jar <input> <output> [options]");
        System.out.println("    <input>  : Path of the problem input file.");
        System.out.println("    <output> : Path of the (output) solution file.");
        System.out.println();
        System.out.println("Options:");
        System.out.println("    -algorithm <algorithm> : sa (default: " + algorithm + ").");
        System.out.println("    -bestKnown <makespan>  : best known makespan for RDP output (default: " + bestKnown + ").");
        System.out.println("    -seed <seed>           : random seed (default: " + seed + ").");
        System.out.println("    -maxIters <maxIters>   : maximum number of consecutive rejections (default: Long.MAXVALUE).");
        System.out.println("    -time <timeLimit>      : time limit in seconds (default: " + timeLimit + ").");
        System.out.println();
        System.out.println("    SA parameters:");
        System.out.println("        -alpha <alpha> : cooling rate for the Simulated Annealing (default: " + alpha + ").");
        System.out.println("        -samax <samax> : iterations before updating the temperature for Simulated Annealing (default: " + saMax + ").");
        System.out.println("        -t0 <t0>       : initial temperature for the Simulated Annealing (default: " + t0 + ").");
        System.out.println();
        System.out.println("    Neighborhoods selection:");
        System.out.println("        -n <id,policy,value> : disables a policy(0..3) for neighborhood id(0..5) if value = 0 and enables it otherwise.");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("    java -jar upmsp.jar instance.txt solution.txt");
        System.out.println("    java -jar upmsp.jar instance.txt solution.txt -algorithm sa -alpha 0.98 -samax 1000 -t0 100000");
        System.out.println();
    }

    /**
     * Reads the input arguments.
     *
     * @param args the input arguments
     */
    public static void readArgs(String args[]) {
        if (args.length < 2) {
            printUsage();
            System.exit(-1);
        }

        int index = -1;

        inFile = args[++index];
        outFile = args[++index];

        while (index < args.length - 1) {
            String option = args[++index].toLowerCase();

            switch (option) {
                case "-algorithm":
                    algorithm = args[++index].toLowerCase();
                    break;
                case "-seed":
                    seed = Integer.parseInt(args[++index]);
                    break;
                case "-maxiters":
                    maxIters = Long.parseLong(args[++index]);
                    break;
                case "-time":
                    timeLimit = Math.round(Double.parseDouble(args[++index]) * 1000.0);
                    break;

                case "-bestknown":
                    bestKnown = Integer.parseInt(args[++index]);
                    break;

                // SA
                case "-alpha":
                    alpha = Double.parseDouble(args[++index]);
                    break;
                case "-samax":
                    saMax = Integer.parseInt(args[++index]);
                    break;
                case "-t0":
                    t0 = Double.parseDouble(args[++index]);
                    break;

                // Neighborhoods selection
                case "-n":
                    String[] values = args[++index].split(",");
                    int i = Integer.parseInt(values[0]) * 4 + Integer.parseInt(values[1]);
                    neighborhoods[i] = values[2].equals("1");
                    break;

                default:
                    printUsage();
                    System.exit(-1);
            }
        }
    }
}
