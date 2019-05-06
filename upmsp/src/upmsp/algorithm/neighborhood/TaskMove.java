package upmsp.algorithm.neighborhood;

import upmsp.model.*;
import upmsp.model.solution.*;

import java.util.*;

/**
 * This class represents a Task Move (as in the paper). A neighbor in the Task Move is generated by moving one job from
 * one machine, m1, to another machine, m2. The parameter "useMakespanMachine" determines whether m1 is always the
 * machine with the largest total execution time or not.
 *
 * @author Tulio Toffolo
 */
public class TaskMove extends Move {

    private Machine machine1, machine2;
    private int posM1, posM2, job;
    private boolean useMakespanMachine;

    /**
     * Instantiates a new Task move.
     *
     * @param problem            the problem
     * @param random             the random
     * @param priority           the priority
     * @param useMakespanMachine the use makespan machine
     */
    public TaskMove(Problem problem, Random random, int priority, boolean useMakespanMachine) {
        super(problem, random, "Task-Move" + (useMakespanMachine ? "(mk)" : ""), priority);
        this.useMakespanMachine = useMakespanMachine;
    }

    public void accept() {
        super.accept();
    }

    public int doMove(Solution solution) {
        super.doMove(solution);

        // selecting machines to involve in operation
        if (useMakespanMachine && solution.makespanMachine.getNJobs() > 0) {
            int m;
            do {
                m = random.nextInt(solution.machines.length);
            }
            while (m == solution.makespanMachine.id);

            machine1 = solution.makespanMachine;
            machine2 = solution.machines[m];
        }
        else {
            int m1, m2;
            do {
                m1 = random.nextInt(solution.machines.length);
                m2 = random.nextInt(solution.machines.length);
            }
            while (m1 == m2 || solution.machines[m1].getNJobs() == 0);
            machine1 = solution.machines[m1];
            machine2 = solution.machines[m2];
        }

        // selecting jobs to perform operation
        posM1 = random.nextInt(machine1.getNJobs());
        posM2 = random.nextInt(machine2.getNJobs() + 1);
        job = machine1.jobs[posM1];

        // moving job
        machine1.delJob(posM1);
        machine2.addJob(job, posM2);

        solution.updateCost();
        return deltaCost = solution.getCost() - initialCost;
    }

    public void reject() {
        super.reject();

        machine1.addJob(job, posM1);
        machine2.delJob(posM2);
        currentSolution.updateCost();
    }
}
