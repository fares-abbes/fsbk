package com.mss.backOffice.request;

public class AtmTerminalRequest {
    private String ateId;
    private int mccCode;
    private int merchantCode;
    private int terstatCode;
    private String ateLibelle;
    private String ipAdresse;
    private Integer model;
    private Integer marque;
    private String portCon;
    public String getAteId() {
        return ateId;
    }

    public void setAteId(String ateId) {
        this.ateId = ateId;
    }

    public int getMccCode() {
        return mccCode;
    }

    public void setMccCode(int mccCode) {
        this.mccCode = mccCode;
    }

    public int getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(int merchantCode) {
        this.merchantCode = merchantCode;
    }

    public int getTerstatCode() {
        return terstatCode;
    }

    public void setTerstatCode(int terstatCode) {
        this.terstatCode = terstatCode;
    }

    public String getAteLibelle() {
        return ateLibelle;
    }

    public void setAteLibelle(String ateLibelle) {
        this.ateLibelle = ateLibelle;
    }

    public String getIpAdresse() {
        return ipAdresse;
    }

    public void setIpAdresse(String ipAdresse) {
        this.ipAdresse = ipAdresse;
    }

    public Integer getModel() {
        return model;
    }

    public void setModel(Integer model) {
        this.model = model;
    }

    public Integer getMarque() {
        return marque;
    }

    public void setMarque(Integer marque) {
        this.marque = marque;
    }

    public String getPortCon() {
        return portCon;
    }

    public void setPortCon(String portCon) {
        this.portCon = portCon;
    }
}
