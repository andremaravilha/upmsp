package upmsp.analysis;

import upmsp.model.Problem;
import upmsp.model.solution.Solution;

/**
 * Perform the analysis of the shift move.
 *
 * @author Andre L. Maravilha
 */
public class ShiftAnalysis implements MoveAnalysis {

    @Override
    public String name() {
        return "shift";
    }

    @Override
    public Result analyze(Problem problem, Solution start) {

        Result result = new Result(name(), start);
        Solution solution = start.clone();

        for (int m = 0; m < problem.nMachines; ++m) {
            for (int idx1 = 0; idx1 < solution.machines[m].getNJobs(); ++idx1) {

                // Get and remove a job from its position
                int job = solution.machines[m].jobs[idx1];
                solution.machines[m].delJob(idx1);

                for (int idx2 = 0; idx2 <= solution.machines[m].getNJobs(); ++idx2) {
                    if (idx2 != idx1) {

                        // Insert the job removed at another position
                        solution.machines[m].addJob(job, idx2);

                        // Evaluate move
                        solution.updateCost();
                        result.register(solution);

                        // Undo insertion
                        solution.machines[m].delJob(idx2);
                    }
                }

                // Undo deletion
                solution.machines[m].addJob(job, idx1);
            }
        }

        return result;
    }
}
