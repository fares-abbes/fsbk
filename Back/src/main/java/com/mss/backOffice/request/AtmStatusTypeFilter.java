package com.mss.backOffice.request;

public class AtmStatusTypeFilter {
    private String statusType;
    private String statusLibelle;

    public String getStatusType() {
        return statusType;
    }

    public String getStatusLibelle() {
        return statusLibelle;
    }

    public void setStatusType(String statusType) {
        this.statusType = statusType;
    }

    public void setStatusLibelle(String statusLibelle) {
        this.statusLibelle = statusLibelle;
    }

    @Override
    public String toString() {
        return "AtmStatusTypeFilter{" +
                "statusType='" + statusType + '\'' +
                ", statusLibelle='" + statusLibelle + '\'' +
                '}';
    }
}
