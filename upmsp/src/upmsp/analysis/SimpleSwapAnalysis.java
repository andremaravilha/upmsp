package upmsp.analysis;

import upmsp.model.Problem;
import upmsp.model.solution.Solution;

/**
 * Perform the analysis of the simple-swap move.
 *
 * @author Andre L. Maravilha
 */
public class SimpleSwapAnalysis implements MoveAnalysis {

    @Override
    public String name() {
        return "direct-swap";
    }

    @Override
    public Result analyze(Problem problem, Solution start) {

        Result result = new Result(name(), start);
        Solution solution = start.clone();

        for (int m1 = 0; m1 < problem.nMachines; ++m1) {
            for (int idx1 = 0; idx1 < solution.machines[m1].getNJobs(); ++idx1) {
                for (int m2 = m1 + 1; m2 < problem.nMachines; ++m2) {
                    for (int idx2 = 0; idx2 < solution.machines[m2].getNJobs(); ++idx2) {

                        // Swap jobs keeping their original position
                        int job1 = solution.machines[m1].jobs[idx1];
                        int job2 = solution.machines[m2].jobs[idx2];
                        solution.machines[m2].setJob(job1, idx2);
                        solution.machines[m1].setJob(job2, idx1);

                        // Evaluate move
                        solution.updateCost();
                        result.register(solution);

                        // Undo swap move
                        solution.machines[m1].setJob(job1, idx1);
                        solution.machines[m2].setJob(job2, idx2);
                    }
                }
            }
        }

        return result;
    }
}
