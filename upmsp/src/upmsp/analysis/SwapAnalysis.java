package upmsp.analysis;

import upmsp.model.Problem;
import upmsp.model.solution.Solution;

/**
 * Perform the analysis of the swap move.
 *
 * @author Andre L. Maravilha
 */
public class SwapAnalysis implements MoveAnalysis {

    @Override
    public String name() {
        return "swap";
    }

    @Override
    public Result analyze(Problem problem, Solution start) {

        Result result = new Result(name(), start);
        Solution solution = start.clone();

        for (int m1 = 0; m1 < problem.nMachines; ++m1) {
            for (int idx1_from = 0; idx1_from  < solution.machines[m1].getNJobs(); ++idx1_from) {

                // Get a job from machine M1 and remove it from there
                int job1 = solution.machines[m1].jobs[idx1_from];
                solution.machines[m1].delJob(idx1_from);

                for (int m2 = m1 + 1; m2 < problem.nMachines; ++m2) {
                    for (int idx2_from = 0; idx2_from  < solution.machines[m2].getNJobs(); ++idx2_from) {

                        // Get a job from machine M2 and remove it from there
                        int job2 = solution.machines[m2].jobs[idx2_from];
                        solution.machines[m2].delJob(idx2_from);

                        for (int idx1_target = 0; idx1_target <= solution.machines[m1].getNJobs(); ++idx1_target) {

                            // Insert job removed from machine M2 in machine M1
                            solution.machines[m1].addJob(job2, idx1_target);

                            for (int idx2_target = 0; idx2_target <= solution.machines[m2].getNJobs(); ++idx2_target) {

                                // Insert job removed from machine M1 in machine M2
                                solution.machines[m2].addJob(job1, idx2_target);

                                // Evaluate move
                                solution.updateCost();
                                result.register(solution);

                                // Undo insertion at machine M2
                                solution.machines[m2].delJob(idx2_target);
                            }

                            // Undo insertion at machine M1
                            solution.machines[m1].delJob(idx1_target);
                        }

                        // Undo deletion from machine M2
                        solution.machines[m2].addJob(job2, idx2_from);
                    }
                }

                // Undo deletion from machine M1
                solution.machines[m1].addJob(job1, idx1_from);
            }
        }

        return result;
    }
}
