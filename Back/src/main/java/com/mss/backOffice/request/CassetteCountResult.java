package com.mss.backOffice.request;

public
class CassetteCountResult {

  private String atmCassetteNoteIn;
  private String atmCassetteNoteRejected;
  private String atmCassetteNoteDispensed;
  private String atmCassetteNoteLastTrans;
  private String atmCassTerminalId;
  private String atmCassNumberId;
  private String atmCassetteValue;
  private String status;

  public String getAtmCassetteValue() {
    return atmCassetteValue;
  }

  public void setAtmCassetteValue(String atmCassetteValue) {
    this.atmCassetteValue = atmCassetteValue;
  }

  public String getAtmCassetteNoteIn() {
    return atmCassetteNoteIn;
  }

  public void setAtmCassetteNoteIn(String atmCassetteNoteIn) {
    this.atmCassetteNoteIn = atmCassetteNoteIn;
  }

  public String getAtmCassetteNoteRejected() {
    return atmCassetteNoteRejected;
  }

  public void setAtmCassetteNoteRejected(String atmCassetteNoteRejected) {
    this.atmCassetteNoteRejected = atmCassetteNoteRejected;
  }

  public String getAtmCassetteNoteDispensed() {
    return atmCassetteNoteDispensed;
  }

  public void setAtmCassetteNoteDispensed(String atmCassetteNoteDispensed) {
    this.atmCassetteNoteDispensed = atmCassetteNoteDispensed;
  }

  public String getAtmCassetteNoteLastTrans() {
    return atmCassetteNoteLastTrans;
  }

  public void setAtmCassetteNoteLastTrans(String atmCassetteNoteLastTrans) {
    this.atmCassetteNoteLastTrans = atmCassetteNoteLastTrans;
  }

  public String getAtmCassTerminalId() {
    return atmCassTerminalId;
  }

  public void setAtmCassTerminalId(String atmCassTerminalId) {
    this.atmCassTerminalId = atmCassTerminalId;
  }

  public String getAtmCassNumberId() {
    return atmCassNumberId;
  }

  public void setAtmCassNumberId(String atmCassNumberId) {
    this.atmCassNumberId = atmCassNumberId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "CassetteCountResult{" +
        "atmCassetteNoteIn='" + atmCassetteNoteIn + '\'' +
        ", atmCassetteNoteRejected='" + atmCassetteNoteRejected + '\'' +
        ", atmCassetteNoteDispensed='" + atmCassetteNoteDispensed + '\'' +
        ", atmCassetteNoteLastTrans='" + atmCassetteNoteLastTrans + '\'' +
        ", atmCassTerminalId='" + atmCassTerminalId + '\'' +
        ", atmCassNumberId='" + atmCassNumberId + '\'' +
        ", atmCassetteValue='" + atmCassetteValue + '\'' +
        '}';
  }
}
