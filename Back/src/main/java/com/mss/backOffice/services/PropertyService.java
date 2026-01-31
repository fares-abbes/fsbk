package com.mss.backOffice.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PropertyService {

	@Value("${filePath}")
	private String filePath;

	@Value("${fileJsonPath}")
	private String fileJsonPath;

	@Value("${posFilesPath}")
	private String posFilesPath;

	@Value("${fileBePath}")
	private String fileBePath;

	@Value("${compensationfilePath}")
	private String compensationfilePath;
	@Value("${compensationfilePathCro}")
	private String compensationfilePathCro;
	@Value("${compensationfilePathCRA}")

	private String compensationfilePathCRA;
	@Value("${compensationfilePathLOT}")

	private String compensationfilePathLOT;
	@Value("${soldeFilePath}")
	private String soldeFilePath;

	@Value("${newFile}")
	private String newFile;

	@Value("${oldFile}")
	private String oldFile;

	@Value("${soldeArchiveFilePath}")
	private String soldeArchiveFilePath;

	@Value("${soldeFileName}")
	private String soldeFileName;
	
	@Value("${fileRef}")
	private String fileRef;
	
	@Value("${fileAuth}")
	private String fileAuth;
	
	@Value("${fileSolde}")
	private String fileSolde;
	
	@Value("${fileJsonPathLog}")
	private String fileJsonPathLog;

	// ------------------SATIM--------------------

	@Value("${server.satim.ip}")
	private String serverSatimIp;

	@Value("${server.satim.user}")
	private String serverSatimUser;

	@Value("${server.satim.password}")
	private String serverSatimPassword;

	// ------------------HOST--------------------

	@Value("${server.host.ip}")
	private String serverHostIp;

	@Value("${server.host.user}")
	private String serverHostUser;

	@Value("${server.host.password}")
	private String serverHostPassword;

	//////////// AMPLITUDE //////////////

	@Value("${amplitude.auth.user}")
	private String amplitudeAuthUser;

	@Value("${amplitude.auth.password}")
	private String amplitudeAuthPassword;

	@Value("${porteurFilesPath}")
	private String porteurFilesPath;
	
	@Value("${env}")
	private String env;
	
	public String getSoldeFileName() {
		return soldeFileName;
	}

	public void setSoldeFileName(String soldeFileName) {
		this.soldeFileName = soldeFileName;
	}

	public String getSoldeFilePath() {
		return soldeFilePath;
	}

	public void setSoldeFilePath(String soldeFilePath) {
		this.soldeFilePath = soldeFilePath;
	}

	public String getSoldeArchiveFilePath() {
		return soldeArchiveFilePath;
	}

	public void setSoldeArchiveFilePath(String soldeArchiveFilePath) {
		this.soldeArchiveFilePath = soldeArchiveFilePath;
	}

	public String getCompensationfilePathCro() {
		return compensationfilePathCro;
	}

	public void setCompensationfilePathCro(String compensationfilePathCro) {
		this.compensationfilePathCro = compensationfilePathCro;

	}

	public String getCompensationfilePath() {
		return compensationfilePath;
	}

	public void setCompensationfilePath(String compensationfilePath) {
		this.compensationfilePath = compensationfilePath;
	}

	public String getFileBePath() {
		return fileBePath;
	}

	public void setFileBePath(String fileBePath) {
		this.fileBePath = fileBePath;
	}

	public String getPosFilesPath() {
		return posFilesPath;
	}

	public void setPosFilesPath(String posFilesPath) {
		this.posFilesPath = posFilesPath;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileJsonPath() {
		return fileJsonPath;
	}

	public void setFileJsonPath(String fileJsonPath) {
		this.fileJsonPath = fileJsonPath;
	}

	public String getNewFile() {
		return newFile;
	}

	public void setNewFile(String newFile) {
		this.newFile = newFile;
	}

	public String getOldFile() {
		return oldFile;
	}

	public void setOldFile(String oldFile) {
		this.oldFile = oldFile;
	}

	public String getCompensationfilePathCRA() {
		return compensationfilePathCRA;
	}

	public void setCompensationfilePathCRA(String compensationfilePathCRA) {
		this.compensationfilePathCRA = compensationfilePathCRA;
	}

	public String getCompensationfilePathLOT() {
		return compensationfilePathLOT;
	}

	public void setCompensationfilePathLOT(String compensationfilePathLOT) {
		this.compensationfilePathLOT = compensationfilePathLOT;
	}

	public String getFileRef() {
		return fileRef;
	}

	public void setFileRef(String fileRef) {
		this.fileRef = fileRef;
	}

	public String getFileAuth() {
		return fileAuth;
	}

	public void setFileAuth(String fileAuth) {
		this.fileAuth = fileAuth;
	}

	public String getFileSolde() {
		return fileSolde;
	}

	public void setFileSolde(String fileSolde) {
		this.fileSolde = fileSolde;
	}

	public String getFileJsonPathLog() {
		return fileJsonPathLog;
	}

	public void setFileJsonPathLog(String fileJsonPathLog) {
		this.fileJsonPathLog = fileJsonPathLog;
	}

	public String getServerSatimIp() {
		return serverSatimIp;
	}

	public void setServerSatimIp(String serverSatimIp) {
		this.serverSatimIp = serverSatimIp;
	}

	public String getServerSatimUser() {
		return serverSatimUser;
	}

	public void setServerSatimUser(String serverSatimUser) {
		this.serverSatimUser = serverSatimUser;
	}

	public String getServerSatimPassword() {
		return serverSatimPassword;
	}

	public void setServerSatimPassword(String serverSatimPassword) {
		this.serverSatimPassword = serverSatimPassword;
	}

	public String getServerHostIp() {
		return serverHostIp;
	}

	public void setServerHostIp(String serverHostIp) {
		this.serverHostIp = serverHostIp;
	}

	public String getServerHostUser() {
		return serverHostUser;
	}

	public void setServerHostUser(String serverHostUser) {
		this.serverHostUser = serverHostUser;
	}

	public String getServerHostPassword() {
		return serverHostPassword;
	}

	public void setServerHostPassword(String serverHostPassword) {
		this.serverHostPassword = serverHostPassword;
	}

	public String getAmplitudeAuthUser() {
		return amplitudeAuthUser;
	}

	public void setAmplitudeAuthUser(String amplitudeAuthUser) {
		this.amplitudeAuthUser = amplitudeAuthUser;
	}

	public String getAmplitudeAuthPassword() {
		return amplitudeAuthPassword;
	}

	public void setAmplitudeAuthPassword(String amplitudeAuthPassword) {
		this.amplitudeAuthPassword = amplitudeAuthPassword;
	}

	public String getPorteurFilesPath() {
		return porteurFilesPath;
	}

	public String getEnv() {
		return env;
	}






}