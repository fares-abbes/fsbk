package com.mss.backOffice.request;

public class FileTSRequest {

	
	byte[] data;    
	String nameTitle;

	public String getNameTitle() {
		return nameTitle;
	}
	public void setNameTitle(String nameTitle) {
		this.nameTitle = nameTitle;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}   
	
	
}
