package com.mss.backOffice.controller;

import com.google.gson.Gson;
import com.mss.backOffice.Response.PosEntryValues;
import com.mss.backOffice.request.CvrRequest;
import com.mss.backOffice.request.SwitchDetails;
import com.mss.backOffice.request.TransactionReportingRequest;
import com.mss.backOffice.request.TvrRequest;
import com.mss.backOffice.services.CryptOracleService;
import com.mss.backOffice.services.TransactionArchiveService;
import com.mss.unified.entities.Cvr;
import com.mss.unified.entities.EmvServiceValues;
import com.mss.unified.entities.Interfaces;
import com.mss.unified.entities.NationalAcquirerFiid;
import com.mss.unified.entities.PosTerminal;
import com.mss.unified.entities.ResponseCode;
import com.mss.unified.entities.SwitchTransaction;
import com.mss.unified.entities.SwitchTransactionArchive;
import com.mss.unified.entities.Tvr;
import com.mss.unified.repositories.CvrRepository;
import com.mss.unified.repositories.EmvServiceValuesRepository;
import com.mss.unified.repositories.InterfacesRepository;
import com.mss.unified.repositories.NationalAcquirerFiidRepository;
import com.mss.unified.repositories.PosTerminalRepository;
import com.mss.unified.repositories.ResponceCodeRepository;
import com.mss.unified.repositories.SwitchRepository;
import com.mss.unified.repositories.SwitchTransactionArchiveRepository;
import com.mss.unified.repositories.TvrRepository;
import java.math.BigInteger;
import java.util.*;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

@RestController
@RequestMapping("switch")
public class SwitchController {

	@Autowired
	SwitchTransactionArchiveRepository switchTransactionArchiveRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;


	@Autowired
	private SwitchRepository switchRepository;
	@Autowired
	private NationalAcquirerFiidRepository nationalAcquirerFiidRepository; 
	@Autowired
	private ResponceCodeRepository responceCodeRepository;

	@Autowired
	CvrRepository cvrRepository;
	@Autowired
	TvrRepository tvrRepository;
	@Autowired
	private EmvServiceValuesRepository emvServiceValuesRepository;
	private static final Logger logger = LoggerFactory.getLogger(SwitchController.class);

	@Autowired
	private ResponceCodeRepository responseRepository;
	private static final Gson gson = new Gson();

	@GetMapping("getResponseCodes")
	public List<ResponseCode> getResponseCodes() {
		return responseRepository.findAllByOrderByMotifOppositionAsc();
	}

	
	@GetMapping("getTransactionDetails/{id}")
	public SwitchDetails getTransactionDetails(@PathVariable(value = "id") int idSwitch) {
		SwitchTransaction transaction = switchRepository.findBySwitchCode(idSwitch);
		SwitchDetails details=new SwitchDetails();
		
		details.setSwitchRequestDate(transaction.getSwitchRequestDate());
		details.setSwitchResponseDate(transaction.getSwitchResponseDate());
		details.setSwitchMtiMessage(transaction.getSwitchMtiMessage());
		details.setSwitchPan(transaction.getSwitchPan());
		details.setTransactionCode(transaction.getSwitchProcessingCode().substring(0,2));
		Optional<EmvServiceValues>  emvServiceValues=emvServiceValuesRepository.findByCodeTransaction(transaction.getSwitchProcessingCode().substring(0,2));
		if (emvServiceValues.isPresent())
			details.setTransactionLabel(emvServiceValues.get().getLibelle());
		details.setSwitchAmountTransaction(transaction.getSwitchAmountTransaction());
		
		details.setExpiryDate(transaction.getSwitchExpirationDate());
		details.setTerminalId(transaction.getSwitchAcceptorTerminalId());
//		Optional<PosTerminal> terminal=posTerminalRepository.getTerminal(transaction.getSwitchAcceptorTerminalId());
//		if (terminal.isPresent())
//			details.setMcc(terminal.get().getMccCode().getMccListId());
		
		details.setMcc(transaction.getSwitchMerchantType());
		
		details.setSwitchRRN(transaction.getSwitchRRN());
		details.setMerchantName(transaction.getSwitchAcceptorAcceptorName());
		details.setMerchantCode(transaction.getSwitchAcceptorMerchantCode());
		details.setCurrency(transaction.getSwitchTransactionCurrencyCode());
		details.setAccountCurrency(transaction.getSwitchDE111());
		details.setSwitchAuthNumber(transaction.getSwitchAuthNumber());
		
		details.setSwitchResponseCode(transaction.getSwitchResponseCode());
		details.setSource(transaction.getSource());
		if (!transaction.getSwitchResponseCode().equals("00")) {
			Optional<ResponseCode> responseCodeObj=responceCodeRepository.findById(transaction.getSwitchAdditionalRespData());
			if (responseCodeObj.isPresent())
				details.setMotif(responseCodeObj.get().getDescription());
		}
		details.setPlafondGlobalDisponible(transaction.getSwitchDE108());
		details.setNbDisponibleGlobal(transaction.getSwitchDE109());
		details.setAccountNum(transaction.getSwitchAccountId1());
		Optional<NationalAcquirerFiid> fiid = nationalAcquirerFiidRepository.findByBin(transaction.getSwitchAcquirerIdenCode());
		if (fiid.isPresent()) 
			details.setBankFiid(fiid.get().getBankCode());
		details.setPosEntryMode(transaction.getSwitchPosEntryMode());
		details.setStatus(transaction.getSource());
		details.setSoldeDisponible(transaction.getSwitchDE110());
		return details;
	}

	@PostMapping("getSwitchTransaction")
	public Page<Object[]> getAllTransactions(@RequestBody TransactionReportingRequest transReportingRequest,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
		    @RequestParam(name = "sortOn") String sortOn,
		    @RequestParam(name = "dir") String dir) {
	
		logger.info(transReportingRequest.toString());
		Page<Object[]> trans = null;

//		List<Order> orders = new ArrayList<Order>();
//		Order order1 = new Order(Sort.Direction.DESC, "switchRequestDate");
//		orders.add(order1);
		Order order=null;
	  	if (dir.equals("desc"))
	  		order = new Order(Sort.Direction.DESC, sortOn);
	  	else 
	  		order = new Order(Sort.Direction.ASC, sortOn);

		if (!transReportingRequest.getDate().equals("") && !transReportingRequest.getEndDate().equals("")) {
			// with date interval
			if (transReportingRequest.getReversal().equals("oui")) {
				trans = switchRepository.reportingWtihStartDateAndReversal(PageRequest.of(page, size, Sort.by(order)),
						transReportingRequest.getPan(), transReportingRequest.getAmount(),
						transReportingRequest.getResponseCode(), transReportingRequest.getMerchantId(),
						transReportingRequest.getAuthCode(), transReportingRequest.getTerminal(),
						transReportingRequest.getDate(), transReportingRequest.getEndDate(), "R",
						transReportingRequest.getFiid(),transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso());

			} else if (transReportingRequest.getReversal().equals("non")) {
				trans = switchRepository.reportingWtihStartDateAndNoReversal(
						PageRequest.of(page, size, Sort.by(order)), transReportingRequest.getPan(),
						transReportingRequest.getAmount(), transReportingRequest.getResponseCode(),
						transReportingRequest.getMerchantId(), transReportingRequest.getAuthCode(),
						transReportingRequest.getTerminal(), transReportingRequest.getDate(),
						transReportingRequest.getEndDate(), "R", transReportingRequest.getFiid(),transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso());
			} else {
				trans = switchRepository.reportingWtihStartDateAndReversal(PageRequest.of(page, size, Sort.by(order)),
						transReportingRequest.getPan(), transReportingRequest.getAmount(),
						transReportingRequest.getResponseCode(), transReportingRequest.getMerchantId(),
						transReportingRequest.getAuthCode(), transReportingRequest.getTerminal(),
						transReportingRequest.getDate(), transReportingRequest.getEndDate(), "",
						transReportingRequest.getFiid(),transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso());
			}

		} else {
			// with no date interval
			if (transReportingRequest.getReversal().equals("oui")) {
				trans = switchRepository.reportingWtihReversal(PageRequest.of(page, size, Sort.by(order)),
						transReportingRequest.getPan(), transReportingRequest.getAmount(),
						transReportingRequest.getResponseCode(), transReportingRequest.getMerchantId(),
						transReportingRequest.getAuthCode(), transReportingRequest.getTerminal(), "R",
						transReportingRequest.getFiid(),transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso());

			} else if (transReportingRequest.getReversal().equals("non")) {

				trans = switchRepository.reportingWtihNoReversal(PageRequest.of(page, size, Sort.by(order)),
						transReportingRequest.getPan(), transReportingRequest.getAmount(),
						transReportingRequest.getResponseCode(), transReportingRequest.getMerchantId(),
						transReportingRequest.getAuthCode(), transReportingRequest.getTerminal(), "R",
						transReportingRequest.getFiid(),transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso());
			} else {
				trans = switchRepository.reportingWtihReversal(PageRequest.of(page, size, Sort.by(order)),
						transReportingRequest.getPan(), transReportingRequest.getAmount(),
						transReportingRequest.getResponseCode(), transReportingRequest.getMerchantId(),
						transReportingRequest.getAuthCode(), transReportingRequest.getTerminal(), "",
						transReportingRequest.getFiid(),transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso());
			}

		}

		return trans;

	}

	@GetMapping("getTvr/{id}")
	public List<Boolean> getTvr(@PathVariable(value = "id") int idSwitch) {
		SwitchTransaction s = switchRepository.findBySwitchCode(idSwitch);

		String tvr = s.getTag95();
		int[] binaryCVR = parseHexBinary(tvr);

		logger.info("TVR " + binaryCVR.toString());

		List<Boolean> booleanSet = new ArrayList<>();
		for (int i : binaryCVR) {
			if (i == 0) {
				booleanSet.add(false);
			} else {
				booleanSet.add(true);
			}
		}
		Collections.reverse(booleanSet);
		return booleanSet;
	}

	@GetMapping("getCvr/{id}")
	public List<Boolean> getCvr(@PathVariable(value = "id") int idSwitch) {
		SwitchTransaction s = switchRepository.findBySwitchCode(idSwitch);

		String cvr = s.getTag9F10().substring(6, 14) + "00";
		int[] binaryCVR = parseHexBinary(cvr);
		logger.info("CVR " + binaryCVR);
		List<Boolean> booleanSet = new ArrayList<>();
		for (int i : binaryCVR) {
			if (i == 0) {
				booleanSet.add(false);
			} else {
				booleanSet.add(true);
			}
		}
		Collections.reverse(booleanSet);
		return booleanSet;
	}

	@GetMapping("getPosEntry/{entryMode}")
	public List<PosEntryValues> getPosEntry(@PathVariable(value = "entryMode") String entryMode) {
		List<PosEntryValues> posEntryMode= new ArrayList<PosEntryValues>();
		String posEntry = entryMode;
		String result = "";
		String s1 = posEntry.substring(0, 1);
		String s2 = posEntry.substring(1, 2);
		String s3 = posEntry.substring(2, 3);
		String s4 = posEntry.substring(3, 4);
		String s5 = posEntry.substring(4, 5);
		String s6 = posEntry.substring(5, 6);
		String s7 = posEntry.substring(6, 7);
		String s8 = posEntry.substring(7, 8);
		String s9 = posEntry.substring(8, 9);
		String s10 = posEntry.substring(9, 10);
		String s11 = posEntry.substring(10, 11);
		String s12 = posEntry.substring(11, 12);
		
		String positionValue="";
		String positionValueDescription="";
		
		switch (s1) {
		case "0":
			positionValue="0";
			positionValueDescription="Unknown";
			break;
		case "1":
			positionValue="0";
			positionValueDescription="Manual";
			break;
		case "2":
			positionValue="2";
			positionValueDescription="Magnetic stripe reader";
			break;
		case "3":
			positionValue="3";
			positionValueDescription="Bar code reader";
			break;
		case "4":
			positionValue="4";
			positionValueDescription="OCR reader";
			break;
		case "5":
			positionValue="5";
			positionValueDescription="EMV compatible ICC reader";
			break;
		case "6":
			positionValue="6";
			positionValueDescription="Key entry";
			break;
		case "7":
			positionValue="7";
			positionValueDescription="Magnetic stripe reader and key entry";
			break;
		case "8":
			positionValue="8";
			positionValueDescription="Magnetic stripe reader, key entry and EMV compatible ICC reader";
			break;
		case "9":
			positionValue="9";
			positionValueDescription="Contactless read";
			break;
		}
		
		posEntryMode.add(new PosEntryValues("Card data input capability",positionValue,positionValueDescription));

		switch (s2) {
		case "0":
			positionValue="0";
			positionValueDescription="No electronic authentication";
			break;
		case "1":
			positionValue="1";
			positionValueDescription="PIN";
			break;
		case "2":
			positionValue="2";
			positionValueDescription="Electronic signature analysis";
		case "3":
			positionValue="3";
			positionValueDescription="Biometrics";
			break;
		case "4":
			positionValue="4";
			positionValueDescription="Biometrics";
		case "5":
			positionValue="5";
			positionValueDescription="Electronic authentication";

			break;
		case "6":
			positionValue="6";
			positionValueDescription="Other";
			break;
		}
		posEntryMode.add(new PosEntryValues("Cardholder authentication capability",positionValue,positionValueDescription));
		
		result = "\r\n"+result+"Card capture capability => ";

		if (s3.equals("1")) {
			//result = result + " 1 : Capture\r\n\r\n";
			posEntryMode.add(new PosEntryValues("Card capture capability","1","Capture"));
		} else {
			//result = result + "0 : None\r\n\r\n";
			posEntryMode.add(new PosEntryValues("Card capture capability","0","None"));

		}
		
		switch (s4) {
		case "0":
			positionValue="0";
			positionValueDescription="No terminal used";
			break;
		case "1":
			positionValue="1";
			positionValueDescription="On premises of card acceptor, attended";
			break;
		case "2":
			positionValue="2";
			positionValueDescription="On premises of card acceptor, untended";
			break;
		case "3":
			positionValue="3";
			positionValueDescription="Off premises of card acceptor, attended";
			break;
		case "4":
			positionValue="4";
			positionValueDescription="Off premises of card acceptor, unattended";
			break;
		case "5":
			positionValue="5";
			positionValueDescription="On premises of card cardholder, unattended";
			break;
		case "9":
			positionValue="9";
			positionValueDescription="On premises of card cardholder, attended";
			break;
		}
		
		posEntryMode.add(new PosEntryValues("Operating environment",positionValue,positionValueDescription));

		switch (s5) {
		case "0":
			positionValue="0";
			positionValueDescription="Present";
			break;
		case "1":
			positionValue="1";
			positionValueDescription="Not present, unspecified";
			break;
		case "2":
			positionValue="2";
			positionValueDescription="Not present, mail order";
			break;
		case "3":
			positionValue="3";
			positionValueDescription="Not present, telephone order";
			break;
		case "4":
			positionValue="4";
			positionValueDescription="Not present, standing order (recurring payment)";
			break;
		case "5":
			positionValue="5";
			positionValueDescription="Not present, electronic order";
			break;
		case "S":
			positionValue="S";
			positionValueDescription="Not present, postponed payment";
			break;
		}
		
		posEntryMode.add(new PosEntryValues("Cardholder presence indicator",positionValue,positionValueDescription));

		result = result+"Card presence => ";
		if (s6.equals("1")) {
			//result = result + " 1 : Present\r\n\r\n";
			posEntryMode.add(new PosEntryValues("Card presence","1","Present"));
		} else {
			//result = result + "0 : Not present\r\n\r\n";
			posEntryMode.add(new PosEntryValues("Card presence","0","Not present"));

		}
		
		switch (s7) {
		case "0":
			positionValue="0";
			positionValueDescription="Unspecified";
			break;
		case "1":
			positionValue="1";
			positionValueDescription="Manual, no terminal";
			break;
		case "2":
			positionValue="2";
			positionValueDescription="Magnetic stripe read";
		case "3":
			positionValue="3";
			positionValueDescription="BAR read";
			break;
		case "4":
			positionValue="4";
			positionValueDescription="OCR read";
			break;
		case "5":
			positionValue="5";
			positionValueDescription="ICC read";
			break;
		case "6":
			positionValue="6";
			positionValueDescription="Key entered";
			break;
		case "7":
			positionValue="7";
			positionValueDescription="Contactless ICC read";
			break;
		case "8":
			positionValue="8";
			positionValueDescription="Contactless magnetic stripe read";
			break;
		case "9":
			positionValue="9";
			positionValueDescription="Contactless read";
			break;
		case "D":
			positionValue="D";
			positionValueDescription="Digital Security Remote Payment";
			break;
		case "S":
			positionValue="S";
			positionValueDescription="E-commerce, merchant certificate only";
			break;
		case "T":
			positionValue="T";
			positionValueDescription="E-commerce, merchant and cardholder certificate / 3-D Secure transaction";
			break;
		case "U":
			positionValue="U";
			positionValueDescription="E-commerce, no security";
			break;
		case "V":
			positionValue="V";
			positionValueDescription="E-commerce, channel encryption";
			break;
		case "W":
			positionValue="W";
			positionValueDescription="Auto entry from external system";
			break;
		}
		
		posEntryMode.add(new PosEntryValues("Card data input mode",positionValue,positionValueDescription));


		switch (s8) {
		case "0":
			positionValue="0";
			positionValueDescription="No authentication";
			break;
		case "1":
			positionValue="1";
			positionValueDescription="PIN";
			break;
		case "2":
			positionValue="2";
			positionValueDescription="Electronic signature analysis";
			break;
		case "3":
			positionValue="3";
			positionValueDescription="Biometrics";
			break;
		case "4":
			positionValue="4";
			positionValueDescription="Biographic";
			break;
		case "5":
			positionValue="5";
			positionValueDescription="Manual signature";
			break;
		case "6":
			positionValue="6";
			positionValueDescription="Other manual authentication";
			break;
		case "P":
			positionValue="P";
			positionValueDescription="Partial shipment or recurring payment";
			break;
		case "T":
			positionValue="T";
			positionValueDescription="E-commerce, UCAF / 3DS authentication is not supported by merchant";
			break;
		case "U":
			positionValue="U";
			positionValueDescription="E-commerce, UCAF / 3DS authentication is supported by merchant but is not provided by issuer";
			break;
		case "V":
			positionValue="V";
			positionValueDescription="E-commerce, UCAF / 3DS authentication is supported by merchant and is provided by issuer";
			break;
		}
		posEntryMode.add(new PosEntryValues("Cardholder authentication method",positionValue,positionValueDescription));
		

		switch (s9) {
		case "0":
			positionValue="0";
			positionValueDescription="Not authenticated";
			break;
		case "1":
			positionValue="1";
			positionValueDescription="ICC";
			break;
		case "3":			
			positionValue="3";
			positionValueDescription="Authorizing agent, online PIN";
		case "4":			
			positionValue="4";
			positionValueDescription="Merchant/card acceptor, signature";
			break;
		case "5":
			positionValue="5";
			positionValueDescription="Other";
			break;
		case "S":
			positionValue="S";
			positionValueDescription="Merchant suspicious";
			break;
		}
		
		posEntryMode.add(new PosEntryValues("Cardholder authentication entity",positionValue,positionValueDescription));

		switch (s10) {
		case "0":
			positionValue="0";
			positionValueDescription="Unknown";
			break;
		case "1":
			positionValue="1";
			positionValueDescription="None";
			break;
		case "2":
			positionValue="2";
			positionValueDescription="Magnetic stripe write";
			
			break;
		case "3":
			positionValue="3";
			positionValueDescription="ICC";
			break;
		}
		posEntryMode.add(new PosEntryValues("Card data output capability",positionValue,positionValueDescription));

		switch (s11) {
		case "0":			
			positionValue="0";
			positionValueDescription="Unknown";
			break;
		case "1":			
			positionValue="1";
			positionValueDescription="None";
			break;
		case "2":			
			positionValue="2";
			positionValueDescription="Printing capability only";
			break;
		case "3":
			positionValue="3";
			positionValueDescription="Display capability only";
			break;
		case "4":			
			positionValue="4";
			positionValueDescription="Printing and display capability";

			break;
		}
		posEntryMode.add(new PosEntryValues("Terminal output capability",positionValue,positionValueDescription));

		switch (s12) {
		case "0":
			positionValue="0";
			positionValueDescription="No PIN capture capability";

			break;
		case "1":
			positionValue="1";
			positionValueDescription="Unknown";

			break;
		case "4":			
			positionValue="4";
			positionValueDescription="PIN capture capability 4 characters maximum";

			break;
		case "5":			
			positionValue="5";
			positionValueDescription="PIN capture capability 5 characters maximum";
			break;
		case "6":
			positionValue="6";
			positionValueDescription="PIN capture capability 6 characters maximum";
			break;
		case "7":			
			positionValue="7";
			positionValueDescription="PIN capture capability 7 characters maximum";
			break;
		case "8":
			positionValue="8";
			positionValueDescription="PIN capture capability 8 characters maximum";
			break;
		case "9":
			positionValue="9";
			positionValueDescription="PIN capture capability 9 characters maximum";
			break;
		case "A":			
			positionValue="A";
			positionValueDescription="PIN capture capability 10 characters maximum";
			break;
		case "B":
			positionValue="B";
			positionValueDescription="PIN capture capability 11 characters maximum";
			break;
		case "C":
			positionValue="C";
			positionValueDescription="PIN capture capability 12 characters maximum";
			break;
		}
		
		posEntryMode.add(new PosEntryValues("PIN capture capability",positionValue,positionValueDescription));

		return posEntryMode;
	}

	@PostMapping("addCvr")
	public ResponseEntity<?> addCvr(@RequestBody CvrRequest cvrRequest) {
		StringBuilder sb = new StringBuilder();
		logger.info(cvrRequest.toString());
		List<Boolean> booleans = cvrRequest.getCvrValue();

		for (Boolean b : booleans) {
			int myInt = b ? 1 : 0;
			sb.append(myInt);
		}

		System.out.println(" binary " + sb.toString());
		String hexString = new BigInteger(sb.toString(), 2).toString(16);
		Cvr cvr = new Cvr(cvrRequest.getLibelle(), hexString);
		cvrRepository.save(cvr);
		logger.info(cvr.toString());

		return ResponseEntity.accepted().body(gson.toJson("Added "));
	}

	@PostMapping("addTvr")
	public ResponseEntity<?> addTvr(@RequestBody TvrRequest cvrRequest) {

		logger.info(cvrRequest.toString());
		StringBuilder sb = new StringBuilder();

		List<Boolean> booleans = cvrRequest.getTvrValue();

		for (Boolean b : booleans) {
			int myInt = b ? 1 : 0;
			sb.append(myInt);
		}

		System.out.println(" binary " + sb.toString());
		String hexString = new BigInteger(sb.toString(), 2).toString(16);
		Tvr cvr = new Tvr(cvrRequest.getLibelle(), hexString);
		tvrRepository.save(cvr);
		logger.info(cvr.toString());

		return ResponseEntity.accepted().body(gson.toJson("Added "));
	}

	@GetMapping("getListCvr")
	public List<Cvr> getListCvr() {

		return cvrRepository.findAll();
	}

	@GetMapping("getListTvr")
	public List<Tvr> getListTvr() {

		return tvrRepository.findAll();
	}

	@GetMapping("getListTvr/{id}")
	public List<Boolean> getListTvrById(@PathVariable(value = "id") Integer userId) {
		Tvr tvr = tvrRepository.findById(userId).get();
		int[] binaryCVR = parseHexBinary(tvr.getTvrValue().toUpperCase());
		logger.info("CVR " + binaryCVR.toString());
		List<Boolean> booleanSet = new ArrayList<>();
		for (int i : binaryCVR) {
			if (i == 0) {
				booleanSet.add(false);
			} else {
				booleanSet.add(true);
			}
		}
		Collections.reverse(booleanSet);
		logger.info(booleanSet.toString());
		return booleanSet;

	}

	@GetMapping("getListCvr/{id}")
	public List<Boolean> getListCvrById(@PathVariable(value = "id") Integer userId) {
		Cvr tvr = cvrRepository.findById(userId).get();
		int[] binaryCVR = parseHexBinary(tvr.getCvrValue().toUpperCase());
		logger.info("CVR " + binaryCVR);
		List<Boolean> booleanSet = new ArrayList<>();
		for (int i : binaryCVR) {
			if (i == 0) {
				booleanSet.add(false);
			} else {
				booleanSet.add(true);
			}
		}
		Collections.reverse(booleanSet);
		return booleanSet;

	}
	
	@GetMapping("getCvrById/{id}")
	public ResponseEntity<String> getCvrById(@PathVariable(value = "id") Integer id) {
		return ResponseEntity.ok().body(gson.toJson(cvrRepository.findById(id).get().getLibelle()));
	}
	
	@GetMapping("getTvrById/{id}")
	public ResponseEntity<String> getTvrById(@PathVariable(value = "id") Integer id) {
		return ResponseEntity.ok().body(gson.toJson(tvrRepository.findById(id).get().getLibelle()));
	}

	@PutMapping("updateCvr/{id}")
	public ResponseEntity<Cvr> updateCvr(@PathVariable(value = "id") Integer userId,
			@Valid @RequestBody CvrRequest cvrRequest) {
		logger.info(cvrRequest.toString());
		Cvr binOnUs = cvrRepository.findById(userId).get();
		StringBuilder sb = new StringBuilder();

		List<Boolean> booleans = cvrRequest.getCvrValue();

		for (Boolean b : booleans) {
			int myInt = b ? 1 : 0;
			sb.append(myInt);
		}

		System.out.println(" binary " + sb.toString());
		
		
		String bin=sb.toString();
		
		 int index = 0;
		String[] hexString = new String[bin.length() / 4];
	    for (int i = 0; i < bin.length() / 4; i++) {
	        hexString[i] = "";
	        for (int j = index; j < index + 4; j++) {
	            hexString[i] += bin.charAt(j);
	        }
	        index += 4;
	    }

	    for (int i = 0; i < bin.length() / 4; i++) {
	        System.out.print(hexString[i] + " ");
	    }

	    System.out.println("\n" + bin.length());
	    String[] result = binaryToHex(hexString);
	    String hexString2="";
	    for (int i = 0; i < result.length; i++) {
	        System.out.print("" + result[i].toUpperCase());
	        hexString2=hexString2+result[i].toUpperCase();
	    }
	    System.out.println("hexString2 "+hexString2);

		//String hexString = new BigInteger(sb.toString(), 2).toString(16);

		binOnUs.setCvrCodee(userId);
		binOnUs.setCvrValue(hexString2);
		binOnUs.setLibelle(cvrRequest.getLibelle());
		final Cvr updatedEmployee = cvrRepository.save(binOnUs);
		logger.info(updatedEmployee.toString());
		return ResponseEntity.ok(updatedEmployee);
	}
	
	public static String[] binaryToHex(String[] bin) {
	    String[] result = new String[bin.length];
	    for (int i = 0; i < bin.length; i++) {
	        result[i] = Integer.toHexString(Integer.parseInt(bin[i], 2));
	    }
	    //return Integer.toHexString(Integer.parseInt(bin[0], 2));
	    return result;
	}

	@PutMapping("updateTvr/{id}")
	public ResponseEntity<Tvr> updateTvr(@PathVariable(value = "id") Integer userId,
			@Valid @RequestBody TvrRequest tvrRequest) {
		logger.info(tvrRequest.toString());
		Tvr binOnUs = tvrRepository.findById(userId).get();
		StringBuilder sb = new StringBuilder();

		List<Boolean> booleans = tvrRequest.getTvrValue();

		for (Boolean b : booleans) {
			int myInt = b ? 1 : 0;
			sb.append(myInt);
		}

		System.out.println(" binary " + sb.toString());
		//String hexString = new BigInteger(sb.toString(), 2).toString(16);
		
		String bin=sb.toString();
		
		 int index = 0;
		String[] hexString = new String[bin.length() / 4];
	    for (int i = 0; i < bin.length() / 4; i++) {
	        hexString[i] = "";
	        for (int j = index; j < index + 4; j++) {
	            hexString[i] += bin.charAt(j);
	        }
	        index += 4;
	    }

	    for (int i = 0; i < bin.length() / 4; i++) {
	        System.out.print(hexString[i] + " ");
	    }

	    System.out.println("\n" + bin.length());
	    String[] result = binaryToHex(hexString);
	    String hexString2="";
	    for (int i = 0; i < result.length; i++) {
	        System.out.print("" + result[i].toUpperCase());
	        hexString2=hexString2+result[i].toUpperCase();
	    }
	    System.out.println("hexString2 "+hexString2);
		
		
		
		
		
		
		
		
		//String hexString = binaryToHex(sb.toString());
		binOnUs.setTvrCode(userId);
		binOnUs.setLibelle(tvrRequest.getLibelle());
		binOnUs.setTvrValue(hexString2);
		final Tvr updatedEmployee = tvrRepository.save(binOnUs);
		logger.info(updatedEmployee.toString());
		return ResponseEntity.ok(updatedEmployee);
	}

	private static int[] parseHexBinary(String hex) {
		String digits = "0123456789ABCDEF";
		int[] binaryValue = new int[hex.length() * 4];
		long val = 0;

		// convert hex to decimal
		for (int i = 0; i < hex.length(); i++) {
			char c = hex.charAt(i);
			int d = digits.indexOf(c);
			val = val * 16 + d;
		}

		// convert decimal to binary
		for (int i = 0; i < binaryValue.length; i++) {

			binaryValue[i] = (int) (val % 2);
			val = val / 2;
		}

		return binaryValue;
	}

	public String getCryptogramme(String pan, String key) {

		jdbcTemplate.setResultsMapCaseInsensitive(true);

		SimpleJdbcCall simpleJdbcCallFunction1 = new SimpleJdbcCall(jdbcTemplate).withFunctionName("MY_ENCRYPT")
				.withSchemaName("PORTEUR");
		MapSqlParameterSource map = new MapSqlParameterSource();
		map.addValue("PAN", pan);
		map.addValue("KEY", key);

		String crypto = simpleJdbcCallFunction1.executeFunction(String.class, map);
		return (crypto);
	}

	public static String startsString(int i) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int j = 0; j < i; j++) {
			stringBuilder.append("X");

		}
		return stringBuilder.toString();
	}
	
	@PostMapping("getSwitchTransactionAr")
	public Page<Object[]> getSwitchTransactionAr(@RequestBody TransactionReportingRequest transReportingRequest,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
		    @RequestParam(name = "sortOn") String sortOn,
		    @RequestParam(name = "dir") String dir) {
	
		logger.info(transReportingRequest.toString());
		Page<Object[]> trans = null;


		Order order=null;
	  	if (dir.equals("desc"))
	  		order = new Order(Sort.Direction.DESC, sortOn);
	  	else 
	  		order = new Order(Sort.Direction.ASC, sortOn);

		if (!transReportingRequest.getDate().equals("") && !transReportingRequest.getEndDate().equals("")) {
			if (transReportingRequest.getReversal().equals("oui")) {
				trans = switchTransactionArchiveRepository.reportingWtihStartDateAndReversal(PageRequest.of(page, size, Sort.by(order)),
						transReportingRequest.getPan(), transReportingRequest.getAmount(),
						transReportingRequest.getResponseCode(), transReportingRequest.getMerchantId(),
						transReportingRequest.getAuthCode(), transReportingRequest.getTerminal(),
						transReportingRequest.getDate(), transReportingRequest.getEndDate(), "R",
						transReportingRequest.getFiid(),transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso());

			} else if (transReportingRequest.getReversal().equals("non")) {
				trans = switchTransactionArchiveRepository.reportingWtihStartDateAndNoReversal(
						PageRequest.of(page, size, Sort.by(order)), transReportingRequest.getPan(),
						transReportingRequest.getAmount(), transReportingRequest.getResponseCode(),
						transReportingRequest.getMerchantId(), transReportingRequest.getAuthCode(),
						transReportingRequest.getTerminal(), transReportingRequest.getDate(),
						transReportingRequest.getEndDate(), "R", transReportingRequest.getFiid(),transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso());
			} else {
				trans = switchTransactionArchiveRepository.reportingWtihStartDateAndReversal(PageRequest.of(page, size, Sort.by(order)),
						transReportingRequest.getPan(), transReportingRequest.getAmount(),
						transReportingRequest.getResponseCode(), transReportingRequest.getMerchantId(),
						transReportingRequest.getAuthCode(), transReportingRequest.getTerminal(),
						transReportingRequest.getDate(), transReportingRequest.getEndDate(), "",
						transReportingRequest.getFiid(),transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso());
			}

		} else {
			// with no date interval
			if (transReportingRequest.getReversal().equals("oui")) {
				trans = switchTransactionArchiveRepository.reportingWtihReversal(PageRequest.of(page, size, Sort.by(order)),
						transReportingRequest.getPan(), transReportingRequest.getAmount(),
						transReportingRequest.getResponseCode(), transReportingRequest.getMerchantId(),
						transReportingRequest.getAuthCode(), transReportingRequest.getTerminal(), "R",
						transReportingRequest.getFiid(),transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso());

			} else if (transReportingRequest.getReversal().equals("non")) {

				trans = switchTransactionArchiveRepository.reportingWtihNoReversal(PageRequest.of(page, size, Sort.by(order)),
						transReportingRequest.getPan(), transReportingRequest.getAmount(),
						transReportingRequest.getResponseCode(), transReportingRequest.getMerchantId(),
						transReportingRequest.getAuthCode(), transReportingRequest.getTerminal(), "R",
						transReportingRequest.getFiid(),transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso());
			} else {
				trans = switchTransactionArchiveRepository.reportingWtihReversal(PageRequest.of(page, size, Sort.by(order)),
						transReportingRequest.getPan(), transReportingRequest.getAmount(),
						transReportingRequest.getResponseCode(), transReportingRequest.getMerchantId(),
						transReportingRequest.getAuthCode(), transReportingRequest.getTerminal(), "",
						transReportingRequest.getFiid(),transReportingRequest.getTransactionCode(),
						transReportingRequest.getPosEntry(),
						transReportingRequest.getResponseCodeIso());
			}

		}

		return trans;

	}

	@GetMapping("getTransactionDetailsAr/{id}")
	public SwitchDetails getTransactionDetailsAr(@PathVariable(value = "id") int idSwitch) {
		SwitchTransactionArchive transaction = switchTransactionArchiveRepository.findById(idSwitch).get();
		SwitchDetails details=new SwitchDetails();
		
		details.setSwitchRequestDate(transaction.getSwitchRequestDate());
		details.setSwitchResponseDate(transaction.getSwitchResponseDate());
		details.setSwitchMtiMessage(transaction.getSwitchMtiMessage());
		details.setSwitchPan(transaction.getSwitchPan());
		details.setTransactionCode(transaction.getSwitchProcessingCode().substring(0,2));
		Optional<EmvServiceValues>  emvServiceValues=emvServiceValuesRepository.findByCodeTransaction(transaction.getSwitchProcessingCode().substring(0,2));
		if (emvServiceValues.isPresent())
			details.setTransactionLabel(emvServiceValues.get().getLibelle());
		details.setSwitchAmountTransaction(transaction.getSwitchAmountTransaction());
		
		details.setExpiryDate(transaction.getSwitchExpirationDate());
		details.setTerminalId(transaction.getSwitchAcceptorTerminalId());
		
		details.setMcc(transaction.getSwitchMerchantType());
		
		details.setSwitchRRN(transaction.getSwitchRRN());
		details.setMerchantName(transaction.getSwitchAcceptorAcceptorName());
		details.setMerchantCode(transaction.getSwitchAcceptorMerchantCode());
		details.setCurrency(transaction.getSwitchTransactionCurrencyCode());
		details.setAccountCurrency(transaction.getSwitchDE111());
		details.setSwitchAuthNumber(transaction.getSwitchAuthNumber());
		
		details.setSwitchResponseCode(transaction.getSwitchResponseCode());
		details.setSource(transaction.getSource());
		if (!transaction.getSwitchResponseCode().equals("00")) {
			Optional<ResponseCode> responseCodeObj=responceCodeRepository.findById(transaction.getSwitchAdditionalRespData());
			if (responseCodeObj.isPresent())
				details.setMotif(responseCodeObj.get().getDescription());
		}
		details.setPlafondGlobalDisponible(transaction.getSwitchDE108());
		details.setNbDisponibleGlobal(transaction.getSwitchDE109());
		details.setAccountNum(transaction.getSwitchAccountId1());
		Optional<NationalAcquirerFiid> fiid = nationalAcquirerFiidRepository.findByBin(transaction.getSwitchAcquirerIdenCode());
		if (fiid.isPresent()) 
			details.setBankFiid(fiid.get().getBankCode());
		details.setPosEntryMode(transaction.getSwitchPosEntryMode());
		details.setStatus(transaction.getSource());
		details.setSoldeDisponible(transaction.getSwitchDE110());
		return details;
	}
	@GetMapping("getTvrAr/{id}")
	public List<Boolean> getTvrAr(@PathVariable(value = "id") int idSwitch) {
		SwitchTransactionArchive s = switchTransactionArchiveRepository.findById(idSwitch).get();

		String tvr = s.getTag95();
		int[] binaryCVR = parseHexBinary(tvr);

		logger.info("TVR " + binaryCVR.toString());

		List<Boolean> booleanSet = new ArrayList<>();
		for (int i : binaryCVR) {
			if (i == 0) {
				booleanSet.add(false);
			} else {
				booleanSet.add(true);
			}
		}
		Collections.reverse(booleanSet);
		return booleanSet;
	}

	@GetMapping("getCvrAr/{id}")
	public List<Boolean> getCvrAr(@PathVariable(value = "id") int idSwitch) {
		SwitchTransactionArchive s = switchTransactionArchiveRepository.findById(idSwitch).get();

		String cvr = s.getTag9F10().substring(6, 14) + "00";
		int[] binaryCVR = parseHexBinary(cvr);
		logger.info("CVR " + binaryCVR);
		List<Boolean> booleanSet = new ArrayList<>();
		for (int i : binaryCVR) {
			if (i == 0) {
				booleanSet.add(false);
			} else {
				booleanSet.add(true);
			}
		}
		Collections.reverse(booleanSet);
		return booleanSet;
	}
}
