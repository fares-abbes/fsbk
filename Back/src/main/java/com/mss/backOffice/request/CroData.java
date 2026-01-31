package com.mss.backOffice.request;

import java.util.Date;

public class CroData {
	
	private String fileName;
	private Date processingDate;
	
	private long sumFromFile;
	private long nbTotalFromFile;
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public Date getProcessingDate() {
		return processingDate;
	}
	public void setProcessingDate(Date processingDate) {
		this.processingDate = processingDate;
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
