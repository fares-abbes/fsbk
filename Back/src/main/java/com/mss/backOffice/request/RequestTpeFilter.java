package com.mss.backOffice.request;

import java.util.Date;

public class RequestTpeFilter {

private String agence;
private String status;
private String numCompte;
private Date  dateDebut;
private Date dateFin;


  public String getAgence() {
    return agence;
  }

  public void setAgence(String agence) {
    this.agence = agence;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getNumCompte() {
    return numCompte;
  }

  public void setNumCompte(String numCompte) {
    this.numCompte = numCompte;
  }

  public Date getDateDebut() {
    return dateDebut;
  }

  public void setDateDebut(Date dateDebut) {
    this.dateDebut = dateDebut;
  }

  public Date getDateFin() {
    return dateFin;
  }

  public void setDateFin(Date dateFin) {
    this.dateFin = dateFin;
  }


  @Override
  public String toString() {
    return "RequestTpeFilter{" +
        "agence='" + agence + '\'' +
        ", status='" + status + '\'' +
        ", numCompte='" + numCompte + '\'' +
        ", dateDebut=" + dateDebut +
        ", dateFin=" + dateFin +
        '}';
  }
}
