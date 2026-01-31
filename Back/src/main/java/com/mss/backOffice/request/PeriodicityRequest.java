package com.mss.backOffice.request;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class PeriodicityRequest {

	
	
	  public int typeCode;

      public int progCode;
	  public Date riskPeriodicityStartDate;

	 
	  public Date getRiskPeriodicityEndDate;
	 
	  public  Set<String> emvServiceValues;
      
	  public String AmountMinPeriod;
	  public String AmountMaxPeriod;
	  public String AmountLimitPeriod;
	  public String NumberLimitPeriod;

	public int getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(int typeCode) {
		this.typeCode = typeCode;
	}

	public Date getRiskPeriodicityStartDate() {
		return riskPeriodicityStartDate;
	}

	public void setRiskPeriodicityStartDate(Date riskPeriodicityStartDate) {
		this.riskPeriodicityStartDate = riskPeriodicityStartDate;
	}

	public Date getGetRiskPeriodicityEndDate() {
		return getRiskPeriodicityEndDate;
	}

	public void setGetRiskPeriodicityEndDate(Date getRiskPeriodicityEndDate) {
		this.getRiskPeriodicityEndDate = getRiskPeriodicityEndDate;
	}

	public Set<String> getEmvServiceValues() {
		return emvServiceValues;
	}

	public void setEmvServiceValues(Set<String> emvServiceValues) {
		this.emvServiceValues = emvServiceValues;
	}


	@Override
	public String toString() {
		return "PeriodicityRequest{" +
				"typeCode=" + typeCode +
				", progCode=" + progCode +
				", riskPeriodicityStartDate=" + riskPeriodicityStartDate +
				", getRiskPeriodicityEndDate=" + getRiskPeriodicityEndDate +
				", emvServiceValues=" + emvServiceValues +
				", AmountMinPeriod='" + AmountMinPeriod + '\'' +
				", AmountMaxPeriod='" + AmountMaxPeriod + '\'' +
				", AmountLimitPeriod='" + AmountLimitPeriod + '\'' +
				", NumberLimitPeriod='" + NumberLimitPeriod + '\'' +
				'}';
	}
}
