package com.mss.backOffice.request;

import java.util.Date;
import java.util.List;

public class CroControlDisplay {
	
	private Date dateReg;
	private List<CroDataList> croDataList;
	
	public Date getDateReg() {
		return dateReg;
	}
	public void setDateReg(Date dateReg) {
		this.dateReg = dateReg;
	}
	public List<CroDataList> getCroDataList() {
		return croDataList;
	}
	public void setCroDataList(List<CroDataList> croDataList) {
		this.croDataList = croDataList;
	}
	
	
	

}
