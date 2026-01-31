package com.mss.backOffice.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mss.backOffice.exception.ExceptionMethod;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.backOffice.request.FileUAPRequest;
import com.mss.unified.entities.CodeBankBC;
import com.mss.unified.entities.UAP040IN;
import com.mss.unified.entities.UAPFransaBank;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.UAP040INFransaBankRepository;

public  strictfp class GenerateFile140 {
	@Autowired
	private CodeBankBCRepository codeBankBCRepository;
	@Autowired
	private UAP040INFransaBankRepository uAP040INFransaBankRepository;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

	@GetMapping(value = "generateFile140")
	public @ResponseBody ResponseEntity<FileUAPRequest> generateFile140()
			throws ResourceNotFoundException, ExceptionMethod {
		   List<UAP040IN> list =uAP040INFransaBankRepository.getUap040ForGenerating140();
		   if (list.size()>0) {
			   CodeBankBC codeBankBC = codeBankBCRepository.findIdentifiant();
				FileUAPRequest fileRequest = new FileUAPRequest();
				String destinationBank = codeBankBC.getIdentifiant();
			    StringBuilder str = new StringBuilder("");
			    
			    String numeroLot="001";
			    String totalAmount="0000000000000000";
			    
			    String Entete = "ECRO"+destinationBank+"000"+numeroLot+"DZD"+String.format("%04d",list.size())+
			    		totalAmount+getSpace(28);
			    str.append(Entete);
		        str.append(System.getProperty("line.separator"));
		        
		        Date currentDate=new Date();
		        for (UAP040IN UAP: list) {
		        	
		        	str.append(destinationBank+"DZD");
		        	
		        	String numRemise="001";
		        	str.append(numRemise);
		        	
		        	//fix me
		        	String refSoumise="000";
		        	str.append(refSoumise);

		        	
		        	String datePresentation=dateFormat.format(currentDate);
		        	str.append(datePresentation);
		        	
		        	
		        }
			    		
			    		
			    	
//		        fileRequest.setData(str.toString());
//		        fileRequest.setNameTitle(fileName);
//		        write.writeFile(fileName,str.toString(),false);

		       /* if (fileRequest.getData()==null) {
		            return ResponseEntity.status(409).body(gson.toJson("Error"));
		            return ResponseEntity.ok().body(fileRequest);
		        } else {

		            return ResponseEntity.status(200).body(gson.toJson(fileRequest.getData()));
		        }*/
		        return ResponseEntity.ok().body(fileRequest);
			    
			   
		   }else return null;
		
		
		 
	 
	    
	    
		 
	}

    public String getSpace(int count)

    {

        String Space="";

        for(int i=0;i<count;i++)

            Space+=" ";

        return Space;

    }
}
