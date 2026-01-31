package com.mss.backOffice.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.icu.text.SimpleDateFormat;
import com.mss.backOffice.request.PosTerminalDispalay;
import com.mss.unified.entities.Account;
import com.mss.unified.entities.HistoriqueCommercant;
import com.mss.unified.entities.HistoriqueRequestPos;
import com.mss.unified.entities.Merchant;
import com.mss.unified.entities.PendingTpe;
import com.mss.unified.entities.PosHistoriqueOfSerial;
import com.mss.unified.entities.PosModel;
import com.mss.unified.entities.PosSerialNumStates;
import com.mss.unified.entities.PosStock;
import com.mss.unified.entities.PosTerminal;
import com.mss.unified.entities.TpeRequest;
import com.mss.unified.repositories.AccountRepository;
import com.mss.unified.repositories.HistoriqueCommercantRepository;
import com.mss.unified.repositories.HistoriqueRequestPosRepository;
import com.mss.unified.repositories.MerchantRepository;
import com.mss.unified.repositories.PendingTpeRepository;
import com.mss.unified.repositories.PosHistoriqueOfSerialRepository;
import com.mss.unified.repositories.PosModelRepository;
import com.mss.unified.repositories.PosSerialNumStatesRepository;
import com.mss.unified.repositories.PosStockRepository;
import com.mss.unified.repositories.PosTerminalRepository;
import com.mss.unified.repositories.TpeRequestRepository;

@Service
public class DeletedMerchantService {
	@Autowired
	AccountRepository accountRepository;
	@Autowired
	MerchantRepository merchantRepository;
	@Autowired
	PosTerminalRepository posTerminalRepository;
	@Autowired
	PosSerialNumStatesRepository posSerialNumStatesRepository;
	@Autowired
	PosModelRepository posModelRepository;
	@Autowired
	PosStockRepository posStockRepository;
	@Autowired
	PosHistoriqueOfSerialRepository posHistoriqueOfSerialRepository;
	@Autowired
	TpeRequestRepository tpeRequestRepository;
	@Autowired
	PendingTpeRepository pendingTpeRepository;
	@Autowired
	HistoriqueRequestPosRepository historiqueRequestPosRepository;
	@Autowired
	HistoriqueCommercantRepository historiqueCommercantRepository;
public String ResiliationMerchant(String rib) {
	System.out.println("test");

	List<PosHistoriqueOfSerial> posHistoriqueOfSerial = new ArrayList<>();
	List<PosSerialNumStates> posSerialNumStates = new ArrayList<>();
	List<PosTerminal> posTerminal = new ArrayList<>();
	List<PosStock> posStocks = new ArrayList<>();
	List<TpeRequest> TpeRequests = new ArrayList<>();
	
	List<PendingTpe> pTpe =new ArrayList<>();

	SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	System.out.println(timestamp);
	String Strdate = sdf3.format(timestamp);
	String code = rib.substring(0, 3);
	String initial = rib.substring(3, 8);
	String accountNum =rib.substring(8, rib.length());
	System.out.println("code "+code);
	System.out.println("code "+initial);
	System.out.println("code "+accountNum);
	String ribAcc = rib.substring(1, rib.length());

	//Account account = accountRepository.findByAccountNum(accountNum);
	Account account = accountRepository.findAccountByAgence(ribAcc,initial);
	List<TpeRequest> Request = tpeRequestRepository.findAllByAccountNumberStatus(account.getAccountNum());
	System.out.println("request size ="+Request.size());

	if (account != null) {
		Merchant findedMerchant = merchantRepository.findByNumAccount(account.getAccountCode());
		
		if(findedMerchant!=null) {
			List<PosTerminal> posTerminals =posTerminalRepository.findAllByMerchantCode(findedMerchant.getMerchantCode());
			if(Request.size()!=0) {
				

			
				for(TpeRequest items : Request) {
					if(items.getPendingTpes().size()!=0) {
						Set<PendingTpe> pendingTpes = items.getPendingTpes();
						List<PendingTpe> list = new ArrayList<PendingTpe>(pendingTpes);
						for(PendingTpe it : pendingTpes) {
							PendingTpe pendingTpe =pendingTpeRepository.getOne(it.getCode());
							pendingTpe.setStatus(6);
							pTpe.add(pendingTpe);
						}
						

					}
					
					TpeRequest tpeRequest=tpeRequestRepository.getOne(items.getRequestCode());
					System.out.println("request code ="+items.getRequestCode());
					tpeRequest.setStatus(6);
					TpeRequests.add(tpeRequest);
					
				}
			}
			for(PosTerminal item : posTerminals) {
				
				
				PosTerminal pt = posTerminalRepository.getOne(item.getPosCode());
				if(!pt.getStatus().equals("SUPPRIME")) {
				if(!pt.getSerialNum().equals("")) {
					PosSerialNumStates pserial =posSerialNumStatesRepository.getOne(pt.getSerialNum());
					
					PosModel posModel =posModelRepository.getOne(pserial.getModel());
					PosStock ps = posStockRepository.getPosStockByMode(posModel.getModelCode());
					pserial.setStatuResiliation("1");
					if(pserial.getStatus()==6) {
						
						pserial.setStatus(6);
						
						
						
					}else if(pserial.getStatus()==3) {
						PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
						ps.setStockDisponible(ps.getStockDisponible()+1);
						ps.setStockConsome(ps.getStockConsome()-1);
						pserial.setStatus(1);
						ph.setSerialNum(pserial.getSerialNum());
						ph.setDateSaisie(Strdate);
						ph.setStatus(1);
						posHistoriqueOfSerial.add(ph);
						

					}else if(pserial.getStatus()==4) {
						PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
						ps.setStockDisponible(ps.getStockDisponible()+1);
						ps.setStockDeployer(ps.getStockDeployer()-1);
						pserial.setStatus(1);
						ph.setSerialNum(pserial.getSerialNum());
						ph.setDateSaisie(Strdate);
						ph.setStatus(1);
						posHistoriqueOfSerial.add(ph);

					}else if (pserial.getStatus()==5 ) {
						
						pserial.setStatus(5);
						

					}
					posSerialNumStates.add(pserial);
					posStocks.add(ps);
				}
				pt.setStatus("SUPPRIME");
				pt.setSerialNum("");
				pt.setFileTM("D");
				pt.setFileTS("");
				posTerminal.add(pt);
				}
			}
			
		}
		//findedMerchant.setAccount(0);
		findedMerchant.setMerchantStatus("4");
		findedMerchant.setStatusBm("C");
		pendingTpeRepository.saveAll(pTpe);
		tpeRequestRepository.saveAll(TpeRequests);
		posHistoriqueOfSerialRepository.saveAll(posHistoriqueOfSerial);
		posStockRepository.saveAll(posStocks);
		posSerialNumStatesRepository.saveAll(posSerialNumStates);
		posTerminalRepository.saveAll(posTerminal);
		merchantRepository.save(findedMerchant);
		}
		return "Merchant DELETED";
		
	}


public String ResiliationMerchantPos(String rib) {
	System.out.println("test");

	List<PosSerialNumStates> posSerialNumStates = new ArrayList<>();
	List<PosTerminal> posTerminal = new ArrayList<>();
	List<TpeRequest> TpeRequests = new ArrayList<>();
	
	List<HistoriqueRequestPos> historiqueRequestPoss = new ArrayList<>();
	List<PosHistoriqueOfSerial> posHistoriqueOfSerial = new ArrayList<>();
	List<PosStock> posStocks = new ArrayList<>();

	List<PendingTpe> pTpe =new ArrayList<>();
	String initial = rib.substring(3, 8);
	String ribAcc = rib.substring(1, rib.length());
	SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	System.out.println(timestamp);
	String Strdate = sdf3.format(timestamp);
	//Account account = accountRepository.findByAccountNum(accountNum);
	Account account = accountRepository.findAccountByAgence(ribAcc,initial);
	List<TpeRequest> Request = tpeRequestRepository.findAllByAccountNumberStatus(account.getAccountNum());
	System.out.println("request size ="+Request.size());

	if (account != null) {
		Merchant findedMerchant = merchantRepository.findByNumAccount(account.getAccountCode());
		
		if(findedMerchant!=null) {
			List<PosTerminal> posTerminals =posTerminalRepository.findAllByMerchantCode(findedMerchant.getMerchantCode());
			if(Request.size()!=0) {
				

			
				for(TpeRequest items : Request) {
					if(items.getPendingTpes().size()!=0) {
						Set<PendingTpe> pendingTpes = items.getPendingTpes();
						for(PendingTpe it : pendingTpes) {
							PendingTpe pendingTpe =pendingTpeRepository.getOne(it.getCode());
							pendingTpe.setStatus(6);
							pTpe.add(pendingTpe);
						}
						

					}
					
					TpeRequest tpeRequest=tpeRequestRepository.getOne(items.getRequestCode());
					System.out.println("request code ="+items.getRequestCode());
					tpeRequest.setStatus(6);
					
					TpeRequests.add(tpeRequest);
					
					/******************** Request historique **********************/
					HistoriqueRequestPos historiqueRequestPos = new HistoriqueRequestPos();
					historiqueRequestPos.setDateStatu(new Date());
					historiqueRequestPos.setStatut("Commercant Resilié");
					historiqueRequestPos.setRequestCode(tpeRequest.getRequestCode());
					historiqueRequestPos.setOperateur("Back Office");
					historiqueRequestPoss.add(historiqueRequestPos);
					
					/******************** Request historique **********************/
					
				}
			}
		for(PosTerminal item : posTerminals) {
				
				
				PosTerminal pt = posTerminalRepository.getOne(item.getPosCode());
				if(!pt.getStatus().equals("SUPPRIME")) {
					
				if(!pt.getSerialNum().equals("")) {
					PosSerialNumStates pserial =posSerialNumStatesRepository.getOne(pt.getSerialNum());
					PosModel posModel =posModelRepository.getOne(pserial.getModel());
					PosStock ps = posStockRepository.getPosStockByMode(posModel.getModelCode());
					
					
					

					if(pserial.getStatus()==2) {
						ps.setStockHS(ps.getStockHS()+1);

						ps.setStockReserve(ps.getStockReserve()-1);

					}
					else if(pserial.getStatus()==3) {
						ps.setStockHS(ps.getStockHS()+1);

						ps.setStockConsome(ps.getStockConsome()-1);


					}else if(pserial.getStatus()==4) {
						ps.setStockHS(ps.getStockHS()+1);
						ps.setStockDeployer(ps.getStockDeployer()-1);

					}
					pserial.setStatuResiliation("1");
					pserial.setStatus(5);
					PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();

					ph.setSerialNum(pserial.getSerialNum());
					ph.setDateSaisie(Strdate);
					ph.setStatus(5);
					posHistoriqueOfSerial.add(ph);
					posSerialNumStates.add(pserial);
					posStocks.add(ps);
				}
				pt.setStatus("SUPPRIME");
				pt.setFileTS("");
				pt.setFileTM("D");
				pt.setStatusSup("Commercant Resilié");
				posTerminal.add(pt);
				}
			}
			
		}
		
	
		//findedMerchant.setAccount(0);
		findedMerchant.setMerchantStatus("4");
		findedMerchant.setStatusBm("C");
		findedMerchant.setStatusBs("S");
		
		/******************** historiqueCommercant **********************/
		HistoriqueCommercant historiqueCommercant = new HistoriqueCommercant();
		historiqueCommercant.setDateStatu(new Date());
		historiqueCommercant.setStatut("Commercant Resilié");
		historiqueCommercant.setMerchantCode(findedMerchant.getMerchantCode());
		historiqueCommercantRepository.save(historiqueCommercant);
		/******************** historiqueCommercant **********************/
		
		pendingTpeRepository.saveAll(pTpe);
		tpeRequestRepository.saveAll(TpeRequests);
		posSerialNumStatesRepository.saveAll(posSerialNumStates);
		posTerminalRepository.saveAll(posTerminal);
		merchantRepository.save(findedMerchant);
		historiqueRequestPosRepository.saveAll(historiqueRequestPoss);
		posHistoriqueOfSerialRepository.saveAll(posHistoriqueOfSerial);
		}
		return "Merchant DELETED";
		
	}


}
