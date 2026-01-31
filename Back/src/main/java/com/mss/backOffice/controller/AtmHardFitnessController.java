package com.mss.backOffice.controller;

import com.google.gson.Gson;

import com.mss.unified.entities.*;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.unified.repositories.*;
import com.mss.backOffice.request.AtmDisplay;
import com.mss.backOffice.request.AtmHardFitnessDisplay;
import com.mss.backOffice.request.AtmMonitoring;
import com.mss.backOffice.request.AtmSuppliesStatusRequest;
import com.mss.backOffice.request.AtmTerminalDisplay;
import com.mss.backOffice.request.AtmTerminalFilter;
import com.mss.backOffice.request.CassetteCountResult;
import com.mss.backOffice.request.EtatTerminal;
import com.mss.backOffice.request.MerchantListDisplay;
import com.mss.backOffice.request.RequestFilterMerchant;
import com.mss.backOffice.request.SwitchDisplay;
import com.mss.backOffice.request.requestMerchantDisplayed;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import com.mss.unified.entities.*;
import com.mss.unified.repositories.*;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("atmHardFitness")
public class AtmHardFitnessController {
	@Autowired
	AgenceAdministrationRepository agenceAdministrationRepository;
	@Autowired
	BankRepository bankRepository;
	@Autowired
	MerchantStatusRepository merchantStatusRepository;
	@Autowired
	AccountRepository accountRepository;
	@Autowired
	AtmMerchantRepository atmMerchantRepository;
	@Autowired
	AtmHardConfDataRepository atmHardConfDataRepository;
	@Autowired
	AtmHardFitnessRepository atmHardFitnessRepository;
	@Autowired
	AtmCassetteCountRepository atmCassetteCountRepository;
	@Autowired
	AtmTerminalRepository atmTerminalRepository;
	@Autowired
	AtmConfigurationRepository atmConfigurationRepository;
	@Autowired
	MccRepository mccRepository;
	@Autowired
	MerchantRepository merchantRepository;
	@Autowired
	AtmModelRepository atmModelRepository;
	@Autowired
	AtmMarqueRepository atmMarqueRepository;
	@Autowired
	AtmCassetteMapValueRepository atmCassetteMapValueRepository;
	@Autowired
	AtmSensorStatusRepository atmSensorStatusRepository;
	@Autowired
	AtmSuppliesStatusRepository atmSuppliesStatusRepository;

	ModelMapper modelMapper;
	private static final Gson gson = new Gson();
	private static final Logger logger = LoggerFactory.getLogger(BinController.class);

	@GetMapping("/atmHardFitness/{id}")
	public ResponseEntity<AtmHardFitness> getatmHardFitnessById(@PathVariable(value = "id") String atmHardFitnessId)
			throws ResourceNotFoundException {
		AtmHardFitness employee = atmHardFitnessRepository.findById(atmHardFitnessId)
				.orElseThrow(() -> new ResourceNotFoundException(" not found for this id :: " + atmHardFitnessId));
		logger.info(employee.toString());
		return ResponseEntity.ok().body(employee);
	}

//  @GetMapping("allAtm")
//  public List<AtmHardFitness> getAllAtm() {
//    return atmHardFitnessRepository.findAll();
//
//  }

	@GetMapping("/NbOfTerminalStatus")
	public EtatTerminal CalculNbOfTerminalStatus() {
		try {
			List<AtmTerminal> atmTerminalSupervisor = atmTerminalRepository.findTerminalStatusEgaleSupervisor();
			int nbOfTerminalStatusEgaleSupervisor = atmTerminalSupervisor.size();
			List<AtmTerminal> atmTerminalDisconnected = atmTerminalRepository.findTerminalStatusEgaleDisconected();
			int nbOfTerminalStatusEgaleDisconected = atmTerminalDisconnected.size();
			List<AtmTerminal> atmTerminalOutOfService = atmTerminalRepository.findTerminalStatusEgaleOutOfService();
			int nbOfTerminalStatusEgaleOutOfService = atmTerminalOutOfService.size();
			List<AtmTerminal> atmTerminal = atmTerminalRepository.findTerminalStatusEgaleOnService();
			int nbOfTerminalStatusEgaleOnService = atmTerminal.size();
			EtatTerminal etat_terminal = new EtatTerminal();
			etat_terminal.setNbDisconnected(nbOfTerminalStatusEgaleDisconected);
			etat_terminal.setNbSupervisor(nbOfTerminalStatusEgaleSupervisor);
			etat_terminal.setOnService(nbOfTerminalStatusEgaleOnService);
			etat_terminal.setOutOfService(nbOfTerminalStatusEgaleOutOfService);
			return etat_terminal;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	@GetMapping("allAtm")
	public List<AtmHardFitnessDisplay> getAllAtm() {

		List<AtmHardFitnessDisplay> list = new ArrayList<AtmHardFitnessDisplay>();
		for (AtmHardFitness element : atmHardFitnessRepository.findAll()) {
			AtmTerminal atm = atmTerminalRepository.findByAteId(element.getAtmHardFitnessId());
			AtmHardFitnessDisplay obj = new AtmHardFitnessDisplay();
			obj.setEtatTerminal(atm.getEtatTerminal());
			obj.setAtmHardFitnessId(element.getAtmHardFitnessId());
			obj.setAtmHardFitnessBiometricCapture(element.getAtmHardFitnessBiometricCapture());
			obj.setAtmHardFitnessBrunchNoteAcceptor(element.getAtmHardFitnessBrunchNoteAcceptor());
			obj.setAtmHardFitnessCardReader(element.getAtmHardFitnessCardReader());
			obj.setAtmHardFitnessCashHandler(element.getAtmHardFitnessCashHandler());
			obj.setAtmHardFitnessCassType1(element.getAtmHardFitnessCassType1());
			obj.setAtmHardFitnessCassType2(element.getAtmHardFitnessCassType2());
			obj.setAtmHardFitnessCassType3(element.getAtmHardFitnessCassType3());
			obj.setAtmHardFitnessCassType4(element.getAtmHardFitnessCassType4());
			obj.setAtmHardFitnessChequeProcessor(element.getAtmHardFitnessChequeProcessor());
			obj.setAtmHardFitnessCoinDispModule(element.getAtmHardFitnessCoinDispModule());
			obj.setAtmHardFitnessDepository(element.getAtmHardFitnessDepository());
			obj.setAtmHardFitnessDocModTamper(element.getAtmHardFitnessDocModTamper());
			obj.setAtmHardFitnessDocProcModule(element.getAtmHardFitnessDocProcModule());
			obj.setAtmHardFitnessDoorAccess(element.getAtmHardFitnessDoorAccess());
			obj.setAtmHardFitnessEncryptor(element.getAtmHardFitnessEncryptor());
			obj.setAtmHardFitnessEnvelopeDispenser(element.getAtmHardFitnessEnvelopeDispenser());
			obj.setAtmHardFitnessFlexDisk(element.getAtmHardFitnessFlexDisk());
			obj.setAtmHardFitnessHighOrderCom(element.getAtmHardFitnessHighOrderCom());
			obj.setAtmHardFitnessJournalPrinter(element.getAtmHardFitnessJournalPrinter());
			obj.setAtmHardFitnessMediaEntryIndi(element.getAtmHardFitnessMediaEntryIndi());
			obj.setAtmHardFitnessNightSafeDeposit(element.getAtmHardFitnessNightSafeDeposit());
			obj.setAtmHardFitnessPosition19(element.getAtmHardFitnessPosition19());
			obj.setAtmHardFitnessPosition20(element.getAtmHardFitnessPosition20());
			obj.setAtmHardFitnessPosition23(element.getAtmHardFitnessPosition23());
			obj.setAtmHardFitnessPosition24(element.getAtmHardFitnessPosition24());
			obj.setAtmHardFitnessPosition31(element.getAtmHardFitnessPosition31());
			obj.setAtmHardFitnessPosition33(element.getAtmHardFitnessPosition33());
			obj.setAtmHardFitnessPosition37(element.getAtmHardFitnessPosition37());
			obj.setAtmHardFitnessPosition8(element.getAtmHardFitnessPosition8());
			obj.setAtmHardFitnessPosition9(element.getAtmHardFitnessPosition9());
			obj.setAtmHardFitnessReceiptPrinter(element.getAtmHardFitnessReceiptPrinter());
			obj.setAtmHardFitnessSecurityCamera(element.getAtmHardFitnessSecurityCamera());
			obj.setAtmHardFitnessSignageDisplay(element.getAtmHardFitnessSignageDisplay());
			obj.setAtmHardFitnessStatementPrinter(element.getAtmHardFitnessStatementPrinter());
			obj.setAtmHardFitnessSystemDisk(element.getAtmHardFitnessSystemDisk());
			obj.setAtmHardFitnessSystemDisplay(element.getAtmHardFitnessSystemDisplay());
			obj.setAtmHardFitnessTimeDay(element.getAtmHardFitnessTimeDay());
			obj.setAtmHardFitnessVoiceGuideSystem(element.getAtmHardFitnessVoiceGuideSystem());

			list.add(obj);
		}

		return list;

	}

//  @PostMapping("getAtmHardFitnessFiltred")
//  public List<AtmHardFitness> getAtmHardFitnessFiltred(@RequestBody String atmHardFitnessId) {
//      if(!atmHardFitnessId.equals("=")){
//    	  
//    	  
//          return atmHardFitnessRepository.findAllByAtmHardFitnessId(atmHardFitnessId.trim());
//      } else{
//          return atmHardFitnessRepository.findAll();
//      }
//  }
	@PostMapping("getAtmHardFitnessFiltred")
	public List<AtmHardFitnessDisplay> getAtmHardFitnessFiltred(@RequestBody String atmHardFitnessId) {

		List<AtmHardFitnessDisplay> list = new ArrayList<AtmHardFitnessDisplay>();
		if (!atmHardFitnessId.equals("=")) {

			for (AtmHardFitness element : atmHardFitnessRepository.findAllByAtmHardFitnessId(atmHardFitnessId.trim())) {
				AtmTerminal atm = atmTerminalRepository.findByAteId(element.getAtmHardFitnessId());
				AtmHardFitnessDisplay obj = new AtmHardFitnessDisplay();
				obj.setEtatTerminal(atm.getEtatTerminal());
				obj.setAtmHardFitnessId(element.getAtmHardFitnessId());
				obj.setAtmHardFitnessBiometricCapture(element.getAtmHardFitnessBiometricCapture());
				obj.setAtmHardFitnessBrunchNoteAcceptor(element.getAtmHardFitnessBrunchNoteAcceptor());
				obj.setAtmHardFitnessCardReader(element.getAtmHardFitnessCardReader());
				obj.setAtmHardFitnessCashHandler(element.getAtmHardFitnessCashHandler());
				obj.setAtmHardFitnessCassType1(element.getAtmHardFitnessCassType1());
				obj.setAtmHardFitnessCassType2(element.getAtmHardFitnessCassType2());
				obj.setAtmHardFitnessCassType3(element.getAtmHardFitnessCassType3());
				obj.setAtmHardFitnessCassType4(element.getAtmHardFitnessCassType4());
				obj.setAtmHardFitnessChequeProcessor(element.getAtmHardFitnessChequeProcessor());
				obj.setAtmHardFitnessCoinDispModule(element.getAtmHardFitnessCoinDispModule());
				obj.setAtmHardFitnessDepository(element.getAtmHardFitnessDepository());
				obj.setAtmHardFitnessDocModTamper(element.getAtmHardFitnessDocModTamper());
				obj.setAtmHardFitnessDocProcModule(element.getAtmHardFitnessDocProcModule());
				obj.setAtmHardFitnessDoorAccess(element.getAtmHardFitnessDoorAccess());
				obj.setAtmHardFitnessEncryptor(element.getAtmHardFitnessEncryptor());
				obj.setAtmHardFitnessEnvelopeDispenser(element.getAtmHardFitnessEnvelopeDispenser());
				obj.setAtmHardFitnessFlexDisk(element.getAtmHardFitnessFlexDisk());
				obj.setAtmHardFitnessHighOrderCom(element.getAtmHardFitnessHighOrderCom());
				obj.setAtmHardFitnessJournalPrinter(element.getAtmHardFitnessJournalPrinter());
				obj.setAtmHardFitnessMediaEntryIndi(element.getAtmHardFitnessMediaEntryIndi());
				obj.setAtmHardFitnessNightSafeDeposit(element.getAtmHardFitnessNightSafeDeposit());
				obj.setAtmHardFitnessPosition19(element.getAtmHardFitnessPosition19());
				obj.setAtmHardFitnessPosition20(element.getAtmHardFitnessPosition20());
				obj.setAtmHardFitnessPosition23(element.getAtmHardFitnessPosition23());
				obj.setAtmHardFitnessPosition24(element.getAtmHardFitnessPosition24());
				obj.setAtmHardFitnessPosition31(element.getAtmHardFitnessPosition31());
				obj.setAtmHardFitnessPosition33(element.getAtmHardFitnessPosition33());
				obj.setAtmHardFitnessPosition37(element.getAtmHardFitnessPosition37());
				obj.setAtmHardFitnessPosition8(element.getAtmHardFitnessPosition8());
				obj.setAtmHardFitnessPosition9(element.getAtmHardFitnessPosition9());
				obj.setAtmHardFitnessReceiptPrinter(element.getAtmHardFitnessReceiptPrinter());
				obj.setAtmHardFitnessSecurityCamera(element.getAtmHardFitnessSecurityCamera());
				obj.setAtmHardFitnessSignageDisplay(element.getAtmHardFitnessSignageDisplay());
				obj.setAtmHardFitnessStatementPrinter(element.getAtmHardFitnessStatementPrinter());
				obj.setAtmHardFitnessSystemDisk(element.getAtmHardFitnessSystemDisk());
				obj.setAtmHardFitnessSystemDisplay(element.getAtmHardFitnessSystemDisplay());
				obj.setAtmHardFitnessTimeDay(element.getAtmHardFitnessTimeDay());
				obj.setAtmHardFitnessVoiceGuideSystem(element.getAtmHardFitnessVoiceGuideSystem());

				list.add(obj);
			}

			return list;
		} else {

			for (AtmHardFitness element : atmHardFitnessRepository.findAll()) {
				AtmTerminal atm = atmTerminalRepository.findByAteId(element.getAtmHardFitnessId());
				AtmHardFitnessDisplay obj = new AtmHardFitnessDisplay();
				obj.setEtatTerminal(atm.getEtatTerminal());
				obj.setAtmHardFitnessId(element.getAtmHardFitnessId());
				obj.setAtmHardFitnessBiometricCapture(element.getAtmHardFitnessBiometricCapture());
				obj.setAtmHardFitnessBrunchNoteAcceptor(element.getAtmHardFitnessBrunchNoteAcceptor());
				obj.setAtmHardFitnessCardReader(element.getAtmHardFitnessCardReader());
				obj.setAtmHardFitnessCashHandler(element.getAtmHardFitnessCashHandler());
				obj.setAtmHardFitnessCassType1(element.getAtmHardFitnessCassType1());
				obj.setAtmHardFitnessCassType2(element.getAtmHardFitnessCassType2());
				obj.setAtmHardFitnessCassType3(element.getAtmHardFitnessCassType3());
				obj.setAtmHardFitnessCassType4(element.getAtmHardFitnessCassType4());
				obj.setAtmHardFitnessChequeProcessor(element.getAtmHardFitnessChequeProcessor());
				obj.setAtmHardFitnessCoinDispModule(element.getAtmHardFitnessCoinDispModule());
				obj.setAtmHardFitnessDepository(element.getAtmHardFitnessDepository());
				obj.setAtmHardFitnessDocModTamper(element.getAtmHardFitnessDocModTamper());
				obj.setAtmHardFitnessDocProcModule(element.getAtmHardFitnessDocProcModule());
				obj.setAtmHardFitnessDoorAccess(element.getAtmHardFitnessDoorAccess());
				obj.setAtmHardFitnessEncryptor(element.getAtmHardFitnessEncryptor());
				obj.setAtmHardFitnessEnvelopeDispenser(element.getAtmHardFitnessEnvelopeDispenser());
				obj.setAtmHardFitnessFlexDisk(element.getAtmHardFitnessFlexDisk());
				obj.setAtmHardFitnessHighOrderCom(element.getAtmHardFitnessHighOrderCom());
				obj.setAtmHardFitnessJournalPrinter(element.getAtmHardFitnessJournalPrinter());
				obj.setAtmHardFitnessMediaEntryIndi(element.getAtmHardFitnessMediaEntryIndi());
				obj.setAtmHardFitnessNightSafeDeposit(element.getAtmHardFitnessNightSafeDeposit());
				obj.setAtmHardFitnessPosition19(element.getAtmHardFitnessPosition19());
				obj.setAtmHardFitnessPosition20(element.getAtmHardFitnessPosition20());
				obj.setAtmHardFitnessPosition23(element.getAtmHardFitnessPosition23());
				obj.setAtmHardFitnessPosition24(element.getAtmHardFitnessPosition24());
				obj.setAtmHardFitnessPosition31(element.getAtmHardFitnessPosition31());
				obj.setAtmHardFitnessPosition33(element.getAtmHardFitnessPosition33());
				obj.setAtmHardFitnessPosition37(element.getAtmHardFitnessPosition37());
				obj.setAtmHardFitnessPosition8(element.getAtmHardFitnessPosition8());
				obj.setAtmHardFitnessPosition9(element.getAtmHardFitnessPosition9());
				obj.setAtmHardFitnessReceiptPrinter(element.getAtmHardFitnessReceiptPrinter());
				obj.setAtmHardFitnessSecurityCamera(element.getAtmHardFitnessSecurityCamera());
				obj.setAtmHardFitnessSignageDisplay(element.getAtmHardFitnessSignageDisplay());
				obj.setAtmHardFitnessStatementPrinter(element.getAtmHardFitnessStatementPrinter());
				obj.setAtmHardFitnessSystemDisk(element.getAtmHardFitnessSystemDisk());
				obj.setAtmHardFitnessSystemDisplay(element.getAtmHardFitnessSystemDisplay());
				obj.setAtmHardFitnessTimeDay(element.getAtmHardFitnessTimeDay());
				obj.setAtmHardFitnessVoiceGuideSystem(element.getAtmHardFitnessVoiceGuideSystem());

				list.add(obj);
			}

			return list;

		}

	}

	@GetMapping("/consultCassette/{id}")
	public List<CassetteCountResult> consultCassette(@PathVariable(value = "id") String atmHardFitnessId) {
		List<AtmCassetteCount> atmCassetteCounts = atmCassetteCountRepository.findByTerminalId(atmHardFitnessId);
		AtmTerminal atmTerminal = atmTerminalRepository.findByAteId(atmHardFitnessId);
		List<CassetteCountResult> cassetteCountResults = new ArrayList<>();
		for (AtmCassetteCount atm : atmCassetteCounts) {

			CassetteCountResult cassetteCountResult = new CassetteCountResult();
			cassetteCountResult.setAtmCassTerminalId(atm.getAtmCassetteCountId().getAtmCassTerminalId());
			cassetteCountResult.setAtmCassNumberId(atm.getAtmCassetteCountId().getAtmCassNumberId());
			cassetteCountResult.setAtmCassetteNoteIn(atm.getAtmCassetteNoteIn());
			cassetteCountResult.setAtmCassetteNoteDispensed(atm.getAtmCassetteNoteDispensed());
			cassetteCountResult.setAtmCassetteNoteLastTrans(atm.getAtmCassetteNoteLastTrans());
			cassetteCountResult.setAtmCassetteNoteRejected(atm.getAtmCassetteNoteRejected());
			cassetteCountResult.setAtmCassetteValue(atm.getAtmCassetteValue());
			cassetteCountResult.setStatus(atmTerminal.getEtatTerminal());

			cassetteCountResults.add(cassetteCountResult);

			logger.info(cassetteCountResult.toString());
		}
		return cassetteCountResults;
	}

	@GetMapping("/consultSensorStatus/{id}")
	public List<AtmSensorStatus> consultSensorStatus(@PathVariable(value = "id") String atmId) {

		return atmSensorStatusRepository.findAtmSensorStatus(atmId);

	}

	@GetMapping("/consultAtmHardFitness/{id}")
	public List<AtmMonitoring> consultAtmHardFitness(@PathVariable(value = "id") String atmId) {
		List<AtmMonitoring> AtmMonitoring = new ArrayList<>();
		AtmHardFitness atmHardFitness = atmHardFitnessRepository.getOne(atmId);
		if (atmHardFitness != null) {
			AtmMonitoring aValue = new AtmMonitoring();
			AtmMonitoring aDescription = new AtmMonitoring();
			String timeOfDay = atmHardFitness.getAtmHardFitnessTimeDay();
			aValue.setTimeOfDayClock(timeOfDay);
			if (timeOfDay == null || timeOfDay.equals("0")) {
				aDescription.setTimeOfDayClock("Pas d'erreur");
			} else {

				if (timeOfDay.equals("1") || timeOfDay.equals("2") || timeOfDay.equals("3")) {
					aDescription.setTimeOfDayClock("Alerte");
				}

				if (timeOfDay.equals("4")) {
					aDescription.setTimeOfDayClock("Erreur");

				}
			}

			String highOrder = atmHardFitness.getAtmHardFitnessHighOrderCom();
			aValue.setHighOrder(highOrder);
			if (highOrder == null || highOrder.equals("0")) {
				aDescription.setHighOrder("Pas d'erreur");
			} else {

				if (highOrder.equals("1") || highOrder.equals("2") || highOrder.equals("3")) {
					aDescription.setHighOrder("Alerte");
				}

				if (highOrder.equals("4")) {
					aDescription.setHighOrder("Erreur");

				}
			}

			String systemDisk = atmHardFitness.getAtmHardFitnessSystemDisk();
			aValue.setSystemDisk(systemDisk);
			if (systemDisk == null || systemDisk.equals("0")) {
				aDescription.setSystemDisk("Pas d'erreur");
			} else {

				if (systemDisk.equals("1") || systemDisk.equals("2") || systemDisk.equals("3")) {
					aDescription.setSystemDisk("Alerte");
				}

				if (systemDisk.equals("4")) {
					aDescription.setSystemDisk("Erreur");

				}
			}

			String cardReader = atmHardFitness.getAtmHardFitnessCardReader();
			aValue.setCardReader(cardReader);
			if (cardReader == null || cardReader.equals("0")) {
				aDescription.setCardReader("Pas d'erreur");
			} else {

				if (cardReader.equals("1") || cardReader.equals("2") || cardReader.equals("3")) {
					aDescription.setCardReader("Alerte");
				}

				if (cardReader.equals("4")) {
					aDescription.setCardReader("Erreur");

				}
			}

			String cashHandler = atmHardFitness.getAtmHardFitnessCashHandler();
			aValue.setCashHandler(cashHandler);
			if (cashHandler == null || cashHandler.equals("0")) {
				aDescription.setCashHandler("Pas d'erreur");
			} else {

				if (cashHandler.equals("1") || cashHandler.equals("2") || cashHandler.equals("3")) {
					aDescription.setCashHandler("Alerte");
				}

				if (cashHandler.equals("4")) {
					aDescription.setCashHandler("Erreur");

				}
			}

			String depository = atmHardFitness.getAtmHardFitnessDepository();
			aValue.setDepository(depository);
			if (depository == null || depository.equals("0")) {
				aDescription.setDepository("Pas d'erreur");
			} else {

				if (depository.equals("1") || depository.equals("2") || depository.equals("3")) {
					aDescription.setDepository("Alerte");
				}

				if (depository.equals("4")) {
					aDescription.setDepository("Erreur");

				}
			}

			String receipt = atmHardFitness.getAtmHardFitnessReceiptPrinter();
			aValue.setRceceiptPrinter(receipt);
			if (receipt == null || receipt.equals("0")) {
				aDescription.setRceceiptPrinter("Pas d'erreur");
			} else {

				if (receipt.equals("1") || receipt.equals("2") || receipt.equals("3")) {
					aDescription.setRceceiptPrinter("Alerte");
				}

				if (receipt.equals("4")) {
					aDescription.setRceceiptPrinter("Erreur");

				}
			}

			String journalPrinter = atmHardFitness.getAtmHardFitnessJournalPrinter();
			aValue.setJournalPrinter(journalPrinter);
			if (journalPrinter == null || journalPrinter.equals("0")) {
				aDescription.setJournalPrinter("Pas d'erreur");
			} else {

				if (journalPrinter.equals("1") || journalPrinter.equals("2") || journalPrinter.equals("3")) {
					aDescription.setJournalPrinter("Alerte");
				}

				if (journalPrinter.equals("4")) {
					aDescription.setJournalPrinter("Erreur");

				}
			}

			String nightSafe = atmHardFitness.getAtmHardFitnessNightSafeDeposit();
			aValue.setNightSafeDepository(nightSafe);
			if (nightSafe == null || nightSafe.equals("0")) {
				aDescription.setNightSafeDepository("Pas d'erreur");
			} else {

				if (nightSafe.equals("1") || nightSafe.equals("2") || nightSafe.equals("3")) {
					aDescription.setNightSafeDepository("Alerte");
				}

				if (nightSafe.equals("4")) {
					aDescription.setNightSafeDepository("Erreur");

				}
			}

			String encryptor = atmHardFitness.getAtmHardFitnessEncryptor();
			aValue.setEncryptor(encryptor);
			if (encryptor == null || encryptor.equals("0")) {
				aDescription.setEncryptor("Pas d'erreur");
			} else {

				if (encryptor.equals("1") || encryptor.equals("2") || encryptor.equals("3")) {
					aDescription.setEncryptor("Alerte");
				}

				if (encryptor.equals("4")) {
					aDescription.setEncryptor("Erreur");

				}
			}

			String camera = atmHardFitness.getAtmHardFitnessSecurityCamera();
			aValue.setSecurityCamera(camera);
			if (camera == null || camera.equals("0")) {
				aDescription.setSecurityCamera("Pas d'erreur");
			} else {

				if (camera.equals("1") || camera.equals("2") || camera.equals("3")) {
					aDescription.setSecurityCamera("Alerte");
				}

				if (camera.equals("4")) {
					aDescription.setSecurityCamera("Erreur");

				}
			}

			String door = atmHardFitness.getAtmHardFitnessDoorAccess();
			aValue.setDoorAccess(door);
			if (door == null || door.equals("0")) {
				aDescription.setDoorAccess("Pas d'erreur");
			} else {

				if (door.equals("1") || door.equals("2") || door.equals("3")) {
					aDescription.setDoorAccess("Alerte");
				}

				if (door.equals("4")) {
					aDescription.setDoorAccess("Erreur");

				}
			}

			String flexDisk = atmHardFitness.getAtmHardFitnessFlexDisk();
			aValue.setFlexDisk(flexDisk);
			if (flexDisk == null || flexDisk.equals("0")) {
				aDescription.setFlexDisk("Pas d'erreur");
			} else {

				if (flexDisk.equals("1") || flexDisk.equals("2") || flexDisk.equals("3")) {
					aDescription.setFlexDisk("Alerte");
				}

				if (flexDisk.equals("4")) {
					aDescription.setFlexDisk("Erreur");

				}
			}

			String cassette1 = atmHardFitness.getAtmHardFitnessCassType1();
			aValue.setCassette1(cassette1);
			if (cassette1 == null || cassette1.equals("0")) {
				aDescription.setCassette1("Pas d'erreur");
			} else {

				if (cassette1.equals("1") || cassette1.equals("2") || cassette1.equals("3")) {
					aDescription.setCassette1("Alerte");
				}

				if (cassette1.equals("4")) {
					aDescription.setCassette1("Erreur");

				}
			}

			String cassette2 = atmHardFitness.getAtmHardFitnessCassType2();
			aValue.setCassette2(cassette2);
			if (cassette2 == null || cassette2.equals("0")) {
				aDescription.setCassette2("Pas d'erreur");
			} else {

				if (cassette2.equals("1") || cassette2.equals("2") || cassette2.equals("3")) {
					aDescription.setCassette2("Alerte");
				}

				if (cassette2.equals("4")) {
					aDescription.setCassette2("Erreur");

				}
			}

			String cassette3 = atmHardFitness.getAtmHardFitnessCassType3();
			aValue.setCassette3(cassette3);
			if (cassette3 == null || cassette3.equals("0")) {
				aDescription.setCassette3("Pas d'erreur");
			} else {

				if (cassette3.equals("1") || cassette3.equals("2") || cassette3.equals("3")) {
					aDescription.setCassette3("Alerte");
				}

				if (cassette3.equals("4")) {
					aDescription.setCassette3("Erreur");

				}
			}

			String cassette4 = atmHardFitness.getAtmHardFitnessCassType4();
			aValue.setCassette4(cassette4);

			if (cassette4 == null || cassette4.equals("0")) {
				aDescription.setCassette4("Pas d'erreur");
			} else {

				if (cassette4.equals("1") || cassette4.equals("2") || cassette4.equals("3")) {
					aDescription.setCassette4("Alerte");
				}

				if (cassette4.equals("4")) {
					aDescription.setCassette4("Erreur");

				}
			}

			String statementPrinter = atmHardFitness.getAtmHardFitnessStatementPrinter();
			aValue.setStatementPrinter(statementPrinter);
			if (statementPrinter == null || statementPrinter.equals("0")) {
				aDescription.setStatementPrinter("Pas d'erreur");
			} else {

				if (statementPrinter.equals("1") || statementPrinter.equals("2") || statementPrinter.equals("3")) {
					aDescription.setStatementPrinter("Alerte");
				}

				if (statementPrinter.equals("4")) {
					aDescription.setStatementPrinter("Erreur");

				}
			}

			String signageDisplay = atmHardFitness.getAtmHardFitnessSignageDisplay();
			aValue.setSignageDisplay(signageDisplay);
			if (signageDisplay == null || signageDisplay.equals("0")) {
				aDescription.setSignageDisplay("Pas d'erreur");
			} else {

				if (signageDisplay.equals("1") || signageDisplay.equals("2") || signageDisplay.equals("3")) {
					aDescription.setSignageDisplay("Alerte");
				}

				if (signageDisplay.equals("4")) {
					aDescription.setSignageDisplay("Erreur");

				}
			}

			String systemDisplay = atmHardFitness.getAtmHardFitnessSystemDisplay();
			aValue.setSystemDisplay(systemDisplay);
			if (systemDisplay == null || systemDisplay.equals("0")) {
				aDescription.setSystemDisplay("Pas d'erreur");
			} else {

				if (systemDisplay.equals("1") || systemDisplay.equals("2") || systemDisplay.equals("3")) {
					aDescription.setSystemDisplay("Alerte");
				}

				if (systemDisplay.equals("4")) {
					aDescription.setSystemDisplay("Erreur");

				}
			}

			String mediaEntry = atmHardFitness.getAtmHardFitnessMediaEntryIndi();
			aValue.setMediaEntryIndicators(mediaEntry);
			if (mediaEntry == null || mediaEntry.equals("0")) {
				aDescription.setMediaEntryIndicators("Pas d'erreur");
			} else {

				if (mediaEntry.equals("1") || mediaEntry.equals("2") || mediaEntry.equals("3")) {
					aDescription.setMediaEntryIndicators("Alerte");
				}

				if (mediaEntry.equals("4")) {
					aDescription.setMediaEntryIndicators("Erreur");

				}
			}

			String envelopeDispenser = atmHardFitness.getAtmHardFitnessEnvelopeDispenser();
			aValue.setEnvelopeDispenser(envelopeDispenser);
			if (envelopeDispenser == null || envelopeDispenser.equals("0")) {
				aDescription.setEnvelopeDispenser("Pas d'erreur");
			} else {

				if (envelopeDispenser.equals("1") || envelopeDispenser.equals("2") || envelopeDispenser.equals("3")) {
					aDescription.setEnvelopeDispenser("Alerte");
				}

				if (envelopeDispenser.equals("4")) {
					aDescription.setEnvelopeDispenser("Erreur");

				}
			}

			String documentProcessingModule = atmHardFitness.getAtmHardFitnessDocProcModule();
			aValue.setDocumentProcessingModule(documentProcessingModule);
			if (documentProcessingModule == null || documentProcessingModule.equals("0")) {
				aDescription.setDocumentProcessingModule("Pas d'erreur");
			} else {

				if (documentProcessingModule.equals("1") || documentProcessingModule.equals("2")
						|| documentProcessingModule.equals("3")) {
					aDescription.setDocumentProcessingModule("Alerte");
				}

				if (documentProcessingModule.equals("4")) {
					aDescription.setDocumentProcessingModule("Erreur");

				}
			}

			String coinDispensing = atmHardFitness.getAtmHardFitnessCoinDispModule();
			aValue.setCoinDispensingModuleTamperIndication(coinDispensing);
			if (coinDispensing == null || coinDispensing.equals("0")) {
				aDescription.setCoinDispensingModuleTamperIndication("Pas d'erreur");
			} else {

				if (coinDispensing.equals("1") || coinDispensing.equals("2") || coinDispensing.equals("3")) {
					aDescription.setCoinDispensingModuleTamperIndication("Alerte");
				}

				if (coinDispensing.equals("4")) {
					aDescription.setCoinDispensingModuleTamperIndication("Erreur");

				}
			}

			String documentProcessingModuleTamper = atmHardFitness.getAtmHardFitnessDocModTamper();
			aValue.setDocumentProcessingModuleTamper(documentProcessingModuleTamper);
			if (documentProcessingModuleTamper == null || documentProcessingModuleTamper.equals("0")) {
				aDescription.setDocumentProcessingModuleTamper("Pas d'erreur");
			} else {

				if (documentProcessingModuleTamper.equals("1") || documentProcessingModuleTamper.equals("2")
						|| documentProcessingModuleTamper.equals("3")) {
					aDescription.setDocumentProcessingModuleTamper("Alerte");
				}

				if (documentProcessingModuleTamper.equals("4")) {
					aDescription.setDocumentProcessingModuleTamper("Erreur");

				}
			}

			String voiceGuidance = atmHardFitness.getAtmHardFitnessVoiceGuideSystem();
			aValue.setVoiceGuidanceSystem(voiceGuidance);
			if (voiceGuidance == null || voiceGuidance.equals("0")) {
				aDescription.setVoiceGuidanceSystem("Pas d'erreur");
			} else {

				if (voiceGuidance.equals("1") || voiceGuidance.equals("2") || voiceGuidance.equals("3")) {
					aDescription.setVoiceGuidanceSystem("Alerte");
				}

				if (voiceGuidance.equals("4")) {
					aDescription.setVoiceGuidanceSystem("Erreur");

				}
			}

			String bunch = atmHardFitness.getAtmHardFitnessBrunchNoteAcceptor();
			aValue.setBunchNoteAcceptor(bunch);
			if (bunch == null || bunch.equals("0")) {
				aDescription.setBunchNoteAcceptor("Pas d'erreur");
			} else {

				if (bunch.equals("1") || bunch.equals("2") || bunch.equals("3")) {
					aDescription.setBunchNoteAcceptor("Alerte");
				}

				if (bunch.equals("4")) {
					aDescription.setBunchNoteAcceptor("Erreur");

				}
			}

			String cheque = atmHardFitness.getAtmHardFitnessChequeProcessor();
			aValue.setChequeProcessor(cheque);
			if (cheque == null || cheque.equals("0")) {
				aDescription.setChequeProcessor("Pas d'erreur");
			} else {

				if (cheque.equals("1") || cheque.equals("2") || cheque.equals("3")) {
					aDescription.setChequeProcessor("Alerte");
				}

				if (cheque.equals("4")) {
					aDescription.setChequeProcessor("Erreur");

				}
			}

			String biometric = atmHardFitness.getAtmHardFitnessBiometricCapture();
			aValue.setBiometricCaptureDevice(biometric);
			if (biometric == null || biometric.equals("0")) {
				aDescription.setBiometricCaptureDevice("Pas d'erreur");
			} else {

				if (biometric.equals("1") || biometric.equals("2") || biometric.equals("3")) {
					aDescription.setBiometricCaptureDevice("Alerte");
				}

				if (biometric.equals("4")) {
					aDescription.setBiometricCaptureDevice("Erreur");

				}
			}

			AtmMonitoring.add(aValue);
			AtmMonitoring.add(aDescription);

		}
		return AtmMonitoring;

	}

	@GetMapping("/consultAtmSuppliesStatus/{id}")
	public List<AtmSuppliesStatusRequest> consultAtmSuppliesStatus(@PathVariable(value = "id") String atmId) {

		AtmSuppliesStatus atmSuppliesStatus = atmSuppliesStatusRepository.getOne(atmId);
		List<AtmSuppliesStatusRequest> atmSuppliesStatusRequest = new ArrayList<>();
		if (atmSuppliesStatus != null) {

			AtmSuppliesStatusRequest aValue = new AtmSuppliesStatusRequest();
			AtmSuppliesStatusRequest aDescription = new AtmSuppliesStatusRequest();

			String cardCaptureBin = atmSuppliesStatus.getAtmSuppliesCardCapture();
			aValue.setCardCaptureBin(cardCaptureBin);

			if (cardCaptureBin == null || cardCaptureBin.equals("0")) {
				aDescription.setCardCaptureBin("Pas configuré");
			} else {

				if (cardCaptureBin.equals("1")) {
					aDescription.setCardCaptureBin("Bonne condition");
				}
				if (cardCaptureBin.equals("2")) {
					aDescription.setCardCaptureBin("Média Bas");

				}
				if (cardCaptureBin.equals("3")) {
					aDescription.setCardCaptureBin("Sortie Média");

				}

				if (cardCaptureBin.equals("4")) {
					aDescription.setCardCaptureBin("Déborder");

				}
			}

			String cashHandler = atmSuppliesStatus.getAtmSuppliesCashHandler();
			aValue.setCashHandlerRejectBin(cashHandler);

			if (cashHandler == null || cashHandler.equals("0")) {
				aDescription.setCashHandlerRejectBin("Pas configuré");
			} else {

				if (cashHandler.equals("1")) {
					aDescription.setCashHandlerRejectBin("Bonne condition");
				}
				if (cashHandler.equals("2")) {
					aDescription.setCashHandlerRejectBin("Média Bas");

				}
				if (cashHandler.equals("3")) {
					aDescription.setCashHandlerRejectBin("Sortie Média");

				}

				if (cashHandler.equals("4")) {
					aDescription.setCashHandlerRejectBin("Déborder");

				}
			}

			String depositBin = atmSuppliesStatus.getAtmSuppliesDepositBin();
			aValue.setDepositBin(depositBin);

			if (depositBin == null || depositBin.equals("0")) {
				aDescription.setDepositBin("Pas configuré");
			} else {

				if (depositBin.equals("1")) {
					aDescription.setDepositBin("Bonne condition");
				}
				if (depositBin.equals("2")) {
					aDescription.setDepositBin("Média Bas");

				}
				if (depositBin.equals("3")) {
					aDescription.setDepositBin("Sortie Média");

				}

				if (depositBin.equals("4")) {
					aDescription.setDepositBin("Déborder");

				}
			}

			String Receipt = atmSuppliesStatus.getAtmSuppliesReceiptPaper();
			aValue.setReceiptPaper(Receipt);

			if (Receipt == null || Receipt.equals("0")) {
				aDescription.setReceiptPaper("Pas configuré");
			} else {

				if (Receipt.equals("1")) {
					aDescription.setReceiptPaper("Bonne condition");
				}
				if (Receipt.equals("2")) {
					aDescription.setReceiptPaper("Média Bas");

				}
				if (Receipt.equals("3")) {
					aDescription.setReceiptPaper("Sortie Média");

				}

				if (Receipt.equals("4")) {
					aDescription.setReceiptPaper("Déborder");

				}
			}

			String journal = atmSuppliesStatus.getAtmSuppliesJournalPaper();
			aValue.setJournalPaper(journal);

			if (journal == null || journal.equals("0")) {
				aDescription.setJournalPaper("Pas configuré");
			} else {

				if (journal.equals("1")) {
					aDescription.setJournalPaper("Bonne condition");
				}
				if (journal.equals("2")) {
					aDescription.setJournalPaper("Média Bas");

				}
				if (journal.equals("3")) {
					aDescription.setJournalPaper("Sortie Média");

				}

				if (journal.equals("4")) {
					aDescription.setJournalPaper("Déborder");

				}
			}

			String night = atmSuppliesStatus.getAtmSuppliesNightSafe();
			aValue.setNightSafe(night);

			if (night == null || night.equals("0")) {
				aDescription.setNightSafe("Pas configuré");
			} else {

				if (night.equals("1")) {
					aDescription.setNightSafe("Bonne condition");
				}
				if (night.equals("2")) {
					aDescription.setNightSafe("Média Bas");

				}
				if (night.equals("3")) {
					aDescription.setNightSafe("Sortie Média");

				}

				if (night.equals("4")) {
					aDescription.setNightSafe("Déborder");

				}
			}

			String cassette1 = atmSuppliesStatus.getAtmSuppliesCurrCass1();
			aValue.setCassette1(cassette1);

			if (cassette1 == null || cassette1.equals("0")) {
				aDescription.setCassette1("Pas configuré");
			} else {

				if (cassette1.equals("1")) {
					aDescription.setCassette1("Bonne condition");
				}
				if (cassette1.equals("2")) {
					aDescription.setCassette1("Média Bas");

				}
				if (cassette1.equals("3")) {
					aDescription.setCassette1("Sortie Média");

				}

				if (cassette1.equals("4")) {
					aDescription.setCassette1("Déborder");

				}
			}

			String cassette2 = atmSuppliesStatus.getAtmSuppliesCurrCass2();
			aValue.setCassette2(cassette2);

			if (cassette2 == null || cassette2.equals("0")) {
				aDescription.setCassette2("Pas configuré");
			} else {

				if (cassette2.equals("1")) {
					aDescription.setCassette2("Bonne condition");
				}
				if (cassette2.equals("2")) {
					aDescription.setCassette2("Média Bas");

				}
				if (cassette2.equals("3")) {
					aDescription.setCassette2("Sortie Média");

				}

				if (cassette2.equals("4")) {
					aDescription.setCassette2("Déborder");

				}
			}

			String cassette3 = atmSuppliesStatus.getAtmSuppliesCurrCass3();
			aValue.setCassette3(cassette3);

			if (cassette3 == null || cassette3.equals("0")) {
				aDescription.setCassette3("Pas configuré");
			} else {

				if (cassette3.equals("1")) {
					aDescription.setCassette3("Bonne condition");
				}
				if (cassette3.equals("2")) {
					aDescription.setCassette3("Média Bas");

				}
				if (cassette3.equals("3")) {
					aDescription.setCassette3("Sortie Média");

				}

				if (cassette3.equals("4")) {
					aDescription.setCassette3("Déborder");

				}
			}

			String cassette4 = atmSuppliesStatus.getAtmSuppliesCurrCass4();
			aValue.setCassette4(cassette4);

			if (cassette4 == null || cassette4.equals("0")) {
				aDescription.setCassette4("Pas configuré");
			} else {

				if (cassette4.equals("1")) {
					aDescription.setCassette4("Bonne condition");
				}
				if (cassette4.equals("2")) {
					aDescription.setCassette4("Média Bas");

				}
				if (cassette4.equals("3")) {
					aDescription.setCassette4("Sortie Média");

				}

				if (cassette4.equals("4")) {
					aDescription.setCassette4("Déborder");

				}
			}

			String statementPaper = atmSuppliesStatus.getAtmSuppliesStatementPaper();
			aValue.setStatementPaper(statementPaper);

			if (statementPaper == null || statementPaper.equals("0")) {
				aDescription.setStatementPaper("Pas configuré");
			} else {

				if (statementPaper.equals("1")) {
					aDescription.setStatementPaper("Bonne condition");
				}
				if (statementPaper.equals("2")) {
					aDescription.setStatementPaper("Média Bas");

				}
				if (statementPaper.equals("3")) {
					aDescription.setStatementPaper("Sortie Média");

				}

				if (statementPaper.equals("4")) {
					aDescription.setStatementPaper("Déborder");

				}
			}

			String statementRibbon = atmSuppliesStatus.getAtmSuppliesStatementRibon();
			aValue.setStatementRibbon(statementRibbon);

			if (statementRibbon == null || statementRibbon.equals("0")) {
				aDescription.setStatementRibbon("Pas configuré");
			} else {

				if (statementRibbon.equals("1")) {
					aDescription.setStatementRibbon("Bonne condition");
				}
				if (statementRibbon.equals("2")) {
					aDescription.setStatementRibbon("Média Bas");

				}
				if (statementRibbon.equals("3")) {
					aDescription.setStatementRibbon("Sortie Média");

				}

				if (statementRibbon.equals("4")) {
					aDescription.setStatementRibbon("Déborder");

				}
			}

			String envelopeDispenser = atmSuppliesStatus.getAtmSuppliesEnvolopeDispenser();
			aValue.setEnvelopeDispenser(envelopeDispenser);

			if (envelopeDispenser == null || envelopeDispenser.equals("0")) {
				aDescription.setEnvelopeDispenser("Pas configuré");
			} else {

				if (envelopeDispenser.equals("1")) {
					aDescription.setEnvelopeDispenser("Bonne condition");
				}
				if (envelopeDispenser.equals("2")) {
					aDescription.setEnvelopeDispenser("Média Bas");

				}
				if (envelopeDispenser.equals("3")) {
					aDescription.setEnvelopeDispenser("Sortie Média");

				}

				if (envelopeDispenser.equals("4")) {
					aDescription.setEnvelopeDispenser("Déborder");

				}
			}
			atmSuppliesStatusRequest.add(aValue);

			atmSuppliesStatusRequest.add(aDescription);

		}

		return atmSuppliesStatusRequest;

	}

	@PostMapping("/addTerminal")
	public ResponseEntity<String> addControlList(@Valid @RequestBody AtmTerminal atmTerminal) {
		// atmTerminal.setZmkKey("65452B5AC05E699CE810530EC04017A0");
		atmTerminal.setTmkKey(
				"S1009651TN00E000076829C00B5EDDAFCD29DCB60AD3702D501E07101CF65BF8216237EC5188AC01F68178C05E74E9A3B");
		// atmTerminal.setTPK_KEY("CFA39EF6BE991767C6A13186E675E19B");
		atmTerminal.setTmkKcv("53F2CC");
		MccList mcc = mccRepository.findByMccListId("6011");
		atmTerminal.setMccCode(mcc.getMccCode());
		atmTerminal = atmTerminalRepository.save(atmTerminal);
		atmTerminal.getAtmConNum();
		Optional<AtmConfiguration> conf = atmConfigurationRepository.findByAtmConNum(atmTerminal.getAtmConNum());
		Optional<AtmCassetteMapValue> mapValue = atmCassetteMapValueRepository.findByAcmpCode(conf.get().getAcmpCode());
		if (mapValue.get().getValueCass1() != 0) {
			AtmCassetteCount atmCassetteCount = new AtmCassetteCount();
			AtmCassetteCountId atmCassetteCountId = new AtmCassetteCountId();
			AtmCassetteMapValue atmCassetteMapp = new AtmCassetteMapValue();
			atmCassetteCountId.setAtmCassTerminalId(atmTerminal.getAteId());
			atmCassetteCountId.setAtmCassNumberId("1");
			atmCassetteCount.setAtmCassetteNoteDispensed("0");
			atmCassetteCount.setAtmCassetteNoteIn("0");
			atmCassetteCount.setAtmCassetteNoteLastTrans("0");
			atmCassetteCount.setAtmCassetteNoteRejected("0");
			atmCassetteCount.setAtmCassetteDevise(mapValue.get().getDeviceCass1());
			atmCassetteCount.setAtmCassetteValue(Integer.toString(mapValue.get().getValueCass1()));
			atmCassetteCount.setAtmCassetteCountId(atmCassetteCountId);
			atmCassetteCountRepository.save(atmCassetteCount);
		}
		if (mapValue.get().getValueCass2() != 0) {
			AtmCassetteCount atmCassetteCount = new AtmCassetteCount();
			AtmCassetteCountId atmCassetteCountId = new AtmCassetteCountId();
			atmCassetteCountId.setAtmCassTerminalId(atmTerminal.getAteId());
			atmCassetteCountId.setAtmCassNumberId("2");
			atmCassetteCount.setAtmCassetteNoteDispensed("0");
			atmCassetteCount.setAtmCassetteNoteIn("0");
			atmCassetteCount.setAtmCassetteNoteLastTrans("0");
			atmCassetteCount.setAtmCassetteNoteRejected("0");
			atmCassetteCount.setAtmCassetteDevise(mapValue.get().getDeviceCass2());
			atmCassetteCount.setAtmCassetteValue(Integer.toString(mapValue.get().getValueCass2()));
			atmCassetteCount.setAtmCassetteCountId(atmCassetteCountId);
			atmCassetteCountRepository.save(atmCassetteCount);
		}
		if (mapValue.get().getValueCass3() != 0) {
			AtmCassetteCount atmCassetteCount = new AtmCassetteCount();
			AtmCassetteCountId atmCassetteCountId = new AtmCassetteCountId();
			atmCassetteCountId.setAtmCassTerminalId(atmTerminal.getAteId());
			atmCassetteCountId.setAtmCassNumberId("3");
			atmCassetteCount.setAtmCassetteNoteDispensed("0");
			atmCassetteCount.setAtmCassetteNoteIn("0");
			atmCassetteCount.setAtmCassetteNoteLastTrans("0");
			atmCassetteCount.setAtmCassetteNoteRejected("0");
			atmCassetteCount.setAtmCassetteDevise(mapValue.get().getDeviceCass3());
			atmCassetteCount.setAtmCassetteValue(Integer.toString(mapValue.get().getValueCass3()));
			atmCassetteCount.setAtmCassetteCountId(atmCassetteCountId);
			atmCassetteCountRepository.save(atmCassetteCount);
		}
		if (mapValue.get().getValueCass4() != 0) {
			AtmCassetteCount atmCassetteCount = new AtmCassetteCount();
			AtmCassetteCountId atmCassetteCountId = new AtmCassetteCountId();

			atmCassetteCountId.setAtmCassTerminalId(atmTerminal.getAteId());
			atmCassetteCountId.setAtmCassNumberId("4");
			atmCassetteCount.setAtmCassetteNoteDispensed("0");
			atmCassetteCount.setAtmCassetteNoteIn("0");
			atmCassetteCount.setAtmCassetteNoteLastTrans("0");
			atmCassetteCount.setAtmCassetteNoteRejected("0");
			atmCassetteCount.setAtmCassetteDevise(mapValue.get().getDeviceCass4());
			atmCassetteCount.setAtmCassetteValue(Integer.toString(mapValue.get().getValueCass4()));
			atmCassetteCount.setAtmCassetteCountId(atmCassetteCountId);
			atmCassetteCountRepository.save(atmCassetteCount);
		}
		AtmHardFitness atmhardf = new AtmHardFitness();
		atmhardf.setAtmHardFitnessId(atmTerminal.getAteId());
		atmhardf.setAtmHardFitnessBiometricCapture("0");
		atmhardf.setAtmHardFitnessBrunchNoteAcceptor("0");
		atmhardf.setAtmHardFitnessCardReader("0");
		atmhardf.setAtmHardFitnessCashHandler("0");
		atmhardf.setAtmHardFitnessCassType1("0");
		atmhardf.setAtmHardFitnessCassType2("0");
		atmhardf.setAtmHardFitnessCassType3("0");
		atmhardf.setAtmHardFitnessCassType4("0");
		atmhardf.setAtmHardFitnessChequeProcessor("0");
		atmhardf.setAtmHardFitnessCoinDispModule("0");
		atmhardf.setAtmHardFitnessDepository("0");
		atmhardf.setAtmHardFitnessDocModTamper("0");
		atmhardf.setAtmHardFitnessDocProcModule("0");
		atmhardf.setAtmHardFitnessDoorAccess("0");
		atmhardf.setAtmHardFitnessEncryptor("0");
		atmhardf.setAtmHardFitnessEnvelopeDispenser("0");
		atmhardf.setAtmHardFitnessFlexDisk("0");
		atmhardf.setAtmHardFitnessHighOrderCom("0");
		atmhardf.setAtmHardFitnessJournalPrinter("0");
		atmhardf.setAtmHardFitnessMediaEntryIndi("0");
		atmhardf.setAtmHardFitnessNightSafeDeposit("0");
		atmhardf.setAtmHardFitnessPosition19("0");
		atmhardf.setAtmHardFitnessPosition20("0");
		atmhardf.setAtmHardFitnessPosition23("0");
		atmhardf.setAtmHardFitnessPosition24("0");
		atmhardf.setAtmHardFitnessPosition31("0");
		atmhardf.setAtmHardFitnessPosition33("0");
		atmhardf.setAtmHardFitnessPosition37("0");
		atmhardf.setAtmHardFitnessPosition8("0");
		atmhardf.setAtmHardFitnessPosition9("0");
		atmhardf.setAtmHardFitnessReceiptPrinter("0");
		atmhardf.setAtmHardFitnessSecurityCamera("0");
		atmhardf.setAtmHardFitnessSignageDisplay("0");
		atmhardf.setAtmHardFitnessStatementPrinter("0");
		atmhardf.setAtmHardFitnessSystemDisk("0");
		atmhardf.setAtmHardFitnessSystemDisplay("0");
		atmhardf.setAtmHardFitnessTimeDay("0");
		atmhardf.setAtmHardFitnessVoiceGuideSystem("0");

		atmHardFitnessRepository.save(atmhardf);

		addSensor(atmTerminal);
		addSuppliesStatus(atmTerminal);
		addAtmHardFitness(atmTerminal);
		addAtmHardConfData(atmTerminal);

		logger.info(atmTerminal.toString());
		return ResponseEntity.ok().body(gson.toJson("AtmTerminal added successfully!"));
	}

	public void addSensor(AtmTerminal atmTerminal) {
		AtmSensorStatus atmSensorStatus = new AtmSensorStatus();
		atmSensorStatus.setAtmSensorId(atmTerminal.getAteId());
		atmSensorStatus.setAtmSensorCurrCass1("0");
		atmSensorStatus.setAtmSensorCurrCass2("0");
		atmSensorStatus.setAtmSensorCurrCass3("0");
		atmSensorStatus.setAtmSensorCurrCass4("0");
		atmSensorStatus.setAtmSensorCurrencyReject("0");
		atmSensorStatus.setAtmSensorDepositBin("0");
		atmSensorStatus.setAtmSensorDoorContact("0");
		atmSensorStatus.setAtmSensorElectronic("0");
		atmSensorStatus.setAtmSensorPosition8("0");
		atmSensorStatus.setAtmSensorSilentSignal("0");
		atmSensorStatus.setAtmSensorSupervisorMode("0");
		atmSensorStatus.setAtmSensorVibrationHeat("0");
		atmSensorStatusRepository.save(atmSensorStatus);
	}

	public void addSuppliesStatus(AtmTerminal atmTerminal) {
		AtmSuppliesStatus atmSuppliesStatus = new AtmSuppliesStatus();
		atmSuppliesStatus.setAtmSuppliesId(atmTerminal.getAteId());
		atmSuppliesStatus.setAtmSuppliesCardCapture("0");
		atmSuppliesStatus.setAtmSuppliesCashHandler("0");
		atmSuppliesStatus.setAtmSuppliesCurrCass1("0");
		atmSuppliesStatus.setAtmSuppliesCurrCass2("0");
		atmSuppliesStatus.setAtmSuppliesCurrCass3("0");
		atmSuppliesStatus.setAtmSuppliesCurrCass4("0");
		atmSuppliesStatus.setAtmSuppliesDepositBin("0");
		atmSuppliesStatus.setAtmSuppliesEnvolopeDispenser("0");
		atmSuppliesStatus.setAtmSuppliesJournalPaper("0");
		atmSuppliesStatus.setAtmSuppliesNightSafe("0");
		atmSuppliesStatus.setAtmSuppliesPosition0("0");
		atmSuppliesStatus.setAtmSuppliesPosition1("0");
		atmSuppliesStatus.setAtmSuppliesPosition2("0");
		atmSuppliesStatus.setAtmSuppliesPosition8("0");
		atmSuppliesStatus.setAtmSuppliesPosition9("0");
		atmSuppliesStatus.setAtmSuppliesPosition11("0");
		atmSuppliesStatus.setAtmSuppliesPosition12("0");
		atmSuppliesStatus.setAtmSuppliesPosition13("0");
		atmSuppliesStatus.setAtmSuppliesPosition19("0");
		atmSuppliesStatus.setAtmSuppliesPosition14("0");
		atmSuppliesStatus.setAtmSuppliesPosition20("0");
		atmSuppliesStatus.setAtmSuppliesPosition23("0");
		atmSuppliesStatus.setAtmSuppliesPosition24("0");
		atmSuppliesStatus.setAtmSuppliesReceiptPaper("0");
		atmSuppliesStatus.setAtmSuppliesStatementPaper("0");
		atmSuppliesStatus.setAtmSuppliesStatementRibon("0");
		atmSuppliesStatusRepository.save(atmSuppliesStatus);
	}

	public void addAtmHardFitness(AtmTerminal atmTerminal) {
		AtmHardFitness atmhardf = new AtmHardFitness();
		atmhardf.setAtmHardFitnessId(atmTerminal.getAteId());
		atmhardf.setAtmHardFitnessBiometricCapture("0");
		atmhardf.setAtmHardFitnessBrunchNoteAcceptor("0");
		atmhardf.setAtmHardFitnessCardReader("0");
		atmhardf.setAtmHardFitnessCashHandler("0");
		atmhardf.setAtmHardFitnessCassType1("0");
		atmhardf.setAtmHardFitnessCassType2("0");
		atmhardf.setAtmHardFitnessCassType3("0");
		atmhardf.setAtmHardFitnessCassType4("0");
		atmhardf.setAtmHardFitnessChequeProcessor("0");
		atmhardf.setAtmHardFitnessCoinDispModule("0");
		atmhardf.setAtmHardFitnessDepository("0");
		atmhardf.setAtmHardFitnessDocModTamper("0");
		atmhardf.setAtmHardFitnessDocProcModule("0");
		atmhardf.setAtmHardFitnessDoorAccess("0");
		atmhardf.setAtmHardFitnessEncryptor("0");
		atmhardf.setAtmHardFitnessEnvelopeDispenser("0");
		atmhardf.setAtmHardFitnessFlexDisk("0");
		atmHardFitnessRepository.save(atmhardf);
	}

	public void addAtmHardConfData(AtmTerminal atmTerminal) {
		AtmHardConfData atmhardconfdata = new AtmHardConfData();
		atmhardconfdata.setAtmHardConfId(atmTerminal.getAteId());
		atmhardconfdata.setAtmHardConfProduct("19");
		atmhardconfdata.setAtmHardConfPosition1("7F");
		atmhardconfdata.setAtmHardConfSystemDisk("00");
		atmhardconfdata.setAtmHardConfCardReader("09");
		atmhardconfdata.setAtmHardConfCashHandler("01");
		atmhardconfdata.setAtmHardConfEnvelopeDeposit("00");
		atmhardconfdata.setAtmHardConfReceiptPrinter("05");
		atmhardconfdata.setAtmHardConfJournalPrinter("80");
		atmhardconfdata.setAtmHardConfPosition8("00");
		atmhardconfdata.setAtmHardConfPosition9("00");
		atmhardconfdata.setAtmHardConfNightSafeDepo("00");
		atmhardconfdata.setAtmHardConfEncryptor("C7");
		atmhardconfdata.setAtmHardConfSecurityCamera("04");
		atmhardconfdata.setAtmHardConfDoorAccess("01");
		atmhardconfdata.setAtmHardConfFlexDisc("00");
		atmhardconfdata.setAtmHardConfTamperIndication("00");
		atmhardconfdata.setAtmHardConfCarholderKeybord("02");
		atmhardconfdata.setAtmHardConfOperatorKeybord("01");
		atmhardconfdata.setAtmHardConfCardHolderDisplayVoice("00");
		atmhardconfdata.setAtmHardConfPosition19("7F");
		atmhardconfdata.setAtmHardConfPosition20("7F");
		atmhardconfdata.setAtmHardConfStatementPrinter("00");
		atmhardconfdata.setAtmHardConfPosition22("0");
		atmhardconfdata.setAtmHardConfPosition23("0");
		atmhardconfdata.setAtmHardConfCoinDispenser("0");
		atmhardconfdata.setAtmHardConfSystemDisplay("0");
		atmhardconfdata.setAtmHardConfMediaEntryIndic("0");
		atmhardconfdata.setAtmHardConfEnvelopeDispenser("0");
		atmhardconfdata.setAtmHardConfDocProdModule("0");
		atmhardconfdata.setAtmHardConfCoinDispModule("0");
		atmhardconfdata.setAtmHardConfDocModTampIndic("0");
		atmhardconfdata.setAtmHardConfPosition31("0");
		atmhardconfdata.setAtmHardConfVoiceGuidance("0");
		atmhardconfdata.setAtmHardConfPosition33("0");
		atmhardconfdata.setAtmHardConfNoteAcceptor("0");
		atmhardconfdata.setAtmHardConfChequeProcessor("0");
		atmhardconfdata.setAtmHardConfPosition36("0");
		atmhardconfdata.setAtmHardConfPosition37("0");
		atmHardConfDataRepository.save(atmhardconfdata);
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<AtmTerminal> updateEmployee(@PathVariable(value = "id") String ateId,
			@Valid @RequestBody AtmTerminal atmTerminalUpdate) throws ResourceNotFoundException {
		logger.info(atmTerminalUpdate.toString());
		AtmTerminal atmTerminal = atmTerminalRepository.findByAteId(ateId);
		atmTerminal.setAteLibelle(atmTerminalUpdate.getAteLibelle());
		atmTerminal.setPORT(atmTerminalUpdate.getPORT());
		atmTerminal.setModel(atmTerminalUpdate.getModel());
		atmTerminal.setMarque(atmTerminalUpdate.getMarque());
		atmTerminal.setMerchantCode(atmTerminalUpdate.getMerchantCode());
		atmTerminal.setIpAdresse(atmTerminalUpdate.getIpAdresse());
		atmTerminal.setAtmConNum(atmTerminalUpdate.getAtmConNum());
		
		MccList mcc = mccRepository.findByMccListId("6011");
		atmTerminal.setMccCode(mcc.getMccCode());
		//atmTerminal = atmTerminalUpdate;
//atmTerminal.setAteId(ateId);
		final AtmTerminal updatedEmployee = atmTerminalRepository.save(atmTerminal);
		return ResponseEntity.ok(updatedEmployee);
	}

	@GetMapping("getAllAtmsTerminal")
	public List<AtmTerminalDisplay> getAllAtmsTerminal() {
		List<AtmTerminalDisplay> list = new ArrayList<AtmTerminalDisplay>();
		for (AtmTerminal element : atmTerminalRepository.findAll()) {

			MccList mcc = mccRepository.findByMccCode(element.getMccCode());
			AtmModel atmModel = atmModelRepository.findById(element.getModel()).get();
			AtmMarque atmMarque = atmMarqueRepository.findById(element.getMarque()).get();
			AtmConfiguration atmConfig = atmConfigurationRepository.findByAtmConNum(element.getAtmConNum()).get();
			// AtmConfiguration atmConfig=
			// atmConfigurationRepository.findById(element.getAtmConNum()).get();
			AtmTerminalDisplay obj = new AtmTerminalDisplay();
			obj.setAteId(element.getAteId());
			obj.setMcc(mcc.getMccListLibelle());
			obj.setMerchantCode(element.getMerchantCode());
			obj.setTerstatCode(element.getTerstatCode());
			obj.setAtmConNum(atmConfig.getAmConfigLibelle());
			obj.setAteLibelle(element.getAteLibelle());
			obj.setZmkKey(element.getZmkKey());
			obj.setZmkKcv(element.getZmkKcv());
			obj.setTmkKey(element.getTmkKey());
			obj.setTmkKcv(element.getTmkKcv());
			obj.setTPK_KEY(element.getTPK_KEY());
			obj.setTPK_KCV(element.getTpk_kcv());
			obj.setIpAdresse(element.getIpAdresse());
			obj.setPortCon(element.getPortCon());
			obj.setEtatTerminal(element.getEtatTerminal());
			obj.setCapturedCardNumber(element.getCapturedCardNumber());
			obj.setEnvelopesDeposited(element.getEnvelopesDeposited());
			obj.setAtmTerminalTsn(element.getAtmTerminalTsn());
			obj.setModel(atmModel.getLibelle());
			obj.setMarque(atmMarque.getLibelle());
			obj.setPORT(element.getPORT());

			list.add(obj);

			// return atmTerminalRepository.findAll();
		}
		return list;
	}

//@PostMapping("getAllAtmsTerminalFiltred")
//public List<AtmTerminalDisplay> getAllAtmsTerminalFiltred(@RequestBody String ateId) {
//	List<AtmTerminalDisplay> list = new ArrayList<AtmTerminalDisplay>();
//    if(!ateId.equals("=")){
//    	
//    	for(AtmTerminal element:atmTerminalRepository.findAllByateId(ateId.trim()) ) {
//    		 MccList mcc= mccRepository.findByMccCode(element.getMccCode());
//		     AtmModel atmModel = atmModelRepository.findById(element.getModel()).get();
//		     AtmMarque atmMarque = atmMarqueRepository.findById(element.getMarque()).get();
//		 AtmConfiguration atmConfig= atmConfigurationRepository.findByAtmConNum(element.getAtmConNum()).get();
//		     // AtmConfiguration atmConfig= atmConfigurationRepository.findById(element.getAtmConNum()).get();
//		      AtmTerminalDisplay obj= new AtmTerminalDisplay();
//		        obj.setAteId(element.getAteId());
//		        obj.setMcc(mcc.getMccListLibelle());
//		        obj.setMerchantCode(element.getMerchantCode());
//		        obj.setTerstatCode(element.getTerstatCode());
//		        obj.setAtmConNum(atmConfig.getAmConfigLibelle());
//		        obj.setAteLibelle(element.getAteLibelle());
//		        obj.setZmkKey(element.getZmkKey());
//		        obj.setZmkKcv(element.getZmkKcv());
//		        obj.setTmkKey(element.getTmkKey());
//		        obj.setTmkKcv(element.getTmkKcv());
//		        obj.setTPK_KEY(element.getTPK_KEY());
//		        obj.setTPK_KCV(element.getTpk_kcv());
//		        obj.setIpAdresse(element.getIpAdresse());
//		        obj.setPortCon(element.getPortCon());
//		      obj.setEtatTerminal(element.getEtatTerminal());
//		        obj.setCapturedCardNumber(element.getCapturedCardNumber());
//		        obj.setEnvelopesDeposited(element.getEnvelopesDeposited());
//		      obj.setAtmTerminalTsn(element.getAtmTerminalTsn());
//		     obj.setModel(atmModel.getLibelle());
//		        obj.setMarque(atmMarque.getLibelle());
//		      obj.setPORT(element.getPORT());
//
//		      list.add(obj);
//		    
//    		
//    	}
//    	return list;
//    }else {
//    	
//    	for(AtmTerminal element:atmTerminalRepository.findAll() ) {
//   		 MccList mcc= mccRepository.findByMccCode(element.getMccCode());
//		     AtmModel atmModel = atmModelRepository.findById(element.getModel()).get();
//		     AtmMarque atmMarque = atmMarqueRepository.findById(element.getMarque()).get();
//		 AtmConfiguration atmConfig= atmConfigurationRepository.findByAtmConNum(element.getAtmConNum()).get();
//		     // AtmConfiguration atmConfig= atmConfigurationRepository.findById(element.getAtmConNum()).get();
//		      AtmTerminalDisplay obj= new AtmTerminalDisplay();
//		        obj.setAteId(element.getAteId());
//		        obj.setMcc(mcc.getMccListLibelle());
//		        obj.setMerchantCode(element.getMerchantCode());
//		        obj.setTerstatCode(element.getTerstatCode());
//		        obj.setAtmConNum(atmConfig.getAmConfigLibelle());
//		        obj.setAteLibelle(element.getAteLibelle());
//		        obj.setZmkKey(element.getZmkKey());
//		        obj.setZmkKcv(element.getZmkKcv());
//		        obj.setTmkKey(element.getTmkKey());
//		        obj.setTmkKcv(element.getTmkKcv());
//		        obj.setTPK_KEY(element.getTPK_KEY());
//		        obj.setTPK_KCV(element.getTpk_kcv());
//		        obj.setIpAdresse(element.getIpAdresse());
//		        obj.setPortCon(element.getPortCon());
//		      obj.setEtatTerminal(element.getEtatTerminal());
//		        obj.setCapturedCardNumber(element.getCapturedCardNumber());
//		        obj.setEnvelopesDeposited(element.getEnvelopesDeposited());
//		      obj.setAtmTerminalTsn(element.getAtmTerminalTsn());
//		     obj.setModel(atmModel.getLibelle());
//		        obj.setMarque(atmMarque.getLibelle());
//		      obj.setPORT(element.getPORT());
//
//		      list.add(obj);
//		    
//   		
//   	}
//   	return list;
//    	
//    	
//    }
//    	 
//}

	@PostMapping("reportingpdfatmterminal")
	public List<AtmTerminalDisplay> reportingpdfatmterminal(@RequestBody AtmTerminalFilter ateId) {
		List<AtmTerminalDisplay> list = new ArrayList<AtmTerminalDisplay>();

		for (AtmTerminal element : atmTerminalRepository.findAllByateId(ateId.getAteId().trim())) {
			MccList mcc = mccRepository.findByMccCode(element.getMccCode());
			AtmModel atmModel = new AtmModel();
			AtmMarque atmMarque = new AtmMarque();
			AtmConfiguration atmConfig = new AtmConfiguration();
			if (element.getModel() != null)
				atmModel = atmModelRepository.findById(element.getModel()).get();
			if (element.getMarque() != null)
				atmMarque = atmMarqueRepository.findById(element.getMarque()).get();

			if (element.getAtmConNum() != 0)
				atmConfig = atmConfigurationRepository.findByAtmConNum(element.getAtmConNum()).get();
			// AtmConfiguration atmConfig=
			// atmConfigurationRepository.findById(element.getAtmConNum()).get();
			// AtmConfiguration atmConfig=
			// atmConfigurationRepository.findById(element.getAtmConNum()).get();
			AtmTerminalDisplay obj = new AtmTerminalDisplay();
			obj.setAteId(element.getAteId() != null ? element.getAteId() : "");
			obj.setMcc(mcc.getMccListLibelle());
			obj.setMerchantCode(element.getMerchantCode());
			obj.setTerstatCode(element.getTerstatCode());
			obj.setAtmConNum(atmConfig.getAmConfigLibelle());
			obj.setAteLibelle(element.getAteLibelle() != null ? element.getAteLibelle() : "");
			obj.setZmkKey(element.getZmkKey() != null ? element.getZmkKey() : "");
			obj.setZmkKcv(element.getZmkKcv() != null ? element.getZmkKcv() : "");
			obj.setTmkKey(element.getTmkKey() != null ? element.getTmkKey() : "");
			obj.setTmkKcv(element.getTmkKcv() != null ? element.getTmkKcv() : "");
			obj.setTPK_KEY(element.getTPK_KEY() != null ? element.getTPK_KEY() : "");
			obj.setTPK_KCV(element.getTpk_kcv() != null ? element.getTpk_kcv() : "");
			obj.setIpAdresse(element.getIpAdresse() != null ? element.getIpAdresse() : "");
			obj.setPortCon(element.getPortCon() != null ? element.getPortCon() : "");
			obj.setEtatTerminal(element.getEtatTerminal() != null ? element.getEtatTerminal() : "");
			obj.setCapturedCardNumber(element.getCapturedCardNumber() != null ? element.getCapturedCardNumber() : "");
			obj.setEnvelopesDeposited(element.getEnvelopesDeposited() != null ? element.getEnvelopesDeposited() : "");
			obj.setAtmTerminalTsn(element.getAtmTerminalTsn() != null ? element.getAtmTerminalTsn() : "");
			obj.setModel(atmModel.getLibelle() != null ? atmModel.getLibelle() : "");
			obj.setMarque(atmMarque.getLibelle() != null ? atmMarque.getLibelle() : "");
			obj.setPORT(element.getPORT() != null ? element.getPORT() : "");

			list.add(obj);

		}
		return list;
	}

	@PostMapping("getAllAtmsTerminalFiltred")
	public Page<AtmTerminalDisplay> getAllAtmsTerminalFiltred(@RequestBody AtmTerminalFilter atmTerminalDisplay,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir) {
		Page<AtmTerminalDisplay> page2 = null;
		try {

			logger.info("filter =" + atmTerminalDisplay);

			List<Order> orders = new ArrayList<Order>();
			Order order1 = null;
			if (dir.equals("desc"))
				order1 = new Order(Sort.Direction.DESC, sortOn);
			else
				order1 = new Order(Sort.Direction.ASC, sortOn);
			orders.add(order1);

			List<AtmTerminalDisplay> list = new ArrayList<>();
			Page<AtmTerminal> terminals = atmTerminalRepository
					.findAllByateId(PageRequest.of(page, size, Sort.by(orders)), atmTerminalDisplay.getAteId());

			for (AtmTerminal element : atmTerminalRepository.findAllByateId(PageRequest.of(page, size, Sort.by(orders)),
					atmTerminalDisplay.getAteId()))

			{
				logger.info("atm num =" + element.getAteId());

				MccList mcc = mccRepository.findByMccCode(element.getMccCode());

				logger.info("mcc libelle =" + mcc.getMccListLibelle());
				AtmModel atmModel = new AtmModel();
				AtmMarque atmMarque = new AtmMarque();
				AtmConfiguration atmConfig = new AtmConfiguration();
				if (element.getModel() != null)
					atmModel = atmModelRepository.findById(element.getModel()).get();
				if (element.getMarque() != null)
					atmMarque = atmMarqueRepository.findById(element.getMarque()).get();

				if (element.getAtmConNum() != 0)
					logger.info("model code =" + atmModel.getModelCode());
				logger.info("marque code =" + atmMarque.getMarqueCode());

				atmConfig = atmConfigurationRepository.findByAtmConNum(element.getAtmConNum()).get();
				// AtmConfiguration atmConfig=
				// atmConfigurationRepository.findById(element.getAtmConNum()).get();
				logger.info("atm config=" + atmConfig.getAtmConNum());
				AtmTerminalDisplay obj = new AtmTerminalDisplay();
				obj.setAteId(element.getAteId() != null ? element.getAteId() : "");

				obj.setMcc(String.valueOf(mcc.getMccListId()));
				Merchant m = merchantRepository.getOne(element.getMerchantCode());
				obj.setMerchantId(m.getMerchantId());
				obj.setMerchantCode(element.getMerchantCode());
				obj.setTerstatCode(element.getTerstatCode());
				obj.setAtmConNum(atmConfig.getAmConfigLibelle());
				obj.setAteLibelle(element.getAteLibelle() != null ? element.getAteLibelle() : "");
				obj.setZmkKey(element.getZmkKey() != null ? element.getZmkKey() : "");
				obj.setZmkKcv(element.getZmkKcv() != null ? element.getZmkKcv() : "");
				obj.setTmkKey(element.getTmkKey() != null ? element.getTmkKey() : "");
				obj.setTmkKcv(element.getTmkKcv() != null ? element.getTmkKcv() : "");
				obj.setTPK_KEY(element.getTPK_KEY() != null ? element.getTPK_KEY() : "");
				obj.setTPK_KCV(element.getTpk_kcv() != null ? element.getTpk_kcv() : "");
				obj.setIpAdresse(element.getIpAdresse() != null ? element.getIpAdresse() : "");
				obj.setPortCon(element.getPortCon() != null ? element.getPortCon() : "");
				obj.setEtatTerminal(element.getEtatTerminal() != null ? element.getEtatTerminal() : "");
				obj.setCapturedCardNumber(
						element.getCapturedCardNumber() != null ? element.getCapturedCardNumber() : "");
				obj.setEnvelopesDeposited(
						element.getEnvelopesDeposited() != null ? element.getEnvelopesDeposited() : "");
				obj.setAtmTerminalTsn(element.getAtmTerminalTsn() != null ? element.getAtmTerminalTsn() : "");
				obj.setModel(atmModel.getLibelle() != null ? atmModel.getLibelle() : "");
				obj.setMarque(atmMarque.getLibelle() != null ? atmMarque.getLibelle() : "");
				obj.setPORT(element.getPORT() != null ? element.getPORT() : "");
				list.add(obj);

			}
			// page2 = new PageImpl<>(PageRequest.of(page, size,Sort.by(orders)), list);
			page2 = new PageImpl<>(list, PageRequest.of(page, size), terminals.getTotalElements());

			return page2;

		} catch (Exception e) {
			e.printStackTrace();
			return page2;

		}
	}

	@GetMapping("/atmTerminal/{id}")
	public ResponseEntity<AtmTerminal> getEmployeeById(@PathVariable(value = "id") String userId) {
		
		AtmTerminal atmTerminal = atmTerminalRepository.findByAteId(userId);
		
		logger.info(atmTerminal.toString());
		return ResponseEntity.ok().body(atmTerminal);
	}

	@GetMapping("getAtmsTerminal")
	public List<AtmDisplay> getAtmsTerminal() {
		List<AtmTerminal> atmTerminals = atmTerminalRepository.findAll();

		List<AtmDisplay> atmDisplays = new ArrayList<>();

		for (AtmTerminal a : atmTerminals) {
			AtmConfiguration atmConfiguration = atmConfigurationRepository.findByAtmConNum(a.getMccCode()).get();
			MccList mccList = mccRepository.findByMccCode(a.getMccCode());
			Merchant merchant = merchantRepository.findByMerchantCode(a.getMerchantCode());
			AtmMarque atmMarque = atmMarqueRepository.findById(a.getMarque()).get();
			AtmModel atmModel = atmModelRepository.findById(a.getModel()).get();
			AtmDisplay atmDisplay = new AtmDisplay(a.getAteId(), atmConfiguration.getAmConfigLibelle(),
					mccList.getMccListLibelle(), a.getAteLibelle(), a.getIpAdresse(), a.getEtatTerminal(),
					merchant.getMerchantId(), atmModel.getLibelle(), atmMarque.getLibelle());
			logger.info(atmDisplay.toString());
			atmDisplays.add(atmDisplay);

		}
		return atmDisplays;
	}

	@PostMapping("getAllAtmMerchants")
	public Page<MerchantListDisplay> getAllAtmMerchants(@RequestBody RequestFilterMerchant requestFilterMerchant,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size

	) {

		Page<Merchant> merchants = null;
		if (requestFilterMerchant.getAccountNumber().equals("")) {
			merchants = merchantRepository.getPageAtmMerchant(PageRequest.of(page, size),
					requestFilterMerchant.getNameMerchant());

		} else {
			merchants = merchantRepository.getPageAtmMerchant(PageRequest.of(page, size),
					requestFilterMerchant.getNameMerchant(),
					Integer.parseInt(requestFilterMerchant.getAccountNumber()));

		}

		List<MerchantListDisplay> merchantListDisplays = new ArrayList<>();
		for (Merchant m : merchants) {

			MerchantListDisplay merchantListDisplay = new MerchantListDisplay();
			merchantListDisplay.setAdresse(m.getAddress());
			merchantListDisplay.setCity(m.getCity());
			merchantListDisplay.setCodeZip(m.getCodeZip());
			merchantListDisplay.setCountry(m.getCountry());
			merchantListDisplay.setCreationDate(m.getCreationDate());
			merchantListDisplay.setMerchantId(m.getMerchantId());
			merchantListDisplay.setMerchantCode(m.getMerchantCode());
			merchantListDisplay.setPhone(m.getPhone());
			merchantListDisplay.setNameMerchant(m.getMerchantLibelle());
			merchantListDisplay.setMerchantstatus(Integer.parseInt(m.getMerchantStatus()));

			Account account = accountRepository.getOne(m.getAccount());

			merchantListDisplay.setAccountNumber(account.getAccountNum());
			merchantListDisplay.setOffshore(m.getOffshore());
			MerchantStatus ms = merchantStatusRepository.getOne(Integer.parseInt(m.getMerchantStatus()));
			merchantListDisplay.setEtatMerchant(ms.getLibelle());

			merchantListDisplays.add(merchantListDisplay);

		}
		Page<MerchantListDisplay> pageDisplay = new PageImpl<>(merchantListDisplays, PageRequest.of(page, size),
				merchants.getTotalElements());
		return pageDisplay;
	}

	@PostMapping("AddAtmMerchant")
	public ResponseEntity<String> AddAtmMerchant(@RequestBody MerchantListDisplay merchant) {

		AgenceAdministration agence = agenceAdministrationRepository
				.findByIdAgence(Integer.parseInt(merchant.getAgence())).get();

		String rib = "35" + agence.getInitial() + merchant.getAccountNumber();
		Merchant m = new Merchant();

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
			m.setMerchantId(merchant.getMerchantId());
			m.setDaira(merchant.getDaira());
			m.setAccount(account.getAccountCode());
			m.setRevenue(merchant.getRevenue());
			m.setMcc("6011");
			m.setMerchantStatus("1");
			m.setCreationDate(new Date());

			merchantRepository.save(m);
		} else {
			logger.info("account not founded");
			System.out.println("account not founded");

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
			m.setMerchantId(merchant.getMerchantId());
			m.setRevenue(merchant.getRevenue());
			m.setMcc("6011");
			m.setMerchantStatus("1");
			m.setCreationDate(new Date());

			m = merchantRepository.save(m);

			Account acc = new Account(null, rib, 1, Integer.toString(m.getMerchantCode()), rib, m.getMerchantLibelle(),
					BigInteger.valueOf(0), BigInteger.valueOf(0), BigInteger.valueOf(0), BigInteger.valueOf(0), "012",
					new Date(), 5, agence.getCodeAgence());
			acc = accountRepository.save(acc);

			m.setAccount(acc.getAccountCode());
			merchantRepository.save(m);

		}

		return ResponseEntity.ok().body(gson.toJson("merchant créé"));

	}

	@GetMapping("getOneMerchantAtm/{id}")
	MerchantListDisplay getOneMerchantAtm(@PathVariable(value = "id") Integer merchantCode) {
		// Merchant m = merchantRepository.findById(merchantCode).get();
		System.out.println(merchantCode);
		MerchantListDisplay md = new MerchantListDisplay();

		Merchant m = merchantRepository.getOne(merchantCode);
		md.setOffshore(m.getOffshore());
		md.setMerchantLibelle(m.getMerchantLibelle());
		md.setCity(m.getCity());
		md.setCodeZip(m.getCodeZip());
		md.setCountry(m.getCountry());
		md.setAddress(m.getAddress());
		md.setPhone(m.getPhone());
		md.setCommune(m.getCommune());
		md.setDaira(m.getDaira());
		md.setEmail(m.getEmail());
		Account account = accountRepository.getOne(m.getAccount());

		AgenceAdministration agence = agenceAdministrationRepository.findByIdAgence(account.getIdAgence()).get();
		md.setAgence(agence.getInitial());

		String accountNum = account.getAccountNum().substring(7, account.getAccountNum().length());
		md.setAccountNumber(accountNum);

		md.setMerchantId(m.getMerchantId());
		System.out.println("phone " + md.getPhone());
		return md;

	}

	@PostMapping("editMerchantAtm/{id}")
	public ResponseEntity<String> editMerchantAtm(@PathVariable(value = "id") Integer merchantCode,
			@RequestBody MerchantListDisplay merchant) {
		AgenceAdministration agence = agenceAdministrationRepository
				.findByIdAgence(Integer.parseInt(merchant.getAgence())).get();

		String rib = "35" + agence.getInitial() + merchant.getAccountNumber();
		Merchant m = merchantRepository.getOne(merchantCode);
		if (accountRepository.existsByAccountNum(rib)) {
			Account account = accountRepository.findByAccountNum(rib);
			logger.info("account founded");

			m.setMerchantLibelle(merchant.getMerchantLibelle());
			m.setMerchantId(merchant.getMerchantId());
			m.setCity(merchant.getCity());
			m.setCodeZip(merchant.getCodeZip());
			m.setCountry(merchant.getCountry());
			m.setAddress(merchant.getAddress());
			m.setPhone(merchant.getPhone());
			m.setCommune(merchant.getCommune());
			m.setDaira(merchant.getDaira());
			m.setEmail(merchant.getEmail());
			m.setAccount(account.getAccountCode());
			merchantRepository.save(m);

		} else {
			logger.info("account not founded");
			m.setMerchantLibelle(merchant.getMerchantLibelle());
			m.setMerchantId(merchant.getMerchantId());
			m.setCity(merchant.getCity());
			m.setCodeZip(merchant.getCodeZip());
			m.setCountry(merchant.getCountry());
			m.setAddress(merchant.getAddress());
			m.setPhone(merchant.getPhone());
			m.setCommune(merchant.getCommune());
			m.setDaira(merchant.getDaira());
			m.setEmail(merchant.getEmail());
			merchantRepository.save(m);
			Account acc = new Account(null, rib, 1, Integer.toString(m.getMerchantCode()), rib, m.getMerchantLibelle(),
					BigInteger.valueOf(0), BigInteger.valueOf(0), BigInteger.valueOf(0), BigInteger.valueOf(0), "208",
					new Date(), 5, agence.getCodeAgence());
			acc = accountRepository.save(acc);

			m.setAccount(acc.getAccountCode());
			merchantRepository.save(m);

		}

		return ResponseEntity.accepted().body(gson.toJson("update merchant successfully!"));

	}

	@PutMapping("activateAtmMerchant/{id}")
	public ResponseEntity<String> activateAtmMerchant(@PathVariable(value = "id") Integer idPos) {

		Merchant atmMerchant = merchantRepository.getOne(idPos);
		atmMerchant.setMerchantStatus("1");
		merchantRepository.save(atmMerchant);

		return ResponseEntity.ok().body(gson.toJson("AtmMerchant Active"));
	}

	@PutMapping("desactivateAtmMerchant/{id}")
	public ResponseEntity<String> desactivateAtmMerchant(@PathVariable(value = "id") Integer idPos) {

		Merchant atmMerchant = merchantRepository.getOne(idPos);
		atmMerchant.setMerchantStatus("3");
		merchantRepository.save(atmMerchant);

		return ResponseEntity.ok().body(gson.toJson("AtmMerchant Désactive"));
	}

	@GetMapping("getAllMerchants")
	public List<Merchant> getAllMerchants() {
		return merchantRepository.getMerchant6011();
	}

	@GetMapping("/atmHardFitnessMonitoring/{id}")
	public ResponseEntity<AtmMonitoring> atmHardFitnessMonitoring(@PathVariable(value = "id") String atmHardFitnessId)
			throws ResourceNotFoundException {
		AtmHardFitness atmHardFitness = atmHardFitnessRepository.getOne(atmHardFitnessId);
		AtmMonitoring a = new AtmMonitoring();

		if (Integer.parseInt(atmHardFitness.getAtmHardFitnessBiometricCapture()) != 0) {

		} else if (Integer.parseInt(atmHardFitness.getAtmHardFitnessBiometricCapture()) != 0) {

		}

		return ResponseEntity.ok().body(a);
	}

}
