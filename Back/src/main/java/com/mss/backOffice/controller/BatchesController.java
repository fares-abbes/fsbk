package com.mss.backOffice.controller;

import com.mss.unified.entities.BatchAtm;
import com.mss.unified.entities.BatchPorteur;
import com.mss.unified.entities.BatchPos;
import com.mss.unified.repositories.BatchAtmRepository;
import com.mss.unified.repositories.BatchPorteurRepository;
import com.mss.unified.repositories.BatchPosRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("Batches")
public class BatchesController {

	@Autowired
	public BatchPorteurRepository batchPorteurRepository;
	
	@Autowired
	public BatchAtmRepository batchAtmRepository;
	@Autowired
	BatchPosRepository batchPosRepository;
	
	
	@GetMapping("getAllBatchPorteur")
	public List<BatchPorteur> getAllBatchPorteur(){
		
		return batchPorteurRepository.findNotVisa();
	}
	
	@GetMapping("getAllVisaBatchPorteur")
	public List<BatchPorteur> getAllVisaBatchPorteur(){
		
		return batchPorteurRepository.findVisa();
	}
	
	
	@GetMapping("getAllBatchLogAtm")
	public List<BatchAtm> getAllBatchLogAtm(){
		
		return batchAtmRepository.findAll();
	}
	
	@GetMapping("getAllBatchPos")
	public List<BatchPos> getAllBatchPos(){
		
		return batchPosRepository.findAll();
	}


}
