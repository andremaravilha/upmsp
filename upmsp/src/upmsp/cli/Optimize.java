package upmsp.cli;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;
import upmsp.Main;
import upmsp.algorithm.constructive.SimpleConstructive;
import upmsp.algorithm.heuristic.AdaptiveSA;
import upmsp.algorithm.heuristic.Heuristic;
import upmsp.algorithm.heuristic.SA;
import upmsp.algorithm.neighborhood.*;
import upmsp.algorithm.utility.StandardUtilityModel;
import upmsp.model.Problem;
import upmsp.model.solution.Solution;
import upmsp.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;


@Command(description = "Solve an instance of the UPMS problem.",
        name = "optimize", mixinStandardHelpOptions = true)
public class Optimize implements Callable<Void> {

    @Option(names = {"--verbose"}, description = "Show optimization progress.")
    private boolean verbose = false;

    @Option(names = {"--algorithm"}, description = "sa, adaptive-sa", defaultValue = "sa")
    private String algorithm;

    @Option(names = {"--seed"}, description = "Seed for pseudo-random number generator.", defaultValue = "0")
    private long seed;

    @Option(names = {"--initial-temperature"}, description = "Initial temperature for the Simulated Annealing.", defaultValue = "1.0")
    private double initialTemperature;

    @Option(names = {"--cooling-rate"}, description = "Cooling rate", defaultValue = "0.96")
    private double coolingRate;

    @Option(names = {"--iterations-per-temperature"}, description = "Iterations before update temperature", defaultValue = "1000000")
    private int iterationsPerTemperature;

    @Option(names = {"--coefficients-path"}, description = "Path of the coefficients of the utility model.")
    private File utilityCoefficientsFile;

    @Option(names = {"--update-frequency"}, description = "Iterations before update utility values.", defaultValue = "1")
    private long updateFrequency;

    @Option(names = {"--max-probability"}, description = "Maximum probability of choosing a move.", defaultValue = "1")
    private long maxProbability;

    @Option(names = {"--time-limit"}, description = "Maximum runtime (in milliseconds). If negative, it is set according to the problem size.", defaultValue = "-1")
    private long timeLimit;

    @Option(names = {"--iterations-limit"}, description = "Maximum runtime (in milliseconds). If negative, it is set according to the problem size.")
    private long iterationsLimit = Long.MAX_VALUE;

    @Option(names = {"--disable"}, description = "shift, direct-swap, swap, switch, task-move, two-shift")
    private String[] disabledMoves = new String[0];

    @Option(names = {"--best-known"}, description = "Best known solution to calculate RDP")
    private int bestKnown = Integer.MAX_VALUE;

    @Parameters(index = "0", description = "Path of the problem input file.", arity = "1..1")
    private File input;

    @Parameters(index = "1", description = "Path of the (output) solution file.", arity = "0..1")
    private File output;

    @Override
    public Void call() throws Exception {

        // Instantiate a random number generator
        Random random = new Random(seed);

        // Load problem data from file
        Problem problem = new Problem(input.getAbsolutePath());

        // Instantiate the chosen heuristic
        Heuristic heuristic = null;
        switch (algorithm.toLowerCase()) {

            case "sa":
                heuristic = new SA(problem, random, coolingRate, initialTemperature, iterationsPerTemperature);
                break;

            case "adaptive-sa":
                try {
                    heuristic = new AdaptiveSA(problem, random, coolingRate, initialTemperature, iterationsPerTemperature,
                            new StandardUtilityModel(utilityCoefficientsFile.toPath().toAbsolutePath()), updateFrequency, maxProbability);
                } catch (IOException e) {
                    System.err.println("ERROR: Could not read file of coefficients of the utility model.");
                    System.exit(-1);
                    return null;
                }
                break;

            default:
                System.err.println("ERROR: Invalid algorithm.");
                System.exit(-1);
                return null;
        }

        // Add moves (neighborhoods)
        List<String> enabledMoves = new ArrayList<>(Arrays.asList("shift", "direct-swap", "swap", "switch", "task-move", "two-shift"));
        enabledMoves.removeAll(Arrays.asList(disabledMoves));

        for (String move : enabledMoves) {
            switch (move) {

                case "shift":
                    heuristic.addMove(new Shift(problem, random, 1, true));
                    heuristic.addMove(new Shift(problem, random, 1, false));
                    heuristic.addMove(new ShiftSmart(problem, random, 1, true));
                    heuristic.addMove(new ShiftSmart(problem, random, 1, false));
                    break;

                case "direct-swap":
                    heuristic.addMove(new SimpleSwap(problem, random, 1, true));
                    heuristic.addMove(new SimpleSwap(problem, random, 1, false));
                    heuristic.addMove(new SimpleSwapSmart(problem, random, 1, true));
                    heuristic.addMove(new SimpleSwapSmart(problem, random, 1, false));
                    break;

                case "swap":
                    heuristic.addMove(new Swap(problem, random, 1, true));
                    heuristic.addMove(new Swap(problem, random, 1, false));
                    heuristic.addMove(new SwapSmart(problem, random, 1, true));
                    heuristic.addMove(new SwapSmart(problem, random, 1, false));
                    break;

                case "switch":
                    heuristic.addMove(new Switch(problem, random, 1, true));
                    heuristic.addMove(new Switch(problem, random, 1, false));
                    heuristic.addMove(new SwitchSmart(problem, random, 1, true));
                    heuristic.addMove(new SwitchSmart(problem, random, 1, false));
                    break;

                case "task-move":
                    heuristic.addMove(new TaskMove(problem, random, 1, true));
                    heuristic.addMove(new TaskMove(problem, random, 1, false));
                    heuristic.addMove(new TaskMoveSmart(problem, random, 1, true));
                    heuristic.addMove(new TaskMoveSmart(problem, random, 1, false));
                    break;

                case "two-shift":
                    heuristic.addMove(new TwoShift(problem, random, 1, true));
                    heuristic.addMove(new TwoShift(problem, random, 1, false));
                    heuristic.addMove(new TwoShiftSmart(problem, random, 1, true));
                    heuristic.addMove(new TwoShiftSmart(problem, random, 1, false));
                    break;
            }
        }

        // Time limit
        if (timeLimit < 0L) {
            timeLimit = (long) (problem.nJobs * (problem.nMachines / 2.0) * 30);
        }

        if (verbose) {
            System.out.printf("Instance....: %s\n", input.getName());
            System.out.printf("Algorithm...: %s\n", heuristic);
            System.out.printf("Other params: maxIters=%s, seed=%d, timeLimit=%.2fs\n\n", Util.longToString(iterationsLimit), seed, timeLimit / 1000.0);
            System.out.printf("    /--------------------------------------------------------\\\n");
            System.out.printf("    | %8s | %8s | %8s | %8s | %10s | %s\n", "Iter", "RDP(%)", "S*", "S'", "Time", "");
            System.out.printf("    |----------|----------|----------|----------|------------|\n");
        }

        // Set value of objective function of the best known solution (if known)
        Main.bestKnown = bestKnown;

        // Set start time
        Main.startTimeMillis = System.currentTimeMillis();

        // Create initial solution
        Solution solution = SimpleConstructive.randomSolution(problem, random);

        // Run heuristic
        if (heuristic.getMoves().size() > 0) {
            heuristic.run(solution, timeLimit, iterationsLimit, (verbose ? System.out : null));
        }

        // Check feasibility
        boolean feasibility = solution.validate((verbose ? System.out : null));

        if (verbose) {
            System.out.printf("    \\--------------------------------------------------------/\n\n");

            System.out.printf("Neighborhoods statistics (values in %%):\n\n");
            System.out.printf("    /----------------------------------------------------------------\\\n");
            System.out.printf("    | %-18s | %8s | %8s | %8s | %8s |\n", "Move", "Improvs.", "Sideways", "Accepts", "Rejects");
            System.out.printf("    |--------------------|----------|----------|----------|----------|\n");

            for (Move move : heuristic.getMoves()) {
                Util.safePrintMoveStatistics(System.out, move, "");
            }
            System.out.printf("    \\----------------------------------------------------------------/\n\n");

            if (Main.bestKnown != Integer.MAX_VALUE) {
                System.out.printf("Best RDP..........: %.4f%%\n", 100 * (double) (solution.getCost() - Main.bestKnown) / (double) Main.bestKnown);
            }
            System.out.printf("Best makespan.....: %d\n", solution.getCost());
            System.out.printf("N. of Iterations..: %d\n", heuristic.getNIters());
            System.out.printf("Total runtime.....: %.2fs\n", (System.currentTimeMillis() - Main.startTimeMillis) / 1000.0);
        }

        // Save best solution found
        if (output != null) {
            solution.write(Paths.get(output.getAbsolutePath()));
        }

        return null;
    }
}
