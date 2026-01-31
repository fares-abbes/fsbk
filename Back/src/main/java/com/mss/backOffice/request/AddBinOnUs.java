package com.mss.backOffice.request;

import com.mss.unified.entities.Range;
import java.util.Set;

public class AddBinOnUs {


  private int binTypeCode;
  
  public String Libelle;

  private int binStatusCode;

  private String bouLowBin;

  private String bouHighBin;


  private int bouLength;

  private int primaryDestination;
  private int binLength;


  private int secondaryDestination;
  private Set<Range> range;


  public Set<Range> getRange() {
    return range;
  }

  public int getBinLength() {
    return binLength;
  }

  public void setBinLength(int binLength) {
    this.binLength = binLength;
  }

  public void setRange(Set<Range> range) {
    this.range = range;
  }

  public int getBinTypeCode() {
    return binTypeCode;
  }

  public void setBinTypeCode(int binTypeCode) {
    this.binTypeCode = binTypeCode;
  }

  public int getBinStatusCode() {
    return binStatusCode;
  }

  public void setBinStatusCode(int binStatusCode) {
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

  public int getPrimaryDestination() {
    return primaryDestination;
  }

  public void setPrimaryDestination(int primaryDestination) {
    this.primaryDestination = primaryDestination;
  }

  public int getSecondaryDestination() {
    return secondaryDestination;
  }

  public void setSecondaryDestination(int secondaryDestination) {
    this.secondaryDestination = secondaryDestination;
  }

  @Override
  public String toString() {
    return "AddBinOnUs{" +
        "binTypeCode=" + binTypeCode +
        ", Libelle='" + Libelle + '\'' +
        ", binStatusCode=" + binStatusCode +
        ", bouLowBin='" + bouLowBin + '\'' +
        ", bouHighBin='" + bouHighBin + '\'' +
        ", bouLength=" + bouLength +
        ", primaryDestination=" + primaryDestination +
        ", binLength=" + binLength +
        ", secondaryDestination=" + secondaryDestination +
        ", range=" + range +
        '}';
  }
}
