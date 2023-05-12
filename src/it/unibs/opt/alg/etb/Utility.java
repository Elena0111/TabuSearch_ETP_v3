package it.unibs.opt.alg.etb;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utility {
	
	public final static int TIME_LIMIT = 2000;
	public final static int MAX_TIMESLOTS_DISTANCE = 5;
	public final static double POSITIVE_THRESHOLD = 1e-1;
	
	//come setTotalne1ne2 in InitialSolution, ma con un array ordinato di esami
	public static void computeSortedne1e2(int[] ne1e2, int[] sortedExams, List<List<Integer>> enrollments) {
        int index = 0;
		for(int i=0; i < sortedExams.length-1; i++) {
			for(int j=i+1; j < sortedExams.length; j++) {
				int count = 0;
				for(List<Integer> enr: enrollments) {
					if(enr.contains(sortedExams[i]) && enr.contains(sortedExams[j])) {
						count++;
					}
				}
				ne1e2[index] = count;
				index++;
			}
		}
	}
	
	//RESTA QUI SOLO PER PROVARE IL TABU SEARCH SENZA DOVER TROVARE OGNI VOLTA LA SOLUZIONE INIZIALE
	
	//ne1e2 è l'array che contiene il numero di studenti iscritti a entrambi gli esami e1 ed e2
		//ne1e2[0] = n. stu. iscritti sia all'esame 1 che al 2
		//...
		//ne1e2[E-1] = n. stu. iscritti sia all'esame 1 che all'esame E
		//ne1e2[E] = n. stu. iscritti sia all'esame 2 che al 3 (1,2 = 2,1 : NON CI SONO DOPPIONI SIMMETRICI)
		public static int[] calculateTotalne1e2(int E, List<List<Integer>> enrollments) {
			int[] ne1e2 = new int[(E-1)*E/2];
			int index = 0;
			for(int e1=1; e1 < E; e1++) {
				for(int e2=e1+1; e2 <= E; e2++) {
					int count = 0;
					for(List<Integer> enr: enrollments) {
						if(enr.contains(e1) && enr.contains(e2)) {
							count++;
						}
					}
					ne1e2[index] = count;
					index++;
				}
			}
			return ne1e2;
		}
		
		//RESTA QUI SOLO PER PROVARE IL TABU SEARCH SENZA DOVER TROVARE OGNI VOLTA LA SOLUZIONE INIZIALE
		public static int[] sortConflictingExams(int[] ne1e2, int E) {
			int i = 0;
			int[] conflicts = new int[E+1];
			Map<Integer,Integer> exams_conflicts = new HashMap<>();
			int[] sortedExams = new int[E];
			
			conflicts[0] = -1;						//per inizializzarlo
			for(int e=1; e <= E; e++) {
				conflicts[e] = 0;
			}
			
			for(int e1=1; e1 <= E-1; e1++) {
				for(int e2=e1+1; e2 <= E; e2++) {
					if(ne1e2[i] != 0) {				//se hanno degli inscritti in comune 
						conflicts[e1]++;
						conflicts[e2]++;
					}
					i++;
				}
			}
			
			for(int e=1; e <= E; e++) {
				exams_conflicts.put(e, conflicts[e]);
			}
			
			//ordina da quello con più conflitti
			Object[] sorted = exams_conflicts.entrySet().stream().
					sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).toArray();
			
			//dubbio
			for(int j=0; j < sorted.length; j++) {
				sortedExams[j] = Integer.parseInt(sorted[j].toString().split("=")[0]);
			}
			//array di stringhe degli esami ordinati 
			return sortedExams;
		}

}
