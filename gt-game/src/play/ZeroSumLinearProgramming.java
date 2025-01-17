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
import java.util.Arrays;
import java.util.Locale;

import static play.GeneralSumPL.CheckNegativeNumbers;

public class ZeroSumLinearProgramming {

    static LinearProgram lp;
    static double[] x;

    static String showNE;

    public ZeroSumLinearProgramming() {
    }

    public static double[][] symmetricNumberMatrix(double[][] originalM) {
        double[][] resultM = new double[originalM.length][originalM[0].length];
        for (int i = 0; i < originalM.length; i++) {
            for (int j = 0; j < originalM[0].length; j++) {
                resultM[i][j] = -originalM[i][j];
            }
        }
        return resultM;
    }

    public static void setLP1(int player, double[][] opponentUtilMatrix) {

        double[][] A = new double[opponentUtilMatrix[0].length + 1][opponentUtilMatrix.length + 1];

        // the constraints matrix will be other player's utility matrix, plus an extra column, plus an extra line
        for(int i=0; i < A.length - 1; i++) {
            for(int j=0; j < A[0].length - 1; j++) {
                if(player == 1)
                    A[i][j] = opponentUtilMatrix[j][i];
                else
                    A[i][j] = opponentUtilMatrix[i][j];
            }
        }

        // the extra column will represent current player's utility * -1
        for(int i=0; i < A.length - 1; i++)
            A[i][A[0].length - 1] = -1.0;

        // the extra row will be all the actions
        for(int j=0; j < A[0].length - 1; j++) {
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
        double min = Double.MAX_VALUE;
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[i].length; j++) {
                if (A[i][j] < min)
                    min = A[i][j];
            }
        }
        Arrays.fill(lb, 0.0);
        lb[lb.length - 1] = min;

        lp = new LinearProgram(c);
        lp.setMinProblem(true);
        for (int i = 0; i<b.length - 1; i++)
            lp.addConstraint(new LinearSmallerThanEqualsConstraint(A[i], b[i], "c"+i));
        lp.addConstraint(new LinearEqualsConstraint(A[A.length - 1], 1.0, "c"+(b.length - 1))); // nao percebo o ultimo argumento but should be this
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

    private static void ShowNE(boolean[] sup1, boolean[] sup2, NormalFormGame game, int player){
        int x_counter = 0;
        if(player == 1) {
            showNE += "Player 1 strategy: \n";
            System.out.println("Player 1 strategy: ");

            for (int i = 0; i < sup1.length; i++) {
                if (sup1[i]) {
                    System.out.println((Math.round(x[x_counter] * 100.0) / 100.0) + " : " + game.rowActions.get(i));
                    showNE += (Math.round(x[x_counter] * 100.0) / 100.0) + " : " + game.rowActions.get(i) + "\n";
                    x_counter++;
                } else {
                    System.out.println("0,00 : " + game.rowActions.get(i));
                    showNE += "0,00 : " + game.rowActions.get(i) + "\n";
                }
            }
        }else {
            System.out.println("Player 2 strategy: ");
            showNE += "Player 2 strategy: \n";
            for (int i = 0; i < sup2.length; i++) {
                if (sup2[i]) {
                    System.out.println((Math.round(x[x_counter] * 100.0) / 100.0) + " : " + game.colActions.get(i));
                    showNE += (Math.round(x[x_counter] * 100.0) / 100.0) + " : " + game.colActions.get(i) + "\n";
                    x_counter++;
                } else {
                    System.out.println("0,00 : " + game.colActions.get(i));
                    showNE += "0,00 : " + game.colActions.get(i) + "\n";
                }
            }
        }
    }

    public static void ComputeZeroSumNE(NormalFormGame game) {
        //CheckNegativeNumbers(game);
        System.out.println("********** PLAYER 1 STRATEGY **********");
        setLP1(1, game.u2);
        showLP();
        solveLP();
        showSolution();

        System.out.println("********** PLAYER 2 STRATEGY **********");
        setLP1(2, game.u1);
        showLP();
        solveLP();
        showSolution();
    }

    public static double[] ComputeMaxMin(NormalFormGame game, int player) {
        System.out.printf("********** PLAYER %d UTILITY **********\n", player);
        if(player == 1) {
            game.u2 = symmetricNumberMatrix(game.u1);
            setLP1(1, game.u2);
        }

        else if(player == 2) {
            game.u1 = symmetricNumberMatrix(game.u2);
            setLP1(2, game.u1);
        }
        showLP();
        solveLP();
        showSolution();
        ShowNE(game.pRow, game.pCol, game, player);

        double[] strat = Arrays.copyOf(x, x.length-1);
        return strat; // TODO
    }

    public static double[] ComputeMinMax(NormalFormGame game, int player) {
        System.out.printf("********** PLAYER %d UTILITY **********\n", player);
        if(player == 1) {
            game.u1 = symmetricNumberMatrix(game.u2);
            setLP1(1, game.u2);
        }

        else if(player == 2) {
            game.u2 = symmetricNumberMatrix(game.u1);
            setLP1(2, game.u1);
        }
        showLP();
        solveLP();
        showSolution();
        ShowNE(game.pRow, game.pCol, game, player);

        double[] strat = Arrays.copyOf(x, x.length-1);
        return strat; // TODO
    }

    public static void main(String[] args) {
        NormalFormGame game = new NormalFormGame();
        game.u1 = new double[][]{{3, 2, 1}, {-5, -4, 0}, {2, 0, -2}};
        game.u2 = new double[][]{{4, 2, 0}, {5, 2, 1}, {-5, -2, 1}};
        // add actions to the game
        game.nRow = 3;
        game.nCol = 3;
        //add actions to the game
        game.rowActions = new ArrayList<>();
        game.rowActions.add("a");
        game.rowActions.add("b");
        game.rowActions.add("c");

        game.colActions = new ArrayList<>();
        game.colActions.add("A");
        game.colActions.add("B");
        game.colActions.add("C");

        ComputeMaxMin(game, 1);
    }

}
