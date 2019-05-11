package upmsp.analysis;

import upmsp.model.Problem;
import upmsp.model.solution.Solution;

/**
 * Perform the analysis of the switch move.
 *
 * @author Andre L. Maravilha
 */
public class SwitchAnalysis implements MoveAnalysis {

    @Override
    public String name() {
        return "switch";
    }

    @Override
    public Result analyze(Problem problem, Solution start) {

        Result result = new Result(name(), start);
        Solution solution = start.clone();

        for (int m = 0; m < problem.nMachines; ++m) {
            for (int idx1 = 0; idx1 < solution.machines[m].getNJobs(); ++idx1) {
                for (int idx2 = idx1 + 1; idx2 < solution.machines[m].getNJobs(); ++idx2) {

                    // Switch jobs keeping their original position
                    int job1 = solution.machines[m].jobs[idx1];
                    int job2 = solution.machines[m].jobs[idx2];
                    solution.machines[m].setJob(job1, idx2);
                    solution.machines[m].setJob(job2, idx1);

                    // Evaluate move
                    solution.updateCost();
                    result.register(solution);

                    // Undo swap move
                    solution.machines[m].setJob(job1, idx1);
                    solution.machines[m].setJob(job2, idx2);
                }
            }
        }

        return result;
    }
}
