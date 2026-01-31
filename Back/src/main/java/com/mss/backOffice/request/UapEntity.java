package com.mss.backOffice.request;

public class UapEntity {
private String numAutorisation;
private String panCro;
private String data;
private String rio;
private String reglement;
private String numTransaction;
public String getNumAutorisation() {
	return numAutorisation;
}
public void setNumAutorisation(String numAutorisation) {
	this.numAutorisation = numAutorisation;
}
public String getPanCro() {
	return panCro;
}
public void setPanCro(String panCro) {
	this.panCro = panCro;
}
public String getData() {
	return data;
}
public void setData(String data) {
	this.data = data;
}
public String getRio() {
	return rio;
}
public void setRio(String rio) {
	this.rio = rio;
}
public String getReglement() {
	return reglement;
}
public void setReglement(String reglement) {
	this.reglement = reglement;
}
public String getNumTransaction() {
	return numTransaction;
}
public void setNumTransaction(String numTransaction) {
	this.numTransaction = numTransaction;
}
@Override
public String toString() {
	return "UapEntity [numAutorisation=" + numAutorisation + ", panCro=" + panCro + ", data=" + data + ", rio=" + rio
			+ ", reglement=" + reglement + "]";
}


}
