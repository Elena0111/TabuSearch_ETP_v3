package it.unibs.opt.alg.etb;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationReader {
	public static Configuration read(String path) {
		Configuration config = new Configuration();
		
		List<String> lines = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {
        	lines = br.lines().collect(Collectors.toList());
        } catch (IOException e) {
			e.printStackTrace();
		}
        
        for(String line : lines) {
        	String[] splitLine = line.split("\\s+");
        	
        	switch(splitLine[0]) {
        		case "PRESOLVE":
        			config.setPresolve(Integer.parseInt(splitLine[1]));
        			break;
        		case "KERNEL_SIZE":
        			config.setKernelSize(Integer.parseInt(splitLine[1]));
        			break;
        		case "BUCKET_SIZE":
        			config.setBucketSize(Integer.parseInt(splitLine[1]));
        			break;
        		case "TIME_FIRST_BUCKET":
        			config.setTimeFirstBucket(Integer.parseInt(splitLine[1]));
        			break;
        		case "TIME_ADDED_TO_EACH_BUCKET":
        			config.setTimeAddedToEachBucket(Integer.parseInt(splitLine[1]));
        			break;
        		case "MIN_EXAMS_PER_PERIOD":
        			config.setMinExamsPerPeriod(Integer.parseInt(splitLine[1]));
        			break;
        		case "METHOD":
        			config.setMethod(Integer.parseInt(splitLine[1]));
        			break;
        		case "THREADS":
        			config.setNumThreads(Integer.parseInt(splitLine[1]));
        			break;
        		case "NUM_ITERATIONS":
        			config.setNumIterations(Integer.parseInt(splitLine[1]));
        			break;
        		default:
        			System.out.println("Unrecognized parameter name.");			
			}
        }
        return config;
	}
}