package com.mss.backOffice.request;


import java.util.Date;
import java.util.Set;

public
class ProgramRequest {


  public String libelle;

  public int binOnUs;

  public int cprLifeCycle;

  public boolean cprRenewl;

  public boolean cprContacless;

  public boolean cprChip;

  public int cprRiskAmountMin;

  public int cprRiskAmountMax;

  public int cprRiskAmount;
  private Set<String> controlLists;
  private Set<String> emvServiceValues;
  public int riskPeriodicity;
  public int FirstPosition;
  public int SecondPosition;
  public int ThirdPosition;
  public int GlNumber;
  public Date StartDate;
  public String tvrValue;
  public String cvrValue;
  public Set<Integer> posEntryModeType;
  public Set<Integer> countriesControl;

  private Integer maxAmountContactless;
  private Integer minAmountContactless;
  private Boolean contactlessWithPin;
  private String cmsCardType;
  private String cardvisualCode;
  private int daysNumberBeforeObliterate;
  private String currency;
  
  
  public String getCurrency() {
	return currency;
}

public void setCurrency(String currency) {
	this.currency = currency;
}

public int getDaysNumberBeforeObliterate() {
	return daysNumberBeforeObliterate;
}

public void setDaysNumberBeforeObliterate(int daysNumberBeforeObliterate) {
	this.daysNumberBeforeObliterate = daysNumberBeforeObliterate;
}

public String getCardvisualCode() {
	return cardvisualCode;
}

public void setCardvisualCode(String cardvisualCode) {
	this.cardvisualCode = cardvisualCode;
}

public String getCmsCardType() {
	return cmsCardType;
}

public void setCmsCardType(String cmsCardType) {
	this.cmsCardType = cmsCardType;
}

public Integer getMaxAmountContactless() {
	return maxAmountContactless;
}

public void setMaxAmountContactless(Integer maxAmountContactless) {
	this.maxAmountContactless = maxAmountContactless;
}

public Integer getMinAmountContactless() {
	return minAmountContactless;
}

public void setMinAmountContactless(Integer minAmountContactless) {
	this.minAmountContactless = minAmountContactless;
}

public Boolean getContactlessWithPin() {
	return contactlessWithPin;
}

public void setContactlessWithPin(Boolean contactlessWithPin) {
	this.contactlessWithPin = contactlessWithPin;
}

public Set<Integer> getCountriesControl() {
	return countriesControl;
}

public void setCountriesControl(Set<Integer> countriesControl) {
	this.countriesControl = countriesControl;
}

public Set<Integer> getPosEntryModeType() {
    return posEntryModeType;
  }

  public void setPosEntryModeType(Set<Integer> posEntryModeType) {
    this.posEntryModeType = posEntryModeType;
  }

  @Override
  public String toString() {
    return "ProgramRequest{" +
            "libelle='" + libelle + '\'' +
            ", binOnUs=" + binOnUs +
            ", cprLifeCycle=" + cprLifeCycle +
            ", cprRenewl=" + cprRenewl +
            ", cprContacless=" + cprContacless +
            ", cprChip=" + cprChip +
            ", cprRiskAmountMin=" + cprRiskAmountMin +
            ", cprRiskAmountMax=" + cprRiskAmountMax +
            ", cprRiskAmount=" + cprRiskAmount +
            ", controlLists=" + controlLists +
            ", emvServiceValues=" + emvServiceValues +
            ", riskPeriodicity=" + riskPeriodicity +
            ", FirstPosition=" + FirstPosition +
            ", SecondPosition=" + SecondPosition +
            ", ThirdPosition=" + ThirdPosition +
            ", GlNumber=" + GlNumber +
            ", StartDate=" + StartDate +
            ", tvrValue='" + tvrValue + '\'' +
            ", cvrValue='" + cvrValue + '\'' +
            ", posEntryModeType=" + posEntryModeType +
            '}';
  }

  public String getLibelle() {
    return libelle;
  }

  public void setLibelle(String libelle) {
    this.libelle = libelle;
  }


  public int getFirstPosition() {
    return FirstPosition;
  }

  public void setFirstPosition(int firstPosition) {
    FirstPosition = firstPosition;
  }

  public int getSecondPosition() {
    return SecondPosition;
  }

  public void setSecondPosition(int secondPosition) {
    SecondPosition = secondPosition;
  }

  public int getThirdPosition() {
    return ThirdPosition;
  }

  public void setThirdPosition(int thirdPosition) {
    ThirdPosition = thirdPosition;
  }

  public int getGlNumber() {
    return GlNumber;
  }

  public void setGlNumber(int glNumber) {
    GlNumber = glNumber;
  }

  public Date getStartDate() {
    return StartDate;
  }

  public void setStartDate(Date startDate) {
    StartDate = startDate;
  }

  public String getTvrValue() {
    return tvrValue;
  }

  public void setTvrValue(String tvrValue) {
    this.tvrValue = tvrValue;
  }

  public String getCvrValue() {
    return cvrValue;
  }

  public void setCvrValue(String cvrValue) {
    this.cvrValue = cvrValue;
  }

  public int getBinOnUs() {
    return binOnUs;
  }

  public void setBinOnUs(int binOnUs) {
    this.binOnUs = binOnUs;
  }

  public int getCprLifeCycle() {
    return cprLifeCycle;
  }

  public void setCprLifeCycle(int cprLifeCycle) {
    this.cprLifeCycle = cprLifeCycle;
  }

  public boolean isCprRenewl() {
    return cprRenewl;
  }

  public void setCprRenewl(boolean cprRenewl) {
    this.cprRenewl = cprRenewl;
  }

  public boolean isCprContacless() {
    return cprContacless;
  }

  public void setCprContacless(boolean cprContacless) {
    this.cprContacless = cprContacless;
  }

  public boolean isCprChip() {
    return cprChip;
  }

  public void setCprChip(boolean cprChip) {
    this.cprChip = cprChip;
  }

  public int getCprRiskAmountMin() {
    return cprRiskAmountMin;
  }

  public void setCprRiskAmountMin(int cprRiskAmountMin) {
    this.cprRiskAmountMin = cprRiskAmountMin;
  }

  public int getCprRiskAmountMax() {
    return cprRiskAmountMax;
  }

  public void setCprRiskAmountMax(int cprRiskAmountMax) {
    this.cprRiskAmountMax = cprRiskAmountMax;
  }

  public int getCprRiskAmount() {
    return cprRiskAmount;
  }

  public void setCprRiskAmount(int cprRiskAmount) {
    this.cprRiskAmount = cprRiskAmount;
  }

  public Set<String> getControlLists() {
    return controlLists;
  }

  public void setControlLists(Set<String> controlLists) {
    this.controlLists = controlLists;
  }

  public Set<String> getEmvServiceValues() {
    return emvServiceValues;
  }

  public void setEmvServiceValues(Set<String> emvServiceValues) {
    this.emvServiceValues = emvServiceValues;
  }

public int getRiskPeriodicity() {
	return riskPeriodicity;
}

public void setRiskPeriodicity(int riskPeriodicity) {
	this.riskPeriodicity = riskPeriodicity;
}

  


}
