package com.mss.backOffice.Response;

import java.math.BigInteger;

public class TpControlDisplay {
	
	
	private String fileName;
	private String processingDate;
	
	private long tpTransactionsNb;
	private long sumTp;
	
	private long nbTotalPres;
	private long sumTotalPres;
	
	private long nbTotalNonPres;
	private long sumTotalNomPres;
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getProcessingDate() {
		return processingDate;
	}
	public void setProcessingDate(String processingDate) {
		this.processingDate = processingDate;
	}
	public long getTpTransactionsNb() {
		return tpTransactionsNb;
	}
	public void setTpTransactionsNb(long tpTransactionsNb) {
		this.tpTransactionsNb = tpTransactionsNb;
	}
	public long getSumTp() {
		return sumTp;
	}
	public void setSumTp(long sumTp) {
		this.sumTp = sumTp;
	}
	public long getNbTotalPres() {
		return nbTotalPres;
	}
	public void setNbTotalPres(long nbTotalPres) {
		this.nbTotalPres = nbTotalPres;
	}
	public long getSumTotalPres() {
		return sumTotalPres;
	}
	public void setSumTotalPres(long sumTotalPres) {
		this.sumTotalPres = sumTotalPres;
	}
	public long getNbTotalNonPres() {
		return nbTotalNonPres;
	}
	public void setNbTotalNonPres(long nbTotalNonPres) {
		this.nbTotalNonPres = nbTotalNonPres;
	}
	public long getSumTotalNomPres() {
		return sumTotalNomPres;
	}
	public void setSumTotalNomPres(long sumTotalNomPres) {
		this.sumTotalNomPres = sumTotalNomPres;
	}
	
	


	
	
}
