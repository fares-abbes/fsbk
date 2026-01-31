package com.mss.backOffice.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mss.unified.entities.DayOperationFeesCardSequence;
import com.mss.unified.repositories.DayOperationFeesCardSequenceRepository;

@Service
public class DayOperationCardSequenceService {
	
	@Autowired
	private DayOperationFeesCardSequenceRepository feesCardSequenceRepository;
	
	public DayOperationFeesCardSequence incrementSequence(DayOperationFeesCardSequence seq) {
		
		if (seq.getSequence()==999999999)
			seq.setSequence(0);
		else
			seq.setSequence(seq.getSequence()+1);
		
		return feesCardSequenceRepository.save(seq);
	}
	
	
	public DayOperationFeesCardSequence incrementSequencePieceComptable(DayOperationFeesCardSequence seq) {
		
		if (seq.getSequence()==999)
			seq.setSequence(0);
		else
			seq.setSequence(seq.getSequence()+1);
		
		return feesCardSequenceRepository.save(seq);
	}
	
	
	public DayOperationFeesCardSequence getSequence() {
		
		return feesCardSequenceRepository.getSeqNumTransaction().get();
	}
	
	public DayOperationFeesCardSequence getSequencePieceComptable() {
		
		return feesCardSequenceRepository.getSeqPieceComptable().get();
	}
	


}
