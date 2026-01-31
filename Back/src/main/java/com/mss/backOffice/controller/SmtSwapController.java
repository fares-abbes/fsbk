package com.mss.backOffice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.mss.backOffice.services.ExecuteCommandService;
import com.mss.backOffice.services.PropertyService;
import com.mss.unified.entities.FileUsed;
import com.mss.unified.entities.Interfaces;
import com.mss.unified.entities.OpeningDay;
import com.mss.unified.repositories.BatchesFFCRepository;
import com.mss.unified.repositories.FileUsedRepository;
import com.mss.unified.repositories.InterfacesRepository;
import com.mss.unified.repositories.OpeningDayRepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("interfacesSwap")
public class SmtSwapController {
	private final Logger logger = LoggerFactory.getLogger(SmtSwapController.class);
	@Autowired
	private InterfacesRepository interfacesRepository;
	@Autowired
	private ExecuteCommandService executeCommandService;
	private static final Gson gson = new Gson();
	@Autowired
	BatchesFFCRepository batchRepo;

	@GetMapping("/getAll")
	private List<Interfaces> getAll() throws IOException {
		return interfacesRepository.findAll();
	}
	@Autowired
	PropertyService propertyService;
	@Autowired
	FileUsedRepository fUR;
	@Autowired
	OpeningDayRepository opdr;

	@GetMapping("/satimUp")
	private void satimUP() throws IOException {

		logger.info("Starting satim service........");
		// Runtime.getRuntime().exec("systemctl start satim.service");
		String result = executeCommandService.executeRemoteCommandSatim("sudo -S systemctl start satim.service");
		logger.info("exit-status........ {}", result);
		if (!result.equals("")) {

			Optional<Interfaces> interfaceSatim = interfacesRepository.findByLibelle("SATIM");

			if (interfaceSatim.isPresent()) {
				interfaceSatim.get().setLastStatus(interfaceSatim.get().getStatus());
				interfaceSatim.get().setStatus("Process up");
				interfaceSatim.get().setDateChangement(new Date());

				interfacesRepository.save(interfaceSatim.get());

			}
		}

	}

	@GetMapping("/satimDown")
	private void satimDOWN() throws IOException {

		logger.info("Stopping satim service........");

		// Runtime.getRuntime().exec("systemctl stop satim.service");
		String result = executeCommandService.executeRemoteCommandSatim("sudo -S systemctl stop satim.service");
		logger.info("exit-status........ {}", result);
		if (!result.equals("")) {
			Optional<Interfaces> interfaceSatim = interfacesRepository.findByLibelle("SATIM");

			if (interfaceSatim.isPresent()) {
				interfaceSatim.get().setLastStatus(interfaceSatim.get().getStatus());
				interfaceSatim.get().setStatus("Process down");
				interfaceSatim.get().setDateChangement(new Date());

				interfacesRepository.save(interfaceSatim.get());

			}
		}

	}

	@GetMapping("/hostUp")
	private void hostUP() throws IOException {
		logger.info("Starting host service........");
		// Runtime.getRuntime().exec("systemctl start hostFsbk.service");
		String result = executeCommandService.executeRemoteCommand("sudo -S systemctl start hostFsbk.service");
		logger.info("exit-status........ {}", result);
		if (!result.equals("")) {
			Optional<Interfaces> interfaceSMT = interfacesRepository.findByLibelle("HOST");

			if (interfaceSMT.isPresent()) {
				interfaceSMT.get().setLastStatus(interfaceSMT.get().getStatus());
				interfaceSMT.get().setStatus("Process up");
				interfaceSMT.get().setDateChangement(new Date());

				interfacesRepository.save(interfaceSMT.get());

			}
		}

	}

	@GetMapping("/hostDown")
	private void hostDOWN() throws IOException {
		logger.info("Stopping host service........");

		// Runtime.getRuntime().exec("systemctl stop hostFsbk.service");
		String result = executeCommandService.executeRemoteCommand("sudo -S systemctl stop hostFsbk.service");
		logger.info("exit-status........ {}", result);
		if (!result.equals("")) {
			Optional<Interfaces> interfaceSMT = interfacesRepository.findByLibelle("HOST");

			if (interfaceSMT.isPresent()) {
				interfaceSMT.get().setLastStatus(interfaceSMT.get().getStatus());
				interfaceSMT.get().setStatus("Process down");
				interfaceSMT.get().setDateChangement(new Date());

				interfacesRepository.save(interfaceSMT.get());

			}
		}

	}

	@GetMapping("/hsmUp")
	private void hsmUP() throws IOException {
		logger.info("Starting hsm service........");
		// Runtime.getRuntime().exec("systemctl start hsm.service");
		String result = executeCommandService.executeRemoteCommand("sudo -S systemctl start hsm.service");
		logger.info("exit-status........ {}", result);
		if (!result.equals("")) {
			Optional<Interfaces> interfaceSMT = interfacesRepository.findByLibelle("HSM");

			if (interfaceSMT.isPresent()) {
				interfaceSMT.get().setLastStatus(interfaceSMT.get().getStatus());
				interfaceSMT.get().setStatus("Process up");
				interfaceSMT.get().setDateChangement(new Date());

				interfacesRepository.save(interfaceSMT.get());

			}
		}

	}

	@GetMapping("/hsmDown")
	private void hsmDOWN() throws IOException {
		logger.info("Stopping hsm service........");

		// Runtime.getRuntime().exec("systemctl stop hsm.service");

		String result = executeCommandService.executeRemoteCommand("sudo -S systemctl stop hsm.service");
		logger.info("exit-status........ {}", result);
		Optional<Interfaces> interfaceSMT = interfacesRepository.findByLibelle("HSM");
		if (!result.equals("")) {
			if (interfaceSMT.isPresent()) {
				interfaceSMT.get().setLastStatus(interfaceSMT.get().getStatus());
				interfaceSMT.get().setStatus("Process down");
				interfaceSMT.get().setDateChangement(new Date());

				interfacesRepository.save(interfaceSMT.get());

			}
		}

	}

	@GetMapping("/getCra")
	private ResponseEntity<?> getCra() throws IOException {
		logger.info("Starting getCra service........");
		try {
			
			Runtime.getRuntime().exec("sh /home/fsbkatm/Documents/COMPENSATION/SCRIPT/GETCRA_new.sh");
			logger.info("OK");
			checkNBlignes(	propertyService.getCompensationfilePathCRA() 
					, "CRA", "CRA");
			return ResponseEntity.ok().body(gson.toJson("OK"));

		} catch (Exception e) {
			checkNBlignes(	propertyService.getCompensationfilePathCRA() 
					, "CRA", "CRA");
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("ERROR"));
		}

	}
	
	@GetMapping("/getCraRejet")
	private ResponseEntity<?> getCraRejet() throws IOException {
		logger.info("Starting getCra service........");
		try {
			
			Runtime.getRuntime().exec("sh /home/fsbkatm/Documents/COMPENSATION/SCRIPT/GETCRAREJ.sh");
			logger.info("OK");
			checkNBlignes(	propertyService.getCompensationfilePathCRA() 
					, "CRA", "CRA");
			return ResponseEntity.ok().body(gson.toJson("OK"));
			
		} catch (Exception e) {
			checkNBlignes(	propertyService.getCompensationfilePathCRA() 
					, "CRA", "CRA");
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("ERROR"));
		}
		
	}

	@GetMapping("/getCro")
	private ResponseEntity<?> getCro() throws IOException {
		logger.info("Starting getCro service........");
		try {
			checkNBlignes(	propertyService.getCompensationfilePath() 
					, "ORD", "ORD");
			Runtime.getRuntime().exec("sh /home/fsbkatm/Documents/COMPENSATION/SCRIPT/getCRO.sh");
			logger.info("OK");
			return ResponseEntity.ok().body(gson.toJson("OK"));

		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("ERROR"));
		}

	}

	@GetMapping("/sendORD")
	private ResponseEntity<?> sendORD() throws IOException {
		logger.info("Starting send ORD service........");
		try {
			Runtime.getRuntime().exec("sh /home/fsbkatm/Documents/COMPENSATION/SCRIPT/sendORD.sh");
			logger.info("OK");
			batchRepo.updateFinishBatch("SENDORD", 10, new Date());
			return ResponseEntity.ok().body(gson.toJson("OK"));

		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
			batchRepo.updateFinishBatch("SENDORD", -1, new Date());
			return ResponseEntity.badRequest().body(gson.toJson("ERROR"));
		}

	}

	@GetMapping("/sendLOT")
	private ResponseEntity<?> sendLOT() throws IOException {
		logger.info("Starting send lot service........");
		try {
			checkNBlignes(	propertyService.getCompensationfilePathLOT() 
, "LOT", "LOT");
			Runtime.getRuntime().exec("sh /home/fsbkatm/Documents/COMPENSATION/SCRIPT/sendLOT.sh");
			logger.info("OK");
			batchRepo.updateFinishBatch("SENDLOT", 10, new Date());
			Thread.sleep(60000);
			batchRepo.updateFinishBatch("SENDORD", -1, new Date());

			return ResponseEntity.ok().body(gson.toJson("OK"));

		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			logger.info("Exception is=>{}", stackTrace);
			batchRepo.updateFinishBatch("SENDLOT", -1, new Date());
			return ResponseEntity.badRequest().body(gson.toJson("ERROR"));
		}

	}

	public void checkNBlignes(String directoryPath, String filter,String type) {
		OpeningDay day = opdr.findAll().get(0);
		String IntegrationDay=day.getFileIntegration();
		
		if (type.equals("CRA")) {
			List<FileUsed> ele = fUR.findByFileIntegrationAndTypeFile(IntegrationDay, type);
			if (ele!=null && ele.size()>0) {
				fUR.deleteAll(ele);
				fUR.flush();	
			}
		}
		List<FileUsed> fus=new ArrayList<FileUsed>();
		
        List<String> filteredFiles = new ArrayList<>();
        File directory = new File(directoryPath);
        File[] files = directory.listFiles((dir, name) -> name.endsWith(filter));
        if (files != null) {
            for (File file : files) {
            	FileUsed fu=new FileUsed();
            	fu.setFileIntegration(IntegrationDay);
            	
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    int lines = 0;
                    while (reader.readLine() != null) {
                        lines++;
                    }
                    fu.setFileName(file.getName());
                    fu.setNblignes((long) lines);
                    fu.setModifiedDate(new Date(file.lastModified()).toString());
                    fu.setTypeFile(type);
                    fus.add(fu);
                    filteredFiles.add(file.getName() + " (" + lines + " lines)");
                } catch (IOException e) {
                    System.err.println("Error reading file: " + file.getName());
                }
            }
        }
        fUR.saveAll(fus);
        fUR.flush();
       // return filteredFiles;
    }
}
