package com.mss.backOffice.request;

public class FiltredVisaOutgoingRecherche {
    private String transactionDate;
    private String dateCompensation;
    private String pan;
    private String retrRefNumber;
    private String responseMessageType;
    private String settlementDate;
    private String responseCode;
    private String atmCode;
    private String cmpDateFin;
    private String trxDateFin;
    private boolean accepted;
    private boolean nab;
    private boolean rejected;
    public String getRetrRefNumber() {
        return retrRefNumber;
    }

    public void setRetrRefNumber(String retrRefNumber) {
        this.retrRefNumber = retrRefNumber;
    }

    public String getAtmCode() {
        return atmCode;
    }

    public void setAtmCode(String atmCode) {
        this.atmCode = atmCode;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDateCompensation() {
        return dateCompensation;
    }

    public void setDateCompensation(String dateCompensation) {
        this.dateCompensation = dateCompensation;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getResponseMessageType() {
        return responseMessageType;
    }

    public void setResponseMessageType(String responseMessageType) {
        this.responseMessageType = responseMessageType;
    }

    public String getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(String settlementDate) {
        this.settlementDate = settlementDate;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getCmpDateFin() {
        return cmpDateFin;
    }

    public void setCmpDateFin(String cmpDateFin) {
        this.cmpDateFin = cmpDateFin;
    }

    public String getTrxDateFin() {
        return trxDateFin;
    }

    public void setTrxDateFin(String trxDateFin) {
        this.trxDateFin = trxDateFin;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isNab() {
        return nab;
    }

    public void setNab(boolean nab) {
        this.nab = nab;
    }

    public boolean isRejected() {
        return rejected;
    }

    public void setRejected(boolean rejected) {
        this.rejected = rejected;
    }

    @Override
    public String toString() {
        return "FiltredVisaOutgoingRecherche{" +
                "transactionDate='" + transactionDate + '\'' +
                ", dateCompensation='" + dateCompensation + '\'' +
                ", pan='" + pan + '\'' +
                ", retrRefNumber='" + retrRefNumber + '\'' +
                ", responseMessageType='" + responseMessageType + '\'' +
                ", settlementDate='" + settlementDate + '\'' +
                ", responseCode='" + responseCode + '\'' +
                ", atmCode='" + atmCode + '\'' +
                ", cmpDateFin='" + cmpDateFin + '\'' +
                ", trxDateFin='" + trxDateFin + '\'' +
                ", accepted=" + accepted +
                ", nab=" + nab +
                ", rejected=" + rejected +
                '}';
    }
}
