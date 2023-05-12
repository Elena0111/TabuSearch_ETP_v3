package it.unibs.opt.alg.etb;

import java.util.ArrayList;
import java.util.List;

public class Bucket {
	
	private List<String> items;
	
	public Bucket() {
		items = new ArrayList<>();
	}
	
	public void addItem(String it) {
		items.add(it);
	}
	
	public int size() {
		return items.size();
	}
	
	public List<String> getItems() {
		return items;
	}
	
	public boolean contains(String it) {
		return items.stream().anyMatch(it2 -> it2.equals(it));
	}
	
	public void removeItem(String it) {
		if(items.stream().anyMatch(it2 -> it2.equals(it))) {
			items.remove(it);
		}
	}

	
	public void build(int[] sortedExams, int[] ne1e2, int T) {
		for(int i=0; i < sortedExams.length; i++) {
			for(int j=1; j <= T; j++) {
				items.add("y_" + sortedExams[i] + "_" + (j));
			}
		}
		int e1 = 0;
		int e2 = 1;
		int decrease_E = 1;
		int increase_e1 = sortedExams.length-decrease_E;
		
		for(int i=0; i < ne1e2.length; i++) {
			if((i+1) > increase_e1) {
				decrease_E++;
				increase_e1 = increase_e1 + sortedExams.length-decrease_E;
				e1++;
				e2=e1+1;
			}
			for(int j=1; j <= 5; j++) {
				if(ne1e2[i] != 0) {
					items.add("u_"+(j)+"_"+sortedExams[e1]+"_"+sortedExams[e2]);
				}
			}
			e2++;
		}
	}
}