package it.unibs.opt.alg.etb;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TabuSearch {
	
	private static final int MIN_EXAM_INDEX = 140;
	private static final int MAX_ITERATIONS = 20;
	//private static final int RANDOM_ITERATIONS = 5;
	private static final int TABU_TENURE = 4;
	private static final int start_randomExam = 100;
	private static final int randomExams = 80;
	
	Model model;
	Solution bestSolution;
	int[] sortedExams;
	int [] ne1e2;
	int E;
	int T;
	private String solutionPath;
	Instant startTime;
	//exam variabile per non ricominciare dall'esame 1 ogni volta che cambio Neighborhood, ma continuo
	int examIndex;
	int numIteration = 0;
	Map<String, Integer> tenure = new HashMap<>();
	
	public TabuSearch(int[] sortedExams, int [] ne1e2, int E, int T, String solutionPath, Instant startTime) {
		this.model = new Model();
		this.bestSolution = model.getSolution();
		this.sortedExams = sortedExams;
		this.ne1e2 = ne1e2;
		this.E = E;
		this.T = T;
		this.solutionPath = solutionPath;
		this.startTime = startTime;
		
		//examIndex = (int) (start_randomExam + Math.round(Math.random()*randomExams));
	}
	
	public void startTabuSearch() {
		randomMove();
		move();
		System.out.println("\n\nTempo impiegato dal Tabu Search: " + Duration.between(startTime, Instant.now()).getSeconds() + " secondi");
		//model.exportSolution();
		bestSolution.writeSolutionOnFile(solutionPath + "_tabu_search.sol");
	}
	
	private void move() {
		int max_iter_last_improvement=10;
		//stop criterion
		if(max_iter_last_improvement == 0) {
			return;
		}
		Solution localBestSolution = new Solution();
		StringBuilder candidateTabu = new StringBuilder("");
		for(int i=E-1; i >= MIN_EXAM_INDEX; i--) {
			int e = sortedExams[i];
			if(localmoveisConvinient(e, localBestSolution, candidateTabu)) {//aggiorna in convinient
				numIteration++;
				max_iter_last_improvement=5;
				printObjFunct();
				move();
				return;
			}
		}
		
		if(!localBestSolution.isEmpty()) {
			bestSolution = localBestSolution;
			updateTenure();
			tenure.put(candidateTabu.toString(),TABU_TENURE);
		}
		numIteration++;
		max_iter_last_improvement--;
		printObjFunct();
		//System.out.println("\n\nTempo impiegato dal Tabu Search: " + Duration.between(startTime, Instant.now()).getSeconds() + " secondi");
		move();
	}
	
	private int randomIndex() {
		return (int) (start_randomExam + Math.round(Math.random()*randomExams));
	}
	
	private void randomMove() {
		int examIndex = randomIndex();
		//stop criterion
  	    if(numIteration == MAX_ITERATIONS) {
			 return;
		}
		Solution localBestSolution = new Solution();
		StringBuilder candidateTabu = new StringBuilder("");
		do {
			int e = sortedExams[examIndex];
			//il primo valore che è ammissibile fa un move
			if(localmoveisConvinient(e, localBestSolution, candidateTabu)) {
				//examIndex = randomIndex();
				numIteration++;
				printObjFunct();
				randomMove();
				return;
			}
			examIndex = randomIndex();
			
		} while(localBestSolution.isEmpty());
		
		
		bestSolution = localBestSolution;
		updateTenure();
		tenure.put(candidateTabu.toString(),TABU_TENURE);
		
//		examIndex--;
		numIteration++;
		printObjFunct();
		randomMove();
	}
	
	private void printObjFunct() {
		System.out.printf("*\n*\n*\n*\n*\nBest value of the Objective Function at iteration %d "
				+ "of Tabu Search: %.3f\n*\n*\n*\n*\n*", numIteration, bestSolution.getObj());
	}
	
	private boolean localmoveisConvinient(int e, Solution localBestSolution, StringBuilder candidateTabu) {
		Solution currentSolution = new Solution();
		assignSolution(currentSolution, bestSolution);
		//metodi per impostare la y e le u, che nella soluzione originale valgono uno, a zero
		String fixedVar = currentSolution.setTargetyVarToZero(e, T);
		List<String> uCandidatesVars = currentSolution.getuCandidatesVars(e);
		currentSolution.setuTargetVarsToZero(uCandidatesVars);
		for(int t=1; t<=T; t++) {
			String y = "y_"+e+"_"+t;
			if(!fixedVar.equals(y) && !tenure.containsKey(y)) {
				//metodi per mettere la y e le u target a uno
				currentSolution.setVarValue(y, 1);
				boolean isFeasible = currentSolution.setuTargetVarsToOne(uCandidatesVars, e, t, T);
				if(isFeasible) {
					model.isFeasible(currentSolution);
					double objFunct = model.getObjFunct();//getObjFunct(ne1e2, currentSolution);
					currentSolution.setObj(objFunct);
					if(objFunct < bestSolution.getObj() /*- objFunct <= 0.01 dopo rimetti >=*/) {
						assignSolution(bestSolution, currentSolution);
						updateTenure();
						tenure.put(fixedVar,TABU_TENURE);
						return true;	
					} else if(localBestSolution.isEmpty() || localBestSolution.getObj() - objFunct > 0) {
						//nel caso in cui non ci sia una migliore prendiamo la meno peggio
						candidateTabu.delete(0, candidateTabu.length());
						candidateTabu.append(fixedVar);
						assignSolution(localBestSolution, currentSolution);
				   }
				}
				//metodi per rimettere la y e le u target a zero
				currentSolution.setVarValue(y, 0);
				currentSolution.setuTargetVarsToZero(uCandidatesVars); 
		     }
		}
	    return false;
	}
	
	private void updateTenure() {
		if(!tenure.isEmpty()) {
			String varToRemove = "";
			for (Iterator<Map.Entry<String, Integer>> it = tenure.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, Integer> entry = it.next();
				entry.setValue(entry.getValue()-1);
				if(entry.getValue() == 0) {
					varToRemove = entry.getKey();
				}
			}
			tenure.remove(varToRemove);
		}
	}
	
	private void assignSolution(Solution s1, Solution s2) {
		Map <String, Double> vars = new HashMap<>();
		vars.putAll(s2.getVars());
		s1.setObj(s2.getObj());
		s1.setVars(vars);
	}
		
//	private double getObjFunct(int ne1e2[], Solution solution) {
//		int obj = 0;
//		int j = 0;
//		List<String> names = solution.getVarsName();
//		for(int i=E*T-1; i < names.size(); i++) {
//			double u = solution.getVarValue(names.get(i));
//			if(u != 0) {
//				int timeslot = Integer.parseInt(names.get(i).split("_")[1]);
//				obj += Math.pow(2, Utility.MAX_TIMESLOTS_DISTANCE-timeslot)*ne1e2[j];
//				j++;
//			}
//		}
//		return obj/S;
//	}	

}
