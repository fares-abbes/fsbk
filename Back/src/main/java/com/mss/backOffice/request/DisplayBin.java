package com.mss.backOffice.request;

import java.util.Set;

public class DisplayBin {
  public String Libelle;
  private String binTypeCode;

  private String binStatusCode;

  private String bouLowBin;

  private String bouHighBin;


  private int bouLength;

  private String primaryDestination;
  private int binLength;


  private String secondaryDestination;
  private Set<RangeDisplay> range;


  public String getLibelle() {
	return Libelle;
}

public void setLibelle(String libelle) {
	this.Libelle = libelle;
}

public String getBinTypeCode() {
    return binTypeCode;
  }

  public void setBinTypeCode(String binTypeCode) {
    this.binTypeCode = binTypeCode;
  }

  public String getBinStatusCode() {
    return binStatusCode;
  }

  public void setBinStatusCode(String binStatusCode) {
    this.binStatusCode = binStatusCode;
  }

  public String getBouLowBin() {
    return bouLowBin;
  }

  public void setBouLowBin(String bouLowBin) {
    this.bouLowBin = bouLowBin;
  }

  public String getBouHighBin() {
    return bouHighBin;
  }

  public void setBouHighBin(String bouHighBin) {
    this.bouHighBin = bouHighBin;
  }

  public int getBouLength() {
    return bouLength;
  }

  public void setBouLength(int bouLength) {
    this.bouLength = bouLength;
  }

  public String getPrimaryDestination() {
    return primaryDestination;
  }

  public void setPrimaryDestination(String primaryDestination) {
    this.primaryDestination = primaryDestination;
  }

  public int getBinLength() {
    return binLength;
  }

  public void setBinLength(int binLength) {
    this.binLength = binLength;
  }

  public String getSecondaryDestination() {
    return secondaryDestination;
  }

  public void setSecondaryDestination(String secondaryDestination) {
    this.secondaryDestination = secondaryDestination;
  }

  public Set<RangeDisplay> getRange() {
    return range;
  }

  public void setRange(Set<RangeDisplay> range) {
    this.range = range;
  }

  @Override
  public String toString() {
    return "DisplayBin{" +
        "Libelle='" + Libelle + '\'' +
        ", binTypeCode='" + binTypeCode + '\'' +
        ", binStatusCode='" + binStatusCode + '\'' +
        ", bouLowBin='" + bouLowBin + '\'' +
        ", bouHighBin='" + bouHighBin + '\'' +
        ", bouLength=" + bouLength +
        ", primaryDestination='" + primaryDestination + '\'' +
        ", binLength=" + binLength +
        ", secondaryDestination='" + secondaryDestination + '\'' +
        ", range=" + range +
        '}';
  }
}
