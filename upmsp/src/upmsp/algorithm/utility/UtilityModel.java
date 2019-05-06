package upmsp.algorithm.utility;

import upmsp.algorithm.neighborhood.Move;
import upmsp.model.Problem;

/**
 * Interface implemented by all prediction model for neighborhoods' utility.
 *
 * @author Andre L. Maravilha
 */
public interface UtilityModel {

    /**
     * Return the predicted utility of a certain neighborhood (or move) in a certain problem at a given moment t.
     * @param problem Instance of the problem.
     * @param neighborhood Class of the neighborhood.
     * @param t Moment (Runtime normalized between 0 and 1).
     * @return The predicted utility.
     */
    double evaluate(Problem problem, Class<? extends Move> neighborhood, double t);

}
