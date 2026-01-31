package com.mss.backOffice.request;


public class RequestPosHistorique {
	private String serialNum;

	private String status;
	
	private String dateSaisie;

	public String getSerialNum() {
		return serialNum;
	}

	public void setSerialNum(String serialNum) {
		this.serialNum = serialNum;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDateSaisie() {
		return dateSaisie;
	}

	public void setDateSaisie(String dateSaisie) {
		this.dateSaisie = dateSaisie;
	}

	@Override
	public String toString() {
		return "RequestPosHistorique [serialNum=" + serialNum + ", status=" + status + ", dateSaisie=" + dateSaisie
				+ "]";
	}
	
	
}
