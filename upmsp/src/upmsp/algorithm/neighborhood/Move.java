package upmsp.algorithm.neighborhood;

import upmsp.model.*;
import upmsp.model.solution.*;

import java.util.*;

/**
 * This abstract class represents a Move (or Neighborhood). The basic methods as well as several counters (for future
 * analysis) are included.
 *
 * @author Tulio Toffolo
 * @author Andre L. Maravilha
 */
public abstract class Move {

    /**
     * Basic statistics about a move.
     */
    public static class Stats {

        private long calls = 0;
        private long rejects = 0;
        private long accepts = 0;
        private long improvements = 0;
        private long sideways = 0;
        private long worsens = 0;

        /**
         * Number of calls.
         * @return number of calls.
         */
        public long getCalls() {
            return calls;
        }

        /**
         * Number of rejects.
         * @return number of rejects.
         */
        public long getRejects() {
            return rejects;
        }

        /**
         * Number of accepts.
         * @return number of accepts.
         */
        public long getAccepts() {
            return accepts;
        }

        /**
         * Number of improvements.
         * @return number of improvements.
         */
        public long getImprovements() {
            return improvements;
        }

        /**
         * Number of sideways.
         * @return number of sideways.
         */
        public long getSideways() {
            return sideways;
        }

        /**
         * Number of worsens.
         * @return number of worsens.
         */
        public long getWorsens() {
            return worsens;
        }

        /**
         * Reset counts.
         */
        private void reset() {
            calls = 0;
            rejects = 0;
            accepts = 0;
            improvements = 0;
            sideways = 0;
            worsens = 0;
        }
    }


    // About the move
    public final Problem problem;
    public final String name;
    public final Random random;

    // Statistics
    private Stats statsOverall;
    private Stats statsRegularRandomMachine;
    private Stats statsRegularMakespanMachine;
    private Stats statsIntensificationRandomMachine;
    private Stats statsIntensificationMakespanMachine;

    // State of the current call to doMove
    protected Solution currentSolution;
    protected boolean intermediateState = false;
    protected int initialCost = Integer.MAX_VALUE;
    protected int deltaCost = 0;
    protected Stats stats = null;


    /**
     * Instantiates a new Move.
     * @param problem the problem reference.
     * @param random  the random number generator.
     */
    public Move(Problem problem, Random random) {
        this.problem = problem;
        this.random = random;
        this.name = "Unnamed move";
    }

    /**
     * Instantiates a new Move.
     * @param problem  the problem reference.
     * @param random   the random number generator.
     * @param name     the name of this neighborhood (for debugging purposes).
     */
    public Move(Problem problem, Random random, String name) {
        this.problem = problem;
        this.random = random;
        this.name = name;

        statsOverall = new Stats();
        statsRegularRandomMachine = new Stats();
        statsRegularMakespanMachine = new Stats();
        statsIntensificationRandomMachine = new Stats();
        statsIntensificationMakespanMachine = new Stats();
    }

    /**
     * This method returns a boolean indicating whether this neighborhood can be applied to the current solution.
     * @return true if this neighborhood can be applied to the current solution and false otherwise.
     */
    public boolean hasMove(Solution solution, boolean useIntensificationPolicy, boolean useMakespanMachine) {
        return true;
    }

    /**
     * This method returns does the move and returns the impact (delta cost) in the solution.
     * @param solution the solution to be modified.
     * @param useIntensificationPolicy if true, a small subset of neighbor solutions is evaluated; otherwise a single
     *        solution is evaluated.
     * @param useMakespanMachine if true, the main machine involved in the neighborhood generation is the makespan
     *        machine; otherwise a random machine is used.
     * @return the impact (delta cost) of this move in the solution.
     */
    public int doMove(Solution solution, boolean useIntensificationPolicy, boolean useMakespanMachine) {
        assert hasMove(solution, useIntensificationPolicy, useMakespanMachine) : "Error: move " + name + " being executed with hasMove() = false.";
        assert !intermediateState : "Error: calling doMove before mandatory call to accept() or reject().";

        intermediateState = true;

        stats = getStats(useIntensificationPolicy, useMakespanMachine);
        stats.calls++;
        statsOverall.calls++;

        currentSolution = solution;
        initialCost = solution.getCost();
        return deltaCost = Integer.MAX_VALUE;
    }

    /**
     * Return the impact (delta cost) of the last call to doMove().
     * @return the impact (delta cost) of the last call to doMove().
     */
    public int getDeltaCost() {
        return deltaCost;
    }

    /**
     * This method must be called whenever the modification made by this move is accepted. It ensures that the solution
     * as well as other structures are updated accordingly.
     */
    public void accept() {
        assert intermediateState : "Error: calling accept() before calling doMove().";
        intermediateState = false;
        updateStats(true);
    }

    /**
     * This method must be called whenever the modification made by this move are rejected. It ensures that the solution
     * as well as other structures are updated accordingly.
     */
    public void reject() {
        assert intermediateState : "Error: calling reject() before calling doMove().";
        intermediateState = false;
        updateStats(false);
    }

    /**
     * This method is called whenever the neighborhood should be reset (mainly to avoid the need of creating another
     * object).
     */
    public void reset() {
        statsOverall.reset();
        statsRegularRandomMachine.reset();
        statsRegularMakespanMachine.reset();
        statsIntensificationRandomMachine.reset();
        statsIntensificationMakespanMachine.reset();
    }

    /**
     * Return the overall stats of the move.
     * @return the overall stats.
     */
    public Stats getStats() {
        return statsOverall;
    }

    /**
     * Return the stats of a specific strategy.
     * @param useIntensificationPolicy if true, a small subset of neighbor solutions is evaluated; otherwise a single
     *        solution is evaluated.
     * @param useMakespanMachine if true, the main machine involved in the neighborhood generation is the makespan
     *        machine; otherwise a random machine is used.
     * @return stats of a specific strategy.
     */
    public Stats getStats(boolean useIntensificationPolicy, boolean useMakespanMachine) {
        if (useIntensificationPolicy) {
            if (useMakespanMachine) {
                return statsIntensificationMakespanMachine;
            } else {
                return statsIntensificationRandomMachine;
            }
        } else {
            if (useMakespanMachine) {
                return statsRegularMakespanMachine;
            } else {
                return statsRegularRandomMachine;
            }
        }
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Update stats.
     * @param accepted true if move was accepted, false otherwise.
     */
    private void updateStats(boolean accepted) {

        if (accepted) {
            statsOverall.accepts++;
            stats.accepts++;
        } else {
            statsOverall.rejects++;
            stats.rejects++;
        }

        if (deltaCost < 0) {
            statsOverall.improvements++;
            stats.improvements++;
        } else if (deltaCost == 0) {
            statsOverall.sideways++;
            stats.sideways++;
        } else {
            statsOverall.worsens++;
            stats.worsens++;
        }
    }

}
