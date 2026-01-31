package com.mss.backOffice.request;


public class AddFileRequest {
  private int id;
  private String fileName;
  private String filePath;
  private String fileDate;
  private String key;

  
public int getId() {
	return id;
}
public void setId(int id) {
	this.id = id;
}
public String getFileName() {
	return fileName;
}
public void setFileName(String fileName) {
	this.fileName = fileName;
}
public String getFilePath() {
	return filePath;
}
public void setFilePath(String filePath) {
	this.filePath = filePath;
}
public String getFileDate() {
	return fileDate;
}
public void setFileDate(String fileDate) {
	this.fileDate = fileDate;
}
public String getKey() {
	return key;
}
public void setKey(String key) {
	this.key = key;
}
@Override
public String toString() {
	return "AddFileRequest [id=" + id + ", fileName=" + fileName + ", filePath=" + filePath + ", fileDate=" + fileDate
			+ ", key=" + key + "]";
}

 
}
