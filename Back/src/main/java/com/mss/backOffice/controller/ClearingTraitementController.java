package com.mss.backOffice.controller;


import com.mss.backOffice.services.*;
import com.mss.unified.entities.*;

import com.mss.unified.references.RecapInter;
import com.mss.unified.repositories.*;

import javassist.expr.NewArray;


import java.text.ParseException;
import java.util.*;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfStructTreeController.returnType;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.backOffice.request.AccountDetails;
import com.mss.backOffice.request.AccountDisplay;
import com.mss.backOffice.request.AccountDisplayFilter;
import com.mss.backOffice.request.AddFileRequest;

import com.mss.backOffice.request.GetTokenAmplitudeResponse;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;


@RestController
@RequestMapping("/clearingTraitement")
public class ClearingTraitementController {
	@Autowired
	DownloadFileBc downloadFileBc;
	@Autowired 
	private TransactionTemplate transactionTemplate;

	
	@Autowired
	public MerchantRepository merchantRepo;

	@Autowired

	private GetTokenAmplitudeService getTokenAmplitudeService;
	@Autowired
	private PropertyService environment;

	private static final Gson gson = new Gson();
	
	@Autowired
	public TotalAmntInterRepository tmi;

	@Autowired
	public JourneeAppureRepository journeeRepo;


	@Autowired
	OpeningDaySummaryRepository openingDaySummaryRepository;

	@Autowired
	JourneeAppureRepository journeeAppureRepository;
	@Autowired
	OperationCodeRepository ocr;
	@Autowired
	private DayOperationInternationalRepository internationalDayRepo;

	
	private String destinationBankCode = "00105";
	


	
	private static final Logger logger = LoggerFactory.getLogger(ClearingTraitementController.class);
	
	@Autowired
	DayOperationInternationalHRepository dayH;
	@Autowired
	DayOperationInternationalRepository day;
	

	@Autowired
	private TotalAmountInterRepository totalAmountInterRepository;
	@Autowired
	private TotalAmntInterRepository totalAmntInterRepository;
	
	private static final String  grilleRefund= "Grille refund";
	private static final int nbIterationgrille=6;
	private static final String typeMcd="MCD";
	private static final String typeVisa="VISA";
	private static final String operationCodeAchatOctf="05";


	@Autowired
	private ChargebacksInternationalRepository chargebacksInternationalRepository;

	@Autowired
	VisaDisputeStatusAdviceRepository visaDisputeStatusAdviceRepository;

	@Autowired
	ChargebackVisaRepository chargebackVisaRepository;
	
	@Autowired
	private VisaRaportRepository visaRaportRepository;
	
	@Autowired
	MontantOutGoingIPMRepository MogIPM;
	
	@GetMapping("getCurrentOpenedDay")
	public ResponseEntity<String> getCurrentOpenedDay() {
		Optional<OpeningDaySummary> summary = openingDaySummaryRepository.findOpenedDay();
		if (summary.isPresent()) {
			// Parse the input date string
			LocalDate localDate = LocalDate.parse(summary.get().getFileDate());

			// Format the date to "ddMMyy" format
			String fileDate = localDate.format(DateTimeFormatter.ofPattern("YYYY-MM-dd"));
			return ResponseEntity.accepted().body(gson.toJson(fileDate));
			// summary.get().getFileDate();
		} else
			return ResponseEntity.accepted().body(gson.toJson(""));
	}
	
	@GetMapping("getClearedDayInter/{fileDate}")
	public List<JourneeAppuree> getClearedDayInter(@PathVariable String fileDate) {

		System.out.println("fileDate --" + fileDate);

		List<JourneeAppuree> journeeAppuree = journeeRepo.findByDateFichier(fileDate);

		return journeeAppuree;

	}
	
	
	
	@GetMapping("geTotalAmountVisa/{fileDate}")
	public List<TotalAmountSettlementInter> geTotalAmountVisa(@PathVariable(value = "fileDate") String date) {
		ToolsService.print("inter visa fileDate " + date, ToolsService.getLineNumber());

		List<TotalAmountSettlementInter> elements = totalAmountInterRepository.findByFileDate(date);

		// Sort the list, placing null orderRow values at the end
		elements.sort(Comparator.comparing(TotalAmountSettlementInter::getOrderRow, Comparator.nullsLast(Integer::compareTo)));

		ToolsService.print("elements  " + elements.toString(), ToolsService.getLineNumber());

		return elements;
	}
	@GetMapping("getSummInter/{fileDate}/{type}")
	public TotalAmountInter getSummInter(@PathVariable(value = "fileDate") String fileDate,
 
			@PathVariable(value = "type") String type
			){
		if(totalAmntInterRepository.findByFileDateAndType(fileDate, type)!= null && totalAmntInterRepository.findByFileDateAndType(fileDate, type).size()>0) {
		return  totalAmntInterRepository.findByFileDateAndType(fileDate, type).get(0) ;
		}
		return null;
		 
		 
	}
	
	@GetMapping("listofdataEUR/{fileDate}")
	public List<VisaRaport> listofdataEUR(@PathVariable(value = "fileDate") String fileDate) {
		List<VisaRaport> listdataISSUER = visaRaportRepository.findByRaportDateAndTypeAndCurrency(fileDate, "ISSUER", "EUR");
		List<VisaRaport> listdataACQ = visaRaportRepository.findByRaportDateAndTypeAndCurrency(fileDate, "ACQ", "EUR");
		listdataISSUER.addAll(listdataACQ);
	return listdataISSUER;
		}
	
	
	
	@PostMapping("AddJourneeAppureInter")
	public  ResponseEntity<String> AddJourneeAppureInter(@RequestBody JourneeAppuree journee) {
		try {
//		DateTimeFormatter format= DateTimeFormatter.ofPattern("d MMMM, yyyy");
			// Parse the input date string
			LocalDate localDate = LocalDate.parse(journee.getDateFichier());
			// Format the date to "ddMMyy" format
			journee.setDateFichier(localDate.format(DateTimeFormatter.ofPattern("ddMMyy")));
			journee.setStatus("D");
			logger.info(" journee du "+localDate.getDayOfWeek(),ToolsService.getLineNumber() );

				journee.setAmountTotal(journee.getAmount());
				ToolsService.print(" montant total = "+journee.getAmountTotal(),ToolsService.getLineNumber() );
				journee.setStatus("S");
				journee = journeeRepo.save(journee);
			downloadFileBc.writeInFileVISA();
				return ResponseEntity.badRequest().body(gson.toJson("ok"));

		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.error("Exception is=>{}", stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("exception occured while saving"));

		}

	}
	@GetMapping("getMontantOUtgoing/{fileDate}")
	public MontantOutGoingIPM getMontantOUtgoing(@PathVariable(value = "fileDate") String fileDate) {
		if (MogIPM.findByDateInter(fileDate).isPresent()) {
			return MogIPM.findByDateInter(fileDate).get();
		} else
			return null;

	}
	
	
	@PostMapping("AddMontantOUtgoing")
	@Transactional
	public void AddMontantOUtgoing(@RequestBody MontantOutGoingIPM moG) {
		try {
			ToolsService.print(moG.toString(), ToolsService.getLineNumber());
			MogIPM.save(moG);
			List<DayOperationInternational> list = new ArrayList<>();
			list.add(generateDayOP(moG, moG.getSettlement(), "G500"));
			list.add(generateDayOP(moG, moG.getInterchange(), "G501"));
			day.saveAll(list);
			List<DayOperationInternationalH> listH = new ArrayList<>();
			listH.add(generateDayOPH(moG, moG.getSettlement(), "G500"));
			listH.add(generateDayOPH(moG, moG.getInterchange(), "G501"));
			dayH.saveAll(listH);
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.error("Exception is=>{}", stackTrace);
		}

	}
	public DayOperationInternationalH generateDayOPH(MontantOutGoingIPM moG, double mnt, String identificatino) {
		DayOperationInternationalH dop = new DayOperationInternationalH();
		OperationCodeCommision settopCode = ocr.findByIdentification(identificatino).get();
		dop.setAffiliation("  ");
		dop.setCardNumber("                ");
		dop.setCodeFile("   ");
		dop.setTypeOperation(settopCode.getCode());
		dop.setCompletedAmtSettlement(mnt);
		dop.setFileDate(moG.getDateInter());
		dop.setTransactionDate(moG.getDateInter());

		dop.setConversionRateSettlement(1.0);
		dop.setCurrencyCodeSettlement(moG.getCurrency());
		dop.setTransactionCurrencyCode("788");

		return dop;

	}
	public DayOperationInternational generateDayOP(MontantOutGoingIPM moG, double mnt, String identificatino) {
		DayOperationInternational dop = new DayOperationInternational();
		OperationCodeCommision settopCode = ocr.findByIdentification(identificatino).get();
		dop.setAffiliation("  ");
		dop.setCardNumber("                ");
		dop.setCodeFile("   ");
		dop.setTypeOperation(settopCode.getCode());
		dop.setCompletedAmtSettlement(mnt);
		dop.setFileDate(moG.getDateInter());
		dop.setTransactionDate(moG.getDateInter());
		dop.setConversionRateSettlement(1.0);
		dop.setCurrencyCodeSettlement(moG.getCurrency());
dop.setCurrencyCodeTransaction("788");
		return dop;

	}
	@PostMapping("saveSummInter/")
	public ResponseEntity<?> saveSumm(@RequestBody Map<String, String> payload
			) {
		 ToolsService.print("start datas "+payload.toString(),  ToolsService.getLineNumber());

        String amount = payload.get("amount");
        String type = payload.get("type");
        String fileDate = payload.get("fileDate");
        String chosesJson = payload.get("choses");
        totalAmntInterRepository.deleteByFileDate(fileDate);
		 TotalAmountInter tt= new TotalAmountInter();
		 tt.setAmount(amount);
		 tt.setType(type);
		 tt.setFileDate(fileDate);
		 tt.setChoix(chosesJson);
		 ToolsService.print("start saving "+tt.toString(),  ToolsService.getLineNumber());
		 totalAmntInterRepository.save(tt);
		 
		 
        return ResponseEntity.ok("Data received successfully");
		 
		 
	}
	
//	@GetMapping("generatedataEUR/{fileDate}")
//	public List<VisaRaport> generatedataEUR(@PathVariable(value = "fileDate") String fileDate) {
//	 VisaSummary summariesV = visaSummaryRepo.findByDate(fileDate);
//
//	Map<String, TotalVisa> listReturn = new HashMap<>();
//	if(summariesV!=null) {
//	TotalVisa purchaseEmission = totalVisaRepository.getTotalPurchaseEmissionEUR(summariesV.getSummaryCode()).orElse(new TotalVisa());
//	TotalVisa purchaseEmissionCasiCsh = totalVisaRepository.getTotalEmissionCASICASHEUR(summariesV.getSummaryCode()).orElse(new TotalVisa());
//
//	TotalVisa retraitEmission = totalVisaRepository.getTotalWithdrawalEmissionEUR(summariesV.getSummaryCode()).orElse(new TotalVisa());
//	TotalVisa intechangeretraitEmission = totalVisaRepository.getTotalEmissionWithdrawalInterchangeEUR(summariesV.getSummaryCode()).orElse(new TotalVisa());
//	TotalVisa refundRetraitEmission = totalVisaRepository.getTotalRefundEmissionOriginalCreditEUR(summariesV.getSummaryCode()).orElse(new TotalVisa());
//	TotalVisa refundAchatEmission = totalVisaRepository.getTotalRefundEmissionMerchandiseReturnEUR(summariesV.getSummaryCode()).orElse(new TotalVisa());
//	TotalVisa demandeSoldeEmission = totalVisaRepository.getTotalBalanceInquiryEmissionEUR(summariesV.getSummaryCode()).orElse(new TotalVisa());
//	TotalVisa visaNotApprouvedEmission = totalVisaRepository.getTotalAtmDeclineEmissionEUR(summariesV.getSummaryCode()).orElse(new TotalVisa());
//	
//	
//	VisaV2 chargeEmissionv = totalVisaRepository.getTotalVisaChargeEUR(summariesV.getSummaryCode()).orElse(null);
//	List<VisaRaport> raportslist= new ArrayList<>();
//	TotalVisa	chargeEmission=new TotalVisa();
//	chargeEmission.setThirdAmount(chargeEmissionv.getNetAmount());
//	VisaRaport achatISSUER= new VisaRaport();
//	achatISSUER.setLibelle("Achat");
//	achatISSUER.setType("ISSUER");
//	achatISSUER.setCurrency("EUR");
//
//	achatISSUER.setRaportDate(fileDate);
////	achatISSUER.setSettAmt(Double.valueOf(purchaseEmission.getFirstAmount()) );
////	achatISSUER.setNbTrans(Integer.valueOf(purchaseEmission.getFirstCount()) );
////	achatISSUER.setInterAmt(Double.valueOf(purchaseEmission.getSecondAmount()));
//	achatISSUER.setSettAmt(Double.valueOf(purchaseEmission.getFirstAmount())+Double.valueOf(purchaseEmissionCasiCsh.getFirstAmount()) );
//	achatISSUER.setNbTrans(Integer.valueOf(purchaseEmission.getFirstCount())+Integer.valueOf(purchaseEmissionCasiCsh.getFirstCount()) );
//	achatISSUER.setInterAmt(Double.valueOf(purchaseEmission.getSecondAmount())+Double.valueOf(purchaseEmissionCasiCsh.getSecondAmount()) );
//	raportslist.add(achatISSUER );
//
//	
//	VisaRaport retraitISSUER= new VisaRaport();
//	retraitISSUER.setLibelle("retrait");
//	retraitISSUER.setType("ISSUER");
//	retraitISSUER.setCurrency("EUR");
//
//	retraitISSUER.setRaportDate(fileDate);
//	retraitISSUER.setSettAmt(Double.valueOf(retraitEmission.getThirdAmount())-Double.valueOf(retraitEmission.getSecondAmount()));
//	retraitISSUER.setNbTrans(Integer.valueOf(retraitEmission.getFirstCount())+Integer.valueOf(demandeSoldeEmission.getFirstCount())+Integer.valueOf(visaNotApprouvedEmission.getFirstCount() ));
//	retraitISSUER.setInterAmt(-Double.valueOf(intechangeretraitEmission.getSecondAmount())+Double.valueOf(intechangeretraitEmission.getThirdAmount())-Double.valueOf(demandeSoldeEmission.getSecondAmount())+Double.valueOf(demandeSoldeEmission.getThirdAmount())-Double.valueOf(visaNotApprouvedEmission.getSecondAmount())+Double.valueOf(visaNotApprouvedEmission.getThirdAmount()));
//	raportslist.add(retraitISSUER );
//
//	VisaRaport refundISSUER= new VisaRaport();
//	refundISSUER.setLibelle("refund");
//	refundISSUER.setType("ISSUER");
//	refundISSUER.setCurrency("EUR");
//	refundISSUER.setRaportDate(fileDate);
//	refundISSUER.setSettAmt(Double.valueOf(refundRetraitEmission.getFirstAmount())+Double.valueOf(refundAchatEmission.getFirstAmount()));
//	refundISSUER.setNbTrans(Integer.valueOf(refundRetraitEmission.getFirstCount())+Integer.valueOf(refundAchatEmission.getFirstCount()));
//	refundISSUER.setInterAmt(Double.valueOf(refundRetraitEmission.getSecondAmount())+Double.valueOf(refundRetraitEmission.getThirdAmount())-Double.valueOf(refundAchatEmission.getSecondAmount())-Double.valueOf(refundAchatEmission.getThirdAmount()) );
//	
//	raportslist.add(refundISSUER );
//
//	
//	
//	
//	//charge
//	VisaRaport charge= new VisaRaport();
//	charge.setLibelle("charge");
//	charge.setType("ISSUER");
//	charge.setCurrency("EUR");
//	charge.setRaportDate(fileDate);
//	charge.setSettAmt(Double.valueOf(chargeEmissionv.getNetAmount()));
//	raportslist.add(charge );
//
//	visaRaportRepository.saveAll(raportslist);
////	
////	listReturn.put("Achat", purchaseEmission != null ? purchaseEmission : new TotalVisa());
////	listReturn.put("Retrait", retraitEmission != null ? retraitEmission : new TotalVisa());
////	listReturn.put("intechangeretraitEmission", intechangeretraitEmission != null ? intechangeretraitEmission : new TotalVisa());
////	listReturn.put("Refund RETRAIT", refundRetraitEmission != null ? refundRetraitEmission : new TotalVisa());
////	listReturn.put("CREDIT VOUCHER", refundAchatEmission != null ? refundAchatEmission : new TotalVisa());
////	listReturn.put("demandeSoldeEmission", demandeSoldeEmission != null ? demandeSoldeEmission : new TotalVisa());
////	listReturn.put("visaNotApprouvedEmission",
////			visaNotApprouvedEmission != null ? visaNotApprouvedEmission : new TotalVisa());
////	listReturn.put("CHARGEBACK", chargeEmission != null ? chargeEmission : new TotalVisa());
//	 
//	return raportslist;}else {
//		return null;
//	}


	
	
	
	
	
	
	
	
	
	


	@GetMapping("getClearedDay/{fileDate}")
	public JourneeAppuree getClearedDay(@PathVariable String fileDate) {
		// Parse the input date string
		LocalDate localDate = LocalDate.parse(fileDate);

		// Format the date to "ddMMyy" format
		fileDate = localDate.format(DateTimeFormatter.ofPattern("ddMMyy"));

		Optional<JourneeAppuree> journeeAppuree = journeeAppureRepository.findByDateFichierAndType(fileDate, "BCT");
		if (journeeAppuree.isPresent())
			return journeeAppuree.get();

		else
			return null;

	}

	
	@PostMapping("getPagedJournee")
	public Page<JourneeAppuree> getPagedJournee(@RequestBody JourneeAppuree journeeAppuree,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {
		ToolsService.print("called", ToolsService.getLineNumber());
		List<Order> orders = new ArrayList<>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);
		ToolsService.print(journeeAppuree.toString(), ToolsService.getLineNumber());
		Page<JourneeAppuree> jAP = journeeRepo.findByPage(PageRequest.of(page, size, Sort.by(orders)),
				journeeAppuree.getDateFichier(), journeeAppuree.getType());
		return jAP;

	}





	private static Double sumJourneeAppureeAmounts(JourneeAppuree... journees) {
		Double total = 0.0;
		for (JourneeAppuree journee : journees) {
			Double amount =  Double.valueOf(journee.getAmount()) ;

			if (journee.getSign().equals("C")) {
				total = total+amount; // Add if signe is 'C'
			} else if (journee.getSign().equals("D")) {
				total = total-amount; // Subtract if signe is 'D'
			}
			ToolsService.print(total.toString(), ToolsService.getLineNumber());
		}

		return total;
	}


	private Integer getAmountWithDefault(Object amount) {

		if (amount == null) {
			return 0;
		}

		if (amount instanceof Integer) {
			return (Integer) amount;
		}

		if (amount instanceof String) {
			try {
				return Integer.parseInt((String) amount);
			} catch (NumberFormatException e) {
				String stackTrace = Throwables.getStackTraceAsString(e);
				logger.error("Exception is=>{}", stackTrace);
				return 0;
			}
		}

		// Gérer d'autres types d'amount si nécessaire
		return 0;
	}
	
	
}
