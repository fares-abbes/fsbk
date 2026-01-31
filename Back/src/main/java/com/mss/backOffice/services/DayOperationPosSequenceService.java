package com.mss.backOffice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mss.unified.entities.DayOperationFeesPosSequence;
import com.mss.unified.repositories.DayOperationFeesPosSequenceRepository;


@Service
public class DayOperationPosSequenceService {

	
	@Autowired
	private DayOperationFeesPosSequenceRepository dayOperationFeesPosSequenceRepository;
	
	public DayOperationFeesPosSequence incrementSequence(DayOperationFeesPosSequence seq) {
		
		if (seq.getSequence()==999999999)
			seq.setSequence(0);
		else
			seq.setSequence(seq.getSequence()+1);
		
		return dayOperationFeesPosSequenceRepository.save(seq);
	}
	
	
	public DayOperationFeesPosSequence incrementSequencePieceComptable(DayOperationFeesPosSequence seq) {
		
		if (seq.getSequence()==999)
			seq.setSequence(0);
		else
			seq.setSequence(seq.getSequence()+1);
		
		return dayOperationFeesPosSequenceRepository.save(seq);
	}
	
	
	public DayOperationFeesPosSequence getSequence() {
		
		return dayOperationFeesPosSequenceRepository.getSeqNumTransaction().get();
	}
	
	public DayOperationFeesPosSequence getSequencePieceComptable() {
		
		return dayOperationFeesPosSequenceRepository.getSeqPieceComptable().get();
	}
	
}
