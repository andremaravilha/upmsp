package upmsp.algorithm.utility;

import upmsp.algorithm.neighborhood.Move;
import upmsp.model.Problem;
import upmsp.model.solution.Solution;

/**
 * Interface implemented by all prediction model for neighborhoods' utility.
 *
 * @author Andre L. Maravilha
 */
public interface UtilityModel {

    /**
     * Return the predicted utility of a move at a given moment of the optimization process.
     * @param problem Instance of the problem.
     * @param neighborhood Class of the neighborhood.
     * @param incumbent Incumbent solution.
     * @param runtime Runtime normalized between 0 and 1.
     * @return The predicted utility.
     */
    double evaluate(Problem problem, Class<? extends Move> neighborhood, Solution incumbent, double runtime);

}
