package play;

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

    static double CheckNegativeNumbers(NormalFormGame game, int rowCol, int player) {
        double min = Integer.MAX_VALUE;

        if(player == 1) {
            double[][] u1 = game.u1;
            //check the lowest number in u1 and u2 and them sum the abs of that number to all if the number is negative
            for (int i = 0; i < u1.length; i++) {
                for (int j = 0; j < u1[0].length; j++) {
                    if (i != rowCol && u1[i][j] < min && game.pRow[i] && game.pCol[j])
                        min = u1[i][j];

                }
            }
        }else{
            double[][] u2 = game.u2;
            //check the lowest number in u1 and u2 and them sum the abs of that number to all if the number is negative
            for (int i = 0; i < u2.length; i++) {
                for (int j = 0; j < u2[0].length; j++) {
                    if (j != rowCol && u2[i][j] < min && game.pRow[i] && game.pCol[j])
                        min = u2[i][j];

                }
            }
        }
        if (min > 0)
            return 0;
        else
            return Math.abs(min);


    }

    private static double[][] addAbsToA(double[][] originalMatrix, double min) {

        double[][] matrix = originalMatrix.clone();

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] += Math.abs(min);
            }
        }

        return matrix;
    }

    private static double[] addAbsToB(double[] originalArray, double min) {
        double[] b = originalArray.clone();
        for (int i = 0; i < b.length; i++) {
            b[i] += Math.abs(min);
        }
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

    private static double[][] makeConstraintsCols(double[][] utilityMatrix, int colRowNum, NormalFormGame game, double min) {
        double[][] A;
        int lineCount = 0;
        int colCount = 0;
        int currentARow = 0;

        for (int i = 0; i < utilityMatrix.length; i++) {
            if (game.pRow[i])
                lineCount++;
        }
        for (int i = 0; i < utilityMatrix[0].length; i++) {
            if (game.pCol[i])
                colCount++;
        }

        A = new double[lineCount][colCount - 1];

        int aux1 = 0;//TODO
        for (int i = 0; i < utilityMatrix.length; i++) {
            int aux2 = 0;
            for (int j = 0; j < utilityMatrix[0].length; j++) {
                if (game.pCol[i] && game.pRow[j] && colRowNum != currentARow) {
                    A[aux2][aux1] = utilityMatrix[i][j] + min;
                    aux2++;
                }
            }
            if (game.pCol[i] && colRowNum != currentARow) aux1++;
            if (game.pCol[i]) currentARow++;
        }

        return A;

    }

    private static double[][] makeConstraints(double[][] utilityMatrix, int colRowNum, NormalFormGame game, double min) {
        double[][] A;
        int lineCount = 0;
        int colCount = 0;
        int currentARow = 0;

        for (int i = 0; i < utilityMatrix.length; i++) {
            if (game.pRow[i])
                lineCount++;
        }
        for (int i = 0; i < utilityMatrix[0].length; i++) {
            if (game.pCol[i])
                colCount++;
        }

        A = new double[colCount][lineCount - 1];

        int aux1 = 0;//TODO
        for (int i = 0; i < utilityMatrix.length; i++) {
            int aux2 = 0;
            for (int j = 0; j < utilityMatrix[0].length; j++) {
                if (game.pCol[j] && game.pRow[i] && colRowNum != currentARow) {
                    A[aux2][aux1] = utilityMatrix[i][j] + min;
                    aux2++;
                }
            }
            if (game.pRow[i] && colRowNum != currentARow) aux1++;
            if (game.pRow[i]) currentARow++;
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

        Arrays.fill(c, 1.0);

        double[] b = new double[0]; // Independent Factors
        double[][] A = new double[0][0]; // constraints left side
        double min = CheckNegativeNumbers(game, colRowNum, player);
        if (player == 1) {
            //b = game.u1[colRowNum];    // constantes das constraints, que aqui vão ser as utilidades da linha/col checked
            int counter = 0;
            for(int j = 0; j < game.u1[0].length; j++){
                if(game.pCol[j]){
                    counter++;
                }
            }
            b = new double[counter];
            int currentGameRow = 0;
            for (int i = 0; i < game.u1.length; i++) {

                if (game.pRow[i]) {
                    if (currentGameRow == colRowNum) {
                        int counter2 = 0;
                        for(int j = 0; j < game.u1[0].length; j++){
                            if(game.pCol[j]){
                                b[counter2] = game.u1[i][j];
                                counter2++;
                            }
                        }
                        break;
                    }
                    currentGameRow++;
                }

            }
            A = makeConstraints(game.u1, colRowNum, game, min);
        }
        if (player == 2) {
            int counter = 0;
            for(int j = 0; j < game.u2.length; j++){
                if(game.pRow[j]){
                    counter++;
                }
            }
            b = new double[counter];
            //b = game.u2[colRowNum];
            int currentGameCol = 0;
            for (int i = 0; i < game.u2[0].length; i++) {

                if (game.pCol[i]) {
                    if (currentGameCol == colRowNum) {
                        int counter2 = 0;
                        for(int j = 0; j < game.u2.length; j++){
                            if(game.pRow[j]){
                                b[counter2] = game.u2[j][i];
                                counter2++;
                            }
                        }
                        break;
                    }
                    currentGameCol++;
                }
            }

            // transpor game.u2
            double[][] transposeG = transposeMatrix(game.u2);

            // aplicar makeConstraints ao resultado
            A = makeConstraintsCols(transposeG, colRowNum, game, min);
            int a = 10;
        }

        //System.err.println("Original matrix: " + Arrays.deepToString(A));
        //A = transposeMatrix(A);
        double[] lb = new double[count - 1];
        lp = new LinearProgram(c);
        lp.setMinProblem(true);
//        System.err.println("matrix: " + Arrays.deepToString(A));
//        System.err.println("Lower bound: " + Arrays.toString(lb));
//        System.err.println("b: " + Arrays.toString(b));
//        System.err.println("c: " + Arrays.toString(c));
        for (int i = 0; i < A.length; i++) {

            lp.addConstraint(new LinearBiggerThanEqualsConstraint(A[i], b[i]+ min, "c" + i));
        }
        lp.setLowerbound(lb);
    }

    public static boolean solveLP() {
        LinearProgramSolver solver = SolverFactory.newDefault();
        x = solver.solve(lp);
        return x != null;
    }

    public static boolean CheckIfDominated(NormalFormGame game, int player, int colRowNum) {
        setLP(game, player, colRowNum);
        if (lp.getC().length == 0)
            return false;
        showLP();
        boolean hasSolution = solveLP();
        showSolution();

        //System.err.println("hasSolution: " + hasSolution);

        if (!hasSolution) return false;

        boolean isDominated = true;

        double sum = 0;

        for (double v : x) {

            sum += v;

        }
        if (sum >= 1) {
            isDominated = false;
        }
        //System.err.println("IsDominated: " + isDominated);

        if (isDominated) {
            //System.err.println("Chegou aqui");
            if (player == 1) {
                //game.pRow[colRowNum] = false;
                int currentGameRow = 0;
                for (int i = 0; i < game.u1.length; i++) {

                    if (game.pRow[i]) {
                        if (currentGameRow == colRowNum) {
                            game.pRow[i] = false;
                            break;
                        }
                        currentGameRow++;
                    }

                }
            } else {
                int currentGameCol = 0;
                for(int i = 0; i<game.u2[0].length; i++){

                    if(game.pCol[i]){
                        if(currentGameCol == colRowNum){
                            game.pCol[i] = false;
                            break;
                        }
                        currentGameCol++;
                    }
                }
            }

        }
        return isDominated;
    }

    public static NormalFormGame IteratedDominationGame(NormalFormGame game) {

        boolean CanRemoveColRow = true;
        boolean dominated;
        int loopCount = 0;
        int activeRows=0;
        int activeCols=0;

        while (CanRemoveColRow && loopCount < 10) {
            activeRows=0;
            activeCols=0;
            dominated = false;
            for (int i = 0; i < game.nRow; i++) {
                if(game.pRow[i]) activeRows++;
            }
            for (int i = 0; i < game.nRow && !dominated; i++) {

                if(activeRows > i)dominated = CheckIfDominated(game, 1, i);


            }

            for (int i = 0; i < game.nCol; i++) {
                if(game.pCol[i]) activeCols++;
            }
            for (int j = 0; j < game.nCol && !dominated; j++) {

                if(activeCols > j)dominated = CheckIfDominated(game, 2, j);
            }

            if (!dominated) {
                //System.err.println("Não há mais linhas/colunas dominadas");
                CanRemoveColRow = false;
            }
            loopCount++;
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
//        showLP();
//        solveLP();
//        showSolution();

        //create a game with this matrix for utility of player 1
        //int[][] A = {{-2, -1, -3, 3, 4}, {0, 4, 0, -1, 2}, {2, -1, 2, 2, -1}, {-1, -2, -3, 1, 0}, {-1, 1, -1, -3, 1}};
        //int[][] A = {{9,2},{0,10}};
        int[][] A = {{2,6,-1,3},{2,-4,5,5}, {0,-1,0,2}, {-2,-2,0,-2}};
        //create a game with this matrix for utility of player 2
        //int[][] B = {{0, -4, 1, 0, -1}, {2, -1, 4, -1, 1}, {3, 0, 3, 2, 1}, {-1, 3, 1, 4, 0}, {4, 1, 4, 0, 6}};
        //int[][] B = {{10,12},{10,2}};
        int[][] B = {{-4,6,-1,-3},{-5,-4,-2,5},{6,1,0,2}, {-6,2,1,2}};
        //create labels for the strategies of player 1
        //String[] labelsRow = {"A", "C", "B", "E", "D"};
//        String[] labelsRow ={"1","2"};
        String[] labelsRow ={"1","2","3","4"};
        //create labels for the strategies of player 2
        //String[] labelsCol = {"Z", "Y", "X", "W", "V"};
        //String[] labelsCol = {"1","2"};
        String[] labelsCol ={"4","3","2","1"};
        //create a game with the matrixes A and B and the labels for the strategies
        NormalFormGame game = new NormalFormGame(A, B, labelsRow, labelsCol);

        game = IteratedDominationGame(game);

        game.showGame();

    }

}
