package com.mss.backOffice.controller;

import com.google.gson.Gson;
import com.mss.unified.entities.*;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.unified.repositories.*;
import com.mss.backOffice.request.*;
import com.mss.backOffice.services.PropertyService;

import java.util.*;
import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bin/")
public
class BinController {

    @Autowired
    BinOnUsRepository binOnUsRepository;

    @Autowired
    BinStatusRepository binOnUsStatusRepository;

    @Autowired
    BinOnUsTypeRepository binOnUsTypeRepository;

    ModelMapper modelMapper = new ModelMapper();

    @Autowired
    NationalBinRepository nationalBinRepository;
    @Autowired
    HsmKeysRepository hsmKeysRepository;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private RangeRepository rangeRepository;
    @Autowired
    private RoutageRepository routageRepository;

    @Autowired
    private RangeStatusRepository rangeStatusRepository;
    @Autowired
    private PropertyService propertyService;
    private static final Gson gson = new Gson();
    private static final Logger logger = LoggerFactory.getLogger(BinController.class);

    @PostMapping("addBinOnUs")
    public ResponseEntity<String> addBinOnUs(@Valid @RequestBody AddBinOnUs addBinOnUs) {
        logger.info(addBinOnUs.toString());
        for (Range range : addBinOnUs.getRange()
        ) {
            if (rangeRepository
                    .existsByBouHighRangeAndBouLowRange(range.getBouHighRange(), range.getBouLowRange())) {
                return new ResponseEntity<String>("Fail -> Range is already in use!",
                        HttpStatus.BAD_REQUEST);
            }
            rangeRepository.save(range);
        }
        if (binOnUsRepository
                .existsByBouHighBinAndBouLowBin(addBinOnUs.getBouHighBin(), addBinOnUs.getBouLowBin())) {
            return new ResponseEntity<String>("Fail -> BIN is already in use!",
                    HttpStatus.BAD_REQUEST);
        }

        BinOnUs binOnUs = new BinOnUs();
        binOnUs.setBouBankCode(1);
        binOnUs.setRanges(addBinOnUs.getRange());
        binOnUs.setBinStatusCode(addBinOnUs.getBinStatusCode());
        binOnUs.setBinTypeCode(addBinOnUs.getBinTypeCode());
        binOnUs.setBouHighBin(addBinOnUs.getBouHighBin());
        binOnUs.setBouLowBin(addBinOnUs.getBouLowBin());
        binOnUs.setBouLength(addBinOnUs.getBouLength());
        binOnUs.setBinLength(addBinOnUs.getBinLength());
        binOnUs.libelle = addBinOnUs.Libelle;
        binOnUs=binOnUsRepository.save(binOnUs);
        
        HsmKeys hsmKeys= new HsmKeys();
        HsmKeysId hsmKeysId= new HsmKeysId();
        hsmKeysId.setBinCode(binOnUs.getBinOnUsCode()+"");
        
        hsmKeys.setHsmKeysId(hsmKeysId);
  	  hsmKeysId.setLibelle("ARQC");

        if (addBinOnUs.Libelle.toLowerCase().contains("visa")) {
        	 // hsmKeysId.setLibelle("ARQC_VISA");
        	  if (propertyService.getEnv().equals("DEV")) {
              	  hsmKeys.setCrypto("S10096E0TN00S0000C8A8D5828994319C61B4120CA1911E19041B4D15FE8D6A198060D276AA7181C85A3B86AB7BC7272B");
                    hsmKeys.setKcv("944A44");
              	
              }else {
              	
                  hsmKeys.setCrypto("S10096E0TN00S000098CC1E744A7365D2F47C93C356AD90D234DC6A28FA63B9078B3A71CB2D8AFC66FCF65336E1DB6CD5");
                  hsmKeys.setKcv("962EAD");
                  
              }
 
        }else {
        	
              if (propertyService.getEnv().equals("DEV")) {
              	  hsmKeys.setCrypto("S10096E0TN00S0000EC54B8851D342A683C8C5DB28B730315BB2D32BCB6353FCFF72D13198DE078091F1931005E703313");
                    hsmKeys.setKcv("5AB80F");
              	
              }else {
              	
                  hsmKeys.setCrypto("S10096E0TN00S000099086B22A725495BE2432E44C712ED5E9582DEB0E59CD39B806D6086FF9E0EC59A2A433BA1F06CB2");
                  hsmKeys.setKcv("7B910E");
                  
              }
        	
        }
        
      

        hsmKeysRepository.save(hsmKeys);
        
        
        HsmKeys hsmKeys2= new HsmKeys();
        HsmKeysId hsmKeysId2= new HsmKeysId();
        hsmKeysId2.setBinCode(binOnUs.getBinOnUsCode()+"");
        hsmKeys2.setHsmKeysId(hsmKeysId2);
        hsmKeysId2.setLibelle("CVV");

        if (addBinOnUs.Libelle.toLowerCase().contains("visa")) {
            //hsmKeysId2.setLibelle("CVV_VISA");

        	if (propertyService.getEnv().equals("DEV")) {
        		hsmKeys2.setCrypto("S1009613TN00S0000AF37BCFBF3AF1DDA16615B82BE2077F45F52A8C6ED419F66D6E052D0E09520B53456D0506608C5FA");
        		hsmKeys2.setKcv("B1A6D3");
                
            }else {
            	
            	hsmKeys2.setCrypto("S1009613TN00S0000E15F3507184BDEB5C458EBA65D229ED0DB5FB072BECF4DD07CF73BB7CEAFCCDD85B14BD6876A74E1");
            	hsmKeys2.setKcv("C67A3A");
                
            }
        	
        }else {
            if (propertyService.getEnv().equals("DEV")) {
            	
            	hsmKeys2.setCrypto("S1009613TN00S00008287A0A963A244D2B193CE7E1924BF96129073328C261AABCE42ECF3291F44814C52A2C0224B668A");
            	hsmKeys2.setKcv("901A73");
            	
            }else {
            	hsmKeys2.setCrypto("S1009613TN00S0000BE9132B67493F6A5061586CAB85FDB16BA12BCF6E73397336EDEDD216CAFDD8E66619673F1BA5D03");
            	hsmKeys2.setKcv("FACBF7");
                
            }
        	
        }
//        hsmKeys2.setCrypto("S1009613TN00S0000BE9132B67493F6A5061586CAB85FDB16BA12BCF6E73397336EDEDD216CAFDD8E66619673F1BA5D03");
//        hsmKeys2.setKcv("FACBF7");
        
        hsmKeysRepository.save(hsmKeys2);
        
        HsmKeys hsmKeys3= new HsmKeys();
        HsmKeysId hsmKeysId3= new HsmKeysId();
        hsmKeysId3.setBinCode(binOnUs.getBinOnUsCode()+"");
        hsmKeys3.setHsmKeysId(hsmKeysId3);
        hsmKeysId3.setLibelle("PINBLOCK");
        if (addBinOnUs.Libelle.toLowerCase().contains("visa")) {
           
            hsmKeys3.setCrypto("S1009672TN00S0000DCE31F26CFCEC36CDFD170E2CA066530E910B23AE8FF06573034E8EA6DFDD232392F2170BF1FB85B");
            hsmKeys3.setKcv("B274B8");
	
        }else {
           
            if (propertyService.getEnv().equals("DEV")) {
            	 hsmKeys3.setCrypto("S1009672TN00S000042D3F19569F24185D2C2D329C2493F2E5A0AF3729403289075AFD497D33DE8506657BF45F688DB3A");
                 hsmKeys3.setKcv("1D045E");
            }else {
                hsmKeys3.setCrypto("S1009672TN00S0000A0318BD7DC746AA6E469B056A065D3D45FA86214F9B152ACF8C1E868A4F54F5C88F950AE8579255A");
                hsmKeys3.setKcv("4C2ABD");
            }
        	
        }
//        hsmKeys3.setCrypto("S1009672TN00S0000A0318BD7DC746AA6E469B056A065D3D45FA86214F9B152ACF8C1E868A4F54F5C88F950AE8579255A");
//        hsmKeys3.setKcv("4C2ABD");
        
        hsmKeysRepository.save(hsmKeys3);
        
        HsmKeys hsmKeys4= new HsmKeys();
        HsmKeysId hsmKeysId4= new HsmKeysId();
        hsmKeysId4.setBinCode(binOnUs.getBinOnUsCode()+"");
        hsmKeys4.setHsmKeysId(hsmKeysId4);
        hsmKeysId4.setLibelle("PVV");
        if (addBinOnUs.Libelle.toLowerCase().contains("visa")) {
            //hsmKeysId4.setLibelle("PVV_VISA");
            if (propertyService.getEnv().equals("DEV")) {
			
            	hsmKeys4.setCrypto("S10096V2TN00S0000024189FB2B9137F5F9745A45A9791420C1E3B804C5E793EF527C952CC7A5BB991019715F8B3A973D");
                hsmKeys4.setKcv("C3C968");
            }else {
             	hsmKeys4.setCrypto("S10096V2TN00S000003360B2E6C28103A22F6C1A578E139D3EED5FC9FE9F9EC678354506B52E2BB35815C9CDB67D21641");
                hsmKeys4.setKcv("C0AC6B");	
            }
        	
        }else {
           
            if (propertyService.getEnv().equals("DEV")) {	
                hsmKeys4.setCrypto("S10096V2TN00S0000089E5CEB3E87D6A58AF6C636B728DB522B40CFD9521C091E25AD5BEFE6D8D5B26B19835EC795230E");
                hsmKeys4.setKcv("1D045E");
            }else {
                hsmKeys4.setCrypto("S10096V2TN00S00003225CF03A144ECD342CDDC1B079A9E77111F41D13F6CD046057795916C4127382F2807394E20DE72");
                hsmKeys4.setKcv("21325F");
            }


        }
//        hsmKeys4.setCrypto("S10096V2TN00S00003225CF03A144ECD342CDDC1B079A9E77111F41D13F6CD046057795916C4127382F2807394E20DE72");
//        hsmKeys4.setKcv("21325F");
        
        hsmKeysRepository.save(hsmKeys4);
        
        
        HsmKeys hsmKeys5= new HsmKeys();
        HsmKeysId hsmKeysId5= new HsmKeysId();
        hsmKeysId5.setBinCode(binOnUs.getBinOnUsCode()+"");
        hsmKeys5.setHsmKeysId(hsmKeysId5);
        hsmKeysId5.setLibelle("SMI");
        if (addBinOnUs.Libelle.toLowerCase().contains("visa")) {
            //hsmKeysId4.setLibelle("PVV_VISA");
            if (propertyService.getEnv().equals("DEV")) {
			
            	hsmKeys4.setCrypto("S10096E2TN00S0000376EA373C1D569DB863ECF343FFF74BB5891D2FB15DCF9B63759F993F67FF8444021DE69EC1488AB");
                hsmKeys4.setKcv("944A44");
            }else {
            	hsmKeys4.setCrypto("S10096E2TN00S000088C27C2B686C80FC040B46094A3C6F8BE76DC11ECB76852235E9C466D93F7E87C281095E698E59E4");
                hsmKeys4.setKcv("5E7C66");
            }
        	
        }else {
           
            if (propertyService.getEnv().equals("DEV")) {	
                hsmKeys4.setCrypto("S10096E2TN00S0000ED1C30862F8DE2C2B5A80AD20DE6579FEA93DD3E4E67C2100DAD4184EF8B4115D6DA7BD0D164E5A9");
                hsmKeys4.setKcv("E3BAE0");
            }else {
                hsmKeys4.setCrypto("S10096E2TN00S0000F364080B9DDDCBAB191EB07E8289CA48C03E8639628AEB4FF4AAF856C79E208612D1726375768C11");
                hsmKeys4.setKcv("BF5BD7");
            }


        }
        hsmKeysRepository.save(hsmKeys5);
        
        HsmKeys hsmKeys6= new HsmKeys();
        HsmKeysId hsmKeysId6= new HsmKeysId();
        hsmKeysId6.setBinCode(binOnUs.getBinOnUsCode()+"");
        hsmKeys6.setHsmKeysId(hsmKeysId6);
        hsmKeysId6.setLibelle("SMC");
        if (addBinOnUs.Libelle.toLowerCase().contains("visa")) {
            //hsmKeysId4.setLibelle("PVV_VISA");
            if (propertyService.getEnv().equals("DEV")) {
			
            	hsmKeys6.setCrypto("S10096E1TN00S00006DD763C06B8DF25B0AC78B0BA71E5D73646EFB29F7CEFB56E8F581A026986C754CD1E0F02BBAD3B4");
            	hsmKeys6.setKcv("944A44");
            }else {
            	hsmKeys6.setCrypto("S10096E1TN00S00002FD25F27162F5E3E93E9BDABE555D09F7ED54046B21B1A05EC760971B189E0D88CD2D540491A6071");
            	hsmKeys6.setKcv("B21DC1");
            }
        	
        }else {
           
            if (propertyService.getEnv().equals("DEV")) {	
            	hsmKeys6.setCrypto("S10096E1TN00S000001E5AA57C4FE701B40AAB561FD7FDD85AEF552B90070E375DCF469893B5E52740BCDBB08EA5977E9");
            	hsmKeys6.setKcv("84EC8C");
            }else {
            	hsmKeys6.setCrypto("S10096E1TN00S0000C1EEEDA0DB9548A4F56380AEE3A0EA9E962CA55305C2F3BEB444D84F5B6C35103EA1F0C029059B51");
            	hsmKeys6.setKcv("34A645");
            }


        }

        hsmKeysRepository.save(hsmKeys6);
        
        logger.info(binOnUs.toString());
        
        
        return ResponseEntity.ok().body(gson.toJson("Bin On Us added successfully!"));

    }

    @PostMapping("addBank")
    public ResponseEntity<String> addBank(@Valid @RequestBody AddBank bank) {
        Bank bank1 = new Bank(bank.getTagBank(), bank.getLibelle(), bank.getIdentificationNumber());
        bankRepository.save(bank1);
        logger.info(bank1.toString());


        return ResponseEntity.ok().body(gson.toJson("Bank added successfully!"));

    }

    @PostMapping("addRange")
    public ResponseEntity<String> addRange(@Valid @RequestBody Range bank) {

        rangeRepository.save(bank);

        return ResponseEntity.ok().body(gson.toJson("Range added successfully!"));

    }

    @GetMapping("allRange")
    public List<Range> allRange() {
        return rangeRepository.findAll();
    }


    @GetMapping("allBinOnUs")
    public List<BinOnUs> allBinOnUs() {
        return binOnUsRepository.findAll();
    }

    @PostMapping("allBinOnUsFilter")
    public List<BinOnUs> allBinOnUs(@RequestBody BinOnUsFilter binOnUsFilter) {
        return binOnUsRepository.getAllByFilter(binOnUsFilter.getLabel().trim(), binOnUsFilter.getMin().trim(), binOnUsFilter.getMax().trim());
    }

    @GetMapping("getRevolvingBins")
    public List<BinOnUs> getRevolvingBins() {
        return binOnUsRepository.findRevolvingBins();
    }

    @PostMapping("Exist")
    public boolean ExistBinOnUs(@RequestBody ValidateBin range) {
        System.out.println(rangeRepository.existsByBouHighRangeAndBouLowRange(range.HightBin, range.LowBin));
        return rangeRepository.existsByBouHighRangeAndBouLowRange(range.HightBin, range.LowBin);
    }

    @GetMapping("getRangeByBin/{id}")
    public Set<RangeDisplay> getRangeByBin(@PathVariable(value = "id") int atmHardFitnessId) {
        BinOnUs binOnUs = binOnUsRepository.findByBinOnUsCode(atmHardFitnessId);
        Set<RangeDisplay> rangeDisplays = new HashSet<>();
        Set<Range> ranges = binOnUs.getRanges();
        for (Range r : ranges
        ) {
            RangeStatus rangeStatus = rangeStatusRepository.findByRangeStatusCode(r.getRangeStatusCode());
            RangeDisplay rangeDisplay = new RangeDisplay(r.getRangeCode(), r.getBouHighRange(), r.getBouLowRange(),
                    r.getRangeStatusCode());
            rangeDisplays.add(rangeDisplay);
        }
        return rangeDisplays;
    }

    @GetMapping("getBin/{id}")
    public ResponseEntity<DisplayBin> getBin(@PathVariable(value = "id") int atmHardFitnessId) {
        BinOnUs binOnUs = binOnUsRepository.findByBinOnUsCode(atmHardFitnessId);
        BinOnUsType binOnUsType = binOnUsTypeRepository.findByBtCode(binOnUs.getBinTypeCode());
        BinStatus binOnUsStatus = binOnUsStatusRepository.findByBstCode(binOnUs.getBinStatusCode());
        Set<Range> ranges = binOnUs.getRanges();
        DisplayBin displayBin = new DisplayBin();

        Set<RangeDisplay> rangeDisplays = new HashSet<>();
        for (Range r : ranges
        ) {
            RangeStatus rangeStatus = rangeStatusRepository.findById(r.getRangeStatusCode()).get();
            RangeDisplay rangeDisplay = new RangeDisplay(r.getRangeCode(), r.getBouHighRange(), r.getBouLowRange(),
                    rangeStatus.getRangeStatusCode());
            rangeDisplays.add(rangeDisplay);
        }
        displayBin.Libelle = binOnUs.libelle;
        displayBin.setBinLength(binOnUs.getBinLength());
        displayBin.setBinStatusCode(binOnUsStatus.getBstLibelle());
        displayBin.setBinTypeCode(binOnUsType.getBtLibelle());
        displayBin.setBouHighBin(binOnUs.getBouHighBin());
        displayBin.setBouLowBin(binOnUs.getBouLowBin());
        displayBin.setBouLength(binOnUs.getBouLength());
        displayBin.setRange(rangeDisplays);
        return ResponseEntity.ok().body(displayBin);
    }


    @GetMapping("getBinCode/{id}")
    public ResponseEntity<BinOnUs> getBinCode(@PathVariable(value = "id") int atmHardFitnessId) {
        BinOnUs binOnUs = binOnUsRepository.findByBinOnUsCode(atmHardFitnessId);

        return ResponseEntity.ok().body(binOnUs);
    }

    @PutMapping("update/{id}/{test}")
    public ResponseEntity<?> updateBinOnUs(@PathVariable(value = "id") String binOnUsId,@PathVariable(value = "test") boolean test,
                                           @Valid @RequestBody BinOnUs binOnusUpdated) {
        logger.info(binOnusUpdated.toString());
        BinOnUs binOnUs = binOnUsRepository.findByBinOnUsCode(Integer.parseInt(binOnUsId));
        if (test==false && binOnusUpdated.getRanges().size()==0) {
        	   if (binOnUs.getBouHighBin().equals(binOnusUpdated.getBouHighBin())
               		&& binOnUs.getBouLowBin().equals(binOnusUpdated.getBouLowBin()))
               	return new ResponseEntity<String>("Fail -> Old range has been deleted! please change bin to proceed",
                           HttpStatus.BAD_REQUEST);	
        	
        }	
        
        if(test) {
        for (Range range : binOnusUpdated.getRanges()
                ) {
                    if (rangeRepository
                            .existsByBouHighRangeAndBouLowRange(range.getBouHighRange(), range.getBouLowRange())) {
                        return new ResponseEntity<String>("Fail -> Range is already in use!",
                                HttpStatus.BAD_REQUEST);
                    }
                    rangeRepository.save(range);
                }
                if (binOnUsRepository
                        .existsByBouHighBinAndBouLowBin(binOnusUpdated.getBouHighBin(), binOnusUpdated.getBouLowBin())) {
                    return new ResponseEntity<String>("Fail -> BIN is already in use!",
                            HttpStatus.BAD_REQUEST);
                }
        }
        binOnUs.setBinLength(binOnusUpdated.getBinLength());
        binOnUs.setBinStatusCode(binOnusUpdated.getBinStatusCode());
        binOnUs.setBinTypeCode(binOnusUpdated.getBinTypeCode());
        binOnUs.setBouBankCode(binOnusUpdated.getBouBankCode());
        binOnUs.setBouHighBin(binOnusUpdated.getBouHighBin());
        binOnUs.setBouBankCode(1);
        binOnUs.setBouLength(binOnusUpdated.getBouLength());
        binOnUs.setBouLowBin(binOnusUpdated.getBouLowBin());
        binOnUs.setLibelle(binOnusUpdated.getLibelle());
        binOnUs.setRanges(binOnusUpdated.getRanges());
       
       
//        for (Range r : binOnusUpdated.getRanges()) {
//            System.out.println("range" + r.getRangeCode());
//        }
   
    /*modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    modelMapper.map(binOnusUpdated, binOnUs);*/


       // binOnUs = binOnusUpdated;
      //  logger.info(binOnUs.toString());
        binOnUs= binOnUsRepository.save(binOnUs);
        //logger.info(binOnUs.toString());
        return ResponseEntity.accepted().body(gson.toJson("Range updated successfully!"));
    }


    @PostMapping("addBinOnUsStatus")
    public ResponseEntity<String> addBinOnUsStatus(@Valid @RequestBody BinStatus program) {
        binOnUsStatusRepository.save(program);
        logger.info(program.toString());
        return ResponseEntity.ok().body("BinOnUs Status added successfully!");

    }


    @GetMapping("allBinOnUsStatus")
    public List<BinStatus> allBinOnUsStatus() {
        return binOnUsStatusRepository.findAll();
    }


    @GetMapping("getBinStatus/{id}")
    public ResponseEntity<BinStatus> getBinStatus(
            @PathVariable(value = "id") int atmHardFitnessId) {
        BinStatus binOnUs = binOnUsStatusRepository.findByBstCode(atmHardFitnessId);
        logger.info(binOnUs.toString());
        return ResponseEntity.ok().body(binOnUs);
    }


    @PutMapping("updateStatus/{id}")
    public ResponseEntity<BinStatus> updateBinOnUsStatus(
            @PathVariable(value = "id") String userId,
            @Valid @RequestBody BinStatus binOnusUpdated) {
        logger.info(binOnusUpdated.toString());
        BinStatus binOnUs = binOnUsStatusRepository.findByBstCode(Integer.parseInt(userId));
        modelMapper.map(binOnusUpdated, binOnUs);

        final BinStatus updatedEmployee = binOnUsStatusRepository.save(binOnUs);
        logger.info(updatedEmployee.toString());
        return ResponseEntity.ok(updatedEmployee);
    }


    @PostMapping("addBinOnUsType")
    public ResponseEntity<String> addBinOnUsType(@Valid @RequestBody BinOnUsType program) {
        binOnUsTypeRepository.save(program);
        logger.info(program.toString());
        return ResponseEntity.ok().body(gson.toJson("BinOnUs Type added successfully!"));

    }


    @GetMapping("allBinOnUsTye")
    public List<BinOnUsType> allBinOnUsType() {
        return binOnUsTypeRepository.findAll();
    }


    @GetMapping("getBinType/{id}")
    public ResponseEntity<BinOnUsType> getBinType(@PathVariable(value = "id") int atmHardFitnessId) {
        BinOnUsType binOnUs = binOnUsTypeRepository.findByBtCode(atmHardFitnessId);
        logger.info(binOnUs.toString());
        return ResponseEntity.ok().body(binOnUs);
    }


    @PutMapping("updateBinType/{id}")
    public ResponseEntity<BinOnUsType> updateBinOnUsStatus(@PathVariable(value = "id") String userId,
                                                           @Valid @RequestBody BinOnUsType binOnusUpdated) {
        logger.info(binOnusUpdated.toString());
        BinOnUsType binOnUs = binOnUsTypeRepository.findByBtCode(Integer.parseInt(userId));
        modelMapper.map(binOnusUpdated, binOnUs);

        final BinOnUsType updatedEmployee = binOnUsTypeRepository.save(binOnUs);
        return ResponseEntity.ok(updatedEmployee);

    }

    @PostMapping("addNationalBinOnUs")
    public ResponseEntity<String> addNationalBinOnUs(@Valid @RequestBody NationalBin program) {
        nationalBinRepository.save(program);
        logger.info(program.toString());
        return ResponseEntity.ok().body(gson.toJson("National Bin  added successfully!"));

    }


    @GetMapping("allNationalBinOnUs")
    public List<NationalBin> allNationalBinOnUs() {

        return (List<NationalBin>) nationalBinRepository.findAll();

    }

    @PostMapping("allNationalBinOnUsFiltred")
    public List<NationalBin> allNationalBinOnUsFiltred(@RequestBody BinNationalFilter binNationalFilter){
        return nationalBinRepository.findbyFilter(binNationalFilter.getMin().trim(), binNationalFilter.getMax().trim());

    }


    @GetMapping("allBank")
    public List<Bank> allBank() {
        return bankRepository.findAll();
    }

    @PostMapping("AllBankFiltred")
    public List<Bank> AllBankFiltred(@RequestBody String libelle) {
        if(!libelle.equals("=")){
            return bankRepository.findAllByLibelle(libelle.trim());
        } else{
            return bankRepository.findAll();
        }
    }

    @GetMapping("getNationalBin/{id}")
    public ResponseEntity<NationalBin> getNationalBin(
            @PathVariable(value = "id") int atmHardFitnessId)
            throws ResourceNotFoundException {
        NationalBin binOnUs = nationalBinRepository.findByNBCode(atmHardFitnessId).
                orElseThrow(
                        () -> new ResourceNotFoundException(" not found for this id :: " + atmHardFitnessId));
        logger.info(binOnUs.toString());
        return ResponseEntity.ok().body(binOnUs);
    }


    @PutMapping("updateNationalBin/{id}")
    public ResponseEntity<NationalBin> updateNationalBin(@PathVariable(value = "id") String userId,
                                                         @Valid @RequestBody NationalBin binOnusUpdated) {
        logger.info(binOnusUpdated.toString());
        NationalBin binOnUs = nationalBinRepository.findByNBCode(Integer.parseInt(userId)).get();
        /*    modelMapper.map(binOnusUpdated, binOnUs);*/
        binOnusUpdated.setnBCode(binOnUs.getnBCode());
        binOnUs = binOnusUpdated;
        final NationalBin updatedEmployee = nationalBinRepository.save(binOnUs);
        logger.info(updatedEmployee.toString());
        return ResponseEntity.ok(updatedEmployee);
    }




    @GetMapping("hsmKeys")
    public List<HsmKeys> hsmKeys() {
        System.out.println("hi");
        List<HsmKeys> all = hsmKeysRepository.findAll();
        System.out.println(all);
        /*all.forEach(x -> {
            String bou = binOnUsRepository.findByBinOnUsCode(Integer.parseInt(x.getHsmKeysId().getBinCode())).getBouLowBin();
            x.setHsmKeysId(new HsmKeysId(x.getHsmKeysId().getLibelle(), bou));
        });*/
        return all;
    }

    @GetMapping("hsmKeysGroupByLibelle")
    public List<String> hsmKeysGroupByLibelle() {
        return hsmKeysRepository.findAllGroupByLibelle();
    }

    @PostMapping("hsmKeys")
    public List<HsmKeys> AddHsmKeys(@RequestBody List<HsmKeys> hsmKeys) {

        return hsmKeysRepository.saveAll(hsmKeys);
    }

    @PostMapping("addRoutage")
    public ResponseEntity<String> addRoutage(@Valid @RequestBody Routage routage) {

        routageRepository.save(routage);

        return ResponseEntity.ok().body(gson.toJson("Routage added successfully!"));

    }

    @GetMapping("allRoutage")
    public List<Routage> allRoutage() {
        return routageRepository.findAll();
    }

    @DeleteMapping("deleteRangeByBin/{id}")
    public ResponseEntity<String> deleteRangeByBin(@PathVariable(value = "id") Integer userId) {

        BinOnUs bin = binOnUsRepository.findById(userId).get();
        Set<Range> ranges = bin.getRanges();

        bin.setRanges(null);
        for (Range r : ranges) {
            System.out.println(r.getRangeCode());
            rangeRepository.delete(r);
        }
        binOnUsRepository.save(bin);


        return ResponseEntity.ok().body(gson.toJson("Ranges deleted  successfully!"));


    }

    @DeleteMapping("deleteBin/{id}")
    public ResponseEntity<String> deleteBin(@PathVariable(value = "id") Integer userId) {

        BinOnUs bin = binOnUsRepository.findById(userId).get();

        binOnUsRepository.delete(bin);

        return ResponseEntity.ok().body("Ranges deleted  successfully!");

    }

    @DeleteMapping("deleteRange/{id}")
    public ResponseEntity<String> deleteRange(@PathVariable(value = "id") Integer userId) {
        BinOnUs bin = binOnUsRepository.findById(userId).get();
        Set<Range> ranges = bin.getRanges();
        for (Range r : ranges) {
            rangeRepository.delete(r);
        }
        //return ResponseEntity.ok().body("Ranges deleted  successfully!");
        return ResponseEntity.ok().body(gson.toJson("Ranges deleted  successfully!"));

    }

    @DeleteMapping("deleteRangeEdit/{id}")
    public ResponseEntity<String> deleteRangeEdit(@PathVariable(value = "id") Integer userId) {
        Range range = rangeRepository.findById(userId).get();
        rangeRepository.delete(range);


        return ResponseEntity.ok().body("Ranges deleted  successfully!");

    }

    //delete national bin
    @DeleteMapping("deleteNationalBin/{id}")
    public ResponseEntity<String> deleteNationalBin(@PathVariable(value = "id") String userId) {

        NationalBin binOnUs = nationalBinRepository.findByNBCode(Integer.parseInt(userId)).get();

        nationalBinRepository.delete(binOnUs);
        return ResponseEntity.ok().body(gson.toJson("Ranges deleted  successfully!"));
    }
    //

    @GetMapping("allRangeStatus")
    public List<RangeStatus> allRangeStatus() {
        return rangeStatusRepository.findAll();
    }

    @PostMapping("addRangeStatus")
    public ResponseEntity<String> addRangeStatus(@Valid @RequestBody RangeStatus routage) {

        rangeStatusRepository.save(routage);

        return ResponseEntity.ok().body(gson.toJson("RangeStatus added successfully!"));

    }

    @GetMapping("getBinAll")
    public List<BinOnUs> getBinAll() {
        List<BinOnUs> binOnUses=new ArrayList<>();
        List<BinOnUs> binOnUses1 = binOnUsRepository.findAll();
        for (BinOnUs b:binOnUses1) {
            if(b.getBinTypeCode()!=4)
            {
                binOnUses.add(b);
            }
        }
        return binOnUses;
    }

}
