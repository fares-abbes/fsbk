package com.mss.backOffice.request;

import java.math.BigInteger;
import java.util.Date;


public class SwitchDetails {
	
	private Date switchRequestDate;
	private Date switchResponseDate;
	private String switchMtiMessage;
	private String switchPan;
	private String transactionCode;
	private String transactionLabel;
	private String switchAmountTransaction;
	private String expiryDate;
	private String currency;
	private String accountCurrency;
    private String mcc;
    private String switchRRN;
    private String switchAuthNumber;
    private String switchResponseCode;
    private String terminalId;
    private String merchantCode;
    private String merchantName;
    private String motif;
   
    private String plafondGlobalDisponible;
    private String nbDisponibleGlobal;
    private String soldeDisponible;
    private String source;
    private String bankFiid;
    private String accountNum;
    private String posEntryMode;
    private String status;

   
    
    
	public String getAccountCurrency() {
		return accountCurrency;
	}
	public void setAccountCurrency(String accountCurrency) {
		this.accountCurrency = accountCurrency;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPosEntryMode() {
		return posEntryMode;
	}
	public void setPosEntryMode(String posEntryMode) {
		this.posEntryMode = posEntryMode;
	}
	public String getAccountNum() {
		return accountNum;
	}
	public void setAccountNum(String accountNum) {
		this.accountNum = accountNum;
	}
	public String getPlafondGlobalDisponible() {
		return plafondGlobalDisponible;
	}
	public void setPlafondGlobalDisponible(String plafondGlobalDisponible) {
		this.plafondGlobalDisponible = plafondGlobalDisponible;
	}
	public String getNbDisponibleGlobal() {
		return nbDisponibleGlobal;
	}
	public void setNbDisponibleGlobal(String nbDisponibleGlobal) {
		this.nbDisponibleGlobal = nbDisponibleGlobal;
	}

	public String getSoldeDisponible() {
		return soldeDisponible;
	}
	public void setSoldeDisponible(String soldeDisponible) {
		this.soldeDisponible = soldeDisponible;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getBankFiid() {
		return bankFiid;
	}
	public void setBankFiid(String bankFiid) {
		this.bankFiid = bankFiid;
	}
	public String getTransactionLabel() {
		return transactionLabel;
	}
	public String getMotif() {
		return motif;
	}
	public void setMotif(String motif) {
		this.motif = motif;
	}
	public void setTransactionLabel(String transactionLabel) {
		this.transactionLabel = transactionLabel;
	}
	public Date getSwitchRequestDate() {
		return switchRequestDate;
	}
	public void setSwitchRequestDate(Date switchRequestDate) {
		this.switchRequestDate = switchRequestDate;
	}
	public Date getSwitchResponseDate() {
		return switchResponseDate;
	}
	public void setSwitchResponseDate(Date switchResponseDate) {
		this.switchResponseDate = switchResponseDate;
	}
	public String getSwitchMtiMessage() {
		return switchMtiMessage;
	}
	public void setSwitchMtiMessage(String switchMtiMessage) {
		this.switchMtiMessage = switchMtiMessage;
	}
	public String getSwitchPan() {
		return switchPan;
	}
	public void setSwitchPan(String switchPan) {
		this.switchPan = switchPan;
	}
	public String getTransactionCode() {
		return transactionCode;
	}
	public void setTransactionCode(String transactionCode) {
		this.transactionCode = transactionCode;
	}
	public String getSwitchAmountTransaction() {
		return switchAmountTransaction;
	}
	public void setSwitchAmountTransaction(String switchAmountTransaction) {
		this.switchAmountTransaction = switchAmountTransaction;
	}

	public String getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getMcc() {
		return mcc;
	}
	public void setMcc(String mcc) {
		this.mcc = mcc;
	}
	public String getSwitchRRN() {
		return switchRRN;
	}
	public void setSwitchRRN(String switchRRN) {
		this.switchRRN = switchRRN;
	}
	public String getSwitchAuthNumber() {
		return switchAuthNumber;
	}
	public void setSwitchAuthNumber(String switchAuthNumber) {
		this.switchAuthNumber = switchAuthNumber;
	}
	public String getSwitchResponseCode() {
		return switchResponseCode;
	}
	public void setSwitchResponseCode(String switchResponseCode) {
		this.switchResponseCode = switchResponseCode;
	}
	public String getTerminalId() {
		return terminalId;
	}
	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}
	public String getMerchantCode() {
		return merchantCode;
	}
	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

    
}
