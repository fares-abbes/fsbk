package com.mss.backOffice.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExtractDataDto {
	
	private ArrayList<String> lines;
	private  Map< String, List<String> > mapContentByFile;
	
	public ArrayList<String> getLines() {
		return lines;
	}
	public void setLines(ArrayList<String> lines) {
		this.lines = lines;
	}
	public Map<String, List<String>> getMapContentByFile() {
		return mapContentByFile;
	}
	public void setMapContentByFile(Map<String, List<String>> mapContentByFile) {
		this.mapContentByFile = mapContentByFile;
	}

}
