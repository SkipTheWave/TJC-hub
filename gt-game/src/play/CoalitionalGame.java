package lp;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CoalitionalGame {
	public double[] v; 
	public int nPlayers;
	public String[] ids;
	public double[] factorialN;
	
	public CoalitionalGame(double[] v) {  
		this.v=v; 
		this.nPlayers = (int)(Math.log(v.length) / Math.log(2));
		setPlayersID();
	}
	
	public void setPlayersID() {  
		int c = 64;
		ids= new String[nPlayers];
		for (int i=nPlayers-1;i>=0;i--) {
			c++;
			ids[i] = (String.valueOf((char)c));
		}
	}
	
	public void showGame() {
		System.out.println("*********** Coalitional Game ***********");
		for (int i=0;i<v.length;i++) {
			showSet(i); 
			System.out.println(" ("+v[i]+")");
		}
	}
	
	public void showSet(long v) {
		boolean showPlayerID = true;
		//boolean showPlayerID = false;
		int power;
		System.out.print("{");
		int cnt = 0;
		for(int i=0;i<nPlayers;i++) {
			power = nPlayers - (i+1);
			if (showPlayerID) {
				if (inSet(i, v)) {
					if (cnt>0) System.out.print(",");
					cnt++;
					System.out.print(ids[power]);
				}
			}
			else {
				if (cnt>0) System.out.print(",");
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
		power = nPlayers - (i+1);
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
		for(int i=0;i<nPlayers;i++) {
			power = nPlayers - (i+1);
			vi = (long) Math.pow(2, power);
			div = v / vi;
			mod = div % 2;
			if (mod == 1) players.add(power);
		}
		return players;
	}
	
	public void permutation(int j, int k, int iZero, long v0) {
		long value = 0;
		if (k==0) {
			showSet(v0);
		}
		else {
			int op = 0;
			if (iZero < j) op = nPlayers - j;
			else op = nPlayers - j - 1;
			if (op==k) {
				for(int i=j;i<nPlayers;i++) {		
					if (i != iZero) value += (long) Math.pow(2, nPlayers-(i+1));
				}
				v0 = v0 + value;
				showSet(v0);
			}
			else {	
				if (j != iZero) permutation(j+1,k-1,iZero,v0+(long) Math.pow(2, nPlayers-(j+1)));
				permutation(j+1,k,iZero,v0);
			}
		}
	}

	public void calculateFactorials(int n) {
		factorialN = new double[n];
		for (int i = 1; i <= n; i++) {
			long factorial = calculateFactorial(i);
			factorialN[i] = factorial;
			System.out.println(i + "! = " + factorial);
		}
	}

	public long calculateFactorial(int num) {
		long factorial = 1;
		for (int i = 1; i <= num; i++) {
			factorial *= i;
		}
		return factorial;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		double[] v1 = {0.0, 0.0, 3.0, 8.0, 2.0, 7.0, 5.0, 10.0, 0.0, 0.0, 4.0, 9.0, 3.0, 8.0, 6.0, 11.0};
		int pCounter = 0;
		int nLines = 0;

		try {
			String pathname = "C:\\Users\\Skip\\Desktop\\The Warehouse\\UNI\\TJC\\Labs\\gt-game\\quiz4_examples\\EC1.txt";
			Scanner file = new Scanner(new File(pathname));
			Scanner fileAux = new Scanner(new File(pathname));

			while (fileAux.hasNextLine()) {
				nLines++;
				fileAux.nextLine();
			}
			double[] v2 = new double[nLines];

			while (file.hasNextLine()) {
				v2[pCounter++] = file.nextDouble();
				file.nextLine();
			}
			file.close();
			fileAux.close();

			System.out.println(Arrays.toString(v2));

			CoalitionalGame c = new CoalitionalGame(v2);
			c.showGame();
			for (int j = 0; j < c.nPlayers; j++) {
				System.out.println("*********** Permutations without player " + c.ids[c.nPlayers - 1 - j] + " ***********");
				for (int i = 0; i < c.nPlayers; i++) {
					System.out.print("With " + i + " players: ");
					c.permutation(0, i, j, 0);
					System.out.println();
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}



}
