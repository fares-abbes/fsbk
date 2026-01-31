package com.mss.backOffice.request;

public class AtmDisplay {

private String  ateId;
private String  atmConNum;
private String  mccCode;
private String  ateLibelle;
private String  ipAddress;
private String  etatTerminal;
private String  merchantCode;
private String  model;
private String  marque;

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getMarque() {
    return marque;
  }

  public void setMarque(String marque) {
    this.marque = marque;
  }

  public String getAteId() {
    return ateId;
  }

  public void setAteId(String ateId) {
    this.ateId = ateId;
  }

  public String getAtmConNum() {
    return atmConNum;
  }

  public void setAtmConNum(String atmConNum) {
    this.atmConNum = atmConNum;
  }

  public String getMccCode() {
    return mccCode;
  }

  public void setMccCode(String mccCode) {
    this.mccCode = mccCode;
  }

  public String getAteLibelle() {
    return ateLibelle;
  }

  public void setAteLibelle(String ateLibelle) {
    this.ateLibelle = ateLibelle;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getEtatTerminal() {
    return etatTerminal;
  }

  public void setEtatTerminal(String etatTerminal) {
    this.etatTerminal = etatTerminal;
  }

  public String getMerchantCode() {
    return merchantCode;
  }

  public void setMerchantCode(String merchantCode) {
    this.merchantCode = merchantCode;
  }


  public AtmDisplay(String ateId, String atmConNum, String mccCode, String ateLibelle,
      String idAddress, String etatTerminal, String merchantCode, String model,
      String marque) {
    this.ateId = ateId;
    this.atmConNum = atmConNum;
    this.mccCode = mccCode;
    this.ateLibelle = ateLibelle;
    this.ipAddress = idAddress;
    this.etatTerminal = etatTerminal;
    this.merchantCode = merchantCode;
    this.model = model;
    this.marque = marque;
  }

  public AtmDisplay() {
  }

  @Override
  public String toString() {
    return "AtmDisplay{" +
        "ateId='" + ateId + '\'' +
        ", atmConNum='" + atmConNum + '\'' +
        ", mccCode='" + mccCode + '\'' +
        ", ateLibelle='" + ateLibelle + '\'' +
        ", ipAddress='" + ipAddress + '\'' +
        ", etatTerminal='" + etatTerminal + '\'' +
        ", merchantCode='" + merchantCode + '\'' +
        ", model='" + model + '\'' +
        ", marque='" + marque + '\'' +
        '}';
  }
}
