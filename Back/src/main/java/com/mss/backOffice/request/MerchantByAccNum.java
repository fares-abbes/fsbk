package com.mss.backOffice.request;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mss.unified.entities.PosTerminal;

public class MerchantByAccNum {
	
	private String username;
	private String nationalCommission;
	private String internationalCommission;
	private String city;
	private String country;
	private String zipCode;
	private String phone;
	private String address;
	
	private String agence;
	private Integer status;
	private Boolean offshore;
	
	private Integer merchantCode;

	private List<PosTerminalDispalay> posTerminals;
	

	
	
	
	public Integer getMerchantCode() {
		return merchantCode;
	}

	public void setMerchantCode(Integer merchantCode) {
		this.merchantCode = merchantCode;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getAgence() {
		return agence;
	}

	public void setAgence(String agence) {
		this.agence = agence;
	}

	public List<PosTerminalDispalay> getPosTerminals() {
		return posTerminals;
	}

	public void setPosTerminals(List<PosTerminalDispalay> posTerminals) {
		this.posTerminals = posTerminals;
	}

	public Boolean getOffshore() {
		return offshore;
	}

	public void setOffshore(Boolean offshore) {
		this.offshore = offshore;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getNationalCommission() {
		return nationalCommission;
	}
	public void setNationalCommission(String nationalCommission) {
		this.nationalCommission = nationalCommission;
	}
	public String getInternationalCommission() {
		return internationalCommission;
	}
	public void setInternationalCommission(String internationalCommission) {
		this.internationalCommission = internationalCommission;
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
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "MerchantByAccNum [username=" + username + ", nationalCommission=" + nationalCommission
				+ ", internationalCommission=" + internationalCommission + ", city=" + city + ", country=" + country
				+ ", zipCode=" + zipCode + ", phone=" + phone + ", address=" + address + ", agence=" + agence
				+ ", status=" + status + ", offshore=" + offshore + ", merchantCode=" + merchantCode + ", posTerminals="
				+ posTerminals + "]";
	}

	
	

}
