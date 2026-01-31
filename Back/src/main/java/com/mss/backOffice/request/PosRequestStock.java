package com.mss.backOffice.request;


public class PosRequestStock {
	public String Model;
	public String Marque;
	private int idStock;
    private int stockInitial;
    
    private int stockConsome;
    
    private int stockDisponible;
    
    private int stockReserve;
    
    
    private int stockDeployer;
 
    private int stockHS;
 
    private String dateSaisie;
    
    
    
	public String getMarque() {
		return Marque;
	}

	public void setMarque(String marque) {
		Marque = marque;
	}

	public String getDateSaisie() {
		return dateSaisie;
	}

	public void setDateSaisie(String dateSaisie) {
		this.dateSaisie = dateSaisie;
	}

	public int getIdStock() {
		return idStock;
	}

	public void setIdStock(int idStock) {
		this.idStock = idStock;
	}

	public String getModel() {
		return Model;
	}

	public void setModel(String model) {
		Model = model;
	}

	public int getStockInitial() {
		return stockInitial;
	}

	public void setStockInitial(int stockInitial) {
		this.stockInitial = stockInitial;
	}

	public int getStockConsome() {
		return stockConsome;
	}

	public void setStockConsome(int stockConsome) {
		this.stockConsome = stockConsome;
	}

	public int getStockDisponible() {
		return stockDisponible;
	}

	public void setStockDisponible(int stockDisponible) {
		this.stockDisponible = stockDisponible;
	}

	public int getStockReserve() {
		return stockReserve;
	}

	public void setStockReserve(int stockReserve) {
		this.stockReserve = stockReserve;
	}

	public int getStockDeployer() {
		return stockDeployer;
	}

	public void setStockDeployer(int stockDeployer) {
		this.stockDeployer = stockDeployer;
	}

	public int getStockHS() {
		return stockHS;
	}

	public void setStockHS(int stockHS) {
		this.stockHS = stockHS;
	}

	@Override
	public String toString() {
		return "PosRequestStock [Model=" + Model + ", Marque=" + Marque + ", idStock=" + idStock + ", stockInitial="
				+ stockInitial + ", stockConsome=" + stockConsome + ", stockDisponible=" + stockDisponible
				+ ", stockReserve=" + stockReserve + ", stockDeployer=" + stockDeployer + ", stockHS=" + stockHS
				+ ", dateSaisie=" + dateSaisie + "]";
	}


	
    
    

}
