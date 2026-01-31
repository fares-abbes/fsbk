package com.mss.backOffice.request;

import java.util.Date;

public class CraData {
	
	private String fileName;

	
	private long sumFromFile;
	private long nbTotalFromFile;
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public long getSumFromFile() {
		return sumFromFile;
	}
	public void setSumFromFile(long sumFromFile) {
		this.sumFromFile = sumFromFile;
	}
	public long getNbTotalFromFile() {
		return nbTotalFromFile;
	}
	public void setNbTotalFromFile(long nbTotalFromFile) {
		this.nbTotalFromFile = nbTotalFromFile;
	}

	
}
