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
     * @param timeLimitMillis the time limit (in milliseconds).
     * @param maxIters        the maximum number of iterations without improvements to execute.
     * @param callback        callback object.
     * @param output          output PrintStream for logging purposes.
     * @return the best solution encountered by the SA.
     */
    public Solution run(Solution initialSolution, long timeLimitMillis, long maxIters, Callback callback, PrintStream output) {

        long startTimeMillis = System.currentTimeMillis();
        long finalTimeMillis = startTimeMillis + timeLimitMillis;

        bestSolution = initialSolution;
        Solution solution = initialSolution.clone();

        // Callback for iteration zero and first incumbent
        if (callback != null) {
            callback.onNewIncumbent(bestSolution, null, 0L, timeLimitMillis, 0L, maxIters);
            callback.onIteration(bestSolution, 0L, timeLimitMillis, 0L, maxIters);
        }

        double temperature = this.t0;
        int itersInTemperature = 0;

        while (System.currentTimeMillis() < finalTimeMillis && nIters < maxIters) {
            Move move = selectMove(solution);
            int delta = move.doMove(solution);

            // if solution is improved...
            if (delta < 0) {
                acceptMove(move);

                if (solution.getCost() < bestSolution.getCost()) {
                    bestSolution = solution.clone();
                    Util.safePrintStatus(output, nIters, bestSolution, solution, System.currentTimeMillis() - startTimeMillis, "*");

                    // Callback for new incumbent solution
                    if (callback != null) {
                        callback.onNewIncumbent(bestSolution, move.getClass(), System.currentTimeMillis() - startTimeMillis, timeLimitMillis, nIters + 1, maxIters);
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
                callback.onIteration(bestSolution, System.currentTimeMillis() - startTimeMillis, timeLimitMillis, nIters, maxIters);
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
