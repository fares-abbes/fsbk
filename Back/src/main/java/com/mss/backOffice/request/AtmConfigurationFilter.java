package com.mss.backOffice.request;

public class AtmConfigurationFilter {
    private  String amConfigLibelle;

    public String getAmConfigLibelle() {
        return amConfigLibelle;
    }

    public void setAmConfigLibelle(String amConfigLibelle) {
        this.amConfigLibelle = amConfigLibelle;
    }

    @Override
    public String toString() {
        return "AtmConfigurationFilter{" +
                "amConfigLibelle='" + amConfigLibelle + '\'' +
                '}';
    }
}
