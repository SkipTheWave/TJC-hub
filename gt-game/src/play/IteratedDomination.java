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


public class IteratedDomination {
    static LinearProgram lp;
    static double[] x;

    public IteratedDomination() {
    }

    //Given a matrix, checks if there are negative numbers, if there are, sum the inverse to all numbers in the same row and column
    private static double checkNegativeNumbers(double[][] matrix) {
        System.err.println(" Verificar se tem negativos");
        double min = Integer.MAX_VALUE;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {

                if (matrix[i][j] < min)
                    min = matrix[i][j];
            }
        }
        System.err.println("min: " + min);

        return min;
    }

    private static double[][] addAbsToA(double[][] originalMatrix, double min) {

		double[][] matrix = originalMatrix.clone();

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] += Math.abs(min);
            }
        }
		System.err.println("matrix: " + Arrays.deepToString(matrix));

        return matrix;
    }

    private static double[] addAbsToB(double[] originalArray, double min) {
		double[] b = originalArray.clone();
        for (int i = 0; i < b.length; i++) {
            b[i] += Math.abs(min);
        }
		System.err.println("b: " + Arrays.toString(b));
        return b;
    }

    public static double[][] transposeMatrix(double[][] originalArray) {

        //given a matrix, compute the transpose

        double[][] transposed = new double[originalArray[0].length][originalArray.length];
        for (int i = 0; i < originalArray.length; i++) {
            for (int j = 0; j < originalArray[0].length; j++) {
                transposed[j][i] = originalArray[i][j];
            }
        }

        return transposed;
    }

    private static double[][] makeConstraints(double[][] utilityMatrix, int colRowNum, NormalFormGame game) {
        double[][] A; // constraints left si
//		if(player == 1)
        A = new double[utilityMatrix.length - 1][utilityMatrix.length];
//		else
//			A = new double[utilityMatrix.length][utilityMatrix.length-1];


        int aux1 = 0;

        for (int i = 0; i <= A.length; i++) {
            int aux2 = 0;
            for (int j = 0; j < game.nCol; j++) {

                if (colRowNum != i && game.pCol[j] == true && game.pRow[i] == true) {
                    A[aux1][aux2] = utilityMatrix[i][j];
                    aux2++;
                }
            }
            if (colRowNum != i && game.pRow[i] == true) aux1++;
        }

        return A;

    }

    // if P1, then colRowNum will be the row being checked
    // if P2, then colRowNum will be the column being checked
    public static void setLP(NormalFormGame game, int player, int colRowNum) {
        double[] c;
        int count = 0;
        if (player == 1) {
            for (int a = 0; a < game.pRow.length; a++) {
                if (game.pRow[a])
                    count++;
            }
        } else {
            for (int a = 0; a < game.pCol.length; a++) {
                if (game.pCol[a])
                    count++;
            }
        }

        c = new double[count - 1];

        for (int i = 0; i < c.length; i++)
            c[i] = 1.0;

        double[] b = new double[0]; // Independent Factors
        double[][] A = new double[0][0]; // constraints left side
        if (player == 1) {
            b = game.u1[colRowNum];    // constantes das constraints, que aqui vão ser as utilidades da linha/col checked
            A = makeConstraints(game.u1, colRowNum, game);
        }
        if (player == 2) {
            System.err.println("player 2.---------------");
            b = game.u2[colRowNum];
            // transpor game.u2
            double[][] transposeG = transposeMatrix(game.u2);
            // aplicar makeConstraints ao resultado
            A = makeConstraints(transposeG, colRowNum, game);
        }
        double min = checkNegativeNumbers(A);
        if (min < 0) {
            A = addAbsToA(A, min);
            b = addAbsToB(b, min);
        }
        A = transposeMatrix(A);
        double[] lb = {0.0, 0.0};
        lp = new LinearProgram(c);
        lp.setMinProblem(true);
        System.err.println(Arrays.deepToString(A));
        for (int i = 0; i < A.length; i++) {

            lp.addConstraint(new LinearBiggerThanEqualsConstraint(A[i], b[i], "c" + i));
        }
        lp.setLowerbound(lb);
    }

    public static boolean solveLP() {
        LinearProgramSolver solver = SolverFactory.newDefault();
        x = solver.solve(lp);
        if (x == null) return false;
        return true;
    }

    public static boolean CheckIfDominated(NormalFormGame game, int player, int colRowNum) {
        setLP(game, player, colRowNum);
        if (lp.getC().length == 0)
            return false;
        showLP();
        boolean hasSolution = solveLP();
        showSolution();

        System.err.println("hasSolution: " + hasSolution);

        if (!hasSolution) return false;

        boolean isDominated = true;

        double sum = 0;

        for (int i = 0; i < x.length; i++) {

            sum += x[i];

        }
        if (sum >= 1) {
            isDominated = false;
        }
        System.err.println("IsDominated: " + isDominated);

        if (isDominated) {
            System.err.println("Chegou aqui");
            if (player == 1) {
                game.pRow[colRowNum] = false;
            } else {
                game.pCol[colRowNum] = false;
            }

        }
        return isDominated;
    }

    public static NormalFormGame IteratedDominationGame(NormalFormGame game) {


        boolean CanRemoveColRow = true;
        boolean dominated;

        while (CanRemoveColRow) {

            dominated = false;
            for (int i = 0; i < game.nRow && !dominated; i++) {
                dominated = CheckIfDominated(game, 1, i);

            }

            for (int j = 0; j < game.nCol && !dominated; j++) {

                dominated = CheckIfDominated(game, 2, j);

            }

            if (!dominated) {
                System.err.println("Não há mais linhas/colunas dominadas");
                CanRemoveColRow = false;
            }

        }
        return game;
    }

    public static void showSolution() {
        if (x == null) System.out.println("*********** NO SOLUTION FOUND ***********");
        else {
            System.out.println("*********** SOLUTION ***********");
            for (int i = 0; i < x.length; i++) System.out.println("x[" + i + "] = " + x[i]);
            System.out.println("f(x) = " + lp.evaluate(x));
        }
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


    public static void main(String[] args) {
        //setLP1();
        showLP();
        solveLP();
        showSolution();

    }

}
