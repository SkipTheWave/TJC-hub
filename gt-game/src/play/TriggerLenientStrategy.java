package play;

import gametree.GameNode;
import gametree.GameNodeDoesNotExistException;
import play.exception.InvalidStrategyException;

import java.security.SecureRandom;
import java.util.*;

/**********************************************************************************
 * This strategy involves starting out cooperating, then playing a variation of Tit for Tat.
 * This variation involves mimicking an opponent who only cooperated on the last turn,
 * and having a slightly lower chance of mimicking an opponent if they defected at least once that turn.
 * However, we are done cooperating forever if the opponent defects more than DEFECT_TOLERANCE times.
 * This variation, with these parameters, is aimed at games with high probability of 18+ iterations occurring.
 * If unlimited iterations, this means a probability of 0.97+ of continuing.
 **********************************************************************************/
public class TriggerLenientStrategy extends Strategy {

	// NOTE: this one has a better leniency/iterations ratio, because it will be used
	// in a game with specifically 20 iterations, and everyone will know them
	// conversely, it also starts perma-defecting earlier than might be expected
	private final int DEFECT_TOLERANCE = 5;
	private final double MIMIC_IF_DEFECT = 0.8;
	private final int MATCHES_TO_START_DEFECTING = 14;
	private int defectCounter = 0;
	private int matchCounter = 0;

	private List<GameNode> getReversePath(GameNode current) {		
		try {
			GameNode n = current.getAncestor();
			List<GameNode> l =  getReversePath(n);
			l.add(current);
			return l;
		} catch (GameNodeDoesNotExistException e) {
			List<GameNode> l = new ArrayList<GameNode>();
			l.add(current);
			return l;
		}
	}
	
	private void cumputeStrategy(List<GameNode> listP1, 
			List<GameNode> listP2,
			PlayStrategy myStrategy,
			SecureRandom random) throws GameNodeDoesNotExistException {

		// if the opponent has already defected too many times, or number of iterations passes a certain threshold,
		// we just defect forever
		if(defectCounter >= DEFECT_TOLERANCE || matchCounter >= MATCHES_TO_START_DEFECTING) {
			Iterator<String> moves = myStrategy.keyIterator();
			while(moves.hasNext()) {
				String k = moves.next();
				if(k.contains("Defect")) {
					myStrategy.put(k, new Double(1));
					System.err.println("Had enough of this");
				}
				else
					myStrategy.put(k, new Double(0));
			}
		}
		// if the opponent has not defected enough times yet, we play a forgiving Tit for Tat
		else {
			Set<String> oponentMoves = new HashSet<String>();

			//When we played as Player1 we are going to check what were the moves
			//of our opponent as player2.
			for (GameNode n : listP1) {
				if (n.isNature() || n.isRoot()) continue;
				if (n.getAncestor().isPlayer2()) {
					oponentMoves.add(n.getLabel());
				}
			}

			//When we played as Player2 we are going to check what were the moves
			//of our opponent as player1.
			for (GameNode n : listP2) {
				if (n.isNature() || n.isRoot()) continue;
				if (n.getAncestor().isPlayer1()) {
					oponentMoves.add(n.getLabel());
				}
			}

			//We now set our strategy to have a probability of 1.0 for the moves used
			//by our adversary in the previous round and zero for the remaining ones.
			//However, if opponent defects, our probability of mimicking is a bit lower than 1.0
			Iterator<String> moves = myStrategy.keyIterator();
			double probMimicked = 1.0;
			if (oponentMoves.contains("1:1:Defect") || oponentMoves.contains("2:1:Defect")) {
				probMimicked = MIMIC_IF_DEFECT;
			}
			double probOthers = 1 - probMimicked;
			while (moves.hasNext()) {
				String k = moves.next();
				System.err.println("Defect counter: " + defectCounter);
				if (oponentMoves.contains(k)) {
					if(k.contains("Defect")) {
						defectCounter++;
					}
					myStrategy.put(k, new Double(probMimicked));
					System.err.println(k + " played with probability " + probMimicked);
				} else {
					myStrategy.put(k, new Double(probOthers));
					System.err.println(k + " played with probability " + probOthers);
				}
			}
		}
		
		//The following piece of code has the goal of checking if there was a portion
		//of the game for which we could not infer the moves of the adversary (because
		//none of the games in the previous round pass through those paths)
		Iterator<Integer> validationSetIte = tree.getValidationSet().iterator();
		Iterator<String> moves = myStrategy.keyIterator();
		while(validationSetIte.hasNext()) {
			int possibleMoves = validationSetIte.next().intValue();
			String[] labels = new String[possibleMoves];
			double[] values = new double[possibleMoves];
			double sum = 0;
			for (int i = 0; i < possibleMoves; i++) {
				labels[i] = moves.next();
				values[i] = ((Double) myStrategy.get(labels[i])).doubleValue();
				sum += values[i];
			}
			if (sum != 1) { //In the previous game we could not infer what the adversary played here
				//Random move on this validation set
				sum = 0;
				for (int i = 0; i < values.length - 1; i++) {
					values[i] = random.nextDouble();
					while (sum + values[i] >= 1) values[i] = random.nextDouble();
					sum = sum + values[i];
				}
				values[values.length - 1] = ((double) 1) - sum;

				for (int i = 0; i < possibleMoves; i++) {
					myStrategy.put(labels[i], values[i]);
					System.err.println("Unexplored path: Setting " + labels[i] + " to prob " + values[i]);
				}
			}
		}
	}
	

	@Override
	public void execute() throws InterruptedException {

		SecureRandom random = new SecureRandom();

		while(!this.isTreeKnown()) {
			System.err.println("Waiting for game tree to become available.");
			Thread.sleep(1000);
		}

		GameNode finalP1 = null;
		GameNode finalP2 = null;
				
		while(true) {

			PlayStrategy myStrategy = this.getStrategyRequest();
			if(myStrategy == null) //Game was terminated by an outside event
				break;	
			boolean playComplete = false;
			matchCounter++;
			
			while(! playComplete ) {
				if(myStrategy.getFinalP1Node() != -1) {
					finalP1 = this.tree.getNodeByIndex(myStrategy.getFinalP1Node());
					if(finalP1 != null)
						System.out.println("Terminal node in last round as P1: " + finalP1);
				}

				if(myStrategy.getFinalP2Node() != -1) {
					finalP2 = this.tree.getNodeByIndex(myStrategy.getFinalP2Node());
					if(finalP2 != null)
						System.out.println("Terminal node in last round as P2: " + finalP2);
				}

				Iterator<Integer> iterator = tree.getValidationSet().iterator();
				Iterator<String> keys = myStrategy.keyIterator();

				if(finalP1 == null || finalP2 == null) {
					//This is the first round, so we choose 1st action (in PD, cooperate)
					while(iterator.hasNext()) {
						double[] moves = new double[iterator.next()];
						double sum = 0;
						for(int i = 0; i < moves.length - 1; i++) {
//							moves[i] = random.nextDouble();
//							while(sum + moves[i] >= 1) moves[i] = random.nextDouble();
//							sum = sum + moves[i];
							moves[i] = 0;
						}
						moves[0] = 1;

						for(int i = 0; i < moves.length; i++) {
							if(!keys.hasNext()) {
								System.err.println("PANIC: Strategy structure does not match the game.");
								return;
							}
							myStrategy.put(keys.next(), moves[i]);
						}
					}
				} else {
					//Lets mimic our adversary strategy (at least what we can infer)
					List<GameNode> listP1 = getReversePath(finalP1);
					List<GameNode> listP2 = getReversePath(finalP2);
					
					try { cumputeStrategy(listP1, listP2, myStrategy, random); }
					catch( GameNodeDoesNotExistException e ) {
						System.err.println("PANIC: Strategy structure does not match the game.");
					}
				}

				try{
					this.provideStrategy(myStrategy);
					playComplete = true;
				} catch (InvalidStrategyException e) {
					System.err.println("Invalid strategy: " + e.getMessage());;
					e.printStackTrace(System.err);
				} 
			}
		}

	}
}
