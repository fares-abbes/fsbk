package com.mss.backOffice.request;

import java.util.HashSet;
import java.util.Set;
import com.mss.unified.entities.PosTerminal;

public class PosAllowedTransConfDisplayDetails {
	private int patc_code;
	private String patc_libelle;
	private Set<PosTerminal> pos_terminal= new HashSet<>();
	private Integer allowed_trans_id;
	private int normal_purchase;
	private int preauth_purshase;
	private int completion;
	private int mail_tel_order;
	private int marchandise_return;
	private int cash_advance;
	private int card_verification;
	private int balance_inquery;
	private int cash_back;
	private int check_verification;
	private int check_guarantee;
	private int purshase_adjustement;
	private int marchandise_ret_adjust;
	private int cash_advance_adjustement;
	private int close_batsh;
	private int close_shift;
	private int close_day;
	private int position_18;
	private int read_mail;
	private int send_mail;
	private int mail_delivred;
	private int sales_draft;
	private int clerk_totals;
	private int cash_back_adjust;
	private int adjust_when_amt;
	private int preaut_lesser_amount;
	private int card_activation;
	private int additional_card_activation;
	private int replenchement;
	private int full_redemption;
	public int getPatc_code() {
		return patc_code;
	}
	public void setPatc_code(int patc_code) {
		this.patc_code = patc_code;
	}
	public String getPatc_libelle() {
		return patc_libelle;
	}
	public void setPatc_libelle(String patc_libelle) {
		this.patc_libelle = patc_libelle;
	}
	public Set<PosTerminal> getPos_terminal() {
		return pos_terminal;
	}
	public void setPos_terminal(Set<PosTerminal> pos_terminal) {
		this.pos_terminal = pos_terminal;
	}
	public Integer getAllowed_trans_id() {
		return allowed_trans_id;
	}
	public void setAllowed_trans_id(Integer allowed_trans_id) {
		this.allowed_trans_id = allowed_trans_id;
	}
	public int getNormal_purchase() {
		return normal_purchase;
	}
	public void setNormal_purchase(int normal_purchase) {
		this.normal_purchase = normal_purchase;
	}
	public int getPreauth_purshase() {
		return preauth_purshase;
	}
	public void setPreauth_purshase(int preauth_purshase) {
		this.preauth_purshase = preauth_purshase;
	}
	public int getCompletion() {
		return completion;
	}
	public void setCompletion(int completion) {
		this.completion = completion;
	}
	public int getMail_tel_order() {
		return mail_tel_order;
	}
	public void setMail_tel_order(int mail_tel_order) {
		this.mail_tel_order = mail_tel_order;
	}
	public int getMarchandise_return() {
		return marchandise_return;
	}
	public void setMarchandise_return(int marchandise_return) {
		this.marchandise_return = marchandise_return;
	}
	public int getCash_advance() {
		return cash_advance;
	}
	public void setCash_advance(int cash_advance) {
		this.cash_advance = cash_advance;
	}
	public int getCard_verification() {
		return card_verification;
	}
	public void setCard_verification(int card_verification) {
		this.card_verification = card_verification;
	}
	public int getBalance_inquery() {
		return balance_inquery;
	}
	public void setBalance_inquery(int balance_inquery) {
		this.balance_inquery = balance_inquery;
	}
	public int getCash_back() {
		return cash_back;
	}
	public void setCash_back(int cash_back) {
		this.cash_back = cash_back;
	}
	public int getCheck_verification() {
		return check_verification;
	}
	public void setCheck_verification(int check_verification) {
		this.check_verification = check_verification;
	}
	public int getCheck_guarantee() {
		return check_guarantee;
	}
	public void setCheck_guarantee(int check_guarantee) {
		this.check_guarantee = check_guarantee;
	}
	public int getPurshase_adjustement() {
		return purshase_adjustement;
	}
	public void setPurshase_adjustement(int purshase_adjustement) {
		this.purshase_adjustement = purshase_adjustement;
	}
	public int getMarchandise_ret_adjust() {
		return marchandise_ret_adjust;
	}
	public void setMarchandise_ret_adjust(int marchandise_ret_adjust) {
		this.marchandise_ret_adjust = marchandise_ret_adjust;
	}
	public int getCash_advance_adjustement() {
		return cash_advance_adjustement;
	}
	public void setCash_advance_adjustement(int cash_advance_adjustement) {
		this.cash_advance_adjustement = cash_advance_adjustement;
	}
	public int getClose_batsh() {
		return close_batsh;
	}
	public void setClose_batsh(int close_batsh) {
		this.close_batsh = close_batsh;
	}
	public int getClose_shift() {
		return close_shift;
	}
	public void setClose_shift(int close_shift) {
		this.close_shift = close_shift;
	}
	public int getClose_day() {
		return close_day;
	}
	public void setClose_day(int close_day) {
		this.close_day = close_day;
	}
	public int getPosition_18() {
		return position_18;
	}
	public void setPosition_18(int position_18) {
		this.position_18 = position_18;
	}
	public int getRead_mail() {
		return read_mail;
	}
	public void setRead_mail(int read_mail) {
		this.read_mail = read_mail;
	}
	public int getSend_mail() {
		return send_mail;
	}
	public void setSend_mail(int send_mail) {
		this.send_mail = send_mail;
	}
	public int getMail_delivred() {
		return mail_delivred;
	}
	public void setMail_delivred(int mail_delivred) {
		this.mail_delivred = mail_delivred;
	}
	public int getSales_draft() {
		return sales_draft;
	}
	public void setSales_draft(int sales_draft) {
		this.sales_draft = sales_draft;
	}
	public int getClerk_totals() {
		return clerk_totals;
	}
	public void setClerk_totals(int clerk_totals) {
		this.clerk_totals = clerk_totals;
	}
	public int getCash_back_adjust() {
		return cash_back_adjust;
	}
	public void setCash_back_adjust(int cash_back_adjust) {
		this.cash_back_adjust = cash_back_adjust;
	}
	public int getAdjust_when_amt() {
		return adjust_when_amt;
	}
	public void setAdjust_when_amt(int adjust_when_amt) {
		this.adjust_when_amt = adjust_when_amt;
	}
	public int getPreaut_lesser_amount() {
		return preaut_lesser_amount;
	}
	public void setPreaut_lesser_amount(int preaut_lesser_amount) {
		this.preaut_lesser_amount = preaut_lesser_amount;
	}
	public int getCard_activation() {
		return card_activation;
	}
	public void setCard_activation(int card_activation) {
		this.card_activation = card_activation;
	}
	public int getAdditional_card_activation() {
		return additional_card_activation;
	}
	public void setAdditional_card_activation(int additional_card_activation) {
		this.additional_card_activation = additional_card_activation;
	}
	public int getReplenchement() {
		return replenchement;
	}
	public void setReplenchement(int replenchement) {
		this.replenchement = replenchement;
	}
	public int getFull_redemption() {
		return full_redemption;
	}
	public void setFull_redemption(int full_redemption) {
		this.full_redemption = full_redemption;
	}
	
	
	
}
