package com.mss.backOffice.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mss.backOffice.Response.FileIntegrationResponse;
import com.mss.backOffice.request.AddFileRequest;
import com.mss.unified.entities.FileTP;
import com.mss.unified.entities.FileTpHeader;

import com.mss.unified.repositories.AtmFileHeaderRepository;
import com.mss.unified.repositories.AtmFileTpRepository;

@RestController
@RequestMapping("FileTP")
public class AtmTpController {
    //public String Path = "C://Users//Lenovo IdeaPad L3//Desktop//Files26";
    @Autowired
    AtmFileTpRepository atmFileTpRepository;

    @Autowired
    AtmFileHeaderRepository atmFileHeaderRepository;

    @PutMapping("addFileTP")
    public void addFileTP(@RequestBody AddFileRequest addFileRequest) throws IOException {
    	try {
    		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            LocalDate localDate = LocalDate.parse(addFileRequest.getFileDate(), formatter);
            int year = localDate.getYear();
            int month = localDate.getMonthValue();
            int day = localDate.getDayOfMonth();
            LocalDate today = LocalDate.of(year, month, day);
            String ordinalDateString = today.format(DateTimeFormatter.ISO_ORDINAL_DATE);
            String yearString = ordinalDateString.substring(2, 4);
            ordinalDateString = yearString + ordinalDateString.substring(5, 8);
            String fileName = addFileRequest.getFilePath() + "/" + addFileRequest.getFileName();// + "201229.txt";

            //String fileName = "C://Users//Lenovo IdeaPad L3//Desktop//Files26//TP220726.txt";
            //+ "TP220726.txt";
    //fsbTP220720-2.txt
            System.out.println(fileName + " name file ");

            List<String> listDetails = new ArrayList<>();
            List<String> stream = Files.readAllLines(Paths.get(fileName), StandardCharsets.ISO_8859_1);

            FileTpHeader f = new FileTpHeader();

    //Add header
            if (!(atmFileHeaderRepository.findByFileNameAndFileDate(addFileRequest.getFileName(), addFileRequest.getFileDate()).isPresent())) {
                System.out.println("this file existe" + addFileRequest.getFileDate());
                if (Files.exists(Paths.get(fileName))) {
                    f.setFileName(addFileRequest.getFileName());
                    f.setFileDate(ordinalDateString);
                    f.setFileprocessingDate(addFileRequest.getFileDate());
                    f = atmFileHeaderRepository.save(f);
                    int idHeader = f.getId();
                    System.out.println("idHeader " + idHeader);
                    List<FileTP> lists = new ArrayList<FileTP>();

                    stream.forEach(e -> {
                    	FileTP fileTP = new FileTP();
                    	fileTP.setCodeDebit(e.substring(0, 1).trim());
                    	fileTP.setCodeBin(e.substring(1, 7).trim());
                    	fileTP.setCodeBank(e.substring(7, 10).trim());
                    	fileTP.setNumRIBEmetteur(e.substring(10, 30).trim());

                    	fileTP.setNumCartePorteur(e.substring(30, 49).trim());
                    	fileTP.setCodeDebitCommercant(e.substring(49, 50).trim());
                    	fileTP.setNumRIBcommercant(e.substring(50, 70).trim());
                    	fileTP.setBinAcquereur(e.substring(70, 76).trim());
                    	fileTP.setCodeBankAcquereur(e.substring(76, 79).trim());
                    	fileTP.setCodeAgence(e.substring(79, 84).trim());

                    	fileTP.setIdTerminal(e.substring(84, 99).trim());
                    	fileTP.setIdCommercant(e.substring(99, 114).trim());

                    	fileTP.setTypeTransaction(e.substring(114, 117).trim());

                    	fileTP.setDateTransaction(e.substring(117, 125).trim());

                    	fileTP.setHeureTransaction(e.substring(125, 131).trim());
                    	fileTP.setMontantTransaction(e.substring(131, 146).trim());
                    	fileTP.setNumFacture(e.substring(146, 161).trim());
                    	fileTP.setEmetteurFacture(e.substring(161, 201).trim());
                    	fileTP.setNumRefTransaction(e.substring(201, 213).trim());
                    	fileTP.setNumAutorisation(e.substring(213, 228).trim());
                    	fileTP.setCodeDebitPorteur(e.substring(228, 229).trim());
                        fileTP.setCommisionPorteur(e.substring(229, 241).trim());

                        fileTP.setCodeDebitCommisionCommercant(e.substring(241, 242).trim());
                        fileTP.setCommisionCommercant(e.substring(242, 254).trim());
                        fileTP.setCommisionInterchange(e.substring(254, 266).trim());
                        fileTP.setFraisOperateurTechnique(e.substring(266, 278).trim());

                        fileTP.setAppCryptogram(e.substring(278, 294).trim());
                        fileTP.setCryptogramInfoData(e.substring(294, 296).trim());
                        fileTP.setAtc(e.substring(296, 300).trim());

                        fileTP.setTerminalVerificationResult(e.substring(300, 310).trim());
                        fileTP.setLibelleCommercant(e.substring(310, 350).trim());

                        fileTP.setRuf(e.substring(350, 410).trim());
                        fileTP.setNumtransaction(e.substring(410, 422).trim());

                        fileTP.setUdf1(e.substring(422, 442).trim());
                        fileTP.setRufAcquereur(e.substring(442, 460).trim());
                        fileTP.setNumTransactionPaiementInternet(e.substring(460, 480).trim());
                        fileTP.setTrackId(e.substring(480, 490).trim());
                        fileTP.setIdOriginTransaction(e.substring(490, 510).trim());
                        fileTP.setRufpaiement(e.substring(510, 528).trim());
                        fileTP.setIdHeder(idHeader + "");
                        lists.add(fileTP);

                    });
                    atmFileTpRepository.saveAll(lists);
                }
            }

        
    	}catch(Exception e) {
    		
    		e.printStackTrace();
    	}
    }      
}
