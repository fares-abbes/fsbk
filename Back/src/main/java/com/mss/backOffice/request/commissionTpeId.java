package com.mss.backOffice.request;

public class commissionTpeId {
    private String montantRefMin;
    private String montantRefMax;

    public String getMontantRefMin() {
        return montantRefMin;
    }

    public void setMontantRefMin(String montantRefMin) {
        this.montantRefMin = montantRefMin;
    }

    public String getMontantRefMax() {
        return montantRefMax;
    }

    public void setMontantRefMax(String montantRefMax) {
        this.montantRefMax = montantRefMax;
    }

    @Override
    public String toString() {
        return "commissionTpeId{" +
                "montantRefMin='" + montantRefMin + '\'' +
                ", montantRefMax='" + montantRefMax + '\'' +
                '}';
    }
}
