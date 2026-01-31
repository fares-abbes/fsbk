package com.mss.backOffice.request;


public class BinStockDTO {
private  String libelle;
private  int codeBinOnUs;

private  int stockInitial;

private  int stockConsomé;

private  int stockDisponible;
private  int stockReserve;

public String getLibelle() {
	return libelle;
}
public void setLibelle(String libelle) {
	this.libelle = libelle;
}
public int getStockInitial() {
	return stockInitial;
}
public void setStockInitial(int stockInitial) {
	this.stockInitial = stockInitial;
}
public int getStockConsomé() {
	return stockConsomé;
}
public void setStockConsomé(int stockConsomé) {
	this.stockConsomé = stockConsomé;
}
public int getStockDisponible() {
	return stockDisponible;
}
public void setStockDisponible(int stockDisponible) {
	this.stockDisponible = stockDisponible;
}
public int getStockReserve() {
	return stockReserve;
}
public void setStockReserve(int stockReserve) {
	this.stockReserve = stockReserve;
}
public int getCodeBinOnUs() {
	return codeBinOnUs;
}
public void setCodeBinOnUs(int codeBinOnUs) {
	this.codeBinOnUs = codeBinOnUs;
}


   
}
