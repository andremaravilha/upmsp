package upmsp.algorithm.heuristic;

import org.apache.commons.math3.util.*;
import upmsp.algorithm.neighborhood.*;
import upmsp.model.*;
import upmsp.model.solution.*;
import upmsp.util.*;

import java.io.*;
import java.util.*;

/**
 * This class is a Simulated Annealing implementation.
 *
 * @author Tulio Toffolo
 */
public class SA extends Heuristic {

    /**
     * SA parameters.
     */
    private double alpha;
    private double t0;
    private int saMax;

    private final static double EPS = 1e-6;

    /**
     * Instantiates a new SA.
     *
     * @param problem problem reference
     * @param random  random number generator.
     * @param alpha   cooling rate for the simulated annealing
     * @param t0      initial temperature, T0
     * @param saMax   number of iterations before update the temperature
     */
    public SA(Problem problem, Random random, double alpha, double t0, int saMax) {
        super(problem, random, "SA");

        // initializing simulated annealing parameters
        this.alpha = alpha;
        this.t0 = t0;
        this.saMax = saMax;
    }

    /**
     * Executes the Simulated Annealing.
     *
     * @param initialSolution the initial (input) solution.
     * @param timeLimitNano   the time limit (in nanoseconds).
     * @param maxIters        the maximum number of iterations without improvements to execute.
     * @param callback        callback object.
     * @param output          output PrintStream for logging purposes.
     * @return the best solution encountered by the SA.
     */
    public Solution run(Solution initialSolution, long timeLimitNano, long maxIters, Callback callback, PrintStream output) {

        long startTimeNano = System.nanoTime();
        long finalTimeNano = startTimeNano + timeLimitNano;

        Solution previousBestSolution = null;
        bestSolution = initialSolution;
        Solution solution = initialSolution.clone();

        // Callback for iteration zero and first incumbent
        if (callback != null) {
            callback.onNewIncumbent(bestSolution, null, 0L, timeLimitNano, 0L, maxIters);
            callback.onIteration(bestSolution, 0L, timeLimitNano, 0L, maxIters);
        }

        double temperature = this.t0;
        int itersInTemperature = 0;

        while (System.nanoTime() < finalTimeNano && nIters < maxIters) {

            // Select a move and a strategy
            Move move = null;
            boolean useIntensificationPolicy = false;
            boolean useMakespanMachine = false;

            do {
                useIntensificationPolicy = random.nextBoolean();
                useMakespanMachine = random.nextBoolean();
                move = selectMove();
            } while (!move.hasMove(solution, useIntensificationPolicy, useMakespanMachine));

            // Do move
            int delta = move.doMove(solution, useIntensificationPolicy, useMakespanMachine);

            // if solution is improved...
            if (delta < 0) {
                acceptMove(move);

                if (solution.getCost() < bestSolution.getCost()) {
                    previousBestSolution = bestSolution;
                    bestSolution = solution.clone();
                    Util.safePrintStatus(output, previousBestSolution, bestSolution, solution, nIters, System.nanoTime() - startTimeNano, "*");

                    // Callback for new incumbent solution
                    if (callback != null) {
                        callback.onNewIncumbent(bestSolution, move.getClass(), System.nanoTime() - startTimeNano, timeLimitNano, nIters + 1, maxIters);
                    }

                }
            }

            // if solution is not improved, but is accepted...
            else if (delta == 0) {
                acceptMove(move);
            }

            // solution is not improved, but may be accepted with a probability...
            else {
                double x = random.nextDouble();
                if (x < 1 / FastMath.exp(delta / temperature)) {
                    acceptMove(move);
                }

                // if solution is rejected..
                else {
                    rejectMove(move);
                }
            }

            // if necessary, updates temperature
            if (++itersInTemperature >= saMax) {
                itersInTemperature = 0;
                temperature = alpha * temperature;
                if (temperature < EPS) {
                    temperature = t0;
                    Util.safePrintText(output, "Re-heating Simulated Annealing");
                }
            }

            nIters++;

            // Callback for iteration
            if (callback != null) {
                callback.onIteration(bestSolution, System.nanoTime() - startTimeNano, timeLimitNano, nIters, maxIters);
            }
        }

        return bestSolution;
    }

    /**
     * Returns the string representation of this heuristic.
     *
     * @return the string representation of this heuristic (with parameters values).
     */
    public String toString() {
        return String.format("Simulated Annealing (cooling-rate=%.3f, iterations-per-temp=%s, initial-temp=%s)",
          alpha, Util.longToString(saMax), Util.longToString(( long ) t0));
    }

}
