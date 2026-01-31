package com.mss.backOffice.request;

import java.util.HashSet;
import java.util.Set;

public class NewPosBinConf {
	private String label;
	private Set<Integer> bins= new HashSet<>();
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public Set<Integer> getBins() {
		return bins;
	}
	public void setBins(Set<Integer> bins) {
		this.bins = bins;
	}
	@Override
	public String toString() {
		return "NewPosBinConf [label=" + label + ", bins=" + bins + "]";
	}
	
	
	
	
}