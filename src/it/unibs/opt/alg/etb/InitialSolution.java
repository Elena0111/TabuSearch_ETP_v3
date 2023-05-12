package it.unibs.opt.alg.etb;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InitialSolution {
	
	private String logPath;
	private String solutionPath;
	private Configuration config;
	private Solution startSolution;
	private Bucket kernel = new Bucket();
	private List<Bucket> buckets = new ArrayList<>();
	private int E;
	private int T;
	private int S;
	List<List<Integer>> enrollments;
	private int[] ne1e2;
	private int[] sortedExams;
	int[] kernelExams;
	private List<int[]> examsForEachBucket;
	private Instant startTime;
	private Model model;
	
	public InitialSolution(String logPath, String solutionPath, Configuration config, int E, int T, int S, 
			List<List<Integer>> enrollments, Instant startTime) {
		this.logPath = logPath;
		this.solutionPath = solutionPath;
		this.config = config;
		this.E = E;
		this.T = T;
		this.S = S;
		this.enrollments = enrollments;
		this.startTime = startTime;
		startSolution = new Solution();
		this.ne1e2 = new int[(this.E-1)*this.E/2];
		setTotalne1e2(this.ne1e2);						//numero di conflitti per ciascun esame
		sortedExams = sortConflictingExams();
		kernelExams = new int[this.config.getKernelSize()];
		examsForEachBucket = new ArrayList<>();
	}
	
	public Solution findInitialSolution() {
		buildKernel(kernel);
		buildBuckets(buckets);
		solveBuckets();
		return startSolution;
	}
	
	public Solution getStartSolution() {
		return startSolution;
	}
	
	public Model getModel() {
		return model;
	}
	
	public int[] getNe1e2() {
		return ne1e2;
	}
	
	public int[] getSorteExams() {
		return this.sortedExams;
	}
	
	private int getRemainingTime() {
		return (int) (Utility.TIME_LIMIT - Duration.between(startTime, Instant.now()).getSeconds());
	}
	
	//ne1e2 è l'array che contiene il numero di studenti iscritti a entrambi gli esami e1 ed e2
	//ne1e2[0] = n. stu. iscritti sia all'esame 1 che al 2
	//...
	//ne1e2[E-1] = n. stu. iscritti sia all'esame 1 che all'esame E
	//ne1e2[E] = n. stu. iscritti sia all'esame 2 che al 3 (1,2 = 2,1 : NON CI SONO DOPPIONI SIMMETRICI)
	private void setTotalne1e2(int[] ne1e2) {
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
	}
	
	private int[] sortConflictingExams() {
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
	
	private void buildKernel(Bucket kernel) {
		int j = 0;				//parte dagli ultimi esami nell'ordine: sono quelli con meno conflitti e ne seleziona
		for(int i = E-kernelExams.length; i < E; i++) {
			kernelExams[j++] = sortedExams[i];
		}						//conflitti per gli esami de kernel 
		int[] kne1ne2 = new int [(kernelExams.length-1)*kernelExams.length/2];
		Utility.computeSortedne1e2(kne1ne2, kernelExams, enrollments);
		kernel.build(kernelExams, kne1ne2, T);
	}
	
	private void buildBuckets(List<Bucket> buckets) {
		List<String> itemsToRemove = new ArrayList<>();
		itemsToRemove.addAll(kernel.getItems());
		int kE = config.getKernelSize();
		int size = config.getBucketSize();
		int nb = (E-kE) % size == 0 ? (E-kE)/size : ((E-kE)/size)+1;
		int first = 0;
		
		for(int i=1; i <= nb; i++) {
			//dimensione ultimo bucket potrebbe essere piï¿½ piccola
			if(i==nb) {
				size = (E-kE) - (nb-1)*size;
			}
			int[] bucketExams = new int[size];
			int k = 0;
			for(int j=first; j < first+size; j++) {
				bucketExams[k++] = sortedExams[j];
			}
			examsForEachBucket.add(bucketExams);
			
			int[] exams = new int[i*size + kE];
			for(int j=0; j < i; j++) {
				for(int e = j*size; e < j*size + size; e++) {
					exams[e] = examsForEachBucket.get(j)[e - j*size];
				}
			}
			k = 0;
			for(int j=i*size; j < exams.length; j++) {
				exams[j] = kernelExams[k++];
			}
			
			int[] bne1ne2 = new int [(exams.length-1)*exams.length/2];
			Utility.computeSortedne1e2(bne1ne2, exams, enrollments);
			Bucket b = new Bucket();
			b.build(exams, bne1ne2, T);
			
			//rimuovo gli item (del kernel o di altri bucket) dal bucket corrente
			for(String it: itemsToRemove) {
				b.removeItem(it);
			}
			
			itemsToRemove.addAll(b.getItems());
			
			first+=size;
			buckets.add(b);
		}
	}

	private void solveBuckets() {
		int tfirstbucket = config.getTimeFirstBucket();
		int tadded = 0;
		int count = 1;
		List<String> currents = new ArrayList<>();
		currents.addAll(kernel.getItems());
		
		for(Bucket b : buckets) {
			System.out.println("\n\n\t\t** Solving bucket "+ count +" **\n");
			
			int bucketsSize = 0;
			for(int i=0; i < count; i++) {
				bucketsSize += examsForEachBucket.get(i).length;
			}
			int[] exams = new int[kernelExams.length + bucketsSize];
			
			for(int j=0; j < count; j++) {
				//assegno dimensioni dichiarate del bucket e quella dell'ultimo, che potrebbe essere differente
				int standardbsize = examsForEachBucket.get(0).length;
				int currentbsize = examsForEachBucket.get(j).length;
				for(int e = j*standardbsize; e < j*standardbsize + currentbsize; e++) {
					exams[e] = examsForEachBucket.get(j)[e - j*standardbsize];
				}
			}
			int k = 0;
			for(int j=bucketsSize; j < exams.length; j++) {
				exams[j] = kernelExams[k++];
			}
			
			currents.addAll(b.getItems());
			model = new Model(logPath, config, exams, T, S, enrollments, Math.min(tfirstbucket+tadded, getRemainingTime()));	
			model.buildModel();
			
			List<String> toDisable = new ArrayList<>();
			if(count != 1) {
				toDisable = currents.stream().filter(it -> !kernel.contains(it) && !b.contains(it)).collect(Collectors.toList());		
				model.disableItems(toDisable);
			}
				
			model.addBucketConstraint(b.getItems());
			model.solve();
			tadded += config.getTimeAddedToEachBucket();
			
			if(model.hasSolution()) {
				if(count != buckets.size()) {
					List<String> selected = model.getSelectedItems(b.getItems(), kernelExams, examsForEachBucket.get(count-1));
					selected.forEach(it -> kernel.addItem(it));
				}
				else {
					startSolution = model.getSolution();
					//model.backToOriginalProblem(toDisable);
					//model.exportSolution(toDisable);
					//model.writeSolutionOnFile(solutionPath+"_solution_v3.sol");
				}
			}	
			
			count++;
			//model.dispose();
		}	
	}
	
}