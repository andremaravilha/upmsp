package upmsp.algorithm.heuristic;

import org.apache.commons.math3.util.FastMath;
import upmsp.algorithm.neighborhood.Move;
import upmsp.algorithm.utility.UtilityModel;
import upmsp.model.Problem;
import upmsp.model.solution.Solution;
import upmsp.util.Util;

import java.io.PrintStream;
import java.util.Random;

/**
 * This class is an Adaptive Simulated Annealing implementation which uses a utility model to define probabilities to
 * choose a neighborhood at each iteration. This utility model considers the moment (runtime) and problem size.
 *
 * @author Andre L. Maravilha
 */
public class AdaptiveSA extends Heuristic {

    /**
     * Adaptive SA parameters.
     */
    private double alpha;
    private double t0;
    private int saMax;

    private UtilityModel utility;
    private long updateFrequency;
    private double maxProbability;
    private double[] probabilities;

    private final static double EPS = 1e-6;

    /**
     * Instantiates a new Adaptive SA.
     *
     * @param problem problem reference
     * @param random  random number generator.
     * @param alpha   cooling rate for the simulated annealing
     * @param t0      initial temperature, T0
     * @param saMax   number of iterations before update the temperature
     * @param utility prediction model for moves' utility
     * @param freq    number of iterations before update utility values (and probability) of moves
     * @param maxProb maximum probability assigned to a move.
     */
    public AdaptiveSA(Problem problem, Random random, double alpha, double t0, int saMax, UtilityModel utility, long freq, double maxProb) {
        super(problem, random, "Adaptive-SA");

        // initializing simulated annealing parameters
        this.alpha = alpha;
        this.t0 = t0;
        this.saMax = saMax;

        // initialize the utility
        this.utility = utility;
        this.updateFrequency = freq;
        this.maxProbability = maxProb / 4.0; // Each move is, in fact, four moves
        this.probabilities = null;
    }

    /**
     * Executes the Adaptive Simulated Annealing.
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

        bestSolution = initialSolution;
        Solution solution = initialSolution.clone();

        // Callback for iteration zero and first incumbent
        if (callback != null) {
            callback.onNewIncumbent(bestSolution, null, 0L, timeLimitNano, 0L, maxIters);
            callback.onIteration(bestSolution, 0L, timeLimitNano, 0L, maxIters);
        }

        // Initialize probabilities assinged to each move
        this.probabilities = new double[moves.size()];
        updateProbabilities((System.nanoTime() - startTimeNano) / (double) timeLimitNano);

        double temperature = this.t0;
        int itersInTemperature = 0;
        long itersInUtility = 0L;

        while (System.nanoTime() < finalTimeNano && nIters < maxIters) {

            //Move move = selectMove(solution);
            Move move = selectMove(solution);
            int delta = move.doMove(solution);

            // if solution is improved...
            if (delta < 0) {
                acceptMove(move);

                if (solution.getCost() < bestSolution.getCost()) {
                    bestSolution = solution.clone();
                    Util.safePrintStatus(output, nIters, bestSolution, solution, System.nanoTime() - startTimeNano, "*");

                    // Callback for new incumbent solution
                    if (callback != null) {
                        callback.onNewIncumbent(bestSolution, move.getClass(),System.nanoTime() - startTimeNano, timeLimitNano, nIters + 1, maxIters);
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

            // Update iteration counter
            nIters++;

            // if necessary, update utility values
            if (++itersInUtility >= updateFrequency) {
                itersInUtility = 0L;
                updateProbabilities((System.nanoTime() - startTimeNano) / (double) timeLimitNano);
            }

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
        return String.format("Adaptive Simulated Annealing (cooling-rate=%.3f, iterations-per-temp=%s, initial-temp=%s, max-prob=%.2f, update-freq=%s)",
          alpha, Util.longToString(saMax), Util.longToString((long) t0), maxProbability, Util.longToString(updateFrequency));
    }

    /**
     * Update probabilities assinged to moves.
     * @param normalized_runtime The runtime normalized between 0 and 1.
     */
    private void updateProbabilities(double normalized_runtime) {

        // Calculate moves' utility
        double sum = 0.0;
        for (int i = 0; i < moves.size(); ++i) {
            probabilities[i] = utility.evaluate(problem, moves.get(i).getClass(), bestSolution, normalized_runtime);
            sum += probabilities[i];
        }

        // Calculate moves' probabilities from utility values
        for (int i = 0; i < probabilities.length; ++i) {
            probabilities[i] = (1.0 - maxProbability + (moves.size() * maxProbability - 1.0) * (probabilities[i] / sum)) / (moves.size() - 1.0);
        }
    }

    /**
     * Select a move considering their utility.
     * @return A move.
     */
    @Override
    protected Move selectMove(Solution solution) {

        Move move = null;
        double ref;
        double acc;
        int idx;

        do {

            // Get a random number to perform a roulette
            ref = random.nextDouble();

            // Perform roulette
            acc = 0.0;
            idx = -1;

            do {
                acc += probabilities[++idx];
            } while ( (acc < ref) && (idx + 1 < moves.size()) );

            // Get selected move
            move = moves.get(idx);

        // Check if there is any possible move
        } while (!moves.get(idx).hasMove(solution));

        return move;
    }

}
