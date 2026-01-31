package com.mss.backOffice.request;

import java.math.BigInteger;

public class AccountDisplay {
  private int accountCode;
  private String  accountNum;
  private String accountNumAttached;
  private BigInteger accountAuthorize;
  private BigInteger accountAvailable;
  private BigInteger accountBilling;
  private BigInteger accountRevolvingLimit;
  private String accountName;
  private int astCode;
  private String currency;
  private String status;
  private Integer idAgence;
  private BigInteger accountExceeding;
  private BigInteger accountBalance;
  private BigInteger preauthAmount;
  private BigInteger refundAmount;

  
  

public BigInteger getRefundAmount() {
	return refundAmount;
}

public void setRefundAmount(BigInteger refundAmount) {
	this.refundAmount = refundAmount;
}

public BigInteger getPreauthAmount() {
	return preauthAmount;
}

public void setPreauthAmount(BigInteger preauthAmount) {
	this.preauthAmount = preauthAmount;
}

public BigInteger getAccountExceeding() {
	return accountExceeding;
}

public void setAccountExceeding(BigInteger accountExceeding) {
	this.accountExceeding = accountExceeding;
}

public BigInteger getAccountBalance() {
	return accountBalance;
}

public void setAccountBalance(BigInteger accountBalance) {
	this.accountBalance = accountBalance;
}

public Integer getIdAgence() {
	return idAgence;
}

public void setIdAgence(Integer idAgence) {
	this.idAgence = idAgence;
}

public int getAccountCode() {
    return accountCode;
  }

  public void setAccountCode(int accountCode) {
    this.accountCode = accountCode;
  }

  public String getAccountNum() {
    return accountNum;
  }

  public void setAccountNum(String accountNum) {
    this.accountNum = accountNum;
  }

  public String getAccountNumAttached() {
    return accountNumAttached;
  }

  public void setAccountNumAttached(String accountNumAttached) {
    this.accountNumAttached = accountNumAttached;
  }

  public BigInteger getAccountAuthorize() {
    return accountAuthorize;
  }

  public void setAccountAuthorize(BigInteger accountAuthorize) {
    this.accountAuthorize = accountAuthorize;
  }

  public BigInteger getAccountAvailable() {
    return accountAvailable;
  }

  public void setAccountAvailable(BigInteger accountAvailable) {
    this.accountAvailable = accountAvailable;
  }

  public BigInteger getAccountBilling() {
    return accountBilling;
  }

  public void setAccountBilling(BigInteger accountBilling) {
    this.accountBilling = accountBilling;
  }

  public BigInteger getAccountRevolvingLimit() {
    return accountRevolvingLimit;
  }

  public void setAccountRevolvingLimit(BigInteger accountRevolvingLimit) {
    this.accountRevolvingLimit = accountRevolvingLimit;
  }

  public String getAccountName() {
    return accountName;
  }

  public void setAccountName(String accountName) {
    this.accountName = accountName;
  }

  public int getAstCode() {
    return astCode;
  }

  public void setAstCode(int astCode) {
    this.astCode = astCode;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public AccountDisplay() {
  }

  public AccountDisplay(int accountCode,String accountNum, String accountNumAttached, BigInteger accountAuthorize,
      BigInteger accountAvailable, BigInteger accountBilling, BigInteger accountRevolvingLimit, String accountName,
      int astCode, String currency,String status,Integer idAgence,BigInteger accountExceeding,BigInteger accountBalance,BigInteger preauthAmount,BigInteger refundAmount) {
  this.accountCode=accountCode;
    this.accountNum = accountNum;
    this.accountNumAttached = accountNumAttached;
    this.accountAuthorize = accountAuthorize;
    this.accountAvailable = accountAvailable;
    this.accountBilling = accountBilling;
    this.accountRevolvingLimit = accountRevolvingLimit;
    this.accountName = accountName;
    this.astCode = astCode;
    this.currency = currency;
    this.status=status;
    this.idAgence=idAgence;
    this.accountExceeding=accountExceeding;
    this.accountBalance=accountBalance;
    this.preauthAmount=preauthAmount;
    this.refundAmount=refundAmount;
  }

  @Override
  public String toString() {
    return "AccountDisplay{" +
        "accountCode=" + accountCode +
        ", accountNum='" + accountNum + '\'' +
        ", accountNumAttached='" + accountNumAttached + '\'' +
        ", accountAuthorize=" + accountAuthorize +
        ", accountAvailable=" + accountAvailable +
        ", accountBilling=" + accountBilling +
        ", accountRevolvingLimit=" + accountRevolvingLimit +
        ", accountName='" + accountName + '\'' +
        ", astCode=" + astCode +
        ", currency='" + currency + '\'' +
        ", status='" + status + '\'' +
        '}';
  }



}
