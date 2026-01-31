package com.mss.backOffice.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.backOffice.request.BinStockDTO;
import com.mss.backOffice.request.GenerateCardPrep;
import com.mss.unified.entities.BinOnUs;
import com.mss.unified.entities.StockBin;
import com.mss.unified.repositories.BinOnUsRepository;
import com.mss.unified.repositories.StockBinRepository;

@RestController
@RequestMapping("/BinOnUsStock/")
public class BinOnUsStockController {
	
    @Autowired
    BinOnUsRepository binOnUsRepository;
    
    @Autowired
    StockBinRepository stockBinRepository;
    private static final Gson gson = new Gson();
    
    


    @PostMapping("AddStock/{id}/{stock}")
    public ResponseEntity<String> Addstock(@PathVariable(value = "id") Integer binOnUsId,
    		@PathVariable(value = "stock") int stock) {
    	StockBin s = new StockBin();
    	s.setCodeBinOnUs(binOnUsId);
    	s.setStockDisponible(stock);
    	s.setStockSatim(0);
    	s.setStockConsome(0);
    	s.setStockReserve(0);
    	s.setDamagedCards(0);
    	stockBinRepository.save(s);
        return ResponseEntity.ok().body(gson.toJson("Stock initial créé"));

    }
    
    @GetMapping("GetAllBinStock")
    public ResponseEntity<List<Object[]>> GetAllBinStock()
            throws ResourceNotFoundException {
    
    	 List<Object[]> obj= stockBinRepository.AllBinOnUsStock();
    
    	 
				/*
				 * String libelle=String.valueOf(o[0]); StockBin stock=(StockBin) o[1];
				 * System.out.println("libelle"+libelle); System.out.println("stock"+stock);
				 * BinStockDTO binDTO=new BinStockDTO(); binDTO.setLibelle(libelle);
				 * binDTO.setStockInitial(stock.getStockInitial());
				 * binDTO.setStockConsomé(stock.getStockConsomé());
				 * binDTO.setStockDisponible(stock.getStockDisponible());
				 * binDTO.setStockReserve(stock.getStockReserve());
				 * binDTO.setCodeBinOnUs(stock.getCodeBinOnUs()); listdto.add(binDTO);
				 */
    		
        return ResponseEntity.ok().body(obj);
    }
    @PutMapping("update/{id}/{stock}/{type}")
    public ResponseEntity<String> updateStock(@PathVariable(value = "id") String binOnUsId,
    		@PathVariable(value = "stock") int stock,@PathVariable(value = "type") String type){
    	 StockBin  stockBin=  stockBinRepository.findBybinOnUsCode(Integer.parseInt(binOnUsId));
    	 if (type.equals("DAMAGED CARDS")) {
    		 stockBin.setDamagedCards(stockBin.getDamagedCards() +stock) ;
    		 stockBin.setStockSatim(stockBin.getStockSatim()-stock);
    	 }
    	 else if (type.equals("SATIM")) {
    		 stockBin.setStockDisponible(stockBin.getStockDisponible()-stock);
    		 stockBin.setStockSatim(stockBin.getStockSatim()+stock);
    	 }else {
    		 stockBin.setStockDisponible(stockBin.getStockDisponible()+stock);
    	 }
    	 stockBinRepository.save(stockBin);
         return ResponseEntity.ok().body(gson.toJson("Stock initial créé"));}
     
    
    
    //verif stock des bins
    
    @PostMapping("verifDispoStock")
  public List<BinOnUs> verifDispoStock(@RequestBody   List<BinOnUs> listbinOnUs ){
    	List<BinOnUs> listBinNonDispo= new ArrayList<BinOnUs>();
    
	  for (BinOnUs b : listbinOnUs) {
		  StockBin  stockBin=  stockBinRepository.findBybinOnUsCode(b.getBinOnUsCode());
	     if(stockBin != null) {
	    	 if(stockBin.getStockSatim()<=0) {
	    		 listBinNonDispo.add(b) ;
	    	 }}
	     else {
    		 listBinNonDispo.add(b);

	     }
	     
	  
	  }
    		return listBinNonDispo;}
    
}
