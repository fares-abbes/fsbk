package com.mss.backOffice.message.response;

import java.time.LocalDateTime;

public class ResetPinOfflineRequestsDisplay {
	

	private String cardNumber;
	
	private Integer status;
	
	private String addedBy;
	private String motif;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
	public String getCardNumber() {
		return cardNumber;
	}
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getAddedBy() {
		return addedBy;
	}
	public void setAddedBy(String addedBy) {
		this.addedBy = addedBy;
	}
	public String getMotif() {
		return motif;
	}
	public void setMotif(String motif) {
		this.motif = motif;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	public LocalDateTime getModifiedAt() {
		return modifiedAt;
	}
	public void setModifiedAt(LocalDateTime modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	
}
