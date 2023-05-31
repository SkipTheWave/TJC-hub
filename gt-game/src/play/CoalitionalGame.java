package play;

import scpsolver.constraints.Constraint;
import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.constraints.LinearEqualsConstraint;
import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LinearProgram;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class CoalitionalGame {
    public double[] v;

    private HashMap<String, Double> valueMap;
    public int nPlayers;
    public String[] ids;

    public double shapley = 0;

    static LinearProgram lp;

    static double[] x;

    public ArrayList<Double> shapleyValue = new ArrayList<>();

    public CoalitionalGame(double[] v) {
        this.v = v;
        this.nPlayers = (int) (Math.log(v.length) / Math.log(2));
        setPlayersID();
        valueMap = new HashMap<>();
        for (int i = 0; i < v.length; i++) {
            valueMap.put(getSet(i).toString(), v[i]);
        }
    }

    public void setPlayersID() {
        int c = 64;
        ids = new String[nPlayers];
        for (int i = nPlayers - 1; i >= 0; i--) {
            c++;
            ids[i] = (String.valueOf((char) c));
        }
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

    public void showGame() {
        System.out.println("*********** Coalitional Game ***********");
        for (int i = 0; i < v.length; i++) {
            showSet(i);
            System.out.println(" (" + v[i] + ")");
        }
    }

    public boolean isShapleyInCore() {
        for (int i = 0; i < v.length; i++) {
            ArrayList<Integer> set = getSet(i);
            double sV = 0; // value of individual value
            for (int j : set) {
                sV += shapleyValue.get(j);
            }
            if (sV < v[i]) {
                System.out.println(sV + " < " + v[i] + " on set " + set);
                System.out.println("Shapley value is not in core");
                return false;
            }

        }
        return true;
    }

    public void showSet(long v) {
        boolean showPlayerID = true;
        //boolean showPlayerID = false;
        int power;
        System.out.print("{");
        int cnt = 0;
        for (int i = 0; i < nPlayers; i++) {
            power = nPlayers - (i + 1);
            if (showPlayerID) {
                if (inSet(i, v)) {
                    if (cnt > 0) System.out.print(",");
                    cnt++;
                    System.out.print(ids[power]);
                }
            } else {
                if (cnt > 0) System.out.print(",");
                cnt++;
                if (inSet(i, v)) System.out.print(1);
                else System.out.print(0);
            }
        }
        System.out.print("}");
    }

    public boolean inSet(int i, long v) {
        int power;
        long vi;
        long div;
        long mod;
        power = nPlayers - (i + 1);
        vi = (long) Math.pow(2, power);
        div = v / vi;
        mod = div % 2;
        return (mod == 1);
    }

    public ArrayList<Integer> getSet(long v) {
        ArrayList<Integer> players = new ArrayList<>();
        int power;
        long vi;
        long div;
        long mod;
        for (int i = 0; i < nPlayers; i++) {
            power = nPlayers - (i + 1);
            vi = (long) Math.pow(2, power);
            div = v / vi;
            mod = div % 2;
            if (mod == 1) players.add(power);
        }
        return players;
    }

    public double getValue(ArrayList<Integer> set) {

//		for(int i=0;i<v.length;i++) {
//			//System.out.println(getSet(i).toString());
//			if (set.toString().equals(getSet(i).toString())) return v[i];
//		}
//
//		return 0.0;

        return valueMap.getOrDefault(set.toString(), 0.0);
    }

    public void permutation(int j, int k, int iZero, long v0, CoalitionalGame coalitional, double[] factorialN) {
        long value = 0;

        if (k == 0) {
            //showSet(v0);
            shapley += GainsInSubSet(getSet(v0), coalitional, nPlayers - 1 - iZero, factorialN);

        } else {
            int op;
            if (iZero < j) op = nPlayers - j;
            else op = nPlayers - j - 1;
            if (op == k) {
                for (int i = j; i < nPlayers; i++) {
                    if (i != iZero) value += (long) Math.pow(2, nPlayers - (i + 1));
                }
                v0 = v0 + value;
                //showSet(v0);
                shapley += GainsInSubSet(getSet(v0), coalitional, nPlayers - 1 - iZero, factorialN);
                //System.out.println("\nShare of " + coalitional.ids[nPlayers - 1 - iZero] + " = " + shapleyValue);
            } else {
                if (j != iZero)
                    permutation(j + 1, k - 1, iZero, v0 + (long) Math.pow(2, nPlayers - (j + 1)), coalitional, factorialN);
                permutation(j + 1, k, iZero, v0, coalitional, factorialN);
            }
        }
    }

    public static double[] calculateFactorials(int n) {
        double[] factorialN = new double[n + 1];
        for (int i = 0; i <= n; i++) {
            long factorial = calculateFactorial(i);
            factorialN[i] = factorial;
            System.out.println(i + "! = " + factorial);
        }
        return factorialN;
    }

    public static long calculateFactorial(int num) {
        long factorial = 1;
        if (num == 0) return factorial;
        else {
            for (int i = 1; i <= num; i++) {
                factorial *= i;
            }
        }

        return factorial;
    }

    public double GainsInSubSet(ArrayList<Integer> subSet, CoalitionalGame coalitional, int j, double[] factorialN) {

        int s = subSet.size();
        int nPlayersComplement = nPlayers - s - 1;
        double sFactorial = factorialN[s];
        double nFactorial = factorialN[nPlayers];
        double complementFactorial = factorialN[nPlayersComplement];

        double valueWithoutPlayer = coalitional.getValue(subSet);
        subSet.add(j);
        Collections.sort(subSet, Collections.reverseOrder());
        double valueWithPlayer = coalitional.getValue(subSet);
        subSet.remove(Integer.valueOf(j));
        Collections.sort(subSet, Collections.reverseOrder());
        return sFactorial * complementFactorial * (valueWithPlayer - valueWithoutPlayer) / nFactorial;
    }

    /*public boolean isCoreEmpty() {
        int nElements = ids.length;//(int) (Math.log(v.length) / Math.log(2));
        double[] c = new double[nElements];
        Arrays.fill(c, 0.0);
        double[] b = v;
        double[][] A = new double[v.length][nElements];

        for (int i=0; i<v.length; i++) {
            ArrayList<Integer> set = getSet(i);		// integer of each player in set
            for(int j=0; j<set.size(); j++) {
                A[i][set.get(j)] = 1.0;
            }
        }
        double[] lb = c;

        lp = new LinearProgram(c);
        lp.setMinProblem(true);
        for(int i =0; i< b.length; i++){
            if(i == b.length -1){
                lp.addConstraint(new LinearEqualsConstraint(A[i], b[i], "c" + i));
            }else{
                lp.addConstraint(new LinearBiggerThanEqualsConstraint(A[i], b[i], "c" + i));
            }
        }
        lp.setLowerbound(lb);
        //showLP();
        return solveLP();
    }*/
    public boolean isCoreEmpty() {
        int nElements = ids.length;
        double[] c = new double[nElements];
        Arrays.fill(c, 0.0);
        double[] b = v;
        double[][] A = new double[v.length][nElements];

        for (int i = 0; i < v.length; i++) {
            ArrayList<Integer> set = getSet(i);
            for (int j : set) {
                A[i][j] = 1.0;
            }
        }

        lp = new LinearProgram(c);
        lp.setMinProblem(true);
        for (int i = 0; i < b.length; i++) {
            if (i == b.length - 1) {
                lp.addConstraint(new LinearEqualsConstraint(A[i], b[i], "c" + i));
            } else {
                lp.addConstraint(new LinearBiggerThanEqualsConstraint(A[i], b[i], "c" + i));
            }
        }
        lp.setLowerbound(c);
        return solveLP();
    }


    public static void main(String[] args) throws FileNotFoundException {
        double[] v1 = {0.0, 0.0, 3.0, 8.0, 2.0, 7.0, 5.0, 10.0, 0.0, 0.0, 4.0, 9.0, 3.0, 8.0, 6.0, 11.0};
        int pCounter = 0;
        int pWCounter = 1;
        int nLines = 0;

        try {

            String pathname = "C:\\Users\\pedro\\IdeaProjects\\TJC-hub\\gt-game\\quiz4_examples\\C7.txt";
            Scanner file = new Scanner(new File(pathname));


            while (file.hasNextLine()) {
                nLines++;
                file.nextLine();
            }

            // Normal Coalition Games - uncomment this block
            double[] v2 = new double[nLines];
            file = new Scanner(new File(pathname));


            while (file.hasNextLine()) {
                v2[pCounter++] = Double.parseDouble(file.nextLine());

            }

            // VOTING GAMES - uncomment this block

            int n = v2.length - 2;
            double quota = v2[n];
            double spending = v2[n + 1];
            double[] v = new double[1 << n];

            // Calculate the worth of each coalition
            for (int i = 0; i < v.length; i++) {
                double sum = 0;
                for (int j = 0; j < n; j++) {
                    if ((i & (1 << j)) != 0) {
                        sum += v2[v2.length - j - 3];
                    }
                }
                v[i] = (sum >= quota) ? spending : 0;
            }

            // Weighted Graph Games - uncomment this block
//            int n = nLines +1;
//            // create a matrix with the worth of each coalition
//            double[][] v = new double[n][n];
//
//            file = new Scanner(new File(pathname));
//
//            while (file.hasNextLine()) {
//                String[] line = file.nextLine().split(" ");
//                for (int i = 0; i < line.length; i++) {
//                    v[pWCounter][i] = Double.parseDouble(line[i]);
//                    v[i][pWCounter] = v[pWCounter][i];
//                }
//                pWCounter++;
//
//            }
//
//            double[] v2 = new double[1 << n];
//
//
//            for (int i = 0; i < v2.length; i++) {
//                for (int j = 0; j < n; j++) {
//                    if ((i & (1 << j)) != 0) {
//                        // calculate the worth of each coalition
//                        for (int k = j + 1; k < n; k++) {
//                            if ((i & (1 << k)) != 0) {
//                                v2[i] += v[j][k];
//                            }
//                        }
//                    }
//                }
//            }

            CoalitionalGame c = new CoalitionalGame(v2);// for coalitional games and weighted graph games
            //CoalitionalGame c = new CoalitionalGame(v); // for voting games
            //CoalitionalGame c = new CoalitionalGame(v1);
            double[] factorialN = calculateFactorials(c.nPlayers);

            //c.showGame();
            for (int j = 0; j < c.nPlayers; j++) {
                //System.out.println("*********** Permutations without player " + c.ids[c.nPlayers - 1 - j] + " ***********");
                c.shapley = 0;
                for (int i = 0; i < c.nPlayers; i++) {
                    //System.out.print("With " + i + " players: ");
                    c.permutation(0, i, j, 0, c, factorialN);
                    //System.out.println();
                }

                double roundShapley = Math.round(c.shapley * 100.0) / 100.0;
                System.out.println("\nShare of " + c.ids[c.nPlayers - 1 - j] + " = " + roundShapley);
                c.shapleyValue.add(roundShapley);

            }
            Collections.reverse(c.shapleyValue);
            boolean inCore = c.isShapleyInCore();
            if (!inCore) {
                boolean sol = c.isCoreEmpty();
                if (sol) {
                    //showSolution();
                    System.out.println("Possible core solution: ");
                    for (int i = 0; i < c.nPlayers; i++) {
                        System.out.println("Player " + c.ids[i] + " = " + x[i]);
                    }
                } else {
                    System.out.println("\nThe core is empty");
                }
            } else {
                System.out.println("\nThe Shapley value is in the core");
                System.out.println("Possible core solution: ");
                for (int i = 0; i < c.nPlayers; i++) {
                    System.out.println("Player " + c.ids[c.ids.length - i - 1] + " = " + c.shapleyValue.get(c.ids.length - i - 1));
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
