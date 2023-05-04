package play;

import play.NormalFormGame;
import scpsolver.constraints.Constraint;
import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.constraints.LinearEqualsConstraint;
import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LinearProgram;

import java.util.ArrayList;
import java.util.Locale;

public class ZeroSumLinearProgramming {

    static LinearProgram lp;
    static double[] x;

    public ZeroSumLinearProgramming() {
    }


    public static void setLP1(NormalFormGame game) {
        double[][] A = new double[game.u2[0].length + 1][game.u2.length + 1];

        // the constraints matrix will be P2's utility matrix, plus an extra column, plus an extra line
        for(int i=0; i < A.length - 1; i++) {
            for(int j=0; j < A[0].length - 1; j++) {
                A[i][j] = game.u2[j][i];
            }
        }

        // the extra column will be other player's utility * -1
        for(int i=0; i < A.length; i++)
            A[i][A[0].length - 1] = -1.0;

        // the extra row will be all the actions
        for(int j=0; j < A[0].length; j++) {
            A[A.length - 1][j] = 1.0;
        }

        // the function to minimize will be simply the last variable: P1's utility
        double[] c = new double[A[0].length];
        Arrays.fill(c, 0.0);
        c[c.length - 1] = 1.0;

        double[] b = new double[A.length];
        Arrays.fill(b, 0.0);
        b[b.length - 1] = 1.0;

        double[] lb = new double[c.length];
        Arrays.fill(c, 0.0);
        //double[] lb = {0.0, 0.0};
        lp = new LinearProgram(c);
        lp.setMinProblem(false);
        for (int i = 0; i<b.length; i++)
            lp.addConstraint(new LinearSmallerThanEqualsConstraint(A[i], b[i], "c"+i));
        lp.setLowerbound(lb);
    }

    public static boolean solveLP() {
        LinearProgramSolver solver  = SolverFactory.newDefault();
        x = solver.solve(lp);
        if (x==null) return false;
        return true;
    }

    public static void showSolution() {
        if (x==null) System.out.println("*********** NO SOLUTION FOUND ***********");
        else {
            System.out.println("*********** SOLUTION ***********");
            for (int i = 0; i<x.length; i++) System.out.println("x["+i+"] = "+x[i]);
            System.out.println("f(x) = "+ lp.evaluate(x));
        }
    }

    public static void showLP() {
        System.out.println("*********** LINEAR PROGRAMMING PROBLEM ***********");
        String fs;
        if (lp.isMinProblem()) System.out.print("  minimize: ");
        else System.out.print("  maximize: ");
        double[] cf = lp.getC();
        for (int i = 0; i<cf.length; i++) if (cf[i] != 0) {
            fs = String.format(Locale.US,"%+7.1f", cf[i]);
            System.out.print(fs + "*x["+i+"]");
        }
        System.out.println("");
        System.out.print("subject to: ");
        ArrayList<Constraint> lcstr = lp.getConstraints();
        double aij;
        double[] ci = null;
        String str = null;
        for (int i = 0; i<lcstr.size(); i++) {
            if (lcstr.get(i) instanceof LinearSmallerThanEqualsConstraint) {
                str = " <= ";
                ci = ((LinearSmallerThanEqualsConstraint) lcstr.get(i)).getC();
            }
            if (lcstr.get(i) instanceof LinearBiggerThanEqualsConstraint) {
                str = " >= ";
                ci = ((LinearBiggerThanEqualsConstraint) lcstr.get(i)).getC();
            }
            if (lcstr.get(i) instanceof LinearEqualsConstraint) {
                str = " == ";
                ci = ((LinearEqualsConstraint) lcstr.get(i)).getC();
            }
            str = str + String.format(Locale.US,"%6.1f", lcstr.get(i).getRHS());
            if (i != 0) System.out.print("            ");
            for(int j=0;j<lp.getDimension();j++) {
                aij = ci[j];
                if (aij != 0) {
                    fs = String.format(Locale.US,"%+7.1f", aij);
                    System.out.print(fs + "*x["+j+"]");
                }
                else System.out.print("            ");
            }
            System.out.println(str);
        }
    }



    public static void main(String[] args) {
        //setLP1();
        showLP();
        solveLP();
        showSolution();

    }

}
