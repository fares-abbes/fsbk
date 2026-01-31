package com.mss.backOffice.request;

public
class MerchantRequest {


  private String merchantId;

  private String merchantLibelle;

  private String city;

  private String country;

  private String codeZip;

  private String phone;
  private String commissionNational;

  private String commissionInternational;
private String address;

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }
  

  public String getCommissionNational() {
    return commissionNational;
  }

  public void setCommissionNational(String commissionNational) {
    this.commissionNational = commissionNational;
  }

  public String getCommissionInternational() {
    return commissionInternational;
  }

  public void setCommissionInternational(String commissionInternational) {
    this.commissionInternational = commissionInternational;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getCodeZip() {
    return codeZip;
  }

  public void setCodeZip(String codeZip) {
    this.codeZip = codeZip;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getMerchantId() {
    return merchantId;
  }

  public void setMerchantId(String merchantId) {
    this.merchantId = merchantId;
  }

  public String getMerchantLibelle() {
    return merchantLibelle;
  }

  public void setMerchantLibelle(String merchantLibelle) {
    this.merchantLibelle = merchantLibelle;
  }

  @Override
  public String toString() {
    return "MerchantRequest{" +
        "merchantId='" + merchantId + '\'' +
        ", merchantLibelle='" + merchantLibelle + '\'' +
        ", city='" + city + '\'' +
        ", country='" + country + '\'' +
        ", codeZip='" + codeZip + '\'' +
        ", phone='" + phone + '\'' +
        ", commissionNational='" + commissionNational + '\'' +
        ", commissionInternational='" + commissionInternational + '\'' +
        ", address='" + address + '\'' +
        '}';
  }
}
