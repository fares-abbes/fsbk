package com.mss.backOffice.Response;

import org.springframework.data.domain.Page;

public class TotalControlDisplay {

	private Page<TpControlDisplay> dataControl;
	private long totalGenTpTransactionsNb;
	private long totalGenSumTp;
	
	
	private long totalGenNbPres;
	private long totalGensumPres;
	
	private long totalGenNbNonPres;
	private long totalGensumNonPres;
	public Page<TpControlDisplay> getDataControl() {
		return dataControl;
	}
	public void setDataControl(Page<TpControlDisplay> dataControl) {
		this.dataControl = dataControl;
	}
	public long getTotalGenTpTransactionsNb() {
		return totalGenTpTransactionsNb;
	}
	public void setTotalGenTpTransactionsNb(long totalGenTpTransactionsNb) {
		this.totalGenTpTransactionsNb = totalGenTpTransactionsNb;
	}
	public long getTotalGenSumTp() {
		return totalGenSumTp;
	}
	public void setTotalGenSumTp(long totalGenSumTp) {
		this.totalGenSumTp = totalGenSumTp;
	}
	public long getTotalGenNbPres() {
		return totalGenNbPres;
	}
	public void setTotalGenNbPres(long totalGenNbPres) {
		this.totalGenNbPres = totalGenNbPres;
	}
	public long getTotalGensumPres() {
		return totalGensumPres;
	}
	public void setTotalGensumPres(long totalGensumPres) {
		this.totalGensumPres = totalGensumPres;
	}
	public long getTotalGenNbNonPres() {
		return totalGenNbNonPres;
	}
	public void setTotalGenNbNonPres(long totalGenNbNonPres) {
		this.totalGenNbNonPres = totalGenNbNonPres;
	}
	public long getTotalGensumNonPres() {
		return totalGensumNonPres;
	}
	public void setTotalGensumNonPres(long totalGensumNonPres) {
		this.totalGensumNonPres = totalGensumNonPres;
	}
	
}
