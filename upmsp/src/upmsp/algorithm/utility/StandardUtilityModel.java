package upmsp.algorithm.utility;

import org.apache.commons.math3.util.FastMath;
import upmsp.algorithm.neighborhood.*;
import upmsp.model.Problem;
import upmsp.model.solution.Solution;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Standard prediction model for neighborhoods' utility.
 */
public class StandardUtilityModel implements UtilityModel {

    private static class Coefficients {
        public double INTERCEPT = 0.0;
        public double J = 0.0;
        public double M = 0.0;
        public double S = 0.0;
        public double SX = 0.0;
        public double T = 0.0;
        public double J2 = 0.0;
        public double M2 = 0.0;
        public double S2 = 0.0;
        public double SX2 = 0.0;
        public double T2 = 0.0;
        public double J_M = 0.0;
        public double J_S = 0.0;
        public double J_T = 0.0;
        public double M_S = 0.0;
        public double M_SX = 0.0;
    }

    private Coefficients cShift = new Coefficients();
    private Coefficients cDirectSwap = new Coefficients();
    private Coefficients cSwap = new Coefficients();
    private Coefficients cSwitch = new Coefficients();
    private Coefficients cTaskMove = new Coefficients();
    private Coefficients cTwoShift = new Coefficients();

    public StandardUtilityModel(Path path) throws IOException {

        // Read file with coefficient values
        try (InputStream input = new FileInputStream(path.toAbsolutePath().toString())) {

            // Load file into a Properties object
            Properties properties = new Properties();
            properties.load(input);

            String[] moves = { "SHIFT", "DIRECT_SWAP", "SWAP", "SWITCH", "TASK_MOVE", "TWO_SHIFT" };
            Coefficients[] coefficients = { cShift, cDirectSwap, cSwap, cSwitch, cTaskMove, cTwoShift };

            // Read coefficients for each move
            for (int i = 0; i < moves.length; ++i) {

                String move = moves[i];
                Coefficients c = coefficients[i];

                c.INTERCEPT = Double.parseDouble(properties.getProperty(move + ".INTERCEPT"));
                c.J = Double.parseDouble(properties.getProperty(move + ".J"));
                c.M = Double.parseDouble(properties.getProperty(move + ".M"));
                c.S = Double.parseDouble(properties.getProperty(move + ".S"));
                c.SX = Double.parseDouble(properties.getProperty(move + ".SX"));
                c.T = Double.parseDouble(properties.getProperty(move + ".T"));
                c.J2 = Double.parseDouble(properties.getProperty(move + ".J2"));
                c.M2 = Double.parseDouble(properties.getProperty(move + ".M2"));
                c.S2 = Double.parseDouble(properties.getProperty(move + ".S2"));
                c.SX2 = Double.parseDouble(properties.getProperty(move + ".SX2"));
                c.T2 = Double.parseDouble(properties.getProperty(move + ".T2"));
                c.J_M = Double.parseDouble(properties.getProperty(move + ".J_M"));
                c.J_S = Double.parseDouble(properties.getProperty(move + ".J_S"));
                c.J_T = Double.parseDouble(properties.getProperty(move + ".J_T"));
                c.M_S = Double.parseDouble(properties.getProperty(move + ".M_S"));
                c.M_SX = Double.parseDouble(properties.getProperty(move + ".M_SX"));
            }
        }
    }

    @Override
    public double evaluate(Problem problem, Class<? extends Move> neighborhood, Solution incumbent, double time) {

        // Transform time
        double t = FastMath.log10(time);

        // Get move coefficients
        Coefficients c = null;
        if (Shift.class.equals(neighborhood) || ShiftSmart.class.equals(neighborhood)) {
            c = cShift;
        } else if (SimpleSwap.class.equals(neighborhood) || SimpleSwapSmart.class.equals(neighborhood)) {
            c = cDirectSwap;
        } else if (Swap.class.equals(neighborhood) || SwapSmart.class.equals(neighborhood)) {
            c = cSwap;
        } else if (Switch.class.equals(neighborhood) || SwitchSmart.class.equals(neighborhood)) {
            c = cSwitch;
        } else if (TaskMove.class.equals(neighborhood) || TaskMoveSmart.class.equals(neighborhood)) {
            c = cTaskMove;
        } else if (TwoShift.class.equals(neighborhood) || TwoShiftSmart.class.equals(neighborhood)) {
            c = cTwoShift;
        }

        double log10E = c.INTERCEPT +
                c.J * problem.nJobs + c.M * problem.nMachines + c.S * problem.maximumSetupTime +
                c.SX * incumbent.getSumMachineTimes() + c.T * t + c.J2 * (problem.nJobs * problem.nJobs) +
                c.M2 * (problem.nMachines * problem.nMachines) + c.S2 * (problem.maximumSetupTime * problem.maximumSetupTime) +
                c.SX2 * (incumbent.getSumMachineTimes() * incumbent.getSumMachineTimes()) + c.T2 * (t * t) +
                c.J_M * (problem.nJobs * problem.nMachines) + c.J_S * (problem.nJobs * problem.maximumSetupTime) +
                c.J_T * (problem.nJobs * t) + c.M_S * (problem.nMachines * problem.maximumSetupTime) +
                c.M_SX * (problem.nMachines * incumbent.getSumMachineTimes());

        double utility = FastMath.pow(10, log10E);
        return utility;
    }
    
}
