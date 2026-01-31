package com.mss.backOffice.request;

import java.util.ArrayList;

import java.util.List;


public class ValidationRequest {
	private int code;
	public List<String> RequestCard=new ArrayList<>();
    
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;




	}


	@Override
	public String toString() {
		return "ValidationRequest{" +
				"code=" + code +
				", RequestCard=" + RequestCard +
				'}';
	}
}
