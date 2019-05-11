package upmsp.analysis;

import upmsp.model.Problem;
import upmsp.model.solution.Solution;

/**
 * Perform the analysis of the two-shift move.
 *
 * @author Andre L. Maravilha
 */
public class TwoShiftAnalysis implements MoveAnalysis {

    @Override
    public String name() {
        return "two-shift";
    }

    @Override
    public Result analyze(Problem problem, Solution start) {

        Result result = new Result(name(), start);
        Solution solution = start.clone();

        for (int m = 0; m < problem.nMachines; ++m) {
            for (int idx1_from = 0; idx1_from  < solution.machines[m].getNJobs(); ++idx1_from) {

                // Get a first job from machine M and remove it from there
                int job1 = solution.machines[m].jobs[idx1_from];
                solution.machines[m].delJob(idx1_from);

                for (int idx2_from = idx1_from + 1; idx2_from  < solution.machines[m].getNJobs(); ++idx2_from) {

                    // Get a second job from machine M and remove it from there
                    int job2 = solution.machines[m].jobs[idx2_from];
                    solution.machines[m].delJob(idx2_from);

                    for (int idx2_target = 0; idx2_target <= solution.machines[m].getNJobs(); ++idx2_target) {

                        // Re-insert the second job removed from machine M
                        solution.machines[m].addJob(job2, idx2_target);

                        for (int idx1_target = 0; idx1_target <= solution.machines[m].getNJobs(); ++idx1_target) {
                            if (idx1_target != idx1_from && idx2_target != idx2_from) {

                                // Re-insert the first job removed from machine M
                                solution.machines[m].addJob(job1, idx1_target);

                                // Evaluate move
                                solution.updateCost();
                                result.register(solution);

                                // Undo insertion of job 1
                                solution.machines[m].delJob(idx1_target);
                            }
                        }

                        // Undo insertion of job 2
                        solution.machines[m].delJob(idx2_target);
                    }

                    // Undo deletion of job 2
                    solution.machines[m].addJob(job2, idx2_from);
                }

                // Undo deletion of job 1
                solution.machines[m].addJob(job1, idx1_from);
            }
        }

        return result;
    }
}
