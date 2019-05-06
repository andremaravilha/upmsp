package upmsp.algorithm.utility;

import org.apache.commons.math3.util.FastMath;
import upmsp.algorithm.neighborhood.*;
import upmsp.model.Problem;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Standard prediction model for neighborhoods' utility.
 */
public class StandardUtilityModel implements UtilityModel {

    final double INTERCEPT;
    final double SHIFT;
    final double DIRECT_SWAP;
    final double SWAP;
    final double SWITCH;
    final double TASK_MOVE;
    final double TWO_SHIFT;
    final double N;
    final double M;
    final double T;
    final double N_M;
    final double N_T;
    final double M_T;
    final double T2;


    public StandardUtilityModel(Path path) throws IOException {

        // Read file with coefficient values
        try (InputStream input = new FileInputStream(path.toAbsolutePath().toString())) {

            // Load file into a Properties object
            Properties properties = new Properties();
            properties.load(input);

            // Get coefficients
            INTERCEPT = Double.parseDouble(properties.getProperty("intercept"));
            SHIFT = Double.parseDouble(properties.getProperty("shift"));
            DIRECT_SWAP = Double.parseDouble(properties.getProperty("direct_swap"));
            SWAP = Double.parseDouble(properties.getProperty("swap"));
            SWITCH = Double.parseDouble(properties.getProperty("switch"));
            TASK_MOVE = Double.parseDouble(properties.getProperty("task_move"));
            TWO_SHIFT = Double.parseDouble(properties.getProperty("two_shift"));
            N = Double.parseDouble(properties.getProperty("n"));
            M = Double.parseDouble(properties.getProperty("m"));
            T = Double.parseDouble(properties.getProperty("t"));
            N_M = Double.parseDouble(properties.getProperty("n_m"));
            N_T = Double.parseDouble(properties.getProperty("n_t"));
            M_T = Double.parseDouble(properties.getProperty("m_t"));
            T2 = Double.parseDouble(properties.getProperty("t2"));
        }
    }

    @Override
    public double evaluate(Problem problem, Class<? extends Move> neighborhood, double t) {

        // Transform time
        double xt = 1.0 / t;

        // Get neighborhood coefficient
        double neighborhood_coeff = 0.0;
        if (Shift.class.equals(neighborhood) || ShiftSmart.class.equals(neighborhood)) neighborhood_coeff = SHIFT;
        else if (SimpleSwap.class.equals(neighborhood) || SimpleSwapSmart.class.equals(neighborhood)) neighborhood_coeff = DIRECT_SWAP;
        else if (Swap.class.equals(neighborhood) || SwapSmart.class.equals(neighborhood)) neighborhood_coeff = SWAP;
        else if (Switch.class.equals(neighborhood) || SwitchSmart.class.equals(neighborhood)) neighborhood_coeff = SWITCH;
        else if (TaskMove.class.equals(neighborhood) || TaskMoveSmart.class.equals(neighborhood)) neighborhood_coeff = TASK_MOVE;
        else if (TwoShift.class.equals(neighborhood) || TwoShiftSmart.class.equals(neighborhood)) neighborhood_coeff = TWO_SHIFT;

        return FastMath.pow(
                INTERCEPT + neighborhood_coeff + N * problem.nJobs + M * problem.nMachines + T * xt +
                N_M * problem.nJobs * problem.nMachines + N_T * problem.nJobs * xt + M_T * problem.nMachines * xt +
                T2 * xt * xt,
                4);
    }
    
}
