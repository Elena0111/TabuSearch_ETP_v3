package it.unibs.opt.alg.etb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.IntAttr;
import gurobi.GRB.StringAttr;
import gurobi.GRBConstr;

public class Model {
	
	private String logPath;
	private Configuration config;
	private GRBEnv env;
	private GRBModel model;
	private int[] sortedExams;
	private int T;
	private int S;
	private int[] ne1e2;
	private GRBVar[][] yet;
	private GRBVar[][] ue1e2;
	private int timeLimit;
	private boolean hasSolution;
	
	public Model(String logPath, Configuration config, int[] sortedExams, int T, int S, List<List<Integer>> enrollments, 
			int timeLimit) {
		this.logPath = logPath;
		this.config = config;
		this.sortedExams = sortedExams;
		this.T = T;
		this.S = S;
		this.ne1e2 = new int[(sortedExams.length-1)*sortedExams.length/2];
		Utility.computeSortedne1e2(this.ne1e2, this.sortedExams, enrollments);
		yet = new GRBVar[this.sortedExams.length][this.T];
		ue1e2 = new GRBVar[this.ne1e2.length][Utility.MAX_TIMESLOTS_DISTANCE];
		this.timeLimit = timeLimit;	
		this.hasSolution = false;
	}
	
	public Model() {
		try {
			env = new GRBEnv();
			env.set(GRB.DoubleParam.TimeLimit, 1);
		    model = new GRBModel(env, "initialSolution\\model02.lp");
			model.read("initialSolution\\bestSolution02.sol");
			model.optimize();
		} catch (GRBException e) {
			e.printStackTrace();
		}
	}
	
	public void buildModel() {
		try {
			env = new GRBEnv();
			setParameters();
			model = new GRBModel(env);
			
			addVariables();
			addObjectiveFunction();
			addConstraint1();
			addConstraint2();
			addConstraint3();
			addConstraint4();
			
		} catch (GRBException e) {
			e.printStackTrace();
		}
	}
	
	private void setParameters() throws GRBException {
		env.set(GRB.StringParam.LogFile, logPath+"\\log.txt");
		env.set(GRB.DoubleParam.TimeLimit, timeLimit);
		env.set(GRB.DoubleParam.NoRelHeurTime, timeLimit);
		env.set(GRB.IntParam.Presolve, config.getPresolve());
		env.set(GRB.IntParam.DegenMoves, 0);
		env.set(GRB.IntParam.Threads, config.getNumThreads());
		env.set(GRB.IntParam.Method, config.getMethod());
		
	}
	
	private void addVariables() throws GRBException {
		for(int i=0; i < yet.length; i++) {
			for(int j=0; j < yet[0].length; j++) {
				yet[i][j] = model.addVar(0,GRB.INFINITY,0,GRB.BINARY,"y_" + sortedExams[i] + "_" + (j+1));
			}
		}
		
		int e1 = 0;
		int e2 = 1;
		int decrease_E = 1;
		int increase_e1 = sortedExams.length-decrease_E;
		
		for(int i=0; i < ue1e2.length; i++) {
			if((i+1) > increase_e1) {
				decrease_E++;
				increase_e1 = increase_e1 + sortedExams.length-decrease_E;
				e1++;
				e2=e1+1;
			}
			for(int j=0; j < ue1e2[0].length; j++) {
				if(ne1e2[i] != 0) {
					ue1e2[i][j] = model.addVar(0,GRB.INFINITY,0,GRB.INTEGER,"u_"+(j+1)+"_"+sortedExams[e1]+"_"+sortedExams[e2]);
				}
			}
			e2++;
		}
	}
	
	private void addObjectiveFunction() throws GRBException {
		GRBLinExpr expr = new GRBLinExpr();
		
		for(int i=0; i < ue1e2.length; i++) {
			for(int j=0; j < ue1e2[0].length; j++) {
				if(ne1e2[i] != 0) {
					expr.addTerm(Math.pow(2, Utility.MAX_TIMESLOTS_DISTANCE-(j+1))*ne1e2[i]/S, ue1e2[i][j]);
				}
			}
		}
				
		model.setObjective(expr, GRB.MINIMIZE);
	}
	
	private void addConstraint1() throws GRBException {
		GRBLinExpr expr = new GRBLinExpr();
		
		for(int i=0; i < yet.length; i++) {
			for(int j=0; j < yet[0].length; j++) {
				expr.addTerm(1, yet[i][j]);
			}
			model.addConstr(expr, GRB.EQUAL, 1, "Exam_" + sortedExams[i] + "_scheduled_once");
			expr = new GRBLinExpr();
		}
	}
	
	private void addConstraint2() throws GRBException {
		GRBLinExpr expr = new GRBLinExpr();
		int conflict = 0;
		
		for(int i=0; i < yet.length-1; i++) {
			for(int j=i+1; j < yet.length; j++) {
				if(ne1e2[conflict] != 0) {
					for(int t=0; t < yet[0].length; t++) {
						expr.addTerm(1, yet[i][t]);
						expr.addTerm(1, yet[j][t]);
						model.addConstr(expr, GRB.LESS_EQUAL, 1, "Conf_Exams_" + sortedExams[i] + "_" + sortedExams[j] +
											"_not_scheduled_in_slot_" + (t+1));
						expr = new GRBLinExpr();
					}
				}
				conflict++;
			}
		}
	}
	
	private void addConstraint3() throws GRBException {
		GRBLinExpr expr = new GRBLinExpr();
		int conflict = 0;
		
		for(int i=0; i < yet.length-1; i++) {
			for(int j=i+1; j < yet.length; j++) {
				if(ne1e2[conflict] != 0) {
					for(int s=0; s < ue1e2[0].length; s++) {
						for(int t=0; t < yet[0].length-(s+1); t++) {
							expr.addTerm(1, yet[i][t]);
							expr.addTerm(1, yet[j][t+(s+1)]);
							expr.addTerm(-1, ue1e2[conflict][s]);
							model.addConstr(expr, GRB.LESS_EQUAL, 1, "Link_vars_y"+sortedExams[i]+"_"+(t+1)+"_y_"+sortedExams[j]+"_"+(t+s+2)
												+"_u"+(s+1)+"_"+conflict);
							expr = new GRBLinExpr();
						}
					}
				}
				conflict++;
			}
		}
		
		conflict = 0;
		for(int i=0; i < yet.length-1; i++) {
			for(int j=i+1; j < yet.length; j++) {
				if(ne1e2[conflict] != 0) {
					for(int s=0; s < ue1e2[0].length; s++) {
						for(int t=0; t < yet[0].length-(s+1); t++) {
							expr.addTerm(1, yet[j][t]);
							expr.addTerm(1, yet[i][t+(s+1)]);
							expr.addTerm(-1, ue1e2[conflict][s]);
							model.addConstr(expr, GRB.LESS_EQUAL, 1, "Link_vars_y"+sortedExams[j]+"_"+(t+1)+"_y_"+sortedExams[i]+"_"+(t+s+2)
												+"_u"+(s+1)+"_"+conflict);
							expr = new GRBLinExpr();
						}
					}
				}
				conflict++;
			}
		}
	}
	
	private void addConstraint4() throws GRBException {
		GRBLinExpr expr = new GRBLinExpr();
		
		for(int i=0; i < yet[0].length; i++) {
			for(int j=0; j < yet.length; j++) {
				expr.addTerm(1, yet[j][i]);
			}
			int exams = config.getMinExamsPerPeriod();
			model.addConstr(expr, GRB.GREATER_EQUAL, exams, "At_least_"+ exams +"_exams_per_period_" + (i+1));
			expr = new GRBLinExpr();
		}
	}
	
	private void removeConstraint4() throws GRBException {
		//model.update();
		for(int t=1; t <= T; t++) {
			GRBConstr constr = model.getConstrByName("At_least_" + config.getMinExamsPerPeriod() + "_exams_per_period_" + t);
			model.remove(constr);
		}
	}
	
	public void solve() {
		try {
			model.optimize();
			if(model.get(IntAttr.SolCount) > 0)
				hasSolution = true;
		} catch (GRBException e) {
			e.printStackTrace();
		}
	}
	
	public void dispose() {
		try	{
			model.dispose();
			env.dispose();
		} catch (GRBException e) {
			e.printStackTrace();
		}
	}
	
	public List<String> getVarNames() {
		List<String> varNames = new ArrayList<>();
		try {
			model.update();
			for(GRBVar v : model.getVars()) {
					varNames.add(v.get(StringAttr.VarName));
				}
		} catch (GRBException e) {
			e.printStackTrace();
		}
		return varNames;
	}
	
	//trasformalo in void
	public boolean isFeasible(Solution solution) {
		boolean isFeasible = true;
		try {
			for(String varName: solution.getVarsName()) {
				GRBVar var = model.getVarByName(varName);
				double value = solution.getVarValue(varName);
				if(value > Utility.POSITIVE_THRESHOLD /*cio� == 1*/) {
					//verifico che non LB e UB non siano gi� a 1
					if(var.get(GRB.DoubleAttr.LB) != 1) {
						var.set(GRB.DoubleAttr.UB, 1);
						var.set(GRB.DoubleAttr.LB, 1);
					}
				} else {
					//verifico che non LB e UB non siano gi� a 0
					if(var.get(GRB.DoubleAttr.UB) != 0) {
						var.set(GRB.DoubleAttr.UB, 0);
						var.set(GRB.DoubleAttr.LB, 0);
					}
				}
			}
			model.optimize();
			//non serve pi� controllare lo status
			if(model.get(GRB.IntAttr.Status) == 3) {
				isFeasible = false;
			}
		} catch (GRBException e) {
			e.printStackTrace();
		}
		return isFeasible;
	}
	
	public double getObjFunct() {
		double obj = 0;
		try {
			obj = model.get(DoubleAttr.ObjVal);
		} catch (GRBException e) {
			e.printStackTrace();
		}
		return obj;
	}

//	public double getVarValue(String v) {
//		try {
//			if(model.get(IntAttr.SolCount) > 0) {
//				return model.getVarByName(v).get(DoubleAttr.X);
//			}
//		} catch (GRBException e) {
//			e.printStackTrace();
//		}
//		return -1;
//	}
	
//	public double getVarRC(String v) {
//		try {
//			if(model.get(IntAttr.SolCount) > 0) {
//				return model.getVarByName(v).get(DoubleAttr.RC);
//			}
//		} catch (GRBException e) {
//			e.printStackTrace();
//		}
//		return -1;
//	}
	
	//trova la variabile uguale a 1 e la fissa a 0, e ne restituisce il nome
//	public String setFixedVarToZero(int e) {
//		String name = null;
//		try {
//			for(int t=1; t<=T; t++) {
//				GRBVar y = model.getVarByName("y_"+e+"_"+t);
//				if(y.get(DoubleAttr.X)>= POSITIVE_THRESHOLD) {
//					y.set(DoubleAttr.X, 0);
//					name = y.get(StringAttr.VarName);
//					break;
//				}
//			}
//		} catch (GRBException e1) {
//			e1.printStackTrace();
//		}
//		return name;
//	}
	
//	public void setVarToZero(String y) {
//		try {
//			model.getVarByName(y).set(DoubleAttr.X, 0);
//		} catch (GRBException e) {
//			e.printStackTrace();
//		}
//	}
	
//	public void setVarToOne(String y) {
//		try {
//			model.getVarByName(y).set(DoubleAttr.X, 1);
//		} catch (GRBException e) {
//			e.printStackTrace();
//		}
//	}
	
	public void disableItems(List<String> items) {
		try {
			model.update();
			GRBLinExpr expr = new GRBLinExpr();	
			for(String it : items) {
				expr.addTerm(1, model.getVarByName(it));
				model.addConstr(expr, GRB.EQUAL, 0, "FIX_VAR_"+it);
				expr = new GRBLinExpr();
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void addBucketConstraint(List<String> items) {
		GRBLinExpr expr = new GRBLinExpr();	
		try {
			model.update();
			for(String it : items) {
				expr.addTerm(1, model.getVarByName(it));
			}
			model.addConstr(expr, GRB.GREATER_EQUAL, 1, "bucketConstraint");
		} catch (GRBException e) {
			e.printStackTrace();
		}	
	}
	
	private void backToOriginalProblem(List<String> items) {
		try {
			model.update();
			this.removeConstraint4();
			for(String it : items) {
				GRBConstr constr = model.getConstrByName("FIX_VAR_"+it);
				model.remove(constr);
			}
			model.remove(model.getConstrByName("bucketConstraint"));
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

//	public void addObjConstraint(double obj) {
//		try {
//			model.update();
//			model.getEnv().set(GRB.DoubleParam.Cutoff, obj);
//		} catch (GRBException e) {
//			e.printStackTrace();
//		}
//	}
	
	public List<String> getSelectedItems(List<String> items, int[] K, int[] B) {
		List<String> selected = new ArrayList<>();
		try {
			for(String it : items) {
				if(model.getVarByName(it).get(DoubleAttr.X) > Utility.POSITIVE_THRESHOLD) {
					selected.add(it);
				}
			}
			for(int i=0; i < B.length; i++) {
				for(int j=0; j < K.length; j++) {
					for(int s=1; s <= Utility.MAX_TIMESLOTS_DISTANCE; s++) {
						String it = "u_"+s+"_"+B[i]+"_"+K[j];
						if(model.getVarByName(it) != null && model.getVarByName(it).get(DoubleAttr.X) < Utility.POSITIVE_THRESHOLD) {
							if(items.stream().anyMatch(it2 -> it2.equals(it))) {
								selected.add(it);
							}
						}
					}
				}
			}
		} catch (GRBException e) {
			e.printStackTrace();
		}
		return selected;
	}
	
//	public void exportSolution(/*List<String> items*/) {
//		try {
//			model.write("initialSolution\\TS_solution.sol");
//			//backToOriginalProblem(items);
//			//model.write("model.lp");
//		} catch (GRBException e) {
//			e.printStackTrace();
//		}
//	}
	
//	public void readSolution(String path) {
//		try {
//			model.read(path);
//		} catch (GRBException e) {
//			e.printStackTrace();
//		}
//	}
	
//	public void readSolution(Solution solution) {
//		try {
//			model.update();
//			for(GRBVar var : model.getVars()) {
//				if(solution.getVars().containsKey(var.get(StringAttr.VarName))) {
//					var.set(DoubleAttr.Start, solution.getVarValue(var.get(StringAttr.VarName)));
//				}
//			}
//		} catch (GRBException e) {
//			e.printStackTrace();
//		}
//	}

	public boolean hasSolution() {
		return hasSolution;
	}
	
	public Solution getSolution() {
		Solution sol = new Solution();
		try {
			sol.setObj(model.get(DoubleAttr.ObjVal));
			Map<String, Double> vars = new HashMap<>();
			for(GRBVar var : model.getVars()) {
				vars.put(var.get(StringAttr.VarName), var.get(DoubleAttr.X));
			}
			sol.setVars(vars);
		} catch (GRBException e) {
			e.printStackTrace();
		}
		return sol;
	}
	
//	public void writeSolutionOnFile(String fileName) {
//		try {
//			GRBVar vars[] = model.getVars();
//			String names[] = model.get(GRB.StringAttr.VarName, vars);
//			double values[] = model.get(GRB.DoubleAttr.X, vars);
//			for(int i=0; i < model.get(GRB.IntAttr.NumVars); i++) {
//				String dataLine[] = names[i].split("_");
//				if(dataLine[0].equals("y")) {
//					if(values[i] != 0) {
//						InstanceReaderWriter.writeOnFile(fileName, String.format(dataLine[1] + " " + dataLine[2] + "\n"));
//					}
//				}
//			}
//		} catch (GRBException e) {
//			System.out.println("Codice d'errore: " + e.getErrorCode() + ":  " + e.getMessage());
//		}
//	}
	
}