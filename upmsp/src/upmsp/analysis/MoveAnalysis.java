package upmsp.analysis;

import upmsp.model.Problem;
import upmsp.model.solution.Solution;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Interface implemented by classes that analyze a move.
 *
 * @author Andre L. Maravilha
 */
public interface MoveAnalysis {

    /**
     * Categories in which a neighbor solution can be classified.
     */
    enum Category {
        BETTER_BETTER, BETTER_EQUAL, BETTER_WORSE,
        EQUAL_BETTER, EQUAL_EQUAL, EQUAL_WORSE,
        WORSE_BETTER, WORSE_EQUAL, WORSE_WORSE
    }

    /**
     * Keep (best, worst and sum) changes on a given value.
     */
    class Delta {
        public Long best = null;
        public Long worst = null;
        public Long sum = null;
    }

    /**
     * Keep stats of a kind of entry.
     */
    class Stats {
        public long count;
        public Delta makespan;
        public Delta sumMachineTimes;

        public Stats() {
            count = 0L;
            makespan = new Delta();
            sumMachineTimes = new Delta();
        }
    }

    /**
     * Stats returned by move analysis.
     */
    class Result {

        public String move;
        public long cardinality;
        public long refMakespan;
        public long refSumMachineTimes;
        public Map<Category, Stats> stats;

        public Result(String move, Solution ref) {

            this.move = move;
            this.cardinality = 0L;
            this.refMakespan = ref.getCost();
            this.refSumMachineTimes = Arrays.asList(ref.machines).stream().mapToLong(x -> x.getMakespan()).sum();

            stats = new HashMap<>();
            for (Category category : Category.values()) {
                stats.put(category, new Stats());
            }
        }

        public void register(Solution solution) {

            long makespan = solution.getCost();
            long sumMachineTimes = Arrays.asList(solution.machines).stream().mapToLong(x -> x.getMakespan()).sum();

            String flag1 = (makespan < refMakespan ? "BETTER" : (makespan > refMakespan ? "WORSE" : "EQUAL"));
            String flag2 = (sumMachineTimes < refSumMachineTimes ? "BETTER" : (sumMachineTimes > refSumMachineTimes ? "WORSE" : "EQUAL"));
            String flag = flag1 + "_" + flag2;

            Category category = Category.valueOf(flag);
            Stats s = stats.get(category);

            s.count +=1;

            s.makespan.best = Math.min(makespan, (s.makespan.best == null ? Long.MAX_VALUE : s.makespan.best));
            s.makespan.worst = Math.max(makespan, (s.makespan.worst == null ? Long.MIN_VALUE : s.makespan.worst));
            s.makespan.sum = (s.makespan.sum == null ? 0L : s.makespan.sum) + makespan;

            s.sumMachineTimes.best = Math.min(sumMachineTimes, (s.sumMachineTimes.best == null ? Long.MAX_VALUE : s.sumMachineTimes.best));
            s.sumMachineTimes.worst = Math.max(sumMachineTimes, (s.sumMachineTimes.worst == null ? Long.MIN_VALUE : s.sumMachineTimes.worst));
            s.sumMachineTimes.sum = (s.sumMachineTimes.sum == null ? 0L : s.sumMachineTimes.sum) + sumMachineTimes;

        }
    }

    /**
     * Return the name of the move.
     * @return The name of the move.
     */
    String name();

    /**
     * Evaluate neighbors obtained from this move.
     * @param problem Instance of the problem.
     * @param start Solution to generate a neighborhood.
     * @return The result of the neighborhood analysis.
     */
    Result analyze(Problem problem, Solution start);

}
