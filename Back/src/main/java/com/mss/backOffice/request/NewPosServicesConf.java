package com.mss.backOffice.request;

import java.util.HashSet;
import java.util.Set;

public class NewPosServicesConf {
	private String label;
	private Set<Integer> services= new HashSet<>();
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public Set<Integer> getServices() {
		return services;
	}
	public void setServices(Set<Integer> services) {
		this.services = services;
	}
	
	
}
