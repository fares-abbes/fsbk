package com.mss.backOffice.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.base.Throwables;
import com.mss.backOffice.request.LastSummaryEmbossingRequest;
import com.mss.backOffice.services.DateService;
import com.mss.backOffice.services.DayOperationCardSequenceService;
import com.mss.backOffice.services.HsmService;
import com.mss.backOffice.services.PropertyService;
import com.mss.unified.entities.Account;
import com.mss.unified.entities.AcsSummary;
import com.mss.unified.entities.ActionCard;
import com.mss.unified.entities.AgenceAdministration;
import com.mss.unified.entities.BESummary;
import com.mss.unified.entities.BatchPorteur;
import com.mss.unified.entities.Card;
import com.mss.unified.entities.CardHistory;
import com.mss.unified.entities.CardHolderInfo;
import com.mss.unified.entities.Customer;
import com.mss.unified.entities.DayOperationCardFransaBank;
import com.mss.unified.entities.DayOperationFeesCardSequence;
import com.mss.unified.entities.EmbossingSummary;
import com.mss.unified.entities.EpanSummary;
import com.mss.unified.entities.EpaymentInfo;
import com.mss.unified.entities.OperationCodeCommision;
import com.mss.unified.entities.Product;
import com.mss.unified.entities.Program;
import com.mss.unified.entities.SettelementFransaBank;
import com.mss.unified.entities.StockBin;
import com.mss.unified.entities.TvaCommissionFransaBank;
import com.mss.unified.entities.UnpersonalizedCard;
import com.mss.unified.entities.User;
import com.mss.unified.repositories.AccountRepository;
import com.mss.unified.repositories.AcsSummaryRepository;
import com.mss.unified.repositories.ActionCardRepository;
import com.mss.unified.repositories.AgenceAdministrationRepository;
import com.mss.unified.repositories.BESummaryRepository;
import com.mss.unified.repositories.BatchPorteurRepository;
import com.mss.unified.repositories.CardHistoryRepository;
import com.mss.unified.repositories.CardHolderInfoRepository;
import com.mss.unified.repositories.CardRepository;
import com.mss.unified.repositories.CustomerRepository;
import com.mss.unified.repositories.DayOperationCardFransaBankRepository;
import com.mss.unified.repositories.DayOperationFeesCardSequenceRepository;
import com.mss.unified.repositories.EmbossingSummaryRepository;
import com.mss.unified.repositories.EpanSummaryRepository;
import com.mss.unified.repositories.EpaymentInfoRepository;
import com.mss.unified.repositories.FirstPositionRepository;
import com.mss.unified.repositories.OperationCodeRepository;
import com.mss.unified.repositories.ProductRepositpry;
import com.mss.unified.repositories.ProgramReposiroty;
import com.mss.unified.repositories.SecondPositionRepository;
import com.mss.unified.repositories.SettelementFransaBankRepository;
import com.mss.unified.repositories.StockBinRepository;
import com.mss.unified.repositories.ThirdPositionRepository;
import com.mss.unified.repositories.TvaCommissionFransaBankRepository;
import com.mss.unified.repositories.UnpersonalizedCardRepository;
import com.mss.unified.repositories.UserRepository;
import com.sleepycat.je.utilint.InternalException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import static com.mss.backOffice.BackOfficeApplication.occupiedEv;
import static com.mss.backOffice.BackOfficeApplication.occupiedEd;
import static com.mss.backOffice.BackOfficeApplication.occupiedEi;
import static com.mss.backOffice.BackOfficeApplication.occupiedEr;
import static com.mss.backOffice.BackOfficeApplication.occupiedEm;
import static com.mss.backOffice.BackOfficeApplication.occupiedBe;
import static com.mss.backOffice.BackOfficeApplication.occupiedEm_A2C;
import static com.mss.backOffice.BackOfficeApplication.occupiedEv_A2C;
import static com.mss.backOffice.BackOfficeApplication.occupiedEd_A2C;
import static com.mss.backOffice.BackOfficeApplication.occupiedEi_A2C;
import static com.mss.backOffice.BackOfficeApplication.occupiedEr_A2C;
import static com.mss.backOffice.BackOfficeApplication.occupiedAcs;
@RestController
@RequestMapping("IntegrationFilePorteur")
public class IntegrationPorteurFilesController {

	@Autowired
	private BatchPorteurRepository batchPorteurRepository;
	@Autowired
	private CardRepository cardRepository;
	@Autowired
	private CardHistoryRepository cardHistoryRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private ProgramReposiroty programReposiroty;
	@Autowired
	private CustomerRepository customerRepository;
	@Autowired
	private ActionCardRepository actionCardRepository;
	@Autowired
	private DateService dateService;
	@Autowired
	private PropertyService propertyService;
	@Autowired
	private FirstPositionRepository firstPositionRepository;
	@Autowired
	private SecondPositionRepository secondPositionRepository;
	@Autowired
	private ThirdPositionRepository thirdPositionRepository;
	@Autowired
	private StockBinRepository stockBinRepository;
	@Autowired
	private AgenceAdministrationRepository agenceAdministrationRepository;
	@Autowired
	private UnpersonalizedCardRepository unpersonalizedCardRepository;
	@Autowired
	private HsmService hsmService;
	@Autowired
	private EmbossingSummaryRepository embossingSummaryRepository;
	@Autowired
	private EpanSummaryRepository epanSummaryRepository;
	private static final Logger logger = LoggerFactory.getLogger(IntegrationPorteurFilesController.class);
	@Autowired
	private EpaymentInfoRepository epaymentInfoRepository;
	@Autowired
	private BESummaryRepository bESummaryRepository;
	@Autowired
	private SettelementFransaBankRepository settlementRepo;
	@Autowired
	private DayOperationCardFransaBankRepository operationRepo;
	@Autowired
	private DayOperationCardSequenceService dayOperationCardSequenceService;
	@Autowired
	private TvaCommissionFransaBankRepository tvaRepo;
	@Autowired
	private ProductRepositpry productRepositpry;

	@Autowired
	private ProgramReposiroty programRepository;
    @Autowired
    CardHolderInfoRepository cardHolderInfoRepository;
	@Autowired
	private AcsSummaryRepository acsSummaryRepository;
	@Autowired
	private OperationCodeRepository operationCodeInternationalRepository;
	
	@Transactional
	private Card writeInFile(PrintWriter writer, Card card, ActionCard action, String operation)
			throws KeyManagementException, NoSuchAlgorithmException {
		// DateFormat dateFormat = new SimpleDateFormat("MMyy");
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		DateFormat effectivedateFormat = new SimpleDateFormat("yyyyMM");

		// DateFormat expDateFormat = new SimpleDateFormat("yyMM");
		DateFormat dateFormatExpDateEmbossing = new SimpleDateFormat("MM/yy");
		Optional<Program> foundedProgram = programReposiroty.findByProgramCode(card.getProgrameId());
		boolean international = false;
		String serviceCode = ""
				+ firstPositionRepository.findByFpscCode(foundedProgram.get().getFirstPosition()).get().getFpscNum()
				+ secondPositionRepository.findBySpscCode(foundedProgram.get().getSecondPosition()).get().getSpscNum()
				+ thirdPositionRepository.findByTpscCode(foundedProgram.get().getThirdPosition()).get().getTpscNum();

		//String clientName = "";
		String customerAddress = "";

		String cardName = card.getCardName() != null ? card.getCardName() : "";
		cardName = cardName.toUpperCase();
		String gender = card.getGender() != null ? card.getGender() : "";
		String accountNumber = "";

		String primaryAuth = "001";
		String pan = card.getClearCardNum();
		logger.info("pan is => {}", pan);
		// String val = "4";
		String formattedPan = pan.replaceAll("(.{4})", "$0 ").trim() + "~";
		formattedPan = StringUtils.rightPad(formattedPan, 30);

		String expDate = dateFormat.format(card.getExpiryDate());

		String oldExpDate = expDate;
		if (card.getNewExpDate() != null) {
			expDate = dateFormat.format(card.getNewExpDate());
		}

		String embossingReason = "";
		//String APSN = "00";
		String APSN = card.getSequenceNumber()!=null?card.getSequenceNumber():"00";

		String previousAPSN = "";
		Date currentDate = new Date();
		String cmsCardType = foundedProgram.get().getCmsCardType();

		// String cmsCardType="0000002";
		if (action != null) {

			if (action.getIsRenewel() == 1) {
				embossingReason = "002";

				// operationType = "4";
				Date newDate = action.getCardNewExpDate();
				expDate = dateFormat.format(dateService.expireCard(newDate));
				card.setNewExpDate(dateService.expireCard(newDate));

				LocalDate localDate = card.getNewExpDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				card.setPreDate(dateService.getPreDate(localDate));

			} else if (action.getIsReplacement() == 1) {
				embossingReason = action.getRemplacementReason();
				if (card.getOldPvv()!=null) {
					card.setOldPvv(null);
				}
				if (embossingReason.equals("005")) {
					cardName = action.getNameInCard().toUpperCase();
					card.setCardName(cardName);
				}
				if (embossingReason.equals("Demande mobile")) {
					embossingReason="006";
				}
				if (action.getReplacementOldCard() != null) {

					if (action.getReplacementOldCard().equals("YES")) {

						if (action.getCardNewExpDate() != null) {

							Date newDate = action.getCardNewExpDate();
							expDate = dateFormat.format(dateService.expireCard(newDate));
							card.setNewExpDate(dateService.expireCard(newDate));

							LocalDate localDate = card.getNewExpDate().toInstant().atZone(ZoneId.systemDefault())
									.toLocalDate();
							card.setPreDate(dateService.getPreDate(localDate));

						}

					}
				}

			} else
				embossingReason = "001";
			// operationType = "2";

		} else {
			// embossingReason = "D";
			if (operation.equals("renewel")) {
				embossingReason = "002";
				// operationType = "4";

				LocalDate localDate = card.getExpiryDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				LocalDate newLocalDate = localDate.plusYears(foundedProgram.get().getCprLifeCycle());
				// Date
				// newDate=Date.from(newLocalDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

				card.setNewExpDate(dateService.expireCard(newLocalDate));
				expDate = dateFormat.format(card.getNewExpDate());

				// System.out.println(Date.from(newLocalDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
				card.setPreDate(dateService.getPreDate(newLocalDate));

			} else {
				embossingReason = "001";

			}

		}
		
		String randomPin = "";
		if (operation.equals("creation")) {
			randomPin = hsmService.generateRandomPin(pan);
			logger.info("randomPin is    " + randomPin);
			if (randomPin.equals("error"))
				throw new InternalException();
			card.setRandomPin(randomPin);
		} else {
			if (card.getRandomPin() != null)
				randomPin = card.getRandomPin();
			else {
				randomPin = hsmService.generateRandomPin(pan);
				logger.info("randomPin is    " + randomPin);
				if (randomPin.equals("error"))
					throw new InternalException();
				card.setRandomPin(randomPin);
			}
		}

		
		String cvv="error";
		
		String icvv ="";
		
		String cvv2 = "error";
				
		String pvv = "error";
		String pinBlock = "error";
		String cvv2ExpDate = expDate.substring(2, expDate.length() - 2);

		cvv2ExpDate = cvv2ExpDate.substring(2, 4) + cvv2ExpDate.substring(0, 2);	
		
		if (serviceCode.startsWith("2") || serviceCode.startsWith("1")) {
			
			//if  international bin
			
			cvv = hsmService.generateCvv(pan, expDate.substring(2, expDate.length() - 2), serviceCode,
					String.valueOf(foundedProgram.get().getBinOnUsCode()),true);
			
			cvv2 =hsmService.generateCvv(pan, cvv2ExpDate, "000",
					String.valueOf(foundedProgram.get().getBinOnUsCode()),true);
			
			pvv = hsmService.generatePvv(pan, randomPin, String.valueOf(foundedProgram.get().getBinOnUsCode()),true);

			pinBlock = hsmService.generatePinBlock(pan, randomPin, String.valueOf(foundedProgram.get().getBinOnUsCode()),true);

			icvv = hsmService.generateCvv(pan, expDate.substring(2, expDate.length() - 2), "999",
					String.valueOf(foundedProgram.get().getBinOnUsCode()),true);
			logger.info("icvv is    " + icvv);
			
			if (icvv.equals("error"))
				throw new InternalException();
			
			international = true;
		}else {
			
			cvv = hsmService.generateCvv(pan, expDate.substring(2, expDate.length() - 2), serviceCode,
					String.valueOf(foundedProgram.get().getBinOnUsCode()),false);
			
			cvv2 =hsmService.generateCvv(pan, cvv2ExpDate, "000",
					String.valueOf(foundedProgram.get().getBinOnUsCode()),false);
			pvv = hsmService.generatePvv(pan, randomPin, String.valueOf(foundedProgram.get().getBinOnUsCode()),false);
			pinBlock = hsmService.generatePinBlock(pan, randomPin, String.valueOf(foundedProgram.get().getBinOnUsCode()),false);
		}
		
		

		logger.info("pinBlock is    " + pinBlock);
		if (pinBlock.equals("error"))
			throw new InternalException();
		logger.info("pvv is    " + pvv);
		if (pvv.equals("error"))
			throw new InternalException();

		logger.info("cvv is    " + cvv);

		if (cvv.equals("error"))
			throw new InternalException();
		logger.info("cvv2 is    " + cvv2);

		if (cvv2.equals("error"))
			throw new InternalException();


		
		
		

		

		
	

		
//		if (action != null) {
////			
////			if (action.getIsReplacement() == 1 && action.isRemplacementWithPin()) 
////				pinBlock="                ";
////			else {
//				
//				
//		//	}
//		}else {
//
//			 pinBlock=hsmService.generatePinBlock(pan, randomPin, String.valueOf(foundedProgram.get().getBinOnUsCode()));
//			
//			logger.info("pinBlock is    "+pinBlock);
//			if (pinBlock.equals("error"))
//				throw new InternalException();
//			
//		}

		String track1CardName = cardName;
		if (track1CardName.length() > 26) {
			track1CardName=track1CardName.substring(0, 26);
		}
		String cvvTowrite = international ? icvv : cvv;
		if (international) {
			track1CardName=track1CardName.replaceFirst(" ", "/");
		}
		
		String track1Data = "%B" + pan + "^" + StringUtils.rightPad(track1CardName, 26) + "^";
		track1Data = track1Data + expDate.substring(2, expDate.length() - 2) + serviceCode + "1" + pvv + (international ? "00" : "" )+ cvv + (international ? "000000?" : "" );
		
		if (international) {
			track1Data =  StringUtils.rightPad(track1Data, 78);

		}else {
			track1Data = StringUtils.rightPad(track1Data, 77, "0") + "?";
		}

		// track1DataFirstPart+StringUtils.rightPad(cardName,
		// cardName.length()+lengthToPadd)+track1DataSecondPart;
		String track2Data = ";" + pan + "=";
		track2Data = track2Data + expDate.substring(2, expDate.length() - 2) + serviceCode + "1" + pvv + cvv;
		track2Data = StringUtils.rightPad(track2Data, 38, "0") + "?";

		Optional<Account> account = accountRepository.findByAccountCode(card.getAccCode());
		int docTypeCode = 0;
		String customerIdentity = "";
		String cutsomerFirstName = "";
		String cutsomerLastName = "";
		if (account.isPresent()) {
			//clientName = account.get().getAccountName();
			accountNumber = account.get().getAccountNum();
			Optional<Customer> customer = customerRepository
					.findByCustomerCode(Integer.parseInt(account.get().getCustomerCode()));
			if (customer.isPresent()) {
				customerAddress = customer.get().getCustomerAddress() != null ? customer.get().getCustomerAddress()
						: "";

				docTypeCode = customer.get().getIdentityCode();
				customerIdentity = customer.get().getClearCustomerIdentidy();
				cutsomerFirstName = customer.get().getCustomerFirstName() != null
						? customer.get().getCustomerFirstName()
						: "";
				cutsomerLastName = customer.get().getCustomerLastName() != null ? customer.get().getCustomerLastName()
						: "";

				if (cutsomerLastName.length() > 26)
					cutsomerLastName = cutsomerLastName.substring(0, 26);

				if (cutsomerFirstName.length() > 26)
					cutsomerFirstName = cutsomerFirstName.substring(0, 26);

				if (customerIdentity.length() > 15)
					customerIdentity = customerIdentity.substring(0, 15);
			}

		}

		//clientName = StringUtils.rightPad(clientName, 26);
		customerAddress = StringUtils.rightPad(customerAddress, 40);
		if (customerAddress.length() > 40) {
			customerAddress = customerAddress.substring(0, 40);
		}

		pan = StringUtils.rightPad(pan, 19);

		String replacesPan = "";
		if (embossingReason.equals("001")) {

			replacesPan = StringUtils.leftPad("", 19);
			//previousAPSN = "00";
			previousAPSN =card.getSequenceNumber()!=null?card.getSequenceNumber():"00";

			oldExpDate = "        ";
		} else {
			replacesPan = pan;
			//previousAPSN = "00";
			previousAPSN =card.getSequenceNumber()!=null?card.getSequenceNumber():"00";

		}

		String icaRefNum = "0000";
		String branchRef = StringUtils.leftPad("" + card.getAgencyCode(), 10);
		String branchName = "";
		Optional<AgenceAdministration> branch = agenceAdministrationRepository
				.findByInitial(String.valueOf(card.getAgencyCode()));
		if (branch.isPresent()) {
			branchName = branch.get().getLibelle();

			if (branchName.length() > 40)
				branchName = branchName.substring(0, 40);
			// accountNumber="0"+accountNumber;
		}

		String bankRef = "      FSBK";
		accountNumber = "0" + accountNumber;
		accountNumber = StringUtils.leftPad(accountNumber, 25);
		// String cvv="858";
		String pinOffset = "0000";
		// String cvv2="233";
		// String pinBlock="EF75FC96F6BCEA9B";

		String startDate = effectivedateFormat.format(card.getStartDate());
		
		String effectiveDate=international ? startDate+"01":startDate;
		String embossingExpDate = "";
		if (card.getNewExpDate() != null) {
			embossingExpDate = dateFormatExpDateEmbossing.format(card.getNewExpDate());
		} else {
			embossingExpDate = dateFormatExpDateEmbossing.format(card.getExpiryDate());
		}
		String embossingLine2 = StringUtils.rightPad(embossingExpDate + "~", 30);
		
		String embossingLine1 = gender + " " + cardName;
		
		if (embossingLine1.length() > 26)
			embossingLine1=embossingLine1.substring(0, 26);
		
		 embossingLine1 = StringUtils.rightPad(embossingLine1 + "~", 30);
		
		String raisonSocial="";
		if(card.getRaisonSocial()!=null)
			raisonSocial= card.getRaisonSocial();
		
		if (raisonSocial.length() > 26)
			raisonSocial=raisonSocial.substring(0, 26);
		
		raisonSocial=raisonSocial+"~";
		String embossingLine3 = StringUtils.rightPad(raisonSocial, 30);
		
		//String embossingLine4 = StringUtils.rightPad("~", 30);

		String signatureData = StringUtils.rightPad("", 30);

		String cardType = cmsCardType.substring(cmsCardType.length() - 2, cmsCardType.length());
		// String cardType ="02";
		String cardVisual = foundedProgram.get().getCardvisualCode() != null ? foundedProgram.get().getCardvisualCode()
				: "";
		if (cardVisual.length() > 2)
			cardVisual = cardVisual.substring(0, 2);

		cardVisual = StringUtils.rightPad(cardVisual, 2);

		String addressBlanks = StringUtils.leftPad("", 40);
		
		
		String lineInFile="0001" + dateFormat.format(currentDate) + "     " + pan.substring(0, 6) + cmsCardType + primaryAuth
				+ embossingReason + pan + APSN + expDate + replacesPan + previousAPSN + oldExpDate + formattedPan
				+ embossingLine1 + embossingLine2 + embossingLine3 + embossingLine3 + signatureData + track1Data
				+ track2Data + effectiveDate + icaRefNum + serviceCode + branchRef + bankRef + accountNumber + "1" + pvv
				+ cvvTowrite + pinOffset + cvv2 + pinBlock + startDate.substring(0, 4)
				+ StringUtils.rightPad(cutsomerLastName.toUpperCase(), 26)
				+ StringUtils.rightPad(cutsomerFirstName.toUpperCase(), 26) + StringUtils.rightPad(gender, 26)
				+ StringUtils.leftPad("" + docTypeCode, 3, "0") + StringUtils.rightPad(customerIdentity, 15)
				+ "               " + "000" + StringUtils.rightPad(cutsomerLastName.toUpperCase(), 30)
				+ StringUtils.rightPad(cutsomerFirstName.toUpperCase(), 30) + customerAddress + addressBlanks
				+ addressBlanks + addressBlanks + cardType + StringUtils.rightPad("00000500", 36) + cardVisual

				+ addressBlanks + addressBlanks + addressBlanks + "000000000000000"
				+ "FRANSABANK ELDJAZAIR ALGERIE            " + StringUtils.rightPad(branchName, 40);
		if (international) {
			writer.println(lineInFile);

		}else {
			writer.print(lineInFile);

		}
		return card;

	}

	@Transactional
	private void writeInFileBe(PrintWriter writer, Card card, Account account, Customer customer, EpaymentInfo info)
			throws KeyManagementException, NoSuchAlgorithmException {
		String indicateur = info.getStatus();
		String pan = card.getClearCardNum();
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		String expDate = dateFormat.format(card.getExpiryDate());

		String cutsomerFirstName = customer.getCustomerFirstName() != null ? customer.getCustomerFirstName() : "";
		cutsomerFirstName = StringUtils.rightPad(cutsomerFirstName.toUpperCase(), 30);
		String cutsomerLastName = customer.getCustomerLastName() != null ? customer.getCustomerLastName() : "";
		cutsomerLastName = StringUtils.rightPad(cutsomerLastName.toUpperCase(), 30);

		String email = customer.getEmail() != null ? customer.getEmail() : "";
		email = StringUtils.rightPad(email, 100);

		String customerAddress = customer.getCustomerAddress() != null ? customer.getCustomerAddress() : "";
		customerAddress = StringUtils.rightPad(customerAddress, 40);
		if (customerAddress.length() > 40) {
			customerAddress = customerAddress.substring(0, 40);
		}
		String addressBlanks = StringUtils.leftPad("", 40);
		String numTel = "+213" + info.getPhoneNumber();
		numTel = StringUtils.rightPad(numTel, 15);
		if (numTel.length() > 15)
			numTel = numTel.substring(0, 15);

		String numContrat = info.getNumContrat();
		String accountNumber = account.getAccountNum();
		accountNumber = "0" + accountNumber;
		accountNumber = StringUtils.rightPad(accountNumber, 20);
		writer.println(indicateur + pan + expDate + cutsomerLastName + cutsomerFirstName + email + customerAddress
				+ addressBlanks + addressBlanks + numTel + numContrat + accountNumber + "M"

		);
	}

	@PostMapping(value = "getLastSummary")
	public EmbossingSummary getLasSummary(@RequestBody LastSummaryEmbossingRequest request) {

		Optional<EmbossingSummary> lastSummary = embossingSummaryRepository.getRowMax(request.getDate(),
				request.getKey());
		if (lastSummary.isPresent()) {

			return lastSummary.get();
		}

		else
			return null;
	}

	@PostMapping(value = "getLastBeSummary")
	public BESummary getLastBeSummary(@RequestBody LastSummaryEmbossingRequest request) {

		Optional<BESummary> lastSummary = bESummaryRepository.getRowMax(request.getDate());
		if (lastSummary.isPresent()) {

			return lastSummary.get();
		}

		else
			return null;
	}
	
	@PostMapping(value = "getLastAcsSummary")
	public AcsSummary getLastAcsSummary(@RequestBody LastSummaryEmbossingRequest request) {

		Optional<AcsSummary> lastSummary = acsSummaryRepository.getRowMax(request.getDate());
		if (lastSummary.isPresent()) {

			return lastSummary.get();
		}

		else
			return null;
	}
	
	@Transactional
	private EmbossingSummary buildSummary(String fileKey) {
		EmbossingSummary summary = new EmbossingSummary();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

		SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyMMdd");
		Date date = new Date();
		String generationDate = dateFormat.format(date);
		summary.setFileKey(fileKey);
		Optional<EmbossingSummary> lastSummary = embossingSummaryRepository.getRowMax(generationDate,
				summary.getFileKey());

		summary.setGenerationDate(dateFormat.format(date));
		summary.setGenerationDateTime(date);

		if (lastSummary.isPresent())
			summary.setSequence(lastSummary.get().getSequence() + 1);
		else
			summary.setSequence(1);

		summary.setFileName(
				summary.getFileKey() +(summary.getFileKey().contains("A2C")?"_":"") + fileNameFormat.format(date) + String.format("%02d", summary.getSequence()));

		return summary;
	}

	@Transactional
	private BESummary buildSummaryBe() {
		BESummary summary = new BESummary();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

		SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyMMdd");
		Date date = new Date();
		String generationDate = dateFormat.format(date);

		Optional<BESummary> lastSummary = bESummaryRepository.getRowMax(generationDate);

		summary.setGenerationDate(dateFormat.format(date));
		summary.setGenerationDateTime(date);

		if (lastSummary.isPresent())
			summary.setSequence(lastSummary.get().getSequence() + 1);
		else
			summary.setSequence(1);

		summary.setFileName("BE" + fileNameFormat.format(date) + String.format("%02d", summary.getSequence()));

		return summary;
	}
	
	@Transactional
	private AcsSummary buildSummaryAcs() {
		AcsSummary summary = new AcsSummary();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

		Date date = new Date();
		String generationDate = dateFormat.format(date);

		Optional<AcsSummary> lastSummary = acsSummaryRepository.getRowMax(generationDate);

		summary.setGenerationDate(dateFormat.format(date));
		summary.setGenerationDateTime(date);

		if (lastSummary.isPresent())
			summary.setSequence(lastSummary.get().getSequence() + 1);
		else
			summary.setSequence(1);

		summary.setFileName("ACS." + dateService.generateDateAndTime());

		return summary;
	}

	@PostMapping(value = "generateFileEmbossingReplacementWithPin", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
	public byte[] generateFileEmbossingReplacementWithPin() throws Exception {
		//try {
			logger.info("*********begin generateFileEmbossingReplacementWithPin*********");
			if (!occupiedEv) {
				
				occupiedEv=true;
			List<Card> cardsToSave = new ArrayList<Card>();
			List<ActionCard> actionsToSave = new ArrayList<ActionCard>();

			File file = new File(propertyService.getFilePath());

			PrintWriter writer = new PrintWriter(file, "UTF-8");
			List<ActionCard> actions = actionCardRepository.findAllNationalReplacementWithPinActions();
			List<CardHistory> cardHistories = new ArrayList<CardHistory>();
			String username = SecurityContextHolder.getContext().getAuthentication().getName();
			Optional<User> user = userRepository.findByUserNameOrUserEmail(username, username);
			List<DayOperationCardFransaBank> dayOperations = new ArrayList<DayOperationCardFransaBank>();

			if (actions.size() > 0) {
				EmbossingSummary summary = buildSummary("EV");

				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat filedateFormat = new SimpleDateFormat("yyMMdd");
				SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
				SimpleDateFormat datePieceComptableFormat = new SimpleDateFormat("ddMMyy");

				Date date = new Date();

				String transactionDate = dateFormat.format(date);
				String transactionTime = timeFormat.format(date);

				TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
				float tvaBase = Float.parseFloat(tvaCommissionFransaBank.getTva()) / 100;

				float tva = tvaBase + 1;
				logger.info("TVA is => {}", tva);

				SettelementFransaBank settlc005 = settlementRepo.findByIdentificationbh("C005");
				SettelementFransaBank settlc006 = settlementRepo.findByIdentificationbh("C006");

				SettelementFransaBank settlc013 = settlementRepo.findByIdentificationbh("C013");
				SettelementFransaBank settlc014 = settlementRepo.findByIdentificationbh("C014");
				
				for (ActionCard action : actions) {
					Optional<Card> card = cardRepository.findByCardCode(action.getCardCode());
					if (card.isPresent()) {
						Card foundedCard = card.get();
						foundedCard = writeInFile(writer, foundedCard, action, "");
						action.setStatus(0);
						foundedCard.setCardActionsStatus("6");
						foundedCard.setLastEmbossingSummaryFile(summary.getFileName());
						foundedCard.setModifDate(new Date());
						//if (!action.getReplacementOldCard().equals("YES"))
						foundedCard.setAtc("0");
						actionsToSave.add(action);
						cardsToSave.add(foundedCard);

						CardHistory cardHistory = new CardHistory();
						cardHistory.setCardCode(foundedCard.getCardCode());
						// cardHistory.setOperation("Carte (remplacement avec PIN) envoyé à SATIM
						// (fichier: "+summary.getFileName()+")");

						if (action.getRemplacementReason().equals("005")) {
							cardHistory
									.setOperation("Carte mal embossée (remplacement avec PIN) envoyé à SATIM (fichier: "
											+ summary.getFileName() + ")");

						} else {
							cardHistory
									.setOperation("Carte endommagée (remplacement avec PIN) envoyé à SATIM (fichier: "
											+ summary.getFileName() + ")");

						}

						cardHistory.setOperation_date(new Date());
						cardHistory.setEditedBy(user.get().getUserName());
						cardHistories.add(cardHistory);

						Optional<Product> product = productRepositpry.findByProductCode(foundedCard.getProductCode());
						if (product.get().getCpCommissionRemplacement()>0) {
							
						
						Optional<Account> account = accountRepository.findByAccountCode(foundedCard.getAccCode());

						DayOperationFeesCardSequence seq = dayOperationCardSequenceService.getSequence();
						DayOperationFeesCardSequence seqPieceComptable = dayOperationCardSequenceService
								.getSequencePieceComptable();

						int settlementAmountHt = Math.round(product.get().getCpCommissionRemplacement() / tva);
						logger.info("settlementAmountHt is => {}", settlementAmountHt);

						int settlementAmountTva = product.get().getCpCommissionRemplacement() - settlementAmountHt;
						logger.info("settlementAmountTva is => {}", settlementAmountTva);

						int transactionAmount = product.get().getCpCommissionRemplacement();
						logger.info("transactionAmount is => {}", transactionAmount);

						String montantTrans = String.format("%014d", transactionAmount);
						montantTrans = montantTrans.substring(0, 12) + "." + montantTrans.substring(12);
						
						if (product.get().getLibelle().toLowerCase().contains("gold")) {
							DayOperationCardFransaBank C005 = new DayOperationCardFransaBank();

							C005.setCodeAgence(foundedCard.getAgencyCode());
							C005.setCodeBankAcquereur("035");
							C005.setCodeBank("035");
							C005.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()) );
							C005.setDateTransaction(transactionDate);
							C005.setHeureTransaction(transactionTime);
							C005.setCompteCredit(settlc005.getCreditAccount());
							C005.setNumCartePorteur(foundedCard.getCardNum());
							C005.setNumtransaction(String.format("%012d", seq.getSequence()));
							C005.setIdenfication(settlc005.getIdentificationbh());
							C005.setMontantSettlement(settlementAmountHt);
							C005.setMontantTransaction(montantTrans);
							C005.setLibelleCommercant("REFECTION MB00432");

							C005.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
							C005.setRefernceLettrage(datePieceComptableFormat.format(date) + foundedCard.getCardNum());
							C005.setFileDate(filedateFormat.format(date));
							C005.setNumAutorisation(C005.getNumtransaction());
							C005.setNumRefTransaction(C005.getNumtransaction());
							C005.setCardProduct("gold");

							// seq=dayOperationCardSequenceService.incrementSequence(seq);

							DayOperationCardFransaBank C006 = new DayOperationCardFransaBank();

							C006.setCodeAgence(foundedCard.getAgencyCode());
							C006.setCodeBankAcquereur("035");
							C006.setCodeBank("035");
							C006.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
							C006.setDateTransaction(transactionDate);
							C006.setHeureTransaction(transactionTime);
							C006.setCompteCredit(settlc006.getCreditAccount());
							C006.setNumCartePorteur(foundedCard.getCardNum());
							C006.setNumtransaction(String.format("%012d", seq.getSequence()));
							C006.setIdenfication(settlc006.getIdentificationbh());
							C006.setMontantSettlement(settlementAmountTva);
							C006.setMontantTransaction(montantTrans);
							C006.setLibelleCommercant("REFECTION MB00432");

							C006.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
							C006.setRefernceLettrage(datePieceComptableFormat.format(date) + foundedCard.getCardNum());
							C006.setFileDate(filedateFormat.format(date));
							C006.setNumAutorisation(C006.getNumtransaction());
							C006.setNumRefTransaction(C006.getNumtransaction());

							C006.setCardProduct("gold");

							dayOperations.add(C005);
							dayOperations.add(C006);

						} else {
							DayOperationCardFransaBank C013 = new DayOperationCardFransaBank();

							C013.setCodeAgence(foundedCard.getAgencyCode());
							C013.setCodeBankAcquereur("035");
							C013.setCodeBank("035");
							C013.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
							C013.setDateTransaction(transactionDate);
							C013.setHeureTransaction(transactionTime);
							C013.setCompteCredit(settlc013.getCreditAccount());
							C013.setNumCartePorteur(foundedCard.getCardNum());
							C013.setNumtransaction(String.format("%012d", seq.getSequence()));
							C013.setIdenfication(settlc013.getIdentificationbh());
							C013.setMontantSettlement(settlementAmountHt);
							C013.setMontantTransaction(montantTrans);
							C013.setLibelleCommercant("REFECTION MB00432");

							C013.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
							C013.setRefernceLettrage(datePieceComptableFormat.format(date) + foundedCard.getCardNum());
							C013.setFileDate(filedateFormat.format(date));
							C013.setNumAutorisation(C013.getNumtransaction());
							C013.setNumRefTransaction(C013.getNumtransaction());
							C013.setCardProduct("classic");

							// seq=dayOperationCardSequenceService.incrementSequence(seq);

							DayOperationCardFransaBank C014 = new DayOperationCardFransaBank();

							C014.setCodeAgence(foundedCard.getAgencyCode());
							C014.setCodeBankAcquereur("035");
							C014.setCodeBank("035");
							C014.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
							C014.setDateTransaction(transactionDate);
							C014.setHeureTransaction(transactionTime);
							C014.setCompteCredit(settlc014.getCreditAccount());
							C014.setNumCartePorteur(foundedCard.getCardNum());
							C014.setNumtransaction(String.format("%012d", seq.getSequence()));
							C014.setIdenfication(settlc014.getIdentificationbh());
							C014.setMontantSettlement(settlementAmountTva);
							C014.setMontantTransaction(montantTrans);
							C014.setLibelleCommercant("REFECTION MB00432");

							C014.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
							C014.setRefernceLettrage(datePieceComptableFormat.format(date) + foundedCard.getCardNum());
							C014.setFileDate(filedateFormat.format(date));
							C014.setNumAutorisation(C014.getNumtransaction());
							C014.setNumRefTransaction(C014.getNumtransaction());

							C014.setCardProduct("classic");

							dayOperations.add(C013);
							dayOperations.add(C014);
						}

						seq = dayOperationCardSequenceService.incrementSequence(seq);
						seqPieceComptable = dayOperationCardSequenceService
								.incrementSequencePieceComptable(seqPieceComptable);
						}
					}

				}

				if (cardsToSave.size() > 0) {
					summary.setCardsNumber(cardsToSave.size());

					embossingSummaryRepository.save(summary);

					cardRepository.saveAll(cardsToSave);
					actionCardRepository.saveAll(actionsToSave);
					cardHistoryRepository.saveAll(cardHistories);
					if (dayOperations.size() > 0)
						operationRepo.saveAll(dayOperations);
					batchPorteurRepository.updateFileName("EV", summary.getFileName());

				}

			}
			writer.close();

			InputStream in = new FileInputStream(file);

			batchPorteurRepository.updateFinishBatch("EV", 1, new Date());
			occupiedEv=false;
			logger.info("*********end generateFileEmbossingReplacementWithPin**********");

			return IOUtils.toByteArray(in);
		}else{
			logger.info("*********occupied generateFileEmbossingReplacementWithPin**********");
			return null;
		 }

	}
	
	@PostMapping(value = "generateFileEmbossingVisaReplacementWithPin", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
	public byte[] generateFileEmbossingVisaReplacementWithPin() throws Exception {
		//try {
			logger.info("*********begin generateFileEmbossingVisaReplacementWithPin*********");
			if (!occupiedEv_A2C) {
				
				occupiedEv_A2C=true;
			List<Card> cardsToSave = new ArrayList<Card>();
			List<ActionCard> actionsToSave = new ArrayList<ActionCard>();

			File file = new File(propertyService.getFilePath());

			PrintWriter writer = new PrintWriter(file, "UTF-8");
			List<ActionCard> actions = actionCardRepository.findAllInternationalReplacementWithPinActions();
			List<CardHistory> cardHistories = new ArrayList<CardHistory>();
			String username = SecurityContextHolder.getContext().getAuthentication().getName();
			Optional<User> user = userRepository.findByUserNameOrUserEmail(username, username);
			List<DayOperationCardFransaBank> dayOperations = new ArrayList<DayOperationCardFransaBank>();

			if (actions.size() > 0) {
				EmbossingSummary summary = buildSummary("EV_A2C");

				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat filedateFormat = new SimpleDateFormat("yyMMdd");
				SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
				SimpleDateFormat datePieceComptableFormat = new SimpleDateFormat("ddMMyy");

				Date date = new Date();

				String transactionDate = dateFormat.format(date);
				String transactionTime = timeFormat.format(date);

				TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
				float tvaBase = Float.parseFloat(tvaCommissionFransaBank.getTva()) / 100;

				float tva = tvaBase + 1;
				logger.info("TVA is => {}", tva);
				
				OperationCodeCommision settlc023 = operationCodeInternationalRepository.findByIdentification("C023").get();
				OperationCodeCommision settlc024 = operationCodeInternationalRepository.findByIdentification("C024").get();
							
				for (ActionCard action : actions) {
					Optional<Card> card = cardRepository.findByCardCode(action.getCardCode());
					if (card.isPresent()) {
						Card foundedCard = card.get();
						foundedCard = writeInFile(writer, foundedCard, action, "");
						action.setStatus(0);
						foundedCard.setCardActionsStatus("6");
						foundedCard.setLastEmbossingSummaryFile(summary.getFileName());
						foundedCard.setModifDate(new Date());
						//if (!action.getReplacementOldCard().equals("YES"))
						foundedCard.setAtc("0");
						actionsToSave.add(action);
						cardsToSave.add(foundedCard);

						CardHistory cardHistory = new CardHistory();
						cardHistory.setCardCode(foundedCard.getCardCode());
						// cardHistory.setOperation("Carte (remplacement avec PIN) envoyé à SATIM
						// (fichier: "+summary.getFileName()+")");

						if (action.getRemplacementReason().equals("005")) {
							cardHistory
									.setOperation("Carte mal embossée (remplacement avec PIN) envoyé à A2C (fichier: "
											+ summary.getFileName() + ")");

						} else {
							cardHistory
									.setOperation("Carte endommagée (remplacement avec PIN) envoyé à A2C (fichier: "
											+ summary.getFileName() + ")");

						}

						cardHistory.setOperation_date(new Date());
						cardHistory.setEditedBy(user.get().getUserName());
						cardHistories.add(cardHistory);

						Optional<Product> product = productRepositpry.findByProductCode(foundedCard.getProductCode());
						if (product.get().getCpCommissionRemplacement()>0) {
							
						
						Optional<Account> account = accountRepository.findByAccountCode(foundedCard.getAccCode());

						DayOperationFeesCardSequence seq = dayOperationCardSequenceService.getSequence();
						DayOperationFeesCardSequence seqPieceComptable = dayOperationCardSequenceService
								.getSequencePieceComptable();

						int settlementAmountHt = Math.round(product.get().getCpCommissionRemplacement() / tva);
						logger.info("settlementAmountHt is => {}", settlementAmountHt);

						int settlementAmountTva = product.get().getCpCommissionRemplacement() - settlementAmountHt;
						logger.info("settlementAmountTva is => {}", settlementAmountTva);

						int transactionAmount = product.get().getCpCommissionRemplacement();
						logger.info("transactionAmount is => {}", transactionAmount);

						String montantTrans = String.format("%014d", transactionAmount);
						montantTrans = montantTrans.substring(0, 12) + "." + montantTrans.substring(12);
						
							DayOperationCardFransaBank C023 = new DayOperationCardFransaBank();
							
							C023.setCodeAgence(foundedCard.getAgencyCode());
							C023.setCodeBankAcquereur("035");
							C023.setCodeBank("035");
							C023.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()) );
							C023.setDateTransaction(transactionDate);
							C023.setHeureTransaction(transactionTime);
							C023.setCompteCredit(settlc023.getCreditAccount());
							C023.setNumCartePorteur(foundedCard.getCardNum());
							C023.setNumtransaction(String.format("%012d", seq.getSequence()));
							C023.setIdenfication(settlc023.getIdentification());
							C023.setMontantSettlement(settlementAmountHt);
							C023.setMontantTransaction(montantTrans);
							C023.setLibelleCommercant("REFECTION MB00432");

							C023.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
							C023.setRefernceLettrage(datePieceComptableFormat.format(date) + foundedCard.getCardNum());
							C023.setFileDate(filedateFormat.format(date));
							C023.setNumAutorisation(C023.getNumtransaction());
							C023.setNumRefTransaction(C023.getNumtransaction());
							C023.setCardProduct("visa");

							// seq=dayOperationCardSequenceService.incrementSequence(seq);

							DayOperationCardFransaBank C024 = new DayOperationCardFransaBank();

							C024.setCodeAgence(foundedCard.getAgencyCode());
							C024.setCodeBankAcquereur("035");
							C024.setCodeBank("035");
							C024.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
							C024.setDateTransaction(transactionDate);
							C024.setHeureTransaction(transactionTime);
							C024.setCompteCredit(settlc024.getCreditAccount());
							C024.setNumCartePorteur(foundedCard.getCardNum());
							C024.setNumtransaction(String.format("%012d", seq.getSequence()));
							C024.setIdenfication(settlc024.getIdentification());
							C024.setMontantSettlement(settlementAmountTva);
							C024.setMontantTransaction(montantTrans);
							C024.setLibelleCommercant("REFECTION MB00432");

							C024.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
							C024.setRefernceLettrage(datePieceComptableFormat.format(date) + foundedCard.getCardNum());
							C024.setFileDate(filedateFormat.format(date));
							C024.setNumAutorisation(C024.getNumtransaction());
							C024.setNumRefTransaction(C024.getNumtransaction());

							C024.setCardProduct("visa");

							dayOperations.add(C023);
							dayOperations.add(C024);
						
						seq = dayOperationCardSequenceService.incrementSequence(seq);
						seqPieceComptable = dayOperationCardSequenceService
								.incrementSequencePieceComptable(seqPieceComptable);
						}
					}

				}

				if (cardsToSave.size() > 0) {
					summary.setCardsNumber(cardsToSave.size());

					embossingSummaryRepository.save(summary);

					cardRepository.saveAll(cardsToSave);
					actionCardRepository.saveAll(actionsToSave);
					cardHistoryRepository.saveAll(cardHistories);
					if (dayOperations.size() > 0)
						operationRepo.saveAll(dayOperations);
					batchPorteurRepository.updateFileName("EV_A2C", summary.getFileName());

				}

			}
			writer.close();

			InputStream in = new FileInputStream(file);

			batchPorteurRepository.updateFinishBatch("EV_A2C", 1, new Date());
			occupiedEv_A2C=false;
			logger.info("*********end generateFileEmbossingVisaReplacementWithPin**********");

			return IOUtils.toByteArray(in);
		}else{
			logger.info("*********occupied generateFileEmbossingVisaReplacementWithPin**********");
			return null;
		 }

	}

	@PostMapping(value = "generateFileEmbossingReplacementWithountPin", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
	public byte[] generateFileEmbossingReplacementWithoutPin() throws Exception {
		//try {
			logger.info("*********begin generateFileEmbossingReplacementWithoutPin*********");
		if (!occupiedEd) {
			occupiedEd=true;
			List<Card> cardsToSave = new ArrayList<Card>();
			List<ActionCard> actionsToSave = new ArrayList<ActionCard>();

			File file = new File(propertyService.getFilePath());

			PrintWriter writer = new PrintWriter(file, "UTF-8");
			List<ActionCard> actions = actionCardRepository.findAllNationalReplacementWithoutPinActions();
			List<CardHistory> cardHistories = new ArrayList<CardHistory>();

			String username = SecurityContextHolder.getContext().getAuthentication().getName();
			Optional<User> user = userRepository.findByUserNameOrUserEmail(username, username);

			List<DayOperationCardFransaBank> dayOperations = new ArrayList<DayOperationCardFransaBank>();

			if (actions.size() > 0) {
				EmbossingSummary summary = buildSummary("ED");
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat filedateFormat = new SimpleDateFormat("yyMMdd");
				SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
				SimpleDateFormat datePieceComptableFormat = new SimpleDateFormat("ddMMyy");
				Date date = new Date();

				String transactionDate = dateFormat.format(date);
				String transactionTime = timeFormat.format(date);

				TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
				float tvaBase = Float.parseFloat(tvaCommissionFransaBank.getTva()) / 100;

				float tva = tvaBase + 1;
				logger.info("TVA is => {}", tva);

				SettelementFransaBank settlc005 = settlementRepo.findByIdentificationbh("C005");
				SettelementFransaBank settlc006 = settlementRepo.findByIdentificationbh("C006");

				SettelementFransaBank settlc013 = settlementRepo.findByIdentificationbh("C013");
				SettelementFransaBank settlc014 = settlementRepo.findByIdentificationbh("C014");

				for (ActionCard action : actions) {
					Optional<Card> card = cardRepository.findByCardCode(action.getCardCode());
					if (card.isPresent()) {
						Card foundedCard = card.get();
						foundedCard = writeInFile(writer, foundedCard, action, "");
						action.setStatus(0);
						foundedCard.setCardActionsStatus("6");
						foundedCard.setLastEmbossingSummaryFile(summary.getFileName());
						foundedCard.setModifDate(new Date());
						foundedCard.setAtc("0");

						actionsToSave.add(action);
						cardsToSave.add(foundedCard);

						CardHistory cardHistory = new CardHistory();
						cardHistory.setCardCode(foundedCard.getCardCode());
						if (action.getRemplacementReason().equals("005")) {
							cardHistory
									.setOperation("Carte mal embossée (remplacement sans PIN) envoyé à SATIM (fichier: "
											+ summary.getFileName() + ")");

						} else {
							cardHistory
									.setOperation("Carte endommagée (remplacement sans PIN) envoyé à SATIM (fichier: "
											+ summary.getFileName() + ")");

						}

						// cardHistory.setOperation("Carte (remplacement sans PIN) envoyé à SATIM
						// (fichier: "+summary.getFileName()+")");
						cardHistory.setOperation_date(new Date());
						cardHistory.setEditedBy(user.get().getUserName());
						cardHistories.add(cardHistory);

						Optional<Product> product = productRepositpry.findByProductCode(foundedCard.getProductCode());
						if (product.get().getCpCommissionRemplacement()>0) {
							
						
						
						Optional<Account> account = accountRepository.findByAccountCode(foundedCard.getAccCode());
						DayOperationFeesCardSequence seq = dayOperationCardSequenceService.getSequence();

						DayOperationFeesCardSequence seqPieceComptable = dayOperationCardSequenceService
								.getSequencePieceComptable();

						int settlementAmountHt = Math.round(product.get().getCpCommissionRemplacement() / tva);
						logger.info("settlementAmountHt is => {}", settlementAmountHt);

						int settlementAmountTva = product.get().getCpCommissionRemplacement() - settlementAmountHt;
						logger.info("settlementAmountTva is => {}", settlementAmountTva);

						int transactionAmount = product.get().getCpCommissionRemplacement();
						logger.info("transactionAmount is => {}", transactionAmount);

						String montantTrans = String.format("%014d", transactionAmount);
						montantTrans = montantTrans.substring(0, 12) + "." + montantTrans.substring(12);
						
						if (product.get().getLibelle().toLowerCase().contains("gold")) {
							DayOperationCardFransaBank C005 = new DayOperationCardFransaBank();

							C005.setCodeAgence(foundedCard.getAgencyCode());
							C005.setCodeBankAcquereur("035");
							C005.setCodeBank("035");
							C005.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
							C005.setDateTransaction(transactionDate);
							C005.setHeureTransaction(transactionTime);
							C005.setCompteCredit(settlc005.getCreditAccount());
							C005.setNumCartePorteur(foundedCard.getCardNum());
							C005.setNumtransaction(String.format("%012d", seq.getSequence()));
							C005.setIdenfication(settlc005.getIdentificationbh());
							C005.setMontantSettlement(settlementAmountHt);
							C005.setMontantTransaction(montantTrans);
							C005.setLibelleCommercant("REFECTION MB00432");

							C005.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
							C005.setRefernceLettrage(datePieceComptableFormat.format(date) + foundedCard.getCardNum());
							C005.setFileDate(filedateFormat.format(date));
							C005.setNumAutorisation(C005.getNumtransaction());
							C005.setNumRefTransaction(C005.getNumtransaction());
							C005.setCardProduct("gold");

							// seq=dayOperationCardSequenceService.incrementSequence(seq);

							DayOperationCardFransaBank C006 = new DayOperationCardFransaBank();

							C006.setCodeAgence(foundedCard.getAgencyCode());
							C006.setCodeBankAcquereur("035");
							C006.setCodeBank("035");
							C006.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
							C006.setDateTransaction(transactionDate);
							C006.setHeureTransaction(transactionTime);
							C006.setCompteCredit(settlc006.getCreditAccount());
							C006.setNumCartePorteur(foundedCard.getCardNum());
							C006.setNumtransaction(String.format("%012d", seq.getSequence()));
							C006.setIdenfication(settlc006.getIdentificationbh());
							C006.setMontantSettlement(settlementAmountTva);
							C006.setMontantTransaction(montantTrans);
							C006.setLibelleCommercant("REFECTION MB00432");

							C006.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
							C006.setRefernceLettrage(datePieceComptableFormat.format(date) + foundedCard.getCardNum());
							C006.setFileDate(filedateFormat.format(date));
							C006.setNumAutorisation(C006.getNumtransaction());
							C006.setNumRefTransaction(C006.getNumtransaction());

							C006.setCardProduct("gold");

							dayOperations.add(C005);
							dayOperations.add(C006);

						} else {
							DayOperationCardFransaBank C013 = new DayOperationCardFransaBank();

							C013.setCodeAgence(foundedCard.getAgencyCode());
							C013.setCodeBankAcquereur("035");
							C013.setCodeBank("035");
							C013.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
							C013.setDateTransaction(transactionDate);
							C013.setHeureTransaction(transactionTime);
							C013.setCompteCredit(settlc013.getCreditAccount());
							C013.setNumCartePorteur(foundedCard.getCardNum());
							C013.setNumtransaction(String.format("%012d", seq.getSequence()));
							C013.setIdenfication(settlc013.getIdentificationbh());
							C013.setMontantSettlement(settlementAmountHt);
							C013.setMontantTransaction(montantTrans);
							C013.setLibelleCommercant("REFECTION MB00432");

							C013.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
							C013.setRefernceLettrage(datePieceComptableFormat.format(date) + foundedCard.getCardNum());
							C013.setFileDate(filedateFormat.format(date));
							C013.setNumAutorisation(C013.getNumtransaction());
							C013.setNumRefTransaction(C013.getNumtransaction());
							C013.setCardProduct("classic");

							// seq=dayOperationCardSequenceService.incrementSequence(seq);

							DayOperationCardFransaBank C014 = new DayOperationCardFransaBank();

							C014.setCodeAgence(foundedCard.getAgencyCode());
							C014.setCodeBankAcquereur("035");
							C014.setCodeBank("035");
							C014.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
							C014.setDateTransaction(transactionDate);
							C014.setHeureTransaction(transactionTime);
							C014.setCompteCredit(settlc014.getCreditAccount());
							C014.setNumCartePorteur(foundedCard.getCardNum());
							C014.setNumtransaction(String.format("%012d", seq.getSequence()));
							C014.setIdenfication(settlc014.getIdentificationbh());
							C014.setMontantSettlement(settlementAmountTva);
							C014.setMontantTransaction(montantTrans);
							C014.setLibelleCommercant("REFECTION MB00432");

							C014.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
							C014.setRefernceLettrage(datePieceComptableFormat.format(date) + foundedCard.getCardNum());
							C014.setFileDate(filedateFormat.format(date));
							C014.setNumAutorisation(C014.getNumtransaction());
							C014.setNumRefTransaction(C014.getNumtransaction());

							C014.setCardProduct("classic");

							dayOperations.add(C013);
							dayOperations.add(C014);
						}

						seq = dayOperationCardSequenceService.incrementSequence(seq);
						seqPieceComptable = dayOperationCardSequenceService
								.incrementSequencePieceComptable(seqPieceComptable);
						}
					}

				}

				if (cardsToSave.size() > 0) {
					summary.setCardsNumber(cardsToSave.size());

					embossingSummaryRepository.save(summary);

					cardRepository.saveAll(cardsToSave);
					actionCardRepository.saveAll(actionsToSave);
					cardHistoryRepository.saveAll(cardHistories);
					if (dayOperations.size() > 0)
						operationRepo.saveAll(dayOperations);

					batchPorteurRepository.updateFileName("ED", summary.getFileName());

				}
			}
			writer.close();
			InputStream in = new FileInputStream(file);

			batchPorteurRepository.updateFinishBatch("ED", 1, new Date());
			occupiedEd=false;
			logger.info("*********end generateFileEmbossingReplacementWithoutPin**********");

			return IOUtils.toByteArray(in);
		}else{
			logger.info("*********occupied generateFileEmbossingReplacementWithoutPin**********");
			return null;
		 } 

	}

	@PostMapping(value = "generateFileEmbossingVisaReplacementWithountPin", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
	public byte[] generateFileEmbossingVisaReplacementWithoutPin() throws Exception {
		//try {
			logger.info("*********begin generateFileEmbossingVisaReplacementWithoutPin*********");
		if (!occupiedEd_A2C) {
			occupiedEd_A2C=true;
			List<Card> cardsToSave = new ArrayList<Card>();
			List<ActionCard> actionsToSave = new ArrayList<ActionCard>();

			File file = new File(propertyService.getFilePath());

			PrintWriter writer = new PrintWriter(file, "UTF-8");
			List<ActionCard> actions = actionCardRepository.findAllInternationalReplacementWithoutPinActions();
			List<CardHistory> cardHistories = new ArrayList<CardHistory>();

			String username = SecurityContextHolder.getContext().getAuthentication().getName();
			Optional<User> user = userRepository.findByUserNameOrUserEmail(username, username);

			List<DayOperationCardFransaBank> dayOperations = new ArrayList<DayOperationCardFransaBank>();

			if (actions.size() > 0) {
				EmbossingSummary summary = buildSummary("ED_A2C");
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat filedateFormat = new SimpleDateFormat("yyMMdd");
				SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
				SimpleDateFormat datePieceComptableFormat = new SimpleDateFormat("ddMMyy");
				Date date = new Date();

				String transactionDate = dateFormat.format(date);
				String transactionTime = timeFormat.format(date);

				TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
				float tvaBase = Float.parseFloat(tvaCommissionFransaBank.getTva()) / 100;

				float tva = tvaBase + 1;
				logger.info("TVA is => {}", tva);

				OperationCodeCommision settlc023 = operationCodeInternationalRepository.findByIdentification("C023").get();
				OperationCodeCommision settlc024 = operationCodeInternationalRepository.findByIdentification("C024").get();
			
				for (ActionCard action : actions) {
					Optional<Card> card = cardRepository.findByCardCode(action.getCardCode());
					if (card.isPresent()) {
						Card foundedCard = card.get();
						foundedCard = writeInFile(writer, foundedCard, action, "");
						action.setStatus(0);
						foundedCard.setCardActionsStatus("6");
						foundedCard.setLastEmbossingSummaryFile(summary.getFileName());
						foundedCard.setModifDate(new Date());
						foundedCard.setAtc("0");

						actionsToSave.add(action);
						cardsToSave.add(foundedCard);

						CardHistory cardHistory = new CardHistory();
						cardHistory.setCardCode(foundedCard.getCardCode());
						if (action.getRemplacementReason().equals("005")) {
							cardHistory
									.setOperation("Carte mal embossée (remplacement sans PIN) envoyé à A2C (fichier: "
											+ summary.getFileName() + ")");

						} else {
							cardHistory
									.setOperation("Carte endommagée (remplacement sans PIN) envoyé à A2C (fichier: "
											+ summary.getFileName() + ")");

						}

						// cardHistory.setOperation("Carte (remplacement sans PIN) envoyé à SATIM
						// (fichier: "+summary.getFileName()+")");
						cardHistory.setOperation_date(new Date());
						cardHistory.setEditedBy(user.get().getUserName());
						cardHistories.add(cardHistory);

						Optional<Product> product = productRepositpry.findByProductCode(foundedCard.getProductCode());
						if (product.get().getCpCommissionRemplacement()>0) {
							
						
						
						Optional<Account> account = accountRepository.findByAccountCode(foundedCard.getAccCode());
						DayOperationFeesCardSequence seq = dayOperationCardSequenceService.getSequence();

						DayOperationFeesCardSequence seqPieceComptable = dayOperationCardSequenceService
								.getSequencePieceComptable();

						int settlementAmountHt = Math.round(product.get().getCpCommissionRemplacement() / tva);
						logger.info("settlementAmountHt is => {}", settlementAmountHt);

						int settlementAmountTva = product.get().getCpCommissionRemplacement() - settlementAmountHt;
						logger.info("settlementAmountTva is => {}", settlementAmountTva);

						int transactionAmount = product.get().getCpCommissionRemplacement();
						logger.info("transactionAmount is => {}", transactionAmount);

						String montantTrans = String.format("%014d", transactionAmount);
						montantTrans = montantTrans.substring(0, 12) + "." + montantTrans.substring(12);

						DayOperationCardFransaBank C023 = new DayOperationCardFransaBank();
						
						C023.setCodeAgence(foundedCard.getAgencyCode());
						C023.setCodeBankAcquereur("035");
						C023.setCodeBank("035");
						C023.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()) );
						C023.setDateTransaction(transactionDate);
						C023.setHeureTransaction(transactionTime);
						C023.setCompteCredit(settlc023.getCreditAccount());
						C023.setNumCartePorteur(foundedCard.getCardNum());
						C023.setNumtransaction(String.format("%012d", seq.getSequence()));
						C023.setIdenfication(settlc023.getIdentification());
						C023.setMontantSettlement(settlementAmountHt);
						C023.setMontantTransaction(montantTrans);
						C023.setLibelleCommercant("REFECTION MB00432");

						C023.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
						C023.setRefernceLettrage(datePieceComptableFormat.format(date) + foundedCard.getCardNum());
						C023.setFileDate(filedateFormat.format(date));
						C023.setNumAutorisation(C023.getNumtransaction());
						C023.setNumRefTransaction(C023.getNumtransaction());
						C023.setCardProduct("visa");

						// seq=dayOperationCardSequenceService.incrementSequence(seq);

						DayOperationCardFransaBank C024 = new DayOperationCardFransaBank();

						C024.setCodeAgence(foundedCard.getAgencyCode());
						C024.setCodeBankAcquereur("035");
						C024.setCodeBank("035");
						C024.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
						C024.setDateTransaction(transactionDate);
						C024.setHeureTransaction(transactionTime);
						C024.setCompteCredit(settlc024.getCreditAccount());
						C024.setNumCartePorteur(foundedCard.getCardNum());
						C024.setNumtransaction(String.format("%012d", seq.getSequence()));
						C024.setIdenfication(settlc024.getIdentification());
						C024.setMontantSettlement(settlementAmountTva);
						C024.setMontantTransaction(montantTrans);
						C024.setLibelleCommercant("REFECTION MB00432");

						C024.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
						C024.setRefernceLettrage(datePieceComptableFormat.format(date) + foundedCard.getCardNum());
						C024.setFileDate(filedateFormat.format(date));
						C024.setNumAutorisation(C024.getNumtransaction());
						C024.setNumRefTransaction(C024.getNumtransaction());

						C024.setCardProduct("visa");

						dayOperations.add(C023);
						dayOperations.add(C024);



						seq = dayOperationCardSequenceService.incrementSequence(seq);
						seqPieceComptable = dayOperationCardSequenceService
								.incrementSequencePieceComptable(seqPieceComptable);
						}
					}

				}

				if (cardsToSave.size() > 0) {
					summary.setCardsNumber(cardsToSave.size());

					embossingSummaryRepository.save(summary);

					cardRepository.saveAll(cardsToSave);
					actionCardRepository.saveAll(actionsToSave);
					cardHistoryRepository.saveAll(cardHistories);
					if (dayOperations.size() > 0)
						operationRepo.saveAll(dayOperations);

					batchPorteurRepository.updateFileName("ED_A2C", summary.getFileName());

				}
			}
			writer.close();
			InputStream in = new FileInputStream(file);

			batchPorteurRepository.updateFinishBatch("ED_A2C", 1, new Date());
			occupiedEd_A2C=false;
			logger.info("*********end generateFileEmbossingVisaReplacementWithoutPin**********");

			return IOUtils.toByteArray(in);
		}else{
			logger.info("*********occupied generateFileEmbossingVisaReplacementWithoutPin**********");
			return null;
		 } 

	}
	@PostMapping(value = "generateFileEmbossingRecalculPin", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
	public byte[] generateFileEmbossingRecalculPin() throws Exception {
		//try {
			logger.info("*********begin generateFileEmbossingRecalculPin*********");
		if (!occupiedEi) {
			occupiedEi=true;

			List<Card> cardsToSave = new ArrayList<Card>();
			List<ActionCard> actionsToSave = new ArrayList<ActionCard>();
			List<DayOperationCardFransaBank> dayOperations = new ArrayList<DayOperationCardFransaBank>();

			File file = new File(propertyService.getFilePath());

			PrintWriter writer = new PrintWriter(file, "UTF-8");
			List<ActionCard> actions = actionCardRepository.findAllNationalChangePinActions();
			List<CardHistory> cardHistories = new ArrayList<CardHistory>();
			String username = SecurityContextHolder.getContext().getAuthentication().getName();
			Optional<User> user = userRepository.findByUserNameOrUserEmail(username, username);

			if (actions.size() > 0) {
				EmbossingSummary summary = buildSummary("EI");
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat filedateFormat = new SimpleDateFormat("yyMMdd");
				SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
				SimpleDateFormat datePieceComptableFormat = new SimpleDateFormat("ddMMyy");

				Date date = new Date();

				String transactionDate = dateFormat.format(date);
				String transactionTime = timeFormat.format(date);

				TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
				float tvaBase = Float.parseFloat(tvaCommissionFransaBank.getTva()) / 100;

				float tva = tvaBase + 1;
				logger.info("TVA is => {}", tva);

				SettelementFransaBank settlc007 = settlementRepo.findByIdentificationbh("C007");
				SettelementFransaBank settlc008 = settlementRepo.findByIdentificationbh("C008");

				SettelementFransaBank settlc015 = settlementRepo.findByIdentificationbh("C015");
				SettelementFransaBank settlc016 = settlementRepo.findByIdentificationbh("C016");

				for (ActionCard action : actions) {
					Optional<Card> card = cardRepository.findByCardCode(action.getCardCode());
					if (card.isPresent()) {
						Card foundedCard = card.get();
						foundedCard = writeInFile(writer, foundedCard, action, "");
						action.setStatus(0);
						foundedCard.setCardActionsStatus("6");
						foundedCard.setLastEmbossingSummaryFile(summary.getFileName());
						foundedCard.setModifDate(new Date());
						actionsToSave.add(action);
						cardsToSave.add(foundedCard);

						CardHistory cardHistory = new CardHistory();
						cardHistory.setCardCode(foundedCard.getCardCode());
						if (action.getReeditionPinReason().equals("001"))
							cardHistory
									.setOperation("Carte (réedition PIN : Code oublié/perdu) envoyé à SATIM (fichier: "
											+ summary.getFileName() + ")");
						else
							cardHistory.setOperation("Carte (réedition PIN : Code illisible) envoyé à SATIM (fichier: "
									+ summary.getFileName() + ")");
						cardHistory.setOperation_date(new Date());
						cardHistory.setEditedBy(user.get().getUserName());
						cardHistories.add(cardHistory);

						// if motif 001 => lost or forgotten => apply fees
						if (action.getReeditionPinReason().equals("001")) {
							Optional<Product> product = productRepositpry
									.findByProductCode(foundedCard.getProductCode());
							if (product.get().getCpCommissionPin()>0) {
								

							
							Optional<Account> account = accountRepository.findByAccountCode(foundedCard.getAccCode());


							DayOperationFeesCardSequence seq = dayOperationCardSequenceService.getSequence();
							DayOperationFeesCardSequence seqPieceComptable = dayOperationCardSequenceService
									.getSequencePieceComptable();

							int settlementAmountHt = Math.round(product.get().getCpCommissionPin() / tva);
							logger.info("settlementAmountHt is => {}", settlementAmountHt);

							int settlementAmountTva = product.get().getCpCommissionPin() - settlementAmountHt;
							logger.info("settlementAmountTva is => {}", settlementAmountTva);

							int transactionAmount = product.get().getCpCommissionPin();
							logger.info("transactionAmount is => {}", transactionAmount);

							String montantTrans = String.format("%014d", transactionAmount);
							montantTrans = montantTrans.substring(0, 12) + "." + montantTrans.substring(12);

							if (product.get().getLibelle().toLowerCase().contains("gold")) {
								DayOperationCardFransaBank C007 = new DayOperationCardFransaBank();

								C007.setCodeAgence(foundedCard.getAgencyCode());
								C007.setCodeBankAcquereur("035");
								C007.setCodeBank("035");
								C007.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
								C007.setDateTransaction(transactionDate);
								C007.setHeureTransaction(transactionTime);
								C007.setCompteCredit(settlc007.getCreditAccount());
								C007.setNumCartePorteur(foundedCard.getCardNum());
								C007.setNumtransaction(String.format("%012d", seq.getSequence()));
								C007.setIdenfication(settlc007.getIdentificationbh());
								C007.setMontantSettlement(settlementAmountHt);
								C007.setMontantTransaction(montantTrans);
								C007.setLibelleCommercant("REEDITION MB00432");

								C007.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
								C007.setRefernceLettrage(
										datePieceComptableFormat.format(date) + foundedCard.getCardNum());
								C007.setFileDate(filedateFormat.format(date));
								C007.setNumAutorisation(C007.getNumtransaction());
								C007.setNumRefTransaction(C007.getNumtransaction());

								C007.setCardProduct("gold");

								// seq=dayOperationCardSequenceService.incrementSequence(seq);

								DayOperationCardFransaBank C008 = new DayOperationCardFransaBank();

								C008.setCodeAgence(foundedCard.getAgencyCode());
								C008.setCodeBankAcquereur("035");
								C008.setCodeBank("035");
								C008.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
								C008.setDateTransaction(transactionDate);
								C008.setHeureTransaction(transactionTime);
								C008.setCompteCredit(settlc008.getCreditAccount());
								C008.setNumCartePorteur(foundedCard.getCardNum());
								C008.setNumtransaction(String.format("%012d", seq.getSequence()));
								C008.setIdenfication(settlc008.getIdentificationbh());
								C008.setMontantSettlement(settlementAmountTva);
								C008.setMontantTransaction(montantTrans);
								C008.setLibelleCommercant("REEDITION MB00432");

								C008.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
								C008.setRefernceLettrage(
										datePieceComptableFormat.format(date) + foundedCard.getCardNum());
								C008.setFileDate(filedateFormat.format(date));
								C008.setNumAutorisation(C008.getNumtransaction());
								C008.setNumRefTransaction(C008.getNumtransaction());

								C008.setCardProduct("gold");

								dayOperations.add(C007);
								dayOperations.add(C008);

							} else {
								DayOperationCardFransaBank C015 = new DayOperationCardFransaBank();

								C015.setCodeAgence(foundedCard.getAgencyCode());
								C015.setCodeBankAcquereur("035");
								C015.setCodeBank("035");
								C015.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
								C015.setDateTransaction(transactionDate);
								C015.setHeureTransaction(transactionTime);
								C015.setCompteCredit(settlc015.getCreditAccount());
								C015.setNumCartePorteur(foundedCard.getCardNum());
								C015.setNumtransaction(String.format("%012d", seq.getSequence()));
								C015.setIdenfication(settlc015.getIdentificationbh());
								C015.setMontantSettlement(settlementAmountHt);
								C015.setMontantTransaction(montantTrans);
								C015.setLibelleCommercant("REEDITION MB00432");

								C015.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
								C015.setRefernceLettrage(
										datePieceComptableFormat.format(date) + foundedCard.getCardNum());
								C015.setFileDate(filedateFormat.format(date));
								C015.setNumAutorisation(C015.getNumtransaction());
								C015.setNumRefTransaction(C015.getNumtransaction());

								C015.setCardProduct("classic");

								// seq=dayOperationCardSequenceService.incrementSequence(seq);

								DayOperationCardFransaBank C016 = new DayOperationCardFransaBank();

								C016.setCodeAgence(foundedCard.getAgencyCode());
								C016.setCodeBankAcquereur("035");
								C016.setCodeBank("035");
								C016.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
								C016.setDateTransaction(transactionDate);
								C016.setHeureTransaction(transactionTime);
								C016.setCompteCredit(settlc016.getCreditAccount());
								C016.setNumCartePorteur(foundedCard.getCardNum());
								C016.setNumtransaction(String.format("%012d", seq.getSequence()));
								C016.setIdenfication(settlc016.getIdentificationbh());
								C016.setMontantSettlement(settlementAmountTva);
								C016.setMontantTransaction(montantTrans);
								C016.setLibelleCommercant("REEDITION MB00432");

								C016.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
								C016.setRefernceLettrage(
										datePieceComptableFormat.format(date) + foundedCard.getCardNum());
								C016.setFileDate(filedateFormat.format(date));
								C016.setNumAutorisation(C016.getNumtransaction());
								C016.setNumRefTransaction(C016.getNumtransaction());

								C016.setCardProduct("classic");

								dayOperations.add(C015);
								dayOperations.add(C016);
							}

							seq = dayOperationCardSequenceService.incrementSequence(seq);
							seqPieceComptable = dayOperationCardSequenceService
									.incrementSequencePieceComptable(seqPieceComptable);
						}
						}

					}
				}

				if (cardsToSave.size() > 0) {
					summary.setCardsNumber(cardsToSave.size());

					embossingSummaryRepository.save(summary);

					cardRepository.saveAll(cardsToSave);
					actionCardRepository.saveAll(actionsToSave);

					cardHistoryRepository.saveAll(cardHistories);
					if (dayOperations.size() > 0)
					operationRepo.saveAll(dayOperations);
					batchPorteurRepository.updateFileName("EI", summary.getFileName());

				}
			}

			writer.close();
			InputStream in = new FileInputStream(file);

			batchPorteurRepository.updateFinishBatch("EI", 1, new Date());
			occupiedEi=false;
			logger.info("*********end generateFileEmbossingRecalculPin**********");

			return IOUtils.toByteArray(in);
		}else{
			logger.info("*********occupied generateFileEmbossingRecalculPin**********");
			return null;
		 }

	}
	
	
	@PostMapping(value = "generateFileEmbossingVisaRecalculPin", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
	public byte[] generateFileEmbossingVisaRecalculPin() throws Exception {
		//try {
			logger.info("*********begin generateFileEmbossingVisaRecalculPin*********");
		if (!occupiedEi_A2C) {
			occupiedEi_A2C=true;

			List<Card> cardsToSave = new ArrayList<Card>();
			List<ActionCard> actionsToSave = new ArrayList<ActionCard>();
			List<DayOperationCardFransaBank> dayOperations = new ArrayList<DayOperationCardFransaBank>();

			File file = new File(propertyService.getFilePath());

			PrintWriter writer = new PrintWriter(file, "UTF-8");
			List<ActionCard> actions = actionCardRepository.findAllInternationalChangePinActions();
			List<CardHistory> cardHistories = new ArrayList<CardHistory>();
			String username = SecurityContextHolder.getContext().getAuthentication().getName();
			Optional<User> user = userRepository.findByUserNameOrUserEmail(username, username);

			if (actions.size() > 0) {
				EmbossingSummary summary = buildSummary("EI_A2C");
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat filedateFormat = new SimpleDateFormat("yyMMdd");
				SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
				SimpleDateFormat datePieceComptableFormat = new SimpleDateFormat("ddMMyy");

				Date date = new Date();

				String transactionDate = dateFormat.format(date);
				String transactionTime = timeFormat.format(date);

				TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
				float tvaBase = Float.parseFloat(tvaCommissionFransaBank.getTva()) / 100;

				float tva = tvaBase + 1;
				logger.info("TVA is => {}", tva);

			
				OperationCodeCommision settlc029 = operationCodeInternationalRepository.findByIdentification("C029").get();
				OperationCodeCommision settlc030 = operationCodeInternationalRepository.findByIdentification("C030").get();
				
				for (ActionCard action : actions) {
					Optional<Card> card = cardRepository.findByCardCode(action.getCardCode());
					if (card.isPresent()) {
						Card foundedCard = card.get();
						foundedCard = writeInFile(writer, foundedCard, action, "");
						action.setStatus(0);
						foundedCard.setCardActionsStatus("6");
						foundedCard.setLastEmbossingSummaryFile(summary.getFileName());
						foundedCard.setModifDate(new Date());
						actionsToSave.add(action);
						cardsToSave.add(foundedCard);

						CardHistory cardHistory = new CardHistory();
						cardHistory.setCardCode(foundedCard.getCardCode());
						if (action.getReeditionPinReason().equals("001"))
							cardHistory
									.setOperation("Carte (réedition PIN : Code oublié/perdu) envoyé à A2C (fichier: "
											+ summary.getFileName() + ")");
						else
							cardHistory.setOperation("Carte (réedition PIN : Code illisible) envoyé à A2C (fichier: "
									+ summary.getFileName() + ")");
						cardHistory.setOperation_date(new Date());
						cardHistory.setEditedBy(user.get().getUserName());
						cardHistories.add(cardHistory);

						// if motif 001 => lost or forgotten => apply fees
						if (action.getReeditionPinReason().equals("001")) {
							Optional<Product> product = productRepositpry
									.findByProductCode(foundedCard.getProductCode());
							if (product.get().getCpCommissionPin()>0) {
								

							
							Optional<Account> account = accountRepository.findByAccountCode(foundedCard.getAccCode());


							DayOperationFeesCardSequence seq = dayOperationCardSequenceService.getSequence();
							DayOperationFeesCardSequence seqPieceComptable = dayOperationCardSequenceService
									.getSequencePieceComptable();

							int settlementAmountHt = Math.round(product.get().getCpCommissionPin() / tva);
							logger.info("settlementAmountHt is => {}", settlementAmountHt);

							int settlementAmountTva = product.get().getCpCommissionPin() - settlementAmountHt;
							logger.info("settlementAmountTva is => {}", settlementAmountTva);

							int transactionAmount = product.get().getCpCommissionPin();
							logger.info("transactionAmount is => {}", transactionAmount);

							String montantTrans = String.format("%014d", transactionAmount);
							montantTrans = montantTrans.substring(0, 12) + "." + montantTrans.substring(12);

								DayOperationCardFransaBank C029 = new DayOperationCardFransaBank();

								C029.setCodeAgence(foundedCard.getAgencyCode());
								C029.setCodeBankAcquereur("035");
								C029.setCodeBank("035");
								C029.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
								C029.setDateTransaction(transactionDate);
								C029.setHeureTransaction(transactionTime);
								C029.setCompteCredit(settlc029.getCreditAccount());
								C029.setNumCartePorteur(foundedCard.getCardNum());
								C029.setNumtransaction(String.format("%012d", seq.getSequence()));
								C029.setIdenfication(settlc029.getIdentification());
								C029.setMontantSettlement(settlementAmountHt);
								C029.setMontantTransaction(montantTrans);
								C029.setLibelleCommercant("REEDITION MB00432");

								C029.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
								C029.setRefernceLettrage(
										datePieceComptableFormat.format(date) + foundedCard.getCardNum());
								C029.setFileDate(filedateFormat.format(date));
								C029.setNumAutorisation(C029.getNumtransaction());
								C029.setNumRefTransaction(C029.getNumtransaction());

								C029.setCardProduct("visa");

								// seq=dayOperationCardSequenceService.incrementSequence(seq);

								DayOperationCardFransaBank C030 = new DayOperationCardFransaBank();

								C030.setCodeAgence(foundedCard.getAgencyCode());
								C030.setCodeBankAcquereur("035");
								C030.setCodeBank("035");
								C030.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
								C030.setDateTransaction(transactionDate);
								C030.setHeureTransaction(transactionTime);
								C030.setCompteCredit(settlc030.getCreditAccount());
								C030.setNumCartePorteur(foundedCard.getCardNum());
								C030.setNumtransaction(String.format("%012d", seq.getSequence()));
								C030.setIdenfication(settlc030.getIdentification());
								C030.setMontantSettlement(settlementAmountTva);
								C030.setMontantTransaction(montantTrans);
								C030.setLibelleCommercant("REEDITION MB00432");

								C030.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
								C030.setRefernceLettrage(
										datePieceComptableFormat.format(date) + foundedCard.getCardNum());
								C030.setFileDate(filedateFormat.format(date));
								C030.setNumAutorisation(C030.getNumtransaction());
								C030.setNumRefTransaction(C030.getNumtransaction());

								C030.setCardProduct("visa");

								dayOperations.add(C029);
								dayOperations.add(C030);

						

							seq = dayOperationCardSequenceService.incrementSequence(seq);
							seqPieceComptable = dayOperationCardSequenceService
									.incrementSequencePieceComptable(seqPieceComptable);
						}
						}

					}
				}

				if (cardsToSave.size() > 0) {
					summary.setCardsNumber(cardsToSave.size());

					embossingSummaryRepository.save(summary);

					cardRepository.saveAll(cardsToSave);
					actionCardRepository.saveAll(actionsToSave);

					cardHistoryRepository.saveAll(cardHistories);
					if (dayOperations.size() > 0)
					operationRepo.saveAll(dayOperations);
					batchPorteurRepository.updateFileName("EI_A2C", summary.getFileName());

				}
			}

			writer.close();
			InputStream in = new FileInputStream(file);

			batchPorteurRepository.updateFinishBatch("EI_A2C", 1, new Date());
			occupiedEi_A2C=false;
			logger.info("*********end generateFileEmbossingVisaRecalculPin**********");

			return IOUtils.toByteArray(in);
		}else{
			logger.info("*********occupied generateFileEmbossingVisaRecalculPin**********");
			return null;
		 }

	}

	@PostMapping(value = "generateFileEmbossingRenewel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
	public byte[] generateFileEmbossingRenewel() throws Exception {
		//try {
			logger.info("*********begin generateFileEmbossingRenewel*********");
		if (!occupiedEr) {
			occupiedEr=true;
			String username = SecurityContextHolder.getContext().getAuthentication().getName();
			Optional<User> user = userRepository.findByUserNameOrUserEmail(username, username);
			List<Card> cardsToSave = new ArrayList<Card>();
			List<ActionCard> actionsToSave = new ArrayList<ActionCard>();
			List<CardHistory> cardHistories = new ArrayList<CardHistory>();
			File file = new File(propertyService.getFilePath());
			List<StockBin> stocks = new ArrayList<StockBin>();
			PrintWriter writer = new PrintWriter(file, "UTF-8");
			List<ActionCard> actions = actionCardRepository.findAllNationalRenewelActions();

			List<Card> cards = cardRepository.getNationalCardsWithPreDate();
			Date currentDay = new Date();
			if (actions.size() > 0 || cards.size() > 0) {
				EmbossingSummary summary = buildSummary("ER");

				for (ActionCard action : actions) {
					Optional<Card> card = cardRepository.findByCardCode(action.getCardCode());
					if (card.isPresent()) {
						Card foundedCard = card.get();
						foundedCard = writeInFile(writer, foundedCard, action, "");
						action.setStatus(0);
						foundedCard.setCardActionsStatus("6");
						foundedCard.setLastEmbossingSummaryFile(summary.getFileName());
						foundedCard.setModifDate(new Date());
						actionsToSave.add(action);
						cardsToSave.add(foundedCard);

						CardHistory cardHistory = new CardHistory();
						cardHistory.setCardCode(foundedCard.getCardCode());
						cardHistory.setOperation(
								"Carte (renouvellement) envoyé à SATIM (fichier: " + summary.getFileName() + ")");
						cardHistory.setOperation_date(new Date());
						cardHistory.setEditedBy(user.get().getUserName());
						cardHistories.add(cardHistory);

					}

				}

				for (Card card : cards) {

					if (dateService.getDatePart(currentDay) > dateService.getDatePart(card.getPreDate())) {

						if (dateService.getDatePartExpRenewel(currentDay) <= dateService
								.getDatePartExpRenewel(card.getExpiryDate())) {
							Optional<Program> foundedProgram = programRepository
									.findByProgramCode(card.getProgrameId());
							if (foundedProgram.isPresent()) {
								
								StockBin stockbin = stockBinRepository
										.findBybinOnUsCode(foundedProgram.get().getBinOnUsCode());
								if (stockbin.getStockSatim() > 0) {

									stockbin.setStockReserve(stockbin.getStockReserve() + 1);
									stockbin.setStockSatim(stockbin.getStockSatim() - 1);

									stocks.add(stockbin);

								} else {
									throw new Exception(
											"Stock insuffisant pour la création d'une demande de remplacement (ancienne carte)");
								}
								
								if (card.getRandomPin() != null) {

									card = writeInFile(writer, card, null, "renewel");
									card.setCardActionsStatus("6");
									card.setLastEmbossingSummaryFile(summary.getFileName());
									card.setModifDate(new Date());
									cardsToSave.add(card);

									CardHistory cardHistory = new CardHistory();
									cardHistory.setCardCode(card.getCardCode());
									cardHistory.setOperation("Carte (renouvellement) envoyé à SATIM (fichier: "
											+ summary.getFileName() + ")");
									cardHistory.setOperation_date(new Date());
									cardHistory.setEditedBy(user.get().getUserName());
									cardHistories.add(cardHistory);	

								} else {
									logger.info("old card to replace => {}", card.getClearCardNum());

//									Optional<Program> foundedProgram = programRepository
//											.findByProgramCode(card.getProgrameId());
									
										ActionCard actionCard = new ActionCard();
										actionCard.setCardCode(card.getCardCode());
										actionCard.setIsRenewel(0);
										actionCard.setIsReplacement(1);
										actionCard.setIsPinChange(0);
										actionCard.setNameInCard("");
										actionCard.setRemplacementWithPin(true);
										actionCard.setReplacementOldCard("YES");
										actionCard.setRemplacementReason("006");
										actionCard.setStatus(1);

										LocalDate localDate = new Date().toInstant().atZone(ZoneId.systemDefault())
												.toLocalDate();
										LocalDate newLocalDate = localDate
												.plusYears(foundedProgram.get().getCprLifeCycle());
										actionCard.setCardNewExpDate(java.sql.Date.valueOf(newLocalDate));
										actionsToSave.add(actionCard);

										CardHistory cardHistory = new CardHistory();
										cardHistory.setCardCode(card.getCardCode());

										cardHistory.setOperation("Creation action de remplacement (Ancienne carte)");
										cardHistory.setOperation_date(new Date());
										cardHistory.setEditedBy(user.get().getUserName());
										cardHistories.add(cardHistory);

									}
							}

						}

					}
				}

				if (cardsToSave.size() > 0 || actionsToSave.size() > 0) {
					logger.info("cardsToSave.size => {}", cardsToSave.size());

					logger.info("actionsToSave.size => {}", actionsToSave.size());
					summary.setCardsNumber(cardsToSave.size());
					embossingSummaryRepository.save(summary);

					cardRepository.saveAll(cardsToSave);

					if (actionsToSave.size() > 0)
						actionCardRepository.saveAll(actionsToSave);

					cardHistoryRepository.saveAll(cardHistories);

					if (stocks.size() > 0)
						stockBinRepository.saveAll(stocks);

					batchPorteurRepository.updateFileName("ER", summary.getFileName());

				}

			}

			writer.close();
			InputStream in = new FileInputStream(file);

			batchPorteurRepository.updateFinishBatch("ER", 1, new Date());
			occupiedEr=false;
			logger.info("*********end generateFileEmbossingRenewel**********");

			return IOUtils.toByteArray(in);
		}else{
			logger.info("*********occupied generateFileEmbossingRenewel**********");
			return null;
		 }


	}
	
	
	@PostMapping(value = "generateFileEmbossingVisaRenewel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
	public byte[] generateFileEmbossingVisaRenewel() throws Exception {
		//try {
			logger.info("*********begin generateFileEmbossingVisaRenewel*********");
		if (!occupiedEr_A2C) {
			occupiedEr_A2C=true;
			String username = SecurityContextHolder.getContext().getAuthentication().getName();
			Optional<User> user = userRepository.findByUserNameOrUserEmail(username, username);
			List<Card> cardsToSave = new ArrayList<Card>();
			List<ActionCard> actionsToSave = new ArrayList<ActionCard>();
			List<CardHistory> cardHistories = new ArrayList<CardHistory>();
			File file = new File(propertyService.getFilePath());
			List<StockBin> stocks = new ArrayList<StockBin>();
			PrintWriter writer = new PrintWriter(file, "UTF-8");
			List<ActionCard> actions = actionCardRepository.findAllInternationalRenewelActions();

			List<Card> cards = cardRepository.getInternationalCardsWithPreDate();
			Date currentDay = new Date();
			if (actions.size() > 0 || cards.size() > 0) {
				EmbossingSummary summary = buildSummary("ER_A2C");

				for (ActionCard action : actions) {
					Optional<Card> card = cardRepository.findByCardCode(action.getCardCode());
					if (card.isPresent()) {
						Card foundedCard = card.get();
						foundedCard = writeInFile(writer, foundedCard, action, "");
						action.setStatus(0);
						foundedCard.setCardActionsStatus("6");
						foundedCard.setLastEmbossingSummaryFile(summary.getFileName());
						foundedCard.setModifDate(new Date());
						actionsToSave.add(action);
						cardsToSave.add(foundedCard);

						CardHistory cardHistory = new CardHistory();
						cardHistory.setCardCode(foundedCard.getCardCode());
						cardHistory.setOperation(
								"Carte (renouvellement) envoyé à A2C (fichier: " + summary.getFileName() + ")");
						cardHistory.setOperation_date(new Date());
						cardHistory.setEditedBy(user.get().getUserName());
						cardHistories.add(cardHistory);

					}

				}

				for (Card card : cards) {

					if (dateService.getDatePart(currentDay) > dateService.getDatePart(card.getPreDate())) {

						if (dateService.getDatePartExpRenewel(currentDay) <= dateService
								.getDatePartExpRenewel(card.getExpiryDate())) {
							Optional<Program> foundedProgram = programRepository
									.findByProgramCode(card.getProgrameId());
							if (foundedProgram.isPresent()) {
								
								StockBin stockbin = stockBinRepository
										.findBybinOnUsCode(foundedProgram.get().getBinOnUsCode());
								if (stockbin.getStockSatim() > 0) {

									stockbin.setStockReserve(stockbin.getStockReserve() + 1);
									stockbin.setStockSatim(stockbin.getStockSatim() - 1);

									stocks.add(stockbin);

								} else {
									throw new Exception(
											"Stock insuffisant");
								}
								
								if (card.getRandomPin() != null) {

									card = writeInFile(writer, card, null, "renewel");
									card.setCardActionsStatus("6");
									card.setLastEmbossingSummaryFile(summary.getFileName());
									card.setModifDate(new Date());
									cardsToSave.add(card);

									CardHistory cardHistory = new CardHistory();
									cardHistory.setCardCode(card.getCardCode());
									cardHistory.setOperation("Carte (renouvellement) envoyé à A2C (fichier: "
											+ summary.getFileName() + ")");
									cardHistory.setOperation_date(new Date());
									cardHistory.setEditedBy(user.get().getUserName());
									cardHistories.add(cardHistory);	

								} 
							}

						}

					}
				}

				if (cardsToSave.size() > 0 || actionsToSave.size() > 0) {
					logger.info("cardsToSave.size => {}", cardsToSave.size());

					logger.info("actionsToSave.size => {}", actionsToSave.size());
					summary.setCardsNumber(cardsToSave.size());
					embossingSummaryRepository.save(summary);

					cardRepository.saveAll(cardsToSave);

					if (actionsToSave.size() > 0)
						actionCardRepository.saveAll(actionsToSave);

					cardHistoryRepository.saveAll(cardHistories);

					if (stocks.size() > 0)
						stockBinRepository.saveAll(stocks);

					batchPorteurRepository.updateFileName("ER_A2C", summary.getFileName());

				}

			}

			writer.close();
			InputStream in = new FileInputStream(file);

			batchPorteurRepository.updateFinishBatch("ER_A2C", 1, new Date());
			occupiedEr_A2C=false;
			logger.info("*********end generateFileEmbossingVisaRenewel**********");

			return IOUtils.toByteArray(in);
		}else{
			logger.info("*********occupied generateFileEmbossingVisaRenewel**********");
			return null;
		 }


	}


	@PostMapping(value = "generateFileEmbossingCreation", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
	public byte[] generateFileEmbossingCreation() throws Exception {
		//try {
			logger.info("*********begin generateFileEmbossingCreation*********");
			if (!occupiedEm) {
				occupiedEm=true;
			String username = SecurityContextHolder.getContext().getAuthentication().getName();
			Optional<User> user = userRepository.findByUserNameOrUserEmail(username, username);
			List<Card> cardsToSave = new ArrayList<Card>();

			List<CardHistory> cardHistories = new ArrayList<CardHistory>();
			File file = new File(propertyService.getFilePath());
			// PrintWriter writer = new PrintWriter(file, StandardCharsets.US_ASCII);

			PrintWriter writer = new PrintWriter(file, "UTF-8");
			List<Card> cards = cardRepository.getNationalCardsForPerso();
			List<DayOperationCardFransaBank> dayOperations = new ArrayList<DayOperationCardFransaBank>();

			if (cards.size() > 0) {
				EmbossingSummary summary = buildSummary("EM");
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat filedateFormat = new SimpleDateFormat("yyMMdd");
				SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
				SimpleDateFormat datePieceComptableFormat = new SimpleDateFormat("ddMMyy");

				Date date = new Date();

				String transactionDate = dateFormat.format(date);
				String transactionTime = timeFormat.format(date);

				TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
				float tvaBase = Float.parseFloat(tvaCommissionFransaBank.getTva()) / 100;

				float tva = tvaBase + 1;
				logger.info("TVA is => {}", tva);

				SettelementFransaBank settlc011 = settlementRepo.findByIdentificationbh("C011");
				SettelementFransaBank settlc012 = settlementRepo.findByIdentificationbh("C012");

				SettelementFransaBank settlc019 = settlementRepo.findByIdentificationbh("C019");
				SettelementFransaBank settlc020 = settlementRepo.findByIdentificationbh("C020");

				for (Card card : cards) {
					card = writeInFile(writer, card, null, "creation");

					CardHistory cardHistory = new CardHistory();
					cardHistory.setCardCode(card.getCardCode());
					cardHistory.setOperation("Carte envoyée à SATIM (fichier: " + summary.getFileName() + ")");
					cardHistory.setOperation_date(new Date());
					cardHistory.setEditedBy(user.get().getUserName());
					cardHistories.add(cardHistory);
					Optional<Product> product = productRepositpry.findByProductCode(card.getProductCode());
					int commission = 0;

					if (product.get().getCpTypeCommissionAnniversary().equals("ANNUAL"))
						commission = product.get().getCpCommissionCreation();
					else
						commission = product.get().getCpCommissionAnniversaryAndCreation();

					if (commission>0) {
					

					Optional<Account> account = accountRepository.findByAccountCode(card.getAccCode());
					DayOperationFeesCardSequence seq = dayOperationCardSequenceService.getSequence();
					DayOperationFeesCardSequence seqPieceComptable = dayOperationCardSequenceService
							.getSequencePieceComptable();

					
					int settlementAmountHt = Math.round(commission / tva);
					logger.info("settlementAmountHt is => {}", settlementAmountHt);

					int settlementAmountTva = commission - settlementAmountHt;
					logger.info("settlementAmountTva is => {}", settlementAmountTva);

					int transactionAmount = commission;
					logger.info("transactionAmount is => {}", transactionAmount);

					String montantTrans = String.format("%014d", transactionAmount);
					montantTrans = montantTrans.substring(0, 12) + "." + montantTrans.substring(12);
					if (product.get().getLibelle().toLowerCase().contains("gold")) {

						DayOperationCardFransaBank C011 = new DayOperationCardFransaBank();

						C011.setCodeAgence(card.getAgencyCode());
						C011.setCodeBankAcquereur("035");
						C011.setCodeBank("035");
						C011.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
						C011.setDateTransaction(transactionDate);
						C011.setHeureTransaction(transactionTime);
						C011.setCompteCredit(settlc011.getCreditAccount());
						C011.setNumCartePorteur(card.getCardNum());
						C011.setNumtransaction(String.format("%012d", seq.getSequence()));
						C011.setIdenfication(settlc011.getIdentificationbh());
						C011.setMontantSettlement(settlementAmountHt);
						C011.setMontantTransaction(montantTrans);
						C011.setLibelleCommercant("FRAIS FAB CARTE RET/PAIEMENT");

						C011.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
						C011.setRefernceLettrage(datePieceComptableFormat.format(date) + card.getCardNum());
						C011.setFileDate(filedateFormat.format(date));
						C011.setNumAutorisation(C011.getNumtransaction());
						C011.setNumRefTransaction(C011.getNumtransaction());

						// seq=dayOperationCardSequenceService.incrementSequence(seq);

						DayOperationCardFransaBank C012 = new DayOperationCardFransaBank();

						C012.setCodeAgence(card.getAgencyCode());
						C012.setCodeBankAcquereur("035");
						C012.setCodeBank("035");
						C012.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
						C012.setDateTransaction(transactionDate);
						C012.setHeureTransaction(transactionTime);
						C012.setCompteCredit(settlc012.getCreditAccount());
						C012.setNumCartePorteur(card.getCardNum());
						C012.setNumtransaction(String.format("%012d", seq.getSequence()));
						C012.setIdenfication(settlc012.getIdentificationbh());
						C012.setMontantSettlement(settlementAmountTva);
						C012.setMontantTransaction(montantTrans);
						C012.setLibelleCommercant("FRAIS FAB CARTE RET/PAIEMENT");

						C012.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
						C012.setRefernceLettrage(datePieceComptableFormat.format(date) + card.getCardNum());
						C012.setFileDate(filedateFormat.format(date));
						C012.setNumAutorisation(C012.getNumtransaction());
						C012.setNumRefTransaction(C012.getNumtransaction());

						C011.setCardProduct("gold");
						C012.setCardProduct("gold");

						dayOperations.add(C011);
						dayOperations.add(C012);

					} else {
						DayOperationCardFransaBank C019 = new DayOperationCardFransaBank();

						C019.setCodeAgence(card.getAgencyCode());
						C019.setCodeBankAcquereur("035");
						C019.setCodeBank("035");
						C019.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
						C019.setDateTransaction(transactionDate);
						C019.setHeureTransaction(transactionTime);
						C019.setCompteCredit(settlc019.getCreditAccount());
						C019.setNumCartePorteur(card.getCardNum());
						C019.setNumtransaction(String.format("%012d", seq.getSequence()));
						C019.setIdenfication(settlc019.getIdentificationbh());
						C019.setMontantSettlement(settlementAmountHt);
						C019.setMontantTransaction(montantTrans);
						C019.setLibelleCommercant("FRAIS FAB CARTE RET/PAIEMENT");

						C019.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
						C019.setRefernceLettrage(datePieceComptableFormat.format(date) + card.getCardNum());
						C019.setFileDate(filedateFormat.format(date));
						C019.setNumAutorisation(C019.getNumtransaction());
						C019.setNumRefTransaction(C019.getNumtransaction());
						C019.setCardProduct("classic");

						// seq=dayOperationCardSequenceService.incrementSequence(seq);

						DayOperationCardFransaBank C020 = new DayOperationCardFransaBank();

						C020.setCodeAgence(card.getAgencyCode());
						C020.setCodeBankAcquereur("035");
						C020.setCodeBank("035");
						C020.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
						C020.setDateTransaction(transactionDate);
						C020.setHeureTransaction(transactionTime);
						C020.setCompteCredit(settlc020.getCreditAccount());
						C020.setNumCartePorteur(card.getCardNum());
						C020.setNumtransaction(String.format("%012d", seq.getSequence()));
						C020.setIdenfication(settlc020.getIdentificationbh());
						C020.setMontantSettlement(settlementAmountTva);
						C020.setMontantTransaction(montantTrans);
						C020.setLibelleCommercant("FRAIS FAB CARTE RET/PAIEMENT");

						C020.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
						C020.setRefernceLettrage(datePieceComptableFormat.format(date) + card.getCardNum());
						C020.setFileDate(filedateFormat.format(date));
						C020.setNumAutorisation(C020.getNumtransaction());
						C020.setNumRefTransaction(C020.getNumtransaction());

						C020.setCardProduct("classic");

						dayOperations.add(C019);
						dayOperations.add(C020);
					}

					seq = dayOperationCardSequenceService.incrementSequence(seq);
					seqPieceComptable = dayOperationCardSequenceService
							.incrementSequencePieceComptable(seqPieceComptable);
				}
					card.setCardStatusCode(6);
					card.setLastEmbossingSummaryFile(summary.getFileName());
					card.setModifDate(new Date());
					cardsToSave.add(card);

				}

				if (cardsToSave.size() > 0) {
					summary.setCardsNumber(cardsToSave.size());
					summary = embossingSummaryRepository.save(summary);
					cardRepository.saveAll(cardsToSave);
					if (dayOperations.size() > 0)
						operationRepo.saveAll(dayOperations);
					cardHistoryRepository.saveAll(cardHistories);
					batchPorteurRepository.updateFileName("EM", summary.getFileName());
				}

			}
			writer.close();
			InputStream in = new FileInputStream(file);

			batchPorteurRepository.updateFinishBatch("EM", 1, new Date());
			occupiedEm=false;
			logger.info("*********end generateFileEmbossingCreation*********");

			return IOUtils.toByteArray(in);
	}else{
		logger.info("*********occupied generateFileEmbossingCreation**********");
		return null;
	 }
	}
	
	
	
	@PostMapping(value = "generateFileEmbossingVisaCreation", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
	public byte[] generateFileEmbossingVisaCreation() throws Exception {
		//try {
			logger.info("*********begin generateFileEmbossingVisaCreation*********");
			if (!occupiedEm_A2C) {
				occupiedEm_A2C=true;
			String username = SecurityContextHolder.getContext().getAuthentication().getName();
			Optional<User> user = userRepository.findByUserNameOrUserEmail(username, username);
			List<Card> cardsToSave = new ArrayList<Card>();

			List<CardHistory> cardHistories = new ArrayList<CardHistory>();
			File file = new File(propertyService.getFilePath());
			// PrintWriter writer = new PrintWriter(file, StandardCharsets.US_ASCII);

			PrintWriter writer = new PrintWriter(file, "UTF-8");
			List<Card> cards = cardRepository.getInternationalCardsForPerso();
			List<DayOperationCardFransaBank> dayOperations = new ArrayList<DayOperationCardFransaBank>();

			if (cards.size() > 0) {
				EmbossingSummary summary = buildSummary("EM_A2C");
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat filedateFormat = new SimpleDateFormat("yyMMdd");
				SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
				SimpleDateFormat datePieceComptableFormat = new SimpleDateFormat("ddMMyy");

				Date date = new Date();

				String transactionDate = dateFormat.format(date);
				String transactionTime = timeFormat.format(date);

				TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
				float tvaBase = Float.parseFloat(tvaCommissionFransaBank.getTva()) / 100;

				float tva = tvaBase + 1;
				logger.info("TVA is => {}", tva);

				
				OperationCodeCommision settlc021 = operationCodeInternationalRepository.findByIdentification("C021").get();
				OperationCodeCommision settlc022 = operationCodeInternationalRepository.findByIdentification("C022").get();

				for (Card card : cards) {
					card = writeInFile(writer, card, null, "creation");

					CardHistory cardHistory = new CardHistory();
					cardHistory.setCardCode(card.getCardCode());
					cardHistory.setOperation("Carte envoyée à A2C (fichier: " + summary.getFileName() + ")");
					cardHistory.setOperation_date(new Date());
					cardHistory.setEditedBy(user.get().getUserName());
					cardHistories.add(cardHistory);
					Optional<Product> product = productRepositpry.findByProductCode(card.getProductCode());
					int commission = 0;

					if (product.get().getCpTypeCommissionAnniversary().equals("ANNUAL"))
						commission = product.get().getCpCommissionCreation();
					else
						commission = product.get().getCpCommissionAnniversaryAndCreation();

					if (commission>0) {
					

						Optional<Account> accountInt = accountRepository.findByAccountCode(card.getAccCode());
						if (accountInt.isPresent()) {
							Account account = accountRepository.findByAccountNum(accountInt.get().getAccountNumAttached());
	
							DayOperationFeesCardSequence seq = dayOperationCardSequenceService.getSequence();
							DayOperationFeesCardSequence seqPieceComptable = dayOperationCardSequenceService
									.getSequencePieceComptable();
	
							
							int settlementAmountHt = Math.round(commission / tva);
							logger.info("settlementAmountHt is => {}", settlementAmountHt);
	
							int settlementAmountTva = commission - settlementAmountHt;
							logger.info("settlementAmountTva is => {}", settlementAmountTva);
	
							int transactionAmount = commission;
							logger.info("transactionAmount is => {}", transactionAmount);
	
							String montantTrans = String.format("%014d", transactionAmount);
							montantTrans = montantTrans.substring(0, 12) + "." + montantTrans.substring(12);
	
								DayOperationCardFransaBank C021 = new DayOperationCardFransaBank();
	
								C021.setCodeAgence(card.getAgencyCode());
								C021.setCodeBankAcquereur("035");
								C021.setCodeBank("035");
								C021.setCompteDebit("0" + account.getAccountNum());
								C021.setDateTransaction(transactionDate);
								C021.setHeureTransaction(transactionTime);
								C021.setCompteCredit(settlc021.getCreditAccount());
								C021.setNumCartePorteur(card.getCardNum());
								C021.setNumtransaction(String.format("%012d", seq.getSequence()));
								C021.setIdenfication(settlc021.getIdentification());
								C021.setMontantSettlement(settlementAmountHt);
								C021.setMontantTransaction(montantTrans);
								C021.setLibelleCommercant("FRAIS FAB CARTE RET/PAIEMENT");
	
								C021.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
								C021.setRefernceLettrage(datePieceComptableFormat.format(date) + card.getCardNum());
								C021.setFileDate(filedateFormat.format(date));
								C021.setNumAutorisation(C021.getNumtransaction());
								C021.setNumRefTransaction(C021.getNumtransaction());
	
								// seq=dayOperationCardSequenceService.incrementSequence(seq);
	
								DayOperationCardFransaBank C022 = new DayOperationCardFransaBank();
	
								C022.setCodeAgence(card.getAgencyCode());
								C022.setCodeBankAcquereur("035");
								C022.setCodeBank("035");
								C022.setCompteDebit("0" + account.getAccountNum());
								C022.setDateTransaction(transactionDate);
								C022.setHeureTransaction(transactionTime);
								C022.setCompteCredit(settlc022.getCreditAccount());
								C022.setNumCartePorteur(card.getCardNum());
								C022.setNumtransaction(String.format("%012d", seq.getSequence()));
								C022.setIdenfication(settlc022.getIdentification());
								C022.setMontantSettlement(settlementAmountTva);
								C022.setMontantTransaction(montantTrans);
								C022.setLibelleCommercant("FRAIS FAB CARTE RET/PAIEMENT");
	
								C022.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
								C022.setRefernceLettrage(datePieceComptableFormat.format(date) + card.getCardNum());
								C022.setFileDate(filedateFormat.format(date));
								C022.setNumAutorisation(C022.getNumtransaction());
								C022.setNumRefTransaction(C022.getNumtransaction());
	
								C021.setCardProduct("visa");
								C022.setCardProduct("visa");
	
								dayOperations.add(C021);
								dayOperations.add(C022);

							seq = dayOperationCardSequenceService.incrementSequence(seq);
							seqPieceComptable = dayOperationCardSequenceService
									.incrementSequencePieceComptable(seqPieceComptable);
						}
				

				}
					card.setCardStatusCode(6);
					card.setLastEmbossingSummaryFile(summary.getFileName());
					card.setModifDate(new Date());
					cardsToSave.add(card);

				}

				if (cardsToSave.size() > 0) {
					summary.setCardsNumber(cardsToSave.size());
					summary = embossingSummaryRepository.save(summary);
					cardRepository.saveAll(cardsToSave);
					if (dayOperations.size() > 0)
						operationRepo.saveAll(dayOperations);
					cardHistoryRepository.saveAll(cardHistories);
					batchPorteurRepository.updateFileName("EM_A2C", summary.getFileName());
				}

			}
			writer.close();
			InputStream in = new FileInputStream(file);

			batchPorteurRepository.updateFinishBatch("EM_A2C", 1, new Date());
			occupiedEm_A2C=false;
			logger.info("*********end generateFileEmbossingVisaCreation*********");

			return IOUtils.toByteArray(in);
	}else{
		logger.info("*********occupied generateFileEmbossingVisaCreation**********");
		return null;
	 }
	}

	@PostMapping(value = "generateFileBe", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
	public byte[] generateFileBe() throws Exception {
//		try {
			logger.info("*********begin generateFile BE*********");
			if (!occupiedBe) {
				occupiedBe=true;
			List<EpaymentInfo> infosToSave = new ArrayList<EpaymentInfo>();
			List<CardHistory> cardHistories = new ArrayList<CardHistory>();

			List<EpaymentInfo> epaymentInfos = epaymentInfoRepository.getInfoToProcess();
			File file = new File(propertyService.getFileBePath());

			PrintWriter writer = new PrintWriter(file, "UTF-8");
			if (epaymentInfos.size() > 0) {
				BESummary summary = buildSummaryBe();
				String username = SecurityContextHolder.getContext().getAuthentication().getName();
				Optional<User> user = userRepository.findByUserNameOrUserEmail(username, username);

				for (EpaymentInfo info : epaymentInfos) {
					Optional<Card> card = cardRepository.findByCardCode(info.getCardCode());

					if (card.isPresent()) {
						Optional<Account> account = accountRepository.findByAccountCode(card.get().getAccCode());

						if (account.isPresent()) {
							Optional<Customer> customer = customerRepository
									.findByCustomerCode(Integer.parseInt(account.get().getCustomerCode()));

							if (customer.isPresent()) {

								writeInFileBe(writer, card.get(), account.get(), customer.get(), info);
								info.setStatusFile(0);
								info.setLastFileBE(summary.getFileName());

								infosToSave.add(info);

								CardHistory cardHistory = new CardHistory();
								cardHistory.setCardCode(info.getCardCode());
								cardHistory.setOperation("Fichier " + summary.getFileName() + " envoyé à SATIM");
								cardHistory.setOperation_date(new Date());
								cardHistory.setEditedBy(user.get().getUserName());
								cardHistories.add(cardHistory);

							}

						}

					}

				}
				if (infosToSave.size() > 0) {
					summary.setCardsNumber(infosToSave.size());
					epaymentInfoRepository.saveAll(infosToSave);
					bESummaryRepository.save(summary);
					cardHistoryRepository.saveAll(cardHistories);

					batchPorteurRepository.updateFileName("BE", summary.getFileName());

				}

			}

			writer.close();

			InputStream in = new FileInputStream(file);

			batchPorteurRepository.updateFinishBatch("BE", 1, new Date());
			occupiedBe=false;
			logger.info("*********end generateFile BE**********");

			return IOUtils.toByteArray(in);
		}else{
				logger.info("*********occupied generateFile BE**********");
				return null;
		}


	}

	@PostMapping("readFilePorteur")
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
	public void readFilePorteur(@RequestParam("file") List<MultipartFile> multipartFile) throws IOException {
		logger.info("*********begin reading file EPAN*********");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(username, username);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving risk !");
		}

//		BatchPorteur batch = batchPorteurRepository.findByKeys("readPorteur").get();
//
//		batch.setBatchStatus(0);
//		batch.setBatchDate(new Date());
//		batch.setError(null);
//		batch.setErrorStackTrace(null);
//		batchPorteurRepository.save(batch);

		List<Card> listForSave = new ArrayList<Card>();
		List<StockBin> listStockBinForSave = new ArrayList<StockBin>();
		List<CardHistory> listHistoryForSave = new ArrayList<CardHistory>();
		List<EpanSummary> summaries = new ArrayList<EpanSummary>();
		boolean test=false;
		List<UnpersonalizedCard> listUnpersonalizedCardForSave = new ArrayList<UnpersonalizedCard>();
//		try {
			for (MultipartFile mf : multipartFile) {
				List<String> listCard = new ArrayList<String>();
				String name = mf.getOriginalFilename();
				logger.info("file name   => {}", name);
				batchPorteurRepository.updateFileName("readPorteur", name);
				
				Optional<EpanSummary> oldFile =epanSummaryRepository.getByFileName(name);
				if (oldFile.isPresent()) {
					logger.info("file already integrated name");
					batchPorteurRepository.updateStatusAndErrorBatch("readPorteur", 2,
							"Le fichier "+name+" a déjà été intégré", new Date(),
							"Le fichier "+name+" a déjà été intégré");	
					test=true;
					break;

				}
					
					
		

				String[] content = new String(mf.getBytes(), StandardCharsets.ISO_8859_1)
						.split(System.getProperty("line.separator"));
				for (String line : content) {
					listCard.add(line);
				}
				Date date = new Date();
				EpanSummary summary = new EpanSummary();
				summary.setFileName(name);
				summary.setInsertDateTime(date);
				summary.setCardsNumber(listCard.size());
				summary.setInsertDate(dateFormat.format(date));
				summaries.add(summary);
//				List<String> listCard = new ArrayList<String>();
//
//				listCard = fileContent.stream().filter(line -> line.startsWith("PO")).map(String::toUpperCase)
//						.collect(Collectors.toList());

				listCard.forEach(e -> {
					String cardNum = e.substring(83, 99).trim();
					// if (e.substring(106, 109).equals("001")) {

					Optional<Card> card = cardRepository.findByCardNum(cardNum);
					if (card.isPresent()) {

						logger.info("card num  {}  will be updated", cardNum);
						Optional<Program> foundedProgram = programReposiroty
								.findByProgramCode(card.get().getProgrameId());
						if (foundedProgram.isPresent()) {

							StockBin stockbin = stockBinRepository
									.findBybinOnUsCode(foundedProgram.get().getBinOnUsCode());
							stockbin.setStockReserve(stockbin.getStockReserve() - 1);

							stockbin.setStockConsome(stockbin.getStockConsome() + 1);
							listStockBinForSave.add(stockbin);
						}

						// if (card.get().getCardStatusCode()==2 &&
						// card.get().getCardActionsStatus().equals("6") )
						if (card.get().getCardActionsStatus() != null) {
							if (card.get().getCardActionsStatus().equals("6"))
								card.get().setCardActionsStatus("7");

						} else if (card.get().getCardStatusCode() == 6)
							card.get().setCardStatusCode(7);

						card.get().setModifDate(new Date());

						CardHistory cardHistory = new CardHistory();
						cardHistory.setCardCode(card.get().getCardCode());
						cardHistory.setOperation("Carte reçue au BO (fichier: " + name + ")");
						cardHistory.setOperation_date(new Date());
						cardHistory.setEditedBy(user.get().getUserName());
						listHistoryForSave.add(cardHistory);

						listForSave.add(card.get());
					}

//					}else {
//						
//						UnpersonalizedCard unpersonalizedCard= new UnpersonalizedCard();
//						unpersonalizedCard.setCardNumber(cardNum);
//						try {
//							unpersonalizedCard.setFileDate(new SimpleDateFormat("aammjj").parse(name.substring(4, 10)));
//						} catch (ParseException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
//						listUnpersonalizedCardForSave.add(unpersonalizedCard);
//					}

				});

			}
			if (!test) {
				
				epanSummaryRepository.saveAll(summaries);
				cardRepository.saveAll(listForSave);
				stockBinRepository.saveAll(listStockBinForSave);
				cardHistoryRepository.saveAll(listHistoryForSave);
				unpersonalizedCardRepository.saveAll(listUnpersonalizedCardForSave);
				batchPorteurRepository.updateFinishBatch("readPorteur", 1, new Date());
			}
		
			logger.info("*********end reading file EPAN*********");

	}
	
	
	@GetMapping("/updateBatchStatus/{key}")
	public void updateBatchStatus( @PathVariable(value = "key") String key) {
		
		BatchPorteur batch = batchPorteurRepository.findByKeys(key).get();

		batch.setBatchStatus(0);
		batch.setBatchDate(new Date());
		batch.setError(null);
		batch.setErrorStackTrace(null);
		batchPorteurRepository.save(batch);
		
	}
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@PostMapping(value = "generateFileACS", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public byte[] generateFileACS() throws Exception {
		logger.info("*********begin generateFile ACS*********");

		if (!occupiedAcs) {
			occupiedAcs = true;
			List<CardHolderInfo> cardHolderInfosToSave = new ArrayList<>();
			List<CardHolderInfo> cardHolderInfos = cardHolderInfoRepository.findAllCardHolderInfoByStatu(0);

			if (cardHolderInfos.size() > 0) {
				AcsSummary summary = buildSummaryAcs();

				File file = new File(propertyService.getPorteurFilesPath() +
						 summary.getFileName() + ".csv");
				PrintWriter writer = new PrintWriter(file, "UTF-8");
				String Entete = "language;firstName;lastName;pan;expiry;phone;behaviour;action";
				writer.println(Entete);
				for (CardHolderInfo item : cardHolderInfos) {

					//CardHolderInfo cardHolderIn = cardHolderInfoRepository.findById(item.getCardHolderInfoCode()).get();
					String body = "fr;" + item.getFirstName() + ";" + item.getLastName() + ";" + item.getPan() + ";"
							+ item.getExpiry() + ";" + item.getPhoneNumber() + ";otp;" + item.getAction();
					writer.println(body);
					item.setStatusGeneration(1);
					cardHolderInfosToSave.add(item);

				}
				if (cardHolderInfos.size()>0) {
					summary.setCardsNumber(cardHolderInfos.size());

					cardHolderInfoRepository.saveAll(cardHolderInfosToSave);
					acsSummaryRepository.save(summary);
					batchPorteurRepository.updateFileName("ACS", summary.getFileName());

				}

				writer.close();
				InputStream in = new FileInputStream(file);
				batchPorteurRepository.updateFinishBatch("ACS", 1, new Date());

				occupiedAcs = false;
				logger.info("*********end generateFile ACS**********");

				return IOUtils.toByteArray(in);
			}
			occupiedAcs = false;
			batchPorteurRepository.updateFinishBatch("ACS", 1, new Date());

			return null;

		} else {
			logger.info("*********occupied generating file ACS**********");
			return null;
		}

	}


}
