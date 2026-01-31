package com.mss.backOffice.request;


import java.util.List;
import java.util.Map;

public class ReadFilesDto {

	private List<String> fileContents ;
	private  Map< String, List<String> > mapContentByFile;
	
	public List<String> getFileContents() {
		return fileContents;
	}
	public void setFileContents(List<String> fileContents) {
		this.fileContents = fileContents;
	}
	public Map<String, List<String>> getMapContentByFile() {
		return mapContentByFile;
	}
	public void setMapContentByFile(Map<String, List<String>> mapContentByFile) {
		this.mapContentByFile = mapContentByFile;
	}

	
	
}
