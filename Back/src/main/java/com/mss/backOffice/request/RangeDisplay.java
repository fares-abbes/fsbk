package com.mss.backOffice.request;

public class RangeDisplay {

  private int rangeCode;
  private String bouHighRange;


  private String bouLowRange;

  private int rangeStatusCode;


  public String getBouHighRange() {
    return bouHighRange;
  }

  public void setBouHighRange(String bouHighRange) {
    this.bouHighRange = bouHighRange;
  }

  public String getBouLowRange() {
    return bouLowRange;
  }

  public void setBouLowRange(String bouLowRange) {
    this.bouLowRange = bouLowRange;
  }



  public int getRangeCode() {
	return rangeCode;
}

public void setRangeCode(int rangeCode) {
	this.rangeCode = rangeCode;
}

public int getRangeStatusCode() {
	return rangeStatusCode;
}

public void setRangeStatusCode(int rangeStatusCode) {
	this.rangeStatusCode = rangeStatusCode;
}

public RangeDisplay() {
  }

  public RangeDisplay(int rangeCode,String bouHighRange, String bouLowRange, int rangeStatusCode) {
    this.rangeCode=rangeCode;
	this.bouHighRange = bouHighRange;
    this.bouLowRange = bouLowRange;
    this.rangeStatusCode = rangeStatusCode;
  }


  @Override
  public String toString() {
    return "RangeDisplay{" +
        "rangeCode=" + rangeCode +
        ", bouHighRange='" + bouHighRange + '\'' +
        ", bouLowRange='" + bouLowRange + '\'' +
        ", rangeStatusCode=" + rangeStatusCode +
        '}';
  }
}
