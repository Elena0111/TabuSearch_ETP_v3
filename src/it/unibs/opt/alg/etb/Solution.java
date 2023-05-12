package it.unibs.opt.alg.etb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Solution {
	
	private double obj;
	private Map <String, Double> vars;
	private boolean empty;
	
	public Solution() {
		empty = true;
		vars = new HashMap<>();
	}
	
	public double getObj() {
		return obj;
	}

	public void setObj(double obj) {
		this.obj = obj;
	}
	
	public double getVarValue(String name) {
		return vars.get(name);
	}

	
	public List<String> getVarsName() {
		List<String> varsNames = new ArrayList<String>();
		varsNames.addAll(vars.keySet());
		return varsNames;
	}
	
	public void setVarValue(String name, double value) {
		vars.replace(name, value);
	}
	
	
	public Map<String, Double> getVars() {
		return vars;
	}

	public void setVars(Map<String, Double> vars) {
		this.vars = vars;
		empty = false;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	public boolean isEmpty() {
		return empty;
	}
	
	//trova la variabile y uguale a 1 e la fissa a 0, e ne restituisce il nome
	public String setTargetyVarToZero(int e, int T) {
		String name = null;
		for(int t=1; t<=T; t++) {
			name = "y_"+e+"_"+t;
			double y = getVarValue("y_"+e+"_"+t);
			if(y >= Utility.POSITIVE_THRESHOLD) {
				setVarValue(name,0);
				break;
			}
		}
		return name;
	}
	
	public List<String> getuCandidatesVars(int e) {
		String e1 = "_" + e + "_";
		String e2 = "_" + e;
		//filtro solo le variabili in forma "u_i_e_x" o "u_i_x_e"
		//per e che va da 1 a 5 devo fare ulteriori controlli perchè coincide con la distanza di time-slot i
		List<String> uCandidatesVars = vars.keySet().stream().filter(var -> ((var.startsWith("u") && var.contains(e1)) || 
				(var.startsWith("u") && var.endsWith(e2)))).collect(Collectors.toList());
		if(e <= Utility.MAX_TIMESLOTS_DISTANCE) {
			//rimuovo "u_e_x1_x2" e tengo solo "u_e_e_x" o "u_e_x_e"
			List<String> uVarsTmp = new ArrayList<>();
			uVarsTmp.addAll(uCandidatesVars);
			for(String u: uVarsTmp) {
				String [] uInfo = u.split("_");
				if(Integer.parseInt(uInfo[2]) != e && Integer.parseInt(uInfo[3]) != e) {
					uCandidatesVars.remove(u);
				}
			}
		}
		//le variabili u sono messe a caso: potrei pensare di riordinarle se conviene
		return uCandidatesVars;
	}
	
	public void setuTargetVarsToZero(List<String> uCandidatesVars) {
		for(String u: uCandidatesVars) {
			if(vars.get(u) >= Utility.POSITIVE_THRESHOLD) {
				vars.put(u, 0.0);
			}
		}
	}
	
	public boolean setuTargetVarsToOne(List<String> uCandidatesVars, int e, int tse, int T) {
		List<String> reduced_u_vars =  uCandidatesVars.stream().filter(var -> ((var.startsWith("u_1")))).collect(Collectors.toList());
		//aggiungo 5 ogni volta perchè i arriva fino a 5 -> NON POSSO applicarlo
		for(int j=0; j < reduced_u_vars.size(); j++/*j += Utility.MAX_TIMESLOTS_DISTANCE*/) {
			String [] uInfo = reduced_u_vars.get(j).split("_");
			String end_u = "";
			int conflictingExam = 0;
			int tsConflictingExam = 0;
			//recupero l'esame in conflitto con e
			if(Integer.parseInt(uInfo[2]) != e) {
				conflictingExam = Integer.parseInt(uInfo[2]);
				end_u = "_"+conflictingExam+"_"+e;
			} else {
				conflictingExam = Integer.parseInt(uInfo[3]);
				end_u = "_"+e+"_"+conflictingExam;
			}
			//recupero il timeSlot dell'esame in confitto con e
			for(int t=1; t <= T; t++) {
				if(vars.get("y_"+conflictingExam+"_"+t) >= Utility.POSITIVE_THRESHOLD) {
					tsConflictingExam = t;
					break;
				}
			}
			//ricavo i confrontando time slot fra l'esame in conflitto ed e
			int i = Math.abs(tse - tsConflictingExam);
			//se i=0 significa che 2 esami in conflitto sono nello stesso time slot -> soluzione non ammissibile
			if(i == 0) {
				return false;
			}
			if(i <= Utility.MAX_TIMESLOTS_DISTANCE) {
				vars.put("u_"+i+end_u, 1.0);
			}
		}
		return true;
	}
	
	public void writeSolutionOnFile(String fileName) {
		for(String varName: vars.keySet()) {
			String dataLine[] = varName.split("_");
			if(dataLine[0].equals("y")) {
				if(vars.get(varName) != 0) {
					InstanceReaderWriter.writeOnFile(fileName, String.format(dataLine[1] + " " + dataLine[2] + "\n"));
				}
			}
		}
	}

}
