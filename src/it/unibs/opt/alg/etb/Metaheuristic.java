package it.unibs.opt.alg.etb;

import java.time.Instant;
import java.util.List;

public class Metaheuristic {
	
	private String logPath;
	private String solutionPath;
	private Configuration config;
	private int E;
	private int T;
	private int S;
	List<List<Integer>> enrollments;
	private Instant startTime;
	private Solution bestSolution;
	private InitialSolution initSol;
	private TabuSearch tabuSearch;
	
	public Metaheuristic(String logPath, String solutionPath, Configuration config, int E, int T, int S, 
			List<List<Integer>> enrollments, Instant startTime) {
		this.logPath = logPath;
		this.solutionPath = solutionPath;
		this.config = config;
		this.E = E;
		this.T = T;
		this.S = S;
		this.enrollments = enrollments;
		this.startTime = startTime;
		bestSolution = new Solution();
		initSol = new InitialSolution(this.logPath, this.solutionPath, this.config, this.E, this.T, this.S, 
				this.enrollments, this.startTime);
		bestSolution = initSol.findInitialSolution();
		//tabuSearch = new TabuSearch(/*initSol.getStartSolution(),*/ initSol.getNe1e2(), this.E, this.T, this.S);
	}

}
