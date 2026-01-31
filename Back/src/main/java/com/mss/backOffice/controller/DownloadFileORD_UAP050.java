package com.mss.backOffice.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mss.backOffice.exception.ExceptionMethod;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.backOffice.request.FileUAPRequest;
import com.mss.unified.entities.CodeBankBC;
import com.mss.unified.repositories.CodeBankBCRepository;
import com.mss.unified.repositories.UAP050FransaBankRepository;

@RestController
@RequestMapping("downloadORD050File")
public class DownloadFileORD_UAP050 {
	public static Integer eve1 = 1;
	@Autowired
	DownloadFileBc write;
	 @Autowired
	    CodeBankBCRepository codeBankBCRepository;
		@Autowired
		UAP050FransaBankRepository uAP050FransaBankRepository;
	   @GetMapping(value ="writeInFileORD050C")
	    public @ResponseBody ResponseEntity<FileUAPRequest> writeInORD050File(String sequenceNumber) throws ResourceNotFoundException, ExceptionMethod {
		   //String result = String.format("%03d", eve1);
		   if (uAP050FransaBankRepository.count()<1) {
		        return ResponseEntity.badRequest().body(null);

		   }
		    CodeBankBC codeBankBC  =codeBankBCRepository.findIdentifiant();
		    StringBuilder str = new StringBuilder("");
	        FileUAPRequest fileRequest = new FileUAPRequest();
	        String destinationBank = codeBankBC.getIdentifiant();
		   String Entete = "ORD"+sequenceNumber+"INLOT"+"000"+sequenceNumber+"050"+"DZD"+getSpace(41)+"\n";
		   List<String> Dates  =uAP050FransaBankRepository.findDate();
			/*String Date="";
			if(Dates.size()>=1) {
			 Date = Dates.get(0);}
			else Date =""; */
		    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		    
		    String Date= dateFormat.format(new Date()); 
	        List<String> hours  =uAP050FransaBankRepository.findHeure();
	    	String hour="";
			if(hours.size()>=1) {
				hour = hours.get(0);}
			else hour =""; 
		   String fileName = destinationBank +"."+sequenceNumber+"."+Date+"."+"ORD";
		   str.append(Entete);
		   fileRequest.setData(str.toString());
	        fileRequest.setNameTitle(fileName);
			write.writeFile(fileName, str.toString(),false);

	        generateSequenceNumber();
	        return ResponseEntity.ok().body(fileRequest);
	   }
	    public String getSpace(int count)

	    {

	        String Space="";

	        for(int i=0;i<count;i++)

	            Space+=" ";

	        return Space;

	    }
	    public static int generateSequenceNumber() {
	        return ++eve1;
	    }

}
