package com.mss.backOffice.request;

import java.util.HashSet;
import java.util.Set;

import com.mss.unified.entities.PosTerminal;

public class PosLimitsConfDisplayDetails {
	
	
	private Integer plc_code;
	private String plc_libelle;
	
	private Set<PosTerminal> pos_terminal= new HashSet<>();
	
	private Integer limits_id;
	private String adjustement_count_limit;
	private String adjustement_amount_limit;
	private String return_count_limit;
	private String return_amount_limit;

	public Integer getPlc_code() {
		return plc_code;
	}
	public void setPlc_code(Integer plc_code) {
		this.plc_code = plc_code;
	}
	public String getPlc_libelle() {
		return plc_libelle;
	}
	public void setPlc_libelle(String plc_libelle) {
		this.plc_libelle = plc_libelle;
	}
	public Set<PosTerminal> getPos_terminal() {
		return pos_terminal;
	}
	public void setPos_terminal(Set<PosTerminal> pos_terminal) {
		this.pos_terminal = pos_terminal;
	}
	public Integer getLimits_id() {
		return limits_id;
	}
	public void setLimits_id(Integer limits_id) {
		this.limits_id = limits_id;
	}
	public String getAdjustement_count_limit() {
		return adjustement_count_limit;
	}
	public void setAdjustement_count_limit(String adjustement_count_limit) {
		this.adjustement_count_limit = adjustement_count_limit;
	}
	public String getAdjustement_amount_limit() {
		return adjustement_amount_limit;
	}
	public void setAdjustement_amount_limit(String adjustement_amount_limit) {
		this.adjustement_amount_limit = adjustement_amount_limit;
	}
	public String getReturn_amount_limit() {
		return return_amount_limit;
	}
	public void setReturn_amount_limit(String return_amount_limit) {
		this.return_amount_limit = return_amount_limit;
	}
	public String getReturn_count_limit() {
		return return_count_limit;
	}
	public void setReturn_count_limit(String return_count_limit) {
		this.return_count_limit = return_count_limit;
	}
	

}
