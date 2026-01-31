package com.mss.backOffice.request;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public class SerialNumberRequest {
	
	public Integer model;
	
	public List<MultipartFile> multipartFile;

	public Integer getModel() {
		return model;
	}

	public void setModel(Integer model) {
		this.model = model;
	}

	public List<MultipartFile> getMultipartFile() {
		return multipartFile;
	}

	public void setMultipartFile(List<MultipartFile> multipartFile) {
		this.multipartFile = multipartFile;
	}

	@Override
	public String toString() {
		return "SerialNumberRequest [model=" + model + ", multipartFile=" + multipartFile + "]";
	}
	
	

}
