package com.mss.backOffice.Response;

import java.util.Date;

public class ExceptionResponce {
	
	private Date   timestamp ;
	
	private String message ;
	
	private String details ;
	
 
	public ExceptionResponce(Date timestamp, String name, String details) {
		super();
		this.timestamp = timestamp;
		this.message = name;
		this.details = details;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
 
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}
	
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	
	
	
	

}
