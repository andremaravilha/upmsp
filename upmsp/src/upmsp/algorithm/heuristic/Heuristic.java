package upmsp.algorithm.heuristic;

import upmsp.algorithm.neighborhood.*;
import upmsp.model.*;
import upmsp.model.solution.*;

import java.io.*;
import java.util.*;

/**
 * This abstract class represents a Heuristic (or Local Search method). The basic methods and neighborhood selection are
 * included.
 *
 * @author Tulio Toffolo
 * @author Andre L. Maravilha
 */
public abstract class Heuristic {

    public final Problem problem;
    public final Random random;
    public final String name;

    protected final List<Move> moves = new ArrayList<>();

    protected Solution bestSolution;
    protected long nIters = 0;


    /**
     * Instantiates a new Heuristic.
     * @param problem the problem reference.
     * @param random  the random number generator.
     * @param name    the name
     */
    public Heuristic(Problem problem, Random random, String name) {
        this.problem = problem;
        this.random = random;
        this.name = name;
    }

    /**
     * Adds a move to the heuristic.
     * @param move the move to be added.
     */
    public void addMove(Move move) {
        moves.add(move);
    }

    /**
     * Returns an unmodifiableList with the moves in the heuristic.
     * @return an unmodifiableList with the moves in the heuristic.
     */
    public List<Move> getMoves() {
        return Collections.unmodifiableList(moves);
    }

    /**
     * Resets all moves considered by the heuristic.
     */
    public void resetMoves() {
        for (Move move : moves) {
            move.reset();
        }
    }

    /**
     * Randomly selects a move.
     * @return a move.
     */
    protected Move selectMove() {
        return moves.get(random.nextInt(moves.size()));
    }

    /**
     * Accepts move.
     * @param move the move to be accepted.
     */
    protected void acceptMove(Move move) {
        move.accept();
    }

    /**
     * Rejects move.
     *
     * @param move the move to be rejected.
     */
    protected void rejectMove(Move move) {
        move.reject();
    }

    /**
     * Runs the heuristic, returning the best solution obtained.
     * @param solution        the initial (input) solution.
     * @param timeLimitNano   the time limit in nanoseconds.
     * @param maxIters        the maximum number of iterations to execute.
     * @param callback        callback object.
     * @param output          the output.
     * @return the solution
     */
    public abstract Solution run(Solution solution, long timeLimitNano, long maxIters, Callback callback, PrintStream output);

    /**
     * Gets best solution.
     * @return the best solution obtained so far.
     */
    public Solution getBestSolution() {
        return bestSolution;
    }

    /**
     * Gets the number of iterations executed.
     * @return the n iters
     */
    public long getNIters() {
        return nIters;
    }

    /**
     * Returns the string representation of the heuristic.
     * @return the string representation of the heuristic.
     */
    @Override
    public String toString() {
        return name;
    }


    /**
     * Callback interface.
     */
    public interface Callback {

        /**
         * Called when a new incumbent solution is found.
         * @param incumbent Incumbent solution.
         * @param move Class of the move that returned the incumbent solution.
         * @param runtimeNano Runtime (in nanoseconds).
         * @param timeLimitNano Time limit (in nanoseconds).
         * @param iteration Iteration.
         * @param iterationLimit Maximum number of iterations.
         */
        void onNewIncumbent(Solution incumbent, Class<? extends Move> move, long runtimeNano, long timeLimitNano, long iteration, long iterationLimit);

        /**
         * Called at the end of an iteration.
         * @param incumbent Incumbent solution.
         * @param runtimeNano Runtime (in nanoseconds).
         * @param timeLimitNano Time limit (in nanoseconds).
         * @param iteration Iteration.
         * @param iterationLimit Maximum number of iterations.
         */
        void onIteration(Solution incumbent, long runtimeNano, long timeLimitNano, long iteration, long iterationLimit);

    }
}
