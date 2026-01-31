package com.mss.backOffice.request;

public class CustomerDisplay {

  private int customerCode;
  private String identityCode;
  private Integer globalRiskCode;
  private Integer customerStatusCode;
  private String customerIdentidy;
  private String customerName;
  private String customerAddress;
  private String customerGSM;
  private String customerCity;
  private String customerTown;
  private String customerPostal;
  private String status;
  private String radical;
  private String firstName;
  private String lastName;

  
  
  public String getFirstName() {
	return firstName;
}

public void setFirstName(String firstName) {
	this.firstName = firstName;
}

public String getLastName() {
	return lastName;
}

public void setLastName(String lastName) {
	this.lastName = lastName;
}

public String getRadical() {
	return radical;
}

public void setRadical(String radical) {
	this.radical = radical;
}

public CustomerDisplay() {
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public CustomerDisplay(int customerCode, String identityCode, Integer globalRiskCode,
      Integer customerStatusCode, String customerIdentidy, String customerName,
      String customerAddress, String customerGSM, String customerCity, String customerTown,
      String customerPostal, String status) {
    this.customerCode = customerCode;
    this.identityCode = identityCode;
    this.globalRiskCode = globalRiskCode;
    this.customerStatusCode = customerStatusCode;
    this.customerIdentidy = customerIdentidy;
    this.customerName = customerName;
    this.customerAddress = customerAddress;
    this.customerGSM = customerGSM;
    this.customerCity = customerCity;
    this.customerTown = customerTown;
    this.customerPostal = customerPostal;
    this.status = status;
  }

  public int getCustomerCode() {
    return customerCode;
  }

  public void setCustomerCode(int customerCode) {
    this.customerCode = customerCode;
  }



  public String getIdentityCode() {
	return identityCode;
}

public void setIdentityCode(String identityCode) {
	this.identityCode = identityCode;
}

public Integer getGlobalRiskCode() {
    return globalRiskCode;
  }

  public void setGlobalRiskCode(Integer globalRiskCode) {
    this.globalRiskCode = globalRiskCode;
  }

  public Integer getCustomerStatusCode() {
    return customerStatusCode;
  }

  public void setCustomerStatusCode(Integer customerStatusCode) {
    this.customerStatusCode = customerStatusCode;
  }

  public String getCustomerIdentidy() {
    return customerIdentidy;
  }

  public void setCustomerIdentidy(String customerIdentidy) {
    this.customerIdentidy = customerIdentidy;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public String getCustomerAddress() {
    return customerAddress;
  }

  public void setCustomerAddress(String customerAddress) {
    this.customerAddress = customerAddress;
  }

  public String getCustomerGSM() {
    return customerGSM;
  }

  public void setCustomerGSM(String customerGSM) {
    this.customerGSM = customerGSM;
  }

  public String getCustomerCity() {
    return customerCity;
  }

  public void setCustomerCity(String customerCity) {
    this.customerCity = customerCity;
  }

  public String getCustomerTown() {
    return customerTown;
  }

  public void setCustomerTown(String customerTown) {
    this.customerTown = customerTown;
  }

  public String getCustomerPostal() {
    return customerPostal;
  }

  public void setCustomerPostal(String customerPostal) {
    this.customerPostal = customerPostal;
  }

  @Override
  public String toString() {
    return "CustomerDisplay{" +
        "customerCode=" + customerCode +
        ", identityCode=" + identityCode +
        ", globalRiskCode=" + globalRiskCode +
        ", customerStatusCode=" + customerStatusCode +
        ", customerIdentidy='" + customerIdentidy + '\'' +
        ", customerName='" + customerName + '\'' +
        ", customerAddress='" + customerAddress + '\'' +
        ", customerGSM='" + customerGSM + '\'' +
        ", customerCity='" + customerCity + '\'' +
        ", customerTown='" + customerTown + '\'' +
        ", customerPostal='" + customerPostal + '\'' +
        ", status='" + status + '\'' +
        '}';
  }
}
