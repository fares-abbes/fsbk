package com.mss.backOffice.request;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

public class AtmTerminalDisplay {




        private String ateId;

        private String mcc;


        private int merchantCode;


        private int terstatCode;
        private String merchantId;

        private String atmConNum;


        private String ateLibelle;


        private String zmkKey;


        private String zmkKcv;


        private String tmkKey;


        private String tmkKcv;


        private String TPK_KEY;


        private String TPK_KCV;


        private String ipAdresse;


        private String portCon;


        private String etatTerminal;


        private String capturedCardNumber;


        private String envelopesDeposited;


        private String atmTerminalTsn;


        private String model;

        private String marque;


        public String getMerchantId() {
			return merchantId;
		}

		public void setMerchantId(String merchantId) {
			this.merchantId = merchantId;
		}

		public String getIpAdresse() {
            return ipAdresse;
        }

        public void setIpAdresse(String ipAdresse) {
            this.ipAdresse = ipAdresse;
        }

        public String getPortCon() {
            return portCon;
        }

        public void setPortCon(String portCon) {
            this.portCon = portCon;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getMarque() {
            return marque;
        }

        public void setMarque(String marque) {
            this.marque = marque;
        }

        public String getAtmTerminalTsn() {
            return atmTerminalTsn;
        }

        public void setAtmTerminalTsn(String atmTerminalTsn) {
            this.atmTerminalTsn = atmTerminalTsn;
        }

        public String getAteId() {
            return ateId;
        }

        public void setAteId(String ateId) {
            this.ateId = ateId;
        }

        public String getMcc() {
            return mcc;
        }

        public void setMcc(String mcc) {
            this.mcc = mcc;
        }

        public int getMerchantCode() {
            return merchantCode;
        }

        public void setMerchantCode(int merchantCode) {
            this.merchantCode = merchantCode;
        }

        public int getTerstatCode() {
            return terstatCode;
        }

        public void setTerstatCode(int terstatCode) {
            this.terstatCode = terstatCode;
        }

        public String getAtmConNum() {
            return atmConNum;
        }

        public void setAtmConNum(String atmConNum) {
            this.atmConNum = atmConNum;
        }

        public String getAteLibelle() {
            return ateLibelle;
        }

        public void setAteLibelle(String ateLibelle) {
            this.ateLibelle = ateLibelle;
        }

        public String getZmkKey() {
            return zmkKey;
        }

        public void setZmkKey(String zmkKey) {
            this.zmkKey = zmkKey;
        }

        public String getTmkKey() {
            return tmkKey;
        }

        public void setTmkKey(String tmkKey) {
            this.tmkKey = tmkKey;
        }

        public String getTPK_KEY() {
            return TPK_KEY;
        }

        public void setTPK_KEY(String tPK_KEY) {
            TPK_KEY = tPK_KEY;
        }



        public String getPORT() {
            return portCon;
        }

        public void setPORT(String pORT) {
            portCon = pORT;
        }

        public String getEtatTerminal() {
            return etatTerminal;
        }

        public void setEtatTerminal(String etatTerminal) {
            this.etatTerminal = etatTerminal;
        }

        public String getCapturedCardNumber() {
            return capturedCardNumber;
        }

        public void setCapturedCardNumber(String capturedCardNumber) {
            this.capturedCardNumber = capturedCardNumber;
        }

        public String getEnvelopesDeposited() {
            return envelopesDeposited;
        }

        public void setEnvelopesDeposited(String envelopesDeposited) {
            this.envelopesDeposited = envelopesDeposited;
        }


        public String getZmkKcv() {
            return zmkKcv;
        }

        public void setZmkKcv(String zmkKcv) {
            this.zmkKcv = zmkKcv;
        }

        public String getTmkKcv() {
            return tmkKcv;
        }

        public void setTmkKcv(String tmkKcv) {
            this.tmkKcv = tmkKcv;
        }

        public String getTPK_KCV() {
            return TPK_KCV;
        }

        public void setTPK_KCV(String tPK_KCV) {
            TPK_KCV = tPK_KCV;
        }


}
