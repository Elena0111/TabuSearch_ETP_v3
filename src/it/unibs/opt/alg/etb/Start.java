package it.unibs.opt.alg.etb;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Start {
	public static void main(String[] args) {
//		String pathmps = args[0];
//		String pathlog = args[1];
//		String pathConfig = args[2];
		
		Instant startTime = Instant.now();
		String path = "C:\\Users\\elena\\Documents\\Optimization_Algorithms\\instances\\instance02";
		
		InstanceReaderWriter reader = new InstanceReaderWriter();
		int T = reader.totalTimeSlots(path+".slo");
		int E = reader.totalExams(path+".exm");
		int S = reader.totalStudents(path+".stu");
		reader.deleteFile(path+"_tabu_search.sol");
		
		List<List<Integer>> enrollments = new ArrayList<>();
		for(int s=1; s <= S; s++) {
			enrollments.add(reader.list_exams_per_students(path+".stu", s));
		}
		
		String pathlog = ".\\logs";
		String pathConfig = "./config.txt";
		Configuration config = ConfigurationReader.read(pathConfig);		
//		Metaheuristic metaheuristic = new Metaheuristic(pathlog, path, config, E, T, S, enrollments, startTime);
		int[] ne1e2 = Utility.calculateTotalne1e2(E, enrollments);
		TabuSearch ts = new TabuSearch(Utility.sortConflictingExams(ne1e2, E), ne1e2, E, T, path, startTime);
		ts.startTabuSearch();
		
		System.out.println("\n\nTempo impiegato dal Tabu Search: " + Duration.between(startTime, Instant.now()).getSeconds() + " secondi");	
	}
}