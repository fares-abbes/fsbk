package com.mss.backOffice.request;

import java.util.List;

import com.mss.unified.entities.CommissionTpe;

public class RequestCommissionTpe {
	  private List<CommissionTpeRequest> commissionTpes;
	  private List<CommissionTpeRequest> commissionTpesInter;
	  private Integer commissionTypeCode;
	  
	  private String commissionInterNational;
	  private String commissionNational;
	    private String montantLoyer;

	
	  
	  




	public String getMontantLoyer() {
			return montantLoyer;
		}



		public void setMontantLoyer(String montantLoyer) {
			this.montantLoyer = montantLoyer;
		}



	public String getCommissionInterNational() {
		return commissionInterNational;
	}



	public void setCommissionInterNational(String commissionInterNational) {
		this.commissionInterNational = commissionInterNational;
	}



	public String getCommissionNational() {
		return commissionNational;
	}



	public void setCommissionNational(String commissionNational) {
		this.commissionNational = commissionNational;
	}



	public Integer getCommissionTypeCode() {
		return commissionTypeCode;
	}



	public void setCommissionTypeCode(Integer commissionTypeCode) {
		this.commissionTypeCode = commissionTypeCode;
	}



	public List<CommissionTpeRequest> getCommissionTpes() {
		return commissionTpes;
	}



	public void setCommissionTpes(List<CommissionTpeRequest> commissionTpes) {
		this.commissionTpes = commissionTpes;
	}



	public List<CommissionTpeRequest> getCommissionTpesInter() {
		return commissionTpesInter;
	}



	public void setCommissionTpesInter(List<CommissionTpeRequest> commissionTpesInter) {
		this.commissionTpesInter = commissionTpesInter;
	}



	@Override
	public String toString() {
		return "RequestCommissionTpe [commissionTpes=" + commissionTpes + ", commissionTpesInter=" + commissionTpesInter
				+ ", commissionTypeCode=" + commissionTypeCode + ", commissionInterNational=" + commissionInterNational
				+ ", commissionNational=" + commissionNational + "]";
	}




	
	  
	  
}
