package com.mss.backOffice.request;

public class TotalComponsationFilter {
    String  type;
    String nombreBrutIncoming;
    String date;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNombreBrutIncoming() {
        return nombreBrutIncoming;
    }

    public void setNombreBrutIncoming(String nombreBrutIncoming) {
        this.nombreBrutIncoming = nombreBrutIncoming;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
