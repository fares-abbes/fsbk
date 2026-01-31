package com.mss.backOffice.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Throwables;
import com.mss.backOffice.request.AddFileRequest;
import com.mss.unified.entities.BatchesFC;
import com.mss.unified.entities.BkmvtiFransaBank;
import com.mss.unified.entities.DayOperationFransaBank;
import com.mss.unified.entities.HistoriqueDayFransaBank;
import com.mss.unified.entities.MvbkConf;
import com.mss.unified.entities.MvbkSettlement;
import com.mss.unified.entities.UAP040IN;
import com.mss.unified.entities.UAP040INHistory;
import com.mss.unified.entities.UAP040INR;
import com.mss.unified.entities.UAP040INRHistory;
import com.mss.unified.entities.UAP050FransaBank;
import com.mss.unified.entities.UAP050FransaBankHistory;
import com.mss.unified.entities.UAP050IN;
import com.mss.unified.entities.UAP050INHistory;
import com.mss.unified.entities.UAP050INR;
import com.mss.unified.entities.UAP050INRHistory;
import com.mss.unified.entities.UAP050RFransaBank;
import com.mss.unified.entities.UAP050RFransaBankHistory;
import com.mss.unified.entities.UAP051FransaBank;
import com.mss.unified.entities.UAP051HistoryFransaBank;
import com.mss.unified.entities.UAP051IN;
import com.mss.unified.entities.UAP051INHistory;
import com.mss.unified.entities.UAP051INR;
import com.mss.unified.entities.UAP051INRHistory;
import com.mss.unified.entities.UAP051RFransaBank;
import com.mss.unified.entities.UAP051RHistoryFransaBank;
import com.mss.unified.entities.UAPFransaBank;
import com.mss.unified.entities.UAPFransaBankHistory;
import com.mss.unified.entities.UAPFransaBankR;
import com.mss.unified.entities.UAPFransaBankRHistory;
import com.mss.unified.entities.User;
import com.mss.unified.entities.dayOperationReglement;
import com.mss.unified.repositories.BatchesFFCRepository;
import com.mss.unified.repositories.BkmvtiFransaBankRepository;
import com.mss.unified.repositories.DayOperationFransaBankRepository;
import com.mss.unified.repositories.HistoriqueDayFransaBankRepository;
import com.mss.unified.repositories.MvbkConfigRepository;
import com.mss.unified.repositories.MvbkSettlementRepository;
import com.mss.unified.repositories.UAP040INFransaBankHistoryRepository;
import com.mss.unified.repositories.UAP040INFransaBankRepository;
import com.mss.unified.repositories.UAP040INRFransaBankHistoryRepository;
import com.mss.unified.repositories.UAP040INRFransaBankRepository;
import com.mss.unified.repositories.UAP050FransaBankHistoryRepository;
import com.mss.unified.repositories.UAP050FransaBankRepository;
import com.mss.unified.repositories.UAP050INFransaBankHistoryRepository;
import com.mss.unified.repositories.UAP050INFransaBankRepository;
import com.mss.unified.repositories.UAP050INRFransaBankHistoryRepository;
import com.mss.unified.repositories.UAP050INRFransaBankRepository;
import com.mss.unified.repositories.UAP050RFransaBankHistoryRepository;
import com.mss.unified.repositories.UAP050RFransaBankRepository;
import com.mss.unified.repositories.UAP051FransaBankHistoryRepository;
import com.mss.unified.repositories.UAP051FransaBankRepository;
import com.mss.unified.repositories.UAP051INFransaBankHistoryRepository;
import com.mss.unified.repositories.UAP051INFransaBankRepository;
import com.mss.unified.repositories.UAP051INRFransaBankHistoryRepository;
import com.mss.unified.repositories.UAP051INRFransaBankRepository;
import com.mss.unified.repositories.UAP051RFransaBankHistoryRepository;
import com.mss.unified.repositories.UAP051RFransaBankRepository;
import com.mss.unified.repositories.UAPFransaBankHistoryRepository;
import com.mss.unified.repositories.UAPFransaBankRHistoryRepository;
import com.mss.unified.repositories.UAPFransaBankRRepository;
import com.mss.unified.repositories.UAPFransaBankRepository;
import com.mss.unified.repositories.UserRepository;
import com.mss.unified.repositories.dayOperationReglementRepository;
import com.mss.unified.repositories.OpeningDayRepository;

@RestController
@RequestMapping("ReglementGlobal")
public strictfp  class ReglementGlobalController {
	private static final Logger logger = LoggerFactory.getLogger(ReglementGlobalController.class);
	@Autowired
	OpeningDayRepository openedDayRepo;
	@Autowired
	BkmvtiFransaBankRepository bkmRepo;
	@Autowired
	BatchesFFCRepository batchesFFCRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
	@Autowired
	public MvbkConfigRepository mvbkConfigR;
	@Autowired
	UAPFransaBankRepository uAPFransaBankRepository;
	@Autowired
	UAP050FransaBankRepository uAP050FransaBankRepository;
	@Autowired
	UAP051FransaBankRepository uAP051FransaBankRepository;
	@Autowired
	UAP050RFransaBankRepository uAP050RFransaBankRepository;
	@Autowired
	UAP051RFransaBankRepository uAP051RFransaBankRepository;
	@Autowired
	UAP051INFransaBankRepository uAP051InFransaBankRepository;
	@Autowired
	UAP050FransaBankHistoryRepository uAP050FransaBankHistoryRepo;
	@Autowired
	UAP050RFransaBankHistoryRepository uAP050RFransaBankHistoryRepo;
	@Autowired
	UAP050INFransaBankHistoryRepository uAP050INFransaBankHistoryRepo;
	@Autowired
	UAP051INFransaBankHistoryRepository uAP051INFransaBankHistoryRepo;
	@Autowired
	UAP050INFransaBankRepository uAP050InFransaBankRepository;
	@Autowired
	UAP040INFransaBankRepository uAP040InFransaBankRepository;
	@Autowired
	UAP051INRFransaBankHistoryRepository uAP051INRFransaBankHistoryRepo;
	@Autowired
	UAP050INRFransaBankHistoryRepository uAP050INRFransaBankHistoryRepo;
	@Autowired
	UAP040INRFransaBankHistoryRepository uAP040INRFransaBankHistoryRepo;
	@Autowired
	UAP050INRFransaBankRepository uAP050InRFransaBankRepository;
	@Autowired
	UAP040INRFransaBankRepository uAP040InRFransaBankRepository;
	@Autowired
	UAP051INRFransaBankRepository uAP051InRFransaBankRepository;
	@Autowired
	MvbkSettlementRepository mvbkSettlementRepository;

	@Autowired
	UAP040INFransaBankHistoryRepository uAP040INFransaBankHistoryRepo;
	@Autowired
	public dayOperationReglementRepository operationRepo;
	@Autowired
	public HistoriqueDayFransaBankRepository historiqueRepo;
	@Autowired
	UAPFransaBankRRepository UAPFransaBankRRep;
	@Autowired
	UAPFransaBankRepository UAPFransaBankRep;
	@Autowired
	UAPFransaBankHistoryRepository uAPFransaBankHistoryRepository;
	@Autowired
	UAPFransaBankRHistoryRepository uAPFransaBankRHistoryRepository;
	@Autowired
	UAP051FransaBankHistoryRepository uAP051HistoryFransaBank;
	@Autowired
	UAP051RFransaBankHistoryRepository uAP051RHistoryFransaBank;
	@Autowired
	DayOperationFransaBankRepository dayOperationRepo;

	public void validFile() throws IOException, ParseException {
		BatchesFC batch = batchesFFCRepository.findByKey("ReglementGlobal").get();
		batch.setBatchLastExcution(batch.getBatchEndDate());
		batch.setBatchStatus(0);
		batch.setBatchDate(new Date());
		batch.setError(null);
		batch.setErrorStackTrace(null);
		batchesFFCRepository.saveAndFlush(batch);
		SimpleDateFormat newFormat = new SimpleDateFormat("yyyyMMdd");
		String today = newFormat.format(new Date());
		HashSet<String> dates = new HashSet<String>();
		List<String> elements040 = uAPFransaBankRepository.findDistinctRegDates(today);
		List<String> elements050 = uAP050FransaBankRepository.findDistinctRegDates(today);
		List<String> elements051 = uAP051FransaBankRepository.findDistinctRegDates(today);
		List<String> elements040In = uAP040InFransaBankRepository.findDistinctRegDates(today);
		List<String> elements050In = uAP050InFransaBankRepository.findDistinctRegDates(today);
		List<String> elements051In = uAP051InFransaBankRepository.findDistinctRegDates(today);
		dates.addAll(elements040);
		dates.addAll(elements050);
		dates.addAll(elements051);
		dates.addAll(elements040In);
		dates.addAll(elements050In);
		dates.addAll(elements051In);
		
			for (String thedate : dates) {
				today = thedate;
				List<MvbkConf> mvks = mvbkConfigR.findByCategorie(537);
				FileRequest.print("mvk size" + mvks.size(), FileRequest.getLineNumber());
				String dateFormat = convertDate(today, "yyyyMMdd", "ddMMyy");
				logger.info("date reg" + dateFormat);
				/* cra */
				long amountPositif040 = uAPFransaBankRepository.getTotalAmountPositif040(today) == null ? 0
						: uAPFransaBankRepository.getTotalAmountPositif040(today);
				long amountPositif050 = uAP050FransaBankRepository.getTotalAmountPositif050(today) == null ? 0
						: uAP050FransaBankRepository.getTotalAmountPositif050(today);
				long amountPositif051 = uAP051FransaBankRepository.getTotalAmountPositif051(today) == null ? 0
						: uAP051FransaBankRepository.getTotalAmountPositif051(today);
				long amountNegatif040 = UAPFransaBankRRep.getTotalAmountNegatif(today) == null ? 0
						: UAPFransaBankRRep.getTotalAmountNegatif(today);
				long amountNegatif050 = uAP050RFransaBankRepository.getTotalAmountNegatif050(today) == null ? 0
						: uAP050RFransaBankRepository.getTotalAmountNegatif050(today);
				long amountNegatif051 = uAP051RFransaBankRepository.getTotalAmountNegatif051(today) == null ? 0
						: uAP051RFransaBankRepository.getTotalAmountNegatif051(today);
				logger.info("amountPositif040=>{}", amountPositif040);
				logger.info("amountPositif050=>{}", amountPositif050);
				logger.info("amountPositif051=>{}", amountPositif051);
				logger.info("amountNegatif040=>{}", amountNegatif040);
				logger.info("amountNegatif050=>{}", amountNegatif050);
				logger.info("amountPositif051=>{}", amountPositif051);

				/* cro */
				long amountPositif040In = uAP040InFransaBankRepository.getTotalAmountPositif040In(today) == null ? 0
						: uAP040InFransaBankRepository.getTotalAmountPositif040In(today);
				long amountPositif050In = uAP050InFransaBankRepository.getTotalAmountPositif050In(today) == null ? 0
						: uAP050InFransaBankRepository.getTotalAmountPositif050In(today);
				long amountPositif051In = uAP051InFransaBankRepository.getTotalAmountPositif050In(today) == null ? 0
						: uAP051InFransaBankRepository.getTotalAmountPositif050In(today);
				long amountNegatif040In = uAP040InRFransaBankRepository.getTotalAmountNegatif(today) == null ? 0
						: uAP040InRFransaBankRepository.getTotalAmountNegatif(today);
				long amountNegatif050In = uAP050InRFransaBankRepository.getTotalAmountNegatif(today) == null ? 0
						: uAP050InRFransaBankRepository.getTotalAmountNegatif(today);
				long amountNegatif051In = uAP051InRFransaBankRepository.getTotalAmountNegatif(today) == null ? 0
						: uAP051InRFransaBankRepository.getTotalAmountNegatif(today);
				logger.info("amountPositif040In=>{}", amountPositif040In);
				logger.info("amountPositif050In=>{}", amountPositif050In);
				logger.info("amountPositif051In=>{}", amountPositif051In);
				logger.info("amountNegatif040In=>{}", amountNegatif040In);
				logger.info("amountNegatif050In=>{}", amountNegatif050In);
				logger.info("amountNegatif051In=>{}", amountNegatif051In);
 
				/* cro */
				long difference040 = amountPositif040 - amountNegatif040 - amountPositif040In+amountNegatif040In;
				long difference050 = amountPositif050 - amountNegatif050 - amountPositif050In+amountNegatif050In;

				/* pour le 51 la formule est CRO accpted */
				long difference051 = amountPositif051In-amountNegatif051In - (amountPositif051 - amountNegatif051);
				logger.info("difference040=>{}", difference040);
				logger.info("difference050=>{}", difference050);
				logger.info("difference051=>{}", difference051);

				long differenceAllerRetour = difference040 + difference050 + difference051;
				logger.info("differenceAllerRetour=>{}", differenceAllerRetour);
				logger.info("bkmRepo.getLastNumIndex()" + bkmRepo.getLastNumIndex());
				String sequenceNumber = bkmRepo.getLastNumIndex() == null ? "0" : bkmRepo.getLastNumIndex();
				try {
					String number =String.format("%06d", Integer.parseInt(sequenceNumber));
				}catch (Exception e) {
					sequenceNumber="0";
				}
				List<BkmvtiFransaBank> bkms = new ArrayList<BkmvtiFransaBank>();
				int length040 = dayOperationRepo.uap040Length(today);
				int length050 = dayOperationRepo.uap050Length(today);
				int length051 = dayOperationRepo.uap051Length(today);
				int length040In = dayOperationRepo.uap040InLength(today);
				int length050In = dayOperationRepo.uap050InLength(today);
				int length051In = dayOperationRepo.uap050InLength(today);
				if (length050 != 0 || length050 != 0 || length051 != 0 || length040In != 0 || length050In != 0
						|| length051In != 0) {

					for (MvbkConf mvk : mvks) {
						BkmvtiFransaBank bkmvtiFransaBank = new BkmvtiFransaBank();
						bkmvtiFransaBank.setNumEvenement(String.format("%06d", Integer.parseInt(sequenceNumber)));
						if (differenceAllerRetour > 0) {
							bkmvtiFransaBank.setMontant(getAmountFormat(differenceAllerRetour));
							bkmvtiFransaBank.setSens(mvk.getSigne());
						} else {
							bkmvtiFransaBank.setMontant(getAmountFormat(Math.abs(differenceAllerRetour)));
							bkmvtiFransaBank.setSens(mvk.getSigne().equals("D") ? "C" : "D");
						}
						bkmvtiFransaBank.setNumCompte(mvk.getAccount());
						bkmvtiFransaBank.setAgenceEmettrice(mvk.getCodeAgence());
						bkmvtiFransaBank.setCodeAgenceSaisie(mvk.getCodeAgence());
						bkmvtiFransaBank.setAgenceDestinatrice(mvk.getCodeAgence());
						bkmvtiFransaBank.setAgence(mvk.getCodeAgence());
						bkmvtiFransaBank.setChapitreComptable(mvk.getAccount().substring(0, 6));
						String lib = mvk.getLibelle() + dateFormat;
						bkmvtiFransaBank.setLibelle(lib + getSpace(40 - lib.length()));
						bkmvtiFransaBank.setIdentification(mvk.getIdentification());
						sameData(bkmvtiFransaBank, today, mvk);
						bkms.add(bkmvtiFransaBank);

					}
				}
				bkmvtiFransaBankRepository.saveAll(bkms);
				FileRequest.print(bkms.toString(), FileRequest.getLineNumber());
			}
			manageOldData();
			batchesFFCRepository.updateFinishBatch("ReglementGlobal", 1, new Date());


	}

	public void deleteDayOperation() {
		// archive day operation
		List<DayOperationFransaBank> dayOperation = dayOperationRepo.findAll();

		List<HistoriqueDayFransaBank> B = dayOperation.stream().map(developer -> {
			HistoriqueDayFransaBank historique = new HistoriqueDayFransaBank();
			try {
				PropertyUtils.copyProperties(historique, developer);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return historique;
		}).collect(Collectors.toList());

		historiqueRepo.saveAll(B);
		dayOperationRepo.deleteAll(dayOperation);
	}

	public void manageOldData() {
		SimpleDateFormat newFormat = new SimpleDateFormat("yyyyMMdd");
		String today = newFormat.format(new Date());

		// delete uap 40
		List<UAPFransaBank> uaps = UAPFransaBankRep.findByDateReglement(today);
		List<UAPFransaBankHistory> historyUAP = uaps.stream().map(developer -> {

			UAPFransaBankHistory historyUap = new UAPFransaBankHistory();
			try {
				PropertyUtils.copyProperties(historyUap, developer);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return historyUap;

		}).collect(Collectors.toList());
		FileRequest.print("here ", FileRequest.getLineNumber());
		uAPFransaBankHistoryRepository.saveAll(historyUAP);
		UAPFransaBankRep.deleteAll(uaps);

		// delete uap 50
		List<UAP050FransaBank> uaps050 = uAP050FransaBankRepository.findByDateReglement(today);
		List<UAP050FransaBankHistory> historyUAP050 = uaps050.stream().map(developer -> {

			UAP050FransaBankHistory historyUap = new UAP050FransaBankHistory();
			try {

				PropertyUtils.copyProperties(historyUap, developer);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return historyUap;

		}).collect(Collectors.toList());
		uAP050FransaBankHistoryRepo.saveAll(historyUAP050);
		uAP050FransaBankRepository.deleteAll(uaps050);

		// delete uap 51
		List<UAP051FransaBank> uaps051 = uAP051FransaBankRepository.findByDateReglement(today);
		List<UAP051HistoryFransaBank> historyUAP051 = uaps051.stream().map(developer -> {
			UAP051HistoryFransaBank historyUap = new UAP051HistoryFransaBank();
			try {
				PropertyUtils.copyProperties(historyUap, developer);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return historyUap;

		}).collect(Collectors.toList());
		uAP051HistoryFransaBank.saveAll(historyUAP051);
		uAP051FransaBankRepository.deleteAll(uaps051);
		
		// delete uap 40
		List<UAPFransaBankR> uapsR = UAPFransaBankRRep.findByDateReglement(today);
		List<UAPFransaBankRHistory> historyUAPR = uapsR.stream().map(developer -> {
			
			UAPFransaBankRHistory historyRUap = new UAPFransaBankRHistory();
			try {
				PropertyUtils.copyProperties(historyRUap, developer);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return historyRUap;
			
		}).collect(Collectors.toList());
		FileRequest.print("here ", FileRequest.getLineNumber());
		uAPFransaBankRHistoryRepository.saveAll(historyUAPR);
		UAPFransaBankRRep.deleteAll(uapsR);
		
		// delete uap 50
		List<UAP050RFransaBank> uaps050R = uAP050RFransaBankRepository.findByDateReglement(today);
		List<UAP050RFransaBankHistory> historyUAP050R = uaps050R.stream().map(developer -> {
			
			UAP050RFransaBankHistory historyUap = new UAP050RFransaBankHistory();
			try {
				
				PropertyUtils.copyProperties(historyUap, developer);
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return historyUap;
			
		}).collect(Collectors.toList());
		uAP050RFransaBankHistoryRepo.saveAll(historyUAP050R);
		uAP050RFransaBankRepository.deleteAll(uaps050R);
		
		// delete uap 51
		List<UAP051RFransaBank> uaps051R = uAP051RFransaBankRepository.findByDateReglement(today);
		List<UAP051RHistoryFransaBank> historyUAP051R = uaps051R.stream().map(developer -> {
			UAP051RHistoryFransaBank historyUap = new UAP051RHistoryFransaBank();
			try {
				PropertyUtils.copyProperties(historyUap, developer);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return historyUap;
			
		}).collect(Collectors.toList());
		uAP051RHistoryFransaBank.saveAll(historyUAP051R);
		uAP051RFransaBankRepository.deleteAll(uaps051R);

		// delete uap 040 in
		List<UAP040IN> uaps040In = uAP040InFransaBankRepository.findReglementByDate(today);
		List<UAP040INHistory> historyUAP040In = uaps040In.stream().map(developer -> {

			UAP040INHistory historyUap = new UAP040INHistory();
			try {

				PropertyUtils.copyProperties(historyUap, developer);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return historyUap;

		}).collect(Collectors.toList());
		FileRequest.print("here ", FileRequest.getLineNumber());
		uAP040INFransaBankHistoryRepo.saveAll(historyUAP040In);
		uAP040InFransaBankRepository.deleteAll(uaps040In);

		// delete uap 050 in
		List<UAP050IN> uaps050In = uAP050InFransaBankRepository.findReglementByDate(today);
		List<UAP050INHistory> historyUAP050In = uaps050In.stream().map(developer -> {

			UAP050INHistory historyUap = new UAP050INHistory();
			try {

				PropertyUtils.copyProperties(historyUap, developer);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return historyUap;

		}).collect(Collectors.toList());
		FileRequest.print("here ", FileRequest.getLineNumber());
		uAP050INFransaBankHistoryRepo.saveAll(historyUAP050In);
		uAP050InFransaBankRepository.deleteAll(uaps050In);

		// delete uap 051 in
		List<UAP051IN> uaps051In = uAP051InFransaBankRepository.findReglementByDate(today);
		List<UAP051INHistory> historyUAP051In = uaps051In.stream().map(developer -> {

			UAP051INHistory historyUap = new UAP051INHistory();
			try {

				PropertyUtils.copyProperties(historyUap, developer);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return historyUap;

		}).collect(Collectors.toList());
		FileRequest.print("here ", FileRequest.getLineNumber());
		uAP051INFransaBankHistoryRepo.saveAll(historyUAP051In);
		uAP051InFransaBankRepository.deleteAll(uaps051In);
		// delete uap 040 in
		List<UAP040INR> uaps040InR = uAP040InRFransaBankRepository.findReglementByDate(today);
		List<UAP040INRHistory> historyUAP040InR = uaps040InR.stream().map(developer -> {

			UAP040INRHistory historyUap = new UAP040INRHistory();
			try {

				PropertyUtils.copyProperties(historyUap, developer);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return historyUap;

		}).collect(Collectors.toList());
		FileRequest.print("here ", FileRequest.getLineNumber());
		uAP040INRFransaBankHistoryRepo.saveAll(historyUAP040InR);
		uAP040InRFransaBankRepository.deleteAll(uaps040InR);

		// delete uap 050 in
		List<UAP050INR> uaps050InR = uAP050InRFransaBankRepository.findReglementByDate(today);
		List<UAP050INRHistory> historyUAP050InR = uaps050InR.stream().map(developer -> {

			UAP050INRHistory historyUap = new UAP050INRHistory();
			try {

				PropertyUtils.copyProperties(historyUap, developer);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return historyUap;

		}).collect(Collectors.toList());
		FileRequest.print("here ", FileRequest.getLineNumber());
		uAP050INRFransaBankHistoryRepo.saveAll(historyUAP050InR);
		uAP050InRFransaBankRepository.deleteAll(uaps050InR);

		// delete uap 051 in
		List<UAP051INR> uaps051InR = uAP051InRFransaBankRepository.findReglementByDate(today);
		List<UAP051INRHistory> historyUAP051InR = uaps051In.stream().map(developer -> {

			UAP051INRHistory historyUap = new UAP051INRHistory();
			try {

				PropertyUtils.copyProperties(historyUap, developer);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return historyUap;

		}).collect(Collectors.toList());
		FileRequest.print("here ", FileRequest.getLineNumber());
		uAP051INRFransaBankHistoryRepo.saveAll(historyUAP051InR);
		uAP051InRFransaBankRepository.deleteAll(uaps051InR);

		if (openedDayRepo.findDoneOpening(today).isPresent()) {
			openedDayRepo.delete(openedDayRepo.findDoneOpening(today).get());
		}

	}

	public String getAmountFormat(long amount) {

		String amountFormat = String.format("%018d,%02d", amount / 100, amount % 100);
		return amountFormat;
	}

	public String getSpace(int count)

	{

		String Space = "";

		for (int i = 0; i < count; i++)

			Space += " ";

		return Space;

	}

	public BkmvtiFransaBank sameData(BkmvtiFransaBank bkmvtiFransaBank, String date, MvbkConf mvk) {
		FileRequest.print("filling data", FileRequest.getLineNumber());

		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		String dateformat = convertDate(date, "yyyyMMdd", "dd/MM/yyyy");
		String dateformat2 = convertDate(date, "yyyyMMdd", "yyyyMMdd");
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving status !");
		}
		bkmvtiFransaBank
				.setCodeUtilisateur(user.get().getUserName() + getSpace(10 - user.get().getUserName().length()));
		bkmvtiFransaBank.setCodeDevice("208");
		bkmvtiFransaBank.setCodeOperation(mvk.getCodeOperation());
		bkmvtiFransaBank.setCodeService("9999");
		bkmvtiFransaBank.setExonerationcommission("N");
		bkmvtiFransaBank.setCodeDeviceOrigine("208");
		bkmvtiFransaBank.setCodeID("99000S");
		bkmvtiFransaBank.setCalculmouvementInteragence("N");
		bkmvtiFransaBank.setMouvementAgence("N");
		bkmvtiFransaBank.setTauxChange("1" + getSpace(6));
		bkmvtiFransaBank.setDateComptable(dateformat);
		bkmvtiFransaBank.setNatureTransaction("REGMON");
		bkmvtiFransaBank.setMontantOrigine(getSpace(19));
		bkmvtiFransaBank.setCleControleCompte(getSpace(2));
		bkmvtiFransaBank.setCodeEtat("VA");
		bkmvtiFransaBank.setRefDossier(getSpace(50));
		bkmvtiFransaBank.setPieceComptable("RB" + bkmvtiFransaBank.getCodeOperation() + dateformat2.substring(2));
		bkmvtiFransaBank.setIdentification(mvk.getIdentification());
		bkmvtiFransaBank.setDateValeur(dateformat);
		bkmvtiFransaBank.setReferanceLettrage(dateformat2);
		bkmvtiFransaBank.setNumPiece("RB" + dateformat2);
		return bkmvtiFransaBank;
	}

	public static String convertDate(String dateString, String inputFormat, String outputFormat) {
		// Parse the date string to a LocalDate object using the input format
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(inputFormat);
		LocalDate date = LocalDate.parse(dateString, inputFormatter);

		// Format the date to the desired output format
		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outputFormat);
		return date.format(outputFormatter);
	}

}
