package com.mss.backOffice.controller;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.mss.backOffice.request.AtmHardFitnessDisplay;
import com.mss.backOffice.services.PropertyService;
import com.mss.unified.entities.*;
import com.mss.unified.references.JournalGabUpdate;
import com.mss.unified.repositories.AccountRepository;
import com.mss.unified.repositories.BatchAtmRepository;
import com.mss.unified.repositories.JournalGabRepository;

import antlr.StringUtils;

@RestController
@RequestMapping("Logs")
public class AtmLogsController {
	@Autowired
	JournalGabRepository journalGabRepository;
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
		

			File file=new File(propertyService.getFileJsonPath());
				
				File[] fileArray = file.listFiles();
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
					
					
					List<JournalGab> listJournal = new ArrayList<JournalGab>();
					
					for (int i = 0; i < jsonarray.size(); i++) {
						JournalGab journal=new JournalGab();
						//System.out.println("jsonarray get("+i+") = "+jsonarray.get(i));
						JSONObject authjsonobj=(JSONObject)jsonarray.get(i);
						journal.atm =(String) authjsonobj.get("ATM");
						journal.authCode =(String) authjsonobj.get("AuthCode");
						journal.montant =(String) authjsonobj.get("Montant");
						if(journal.montant!=null) {
							journal.montant = journal.montant.replace(",", "");
							
							String m =org.apache.commons.lang3.StringUtils.leftPad(journal.montant, 15, '0');
							
							journal.montant = m;
							
						}
						
						
						
						journal.card =(String) authjsonobj.get("Num_Carte");
						journal.utrnno =(String) authjsonobj.get("UTRNNO");
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
						journal.statusDab =(String) authjsonobj.get("Status DAB");
						journal.dateTR = (String) authjsonobj.get("Date TR");
						if(journal.dateTR!=null) {
							journal.dateTR = journal.dateTR.subSequence(0, journal.dateTR.length()-2)+"20"+journal.dateTR.subSequence(6, journal.dateTR.length());
						}
						journal.message =(String) authjsonobj.get("message");
						
						
						
						if(!(journal.atm==null && journal.authCode==null && journal.card==null && journal.utrnno==null)) {
							listJournal.add(journal);
						}
					}
					
					journalGabRepository.saveAll(listJournal);
					
					
					List<Object[]> trans = journalGabRepository.getLogJournalGab();
					
					//List<JournalGabUpdate> listJournalGabUpdate = new ArrayList<JournalGabUpdate>();
					// new ArrayList<Object[]>();
					
					trans.forEach(element ->{
//						System.out.println(String.valueOf(element[1]));
//						System.out.println(String.valueOf(element[2]));
//						System.out.println(String.valueOf(element[0]));
//
//						
						
						journalGabRepository.updateJournalGab(String.valueOf(element[1]), String.valueOf(element[2]), Integer.valueOf(String.valueOf(element[0])));
						
					});
					
					f.delete();
					
					journalGabRepository.updateJournalGabTO();
					
				}
				batchAtmRepository.updateFinishBatch("parseFile", 1, new java.util.Date());
				
				return ResponseEntity.ok().body(gson.toJson("Data successfully added!"));
					} catch (Exception exception) {
						// TODO: handle exception
						logger.info("Exception is=>{}", exception.toString());
						exception.printStackTrace();
						String stackTrace = Throwables.getStackTraceAsString(exception);
						if (stackTrace.length() > 4000)
							stackTrace = stackTrace.substring(0, 3999);

						batchAtmRepository.updateStatusAndErrorBatch("parseFile", 2,
								exception.getMessage() == null ? exception.toString() : exception.getMessage(), new java.util.Date(),
								stackTrace);
						return ResponseEntity.ok().body(gson.toJson("error parsing file!"));
					}
					
			
	}
}
					
					
				
