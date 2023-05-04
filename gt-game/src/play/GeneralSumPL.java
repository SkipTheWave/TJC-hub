package play;

import scpsolver.constraints.Constraint;
import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.constraints.LinearEqualsConstraint;
import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LinearProgram;
import util.GetSubSets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class GeneralSumPL {

    static LinearProgram lp;
    static double[] x;

    public GeneralSumPL() {
    }

    private static double GetActionUtilities(int player, NormalFormGame game, int action, int currentRow, boolean[] sup) {

        int activeRC = 0;
        if (player == 1) {

            for (int i = 0; i < sup.length; i++) {
                if (sup[i]) {
                    if (action == activeRC) {
                        return game.u1[currentRow][i];
                    }
                    activeRC++;
                }
            }

        } else {
            for (int i = 0; i < sup.length; i++) {
                if (sup[i]) {

                    if (action == activeRC) {
                        return game.u2[i][currentRow];
                    }
                    activeRC++;
                }
            }

        }
        return 0.0;
    }

    private static void ComputeGeneralSum(int numberSupport, boolean[] sup1, boolean[] sup2, NormalFormGame game) {

        double[][] A = new double[sup1.length + sup2.length + 2][(numberSupport * 2) + 2];

        StringBuilder x = new StringBuilder();
        for (boolean value : sup1) {
            if (value)
                x.append("1 ");
            else
                x.append("0 ");
        }
        StringBuilder y = new StringBuilder();
        for (boolean value : sup2) {
            if (value)
                y.append("1 ");
            else
                y.append("0 ");
        }
        System.out.println("Test: X " + x + " Y " + y);

        double[] c = new double[A[0].length];
        double[] b = new double[A.length];
        for (int i = 0; i < b.length; i++) {
            if(i < b.length - 2)
                b[i] = 0;
            else
                b[i] = 1;
        }
        double[] lb = new double[A[0].length];
        //iterate A
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++) {

                A[i][j] = 0;

                //Adiciona nas ultimas 2 linhas quantos suportes tem
                if (i == A.length - 2 && j < numberSupport) {
                    A[i][j] = 1;
                } else if (i == A.length - 1 && j >= numberSupport && j < numberSupport * 2) {
                    A[i][j] = 1;
                }

                //Adicionar ações dos players
                if (j < numberSupport) { // Player1
                    if (i >= sup1.length && i < sup1.length + sup2.length) {
                        A[i][j] = GetActionUtilities(2, game, j, i - sup1.length, sup1);
                    }
                } else if (j >= numberSupport && j < numberSupport * 2) {
                    if (i < sup1.length) { //Player2
                        A[i][j] = GetActionUtilities(1, game, j - numberSupport, i, sup2);
                    }
                }

                // Adiciona nas ultimas 2 colunas os b´s
                if (j == A[0].length - 2 && i < sup1.length) {
                    A[i][j] = -1;
                } else if (j == A[0].length - 1 && i >= sup1.length && i < sup1.length + sup2.length) {
                    A[i][j] = -1;
                }

            }
        }

        //resolve LP
        lp = new LinearProgram(c);
        lp.setMinProblem(true);
        for (int i = 0; i < b.length; i++) {
            if (i < sup1.length && sup1[i])
                lp.addConstraint(new LinearEqualsConstraint(A[i], b[i], "c" + i));
            else if (i >= sup1.length && i < sup1.length + sup2.length && sup2[i - sup1.length])
                lp.addConstraint(new LinearEqualsConstraint(A[i], b[i], "c" + i));
            else if (i >= sup1.length + sup2.length)
                lp.addConstraint(new LinearEqualsConstraint(A[i], b[i], "c" + i));
            else
                lp.addConstraint(new LinearSmallerThanEqualsConstraint(A[i], b[i], "c" + i));
        }
        lp.setLowerbound(lb);


    }

    public static boolean solveLP() {
        LinearProgramSolver solver = SolverFactory.newDefault();
        x = solver.solve(lp);
        return x != null;
    }

    public static void showLP() {
        System.out.println("*********** LINEAR PROGRAMMING PROBLEM ***********");
        String fs;
        if (lp.isMinProblem()) System.out.print("  minimize: ");
        else System.out.print("  maximize: ");
        double[] cf = lp.getC();
        for (int i = 0; i < cf.length; i++)
            if (cf[i] != 0) {
                fs = String.format(Locale.US, "%+7.1f", cf[i]);
                System.out.print(fs + "*x[" + i + "]");
            }
        System.out.println("");
        System.out.print("subject to: ");
        ArrayList<Constraint> lcstr = lp.getConstraints();
        double aij;
        double[] ci = null;
        String str = null;
        for (int i = 0; i < lcstr.size(); i++) {
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
            str = str + String.format(Locale.US, "%6.1f", lcstr.get(i).getRHS());
            if (i != 0) System.out.print("            ");
            for (int j = 0; j < lp.getDimension(); j++) {
                aij = ci[j];
                if (aij != 0) {
                    fs = String.format(Locale.US, "%+7.1f", aij);
                    System.out.print(fs + "*x[" + j + "]");
                } else System.out.print("            ");
            }
            System.out.println(str);
        }
    }

    public static void showSolution() {
        if (x == null) System.out.println("*********** NO SOLUTION FOUND ***********");
        else {
            System.out.println("*********** SOLUTION ***********");
            for (int i = 0; i < x.length; i++) System.out.println("x[" + i + "] = " + x[i]);
            System.out.println("f(x) = " + lp.evaluate(x));
        }
    }

//    public static void showNashEquilibrium(boolean[] p1Supp, boolean[] p2Supp) {
//        if (x == null) System.out.println("*********** NO NASH EQUILIBRIUM FOUND ***********");
//        else {
//            System.out.println("*********** NASH EQUILIBRIUM ***********");
//
//            int currAction = -1;
//            for(int i=0; i < p1Supp.length; i++) {
//                if(p1Supp[i] == true) {
//                    currAction = i;
//                    break;
//                }
//            }
//
//            for (int i = 0; i < x.length; i++)
//                if(System.out.println("x[" + i + "] = " + x[i]);
//            System.out.println("f(x) = " + lp.evaluate(x));
//        }
//    }

    private static void ShowNE(boolean[] sup1, boolean[] sup2, NormalFormGame game){

        System.out.println("Player 1 strategy: ");
        for (int i = 0; i < sup1.length; i++) {
            if(sup1[i])
                System.out.println(x[i] + " : "+ game.rowActions.get(i));
            else
                System.out.println("0,00 : "+ game.rowActions.get(i));
        }
        System.out.println("Player 2 strategy: ");
        for (int i = 0; i < sup2.length; i++) {
            if(sup2[i])
                System.out.println(x[i] + " : "+ game.colActions.get(i));
            else
                System.out.println("0,00 : "+ game.colActions.get(i));
        }
        System.out.println("f(x) = " + lp.evaluate(x));

    }

    static void CheckNegativeNumbers(NormalFormGame game){

        double[][] u1 = game.u1;
        double[][] u2 = game.u2;

        double min = Integer.MIN_VALUE;
        //check the lowest number in u1 and u2 and them sum the abs of that number to all if the number is negative
        for (int i = 0; i < u1.length; i++) {
            for (int j = 0; j < u1[0].length; j++) {
                if (u1[i][j] < min && u1[i][j] < u2[i][j]) {
                    min = u1[i][j];
                }else if (u2[i][j] < min && u2[i][j] < u1[i][j]) {
                    min = u2[i][j];
                }
            }
        }
        if(min>0)
            return;

        double absMin = Math.abs(min);
        for (int i = 0; i < u1.length; i++) {
            for (int j = 0; j < u1[0].length; j++) {
                u1[i][j] += absMin;
                u2[i][j] += absMin;
            }
        }


    }

    private static void ReceiveSupports(int numberSupport, List<boolean[]> sup1, List<boolean[]> sup2, NormalFormGame game) {

       // CheckNegativeNumbers(game);

        int numberOfNE = 0;
        for (boolean[] value : sup1) {
            for (boolean[] booleans : sup2) {
                ComputeGeneralSum(numberSupport, value, booleans, game);
                boolean solutionFound = solveLP();
                if (solutionFound) {
                    numberOfNE++;
                    System.out.println("SOLUTION FOUND");
                    showLP();
                    showSolution();
                } else {
                    System.out.println("NO SOLUTION FOUND");
                }
            }
        }

    }

    public static void ComputeGame(NormalFormGame game) {


        List<boolean[]> support, support2;
        boolean[] iRow = new boolean[game.nRow];
        Arrays.fill(iRow, true);
        boolean[] iCol = new boolean[game.nCol];
        Arrays.fill(iCol, true);

        int numberOfSupports = Math.min(game.nRow, game.nCol);

        for (int i = 1; i <= numberOfSupports; i++) {
            System.out.println("Support " + i + " + " + i);
            support = GetSubSets.getSubSets(0, i, game.nRow, iRow);
            support2 = GetSubSets.getSubSets(0, i, game.nCol, iCol);
            ReceiveSupports(i, support, support2, game);

        }

    }

    public static void main(String[] args) {
        NormalFormGame game = new NormalFormGame();
        //create a prisioner dilema game
        game.u1 = new double[][]{{3, 0, 1}, {5, 1, 0}};
        game.u2 = new double[][]{{3, 5, 6}, {0, 1, 2}};
        // add actions to the game
        game.nRow = 2;
        game.nCol = 3;
        //add actions to the game
        game.rowActions = new ArrayList<>();
        game.rowActions.add("A");
        game.rowActions.add("B");

        game.colActions = new ArrayList<>();
        game.colActions.add("C");
        game.colActions.add("D");
        game.colActions.add("E");

        ComputeGame(game);

    }
}
