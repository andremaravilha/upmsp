package upmsp.algorithm.neighborhood;

import upmsp.model.*;
import upmsp.model.solution.*;

import java.util.*;

/**
 * This class represents a Switch Move. A neighbor in the Switch Move structure
 * is generated by switching the order of two jobs of a machine. The parameter
 * "useMakespanMachine" determines whether the machine with the largest total
 * execution time is always used.
 *
 * @author Tulio Toffolo
 * @author Andre L Maravilha
 */
public class Switch extends Move {

    private Machine machine;
    private int pos1, pos2, job1, job2;

    /**
     * Instantiates a new Switch.
     * @param problem problem.
     * @param random  random number generator.
     */
    public Switch(Problem problem, Random random) {
        super(problem, random, "switch");
    }

    @Override
    public int doMove(Solution solution, boolean useIntensificationPolicy, boolean useMakespanMachine) {
        super.doMove(solution, useIntensificationPolicy, useMakespanMachine);

        // selecting machine for operation
        if (useMakespanMachine && solution.makespanMachine.getNJobs() > 1) {
            machine = solution.makespanMachine;
        } else {
            int m;
            do {
                m = random.nextInt(solution.machines.length);
            }
            while (solution.machines[m].getNJobs() <= 1);
            machine = solution.machines[m];
        }

        // selecting jobs to perform operation
        if (useIntensificationPolicy) {

            pos1 = random.nextInt(machine.getNJobs());
            job1 = machine.jobs[pos1];

            // selecting job in machine2
            int cost = Integer.MAX_VALUE;
            for (int p = 0; p < machine.getNJobs(); p++) {
                if (p == pos1) continue;
                int candidateJob = machine.jobs[p];
                int simulatedCost = machine.getDeltaCostSetJob(candidateJob, pos1) + machine.getDeltaCostSetJob(job1, p);
                if (simulatedCost < cost) {
                    cost = simulatedCost;
                    pos2 = p;
                    job2 = candidateJob;
                }
            }

            // swapping jobs
            machine.setJob(job2, pos1);
            machine.setJob(job1, pos2);

        } else {
            pos1 = random.nextInt(machine.getNJobs());
            pos2 = random.nextInt(machine.getNJobs());
            job1 = machine.jobs[pos1];
            job2 = machine.jobs[pos2];

            // swapping jobs
            machine.setJob(job2, pos1);
            machine.setJob(job1, pos2);
        }

        solution.updateCost();
        return deltaCost = solution.getCost() - initialCost;
    }

    @Override
    public boolean hasMove(Solution solution, boolean useIntensificationPolicy, boolean useMakespanMachine) {
        return !useMakespanMachine || solution.makespanMachine.getNJobs() > 1;
    }

    @Override
    public void accept() {
        super.accept();
    }

    @Override
    public void reject() {
        super.reject();

        machine.setJob(job1, pos1);
        machine.setJob(job2, pos2);
        currentSolution.updateCost();
    }
}
