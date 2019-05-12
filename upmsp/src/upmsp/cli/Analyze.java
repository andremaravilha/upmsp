package upmsp.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import upmsp.algorithm.constructive.SimpleConstructive;
import upmsp.algorithm.heuristic.Heuristic;
import upmsp.algorithm.heuristic.SA;
import upmsp.algorithm.neighborhood.*;
import upmsp.analysis.*;
import upmsp.model.Problem;
import upmsp.model.solution.Solution;

import java.io.*;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Command to perform neighborhood analysis.
 *
 * @author Andre L. Maravilha
 */
@Command(description = "Run the Simulated Annealing to get data about neighborhoods.",
        name = "analyze", mixinStandardHelpOptions = true)
public class Analyze implements Callable<Void> {

    @Option(names = {"--verbose"}, description = "Show progress.")
    private boolean verbose = false;

    @Option(names = {"--threads"}, description = "Number of threads used to perform this analysis.")
    private int threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);

    @Option(names = {"--repetitions"}, description = "Number of times this analysis should be repeated.")
    private int repetitions = 1;

    @Parameters(index = "0", description = "Path of the directory with input problem files.", arity = "1..1")
    private File input;

    @Parameters(index = "1", description = "Path of the (output) CSV file.", arity = "1..1")
    private File output;

    private PrintWriter writer;
    private long totalEntries;
    private long completedEntries;
    private MoveAnalysis[] moves;


    @Override
    public Void call() throws Exception {

        // Array of input problems
        File[] instances = input.listFiles((directory, filename) -> filename.endsWith(".txt"));

        // List of neighborhoods to analyze
        moves = new MoveAnalysis[] {
                new ShiftAnalysis() , new SimpleSwapAnalysis(), new SwapAnalysis(), new SwitchAnalysis(),
                new TaskMoveAnalysis(), new TwoShiftAnalysis()
        };

        // Number of entries to run
        totalEntries = instances.length * moves.length * repetitions;
        completedEntries = 0L;

        // Log
        if (verbose) {
            System.out.printf("Progress: %d of %d (%.2f%%)",
                    completedEntries,
                    totalEntries,
                    100.0 * (completedEntries / (double) totalEntries));
        }

        // Open output file and write its header
        output = output.getAbsoluteFile();
        Files.createDirectories(output.toPath().getParent());
        try (BufferedWriter bw = Files.newBufferedWriter(output.toPath())) {

            writer = new PrintWriter(bw);
            writer.printf("INSTANCE,N,M,SEED,TIME.LIMIT.NANO,TIME.NANO,ITERATION,INCUMBENT.MOVE,INCUMBENT.MS,INCUMBENT.SMT," +
                    "MOVE,CARDINALITY,CATEGORY.MS,CATEGORY.SMT,COUNT,MS.BEST,MS.WORST,MS.MEAN,SMT.BEST,SMT.WORST,SMT.MEAN\n");

            // Run entries
            Files.createDirectories(output.toPath().toAbsolutePath().getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(output.toPath().toAbsolutePath())) {

                ExecutorService executor = Executors.newFixedThreadPool(threads);
                for (int repetition = 1; repetition <= repetitions; ++repetition) {
                    for (File instance : instances) {
                        executor.submit(new Runner(instance, repetition));
                    }
                }

                // Wait all threads to finish
                executor.shutdown();
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            }

        }

        // Log
        if (verbose) {
            System.out.println();
        }

        return null;
    }

    private synchronized void writeEntryResult(File instance, Problem problem, Incumbent incumbent, long seed, long timeLimitNano, MoveAnalysis.Result result) {
        for (MoveAnalysis.Category category : MoveAnalysis.Category.values()) {
            MoveAnalysis.Stats stats = result.stats.get(category);
            writer.printf("%s,%d,%d,%d,%d,%d,%d,%s,%d,%d,%s,%d,%s,%s,%d,%d,%d,%.6f,%d,%d,%.6f\n",
                    instance.getName().replaceAll(".txt", ""),
                    problem.nJobs,
                    problem.nMachines,
                    seed,
                    timeLimitNano,
                    incumbent.time,
                    incumbent.iteration,
                    incumbent.move,
                    result.refMakespan,
                    result.refSumMachineTimes,
                    result.move,
                    result.cardinality,
                    category.name().toLowerCase().split("_")[0],
                    category.name().toLowerCase().split("_")[1],
                    stats.count,
                    (stats.count > 0 ? stats.makespan.best : 0),
                    (stats.count > 0 ? stats.makespan.worst : 0),
                    (stats.count > 0 ? stats.makespan.sum / (double) stats.count : 0.0),
                    (stats.count > 0 ? stats.sumMachineTimes.best : 0),
                    (stats.count > 0 ? stats.sumMachineTimes.worst : 0),
                    (stats.count > 0 ? stats.sumMachineTimes.sum / (double) stats.count : 0.0));
        }
    }

    private synchronized void onEntryFinished() {
        ++completedEntries;
        if (verbose) {
            System.out.printf("\rProgress: %d of %d (%.2f%%)",
                    completedEntries,
                    totalEntries,
                    100.0 * (completedEntries / (double) totalEntries));
        }
    }


    /**
     * Class used by runner to keep data about incumbent solutions.
     */
    private static class Incumbent {

        public Solution solution;
        public String move;
        public long iteration;
        public long time;

        public Incumbent(Solution solution, String move, long iteration, long time) {
            this.solution = solution;
            this.move = move;
            this.iteration = iteration;
            this.time = time;
        }
    }


    /**
     * Inner class used to launch an entry in which an instance is solved and moves are evaluated each time the
     * incumbent solution changes.
     */
    private class Runner implements Runnable, Heuristic.Callback {

        private File instance;
        private long seed;
        private long initialSolutionRuntime;
        private List<Incumbent> track;

        /**
         * Constructor.
         */
        public Runner(File instance, long seed) {
            this.instance = instance;
            this.seed = seed;
            this.track = new LinkedList<>();
        }

        @Override
        public void onNewIncumbent(Solution incumbent, Class<? extends Move> move, long runtimeNano, long timeLimitNano, long iteration, long iterationLimit) {
            if (move != null) {
                String moveName = move.getSimpleName()
                        .toLowerCase()
                        .replace("smart", "")
                        .replace("simple", "direct-")
                        .replace("task", "task-")
                        .replace("two", "two-");

                track.add(new Incumbent(incumbent, moveName, iteration, runtimeNano + initialSolutionRuntime));
            }
        }

        @Override
        public void onIteration(Solution incumbent, long runtimeNano, long timeLimitNano, long iteration, long iterationLimit) {
            // Do nothing
        }

        @Override
        public void run() {
            try {

                // Instantiate a random number generator
                Random random = new Random(seed);

                // Load problem data from file
                Problem problem = new Problem(instance.getAbsolutePath());

                // Instantiate the chosen heuristic
                Heuristic heuristic = new SA(problem, random, 0.96, 1.0, 1176628);

                // Add neighborhoods
                heuristic.addMove(new Shift(problem, random, 1, true));
                heuristic.addMove(new Shift(problem, random, 1, false));
                heuristic.addMove(new ShiftSmart(problem, random, 1, true));
                heuristic.addMove(new ShiftSmart(problem, random, 1, false));
                heuristic.addMove(new SimpleSwap(problem, random, 1, true));
                heuristic.addMove(new SimpleSwap(problem, random, 1, false));
                heuristic.addMove(new SimpleSwapSmart(problem, random, 1, true));
                heuristic.addMove(new SimpleSwapSmart(problem, random, 1, false));
                heuristic.addMove(new Swap(problem, random, 1, true));
                heuristic.addMove(new Swap(problem, random, 1, false));
                heuristic.addMove(new SwapSmart(problem, random, 1, true));
                heuristic.addMove(new SwapSmart(problem, random, 1, false));
                heuristic.addMove(new Switch(problem, random, 1, true));
                heuristic.addMove(new Switch(problem, random, 1, false));
                heuristic.addMove(new SwitchSmart(problem, random, 1, true));
                heuristic.addMove(new SwitchSmart(problem, random, 1, false));
                heuristic.addMove(new TaskMove(problem, random, 1, true));
                heuristic.addMove(new TaskMove(problem, random, 1, false));
                heuristic.addMove(new TaskMoveSmart(problem, random, 1, true));
                heuristic.addMove(new TaskMoveSmart(problem, random, 1, false));
                heuristic.addMove(new TwoShift(problem, random, 1, true));
                heuristic.addMove(new TwoShift(problem, random, 1, false));
                heuristic.addMove(new TwoShiftSmart(problem, random, 1, true));
                heuristic.addMove(new TwoShiftSmart(problem, random, 1, false));

                // Calculate time limit
                long timeLimit = (long) ((problem.nJobs * (problem.nMachines / 2.0) * 30) * 1000000L);

                // Create initial solution
                initialSolutionRuntime = System.nanoTime();
                Solution solution = SimpleConstructive.randomSolution(problem, random);
                initialSolutionRuntime = System.nanoTime() - initialSolutionRuntime;

                track.add(new Incumbent(solution, "", 0L, 0L));

                // Run heuristic
                heuristic.run(solution, timeLimit - initialSolutionRuntime, Long.MAX_VALUE, this, null);

                // Write data
                for (MoveAnalysis move : Analyze.this.moves) {

                    for (Incumbent incumbent : track) {
                        MoveAnalysis.Result result = move.analyze(problem, incumbent.solution);
                        writeEntryResult(instance, problem, incumbent, seed, timeLimit, result);
                    }

                    // Update progress
                    Analyze.this.onEntryFinished();
                }

            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

}
