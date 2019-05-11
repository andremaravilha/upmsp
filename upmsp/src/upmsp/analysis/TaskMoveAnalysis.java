package upmsp.analysis;

import upmsp.model.Problem;
import upmsp.model.solution.Solution;

/**
 * Perform the analysis of the task-move move.
 *
 * @author Andre L. Maravilha
 */
public class TaskMoveAnalysis implements MoveAnalysis {

    @Override
    public String name() {
        return "task-move";
    }

    @Override
    public Result analyze(Problem problem, Solution start) {

        Result result = new Result(name(), start);
        Solution solution = start.clone();

        for (int m1 = 0; m1 < problem.nMachines; ++m1) {
            for (int idx1 = 0; idx1  < solution.machines[m1].getNJobs(); ++idx1) {

                // Get a job from machine M1 and remove it from there
                int job1 = solution.machines[m1].jobs[idx1];
                solution.machines[m1].delJob(idx1);

                for (int m2 = 0; m2 < problem.nMachines; ++m2) {
                    if (m2 != m1) {
                        for (int idx2 = 0; idx2 <= solution.machines[m2].getNJobs(); ++idx2) {

                            // Insert job removed from machine M1 in machine M2
                            solution.machines[m2].addJob(job1, idx2);

                            // Evaluate move
                            solution.updateCost();
                            result.register(solution);

                            // Undo insertion at machine M2
                            solution.machines[m2].delJob(idx2);
                        }
                    }
                }

                // Undo deletion from machine M1
                solution.machines[m1].addJob(job1, idx1);
            }
        }

        return result;
    }
}
