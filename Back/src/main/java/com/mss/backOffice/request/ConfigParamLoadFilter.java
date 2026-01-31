package com.mss.backOffice.request;

public class ConfigParamLoadFilter {
    private String fieldMValue;
    private String cameraValue;
    private String libelle;

    public String getFieldMValue() {
        return fieldMValue;
    }

    public String getCameraValue() {
        return cameraValue;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setFieldMValue(String fieldMValue) {
        this.fieldMValue = fieldMValue;
    }

    public void setCameraValue(String cameraValue) {
        this.cameraValue = cameraValue;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String toString() {
        return "ConfigParamLoadFilter{" +
                "fieldMValue='" + fieldMValue + '\'' +
                ", cameraValue='" + cameraValue + '\'' +
                ", libelle='" + libelle + '\'' +
                '}';
    }
}
