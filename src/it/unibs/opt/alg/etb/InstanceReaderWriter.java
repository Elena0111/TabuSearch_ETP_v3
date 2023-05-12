package it.unibs.opt.alg.etb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InstanceReaderWriter {
	
	public int totalTimeSlots(String fileName) {
		BufferedReader input = null;
		int T = 0;
		try {
			input = new BufferedReader(new FileReader(fileName));
			T = Integer.parseInt(input.readLine());  
        }
        catch (IOException ex) {
        	ex.printStackTrace();
        } finally {
        	try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return T;
	}
	
	public int totalExams(String fileName) {
		BufferedReader input = null;
		int E = 0;
		try {
			input = new BufferedReader(new FileReader(fileName));
			String last = "", line;
		    while ((line = input.readLine()) != null) { 
		        last = line;
		    }
            String dataLine[] = last.split(" ");
            E = Integer.parseInt(dataLine[0]);   
        }
        catch (IOException ex) {
        	ex.printStackTrace();
        } finally {
        	try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return E;
		
	}
	
	public int totalStudents(String fileName) {
		BufferedReader input = null;
		int S = 0;
		try {
			input = new BufferedReader(new FileReader(fileName));
			String last = "", line;
		    while ((line = input.readLine()) != null) { 
		        last = line;
		    }
            String dataLine[] = last.split(" ");
            S = Integer.parseInt(dataLine[0].substring(1));   
        }
        catch (IOException ex) {
        	ex.printStackTrace();
        } finally {
        	try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return S;
		
	}
	
	public List<Integer> list_exams_per_students(String fileName, int stu) {
		BufferedReader input = null;
		List<Integer> list_exams = new ArrayList<>();
		try {
			input = new BufferedReader(new FileReader(fileName));
			List<String> lines = input.lines().collect(Collectors.toList());
			for(int i=0; i < lines.size(); i++) {
				 String dataLine[] = lines.get(i).split(" ");
				 int student = Integer.parseInt(dataLine[0].substring(1));
				 if(student == stu) {
					 list_exams.add(Integer.parseInt(dataLine[1]));
				 }
			} 
        }
        catch (IOException ex) {
        	ex.printStackTrace();
        } finally {
        	try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return list_exams;
	}
	
	public void deleteFile(String nomeFile) {
		File fileOld = new File(nomeFile);
		fileOld.delete();
	}
	
	//metodo statico per scrivere su file, tenendo salvati tutti i tentativi
	public static void writeOnFile(String nomeFile, String text) {
		PrintWriter pw = null;
			try {
				pw = new PrintWriter(new FileOutputStream(new File(nomeFile), true));
				pw.append(text);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				if(pw != null) {
					pw.close();
				}
			}
	}
	
}
