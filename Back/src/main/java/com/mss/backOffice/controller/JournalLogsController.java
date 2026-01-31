package com.mss.backOffice.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.mss.backOffice.services.PropertyService;
import com.mss.unified.entities.BatchAtm;
import com.mss.unified.entities.JournalGabAtm;
import com.mss.unified.repositories.BatchAtmRepository;
import com.mss.unified.repositories.JournalGabAtmRepository;


@RestController
@RequestMapping("JournalLogs")
public class JournalLogsController {
	@Autowired
	JournalGabAtmRepository journalGabAtmRepository;
	@Autowired
	BatchAtmRepository batchAtmRepository;
	@Autowired
	private PropertyService propertyService;
	
	private static final Logger logger = LoggerFactory.getLogger(IntegrationPorteurFilesController.class);
	private static final Gson gson = new Gson();
	
	public static String padLeft(String s, int n) {
	    return String.format("%" + n + "s", s);  
	}
	@GetMapping("parseFile")
	public ResponseEntity<String> Parsefile() {
		BatchAtm batch = batchAtmRepository.findByKeys("parseFile").get();

		batch.setBatchStatus(0);
		batch.setBatchDate(new java.util.Date());
		batch.setError(null);
		batch.setErrorStackTrace(null);
		batchAtmRepository.save(batch);

		try {
			
		
	
		
		JSONParser jsonparser = new JSONParser();
		

			File file=new File(propertyService.getFileJsonPathLog());
			
			File fileRef=new File(propertyService.getFileRef());
			File fileAuth=new File(propertyService.getFileAuth());
			File fileSolde=new File(propertyService.getFileSolde());
			
				File[] fileArray = file.listFiles();
				File[] fileRefArray = fileRef.listFiles();
				File[] fileAuthArray = fileAuth.listFiles();
				File[] fileSoldeArray = fileSolde.listFiles();
				LocalDate currentDate = LocalDate.now();
				for ( File f:fileRefArray) {
					Path path=Paths.get(f.toString());
					LocalDate fileDate =LocalDate.parse(path.getFileName().toString().substring(4,14)) ;
					long daysDifference = ChronoUnit.DAYS.between(fileDate,currentDate);
					if (daysDifference>=5) {
						f.delete();
					}
				}
				for ( File f:fileAuthArray) {
					Path path=Paths.get(f.toString());
					LocalDate fileDate =LocalDate.parse(path.getFileName().toString().substring(4,14)) ;
					long daysDifference = ChronoUnit.DAYS.between(fileDate,currentDate);
					if (daysDifference>=5) {
						f.delete();
					}
					}
				for ( File f:fileSoldeArray) {
					Path path=Paths.get(f.toString());
					LocalDate fileDate =LocalDate.parse(path.getFileName().toString().substring(4,14)) ;
					long daysDifference = ChronoUnit.DAYS.between(fileDate,currentDate);
					if (daysDifference>=5) {
						f.delete();
					}
				}
				for ( File f:fileArray) {
					Path path=Paths.get(f.toString());
					System.out.println(path);

					String firstLine;
						firstLine = Files.readAllLines(path).get(0);
					
					firstLine='['+firstLine+']';
					//System.out.println("lastLine"+firstLine);
				//	f.delete();
					
					Object obj=jsonparser.parse(firstLine);
					
					JSONArray jsonarray = (JSONArray)obj;
					System.out.println("jsonarray size = "+jsonarray.size());
					
					
					List<JournalGabAtm> listJournal = new ArrayList<JournalGabAtm>();
					List<String> motCleList=Arrays.asList("Transaction Refused","DEMANDE AUTHENTIFICATION","DEMANDE DE SOLDE","TRANSACTION END","RETRAIT D'ESPECES");
					for (int i = 0; i < jsonarray.size(); i++) {
						JournalGabAtm journal=new JournalGabAtm();
						//System.out.println("jsonarray get("+i+") = "+jsonarray.get(i));
						JSONObject authjsonobj=(JSONObject)jsonarray.get(i);
						journal.date =(String) authjsonobj.get("Date");
						journal.gab =(String) authjsonobj.get("GAB");
						if(journal.gab == null) {
							journal.gab = "";
						}
						journal.response = (String) authjsonobj.get("RESPONSE");
						journal.montant =(String) authjsonobj.get("Montant");
						if(journal.montant!=null) {
							journal.montant = journal.montant.replace(",", "");
							
							String m =org.apache.commons.lang3.StringUtils.leftPad(journal.montant, 15, '0');
							
							journal.montant = m;
							
						}
						else if(journal.response == null) {
							journal.montant = "000000000000.00";
						}
						
						
						journal.card =(String) authjsonobj.get("NUMERO CARTE");
						
						journal.trx =(String) authjsonobj.get("TRX");
						
						journal.rrn =(String) authjsonobj.get("RRN");
						if(journal.trx == null) {
							journal.trx = "";
						}
						//System.out.println("RRN ="+journal.rrn+"TRX= "+journal.trx);
						
						journal.cassette1 =(String) authjsonobj.get("cassette1");
						journal.cassette2 =(String) authjsonobj.get("cassette2");
						journal.cassette3 =(String) authjsonobj.get("cassette3");
						journal.cassette4 =(String) authjsonobj.get("cassette4");
						journal.dispensedT1 =(String) authjsonobj.get("DISPENSED T1");
						journal.dispensedT2 =(String) authjsonobj.get("DISPENSED T2");
						journal.dispensedT3 =(String) authjsonobj.get("DISPENSED T3");
						journal.dispensedT4 =(String) authjsonobj.get("DISPENSED T4");
						journal.rejectedT1 =(String) authjsonobj.get("REJECTED T1");
						journal.rejectedT2 =(String) authjsonobj.get("REJECTED T2");
						journal.rejectedT3 =(String) authjsonobj.get("REJECTED T3");
						journal.rejectedT4 =(String) authjsonobj.get("REJECTED T4");
						journal.remainingT1 =(String) authjsonobj.get("REMAINING T1");
						journal.remainingT2 =(String) authjsonobj.get("REMAINING T2");
						journal.remainingT3 =(String) authjsonobj.get("REMAINING T3");
						journal.remainingT4 =(String) authjsonobj.get("REMAINING T4");
						

						journal.statusSpark = "NOT FOUND";
						journal.statusDab = "NOT FOUND";
						journal.statusSatim = "NOT FOUND";
						journal.isExtourne="0";
						journal.message =(String) authjsonobj.get("message");
						
						
						if(!journal.message.startsWith("     *TRANSACTION START*")) {
							int index = journal.message.length() ;
							for(String cle : motCleList) {
								if ((journal.message.indexOf(cle) > 11) && (journal.message.indexOf(cle)< index)   ) {
									index = journal.message.indexOf(cle);
								}
								
							}
							
							journal.message=journal.message.substring(0, index);
							
							
						}

						if(((String) authjsonobj.get("topic_name")).equals("kafka-topic-ref")) {
							journal.statusDab = "NOT OK";
						}
						else {
							journal.statusDab = "OK";
						}
						
						if(!(journal.rrn==null && journal.card==null)) {
							listJournal.add(journal);
						}
					}
					
					journalGabAtmRepository.saveAll(listJournal);
					
					List<Object[]> trans = journalGabAtmRepository.getLogJournalGab();

					for (Object[] element : trans) {
					    System.out.println("trans " + String.valueOf(element[0]));
					    journalGabAtmRepository.updateJournalGab(Integer.valueOf(String.valueOf(element[0])));
					}
					
					List<Object[]> transSparkApprouved = journalGabAtmRepository.getLogJournalGabAuthApprouved();
					
					for (Object[] element : transSparkApprouved) {
					    System.out.println("transSparkApprouved " + String.valueOf(element[0]));
					    journalGabAtmRepository.updateJournalGabAuthApprouved(Integer.valueOf(String.valueOf(element[0])));
					}

					List<Object[]> transSparkNotApprouved = journalGabAtmRepository.getLogJournalGabAuthNotApprouved();
					
					for (Object[] element : transSparkNotApprouved) {
				    System.out.println("transSparkNotApprouved " + String.valueOf(element[0]));
					    journalGabAtmRepository.updateJournalGabAuthNotApprouved(Integer.valueOf(String.valueOf(element[0])));
					}
					
					
					journalGabAtmRepository.updateJournalGabisExtourne();
					
					
					f.delete();
				}
				
				
//				List<JournalGabAtm> journals =journalGabAtmRepository.findAll();
//				System.out.println("here.........."+journals);
//				for(JournalGabAtm journalX:journals) {
//					if(journalX.getStatusDab()!=journalX.getStatusSpark() ) {
//						journalX.setIsExtourne(1);	
//						}else if(journalX.getStatusDab()=="OK" && journalX.getStatusSpark()=="OK" &&  journalX.getStatusSatim()=="NOT FOUND" && 
//								journalX.getCard().substring(0, 6)!="502127"){
//							journalX.setIsExtourne(1);
//						
//						}else {
//							journalX.setIsExtourne(0);
//						}
//					
//				}
//				
//				journalGabAtmRepository.saveAll(journals);
//				
				
				batchAtmRepository.updateFinishBatch("parseFile", 1, new java.util.Date());
				
				
				
				return ResponseEntity.ok().body(gson.toJson("Data successfully added!"));
					} catch (Exception exception) {
						// TODO: handle exception
						logger.info("Exception is=>{}", exception.toString());
						exception.printStackTrace();
						String stackTrace = Throwables.getStackTraceAsString(exception);
						if (stackTrace.length() > 4000)
							stackTrace = stackTrace.substring(0, 3999);
						String error = exception.getMessage() == null ? exception.toString() : exception.getMessage();
						if (error.length() > 255)
							error = error.substring(0, 254);
						batchAtmRepository.updateStatusAndErrorBatch("parseFile", 2,
								error, new java.util.Date(),
								stackTrace);
						return ResponseEntity.ok().body(gson.toJson("error parsing file!"));
					}
					
			
	}
}
					