package com.mss.backOffice.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.mss.backOffice.exception.ExceptionMethod;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.backOffice.request.FileUAPRequest;
import com.mss.unified.entities.CodeBankBC;
import com.mss.unified.entities.CraControl;
import com.mss.unified.entities.UAP050FransaBank;
import com.mss.unified.entities.UAP051FransaBank;
import com.mss.unified.entities.OpeningDay;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.CraControlRepository;
import com.mss.unified.repositories.UAP051FransaBankRepository;
import com.mss.unified.repositories.UAP051INFransaBankRepository;
import com.mss.unified.repositories.OpeningDayRepository;


@RestController
@RequestMapping("downloadUAP051File")
public class DownloadFileUAP_051_FC {
	@Autowired
	UAP051FransaBankRepository uAP050FransaBankRepository;
	@Autowired
	CodeBankBCRepository codeBankBCRepository;
	@Autowired
	DownloadFileBc write;
	public static Integer eve1 = 1;
	@Autowired
	OpeningDayRepository opr;
	@Autowired
	private CraControlRepository craControlRepository;
	private static final Gson gson = new Gson();
	private static final Logger logger = LoggerFactory.getLogger(DownloadFileUAP_051_FC.class);

	@GetMapping(value = "writeInFileUAP051BC")
	public @ResponseBody ResponseEntity<FileUAPRequest> writeInUAP051File() throws ResourceNotFoundException, ExceptionMethod {
		CodeBankBC codeBankBC = codeBankBCRepository.findIdentifiant();
		FileUAPRequest fileRequest = new FileUAPRequest();
		String destinationBank = codeBankBC.getIdentifiant();
        OpeningDay op= opr.findForCra();
        
        List<UAP051FransaBank> UAPFransaBankS = uAP050FransaBankRepository.findReglementByDate(op.getFileIntegration());
        Long totalAmount = uAP050FransaBankRepository.getTotalAmountByDateIntegration( op.getFileIntegration());
        String nbreOperation = String.valueOf(uAP050FransaBankRepository.getNmbreOprerationByDateIntegration(op.getFileIntegration()));
 
 	
		StringBuilder str = new StringBuilder("");
		totalAmount = (totalAmount == null) ? new Long("0") : totalAmount;

		 String convertAmount = String.valueOf(totalAmount);

		 
	        String totalAmountFinal = getZero(16-convertAmount.length())+convertAmount;
	        //String result = String.format("%03d", eve1);

		    logger.info("nbreOperation =>{}",nbreOperation);
		   String nbreLigne="";
	        if (nbreOperation.length()<4)
	        {
	        	  nbreLigne = getZero(4-nbreOperation.length())+nbreOperation; 	
	        }else {
	        	  nbreLigne = nbreOperation.substring(0,4);
	        }
		
		
		String Entete = "ELOT" + destinationBank + "000" + "001" + "DZD" + nbreLigne + totalAmountFinal + getSpace(28);
		FileRequest.print(Entete, FileRequest.getLineNumber());
		List<String> Dates = uAP050FransaBankRepository.findDate();
		// String Date = UAPDate.getDatetransaction();
		String Date="";
		if(Dates.size()>=1) {
		 Date = Dates.get(0);}
		else Date =""; 
		String fileName = destinationBank + "." + "000" + "." + "001" + "." + "051" + "." + "DZD" + "." + "LOT" ;
		str.append(Entete);
		str.append(System.getProperty("line.separator"));
        if(UAPFransaBankS.size()==0) {
            return ResponseEntity.badRequest().body(null);

        }
		for (UAP051FransaBank UAP : UAPFransaBankS) {

			if (UAP.getTypeTransaction() == null) {
				  throw new ExceptionMethod("Error");    
			} else {
				str.append(UAP.getTypeTransaction());
			}

			if (UAP.getTypePaiement() == null) {
				str.append(getSpace(2));
			} else {
				str.append(UAP.getTypePaiement());
			}

			if (UAP.getReferenceArchivage() == null) {
				str.append(getSpace(18));
			} else {
				str.append(UAP.getReferenceArchivage());
			}
			if (UAP.getIndicateur() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getIndicateur());
			}
			
			if (UAP.getNumRIBcommercant() == null) {
				str.append(getSpace(20));
			} else {
				str.append(UAP.getNumRIBcommercant());
			}
			
			
			if (UAP.getPrefixeIBAN() == null) {
				str.append(getSpace(4));
			} else {
				str.append(UAP.getPrefixeIBAN());
			}
			if (UAP.getLibelleCommercant() == null) {
				str.append(getSpace(50));
			} else {
				str.append(UAP.getLibelleCommercant());
			}

			if (UAP.getAdresseCommercant() == null) {
				str.append(getSpace(70));
			} 
			else {
				if(UAP.getAdresseCommercant().length()>70) {
					String adress=UAP.getAdresseCommercant().substring(0, 70);
					str.append(adress);
				}
				else {
					String adress=StringUtils.rightPad(UAP.getAdresseCommercant(), 70);
					str.append(adress);
				}
				

				
			}
			str.append(getSpace(10));
			/*if (UAP.getTelephoneCommercant() == null) {
				str.append(getSpace(10));
			} else {
				str.append(UAP.getTelephoneCommercant());
			}*/

			if (UAP.getNumContratAccepteur() == null) {
				str.append(getSpace(15));
			} else {
				str.append(UAP.getNumContratAccepteur());
			}
			
			
			if (UAP.getCodeActivite() == null) {
				str.append(getSpace(6));
			} else {
				str.append(UAP.getCodeActivite());
			}
			if (UAP.getReserved() == null) {
				str.append(getSpace(3));
			} else {
				str.append(UAP.getReserved());
			}
			if (UAP.getCodeBin() == null) {
				str.append(getSpace(6));
			} else {
				str.append(UAP.getCodeBin());
			}
			if (UAP.getTypeCarte() == null) {
				str.append(getSpace(2));
			} else {
				str.append(UAP.getTypeCarte());
			}
			if (UAP.getNumSeq() == null) {
				str.append(getSpace(6));
			} else {
				str.append(UAP.getNumSeq());
			}
			if (UAP.getNumOrdre() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getNumOrdre());
			}
			if (UAP.getCle() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getCle());
			}
			if (UAP.getMontantRetrait() == null) {
				str.append(getSpace(15));
			} else {
				str.append(UAP.getMontantRetrait());
			}
			if (UAP.getCodeDebitCommercant() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getCodeDebitCommercant());
			}
			if (UAP.getMontantAComponser() == null) {
				str.append(getSpace(15));
			} else {
				str.append(UAP.getMontantAComponser());
			}
			
			if (UAP.getCodeDebitPorteur() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getCodeDebitPorteur());
			}
	
			if (UAP.getMontantCommission() == null) {
				str.append(getSpace(7));
			} else {
				str.append(UAP.getMontantCommission());
			}
			if (UAP.getNumTransaction() == null) {
				str.append(getSpace(12));
			} else {
				str.append(UAP.getNumTransaction());
			}
			if (UAP.getDateTransaction() == null) {
				str.append(getSpace(8));
			} else {
				str.append(UAP.getDateTransaction());
			}

			if (UAP.getHeureTransaction() == null) {
				str.append(getSpace(6));
			} else {
				str.append(UAP.getHeureTransaction());
			}

			if (UAP.getIdentifSystem() == null) {
				str.append(getSpace(10));
			} else {
				str.append(UAP.getIdentifSystem());
			}
			if (UAP.getIdentifPointRetrait() == null) {
				str.append(getSpace(10));
			} else {
				str.append(UAP.getIdentifPointRetrait());
			}
			if (UAP.getModeLectureCarte() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getModeLectureCarte());
			}
			if (UAP.getMethAuthPorteur() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getMethAuthPorteur());
			}
			if (UAP.getNumAutorisation() == null) {
				str.append(getSpace(6));
			} else {
				
				  str.append(UAP.getNumAutorisation().substring(UAP.getNumAutorisation().length()-6,UAP.getNumAutorisation().length()));

			}
			if (UAP.getDateDebutValiditeCarte() == null) {
				str.append(getSpace(8));
			} else {
				str.append(UAP.getDateDebutValiditeCarte());
			}
			if (UAP.getDateFinValiditeCarte() == null) {
				str.append(getSpace(8));
			} else {
				str.append(UAP.getDateFinValiditeCarte());
			}
			if (UAP.getCryptogramData() == null) {
				str.append(getSpace(1));
			} else {
				str.append(UAP.getCryptogramData());
			}
			if (UAP.getAtc() == null) {
				str.append(getSpace(2));
			} else {
				str.append(UAP.getAtc());
			}
			if (UAP.getTvr() == null) {
				str.append(getSpace(5));
			} else {
				str.append(UAP.getTvr());
			}
			if (UAP.getDateRemise() == null) {
				str.append(getSpace(8));
			} else {
				str.append(UAP.getDateRemise());
			}
			str.append(getSpace(307));
			str.append(System.getProperty("line.separator"));
		}
		// String data = str.toString();
		fileRequest.setData(str.toString());
		fileRequest.setNameTitle(fileName);
		
		CraControl craControl = new CraControl(
				fileName,
				"051",
				op.getFileIntegration(),
				Long.parseLong(totalAmountFinal),
				Long.parseLong(nbreLigne)
				);
		craControlRepository.save(craControl);
		
		write.writeFile(fileName, str.toString(),false);

		/*
		 * if (fileRequest.getData()==null) { return
		 * ResponseEntity.status(409).body(gson.toJson("Error")); return
		 * ResponseEntity.ok().body(fileRequest); } else {
		 * 
		 * return ResponseEntity.status(200).body(gson.toJson(fileRequest.getData())); }
		 */
        generateSequenceNumber();

		return ResponseEntity.ok().body(fileRequest);
	}
	  public static int generateSequenceNumber() {
	        return ++eve1;
	    }
	public String getSpace(int count)

	{

		String Space = "";

		for (int i = 0; i < count; i++)

			Space += " ";

		return Space;

	}
    public String getZero(int count)

    {

        String zero="";

        for(int i=0;i<count;i++)

            zero+="0";

        return zero;

    }

}
