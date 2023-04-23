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


public class IteratedDomination {
	static LinearProgram lp;
	static double[] x;

	public IteratedDomination() {
	}

	public static double[][] transposeMatrix(double[][] originalArray) {
		double[][] transposed = new double[originalArray.length][originalArray[0].length];
		for(int i=0;i< originalArray.length;i++){
			for(int j=0;j< originalArray[i].length;j++){
				transposed[i][j]=originalArray[j][i];
			}
		}
		return transposed;
	}

	private static double[][] makeConstraints(double[][] utilityMatrix, int colRowNum, NormalFormGame game){
		double[][] A; // constraints left si
//		if(player == 1)
			A = new double[utilityMatrix.length - 1][utilityMatrix.length];
//		else
//			A = new double[utilityMatrix.length][utilityMatrix.length-1];

		for (int i=0; i < A.length; i++) {
			for (int j=0; j < A[i].length; j++) {

				if(colRowNum != i)
					A[i][j] = utilityMatrix[i][j];
			}
		}

		return A;

	}
	
	// if P1, then colRowNum will be the row being checked
	// if P2, then colRowNum will be the column being checked
	public static void setLP(NormalFormGame game, int player, int colRowNum) {

		/**double[] c = { 1.0, 1.0 }; // Function MIN or MAX
		double[] b = new double[0]; // Independent Factors
		double[][] A = new double[0][0]; // constraints left side

		b = game.u1[colRowNum];

		A = new double[game.u1.length][game.u1.length];

		for (int i=0; i < A.length; i++) {
			for (int j=0; j < A[i].length; j++) {

				if(colRowNum != j && game.pCol[j] == true && game.pRow[i] == true)
					A[i][j] = game.u1[i][j];
			}
		}**/

		double[] c = { 1.0, 1.0 }; // Function MIN or MAX
		double[] b = new double[0]; // Independent Factors
		double[][] A = new double[0][0]; // constraints left side
		if(player == 1) {
			b = game.u1[colRowNum];    // constantes das constraints, que aqui vão ser as utilidades da linha/col checked
			A = makeConstraints(game.u1, colRowNum, game);
		}
		if(player == 2) {
			b = game.u2[colRowNum];
			// transpor game.u2
			double[][] transposeG = transposeMatrix(game.u2);
			// aplicar makeConstraints ao resultado
			A = makeConstraints(transposeG, colRowNum, game);
		}
        double[] lb = {0.0, 0.0};
		System.err.println("new LP");
		lp = new LinearProgram(c);
		lp.setMinProblem(false);
		for (int i = 0; i < A.length - 1; i++) {

			lp.addConstraint(new LinearBiggerThanEqualsConstraint(A[i], b[i], "c" + i));
		}
		lp.setLowerbound(lb);

	}

	public static boolean solveLP() {
		LinearProgramSolver solver  = SolverFactory.newDefault();  
		x = solver.solve(lp);
		if (x==null) return false;
		return true;
	}

	public static boolean CheckIfDominated(NormalFormGame game, int player, int colRowNum){
		setLP(game, player, colRowNum);
		boolean hasSolution = solveLP();

		System.err.println( "hasSolution: " + hasSolution);

		if(!hasSolution) return false;

		boolean isDominated = true;

		for (int i = 0; i < x.length; i++) {

			if(x[i] == 0)
			{
				System.err.println(" Sei la o qur " + x[i] );
				isDominated = false;
			}

		}

		System.err.println( "IsDominated: " + isDominated);

		if(isDominated){

			if(player == 1){
				game.pRow[colRowNum] = false;
			}else{
				game.pCol[colRowNum] = false;
			}

		}
		return isDominated;
	}

	public static NormalFormGame IteratedDominationGame(NormalFormGame game){


		boolean CanRemoveColRow = true;
		boolean dominated;
		int aux = 0;

		while (CanRemoveColRow && aux < 10){

			dominated = false;
			for(int i=0; i < game.nRow && !dominated; i++){
				dominated = CheckIfDominated(game, 1, i);

			}

			for(int j = 0; j < game.nCol && !dominated; j++){

				dominated = CheckIfDominated(game, 2, j);

			}

			if(!dominated)
			{
				System.err.println("Não há mais linhas/colunas dominadas");
				CanRemoveColRow = false;
			}
			aux++;

		}
		return game;
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
