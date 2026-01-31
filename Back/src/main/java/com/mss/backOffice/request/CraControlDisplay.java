package com.mss.backOffice.request;

import java.util.Date;
import java.util.List;

public class CraControlDisplay {
	
	private String fileIntegrationDate;
	private List<CraDataList> craDataList;
	public String getFileIntegrationDate() {
		return fileIntegrationDate;
	}
	public void setFileIntegrationDate(String fileIntegrationDate) {
		this.fileIntegrationDate = fileIntegrationDate;
	}
	public List<CraDataList> getCraDataList() {
		return craDataList;
	}
	public void setCraDataList(List<CraDataList> craDataList) {
		this.craDataList = craDataList;
	}
	


}
