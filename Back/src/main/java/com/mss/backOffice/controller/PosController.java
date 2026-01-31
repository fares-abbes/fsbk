package com.mss.backOffice.controller;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.backOffice.request.*;
import com.mss.backOffice.services.AccountService;
import com.mss.backOffice.services.ApiPos;
import com.mss.backOffice.services.DayOperationPosSequenceService;
import com.mss.backOffice.services.DeletedMerchantService;
import com.mss.backOffice.services.GetTokenAmplitudeService;
import com.mss.backOffice.services.MyEmailService;
import com.mss.backOffice.services.PropertyService;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.mss.unified.entities.*;
import com.mss.unified.repositories.*;

import java.io.File;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.TextPosition;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.SystemOutLogger;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.jline.terminal.Terminal;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("POS")
public class PosController {
	@Autowired
	EligiblePosRepository eligiblePosRepository;
	@Autowired
	HistoriqueCommercantRepository historiqueCommercantRepository;
	@Autowired
	SequentielSerieBMRepository sequentielSerieBMRepository;
	@Autowired
	SequentielSerieTSRepository sequentielSerieTSRepository;
	@Autowired
	DayOperationPosFransaBankRepository operationRepo;
	@Autowired
	DayOperationPosSequenceService dayOperationPosSequenceService;
	@Autowired
	TvaCommissionFransaBankRepository tvaRepo;
	@Autowired
	SettelementFransaBankRepository settlementRepo;
	@Autowired
	MontantLoyerRepository montantLoyerRepository;
	@Autowired
	SequentielSerieTMRepository sequentielSerieTMRepository;
	@Autowired
	HistoriqueRequestPosRepository historiqueRequestPosRepository;
	@Autowired
	SequentielSerieBSRepository sequentielSerieBSRepository;
	@Autowired
	SequentielSerieRepository sequentielSerieRepository;
	@Autowired
	CurrencyFSBKRepository currencyFSBKRepository;
	@Autowired
	ApiPos apiPos;
	@Autowired
	GetTokenAmplitudeService getTokenAmplitudeService;
	@Autowired
	DeletedMerchantService deletedMerchantService;
	@Autowired
	MerchantStatusRepository merchantStatusRepository;
	@Autowired
	MyEmailService myEmailService;
	@Autowired
	ZoneRepository zoneRepository;
//  @Autowired
//  AgenceRepository agenceRepository;
	@Autowired
	PosStockRepository posStockRepository;
	@Autowired
	PosHistoriqueOfSerialRepository posHistoriqueOfSerialRepository;
	@Autowired
	BatchPosRepository batchPosRepository;

	@Autowired
	TpeRequestRepository tpeRequestRepository;
	@Autowired
	CommissionTpeHistoriqueRepository commissionTpeHistoriqueRepository;
	@Autowired
	PendingTpeRepository pendingTpeRepository;

	@Autowired
	PosTypeRepository posTypeRepository;
	@Autowired
	MccRepository mccRepository;

	@Autowired
	TpeRequestStatusRepository tpeRequestStatusRepository;
	@Autowired

	CommissionFransaBankInternetRepository commissionFransaBankInternetRepository;
	// @Autowired
	// CommissionFransaBankInternetRepository commissionAchatInternetFBRepository;

	@Autowired
	CommissionFransaBankRepository commissionFransaBankRepository;

	@Autowired
	MerchantRepository merchantRepository;
	@Autowired
	PosTerminalRepository posTerminalRepository;

	@Autowired
	ModelTpeRepository modelTpeRepository;
	@Autowired
	PosEtatsRepository posEtatsRepository;
	@Autowired
	AccountRepository accountRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	BankRepository bankRepository;
	@Autowired
	PosAllowedTransactionRepository posAllowedTransactionRepository;
	@Autowired
	PosLimitsRepository posLimitsRepository;
	@Autowired
	PosAllowedTransactionConfRepository posAllowedTransactionConfRepository;
	@Autowired
	PosLimitsConfRepository posLimitsConfRepository;
	@Autowired
	PosServicesConfRepository posServicesConfRepository;
	@Autowired
	PosBinConfRepository posBinConfRepository;
	@Autowired
	PosBinRepository posBinRepository;
	@Autowired
	PosServiceRepository posServiceRepository;
	@Autowired
	CommissionAchatFransaBankRepository commissionAchatFransaBankRepository;
	@Autowired
	CommissionTypeRepository commissionTypeRepository;
	@Autowired
	CommissionTpeRepository commissionTpeRepository;
	@Autowired
	RequestTpeTypeRepository requestTpeTypeRepository;
	@Autowired
	PosModelRepository posModelRepository;
	@Autowired
	PosMarqueRepository posMarqueRepository;
	@Autowired
	FamillePosRepository famillePosRepository;
	@Autowired
	AgenceRepository agenceRepository;
	@Autowired
	PosSerialNumStatesRepository posSerialNumStatesRepository;
	@Autowired
	private AgenceAdministrationRepository agenceAdministrationRepository;
	@Autowired
	CommissionRequestHistoriqueRepository commissionRequestHistoriqueRepository;
//    @Autowired
//    ReclamationRepository reclamationRepository;
//    @Autowired
//    ReclamationTypeRepository reclamationTypeRepository;
	private static final Gson gson = new Gson();
	private static final Logger logger = LoggerFactory.getLogger(PosController.class);
	private ModelMapper modelMapper = new ModelMapper();
	@Autowired
	private PropertyService propertyService;

//  @PostMapping("addAgence")
//  public ResponseEntity<String> addAgence(@RequestBody Agence agence) {
//
//    logger.info(agence.toString());
//
//    agenceRepository.save(agence);
//    return ResponseEntity.ok().body(gson.toJson("Agence added successfully!"));
//  }

	@PostMapping("addMccList")
	public ResponseEntity<String> addMccList(@RequestBody MccList agence) {

		logger.info(agence.toString());

		mccRepository.save(agence);
		return ResponseEntity.accepted().body(gson.toJson("MccList added successfully!"));
	}

	@PostMapping("addTpeType")
	public ResponseEntity<String> addMccList(@RequestBody PosType agence) {
		logger.info(agence.toString());

		posTypeRepository.save(agence);
		return ResponseEntity.ok().body(gson.toJson("PosType added successfully!"));
	}

	@PostMapping("addStatusRequest")
	public ResponseEntity<String> addStatusRequest(@RequestBody TpeRequestStatus agence) {
		logger.info(agence.toString());

		tpeRequestStatusRepository.save(agence);
		return ResponseEntity.ok().body(gson.toJson("TpeRequestStatus added successfully!"));
	}

	@PostMapping("addModel")
	public ResponseEntity<String> addModel(@RequestBody ModelTpe agence) {
		logger.info(agence.toString());
		modelTpeRepository.save(agence);
		return ResponseEntity.ok().body(gson.toJson("ModelTpe added successfully!"));
	}

	@PostMapping("addRequestTpe")
	@Transactional
	public ResponseEntity<String> addRequestTpe(@RequestBody TpeRequestDisplay tpeRequest)
			throws IOException, DocumentException {
		logger.info("**********begin add request**************");

		logger.info(tpeRequest.toString());
		TpeRequest Request = new TpeRequest();
		// String name =
		// SecurityContextHolder.getContext().getAuthentication().getName();

		// User user = userRepository.findByUserNameOrUserEmail(name, name).get();

		Set<PendingTpe> pendingTpes = tpeRequest.getPendingTpes();

		List<PosStock> ps = new ArrayList<>();
		List<PosSerialNumStates> posSerialNumStatess = new ArrayList<>();
		List<PosSerialNumStates> pSerial = null;

		int counter = 0;
		int x = 0;
		int xm = 0;
		int xf = 0;
		int xp = 0;
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<PosHistoriqueOfSerial> posHistoriqueOfSerial = new ArrayList<PosHistoriqueOfSerial>();
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.out.println(timestamp);
		String Strdate = sdf3.format(timestamp);

		for (PendingTpe p : pendingTpes) {

			if ((p.getFamillePosCode() == 1) || (p.getFamillePosCode() == 4)) {

				p.setStatus(1);
				pendingTpes.add(p);

			} else {
				pSerial = posSerialNumStatesRepository.getOnePosSerialNumStatesByStockByType(p.getType());
				System.out.println("Type  = " + p.getType());
				if (pSerial.size() > 0) {
					PosSerialNumStates posSerialNumStates = null;
					PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();

					List<PendingTpe> list = new ArrayList<PendingTpe>(pendingTpes);
					logger.info("FamillePosCode  = " + p.getFamillePosCode());

					/*
					 * if (p.getType() != 0) { x = x + 1; counter = counter + 1;
					 * System.out.println("x =" + x); }
					 */
					if (p.getType() == 22) {
						xm = xm + 1;
						x = xm;
						System.out.println("xm =" + xm);

					}
					if (p.getType() == 10) {
						xf = xf + 1;
						x = xf;
						System.out.println("xf =" + xf);
					}
					if (p.getType() == 23) {
						xp = xp + 1;
						x = xp;
						System.out.println("xp =" + xp);
					}
					System.out.println("x out =" + x);
					for (int i = 0; i < x; i++) {
						posSerialNumStates = posSerialNumStatesRepository.getOne(pSerial.get(i).getSerialNum());
						posSerialNumStates.setStatus(2);

						System.out.println("posSerialNumStates = " + posSerialNumStates.getSerialNum());
						posSerialNumStatess.add(posSerialNumStates);
						ph.setSerialNum(posSerialNumStates.getSerialNum());
						ph.setDateSaisie(Strdate);
						ph.setStatus(2);
						posHistoriqueOfSerial.add(ph);
					}
					PosModel posModel = posModelRepository.getOne(posSerialNumStates.getModel());
					System.out.println(posModel.getModelCode());
					PosStock posStock = posStockRepository.getPosStockByMode(posModel.getModelCode());
					System.out.println(posStock.getStockReserve());

					posStock.setStockReserve(posStock.getStockReserve() + 1);
					posStock.setStockDisponible(posStock.getStockDisponible() - 1);

					System.out.println("TPE    " + pendingTpes.toString());
					p.setModel(Integer.toString(posModel.getModelCode()));
					p.setStatus(1);
					p.setSerialNum(posSerialNumStates.getSerialNum());
					ps.add(posStock);

					pendingTpes.add(p);

					// }
				} else {
					PosType pT = posTypeRepository.getOne(p.getType());
					return new ResponseEntity<String>(" Stock of POS " + pT.getLibelle() + " < 0",
							HttpStatus.BAD_REQUEST);
				}

			}

		}

		System.out.println("TPE1    " + pendingTpes.size());

		posStockRepository.saveAll(ps);
		posSerialNumStatesRepository.saveAll(posSerialNumStatess);
		posHistoriqueOfSerialRepository.saveAll(posHistoriqueOfSerial);
		pendingTpeRepository.saveAll(pendingTpes);

		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		if (!userRepository.findByUserNameOrUserEmail(name, name).isPresent()) {
			return null;

		}
		User user = userRepository.findByUserNameOrUserEmail(name, name).get();
		Request.setNom(tpeRequest.getNom());
		Request.setPrenom(tpeRequest.getPrenom());
		Request.setCommissionInterNational("");
		Request.setCommissionNational("");
		Request.setAdresse(tpeRequest.getAdresse());
		Request.setAgence(Integer.parseInt(tpeRequest.getAgence()));
		Request.setAgentName(user.getUserName());
		Request.setCity(tpeRequest.getCity());
		Request.setCodeZip(tpeRequest.getCodeZip());
		Request.setUserName(tpeRequest.getUserName());
		Request.setOffshore(tpeRequest.getOffshore());
		Request.setCommissionTypeCode(Integer.parseInt(tpeRequest.getCommissionTypeCode()));
		Request.setCountry(tpeRequest.getCountry());
		Request.setNif(tpeRequest.getNif());
		Request.setRso(tpeRequest.getRso());
		Request.setRc(tpeRequest.getRc());
		Request.setCommune(tpeRequest.getCommune());
		Request.setDaira(tpeRequest.getDaira());
		Request.setEmail(tpeRequest.getEmail());
		Request.setRevenue(tpeRequest.getRevenue());
		Request.setCodeWilaya(tpeRequest.getCodeWilaya());
		Request.setSiteWeb(tpeRequest.getSiteWeb());
		Request.setNomC(tpeRequest.getNomC());
		System.out.println("montant loyer " + tpeRequest.getMontantLoyer());

		Request.setMontantLoyer(tpeRequest.getMontantLoyer());
		Request.setTitleC(tpeRequest.getTitleC());
		if (tpeRequest.getTitleC().equals("employe")) {
			Request.setTypeC("002");
		} else {
			Request.setTypeC("001");
		}

		Request.setSiteWeb(tpeRequest.getSiteWeb());

		AgenceAdministration ag = agenceAdministrationRepository
				.findByIdAgence(Integer.parseInt(tpeRequest.getAgence())).get();
		String rib = "35" + ag.getInitial() + tpeRequest.getAccountNumber();

		Request.setAccountNumber(rib);
		Request.setDateCreation(new Date());
		Request.setDateDecision(tpeRequest.getDateDecision());
		Request.setNombreTPE(tpeRequest.getNombreTPE());
		Request.setPhone(tpeRequest.getPhone());
		System.out.println("devise " + tpeRequest.getDevise());

		String devise;
		if (tpeRequest.getDevise() == null) {
			devise = "208";
		} else {
			devise = tpeRequest.getDevise();
		}

		Request.setDevise(devise);
		Request.setStatus(1);
		Request.setEmailCharge(user.getUserEmail());

		Request.setPendingTpes(tpeRequest.getPendingTpes());
		Request = tpeRequestRepository.save(Request);
		/******************** Request historique **********************/
		HistoriqueRequestPos historiqueRequestPos = new HistoriqueRequestPos();
		historiqueRequestPos.setDateStatu(new Date());
		historiqueRequestPos.setStatut("Demande Créée");
		historiqueRequestPos.setRequestCode(Request.getRequestCode());
		historiqueRequestPos.setOperateur("Chargé Clientèle");
		historiqueRequestPosRepository.save(historiqueRequestPos);
		/******************** Request historique **********************/

		/******************** merchant existe **********************/
		Account a = null;
		Merchant m = null;

		System.out.println("Account number= " + tpeRequest.getAccountNumber());

		a = accountRepository.findByAccountNum(tpeRequest.getAccountNumber());

		List<CommissionTpe> comExiste = null;

		List<CommissionTpe> coms = new ArrayList<>();
		;
		List<CommissionTpeHistorique> mhs = new ArrayList<>();

		if (a != null) {
			m = merchantRepository.findByNumAccount(a.getAccountCode());

			if (m != null) {
				System.out.println("Merchant != null");
				comExiste = commissionTpeRepository.findByMerchantCode(m.getMerchantCode());
				if (comExiste != null) {

					List<CommissionRequestHistorique> cRequest = new ArrayList<>();

					for (CommissionTpe i : comExiste) {
						List<CommissionTpeHistorique> hComRequest = commissionTpeHistoriqueRepository
								.findByRequestCode(i.getRequestCode());
						if (hComRequest.size() != 0) {
							System.out.println("true");

							for (CommissionTpeHistorique item : hComRequest) {
								CommissionRequestHistorique h = new CommissionRequestHistorique();

								h.setCommissionInterNational(item.getCommissionInterNational());
								h.setCommissionNational(item.getCommissionNational());
								h.setCommissionType(item.getCommissionType());
								h.setIdCommission(item.getIdCommission());
								h.setLabel(item.getLabel());
								h.setMerchantCode(item.getMerchantCode());
								h.setMontantRefMax(item.getMontantRefMax());
								h.setMontantRefMin(item.getMontantRefMin());
								h.setOperateurMax(item.getOperateurMax());
								h.setOperateurMin(item.getOperateurMin());

								h.setRequestCode(item.getRequestCode());

								h.setValeurComFixMax(item.getValeurComFixMax());
								h.setValeurComFixMin(item.getValeurComFixMin());
								h.setValeurCommissionInternationaleFixe(item.getValeurCommissionInternationaleFixe());
								h.setValeurCommissionNationaleFix(item.getValeurCommissionNationaleFix());
								h.setValeurComVariableMax(item.getValeurComVariableMax());
								h.setValeurComVariableMin(item.getValeurComVariableMin());
								h.setDateSaisie(item.getDateSaisie());
								cRequest.add(h);
							}
							commissionRequestHistoriqueRepository.saveAll(cRequest);

							commissionTpeHistoriqueRepository
									.deleteCommissionHistoriqueByRequestCode(i.getRequestCode());

						}

						CommissionTpeHistorique h = new CommissionTpeHistorique();
						System.out.println("i.getRequestCode() =" + i.getRequestCode());

						System.out.println("tpeRequest.getRequestCode() =" + Request.getRequestCode());
						System.out.println("false");

						h.setCommissionInterNational(i.getCommissionInterNational());
						h.setCommissionNational(i.getCommissionNational());
						h.setCommissionType(i.getCommissionType());
						h.setIdCommission(i.getIdCommission());
						h.setLabel(i.getLabel());
						h.setMerchantCode(i.getMerchantCode());
						h.setMontantRefMax(i.getMontantRefMax());
						h.setMontantRefMin(i.getMontantRefMin());
						h.setOperateurMax(i.getOperateurMax());
						h.setOperateurMin(i.getOperateurMin());
						h.setRequestCode(i.getRequestCode());
						h.setValeurComFixMax(i.getValeurComFixMax());
						h.setValeurComFixMin(i.getValeurComFixMin());
						h.setValeurCommissionInternationaleFixe(i.getValeurCommissionInternationaleFixe());
						h.setValeurCommissionNationaleFix(i.getValeurCommissionNationaleFix());
						h.setValeurComVariableMax(i.getValeurComVariableMax());
						h.setValeurComVariableMin(i.getValeurComVariableMin());
						h.setDateSaisie(i.getDateSaisie());

						mhs.add(h);

						i.setRequestCode(Request.getRequestCode());
						coms.add(i);

					}
					commissionTpeHistoriqueRepository.saveAll(mhs);
					commissionTpeRepository.saveAll(coms);
					System.out.println("comExiste != null");
					// System.out.println("comExiste" + comExiste.get(0).getLabel());

				}
			}
		}

		if (comExiste == null || comExiste.size() == 0) {
			System.out.println("comExiste = null");

			if (Integer.parseInt(tpeRequest.getCommissionTypeCode()) == 1) {
				System.out.println("if");

				CommissionTpe com = new CommissionTpe();
				com.setCommissionNational("0");
				com.setCommissionInterNational("0");
				com.setMerchantCode(0);
				com.setMontantRefMin("");
				com.setMontantRefMax("");
				com.setRequestCode(Request.getRequestCode());

				// com.setCommissionTpeId(commissionTpeId);
				com.setOperateurMin("");
				com.setOperateurMax("");
				com.setValeurComFixMin("");
				// com.setValeurComFixMax(commissionTpe.getValeurComFixMax());
				com.setValeurComVariableMin("");
				com.setValeurCommissionInternationaleFixe(tpeRequest.getCommissionInterNational());
				com.setValeurCommissionNationaleFix(tpeRequest.getCommissionNational());

				com.setCommissionType(1);
				com.setDateSaisie(Strdate);
				// com.setValeurComVariableMax(commissionTpe.getValeurComVariableMax());

				commissionTpeRepository.save(com);
			}

			if (Integer.parseInt(tpeRequest.getCommissionTypeCode()) == 2) {
				List<CommissionTpeRequest> commissionTpes = tpeRequest.getCommissionTpes();
				System.out.println("else");

				for (CommissionTpeRequest commissionTpe : commissionTpes) {
					System.out.println(commissionTpes.toString());
					CommissionTpe com = new CommissionTpe();
					CommissionTpeId commissionTpeId = new CommissionTpeId();
					com.setCommissionNational("1");
					com.setCommissionInterNational("0");
					com.setMerchantCode(0);
					com.setMontantRefMin(commissionTpe.getMontantRefMin());
					com.setMontantRefMax(commissionTpe.getMontantRefMax());
					com.setRequestCode(Request.getRequestCode());
					System.out.println(commissionTpeId.toString());

					// com.setCommissionTpeId(commissionTpeId);
					com.setOperateurMin(commissionTpe.getOperateurMin());
					com.setOperateurMax(commissionTpe.getOperateurMax());
					com.setValeurComFixMin(commissionTpe.getValeurComFixMin());
					// com.setValeurComFixMax(commissionTpe.getValeurComFixMax());
					com.setValeurComVariableMin(commissionTpe.getValeurComVariableMin());
					com.setCommissionType(2);
					System.out.println("ommissionTypeCode = " + tpeRequest.getCommissionTypeCode());
					com.setValeurCommissionInternationaleFixe("");
					com.setValeurCommissionNationaleFix("");

					com.setDateSaisie(Strdate);

					commissionTpeRepository.save(com);
				}
				List<CommissionTpeRequest> CommissionTpesInter = tpeRequest.getCommissionTpesInter();

				for (CommissionTpeRequest commissionTpeinter : CommissionTpesInter) {
					CommissionTpe comInter = new CommissionTpe();
					CommissionTpeId commissionTpeId2 = new CommissionTpeId();
					comInter.setRequestCode(Request.getRequestCode());
					comInter.setCommissionNational("0");
					comInter.setCommissionInterNational("1");
					comInter.setMontantRefMin(commissionTpeinter.getMontantRefMin());
					comInter.setMontantRefMax(commissionTpeinter.getMontantRefMax());
//                commissionTpeId2.setMerchantCode(commissionTpeinter.getMerchantCode());
					comInter.setMerchantCode(0);
					// comInter.setCommissionTpeId(commissionTpeId2);
					comInter.setOperateurMin(commissionTpeinter.getOperateurMin());
					comInter.setOperateurMax(commissionTpeinter.getOperateurMax());
					comInter.setValeurComFixMin(commissionTpeinter.getValeurComFixMin());
					// comInter.setValeurComFixMax(commissionTpeinter.getValeurComFixMax());
					comInter.setValeurComVariableMin(commissionTpeinter.getValeurComVariableMin());
					// comInter.setValeurComVariableMax(commissionTpeinter.getValeurComVariableMax());
					comInter.setCommissionType(2);
					System.out.println("commissionTypeCode 1 = " + tpeRequest.getCommissionTypeCode());

					comInter.setValeurCommissionInternationaleFixe("");
					comInter.setValeurCommissionNationaleFix("");
					comInter.setDateSaisie(Strdate);

					commissionTpeRepository.save(comInter);
				}
			}
		}
		try {
			logger.info("begin mail");
			String nomClient = Request.getRso();
			String numCompte = "035 " + ag.getInitial() + " " + Request.getAccountNumber();
			myEmailService.sendOtpMessage(ag.getEmail(), "Demande d'acquisition TPE en attente de validation",
					"Bonjour,\n\nNous vous informons qu’une demande de POS du commerçant \"" + nomClient
							+ "\" domicilié sous le N° de compte \"" + numCompte
							+ "\" est en attente de votre validation.\n\nCordialement");
			logger.info("end mail");

		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}
		// generatePDF(tpeRequest);
		logger.info("*************end add request****************");
		return ResponseEntity.accepted().body(gson.toJson("TpeRequest added successfully!"));

	}

//  @PostMapping("addRequestTpe")
//  public ResponseEntity<String> addRequestTpe(@RequestBody TpeRequestDisplay tpeRequest) {
//    logger.info(tpeRequest.toString());
//TpeRequest Request = new TpeRequest();
//      String name = SecurityContextHolder.getContext().getAuthentication().getName();
//      if (!userRepository.findByUserNameOrUserEmail(name, name).isPresent()) {
//          return null;
//
//      }
//      User user = userRepository.findByUserNameOrUserEmail(name, name).get();
//      if(tpeRequest.getCommissionInterNational()==null){
//          Request.setCommissionInterNational("");
//      }
//      else{
//          Request.setCommissionInterNational(tpeRequest.getCommissionInterNational());
//      }
//      if(tpeRequest.getCommissionNational()==null){
//          Request.setCommissionNational("");
//
//      }
//      else
//      {
//          Request.setCommissionNational(tpeRequest.getCommissionNational());
//      }
//      Request.setAdresse(tpeRequest.getAdresse());
//      Request.setAgence(Integer.parseInt(tpeRequest.getAgence()));
//      Request.setAgentName(user.getUserName());
//      Request.setCity(tpeRequest.getCity());
//      Request.setCodeZip(tpeRequest.getCodeZip());
//     Request.setUserName(tpeRequest.getUserName());
//Request.setCommissionTypeCode(Integer.parseInt(tpeRequest.getCommissionTypeCode()));
//      Request.setCountry(tpeRequest.getCountry());
//      Request.setAccountNumber(tpeRequest.getAccountNumber());
//      Request.setDateCreation(new Date());
//      Request.setDateDecision(tpeRequest.getDateDecision());
//      Request.setNombreTPE(tpeRequest.getNombreTPE());
//      Request.setPhone(tpeRequest.getPhone());
//
//
//      Set<PendingTpeDisplay> pedndingTpes = tpeRequest.getPedndingTpes();
//      System.out.println("TPE1    "+pedndingTpes.toString());
//      for (PendingTpeDisplay p : pedndingTpes
//      ) {
//          PendingTpe pending = new PendingTpe();
//          System.out.println("TPE    "+pedndingTpes.toString());
//       //   p.setStatus(1);
//          pending.setAdresse(p.getAdresse());
//          pending.setCity(p.getCity());
//          pending.setCountry(p.getCountry());
//          pending.setLibelle(p.getLibelle());
//          pending.setMccCode(p.getMccCode());
//          pending.setMc
//          pendingTpeRepository.save(p);
//      }
//      tpeRequestRepository.save(Request);
//      List<CommissionTpeRequest> commissionTpes = tpeRequest.getCommissionTpes();
//
//      for (CommissionTpeRequest commissionTpe : commissionTpes
//      ) {
//          System.out.println(commissionTpes.toString());
//          CommissionTpe com = new CommissionTpe();
//          CommissionTpeId commissionTpeId = new CommissionTpeId();
//          commissionTpeId.setCommissionNational("1");
//          commissionTpeId.setCommissionInterNational("0");
//          System.out.println("monatnt ref    "+commissionTpe.getMontantRef());
//
//          commissionTpeId.setMontantRef(commissionTpe.getMontantRef());
//          commissionTpeId.setRequestCode(Request.getRequestCode());
//          System.out.println(commissionTpeId.toString());
//
//          com.setCommissionTpeId(commissionTpeId);
//          com.setOperateur(commissionTpe.getOperateur());
//          com.setValeurComFix(commissionTpe.getValeurComFix());
//          com.setValeurComVariable(commissionTpe.getValeurComVariable());
//
//          commissionTpeRepository.save(com);
//      }
//      List<CommissionTpeRequest> CommissionTpesInter = tpeRequest.getCommissionTpesInter();
//
//
//      for (CommissionTpeRequest commissionTpeinter : CommissionTpesInter
//      ) {
//          CommissionTpe comInter = new CommissionTpe();
//          CommissionTpeId commissionTpeId2 = new CommissionTpeId();
//          commissionTpeId2.setRequestCode(Request.getRequestCode());
//          commissionTpeId2.setCommissionNational("0");
//          commissionTpeId2.setCommissionInterNational("1");
//          commissionTpeId2.setMontantRef(commissionTpeinter.getMontantRef());
//          comInter.setCommissionTpeId(commissionTpeId2);
//          comInter.setOperateur(commissionTpeinter.getOperateur());
//          comInter.setValeurComFix(commissionTpeinter.getValeurComFix());
//          comInter.setValeurComVariable(commissionTpeinter.getValeurComVariable());
//
//          commissionTpeRepository.save(comInter);
//      }
//
//
//
//      return ResponseEntity.accepted().body(gson.toJson("TpeRequest added successfully!"));
//
//
//  }
// @PostMapping("addRequestTpe")
// public ResponseEntity<String> addRequestTpe(@RequestBody TpeRequest tpeRequest) {
//     logger.info(tpeRequest.toString());
//
//     String name = SecurityContextHolder.getContext().getAuthentication().getName();
//     System.out.println(" nammmmmeee " + name);
//     //  String id = userPrincipal.getId();
//     if (!userRepository.findByUserNameOrUserEmail(name, name).isPresent()) {
//         return null;
//
//     }
//     User user = userRepository.findByUserNameOrUserEmail(name, name).get();
//     //   String id = userPrincipal.getUsername();
//
//     //  User u = userRepository.findByUserName(id).get();
//     //   System.out.println("Useerrrrrrrrrrrrr " + u.getUserName());
//     tpeRequest.setDateCreation(new Date());
//     tpeRequest.setStatus(1);
//     tpeRequest.setAgentName(user.getUserName());
//     Set<PendingTpe> pedndingTpes = tpeRequest.getPendingTpes();
//     for (PendingTpe p : pedndingTpes
//     ) {
//         p.setStatus(1);
//         pendingTpeRepository.save(p);
//     }
//     tpeRequestRepository.save(tpeRequest);
//     return ResponseEntity.accepted().body(gson.toJson("TpeRequest added successfully!"));
// }

	@GetMapping("posTypes")
	public List<PosType> getAllTpeType() {
		logger.info(posTypeRepository.findAll().toString());

		return posTypeRepository.findAll();
	}

	@GetMapping("modeTpes")
	public List<ModelTpe> getAllModelTpe() {

		List<ModelTpe> modelTpes = new ArrayList<>();
		List<ModelTpe> all = modelTpeRepository.findAll();

		for (ModelTpe model : all) {

			if (model.getNbreModel() != 0) {
				modelTpes.add(model);
			}
		}
		logger.info(modelTpes.toString());

		return modelTpes;
	}

//  @GetMapping("agences")
//  public List<Agence> getAllAgence() {
//
//    logger.info(agenceRepository.findAll().toString());
//
//    return agenceRepository.findAll();
//  }

	@GetMapping("posAllowedTransConfs")
	public List<PosAllowedTransConf> getAllAllowedTransConfs() {
		List<PosAllowedTransConf> list = new ArrayList<PosAllowedTransConf>();
		for (PosAllowedTransConf conf : posAllowedTransactionConfRepository.findAll()) {
			PosAllowedTransConf posAllowedTrans = new PosAllowedTransConf();
			posAllowedTrans.setPatcCode(conf.getPatcCode());
			posAllowedTrans.setPatcLibelle(conf.getPatcLibelle());

			list.add(posAllowedTrans);
		}
		return list;
	}

	@PostMapping("GetAllposAllowedTransConfseFiltred")
	public List<PosAllowedTransConf> GetAllposAllowedTransConfseFiltred(@RequestBody String patcLibelle) {
		List<PosAllowedTransConf> list = new ArrayList<PosAllowedTransConf>();
		if (!patcLibelle.equals("=")) {
			for (PosAllowedTransConf conf : posAllowedTransactionConfRepository
					.findAllpatcLibelle(patcLibelle.trim())) {
				PosAllowedTransConf posAllowedTrans = new PosAllowedTransConf();
				posAllowedTrans.setPatcCode(conf.getPatcCode());
				posAllowedTrans.setPatcLibelle(conf.getPatcLibelle());

				list.add(posAllowedTrans);
			}
			return list;
		} else {
			for (PosAllowedTransConf conf : posAllowedTransactionConfRepository.findAll()) {
				PosAllowedTransConf posAllowedTrans = new PosAllowedTransConf();
				posAllowedTrans.setPatcCode(conf.getPatcCode());
				posAllowedTrans.setPatcLibelle(conf.getPatcLibelle());

				list.add(posAllowedTrans);
			}
			return list;

		}

	}

	@GetMapping("getPosAllowedTransConfById/{id}")
	public ResponseEntity<PosAllowedTransConfDisplayDetails> getPosAllowedTransConfById(
			@PathVariable(value = "id") int id) {
		PosAllowedTransConfDisplayDetails details = new PosAllowedTransConfDisplayDetails();
		List<PosAllowedTransConfDisplayDetails> list = new ArrayList<PosAllowedTransConfDisplayDetails>();
		Optional<PosAllowedTransConf> conf = posAllowedTransactionConfRepository.findById(id);
		if (conf.isPresent()) {
			details.setPatc_code(conf.get().getPatcCode());
			details.setPatc_libelle(conf.get().getPatcLibelle());
			// details.setPos_terminal(conf.get().getPOS_TERMINAL());
			details.setAdditional_card_activation(conf.get().getPOS_ALLOWED_TRANS().getAdditionalCardActivation());
			details.setAdjust_when_amt(conf.get().getPOS_ALLOWED_TRANS().getAdjustWhenAmt());
			details.setAllowed_trans_id(conf.get().getPOS_ALLOWED_TRANS().getId());
			details.setBalance_inquery(conf.get().getPOS_ALLOWED_TRANS().getBalanceInquery());
			details.setCard_activation(conf.get().getPOS_ALLOWED_TRANS().getCardActivation());
			details.setCard_verification(conf.get().getPOS_ALLOWED_TRANS().getCardVerification());
			details.setCash_advance(conf.get().getPOS_ALLOWED_TRANS().getCashAdvance());
			details.setCash_advance_adjustement(conf.get().getPOS_ALLOWED_TRANS().getCashAdvanceAdjustement());
			details.setCash_back(conf.get().getPOS_ALLOWED_TRANS().getCashBack());
			details.setCash_back_adjust(conf.get().getPOS_ALLOWED_TRANS().getCashBackAdjust());
			details.setCheck_guarantee(conf.get().getPOS_ALLOWED_TRANS().getCheckGuarantee());
			details.setCheck_verification(conf.get().getPOS_ALLOWED_TRANS().getCheckVerification());
			details.setClerk_totals(conf.get().getPOS_ALLOWED_TRANS().getClerkTotals());
			details.setClose_batsh(conf.get().getPOS_ALLOWED_TRANS().getCloseBatsh());
			details.setClose_day(conf.get().getPOS_ALLOWED_TRANS().getCloseDay());
			details.setClose_shift(conf.get().getPOS_ALLOWED_TRANS().getCloseShift());
			details.setCompletion(conf.get().getPOS_ALLOWED_TRANS().getCompletion());
			details.setFull_redemption(conf.get().getPOS_ALLOWED_TRANS().getFullRedemption());
			details.setMail_delivred(conf.get().getPOS_ALLOWED_TRANS().getMailDelivred());
			details.setMail_tel_order(conf.get().getPOS_ALLOWED_TRANS().getMailTelOrder());
			details.setMarchandise_ret_adjust(conf.get().getPOS_ALLOWED_TRANS().getMarchandiseRetAdjust());
			details.setMarchandise_return(conf.get().getPOS_ALLOWED_TRANS().getMarchandiseReturn());
			details.setNormal_purchase(conf.get().getPOS_ALLOWED_TRANS().getNormalPurchase());
			details.setPreauth_purshase(conf.get().getPOS_ALLOWED_TRANS().getPreauthPurshase());
			details.setSend_mail(conf.get().getPOS_ALLOWED_TRANS().getSendMail());
			details.setRead_mail(conf.get().getPOS_ALLOWED_TRANS().getReadMail());
			details.setPosition_18(conf.get().getPOS_ALLOWED_TRANS().getPosition18());
			details.setPurshase_adjustement(conf.get().getPOS_ALLOWED_TRANS().getPurshaseAdjustement());
			details.setSales_draft(conf.get().getPOS_ALLOWED_TRANS().getSalesDraft());
			details.setPreaut_lesser_amount(conf.get().getPOS_ALLOWED_TRANS().getPreautLesserAmount());
			details.setReplenchement(conf.get().getPOS_ALLOWED_TRANS().getReplenchement());

		}
		// list.add(details);
		return ResponseEntity.ok().body(details);

	}

	@PostMapping("addNewPosAllowedTransConfig")
	public ResponseEntity<String> addNewPosAllowedTransConfig(
			@RequestBody NewPosAllowedTransConf newPosAllowedTransConf) {

		try {

			PosAllowedTransConf conf = new PosAllowedTransConf();
			PosAllowedTrans allowedTrans = new PosAllowedTrans();

			allowedTrans.setAdditionalCardActivation(newPosAllowedTransConf.getAdditional_card_activation());
			allowedTrans.setAdjustWhenAmt(newPosAllowedTransConf.getAdjust_when_amt());
			allowedTrans.setBalanceInquery(newPosAllowedTransConf.getBalance_inquery());
			allowedTrans.setCardActivation(newPosAllowedTransConf.getCard_activation());
			allowedTrans.setCardVerification(newPosAllowedTransConf.getCard_verification());
			allowedTrans.setCashAdvance(newPosAllowedTransConf.getCash_advance());
			allowedTrans.setCashBack(newPosAllowedTransConf.getCash_back());
			allowedTrans.setCashBackAdjust(newPosAllowedTransConf.getCash_back_adjust());
			allowedTrans.setCashAdvanceAdjustement(newPosAllowedTransConf.getCash_advance_adjustement());
			allowedTrans.setCheckGuarantee(newPosAllowedTransConf.getCheck_guarantee());
			allowedTrans.setCheckVerification(newPosAllowedTransConf.getCheck_verification());
			allowedTrans.setClerkTotals(newPosAllowedTransConf.getClerk_totals());
			allowedTrans.setCloseBatsh(newPosAllowedTransConf.getClose_batsh());
			allowedTrans.setCloseDay(newPosAllowedTransConf.getClose_day());
			allowedTrans.setCloseShift(newPosAllowedTransConf.getClose_shift());
			allowedTrans.setCompletion(newPosAllowedTransConf.getCompletion());
			allowedTrans.setFullRedemption(newPosAllowedTransConf.getFull_redemption());
			allowedTrans.setMailDelivred(newPosAllowedTransConf.getMail_delivred());
			allowedTrans.setMailTelOrder(newPosAllowedTransConf.getMail_tel_order());
			allowedTrans.setMarchandiseRetAdjust(newPosAllowedTransConf.getMarchandise_ret_adjust());
			allowedTrans.setMarchandiseReturn(newPosAllowedTransConf.getMarchandise_return());
			allowedTrans.setNormalPurchase(newPosAllowedTransConf.getNormal_purchase());
			allowedTrans.setPreauthPurshase(newPosAllowedTransConf.getPreauth_purshase());
			allowedTrans.setSendMail(newPosAllowedTransConf.getSend_mail());
			allowedTrans.setReadMail(newPosAllowedTransConf.getRead_mail());
			allowedTrans.setPosition18(newPosAllowedTransConf.getPosition_18());
			allowedTrans.setPurshaseAdjustement(newPosAllowedTransConf.getPurshase_adjustement());
			allowedTrans.setSalesDraft(newPosAllowedTransConf.getSales_draft());
			allowedTrans.setPreautLesserAmount(newPosAllowedTransConf.getPreaut_lesser_amount());
			allowedTrans.setReplenchement(newPosAllowedTransConf.getReplenchement());

			allowedTrans = posAllowedTransactionRepository.save(allowedTrans);

			conf.setPatcLibelle(newPosAllowedTransConf.getLabel());
			conf.setPOS_ALLOWED_TRANS(allowedTrans);

			allowedTrans.getPosAllowedTransConfs().add(conf);

			posAllowedTransactionConfRepository.save(conf);
			posAllowedTransactionRepository.save(allowedTrans);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ResponseEntity.accepted().body(gson.toJson("PosAllowedTrans added successfully!"));
	}

	@PutMapping("updatePosAllowedTransConfig/{id}")
	public ResponseEntity<String> updatePosAllowedTransConfig(@PathVariable(value = "id") Integer id,
			@RequestBody NewPosAllowedTransConf updateRequest) {
		Optional<PosAllowedTransConf> conf = posAllowedTransactionConfRepository.findById(id);
		if (conf.isPresent()) {
			conf.get().setPatcLibelle(updateRequest.getLabel());
			posAllowedTransactionConfRepository.save(conf.get());
			Optional<PosAllowedTrans> posAllowedTrans = posAllowedTransactionRepository
					.findById(conf.get().getPOS_ALLOWED_TRANS().getId());
			posAllowedTrans.get().setAdditionalCardActivation(updateRequest.getAdditional_card_activation());
			posAllowedTrans.get().setAdjustWhenAmt(updateRequest.getAdjust_when_amt());
			posAllowedTrans.get().setBalanceInquery(updateRequest.getBalance_inquery());
			posAllowedTrans.get().setCardActivation(updateRequest.getCard_activation());
			posAllowedTrans.get().setCardVerification(updateRequest.getCard_verification());
			posAllowedTrans.get().setCashAdvance(updateRequest.getCash_advance());
			posAllowedTrans.get().setCashBack(updateRequest.getCash_back());
			posAllowedTrans.get().setCashBackAdjust(updateRequest.getCash_back_adjust());
			posAllowedTrans.get().setCashAdvanceAdjustement(updateRequest.getCash_advance_adjustement());
			posAllowedTrans.get().setCheckGuarantee(updateRequest.getCheck_guarantee());
			posAllowedTrans.get().setCheckVerification(updateRequest.getCheck_verification());
			posAllowedTrans.get().setClerkTotals(updateRequest.getClerk_totals());
			posAllowedTrans.get().setCloseBatsh(updateRequest.getClose_batsh());
			posAllowedTrans.get().setCloseDay(updateRequest.getClose_day());
			posAllowedTrans.get().setCloseShift(updateRequest.getClose_shift());
			posAllowedTrans.get().setCompletion(updateRequest.getCompletion());
			posAllowedTrans.get().setFullRedemption(updateRequest.getFull_redemption());
			posAllowedTrans.get().setMailDelivred(updateRequest.getMail_delivred());
			posAllowedTrans.get().setMailTelOrder(updateRequest.getMail_tel_order());
			posAllowedTrans.get().setMarchandiseRetAdjust(updateRequest.getMarchandise_ret_adjust());
			posAllowedTrans.get().setMarchandiseReturn(updateRequest.getMarchandise_return());
			posAllowedTrans.get().setNormalPurchase(updateRequest.getNormal_purchase());
			posAllowedTrans.get().setPreauthPurshase(updateRequest.getPreauth_purshase());
			posAllowedTrans.get().setSendMail(updateRequest.getSend_mail());
			posAllowedTrans.get().setReadMail(updateRequest.getRead_mail());
			posAllowedTrans.get().setPosition18(updateRequest.getPosition_18());
			posAllowedTrans.get().setPurshaseAdjustement(updateRequest.getPurshase_adjustement());
			posAllowedTrans.get().setSalesDraft(updateRequest.getSales_draft());
			posAllowedTrans.get().setPreautLesserAmount(updateRequest.getPreaut_lesser_amount());
			posAllowedTrans.get().setReplenchement(updateRequest.getReplenchement());

			posAllowedTransactionRepository.save(posAllowedTrans.get());
		}

		return ResponseEntity.accepted().body(gson.toJson("PosAllowedTrans updated successfully!"));

	}

	@GetMapping("posLimitsConfs")
	public List<PosLimitsConf> getAllLimitsConfs() {
		List<PosLimitsConf> list = new ArrayList<PosLimitsConf>();

		for (PosLimitsConf conf : posLimitsConfRepository.findAll()) {
			PosLimitsConf posLimits = new PosLimitsConf();
			posLimits.setPlcCode(conf.getPlcCode());
			posLimits.setPlcLibelle(conf.getPlcLibelle());
			list.add(posLimits);
		}
// return  posLimitsConfRepository.findAllPosLimitsConfs();
		return list;

	}

	@PostMapping("getAllposLimitsConfsfFiltred")
	public List<PosLimitsConf> getAllposLimitsConfsfFiltred(@RequestBody String plcLibelle) {
		List<PosLimitsConf> list = new ArrayList<PosLimitsConf>();
		if (!plcLibelle.equals("=")) {
			for (PosLimitsConf conf : posLimitsConfRepository.findAllplcLibelle(plcLibelle.trim())) {
				PosLimitsConf posLimits = new PosLimitsConf();
				posLimits.setPlcCode(conf.getPlcCode());
				posLimits.setPlcLibelle(conf.getPlcLibelle());
				list.add(posLimits);
			}
			return list;
		} else {
			for (PosLimitsConf conf : posLimitsConfRepository.findAll()) {
				PosLimitsConf posLimits = new PosLimitsConf();
				posLimits.setPlcCode(conf.getPlcCode());
				posLimits.setPlcLibelle(conf.getPlcLibelle());
				list.add(posLimits);
			}
			return list;

		}

	}

	@GetMapping("getposLimitsConfById/{id}")
	public ResponseEntity<List<PosLimitsConfDisplayDetails>> getposLimitsConfById(@PathVariable(value = "id") int id) {
		PosLimitsConfDisplayDetails details = new PosLimitsConfDisplayDetails();
		List<PosLimitsConfDisplayDetails> list = new ArrayList<PosLimitsConfDisplayDetails>();
		Optional<PosLimitsConf> conf = posLimitsConfRepository.findById(id);
		if (conf.isPresent()) {
			details.setPlc_code(conf.get().getPlcCode());
			details.setPlc_libelle(conf.get().getPlcLibelle());
			details.setLimits_id(conf.get().getPosLimits().getId());
			details.setAdjustement_amount_limit(conf.get().getPosLimits().getAdjustementAmountLimit());
			details.setAdjustement_count_limit(conf.get().getPosLimits().getAdjustementCountLimit());
			details.setReturn_amount_limit(conf.get().getPosLimits().getReturnAmountLimit());
			details.setReturn_count_limit(conf.get().getPosLimits().getReturnCountLimit());
		}
		list.add(details);
		return ResponseEntity.ok().body(list);

	}

	@PostMapping("addNewPosLimitsConfig")
	public ResponseEntity<String> addNewPosLimitsConfig(@RequestBody NewPosLimitsConf newPosLimitsConf) {
		logger.info(newPosLimitsConf.toString());
		PosLimitsConf conf = new PosLimitsConf();
		PosLimits posLimits = new PosLimits();

		posLimits.setAdjustementCountLimit(newPosLimitsConf.getAdjustementCountLimit());
		posLimits.setAdjustementAmountLimit(newPosLimitsConf.getAdjustementAmountLimit());
		posLimits.setReturnAmountLimit(newPosLimitsConf.getReturnAmountLimit());
		posLimits.setReturnCountLimit(newPosLimitsConf.getReturnCountLimit());

		posLimits = posLimitsRepository.save(posLimits);

		conf.setPlcLibelle(newPosLimitsConf.getLabel());
		conf.setPosLimits(posLimits);
		posLimits.getPosLimitsConfs().add(conf);

		posLimitsConfRepository.save(conf);
		posLimitsRepository.save(posLimits);

		return ResponseEntity.accepted().body(gson.toJson("PosLimitsConf added successfully!"));
	}

	@PutMapping("updatePosLimitsConfig/{id}")
	public ResponseEntity<String> updatePosLimitsConfig(@PathVariable(value = "id") Integer id,
			@RequestBody NewPosLimitsConf updateRequest) {

		// POS_LIMITS_CONF conf=posLimitsConfRepository.f
		Optional<PosLimitsConf> conf = posLimitsConfRepository.findById(id);
		if (conf.isPresent()) {
			conf.get().setPlcLibelle(updateRequest.getLabel());
			posLimitsConfRepository.save(conf.get());
			Optional<PosLimits> posLimits = posLimitsRepository.findById(conf.get().getPosLimits().getId());

			if (posLimits.isPresent()) {

				posLimits.get().setAdjustementAmountLimit(updateRequest.getAdjustementAmountLimit());
				posLimits.get().setAdjustementCountLimit(updateRequest.getAdjustementCountLimit());
				posLimits.get().setReturnAmountLimit(updateRequest.getReturnAmountLimit());
				posLimits.get().setReturnCountLimit(updateRequest.getReturnCountLimit());

				posLimitsRepository.save(posLimits.get());

			}
		}
		return ResponseEntity.accepted().body(gson.toJson("PosLimitsConf updated successfully!"));

	}

	@GetMapping("posServicesConfs")
	public List<PosServiceConf> getAllServicesConfs() {
		List<PosServiceConf> list = new ArrayList<PosServiceConf>();

		for (PosServiceConf conf : posServicesConfRepository.findAll()) {
			PosServiceConf posServices = new PosServiceConf();
			posServices.setPscCode(conf.getPscCode());
			posServices.setPscLibelle(conf.getPscLibelle());
			list.add(posServices);
		}
		return list;
	}

	@PostMapping("getAllPosServiceConfFiltred")
	public List<PosServiceConf> getAllPosServiceConfFiltred(@RequestBody String pscLibelle) {
		List<PosServiceConf> list = new ArrayList<PosServiceConf>();
		if (!pscLibelle.equals("=")) {
			for (PosServiceConf conf : posServicesConfRepository.findAllBypscLibelle(pscLibelle.trim())) {
				PosServiceConf posServices = new PosServiceConf();
				posServices.setPscCode(conf.getPscCode());
				posServices.setPscLibelle(conf.getPscLibelle());
				list.add(posServices);
			}
			return list;

		} else {
			for (PosServiceConf conf : posServicesConfRepository.findAll()) {
				PosServiceConf posServices = new PosServiceConf();
				posServices.setPscCode(conf.getPscCode());
				posServices.setPscLibelle(conf.getPscLibelle());
				list.add(posServices);
			}
			return list;

		}

	}

	@GetMapping("posBinConfs")
	public List<PosBinConf> getAllPosBinConfs() {
		List<PosBinConf> list = new ArrayList<PosBinConf>();

		for (PosBinConf conf : posBinConfRepository.findAll()) {
			PosBinConf posBin = new PosBinConf();
			posBin.setPbcCode(conf.getPbcCode());
			posBin.setPbcLibelle(conf.getPbcLibelle());
			list.add(posBin);
		}
		return list;
	}

	@PostMapping("GetAllPosBinConfFiltred")
	public List<PosBinConf> GetAllPosBinConfFiltred(@RequestBody String pbcLibelle) {
		List<PosBinConf> list = new ArrayList<PosBinConf>();
		if (!pbcLibelle.equals("=")) {
			for (PosBinConf conf : posBinConfRepository.findAllpbcLibelle(pbcLibelle.trim())) {
				PosBinConf posBin = new PosBinConf();
				posBin.setPbcCode(conf.getPbcCode());
				posBin.setPbcLibelle(conf.getPbcLibelle());
				list.add(posBin);
			}

			return list;
		} else {
			for (PosBinConf conf : posBinConfRepository.findAll()) {
				PosBinConf posBin = new PosBinConf();
				posBin.setPbcCode(conf.getPbcCode());
				posBin.setPbcLibelle(conf.getPbcLibelle());
				list.add(posBin);
			}

			return list;

		}

	}

	@GetMapping("posServicesList")
	public List<PosService> getAllPosServices() {
		List<PosService> list = new ArrayList<PosService>();
		for (PosService service : posServiceRepository.findAllWithSort()) {
			PosService posService = new PosService();
			posService.setCardType(service.getCardType());
			posService.setCashadvanceFloorLimit(service.getCashadvanceFloorLimit());
			;
			posService.setMailFloorLimit(service.getMailFloorLimit());
			posService.setPsCode(service.getPsCode());
			posService.setPurchaiseFloorLimit(service.getPurchaiseFloorLimit());
			posService.setTransactionLimit(service.getTransactionLimit());
			posService.setTransactionProfile(service.getTransactionProfile());
			list.add(posService);
		}
		return list;
	}

	@PostMapping("GetAllPosServiceFiltred")
	public List<PosService> GetAllPosServiceFiltred(@RequestBody String cardType) {
		List<PosService> list = new ArrayList<PosService>();
		if (!cardType.equals("=")) {
			for (PosService service : posServiceRepository.findAllcardType(cardType.trim())) {
				PosService posService = new PosService();
				posService.setCardType(service.getCardType());
				posService.setCashadvanceFloorLimit(service.getCashadvanceFloorLimit());
				;
				posService.setMailFloorLimit(service.getMailFloorLimit());
				posService.setPsCode(service.getPsCode());
				posService.setPurchaiseFloorLimit(service.getPurchaiseFloorLimit());
				posService.setTransactionLimit(service.getTransactionLimit());
				posService.setTransactionProfile(service.getTransactionProfile());
				list.add(posService);
			}

			return list;
		} else {
			for (PosService service : posServiceRepository.findAll()) {
				PosService posService = new PosService();
				posService.setCardType(service.getCardType());
				posService.setCashadvanceFloorLimit(service.getCashadvanceFloorLimit());
				;
				posService.setMailFloorLimit(service.getMailFloorLimit());
				posService.setPsCode(service.getPsCode());
				posService.setPurchaiseFloorLimit(service.getPurchaiseFloorLimit());
				posService.setTransactionLimit(service.getTransactionLimit());
				posService.setTransactionProfile(service.getTransactionProfile());
				list.add(posService);
			}

			return list;

		}

	}

	@GetMapping("posBinList")
	public List<PosBin> getAllPosBin() {
		List<PosBin> list = new ArrayList<PosBin>();

		for (PosBin bin : posBinRepository.findAllWithSort()) {
			PosBin posBin = new PosBin();
			posBin.setBinCode(bin.getBinCode());
			posBin.setBachTelNumber(bin.getBachTelNumber());
			posBin.setHighPrefix(bin.getHighPrefix());
			posBin.setLowPrefix(bin.getLowPrefix());
			posBin.setMainTelNumber(bin.getMainTelNumber());
			posBin.setDraftCaptureFlag(bin.getDraftCaptureFlag());
			posBin.setMod10CheckFlag(bin.getMod10CheckFlag());
			posBin.setPanFraudCheckFlag(bin.getPanFraudCheckFlag());
			posBin.setPinValidationFlag(bin.getPinValidationFlag());
			posBin.setReceiptFlag(bin.getReceiptFlag());
			posBin.setReferralTelNumber(bin.getReferralTelNumber());
			posBin.setRetailerId(bin.getRetailerId());
			posBin.setTotalsFlag(bin.getTotalsFlag());
			posBin.setUserDefinedData(bin.getUserDefinedData());

			list.add(posBin);
		}
		return list;
	}

	@PostMapping("GetAllposBinListFiltred")
	public List<PosBin> GetAllposBinListFiltred(@RequestBody String lowPrefix) {
		List<PosBin> list = new ArrayList<PosBin>();
		if (!lowPrefix.equals("=")) {
			for (PosBin bin : posBinRepository.findAlllowPrefix(lowPrefix.trim())) {
				PosBin posBin = new PosBin();
				posBin.setBinCode(bin.getBinCode());
				posBin.setBachTelNumber(bin.getBachTelNumber());
				posBin.setHighPrefix(bin.getHighPrefix());
				posBin.setLowPrefix(bin.getLowPrefix());
				posBin.setMainTelNumber(bin.getMainTelNumber());
				posBin.setDraftCaptureFlag(bin.getDraftCaptureFlag());
				posBin.setMod10CheckFlag(bin.getMod10CheckFlag());
				posBin.setPanFraudCheckFlag(bin.getPanFraudCheckFlag());
				posBin.setPinValidationFlag(bin.getPinValidationFlag());
				posBin.setReceiptFlag(bin.getReceiptFlag());
				posBin.setReferralTelNumber(bin.getReferralTelNumber());
				posBin.setRetailerId(bin.getRetailerId());
				posBin.setTotalsFlag(bin.getTotalsFlag());
				posBin.setUserDefinedData(bin.getUserDefinedData());

				list.add(posBin);
			}
			return list;
		} else {
			for (PosBin bin : posBinRepository.findAll()) {
				PosBin posBin = new PosBin();
				posBin.setBinCode(bin.getBinCode());
				posBin.setBachTelNumber(bin.getBachTelNumber());
				posBin.setHighPrefix(bin.getHighPrefix());
				posBin.setLowPrefix(bin.getLowPrefix());
				posBin.setMainTelNumber(bin.getMainTelNumber());
				posBin.setDraftCaptureFlag(bin.getDraftCaptureFlag());
				posBin.setMod10CheckFlag(bin.getMod10CheckFlag());
				posBin.setPanFraudCheckFlag(bin.getPanFraudCheckFlag());
				posBin.setPinValidationFlag(bin.getPinValidationFlag());
				posBin.setReceiptFlag(bin.getReceiptFlag());
				posBin.setReferralTelNumber(bin.getReferralTelNumber());
				posBin.setRetailerId(bin.getRetailerId());
				posBin.setTotalsFlag(bin.getTotalsFlag());
				posBin.setUserDefinedData(bin.getUserDefinedData());

				list.add(posBin);
			}
			return list;

		}

	}

	@GetMapping("getPosServicesById/{id}")
	public ResponseEntity<PosService> getPosServicesById(@PathVariable(value = "id") int id) {
		Optional<PosService> service = posServiceRepository.findById(id);
		PosService posService = new PosService();
		posService.setCardType(service.get().getCardType());
		posService.setCashadvanceFloorLimit(service.get().getCashadvanceFloorLimit());
		posService.setMailFloorLimit(service.get().getMailFloorLimit());
		posService.setPsCode(service.get().getPsCode());
		posService.setPurchaiseFloorLimit(service.get().getPurchaiseFloorLimit());
		posService.setTransactionLimit(service.get().getTransactionLimit());
		posService.setTransactionProfile(service.get().getTransactionProfile());

		return ResponseEntity.ok().body(posService);
	}

	@GetMapping("getposBinById/{id}")
	public ResponseEntity<PosBin> getposBinById(@PathVariable(value = "id") int id) {
		Optional<PosBin> bin = posBinRepository.findById(id);

		PosBin posBin = new PosBin();
		posBin.setBinCode(bin.get().getBinCode());
		posBin.setBachTelNumber(bin.get().getBachTelNumber());
		posBin.setHighPrefix(bin.get().getHighPrefix());
		posBin.setLowPrefix(bin.get().getLowPrefix());
		posBin.setMainTelNumber(bin.get().getMainTelNumber());
		posBin.setDraftCaptureFlag(bin.get().getDraftCaptureFlag());
		posBin.setMod10CheckFlag(bin.get().getMod10CheckFlag());
		posBin.setPanFraudCheckFlag(bin.get().getPanFraudCheckFlag());
		posBin.setPinValidationFlag(bin.get().getPinValidationFlag());
		posBin.setReceiptFlag(bin.get().getReceiptFlag());
		posBin.setReferralTelNumber(bin.get().getReferralTelNumber());
		posBin.setRetailerId(bin.get().getRetailerId());
		posBin.setTotalsFlag(bin.get().getTotalsFlag());
		posBin.setUserDefinedData(bin.get().getUserDefinedData());

		return ResponseEntity.ok().body(posBin);

	}

	@PostMapping("addNewPosService")
	public ResponseEntity<String> addNewPosService(@RequestBody PosService newPosService) {
		posServiceRepository.save(newPosService);
		return ResponseEntity.accepted().body(gson.toJson("Pos Service added successfully!"));
	}

	@PostMapping("addNewPosBin")
	public ResponseEntity<String> addNewPosBin(@RequestBody PosBin newPosBin) {
		posBinRepository.save(newPosBin);
		return ResponseEntity.accepted().body(gson.toJson("Pos Bin added successfully!"));
	}

	@PutMapping("updatePosBin/{id}")
	public ResponseEntity<String> updatePosBin(@PathVariable(value = "id") Integer id,
			@RequestBody PosBin updateRequest) {

		Optional<PosBin> bin = posBinRepository.findById(id);
		if (bin.isPresent()) {
			bin.get().setBachTelNumber(updateRequest.getBachTelNumber());
			bin.get().setHighPrefix(updateRequest.getHighPrefix());
			bin.get().setLowPrefix(updateRequest.getLowPrefix());
			bin.get().setMainTelNumber(updateRequest.getMainTelNumber());
			bin.get().setDraftCaptureFlag(updateRequest.getDraftCaptureFlag());
			bin.get().setMod10CheckFlag(updateRequest.getMod10CheckFlag());
			bin.get().setPanFraudCheckFlag(updateRequest.getPanFraudCheckFlag());
			bin.get().setPinValidationFlag(updateRequest.getPinValidationFlag());
			bin.get().setReceiptFlag(updateRequest.getReceiptFlag());
			bin.get().setReferralTelNumber(updateRequest.getReferralTelNumber());
			bin.get().setRetailerId(updateRequest.getRetailerId());
			bin.get().setTotalsFlag(updateRequest.getTotalsFlag());
			bin.get().setUserDefinedData(updateRequest.getUserDefinedData());

			posBinRepository.save(bin.get());
		}
		return ResponseEntity.accepted().body(gson.toJson("Pos Bin updated successfully!"));

	}

	@PutMapping("updatePosService/{id}")
	public ResponseEntity<String> updatePosService(@PathVariable(value = "id") Integer id,
			@RequestBody PosService updateRequest) {

		Optional<PosService> service = posServiceRepository.findById(id);
		if (service.isPresent()) {

			service.get().setCardType(updateRequest.getCardType());
			service.get().setCashadvanceFloorLimit(updateRequest.getCashadvanceFloorLimit());
			service.get().setMailFloorLimit(updateRequest.getMailFloorLimit());
			service.get().setPurchaiseFloorLimit(updateRequest.getPurchaiseFloorLimit());
			service.get().setTransactionLimit(updateRequest.getTransactionLimit());
			service.get().setTransactionProfile(updateRequest.getTransactionProfile());

			posServiceRepository.save(service.get());
		}
		return ResponseEntity.accepted().body(gson.toJson("Pos Service updated successfully!"));

	}

	@PostMapping("addNewPosBinConfig")
	public ResponseEntity<String> addNewPosBinConfig(@RequestBody NewPosBinConf newPosBinConf) {

		PosBinConf conf = new PosBinConf();
		Optional<PosBin> bin = null;
		Set<PosBin> set = new HashSet<PosBin>();
		List<PosBin> list = new ArrayList<PosBin>();

		conf.setPbcLibelle(newPosBinConf.getLabel());

		for (int binCode : newPosBinConf.getBins()) {
			bin = posBinRepository.findById(binCode);
			set.add(bin.get());
			list.add(bin.get());

		}

		conf.setPosbinList(set);
		conf = posBinConfRepository.save(conf);

		int i = 0;
		while (i < list.size()) {
			Set<PosBinConf> confList = new HashSet<PosBinConf>();
			PosBin binRow = list.get(i);
			confList = binRow.getPosBinConfsList();
			confList.add(conf);
			binRow.setPosBinConfsList(confList);
			posBinRepository.save(binRow);
			i++;
		}

		return ResponseEntity.accepted().body(gson.toJson("PosBinConf added successfully!"));
	}

	@PostMapping("addNewPosServicesConfig")
	public ResponseEntity<String> addNewPosServicesConfig(@RequestBody NewPosServicesConf newPosServicesConf) {

		PosServiceConf conf = new PosServiceConf();
		Optional<PosService> service = null;
		Set<PosService> set = new HashSet<PosService>();
		List<PosService> list = new ArrayList<PosService>();

		conf.setPscLibelle(newPosServicesConf.getLabel());

		for (int serviceCode : newPosServicesConf.getServices()) {
			service = posServiceRepository.findById(serviceCode);
			set.add(service.get());
			list.add(service.get());
		}
		conf.setPosServiceLIST(set);

		conf = posServicesConfRepository.save(conf);

		int i = 0;
		while (i < list.size()) {
			Set<PosServiceConf> confList = new HashSet<PosServiceConf>();
			PosService serviceRow = list.get(i);
			confList = serviceRow.getPosServiceConfLIST();
			confList.add(conf);
			serviceRow.setPosServiceConfLIST(confList);
			posServiceRepository.save(serviceRow);
			i++;
		}

		return ResponseEntity.accepted().body(gson.toJson("Pos Services Conf added successfully!"));
	}

	@GetMapping("getPosBinConfById/{id}")
	public ResponseEntity<PosBinConfDisplayDetails> getPosBinConfById(@PathVariable(value = "id") int id) {

		PosBinConfDisplayDetails details = new PosBinConfDisplayDetails();
		List<PosBinDetailsForBinConf> bins = new ArrayList<PosBinDetailsForBinConf>();
		Optional<PosBinConf> conf = posBinConfRepository.findById(id);
		if (conf.isPresent()) {
			details.setPbc_code(conf.get().getPbcCode());
			details.setPbc_libelle(conf.get().getPbcLibelle());
			for (PosBin bin : conf.get().getPosbinList()) {

				PosBinDetailsForBinConf posBin = new PosBinDetailsForBinConf();
				posBin.setBach_tel_number(bin.getBachTelNumber());
				posBin.setBin_code(bin.getBinCode());
				posBin.setDraft_capture_flag(bin.getDraftCaptureFlag());
				posBin.setHigh_prefix(bin.getHighPrefix());
				posBin.setLow_prefix(bin.getLowPrefix());
				posBin.setMain_tel_number(bin.getMainTelNumber());
				posBin.setMod_10_check_flag(bin.getMod10CheckFlag());
				posBin.setPan_fraud_check_flag(bin.getPanFraudCheckFlag());
				posBin.setPin_validation_flag(bin.getPinValidationFlag());
				posBin.setReceipt_flag(bin.getReceiptFlag());
				posBin.setReferral_tel_number(bin.getReferralTelNumber());
				posBin.setRetailer_id(bin.getRetailerId());
				posBin.setTotals_flag(bin.getTotalsFlag());
				posBin.setUser_defined_data(bin.getUserDefinedData());
				bins.add(posBin);
			}

			details.setBin_list(bins);
		}

		return ResponseEntity.ok().body(details);

	}

	@GetMapping("getPosServicesConfById/{id}")
	public ResponseEntity<PosServicesConfDisplayDetails> getPosServicesConfById(@PathVariable(value = "id") int id) {

		PosServicesConfDisplayDetails details = new PosServicesConfDisplayDetails();
		List<PosServiceDetailsForServicesConf> services = new ArrayList<PosServiceDetailsForServicesConf>();
		Optional<PosServiceConf> conf = posServicesConfRepository.findById(id);
		if (conf.isPresent()) {
			details.setPsc_code(conf.get().getPscCode());
			details.setPsc_libelle(conf.get().getPscLibelle());

			for (PosService service : conf.get().getPosServiceLIST()) {

				PosServiceDetailsForServicesConf posService = new PosServiceDetailsForServicesConf();
				posService.setCard_type(service.getCardType());
				posService.setCashadvance_floor_limit(service.getCashadvanceFloorLimit());
				;
				posService.setMail_floor_limit(service.getMailFloorLimit());
				posService.setPs_code(service.getPsCode());
				posService.setPurchaise_floor_limit(service.getPurchaiseFloorLimit());
				posService.setTransaction_limit(service.getTransactionLimit());
				posService.setTransaction_profile(service.getTransactionProfile());
				services.add(posService);
			}
			details.setServices_list(services);
		}

		return ResponseEntity.ok().body(details);

	}

	@PutMapping("updatePosBinConfig/{id}")
	public ResponseEntity<String> updatePosBinConfig(@RequestBody NewPosBinConf newPosBinConf,
			@PathVariable(value = "id") int id) {
		try {

			Optional<PosBinConf> conf = posBinConfRepository.findById(id);
			if (conf.isPresent()) {
				Set<PosBin> set = new HashSet<PosBin>();
				Set<Integer> oldBins = new HashSet<>();
				for (PosBin i : conf.get().getPosbinList()) {
					oldBins.add(i.getBinCode());
				}
				conf.get().setPbcLibelle(newPosBinConf.getLabel());
				Optional<PosBin> bin = null;
				List<PosBin> list = new ArrayList<PosBin>();

				for (int binCode : newPosBinConf.getBins()) {
					bin = posBinRepository.findById(binCode);
					set.add(bin.get());
					list.add(bin.get());
				}

				Set<Integer> diff = new HashSet<>(oldBins);
				diff.removeAll(newPosBinConf.getBins());

				Set<Integer> diff2 = new HashSet<>(newPosBinConf.getBins());
				diff2.removeAll(oldBins);

				Set<Integer> allDiff = new HashSet<>();
				allDiff.addAll(diff);
				allDiff.addAll(diff2);

				for (int code : allDiff) {
					bin = posBinRepository.findById(code);
					Set<PosBinConf> confList = new HashSet<PosBinConf>();
					confList = bin.get().getPosBinConfsList();
					// add new bin
					if (newPosBinConf.getBins().contains(code) && !oldBins.contains(code)) {
						logger.info("adding");
						confList.add(conf.get());
					}
					// delete bin
					if (!newPosBinConf.getBins().contains(code) && oldBins.contains(code)) {

						confList.remove(conf.get());
					}
					bin.get().setPosBinConfsList(confList);
					posBinRepository.save(bin.get());
				}
				conf.get().setPosbinList(set);
				posBinConfRepository.save(conf.get());
			}

		} catch (Exception e) {
			logger.error("ee => {}", e);
		}
		return ResponseEntity.accepted().body(gson.toJson("PosBinConf updated successfully!"));

	}

	@PutMapping("updatePosServicesConfig/{id}")
	public ResponseEntity<String> updatePosServicesConfig(@RequestBody NewPosServicesConf newPosServicesConf,
			@PathVariable(value = "id") int id) {
		try {

			Optional<PosServiceConf> conf = posServicesConfRepository.findById(id);
			if (conf.isPresent()) {
				Set<PosService> set = new HashSet<PosService>();
				Set<Integer> oldServices = new HashSet<>();
				for (PosService i : conf.get().getPosServiceLIST()) {
					oldServices.add(i.getPsCode());
				}
				conf.get().setPscLibelle(newPosServicesConf.getLabel());
				Optional<PosService> service = null;
				List<PosService> list = new ArrayList<PosService>();

				for (int serviceCode : newPosServicesConf.getServices()) {
					service = posServiceRepository.findById(serviceCode);
					set.add(service.get());
					list.add(service.get());
				}

				Set<Integer> diff = new HashSet<>(oldServices);
				diff.removeAll(newPosServicesConf.getServices());

				Set<Integer> diff2 = new HashSet<>(newPosServicesConf.getServices());
				diff2.removeAll(oldServices);

				Set<Integer> allDiff = new HashSet<>();
				allDiff.addAll(diff);
				allDiff.addAll(diff2);

				for (int code : allDiff) {
					service = posServiceRepository.findById(code);
					Set<PosServiceConf> confList = new HashSet<PosServiceConf>();
					confList = service.get().getPosServiceConfLIST();

					// add new service
					if (newPosServicesConf.getServices().contains(code) && !oldServices.contains(code)) {
						confList.add(conf.get());
					}
					// delete bin
					if (!newPosServicesConf.getServices().contains(code) && oldServices.contains(code)) {
						confList.remove(conf.get());
					}
					service.get().setPosServiceConfLIST(confList);
					posServiceRepository.save(service.get());
				}
				conf.get().setPosServiceLIST(set);
				posServicesConfRepository.save(conf.get());
			}

		} catch (Exception e) {
			logger.error("ee => {}", e);
		}
		return ResponseEntity.accepted().body(gson.toJson("Pos Services Conf updated successfully!"));

	}

	@PostMapping("getAlltpeRequestsFiltred")
	public List<TpeRequest> getAlltpeRequestsFiltred(@RequestBody String userName) {
		if (!userName.equals("=")) {
			return tpeRequestRepository.findAllByUsername(userName.trim());
		} else {
			return tpeRequestRepository.findAll();
		}
	}

	@GetMapping("mccList")
	public List<MccList> getMccList() {
		return mccRepository.findAll();
	}

	@PostMapping("getAllmccListFiltred")
	public List<MccList> getAllmccListFiltred(@RequestBody String mccListLibelle) {
		if (!mccListLibelle.equals("=")) {
			return mccRepository.findAllmccListLibellee(mccListLibelle.trim());
		} else {
			return mccRepository.findAll();
		}
	}

//  @GetMapping("tpeRequests")
//  public List<TpeRequestDisplay> getTpeRequest() {
//
//    List<TpeRequest> tpeRequests = tpeRequestRepository.findAll();
//    List<TpeRequestDisplay> tpeRequestDisplays = new ArrayList<>();
//    for (TpeRequest tr : tpeRequests
//    ) {
//      TpeRequestDisplay tpeRequestDisplay = new TpeRequestDisplay();
//      tpeRequestDisplay.setAccountNumber(tr.getAccountNumber());
//      Agence agence = agenceRepository.findById(tr.getAgence()).get();
//      tpeRequestDisplay.setAgence(agence.getLibelle());
//      tpeRequestDisplay.setCity(tr.getCity());
//      tpeRequestDisplay.setCodeZip(tr.getCodeZip());
//      tpeRequestDisplay.setCommissionInterNational(tr.getCommissionInterNational());
//      tpeRequestDisplay.setCommissionNational(tr.getCommissionNational());
//      tpeRequestDisplay.setCountry(tr.getCountry());
//      TpeRequestStatus tpeRequestStat = tpeRequestStatusRepository.findByStatusCode(tr.getStatus());
//      tpeRequestDisplay.setStatus(tpeRequestStat.getLibelle());
//      tpeRequestDisplay.setDateCreation(tr.getDateCreation());
//      tpeRequestDisplay.setDateDecision(tr.getDateDecision());
//      tpeRequestDisplay.setNombreTPE(tr.getNombreTPE());
//      tpeRequestDisplay.setCodeZip(tr.getCodeZip());
//      tpeRequestDisplay.setPhone(tr.getPhone());
//      tpeRequestDisplay.setRequestCode(tr.getRequestCode());
//      tpeRequestDisplay.setUserName(tr.getUserName());
//      tpeRequestDisplay.setAdresse(tr.getAdresse());
//        CommissionType commissionType = commissionTypeRepository.findByCommissionTypeCode(tr.getCommissionTypeCode());
//        tpeRequestDisplay.setCommissionTypeCode(commissionType.getComTypeLibelle());
//      tpeRequestDisplays.add(tpeRequestDisplay);
//    }
//    logger.info(tpeRequestDisplays.toString());
//
//    return tpeRequestDisplays;
//  }

	@PostMapping("tpeRequests")
	public List<TpeRequestDisplay> getTpeRequest(@RequestBody RequestTpeFilter requestTpeFilter) {
		List<TpeRequestDisplay> tpeRequestDisplays = new ArrayList<>();

		try {
			List<TpeRequest> tpeRequests = tpeRequestRepository.findAll();
			Calendar cal = Calendar.getInstance();
			cal.setTime(requestTpeFilter.getDateFin());
			cal.add(Calendar.DATE, 1);
			tpeRequests = tpeRequests.stream()

					.filter(e -> Strings.isNullOrEmpty(requestTpeFilter.getAgence())
							|| String.valueOf(e.getAgence()).equals(requestTpeFilter.getAgence()))
					.filter(e -> Strings.isNullOrEmpty(requestTpeFilter.getNumCompte())
							|| e.getAccountNumber().equals(requestTpeFilter.getNumCompte()))
					.filter(e -> Strings.isNullOrEmpty(requestTpeFilter.getStatus())
							|| String.valueOf(e.getStatus()).equals(requestTpeFilter.getStatus()))
					.filter(e -> e.getDateCreation().after(requestTpeFilter.getDateDebut())
							&& e.getDateCreation().before(cal.getTime()))
					.collect(Collectors.toList());

			for (TpeRequest tr : tpeRequests) {
				TpeRequestDisplay tpeRequestDisplay = new TpeRequestDisplay();
				tpeRequestDisplay.setAccountNumber(tr.getAccountNumber());
				tpeRequestDisplay.setRaisonRejet(tr.getDescriptionReject());
				AgenceAdministration agence = agenceAdministrationRepository.findByIdAgence(tr.getAgence()).get();
				tpeRequestDisplay.setAgence(agence.getLibelle());
				tpeRequestDisplay.setCity(tr.getCity());
				tpeRequestDisplay.setCodeZip(tr.getCodeZip());
				tpeRequestDisplay.setCommissionInterNational(tr.getCommissionInterNational());
				tpeRequestDisplay.setCommissionNational(tr.getCommissionNational());
				tpeRequestDisplay.setCountry(tr.getCountry());
				TpeRequestStatus tpeRequestStat = tpeRequestStatusRepository.findByStatusCode(tr.getStatus());
				tpeRequestDisplay.setStatus(tpeRequestStat.getLibelleFr());
				tpeRequestDisplay.setDateCreation(tr.getDateCreation());
				tpeRequestDisplay.setDateDecision(tr.getDateDecision());
				tpeRequestDisplay.setNombreTPE(tr.getNombreTPE());
				tpeRequestDisplay.setCodeZip(tr.getCodeZip());
				tpeRequestDisplay.setPhone(tr.getPhone());
				tpeRequestDisplay.setRequestCode(tr.getRequestCode());
				tpeRequestDisplay.setUserName(tr.getUserName());
				tpeRequestDisplay.setAdresse(tr.getAdresse());
				tpeRequestDisplay.setStatusVa(tr.getStatus());
				tpeRequestDisplay.setStatuContrat(tr.getStatuContrat());
				tpeRequestDisplays.add(tpeRequestDisplay);
			}
			logger.info(tpeRequestDisplays.toString());

		} catch (Exception e) {

			e.printStackTrace();
		}

		return tpeRequestDisplays;
	}

	@GetMapping("getRequest/{id}")
	public ResponseEntity<Set<TpePendingDisplay>> getBin(@PathVariable(value = "id") int id) {

		TpeRequest tpeRequest = tpeRequestRepository.findByRequestCode(id);

		Set<PendingTpe> pedndingTpes = tpeRequest.getPendingTpes();
		Set<TpePendingDisplay> tpePendingDisplays = new HashSet<>();

		for (PendingTpe p : pedndingTpes) {
			TpePendingDisplay tpePendingDisplay = new TpePendingDisplay();
			tpePendingDisplay.setActivite(p.getActivite());

			if (p.getStatus() == 0) {
				tpePendingDisplay.setStatus("");
			} else {
				TpeRequestStatus tpeRequestStat = tpeRequestStatusRepository.findByStatusCode(p.getStatus());
				tpePendingDisplay.setStatus(tpeRequestStat.getLibelle());
			}
			if (p.getType() == 0) {
				tpePendingDisplay.setType("");

			} else {
				PosType posType = posTypeRepository.findByPosTypeCode(p.getType());

				tpePendingDisplay.setType(posType.getLibelle());
			}
			tpePendingDisplay.setCode(p.getCode());

			tpePendingDisplay.setZipCode(p.getZipCode());
			tpePendingDisplay.setPhone(p.getPhone());
			tpePendingDisplay.setCountry(p.getCountry());
			tpePendingDisplay.setCity(p.getCity());
			tpePendingDisplay.setAddress(p.getAdresse());

			if (p.getMccCode() == null) {
				tpePendingDisplay.setMccCode("");

			} else {
				MccList mccList = mccRepository.findByMccCode(p.getMccCode());
				tpePendingDisplay.setMccCode(mccList.getMccListLibelle());
			}

			if (p.getPreAutorisation()) {
				tpePendingDisplay.setPreAutorisation("YES");

			} else {
				tpePendingDisplay.setPreAutorisation("NO");
			}
			FamillePos famillePos = famillePosRepository.getOne(p.getFamillePosCode());
			tpePendingDisplay.setFamillePosCode(famillePos.getFamillePosLibelle());
			tpePendingDisplay.setFamilleCode(p.getFamillePosCode());
			tpePendingDisplay.setLibelle(p.getLibelle());
			tpePendingDisplay.setMotif(p.getMotif());
			tpePendingDisplay.setModel(p.getModel());

			tpePendingDisplays.add(tpePendingDisplay);

		}

		logger.info(tpePendingDisplays.toString());

		return ResponseEntity.ok().body(tpePendingDisplays);
	}

	@GetMapping("getOne/{id}")
	public ResponseEntity<TpeRequestDisplayDetails> getOne(@PathVariable(value = "id") int id) {

		TpeRequest tr = tpeRequestRepository.findByRequestCode(id);
		System.out.println("TRRRRRRRR" + tr);

		Set<PendingTpe> pedndingTpes = tr.getPendingTpes();
		Set<TpePendingDisplay> tpePendingDisplays = new HashSet<>();
		List<CommissionTpe> commissionTpesList = new ArrayList<>();
		List<CommissionTpe> commissionTpesListInter = new ArrayList<>();
		TpeRequestDisplayDetails tpeRequestDisplay = new TpeRequestDisplayDetails();

		String nbAccount = tr.getAccountNumber().substring(7, tr.getAccountNumber().length());

		tpeRequestDisplay.setAccountNumber(nbAccount);
		AgenceAdministration agence = agenceAdministrationRepository.findByIdAgence(tr.getAgence()).get();

		tpeRequestDisplay.setAgence(agence.getInitial());
		CommissionType commissionType = commissionTypeRepository.findByCommissionTypeCode(tr.getCommissionTypeCode());
		tpeRequestDisplay.setCommissionTypeCode(String.valueOf(commissionType.getComTypeLibelle()));

		tpeRequestDisplay.setCity(tr.getCity());
		tpeRequestDisplay.setCodeZip(tr.getCodeZip());
		tpeRequestDisplay.setOffshore(tr.getOffshore());

		tpeRequestDisplay.setCountry(tr.getCountry());
		TpeRequestStatus tpeRequestStat = tpeRequestStatusRepository.findByStatusCode(tr.getStatus());
		tpeRequestDisplay.setStatus(tpeRequestStat.getLibelleFr());
		tpeRequestDisplay.setDateCreation(tr.getDateCreation());
		tpeRequestDisplay.setDateDecision(tr.getDateDecision());
		tpeRequestDisplay.setNombreTPE(tr.getNombreTPE());
		tpeRequestDisplay.setCodeZip(tr.getCodeZip());
		tpeRequestDisplay.setPhone(tr.getPhone());
		tpeRequestDisplay.setRequestCode(tr.getRequestCode());
		tpeRequestDisplay.setUserName(tr.getUserName());
		tpeRequestDisplay.setAdresse(tr.getAdresse());
		tpeRequestDisplay.setCommissionNational(tr.getCommissionNational());
		tpeRequestDisplay.setCommissionInterNational(tr.getCommissionInterNational());
		tpeRequestDisplay.setCodeCommission(tr.getCommissionTypeCode());
		tpeRequestDisplay.setSiteWeb(tr.getSiteWeb());

		tpeRequestDisplay.setNom(tr.getNom());
		tpeRequestDisplay.setPrenom(tr.getPrenom());
		tpeRequestDisplay.setNif(tr.getNif());
		tpeRequestDisplay.setRso(tr.getRso());
		tpeRequestDisplay.setRc(tr.getRc());
		tpeRequestDisplay.setEmail(tr.getEmail());
		tpeRequestDisplay.setDaira(tr.getDaira());
		tpeRequestDisplay.setCommmune(tr.getCommune());
		tpeRequestDisplay.setRevenue(tr.getRevenue());
		tpeRequestDisplay.setNomC(tr.getNomC());
		tpeRequestDisplay.setTitleC(tr.getTitleC());
		tpeRequestDisplay.setMontantLoyer(tr.getMontantLoyer());

		for (PendingTpe p : pedndingTpes) {

			TpePendingDisplay tpePendingDisplay = new TpePendingDisplay();
			tpePendingDisplay.setCode(p.getCode());
			tpePendingDisplay.setActivite(p.getActivite());

			if (p.getStatus() == 0) {
				tpePendingDisplay.setStatus("");
			} else {
				TpeRequestStatus t = tpeRequestStatusRepository.findByStatusCode(p.getStatus());
				tpePendingDisplay.setStatus(t.getLibelleFr());

			}
			if (p.getType() == 0) {
				tpePendingDisplay.setType("");

			} else {
				PosType posType = posTypeRepository.findByPosTypeCode(p.getType());

				tpePendingDisplay.setType(posType.getLibelle());
			}

			tpePendingDisplay.setZipCode(p.getZipCode());
			tpePendingDisplay.setPhone(p.getPhone());
			tpePendingDisplay.setCountry(p.getCountry());
			tpePendingDisplay.setCity(p.getCity());
			tpePendingDisplay.setAddress(p.getAdresse());

			if (p.getMccCode() == null) {
				tpePendingDisplay.setMccCode("");

			} else {
				MccList mccList = mccRepository.findByMccCode(p.getMccCode());
				tpePendingDisplay.setMccCode(mccList.getMccListLibelle());
			}

			if (p.getPreAutorisation()) {
				tpePendingDisplay.setPreAutorisation("YES");

			} else {
				tpePendingDisplay.setPreAutorisation("NO");
			}

			tpePendingDisplay.setLibelle(p.getLibelle());
			tpePendingDisplay.setMotif(p.getMotif());
			if (p.getModel() == null) {
				tpePendingDisplay.setModel("");

			} else {
				PosModel posModel = posModelRepository.getOne(Integer.parseInt(p.getModel()));
				tpePendingDisplay.setModel(posModel.getLibelle());
			}
			FamillePos famillePos = famillePosRepository.getOne(p.getFamillePosCode());
			tpePendingDisplay.setFamillePosCode(famillePos.getFamillePosLibelle());
			tpePendingDisplay.setFamilyTPELabelle(famillePos.getFamillePosLibelle());
			tpePendingDisplay.setFamilleCode(p.getFamillePosCode());
			tpePendingDisplays.add(tpePendingDisplay);

		}
		tpeRequestDisplay.setPedndingTpes(tpePendingDisplays);

		System.out.println("idddd  " + id);
		/*
		 * Account a = accountRepository.findByAccountNum(null); Merchant m =
		 * merchantRepository.fi
		 */

		Account a = null;
		Merchant m = null;

		a = accountRepository.findByAccountNum(tr.getAccountNumber());
		if (a == null && m == null) {
			System.out.println("4");

			if (tr.getCommissionTypeCode() == 1) {
				CommissionTpe commission = commissionTpeRepository.findByRequestCodeComFixes(id);

				tpeRequestDisplay.setCommissionNational(commission.getValeurCommissionNationaleFix());
				tpeRequestDisplay.setCommissionInterNational(commission.getValeurCommissionInternationaleFixe());
			} else {
				List<CommissionTpe> commissionTpes = commissionTpeRepository.findByRequestCodeNational(id);

				List<CommissionTpe> CommissionTpesInter = commissionTpeRepository.findByRequestCodeInternational(id);

				tpeRequestDisplay.setCommissionTpesInter(CommissionTpesInter);
				tpeRequestDisplay.setCommissionTpes(commissionTpes);

			}
		}

		if (a != null) {
			System.out.println("1");

			m = merchantRepository.findByNumAccount(a.getAccountCode());

			if (m == null) {
				System.out.println("2");
				if (tr.getCommissionTypeCode() == 1) {
					CommissionTpe commission = commissionTpeRepository.findByRequestCodeComFixes(id);

					System.out.println("commission= " + commission.getValeurCommissionNationaleFix());
					tpeRequestDisplay.setCommissionNational(commission.getValeurCommissionNationaleFix());
					tpeRequestDisplay.setCommissionInterNational(commission.getValeurCommissionInternationaleFixe());
				} else {
					List<CommissionTpe> commissionTpes = commissionTpeRepository.findByRequestCodeNational(id);

					List<CommissionTpe> CommissionTpesInter = commissionTpeRepository
							.findByRequestCodeInternational(id);

					tpeRequestDisplay.setCommissionTpesInter(CommissionTpesInter);
					tpeRequestDisplay.setCommissionTpes(commissionTpes);

				}
			}
			if (m != null) {
				System.out.println("7");
				if (tr.getCommissionTypeCode() == 1) {
					CommissionTpe commission = commissionTpeRepository.findByRequestCodeComFixes(id);

					tpeRequestDisplay.setCommissionNational(commission.getValeurCommissionNationaleFix());
					tpeRequestDisplay.setCommissionInterNational(commission.getValeurCommissionInternationaleFixe());
				} else {
					List<CommissionTpe> commissionTpes = commissionTpeRepository.findByRequestCodeNational(id);

					List<CommissionTpe> CommissionTpesInter = commissionTpeRepository
							.findByRequestCodeInternational(id);

					tpeRequestDisplay.setCommissionTpesInter(CommissionTpesInter);
					tpeRequestDisplay.setCommissionTpes(commissionTpes);

				}
			}
			if (m != null && !m.getMerchantStatus().equals("4")) {
				System.out.println("3");

				if (tr.getCommissionTypeCode() == 1) {
					CommissionTpe commission = commissionTpeRepository.findByRequestMerchantCode(m.getMerchantCode());

					tpeRequestDisplay.setCommissionNational(commission.getValeurCommissionNationaleFix());
					tpeRequestDisplay.setCommissionInterNational(commission.getValeurCommissionInternationaleFixe());
				} else {
					List<CommissionTpe> commissionTpes = commissionTpeRepository
							.findByMerchantCodeNational(m.getMerchantCode());

					List<CommissionTpe> CommissionTpesInter = commissionTpeRepository
							.findByMerchantCodeInternational(m.getMerchantCode());

					tpeRequestDisplay.setCommissionTpesInter(CommissionTpesInter);
					tpeRequestDisplay.setCommissionTpes(commissionTpes);

				}

			}
		}

		logger.info(tpeRequestDisplay.toString());

		return ResponseEntity.ok().body(tpeRequestDisplay);
	}

//	@PostMapping("validate")
//	public ResponseEntity<String> addStatusRequest(@RequestBody ValidateRequestEdit agence) {
//		
//	/*try {
//			
//		}catch(Exception e) {
//			e.printStackTrace();
//		}*/
//		logger.info("**********begin validate BO**************");
//
//		try {
//		List<Merchant> m = merchantRepository.findlistofMerchantPos();
//		
//		int nbSeq = m.size()+1;
//		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		List<PosHistoriqueOfSerial> posHistoriqueOfSerialList = new ArrayList<PosHistoriqueOfSerial>();
//		List<PosSerialNumStates> psnsList = new ArrayList<PosSerialNumStates>();
//		List<PosStock> posStockList = new ArrayList<PosStock>();
//		List<PendingTpe> pendingTpeList = new ArrayList<PendingTpe>();
//		List<PosTerminal> posTerminalList = new ArrayList<PosTerminal>();
//		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//		System.out.println(timestamp);
//		String Strdate = sdf3.format(timestamp);
//		boolean testMerchant = false;
//		List<Merchant> merchants = new ArrayList<Merchant>();
//		int k = 0;
//		int j = 0;
//		logger.info(agence.toString());
//		Integer merchantIdCom = 0;
//		String testDone = "";
//
//		TpeRequest codeRequest = tpeRequestRepository.findByRequestCode(agence.getCodeRequest());
//		codeRequest.setDateDecision(new Date());
//		codeRequest.setCommissionTypeCode(Integer.parseInt(agence.getCommissionTypeCode()));
//		
//		List<ValidateTpeRequest> integers = agence.getListIdTpe();
//		Set<PosTerminal> posTerminals = new HashSet<>();
//		AgenceAdministration ag = agenceAdministrationRepository.findByIdAgence(codeRequest.getAgence())
//				.get();
//		for (ValidateTpeRequest i : integers) {
//
//			PendingTpe pendingTpe = pendingTpeRepository.findById(i.getCode()).get();
//			if (i.isValidate() && !i.isReject()) {
//				System.out.println("test+++++++++++");
//				testDone = "done";
//				k++;
//
//				pendingTpe.setStatus(3);
//				pendingTpe.setMotif(null);
//				pendingTpe.setCode(pendingTpe.getCode());
//				//System.out.println("mcc Code= " + i.getMccCode());
//				// pendingTpe.setMccCode(i.getMccCode());
//				if ((pendingTpe.getFamillePosCode() != 1 || pendingTpe.getFamillePosCode() != 4)
//						&& pendingTpe.getModel() != null) {
//					System.out.println("model " + pendingTpe.getModel());
//					System.out.println("model " + i.getModel() );
//					if (i.getModel() == Integer.parseInt(pendingTpe.getModel())) {
//						pendingTpe.setModel(String.valueOf(i.getModel()));
//						System.out.println("+++++++++++++test if++++++++++++++++");
//						System.out.println("pendingTpe serial num =" + pendingTpe.getSerialNum());
//						System.out.println("model =" + pendingTpe.getModel());
//						System.out.println("model i =" + i.getModel());
//						PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository
//								.getOne(pendingTpe.getSerialNum());
//						// List<PosSerialNumStates>
//						// posSerialNumStates=posSerialNumStatesRepository.getOnePosSerialNumStatesByStockByType(pendingTpe.getType());
//						System.out.println("serial num dans pendingTpe =" + posSerialNumStates.getSerialNum());
//
//						// PosModel posModel
//						// =posModelRepository.getOne(Integer.parseInt(pendingTpe.getModel()));
//						PosModel posModel = posModelRepository.getOne(i.getModel());
//
//						PosStock posStock = posStockRepository.getPosStockByMode(posModel.getModelCode());
//						System.out.println(posStock.getStockReserve());
//
//						posStock.setStockReserve(posStock.getStockReserve() - 1);
//						posStock.setStockConsome(posStock.getStockConsome() + 1);
//
//						PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
//						ph.setSerialNum(posSerialNumStates.getSerialNum());
//						ph.setDateSaisie(Strdate);
//						ph.setStatus(3);
//
//						posSerialNumStates.setStatus(3);
//
//						posHistoriqueOfSerialList.add(ph);
//						psnsList.add(posSerialNumStates);
//						posStockList.add(posStock);
//						pendingTpeList.add(pendingTpe);
//
//					}
//
//					else {
//						System.out.println("+++++++++++++test else++++++++++++++++");
//
//						System.out.println("model =" + pendingTpe.getModel());
//						System.out.println("model i =" + i.getModel());
//						PosModel posModelExist = posModelRepository.getOne(Integer.parseInt(pendingTpe.getModel()));
//						PosStock posStockExist = posStockRepository.getPosStockByMode(posModelExist.getModelCode());
//						posStockExist.setStockDisponible(posStockExist.getStockDisponible() + 1);
//						posStockExist.setStockReserve(posStockExist.getStockReserve() - 1);
//						posStockList.add(posStockExist);
//
//						// posStockRepository.save(posStockExist);
//
//						PosModel posModel = posModelRepository.getOne(i.getModel());
//						System.out.println("model " + posModel.getModelCode());
//						pendingTpe.setModel(Integer.toString(posModel.getModelCode()));
//						PosStock posStock = posStockRepository.getPosStockByMode(posModel.getModelCode());
//						System.out.println(posStock.getStockReserve());
//						posStock.setStockDisponible(posStock.getStockDisponible() - 1);
//						posStock.setStockConsome(posStock.getStockConsome() + 1);
//						// posStockRepository.save(posStock);
//						posStockList.add(posStock);
//
//						PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository
//								.getOne(pendingTpe.getSerialNum());
//						posSerialNumStates.setStatus(1);
//						PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
//						ph.setSerialNum(posSerialNumStates.getSerialNum());
//						ph.setDateSaisie(Strdate);
//						ph.setStatus(1);
//						// posHistoriqueOfSerialRepository.save(ph);
//						posHistoriqueOfSerialList.add(ph);
//
//						
//						List<PosSerialNumStates> p1 = posSerialNumStatesRepository
//								.getOnePosSerialNumStatesByStockByTypeModel(pendingTpe.getType(),
//										Integer.parseInt(pendingTpe.getModel()));
//						
//						PosSerialNumStates posSerialNumState = posSerialNumStatesRepository
//								.findById(p1.get(0).getSerialNum()).get();
//						pendingTpe.setSerialNum(posSerialNumState.getSerialNum());
//						posSerialNumState.setStatus(3);
//
//						PosHistoriqueOfSerial ph1 = new PosHistoriqueOfSerial();
//						ph1.setSerialNum(posSerialNumState.getSerialNum());
//						ph1.setDateSaisie(Strdate);
//						ph1.setStatus(3);
//						posHistoriqueOfSerialList.add(ph1);
//						psnsList.add(posSerialNumState);
//						psnsList.add(posSerialNumStates);
//						pendingTpeList.add(pendingTpe);
//
//						// posHistoriqueOfSerialRepository.save(ph1);
//
//						// posSerialNumStatesRepository.save(posSerialNumState);
//						// posSerialNumStatesRepository.save(posSerialNumStates);
//
//						// pendingTpeRepository.save(pendingTpe);
//						// modelTpeRepository.save(modelTpe);
//					}
//					posHistoriqueOfSerialRepository.saveAll(posHistoriqueOfSerialList);
//					posSerialNumStatesRepository.saveAll(psnsList);
//					posStockRepository.saveAll(posStockList);
//					pendingTpeRepository.saveAll(pendingTpeList);
//				} else {
//					pendingTpeRepository.save(pendingTpe);
//
//				}
//
//				if (!accountRepository.existsByAccountNum(codeRequest.getAccountNumber())) {
//					System.out.println("account not founded");
//
//					logger.info("account not founded");
//
//					Bank bank = bankRepository.findById(1).get();
//					String bankIdentification = bank.getIdentificationNumber();
//					if (bankIdentification.length() < 3) {
//						int bankIdIndex = bankIdentification.length();
//						while (bankIdIndex < 3) {
//							bankIdentification = bankIdentification + "0";
//							bankIdIndex++;
//						}
//					}
//					//if (merchantRepository.existsByMerchantLibelle(codeRequest.getUserName())) {
//
////						Merchant merchant = merchantRepository.findByMerchantLibelle(codeRequest.getUserName());
////						MccList mccList = mccRepository.findByMccCode(pendingTpe.getMccCode());
////						posTerminals = merchant.getPOS_TERMINAL();
////
////						PosTerminal posTerminal = new PosTerminal(pendingTpe.getLibelle(), pendingTpe.getCity(),
////								pendingTpe.getCity(), pendingTpe.getCity(), pendingTpe.getCountry(),
////								pendingTpe.getPhone(), pendingTpe.getLibelle(), pendingTpe.getPhone(), new Date(),
////								pendingTpe.getAdresse());
////						posTerminal.setTitleC(pendingTpe.getTitleC());
////						posTerminal.setNomC(pendingTpe.getNomC());
////						posTerminal.setTypeC(pendingTpe.getTypeC());
////						posTerminal.setCommune(pendingTpe.getCommune());
////						posTerminal.setDaira(pendingTpe.getDaira());
////						posTerminal.setSiteWeb(codeRequest.getSiteWeb());
////						posTerminal.setCodeZip(pendingTpe.getZipCode());
////						posTerminal.setFileTM("A");
////						posTerminal.setFamillePosCode(pendingTpe.getFamillePosCode());
////						posTerminal.setType(pendingTpe.getType());
////						System.out.println("pendingTpe.getType() "+pendingTpe.getType());
////						
////						posTerminal.setAgence(ag.getInitial());
////						if (pendingTpe.getSerialNum() != null) {
////							PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository
////									.getOne(pendingTpe.getSerialNum());
////							PosEtats posEtats = posEtatsRepository.getOne(posSerialNumStates.getStatus());
////							posTerminal.setSerialNum(pendingTpe.getSerialNum());
////						} else {
////							posTerminal.setSerialNum("");
////						}
////						posTerminal.setMontantLoyer(codeRequest.getMontantLoyer());
////						posTerminal.setStatus("DESACTIVE");
////						posTerminal.setMerchantCode(merchant);
////						posTerminal.setMccCode(mccList);
////						if (pendingTpe.getFamillePosCode() == 1) {
////							posTerminal.setTypeTerminal(null);
////						} else if (pendingTpe.getFamillePosCode() == 4) {
////							posTerminal.setTypeTerminal(null);
////						} else {
////							posTerminal.setTypeTerminal(String.valueOf(pendingTpe.getType()));
////						}
////
////						if (pendingTpe.getPreAutorisation()) {
////							PosAllowedTransConf allowedTrans = posAllowedTransactionConfRepository
////									.findByLibelle("preauth");
////							posTerminal.setPosAllowedTransConf(allowedTrans);
////						} else {
////							PosAllowedTransConf allowedTrans = posAllowedTransactionConfRepository
////									.findByLibelle("withoutPreauth");
////							posTerminal.setPosAllowedTransConf(allowedTrans);
////						}
////
////						PosLimitsConf posLimits = posLimitsConfRepository.findById(i.getPosLimits()).get();
////						posTerminal.setPosLimitsConf(posLimits);
////
////						PosServiceConf posServices = posServicesConfRepository.findById(i.getPosServices()).get();
////						posTerminal.setPosServiceConf(posServices);
////
////						PosBinConf posBin = posBinConfRepository.findById(i.getPosBin()).get();
////						posTerminal.setPbcBinConf(posBin);
////
////						// String numPos = merchant.getMerchantId() +
////						// posTerminalRepository.getNextSeriesId();
////						String seriesId = merchant.getMerchantId();
////						seriesId = seriesId.substring(0, seriesId.length() - 2);
////
////						int size = posTerminals.size() + 1;
////						String sizeString = String.valueOf(size);
////						if (sizeString.length() < 2) {
////							sizeString = "0" + sizeString;
////						}
////						String merchantId = seriesId + sizeString;
////						posTerminal.setPosNum(merchantId);
////						posTerminals.add(posTerminal);
////
////						posTerminal.setPosStatusCode(1);
////						// logger.info(posTerminal.toString());
////						posTerminalList.add(posTerminal);
////						posTerminalRepository.save(posTerminal);
////
////						merchant.setPOS_TERMINAL(posTerminals);
//////		                  merchant.setCommissionInternational(agence.getCommissionInterNational());
//////		                  merchant.setCommissionNational(agence.getCommissionNational());
////						merchant.setPhone(agence.getPhone());
////						merchant.setAddress(agence.getAdresse());
////						merchant.setCodeZip(agence.getCodeZip());
////						merchant.setCity(agence.getCity());
////						merchant.setCountry(agence.getCountry());
////						merchant.setOffshore(agence.isOffshore());
////						merchant.setCommissionType(Integer.parseInt(agence.getCommissionTypeCode()));
////						// logger.info(merchant.toString());
////						merchant.setEmail(codeRequest.getEmail());
////						merchant.setDaira(codeRequest.getDaira());
////						merchant.setCommune(codeRequest.getCommune());
////						merchant.setCodeWilaya(codeRequest.getCodeWilaya());
////						
////						merchant.setNif(codeRequest.getNif());
////						merchant.setRso(codeRequest.getRso());
////						merchant.setRc(codeRequest.getRc());						
////						merchant.setRevenue(codeRequest.getRevenue());		
////						merchant.setSiteWeb(codeRequest.getSiteWeb());	
////						
////						/*DateFormat formatter2 = new SimpleDateFormat("yy");
////						String ans = formatter2.format(new Date());
////						
////						String nbSequentielle = String.format("%05d", nbSeq);
////						String idContrat ="035"+ag.getInitial()+ans+nbSequentielle;
////						merchant.setIdContrat(idContrat);	*/
////						
////						merchant = merchantRepository.save(merchant);
////						merchantIdCom = merchant.getMerchantCode();
////					} else {
//						testMerchant=true;
//						Long seriesId = merchantRepository.getNextSeriesId();
//						String seriesString = Long.toString(seriesId);
//						if (seriesString.length() < 5) {
//							int index = seriesString.length();
//							while (index < 5) {
//								seriesString = "0" + seriesString;
//								index++;
//							}
//						}
//						String merchantId = bankIdentification + seriesString + "00";
//
//						Merchant merchant = new Merchant("1", merchantId, codeRequest.getUserName(),
//								pendingTpe.getCity(), pendingTpe.getCountry(), pendingTpe.getZipCode(),
//								pendingTpe.getPhone(), new Date(), pendingTpe.getAdresse());
//						
//						DateFormat formatter2 = new SimpleDateFormat("yy");
//						String ans = formatter2.format(new Date());
//						
//						
//						String nbSequentielle = String.format("%05d", nbSeq);
//						String idContrat ="035"+ag.getInitial()+ans+nbSequentielle;
//						merchant.setIdContrat(idContrat);	
//						
//					
//
////		                  merchant.setCommissionInternational(agence.getCommissionInterNational());
////		                  merchant.setCommissionNational(agence.getCommissionNational());
//						merchant = merchantRepository.save(merchant);
//						
//						/******************** historiqueCommercant **********************/
//						HistoriqueCommercant historiqueCommercant = new HistoriqueCommercant();
//						historiqueCommercant.setDateStatu(new Date());
//						historiqueCommercant.setStatut("Commercant Créé");
//						historiqueCommercant.setMerchantCode(merchant.getMerchantCode());
//						historiqueCommercantRepository.save(historiqueCommercant);
//						/******************** historiqueCommercant **********************/
//						String rib = codeRequest.getAccountNumber();
//
//						CurrencyFSBK currencyFSBK = currencyFSBKRepository.getOne(codeRequest.getDevise());
//						String devise = currencyFSBK.getCodeDevise();
//
//						Account account = new Account(null, rib, 1, Integer.toString(merchant.getMerchantCode()), rib,
//								codeRequest.getUserName(), BigInteger.valueOf(0), BigInteger.valueOf(0),
//								BigInteger.valueOf(0), BigInteger.valueOf(0), devise, new Date(), 5,
//								codeRequest.getAgence());
//
//						accountRepository.save(account);
//
//						MccList mccList = mccRepository.findByMccCode(pendingTpe.getMccCode());
//
//						PosTerminal posTerminal = new PosTerminal(pendingTpe.getLibelle(), pendingTpe.getCity(),
//								pendingTpe.getCity(), pendingTpe.getCity(), pendingTpe.getCountry(),
//								pendingTpe.getPhone(), pendingTpe.getLibelle(), pendingTpe.getPhone(), new Date(),
//								pendingTpe.getAdresse());
//						posTerminal.setTitleC(pendingTpe.getTitleC());
//						posTerminal.setNomC(pendingTpe.getNomC());
//						posTerminal.setTypeC(pendingTpe.getTypeC());
//						posTerminal.setCommune(pendingTpe.getCommune());
//						posTerminal.setSiteWeb(codeRequest.getSiteWeb());
//						posTerminal.setDaira(pendingTpe.getDaira());
//						posTerminal.setCodeZip(pendingTpe.getZipCode());
//						posTerminal.setFileTM("A");
//						posTerminal.setMontantLoyer(codeRequest.getMontantLoyer());
//
//						posTerminal.setFamillePosCode(pendingTpe.getFamillePosCode());
//						posTerminal.setType(pendingTpe.getType());
//						System.out.println("pendingTpe.getType() "+pendingTpe.getType());
//						
//						posTerminal.setAgence(ag.getInitial());
//						if (pendingTpe.getSerialNum() != null) {
//							PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository
//									.getOne(pendingTpe.getSerialNum());
//							PosEtats posEtats = posEtatsRepository.getOne(posSerialNumStates.getStatus());
//							posTerminal.setSerialNum(pendingTpe.getSerialNum());
//						} else {
//							posTerminal.setSerialNum("");
//						}
//						if (pendingTpe.getFamillePosCode() == 1) {
//							posTerminal.setTypeTerminal(null);
//						} else if (pendingTpe.getFamillePosCode() == 4) {
//							posTerminal.setTypeTerminal(null);
//						} else {
//							posTerminal.setTypeTerminal(String.valueOf(pendingTpe.getType()));
//						}
//						// PosSerialNumStates posSerialNumStates
//						// =posSerialNumStatesRepository.getOne(pendingTpe.getSerialNum());
//						// PosEtats posEtats =
//						// posEtatsRepository.getOne(posSerialNumStates.getStatus());
//
//						posTerminal.setStatus("DESACTIVE");
//						// posTerminal.setSerialNum(pendingTpe.getSerialNum());
//						posTerminal.setMerchantCode(merchant);
//						posTerminal.setMccCode(mccList);
//
//						if (pendingTpe.getPreAutorisation()) {
//							PosAllowedTransConf allowedTrans = posAllowedTransactionConfRepository
//									.findByLibelle("preauth");
//							posTerminal.setPosAllowedTransConf(allowedTrans);
//						} else {
//							PosAllowedTransConf allowedTrans = posAllowedTransactionConfRepository
//									.findByLibelle("withoutPreauth");
//							posTerminal.setPosAllowedTransConf(allowedTrans);
//						}
//
//						PosLimitsConf posLimits = posLimitsConfRepository.findById(i.getPosLimits()).get();
//						posTerminal.setPosLimitsConf(posLimits);
//
//						PosServiceConf posServices = posServicesConfRepository.findById(i.getPosServices()).get();
//						posTerminal.setPosServiceConf(posServices);
//
//						PosBinConf posBin = posBinConfRepository.findById(i.getPosBin()).get();
//						posTerminal.setPbcBinConf(posBin);
//
//						// String numPos = merchant.getMerchantId() +
//						// posTerminalRepository.getNextSeriesId();
//						int size = posTerminals.size() + 1;
//						String sizeString = String.valueOf(size);
//						if (sizeString.length() < 2) {
//							sizeString = "0" + sizeString;
//						}
//						posTerminal.setPosNum(bankIdentification + seriesString + sizeString);
//						posTerminals.add(posTerminal);
//						// logger.info(posTerminal.toString());
//						posTerminal.setPosStatusCode(1);
//						posTerminalList.add(posTerminal);
//						posTerminalRepository.save(posTerminal);
//						merchant.setPOS_TERMINAL(posTerminals);
//						merchant.setAccount(account.getAccountCode());
//						merchant.setPhone(agence.getPhone());
//						merchant.setAddress(agence.getAdresse());
//						merchant.setCodeZip(agence.getCodeZip());
//						merchant.setCity(agence.getCity());
//						merchant.setCountry(agence.getCountry());
//						merchant.setOffshore(agence.isOffshore());
//						merchant.setCommissionType(Integer.parseInt(agence.getCommissionTypeCode()));
//						merchant.setEmail(codeRequest.getEmail());
//						merchant.setDaira(codeRequest.getDaira());
//						merchant.setCommune(codeRequest.getCommune());
//						merchant.setCodeWilaya(codeRequest.getCodeWilaya());
//						merchant.setNif(codeRequest.getNif());
//						merchant.setRso(codeRequest.getRso());
//						merchant.setRc(codeRequest.getRc());						
//						merchant.setRevenue(codeRequest.getRevenue());
//						merchant.setSiteWeb(codeRequest.getSiteWeb());	
//
//						
//						merchant.setIdContrat(idContrat);	
//
//						merchant = merchantRepository.save(merchant);
//						
//						merchantIdCom = merchant.getMerchantCode();
//
//					//}
//				} else {
//					logger.info("account founded");
//					System.out.println("account founded");
//					 System.out.println("codeRequest.getAccountNumber() "+codeRequest.getAccountNumber());
//					Account account = accountRepository.findByAccountNum(codeRequest.getAccountNumber());
//					 System.out.println("account "+account);
//					 System.out.println("account "+account.getAccountCode());
//					 Merchant merchant = null;
//					 if(account!=null) {
//							merchant = merchantRepository.findByNumAccount(account.getAccountCode());
//
//					 }
//				    System.out.println("merchant "+merchant);
//					if (merchant == null || merchant.getMerchantStatus().equals("4")) {
//
//						Long seriesId = merchantRepository.getNextSeriesId();
//						String seriesString = Long.toString(seriesId);
//						if (seriesString.length() < 5) {
//							int index = seriesString.length();
//							while (index < 5) {
//								seriesString = "0" + seriesString;
//								index++;
//							}
//						}
//
//						Bank bank = bankRepository.findById(1).get();
//					    System.out.println("bank "+bank);
//						String bankIdentification = bank.getIdentificationNumber();
//						String merchantId = bankIdentification + seriesString + "00";
//
//						merchant = new Merchant("1", merchantId, codeRequest.getUserName(), pendingTpe.getCity(),
//								pendingTpe.getCountry(), pendingTpe.getZipCode(), pendingTpe.getPhone(), new Date(),
//								pendingTpe.getAdresse());
//						
//						  System.out.println("merchant "+merchant);
//						merchant.setAccount(account.getAccountCode());
//						merchant.setEmail(codeRequest.getEmail());
//						merchant.setDaira(codeRequest.getDaira());
//						merchant.setCommune(codeRequest.getCommune());
//						merchant.setCodeWilaya(codeRequest.getCodeWilaya());
//						merchant.setNif(codeRequest.getNif());
//						merchant.setRso(codeRequest.getRso());
//						merchant.setRc(codeRequest.getRc());						
//						merchant.setRevenue(codeRequest.getRevenue());
//						merchant.setSiteWeb(codeRequest.getSiteWeb());		
//						DateFormat formatter2 = new SimpleDateFormat("yy");
//						String ans = formatter2.format(new Date());
//					
//						String nbSequentielle = String.format("%05d", nbSeq);
//						String idContrat ="035"+ag.getInitial()+ans+nbSequentielle;
//						merchant.setIdContrat(idContrat);	
//
//						merchant = merchantRepository.save(merchant);
//						/******************** historiqueCommercant **********************/
//						HistoriqueCommercant historiqueCommercant = new HistoriqueCommercant();
//						historiqueCommercant.setDateStatu(new Date());
//						historiqueCommercant.setStatut("Commercant Créé");
//						historiqueCommercant.setMerchantCode(merchant.getMerchantCode());
//						historiqueCommercantRepository.save(historiqueCommercant);
//						/******************** historiqueCommercant **********************/
//						  System.out.println("merchant 1"+merchant.getMerchantCode());
//					}
//				    
//
//					posTerminals = merchant.getPOS_TERMINAL();
//					System.out.println("posTerminals "+posTerminals);
//					MccList mccList = mccRepository.findByMccCode(pendingTpe.getMccCode());
//					System.out.println("mccList "+mccList);
//					PosTerminal posTerminal = new PosTerminal(pendingTpe.getLibelle(), pendingTpe.getCity(),
//							pendingTpe.getCity(), pendingTpe.getCity(), pendingTpe.getCountry(), pendingTpe.getPhone(),
//							pendingTpe.getLibelle(), pendingTpe.getPhone(), new Date(), pendingTpe.getAdresse());
//					///////// ifffffffffffffffff/////////////
//					posTerminal.setTitleC(pendingTpe.getTitleC());
//					posTerminal.setNomC(pendingTpe.getNomC());
//					posTerminal.setTypeC(pendingTpe.getTypeC());
//					posTerminal.setCommune(pendingTpe.getCommune());
//					posTerminal.setSiteWeb(codeRequest.getSiteWeb());
//					posTerminal.setDaira(pendingTpe.getDaira());
//					posTerminal.setCodeZip(pendingTpe.getZipCode());
//					posTerminal.setFileTM("A");
//					posTerminal.setMontantLoyer(codeRequest.getMontantLoyer());
//
//					System.out.println("pendingTpe.getFamillePosCode() "+pendingTpe.getFamillePosCode());
//					posTerminal.setFamillePosCode(pendingTpe.getFamillePosCode());
//					posTerminal.setType(pendingTpe.getType());
//					System.out.println("pendingTpe.getType() "+pendingTpe.getType());
//					
//					
//					posTerminal.setAgence(ag.getInitial());
//					if (pendingTpe.getSerialNum() != null) {
//						PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository
//								.getOne(pendingTpe.getSerialNum());
//						PosEtats posEtats = posEtatsRepository.getOne(posSerialNumStates.getStatus());
//						posTerminal.setSerialNum(pendingTpe.getSerialNum());
//					} else {
//						posTerminal.setSerialNum("");
//					}
//					if (pendingTpe.getFamillePosCode() == 1) {
//						posTerminal.setTypeTerminal(null);
//					} else if (pendingTpe.getFamillePosCode() == 4) {
//						posTerminal.setTypeTerminal(null);
//					} else {
//						posTerminal.setTypeTerminal(String.valueOf(pendingTpe.getType()));
//					}
//					posTerminal.setStatus("DESACTIVE");
//
//					posTerminal.setMerchantCode(merchant);
//					posTerminal.setMccCode(mccList);
//					if (pendingTpe.getPreAutorisation()) {
//						PosAllowedTransConf allowedTrans = posAllowedTransactionConfRepository.findByLibelle("preauth");
//
//						posTerminal.setPosAllowedTransConf(allowedTrans);
//					} else {
//						PosAllowedTransConf allowedTrans = posAllowedTransactionConfRepository
//								.findByLibelle("withoutPreauth");
//
//						posTerminal.setPosAllowedTransConf(allowedTrans);
//					}
//					PosLimitsConf posLimits = posLimitsConfRepository.findById(i.getPosLimits()).get();
//
//					posTerminal.setPosLimitsConf(posLimits);
//
//					PosServiceConf posServices = posServicesConfRepository.findById(i.getPosServices()).get();
//					posTerminal.setPosServiceConf(posServices);
//
//					PosBinConf posBin = posBinConfRepository.findById(i.getPosBin()).get();
//					posTerminal.setPbcBinConf(posBin);
//
//					// String numPos = merchant.getMerchantId() +
//					// posTerminalRepository.getNextSeriesId();
//					String seriesId = merchant.getMerchantId();
//					seriesId = seriesId.substring(0, seriesId.length() - 2);
//
//					int size = posTerminals.size() + 1;
//					String sizeString = String.valueOf(size);
//					if (sizeString.length() < 2) {
//						sizeString = "0" + sizeString;
//					}
//					String merchantId = seriesId + sizeString;
//					posTerminal.setPosNum(merchantId);
//					posTerminals.add(posTerminal);
//					posTerminal.setPosStatusCode(1);
//					posTerminalList.add(posTerminal);
//					posTerminalRepository.save(posTerminal);
//
//					merchant.setPOS_TERMINAL(posTerminals);
////		               merchant.setCommissionInternational(agence.getCommissionInterNational());
////		               merchant.setCommissionNational(agence.getCommissionNational());
//					merchant.setPhone(agence.getPhone());
//					merchant.setAddress(agence.getAdresse());
//					merchant.setCodeZip(agence.getCodeZip());
//					merchant.setCity(agence.getCity());
//					merchant.setCountry(agence.getCountry());
//					merchant.setOffshore(agence.isOffshore());
//					merchant.setCommissionType(Integer.parseInt(agence.getCommissionTypeCode()));
//					merchant.setEmail(codeRequest.getEmail());
//					merchant.setDaira(codeRequest.getDaira());
//					merchant.setCommune(codeRequest.getCommune());
//					merchant.setCodeWilaya(codeRequest.getCodeWilaya());
//					merchant.setNif(codeRequest.getNif());
//					merchant.setRso(codeRequest.getRso());
//					merchant.setRc(codeRequest.getRc());						
//					merchant.setRevenue(codeRequest.getRevenue());
//					merchant.setSiteWeb(codeRequest.getSiteWeb());	
//
//					/*DateFormat formatter2 = new SimpleDateFormat("yy");
//					String ans = formatter2.format(new Date());
//					
//					String nbSequentielle = String.format("%05d", nbSeq);
//					String idContrat ="035"+ag.getInitial()+ans+nbSequentielle;
//					merchant.setIdContrat(idContrat);*/
//					merchant = merchantRepository.save(merchant);
//					
//					
//					merchantIdCom = merchant.getMerchantCode();
//				}
//				
//				
//				
//
//			} else {
//				System.out.println("test+++++++++++ else " + pendingTpe.getModel());
//
//				if (pendingTpe.getModel() != null) {
//					PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository
//							.getOne(pendingTpe.getSerialNum());
//					PosModel posModel = posModelRepository.getOne(Integer.parseInt(pendingTpe.getModel()));
//					System.out.println(posModel.getModelCode());
//
//					PosStock posStock = posStockRepository.getPosStockByMode(posModel.getModelCode());
//					System.out.println(posStock.getStockReserve());
//
//					posStock.setStockReserve(posStock.getStockReserve() - 1);
//					posStock.setStockDisponible(posStock.getStockDisponible() + 1);
//
//					posSerialNumStates.setStatus(1);
//
//					posStockRepository.save(posStock);
//					posSerialNumStatesRepository.save(posSerialNumStates);
//				}
//
//				j++;
//				pendingTpe.setStatus(2);
//				pendingTpe.setMotif(i.getMotif());
//				pendingTpe.setModel(null);
//				pendingTpeRepository.save(pendingTpe);
//
//			}
//			
//			System.out.println("posTerminalList" +posTerminalList.size());
//			validationPOS(posTerminalList);
//		}
//		//posTerminalList =posTerminalRepository.saveAll(posTerminalList);
//		
//		
//		if (testDone.equals("done")) {
//			codeRequest.setStatus(3);
//			/******************** Request historique **********************/
//			HistoriqueRequestPos historiqueRequestPos = new HistoriqueRequestPos();
//			historiqueRequestPos.setDateStatu(new Date());
//			historiqueRequestPos.setStatut("Demande approuvée");
//			historiqueRequestPos.setRequestCode(codeRequest.getRequestCode());
//			historiqueRequestPos.setOperateur("BackOffice");
//			historiqueRequestPosRepository.save(historiqueRequestPos);
//			/******************** Request historique **********************/
//			tpeRequestRepository.save(codeRequest);
//		} else if (integers.size() == k) {
//			codeRequest.setStatus(3);
//			/******************** Request historique **********************/
//			HistoriqueRequestPos historiqueRequestPos = new HistoriqueRequestPos();
//			historiqueRequestPos.setDateStatu(new Date());
//			historiqueRequestPos.setStatut("Demande approuvée");
//			historiqueRequestPos.setRequestCode(codeRequest.getRequestCode());
//			historiqueRequestPos.setOperateur("BackOffice");
//			historiqueRequestPosRepository.save(historiqueRequestPos);
//			/******************** Request historique **********************/
//			tpeRequestRepository.save(codeRequest);
//		} else if (integers.size() == j) {
//			
//			codeRequest.setStatus(2);
//			/******************** Request historique **********************/
//			HistoriqueRequestPos historiqueRequestPos = new HistoriqueRequestPos();
//			historiqueRequestPos.setDateStatu(new Date());
//			historiqueRequestPos.setStatut("Demande rejetée");
//			historiqueRequestPos.setRequestCode(codeRequest.getRequestCode());
//			historiqueRequestPos.setOperateur("BackOffice");
//			historiqueRequestPosRepository.save(historiqueRequestPos);
//			/******************** Request historique **********************/
//			tpeRequestRepository.save(codeRequest);
//		} else {
//			codeRequest.setStatus(1);
//			tpeRequestRepository.save(codeRequest);
//		}
//		List<CommissionTpe> cTpe = commissionTpeRepository.findByRequestCode(codeRequest.getRequestCode());
//		List<CommissionTpe> listCom = new ArrayList<>();
//		for (CommissionTpe item : cTpe) {
//			CommissionTpe c = commissionTpeRepository.getOne(item.getIdCommission());
//			c.setMerchantCode(merchantIdCom);
//			listCom.add(c);
//		}
//		commissionTpeRepository.saveAll(listCom);
//		
//		try {
//			String nomClient = codeRequest.getUserName();
//	         String numCompte = "0"+codeRequest.getAccountNumber();
//		      myEmailService.sendOtpMessage("Monetique@fransabank.dz", "Demande d'acquisition TPE ",
//		    		  			"Bonjour,\n\n"
//		    		            + "Nous vous informons que la demande d’acquisition TPE du client \"" + nomClient + "\" domicilié sous le N° de compte \"" + numCompte + "\" a été " + "Validée" + " par le chef d’agence.\n\n"
//		    		            + "Cordialement,\n");
//		    } catch (Exception e) {
//				String stackTrace=Throwables.getStackTraceAsString ( e ) ;
//				
//		    	logger.info(stackTrace);
//		      return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
//		    }
//		
//		try {
//			logger.info("**********behin mail validate BO**************");
//
//			String nomClient = codeRequest.getUserName();
//	        String numCompte = "0"+codeRequest.getAccountNumber();
//			myEmailService.sendOtpMessage(codeRequest.getEmailCharge(), "Demande d'acquisition TPE",
//							"Bonjour,\n\n"
//	    		            + "Nous vous informons que la demande d’acquisition TPE du client \"" + nomClient + "\" domicilié sous le N° de compte \"" + numCompte + "\" a été " + "Validée" + " par le chef d’agence.\n\n"
//	    		            + "Cordialement,\n");
//			logger.info("**********end mail validate BO**************");
//
//			} catch (Exception e) {
//			String stackTrace=Throwables.getStackTraceAsString ( e ) ;
//			logger.info(stackTrace);
//			 return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
//			}
//	}catch(Exception e) {
//			e.printStackTrace();
//		}
//		logger.info("**********end validate BO**************");
//
//		
//		return ResponseEntity.accepted().body(gson.toJson("POS validated successfully!"));
//
//	}

	@GetMapping("getMerchantInfoByAccount/{accountNum}")
	public ResponseEntity<MerchantByAccNum> getMerchantInfoByAccount(
			@PathVariable(value = "accountNum") String accountNum) {

		MerchantByAccNum merchant = new MerchantByAccNum();
		Account account = accountRepository.findByAccountNum(accountNum);
		if (account != null) {
			Merchant findedMerchant = merchantRepository.findByAccount(account.getAccountCode());
			if (findedMerchant != null) {

				merchant.setAddress(findedMerchant.getAddress());
				merchant.setCity(findedMerchant.getCity());
				merchant.setCountry(findedMerchant.getCountry());
				merchant.setInternationalCommission(findedMerchant.getCommissionInternational());
				merchant.setNationalCommission(findedMerchant.getCommissionNational());
				merchant.setPhone(findedMerchant.getPhone());
				merchant.setUsername(findedMerchant.getMerchantLibelle());
				merchant.setZipCode(findedMerchant.getCodeZip());
			}

		}
		return ResponseEntity.ok(merchant);

	}

	@PutMapping("updateRequest/{id}")
	public ResponseEntity<TpeRequest> getCommission(@PathVariable(value = "id") Integer type,
			@RequestBody UpdateTpeRequest updateRequest) {

		TpeRequest bin = tpeRequestRepository.findById(type).get();
		Set<TpePendingDisplay> tpePendingDisplays = updateRequest.getPedndingTpes();
		for (TpePendingDisplay tpd : tpePendingDisplays) {
			PendingTpe pedndingTpe = pendingTpeRepository.findById(tpd.getCode()).get();
			MccList mccList = mccRepository.findByMccListLibelle(tpd.getMccCode());
			pedndingTpe.setMccCode(mccList.getMccCode());
			pedndingTpe.setPreAutorisation(tpd.getPreAutorisation().equals("YES"));
			switch (tpd.getStatus()) {
			case "PENDING":
				pedndingTpe.setStatus(1);

				break;
			case "OK":
				pedndingTpe.setStatus(3);
				break;
			default:
				pedndingTpe.setStatus(2);
			}
			List<ModelTpe> modelTpes = modelTpeRepository.findAll();
			for (ModelTpe mt : modelTpes) {
				if (tpd.getType().equals(mt.getLibelle())) {
					pedndingTpe.setType(mt.getModelTpe());
				}
			}
			pedndingTpe.setCity(tpd.getCity());
			pedndingTpe.setCountry(tpd.getCountry());
			pedndingTpe.setMotif(tpd.getMotif());
			pedndingTpe.setZipCode(tpd.getZipCode());
			pedndingTpe.setLibelle(tpd.getLibelle());
			pedndingTpe.setPhone(tpd.getPhone());

			pendingTpeRepository.save(pedndingTpe);
		}
		bin.setUserName(updateRequest.getUserName());

		bin.setAgence(updateRequest.getAgence());
		bin.setCity(updateRequest.getCity());
		bin.setCodeZip(updateRequest.getCountry());
		bin.setCommissionInterNational(updateRequest.getCommissionInterNational());
		bin.setCommissionNational(updateRequest.getCommissionNational());
		bin.setPhone(updateRequest.getPhone());
		bin.setNombreTPE(updateRequest.getNombreTPE());
		bin.setAdresse(updateRequest.getAdresse());
		final TpeRequest updatedEmployee = tpeRequestRepository.save(bin);
		return ResponseEntity.ok(updatedEmployee);
	}

	@PostMapping("getListTpeRequest")
	public List<TpeRequestDisplay> getListTpeRequest(@RequestBody RequestTpeFilter requestTpeFilter) {

		List<TpeRequest> tpeRequest = tpeRequestRepository.findAllTpeRequestLevel3();
		List<TpeRequest> result = new ArrayList<>();

		List<TpeRequestDisplay> tpeRequestDisplays = new ArrayList<>();

		Calendar cal = Calendar.getInstance();
		cal.setTime(requestTpeFilter.getDateFin());
		cal.add(Calendar.DATE, 1);

		tpeRequest = tpeRequest.stream()

				.filter(e -> Strings.isNullOrEmpty(requestTpeFilter.getAgence())
						|| String.valueOf(e.getAgence()).equals(requestTpeFilter.getAgence()))
				.filter(e -> Strings.isNullOrEmpty(requestTpeFilter.getNumCompte())
						|| e.getAccountNumber().equals(requestTpeFilter.getNumCompte()))
				.filter(e -> Strings.isNullOrEmpty(requestTpeFilter.getStatus())
						|| String.valueOf(e.getStatus()).equals(requestTpeFilter.getStatus()))
				.filter(e -> e.getDateCreation().after(requestTpeFilter.getDateDebut())
						&& e.getDateCreation().before(cal.getTime()))

				.collect(Collectors.toList());
		for (TpeRequest tr : tpeRequest) {
			TpeRequestDisplay tpeRequestDisplay = new TpeRequestDisplay();
			tpeRequestDisplay.setAccountNumber(tr.getAccountNumber());
			/// Agence agence = agenceRepository.findById(tr.getAgence()).get();
			AgenceAdministration agence = agenceAdministrationRepository.findByIdAgence(tr.getAgence()).get();

			tpeRequestDisplay.setAgence(agence.getLibelle());
			tpeRequestDisplay.setCity(tr.getCity());
			tpeRequestDisplay.setCodeZip(tr.getCodeZip());
			tpeRequestDisplay.setCommissionInterNational(tr.getCommissionInterNational());
			tpeRequestDisplay.setCommissionNational(tr.getCommissionNational());
			tpeRequestDisplay.setCountry(tr.getCountry());
			TpeRequestStatus tpeRequestStat = tpeRequestStatusRepository.findByStatusCode(tr.getStatus());
			tpeRequestDisplay.setStatus(tpeRequestStat.getLibelleFr());
			tpeRequestDisplay.setDateCreation(tr.getDateCreation());
			tpeRequestDisplay.setDateDecision(tr.getDateDecision());
			tpeRequestDisplay.setNombreTPE(tr.getNombreTPE());
			tpeRequestDisplay.setCodeZip(tr.getCodeZip());
			tpeRequestDisplay.setPhone(tr.getPhone());
			tpeRequestDisplay.setRequestCode(tr.getRequestCode());
			tpeRequestDisplay.setUserName(tr.getUserName());
			tpeRequestDisplay.setStatuContrat(tr.getStatuContrat());
			tpeRequestDisplays.add(tpeRequestDisplay);
		}
		return tpeRequestDisplays;
	}

	@PutMapping("updateMcc/{id}")
	public ResponseEntity<String> updateMcc(@PathVariable(value = "id") Integer type,
			@RequestBody MccList updateRequest) {

		MccList bin = mccRepository.findByMccCode(type);
		modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		modelMapper.map(updateRequest, bin);
		mccRepository.save(bin);

		return ResponseEntity.ok().body(gson.toJson("MCC updated successfully!"));

	}

	@DeleteMapping("deletMcc/{id}")
	public ResponseEntity<String> deletMcc(@PathVariable(value = "id") Integer type) {

		MccList bin = mccRepository.findById(String.valueOf(type)).get();
		mccRepository.delete(bin);

		return ResponseEntity.accepted().body(gson.toJson("MCC deleted successfully!"));

	}

	@GetMapping("ListAccount")
	public List<Account> getListAccount() {
		return accountRepository.findAll();
	}

	@PostMapping("merchants")
	public Page<MerchantListDisplay> getAllMerchant(@RequestBody RequestFilterMerchant requestFilterMerchant,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size

	) {

		Page<Merchant> merchants = null;
		if (requestFilterMerchant.getAccountNumber().equals("") && requestFilterMerchant.getAgence().equals("")) {
			merchants = merchantRepository.getPageMerchant(PageRequest.of(page, size),
					requestFilterMerchant.getNameMerchant());

		}
		if (!requestFilterMerchant.getAccountNumber().equals("") && requestFilterMerchant.getAgence().equals("")) {
			merchants = merchantRepository.getPageMerchantAccountNum(PageRequest.of(page, size),
					requestFilterMerchant.getNameMerchant(),
					Integer.parseInt(requestFilterMerchant.getAccountNumber()));
		}
		if (requestFilterMerchant.getAccountNumber().equals("") && !requestFilterMerchant.getAgence().equals("")) {

			merchants = merchantRepository.getPageMerchantAgence(PageRequest.of(page, size),
					requestFilterMerchant.getNameMerchant(), Integer.parseInt(requestFilterMerchant.getAgence()));
		}
		if (!requestFilterMerchant.getAccountNumber().equals("") && !requestFilterMerchant.getAgence().equals("")) {

			merchants = merchantRepository.getPageMerchant(PageRequest.of(page, size),
					requestFilterMerchant.getNameMerchant(), Integer.parseInt(requestFilterMerchant.getAccountNumber()),
					Integer.parseInt(requestFilterMerchant.getAgence()));

		}

		List<MerchantListDisplay> merchantListDisplays = new ArrayList<>();
		for (Merchant m : merchants) {
			Account account = null;
			if (m.getAccount() != 0) {
				// account = accountRepository.findByAccountCode(m.getAccount());
				Optional<Account> getAccount = accountRepository.findByAccountCode(m.getAccount());
				// .get();
				if (getAccount.isPresent()) {
					account = getAccount.get();
				}
			}

			List<PosTerminal> posTerminals = posTerminalRepository.findAllByMerchantCode(m.getMerchantCode());
			MerchantListDisplay merchantListDisplay = new MerchantListDisplay(m.getMerchantCode(),
					m.getMerchantLibelle(), "1000", m.getCreationDate(), m.getCommissionInternational(),
					m.getCommissionNational(), String.valueOf(posTerminals.size()), m.getMerchantId(), m.getOffshore(),
					m.getCommissionType());
			merchantListDisplay.setMerchantstatus(Integer.parseInt(m.getMerchantStatus()));
			merchantListDisplay.setIdContrat(m.getIdContrat());
			merchantListDisplay.setDetailResiliation(m.getDetailResiliation());
			MerchantStatus ms = merchantStatusRepository.getOne(Integer.parseInt(m.getMerchantStatus()));
			merchantListDisplay.setEtatMerchant(ms.getLibelleFr());
			if (account != null)
				merchantListDisplay.setAccountNumber(account.getAccountNum());
			merchantListDisplays.add(merchantListDisplay);

		}
		Page<MerchantListDisplay> pageDisplay = new PageImpl<>(merchantListDisplays, PageRequest.of(page, size),
				merchants.getTotalElements());
		return pageDisplay;
	}

	@PostMapping("merchantsResiliation")
	public Page<MerchantListDisplay> merchantsResiliation(@RequestBody RequestFilterMerchant requestFilterMerchant,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size

	) {

		Page<Merchant> merchants = null;
		if (requestFilterMerchant.getAccountNumber().equals("")) {
			merchants = merchantRepository.getPageMerchantResiliation(PageRequest.of(page, size),
					requestFilterMerchant.getNameMerchant());

		} else {
			merchants = merchantRepository.getPageMerchantResiliation(PageRequest.of(page, size),
					requestFilterMerchant.getNameMerchant(),
					Integer.parseInt(requestFilterMerchant.getAccountNumber()));

		}

		List<MerchantListDisplay> merchantListDisplays = new ArrayList<>();
		for (Merchant m : merchants) {
			Account account = null;
			if (m.getAccount() != 0) {
				// account = accountRepository.findByAccountCode(m.getAccount());
				Optional<Account> getAccount = accountRepository.findByAccountCode(m.getAccount());
				// .get();
				if (getAccount.isPresent()) {
					account = getAccount.get();
				}
			}

			List<PosTerminal> posTerminals = posTerminalRepository.findAllByMerchantCode(m.getMerchantCode());
			MerchantListDisplay merchantListDisplay = new MerchantListDisplay(m.getMerchantCode(),
					m.getMerchantLibelle(), "1000", m.getCreationDate(), m.getCommissionInternational(),
					m.getCommissionNational(), String.valueOf(posTerminals.size()), m.getMerchantId(), m.getOffshore(),
					m.getCommissionType());
			merchantListDisplay.setIdContrat(m.getIdContrat());
			merchantListDisplay.setMerchantstatus(Integer.parseInt(m.getMerchantStatus()));
			merchantListDisplay.setDetailResiliation(m.getDetailResiliation());
			MerchantStatus ms = merchantStatusRepository.getOne(Integer.parseInt(m.getMerchantStatus()));
			merchantListDisplay.setEtatMerchant(ms.getLibelleFr());
			if (account != null)
				merchantListDisplay.setAccountNumber(account.getAccountNum());
			merchantListDisplays.add(merchantListDisplay);

		}
		Page<MerchantListDisplay> pageDisplay = new PageImpl<>(merchantListDisplays, PageRequest.of(page, size),
				merchants.getTotalElements());
		return pageDisplay;
	}

	@GetMapping("Allmerchants")
	public List<MerchantListDisplay> getAllMerchantList() {

		List<Merchant> merchants = merchantRepository.findAll();
		List<MerchantListDisplay> merchantListDisplays = new ArrayList<>();
		for (Merchant m : merchants) {
			Account account = null;
			if (m.getAccount() != 0) {
				// account = accountRepository.findByAccountCode(m.getAccount());
				Optional<Account> getAccount = accountRepository.findByAccountCode(m.getAccount());
				// .get();
				if (getAccount.isPresent()) {
					account = getAccount.get();
				}
			}

			List<PosTerminal> posTerminals = posTerminalRepository.findAllByMerchantCode(m.getMerchantCode());
			MerchantListDisplay merchantListDisplay = new MerchantListDisplay(m.getMerchantCode(),
					m.getMerchantLibelle(), "1000", m.getCreationDate(), m.getCommissionInternational(),
					m.getCommissionNational(), String.valueOf(posTerminals.size()), m.getMerchantId(), m.getOffshore(),
					m.getCommissionType());
			if (account != null)
				merchantListDisplay.setAccountNumber(account.getAccountNum());
			merchantListDisplays.add(merchantListDisplay);

		}
		return merchantListDisplays;
	}

	@GetMapping("getPOS/{id}")
	public ResponseEntity<Set<TpePendingDisplay>> getPOS(@PathVariable(value = "id") String id) {

		Merchant tpeRequest = merchantRepository.findByMerchantCode(Integer.parseInt(id));

		List<PosTerminal> posTerminals = posTerminalRepository.findAllByMerchantCode(tpeRequest.getMerchantCode());
		Set<TpePendingDisplay> tpePendingDisplays = new HashSet<>();

		for (PosTerminal p : posTerminals) {
			TpePendingDisplay tpePendingDisplay = new TpePendingDisplay();

			tpePendingDisplay.setCode(p.getPosCode());
			if (!p.getSerialNum().equals("")) {
				PosSerialNumStates psn = posSerialNumStatesRepository.getOne(p.getSerialNum());
				PendingTpe pendingTpe = pendingTpeRepository.getOneBySerialNum(p.getSerialNum());

				if (pendingTpe != null) {
					PosType posType = posTypeRepository.getOne(pendingTpe.getType());

					if (posType != null) {
						tpePendingDisplay.setType(posType.getLibelle());
					}

				}

				if (psn != null) {
					PosModel pm = posModelRepository.getOne(psn.getModel());
					tpePendingDisplay.setModel(pm.getLibelle());
				} else {
					tpePendingDisplay.setModel("");

				}
			}

			tpePendingDisplay.setPhone(p.getPhone());
			tpePendingDisplay.setCountry(p.getCountry());
			tpePendingDisplay.setCity(p.getCity());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());
			tpePendingDisplay.setMccCode(mccList.getMccListLibelle());

			tpePendingDisplay.setLibelle(p.getPosLibelle());

			tpePendingDisplays.add(tpePendingDisplay);

		}

		return ResponseEntity.ok().body(tpePendingDisplays);
	}

	@GetMapping("getMerchantCommissions/{id}")
	List<CommissionTpe> getMerchantCommissions(@PathVariable(value = "id") Integer id) {
		try {
			return commissionTpeRepository.findByMerchantCode(id);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	@GetMapping("getListPosTerminal")
	public List<PosTerminalDispalay> getListPosTerminal() {
		List<PosTerminal> posTerminals = posTerminalRepository.findAll();
		List<PosTerminalDispalay> posTerminalDispalays = new ArrayList<>();

		for (PosTerminal p : posTerminals) {

			Merchant merchant = merchantRepository.findByMerchantCode(p.getMerchantCode().getMerchantCode());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());
			String label = "";
			String codeEtats = "";
			PosEtats posEtats = null;
			if (p.getSerialNum().equals("")) {
				p.setSerialNum("");
				label = "";
				codeEtats = "";
			} else {
				PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.getOne(p.getSerialNum());
				posEtats = posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
				label = posEtats.getLibelleFr();
				codeEtats = String.valueOf(posEtats.getCodeStatus());

			}

			PosTerminalDispalay posTerminalDispalay = new PosTerminalDispalay(p.getPosCode(), merchant.getRso(),
					mccList.getMccListLibelle(), p.getPosNum(), p.getPosLibelle(), p.getPosLocation(), p.getCity(),
					p.getState(), p.getCountry(), p.getPhone(), p.getTerminalOwner(), p.getServiceRepInfo(),
					p.getReferralTel(), p.getCreationDate(), p.getAdresse(), p.getPosLimitsConf().getPlcLibelle(),
					p.getPosAllowedTransConf().getPatcLibelle(), p.getPosServiceConf().getPscLibelle(),
					p.getPbcBinConf().getPbcLibelle(), p.getStatus(), p.getSerialNum(), label, codeEtats);
			posTerminalDispalay.setStatusSup(p.getStatusSup());
			posTerminalDispalay.setStatusUpdtae(p.getStatusUpdtae());
			posTerminalDispalays.add(posTerminalDispalay);
		}
		return posTerminalDispalays;
	}

	@GetMapping("getListPosTerminalToUpdate")
	public List<PosTerminalDispalay> getListPosTerminalToUpdate() {
		List<PosTerminal> posTerminals = posTerminalRepository.getAllposToSupp();
		List<PosTerminalDispalay> posTerminalDispalays = new ArrayList<>();

		for (PosTerminal p : posTerminals) {

			Merchant merchant = merchantRepository.findByMerchantCode(p.getMerchantCode().getMerchantCode());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());
			String label = "";
			String codeEtats = "";
			PosEtats posEtats = null;
			if (p.getSerialNum().equals("")) {
				p.setSerialNum("");
				label = "";
				codeEtats = "";
			} else {
				PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.getOne(p.getSerialNum());
				posEtats = posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
				label = posEtats.getLibelleFr();
				codeEtats = String.valueOf(posEtats.getCodeStatus());

			}

			PosTerminalDispalay posTerminalDispalay = new PosTerminalDispalay(p.getPosCode(), merchant.getRso(),
					mccList.getMccListLibelle(), p.getPosNum(), p.getPosLibelle(), p.getPosLocation(), p.getCity(),
					p.getState(), p.getCountry(), p.getPhone(), p.getTerminalOwner(), p.getServiceRepInfo(),
					p.getReferralTel(), p.getCreationDate(), p.getAdresse(), p.getPosLimitsConf().getPlcLibelle(),
					p.getPosAllowedTransConf().getPatcLibelle(), p.getPosServiceConf().getPscLibelle(),
					p.getPbcBinConf().getPbcLibelle(), p.getStatus(), p.getSerialNum(), label, codeEtats);
			posTerminalDispalay.setStatusSup(p.getStatusSup());
			posTerminalDispalay.setStatusUpdtae(p.getStatusUpdtae());
			posTerminalDispalays.add(posTerminalDispalay);
		}
		return posTerminalDispalays;
	}

	@GetMapping("getListPosTerminalToInstall")
	public List<PosTerminalDispalay> getListPosTerminalToInstall() {
		List<PosTerminal> posTerminals = posTerminalRepository.getAllposbeginwithP();
		List<PosTerminalDispalay> posTerminalDispalays = new ArrayList<>();

		for (PosTerminal p : posTerminals) {

			Merchant merchant = merchantRepository.findByMerchantCode(p.getMerchantCode().getMerchantCode());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());
			String label = "";
			String codeEtats = "";
			PosEtats posEtats = null;
			if (p.getSerialNum().equals("")) {
				p.setSerialNum("");
				label = "";
				codeEtats = "";
			} else {
				PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.getOne(p.getSerialNum());
				posEtats = posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
				label = posEtats.getLibelleFr();
				codeEtats = String.valueOf(posEtats.getCodeStatus());

			}

			PosTerminalDispalay posTerminalDispalay = new PosTerminalDispalay(p.getPosCode(), merchant.getRso(),
					mccList.getMccListLibelle(), p.getPosNum(), p.getPosLibelle(), p.getPosLocation(), p.getCity(),
					p.getState(), p.getCountry(), p.getPhone(), p.getTerminalOwner(), p.getServiceRepInfo(),
					p.getReferralTel(), p.getCreationDate(), p.getAdresse(), p.getPosLimitsConf().getPlcLibelle(),
					p.getPosAllowedTransConf().getPatcLibelle(), p.getPosServiceConf().getPscLibelle(),
					p.getPbcBinConf().getPbcLibelle(), p.getStatus(), p.getSerialNum(), label, codeEtats);
			posTerminalDispalay.setStatusSup(p.getStatusSup());
			posTerminalDispalay.setStatusUpdtae(p.getStatusUpdtae());
			posTerminalDispalays.add(posTerminalDispalay);
		}
		return posTerminalDispalays;
	}

	@GetMapping("getListPosTerminalToSupp")
	public List<PosTerminalDispalay> getListPosTerminalToSupp() {
		List<PosTerminal> posTerminals = posTerminalRepository.findAllposToSupp();
		List<PosTerminalDispalay> posTerminalDispalays = new ArrayList<>();

		for (PosTerminal p : posTerminals) {

			Merchant merchant = merchantRepository.findByMerchantCode(p.getMerchantCode().getMerchantCode());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());
			String label = "";
			String codeEtats = "";
			PosEtats posEtats = null;
			if (p.getSerialNum().equals("")) {
				p.setSerialNum("");
				label = "";
				codeEtats = "";
			} else {
				PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.getOne(p.getSerialNum());
				posEtats = posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
				label = posEtats.getLibelleFr();
				codeEtats = String.valueOf(posEtats.getCodeStatus());

			}

			PosTerminalDispalay posTerminalDispalay = new PosTerminalDispalay(p.getPosCode(), merchant.getRso(),
					mccList.getMccListLibelle(), p.getPosNum(), p.getPosLibelle(), p.getPosLocation(), p.getCity(),
					p.getState(), p.getCountry(), p.getPhone(), p.getTerminalOwner(), p.getServiceRepInfo(),
					p.getReferralTel(), p.getCreationDate(), p.getAdresse(), p.getPosLimitsConf().getPlcLibelle(),
					p.getPosAllowedTransConf().getPatcLibelle(), p.getPosServiceConf().getPscLibelle(),
					p.getPbcBinConf().getPbcLibelle(), p.getStatus(), p.getSerialNum(), label, codeEtats);
			posTerminalDispalay.setStatusSup(p.getStatusSup());
			posTerminalDispalay.setStatusUpdtae(p.getStatusUpdtae());
			posTerminalDispalay.setDetailSupp(p.getDetailSupp());
			posTerminalDispalays.add(posTerminalDispalay);
		}
		return posTerminalDispalays;
	}

	@GetMapping("getListTerminalToSupp")
	public List<PosTerminalDispalay> getListTerminalToSupp() {
		List<PosTerminal> posTerminals = posTerminalRepository.findAllToSupp();
		List<PosTerminalDispalay> posTerminalDispalays = new ArrayList<>();

		for (PosTerminal p : posTerminals) {

			Merchant merchant = merchantRepository.findByMerchantCode(p.getMerchantCode().getMerchantCode());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());
			String label = "";
			String codeEtats = "";
			PosEtats posEtats = null;
			if (p.getSerialNum().equals("")) {
				p.setSerialNum("");
				label = "";
				codeEtats = "";
			} else {
				PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.getOne(p.getSerialNum());
				posEtats = posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
				label = posEtats.getLibelleFr();
				codeEtats = String.valueOf(posEtats.getCodeStatus());

			}

			PosTerminalDispalay posTerminalDispalay = new PosTerminalDispalay(p.getPosCode(), merchant.getRso(),
					mccList.getMccListLibelle(), p.getPosNum(), p.getPosLibelle(), p.getPosLocation(), p.getCity(),
					p.getState(), p.getCountry(), p.getPhone(), p.getTerminalOwner(), p.getServiceRepInfo(),
					p.getReferralTel(), p.getCreationDate(), p.getAdresse(), p.getPosLimitsConf().getPlcLibelle(),
					p.getPosAllowedTransConf().getPatcLibelle(), p.getPosServiceConf().getPscLibelle(),
					p.getPbcBinConf().getPbcLibelle(), p.getStatus(), p.getSerialNum(), label, codeEtats);
			posTerminalDispalay.setStatusSup(p.getStatusSup());
			posTerminalDispalay.setStatusUpdtae(p.getStatusUpdtae());
			posTerminalDispalays.add(posTerminalDispalay);
		}
		return posTerminalDispalays;
	}

	@GetMapping("findAllToRemplace")
	public List<PosTerminalDispalay> findAllToRemplace() {
		List<PosTerminal> posTerminals = posTerminalRepository.findAllToRemplace();
		List<PosTerminalDispalay> posTerminalDispalays = new ArrayList<>();

		for (PosTerminal p : posTerminals) {

			Merchant merchant = merchantRepository.findByMerchantCode(p.getMerchantCode().getMerchantCode());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());
			String label = "";
			String codeEtats = "";
			PosEtats posEtats = null;
			if (p.getSerialNum().equals("")) {
				p.setSerialNum("");
				label = "";
				codeEtats = "";
			} else {
				PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.getOne(p.getSerialNum());
				posEtats = posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
				label = posEtats.getLibelleFr();
				codeEtats = String.valueOf(posEtats.getCodeStatus());

			}

			PosTerminalDispalay posTerminalDispalay = new PosTerminalDispalay(p.getPosCode(), merchant.getRso(),
					mccList.getMccListLibelle(), p.getPosNum(), p.getPosLibelle(), p.getPosLocation(), p.getCity(),
					p.getState(), p.getCountry(), p.getPhone(), p.getTerminalOwner(), p.getServiceRepInfo(),
					p.getReferralTel(), p.getCreationDate(), p.getAdresse(), p.getPosLimitsConf().getPlcLibelle(),
					p.getPosAllowedTransConf().getPatcLibelle(), p.getPosServiceConf().getPscLibelle(),
					p.getPbcBinConf().getPbcLibelle(), p.getStatus(), p.getSerialNum(), label, codeEtats);
			posTerminalDispalay.setStatusSup(p.getStatusSup());
			posTerminalDispalay.setStatusUpdtae(p.getStatusUpdtae());
			posTerminalDispalay.setDetailUpdate(p.getDetailUpdate());

			posTerminalDispalays.add(posTerminalDispalay);
		}
		return posTerminalDispalays;
	}

	@GetMapping("getListTerminalToDeployed")
	public List<PosTerminalDispalay> getListTerminalToDeployed() {
		List<PosTerminal> posTerminals = posTerminalRepository.getAllposToDeployed();
		List<PosTerminalDispalay> posTerminalDispalays = new ArrayList<>();

		for (PosTerminal p : posTerminals) {

			Merchant merchant = merchantRepository.findByMerchantCode(p.getMerchantCode().getMerchantCode());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());
			String label = "";
			String codeEtats = "";
			PosEtats posEtats = null;
			if (p.getSerialNum().equals("")) {
				p.setSerialNum("");
				label = "";
				codeEtats = "";
			} else {
				PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.getOne(p.getSerialNum());
				posEtats = posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
				label = posEtats.getLibelleFr();
				codeEtats = String.valueOf(posEtats.getCodeStatus());

			}

			PosTerminalDispalay posTerminalDispalay = new PosTerminalDispalay(p.getPosCode(), merchant.getRso(),
					mccList.getMccListLibelle(), p.getPosNum(), p.getPosLibelle(), p.getPosLocation(), p.getCity(),
					p.getState(), p.getCountry(), p.getPhone(), p.getTerminalOwner(), p.getServiceRepInfo(),
					p.getReferralTel(), p.getCreationDate(), p.getAdresse(), p.getPosLimitsConf().getPlcLibelle(),
					p.getPosAllowedTransConf().getPatcLibelle(), p.getPosServiceConf().getPscLibelle(),
					p.getPbcBinConf().getPbcLibelle(), p.getStatus(), p.getSerialNum(), label, codeEtats);
			posTerminalDispalay.setStatusSup(p.getStatusSup());
			posTerminalDispalay.setStatusUpdtae(p.getStatusUpdtae());
			posTerminalDispalays.add(posTerminalDispalay);
		}
		return posTerminalDispalays;
	}

	@GetMapping("getListPosTerminalToRemp")
	public List<PosTerminalDispalay> getListPosTerminalToRemp() {
		List<PosTerminal> posTerminals = posTerminalRepository.findAllposToUpdate();
		List<PosTerminalDispalay> posTerminalDispalays = new ArrayList<>();

		for (PosTerminal p : posTerminals) {

			Merchant merchant = merchantRepository.findByMerchantCode(p.getMerchantCode().getMerchantCode());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());
			String label = "";
			String codeEtats = "";
			PosEtats posEtats = null;
			if (p.getSerialNum().equals("")) {
				p.setSerialNum("");
				label = "";
				codeEtats = "";
			} else {
				PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.getOne(p.getSerialNum());
				posEtats = posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
				label = posEtats.getLibelleFr();
				codeEtats = String.valueOf(posEtats.getCodeStatus());

			}

			PosTerminalDispalay posTerminalDispalay = new PosTerminalDispalay(p.getPosCode(), merchant.getRso(),
					mccList.getMccListLibelle(), p.getPosNum(), p.getPosLibelle(), p.getPosLocation(), p.getCity(),
					p.getState(), p.getCountry(), p.getPhone(), p.getTerminalOwner(), p.getServiceRepInfo(),
					p.getReferralTel(), p.getCreationDate(), p.getAdresse(), p.getPosLimitsConf().getPlcLibelle(),
					p.getPosAllowedTransConf().getPatcLibelle(), p.getPosServiceConf().getPscLibelle(),
					p.getPbcBinConf().getPbcLibelle(), p.getStatus(), p.getSerialNum(), label, codeEtats);
			posTerminalDispalay.setStatusSup(p.getStatusSup());
			posTerminalDispalay.setStatusUpdtae(p.getStatusUpdtae());
			posTerminalDispalay.setDetailUpdate(p.getDetailUpdate());
			posTerminalDispalays.add(posTerminalDispalay);
		}
		return posTerminalDispalays;
	}

	@PostMapping("GetAllPosTerminalFiltred")
	public List<PosTerminalDispalay> GetAllPosTerminalFiltred(
			@RequestBody RequestFilterPosTerminal requestFilterPosTerminal) {
		List<PosTerminalDispalay> posTerminalDispalays = new ArrayList<>();

		for (PosTerminal p : posTerminalRepository.findAllposNum(requestFilterPosTerminal.getPosNum().trim(),
				requestFilterPosTerminal.getAgencyCode().trim())) {
			String label = "";
			String codeEtats = "";
			PosEtats posEtats = null;
			if (p.getSerialNum().equals("")) {
				p.setSerialNum("");
				label = "";
				codeEtats = "";
			} else {
				PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.getOne(p.getSerialNum());
				posEtats = posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
				label = posEtats.getLibelleFr();
				codeEtats = String.valueOf(posEtats.getCodeStatus());

			}
			Merchant merchant = merchantRepository.findByMerchantCode(p.getMerchantCode().getMerchantCode());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());

			PosTerminalDispalay posTerminalDispalay = new PosTerminalDispalay(p.getPosCode(), merchant.getRso(),
					null, p.getPosNum(), p.getPosLibelle(), p.getPosLocation(), p.getCity(),
					p.getState(), p.getCountry(), p.getPhone(), p.getTerminalOwner(), p.getServiceRepInfo(),
					p.getReferralTel(), p.getCreationDate(), p.getAdresse(), p.getPosLimitsConf().getPlcLibelle(),
					p.getPosAllowedTransConf().getPatcLibelle(), p.getPosServiceConf().getPscLibelle(),
					p.getPbcBinConf().getPbcLibelle(), p.getStatus(), p.getSerialNum(), label, codeEtats);
			posTerminalDispalay.setStatusSup(p.getStatusSup());
			posTerminalDispalay.setStatusUpdtae(p.getStatusUpdtae());
			posTerminalDispalay.setDetailUpdate(p.getDetailUpdate());
			posTerminalDispalay.setIdContrat(p.getMerchantCode().getIdContrat());
			if(mccList!=null) {
				posTerminalDispalay.setMccCode(mccList.getMccListId());
				posTerminalDispalay.setMcc(mccList.getMccListLibelle());
			}
			
			
			FamillePos fp = famillePosRepository.findById(p.getFamillePosCode()).get();
			posTerminalDispalay.setTerminalFamilly(fp.getFamillePosLibelle());
			if (p.getType() != 0) {
				PosType ptype = posTypeRepository.findById(p.getType()).get();
				posTerminalDispalay.setTerminalType(ptype.getLibelle());
			} else {
				posTerminalDispalay.setTerminalType("");
			}
			posTerminalDispalays.add(posTerminalDispalay);
		}
		return posTerminalDispalays;

	}

	@PostMapping("GetAllPosTerminalDeployedFiltred")
	public List<PosTerminalDispalay> GetAllPosTerminalDeployedFiltred(
			@RequestBody RequestFilterPosTerminal requestFilterPosTerminal) {
		List<PosTerminalDispalay> posTerminalDispalays = new ArrayList<>();

		for (PosTerminal p : posTerminalRepository.findAllposToDeployed(requestFilterPosTerminal.getPosNum().trim(),
				requestFilterPosTerminal.getAgencyCode().trim())) {
			String label = "";
			String codeEtats = "";
			PosEtats posEtats = null;
			if (p.getSerialNum().equals("")) {
				p.setSerialNum("");
				label = "";
				codeEtats = "";
			} else {
				PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.getOne(p.getSerialNum());
				posEtats = posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
				label = posEtats.getLibelleFr();
				codeEtats = String.valueOf(posEtats.getCodeStatus());

			}

			Merchant merchant = merchantRepository.findByMerchantCode(p.getMerchantCode().getMerchantCode());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());

			PosTerminalDispalay posTerminalDispalay = new PosTerminalDispalay(p.getPosCode(), merchant.getRso(),
					mccList.getMccListLibelle(), p.getPosNum(), p.getPosLibelle(), p.getPosLocation(), p.getCity(),
					p.getState(), p.getCountry(), p.getPhone(), p.getTerminalOwner(), p.getServiceRepInfo(),
					p.getReferralTel(), p.getCreationDate(), p.getAdresse(), p.getPosLimitsConf().getPlcLibelle(),
					p.getPosAllowedTransConf().getPatcLibelle(), p.getPosServiceConf().getPscLibelle(),
					p.getPbcBinConf().getPbcLibelle(), p.getStatus(), p.getSerialNum(), label, codeEtats);
			posTerminalDispalay.setStatusSup(p.getStatusSup());
			posTerminalDispalay.setStatusUpdtae(p.getStatusUpdtae());
			System.out.println(p.getFamillePosCode() + " and " + p.getType());
			FamillePos fp = famillePosRepository.findById(p.getFamillePosCode()).get();
			posTerminalDispalay.setTerminalFamilly(fp.getFamillePosLibelle());
			if (p.getType() != 0) {
				PosType ptype = posTypeRepository.findById(p.getType()).get();
				posTerminalDispalay.setTerminalType(ptype.getLibelle());
			} else {
				posTerminalDispalay.setTerminalType("");
			}

			posTerminalDispalays.add(posTerminalDispalay);
		}
		return posTerminalDispalays;

	}

	@PostMapping("GetAllPosTerminalFiltredToSupp")
	public List<PosTerminalDispalay> GetAllPosTerminalFiltredToSupp(
			@RequestBody RequestFilterPosTerminal requestFilterPosTerminal) {
		List<PosTerminalDispalay> posTerminalDispalays = new ArrayList<>();

		for (PosTerminal p : posTerminalRepository.findAllposNumFilterTosupp(
				requestFilterPosTerminal.getPosNum().trim(), requestFilterPosTerminal.getAgencyCode().trim())) {
			String label = "";
			String codeEtats = "";
			PosEtats posEtats = null;
			if (p.getSerialNum().equals("")) {
				p.setSerialNum("");
				label = "";
				codeEtats = "";
			} else {
				PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.getOne(p.getSerialNum());
				posEtats = posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
				label = posEtats.getLibelleFr();
				codeEtats = String.valueOf(posEtats.getCodeStatus());

			}
			// PosSerialNumStates
			// posSerialNumStates=posSerialNumStatesRepository.getOne(p.getSerialNum());
			// PosEtats posEtats =
			// posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
			Merchant merchant = merchantRepository.findByMerchantCode(p.getMerchantCode().getMerchantCode());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());
			// PosLimits posLimits = posLimitsRepository.findById(p.getPosLimits()).get();
			// PosAllowedTransaction posAllowedTransaction =
			// posAllowedTransactionRepository.findById(p.getPosAllowedTrans()).get();
			PosTerminalDispalay posTerminalDispalay = new PosTerminalDispalay(p.getPosCode(), merchant.getRso(),
					mccList.getMccListLibelle(), p.getPosNum(), p.getPosLibelle(), p.getPosLocation(), p.getCity(),
					p.getState(), p.getCountry(), p.getPhone(), p.getTerminalOwner(), p.getServiceRepInfo(),
					p.getReferralTel(), p.getCreationDate(), p.getAdresse(), p.getPosLimitsConf().getPlcLibelle(),
					p.getPosAllowedTransConf().getPatcLibelle(), p.getPosServiceConf().getPscLibelle(),
					p.getPbcBinConf().getPbcLibelle(), p.getStatus(), p.getSerialNum(), label, codeEtats);
			posTerminalDispalay.setStatusSup(p.getStatusSup());
			posTerminalDispalay.setStatusUpdtae(p.getStatusUpdtae());
			posTerminalDispalay.setDetailSupp(p.getDetailSupp());
			FamillePos fp = famillePosRepository.findById(p.getFamillePosCode()).get();
			posTerminalDispalay.setTerminalFamilly(fp.getFamillePosLibelle());
			if (p.getType() != 0) {
				PosType ptype = posTypeRepository.findById(p.getType()).get();
				posTerminalDispalay.setTerminalType(ptype.getLibelle());
			} else {
				posTerminalDispalay.setTerminalType("");
			}
			posTerminalDispalays.add(posTerminalDispalay);
		}
		return posTerminalDispalays;

	}

	@PostMapping("findAllFilterToRemplace")
	public List<PosTerminalDispalay> findAllFilterToRemplace(
			@RequestBody RequestFilterPosTerminal requestFilterPosTerminal) {
		List<PosTerminalDispalay> posTerminalDispalays = new ArrayList<>();

		for (PosTerminal p : posTerminalRepository.findAllFilterToRemplace(requestFilterPosTerminal.getPosNum().trim(),
				requestFilterPosTerminal.getAgencyCode().trim())) {
			String label = "";
			String codeEtats = "";
			PosEtats posEtats = null;
			if (p.getSerialNum().equals("")) {
				p.setSerialNum("");
				label = "";
				codeEtats = "";
			} else {
				PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.getOne(p.getSerialNum());
				posEtats = posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
				label = posEtats.getLibelleFr();
				codeEtats = String.valueOf(posEtats.getCodeStatus());

			}

			Merchant merchant = merchantRepository.findByMerchantCode(p.getMerchantCode().getMerchantCode());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());

			PosTerminalDispalay posTerminalDispalay = new PosTerminalDispalay(p.getPosCode(), merchant.getRso(),
					mccList.getMccListLibelle(), p.getPosNum(), p.getPosLibelle(), p.getPosLocation(), p.getCity(),
					p.getState(), p.getCountry(), p.getPhone(), p.getTerminalOwner(), p.getServiceRepInfo(),
					p.getReferralTel(), p.getCreationDate(), p.getAdresse(), p.getPosLimitsConf().getPlcLibelle(),
					p.getPosAllowedTransConf().getPatcLibelle(), p.getPosServiceConf().getPscLibelle(),
					p.getPbcBinConf().getPbcLibelle(), p.getStatus(), p.getSerialNum(), label, codeEtats);
			posTerminalDispalay.setStatusSup(p.getStatusSup());
			posTerminalDispalay.setStatusUpdtae(p.getStatusUpdtae());
			posTerminalDispalays.add(posTerminalDispalay);
		}
		return posTerminalDispalays;

	}

	@PostMapping("GetAllTerminalFiltredToSupp")
	public List<PosTerminalDispalay> GetAllTerminalFiltredToSupp(
			@RequestBody RequestFilterPosTerminal requestFilterPosTerminal) {
		List<PosTerminalDispalay> posTerminalDispalays = new ArrayList<>();

		for (PosTerminal p : posTerminalRepository.findAllFilterTosupp(requestFilterPosTerminal.getPosNum().trim(),
				requestFilterPosTerminal.getAgencyCode().trim())) {
			String label = "";
			String codeEtats = "";
			PosEtats posEtats = null;
			if (p.getSerialNum().equals("")) {
				p.setSerialNum("");
				label = "";
				codeEtats = "";
			} else {
				PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.getOne(p.getSerialNum());
				posEtats = posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
				label = posEtats.getLibelleFr();
				codeEtats = String.valueOf(posEtats.getCodeStatus());

			}

			Merchant merchant = merchantRepository.findByMerchantCode(p.getMerchantCode().getMerchantCode());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());

			PosTerminalDispalay posTerminalDispalay = new PosTerminalDispalay(p.getPosCode(), merchant.getRso(),
					mccList.getMccListLibelle(), p.getPosNum(), p.getPosLibelle(), p.getPosLocation(), p.getCity(),
					p.getState(), p.getCountry(), p.getPhone(), p.getTerminalOwner(), p.getServiceRepInfo(),
					p.getReferralTel(), p.getCreationDate(), p.getAdresse(), p.getPosLimitsConf().getPlcLibelle(),
					p.getPosAllowedTransConf().getPatcLibelle(), p.getPosServiceConf().getPscLibelle(),
					p.getPbcBinConf().getPbcLibelle(), p.getStatus(), p.getSerialNum(), label, codeEtats);
			posTerminalDispalay.setStatusSup(p.getStatusSup());
			posTerminalDispalay.setStatusUpdtae(p.getStatusUpdtae());
			posTerminalDispalays.add(posTerminalDispalay);
		}
		return posTerminalDispalays;

	}

	@PostMapping("GetAllPosTerminalFiltredToRemp")
	public List<PosTerminalDispalay> GetAllPosTerminalFiltredToRemp(
			@RequestBody RequestFilterPosTerminal requestFilterPosTerminal) {
		List<PosTerminalDispalay> posTerminalDispalays = new ArrayList<>();

		for (PosTerminal p : posTerminalRepository.findAllposNumFilterToUpdate(
				requestFilterPosTerminal.getPosNum().trim(), requestFilterPosTerminal.getAgencyCode().trim())) {
			String label = "";
			String codeEtats = "";
			PosEtats posEtats = null;
			if (p.getSerialNum().equals("")) {
				p.setSerialNum("");
				label = "";
				codeEtats = "";
			} else {
				PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.getOne(p.getSerialNum());
				posEtats = posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
				label = posEtats.getLibelleFr();
				codeEtats = String.valueOf(posEtats.getCodeStatus());

			}
			// PosSerialNumStates
			// posSerialNumStates=posSerialNumStatesRepository.getOne(p.getSerialNum());
			// PosEtats posEtats =
			// posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
			Merchant merchant = merchantRepository.findByMerchantCode(p.getMerchantCode().getMerchantCode());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());
			// PosLimits posLimits = posLimitsRepository.findById(p.getPosLimits()).get();
			// PosAllowedTransaction posAllowedTransaction =
			// posAllowedTransactionRepository.findById(p.getPosAllowedTrans()).get();
			PosTerminalDispalay posTerminalDispalay = new PosTerminalDispalay(p.getPosCode(), merchant.getRso(),
					mccList.getMccListLibelle(), p.getPosNum(), p.getPosLibelle(), p.getPosLocation(), p.getCity(),
					p.getState(), p.getCountry(), p.getPhone(), p.getTerminalOwner(), p.getServiceRepInfo(),
					p.getReferralTel(), p.getCreationDate(), p.getAdresse(), p.getPosLimitsConf().getPlcLibelle(),
					p.getPosAllowedTransConf().getPatcLibelle(), p.getPosServiceConf().getPscLibelle(),
					p.getPbcBinConf().getPbcLibelle(), p.getStatus(), p.getSerialNum(), label, codeEtats);
			posTerminalDispalay.setStatusSup(p.getStatusSup());
			posTerminalDispalay.setStatusUpdtae(p.getStatusUpdtae());
			posTerminalDispalay.setDetailUpdate(p.getDetailUpdate());
			FamillePos fp = famillePosRepository.findById(p.getFamillePosCode()).get();
			posTerminalDispalay.setTerminalFamilly(fp.getFamillePosLibelle());
			if (p.getType() != 0) {
				PosType ptype = posTypeRepository.findById(p.getType()).get();
				posTerminalDispalay.setTerminalType(ptype.getLibelle());
			} else {
				posTerminalDispalay.setTerminalType("");
			}
			posTerminalDispalays.add(posTerminalDispalay);
		}
		return posTerminalDispalays;

	}

	@GetMapping("commissionsType")
	public List<CommissionType> getAllCommissionsType() {

		logger.info(commissionTypeRepository.findAll().toString());

		return commissionTypeRepository.findAll();
	}

	@GetMapping("getOffshoreType")
	public List<RequestTpeType> getOffshoreType() {

		logger.info(requestTpeTypeRepository.findAll().toString());

		return requestTpeTypeRepository.findAll();
	}

	@GetMapping("getFamillePos")
	public List<FamillePos> getFamillePos() {

		logger.info(famillePosRepository.findAll().toString());

		return famillePosRepository.findAll();
	}

//    @PostMapping("addReclamation")
//    public ResponseEntity<String> addReclamation(
//            @Valid @RequestBody Reclamation reclamation) {
//        reclamationRepository.save(reclamation);
//
//        return ResponseEntity.ok().body(gson.toJson("reclamation added successfully!"));
//
//    }
//    @PostMapping("getListReclamations")
//    public List<ReclamationDisplay> getListReclamations(@RequestBody ReclamationFilter reclamationFilter) {
//        List<Reclamation> reclamation = reclamationRepository.findAllByMerchant(reclamationFilter.getMerchantId());
//        List<ReclamationDisplay> reclamationDisplay = new ArrayList<>();
//
//        for (Reclamation p : reclamation
//        ) {
//            Merchant merchant= merchantRepository.findByMerchantCode(p.getMerchantCode());
//            PosTerminal pos = posTerminalRepository.findByPosCode(p.getPosCode());
//            ReclamationType recType =reclamationTypeRepository.findById(p.getStatus()).get();
//            ReclamationDisplay rec = new ReclamationDisplay();
//            rec.setReclamationCode(String.valueOf(p.getReclamationCode()));
//            rec.setDateReclamation(p.getDateReclamation());
//            rec.setMotif(p.getMotif());
//            rec.setStatus(recType.getLibelle());
//            rec.setMerchantCode(merchant.getMerchantId());
//            rec.setPosCode(pos.getPosLibelle());
//            rec.setAction(p.getAction());
//            reclamationDisplay.add(rec);
//        }
//        return reclamationDisplay;
//    }
	@GetMapping("getMerchant")
	public List<Merchant> getMerchant() {
		return merchantRepository.findAll();
	}

	@GetMapping("getPosTerminal")
	public List<PosTerminal> getPosTerminal() {
		return posTerminalRepository.findAll();
	}
//    @GetMapping("getReclamationStatus")
//    public List<ReclamationType> getReclamationStatus() {
//        return reclamationTypeRepository.findAll();
//    }
//
//    @PutMapping("AddAction")
//    public void AddAction(@RequestBody ReclamationRequest reclamationRequest) {
//
//        Reclamation reclamation = reclamationRepository.findById(Integer.parseInt(reclamationRequest.getReclamationCode())).get();
//        reclamation.setAction(reclamationRequest.getAction());
//
//        reclamationRepository.save(reclamation);
//    }

	@PostMapping("getAllPagePosStock")
	public Page<PosRequestStock> getAllPagePosStock(@RequestBody PosRequestLibelle filter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir

	) {
		List<PosRequestStock> list = new ArrayList<>();
		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);

		orders.add(order1);
		if (filter.getLibelle().equals("")) {
			Page<PosStock> page1 = posStockRepository.getPagePosStock(PageRequest.of(page, size, Sort.by(orders)));

			for (PosStock tr : page1.getContent()) {
				PosRequestStock posRequestStock = new PosRequestStock();
				posRequestStock.setIdStock(tr.getIdStock());
				posRequestStock.setStockConsome(tr.getStockConsome());
				posRequestStock.setStockDeployer(tr.getStockDeployer());
				posRequestStock.setStockDisponible(tr.getStockDisponible());
				posRequestStock.setStockHS(tr.getStockHS());
				posRequestStock.setStockInitial(tr.getStockInitial());
				posRequestStock.setStockReserve(tr.getStockReserve());
				posRequestStock.setDateSaisie(tr.getDateSaisie());
				PosModel posModel = posModelRepository.getOne(tr.getModel());
				PosMarque posMarque = posMarqueRepository.getOne(posModel.getMarqueCode());
				posRequestStock.setMarque(posMarque.getLibelle());
				posRequestStock.setModel(posModel.getLibelle());
				list.add(posRequestStock);

			}
			Page<PosRequestStock> pages = new PageImpl<PosRequestStock>(list,
					PageRequest.of(page, size, Sort.by(orders)), page1.getTotalElements());
			return pages;
		} else {
			Page<PosStock> page1 = posStockRepository.getPagePosStock(PageRequest.of(page, size, Sort.by(orders)),
					Integer.parseInt(filter.getLibelle()));
			for (PosStock tr : page1.getContent()) {
				PosRequestStock posRequestStock = new PosRequestStock();
				posRequestStock.setIdStock(tr.getIdStock());
				posRequestStock.setStockConsome(tr.getStockConsome());
				posRequestStock.setStockDeployer(tr.getStockDeployer());
				posRequestStock.setStockDisponible(tr.getStockDisponible());
				posRequestStock.setStockHS(tr.getStockHS());
				posRequestStock.setStockInitial(tr.getStockInitial());
				posRequestStock.setStockReserve(tr.getStockReserve());
				posRequestStock.setDateSaisie(tr.getDateSaisie());
				PosModel posModel = posModelRepository.getOne(tr.getModel());
				PosMarque posMarque = posMarqueRepository.getOne(posModel.getMarqueCode());
				posRequestStock.setMarque(posMarque.getLibelle());
				posRequestStock.setModel(posModel.getLibelle());
				list.add(posRequestStock);

			}
			Page<PosRequestStock> pages = new PageImpl<PosRequestStock>(list,
					PageRequest.of(page, size, Sort.by(orders)), page1.getTotalElements());
			return pages;
		}

	}

	@PostMapping("AddPosStock")
	public ResponseEntity<String> AddPosStock(@RequestBody PosStock posStock) {
		PosStock s = new PosStock();
		s.setIdStock(posStock.getIdStock());
		PosStock ss = posStockRepository.getOne(posStock.getIdStock());
		s.setModel(ss.getModel());
		s.setStockConsome(0);
		s.setStockDisponible(posStock.getStockInitial());
		s.setStockInitial(posStock.getStockInitial());
		s.setStockReserve(0);
		s.setStockDeployer(0);
		s.setStockHS(0);
		s.setDateSaisie(ss.getDateSaisie());
		posStockRepository.save(s);
		return ResponseEntity.ok().body(gson.toJson("Stock initial créé"));

	}

	@PutMapping("update/{id}/{stock}")
	public ResponseEntity<String> updateStock(@PathVariable(value = "id") String idStock,
			@PathVariable(value = "stock") int stock) {
		PosStock posStock = posStockRepository.getOne(Integer.parseInt(idStock));
		int somme = posStock.getStockDisponible() + stock;
		posStock.setStockDisponible(somme);
		posStockRepository.save(posStock);
		return ResponseEntity.ok().body(gson.toJson("Stock initial créé"));
	}

	@PostMapping("getAllPagePosModel")
	public Page<PosRequestModel> getAllPageModelPos(@RequestBody PosRequestLibelle filter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir

	) {

		List<PosRequestModel> list = new ArrayList<>();

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);

		orders.add(order1);
		Page<PosModel> page1 = posModelRepository.getPagePosModel(PageRequest.of(page, size, Sort.by(orders)),
				filter.getLibelle().trim());
		List<PosModel> posModel = posModelRepository.findAll();
		for (PosModel tr : page1.getContent()) {
			PosRequestModel posRequestModel = new PosRequestModel();
			posRequestModel.setCodeModel(tr.getModelCode());
			PosType posType = posTypeRepository.getOne(tr.getCodeType());
			PosMarque posMarque = posMarqueRepository.getOne(tr.getMarqueCode());
			posRequestModel.setCodeType(posType.getLibelle());
			posRequestModel.setMarque(posMarque.getLibelle());
			posRequestModel.setLibelle(tr.getLibelle());
			list.add(posRequestModel);

		}

		Page<PosRequestModel> pages = new PageImpl<PosRequestModel>(list, PageRequest.of(page, size, Sort.by(orders)),
				page1.getTotalElements());
		return pages;

	}

	@PostMapping("getAllPagePosMarque")
	public Page<PosMarque> getAllPageMarquePos(@RequestBody PosRequestLibelle filter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir

	) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);

		orders.add(order1);
		return posMarqueRepository.getPagePosMarque(PageRequest.of(page, size, Sort.by(orders)),
				filter.getLibelle().trim());

	}

	@GetMapping("getAllPosMarque")
	public List<PosMarque> getAllPosMarque() {

		return posMarqueRepository.findAll();
	}

	@GetMapping("getAllPosModel/{id}")
	public PosModel getAllPosModel(@PathVariable(value = "id") Integer idModel) {
		PosModel posModel = posModelRepository.getOneByCode(idModel);

		return posModel;
	}

	@PostMapping("AddPosMarque")
	public ResponseEntity<String> AddPosMarque(@RequestBody PosMarque posMarque) {
		PosMarque posM = posMarqueRepository.getPosMarqueBylibelle(posMarque.getLibelle());
		if (posM == null) {
			posMarqueRepository.save(posMarque);
			return ResponseEntity.ok().body(gson.toJson("posMarque créé"));
		} else {
			return new ResponseEntity<String>(" Marque of POS With label " + posMarque.getLibelle() + " existed",
					HttpStatus.BAD_REQUEST);
		}

	}

	@PutMapping("updatePosMarque/{id}")
	public ResponseEntity<String> updatePosMarque(@PathVariable(value = "id") Integer idMarque,
			@RequestBody PosMarque posMarque) {

		PosMarque posMarques = posMarqueRepository.getOne(idMarque);
		posMarques.setLibelle(posMarque.getLibelle());
		posMarqueRepository.save(posMarques);
		return ResponseEntity.ok().body(gson.toJson("posMarque update"));

	}

	@PostMapping("AddPosModel")
	public ResponseEntity<String> AddPosModel(@RequestBody PosModel posModel) {
		PosModel pm = posModelRepository.getOneByLibelle(posModel.getLibelle());
		if (pm == null) {

			PosStock s = new PosStock();
			// PosMarque posMarques = posMarqueRepository.getOne(posModel.getMarqueCode());
			SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			Date date = new Date();
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			System.out.println(timestamp);
			String strDate = sdf3.format(timestamp);
			s.setDateSaisie(strDate);
			s.setStockConsome(0);
			s.setStockDisponible(0);
			s.setStockInitial(0);
			s.setStockReserve(0);
			s.setStockDeployer(0);
			s.setStockHS(0);
			posModel = posModelRepository.save(posModel);

			System.out.println(posModel.getCodeType());

			s.setModel(posModel.getModelCode());
			posStockRepository.save(s);
			return ResponseEntity.ok().body(gson.toJson("PosModel créé"));
		} else {
			return new ResponseEntity<String>(" Model of POS With label " + posModel.getLibelle() + " existed",
					HttpStatus.BAD_REQUEST);
		}

	}

	@PutMapping("updatePosModel/{id}")
	public ResponseEntity<String> updatePosModel(@PathVariable(value = "id") Integer idModel,
			@RequestBody PosModel posModel) {

		PosModel posModels = posModelRepository.getOne(idModel);
		posModels.setLibelle(posModel.getLibelle());
		posModels.setMarqueCode(posModel.getMarqueCode());
		posModels.setCodeType(posModel.getCodeType());
		posModelRepository.save(posModels);
		return ResponseEntity.ok().body(gson.toJson("posModel update"));

	}

	@PostMapping("getPagePosSerialNumStates")
	public Page<RequestPosSerialNumStates> getPagePosSerialNumStates(@RequestBody PosRequestLibelle filter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "sortOn", defaultValue = "dateSaisie") String sortOn,
			@RequestParam(name = "dir", defaultValue = "desc") String dir) {

		List<RequestPosSerialNumStates> list = new ArrayList<>();

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);

		orders.add(order1);

		Page<Object[]> page1 = null;
		if (filter.getPosNum().trim().equals("") && filter.getMerchantCode().trim().equals("")) {

			System.out.println("if");
			page1 = posSerialNumStatesRepository.getPagePosSerialNumStates2(PageRequest.of(page, size, Sort.by(orders)),
					filter.getLibelle().trim());

		} else {

			System.out.println("else");
			page1 = posSerialNumStatesRepository.getPagePosSerialNumStates1(PageRequest.of(page, size, Sort.by(orders)),
					filter.getLibelle().trim(), filter.getPosNum().trim(), filter.getMerchantCode().trim());

		}

		for (Object[] tr : page1.getContent()) {
			RequestPosSerialNumStates requestPosSerialNumStates = new RequestPosSerialNumStates();
			PosSerialNumStates posSerialNumStates = (PosSerialNumStates) tr[0];
			PosModel posModel = posModelRepository.getOne(posSerialNumStates.getModel());
			PosMarque posMarque = posMarqueRepository.getOne(posModel.getMarqueCode());
			PosEtats posEtats = posEtatsRepository.getOne(posSerialNumStates.getStatus());

			PosTerminal posTerminal = posTerminalRepository.findAllposBySerialNum(posSerialNumStates.getSerialNum());

			PosType posType = posTypeRepository.getOne(posModel.getCodeType());
			if (posTerminal != null) {
				Merchant m = merchantRepository.findById(posTerminal.getMerchantCode().getMerchantCode()).get();
				requestPosSerialNumStates.setLibellePOS(posTerminal.getPosLibelle());
				requestPosSerialNumStates.setPosNum(posTerminal.getPosNum());
				requestPosSerialNumStates.setMerchantCode(m.getIdContrat());
				requestPosSerialNumStates.setAdresse(posTerminal.getAdresse());
			} else {
				requestPosSerialNumStates.setLibellePOS("");
				requestPosSerialNumStates.setPosNum("");
				requestPosSerialNumStates.setMerchantCode("");
				requestPosSerialNumStates.setAdresse("");
			}
			requestPosSerialNumStates.setTypeCode(posModel.getCodeType());
			requestPosSerialNumStates.setType(posType.getLibelle());
			requestPosSerialNumStates.setNumSim(posSerialNumStates.getNumSim());
			requestPosSerialNumStates.setStatuRemplacement(posSerialNumStates.getStatuRemplacement());
			requestPosSerialNumStates.setMarque(posMarque.getLibelle());
			requestPosSerialNumStates.setStatusCode(posSerialNumStates.getStatus());
			requestPosSerialNumStates.setSerialNum(posSerialNumStates.getSerialNum());
			requestPosSerialNumStates.setModel(posModel.getLibelle());
			requestPosSerialNumStates.setStatus(posEtats.getLibelleFr());
			requestPosSerialNumStates.setDateSaisie(posSerialNumStates.getDateSaisie());
			list.add(requestPosSerialNumStates);
		}

		Page<RequestPosSerialNumStates> pages = new PageImpl<RequestPosSerialNumStates>(list,
				PageRequest.of(page, size, Sort.by(orders)), page1.getTotalElements());

		return pages;
	}

	@PostMapping("getPagePosSerialNumStatesResiliation")
	public Page<RequestPosSerialNumStates> getPagePosSerialNumStatesResiliation(@RequestBody PosRequestLibelle filter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "sortOn", defaultValue = "dateSaisie") String sortOn,
			@RequestParam(name = "dir", defaultValue = "desc") String dir) {

		List<RequestPosSerialNumStates> list = new ArrayList<>();

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);

		orders.add(order1);

		Page<Object[]> page1 = null;
		if (filter.getPosNum().trim().equals("") && filter.getMerchantCode().trim().equals("")) {

			System.out.println("if");
			page1 = posSerialNumStatesRepository.getPagePosSerialNumStates4(PageRequest.of(page, size, Sort.by(orders)),
					filter.getLibelle().trim());

		} else {

			System.out.println("else");
			page1 = posSerialNumStatesRepository.getPagePosSerialNumStates3(PageRequest.of(page, size, Sort.by(orders)),
					filter.getLibelle().trim(), filter.getPosNum().trim(), filter.getMerchantCode().trim());

		}

		for (Object[] tr : page1.getContent()) {
			RequestPosSerialNumStates requestPosSerialNumStates = new RequestPosSerialNumStates();
			PosSerialNumStates posSerialNumStates = (PosSerialNumStates) tr[0];
			PosModel posModel = posModelRepository.getOne(posSerialNumStates.getModel());
			PosMarque posMarque = posMarqueRepository.getOne(posModel.getMarqueCode());
			PosEtats posEtats = posEtatsRepository.getOne(posSerialNumStates.getStatus());

			PosTerminal posTerminal = posTerminalRepository.findAllposBySerialNum(posSerialNumStates.getSerialNum());
			PosType posType = posTypeRepository.getOne(posModel.getCodeType());

			if (posTerminal != null) {
				Merchant m = merchantRepository.findById(posTerminal.getMerchantCode().getMerchantCode()).get();
				requestPosSerialNumStates.setLibellePOS(posTerminal.getPosLibelle());
				requestPosSerialNumStates.setPosNum(posTerminal.getPosNum());
				requestPosSerialNumStates.setMerchantCode(m.getIdContrat());
				requestPosSerialNumStates.setAdresse(posTerminal.getAdresse());
			} else {
				requestPosSerialNumStates.setLibellePOS("");
				requestPosSerialNumStates.setPosNum("");
				requestPosSerialNumStates.setMerchantCode("");
				requestPosSerialNumStates.setAdresse("");
			}
			requestPosSerialNumStates.setTypeCode(posModel.getCodeType());
			requestPosSerialNumStates.setType(posType.getLibelle());
			requestPosSerialNumStates.setNumSim(posSerialNumStates.getNumSim());

			requestPosSerialNumStates.setStatuRemplacement(posSerialNumStates.getStatuRemplacement());
			requestPosSerialNumStates.setMarque(posMarque.getLibelle());
			requestPosSerialNumStates.setStatusCode(posSerialNumStates.getStatus());
			requestPosSerialNumStates.setSerialNum(posSerialNumStates.getSerialNum());
			requestPosSerialNumStates.setModel(posModel.getLibelle());
			requestPosSerialNumStates.setStatus(posEtats.getLibelleFr());
			requestPosSerialNumStates.setDateSaisie(posSerialNumStates.getDateSaisie());
			list.add(requestPosSerialNumStates);
		}

		Page<RequestPosSerialNumStates> pages = new PageImpl<RequestPosSerialNumStates>(list,
				PageRequest.of(page, size, Sort.by(orders)), page1.getTotalElements());

		return pages;
	}

	@PostMapping("getPagePosSerialNumStatesToResi")
	public Page<RequestPosSerialNumStates> getPagePosSerialNumStatesToResi(@RequestBody PosRequestLibelle filter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir

	) {

		List<RequestPosSerialNumStates> list = new ArrayList<>();

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);

		orders.add(order1);
		Page<PosSerialNumStates> page1 = posSerialNumStatesRepository
				.getPagePosSerialNumStatesTo(PageRequest.of(page, size, Sort.by(orders)), filter.getLibelle().trim());
		for (PosSerialNumStates tr : page1.getContent()) {
			RequestPosSerialNumStates requestPosSerialNumStates = new RequestPosSerialNumStates();

			PosModel posModel = posModelRepository.getOne(tr.getModel());
			PosMarque posMarque = posMarqueRepository.getOne(posModel.getMarqueCode());
			PosEtats posEtats = posEtatsRepository.getOne(tr.getStatus());

			PosTerminal posTerminal = posTerminalRepository.findAllposBySerialNum(tr.getSerialNum());

			if (posTerminal != null) {
				requestPosSerialNumStates.setLibellePOS(posTerminal.getPosLibelle());
			} else {
				requestPosSerialNumStates.setLibellePOS("");
			}
			requestPosSerialNumStates.setNumSim(tr.getNumSim());

			requestPosSerialNumStates.setMarque(posMarque.getLibelle());
			requestPosSerialNumStates.setStatusCode(tr.getStatus());
			requestPosSerialNumStates.setSerialNum(tr.getSerialNum());
			requestPosSerialNumStates.setModel(posModel.getLibelle());
			requestPosSerialNumStates.setStatus(posEtats.getLibelleFr());
			requestPosSerialNumStates.setDateSaisie(tr.getDateSaisie());
			list.add(requestPosSerialNumStates);

		}

		Page<RequestPosSerialNumStates> pages = new PageImpl<RequestPosSerialNumStates>(list,
				PageRequest.of(page, size, Sort.by(orders)), page1.getTotalElements());
		return pages;
	}

	@GetMapping("getAllPosModelByMarque")
	public List<PosModel> getAllPosModelByMarque(@RequestBody Integer codeModel) {

		return posModelRepository.getAllModelByCodeMarque(codeModel);
	}

	@GetMapping("getAllPosModel")
	public List<PosModel> getAllPosModel() {

		return posModelRepository.findAll();
	}

	@GetMapping("getAllModelStock")
	public List<PosModel> getAllModelStock() {

		return posModelRepository.getAllModelStock();
	}

	@GetMapping("getAllPosSerialNumber")
	public List<PosSerialNumStates> getAllPosSerialNumber() {

		return posSerialNumStatesRepository.findAll();
	}

	@GetMapping("getOnePosSerialNumber/{id}")
	public PosSerialNumStates getOnePosSerialNumber(@PathVariable(value = "id") String serial) {

		return posSerialNumStatesRepository.findById(serial).get();
	}

	@GetMapping("getAllPosSerialNumberInStock")
	public List<PosSerialNumStates> getAllPosSerialNumberInStock() {

		return posSerialNumStatesRepository.getOnePosSerialNumStates();
	}

	@GetMapping("getOnePosSerialNumStatesByStock")
	public List<PosSerialNumStates> getOnePosSerialNumStatesByStock() {

		return posSerialNumStatesRepository.getOnePosSerialNumStatesByStock();
	}

	@GetMapping("getOnePosModel/{id}")
	public PosModel getOnePosModel(@PathVariable(value = "id") Integer serial) {

		return posModelRepository.findById(serial).get();
	}

	@PostMapping("readFileSerial")
	public ResponseEntity<String> readFilePorteur(@RequestParam("file") MultipartFile multipartFile,
			@RequestParam("model") Integer model) throws IOException {
		logger.info("*********begin reading file proteur (tsb)**********");

		System.out.println("model" + model);
		List<PosSerialNumStates> posSerialNumStates = new ArrayList<PosSerialNumStates>();

		List<PosHistoriqueOfSerial> posHistoriqueOfSerial = new ArrayList<PosHistoriqueOfSerial>();
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.out.println(timestamp);
		String Strdate = sdf3.format(timestamp);
		PosStock ps = posStockRepository.getPosStockByMode(model);

		try {
			// for (MultipartFile mf : multipartFile) {

			XSSFWorkbook workbook = new XSSFWorkbook(multipartFile.getInputStream());
			XSSFSheet sheet = workbook.getSheetAt(0);

			for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
				PosSerialNumStates posSerialNumState = new PosSerialNumStates();

				XSSFRow row = sheet.getRow(i);
				PosSerialNumStates posSerialNumStat = posSerialNumStatesRepository
						.getOnePosSerialNumStat(row.getCell(2).getStringCellValue());
				if (posSerialNumStat != null) {
					return new ResponseEntity<String>(
							" Serial Number with " + posSerialNumStat.getSerialNum() + " existed",
							HttpStatus.BAD_REQUEST);

				} else {
					if (!row.getCell(2).getStringCellValue().equals("")) {
						PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
						ph.setSerialNum(row.getCell(2).getStringCellValue().trim());
						ph.setDateSaisie(Strdate);
						ph.setStatus(1);
						posHistoriqueOfSerial.add(ph);

						ps.setStockDisponible(ps.getStockDisponible() + 1);
						posSerialNumState.setDateSaisie(Strdate);
						posSerialNumState.setSerialNum(row.getCell(2).getStringCellValue());
						posSerialNumState.setModel(model);
						posSerialNumState.setStatus(1);

						System.out.print(posSerialNumState.getSerialNum() + " mmm ");
						System.out.print(row.getCell(2) + " ");
						posSerialNumStates.add(posSerialNumState);
					} else {
						return new ResponseEntity<String>("Votre fichier contient des numéros de série vide",
								HttpStatus.BAD_REQUEST);
					}

				}
			}

			// }
			posStockRepository.save(ps);
			posSerialNumStatesRepository.saveAll(posSerialNumStates);
			posHistoriqueOfSerialRepository.saveAll(posHistoriqueOfSerial);

			return ResponseEntity.ok().body(gson.toJson("sucess"));

		} catch (Exception exception) {

			exception.printStackTrace();

		}

		return null;

	}

	@PutMapping("updatePosTerminal/{id}")
	public ResponseEntity<String> updatePosTerminal(@PathVariable(value = "id") Integer idTerminal,
			@RequestBody String SerialNum) {
		try {
			PosTerminal posTerminals = posTerminalRepository.getOne(idTerminal);
			System.out.println(" posTerminals =" + posTerminals.getSerialNum());

			PosSerialNumStates posSerialNumState = posSerialNumStatesRepository.getOne(posTerminals.getSerialNum());
			System.out.println("serial num 1 = " + posSerialNumState.getSerialNum());

			PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.findById(SerialNum).get();
			System.out.println("serial num= " + posSerialNumStates.getSerialNum());
			posSerialNumStates.setStatus(3);

			SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			System.out.println(timestamp);
			String Strdate = sdf3.format(timestamp);

			PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
			ph.setSerialNum(posSerialNumStates.getSerialNum());
			ph.setDateSaisie(Strdate);
			ph.setStatus(3);
			posHistoriqueOfSerialRepository.save(ph);

			posSerialNumStatesRepository.save(posSerialNumStates);
			posSerialNumStatesRepository.save(posSerialNumState);
			PosStock posStock1 = posStockRepository.getPosStockByMode(posSerialNumStates.getModel());

			posStock1.setStockDisponible(posStock1.getStockDisponible() - 1);
			posStock1.setStockConsome(posStock1.getStockConsome() + 1);
			posStockRepository.save(posStock1);
			posTerminals.setSerialNum(posSerialNumStates.getSerialNum());
			// posTerminals.setStatusSerial(Integer.toString(posSerialNumStates.getStatus()));
			posTerminalRepository.save(posTerminals);
			return ResponseEntity.ok().body(gson.toJson("PosTerminal update"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	@PutMapping("updatePosTerminalSerial/{id}")
	public ResponseEntity<String> updatePosTerminalSerial(@PathVariable(value = "id") String SerialNum,
			@RequestBody String posNum) {
		try {
			PosTerminal posTerminals = posTerminalRepository.getTerminal(posNum).get();
			SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			System.out.println(timestamp);
			String Strdate = sdf3.format(timestamp);
			String numSim = "";
			logger.info("Processing file: {}", posTerminals.getSerialNum());
			if (posTerminals != null) {
				if (!posTerminals.getSerialNum().equals("")) {

					logger.info("if ------------1-------", posTerminals.getSerialNum());
					PosSerialNumStates posSerialNumState = posSerialNumStatesRepository
							.getOne(posTerminals.getSerialNum());
					if (posSerialNumState.getNumSim() != null) {
						numSim = posSerialNumState.getNumSim();

					}
					logger.info("numSim -------------------", numSim);

					PosStock posStock1 = posStockRepository.getPosStockByMode(posSerialNumState.getModel());
					System.out.println(posStock1.getStockConsome());
					System.out.println(posSerialNumState.getStatus());
					if (posSerialNumState.getStatuResiliation() != null
							&& posSerialNumState.getStatuResiliation().equals("TPE retourne")) {
						logger.info("if TPE retourne-------------------");
						PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
						ph.setSerialNum(posSerialNumState.getSerialNum());
						ph.setDateSaisie(Strdate);
						ph.setStatus(1);

						if (posSerialNumState.getStatus() == 2) {
							logger.info("reservé-------------------");
							posStock1.setStockDisponible(posStock1.getStockDisponible() + 1);
							posStock1.setStockReserve(posStock1.getStockReserve() - 1);
							posStockRepository.save(posStock1);

						} else if (posSerialNumState.getStatus() == 3) {
							logger.info("affecté-------------------");
							posStock1.setStockDisponible(posStock1.getStockDisponible() + 1);
							posStock1.setStockConsome(posStock1.getStockConsome() - 1);
							posStockRepository.save(posStock1);

						} else if (posSerialNumState.getStatus() == 4) {
							logger.info("deployé-------------------");
							posStock1.setStockDisponible(posStock1.getStockDisponible() + 1);
							posStock1.setStockDeployer(posStock1.getStockDeployer() - 1);
							posStockRepository.save(posStock1);

						} else if (posSerialNumState.getStatus() == 5) {
							logger.info("HS-------------------");
							posStock1.setStockDisponible(posStock1.getStockDisponible() + 1);
							posStock1.setStockHS(posStock1.getStockHS() - 1);
							posStockRepository.save(posStock1);

						}
						posSerialNumState.setStatus(1);

						posHistoriqueOfSerialRepository.save(ph);

					}
					if (posSerialNumState.getStatuResiliation() != null
							&& posSerialNumState.getStatuResiliation().equals("TPE defaillant")) {
						logger.info("if TPE defaillant-------------------");
						PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
						ph.setSerialNum(posSerialNumState.getSerialNum());
						ph.setDateSaisie(Strdate);
						ph.setStatus(5);

						if (posSerialNumState.getStatus() == 2) {
							logger.info("reservé-------------------");
							posStock1.setStockHS(posStock1.getStockHS() + 1);
							posStock1.setStockReserve(posStock1.getStockReserve() - 1);
							posStockRepository.save(posStock1);
						} else if (posSerialNumState.getStatus() == 3) {
							logger.info("affectéé-------------------");
							posStock1.setStockHS(posStock1.getStockHS() + 1);
							posStock1.setStockConsome(posStock1.getStockConsome() - 1);
							posStockRepository.save(posStock1);
						} else if (posSerialNumState.getStatus() == 4) {
							logger.info("deployer-------------------");
							posStock1.setStockHS(posStock1.getStockHS() + 1);
							posStock1.setStockDeployer(posStock1.getStockDeployer() - 1);
							posStockRepository.save(posStock1);

						}
						posSerialNumState.setStatus(5);

						posHistoriqueOfSerialRepository.save(ph);

					} else {
						logger.info("else-------------------");
						PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
						ph.setSerialNum(posSerialNumState.getSerialNum());
						ph.setDateSaisie(Strdate);
						ph.setStatus(1);

						if (posSerialNumState.getStatus() == 2) {
							logger.info("reservé-------------------");
							posStock1.setStockDisponible(posStock1.getStockDisponible() + 1);
							posStock1.setStockReserve(posStock1.getStockReserve() - 1);
							posStockRepository.save(posStock1);

						} else if (posSerialNumState.getStatus() == 3) {
							logger.info("affectéé-------------------");
							posStock1.setStockDisponible(posStock1.getStockDisponible() + 1);
							posStock1.setStockConsome(posStock1.getStockConsome() - 1);
							posStockRepository.save(posStock1);

						} else if (posSerialNumState.getStatus() == 4) {
							logger.info("deployer-------------------");
							posStock1.setStockDisponible(posStock1.getStockDisponible() + 1);
							posStock1.setStockDeployer(posStock1.getStockDeployer() - 1);
							posStockRepository.save(posStock1);

						} else if (posSerialNumState.getStatus() == 5) {
							logger.info("HS-------------------");
							posStock1.setStockDisponible(posStock1.getStockDisponible() + 1);
							posStock1.setStockHS(posStock1.getStockHS() - 1);
							posStockRepository.save(posStock1);

						}
						posSerialNumState.setStatus(1);
						posHistoriqueOfSerialRepository.save(ph);
					}

					posStockRepository.save(posStock1);
					posSerialNumState.setStatuRemplacement(0);
					posSerialNumState.setNumSim(null);
					posSerialNumStatesRepository.save(posSerialNumState);

					PosSerialNumStates posSerialNumSta = posSerialNumStatesRepository.getOne(SerialNum);

					PosStock posStock = posStockRepository.getPosStockByMode(posSerialNumSta.getModel());

					posStock.setStockDisponible(posStock.getStockDisponible() - 1);
					posStock.setStockDeployer(posStock.getStockDeployer() + 1);

					posTerminals.setSerialNum(SerialNum);

					PosHistoriqueOfSerial ph1 = new PosHistoriqueOfSerial();
					ph1.setSerialNum(posSerialNumSta.getSerialNum());
					ph1.setDateSaisie(Strdate);
					ph1.setStatus(4);

					// posStockRepository.save(posStock1);

					posHistoriqueOfSerialRepository.save(ph1);
					posStockRepository.save(posStock);
					posTerminalRepository.save(posTerminals);
					posSerialNumSta.setStatus(4);
					posSerialNumSta.setNumSim(numSim);
					posSerialNumStatesRepository.save(posSerialNumSta);

				} else {

					logger.info("else 2-------------------");

					PosSerialNumStates posSerialNumSta = posSerialNumStatesRepository.getOne(SerialNum);
					posSerialNumSta.setStatus(4);

					PosStock posStock = posStockRepository.getPosStockByMode(posSerialNumSta.getModel());

					posStock.setStockDisponible(posStock.getStockDisponible() - 1);
					posStock.setStockDeployer(posStock.getStockDeployer() + 1);

					posTerminals.setSerialNum(SerialNum);

					PosHistoriqueOfSerial ph1 = new PosHistoriqueOfSerial();
					ph1.setSerialNum(posSerialNumSta.getSerialNum());
					ph1.setDateSaisie(Strdate);
					ph1.setStatus(4);

					posHistoriqueOfSerialRepository.save(ph1);
					posStockRepository.save(posStock);

					posTerminalRepository.save(posTerminals);

					posSerialNumStatesRepository.save(posSerialNumSta);

				}

			}

			return ResponseEntity.ok().body(gson.toJson("PosTerminal update"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	@PutMapping("InstallPosTerminal/{id}")
	public ResponseEntity<String> InstallPosTerminal(@PathVariable(value = "id") Integer idTerminal) {

		PosTerminal posTerminals = posTerminalRepository.getOne(idTerminal);
		PosSerialNumStates posSerialNumState = posSerialNumStatesRepository.getOne(posTerminals.getSerialNum());

		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.out.println(timestamp);
		String Strdate = sdf3.format(timestamp);

		PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
		ph.setSerialNum(posSerialNumState.getSerialNum());
		ph.setDateSaisie(Strdate);
		ph.setStatus(4);

		posSerialNumState.setStatus(4);

		PosModel posModel = posModelRepository.getOne(posSerialNumState.getModel());

		PosStock posStock = posStockRepository.getPosStockByMode(posModel.getModelCode());

		posStock.setStockDeployer(posStock.getStockDeployer() + 1);
		posStock.setStockConsome(posStock.getStockConsome() - 1);

		posSerialNumStatesRepository.save(posSerialNumState);
		posHistoriqueOfSerialRepository.save(ph);
		posStockRepository.save(posStock);
		// posTerminals.setStatusSerial(Integer.toString(posSerialNumState.getStatus()));
		// posTerminalRepository.save(posTerminals);
		return ResponseEntity.ok().body(gson.toJson("PosTerminal update"));

	}

//	@PutMapping("addNumSimPosSerialNumStates/{id}")
//	public ResponseEntity<String> addNumSimPosSerialNumStates(@PathVariable(value = "id") String numSerie ,@RequestBody String numSim) {
//
//		PosSerialNumStates posSerial = posSerialNumStatesRepository.findByNumSim(numSim);
//		if(posSerial!=null) {
//			return new ResponseEntity<String>(
//					" Ce numero "+numSim+" est affect a " +posSerial ,
//					HttpStatus.BAD_REQUEST);
//		}else {
//		PosSerialNumStates posSerialNumState = posSerialNumStatesRepository.getOne(numSerie);
//		posSerialNumState.setNumSim(numSim);
//		posSerialNumStatesRepository.save(posSerialNumState);
//		return ResponseEntity.ok().body(gson.toJson("success"));
//		}
//	}

	@PutMapping("addNumSimPosSerialNumStates/{id}")
	public ResponseEntity<String> addNumSimPosSerialNumStates(@PathVariable(value = "id") String numSerie,
			@RequestBody String numSim) {
		PosSerialNumStates posSerial = posSerialNumStatesRepository.findByNumSim(numSim);
		if (posSerial != null) {
			return new ResponseEntity<String>("Ce numero " + numSim + " est déjà affecté à " + posSerial.getSerialNum(),
					HttpStatus.BAD_REQUEST);
		} else {
			PosSerialNumStates posSerialNumState = posSerialNumStatesRepository.getOne(numSerie);
			if (posSerialNumState != null) {
				posSerialNumState.setNumSim(numSim);
				posSerialNumStatesRepository.save(posSerialNumState);
				return ResponseEntity.ok().body(gson.toJson("success"));
			} else {
				return new ResponseEntity<String>("Le numéro de série " + numSerie + " n'a pas été trouvé.",
						HttpStatus.NOT_FOUND);
			}
		}
	}

	@PutMapping("activatePosTerminal/{id}")
	public ResponseEntity<String> activatePosTerminal(@PathVariable(value = "id") Integer idPos) {

		PosTerminal posTerminal = posTerminalRepository.getOne(idPos);
		posTerminal.setStatus("ACTIVE");
		posTerminalRepository.save(posTerminal);

		return ResponseEntity.ok().body(gson.toJson("PosTerminal Active"));
	}

	@PutMapping("desactivatePosTerminal/{id}")
	public ResponseEntity<String> desactivatePosTerminal(@PathVariable(value = "id") Integer idPos) {

		PosTerminal posTerminal = posTerminalRepository.getOne(idPos);
		posTerminal.setStatus("DESACTIVE");
		posTerminalRepository.save(posTerminal);

		return ResponseEntity.ok().body(gson.toJson("PosTerminal Désactive"));
	}

	@PutMapping("mettreStatusSerialHs/{id}")
	public ResponseEntity<String> mettreStatusSerialHs(@PathVariable(value = "id") String serial) {

		PosSerialNumStates p1 = posSerialNumStatesRepository.findById(serial).get();

		p1.setStatus(5);

		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.out.println(timestamp);
		String Strdate = sdf3.format(timestamp);

		PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
		ph.setSerialNum(p1.getSerialNum());
		ph.setDateSaisie(Strdate);
		ph.setStatus(5);
		posSerialNumStatesRepository.save(p1);

		posHistoriqueOfSerialRepository.save(ph);
		PosStock posStock = posStockRepository.getPosStockByMode(p1.getModel());
		posStock.setStockDeployer(posStock.getStockDeployer() - 1);
		posStock.setStockHS(posStock.getStockHS() + 1);
		posStockRepository.save(posStock);
		return ResponseEntity.ok().body(gson.toJson("Serial HS"));

	}

	@PutMapping("mettreStatusSerialDeteriorated/{id}")
	public ResponseEntity<String> mettreStatusSerialDeteriorated(@PathVariable(value = "id") String serial) {

		PosSerialNumStates p1 = posSerialNumStatesRepository.findById(serial).get();

		p1.setStatus(6);

		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.out.println(timestamp);
		String Strdate = sdf3.format(timestamp);

		PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
		ph.setSerialNum(p1.getSerialNum());
		ph.setDateSaisie(Strdate);
		ph.setStatus(6);
		posSerialNumStatesRepository.save(p1);
		posHistoriqueOfSerialRepository.save(ph);

		return ResponseEntity.ok().body(gson.toJson("Serial Deteriorated"));

	}

	@PutMapping("mettreStatusSerialInStock/{id}")
	public ResponseEntity<String> mettreStatusSerialInStock(@PathVariable(value = "id") String serial,
			@RequestBody RequestDeletePos requestDeletePos) {

		PosTerminal posTerminal = posTerminalRepository.findAllposBySerialNum(serial);
		PosSerialNumStates p1 = posSerialNumStatesRepository.findById(serial).get();
		p1.setStatuResiliation(requestDeletePos.getStatuSerial());
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.out.println(timestamp);
		String Strdate = sdf3.format(timestamp);
		PosStock posStock = posStockRepository.getPosStockByMode(p1.getModel());
		if (posTerminal != null) {
			posTerminal.setSerialNum("");

			posTerminalRepository.save(posTerminal);
			if (requestDeletePos.getStatuSerial().equals("TPE retourne")) {
				PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
				ph.setSerialNum(p1.getSerialNum());
				ph.setDateSaisie(Strdate);
				ph.setStatus(1);
				if (p1.getStatus() == 2) {
					posStock.setStockReserve(posStock.getStockReserve() - 1);
					posStock.setStockDisponible(posStock.getStockDisponible() + 1);

				} else if (p1.getStatus() == 3) {
					posStock.setStockConsome(posStock.getStockConsome() - 1);
					posStock.setStockDisponible(posStock.getStockDisponible() + 1);
				} else if (p1.getStatus() == 4) {
					posStock.setStockDeployer(posStock.getStockDeployer() - 1);
					posStock.setStockDisponible(posStock.getStockDisponible() + 1);
				} else if (p1.getStatus() == 5) {
					posStock.setStockHS(posStock.getStockHS() - 1);
					posStock.setStockDisponible(posStock.getStockDisponible() + 1);
				}
				p1.setStatus(1);
				posSerialNumStatesRepository.save(p1);
				posHistoriqueOfSerialRepository.save(ph);
				posStockRepository.save(posStock);
				return ResponseEntity.ok().body(gson.toJson("Serial Affecte"));

			}

			else {
				PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
				ph.setSerialNum(p1.getSerialNum());
				ph.setDateSaisie(Strdate);
				ph.setStatus(6);
				if (p1.getStatus() == 2) {
					posStock.setStockReserve(posStock.getStockReserve() - 1);
					posStock.setStockHS(posStock.getStockHS() + 1);

				} else if (p1.getStatus() == 3) {
					posStock.setStockConsome(posStock.getStockConsome() - 1);
					posStock.setStockHS(posStock.getStockHS() + 1);
				} else if (p1.getStatus() == 4) {
					posStock.setStockDeployer(posStock.getStockDeployer() - 1);
					posStock.setStockHS(posStock.getStockHS() + 1);
				} else if (p1.getStatus() == 5) {
					posStock.setStockHS(posStock.getStockHS() - 1);
					posStock.setStockHS(posStock.getStockHS() + 1);
				}
				p1.setStatus(6);

				posSerialNumStatesRepository.save(p1);
				posHistoriqueOfSerialRepository.save(ph);
				posStockRepository.save(posStock);
				return ResponseEntity.ok().body(gson.toJson("Serial detroie"));

			}
		} else {

			if (requestDeletePos.getStatuSerial().equals("TPE retourne")) {
				PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
				ph.setSerialNum(p1.getSerialNum());
				ph.setDateSaisie(Strdate);
				ph.setStatus(1);
				if (p1.getStatus() == 2) {
					posStock.setStockReserve(posStock.getStockReserve() - 1);
					posStock.setStockDisponible(posStock.getStockDisponible() + 1);

				} else if (p1.getStatus() == 3) {
					posStock.setStockConsome(posStock.getStockConsome() - 1);
					posStock.setStockDisponible(posStock.getStockDisponible() + 1);
				} else if (p1.getStatus() == 4) {
					posStock.setStockDeployer(posStock.getStockDeployer() - 1);
					posStock.setStockDisponible(posStock.getStockDisponible() + 1);
				} else if (p1.getStatus() == 5) {
					posStock.setStockHS(posStock.getStockHS() - 1);
					posStock.setStockDisponible(posStock.getStockDisponible() + 1);
				}
				p1.setStatus(1);
				posSerialNumStatesRepository.save(p1);
				posHistoriqueOfSerialRepository.save(ph);
				posStockRepository.save(posStock);
				return ResponseEntity.ok().body(gson.toJson("Serial Affecte"));

			}

			else {
				PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
				ph.setSerialNum(p1.getSerialNum());
				ph.setDateSaisie(Strdate);
				ph.setStatus(6);
				if (p1.getStatus() == 2) {
					posStock.setStockReserve(posStock.getStockReserve() - 1);
					posStock.setStockHS(posStock.getStockHS() + 1);

				} else if (p1.getStatus() == 3) {
					posStock.setStockConsome(posStock.getStockConsome() - 1);
					posStock.setStockHS(posStock.getStockHS() + 1);
				} else if (p1.getStatus() == 4) {
					posStock.setStockDeployer(posStock.getStockDeployer() - 1);
					posStock.setStockHS(posStock.getStockHS() + 1);
				} else if (p1.getStatus() == 5) {
					posStock.setStockHS(posStock.getStockHS() - 1);
					posStock.setStockHS(posStock.getStockHS() + 1);
				}
				p1.setStatus(6);

				posSerialNumStatesRepository.save(p1);
				posHistoriqueOfSerialRepository.save(ph);
				posStockRepository.save(posStock);
				return ResponseEntity.ok().body(gson.toJson("Serial detroie"));

			}
		}

	}

	@PutMapping("reprisedeStock/{id}")
	public ResponseEntity<String> reprisedeStock(@PathVariable(value = "id") String serial,
			@RequestBody RequestDeletePos requestDeletePos) {

		PosTerminal posTerminal = posTerminalRepository.findAllposBySerialNum(serial);
		PosSerialNumStates p1 = posSerialNumStatesRepository.findById(serial).get();
		// p1.setStatuResiliation(requestDeletePos.getStatuSerial());
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.out.println(timestamp);
		String Strdate = sdf3.format(timestamp);
		PosStock posStock = posStockRepository.getPosStockByMode(p1.getModel());
		if (posTerminal != null) {
			posTerminal.setSerialNum("");

			posTerminalRepository.save(posTerminal);
			if (requestDeletePos.getStatuSerial().equals("TPE retourne")) {
				PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
				ph.setSerialNum(p1.getSerialNum());
				ph.setDateSaisie(Strdate);
				ph.setStatus(1);

				posStock.setStockHS(posStock.getStockHS() - 1);
				posStock.setStockDisponible(posStock.getStockDisponible() + 1);
				p1.setStatuResiliation(null);
				p1.setStatuRemplacement(null);
				p1.setStatus(1);
				posSerialNumStatesRepository.save(p1);
				posHistoriqueOfSerialRepository.save(ph);
				posStockRepository.save(posStock);
				return ResponseEntity.ok().body(gson.toJson("Serial Affecte"));

			}

			else {
				PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
				ph.setSerialNum(p1.getSerialNum());
				ph.setDateSaisie(Strdate);
				ph.setStatus(6);
				p1.setStatuResiliation(null);
				p1.setStatuRemplacement(null);
				p1.setStatus(6);

				posSerialNumStatesRepository.save(p1);
				posHistoriqueOfSerialRepository.save(ph);
				posStockRepository.save(posStock);
				return ResponseEntity.ok().body(gson.toJson("Serial detroie"));

			}

		}
		return null;

	}

	@GetMapping("getAllCommissionAchatFransaBank")
	public List<CommissionAchatFransaBankDisplay> getAllCommissionAchatFransaBank() {
		List<CommissionAchatFransaBank> c = commissionAchatFransaBankRepository.findAll();
		List<CommissionAchatFransaBankDisplay> list = new ArrayList<>();
		DecimalFormat df = new DecimalFormat("0.00");
		for (CommissionFransaBank items : commissionFransaBankRepository.findCommissionFransaBankByCode()) {
			CommissionAchatFransaBankDisplay b = new CommissionAchatFransaBankDisplay();
			double vFix = (Double.parseDouble(items.getCommissionAcq())
					+ Double.parseDouble(items.getCommissionIssuer())) / 100;

			b.setValeurMin("0.00");
			b.setValeurMax("2000.00");
			b.setValeurFix(String.valueOf(df.format(vFix)));
			b.setValeurVarivable("0 %");
			list.add(b);
		}
		for (CommissionAchatFransaBank item : c) {

			CommissionAchatFransaBankDisplay a = new CommissionAchatFransaBankDisplay();
			double valeurMin = Double.parseDouble(item.getValeurMin()) / 100;
			double valeurMax = Double.parseDouble(item.getValeurMax()) / 100;
			double valeurFix = Double.parseDouble(item.getValeurFix()) / 100;
			String valeurVarivable = item.getValeurVarivable().concat(" %");

			a.setValeurVarivable(valeurVarivable);
			a.setValeurMin(String.valueOf(df.format(valeurMin)));
			a.setValeurMax(String.valueOf(df.format(valeurMax)));
			a.setValeurFix(String.valueOf(df.format(valeurFix)));
			a.setCmi(item.getCmi());
			a.setCode(item.getCode());
			a.setCpi(item.getCpi());
			list.add(a);

		}

		return list;
	}

	@GetMapping("getAllCommissionInternetFSBK")
	public List<CommissionAchatFransaBankDisplay> getAllCommissionInternetFSBK() {
		List<CommissionAchatInternetFB> c = commissionFransaBankInternetRepository
				.getOneCommissionAchatInternetFBfindByType();
		List<CommissionAchatFransaBankDisplay> list = new ArrayList<>();
		DecimalFormat df = new DecimalFormat("0.00");

		for (CommissionAchatInternetFB item : c) {
			CommissionAchatFransaBankDisplay a = new CommissionAchatFransaBankDisplay();
			double valeurMin = Double.parseDouble(item.getValeurMin()) / 100;
			double valeurMax = Double.parseDouble(item.getValeurMax()) / 100;
			double valeurFix = Double.parseDouble(item.getValeurFix()) / 100;
			String valeurVarivable = item.getValeurVarivable().concat(" %");
			a.setValeurVarivable(valeurVarivable);
			a.setValeurMin(String.valueOf(df.format(valeurMin)));
			a.setValeurMax(String.valueOf(df.format(valeurMax)));
			a.setValeurFix(String.valueOf(df.format(valeurFix)));
			list.add(a);

		}

		return list;

	}

	@PostMapping(value = "generatePDF/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public byte[] generatePDF(@PathVariable(value = "id") Integer request) throws IOException, DocumentException {

		String DEST = propertyService.getPosFilesPath() + "contrat_TPE_FSBK.pdf";
		TpeRequest tpeRequest = tpeRequestRepository.getOne(request);
		tpeRequest.setStatuContrat("1");

		// positionPDF();

		// File file = new File(DEST);
		// file.getParentFile().mkdirs();
		// manipulatePdf("src/main/resources/contrat_TPE_FSBK.pdf", DEST);
		AgenceAdministration ag = agenceAdministrationRepository.findoneBycode(tpeRequest.getAgence());
		String adresse = tpeRequest.getAdresse();
		String city = tpeRequest.getCity();
		String codeZip = tpeRequest.getCodeZip();
		String phone = tpeRequest.getPhone();
		String username = tpeRequest.getUserName();

		String name = SecurityContextHolder.getContext().getAuthentication().getName();

		Date date = new Date();

		DateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd");

		String dateCreation = formatter1.format(date);

		PdfReader reader = new PdfReader(propertyService.getPosFilesPath() + "contrat_TPE.pdf");

		// PdfReader reader = new PdfReader(propertyService.getPosFilesPath() +
		// "contrat_TPE_FSBK.pdf");

		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(DEST));
		PdfContentByte cb = stamper.getOverContent(2);
		Font f = new Font();

		PdfContentByte cb1 = stamper.getOverContent(6);

		/****************** date signe ***********************/

		ColumnText ct1 = new ColumnText(cb1);
		ct1.setSimpleColumn(150f, 90f, 500f, 398f);
		Paragraph dateSig = new Paragraph(new Phrase(20, dateCreation, f));
		ct1.addElement(dateSig);
		ct1.go();
		/*********************** END ***************************/
		/****************** code postal ***********************/

		ColumnText ct = new ColumnText(cb);
		ct.setSimpleColumn(150f, 48f, 250f, 384f);
		Paragraph postal = new Paragraph(new Phrase(20, codeZip, f));
		ct.addElement(postal);
		ct.go();
		/*********************** END ***************************/

		/****************** m1 ***********************/

		ct.setSimpleColumn(150f, 48f, 550f, 528f);
		Paragraph m1 = new Paragraph(new Phrase(20, ag.getManager(), f));
		ct.addElement(m1);
		ct.go();
		/*********************** END ***************************/

		/****************** m2 ***********************/
		String libelleagence = ag.getInitialZone() + ag.getLibelle();
		ct.setSimpleColumn(550f, 48f, 420f, 528f);
		Paragraph m2 = new Paragraph(new Phrase(20, libelleagence, f));
		ct.addElement(m2);
		ct.go();
		/*********************** END ***************************/

		/****************** ville ***********************/

		ct.setSimpleColumn(550f, 48f, 250f, 384f);
		Paragraph ville = new Paragraph(new Phrase(20, city, f));
		ct.addElement(ville);
		ct.go();
		/*********************** END ***************************/

		/****************** tel ***********************/

		ct.setSimpleColumn(550f, 48f, 430f, 384f);
		Paragraph tel = new Paragraph(new Phrase(20, phone, f));
		ct.addElement(tel);
		ct.go();
		/*********************** END ***************************/

		/****************** adresse ***********************/

		ct.setSimpleColumn(180f, 48f, 550f, 397f);
		Paragraph adresse1 = new Paragraph(new Phrase(20, adresse, f));
		ct.addElement(adresse1);
		ct.go();
		/*********************** END ***************************/

		/****************** capital ***********************/
		String capital = tpeRequest.getRevenue();
		ct.setSimpleColumn(180f, 48f, 250f, 414f);
		Paragraph capital1 = new Paragraph(new Phrase(20, capital, f));
		ct.addElement(capital1);
		ct.go();
		/*********************** END ***************************/

		/****************** Raison ***********************/
		String raison = tpeRequest.getRso();
		ct.setSimpleColumn(180f, 48f, 250f, 428f);
		Paragraph raison1 = new Paragraph(new Phrase(20, raison, f));
		ct.addElement(raison1);
		ct.go();
		/*********************** END ***************************/

		/****************** num ***********************/
		String numCommerce = tpeRequest.getRc();
		ct.setSimpleColumn(187f, 48f, 260f, 371f);
		Paragraph num = new Paragraph(new Phrase(20, numCommerce, f));
		ct.addElement(num);
		ct.go();
		/*********************** END ***************************/

		/****************** date ***********************/

		BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, "Cp1252", BaseFont.EMBEDDED);
		Font d = new Font(bf, 10);
		ct.setSimpleColumn(380f, 48f, 262f, 361f);
		Paragraph date1 = new Paragraph(new Phrase(10, "", d));
		ct.addElement(date1);
		ct.go();
		/*********************** END ***************************/

		/****************** CNRC ***********************/
		String cnrc1 = "";

		ct.setSimpleColumn(550f, 48f, 390f, 371f);
		Paragraph cnrc = new Paragraph(new Phrase(20, cnrc1, f));
		ct.addElement(cnrc);
		ct.go();
		/*********************** END ***************************/

		/****************** identifiant fiscale ***********************/
		String identifiant = tpeRequest.getNif();
		ct.setSimpleColumn(180f, 48f, 250f, 358f);
		Paragraph fiscale = new Paragraph(new Phrase(20, identifiant, f));
		ct.addElement(fiscale);
		ct.go();
		/*********************** END ***************************/

		/****************** nom ***********************/
		String[] arrOfStr = username.split(" ");
		Font s = new Font(bf, 5);
		ct.setSimpleColumn(195f, 48f, 470f, 313f);
		Paragraph nom1 = new Paragraph(new Phrase(5, tpeRequest.getUserName(), s));
		ct.addElement(nom1);
		ct.go();
		/*********************** END ***************************/

		/****************** prenom ***********************/
		String prenom = tpeRequest.getAgentName();
		Font s1 = new Font(bf, 7);

		ct.setSimpleColumn(285f, 48f, 462f, 314f);
		Paragraph prenom1 = new Paragraph(new Phrase(7, prenom, s1));
		ct.addElement(prenom1);
		ct.go();
		/*********************** END ***************************/

		/****************** qualite ***********************/
		String qualite1 = "";
		ct.setSimpleColumn(550f, 48f, 420f, 327f);
		Paragraph qualite = new Paragraph(new Phrase(20, qualite1, f));
		ct.addElement(qualite);
		ct.go();
		/*********************** END ***************************/

		/****************** nCompte ***********************/
		String numCompte = tpeRequest.getAccountNumber();
		ct.setSimpleColumn(150f, 180f, 350f, 306f);
		Paragraph nCompte = new Paragraph(new Phrase(20, numCompte, f));
		ct.addElement(nCompte);
		ct.go();
		/*********************** END ***************************/

		/****************** agence ***********************/

		ct.setSimpleColumn(200f, 90f, 350f, 287f);
		Paragraph agence1 = new Paragraph(new Phrase(20, libelleagence, f));
		ct.addElement(agence1);
		ct.go();
		/*********************** END ***************************/

		/****************** email ***********************/
		String mail = "";
		ct.setSimpleColumn(120f, 90f, 500f, 273f);
		Paragraph email = new Paragraph(new Phrase(20, tpeRequest.getEmail(), f));
		ct.addElement(email);
		ct.go();
		/*********************** END ***************************/

		stamper.close();
		reader.close();

		File fileDest = new File(DEST);

		InputStream in = new FileInputStream(fileDest);

		DateFormat formatter2 = new SimpleDateFormat("yy");
		String ans = formatter2.format(new Date());
		String s3;
		formatter1 = new SimpleDateFormat("yyyy-MM-dd");
		s3 = formatter1.format(date);
		SequentielSerie a = new SequentielSerie();
		a.setDateG(s3);
		a = sequentielSerieRepository.save(a);
		String nbSequentielle = String.format("%05d", a.getSequenceSer());

		String NumeroContrat = "035" + ag.getInitial() + ans + nbSequentielle;
		tpeRequest.setNumContrat(NumeroContrat);
		tpeRequestRepository.save(tpeRequest);
		return IOUtils.toByteArray(in);

	}

	@GetMapping("getCommissionsByMerchant/{id}")
	RequestCommissionTpe getCommissionsByMerchant(@PathVariable(value = "id") String AccountCode) {
		String ribAcc = AccountCode.substring(1, AccountCode.length());

		Account a = accountRepository.findByAccountNum(ribAcc);
		List<CommissionTpe> com = null;
		Set<TpePendingDisplay> tpePendingDisplays = new HashSet<>();
		List<CommissionTpeRequest> commissionTpeRequest = new ArrayList<>();
		List<CommissionTpeRequest> commissionTpeRequestInter = new ArrayList<>();
		List<RequestCommissionTpe> list = new ArrayList<>();
		// List<TpeRequestDisplayDetails> commissionTpeRequest = new ArrayList<>();
		RequestCommissionTpe requestCommissionTpe = new RequestCommissionTpe();

		if (a != null) {

			Merchant m = merchantRepository.findByNumAccount(a.getAccountCode());

			if (m != null && !m.getMerchantStatus().equals("4")) {
				com = commissionTpeRepository.findByMerchantCode(m.getMerchantCode());
				requestCommissionTpe.setCommissionTypeCode(m.getCommissionType());
				for (CommissionTpe item : com) {

					System.out.println(item);
					requestCommissionTpe.setCommissionInterNational(item.getValeurCommissionInternationaleFixe());
					requestCommissionTpe.setCommissionNational(item.getValeurCommissionNationaleFix());

					if (item.getCommissionNational().equals("1")) {
						CommissionTpeRequest cTpe = new CommissionTpeRequest();
						cTpe.setMontantRefMax(item.getMontantRefMax());
						cTpe.setMontantRefMin(item.getMontantRefMin());
						cTpe.setValeurComFixMin(item.getValeurComFixMin());
						cTpe.setValeurComVariableMin(item.getValeurComVariableMin());
						cTpe.setCommissionType(item.getCommissionType());
						cTpe.setCommissionNational(item.getCommissionNational());
						cTpe.setCommissionInterNational(item.getCommissionInterNational());
						cTpe.setMerchantCode(item.getMerchantCode());
						commissionTpeRequest.add(cTpe);
						requestCommissionTpe.setCommissionTpes(commissionTpeRequest);

					}

					if (item.getCommissionInterNational().equals("1")) {
						CommissionTpeRequest cTpe = new CommissionTpeRequest();
						cTpe.setMontantRefMax(item.getMontantRefMax());
						cTpe.setMontantRefMin(item.getMontantRefMin());
						cTpe.setValeurComFixMin(item.getValeurComFixMin());
						cTpe.setValeurComVariableMin(item.getValeurComVariableMin());
						cTpe.setCommissionType(item.getCommissionType());
						cTpe.setCommissionNational(item.getCommissionNational());
						cTpe.setCommissionInterNational(item.getCommissionInterNational());
						cTpe.setMerchantCode(item.getMerchantCode());
						commissionTpeRequestInter.add(cTpe);

						requestCommissionTpe.setCommissionTpesInter(commissionTpeRequestInter);

					}

				}
				return requestCommissionTpe;
			} else {

				return requestCommissionTpe;
			}
		} else {
			return requestCommissionTpe;
		}

	}

	@PutMapping("ValidationLevel2RequestTpe/{id}")
	public ResponseEntity<String> ValidationLevel2RequestTpe(@PathVariable(value = "id") Integer idTpe) {
		logger.info("**********begin validate Agence**************");

		TpeRequest t = tpeRequestRepository.findById(idTpe).get();
		Set<PendingTpe> pendingTpes = t.getPendingTpes();
		for (PendingTpe p : pendingTpes) {

			p.setStatus(4);
			pendingTpes.add(p);

		}
		t.setStatus(4);
		t.setDateDecision(new Date());
		pendingTpeRepository.saveAll(pendingTpes);
		tpeRequestRepository.save(t);

		/******************** Request historique **********************/
		HistoriqueRequestPos historiqueRequestPos = new HistoriqueRequestPos();
		historiqueRequestPos.setDateStatu(new Date());
		historiqueRequestPos.setStatut("Demande approuvée");
		historiqueRequestPos.setRequestCode(t.getRequestCode());
		historiqueRequestPos.setOperateur("Chef D'Agence");
		historiqueRequestPosRepository.save(historiqueRequestPos);
		/******************** Request historique **********************/

		AgenceAdministration ag = agenceAdministrationRepository.findByIdAgence(t.getAgence()).get();
		Zone z = zoneRepository.findByCodeZone(ag.getCodeZone()).get();

		try {
			logger.info("**********begin mail validate Agence to zone**************");

			String nomClient = t.getRso();
			String numCompte = "0" + t.getAccountNumber();
			myEmailService.sendOtpMessage(z.getEmail(), "Validation Directeur d'agence",
					"Bonjour,\n\nNous vous informons qu’une demande de POS du commerçant \"" + nomClient
							+ "\" domicilié sous le N° de compte \"" + numCompte
							+ "\" le directeur d’agence et demeure en attente de validation réseau..\n\nCordialement");
			logger.info("**********end mail validate Agence to zone**************");

		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}

		try {
			logger.info("**********begin mail validate Agence to charge clientele**************");

			String nomClient = t.getRso();
			String numCompte = "0" + t.getAccountNumber();
			myEmailService.sendOtpMessage(t.getEmailCharge(), "Validation Directeur d'agence",
					"Bonjour,\n\n" + "Nous vous informons qu’une demande de POS du commerçant \"" + nomClient
							+ "\" domicilié sous le N° de compte \"" + numCompte + "\" a été " + "Validée"
							+ " par le chef d’agence.\n\n" + "Cordialement,\n");
			logger.info("**********end mail validate Agence to charge clientele**************");

		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}
		logger.info("**********end validate Agence**************");

		return ResponseEntity.ok().body(gson.toJson("validation level 2"));

	}

	@PutMapping("RejectedRequestTpe/{id}")
	public ResponseEntity<String> RejectedRequestTpe(@PathVariable(value = "id") Integer idTpe,
			@RequestBody DescreptionRejectRequest descreptionRejectRequest) {
		logger.info("**********begin rejected chef d'agence**************");

		TpeRequest t = tpeRequestRepository.getOne(idTpe);
		t.setDateDecision(new Date());
		t.setDescriptionReject(descreptionRejectRequest.getDescriptionReject());
		Set<PendingTpe> pendingTpes = t.getPendingTpes();
		List<PosHistoriqueOfSerial> phs = new ArrayList<>();
		List<PosSerialNumStates> psn = new ArrayList<>();
		List<PosStock> posStock = new ArrayList<>();

		/******************** Request historique **********************/
		HistoriqueRequestPos historiqueRequestPos = new HistoriqueRequestPos();
		historiqueRequestPos.setDateStatu(new Date());
		historiqueRequestPos.setRequestCode(t.getRequestCode());
		historiqueRequestPos.setOperateur("Chef D'Agence");
		/******************** Request historique **********************/
		if (descreptionRejectRequest.getStatus().equals("Reversible")) {

			t.setStatus(7);
			historiqueRequestPos.setStatut("Demande rejetée Mais Réversible");
			historiqueRequestPosRepository.save(historiqueRequestPos);
		} else {
			t.setStatus(2);

			for (PendingTpe item : pendingTpes) {

				item.setStatus(2);
				pendingTpes.add(item);

				if (item.getSerialNum() != null) {
					PosSerialNumStates pn = posSerialNumStatesRepository.getOne(item.getSerialNum());
					PosStock ps = posStockRepository.getPosStockByMode(pn.getModel());
					pn.setStatus(1);
					ps.setStockDisponible(ps.getStockDisponible() + 1);
					ps.setStockReserve(ps.getStockReserve() - 1);
					SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					System.out.println(timestamp);
					String Strdate = sdf3.format(timestamp);

					PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
					ph.setSerialNum(pn.getSerialNum());
					ph.setDateSaisie(Strdate);
					ph.setStatus(1);

					posStock.add(ps);
					phs.add(ph);
					psn.add(pn);
					historiqueRequestPos.setStatut("Demande rejetée");
					historiqueRequestPosRepository.save(historiqueRequestPos);

				}

			}
		}
		posHistoriqueOfSerialRepository.saveAll(phs);
		posSerialNumStatesRepository.saveAll(psn);
		posStockRepository.saveAll(posStock);
		pendingTpeRepository.saveAll(pendingTpes);
		tpeRequestRepository.save(t);

		try {
			String nomClient = t.getRso();
			String numCompte = "0" + t.getAccountNumber();
			myEmailService.sendOtpMessage(t.getEmailCharge(), "Rejet de la demande POS par le directeur d'agence",
					"Bonjour,\n\n" + "Nous vous informons qu’une demande de POS du commerçant \"" + nomClient
							+ "\" domicilié sous le N° de compte \"" + numCompte + "\" a été " + "Rejetée"
							+ " par le directeur d’agence ; merci de consulter le motif de rejet.\n\n"
							+ "Cordialement,\n");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}
		logger.info("**********begin rejected chef d'agence**************");

		return ResponseEntity.ok().body(gson.toJson("rejected"));

	}

	@PutMapping("RejectedRequestComm/{id}")
	public ResponseEntity<String> RejectedRequestComm(@PathVariable(value = "id") Integer idTpe,
			@RequestBody String descriptionReject) {
		logger.info("**********begin rejected Reseaux**************");

		TpeRequest t = tpeRequestRepository.getOne(idTpe);
		t.setDateDecision(new Date());
		t.setDescriptionReject(descriptionReject);
		Set<PendingTpe> pendingTpes = t.getPendingTpes();

		AgenceAdministration ag = agenceAdministrationRepository.findByIdAgence(t.getAgence()).get();
		List<PosHistoriqueOfSerial> phs = new ArrayList<>();
		List<PosSerialNumStates> psn = new ArrayList<>();
		List<PosStock> posStock = new ArrayList<>();
		t.setStatus(2);
		for (PendingTpe item : pendingTpes) {
			item.setStatus(2);
			pendingTpes.add(item);
			if (item.getSerialNum() != null) {
				PosSerialNumStates pn = posSerialNumStatesRepository.getOne(item.getSerialNum());
				PosStock ps = posStockRepository.getPosStockByMode(pn.getModel());
				pn.setStatus(1);
				ps.setStockDisponible(ps.getStockDisponible() + 1);
				ps.setStockReserve(ps.getStockReserve() - 1);
				SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				System.out.println(timestamp);
				String Strdate = sdf3.format(timestamp);

				PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
				ph.setSerialNum(pn.getSerialNum());
				ph.setDateSaisie(Strdate);
				ph.setStatus(1);

				posStock.add(ps);
				phs.add(ph);
				psn.add(pn);

			}

		}
		/******************** Request historique **********************/
		HistoriqueRequestPos historiqueRequestPos = new HistoriqueRequestPos();
		historiqueRequestPos.setDateStatu(new Date());
		historiqueRequestPos.setStatut("Demande rejetée");
		historiqueRequestPos.setRequestCode(t.getRequestCode());
		historiqueRequestPos.setOperateur("Réseau");
		historiqueRequestPosRepository.save(historiqueRequestPos);
		/******************** Request historique **********************/
		posHistoriqueOfSerialRepository.saveAll(phs);
		posSerialNumStatesRepository.saveAll(psn);
		posStockRepository.saveAll(posStock);
		pendingTpeRepository.saveAll(pendingTpes);
		tpeRequestRepository.save(t);

		try {
			String nomClient = t.getRso();
			String numCompte = "0" + t.getAccountNumber();
			myEmailService.sendOtpMessage(t.getEmailCharge(), "Rejet de la demande POS par le réseau",
					"Bonjour,\n\n" + "Nous vous informons qu’une demande de POS du commerçant \"" + nomClient
							+ "\" domicilié sous le N° de compte \"" + numCompte + "\" a été " + "Rejetée"
							+ " par le réseau, merci de consulter le motif de rejet.\n\n" + "Cordialement,\n");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}

		try {
			String nomClient = t.getRso();
			String numCompte = "0" + t.getAccountNumber();
			myEmailService.sendOtpMessage(ag.getEmail(), "Rejet de la demande POS par le réseau",
					"Bonjour,\n\n" + "Nous vous informons qu’une demande de POS du commerçant \"" + nomClient
							+ "\" domicilié sous le N° de compte \"" + numCompte + "\" a été " + "Rejetée"
							+ " par le réseau, merci de consulter le motif de rejet.\n\n" + "Cordialement,\n");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}
		logger.info("**********end rejected Reseaux**************");

		return ResponseEntity.ok().body(gson.toJson("rejected"));

	}

	@PostMapping("getPagePosHistoriqueOfSerial/{id}")
	public Page<RequestPosHistorique> getPagePosHistoriqueOfSerial(@PathVariable(value = "id") String serial,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir

	) {

		List<RequestPosHistorique> list = new ArrayList<>();

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);

		orders.add(order1);
		Page<PosHistoriqueOfSerial> page1 = posHistoriqueOfSerialRepository
				.getPagePosHistoriqueOfSerial(PageRequest.of(page, size, Sort.by(orders)), serial);
		for (PosHistoriqueOfSerial tr : page1.getContent()) {
			RequestPosHistorique requestPosHistorique = new RequestPosHistorique();

			PosEtats posEtats = posEtatsRepository.getOne(tr.getStatus());
			requestPosHistorique.setSerialNum(tr.getSerialNum());
			requestPosHistorique.setStatus(posEtats.getLibelleFr());
			requestPosHistorique.setDateSaisie(tr.getDateSaisie());
			list.add(requestPosHistorique);

		}

		Page<RequestPosHistorique> pages = new PageImpl<RequestPosHistorique>(list,
				PageRequest.of(page, size, Sort.by(orders)), page1.getTotalElements());
		return pages;
	}

	@GetMapping("getCommissionsByMerchantCode/{id}")
	TpeRequestDisplayDetails getCommissionsByMerchantCode(@PathVariable(value = "id") Integer merchantCode) {

		Merchant m = merchantRepository.findByMerchantCode(merchantCode);

		List<CommissionTpe> com = commissionTpeRepository.findByMerchantCode(m.getMerchantCode());

		TpeRequestDisplayDetails tpeRequestDisplay = new TpeRequestDisplayDetails();
		CommissionType ct = commissionTypeRepository.getOne(m.getCommissionType());
		tpeRequestDisplay.setCommissionTypeCode(ct.getComTypeLibelle());
		tpeRequestDisplay.setCodeCommission(ct.getCommissionTypeCode());
		for (CommissionTpe item : com) {

			if (item.getCommissionType() == 1) {
				CommissionTpe commission = commissionTpeRepository.findByRequestMerchantCode(m.getMerchantCode());
				tpeRequestDisplay.setCommissionNational(commission.getValeurCommissionNationaleFix());
				tpeRequestDisplay.setCommissionInterNational(commission.getValeurCommissionInternationaleFixe());

			} else {
				List<CommissionTpe> commissionTpes = commissionTpeRepository
						.findByMerchantCodeNational(m.getMerchantCode());

				System.out.println("COMMSSION NATIONAL" + commissionTpes.toString());
				List<CommissionTpe> CommissionTpesInter = commissionTpeRepository
						.findByMerchantCodeInternational(m.getMerchantCode());
				System.out.println("CommissionTpesInter" + CommissionTpesInter.size());

				tpeRequestDisplay.setCommissionTpesInter(CommissionTpesInter);
				tpeRequestDisplay.setCommissionTpes(commissionTpes);

			}
		}
		return tpeRequestDisplay;
	}

	@PostMapping("editCommision/{id}")
	public ResponseEntity<String> editCommision(@PathVariable(value = "id") Integer merchantCode,
			@RequestBody ValidateRequestEdit agence) {
		List<CommissionTpe> comT = commissionTpeRepository.findByMerchantCode(merchantCode);
		// List<CommissionTpeHistorique> hCtpe =
		// commissionTpeHistoriqueRepository.findAll();

		List<CommissionTpeHistorique> mh = new ArrayList<>();
		List<CommissionRequestHistorique> mR = new ArrayList<>();
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.out.println(timestamp);
		String Strdate = sdf3.format(timestamp);

		for (CommissionTpe item : comT) {

			List<CommissionTpeHistorique> cTpeH = commissionTpeHistoriqueRepository
					.findByRequestCode(item.getRequestCode());

			if (cTpeH.size() != 0) {
				for (CommissionTpeHistorique itemCtpe : cTpeH) {

					CommissionRequestHistorique h = new CommissionRequestHistorique();

					h.setCommissionInterNational(itemCtpe.getCommissionInterNational());
					h.setCommissionNational(itemCtpe.getCommissionNational());
					h.setCommissionType(itemCtpe.getCommissionType());
					h.setIdCommission(itemCtpe.getIdCommission());
					h.setLabel(itemCtpe.getLabel());
					h.setMerchantCode(itemCtpe.getMerchantCode());
					h.setMontantRefMax(itemCtpe.getMontantRefMax());
					h.setMontantRefMin(itemCtpe.getMontantRefMin());
					h.setOperateurMax(itemCtpe.getOperateurMax());
					h.setOperateurMin(itemCtpe.getOperateurMin());

					h.setRequestCode(itemCtpe.getRequestCode());

					h.setValeurComFixMax(itemCtpe.getValeurComFixMax());
					h.setValeurComFixMin(itemCtpe.getValeurComFixMin());
					h.setValeurCommissionInternationaleFixe(itemCtpe.getValeurCommissionInternationaleFixe());
					h.setValeurCommissionNationaleFix(itemCtpe.getValeurCommissionNationaleFix());
					h.setValeurComVariableMax(itemCtpe.getValeurComVariableMax());
					h.setValeurComVariableMin(itemCtpe.getValeurComVariableMin());
					h.setDateSaisie(itemCtpe.getDateSaisie());
					mR.add(h);

				}
				commissionRequestHistoriqueRepository.saveAll(mR);
				commissionTpeHistoriqueRepository
						.deleteCommissionHistoriqueByRequestCode(cTpeH.get(0).getRequestCode());

			}
			CommissionTpeHistorique h = new CommissionTpeHistorique();

			h.setCommissionInterNational(item.getCommissionInterNational());
			h.setCommissionNational(item.getCommissionNational());
			h.setCommissionType(item.getCommissionType());
			h.setIdCommission(item.getIdCommission());
			h.setLabel(item.getLabel());
			h.setMerchantCode(item.getMerchantCode());
			h.setMontantRefMax(item.getMontantRefMax());
			h.setMontantRefMin(item.getMontantRefMin());
			h.setOperateurMax(item.getOperateurMax());
			h.setOperateurMin(item.getOperateurMin());

			h.setRequestCode(item.getRequestCode());

			h.setValeurComFixMax(item.getValeurComFixMax());
			h.setValeurComFixMin(item.getValeurComFixMin());
			h.setValeurCommissionInternationaleFixe(item.getValeurCommissionInternationaleFixe());
			h.setValeurCommissionNationaleFix(item.getValeurCommissionNationaleFix());
			h.setValeurComVariableMax(item.getValeurComVariableMax());
			h.setValeurComVariableMin(item.getValeurComVariableMin());
			h.setDateSaisie(item.getDateSaisie());

			mh.add(h);

		}

		commissionTpeHistoriqueRepository.saveAll(mh);
		commissionTpeRepository.deleteCommissionByMerchantCode(merchantCode);

		if (Integer.parseInt(agence.getCommissionTypeCode()) == 1) {
			CommissionTpe com = new CommissionTpe();
			com.setCommissionNational("0");
			com.setCommissionInterNational("0");
			com.setMontantRefMin("");
			com.setMontantRefMax("");
			com.setMerchantCode(merchantCode);

			com.setOperateurMin("");
			com.setOperateurMax("");
			com.setValeurComFixMin("");

			com.setValeurComVariableMin("");

			com.setCommissionType(1);
			com.setValeurCommissionInternationaleFixe("0");
			com.setValeurCommissionNationaleFix(agence.getCommissionNational());
			com.setDateSaisie(Strdate);
			com = commissionTpeRepository.save(com);
			Merchant m = merchantRepository.findByMerchantCode(merchantCode);
			m.setCommissionInternational(com.getCommissionInterNational());
			m.setCommissionNational(com.getCommissionNational());
			m.setCommissionType(com.getCommissionType());
			merchantRepository.save(m);
		}

		if (Integer.parseInt(agence.getCommissionTypeCode()) == 2) {

			List<CommissonTpeRequestEdit> commissionTpes = agence.getCommissionTpes();
			System.out.println("ListCommmmmmmmmmm" + agence.getCommissionTpes());
			for (CommissonTpeRequestEdit commissionTpe : commissionTpes) {
				System.out.println(commissionTpes.toString());
				CommissionTpe com = new CommissionTpe();
				com.setCommissionNational("1");
				com.setCommissionInterNational("0");

				com.setMontantRefMin(commissionTpe.getMontantRefMin());
				com.setMerchantCode(merchantCode);

				com.setMerchantCode(merchantCode);
				com.setMontantRefMax(commissionTpe.getMontantRefMax());

				com.setOperateurMin(commissionTpe.getOperateurMin());
				com.setOperateurMax(commissionTpe.getOperateurMax());
				com.setValeurComFixMin(commissionTpe.getValeurComFixMin());
				com.setValeurComVariableMin(commissionTpe.getValeurComVariableMin());
				com.setCommissionType(2);
				com.setValeurCommissionInternationaleFixe("");
				com.setValeurCommissionNationaleFix("");
				com.setDateSaisie(Strdate);

				com = commissionTpeRepository.save(com);
				Merchant m = merchantRepository.findByMerchantCode(merchantCode);
				m.setCommissionInternational(com.getCommissionInterNational());
				m.setCommissionNational(com.getCommissionNational());
				m.setCommissionType(com.getCommissionType());

				merchantRepository.save(m);
			}
			List<CommissonTpeRequestEdit> CommissionTpesInter = agence.getCommissionTpesInter();

			for (CommissonTpeRequestEdit commissionTpeinter : CommissionTpesInter) {
				CommissionTpe comInter = new CommissionTpe();
				comInter.setCommissionNational("0");
				comInter.setCommissionInterNational("1");
				comInter.setMerchantCode(merchantCode);
				comInter.setMontantRefMin(commissionTpeinter.getMontantRefMin());
				comInter.setMontantRefMax(commissionTpeinter.getMontantRefMax());
				comInter.setMerchantCode(merchantCode);

				comInter.setOperateurMin(commissionTpeinter.getOperateurMin());
				comInter.setOperateurMax(commissionTpeinter.getOperateurMax());
				comInter.setValeurComFixMin(commissionTpeinter.getValeurComFixMin());
				comInter.setValeurComVariableMin(commissionTpeinter.getValeurComVariableMin());
				comInter.setCommissionType(2);
				comInter.setDateSaisie(Strdate);

				comInter.setValeurCommissionInternationaleFixe("");
				comInter.setValeurCommissionNationaleFix("");

				comInter = commissionTpeRepository.save(comInter);
				Merchant m = merchantRepository.findByMerchantCode(merchantCode);
				m.setCommissionInternational(comInter.getCommissionInterNational());
				m.setCommissionNational(comInter.getCommissionNational());
				m.setCommissionType(comInter.getCommissionType());

				merchantRepository.save(m);
			}
		} else {
			Merchant m = merchantRepository.findByMerchantCode(merchantCode);
			m.setCommissionType(Integer.parseInt(agence.getCommissionTypeCode()));

			merchantRepository.save(m);

		}
		return ResponseEntity.accepted().body(gson.toJson("update Commission successfully!"));

	}

	@GetMapping("getOne1/{id}")
	public ResponseEntity<TpeRequestDisplayDetails> getOne1(@PathVariable(value = "id") int id) {

		TpeRequest tr = tpeRequestRepository.findByRequestCode(id);
		System.out.println("TRRRRRRRR" + tr);

		Set<PendingTpe> pedndingTpes = tr.getPendingTpes();
		Set<TpePendingDisplay> tpePendingDisplays = new HashSet<>();
		List<CommissionTpe> commissionTpesList = new ArrayList<>();
		List<CommissionTpe> commissionTpesListInter = new ArrayList<>();

		List<CommissionTpe> commissionTpesListh = new ArrayList<>();
		List<CommissionTpe> commissionTpesListInterh = new ArrayList<>();
		TpeRequestDisplayDetails tpeRequestDisplay = new TpeRequestDisplayDetails();
		MontantLoyer mL = montantLoyerRepository.findMontantLoyerByRequest(tr.getRequestCode());

		String nbAccount = tr.getAccountNumber().substring(7, tr.getAccountNumber().length());

		tpeRequestDisplay.setAccountNumber(nbAccount);
		System.out.println("MontantLoyer " + mL);
		tpeRequestDisplay.setMontantLoyerh(tr.getMontantLoyer());
		if (mL != null) {
			tpeRequestDisplay.setMontantLoyerh(mL.getMontantLoyer());

		}
		AgenceAdministration agence = agenceAdministrationRepository.findByIdAgence(tr.getAgence()).get();

		tpeRequestDisplay.setAgence(agence.getInitial());
		CommissionType commissionType = commissionTypeRepository.findByCommissionTypeCode(tr.getCommissionTypeCode());
		tpeRequestDisplay.setCommissionTypeCode(String.valueOf(commissionType.getComTypeLibelle()));

		tpeRequestDisplay.setCity(tr.getCity());
		tpeRequestDisplay.setCodeZip(tr.getCodeZip());
		tpeRequestDisplay.setOffshore(tr.getOffshore());
		// tpeRequestDisplay.setCommissionInterNational(tr.getCommissionInterNational());
		// tpeRequestDisplay.setCommissionNational(tr.getCommissionNational());
		tpeRequestDisplay.setCountry(tr.getCountry());
		TpeRequestStatus tpeRequestStat = tpeRequestStatusRepository.findByStatusCode(tr.getStatus());
		tpeRequestDisplay.setStatus(tpeRequestStat.getLibelleFr());
		tpeRequestDisplay.setDateCreation(tr.getDateCreation());
		tpeRequestDisplay.setDateDecision(tr.getDateDecision());
		tpeRequestDisplay.setNombreTPE(tr.getNombreTPE());
		tpeRequestDisplay.setCodeZip(tr.getCodeZip());
		tpeRequestDisplay.setPhone(tr.getPhone());
		tpeRequestDisplay.setRequestCode(tr.getRequestCode());
		tpeRequestDisplay.setUserName(tr.getUserName());
		tpeRequestDisplay.setAdresse(tr.getAdresse());
		tpeRequestDisplay.setCommissionNational(tr.getCommissionNational());
		tpeRequestDisplay.setCommissionInterNational(tr.getCommissionInterNational());
		tpeRequestDisplay.setCodeCommission(tr.getCommissionTypeCode());
		tpeRequestDisplay.setNom(tr.getNom());
		tpeRequestDisplay.setPrenom(tr.getPrenom());
		tpeRequestDisplay.setNif(tr.getNif());
		tpeRequestDisplay.setRso(tr.getRso());
		tpeRequestDisplay.setRc(tr.getRc());
		tpeRequestDisplay.setEmail(tr.getEmail());
		tpeRequestDisplay.setSiteWeb(tr.getSiteWeb());
		tpeRequestDisplay.setDaira(tr.getDaira());
		tpeRequestDisplay.setCommmune(tr.getCommune());
		tpeRequestDisplay.setRevenue(tr.getRevenue());
		tpeRequestDisplay.setNomC(tr.getNomC());
		tpeRequestDisplay.setTitleC(tr.getTitleC());
		tpeRequestDisplay.setMontantLoyer(tr.getMontantLoyer());
		for (PendingTpe p : pedndingTpes) {

			TpePendingDisplay tpePendingDisplay = new TpePendingDisplay();
			tpePendingDisplay.setCode(p.getCode());
			tpePendingDisplay.setActivite(p.getActivite());

			if (p.getStatus() == 0) {
				tpePendingDisplay.setStatus("");
			} else {
				TpeRequestStatus t = tpeRequestStatusRepository.findByStatusCode(p.getStatus());
				tpePendingDisplay.setStatus(t.getLibelleFr());

			}
			if (p.getType() == 0) {
				tpePendingDisplay.setType("");

			} else {
				PosType posType = posTypeRepository.findByPosTypeCode(p.getType());
				tpePendingDisplay.setType(posType.getLibelle());
			}

			tpePendingDisplay.setZipCode(p.getZipCode());
			tpePendingDisplay.setPhone(p.getPhone());
			tpePendingDisplay.setCountry(p.getCountry());
			tpePendingDisplay.setCity(p.getCity());
			tpePendingDisplay.setAddress(p.getAdresse());

			if (p.getMccCode() == null) {
				tpePendingDisplay.setMccCode("");

			} else {
				MccList mccList = mccRepository.findByMccCode(p.getMccCode());
				tpePendingDisplay.setMccCode(mccList.getMccListLibelle());
			}

			if (p.getPreAutorisation()) {
				tpePendingDisplay.setPreAutorisation("YES");

			} else {
				tpePendingDisplay.setPreAutorisation("NO");
			}

			tpePendingDisplay.setLibelle(p.getLibelle());
			tpePendingDisplay.setMotif(p.getMotif());

			if (p.getModel() == null) {
				tpePendingDisplay.setModel("");

			} else {
				PosModel posModel = posModelRepository.getOne(Integer.parseInt(p.getModel()));
				tpePendingDisplay.setModel(posModel.getLibelle());
			}

			FamillePos famillePos = famillePosRepository.getOne(p.getFamillePosCode());
			tpePendingDisplay.setFamillePosCode(famillePos.getFamillePosLibelle());
			tpePendingDisplay.setFamilleCode(p.getFamillePosCode());
			tpePendingDisplays.add(tpePendingDisplay);

		}
		tpeRequestDisplay.setPedndingTpes(tpePendingDisplays);

		System.out.println("idddd  " + id);

		if (tr.getCommissionTypeCode() == 1) {
			CommissionTpe commission = commissionTpeRepository.findByRequestCodeComFixes(id);
			System.out.println("commission " + commission);
			if (commission != null) {
				tpeRequestDisplay.setCommissionNational(commission.getValeurCommissionNationaleFix());
				tpeRequestDisplay.setCommissionInterNational(commission.getValeurCommissionInternationaleFixe());
			} else {
				CommissionTpeHistorique commissionh = commissionTpeHistoriqueRepository.findByRequestCodeComFixes(id);
				tpeRequestDisplay.setCommissionNational(commissionh.getValeurCommissionNationaleFix());
				tpeRequestDisplay.setCommissionInterNational(commissionh.getValeurCommissionInternationaleFixe());

			}

		} else {
			List<CommissionTpe> commissionTpes = commissionTpeRepository.findByRequestCodeNational(id);
			if (commissionTpes.size() != 0) {
				System.out.println("COMMSSION NATIONAL" + commissionTpes.toString());
				List<CommissionTpe> CommissionTpesInter = commissionTpeRepository.findByRequestCodeInternational(id);
				System.out.println("CommissionTpesInter" + CommissionTpesInter.size());

				tpeRequestDisplay.setCommissionTpesInter(CommissionTpesInter);
				tpeRequestDisplay.setCommissionTpes(commissionTpes);

			} else {
				List<CommissionTpeHistorique> commissionTpesh = commissionTpeHistoriqueRepository
						.findByRequestCodeNational(id);
				System.out.println("COMMSSION NATIONAL" + commissionTpesh.toString());

				for (CommissionTpeHistorique item : commissionTpesh) {
					CommissionTpe h = new CommissionTpe();
					h.setCommissionInterNational(item.getCommissionInterNational());
					h.setCommissionNational(item.getCommissionNational());
					h.setCommissionType(item.getCommissionType());
					h.setIdCommission(item.getIdCommission());
					h.setLabel(item.getLabel());
					h.setMerchantCode(item.getMerchantCode());
					h.setMontantRefMax(item.getMontantRefMax());
					h.setMontantRefMin(item.getMontantRefMin());
					h.setOperateurMax(item.getOperateurMax());
					h.setOperateurMin(item.getOperateurMin());
					h.setRequestCode(item.getRequestCode());
					h.setValeurComFixMax(item.getValeurComFixMax());
					h.setValeurComFixMin(item.getValeurComFixMin());
					h.setValeurCommissionInternationaleFixe(item.getValeurCommissionInternationaleFixe());
					h.setValeurCommissionNationaleFix(item.getValeurCommissionNationaleFix());
					h.setValeurComVariableMax(item.getValeurComVariableMax());
					h.setValeurComVariableMin(item.getValeurComVariableMin());
					commissionTpes.add(h);

				}
				List<CommissionTpeHistorique> CommissionTpesInterh = commissionTpeHistoriqueRepository
						.findByRequestCodeInternational(id);
				System.out.println("CommissionTpesInter" + CommissionTpesInterh.size());
				for (CommissionTpeHistorique item : CommissionTpesInterh) {
					CommissionTpe h = new CommissionTpe();
					h.setCommissionInterNational(item.getCommissionInterNational());
					h.setCommissionNational(item.getCommissionNational());
					h.setCommissionType(item.getCommissionType());
					h.setIdCommission(item.getIdCommission());
					h.setLabel(item.getLabel());
					h.setMerchantCode(item.getMerchantCode());
					h.setMontantRefMax(item.getMontantRefMax());
					h.setMontantRefMin(item.getMontantRefMin());
					h.setOperateurMax(item.getOperateurMax());
					h.setOperateurMin(item.getOperateurMin());
					h.setRequestCode(item.getRequestCode());
					h.setValeurComFixMax(item.getValeurComFixMax());
					h.setValeurComFixMin(item.getValeurComFixMin());
					h.setValeurCommissionInternationaleFixe(item.getValeurCommissionInternationaleFixe());
					h.setValeurCommissionNationaleFix(item.getValeurCommissionNationaleFix());
					h.setValeurComVariableMax(item.getValeurComVariableMax());
					h.setValeurComVariableMin(item.getValeurComVariableMin());
					commissionTpesListInter.add(h);
				}
				tpeRequestDisplay.setCommissionTpesInter(commissionTpesListInter);
				tpeRequestDisplay.setCommissionTpes(commissionTpes);
			}

		}

		List<CommissionTpeHistorique> ctph = commissionTpeHistoriqueRepository.findByRequestCode(id);
		System.out.println("ctph " + ctph);

		if (ctph != null && ctph.size() != 0) {

			List<CommissionRequestHistorique> comRequest = commissionRequestHistoriqueRepository.findByRequestCode(id);

			if (comRequest.size() != 0) {

				CommissionType comtypeh = commissionTypeRepository
						.findByCommissionTypeCode(comRequest.get(0).getCommissionType());
				tpeRequestDisplay.setCommissionTypeCodeh(String.valueOf(comtypeh.getComTypeLibelle()));

				tpeRequestDisplay.setCommissionNationalh(comRequest.get(0).getValeurCommissionNationaleFix());
				tpeRequestDisplay
						.setCommissionInterNationalh(comRequest.get(0).getValeurCommissionInternationaleFixe());
				System.out.println("code type = " + comRequest.get(0).getCommissionType());
				tpeRequestDisplay.setCodeCommissionh(comRequest.get(0).getCommissionType());

				System.out.println("if");
				List<CommissionRequestHistorique> commissionTpeshs = commissionRequestHistoriqueRepository
						.findByRequestCodeNational(id);

				for (CommissionRequestHistorique item : commissionTpeshs) {
					CommissionTpe h = new CommissionTpe();
					h.setCommissionInterNational(item.getCommissionInterNational());
					h.setCommissionNational(item.getCommissionNational());
					h.setIdCommission(item.getIdCommission());
					h.setLabel(item.getLabel());
					h.setMerchantCode(item.getMerchantCode());
					h.setMontantRefMax(item.getMontantRefMax());
					h.setMontantRefMin(item.getMontantRefMin());
					h.setOperateurMax(item.getOperateurMax());
					h.setOperateurMin(item.getOperateurMin());
					h.setRequestCode(item.getRequestCode());
					h.setValeurComFixMax(item.getValeurComFixMax());
					h.setValeurComFixMin(item.getValeurComFixMin());
					h.setValeurCommissionInternationaleFixe(item.getValeurCommissionInternationaleFixe());
					h.setValeurCommissionNationaleFix(item.getValeurCommissionNationaleFix());
					h.setValeurComVariableMax(item.getValeurComVariableMax());
					h.setValeurComVariableMin(item.getValeurComVariableMin());
					commissionTpesListh.add(h);
				}

				List<CommissionRequestHistorique> CommissionTpesInterh = commissionRequestHistoriqueRepository
						.findByRequestCodeInternational(id);
				System.out.println("CommissionTpesInter" + CommissionTpesInterh.size());
				for (CommissionRequestHistorique item : CommissionTpesInterh) {
					CommissionTpe h = new CommissionTpe();
					h.setCommissionInterNational(item.getCommissionInterNational());
					h.setCommissionNational(item.getCommissionNational());
					h.setIdCommission(item.getIdCommission());
					h.setLabel(item.getLabel());
					h.setMerchantCode(item.getMerchantCode());
					h.setMontantRefMax(item.getMontantRefMax());
					h.setMontantRefMin(item.getMontantRefMin());
					h.setOperateurMax(item.getOperateurMax());
					h.setOperateurMin(item.getOperateurMin());
					h.setRequestCode(item.getRequestCode());
					h.setValeurComFixMax(item.getValeurComFixMax());
					h.setValeurComFixMin(item.getValeurComFixMin());
					h.setValeurCommissionInternationaleFixe(item.getValeurCommissionInternationaleFixe());
					h.setValeurCommissionNationaleFix(item.getValeurCommissionNationaleFix());
					h.setValeurComVariableMax(item.getValeurComVariableMax());
					h.setValeurComVariableMin(item.getValeurComVariableMin());
					commissionTpesListInterh.add(h);
				}
				tpeRequestDisplay.setCommissionTpesInterh(commissionTpesListInterh);
				tpeRequestDisplay.setCommissionTpesh(commissionTpesListh);

			} else {
				CommissionType comtypeh = commissionTypeRepository
						.findByCommissionTypeCode(ctph.get(0).getCommissionType());
				tpeRequestDisplay.setCommissionTypeCodeh(String.valueOf(comtypeh.getComTypeLibelle()));

				tpeRequestDisplay.setCommissionNationalh(ctph.get(0).getValeurCommissionNationaleFix());
				tpeRequestDisplay.setCommissionInterNationalh(ctph.get(0).getValeurCommissionInternationaleFixe());
				System.out.println("code type = " + ctph.get(0).getCommissionType());
				tpeRequestDisplay.setCodeCommissionh(ctph.get(0).getCommissionType());

				System.out.println("else");
				List<CommissionTpeHistorique> commissionTpesh = commissionTpeHistoriqueRepository
						.findByRequestCodeNational(id);

				for (CommissionTpeHistorique item : commissionTpesh) {
					CommissionTpe h = new CommissionTpe();
					h.setCommissionInterNational(item.getCommissionInterNational());
					h.setCommissionNational(item.getCommissionNational());
					h.setIdCommission(item.getIdCommission());
					h.setLabel(item.getLabel());
					h.setMerchantCode(item.getMerchantCode());
					h.setMontantRefMax(item.getMontantRefMax());
					h.setMontantRefMin(item.getMontantRefMin());
					h.setOperateurMax(item.getOperateurMax());
					h.setOperateurMin(item.getOperateurMin());
					h.setRequestCode(item.getRequestCode());
					h.setValeurComFixMax(item.getValeurComFixMax());
					h.setValeurComFixMin(item.getValeurComFixMin());
					h.setValeurCommissionInternationaleFixe(item.getValeurCommissionInternationaleFixe());
					h.setValeurCommissionNationaleFix(item.getValeurCommissionNationaleFix());
					h.setValeurComVariableMax(item.getValeurComVariableMax());
					h.setValeurComVariableMin(item.getValeurComVariableMin());
					commissionTpesListh.add(h);

				}
				List<CommissionTpeHistorique> CommissionTpesInterh = commissionTpeHistoriqueRepository
						.findByRequestCodeInternational(id);
				System.out.println("CommissionTpesInter" + CommissionTpesInterh.size());
				for (CommissionTpeHistorique item : CommissionTpesInterh) {
					CommissionTpe h = new CommissionTpe();
					h.setCommissionInterNational(item.getCommissionInterNational());
					h.setCommissionNational(item.getCommissionNational());
					h.setIdCommission(item.getIdCommission());
					h.setLabel(item.getLabel());
					h.setMerchantCode(item.getMerchantCode());
					h.setMontantRefMax(item.getMontantRefMax());
					h.setMontantRefMin(item.getMontantRefMin());
					h.setOperateurMax(item.getOperateurMax());
					h.setOperateurMin(item.getOperateurMin());
					h.setRequestCode(item.getRequestCode());
					h.setValeurComFixMax(item.getValeurComFixMax());
					h.setValeurComFixMin(item.getValeurComFixMin());
					h.setValeurCommissionInternationaleFixe(item.getValeurCommissionInternationaleFixe());
					h.setValeurCommissionNationaleFix(item.getValeurCommissionNationaleFix());
					h.setValeurComVariableMax(item.getValeurComVariableMax());
					h.setValeurComVariableMin(item.getValeurComVariableMin());
					commissionTpesListInterh.add(h);
				}
				tpeRequestDisplay.setCommissionTpesInterh(commissionTpesListInterh);
				tpeRequestDisplay.setCommissionTpesh(commissionTpesListh);
			}

		}

		System.out.println("BEFORE");

		logger.info(tpeRequestDisplay.toString());

		return ResponseEntity.ok().body(tpeRequestDisplay);
	}

	@PostMapping("validateCommision")
	public ResponseEntity<String> validateCommision(@RequestBody ValidateRequestEdit agence) {
		logger.info("**********begin validate Reseaux**************");

		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.out.println(timestamp);
		String Strdate = sdf3.format(timestamp);
		Integer merchantIdCom = 0;
		TpeRequest t = tpeRequestRepository.getOne(agence.getCodeRequest());
		Set<PendingTpe> pendingTpes = t.getPendingTpes();

		t.setStatus(5);
		t.setDateDecision(new Date());
		MontantLoyer ml = new MontantLoyer();
		System.out.println(t.getMontantLoyer());
		for (PendingTpe p : pendingTpes) {

			p.setStatus(5);
			pendingTpes.add(p);

		}
		if (t.getMontantLoyer().equals("0")) {
			ml.setMontantLoyer("0");
		} else {
			ml.setMontantLoyer(t.getMontantLoyer());

		}

		ml.setRequestCode(t.getRequestCode());

		Account a = accountRepository.findByAccountNum(t.getAccountNumber());

		if (a != null) {
			System.out.println("account");
			Merchant m = merchantRepository.findByNumAccount(a.getAccountCode());
			if (m != null) {
				System.out.println("merchant");
				System.out.println("if++++");
				merchantIdCom = m.getMerchantCode();

				List<CommissionTpeHistorique> hCtpe = commissionTpeHistoriqueRepository.findAll();

				List<CommissionTpe> comT = commissionTpeRepository.findByMerchantCode(merchantIdCom);
				List<CommissionTpeHistorique> mh = new ArrayList<>();
				List<CommissionRequestHistorique> cRequest = new ArrayList<>();
				List<CommissionTpeHistorique> hComRequest = commissionTpeHistoriqueRepository
						.findByRequestCode(agence.getCodeRequest());
				if (hComRequest.size() != 0) {

					for (CommissionTpeHistorique item : hComRequest) {
						CommissionRequestHistorique h = new CommissionRequestHistorique();

						h.setCommissionInterNational(item.getCommissionInterNational());
						h.setCommissionNational(item.getCommissionNational());
						h.setCommissionType(item.getCommissionType());
						h.setIdCommission(item.getIdCommission());
						h.setLabel(item.getLabel());
						h.setMerchantCode(item.getMerchantCode());
						h.setMontantRefMax(item.getMontantRefMax());
						h.setMontantRefMin(item.getMontantRefMin());
						h.setOperateurMax(item.getOperateurMax());
						h.setOperateurMin(item.getOperateurMin());

						h.setRequestCode(item.getRequestCode());

						h.setValeurComFixMax(item.getValeurComFixMax());
						h.setValeurComFixMin(item.getValeurComFixMin());
						h.setValeurCommissionInternationaleFixe(item.getValeurCommissionInternationaleFixe());
						h.setValeurCommissionNationaleFix(item.getValeurCommissionNationaleFix());
						h.setValeurComVariableMax(item.getValeurComVariableMax());
						h.setValeurComVariableMin(item.getValeurComVariableMin());
						h.setDateSaisie(item.getDateSaisie());
						cRequest.add(h);
					}
					commissionRequestHistoriqueRepository.saveAll(cRequest);

					commissionTpeHistoriqueRepository.deleteCommissionHistoriqueByRequestCode(agence.getCodeRequest());

				}
				for (CommissionTpe item : comT) {
					CommissionTpeHistorique h = new CommissionTpeHistorique();

					h.setCommissionInterNational(item.getCommissionInterNational());
					h.setCommissionNational(item.getCommissionNational());
					h.setCommissionType(item.getCommissionType());
					h.setIdCommission(item.getIdCommission());
					h.setLabel(item.getLabel());
					h.setMerchantCode(item.getMerchantCode());
					h.setMontantRefMax(item.getMontantRefMax());
					h.setMontantRefMin(item.getMontantRefMin());
					h.setOperateurMax(item.getOperateurMax());
					h.setOperateurMin(item.getOperateurMin());
					/*
					 * if(hCtpe.size()==0) { h.setRequestCode(0);
					 * 
					 * } else { h.setRequestCode(item.getRequestCode()); }
					 */
					h.setRequestCode(t.getRequestCode());
					h.setValeurComFixMax(item.getValeurComFixMax());
					h.setValeurComFixMin(item.getValeurComFixMin());
					h.setValeurCommissionInternationaleFixe(item.getValeurCommissionInternationaleFixe());
					h.setValeurCommissionNationaleFix(item.getValeurCommissionNationaleFix());
					h.setValeurComVariableMax(item.getValeurComVariableMax());
					h.setValeurComVariableMin(item.getValeurComVariableMin());
					h.setDateSaisie(item.getDateSaisie());

					mh.add(h);
				}

				commissionTpeHistoriqueRepository.saveAll(mh);

				commissionTpeRepository.deleteCommissionByRequestCode(t.getRequestCode());
				System.out.println("merchantIdCom" + merchantIdCom);
				commissionTpeRepository.deleteCommissionByMerchantCode(merchantIdCom);
				if (Integer.parseInt(agence.getCommissionTypeCode()) == 1) {
					CommissionTpe com = new CommissionTpe();
					com.setCommissionNational("0");
					com.setCommissionInterNational("0");
					com.setMerchantCode(merchantIdCom);
					com.setMontantRefMin("");
					com.setMontantRefMax("");
					com.setRequestCode(t.getRequestCode());
					com.setMerchantCode(merchantIdCom);

					com.setOperateurMin("");
					com.setOperateurMax("");
					com.setValeurComFixMin("");

					com.setValeurComVariableMin("");
					com.setValeurCommissionInternationaleFixe(agence.getCommissionInterNational());
					com.setValeurCommissionNationaleFix(agence.getCommissionNational());

					com.setCommissionType(1);
					t.setCommissionTypeCode(1);
					com.setDateSaisie(Strdate);
					com = commissionTpeRepository.save(com);
					Merchant m1 = merchantRepository.findByMerchantCode(merchantIdCom);
					m1.setCommissionInternational(com.getCommissionInterNational());
					m1.setCommissionNational(com.getCommissionNational());
					m1.setCommissionType(com.getCommissionType());
					merchantRepository.save(m1);
				}

				if (Integer.parseInt(agence.getCommissionTypeCode()) == 2) {

					List<CommissonTpeRequestEdit> commissionTpes = agence.getCommissionTpes();
					System.out.println("ListCommmmmmmmmmm" + agence.getCommissionTpes());
					for (CommissonTpeRequestEdit commissionTpe : commissionTpes) {
						System.out.println(commissionTpes.toString());
						CommissionTpe com = new CommissionTpe();
						com.setCommissionNational("1");
						com.setCommissionInterNational("0");

						com.setMontantRefMin(commissionTpe.getMontantRefMin());
						com.setMerchantCode(merchantIdCom);

						com.setMerchantCode(merchantIdCom);
						com.setMontantRefMax(commissionTpe.getMontantRefMax());
						com.setRequestCode(agence.getCodeRequest());
						com.setOperateurMin(commissionTpe.getOperateurMin());
						com.setOperateurMax(commissionTpe.getOperateurMax());
						com.setValeurComFixMin(commissionTpe.getValeurComFixMin());
						com.setValeurComVariableMin(commissionTpe.getValeurComVariableMin());
						com.setCommissionType(2);
						t.setCommissionTypeCode(2);

						com.setValeurCommissionInternationaleFixe("");
						com.setValeurCommissionNationaleFix("");
						com.setDateSaisie(Strdate);

						com = commissionTpeRepository.save(com);
						Merchant m1 = merchantRepository.findByMerchantCode(merchantIdCom);
						m1.setCommissionInternational(com.getCommissionInterNational());
						m1.setCommissionNational(com.getCommissionNational());
						m1.setCommissionType(com.getCommissionType());

						merchantRepository.save(m1);
					}
					List<CommissonTpeRequestEdit> CommissionTpesInter = agence.getCommissionTpesInter();

					for (CommissonTpeRequestEdit commissionTpeinter : CommissionTpesInter) {
						CommissionTpe comInter = new CommissionTpe();
						comInter.setRequestCode(agence.getCodeRequest());
						comInter.setCommissionNational("0");
						comInter.setCommissionInterNational("1");
						comInter.setMerchantCode(merchantIdCom);
						comInter.setMontantRefMin(commissionTpeinter.getMontantRefMin());
						comInter.setMontantRefMax(commissionTpeinter.getMontantRefMax());
						comInter.setMerchantCode(merchantIdCom);

						comInter.setOperateurMin(commissionTpeinter.getOperateurMin());
						comInter.setOperateurMax(commissionTpeinter.getOperateurMax());
						comInter.setValeurComFixMin(commissionTpeinter.getValeurComFixMin());
						comInter.setValeurComVariableMin(commissionTpeinter.getValeurComVariableMin());
						comInter.setCommissionType(2);
						t.setCommissionTypeCode(2);

						comInter.setValeurCommissionInternationaleFixe("");
						comInter.setValeurCommissionNationaleFix("");
						comInter.setDateSaisie(Strdate);

						comInter = commissionTpeRepository.save(comInter);
						Merchant m1 = merchantRepository.findByMerchantCode(merchantIdCom);
						m1.setCommissionInternational(comInter.getCommissionInterNational());
						m1.setCommissionNational(comInter.getCommissionNational());
						m1.setCommissionType(comInter.getCommissionType());

						merchantRepository.save(m1);
					}

				}
			}

		}
		if (merchantIdCom == 0) {

			System.out.println("else++++");
			List<CommissionTpeHistorique> hCtpe = commissionTpeHistoriqueRepository.findAll();

			List<CommissionTpe> comT = commissionTpeRepository.findByMerchantCode(merchantIdCom);
			List<CommissionTpeHistorique> mh = new ArrayList<>();

			for (CommissionTpe item : comT) {
				CommissionTpeHistorique h = new CommissionTpeHistorique();

				h.setCommissionInterNational(item.getCommissionInterNational());
				h.setCommissionNational(item.getCommissionNational());
				h.setCommissionType(item.getCommissionType());
				h.setIdCommission(item.getIdCommission());
				h.setLabel(item.getLabel());
				h.setMerchantCode(item.getMerchantCode());
				h.setMontantRefMax(item.getMontantRefMax());
				h.setMontantRefMin(item.getMontantRefMin());
				h.setOperateurMax(item.getOperateurMax());
				h.setOperateurMin(item.getOperateurMin());
				/*
				 * if(hCtpe.size()==0) { h.setRequestCode(0);
				 * 
				 * } else { h.setRequestCode(item.getRequestCode()); }
				 */
				h.setRequestCode(item.getRequestCode());

				h.setValeurComFixMax(item.getValeurComFixMax());
				h.setValeurComFixMin(item.getValeurComFixMin());
				h.setValeurCommissionInternationaleFixe(item.getValeurCommissionInternationaleFixe());
				h.setValeurCommissionNationaleFix(item.getValeurCommissionNationaleFix());
				h.setValeurComVariableMax(item.getValeurComVariableMax());
				h.setValeurComVariableMin(item.getValeurComVariableMin());
				h.setDateSaisie(item.getDateSaisie());
				mh.add(h);
			}

			commissionTpeHistoriqueRepository.saveAll(mh);

			commissionTpeRepository.deleteCommissionByRequestCode(t.getRequestCode());

			if (Integer.parseInt(agence.getCommissionTypeCode()) == 1) {
				CommissionTpe com = new CommissionTpe();
				com.setCommissionNational("0");
				com.setCommissionInterNational("0");
				com.setMerchantCode(0);
				com.setMontantRefMin("");
				com.setMontantRefMax("");
				com.setRequestCode(t.getRequestCode());
				com.setMerchantCode(0);

				com.setOperateurMin("");
				com.setOperateurMax("");
				com.setValeurComFixMin("");

				com.setValeurComVariableMin("");
				com.setValeurCommissionInternationaleFixe(agence.getCommissionInterNational());
				com.setValeurCommissionNationaleFix(agence.getCommissionNational());

				com.setCommissionType(1);
				t.setCommissionTypeCode(1);

				com.setDateSaisie(Strdate);

				com = commissionTpeRepository.save(com);

			}

			if (Integer.parseInt(agence.getCommissionTypeCode()) == 2) {

				List<CommissonTpeRequestEdit> commissionTpes = agence.getCommissionTpes();
				System.out.println("ListCommmmmmmmmmm" + agence.getCommissionTpes());
				for (CommissonTpeRequestEdit commissionTpe : commissionTpes) {
					System.out.println(commissionTpes.toString());
					CommissionTpe com = new CommissionTpe();
					CommissionTpeId commissionTpeId = new CommissionTpeId();
					com.setCommissionNational("1");
					com.setCommissionInterNational("0");

					com.setMontantRefMin(commissionTpe.getMontantRefMin());

					com.setMontantRefMax(commissionTpe.getMontantRefMax());
					com.setRequestCode(agence.getCodeRequest());

					com.setOperateurMin(commissionTpe.getOperateurMin());
					com.setOperateurMax(commissionTpe.getOperateurMax());
					com.setValeurComFixMin(commissionTpe.getValeurComFixMin());
					com.setValeurComVariableMin(commissionTpe.getValeurComVariableMin());
					com.setCommissionType(2);
					t.setCommissionTypeCode(2);

					com.setValeurCommissionInternationaleFixe("");
					com.setValeurCommissionNationaleFix("");
					com.setDateSaisie(Strdate);

					com = commissionTpeRepository.save(com);

				}
				List<CommissonTpeRequestEdit> CommissionTpesInter = agence.getCommissionTpesInter();

				for (CommissonTpeRequestEdit commissionTpeinter : CommissionTpesInter) {
					CommissionTpe comInter = new CommissionTpe();
					comInter.setRequestCode(agence.getCodeRequest());
					comInter.setCommissionNational("0");
					comInter.setCommissionInterNational("1");
					comInter.setMontantRefMin(commissionTpeinter.getMontantRefMin());
					comInter.setMontantRefMax(commissionTpeinter.getMontantRefMax());

					comInter.setOperateurMin(commissionTpeinter.getOperateurMin());
					comInter.setOperateurMax(commissionTpeinter.getOperateurMax());
					comInter.setValeurComFixMin(commissionTpeinter.getValeurComFixMin());
					comInter.setValeurComVariableMin(commissionTpeinter.getValeurComVariableMin());
					comInter.setCommissionType(2);
					t.setCommissionTypeCode(2);

					comInter.setValeurCommissionInternationaleFixe("");
					comInter.setValeurCommissionNationaleFix("");
					comInter.setDateSaisie(Strdate);

					comInter = commissionTpeRepository.save(comInter);

				}
			}
		}
		t.setCommissionTypeCode(Integer.parseInt(agence.getCommissionTypeCode()));

		/******************** Request historique **********************/
		HistoriqueRequestPos historiqueRequestPos = new HistoriqueRequestPos();
		historiqueRequestPos.setDateStatu(new Date());
		historiqueRequestPos.setStatut("Demande approuvée");
		historiqueRequestPos.setRequestCode(t.getRequestCode());
		historiqueRequestPos.setOperateur("Réseau");
		historiqueRequestPosRepository.save(historiqueRequestPos);
		/******************** Request historique **********************/
		montantLoyerRepository.save(ml);
		t.setMontantLoyer(agence.getMontantLoyer());

		tpeRequestRepository.save(t);

		AgenceAdministration ag = agenceAdministrationRepository.findByIdAgence(t.getAgence()).get();
		Zone z = zoneRepository.findByCodeZone(ag.getCodeZone()).get();
		String recipientName = t.getRso();
		String accountNumber = "0" + t.getAccountNumber();

		try {
			myEmailService.sendOtpMessage(t.getEmailCharge(), "validation réseaux",
					"Bonjour,\n\n" + "Nous vous informons qu’une demande de POS du commerçant \"" + recipientName
							+ "\" domicilié sous le N° de compte \"" + accountNumber
							+ "\" a été validée par le réseau et demeure en attente de validation Back Office.\n\n"
							+ "Cordialement,\n");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}

		try {
			logger.info("**********begin mail validate Reseaux**************");

			myEmailService.sendOtpMessage(ag.getEmail(), "validation réseaux",
					"Bonjour,\n\n" + "Nous vous informons qu’une demande de POS du commerçant \"" + recipientName
							+ "\" domicilié sous le N° de compte \"" + accountNumber
							+ "\" a été validée par le réseau et demeure en attente de validation Back Office.\n\n"
							+ "Cordialement,\n");
			logger.info("**********end mail validate Reseaux**************");

		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info(stackTrace);
			logger.info("**********end validate Reseaux**************");

			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}

		try {
			myEmailService.sendOtpMessage(ag.getEmail(), "validation réseaux",
					"Bonjour,\n\n" + "Nous vous informons qu’une demande de POS du commerçant \"" + recipientName
							+ "\" domicilié sous le N° de compte \"" + accountNumber
							+ "\" a été validée par le réseau et demeure en attente de validation Back Office.\n\n"
							+ "Cordialement,\n");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}

		return ResponseEntity.ok().body(gson.toJson("validation level 3"));

	}

	@PutMapping("validateRevesibleRequest/{id}")
	public ResponseEntity<String> validateRevesible(@PathVariable(value = "id") Integer requestcode,
			@RequestBody TpeRequestDisplay agence) {
		logger.info("**********begin validate Revesible**************");

		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String Strdate = sdf3.format(timestamp);
		Integer merchantIdCom = 0;
		TpeRequest t = tpeRequestRepository.findById(requestcode).get();
		Set<PendingTpe> pendingTpes = agence.getPendingTpes();
		List<PosStock> ps = new ArrayList<>();
		List<PosSerialNumStates> posSerialNumStatess = new ArrayList<>();

		Set<PendingTpe> pendingTpess = new HashSet<>();
		List<PosSerialNumStates> pSerial = null;
		int counter = 0;
		int x = 0;
		int xm = 0;
		int xf = 0;
		int xp = 0;
		List<PosHistoriqueOfSerial> posHistoriqueOfSerial = new ArrayList<PosHistoriqueOfSerial>();
		Set<PendingTpe> pendingTpeh = t.getPendingTpes();

		System.out.println("pend =" + pendingTpes.size());
		System.out.println("pend =" + pendingTpeh);

		for (PendingTpe pen : pendingTpeh) {
			PendingTpe p = pendingTpeRepository.findById(pen.getCode()).get();
			if (p.getSerialNum() != null && !p.getSerialNum().equals("")) {
				System.out.println("SerialNum =" + p.getSerialNum());
				PosSerialNumStates psn = posSerialNumStatesRepository.findById(p.getSerialNum()).get();

				PosStock pss = posStockRepository.getPosStockByMode(psn.getModel());

				pss.setStockDisponible(pss.getStockDisponible() + 1);
				pss.setStockReserve(pss.getStockReserve() - 1);
				psn.setStatus(1);
				posSerialNumStatesRepository.save(psn);
				posStockRepository.save(pss);
			}
		}

		for (PendingTpe p : pendingTpes) {
			System.out.println("test" + p.getCode());
			// PendingTpe pends = pendingTpeRepository.findById(p.getCode()).get();

			// System.out.println("pend ="+p.getCode());
			System.out.println("p =" + p.getCode());
			if ((p.getFamillePosCode() == 1) || (p.getFamillePosCode() == 4)) {

				p.setStatus(1);
				pendingTpess.add(p);
			} else {
				pSerial = posSerialNumStatesRepository.getOnePosSerialNumStatesByStockByType(p.getType());
				System.out.println("Type  = " + p.getType());
				if (pSerial.size() > 0) {
					PosSerialNumStates posSerialNumStates = null;
					PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();

					System.out.println("FamillePosCode  = " + p.getFamillePosCode());

					if (p.getType() == 22) {
						xm = xm + 1;
						x = xm;
						System.out.println("xm =" + xm);

					}
					if (p.getType() == 10) {
						xf = xf + 1;
						x = xf;
						System.out.println("xf =" + xf);
					}
					if (p.getType() == 23) {
						xp = xp + 1;
						x = xp;
						System.out.println("xp =" + xp);
					}
					System.out.println("x out =" + x);
					for (int i = 0; i < x; i++) {
						posSerialNumStates = posSerialNumStatesRepository.findById(pSerial.get(i).getSerialNum()).get();
						posSerialNumStates.setStatus(2);

						posSerialNumStatess.add(posSerialNumStates);
						ph.setSerialNum(posSerialNumStates.getSerialNum());
						ph.setDateSaisie(Strdate);
						ph.setStatus(2);
						posHistoriqueOfSerial.add(ph);
					}
					System.out.println("posSerialNumStates = " + posSerialNumStates.getSerialNum());
					PosModel posModel = posModelRepository.findById(posSerialNumStates.getModel()).get();
					System.out.println(posModel.getModelCode());
					PosStock posStock = posStockRepository.getPosStockByMode(posModel.getModelCode());
					System.out.println(posStock.getStockReserve());

					posStock.setStockReserve(posStock.getStockReserve() + 1);
					posStock.setStockDisponible(posStock.getStockDisponible() - 1);

					System.out.println("TPE    " + pendingTpes.toString());
					System.out.println("model    " + posModel.getModelCode());
					p.setModel(Integer.toString(posModel.getModelCode()));
					p.setType(posModel.getCodeType());
					// pends.setStatus(1);
					p.setStatus(1);
					p.setSerialNum(posSerialNumStates.getSerialNum());
					ps.add(posStock);
					pendingTpess.add(p);
					// pendingTpes.add(pends);

					// }
				} else {
					PosType pT = posTypeRepository.findById(p.getType()).get();
					return new ResponseEntity<String>(" Stock of POS " + pT.getLibelle() + " < 0",
							HttpStatus.BAD_REQUEST);
				}

			}

		}
		t.setPendingTpes(pendingTpess);

		t.setStatus(1);
		t.setCommune(agence.getCommune());
		t.setDaira(agence.getDaira());
		t.setCodeZip(agence.getCodeZip());
		t.setPhone(agence.getPhone());
		t.setRevenue(agence.getRevenue());
		t.setSiteWeb(agence.getSiteWeb());
		t.setNomC(agence.getNomC());
		t.setTitleC(agence.getTitleC());
		if (agence.getTitleC().equals("employe")) {
			t.setTypeC("002");
		} else {
			t.setTypeC("001");
		}

		tpeRequestRepository.save(t);
		posStockRepository.saveAll(ps);
		posSerialNumStatesRepository.saveAll(posSerialNumStatess);
		posHistoriqueOfSerialRepository.saveAll(posHistoriqueOfSerial);
		pendingTpeRepository.saveAll(pendingTpess);
		tpeRequestRepository.save(t);

		/******************** Request historique **********************/
		HistoriqueRequestPos historiqueRequestPos = new HistoriqueRequestPos();
		historiqueRequestPos.setDateStatu(new Date());
		historiqueRequestPos.setStatut("Demande Modifié");
		historiqueRequestPos.setRequestCode(t.getRequestCode());
		historiqueRequestPos.setOperateur("Chargé Clientèle");
		historiqueRequestPosRepository.save(historiqueRequestPos);
		/******************** Request historique **********************/

		AgenceAdministration ag = agenceAdministrationRepository.findByIdAgence(t.getAgence()).get();
		Zone z = zoneRepository.findByCodeZone(ag.getCodeZone()).get();
		String recipientName = t.getUserName();
		String accountNumber = "0" + t.getAccountNumber();

		try {
			logger.info("**********begin mail validate Revesible**************");

			String nomClient = t.getRso();
			String numCompte = "0" + t.getAccountNumber();
			myEmailService.sendOtpMessage(ag.getEmail(),
					"Rectification de la demande suite au rejet du directeur d’agence",
					"Bonjour,\n\nNous vous informons qu’une demande de POS du commerçant \"" + nomClient
							+ "\" domicilié sous le N° de compte \"" + numCompte
							+ "\" a été rectifiée par le chargé de clientèle et demeure en attente de validation.\n\nCordialement");
			logger.info("**********end mail validate Revesible**************");

		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}

		logger.info("**********end validate Revesible**************");

		return ResponseEntity.ok().body(gson.toJson("validation level 3"));

	}

	/*
	 * try { String nomClient = t.getUserName(); String numCompte =
	 * "035 "+ag.getInitial()+" "+t.getAccountNumber();
	 * myEmailService.sendOtpMessage(ag.getEmail(),
	 * "Demande d'acquisition TPE en attente de validation",
	 * "Bonjour,\n\nNous vous informons que la demande d’acquisition TPE du client \""
	 * + nomClient + "\" domicilié sous le N° de compte \"" + numCompte +
	 * "\" est en attente de votre validation.\n\nCordialement"); } catch (Exception
	 * e) { String stackTrace=Throwables.getStackTraceAsString ( e ) ;
	 * 
	 * logger.info(stackTrace); return
	 * ResponseEntity.badRequest().body(gson.toJson("Email is not valid")); }
	 */

	@GetMapping("getListCommissionsByMerchantCode/{id}")
	List<TpeRequestDisplayDetails> getListCommissionsByMerchantCode(@PathVariable(value = "id") Integer merchantCode) {

		Merchant m = merchantRepository.findByMerchantCode(merchantCode);

		List<CommissionTpe> com = commissionTpeRepository.findByMerchantCode(m.getMerchantCode());
		List<TpeRequestDisplayDetails> ListCommission = new ArrayList<>();

		TpeRequestDisplayDetails tpeRequestDisplay = new TpeRequestDisplayDetails();
		CommissionType ct = commissionTypeRepository.getOne(m.getCommissionType());
		tpeRequestDisplay.setCommissionTypeCode(ct.getComTypeLibelle());
		tpeRequestDisplay.setCodeCommission(m.getCommissionType());
		for (CommissionTpe item : com) {
			tpeRequestDisplay.setRequestCode(item.getRequestCode());
			tpeRequestDisplay.setDateSaisie(item.getDateSaisie());

			if (item.getCommissionType() == 1) {
				CommissionTpe commission = commissionTpeRepository.findByRequestMerchantCode(m.getMerchantCode());
				tpeRequestDisplay.setCommissionNational(commission.getValeurCommissionNationaleFix());
				tpeRequestDisplay.setCommissionInterNational(commission.getValeurCommissionInternationaleFixe());

			} else {
				List<CommissionTpe> commissionTpes = commissionTpeRepository
						.findByMerchantCodeNational(m.getMerchantCode());

				System.out.println("COMMSSION NATIONAL" + commissionTpes.toString());
				List<CommissionTpe> CommissionTpesInter = commissionTpeRepository
						.findByMerchantCodeInternational(m.getMerchantCode());
				System.out.println("CommissionTpesInter" + CommissionTpesInter.size());

				tpeRequestDisplay.setCommissionTpesInter(CommissionTpesInter);
				tpeRequestDisplay.setCommissionTpes(commissionTpes);

			}
		}
		ListCommission.add(tpeRequestDisplay);

		List<Integer> listComHis = commissionTpeHistoriqueRepository.findByGroupeBy(m.getMerchantCode());

		for (Integer item : listComHis) {
			List<CommissionTpeHistorique> listComHisByRequest = commissionTpeHistoriqueRepository
					.findByRequestCode(item);
			TpeRequestDisplayDetails tpeRequestDisplayh = new TpeRequestDisplayDetails();

			for (CommissionTpeHistorique items : listComHisByRequest) {

				CommissionType cth = commissionTypeRepository.getOne(items.getCommissionType());
				tpeRequestDisplayh.setCommissionTypeCode(cth.getComTypeLibelle());
				tpeRequestDisplayh.setCodeCommission(items.getCommissionType());

				tpeRequestDisplayh.setRequestCode(item);
				tpeRequestDisplayh.setDateSaisie(items.getDateSaisie());

				if (items.getCommissionType() == 1) {

					CommissionTpeHistorique commission = commissionTpeHistoriqueRepository
							.findByRequestCodeComFixes(item);
					tpeRequestDisplayh.setCommissionNational(commission.getValeurCommissionNationaleFix());
					tpeRequestDisplayh.setCommissionInterNational(commission.getValeurCommissionInternationaleFixe());

				} else {
					List<CommissionTpeHistorique> commissionTpesh = commissionTpeHistoriqueRepository
							.findByRequestCodeNational(items.getRequestCode());

					List<CommissionTpe> commissionTpes = new ArrayList<>();

					for (CommissionTpeHistorique itemss : commissionTpesh) {
						CommissionTpe h = new CommissionTpe();
						h.setCommissionInterNational(itemss.getCommissionInterNational());
						h.setCommissionNational(itemss.getCommissionNational());
						h.setCommissionType(itemss.getCommissionType());
						h.setIdCommission(itemss.getIdCommission());
						h.setLabel(itemss.getLabel());
						h.setMerchantCode(itemss.getMerchantCode());
						h.setMontantRefMax(itemss.getMontantRefMax());
						h.setMontantRefMin(itemss.getMontantRefMin());
						h.setOperateurMax(itemss.getOperateurMax());
						h.setOperateurMin(itemss.getOperateurMin());
						h.setRequestCode(itemss.getRequestCode());
						h.setValeurComFixMax(itemss.getValeurComFixMax());
						h.setValeurComFixMin(itemss.getValeurComFixMin());
						h.setValeurCommissionInternationaleFixe(itemss.getValeurCommissionInternationaleFixe());
						h.setValeurCommissionNationaleFix(itemss.getValeurCommissionNationaleFix());
						h.setValeurComVariableMax(itemss.getValeurComVariableMax());
						h.setValeurComVariableMin(itemss.getValeurComVariableMin());
						h.setDateSaisie(itemss.getDateSaisie());
						commissionTpes.add(h);

					}
					List<CommissionTpeHistorique> CommissionTpesInterh = commissionTpeHistoriqueRepository
							.findByRequestCodeInternational(items.getRequestCode());
					System.out.println("CommissionTpesInter" + CommissionTpesInterh.size());

					List<CommissionTpe> commissionTpesListInter = new ArrayList<>();
					for (CommissionTpeHistorique itemsss : CommissionTpesInterh) {
						CommissionTpe h = new CommissionTpe();
						h.setCommissionInterNational(itemsss.getCommissionInterNational());
						h.setCommissionNational(itemsss.getCommissionNational());
						h.setCommissionType(itemsss.getCommissionType());
						h.setIdCommission(itemsss.getIdCommission());
						h.setLabel(itemsss.getLabel());
						h.setMerchantCode(itemsss.getMerchantCode());
						h.setMontantRefMax(itemsss.getMontantRefMax());
						h.setMontantRefMin(itemsss.getMontantRefMin());
						h.setOperateurMax(itemsss.getOperateurMax());
						h.setOperateurMin(itemsss.getOperateurMin());
						h.setRequestCode(itemsss.getRequestCode());
						h.setValeurComFixMax(itemsss.getValeurComFixMax());
						h.setValeurComFixMin(itemsss.getValeurComFixMin());
						h.setValeurCommissionInternationaleFixe(itemsss.getValeurCommissionInternationaleFixe());
						h.setValeurCommissionNationaleFix(itemsss.getValeurCommissionNationaleFix());
						h.setValeurComVariableMax(itemsss.getValeurComVariableMax());
						h.setValeurComVariableMin(itemsss.getValeurComVariableMin());
						h.setDateSaisie(itemsss.getDateSaisie());

						commissionTpesListInter.add(h);
					}
					tpeRequestDisplayh.setCommissionTpesInter(commissionTpesListInter);
					tpeRequestDisplayh.setCommissionTpes(commissionTpes);

				}

			}

			ListCommission.add(tpeRequestDisplayh);

		}

		return ListCommission;
	}

	@GetMapping("getOneMerchant/{id}")
	MerchantListDisplay getOneMerchant(@PathVariable(value = "id") Integer merchantCode) {
		// Merchant m = merchantRepository.findById(merchantCode).get();
		System.out.println(merchantCode);
		MerchantListDisplay md = new MerchantListDisplay();

		Merchant m = merchantRepository.findByMerchantCode(merchantCode);

		Account account = accountRepository.getOne(m.getAccount());
		String ribAcc = account.getAccountNum().substring(7, account.getAccountNum().length());
		AgenceAdministration agence = agenceAdministrationRepository.findByIdAgence(account.getIdAgence()).get();
		md.setAccountNumber(ribAcc);
		md.setAgence(agence.getInitial());
		md.setOffshore(m.getOffshore());
		md.setNameMerchant(m.getMerchantLibelle());
		md.setCity(m.getCity());
		md.setCodeZip(m.getCodeZip());
		md.setCountry(m.getCountry());
		md.setAdresse(m.getAddress());
		md.setPhone(m.getPhone());
		md.setEmail(m.getEmail());
		md.setCommune(m.getCommune());
		md.setDaira(m.getDaira());

		md.setRevenue(m.getRevenue());
		md.setRc(m.getRc());
		md.setRso(m.getRso());
		md.setNif(m.getNif());
		System.out.println("phone " + md.getPhone());
		return md;

	}

	@PostMapping("editMerchant/{id}")
	public ResponseEntity<String> editMerchant(@PathVariable(value = "id") Integer merchantCode,
			@RequestBody MerchantListDisplay merchant) {
		AgenceAdministration agence = agenceAdministrationRepository
				.findByIdAgence(Integer.parseInt(merchant.getAgence())).get();

		String rib = "35" + agence.getInitial() + merchant.getAccountNumber();
		//
		Merchant m = merchantRepository.findByMerchantCode(merchantCode);

		System.out.println(rib);
		// System.out.println("account "+account.getAccountNum());

		if (accountRepository.existsByAccountNum(rib)) {
			Account account = accountRepository.findByAccountNum(rib);
			logger.info("account founded");
			System.out.println("account founded");
			m.setMerchantLibelle(merchant.getNameMerchant());
			m.setCity(merchant.getCity());
			m.setCodeZip(merchant.getCodeZip());
			m.setCountry(merchant.getCountry());
			m.setAddress(merchant.getAdresse());
			m.setPhone(merchant.getPhone());
			m.setOffshore(merchant.isOffshore());
			m.setEmail(merchant.getEmail());
			m.setCommune(merchant.getCommune());
			m.setDaira(merchant.getDaira());
			m.setAccount(account.getAccountCode());
			m.setRevenue(merchant.getRevenue());
			m.setRc(merchant.getRc());
			m.setRso(merchant.getRso());
			m.setNif(merchant.getNif());
			m.setStatusBm("C");
			m.setStatusBs("A");

			merchantRepository.save(m);
		} else {
			logger.info("account not founded");
			System.out.println("account not founded");
			Account acc = new Account(null, rib, 1, Integer.toString(m.getMerchantCode()), rib,
					merchant.getNameMerchant(), BigInteger.valueOf(0), BigInteger.valueOf(0), BigInteger.valueOf(0),
					BigInteger.valueOf(0), "208", new Date(), 5, agence.getCodeAgence());
			accountRepository.save(acc);

			m.setMerchantLibelle(merchant.getNameMerchant());
			m.setCity(merchant.getCity());
			m.setCodeZip(merchant.getCodeZip());
			m.setCountry(merchant.getCountry());
			m.setAddress(merchant.getAdresse());
			m.setPhone(merchant.getPhone());
			m.setOffshore(merchant.isOffshore());
			m.setEmail(merchant.getEmail());
			m.setCommune(merchant.getCommune());
			m.setDaira(merchant.getDaira());
			m.setAccount(acc.getAccountCode());
			m.setRevenue(merchant.getRevenue());
			m.setRc(merchant.getRc());
			m.setRso(merchant.getRso());
			m.setNif(merchant.getNif());
			m.setStatusBm("C");

			merchantRepository.save(m);

		}

		/******************** historiqueCommercant **********************/
		HistoriqueCommercant historiqueCommercant = new HistoriqueCommercant();
		historiqueCommercant.setDateStatu(new Date());
		historiqueCommercant.setStatut("Les informations du Commerçant ont été mises à jour");
		historiqueCommercant.setMerchantCode(merchantCode);
		historiqueCommercantRepository.save(historiqueCommercant);
		/******************** historiqueCommercant **********************/

		List<String> recipients = userRepository.getByAllUserAgence(agence.getCodeAgence());
		System.out.println("recipients " + recipients);
		String nomClient = m.getRso();
		String numCompte = "0" + rib;

		try {

			myEmailService.sendOtpMessage("Monetique@fransabank.dz", "Modification des informations Commerçant",
					"Bonjour,\n\n" + "Nous vous informons qu’une modification des informations du commerçant \""
							+ nomClient + "\" domicilié sous le N° de compte \"" + numCompte + "\" a été " + "Validée"
							+ " est en attente de validation.\n\n" + "Cordialement,\n");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}

		try {

			myEmailService.sendOtpMessage(agence.getEmail(), "Modification des informations Commerçant",
					"Bonjour,\n\n" + "Nous vous informons qu’une modification des informations du commerçant \""
							+ nomClient + "\" domicilié sous le N° de compte \"" + numCompte + "\" a été " + "Validée"
							+ " est en attente de validation.\n\n" + "Cordialement,\n");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}

		try {
			for (String recipient : recipients) {
				myEmailService.sendOtpMessage(recipient, "Modification des informations Commerçant",
						"Bonjour,\n\n" + "Nous vous informons qu’une modification des informations du commerçant \""
								+ nomClient + "\" domicilié sous le N° de compte \"" + numCompte + "\" a été "
								+ "Validée" + " est en attente de validation.\n\n" + "Cordialement,\n");
			}
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}

		return ResponseEntity.accepted().body(gson.toJson("update merchant successfully!"));

	}

	@PostMapping(value = "generateFileTM")
	@ResponseBody
	ResponseEntity<FileTerminauxRequest> writeInFile()
			throws ResourceNotFoundException, FileNotFoundException, UnsupportedEncodingException {
		try {
			logger.info("*********BEGIN generate File TM********");

			FileTerminauxRequest fileRequests = new FileTerminauxRequest();

			Date date = new Date();

			String sTm;

			DateFormat formatter8;
			formatter8 = new SimpleDateFormat("yyyy-MM-dd");

			sTm = formatter8.format(date);

			List<PosTerminal> posTerminals = new ArrayList<>();
			DateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd");
			String Destination = propertyService.getPosFilesPath();

			BatchPos batch = batchPosRepository.findByKeys("generateTM").get();

			batch.setBatchStatus(0);
			batch.setBatchDate(new Date());
			batch.setError(null);
			batch.setErrorStackTrace(null);
			batch.setBatchLibelle("Generation file TM");
			batch.setFileName("TM");
			batch.setFileLocation(Destination);
			batchPosRepository.save(batch);
			String dateCreation = formatter1.format(date);
			String s;
			DateFormat formatter;
			formatter = new SimpleDateFormat("yyMMdd");
			s = formatter.format(date);
			System.out.println(s);

			List<SequentielSerieTM> sqTM = sequentielSerieTMRepository.findAllSequentielSerieTMByDate(sTm);
			int numOrdre = 0;
			List<SequentielSerieTM> ListSequ = new ArrayList<>();
			if (sqTM.size() == 0) {
				numOrdre = numOrdre + 1;
				SequentielSerieTM bs = new SequentielSerieTM();
				bs.setDateG(sTm);
				bs.setSequence(numOrdre);
				ListSequ.add(bs);

			} else {

				numOrdre = sqTM.get(sqTM.size() - 1).getSequence() + 1;
				SequentielSerieTM bs = new SequentielSerieTM();
				bs.setDateG(sTm);
				bs.setSequence(numOrdre);
				ListSequ.add(bs);

			}

			System.out.println("numOrdre" + numOrdre);
			List<Merchant> merchant = merchantRepository.findByMerchantToTM();
			logger.info("Size of List Merchant is " + merchant.size());

			if (merchant.size() != 0) {

				String name = SecurityContextHolder.getContext().getAuthentication().getName();
				User user = userRepository.findByUserNameOrUserEmail(name, name).get();
				String Source_id = String.format("%08d", user.getUserCode());
				System.out.println("Source_id " + Source_id);

				StringBuilder str = new StringBuilder("");

				String nOr = String.format("%02d", numOrdre);

				String Entete = "000" + "TM" + s + nOr + "MONETIQU" + "502127" + "X";
				String fileName = "TM" + s + nOr;

				int nbEnregistrement = 2;
				int nbS = 0;
				List<PosTerminal> posTerminal = posTerminalRepository.getAllposTofileTM();

				logger.info("Size of List POS is " + posTerminal.size());

				if (posTerminal.size() != 0) {
					str.append(Entete);
					str.append(System.getProperty("line.separator"));
					for (PosTerminal items : posTerminal) {
						PosTerminal pos = posTerminalRepository.getOne(items.getPosCode());
						logger.info("POS is " + pos);

						Merchant m = merchantRepository.findById(items.getMerchantCode().getMerchantCode()).get();
						logger.info("Merchant is " + m);

						if (m != null) {
							if (m.getStatusBm().equals("F")) {

								pos.setStatus("En attente de déploiement");
								logger.info("****Update Status***");

							}

							pos.setGenerationTM("F");
							posTerminals.add(pos);
							nbEnregistrement = nbEnregistrement + 1;
							nbS = nbS + 1;
							DateFormat formatter2 = new SimpleDateFormat("yy");
							// String ans = formatter2.format(mm.getCreationDate());
							String nbSequentielle = String.format("%07d", nbS);
							String codeM = "A";
							if (items.getFileTM().equals("C")) {
								codeM = "C";
							}
							if (items.getFileTM().equals("D")) {
								codeM = "D";
							}

							// Merchant m =
							// merchantRepository.findById(items.getMerchantCode().getMerchantCode()).get();
							String NumContrat = "";
							if (m.getIdContrat().length() >= 15) {
								NumContrat = m.getIdContrat().substring(0, 15);
							} else {
								NumContrat = org.apache.commons.lang3.StringUtils.rightPad(m.getIdContrat(), 15, ' ');

							}

							String identifiantTerminal = org.apache.commons.lang3.StringUtils.rightPad("", 15, ' ');

							// String idT = String.format("%015d", items.getPosCode());
							String libelleT = "";
							if (items.getPosLibelle().length() >= 30) {
								libelleT = items.getPosLibelle().substring(0, 30);
							} else {
								libelleT = org.apache.commons.lang3.StringUtils.rightPad(items.getPosLibelle(), 30,
										' ');

							}
							String adresse = items.getAdresse();
							if (adresse.length() >= 40) {
								adresse = adresse.substring(0, 40);
							}
							String adresseTerminal = org.apache.commons.lang3.StringUtils.rightPad(adresse, 40, ' ');

							String codeCommune = org.apache.commons.lang3.StringUtils.rightPad("", 2, ' ');

							String commune = items.getCommune();

							if (commune.length() >= 20) {
								commune = commune.substring(0, 20);
							}
							String daira = items.getDaira();

							if (daira.length() >= 20) {
								daira = daira.substring(0, 20);
							}

							String wilaya = items.getCity();

							if (wilaya.length() >= 15) {
								wilaya = wilaya.substring(0, 15);
							}

							String nomCommune = org.apache.commons.lang3.StringUtils.rightPad(commune, 20, ' ');
							String nomDaira = org.apache.commons.lang3.StringUtils.rightPad(daira, 20, ' ');
							String codeWilaya = "0" + m.getCodeWilaya();
							String nomWilaya = org.apache.commons.lang3.StringUtils.rightPad(wilaya, 15, ' ');
							String codePostal = org.apache.commons.lang3.StringUtils.rightPad(m.getCodeZip(), 5, ' ');

							String telT = org.apache.commons.lang3.StringUtils.rightPad("+213" + items.getPhone(), 15,
									' ');
							String faxT = org.apache.commons.lang3.StringUtils.rightPad("", 15, ' ');
							String phoneT = org.apache.commons.lang3.StringUtils.rightPad("", 15, ' ');
							String emailT = org.apache.commons.lang3.StringUtils.rightPad(m.getEmail(), 50, ' ');

							String TRX_Debit = "1";
							String TRX_Remb = "1";
							String TRX_Annul = "1";
							String TRX_Solde = "1";
							String TRX_P_Autor = "1";
							String TRX_Tel = "1";
							String TRX_Retrait = "1";
							String TRX_Cash_Advance = "1";
							String TRX_Paiement = "1";

							String H_deb = "0000";
							String H_fin = "2359";
							String MAJtypeCarte = "A";
							if (items.getStatus().equals("SUPPRIME")) {
								MAJtypeCarte = "C";
							}

							String TypeCarteC = org.apache.commons.lang3.StringUtils.rightPad("C0", 2, ' ');

							String Limit = org.apache.commons.lang3.StringUtils.rightPad("001000000", 9, ' ');

							String TypeCarteC1 = "C1";
							String Limit1 = org.apache.commons.lang3.StringUtils.rightPad("001000000", 9, ' ');

							String TypeCarteC2 = "C2";
							String Limit2 = org.apache.commons.lang3.StringUtils.rightPad("001000000", 9, ' ');

							String TypeCarteC3 = "C3";
							String Limit3 = org.apache.commons.lang3.StringUtils.rightPad("001000000", 9, ' ');

							String TypeCarteC4 = "C4";
							String Limit4 = org.apache.commons.lang3.StringUtils.rightPad("001000000", 9, ' ');

							String TypeCarteC5 = "C5";
							String Limit5 = org.apache.commons.lang3.StringUtils.rightPad("001000000", 9, ' ');

							String TypeCarteC6 = "C6";
							String Limit6 = org.apache.commons.lang3.StringUtils.rightPad("001000000", 9, ' ');

							String TypeTerminal = org.apache.commons.lang3.StringUtils.rightPad("", 2, ' ');
							if (items.getFamillePosCode() == 1) {
								TypeTerminal = org.apache.commons.lang3.StringUtils.rightPad("25", 2, ' ');

							}

							else if (items.getFamillePosCode() == 4) {
								TypeTerminal = org.apache.commons.lang3.StringUtils.rightPad("24", 2, ' ');
							} else {
								TypeTerminal = org.apache.commons.lang3.StringUtils.rightPad(items.getTypeTerminal(), 2,
										' ');
							}

							String FinEnreg = "X";

							String dataT = "001" + nbSequentielle + codeM + NumContrat + identifiantTerminal + libelleT
									+ adresseTerminal + codeCommune + nomCommune + nomDaira + codeWilaya + nomWilaya
									+ codePostal + telT + faxT + phoneT + emailT + TRX_Debit + TRX_Remb + TRX_Annul
									+ TRX_Solde + TRX_P_Autor + TRX_Tel + TRX_Retrait + TRX_Cash_Advance + TRX_Paiement
									+ H_deb + H_fin + MAJtypeCarte + TypeCarteC + Limit + TypeCarteC1 + Limit1
									+ TypeCarteC2 + Limit2 + TypeCarteC3 + Limit3 + TypeCarteC4 + Limit4 + TypeCarteC5
									+ Limit5 + TypeCarteC6 + Limit6 + TypeTerminal + FinEnreg;

							str.append(dataT);
							str.append(System.getProperty("line.separator"));

						}
					}
					String nbEn = String.format("%07d", nbEnregistrement);

					String Fin = "999" + nbEn + "X";
					str.append(Fin);
					File fileRequest = new File(Destination + fileName);
					PrintWriter writer = new PrintWriter(fileRequest, "UTF-8");
					writer.println(str);
					writer.close();
					fileRequests.setData(str.toString());
					fileRequests.setNameTitle(fileName);
					posTerminalRepository.saveAll(posTerminals);
					sequentielSerieTMRepository.saveAll(ListSequ);

				}
			}
			batchPosRepository.updateFinishBatch("generateTM", 1, new Date());

			logger.info("*********END generate File TM********");

			// }

			return ResponseEntity.ok().body(fileRequests);

		} catch (Exception e) {

			logger.info("Exception is=>{}", e.toString());
			e.printStackTrace();
			String stackTrace = Throwables.getStackTraceAsString(e);
			if (stackTrace.length() > 4000)
				stackTrace = stackTrace.substring(0, 3999);

			batchPosRepository.updateStatusAndErrorBatch("generateTM", 2,
					e.getMessage() == null ? e.toString() : e.getMessage(), new Date(), stackTrace);
			return null;
		}

	}

	@PostMapping(value = "generateFileBM")
	@ResponseBody
	ResponseEntity<FileTerminauxRequest> writeCommercantFile()
			throws ResourceNotFoundException, FileNotFoundException, UnsupportedEncodingException {
		FileTerminauxRequest fileRequests = new FileTerminauxRequest();

		try {
			List<PosTerminal> posTerminals = new ArrayList<>();

			List<Merchant> merchant = merchantRepository.getListMerchantstausBm();
			System.out.println("merchant " + merchant.size());
			if (merchant.size() != 0) {
				BatchPos batch = batchPosRepository.findByKeys("generateBM").get();

				String Destination = propertyService.getPosFilesPath();

				batch.setBatchStatus(0);
				batch.setBatchDate(new Date());
				batch.setError(null);
				batch.setErrorStackTrace(null);
				batch.setFileName("BM");
				batch.setFileLocation(Destination);
				batch.setBatchLibelle("Generation file BM");
				batchPosRepository.save(batch);

				String name = SecurityContextHolder.getContext().getAuthentication().getName();
				User user = userRepository.findByUserNameOrUserEmail(name, name).get();
				String Source_id = String.format("%08d", user.getUserCode());
				System.out.println("Source_id " + Source_id);
				StringBuilder str = new StringBuilder("");

				Date date = new Date();
				String sTm;

				DateFormat formatter8;
				formatter8 = new SimpleDateFormat("yyyy-MM-dd");

				sTm = formatter8.format(date);

				List<SequentielSerieBM> sqTM = sequentielSerieBMRepository.findAllSequentielSerieBMByDate(sTm);
				int numOrdre = 0;
				if (sqTM.size() == 0) {
					numOrdre = numOrdre + 1;
					SequentielSerieBM bs = new SequentielSerieBM();
					bs.setDateG(sTm);
					bs.setSequence(numOrdre);
					sequentielSerieBMRepository.save(bs);
				} else {

					numOrdre = sqTM.get(sqTM.size() - 1).getSequence() + 1;
					SequentielSerieBM bs = new SequentielSerieBM();
					bs.setDateG(sTm);
					bs.setSequence(numOrdre);
					sequentielSerieBMRepository.save(bs);
				}

				String s;
				DateFormat formatter;
				formatter = new SimpleDateFormat("yyMMdd");
				s = formatter.format(date);
				int nbS = 0;

				// int nbOr = numOrdre + 1;
				int nbEnregistrement = 2;
				String nOr = String.format("%02d", numOrdre);

				String Entete = "000" + "BM" + s + nOr + "MONETIQU" + "000035" + "X";
				String fileName = "BM" + s + nOr;

				str.append(Entete);
				str.append(System.getProperty("line.separator"));

				for (Merchant item : merchant) {
					List<PosTerminal> posTerminal = posTerminalRepository.findAllByMerchantCode(item.getMerchantCode());
					if (posTerminal.size() != 0) {
						MccList mcc = null;
						for (PosTerminal posT : posTerminal) {
							/*
							 * PosTerminal pos = posTerminalRepository.getOne(posT.getPosCode());
							 * pos.setStatus("En attente de déploiement"); posTerminals.add(pos);
							 */
							mcc = mccRepository.findByMccCode(posT.getMccCode().getMccCode());

						}
						String lcommerce = mcc.getMccListLibelle();
						if (lcommerce.length() >= 40) {
							lcommerce = lcommerce.substring(0, 37);
						}

						String libelleCommerce = org.apache.commons.lang3.StringUtils.rightPad(lcommerce, 40, ' ');

						Merchant m = merchantRepository.getOne(item.getMerchantCode());

						nbEnregistrement = nbEnregistrement + 1;
						nbS = nbS + 1;
						String nbSequentielle = String.format("%07d", nbS);
						String codeM = "A";
//						if (item.getMerchantStatus().equals("4")) {
//							codeM = "D";
//						}
//
//						if ((item.getStatusBm() == null)
//								|| (!item.getMerchantStatus().equals("4") && !item.getStatusBm().equals("C"))) {
//							codeM = "A";
//
//						}
//						if (item.getStatusBm() != null && item.getStatusBm().equals("C")) {
//							codeM = "C";
//						}

//						if(item.getMerchantStatus().equals("4")) {
//		         			codeM = "D";
//		         		}else {
//		         			if (item.getStatusBm()!=null) {
//		         				if(item.getStatusBm().equals("C"))
//		                 			codeM = "C";
//		                 		
//		         				else 
//		                 			codeM = "A";
//		         			}
//		         			
//		         		}

						System.out.println("codeM " + codeM);
						Account account = accountRepository.getOne(item.getAccount());
						AgenceAdministration agence = agenceAdministrationRepository
								.findByIdAgence(account.getIdAgence()).get();
						/************* web service FSBK ***************/
						String rib = "0" + account.getAccountNum();

						List<TpeRequest> Request = tpeRequestRepository.findAllByAccountNumber(account.getAccountNum());

						DateFormat formatter1 = new SimpleDateFormat("yy");
						String ans = formatter1.format(item.getCreationDate());
						String thisAns = formatter1.format(date);

						String NumContrat = item.getIdContrat();
						String idFiscaleB = "001";
						String idFiscaleC = "";
						if (item.getNif().length() >= 15) {
							idFiscaleC = item.getNif().substring(0, 15);
						} else {
							idFiscaleC = org.apache.commons.lang3.StringUtils.rightPad(item.getNif(), 15, ' ');
						}

						String RaisonSocialCom = "";
						if (item.getRso().length() >= 30) {
							RaisonSocialCom = item.getRso().substring(0, 30);
						} else {
							RaisonSocialCom = org.apache.commons.lang3.StringUtils.rightPad(item.getRso(), 30, ' ');
						}
						String NomCom = "";
						if (item.getMerchantLibelle().length() >= 30) {
							NomCom = item.getMerchantLibelle().substring(0, 30);
						} else {
							NomCom = org.apache.commons.lang3.StringUtils.rightPad(item.getMerchantLibelle(), 30, ' ');
						}

						int nbA = Integer.parseInt(ans) - Integer.parseInt(thisAns);

						String nbAns = String.format("%03d", nbA);
						String NomConatactp = "";
						String typeContact = "";
						String titreC = "";
						if (Request.size() != 0) {
							titreC = org.apache.commons.lang3.StringUtils.rightPad(Request.get(0).getTitleC(), 15, ' ');
							if (Request.get(0).getNomC().length() >= 25) {
								NomConatactp = Request.get(0).getNomC().substring(0, 25);

							} else {
								NomConatactp = org.apache.commons.lang3.StringUtils.rightPad(Request.get(0).getNomC(),
										25, ' ');
							}

							if (Request.get(0).getTitleC().equals("employe")) {
								typeContact = org.apache.commons.lang3.StringUtils.rightPad("002", 3, ' ');
							} else {
								typeContact = org.apache.commons.lang3.StringUtils.rightPad("001", 3, ' ');
							}
						} else {
							NomConatactp = org.apache.commons.lang3.StringUtils.rightPad(NomConatactp, 25, ' ');
							typeContact = org.apache.commons.lang3.StringUtils.rightPad(typeContact, 3, ' ');
							titreC = org.apache.commons.lang3.StringUtils.rightPad(titreC, 15, ' ');

						}

						String Nom2emeContactCom = org.apache.commons.lang3.StringUtils.rightPad("", 25, ' ');

						String numRegistreCom = "";
						if (item.getRc().length() >= 30) {
							numRegistreCom = item.getRc().substring(0, 30);
						} else {
							numRegistreCom = org.apache.commons.lang3.StringUtils.rightPad(item.getRc(), 30, ' ');
						}

						String adressL1 = org.apache.commons.lang3.StringUtils.rightPad("", 3, ' ');
						String adresse = item.getAddress();
						if (adresse.length() >= 37) {
							adresse = adresse.substring(0, 37);
						}

						String adressL2 = org.apache.commons.lang3.StringUtils.rightPad(adresse, 37, ' ');
						String adressL3 = org.apache.commons.lang3.StringUtils.rightPad("", 2, ' ');

						String adresse4 = item.getCommune();
						if (adresse4.length() >= 20) {
							adresse4 = adresse4.substring(0, 20);
						}

						String daira = item.getDaira();

						if (daira.length() >= 20) {
							daira = daira.substring(0, 20);
						}

						String wilaya = item.getCity();

						if (wilaya.length() >= 15) {
							wilaya = wilaya.substring(0, 15);
						}
						String adressL4 = org.apache.commons.lang3.StringUtils.rightPad(adresse4, 20, ' ');
						String adressL5 = org.apache.commons.lang3.StringUtils.rightPad(daira, 20, ' ');
						String codeWilaya = "0" + item.getCodeZip().substring(0, 2);
						System.out.println("codeWilaya =" + codeWilaya);
						String adressL6 = org.apache.commons.lang3.StringUtils.rightPad(codeWilaya, 3, ' ');
						String adressL7 = org.apache.commons.lang3.StringUtils.rightPad(wilaya, 15, ' ');
						String adressL8 = org.apache.commons.lang3.StringUtils.rightPad(item.getCodeZip(), 5, ' ');
						String CategorieCommerce = org.apache.commons.lang3.StringUtils
								.rightPad("0000" + mcc.getMccListId(), 8, ' ');
						String numTel = org.apache.commons.lang3.StringUtils.rightPad("+213" + item.getPhone(), 15,
								' ');
						String numFax = org.apache.commons.lang3.StringUtils.rightPad("", 15, ' ');
						String numMobile = org.apache.commons.lang3.StringUtils.rightPad("", 15, ' ');
						String email = org.apache.commons.lang3.StringUtils.rightPad(item.getEmail(), 50, ' ');

						String codeAgence = org.apache.commons.lang3.StringUtils.rightPad(agence.getInitial(), 5, ' ');
						String libelleAgenceCommercant = org.apache.commons.lang3.StringUtils
								.rightPad(agence.getLibelle(), 35, ' ');
						String siteWeb = "";
						if (Request.size() != 0) {
							if (Request.get(0).getSiteWeb() != null) {
								siteWeb = Request.get(0).getSiteWeb();
							} else {
								siteWeb = "";
							}
						}
						String adressesiteWeb = org.apache.commons.lang3.StringUtils.rightPad(siteWeb, 40, ' ');
						String SeuilCommercant = org.apache.commons.lang3.StringUtils.rightPad("000099999999", 12, ' ');

						String dataCom = "001" + nbSequentielle + codeM + NumContrat + idFiscaleB + idFiscaleC
								+ RaisonSocialCom + NomCom + nbAns + NomConatactp + typeContact + titreC
								+ Nom2emeContactCom + numRegistreCom + adressL1 + adressL2 + adressL3 + adressL4
								+ adressL5 + adressL6 + adressL7 + adressL8 + CategorieCommerce + libelleCommerce
								+ numTel + numFax + numMobile + email + rib + codeAgence + libelleAgenceCommercant
								+ adressesiteWeb + SeuilCommercant + "X";

						str.append(dataCom);
						str.append(System.getProperty("line.separator"));

						m.setStatusBm("F");

						merchantRepository.save(m);

						// }
					}
				}

				String nbEn = String.format("%07d", nbEnregistrement);

				String Fin = "999" + nbEn + "X";
				str.append(Fin);

				File fileRequest = new File(Destination + fileName);
				PrintWriter writer = new PrintWriter(fileRequest, "UTF-8");
				writer.println(str);
				writer.close();
				fileRequests.setData(str.toString());
				fileRequests.setNameTitle(fileName);
				batchPosRepository.updateFinishBatch("generateBM", 1, new Date());
				posTerminalRepository.saveAll(posTerminals);

			}
			return ResponseEntity.ok().body(fileRequests);

		} catch (Exception e) {
			logger.info("Exception is=>{}", e.toString());
			e.printStackTrace();
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info(stackTrace);
			if (stackTrace.length() > 4000)
				stackTrace = stackTrace.substring(0, 3999);

			batchPosRepository.updateStatusAndErrorBatch("generateBM", 2,
					e.getMessage() == null ? e.toString() : e.getMessage(), new Date(), stackTrace);
			return null;
		}

	}

	@PutMapping("DemandeTodeletePosTerminal/{id}")
	public ResponseEntity<String> DemandeTodeletePosTerminal(@PathVariable(value = "id") Integer idPos,
			@RequestBody RequestDeletePos requestDeletePos) {

		PosTerminal posTerminal = posTerminalRepository.getOne(idPos);
		if (!posTerminal.getSerialNum().equals("")) {
			PosSerialNumStates pserial = posSerialNumStatesRepository.getOne(posTerminal.getSerialNum());
			pserial.setStatuResiliation(requestDeletePos.getStatuSerial());
		}
		posTerminal.setStatusSup("SUPPRIME");
		posTerminal.setDetailSupp(requestDeletePos.getDetailSupp());
		// posTerminal.setStatuSerial(requestDeletePos.getStatuSerial());

		String name = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByUserNameOrUserEmail(name, name).get();
		posTerminal.setEmailCharge(user.getUserEmail());
		posTerminalRepository.save(posTerminal);

		AgenceAdministration ag = agenceAdministrationRepository.findByInitial(posTerminal.getAgence()).get();

		String nomClient = posTerminal.getMerchantCode().getRso();

		Account a = accountRepository.findById(posTerminal.getMerchantCode().getAccount()).get();
		String numCompte = "0 " + a.getAccountNum();
		try {

			myEmailService.sendOtpMessage(ag.getEmail(), "Suppression TPE",
					"Bonjour,\n\nNous vous informons qu’une demande de suppression TPE du commerçant \"" + nomClient
							+ "\" domicilié sous le N° de compte \"" + numCompte
							+ "\" est en attente de validation.\n\nCordialement.");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}

		try {

			myEmailService.sendOtpMessage("Monetique@fransabank.dz", "Suppression POS",
					"Bonjour,\n\nNous vous informons qu’une demande de Suppression POS du commerçant \"" + nomClient
							+ "\" domicilié sous le N° de compte \"" + numCompte
							+ "\"  a été validée.\n\nCordialement.");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}

		return ResponseEntity.ok().body(gson.toJson("PosTerminal DELETED"));
	}

	@PutMapping("DemandeToChangeSerial/{id}")
	public ResponseEntity<String> DemandeToChangeSerial(@PathVariable(value = "id") Integer idPos,
			@RequestBody RequestDeletePos requestDeletePos) {

		PosTerminal posTerminal = posTerminalRepository.getOne(idPos);
		posTerminal.setStatusUpdtae("MODIFIER");

		posTerminal.setDetailUpdate(requestDeletePos.getDetailSupp());
		// posTerminal.setStatuSerial(requestDeletePos.getStatuSerial());

		PosSerialNumStates ps = posSerialNumStatesRepository.getOne(posTerminal.getSerialNum());
		ps.setStatuResiliation(requestDeletePos.getStatuSerial());

		String name = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByUserNameOrUserEmail(name, name).get();
		posTerminal.setEmailCharge(user.getUserEmail());
		posSerialNumStatesRepository.save(ps);
		posTerminalRepository.save(posTerminal);

		AgenceAdministration ag = agenceAdministrationRepository.findByInitial(posTerminal.getAgence()).get();
		String nomClient = posTerminal.getMerchantCode().getRso();

		Account a = accountRepository.findById(posTerminal.getMerchantCode().getAccount()).get();
		String numCompte = "0 " + a.getAccountNum();

		try {

			myEmailService.sendOtpMessage(ag.getEmail(), "Remplacement POS",
					"Bonjour,\n\nNous vous informons qu’une demande de remplacement POS du commerçant \"" + nomClient
							+ "\" domicilié sous le N° de compte \"" + numCompte
							+ "\" est en attente de validation.\n\nCordialement.");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}
		try {

			myEmailService.sendOtpMessage("Monetique@fransabank.dz", "Remplacement POS",
					"Bonjour,\n\nNous vous informons qu’une demande de remplacement POS du commerçant \"" + nomClient
							+ "\" domicilié sous le N° de compte \"" + numCompte
							+ "\" est en attente de validation.\n\nCordialement.");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}

		return ResponseEntity.ok().body(gson.toJson("DEMANDE VALIDER"));
	}

	@PutMapping("deletePosTerminal/{id}")
	public ResponseEntity<String> deletePosTerminal(@PathVariable(value = "id") Integer idPos,
			@RequestBody RequestDeletePos requestDeletePos) {

		PosTerminal posTerminal = posTerminalRepository.getOne(idPos);
		posTerminal.setStatuSerial(requestDeletePos.getStatuSerial());
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.out.println(timestamp);
		String Strdate = sdf3.format(timestamp);

		if (!posTerminal.getSerialNum().equals("")) {
			System.out.println("testttttttt");
			PosSerialNumStates pserial = posSerialNumStatesRepository.getOne(posTerminal.getSerialNum());
			pserial.setStatuResiliation(requestDeletePos.getStatuSerial());
			PosModel posModel = posModelRepository.getOne(pserial.getModel());
			PosStock ps = posStockRepository.getPosStockByMode(posModel.getModelCode());
			PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
			if (requestDeletePos.getStatuSerial().equals("TPE retourne")) {

				if (pserial.getStatus() == 6) {

					pserial.setStatus(6);
					posStockRepository.save(ps);
					posSerialNumStatesRepository.save(pserial);

				} else if (pserial.getStatus() == 3) {
					ps.setStockDisponible(ps.getStockDisponible() + 1);
					ps.setStockConsome(ps.getStockConsome() - 1);
					pserial.setStatus(1);
					ph.setSerialNum(pserial.getSerialNum());
					ph.setDateSaisie(Strdate);
					ph.setStatus(1);
					posHistoriqueOfSerialRepository.save(ph);
					posStockRepository.save(ps);
					posSerialNumStatesRepository.save(pserial);

				} else if (pserial.getStatus() == 4) {
					ps.setStockDisponible(ps.getStockDisponible() + 1);
					ps.setStockDeployer(ps.getStockDeployer() - 1);
					pserial.setStatus(1);
					ph.setSerialNum(pserial.getSerialNum());
					ph.setDateSaisie(Strdate);
					ph.setStatus(1);
					posHistoriqueOfSerialRepository.save(ph);
					posStockRepository.save(ps);
					posSerialNumStatesRepository.save(pserial);

				} else if (pserial.getStatus() == 5) {

					ps.setStockDisponible(ps.getStockDisponible() + 1);
					ps.setStockHS(ps.getStockHS() - 1);

					pserial.setStatus(1);
					posStockRepository.save(ps);
					posSerialNumStatesRepository.save(pserial);

				}
			}

			if (!requestDeletePos.getStatuSerial().equals("TPE retourne")) {
				ps.setStockHS(ps.getStockHS() + 1);

				if (pserial.getStatus() == 3) {
					ps.setStockConsome(ps.getStockConsome() - 1);


				} else if (pserial.getStatus() == 4) {
					ps.setStockDeployer(ps.getStockDeployer() - 1);


				} 
				ph.setSerialNum(pserial.getSerialNum());
				ph.setDateSaisie(Strdate);
				ph.setStatus(5);
				posHistoriqueOfSerialRepository.save(ph);
				pserial.setStatus(5);
				posStockRepository.save(ps);
				posSerialNumStatesRepository.save(pserial);

			}

		}
		posTerminal.setStatus("SUPPRIME");
		posTerminal.setSerialNum("");
		posTerminal.setFileTM("D");
		posTerminal.setFileTS("");
		AgenceAdministration ag = agenceAdministrationRepository.findByInitial(posTerminal.getAgence()).get();
		String nomClient = posTerminal.getMerchantCode().getRso();

		Account a = accountRepository.findById(posTerminal.getMerchantCode().getAccount()).get();
		String numCompte = "0 " + a.getAccountNum();

		try {

			myEmailService.sendOtpMessage(ag.getEmail(), "Suppression TPE",
					"Bonjour,\n\nNous vous informons qu’une demande de suppression TPE du commerçant \"" + nomClient
							+ "\" domicilié sous le N° de compte \"" + numCompte
							+ "\" a été validée.\n\nCordialement.");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}

		try {

			myEmailService.sendOtpMessage("Monetique@fransabank.dz", "Suppression POS",
					"Bonjour,\n\nNous vous informons qu’une demande de suppression TPE du commerçant \"" + nomClient
							+ "\" domicilié sous le N° de compte \"" + numCompte
							+ "\" a été validée.\n\nCordialement.");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}

		try {

			myEmailService.sendOtpMessage(posTerminal.getEmailCharge(), "Suppression POS",
					"Bonjour,\n\nNous vous informons qu’une demande de suppression TPE du commerçant \"" + nomClient
							+ "\" domicilié sous le N° de compte \"" + numCompte
							+ "\" a été validée.\n\nCordialement.");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}
		posTerminalRepository.save(posTerminal);

		return ResponseEntity.ok().body(gson.toJson("PosTerminal DELETED"));
	}

	@PutMapping("remplacementPosTerminal/{id}")
	public ResponseEntity<String> remplacementPosTerminal(@PathVariable(value = "id") Integer idPos,
			@RequestBody RequestDeletePos requestDeletePos) {
		// @RequestBody RequestDeletePos requestDeletePos
		PosTerminal posTerminal = posTerminalRepository.getOne(idPos);
		// posTerminal.setDetailUpdate(requestDeletePos.getDetailSupp());
		posTerminal.setStatuSerial(requestDeletePos.getStatuSerial());
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.out.println(timestamp);
		String Strdate = sdf3.format(timestamp);

		posTerminal.setStatusUpdtae("");

		PosSerialNumStates pserial = posSerialNumStatesRepository.getOne(posTerminal.getSerialNum());
		pserial.setStatuResiliation(requestDeletePos.getStatuSerial());
		PosModel posModel = posModelRepository.getOne(pserial.getModel());
		PosStock ps = posStockRepository.getPosStockByMode(posModel.getModelCode());
		PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();

		ph.setSerialNum(pserial.getSerialNum());
		ph.setDateSaisie(Strdate);
		ph.setStatus(5);

		if (pserial.getStatus() == 2) {
			ps.setStockHS(ps.getStockHS() + 1);
			ps.setStockReserve(ps.getStockReserve() - 1);
			posStockRepository.save(ps);

		} else if (pserial.getStatus() == 3) {
			ps.setStockHS(ps.getStockHS() + 1);
			ps.setStockConsome(ps.getStockConsome() - 1);
			posStockRepository.save(ps);

		} else if (pserial.getStatus() == 4) {
			ps.setStockHS(ps.getStockHS() + 1);
			ps.setStockDeployer(ps.getStockDeployer() - 1);

			posStockRepository.save(ps);

		}
		pserial.setStatus(5);
		pserial.setStatuRemplacement(1);
		AgenceAdministration ag = agenceAdministrationRepository.findByInitial(posTerminal.getAgence()).get();
		String nomClient = posTerminal.getMerchantCode().getRso();

		Account a = accountRepository.findById(posTerminal.getMerchantCode().getAccount()).get();
		String numCompte = "0 " + a.getAccountNum();

		try {

			myEmailService.sendOtpMessage(ag.getEmail(), "Remplacement POS",
					"Bonjour,\n\nNous vous informons qu’une demande de remplacement POS du commerçant \"" + nomClient
							+ "\" domicilié sous le N° de compte \"" + numCompte
							+ "\"  a été validée.\n\nCordialement.");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}

		try {

			myEmailService.sendOtpMessage("Monetique@fransabank.dz", "Remplacement POS",
					"Bonjour,\n\nNous vous informons qu’une demande de remplacement POS du commerçant \"" + nomClient
							+ "\" domicilié sous le N° de compte \"" + numCompte
							+ "\"  a été validée.\n\nCordialement.");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}
		try {

			myEmailService.sendOtpMessage(posTerminal.getEmailCharge(), "Remplacement POS",
					"Bonjour,\n\nNous vous informons qu’une demande de remplacement POS du commerçant \"" + nomClient
							+ "\" domicilié sous le N° de compte \"" + numCompte
							+ "\"  a été validée.\n\nCordialement.");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}

		posHistoriqueOfSerialRepository.save(ph);

		posSerialNumStatesRepository.save(pserial);

		posTerminalRepository.save(posTerminal);

		return ResponseEntity.ok().body(gson.toJson("PosTerminal update"));
	}

	@GetMapping("getMerchantByAccount/{rib}")
	public ResponseEntity<MerchantByAccNum> getMerchantByAccount(@PathVariable(value = "rib") String rib) {
		MerchantByAccNum merchant = new MerchantByAccNum();
		// Account account = accountRepository.findByAccountNum(accountNum);
		String code = rib.substring(0, 3);
		String initial = rib.substring(3, 8);
		String accountNum = rib.substring(8, rib.length());
		System.out.println("code " + code);
		System.out.println("code " + initial);
		System.out.println("code " + accountNum);
		String ribAcc = rib.substring(1, rib.length());

		// Account account = accountRepository.findByAccountNum(accountNum);
		Account account = accountRepository.findAccountByAgence(ribAcc, initial);
		List<PosTerminalDispalay> PosTerminalDisplay = new ArrayList<>();

		if (account != null) {
			Merchant findedMerchant = merchantRepository.findByNumAccount(account.getAccountCode());
			if (findedMerchant != null) {
				AgenceAdministration agence = agenceAdministrationRepository.findoneBycode(account.getIdAgence());
				List<PosTerminal> posTerminal = posTerminalRepository
						.findAllByMerchantCode(findedMerchant.getMerchantCode());
				merchant.setAddress(findedMerchant.getAddress());
				merchant.setCity(findedMerchant.getCity());
				merchant.setCountry(findedMerchant.getCountry());
				merchant.setInternationalCommission(findedMerchant.getCommissionInternational());
				merchant.setNationalCommission(findedMerchant.getCommissionNational());
				merchant.setPhone(findedMerchant.getPhone());
				merchant.setUsername(findedMerchant.getMerchantLibelle());
				merchant.setZipCode(findedMerchant.getCodeZip());
				merchant.setOffshore(findedMerchant.getOffshore());
				merchant.setAgence(agence.getInitial());
				merchant.setStatus(Integer.parseInt(findedMerchant.getMerchantStatus()));
				merchant.setMerchantCode(findedMerchant.getMerchantCode());
				for (PosTerminal item : posTerminal) {
					PosTerminalDispalay pt = new PosTerminalDispalay();
					pt.setAdresse(item.getAdresse());
					pt.setCity(item.getCity());
					pt.setCountry(item.getCountry());
					pt.setCreationDate(item.getCreationDate());
					MccList mccList = mccRepository.findByMccCode(item.getMccCode().getMccCode());
					pt.setMccCode(mccList.getMccListLibelle());
					pt.setMerchantCode(findedMerchant.getMerchantId());
					pt.setPhone(item.getPhone());
					pt.setPosAllowedTrans(item.getPosAllowedTransConf().getPatcLibelle());
					pt.setPosBins(item.getPbcBinConf().getPbcLibelle());
					pt.setPosCode(item.getPosCode());
					pt.setPosLibelle(item.getPosLibelle());
					pt.setPosLimits(item.getPosLimitsConf().getPlcLibelle());
					pt.setPosLocation(item.getPosLocation());
					pt.setPosNum(item.getPosNum());
					pt.setPosServices(item.getPosServiceConf().getPscLibelle());
					pt.setReferralTel(item.getReferralTel());
					pt.setSerialNum(item.getSerialNum());
					pt.setServiceRepInfo(item.getServiceRepInfo());
					pt.setState(item.getState());
					pt.setStatus(item.getStatus());
					if (!item.getSerialNum().equals("")) {
						PosSerialNumStates pSerial = posSerialNumStatesRepository.getOne(item.getSerialNum());
						pt.setStatusSerial(pSerial.getSerialNum());
					} else {
						pt.setStatusSerial("");
					}
					pt.setTerminalOwner(item.getTerminalOwner());

					PosTerminalDisplay.add(pt);
				}
				merchant.setPosTerminals(PosTerminalDisplay);

			}

		}
		return ResponseEntity.ok().body(merchant);

	}

	@PutMapping("ResiliationMerchant/{rib}")
	public ResponseEntity<String> ResiliationMerchant(@PathVariable(value = "rib") String rib) {

		try {

			myEmailService.sendOtpMessage("Projet-Monetique@fransabank.dz", "Resiliation Commerçant",
					"Bonjour,\n\n" + "La demande de résiliation du Commerçant avec le numéro de compte " + rib
							+ " a été validé.\n\n" + "Cordialement,\n");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}

		return ResponseEntity.ok().body(gson.toJson(deletedMerchantService.ResiliationMerchantPos(rib)));

	}

	@PutMapping("DemandeResiliationMerchant/{rib}")
	public ResponseEntity<String> DemandeResiliationMerchant(@PathVariable(value = "rib") String rib,
			@RequestBody String detailResiliation) {
		String initial = rib.substring(3, 8);
		String ribAcc = rib.substring(1, rib.length());

		Account account = accountRepository.findAccountByAgence(ribAcc, initial);
		Merchant findedMerchant = merchantRepository.findByNumAccount(account.getAccountCode());
		findedMerchant.setMerchantStatus("5");
		findedMerchant.setDetailResiliation(detailResiliation);
		merchantRepository.save(findedMerchant);

		AgenceAdministration ag = agenceAdministrationRepository.findByIdAgence(account.getIdAgence()).get();
		Zone z = zoneRepository.findByCodeZone(ag.getCodeZone()).get();
		String nomClient = findedMerchant.getRso();

		Account a = accountRepository.findById(findedMerchant.getAccount()).get();
		String numCompte = "0 " + a.getAccountNum();

		try {

			myEmailService.sendOtpMessage(ag.getEmail(), "Résiliation contrat commercant",
					"Bonjour,\n\nNous vous informons qu’une demande de résiliation de contrat du commerçant \""
							+ nomClient + "\" domicilié sous le N° de compte \"" + numCompte
							+ "\" est en attente de validation.\n\nCordialement");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}

		try {

			myEmailService.sendOtpMessage(z.getEmail(), "Résiliation contrat commercant",
					"Bonjour,\n\nNous vous informons qu’une demande de résiliation de contrat du commerçant \""
							+ nomClient + "\" domicilié sous le N° de compte \"" + numCompte
							+ "\" est en attente de validation.\n\nCordialement");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}
		return ResponseEntity.ok().body(gson.toJson("Demande de résiliation du Commerçant a été validé"));

	}

	@GetMapping(value = "writeInTSFile")
	@ResponseBody
	ResponseEntity<FileTerminauxRequest> writeInTSFile() throws ResourceNotFoundException {
		try {
			FileTerminauxRequest fileRequest = new FileTerminauxRequest();

			Date date = new Date();

			DateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd");

			String dateCreation = formatter1.format(date);
			String s;
			DateFormat formatter;
			formatter = new SimpleDateFormat("yyMMdd");
			s = formatter.format(date);
			System.out.println(s);
			int numOrdre = 0;

			System.out.println(dateCreation);

			List<PosTerminal> posTerminal = posTerminalRepository.getAllposTofileTS();

			if (posTerminal.size() != 0) {

				BatchPos batch = batchPosRepository.findByKeys("generateTS").get();

				batch.setBatchStatus(0);
				batch.setBatchDate(new Date());
				batch.setError(null);
				batch.setErrorStackTrace(null);
				batch.setFileName("TS");
				batch.setBatchLibelle("Generation file TS");
				batchPosRepository.save(batch);
				for (PosTerminal po : posTerminal) {

					StringBuilder str = new StringBuilder("");
					int nbOr = numOrdre + 1;
					String nOr = String.format("%02d", nbOr);

					String Entete = "000" + "TS" + s + nOr + "00000000" + "000035" + "X";
					String fileName = "TS" + s + nOr;

					int nbEnregistrement = 2;
					int nbS = 0;
					if (posTerminal != null) {
						Merchant m = merchantRepository.getOne(po.getMerchantCode().getMerchantCode());
						str.append(Entete);
						str.append(System.getProperty("line.separator"));
						nbEnregistrement = nbEnregistrement + 1;
						nbS = nbS + 1;
						DateFormat formatter2 = new SimpleDateFormat("yy");
						String ans = formatter2.format(po.getCreationDate());
						String nbSequentielle = String.format("%07d", nbS);
						String codeM = "A";
						Account account = accountRepository.getOne(m.getAccount());
						AgenceAdministration agence = agenceAdministrationRepository
								.findByIdAgence(account.getIdAgence()).get();
						List<TpeRequest> Request = tpeRequestRepository.findAllByAccountNumber(account.getAccountNum());

						String NumContrat = Request.get(0).getNumContrat();
						String idT = String.format("%015d", po.getPosCode());
						String libelleT = po.getPosLibelle();

						String faxT = po.getPhone();
						String phoneT = po.getPhone();
						String emailT = "*";

						String adresseTerminal = org.apache.commons.lang3.StringUtils.rightPad(po.getAdresse(), 40,
								' ');

						String codeCommune = "*";
						String nomCommune = org.apache.commons.lang3.StringUtils.rightPad(Request.get(0).getCommune(),
								20, ' ');
						String nomDaira = org.apache.commons.lang3.StringUtils.rightPad(Request.get(0).getDaira(), 20,
								' ');
						String codeWilaya = Request.get(0).getCodeZip();
						String nomWilaya = org.apache.commons.lang3.StringUtils.rightPad(Request.get(0).getCity(), 15,
								' ');
						String codePostal = org.apache.commons.lang3.StringUtils.rightPad(m.getCodeZip(), 5, ' ');

						String telT = org.apache.commons.lang3.StringUtils.rightPad(po.getPhone(), 15, ' ');

						PosAllowedTransConf PAT = posAllowedTransactionConfRepository
								.getOne(po.getPosAllowedTransConf().getPatcCode());

						PosAllowedTrans PATC = posAllowedTransactionRepository
								.getOne(PAT.getPOS_ALLOWED_TRANS().getId());

						String TRX_Debit = String.valueOf(PATC.getNormalPurchase());
						String TRX_Remb = String.valueOf(PATC.getCashBack());
						String TRX_Annul = "*";
						String TRX_Solde = String.valueOf(PATC.getBalanceInquery());
						String TRX_P_Autor = String.valueOf(PATC.getPreauthPurshase());
						String TRX_Tel = String.valueOf(PATC.getMailTelOrder());
						String TRX_Retrait = "0";
						String TRX_Cash_Advance = String.valueOf(PATC.getCashAdvance());
						String TRX_Paiement = String.valueOf(PATC.getCheckGuarantee());

						String H_deb = "****";
						String H_fin = "****";
						String MAJtypeCarte = "A";

						PosServiceConf PSC = posServicesConfRepository.getOne(po.getPosServiceConf().getPscCode());

						List<PosService> listPS = new ArrayList<>(PSC.getPosServiceLIST());
						String TypeCarteC = "";
						String Limit = "";
						if (listPS.size() != 0) {
							for (PosService itemps : listPS) {
								if (itemps.getCardType().equals("V")) {
									TypeCarteC = "C2";
									Limit = itemps.getTransactionLimit();
								}
								if (itemps.getCardType().equals("C0")) {
									TypeCarteC = "C0";
									Limit = itemps.getTransactionLimit();
								}
								if (itemps.getCardType().equals("C1")) {
									TypeCarteC = "C1";
									Limit = itemps.getTransactionLimit();
								}
								if (itemps.getCardType().equals("C3")) {
									TypeCarteC = "C3";
									Limit = itemps.getTransactionLimit();
								}
								if (itemps.getCardType().equals("C4")) {
									TypeCarteC = "C4";
									Limit = itemps.getTransactionLimit();
								}
								if (itemps.getCardType().equals("C5")) {
									TypeCarteC = "C5";
									Limit = itemps.getTransactionLimit();
								}
								if (itemps.getCardType().equals("C6")) {
									TypeCarteC = "C6";
									Limit = itemps.getTransactionLimit();
								}

							}

						}
						String TypeTerminal;
						if (po.getTypeTerminal() == null) {
							TypeTerminal = "**";
						} else {
							TypeTerminal = po.getTypeTerminal();
						}

						String FinEnreg = "X";

						String dataT = "001" + nbSequentielle + codeM + NumContrat + idT + libelleT + adresseTerminal
								+ codeCommune + nomCommune + nomDaira + codeWilaya + nomWilaya + codePostal + telT
								+ faxT + phoneT + emailT + TRX_Debit + TRX_Remb + TRX_Annul + TRX_Solde + TRX_P_Autor
								+ TRX_Tel + TRX_Retrait + TRX_Cash_Advance + TRX_Paiement + H_deb + H_fin + MAJtypeCarte
								+ TypeCarteC + Limit + TypeTerminal + FinEnreg;

						str.append(dataT);
						str.append(System.getProperty("line.separator"));

						String nbEn = String.format("%07d", nbEnregistrement);

						String Fin = "999" + nbEn + "X";
						str.append(Fin);

						fileRequest.setData(str.toString());
						fileRequest.setNameTitle(fileName);

					}

				}
				batchPosRepository.updateFinishBatch("generateTS", 1, new Date());

				return ResponseEntity.ok().body(fileRequest);
			}
		} catch (Exception e) {
			logger.info("Exception is=>{}", e.toString());
			e.printStackTrace();
			String stackTrace = Throwables.getStackTraceAsString(e);
			if (stackTrace.length() > 4000)
				stackTrace = stackTrace.substring(0, 3999);

			batchPosRepository.updateStatusAndErrorBatch("generateTS", 2,
					e.getMessage() == null ? e.toString() : e.getMessage(), new Date(), stackTrace);
			return null;
		}
		return null;

	}

	@GetMapping(value = "writeBMFile")
	@ResponseBody
	ResponseEntity<FileTerminauxRequest> writeBMFile() throws ResourceNotFoundException {
		FileTerminauxRequest fileRequest = new FileTerminauxRequest();
		try {

			// List<Merchant> findedMerchant =
			// merchantRepository.getListMerchantstausBmEgaleC();
			// List<Merchant> findedMerchant =
			// merchantRepository.getListMerchantToGenerateBS();
			List<Merchant> findedMerchant = merchantRepository.getListMerchantToGenerateBSFile();
			List<Merchant> listMerchant = new ArrayList<>();

			if (findedMerchant.size() != 0) {
				for (Merchant m : findedMerchant) {
					Merchant merchant = merchantRepository.findById(m.getMerchantCode()).get();
					merchant.setStatusBs(null);
					listMerchant.add(merchant);
					StringBuilder str = new StringBuilder("");
					BatchPos batch = batchPosRepository.findByKeys("generateBS").get();

					batch.setBatchStatus(0);
					batch.setBatchDate(new Date());
					batch.setError(null);
					batch.setErrorStackTrace(null);
					batch.setFileName("BS");
					batch.setBatchLibelle("Generation file BS");
					batchPosRepository.save(batch);

					Date date = new Date();
					String s;
					String s1;
					DateFormat formatter;
					DateFormat formatter1;
					formatter1 = new SimpleDateFormat("yyyy-MM-dd");
					formatter = new SimpleDateFormat("yyMMdd");
					s = formatter.format(date);
					s1 = formatter1.format(date);
					int nbS = 0;
					List<SequentielSerieBS> sqBs = sequentielSerieBSRepository.findAllSequentielSerieBSByDate(s1);
					int numOrdre = 0;
					if (sqBs.size() == 0) {
						numOrdre = numOrdre + 1;
						SequentielSerieBS bs = new SequentielSerieBS();
						bs.setDateG(s1);
						bs.setSequence(numOrdre);
						sequentielSerieBSRepository.save(bs);
					} else {

						numOrdre = sqBs.get(sqBs.size() - 1).getSequence() + 1;
						SequentielSerieBS bs = new SequentielSerieBS();
						bs.setDateG(s1);
						bs.setSequence(numOrdre);
						sequentielSerieBSRepository.save(bs);
					}

					int nbEnregistrement = 3;
					String nOr = String.format("%02d", numOrdre);

					String Entete = "000" + "BS" + s + nOr + "MONETIQU" + "000035";
					String fileName = "BS" + s + nOr;
					System.out.println(fileName);
					str.append(Entete);
					str.append(System.getProperty("line.separator"));

					Account account = accountRepository.findById(m.getAccount()).get();
					List<TpeRequest> Request = tpeRequestRepository.findAllByAccountNumber(account.getAccountNum());
					List<PosTerminal> posTerminal = posTerminalRepository.findAllByMerchantCode(m.getMerchantCode());

					AgenceAdministration agence = agenceAdministrationRepository.findoneBycode(account.getIdAgence());

					String NumeroContrat = org.apache.commons.lang3.StringUtils.rightPad(m.getIdContrat(), 15, ' ');

					String dataCom = "001" + NumeroContrat;

					str.append(dataCom);
					str.append(System.getProperty("line.separator"));

					String adresse1 = m.getAddress();
					if (adresse1.length() >= 40) {
						adresse1 = adresse1.substring(0, 40);
					}
					String comm = m.getCommune();

					if (comm.length() >= 20) {
						comm = comm.substring(0, 20);
					}

					String daira1 = m.getDaira();

					if (daira1.length() >= 20) {
						daira1 = daira1.substring(0, 20);
					}

					String adresse = org.apache.commons.lang3.StringUtils.rightPad(adresse1, 40, ' ');
					String codeCommune = org.apache.commons.lang3.StringUtils.rightPad("", 2, '0');

					String commune = org.apache.commons.lang3.StringUtils.rightPad(comm, 20, ' ');
					String daira = org.apache.commons.lang3.StringUtils.rightPad(daira1, 20, ' ');
					String codePostal = org.apache.commons.lang3.StringUtils.rightPad(m.getCodeZip(), 5, ' ');
					String codeWilaya = org.apache.commons.lang3.StringUtils.rightPad("0" + m.getCodeWilaya(), 3, ' ');
					String email = org.apache.commons.lang3.StringUtils.rightPad(m.getEmail(), 50, ' ');
					String numTel = org.apache.commons.lang3.StringUtils.rightPad("+213" + m.getPhone(), 15, ' ');
					String Web = m.getSiteWeb();
					if (Web == null) {
						Web = "";
					}
					String adresseWeb = org.apache.commons.lang3.StringUtils.rightPad(Web, 40, ' ');
					String fax = org.apache.commons.lang3.StringUtils.rightPad("", 15, ' ');
					String mobile = org.apache.commons.lang3.StringUtils.rightPad("", 15, ' ');
					String dataCom1 = "002" + adresse + codeCommune + commune + daira + codeWilaya + codePostal + numTel
							+ fax + mobile + email + adresseWeb;

					str.append(dataCom1);
					str.append(System.getProperty("line.separator"));

					String Ncompte = "0" + account.getAccountNum();
					String status = "";

					if (m.getMerchantStatus().equals("4")) {
						status = "001";
					} else {
						status = "000";
					}
					String dataCom2 = "003" + Ncompte + status;

					str.append(dataCom2);
					str.append(System.getProperty("line.separator"));

					String nbEn = String.format("%07d", nbEnregistrement);

					String Fin = "999" + nbEn;
					str.append(Fin);

					fileRequest.setData(str.toString());
					fileRequest.setNameTitle(fileName);
					batchPosRepository.updateFinishBatch("generateBS", 1, new Date());
					merchantRepository.saveAll(listMerchant);
					return ResponseEntity.ok().body(fileRequest);

				}
			}

		} catch (Exception e) {
			logger.info("Exception is=>{}", e.toString());
			e.printStackTrace();
			String stackTrace = Throwables.getStackTraceAsString(e);
			if (stackTrace.length() > 4000)
				stackTrace = stackTrace.substring(0, 3999);

			batchPosRepository.updateStatusAndErrorBatch("generateBS", 2,
					e.getMessage() == null ? e.toString() : e.getMessage(), new Date(), stackTrace);
			return null;
		}
		return null;

	}

	@GetMapping("getAccountPosDetails/{rib}")
	public AccountPosDetails getAccountPosDetails(@PathVariable(value = "rib") String rib) {
		/*********** appel web service ***********/
		try {
			// String ribAcc = rib.substring(1, rib.length());

			// AccountPosDetails aPos1 =

			return apiPos.getAccountPosDetails(rib, getTokenAmplitudeService.getToken().getToken());

//			AccountPosDetails aPos = new AccountPosDetails();
//			aPos.setNom("khayat");
//			aPos.setEmail("Khayat.mohamed@esprit.tn");
//			aPos.setNif("000916100247244");
//			aPos.setRso("RSO0000000051");
//			aPos.setCodeWilaya("16");
//			aPos.setWilaya("ALGER");
//			aPos.setRc("17B1011678");
//			aPos.setAdresse("20 RUE JEAN JAURES");
//
//
//
//
//			aPos.setDaira("BAB EL OUED");
//
//			aPos.setCommune("CASBAH");
//			aPos.setDevise("208");
//			aPos.setTel("0558851489");
//			aPos.setAgence("01601");
//			aPos.setStatutCompte("19");
//			aPos.setStatutClient("");
//			aPos.setTypeIdentite("CNI");
//			aPos.setPays("ALGERIE");
//			aPos.setPrenom("Mohamed");
//			aPos.setNomCompte("CPTES CHEQUES DES PARTICULIERS");
//			aPos.setUsername("med_333");
//			aPos.setIdentite("0000000140");
//			
//
//			AccountPosDetails aPos1 = new AccountPosDetails();
//			aPos1.setWilaya("ALGER");
//			aPos1.setDaira("BAB EL OUED");
//			aPos1.setCommune("CASBAH");
//			aPos1.setEmail("HDKBHNHYY");
//
//			aPos1.setDevise("208");
//			aPos1.setTel("0558851489");
//			aPos1.setAgence("01601");
//
//			aPos1.setStatutCompte("19");
//			aPos1.setStatutClient("");
//			aPos1.setTypeIdentite("CNI");
//			aPos1.setPays("ALGERIE");
//			aPos1.setPrenom("Aymen");
//			aPos1.setNom("Dammak");
//			aPos1.setIdentite("0000000150");
//			aPos1.setUsername("aymen_333");
//
//			aPos1.setAdresse("N° 47, LOT PETITE PROVENCE SIDI YAHIA");
//			aPos1.setCodeWilaya("16");
//			aPos1.setNif("001716101167848");
//			aPos1.setNomCompte("CPTES CHEQUES DES PARTICULIERS");
//			aPos1.setRc("17B1011678");
//			aPos1.setRso("RSO0000005088");
//
//			AccountPosDetails aPos2 = new AccountPosDetails();
//			aPos2.setWilaya("ALGER");
//			aPos2.setDaira("BAB EL OUED");
//			aPos2.setCommune("CASBAH");
//			aPos2.setEmail("***********@fsbk.dz");
//
//			aPos2.setDevise("208");
//			aPos2.setTel("0558851489");
//			aPos2.setAgence("01601");
//
//			aPos2.setStatutCompte("19");
//			aPos2.setStatutClient("");
//			aPos2.setTypeIdentite("CNI");
//			aPos2.setPays("ALGERIE");
//			aPos2.setPrenom("Seif");
//			aPos2.setNom("salah");
//			aPos2.setIdentite("0000000170");
//			aPos2.setUsername("SAIF_333");
//
//			aPos2.setAdresse("NIV 99 CENTRE DES ARTS RIADH EL FETH EL MADANIA");
//			aPos2.setCodeWilaya("16");
//			aPos2.setEmail("MAIL0000005880 ,MAIL0000005880");
//			aPos2.setNif("000416096616623");
//			aPos2.setNomCompte("CPTES CHEQUES DES PARTICULIERS");
//			aPos2.setRc("16/00-0966166B04");
//			aPos2.setRso("RSO0000005880");
//
//			AccountPosDetails aPos3 = new AccountPosDetails();
//			aPos3.setPays("ALGERIE");
//			aPos3.setWilaya("ALGER");
//			aPos3.setDaira("BAB EL OUED");
//			aPos3.setCommune("CASBAH");
//			aPos3.setEmail("***********@fsbk.dz");
//
//			aPos3.setDevise("208");
//			aPos3.setTel("0558851489");
//			aPos3.setAgence("01601");
//
//			aPos3.setStatutCompte("19");
//			aPos3.setStatutClient("");
//			aPos3.setTypeIdentite("CNI");
//			aPos3.setPrenom("Maher");
//			aPos3.setUsername("Maher099");
//
//			aPos3.setNom("Manoubi");
//			aPos3.setIdentite("0000000180");
//
//			aPos3.setAdresse("ROUTE BOUDJEMAA TEMIM GP N°06 SECTION 03");
//			aPos3.setCodeWilaya("16");
//			aPos3.setEmail("MAIL0000004602 ,MAIL0000004602");
//			aPos3.setNif("000916100247204");
//			aPos3.setNomCompte("CPTES CHEQUES DES PARTICULIERS");
//			aPos3.setRc("09B1002472");
//			aPos3.setRso("RSO0000004602");
//			aPos3.setWilaya("ALGER");
//
//			if (rib.equals("03501601220200001144"))
//				return aPos;
//			if (rib.equals("03501601220210003084"))
//				return aPos;
//			if (rib.equals("03501601220110000971"))
//				return aPos2;
//
//			if (rib.equals("03501601220110000874"))
//				return aPos3;

		} catch (Exception e) {
			// TODO Auto-generated catch block

			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info(stackTrace);
			return null;
		}
		//return null;

	}

	@GetMapping("generate-TS")
	public byte[] generateTSExcel() throws IOException {

		try {
			List<PosTerminal> posTerminal = posTerminalRepository.getAllposTofileTS();
			List<PosTerminal> posTerminals = new ArrayList<>();
			int rowNum = 1;
			if (posTerminal.size() != 0) {
				BatchPos batch = batchPosRepository.findByKeys("generateTS").get();

				batch.setBatchStatus(0);
				batch.setBatchDate(new Date());
				batch.setError(null);
				batch.setErrorStackTrace(null);
				batch.setFileName("TS");
				batch.setBatchLibelle("Generation file TS");
				batchPosRepository.save(batch);
				// créer un nouveau classeur Excel
				Workbook workbook = new XSSFWorkbook();

				// créer une feuille de calcul

				Sheet sheet = workbook.createSheet("Feuille1");

				sheet.setColumnWidth(0, 20 * 256);
				sheet.setColumnWidth(1, 10 * 256);
				sheet.setColumnWidth(2, 20 * 256);
				sheet.setColumnWidth(3, 20 * 256);
				sheet.setColumnWidth(4, 20 * 256);
				sheet.setColumnWidth(5, 20 * 256);
				sheet.setColumnWidth(6, 40 * 256);
				sheet.setColumnWidth(7, 20 * 256);
				sheet.setColumnWidth(8, 20 * 256);
				// ajouter des données
				Row headerRow = sheet.createRow(0);
				Cell headerCell1 = headerRow.createCell(0);
				headerCell1.setCellValue("ID terminal");

				Cell headerCell2 = headerRow.createCell(1);
				headerCell2.setCellValue("Action");

				Cell headerCell3 = headerRow.createCell(2);
				headerCell3.setCellValue("Libelle TPE");
				Cell headerCell4 = headerRow.createCell(3);
				headerCell4.setCellValue("Numéro de téléphone");

				Cell headerCell5 = headerRow.createCell(4);
				headerCell5.setCellValue("Wilaya");
				Cell headerCell6 = headerRow.createCell(5);
				headerCell6.setCellValue("Commune");

				Cell headerCell7 = headerRow.createCell(6);
				headerCell7.setCellValue("Rue");

				Cell headerCell8 = headerRow.createCell(7);
				headerCell8.setCellValue("Code MCC");

				Cell headerCell9 = headerRow.createCell(8);
				headerCell9.setCellValue("Version");

				for (PosTerminal po : posTerminal) {
					PosTerminal pt = posTerminalRepository.getOne(po.getPosCode());
					pt.setFileTS("F");
					posTerminals.add(pt);
					Row dataRow1 = sheet.createRow(rowNum++);
					Cell dataCell1 = dataRow1.createCell(0);
					dataCell1.setCellValue(po.getPosNum());
					Cell dataCell2 = dataRow1.createCell(1);
					String code = "2";
					if (pt.getFileTM().equals("D")) {
						code = "2";
					}
					if (pt.getFileTM().equals("C")) {
						code = "1";
					}
					dataCell2.setCellValue(code);

					Cell dataCell3 = dataRow1.createCell(2);
					dataCell3.setCellValue(po.getPosLibelle());
					Cell dataCell4 = dataRow1.createCell(3);
					dataCell4.setCellValue(po.getPhone());

					Cell dataCell5 = dataRow1.createCell(4);
					dataCell5.setCellValue(po.getCity());
					Cell dataCell6 = dataRow1.createCell(5);
					dataCell6.setCellValue(po.getCommune());

					Cell dataCell7 = dataRow1.createCell(6);
					dataCell7.setCellValue(po.getAdresse());

					Cell dataCell8 = dataRow1.createCell(7);
					dataCell8.setCellValue(po.getMccCode().getMccListId());

					Cell dataCell9 = dataRow1.createCell(8);
					dataCell9.setCellValue("");

				}

				// créer un flux de sortie pour écrire le contenu du classeur dans un tableau
				// d'octets
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				try {
					workbook.write(outputStream);
				} catch (IOException e) {
					e.printStackTrace();
				}

				// fermer le classeur
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// retourner le contenu du fichier Excel sous forme de tableau d'octets
				batchPosRepository.updateFinishBatch("generateTS", 1, new Date());
				posTerminalRepository.saveAll(posTerminals);

				return outputStream.toByteArray();

			}
		} catch (Exception e) {
			logger.info("Exception is=>{}", e.toString());
			e.printStackTrace();
			String stackTrace = Throwables.getStackTraceAsString(e);
			if (stackTrace.length() > 4000)
				stackTrace = stackTrace.substring(0, 3999);

			batchPosRepository.updateStatusAndErrorBatch("generateTS", 2,
					e.getMessage() == null ? e.toString() : e.getMessage(), new Date(), stackTrace);
		}
		return null;
	}

	@GetMapping("/generate-name-excel")
	public ResponseEntity<String> generateNameExcel() {
		Date date = new Date();
		String sTm;

		DateFormat formatter8;
		formatter8 = new SimpleDateFormat("yyyy-MM-dd");

		sTm = formatter8.format(date);

		sTm = formatter8.format(date);

		List<SequentielSerieTS> sqTM = sequentielSerieTSRepository.findAllSequentielSerieTSByDate(sTm);
		int numOrdre = 0;
		if (sqTM.size() == 0) {

			numOrdre = numOrdre + 1;
			SequentielSerieTS bs = new SequentielSerieTS();
			bs.setDateG(sTm);
			bs.setSequence(numOrdre);
			System.out.println("if " + numOrdre);
			sequentielSerieTSRepository.save(bs);
		} else {

			numOrdre = sqTM.get(sqTM.size() - 1).getSequence() + 1;
			SequentielSerieTS bs = new SequentielSerieTS();
			bs.setDateG(sTm);
			bs.setSequence(numOrdre);
			System.out.println("else " + numOrdre);
			sequentielSerieTSRepository.save(bs);
		}

		String s;
		DateFormat formatter;
		formatter = new SimpleDateFormat("yyMMdd");
		s = formatter.format(date);

		String nOr = String.format("%02d", numOrdre);

		// générer le fichier Excel avec un nom dynamique
		String fileName = "TS" + s + nOr;
		return ResponseEntity.ok().body(gson.toJson(fileName + ".xlsx"));

	}

	@GetMapping("/generate-excel")
	public ResponseEntity<byte[]> generateExcel() throws IOException {

		// générer le fichier Excel avec un nom dynamique
		String fileName = "TM";
		byte[] excelContent = generateTSExcel();

		// créer la réponse HTTP avec le contenu du fichier et le nom de fichier
		// dynamique
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment", fileName + ".xlsx");
		ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(excelContent, headers, HttpStatus.OK);

		return response;
	}

//	@PostMapping("readFile")
//	public ResponseEntity<String> readFile(@RequestParam("file") List<MultipartFile> files) throws IOException {
//	    String username = SecurityContextHolder.getContext().getAuthentication().getName();
//	    Optional<User> user = userRepository.findByUserNameOrUserEmail(username, username);
//	    if (!user.isPresent()) {
//	        throw new RuntimeException("Error saving risk !");
//	    }
//
//	    BatchPos batch = batchPosRepository.findByKeys("readPorteur")
//	            .orElseThrow(() -> new RuntimeException("Batch not found"));
//	    List<PosTerminal> posTerminals = new ArrayList<PosTerminal>();
//	    List<Merchant> merchants = new ArrayList<Merchant>();
//
//	    batch.setBatchStatus(0);
//	    batch.setBatchDate(new Date());
//	    batch.setError(null);
//	    batch.setErrorStackTrace(null);
//	    batchPosRepository.save(batch);
//
//	    try {
//	    	for (MultipartFile file : files) {
//	    	
//	    			String fileName = file.getOriginalFilename();
//	    			logger.info("Processing file: {}", fileName);
//	    			String fileContent = new String(file.getBytes(), StandardCharsets.ISO_8859_1);
//	    			String[] lines = fileContent.split(System.getProperty("line.separator"));
//	    			
//	    			// Removed unnecessary code that was not being used
//	    			
//	    			String firstLine = lines[0].replaceAll("\\s{2,}", "  ").trim();
//	    			String[] parts = firstLine.split("  ");
//	    			List<String> champ = Arrays.asList(parts);
//	    			int terminalIndex = champ.indexOf("ID Terminal") - 1;
//	    			int merchantIndex = terminalIndex - 2;
//	    			System.out.println(parts[terminalIndex]);
//	    			System.out.println("--------------------------");
//	    			int libelleIndex = terminalIndex + 1;
//	    			
//	    			// Changed from using BufferedReader to processing the lines directly from the array
//	    			
//	    			Arrays.stream(lines)
//	    				.skip(1) // skip the first line since it has already been processed
//	    				.map(line -> line.replaceAll("\\s{2,}", "  ").trim().split("  "))
//	    				.forEach(datas -> {
//	    					String terminal = datas[terminalIndex];
//	    					String libelle = datas[libelleIndex];
//	    					String merchant = datas[merchantIndex];
//
//	    					System.out.println(terminal);
//	    					System.out.println(libelle);
//	    					System.out.println(merchant);
//	    					
//	    					PosTerminal postest = posTerminalRepository.findByposNum(terminal);
//	    					if(postest==null) {
//	    						return new ResponseEntity<String>(
//	    								" Pos Terminal with id " + terminal + " existed",
//	    								HttpStatus.BAD_REQUEST);
//	    					}else {
//	    						Merchant m = merchantRepository.findMerchantByIdContrat(merchant);
//		    					m.setMerchantId(merchant);
//		    					merchants.add(m);
//	    						PosTerminal pos = posTerminalRepository.findPosTerminalByLibelle(libelle,merchant);
//		    					pos.setPosNum(terminal);
//		    					posTerminals.add(pos);
//	    					}
//	    					
//	    					
//	    				});
//	    		}
//	    	merchantRepository.saveAll(merchants);
//
//	    	posTerminalRepository.saveAll(posTerminals);
//	        batchPosRepository.updateFinishBatch("readPorteur", 1, new Date());
//			return ResponseEntity.ok().body(gson.toJson("sucess"));
//
//
//	    } catch (Exception e) {
//	    	logger.info("Exception is=>{}", e.toString());
//			e.printStackTrace();
//			String stackTrace = Throwables.getStackTraceAsString(e);
//			if (stackTrace.length() > 4000)
//				stackTrace = stackTrace.substring(0, 3999);
//	        batchPosRepository.updateStatusAndErrorBatch("readPorteur", 2,
//	                e.getMessage() == null ? e.toString() : e.getMessage(), new Date(),
//	                stackTrace);
//	    }
//	    logger.info("Finished processing files");
//
//		return ResponseEntity.ok().body(gson.toJson("sucess Finished processing files"));
//
//	}

	@PostMapping("readFile")
	public ResponseEntity<String> readFile(@RequestParam("file") List<MultipartFile> files) throws IOException {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(username, username);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving risk!");
		}

		BatchPos batch = batchPosRepository.findByKeys("readPorteur")
				.orElseThrow(() -> new RuntimeException("Batch not found"));
		List<PosTerminal> posTerminals = new ArrayList<>();
		List<Merchant> merchants = new ArrayList<>();

		batch.setBatchStatus(0);
		batch.setBatchDate(new Date());
		batch.setError(null);
		batch.setErrorStackTrace(null);
		batchPosRepository.save(batch);

		try {
			for (MultipartFile file : files) {
				String fileName = file.getOriginalFilename();
				logger.info("Processing file: {}", fileName);
				String fileContent = new String(file.getBytes(), StandardCharsets.ISO_8859_1);
				String[] lines = fileContent.split(System.getProperty("line.separator"));

				String firstLine = lines[0].replaceAll("\\s{2,}", "  ").trim();
				String[] parts = firstLine.split("  ");
				List<String> champ = Arrays.asList(parts);
				int terminalIndex = champ.indexOf("ID Terminal");
				int carteAdminIndex = champ.indexOf("Carte Admin");
				int merchantIndex = terminalIndex - 2;
				// int merchantIndex = champ.indexOf("ID Commerçant");
				int libelleIndex = terminalIndex + 1;
				// int libelleIndex = champ.indexOf("N° Caisse");

//	            System.out.println(parts[terminalIndex]);
//	            System.out.println(parts[merchantIndex]);
//	            System.out.println(parts[libelleIndex]);
//	            System.out.println(terminalIndex);
//	            System.out.println(merchantIndex);
//	            System.out.println(libelleIndex);
//	            System.out.println("--------------------------");

				Arrays.stream(lines).skip(1) // skip the first line since it has already been processed
						.map(line -> line.replaceAll("\\s{2,}", "  ").trim().split("  ")).forEach(datas -> {
							String carteAdmin = datas[carteAdminIndex];
							String terminal = datas[terminalIndex];
							String libelle = datas[libelleIndex];
							String merchant = datas[merchantIndex];
							if (!terminal.startsWith("P035") && !terminal.startsWith("Q035")) {
								terminal = datas[terminalIndex - 1];
								libelle = datas[libelleIndex - 1];
								merchant = datas[merchantIndex - 1];
							}

//	                        System.out.println(carteAdmin);
//	                        System.out.println(terminal);
//	                        System.out.println(libelle);
//	                        System.out.println(merchant);

							PosTerminal postest = posTerminalRepository.findByposNum(terminal);
							if (postest != null) {
								throw new RuntimeException("Pos Terminal with id " + terminal + " exist");
							} else {
								Merchant m = merchantRepository.findMerchantByIdContrat(merchant);
								if (m != null) {
									m.setMerchantId(merchant);
									merchants.add(m);
								} else {
									throw new RuntimeException("Merchant with id " + merchant + " does not exist");
								}

								PosTerminal pos = posTerminalRepository.findPosTerminalByLibelle(libelle, merchant);
								if (pos != null) {
									pos.setPosNum(terminal);
									posTerminals.add(pos);
								} else {
									throw new RuntimeException("Pos Terminal with libelle " + libelle + " and merchant "
											+ merchant + " does not exist");
								}
							}
						});
			}
			merchantRepository.saveAll(merchants);
			posTerminalRepository.saveAll(posTerminals);
			batchPosRepository.updateFinishBatch("readPorteur", 1, new Date());
			return ResponseEntity.ok().body(gson.toJson("success"));
		} catch (Exception e) {
			logger.info("Exception is=>{}", e.toString());
			e.printStackTrace();
			String stackTrace = Throwables.getStackTraceAsString(e);
			if (stackTrace.length() > 4000)
				stackTrace = stackTrace.substring(0, 3999);
			batchPosRepository.updateStatusAndErrorBatch("readPorteur", 2,
					e.getMessage() == null ? e.toString() : e.getMessage(), new Date(), stackTrace);
		}
		logger.info("Finished processing files");
		return ResponseEntity.ok().body(gson.toJson("success Finished processing files"));
	}

	@GetMapping("getDetailsResiliationMerchant/{id}")
	public ResponseEntity<String> getDetailsResiliationMerchant(@PathVariable(value = "id") int merchantCode) {

		Merchant m = merchantRepository.findById(merchantCode).get();
		String nomClient = m.getRso();

		Account a = accountRepository.findById(m.getAccount()).get();
		AgenceAdministration ag = agenceAdministrationRepository.findByIdAgence(a.getIdAgence()).get();
		String numCompte = "0 " + a.getAccountNum();
		try {

			myEmailService.sendOtpMessage("Monetique@fransabank.dz", "Résiliation contrat commercant",
					"Bonjour,\n\nNous vous informons qu’une demande de résiliation de contrat du commerçant \""
							+ nomClient + "\" domicilié sous le N° de compte \"" + numCompte
							+ "\" a été validée.\n\nCordialement");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}

		try {

			myEmailService.sendOtpMessage(ag.getEmail(), "Résiliation contrat commercant",
					"Bonjour,\n\nNous vous informons qu’une demande de résiliation de contrat du commerçant \""
							+ nomClient + "\" domicilié sous le N° de compte \"" + numCompte
							+ "\" a été validée.\n\nCordialement");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}
		return ResponseEntity.ok().body(gson.toJson(m.getDetailResiliation()));

	}

	@GetMapping("getDetailsSuppPos/{id}")
	public ResponseEntity<String> getDetailsSuppPos(@PathVariable(value = "id") int posCode) {
		PosTerminal pt = posTerminalRepository.findById(posCode).get();
		return ResponseEntity.ok().body(gson.toJson(pt.getDetailSupp()));

	}

	@GetMapping("getDetailsRemplacementPos/{id}")
	public ResponseEntity<String> getDetailsRemplacementPos(@PathVariable(value = "id") int posCode) {
		PosTerminal pt = posTerminalRepository.findById(posCode).get();
		return ResponseEntity.ok().body(gson.toJson(pt.getDetailUpdate()));

	}

	@PostMapping("getAllPagePosStockToExc")
	public List<PosRequestStock> getAllPagePosStockToExc(@RequestBody PosRequestLibelle filter

	) {
		List<PosRequestStock> list = new ArrayList<>();
		List<Order> orders = new ArrayList<Order>();

		if (filter.getLibelle().equals("")) {
			List<PosStock> list1 = posStockRepository.findAll();

			for (PosStock tr : list1) {
				PosRequestStock posRequestStock = new PosRequestStock();
				posRequestStock.setIdStock(tr.getIdStock());
				posRequestStock.setStockConsome(tr.getStockConsome());
				posRequestStock.setStockDeployer(tr.getStockDeployer());
				posRequestStock.setStockDisponible(tr.getStockDisponible());
				posRequestStock.setStockHS(tr.getStockHS());
				posRequestStock.setStockInitial(tr.getStockInitial());
				posRequestStock.setStockReserve(tr.getStockReserve());
				posRequestStock.setDateSaisie(tr.getDateSaisie());
				PosModel posModel = posModelRepository.getOne(tr.getModel());
				PosMarque posMarque = posMarqueRepository.getOne(posModel.getMarqueCode());
				posRequestStock.setMarque(posMarque.getLibelle());
				posRequestStock.setModel(posModel.getLibelle());
				list.add(posRequestStock);

			}

			return list;
		} else {
			List<PosStock> page1 = posStockRepository.getPagePosStockByLibelle(Integer.parseInt(filter.getLibelle()));
			for (PosStock tr : page1) {
				PosRequestStock posRequestStock = new PosRequestStock();
				posRequestStock.setIdStock(tr.getIdStock());
				posRequestStock.setStockConsome(tr.getStockConsome());
				posRequestStock.setStockDeployer(tr.getStockDeployer());
				posRequestStock.setStockDisponible(tr.getStockDisponible());
				posRequestStock.setStockHS(tr.getStockHS());
				posRequestStock.setStockInitial(tr.getStockInitial());
				posRequestStock.setStockReserve(tr.getStockReserve());
				posRequestStock.setDateSaisie(tr.getDateSaisie());
				PosModel posModel = posModelRepository.getOne(tr.getModel());
				PosMarque posMarque = posMarqueRepository.getOne(posModel.getMarqueCode());
				posRequestStock.setMarque(posMarque.getLibelle());
				posRequestStock.setModel(posModel.getLibelle());
				list.add(posRequestStock);

			}

			return list;
		}

	}

	@PostMapping("getAllMarquePosF")
	public List<PosMarque> getAllMarquePosF(@RequestBody PosRequestLibelle filter) {

		return posMarqueRepository.getPagePosMarque(filter.getLibelle().trim());

	}

	@PostMapping("getAllPosModelF")
	public List<PosRequestModel> getAllPosModelF(@RequestBody PosRequestLibelle filter) {

		List<PosRequestModel> list = new ArrayList<>();

		List<PosModel> page1 = posModelRepository.getPagePosModel(filter.getLibelle().trim());
		List<PosModel> posModel = posModelRepository.findAll();
		for (PosModel tr : page1) {
			PosRequestModel posRequestModel = new PosRequestModel();
			posRequestModel.setCodeModel(tr.getModelCode());
			PosType posType = posTypeRepository.getOne(tr.getCodeType());
			PosMarque posMarque = posMarqueRepository.getOne(tr.getMarqueCode());
			posRequestModel.setCodeType(posType.getLibelle());
			posRequestModel.setMarque(posMarque.getLibelle());
			posRequestModel.setLibelle(tr.getLibelle());
			list.add(posRequestModel);

		}

		return list;

	}

	@PostMapping("getPagePosSerialNumStatesF")
	public List<RequestPosSerialNumStates> getPagePosSerialNumStatesF(@RequestBody PosRequestLibelle filter) {

		List<RequestPosSerialNumStates> list = new ArrayList<>();

		List<Object[]> page1 = null;
		if (filter.getPosNum().trim().equals("") && filter.getMerchantCode().trim().equals("")) {

			System.out.println("if");
			page1 = posSerialNumStatesRepository.getPagePosSerialNumStates2(filter.getLibelle().trim());

		} else {

			System.out.println("else");
			page1 = posSerialNumStatesRepository.getPagePosSerialNumStates1(filter.getLibelle().trim(),
					filter.getPosNum().trim(), filter.getMerchantCode().trim());

		}

		for (Object[] tr : page1) {
			RequestPosSerialNumStates requestPosSerialNumStates = new RequestPosSerialNumStates();
			PosSerialNumStates posSerialNumStates = (PosSerialNumStates) tr[0];
			PosModel posModel = posModelRepository.getOne(posSerialNumStates.getModel());
			PosMarque posMarque = posMarqueRepository.getOne(posModel.getMarqueCode());
			PosEtats posEtats = posEtatsRepository.getOne(posSerialNumStates.getStatus());

			PosTerminal posTerminal = posTerminalRepository.findAllposBySerialNum(posSerialNumStates.getSerialNum());

			PosType posType = posTypeRepository.getOne(posModel.getCodeType());
			if (posTerminal != null) {
				Merchant m = merchantRepository.findById(posTerminal.getMerchantCode().getMerchantCode()).get();
				requestPosSerialNumStates.setLibellePOS(posTerminal.getPosLibelle());
				requestPosSerialNumStates.setPosNum(posTerminal.getPosNum());
				requestPosSerialNumStates.setMerchantCode(m.getIdContrat());
				requestPosSerialNumStates.setAdresse(posTerminal.getAdresse());
			} else {
				requestPosSerialNumStates.setLibellePOS("");
				requestPosSerialNumStates.setPosNum("");
				requestPosSerialNumStates.setMerchantCode("");
				requestPosSerialNumStates.setAdresse("");
			}
			// requestPosSerialNumStates.setTypeCode(posModel.getCodeType());
			requestPosSerialNumStates.setType(posType.getLibelle());
			requestPosSerialNumStates.setNumSim(posSerialNumStates.getNumSim());
			// requestPosSerialNumStates.setStatuRemplacement(posSerialNumStates.getStatuRemplacement());
			requestPosSerialNumStates.setMarque(posMarque.getLibelle());
			// requestPosSerialNumStates.setStatusCode(posSerialNumStates.getStatus());
			requestPosSerialNumStates.setSerialNum(posSerialNumStates.getSerialNum());
			requestPosSerialNumStates.setModel(posModel.getLibelle());
			requestPosSerialNumStates.setStatus(posEtats.getLibelleFr());
			requestPosSerialNumStates.setDateSaisie(posSerialNumStates.getDateSaisie());
			list.add(requestPosSerialNumStates);
		}

		return list;
	}

	@PostMapping("getPagePosSerialNumStatesResiliationF")
	public List<RequestPosSerialNumStates> getPagePosSerialNumStatesResiliationF(
			@RequestBody PosRequestLibelle filter) {

		List<RequestPosSerialNumStates> list = new ArrayList<>();

		List<Object[]> page1 = null;
		if (filter.getPosNum().trim().equals("") && filter.getMerchantCode().trim().equals("")) {

			System.out.println("if");
			page1 = posSerialNumStatesRepository.getPagePosSerialNumStates4(filter.getLibelle().trim());

		} else {

			System.out.println("else");
			page1 = posSerialNumStatesRepository.getPagePosSerialNumStates3(filter.getLibelle().trim(),
					filter.getPosNum().trim(), filter.getMerchantCode().trim());

		}

		for (Object[] tr : page1) {
			RequestPosSerialNumStates requestPosSerialNumStates = new RequestPosSerialNumStates();
			PosSerialNumStates posSerialNumStates = (PosSerialNumStates) tr[0];
			PosModel posModel = posModelRepository.getOne(posSerialNumStates.getModel());
			PosMarque posMarque = posMarqueRepository.getOne(posModel.getMarqueCode());
			PosEtats posEtats = posEtatsRepository.getOne(posSerialNumStates.getStatus());

			PosTerminal posTerminal = posTerminalRepository.findAllposBySerialNum(posSerialNumStates.getSerialNum());
			PosType posType = posTypeRepository.getOne(posModel.getCodeType());

			if (posTerminal != null) {
				Merchant m = merchantRepository.findById(posTerminal.getMerchantCode().getMerchantCode()).get();
				requestPosSerialNumStates.setLibellePOS(posTerminal.getPosLibelle());
				requestPosSerialNumStates.setPosNum(posTerminal.getPosNum());
				requestPosSerialNumStates.setMerchantCode(m.getIdContrat());
				requestPosSerialNumStates.setAdresse(posTerminal.getAdresse());
			} else {
				requestPosSerialNumStates.setLibellePOS("");
				requestPosSerialNumStates.setPosNum("");
				requestPosSerialNumStates.setMerchantCode("");
				requestPosSerialNumStates.setAdresse("");
			}
			// requestPosSerialNumStates.setTypeCode(posModel.getCodeType());
			requestPosSerialNumStates.setType(posType.getLibelle());
			requestPosSerialNumStates.setNumSim(posSerialNumStates.getNumSim());

			// requestPosSerialNumStates.setStatuRemplacement(posSerialNumStates.getStatuRemplacement());
			requestPosSerialNumStates.setMarque(posMarque.getLibelle());
			// requestPosSerialNumStates.setStatusCode(posSerialNumStates.getStatus());
			requestPosSerialNumStates.setSerialNum(posSerialNumStates.getSerialNum());
			requestPosSerialNumStates.setModel(posModel.getLibelle());
			requestPosSerialNumStates.setStatus(posEtats.getLibelleFr());
			requestPosSerialNumStates.setDateSaisie(posSerialNumStates.getDateSaisie());
			list.add(requestPosSerialNumStates);
		}

		return list;
	}

	@PostMapping("tpeRequestsF")
	public List<tpeRequestGenFichier> tpeRequestsF(@RequestBody RequestTpeFilter requestTpeFilter) {
		List<tpeRequestGenFichier> tpeRequestDisplays = new ArrayList<>();

		try {
			List<TpeRequest> tpeRequests = tpeRequestRepository.findAll();
			Calendar cal = Calendar.getInstance();
			cal.setTime(requestTpeFilter.getDateFin());
			cal.add(Calendar.DATE, 1);
			tpeRequests = tpeRequests.stream()

					.filter(e -> Strings.isNullOrEmpty(requestTpeFilter.getAgence())
							|| String.valueOf(e.getAgence()).equals(requestTpeFilter.getAgence()))
					.filter(e -> Strings.isNullOrEmpty(requestTpeFilter.getNumCompte())
							|| e.getAccountNumber().equals(requestTpeFilter.getNumCompte()))
					.filter(e -> Strings.isNullOrEmpty(requestTpeFilter.getStatus())
							|| String.valueOf(e.getStatus()).equals(requestTpeFilter.getStatus()))
					.filter(e -> e.getDateCreation().after(requestTpeFilter.getDateDebut())
							&& e.getDateCreation().before(cal.getTime()))
					.collect(Collectors.toList());

			for (TpeRequest tr : tpeRequests) {
				tpeRequestGenFichier tpeRequestDisplay = new tpeRequestGenFichier();

				AgenceAdministration agence = agenceAdministrationRepository.findByIdAgence(tr.getAgence()).get();
				tpeRequestDisplay.setUserName(tr.getUserName());
				tpeRequestDisplay.setAccountNumber(tr.getAccountNumber());
				tpeRequestDisplay.setAdresse(tr.getAdresse());
				tpeRequestDisplay.setNombreTPE(tr.getNombreTPE());
				tpeRequestDisplay.setDateCreation(tr.getDateCreation());
				TpeRequestStatus tpeRequestStat = tpeRequestStatusRepository.findByStatusCode(tr.getStatus());
				tpeRequestDisplay.setStatus(tpeRequestStat.getLibelleFr());

				tpeRequestDisplays.add(tpeRequestDisplay);
			}
			logger.info(tpeRequestDisplays.toString());

		} catch (Exception e) {

			e.printStackTrace();
		}

		return tpeRequestDisplays;
	}

	@PostMapping("getListTpeRequestF")
	public List<tpeRequestGenFichier> getListTpeRequestF(@RequestBody RequestTpeFilter requestTpeFilter) {

		List<TpeRequest> tpeRequest = tpeRequestRepository.findAllTpeRequestLevel3();
		List<TpeRequest> result = new ArrayList<>();

		List<tpeRequestGenFichier> tpeRequestDisplays = new ArrayList<>();

		Calendar cal = Calendar.getInstance();
		cal.setTime(requestTpeFilter.getDateFin());
		cal.add(Calendar.DATE, 1);

		tpeRequest = tpeRequest.stream()

				.filter(e -> Strings.isNullOrEmpty(requestTpeFilter.getAgence())
						|| String.valueOf(e.getAgence()).equals(requestTpeFilter.getAgence()))
				.filter(e -> Strings.isNullOrEmpty(requestTpeFilter.getNumCompte())
						|| e.getAccountNumber().equals(requestTpeFilter.getNumCompte()))
				.filter(e -> Strings.isNullOrEmpty(requestTpeFilter.getStatus())
						|| String.valueOf(e.getStatus()).equals(requestTpeFilter.getStatus()))
				.filter(e -> e.getDateCreation().after(requestTpeFilter.getDateDebut())
						&& e.getDateCreation().before(cal.getTime()))

				.collect(Collectors.toList());
		for (TpeRequest tr : tpeRequest) {
			tpeRequestGenFichier tpeRequestDisplay = new tpeRequestGenFichier();
			/// Agence agence = agenceRepository.findById(tr.getAgence()).get();
			tpeRequestDisplay.setUserName(tr.getUserName());
			tpeRequestDisplay.setAccountNumber(tr.getAccountNumber());
			tpeRequestDisplay.setAdresse(tr.getAdresse());
			tpeRequestDisplay.setNombreTPE(tr.getNombreTPE());
			tpeRequestDisplay.setDateCreation(tr.getDateCreation());
			TpeRequestStatus tpeRequestStat = tpeRequestStatusRepository.findByStatusCode(tr.getStatus());
			tpeRequestDisplay.setStatus(tpeRequestStat.getLibelleFr());
			tpeRequestDisplays.add(tpeRequestDisplay);
		}
		return tpeRequestDisplays;
	}

	@PostMapping("merchantsResiliationF")
	public List<RequestMerchantPdf> merchantsResiliationF(@RequestBody RequestFilterMerchant requestFilterMerchant

	) {

		List<Merchant> merchants = null;
		if (requestFilterMerchant.getAccountNumber().equals("")) {
			merchants = merchantRepository.getPageMerchantResiliation(requestFilterMerchant.getNameMerchant());

		} else {
			merchants = merchantRepository.getPageMerchantResiliation(requestFilterMerchant.getNameMerchant(),
					Integer.parseInt(requestFilterMerchant.getAccountNumber()));

		}

		List<RequestMerchantPdf> merchantListDisplays = new ArrayList<>();
		for (Merchant m : merchants) {
			Account account = null;
			if (m.getAccount() != 0) {
				// account = accountRepository.findByAccountCode(m.getAccount());
				Optional<Account> getAccount = accountRepository.findByAccountCode(m.getAccount());
				// .get();
				if (getAccount.isPresent()) {
					account = getAccount.get();
				}
			}

			List<PosTerminal> posTerminals = posTerminalRepository.findAllByMerchantCode(m.getMerchantCode());
			RequestMerchantPdf merchantListDisplay = new RequestMerchantPdf();
			MerchantStatus ms = merchantStatusRepository.getOne(Integer.parseInt(m.getMerchantStatus()));
			merchantListDisplay.setNbPOS(String.valueOf(posTerminals.size()));
			merchantListDisplay.setEtatMerchant(ms.getLibelleFr());
			merchantListDisplay.setCreationDate(m.getCreationDate());
			merchantListDisplay.setNameMerchant(m.getMerchantLibelle());
			merchantListDisplay.setAccountNumber(account.getAccountNum());
			merchantListDisplay.setIdContrat(m.getIdContrat());
			merchantListDisplays.add(merchantListDisplay);

		}

		return merchantListDisplays;
	}

	@PostMapping("merchantsF")
	public List<RequestMerchantPdf> getAllMerchantF(@RequestBody RequestFilterMerchant requestFilterMerchant

	) {

		List<Merchant> merchants = null;
		if (requestFilterMerchant.getAccountNumber().equals("")) {
			merchants = merchantRepository.getPageMerchant(requestFilterMerchant.getNameMerchant());

		} else {
			merchants = merchantRepository.getPageMerchant(requestFilterMerchant.getNameMerchant(),
					Integer.parseInt(requestFilterMerchant.getAccountNumber()));

		}

		List<RequestMerchantPdf> merchantListDisplays = new ArrayList<>();
		for (Merchant m : merchants) {
			Account account = null;
			if (m.getAccount() != 0) {
				// account = accountRepository.findByAccountCode(m.getAccount());
				Optional<Account> getAccount = accountRepository.findByAccountCode(m.getAccount());
				// .get();
				if (getAccount.isPresent()) {
					account = getAccount.get();
				}
			}

			List<PosTerminal> posTerminals = posTerminalRepository.findAllByMerchantCode(m.getMerchantCode());
			RequestMerchantPdf merchantListDisplay = new RequestMerchantPdf();
			MerchantStatus ms = merchantStatusRepository.getOne(Integer.parseInt(m.getMerchantStatus()));
			merchantListDisplay.setNbPOS(String.valueOf(posTerminals.size()));
			merchantListDisplay.setEtatMerchant(ms.getLibelleFr());
			merchantListDisplay.setCreationDate(m.getCreationDate());
			merchantListDisplay.setNameMerchant(m.getMerchantLibelle());
			merchantListDisplay.setAccountNumber(account.getAccountNum());
			merchantListDisplay.setIdContrat(m.getIdContrat());
			merchantListDisplays.add(merchantListDisplay);
		}
		return merchantListDisplays;
	}

	@GetMapping("getDetailsRejet/{id}")
	public ResponseEntity<String> getDetailsRejet(@PathVariable(value = "id") int requestCode) {
		TpeRequest pt = tpeRequestRepository.findById(requestCode).get();

		return ResponseEntity.ok().body(gson.toJson(pt.getDescriptionReject()));

	}

	@GetMapping("getDetailsRejetMerchant/{id}")
	public ResponseEntity<String> getDetailsRejetMerchant(@PathVariable(value = "id") int merchantCode) {
		Merchant pt = merchantRepository.findByMerchantCode(merchantCode);

		return ResponseEntity.ok().body(gson.toJson(pt.getDetailResiliation()));

	}

	@GetMapping("getRequestDetails/{id}")
	public ResponseEntity<TpeRequestDisplayDetails> getRequestDetails(@PathVariable(value = "id") int id) {

		TpeRequest tr = tpeRequestRepository.findByRequestCode(id);

		Set<PendingTpe> pedndingTpes = tr.getPendingTpes();
		Set<TpePendingDisplay> tpePendingDisplays = new HashSet<>();
		List<CommissionTpe> commissionTpesList = new ArrayList<>();
		List<CommissionTpe> commissionTpesListInter = new ArrayList<>();
		TpeRequestDisplayDetails tpeRequestDisplay = new TpeRequestDisplayDetails();

		String nbAccount = tr.getAccountNumber().substring(7, tr.getAccountNumber().length());

		tpeRequestDisplay.setAccountNumber(nbAccount);
		AgenceAdministration agence = agenceAdministrationRepository.findByIdAgence(tr.getAgence()).get();

		tpeRequestDisplay.setAgence(agence.getInitial());
		CommissionType commissionType = commissionTypeRepository.findByCommissionTypeCode(tr.getCommissionTypeCode());
		tpeRequestDisplay.setCommissionTypeCode(String.valueOf(commissionType.getComTypeLibelle()));

		tpeRequestDisplay.setCity(tr.getCity());
		tpeRequestDisplay.setCodeZip(tr.getCodeZip());
		tpeRequestDisplay.setOffshore(tr.getOffshore());

		tpeRequestDisplay.setCountry(tr.getCountry());
		TpeRequestStatus tpeRequestStat = tpeRequestStatusRepository.findByStatusCode(tr.getStatus());
		tpeRequestDisplay.setStatus(tpeRequestStat.getLibelleFr());
		tpeRequestDisplay.setDateCreation(tr.getDateCreation());
		tpeRequestDisplay.setDateDecision(tr.getDateDecision());
		tpeRequestDisplay.setNombreTPE(tr.getNombreTPE());
		tpeRequestDisplay.setCodeZip(tr.getCodeZip());
		tpeRequestDisplay.setPhone(tr.getPhone());
		tpeRequestDisplay.setRequestCode(tr.getRequestCode());
		tpeRequestDisplay.setUserName(tr.getUserName());
		tpeRequestDisplay.setAdresse(tr.getAdresse());
		tpeRequestDisplay.setCommissionNational(tr.getCommissionNational());
		tpeRequestDisplay.setCommissionInterNational(tr.getCommissionInterNational());
		tpeRequestDisplay.setCodeCommission(tr.getCommissionTypeCode());
		tpeRequestDisplay.setSiteWeb(tr.getSiteWeb());

		tpeRequestDisplay.setNom(tr.getNom());
		tpeRequestDisplay.setPrenom(tr.getPrenom());
		tpeRequestDisplay.setNif(tr.getNif());
		tpeRequestDisplay.setRso(tr.getRso());
		tpeRequestDisplay.setRc(tr.getRc());
		tpeRequestDisplay.setEmail(tr.getEmail());
		tpeRequestDisplay.setDaira(tr.getDaira());
		tpeRequestDisplay.setCommmune(tr.getCommune());
		tpeRequestDisplay.setRevenue(tr.getRevenue());
		tpeRequestDisplay.setNomC(tr.getNomC());
		tpeRequestDisplay.setTitleC(tr.getTitleC());

		tpeRequestDisplay.setPendingTpess(pedndingTpes);

		System.out.println("idddd  " + id);

		Account a = null;
		Merchant m = null;

		System.out.println("account number" + tr.getAccountNumber());
		a = accountRepository.findByAccountNum(tr.getAccountNumber());
		if (a == null && m == null) {

			if (tr.getCommissionTypeCode() == 1) {
				CommissionTpe commission = commissionTpeRepository.findByRequestCodeComFixes(id);

				tpeRequestDisplay.setCommissionNational(commission.getValeurCommissionNationaleFix());
				tpeRequestDisplay.setCommissionInterNational(commission.getValeurCommissionInternationaleFixe());
			} else {
				List<CommissionTpe> commissionTpes = commissionTpeRepository.findByRequestCodeNational(id);

				List<CommissionTpe> CommissionTpesInter = commissionTpeRepository.findByRequestCodeInternational(id);

				tpeRequestDisplay.setCommissionTpesInter(CommissionTpesInter);
				tpeRequestDisplay.setCommissionTpes(commissionTpes);

			}
		}

		if (a != null) {

			m = merchantRepository.findByNumAccount(a.getAccountCode());

			if (m == null) {
				if (tr.getCommissionTypeCode() == 1) {
					CommissionTpe commission = commissionTpeRepository.findByRequestCodeComFixes(id);

					tpeRequestDisplay.setCommissionNational(commission.getValeurCommissionNationaleFix());
					tpeRequestDisplay.setCommissionInterNational(commission.getValeurCommissionInternationaleFixe());
				} else {
					List<CommissionTpe> commissionTpes = commissionTpeRepository.findByRequestCodeNational(id);

					List<CommissionTpe> CommissionTpesInter = commissionTpeRepository
							.findByRequestCodeInternational(id);

					tpeRequestDisplay.setCommissionTpesInter(CommissionTpesInter);
					tpeRequestDisplay.setCommissionTpes(commissionTpes);

				}
			}
			if (m != null) {
				System.out.println("7");
				if (tr.getCommissionTypeCode() == 1) {
					CommissionTpe commission = commissionTpeRepository.findByRequestCodeComFixes(id);

					tpeRequestDisplay.setCommissionNational(commission.getValeurCommissionNationaleFix());
					tpeRequestDisplay.setCommissionInterNational(commission.getValeurCommissionInternationaleFixe());
				} else {
					List<CommissionTpe> commissionTpes = commissionTpeRepository.findByRequestCodeNational(id);

					List<CommissionTpe> CommissionTpesInter = commissionTpeRepository
							.findByRequestCodeInternational(id);

					tpeRequestDisplay.setCommissionTpesInter(CommissionTpesInter);
					tpeRequestDisplay.setCommissionTpes(commissionTpes);

				}
			}
			if (m != null && !m.getMerchantStatus().equals("4")) {
				System.out.println("3");

				if (tr.getCommissionTypeCode() == 1) {
					CommissionTpe commission = commissionTpeRepository.findByRequestMerchantCode(m.getMerchantCode());
					if (commission.getValeurCommissionNationaleFix() == null) {
						tpeRequestDisplay.setCommissionNational("");
						tpeRequestDisplay.setCommissionInterNational("");
					} else {
						tpeRequestDisplay.setCommissionNational(commission.getValeurCommissionNationaleFix());
						tpeRequestDisplay
								.setCommissionInterNational(commission.getValeurCommissionInternationaleFixe());
					}
				} else {
					List<CommissionTpe> commissionTpes = commissionTpeRepository
							.findByMerchantCodeNational(m.getMerchantCode());

					List<CommissionTpe> CommissionTpesInter = commissionTpeRepository
							.findByMerchantCodeInternational(m.getMerchantCode());

					tpeRequestDisplay.setCommissionTpesInter(CommissionTpesInter);
					tpeRequestDisplay.setCommissionTpes(commissionTpes);

				}

			}
		}

		System.out.println("BEFORE");

		logger.info(tpeRequestDisplay.toString());

		return ResponseEntity.ok().body(tpeRequestDisplay);
	}

	@GetMapping("getOnePosTerminal/{id}")
	public PosTerminalDispalay getOnePosTerminal(@PathVariable(value = "id") int posCode) {
		PosTerminal pt = posTerminalRepository.findById(posCode).get();
		PosTerminalDispalay pd = new PosTerminalDispalay();
		pd.setAdresse(pt.getAdresse());
		pd.setCity(pt.getCity());
		pd.setCountry(pt.getCountry());
		pd.setCodeZip(pt.getCodeZip());
		pd.setCommune(pt.getCommune());
		pd.setDaira(pt.getDaira());
		pd.setMccCode(Integer.toString(pt.getMccCode().getMccCode()));
		pd.setMcc(pt.getMccCode().getMccListLibelle());
		pd.setPosLibelle(pt.getPosLibelle());
		pd.setMontantLoyer(pt.getMontantLoyer());
		pd.setFamillyPos(pt.getFamillePosCode());

		return pd;

	}

	@PostMapping("editPosTerminal/{id}")
	public ResponseEntity<String> getOnePosTerminal(@PathVariable(value = "id") int posCode,
			@RequestBody PosTerminalDispalay pd) {

		PosTerminal pt = posTerminalRepository.findById(posCode).get();

		MccList mccList = mccRepository.findByMccCode(Integer.parseInt(pd.getMccCode()));
		pt.setAdresse(pd.getAdresse());
		pt.setCity(pd.getCity());
		pt.setCountry(pd.getCountry());
		pt.setCodeZip(pd.getCodeZip());
		pt.setCommune(pd.getCommune());
		pt.setDaira(pd.getDaira());
		pt.setMccCode(mccList);
		pt.setPosLibelle(pd.getPosLibelle());
		pt.setFileTM("C");
		pt.setFileTS("");
		pt.setMontantLoyer(pd.getMontantLoyer());
		pt = posTerminalRepository.save(pt);

		AgenceAdministration agence = agenceAdministrationRepository.findByInitial(pt.getAgence()).get();
		List<String> recipients = userRepository.getByAllUserAgence(agence.getCodeAgence());
		System.out.println("recipients " + recipients);
		Merchant m = merchantRepository.findById(pt.getMerchantCode().getMerchantCode()).get();
		String nomClient = m.getRso();
		Account acc = accountRepository.findById(m.getAccount()).get();
		String numCompte = "0" + acc.getAccountNum();

		try {

			myEmailService.sendOtpMessage(agence.getEmail(), "Modification des informations POS",
					"Bonjour,\n\n" + "Nous vous informons qu’une modification des informations du TPE \"" + nomClient
							+ "\" domicilié sous le N° de compte \"" + numCompte + "\" a été " + "Validée."
							+ "Cordialement,\n");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}

		try {
			for (String recipient : recipients) {
				myEmailService.sendOtpMessage(recipient, "Modification des informations POS",
						"Bonjour,\n\n" + "Nous vous informons qu’une modification des informations du TPE \""
								+ nomClient + "\" domicilié sous le N° de compte \"" + numCompte + "\" a été "
								+ "Validée." + "Cordialement,\n");
			}
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}
		try {

			myEmailService.sendOtpMessage("Projet-Monetique@fransabank.dz", "Modification des informations POS",
					"Bonjour,\n\n" + "Nous vous informons qu’une modification des informations du TPE \"" + nomClient
							+ "\" domicilié sous le N° de compte \"" + numCompte + "\" a été " + "Validée."
							+ "Cordialement,\n");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}
		return ResponseEntity.accepted().body(gson.toJson("update TERMINAL successfully!"));

	}

	@GetMapping("getAllHistoriqueRequestPos/{id}")
	public List<HistoriqueRequestPos> getAllHistoriqueRequestPos(@PathVariable(value = "id") int idHistorique) {
		return historiqueRequestPosRepository.getAllHistoriqueRequestPos(idHistorique);

	}

	@PostMapping("GetPagePosTerminalFiltred")
	public Page<PosTerminalDispalay> GetPagePosTerminalFiltred(
			@RequestBody RequestFilterPosTerminal requestFilterPosTerminal,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {
		List<PosTerminalDispalay> list = new ArrayList<>();
		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);

		orders.add(order1);

		Page<PosTerminal> page1 = posTerminalRepository.findPageposNum(PageRequest.of(page, size, Sort.by(orders)),
				requestFilterPosTerminal.getPosNum().trim(), requestFilterPosTerminal.getAgencyCode().trim());
		System.out.println(page1.getContent().size());
		for (PosTerminal p : page1.getContent()) {
			String label = "";
			String codeEtats = "";
			PosEtats posEtats = null;
			if (p.getSerialNum().equals("")) {
				p.setSerialNum("");
				label = "";
				codeEtats = "";
			} else {
				PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.getOne(p.getSerialNum());
				posEtats = posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
				label = posEtats.getLibelleFr();
				codeEtats = String.valueOf(posEtats.getCodeStatus());

			}
			Merchant merchant = merchantRepository.findByMerchantCode(p.getMerchantCode().getMerchantCode());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());

			PosTerminalDispalay posTerminalDispalay = new PosTerminalDispalay(p.getPosCode(), merchant.getRso(),
					mccList.getMccListLibelle(), p.getPosNum(), p.getPosLibelle(), p.getPosLocation(), p.getCity(),
					p.getState(), p.getCountry(), p.getPhone(), p.getTerminalOwner(), p.getServiceRepInfo(),
					p.getReferralTel(), p.getCreationDate(), p.getAdresse(), p.getPosLimitsConf().getPlcLibelle(),
					p.getPosAllowedTransConf().getPatcLibelle(), p.getPosServiceConf().getPscLibelle(),
					p.getPbcBinConf().getPbcLibelle(), p.getStatus(), p.getSerialNum(), label, codeEtats);
			posTerminalDispalay.setStatusSup(p.getStatusSup());
			posTerminalDispalay.setStatusUpdtae(p.getStatusUpdtae());
			posTerminalDispalay.setDetailUpdate(p.getDetailUpdate());
			posTerminalDispalay.setDetailSupp(p.getDetailSupp());
			posTerminalDispalay.setIdContrat(merchant.getIdContrat());
			if (Long.parseLong(p.getMontantLoyer()) == 0) {
				posTerminalDispalay.setMontantLoyer("0,00 DZ");
			} else {
				BigInteger bigInt = BigInteger.valueOf(Long.parseLong(p.getMontantLoyer()));
				BigDecimal mL = new BigDecimal(bigInt).divide(new BigDecimal(100));

				DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.FRENCH));
				String montantLoyerString = df.format(mL);
				posTerminalDispalay.setMontantLoyer(montantLoyerString + " DZ");
			}

			FamillePos fp = famillePosRepository.findById(p.getFamillePosCode()).get();
			posTerminalDispalay.setTerminalFamilly(fp.getFamillePosLibelle());
			if (p.getType() != 0) {
				PosType ptype = posTypeRepository.findById(p.getType()).get();
				posTerminalDispalay.setTerminalType(ptype.getLibelle());
			} else {
				posTerminalDispalay.setTerminalType(fp.getFamillePosLibelle());
			}
			list.add(posTerminalDispalay);
		}

		Page<PosTerminalDispalay> pages = new PageImpl<PosTerminalDispalay>(list,
				PageRequest.of(page, size, Sort.by(orders)), page1.getTotalElements());
		return pages;

	}

	@PostMapping("GetPagePosTerminalFiltredSupp")
	public Page<PosTerminalDispalay> GetPagePosTerminalFiltredSupp(
			@RequestBody RequestFilterPosTerminal requestFilterPosTerminal,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {
		List<PosTerminalDispalay> list = new ArrayList<>();
		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);

		orders.add(order1);

		Page<PosTerminal> page1 = posTerminalRepository.findPageposNumSupp(PageRequest.of(page, size, Sort.by(orders)),
				requestFilterPosTerminal.getPosNum().trim(), requestFilterPosTerminal.getAgencyCode().trim());
		System.out.println(page1.getContent().size());
		for (PosTerminal p : page1.getContent()) {
			String label = "";
			String codeEtats = "";
			PosEtats posEtats = null;
			if (p.getSerialNum().equals("")) {
				p.setSerialNum("");
				label = "";
				codeEtats = "";
			} else {
				PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.getOne(p.getSerialNum());
				posEtats = posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
				label = posEtats.getLibelleFr();
				codeEtats = String.valueOf(posEtats.getCodeStatus());

			}
			Merchant merchant = merchantRepository.findByMerchantCode(p.getMerchantCode().getMerchantCode());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());

			PosTerminalDispalay posTerminalDispalay = new PosTerminalDispalay(p.getPosCode(), merchant.getRso(),
					mccList.getMccListLibelle(), p.getPosNum(), p.getPosLibelle(), p.getPosLocation(), p.getCity(),
					p.getState(), p.getCountry(), p.getPhone(), p.getTerminalOwner(), p.getServiceRepInfo(),
					p.getReferralTel(), p.getCreationDate(), p.getAdresse(), p.getPosLimitsConf().getPlcLibelle(),
					p.getPosAllowedTransConf().getPatcLibelle(), p.getPosServiceConf().getPscLibelle(),
					p.getPbcBinConf().getPbcLibelle(), p.getStatus(), p.getSerialNum(), label, codeEtats);
			posTerminalDispalay.setStatusSup(p.getStatusSup());
			posTerminalDispalay.setStatusUpdtae(p.getStatusUpdtae());
			posTerminalDispalay.setDetailUpdate(p.getDetailUpdate());
			posTerminalDispalay.setIdContrat(merchant.getIdContrat());
			if (Long.parseLong(p.getMontantLoyer()) == 0) {
				posTerminalDispalay.setMontantLoyer("0,00 DZ");
			} else {
				BigInteger bigInt = BigInteger.valueOf(Long.parseLong(p.getMontantLoyer()));
				BigDecimal mL = new BigDecimal(bigInt).divide(new BigDecimal(100));

				DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.FRENCH));
				String montantLoyerString = df.format(mL);
				posTerminalDispalay.setMontantLoyer(montantLoyerString + " DZ");
			}

			FamillePos fp = famillePosRepository.findById(p.getFamillePosCode()).get();
			posTerminalDispalay.setTerminalFamilly(fp.getFamillePosLibelle());
			if (p.getType() != 0) {
				PosType ptype = posTypeRepository.findById(p.getType()).get();
				posTerminalDispalay.setTerminalType(ptype.getLibelle());
			} else {
				posTerminalDispalay.setTerminalType(fp.getFamillePosLibelle());
			}
			list.add(posTerminalDispalay);
		}

		Page<PosTerminalDispalay> pages = new PageImpl<PosTerminalDispalay>(list,
				PageRequest.of(page, size, Sort.by(orders)), page1.getTotalElements());
		return pages;

	}

	@PostMapping("findPageposNumFilterTosupp")
	public Page<PosTerminalDispalay> findPageposNumFilterTosupp(
			@RequestBody RequestFilterPosTerminal requestFilterPosTerminal,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {
		List<PosTerminalDispalay> list = new ArrayList<>();
		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);

		orders.add(order1);

		Page<PosTerminal> page1 = posTerminalRepository.findPageposNumFilterTosupp(
				PageRequest.of(page, size, Sort.by(orders)), requestFilterPosTerminal.getPosNum().trim(),
				requestFilterPosTerminal.getAgencyCode().trim());

		for (PosTerminal p : page1.getContent()) {
			String label = "";
			String codeEtats = "";
			PosEtats posEtats = null;
			if (p.getSerialNum().equals("")) {
				p.setSerialNum("");
				label = "";
				codeEtats = "";
			} else {
				PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.getOne(p.getSerialNum());
				posEtats = posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
				label = posEtats.getLibelleFr();
				codeEtats = String.valueOf(posEtats.getCodeStatus());

			}
			Merchant merchant = merchantRepository.findByMerchantCode(p.getMerchantCode().getMerchantCode());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());

			PosTerminalDispalay posTerminalDispalay = new PosTerminalDispalay(p.getPosCode(), merchant.getRso(),
					mccList.getMccListLibelle(), p.getPosNum(), p.getPosLibelle(), p.getPosLocation(), p.getCity(),
					p.getState(), p.getCountry(), p.getPhone(), p.getTerminalOwner(), p.getServiceRepInfo(),
					p.getReferralTel(), p.getCreationDate(), p.getAdresse(), p.getPosLimitsConf().getPlcLibelle(),
					p.getPosAllowedTransConf().getPatcLibelle(), p.getPosServiceConf().getPscLibelle(),
					p.getPbcBinConf().getPbcLibelle(), p.getStatus(), p.getSerialNum(), label, codeEtats);
			posTerminalDispalay.setStatusSup(p.getStatusSup());
			posTerminalDispalay.setStatusUpdtae(p.getStatusUpdtae());
			posTerminalDispalay.setDetailUpdate(p.getDetailUpdate());
			posTerminalDispalay.setIdContrat(merchant.getIdContrat());
			if (Long.parseLong(p.getMontantLoyer()) == 0) {
				posTerminalDispalay.setMontantLoyer("0,00 DZ");
			} else {
				BigInteger bigInt = BigInteger.valueOf(Long.parseLong(p.getMontantLoyer()));
				BigDecimal mL = new BigDecimal(bigInt).divide(new BigDecimal(100));

				DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.FRENCH));
				String montantLoyerString = df.format(mL);
				posTerminalDispalay.setMontantLoyer(montantLoyerString + " DZ");
			}

			FamillePos fp = famillePosRepository.findById(p.getFamillePosCode()).get();
			posTerminalDispalay.setTerminalFamilly(fp.getFamillePosLibelle());
			if (p.getType() != 0) {
				PosType ptype = posTypeRepository.findById(p.getType()).get();
				posTerminalDispalay.setTerminalType(ptype.getLibelle());
			} else {
				posTerminalDispalay.setTerminalType(fp.getFamillePosLibelle());
			}
			list.add(posTerminalDispalay);
		}

		Page<PosTerminalDispalay> pages = new PageImpl<PosTerminalDispalay>(list,
				PageRequest.of(page, size, Sort.by(orders)), page1.getTotalElements());
		return pages;

	}

	@PostMapping("findPageFilterTosupp")
	public Page<PosTerminalDispalay> findPageFilterTosupp(
			@RequestBody RequestFilterPosTerminal requestFilterPosTerminal,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {
		List<PosTerminalDispalay> list = new ArrayList<>();
		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);

		orders.add(order1);

		Page<PosTerminal> page1 = posTerminalRepository.findPageFilterTosupp(
				PageRequest.of(page, size, Sort.by(orders)), requestFilterPosTerminal.getPosNum().trim(),
				requestFilterPosTerminal.getAgencyCode().trim());

		for (PosTerminal p : page1.getContent()) {
			String label = "";
			String codeEtats = "";
			PosEtats posEtats = null;
			if (p.getSerialNum().equals("")) {
				p.setSerialNum("");
				label = "";
				codeEtats = "";
			} else {
				PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.getOne(p.getSerialNum());
				posEtats = posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
				label = posEtats.getLibelleFr();
				codeEtats = String.valueOf(posEtats.getCodeStatus());

			}
			Merchant merchant = merchantRepository.findByMerchantCode(p.getMerchantCode().getMerchantCode());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());

			PosTerminalDispalay posTerminalDispalay = new PosTerminalDispalay(p.getPosCode(), merchant.getRso(),
					mccList.getMccListLibelle(), p.getPosNum(), p.getPosLibelle(), p.getPosLocation(), p.getCity(),
					p.getState(), p.getCountry(), p.getPhone(), p.getTerminalOwner(), p.getServiceRepInfo(),
					p.getReferralTel(), p.getCreationDate(), p.getAdresse(), p.getPosLimitsConf().getPlcLibelle(),
					p.getPosAllowedTransConf().getPatcLibelle(), p.getPosServiceConf().getPscLibelle(),
					p.getPbcBinConf().getPbcLibelle(), p.getStatus(), p.getSerialNum(), label, codeEtats);
			posTerminalDispalay.setStatusSup(p.getStatusSup());
			posTerminalDispalay.setStatusUpdtae(p.getStatusUpdtae());
			posTerminalDispalay.setDetailUpdate(p.getDetailUpdate());
			posTerminalDispalay.setIdContrat(merchant.getIdContrat());
			if (Long.parseLong(p.getMontantLoyer()) == 0) {
				posTerminalDispalay.setMontantLoyer("0,00 DZ");
			} else {
				BigInteger bigInt = BigInteger.valueOf(Long.parseLong(p.getMontantLoyer()));
				BigDecimal mL = new BigDecimal(bigInt).divide(new BigDecimal(100));

				DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.FRENCH));
				String montantLoyerString = df.format(mL);
				posTerminalDispalay.setMontantLoyer(montantLoyerString + " DZ");
			}

			FamillePos fp = famillePosRepository.findById(p.getFamillePosCode()).get();
			posTerminalDispalay.setTerminalFamilly(fp.getFamillePosLibelle());
			if (p.getType() != 0) {
				PosType ptype = posTypeRepository.findById(p.getType()).get();
				posTerminalDispalay.setTerminalType(ptype.getLibelle());
			} else {
				posTerminalDispalay.setTerminalType(fp.getFamillePosLibelle());
			}
			list.add(posTerminalDispalay);
		}

		Page<PosTerminalDispalay> pages = new PageImpl<PosTerminalDispalay>(list,
				PageRequest.of(page, size, Sort.by(orders)), page1.getTotalElements());
		return pages;

	}

	@PostMapping("findPageposNumFilterToUpdate")
	public Page<PosTerminalDispalay> findPageposNumFilterToUpdate(
			@RequestBody RequestFilterPosTerminal requestFilterPosTerminal,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {
		List<PosTerminalDispalay> posTerminalDispalays = new ArrayList<>();
		List<PosTerminalDispalay> list = new ArrayList<>();
		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);

		orders.add(order1);

		Page<PosTerminal> page1 = posTerminalRepository.findPageposNumFilterToUpdate(
				PageRequest.of(page, size, Sort.by(orders)), requestFilterPosTerminal.getPosNum().trim(),
				requestFilterPosTerminal.getAgencyCode().trim());

		for (PosTerminal p : page1.getContent()) {
			String label = "";
			String codeEtats = "";
			PosEtats posEtats = null;
			if (p.getSerialNum().equals("")) {
				p.setSerialNum("");
				label = "";
				codeEtats = "";
			} else {
				PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.getOne(p.getSerialNum());
				posEtats = posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
				label = posEtats.getLibelleFr();
				codeEtats = String.valueOf(posEtats.getCodeStatus());

			}
			Merchant merchant = merchantRepository.findByMerchantCode(p.getMerchantCode().getMerchantCode());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());

			PosTerminalDispalay posTerminalDispalay = new PosTerminalDispalay(p.getPosCode(), merchant.getRso(),
					mccList.getMccListLibelle(), p.getPosNum(), p.getPosLibelle(), p.getPosLocation(), p.getCity(),
					p.getState(), p.getCountry(), p.getPhone(), p.getTerminalOwner(), p.getServiceRepInfo(),
					p.getReferralTel(), p.getCreationDate(), p.getAdresse(), p.getPosLimitsConf().getPlcLibelle(),
					p.getPosAllowedTransConf().getPatcLibelle(), p.getPosServiceConf().getPscLibelle(),
					p.getPbcBinConf().getPbcLibelle(), p.getStatus(), p.getSerialNum(), label, codeEtats);
			posTerminalDispalay.setStatusSup(p.getStatusSup());
			posTerminalDispalay.setStatusUpdtae(p.getStatusUpdtae());
			posTerminalDispalay.setDetailUpdate(p.getDetailUpdate());
			posTerminalDispalay.setIdContrat(merchant.getIdContrat());
			if (Long.parseLong(p.getMontantLoyer()) == 0) {
				posTerminalDispalay.setMontantLoyer("0,00 DZ");
			} else {
				BigInteger bigInt = BigInteger.valueOf(Long.parseLong(p.getMontantLoyer()));
				BigDecimal mL = new BigDecimal(bigInt).divide(new BigDecimal(100));

				DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.FRENCH));
				String montantLoyerString = df.format(mL);
				posTerminalDispalay.setMontantLoyer(montantLoyerString + " DZ");
			}

			FamillePos fp = famillePosRepository.findById(p.getFamillePosCode()).get();
			posTerminalDispalay.setTerminalFamilly(fp.getFamillePosLibelle());
			if (p.getType() != 0) {
				PosType ptype = posTypeRepository.findById(p.getType()).get();
				posTerminalDispalay.setTerminalType(ptype.getLibelle());
			} else {
				posTerminalDispalay.setTerminalType(fp.getFamillePosLibelle());
			}
			list.add(posTerminalDispalay);
		}

		Page<PosTerminalDispalay> pages = new PageImpl<PosTerminalDispalay>(list,
				PageRequest.of(page, size, Sort.by(orders)), page1.getTotalElements());
		return pages;

	}

	@PostMapping("findPageFilterToRemplace")
	public Page<PosTerminalDispalay> findPageFilterToRemplace(
			@RequestBody RequestFilterPosTerminal requestFilterPosTerminal,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {
		List<PosTerminalDispalay> posTerminalDispalays = new ArrayList<>();
		List<PosTerminalDispalay> list = new ArrayList<>();
		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);

		orders.add(order1);

		Page<PosTerminal> page1 = posTerminalRepository.findPageFilterToRemplace(
				PageRequest.of(page, size, Sort.by(orders)), requestFilterPosTerminal.getPosNum().trim(),
				requestFilterPosTerminal.getAgencyCode().trim());

		for (PosTerminal p : page1.getContent()) {
			String label = "";
			String codeEtats = "";
			PosEtats posEtats = null;
			if (p.getSerialNum().equals("")) {
				p.setSerialNum("");
				label = "";
				codeEtats = "";
			} else {
				PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.getOne(p.getSerialNum());
				posEtats = posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
				label = posEtats.getLibelleFr();
				codeEtats = String.valueOf(posEtats.getCodeStatus());

			}
			Merchant merchant = merchantRepository.findByMerchantCode(p.getMerchantCode().getMerchantCode());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());

			PosTerminalDispalay posTerminalDispalay = new PosTerminalDispalay(p.getPosCode(), merchant.getRso(),
					mccList.getMccListLibelle(), p.getPosNum(), p.getPosLibelle(), p.getPosLocation(), p.getCity(),
					p.getState(), p.getCountry(), p.getPhone(), p.getTerminalOwner(), p.getServiceRepInfo(),
					p.getReferralTel(), p.getCreationDate(), p.getAdresse(), p.getPosLimitsConf().getPlcLibelle(),
					p.getPosAllowedTransConf().getPatcLibelle(), p.getPosServiceConf().getPscLibelle(),
					p.getPbcBinConf().getPbcLibelle(), p.getStatus(), p.getSerialNum(), label, codeEtats);
			posTerminalDispalay.setStatusSup(p.getStatusSup());
			posTerminalDispalay.setStatusUpdtae(p.getStatusUpdtae());
			posTerminalDispalay.setDetailUpdate(p.getDetailUpdate());
			posTerminalDispalay.setIdContrat(merchant.getIdContrat());
			if (Long.parseLong(p.getMontantLoyer()) == 0) {
				posTerminalDispalay.setMontantLoyer("0,00 DZ");
			} else {
				BigInteger bigInt = BigInteger.valueOf(Long.parseLong(p.getMontantLoyer()));
				BigDecimal mL = new BigDecimal(bigInt).divide(new BigDecimal(100));

				DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.FRENCH));
				String montantLoyerString = df.format(mL);
				posTerminalDispalay.setMontantLoyer(montantLoyerString + " DZ");
			}

			FamillePos fp = famillePosRepository.findById(p.getFamillePosCode()).get();
			posTerminalDispalay.setTerminalFamilly(fp.getFamillePosLibelle());
			if (p.getType() != 0) {
				PosType ptype = posTypeRepository.findById(p.getType()).get();
				posTerminalDispalay.setTerminalType(ptype.getLibelle());
			} else {
				posTerminalDispalay.setTerminalType(fp.getFamillePosLibelle());
			}
			list.add(posTerminalDispalay);
		}

		Page<PosTerminalDispalay> pages = new PageImpl<PosTerminalDispalay>(list,
				PageRequest.of(page, size, Sort.by(orders)), page1.getTotalElements());
		return pages;

	}

	@PostMapping("findPageposToDeployed")
	public Page<PosTerminalDispalay> findPageposToDeployed(
			@RequestBody RequestFilterPosTerminal requestFilterPosTerminal,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {
		List<PosTerminalDispalay> posTerminalDispalays = new ArrayList<>();
		List<PosTerminalDispalay> list = new ArrayList<>();
		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);

		orders.add(order1);

		Page<PosTerminal> page1 = posTerminalRepository.findPageposToDeployed(
				PageRequest.of(page, size, Sort.by(orders)), requestFilterPosTerminal.getPosNum().trim(),
				requestFilterPosTerminal.getAgencyCode().trim());

		for (PosTerminal p : page1.getContent()) {
			String label = "";
			String codeEtats = "";
			PosEtats posEtats = null;
			if (p.getSerialNum().equals("")) {
				p.setSerialNum("");
				label = "";
				codeEtats = "";
			} else {
				PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository.getOne(p.getSerialNum());
				posEtats = posEtatsRepository.findById(posSerialNumStates.getStatus()).get();
				label = posEtats.getLibelleFr();
				codeEtats = String.valueOf(posEtats.getCodeStatus());

			}
			Merchant merchant = merchantRepository.findByMerchantCode(p.getMerchantCode().getMerchantCode());
			MccList mccList = mccRepository.findByMccCode(p.getMccCode().getMccCode());

			PosTerminalDispalay posTerminalDispalay = new PosTerminalDispalay(p.getPosCode(), merchant.getRso(),
					mccList.getMccListLibelle(), p.getPosNum(), p.getPosLibelle(), p.getPosLocation(), p.getCity(),
					p.getState(), p.getCountry(), p.getPhone(), p.getTerminalOwner(), p.getServiceRepInfo(),
					p.getReferralTel(), p.getCreationDate(), p.getAdresse(), p.getPosLimitsConf().getPlcLibelle(),
					p.getPosAllowedTransConf().getPatcLibelle(), p.getPosServiceConf().getPscLibelle(),
					p.getPbcBinConf().getPbcLibelle(), p.getStatus(), p.getSerialNum(), label, codeEtats);
			posTerminalDispalay.setStatusSup(p.getStatusSup());
			posTerminalDispalay.setStatusUpdtae(p.getStatusUpdtae());
			posTerminalDispalay.setDetailUpdate(p.getDetailUpdate());
			posTerminalDispalay.setIdContrat(merchant.getIdContrat());
			if (Long.parseLong(p.getMontantLoyer()) == 0) {
				posTerminalDispalay.setMontantLoyer("0,00 DZ");
			} else {
				BigInteger bigInt = BigInteger.valueOf(Long.parseLong(p.getMontantLoyer()));
				BigDecimal mL = new BigDecimal(bigInt).divide(new BigDecimal(100));

				DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.FRENCH));
				String montantLoyerString = df.format(mL);
				posTerminalDispalay.setMontantLoyer(montantLoyerString + " DZ");
			}

			FamillePos fp = famillePosRepository.findById(p.getFamillePosCode()).get();
			posTerminalDispalay.setTerminalFamilly(fp.getFamillePosLibelle());
			if (p.getType() != 0) {
				PosType ptype = posTypeRepository.findById(p.getType()).get();
				posTerminalDispalay.setTerminalType(ptype.getLibelle());
			} else {
				posTerminalDispalay.setTerminalType(fp.getFamillePosLibelle());
			}
			list.add(posTerminalDispalay);
		}

		Page<PosTerminalDispalay> pages = new PageImpl<PosTerminalDispalay>(list,
				PageRequest.of(page, size, Sort.by(orders)), page1.getTotalElements());
		return pages;

	}

	public void validationPOS(List<PosTerminal> posTerminal) {

		logger.info("*********begin validationPOS*********");

		if (posTerminal.size() != 0) {

			Account a = accountRepository.findByAccountCode(posTerminal.get(0).getMerchantCode().getAccount()).get();

			List<PosTerminal> actions = posTerminal;

			List<DayOperationPosFransaBank> dayOperations = new ArrayList<DayOperationPosFransaBank>();

			if (actions.size() > 0) {

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

				SettelementFransaBank settlt001 = settlementRepo.findByIdentificationbh("T001");
				SettelementFransaBank settlt002 = settlementRepo.findByIdentificationbh("T002");
				DayOperationFeesPosSequence seq = dayOperationPosSequenceService.getSequence();
				DayOperationFeesPosSequence seqPieceComptable = dayOperationPosSequenceService
						.getSequencePieceComptable();
				for (PosTerminal p : actions) {
					String agence = p.getAgence();

					long settlementAmountHt = Math.round(Long.parseLong(p.getMontantLoyer()) / tva);
					logger.info("settlementAmountHt is => {}", settlementAmountHt);

					long settlementAmountTva = Long.parseLong(p.getMontantLoyer()) - settlementAmountHt;
					logger.info("settlementAmountTva is => {}", settlementAmountTva);

					long transactionAmount = Integer.parseInt(p.getMontantLoyer());
					logger.info("transactionAmount is => {}", transactionAmount);

					String montantTrans = String.format("%014d", transactionAmount);
					montantTrans = montantTrans.substring(0, 12) + "." + montantTrans.substring(12);

					DayOperationPosFransaBank T001 = new DayOperationPosFransaBank();

					T001.setCodeAgence(agence);
					T001.setCodeBankAcquereur("035");
					T001.setCodeBank("035");
					T001.setCompteDebit("0" + a.getAccountNum());
					T001.setDateTransaction(transactionDate);
					T001.setHeureTransaction(transactionTime);
					T001.setCompteCredit(settlt001.getCreditAccount());
					T001.setNumCartePorteur(p.getPosNum());
					T001.setNumtransaction(String.format("%012d", seq.getSequence()));
					T001.setIdenfication(settlt001.getIdentificationbh());
					T001.setMontantSettlement(String.format("%014d", settlementAmountHt));
					T001.setMontantTransaction(montantTrans);
					T001.setLibelleCommercant("REFABRICATION CARTE");

					T001.setPieceComptable("FC" + datePieceComptableFormat.format(date)
							+ String.format("%03d", seqPieceComptable.getSequence()));
					T001.setRefernceLettrage(datePieceComptableFormat.format(date) + p.getPosNum());
					T001.setFileDate(filedateFormat.format(date));
					T001.setNumAutorisation(T001.getNumtransaction());
					T001.setNumRefTransaction(T001.getNumtransaction());

					dayOperations.add(T001);

					// seq=dayOperationCardSequenceService.incrementSequence(seq);

					DayOperationPosFransaBank T002 = new DayOperationPosFransaBank();

					T002.setCodeAgence(agence);
					T002.setCodeBankAcquereur("035");
					T002.setCodeBank("035");
					T002.setCompteDebit("0" + a.getAccountNum());
					T002.setDateTransaction(transactionDate);
					T002.setHeureTransaction(transactionTime);
					T002.setCompteCredit(settlt002.getCreditAccount());
					T002.setNumCartePorteur(p.getPosNum());
					T002.setNumtransaction(String.format("%012d", seq.getSequence()));
					T002.setIdenfication(settlt002.getIdentificationbh());
					T002.setMontantSettlement(String.format("%014d", settlementAmountTva));
					T002.setMontantTransaction(montantTrans);
					T002.setLibelleCommercant("REFABRICATION CARTE");

					T002.setPieceComptable("FC" + datePieceComptableFormat.format(date)
							+ String.format("%03d", seqPieceComptable.getSequence()));
					T002.setRefernceLettrage(datePieceComptableFormat.format(date) + p.getPosNum());
					T002.setFileDate(filedateFormat.format(date));
					T002.setNumAutorisation(T002.getNumtransaction());
					T002.setNumRefTransaction(T002.getNumtransaction());

					dayOperations.add(T002);

					seq = dayOperationPosSequenceService.incrementSequence(seq);
					seqPieceComptable = dayOperationPosSequenceService
							.incrementSequencePieceComptable(seqPieceComptable);

				}

			}

			operationRepo.saveAll(dayOperations);

		}

	}

	@GetMapping("getAllHistoriqueCommercant/{id}")
	public List<HistoriqueCommercant> getAllHistoriqueCommercant(@PathVariable(value = "id") int merchantCode) {
		System.out.println("merchantCode" + merchantCode);
		return historiqueCommercantRepository.getAllHistoriqueCommercant(merchantCode);

	}

	@GetMapping("verifEligiblePos/{id}")
	public boolean verifEligiblePos(@PathVariable(value = "id") String chapter) {
		boolean test = false;
		Optional<EligiblePos> eligiblePos = eligiblePosRepository.findByChapter(chapter);
		if (eligiblePos.isPresent()) {
			test = true;
		}

		return test;

	}

	@PostMapping("getPageEligiblePos")
	public Page<EligiblePos> getPageEligiblePos(@RequestBody PosRequestLibelle filter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir

	) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);

		orders.add(order1);
		return eligiblePosRepository.getPageEligiblePos(PageRequest.of(page, size, Sort.by(orders)),
				filter.getLibelle().trim());

	}

	@PostMapping("AddEligiblePos")
	public ResponseEntity<String> AddEligiblePos(@RequestBody EligiblePos eligiblePos) {

		eligiblePosRepository.save(eligiblePos);
		return ResponseEntity.ok().body(gson.toJson("EligiblePos créé"));

	}

	@PutMapping("updateEligiblePos/{id}")
	public ResponseEntity<String> updateEligiblePos(@PathVariable(value = "id") Integer eligiblePosCode,
			@RequestBody EligiblePos eligiblePos) {

		EligiblePos eligiblePosExist = eligiblePosRepository.getOne(eligiblePosCode);
		eligiblePosExist.setLabel(eligiblePos.getLabel());
		eligiblePosExist.setType(eligiblePos.getType());
		eligiblePosExist.setChapter(eligiblePos.getChapter());
		eligiblePosRepository.save(eligiblePosExist);
		return ResponseEntity.ok().body(gson.toJson("EligiblePos update"));

	}

	@DeleteMapping("deletEligiblePos/{id}")
	public ResponseEntity<String> deletEligiblePos(@PathVariable(value = "id") Integer eligiblePosCode) {

		EligiblePos eligiblePosExist = eligiblePosRepository.getOne(eligiblePosCode);
		eligiblePosRepository.delete(eligiblePosExist);

		return ResponseEntity.accepted().body(gson.toJson("deleted successfully!"));

	}

	@GetMapping("getAllModelStockToRequest/{id}")
	public List<PosModel> getAllModelStockToRequest(@PathVariable(value = "id") Integer codePending) {
		List<PosModel> listPosModel = new ArrayList<>();
		listPosModel.addAll(posModelRepository.getAllModelStock());
		Optional<PendingTpe> pendingTpe = pendingTpeRepository.findById(codePending);
		if (pendingTpe.isPresent()) {
			if (pendingTpe.get().getModel() != null) {
				Optional<PosModel> posModel = posModelRepository
						.findById(Integer.parseInt(pendingTpe.get().getModel()));
				if (posModel.isPresent()) {
					listPosModel.add(posModel.get());
				}
			}
		}

		return listPosModel;
	}

	@PostMapping("validate")
	@Transactional
	public ResponseEntity<String> addStatusRequest(@Valid @RequestBody ValidateRequestEdit requestPos) {
		logger.info("**********begin validate BO**************");
		TpeRequest tpeRequest = tpeRequestRepository.findByRequestCode(requestPos.getCodeRequest());
		Account account = null;
		Merchant merchant = null;
		int j = 0;
		AgenceAdministration ag = agenceAdministrationRepository.findByIdAgence(tpeRequest.getAgence()).get();
		List<Merchant> listAllMerchant = merchantRepository.findlistofMerchantPos();
		int nbSeq = listAllMerchant.size() + 1;
		DateFormat formatter2 = new SimpleDateFormat("yy");
		String ans = formatter2.format(new Date());
		String nbSequentielle = String.format("%05d", nbSeq);
		String idContrat = "035" + ag.getInitial() + ans + nbSequentielle;
		String merchantId = idContrat;
		Set<PosTerminal> posTerminals = new HashSet<>();
		Set<PendingTpe> oldPendingTpe = requestPos.getPendingTpes();
	    System.out.println("Element : " + oldPendingTpe);

	    System.out.println("Element : " + requestPos);

		String testDone = "";
		List<ValidateTpeRequest> integers = requestPos.getListIdTpe();
		tpeRequest.setDateDecision(new Date());
		for (ValidateTpeRequest item : integers) {
			if (item.isValidate() && !item.isReject()) {
				testDone = "done";
			}
		}
		if (testDone.equals("done")) {
			if (!accountRepository.existsByAccountNum(tpeRequest.getAccountNumber())) {

				logger.info("********* begin account not found**********");
				logger.info("begin add Merchant");
				merchant = addMerchant(tpeRequest, merchantId, requestPos.isOffshore(),
						requestPos.getCommissionTypeCode());
				merchant = merchantRepository.save(merchant);
				logger.info("end add Merchant");

				/******************** historiqueCommercant **********************/
				logger.info("begin add Historique Merchant");

				historiqueCommercantRepository.save(addHistoriqueCommercant(merchant.getMerchantCode()));
				logger.info("end add Historique Merchant");

				/******************** historiqueCommercant **********************/
				String rib = tpeRequest.getAccountNumber();

				CurrencyFSBK currencyFSBK = currencyFSBKRepository.getOne(tpeRequest.getDevise());
				String devise = currencyFSBK.getCodeDevise();
				logger.info("begin add ACCOUNT");

				account = new Account(null, rib, 1, Integer.toString(merchant.getMerchantCode()), rib,
						tpeRequest.getUserName(), BigInteger.valueOf(0), BigInteger.valueOf(0), BigInteger.valueOf(0),
						BigInteger.valueOf(0), devise, new Date(), 5, tpeRequest.getAgence());

				account = accountRepository.save(account);
				logger.info("END add ACCOUNT");

				merchant.setAccount(account.getAccountCode());
				merchant = merchantRepository.save(merchant);

				logger.info("********* end account not found**********");

			} else {
				logger.info("********* begin account founded**********");
				account = accountRepository.findByAccountNum(tpeRequest.getAccountNumber());
				merchant = merchantRepository.findByNumAccount(account.getAccountCode());
				if (merchant == null) {
					logger.info("Merchant not found");
					logger.info("begin add Merchant");
					merchant = addMerchant(tpeRequest, merchantId, requestPos.isOffshore(),
							requestPos.getCommissionTypeCode());
					merchant.setAccount(account.getAccountCode());
					merchant = merchantRepository.save(merchant);
					historiqueCommercantRepository.save(addHistoriqueCommercant(merchant.getMerchantCode()));
					logger.info("end add Merchant");

				}
				logger.info("********* end account founded**********");

			}
			addCommission(tpeRequest.getRequestCode(), merchant.getMerchantCode());
		}

		for (ValidateTpeRequest i : integers) {

			PendingTpe pendingTpe = pendingTpeRepository.findById(i.getCode()).get();
			Optional<PendingTpe> foundTpe = oldPendingTpe.stream()
				    .filter(p -> p.getCode().equals(pendingTpe.getCode()))
				    .findFirst();

				if (foundTpe.isPresent()) {
				    System.out.println("Element found: " + foundTpe.get());
				    pendingTpe.setLibelle(foundTpe.get().getLibelle());
				    pendingTpe.setAdresse(foundTpe.get().getAdresse());
				    pendingTpe.setCity(foundTpe.get().getCity());
				    pendingTpe.setCountry(foundTpe.get().getCountry());
				    pendingTpe.setPhone(foundTpe.get().getPhone());
				    pendingTpe.setZipCode(foundTpe.get().getZipCode());

				}
				else {
			        System.out.println("Element not found in the set.");
			    }
			if (i.isValidate() && !i.isReject()) {

				pendingTpe.setStatus(3);
				pendingTpe.setMotif(null);
				pendingTpe.setCode(pendingTpe.getCode());
				logger.info("********* begin gestion stock**********");

				gestionStock(i, pendingTpe);

				logger.info("********* end gestion stock**********");
				logger.info("********* begin add POS TERMINAL**********");

				PosTerminal posTerminal = new PosTerminal(pendingTpe.getLibelle(), pendingTpe.getCity(),
						pendingTpe.getCity(), pendingTpe.getCity(), pendingTpe.getCountry(), pendingTpe.getPhone(),
						pendingTpe.getLibelle(), pendingTpe.getPhone(), new Date(), pendingTpe.getAdresse());
				posTerminal.setCommune(pendingTpe.getCommune());
				posTerminal.setSiteWeb(tpeRequest.getSiteWeb());
				posTerminal.setDaira(pendingTpe.getDaira());
				posTerminal.setCodeZip(pendingTpe.getZipCode());
				posTerminal.setFileTM("A");
				posTerminal.setMontantLoyer(tpeRequest.getMontantLoyer());
				posTerminal.setFamillePosCode(pendingTpe.getFamillePosCode());
				posTerminal.setType(pendingTpe.getType());
				posTerminal.setAgence(ag.getInitial());
				if (pendingTpe.getSerialNum() != null) {
					posTerminal.setSerialNum(pendingTpe.getSerialNum());
				} else {
					posTerminal.setSerialNum("");
				}
				if (pendingTpe.getFamillePosCode() == 1) {
					posTerminal.setTypeTerminal(null);
				} else if (pendingTpe.getFamillePosCode() == 4) {
					posTerminal.setTypeTerminal(null);
				} else {
					posTerminal.setTypeTerminal(String.valueOf(pendingTpe.getType()));
				}
				posTerminal.setStatus("DESACTIVE");
				posTerminal.setMerchantCode(merchant);
				MccList mccList = mccRepository.findByMccCode(pendingTpe.getMccCode());
				posTerminal.setMccCode(mccList);
				if (pendingTpe.getPreAutorisation()) {
					PosAllowedTransConf allowedTrans = posAllowedTransactionConfRepository.findByLibelle("preauth");
					posTerminal.setPosAllowedTransConf(allowedTrans);
				} else {
					PosAllowedTransConf allowedTrans = posAllowedTransactionConfRepository
							.findByLibelle("withoutPreauth");
					posTerminal.setPosAllowedTransConf(allowedTrans);
				}

				PosLimitsConf posLimits = posLimitsConfRepository.findById(i.getPosLimits()).get();
				posTerminal.setPosLimitsConf(posLimits);

				PosServiceConf posServices = posServicesConfRepository.findById(i.getPosServices()).get();
				posTerminal.setPosServiceConf(posServices);

				PosBinConf posBin = posBinConfRepository.findById(i.getPosBin()).get();
				posTerminal.setPbcBinConf(posBin);
				posTerminals = merchant.getPOS_TERMINAL();
				posTerminal.setPosNum(posNum(posTerminals));
				posTerminal.setPosStatusCode(1);
				posTerminals.add(posTerminal);

				posTerminalRepository.save(posTerminal);
				merchant.setPOS_TERMINAL(posTerminals);
				merchant = merchantRepository.save(merchant);
				logger.info("********* end add POS TERMINAL**********");

			} else {
				if (pendingTpe.getModel() != null) {
					PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository
							.getOne(pendingTpe.getSerialNum());
					PosModel posModel = posModelRepository.getOne(Integer.parseInt(pendingTpe.getModel()));
					System.out.println(posModel.getModelCode());

					PosStock posStock = posStockRepository.getPosStockByMode(posModel.getModelCode());
					System.out.println(posStock.getStockReserve());

					posStock.setStockReserve(posStock.getStockReserve() - 1);
					posStock.setStockDisponible(posStock.getStockDisponible() + 1);

					posSerialNumStates.setStatus(1);

					posHistoriqueOfSerialRepository.save(addHistoriqueOfSerial(posSerialNumStates.getSerialNum(), 1));
					posStockRepository.save(posStock);
					posSerialNumStatesRepository.save(posSerialNumStates);
				}

				j++;
				pendingTpe.setStatus(2);
				pendingTpe.setMotif(i.getMotif());
				pendingTpe.setModel(null);
				pendingTpeRepository.save(pendingTpe);
			}
		}
		if (testDone.equals("done")) {
			requestHistoriqueApprouve(tpeRequest.getRequestCode());
			tpeRequest.setStatus(3);
			tpeRequestRepository.save(tpeRequest);

		} else {
			requestHistoriqueRejete(tpeRequest.getRequestCode());
			tpeRequest.setStatus(2);
			tpeRequestRepository.save(tpeRequest);

		}

		try {
			sendEmailToMonetique(tpeRequest.getUserName(), "0" + tpeRequest.getAccountNumber());
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info(stackTrace);
		}
		try {
			sendEmailToCharge(tpeRequest.getUserName(), "0" + tpeRequest.getAccountNumber(),
					tpeRequest.getEmailCharge());

		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info(stackTrace);
		}
		logger.info("**********end validate BO**************");
		return ResponseEntity.accepted().body(gson.toJson("Successfully!"));
	}

	public Merchant addMerchant(TpeRequest tpeRequest, String merchantId, boolean offshore, String commissionTypeCode) {
		Merchant merchant = new Merchant("1", merchantId, tpeRequest.getUserName(), tpeRequest.getCity(),
				tpeRequest.getCountry(), tpeRequest.getCodeZip(), tpeRequest.getPhone(), new Date(),
				tpeRequest.getAdresse());
		merchant.setIdContrat(merchantId);
		merchant.setOffshore(offshore);
		merchant.setCommissionType(Integer.parseInt(commissionTypeCode));
		merchant.setEmail(tpeRequest.getEmail());
		merchant.setDaira(tpeRequest.getDaira());
		merchant.setCommune(tpeRequest.getCommune());
		merchant.setCodeWilaya(tpeRequest.getCodeWilaya());
		merchant.setNif(tpeRequest.getNif());
		merchant.setRso(tpeRequest.getRso());
		merchant.setRc(tpeRequest.getRc());
		merchant.setRevenue(tpeRequest.getRevenue());
		merchant.setSiteWeb(tpeRequest.getSiteWeb());
		return merchant;
	}

	public HistoriqueCommercant addHistoriqueCommercant(Integer merchantCode) {
		HistoriqueCommercant historiqueCommercant = new HistoriqueCommercant();
		historiqueCommercant.setDateStatu(new Date());
		historiqueCommercant.setStatut("Commercant Créé");
		historiqueCommercant.setMerchantCode(merchantCode);
		return historiqueCommercant;
	}

	public void gestionStock(ValidateTpeRequest i, PendingTpe pendingTpe) {
		List<PosHistoriqueOfSerial> posHistoriqueOfSerialList = new ArrayList<PosHistoriqueOfSerial>();
		List<PosSerialNumStates> psnsList = new ArrayList<PosSerialNumStates>();
		List<PosStock> posStockList = new ArrayList<PosStock>();
		List<PendingTpe> pendingTpeList = new ArrayList<PendingTpe>();

		if (pendingTpe.getModel() != null) {

			if ((pendingTpe.getFamillePosCode() != 1 || pendingTpe.getFamillePosCode() != 4)) {

				if (i.getModel() == Integer.parseInt(pendingTpe.getModel())) {

					PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository
							.getOne(pendingTpe.getSerialNum());

					PosModel posModel = posModelRepository.getOne(Integer.parseInt(pendingTpe.getModel()));

					PosStock posStock = posStockRepository.getPosStockByMode(posModel.getModelCode());

					posStock.setStockReserve(posStock.getStockReserve() - 1);
					posStock.setStockConsome(posStock.getStockConsome() + 1);

					posSerialNumStates.setStatus(3);

					posHistoriqueOfSerialList.add(addHistoriqueOfSerial(posSerialNumStates.getSerialNum(), 3));
					psnsList.add(posSerialNumStates);
					posStockList.add(posStock);
					pendingTpeList.add(pendingTpe);

				}

				else {

					PosModel posModelExist = posModelRepository.getOne(Integer.parseInt(pendingTpe.getModel()));
					PosStock posStockExist = posStockRepository.getPosStockByMode(posModelExist.getModelCode());
					posStockExist.setStockDisponible(posStockExist.getStockDisponible() + 1);
					posStockExist.setStockReserve(posStockExist.getStockReserve() - 1);
					posStockList.add(posStockExist);

					PosModel posModel = posModelRepository.getOne(i.getModel());
					pendingTpe.setModel(Integer.toString(posModel.getModelCode()));
					pendingTpe.setType(posModel.getCodeType());
					PosType posType = posTypeRepository.findById(posModel.getCodeType()).get();
					pendingTpe.setFamillePosCode(posType.getFamillePosCode());
					PosStock posStock = posStockRepository.getPosStockByMode(posModel.getModelCode());
					posStock.setStockDisponible(posStock.getStockDisponible() - 1);
					posStock.setStockConsome(posStock.getStockConsome() + 1);
					posStockList.add(posStock);

					PosSerialNumStates posSerialNumStates = posSerialNumStatesRepository
							.getOne(pendingTpe.getSerialNum());
					posSerialNumStates.setStatus(1);

					posHistoriqueOfSerialList.add(addHistoriqueOfSerial(posSerialNumStates.getSerialNum(), 1));

					List<PosSerialNumStates> p1 = posSerialNumStatesRepository
							.getOnePosSerialNumStatesByStockByTypeModel(i.getModel());

					PosSerialNumStates posSerialNumState = posSerialNumStatesRepository
							.findById(p1.get(0).getSerialNum()).get();
					pendingTpe.setSerialNum(posSerialNumState.getSerialNum());

					posSerialNumState.setStatus(3);

					posHistoriqueOfSerialList.add(addHistoriqueOfSerial(posSerialNumState.getSerialNum(), 3));
					psnsList.add(posSerialNumState);
					psnsList.add(posSerialNumStates);
					pendingTpeList.add(pendingTpe);

				}
				posHistoriqueOfSerialRepository.saveAll(posHistoriqueOfSerialList);
				posSerialNumStatesRepository.saveAll(psnsList);
				posStockRepository.saveAll(posStockList);
				pendingTpeRepository.saveAll(pendingTpeList);
			}
		} else {
			pendingTpeRepository.save(pendingTpe);

		}
	}

	public String bankIdentification() {
		Bank bank = bankRepository.findById(1).get();
		String bankIdentification = bank.getIdentificationNumber();
		if (bankIdentification.length() < 3) {
			int bankIdIndex = bankIdentification.length();
			while (bankIdIndex < 3) {
				bankIdentification = bankIdentification + "0";
				bankIdIndex++;
			}
		}
		return bankIdentification;
	}

	public String seriesString() {
		Long seriesId = merchantRepository.getNextSeriesId();
		String seriesString = Long.toString(seriesId);
		if (seriesString.length() < 5) {
			int index = seriesString.length();
			while (index < 5) {
				seriesString = "0" + seriesString;
				index++;
			}
		}
		return seriesString;
	}

	public String posNum(Set<PosTerminal> posTerminals) {
		int size = posTerminals.size() + 1;
		String sizeString = String.valueOf(size);
		if (sizeString.length() < 2) {
			sizeString = "0" + sizeString;
		}
		return bankIdentification() + seriesString() + sizeString;
	}

	public void requestHistoriqueApprouve(Integer requestCode) {
		HistoriqueRequestPos historiqueRequestPos = new HistoriqueRequestPos();
		historiqueRequestPos.setDateStatu(new Date());
		historiqueRequestPos.setStatut("Demande approuvée");
		historiqueRequestPos.setRequestCode(requestCode);
		historiqueRequestPos.setOperateur("BackOffice");
		historiqueRequestPosRepository.save(historiqueRequestPos);
	}

	public void requestHistoriqueRejete(Integer requestCode) {
		HistoriqueRequestPos historiqueRequestPos = new HistoriqueRequestPos();
		historiqueRequestPos.setDateStatu(new Date());
		historiqueRequestPos.setStatut("Demande rejetée");
		historiqueRequestPos.setRequestCode(requestCode);
		historiqueRequestPos.setOperateur("BackOffice");
		historiqueRequestPosRepository.save(historiqueRequestPos);
	}

	public void addCommission(Integer requestCode, Integer merchantCode) {
		List<CommissionTpe> cTpe = commissionTpeRepository.findByRequestCode(requestCode);
		List<CommissionTpe> listCom = new ArrayList<>();
		for (CommissionTpe item : cTpe) {
			CommissionTpe c = commissionTpeRepository.getOne(item.getIdCommission());
			c.setMerchantCode(merchantCode);
			listCom.add(c);
		}
		commissionTpeRepository.saveAll(listCom);
	}

	public void sendEmailToMonetique(String nomClient, String numCompte) {
		try {

			myEmailService.sendOtpMessage("Monetique@fransabank.dz", "Demande d'acquisition TPE ",
					"Bonjour,\n\n" + "Nous vous informons que la demande d’acquisition TPE du client \"" + nomClient
							+ "\" domicilié sous le N° de compte \"" + numCompte + "\" a été " + "Validée"
							+ " par le chef d’agence.\n\n" + "Cordialement,\n");
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info(stackTrace);
		}
	}

	public void sendEmailToCharge(String nomClient, String numCompte, String emailCharge) {
		try {

			myEmailService.sendOtpMessage(emailCharge, "Demande d'acquisition TPE",
					"Bonjour,\n\n" + "Nous vous informons que la demande d’acquisition TPE du client \"" + nomClient
							+ "\" domicilié sous le N° de compte \"" + numCompte + "\" a été " + "Validée"
							+ " par le chef d’agence.\n\n" + "Cordialement,\n");

		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info(stackTrace);
		}

	}

	public PosHistoriqueOfSerial addHistoriqueOfSerial(String SerialNum, Integer status) {
		PosHistoriqueOfSerial ph = new PosHistoriqueOfSerial();
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String Strdate = sdf3.format(timestamp);
		ph.setSerialNum(SerialNum);
		ph.setDateSaisie(Strdate);
		ph.setStatus(status);
		return ph;
	}

	@PutMapping("RejeteDemandeTodeletePosTerminal/{id}")
	public ResponseEntity<String> RejeteDemandeTodeletePosTerminal(@PathVariable(value = "id") Integer idPos) {
		PosTerminal posTerminal = posTerminalRepository.getOne(idPos);
		posTerminal.setStatusSup("");
		posTerminal.setDetailSupp("");

		posTerminalRepository.save(posTerminal);

		return ResponseEntity.ok().body(gson.toJson("Demande REJETE"));

	}

	@PutMapping("RejeteDemandeToChangeSerial/{id}")
	public ResponseEntity<String> RejeteDemandeToChangeSerial(@PathVariable(value = "id") Integer idPos) {
		PosTerminal posTerminal = posTerminalRepository.getOne(idPos);
		posTerminal.setStatusUpdtae("");
		posTerminal.setDetailUpdate("");
		posTerminalRepository.save(posTerminal);

		PosSerialNumStates ps = posSerialNumStatesRepository.getOne(posTerminal.getSerialNum());
		ps.setStatuResiliation("");
		posSerialNumStatesRepository.save(ps);

		return ResponseEntity.ok().body(gson.toJson("Demande REJETE"));

	}

}
