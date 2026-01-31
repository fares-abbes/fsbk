package com.mss.backOffice.request;

import java.util.List;

public class UapIn {
	private List<Integer> idRejet;
	private List<Integer> idAcceptation;
	private String motifRNotOk;
	private String motifRExtra;
	private String handleAll;
	

	public String getHandleAll() {
		return handleAll;
	}

	public void setHandleAll(String handleAll) {
		this.handleAll = handleAll;
	}

	public List<Integer> getIdRejet() {
		return idRejet;
	}

	public void setIdRejet(List<Integer> idRejet) {
		this.idRejet = idRejet;
	}

	public List<Integer> getIdAcceptation() {
		return idAcceptation;
	}

	public void setIdAcceptation(List<Integer> idAcceptation) {
		this.idAcceptation = idAcceptation;
	}

	public String getMotifRNotOk() {
		return motifRNotOk;
	}

	public void setMotifRNotOk(String motifRNotOk) {
		this.motifRNotOk = motifRNotOk;
	}

	public String getMotifRExtra() {
		return motifRExtra;
	}

	public void setMotifRExtra(String motifRExtra) {
		this.motifRExtra = motifRExtra;
	}

	@Override
	public String toString() {
		return "UapIn [idRejet=" + idRejet + ", idAcceptation=" + idAcceptation + ", motifRNotOk=" + motifRNotOk
				+ ", motifRExtra=" + motifRExtra + ", handleAll=" + handleAll + "]";
	}

 


}
