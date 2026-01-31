package com.mss.backOffice.controller;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ibm.icu.text.SimpleDateFormat;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.backOffice.request.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;
import javax.validation.Valid;
import com.mss.unified.entities.*;
import com.mss.unified.repositories.*;

 

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/atmConfiguration")
public class AtmConfigurationController {
	@Autowired
	private AtmJournalConfigRepository atmJournalConfigRepository;
	private ModelMapper modelMapper;

	@Autowired
	private TimerParamsRepository timerParamsRepository;

	@Autowired
	private AtmFitsApplicationRepository atmFitsApplicationRepository;

	@Autowired
	private AtmStatusTypeRepository atmStatusTypeRepository;
	@Autowired
	EnhancedConfigParamLoadRepository enhancedConfigParamLoadRepository;
	@Autowired
	ConfigurationParaLoadRepository configurationParaLoadRepository;

	@Autowired
	FieldMRepository fieldMRepository;
	@Autowired
	FieldHRepository fieldHRepository;
	@Autowired
	AtmStatusConfigurationRepository atmStatusConfigurationRepository;

	@Autowired
	AtmCassetteMapValueRepository atmCassetteMapValueRepository;
	@Autowired
	AtmConfigurationRepository atmConfigurationRepository;
	@Autowired
	StatConfigRepository statConfigRepository;

	@Autowired
	AtmStatusValueRepository atmStatusValueRepository;

	@Autowired
	EmvCurrencyValueRepository emvCurrencyValueRepository;
	@Autowired
	AtmTransactionReplyConfigurationRepository atmTransactionReplyConfigurationRepository;
	@Autowired
	EmvTerminalDataRepository emvTerminalDataRepository;
	@Autowired
	ConfigurationParaLoadRepository confParamLoadRepository;
	@Autowired
	AtmImpressionConfigurationRepository atmImpressionConfigurationRepository;
	@Autowired
	AtmScreenConfigRepository atmScreenConfigRepository;
	@Autowired
	EmvLangConfigRepository emvLangConfigRepository;
	@Autowired
	EmvAidConfigRepository emvAidConfigRepository;
	@Autowired
	AtmModelRepository atmModelRepository;
	@Autowired
	AtmMarqueRepository atmMarqueRepository;
	@Autowired
	EmvServiceAppRepository emvServiceAppRepository;
	@Autowired
	AtmCurrencyValueRepository atmCurrencyValueRepository;
	@Autowired
	EmvCurrencyRepository emvCurrencyRepository;
	@Autowired
	AtmCurrencyMappingRepository atmCurrencyMappingRepository;
	@Autowired
	AtmEmvCurrRepository atmEmvCurrRepository;
	@Autowired
	AtmScreenValueRepository atmScreenValueRepository;
	@Autowired
	private AtmAlertsHierarchyRepository atmAlertsHierarchyRepository;
	
	private static final Gson gson = new Gson();
	private static final Logger logger = LoggerFactory.getLogger(AtmConfigurationController.class);
	
	@PostMapping("addHierarchyAlert")
	public ResponseEntity<String> addHierarchyAlert(@RequestBody List<AtmAlertsHierarchy> alert) {
		atmAlertsHierarchyRepository.deleteAll();
		atmAlertsHierarchyRepository.saveAll(alert);
		logger.info(alert.toString());
		return ResponseEntity.ok().body(gson.toJson("Atm Alert Hierarchy added successfully!"));

	}
	@GetMapping("getAllHierarchyAlerts")
	public List<AtmAlertsHierarchy> getAllHierarchyAlerts() {
		return atmAlertsHierarchyRepository.findAll();
	}


	@PostMapping("addTimerParamsConfig")
	public ResponseEntity<String> addTimerParamsConfig(@Valid @RequestBody TimerParams product) {
		timerParamsRepository.save(product);
		logger.info(product.toString());
		return ResponseEntity.ok().body(gson.toJson("timer added successfully!"));

	}

	@GetMapping("getAllTimers")
	public List<TimerParams> getAllTimers() {
		return timerParamsRepository.findAll();
	}


	@PostMapping("AllTimerParamsFiltred")
	public List<TimerParams> AllTimerParamsFiltred(@RequestBody String timerConfigLibelle) {
		if(!timerConfigLibelle.equals("=")){
			return timerParamsRepository.findAllByTimerConfigLibelle(timerConfigLibelle.trim());
		} else{
			return timerParamsRepository.findAll();
		}
	}








	@GetMapping("/timerParamsConfig/{id}")
	public ResponseEntity<TimerParams> gettimerParamsConfigById(@PathVariable(value = "id") int atmHardFitnessId)
			throws ResourceNotFoundException {
		TimerParams product = timerParamsRepository.findById(atmHardFitnessId)
				.orElseThrow(() -> new ResourceNotFoundException(" not found for this id :: " + atmHardFitnessId));
		logger.info(product.toString());
		return ResponseEntity.ok().body(product);
	  }
	
	

//	@PutMapping("/updateTimerParams/{id}")
//	public ResponseEntity<TimerParams> updateEmployee(@PathVariable(value = "id") String userId,
//			@RequestBody UpdateTimer userDetails) throws ResourceNotFoundException {
//		logger.info(userDetails.toString());
//		TimerParams employee = timerParamsRepository.findByTimerConfigNum(Integer.parseInt(userId))
//				.orElseThrow(() -> new ResourceNotFoundException(" not found for this id :: " + userId));
//
//		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STANDARD);
//		modelMapper.getConfiguration().setSkipNullEnabled(true);
//
//		// modelMapper.getConfiguration().setAmbiguityIgnored(true);
//		// modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
//
//		modelMapper.map(userDetails, employee);
//
//		final TimerParams updatedEmployee = timerParamsRepository.save(employee);
//		logger.info(updatedEmployee.toString());
//		return ResponseEntity.ok(employee);
//	}

	
	  @PutMapping("/updateTimerParams/{id}")
	  public ResponseEntity<String> updateEmployee(@PathVariable(value = "id") Integer userId,
	                                                   @RequestBody TimerParams timer)
	      throws ResourceNotFoundException {

	    TimerParams employee = timerParamsRepository.findById(userId).get();
	    timer.setTimerConfigNum(employee.getTimerConfigNum());
	    merge(timer, employee);

	    timerParamsRepository.save(employee);

	    System.out.println("timer config."+employee);


	    return ResponseEntity.ok().body(gson.toJson("Timer updated successfully!"));
	  }
	  
	  public static <TimerParams> void merge(TimerParams timer, TimerParams employee) {
		    ModelMapper modelMapper = new ModelMapper();
		    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		    modelMapper.map(timer, employee);
		  }


	@PostMapping("addAtmFitsApplication")
	public ResponseEntity<String> addAtmFitsApplication(@Valid @RequestBody AtmFitsApplication product) {
		atmFitsApplicationRepository.save(product);
		logger.info(product.toString());
		return ResponseEntity.ok().body(gson.toJson("atm Fits application added successfully!"));

	}

	@GetMapping("getAllAtmFitsApplication")
	public List<AtmFitsApplication> getAllAtmFitsApplication() {
		return atmFitsApplicationRepository.findAll();
	}
	 
	
	
	
	
	
	
	
	@PostMapping("addAtmStatusType")
	public ResponseEntity<String> addAtmStatusType(@Valid @RequestBody AtmStatusType product) {
		atmStatusTypeRepository.save(product);
		logger.info(product.toString());
		return ResponseEntity.ok().body(gson.toJson("atm Fits application added successfully!"));

	}

	@GetMapping("getAllAtmStatusType")
	public List<AtmStatusType> getAllAtmStatusType() {
		return atmStatusTypeRepository.findAll();
	}

	@GetMapping("/AtmStatusType/{id}")
	public ResponseEntity<AtmStatusType> getAtmStatusTypeById(@PathVariable(value = "id") int atmHardFitnessId)
			throws ResourceNotFoundException {
		AtmStatusType product = atmStatusTypeRepository.findById(atmHardFitnessId).orElseThrow(
				() -> new ResourceNotFoundException("AtmStatusType not found for this id :: " + atmHardFitnessId));
		logger.info(product.toString());
		return ResponseEntity.ok().body(product);
	}

//	@PostMapping("/updateAtmStatusType/{id}")
//	public ResponseEntity<AtmStatusType> updateAtmStatusType(@PathVariable(value = "id") String userId,
//			@Valid @RequestBody AtmStatusType userDetails) throws ResourceNotFoundException {
//
//		AtmStatusType employee = atmStatusTypeRepository.findByAtmStatusTypeCode(Integer.parseInt(userId))
//				.orElseThrow(() -> new ResourceNotFoundException("AtmStatusType not found for this id :: " + userId));
//
//		modelMapper.map(userDetails, employee);
//		final AtmStatusType updatedEmployee = atmStatusTypeRepository.save(employee);
//		logger.info(updatedEmployee.toString());
//		return ResponseEntity.ok(updatedEmployee);
//	}
	
	@GetMapping("getBYStatusType")
	public List<AtmStatusTypeDisplay> getBYStatusType() {
	  List<AtmStatusTypeDisplay> list = new ArrayList<AtmStatusTypeDisplay>();
	  for( AtmStatusType element:atmStatusTypeRepository.findAll()) {
	    AtmStatusTypeDisplay obj= new AtmStatusTypeDisplay();
	    obj.setAtmStatusTypeCode(element.getAtmStatusTypeCode());
	    obj.setStatusType(element.getStatusType());
	    list.add(obj);
	  }
	  return list;
	}
	@PutMapping("/updateAtmStatusType/{id}")
	public ResponseEntity<String> updateAtmStatusType(@PathVariable(value = "id") String userId,
			@Valid @RequestBody AtmStatusType atmStatusType) throws ResourceNotFoundException {

		AtmStatusType employee = atmStatusTypeRepository.findByAtmStatusTypeCode(Integer.parseInt(userId))
				.orElseThrow(() -> new ResourceNotFoundException("AtmStatusType not found for this id :: " + userId));

		mergeConf(atmStatusType,employee);

		atmStatusTypeRepository.save(employee);
		return ResponseEntity.ok().body(gson.toJson("AtmStatusType updated successfully!"));
	}
	public static <AtmStatusType> void mergeStatusType(AtmStatusType atmStatusType, AtmStatusType employee) {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		modelMapper.map(atmStatusType, employee);
	}

	@PostMapping("addEnhancedConfigParamLoad")
	public ResponseEntity<String> addEnhancedConfigParamLoad(@Valid @RequestBody EnhancedConfigParamLoad product) {
		enhancedConfigParamLoadRepository.save(product);
		logger.info(product.toString());
		return ResponseEntity.ok().body(gson.toJson("EnhancedConfigParamLoad added successfully!"));

	}

	@GetMapping("getAllEnhancedConfigParamLoad")
	public List<EnhancedConfigParamLoad> getAllEnhancedConfigParamLoad() {
		return enhancedConfigParamLoadRepository.findAll();
	}

	  @GetMapping("/EnhancedConfigParamLoad/{id}")
	  public ResponseEntity<EnhancedConfigParamLoad> getEnhancedConfigParamLoadById(
	      @PathVariable(value = "id") int atmHardFitnessId)
	      throws ResourceNotFoundException {
	    EnhancedConfigParamLoad product = enhancedConfigParamLoadRepository.findById(atmHardFitnessId)
	        .orElseThrow(() -> new ResourceNotFoundException(
	            "EnhancedConfigParamLoad not found for this id :: " + atmHardFitnessId));
	    logger.info(product.toString());
	    return ResponseEntity.ok().body(product);
	  }

	  @PutMapping("/updateEnhancedConfigParamLoad/{id}")
	  public ResponseEntity<String> updateEnhancedConfigParamLoad(
	      @PathVariable(value = "id") Integer userId,
	       @RequestBody EnhancedConfigParamLoad enhanced) throws ResourceNotFoundException {

	    EnhancedConfigParamLoad employee = enhancedConfigParamLoadRepository
	        .findById(userId)
	        .orElseThrow(() -> new ResourceNotFoundException(" not found for this id :: " + userId));
	    //enhanced.getEcplCode(employee.getEcplCode());
	    mergeConf(enhanced,employee);
	    enhancedConfigParamLoadRepository.save(employee);

	    return ResponseEntity.ok().body(gson.toJson("EnhancedConfigParamLoad updated successfully!"));
	  }
	  public static <EnhancedConfigParamLoad> void mergeConf(EnhancedConfigParamLoad enhanced, EnhancedConfigParamLoad employee) {
	    ModelMapper modelMapper = new ModelMapper();
	    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
	    modelMapper.map(enhanced, employee);
	  }

	@PostMapping("AllEnhancedConfigParamLoadFiltred")
	public List<EnhancedConfigParamLoad> AllEnhancedConfigParamLoadFiltred(@RequestBody String libelle) {
		if(!libelle.equals("=")){
			return enhancedConfigParamLoadRepository.findAllByLibelle(libelle.trim());
		} else{
			return enhancedConfigParamLoadRepository.findAll();
		}
	}




	  @PostMapping("addConfigurationParaLoad")
	  public ResponseEntity<String> addConfigurationParaLoad(
	      @Valid @RequestBody ConfigurationParaLoad product) {
	    configurationParaLoadRepository.save(product);

	    return ResponseEntity.ok().body(gson.toJson("ConfigurationParaLoad added successfully!"));

	  }
	  @GetMapping("getAllConfigurationParaLoad")
	  public List<ConfigurationParaLoadDisplay> getAllConfigurationParaLoad() {
	    List<ConfigurationParaLoadDisplay> list = new ArrayList<ConfigurationParaLoadDisplay>();
	    for (ConfigurationParaLoad element : configurationParaLoadRepository.findAll()) {
	      Optional<FieldM> fieldM = fieldMRepository.findByFieldMValue(element.getFieldMValue());
	      Optional <FieldH> fieldH = fieldHRepository.findByCameraValue(element.getCameraValue());
	      ConfigurationParaLoadDisplay obj = new ConfigurationParaLoadDisplay();
	      obj.setAtmConfigParamLoadCode(element.getAtmConfigParamLoadCode());
	      obj.setFieldMValue(fieldM.get().getLibelle());
	      obj.setCameraValue(fieldH.get().getLibelle());
	      obj.setLibelle(element.getLibelle());
	      obj.setFieldI(element.getFieldI());
	      obj.setFieldJ(element.getFieldJ());
	      obj.setFieldK(element.getFieldK());
	      obj.setFieldL(element.getFieldL());
	      obj.setFieldN(element.getFieldN());
	      obj.setTimer00(element.getTimer00());
	      obj.setTimer01(element.getTimer01());
	      obj.setTimer02(element.getTimer02());
	      obj.setTimer03(element.getTimer03());
	      obj.setTimer04(element.getTimer04());
	      obj.setTimer05(element.getTimer05());
	      obj.setTimer06(element.getTimer06());
	      obj.setTimer07(element.getTimer07());
	      obj.setTimer08(element.getTimer08());
	      obj.setTimer09(element.getTimer09());
	      obj.setTimer10(element.getTimer10());
	      obj.setTimer60(element.getTimer60());
	      obj.setTimer61(element.getTimer61());
	      obj.setTimer63(element.getTimer63());
	      obj.setTimer68(element.getTimer68());
	      obj.setTimer69(element.getTimer69());
	      obj.setTimer72(element.getTimer72());
	      obj.setTimer77(element.getTimer77());
	      obj.setTimer78(element.getTimer78());
	      obj.setTimer87(element.getTimer87());
	      obj.setTimer91(element.getTimer91());
	      obj.setTimer92(element.getTimer92());
	      obj.setTimer94(element.getTimer94());
	      obj.setTimer95(element.getTimer95());
	      obj.setTimer96(element.getTimer96());
	      obj.setTimer97(element.getTimer97());

	      list.add(obj);}
	      return list;

	  }
	  @GetMapping("/ConfigurationParaLoad/{id}")
	  public ResponseEntity<ConfigurationParaLoad> getConfigurationParaLoadById(
	      @PathVariable(value = "id") int atmHardFitnessId)
	      throws ResourceNotFoundException {
	    ConfigurationParaLoad product = configurationParaLoadRepository.findById(atmHardFitnessId)
	        .orElseThrow(() -> new ResourceNotFoundException(
	            " ConfigurationParaLoadnot found for this id :: " + atmHardFitnessId));
	    return ResponseEntity.ok().body(product);
	  }

	  @PutMapping("/updateConfigurationParaLoad/{id}")
	  public ResponseEntity<String> updateEmployee(
	      @PathVariable(value = "id") Integer userId,
	      @RequestBody ConfigurationParaLoad config)
	      throws ResourceNotFoundException {

	    ConfigurationParaLoad employee = configurationParaLoadRepository
	        .findByAtmConfigParamLoadCode(userId)
	        .orElseThrow(() -> new ResourceNotFoundException(" not found for this id :: " + userId));
		  config.setAtmConfigParamLoadCode(employee.getAtmConfigParamLoadCode());
	    mergeConfPara(config,employee);
	    configurationParaLoadRepository.save(employee);
	    return ResponseEntity.ok().body(gson.toJson("ConfigurationParaLoad updated successfully!"));
	  }
	  public static <ConfigurationParaLoad> void mergeConfPara(ConfigurationParaLoad config, ConfigurationParaLoad employee) {
	    ModelMapper modelMapper = new ModelMapper();
	    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
	    modelMapper.map(config, employee);
	  }


	  @PostMapping("addFieldM")
	  public ResponseEntity<String> addFieldM(@Valid @RequestBody FieldM product) {
	    fieldMRepository.save(product);

	    return ResponseEntity.ok().body(gson.toJson("FieldM added successfully!"));

	  }

	  @GetMapping("getAllFieldM")
	  public List<FieldM> getAllFieldM() {
	    return fieldMRepository.findAll();
	  }


	@PostMapping("AllFieldMFiltred")
	public List<FieldM> AllFieldMFiltred(@RequestBody String libelle) {
		if(!libelle.equals("=")){
			return fieldMRepository.findAllByLibelle(libelle.trim());
		} else{
			return fieldMRepository.findAll();
		}
	}





	  @GetMapping("/FieldM/{id}")
	  public ResponseEntity<FieldM> getFieldMById(@PathVariable(value = "id") String atmHardFitnessId)
	      throws ResourceNotFoundException {
	    FieldM product = fieldMRepository.findById(atmHardFitnessId)
	        .orElseThrow(() -> new ResourceNotFoundException(
	            " FieldM found for this id :: " + atmHardFitnessId));
	    return ResponseEntity.ok().body(product);
	  }


	  @PutMapping("/updateFieldM/{id}")
	  public ResponseEntity<FieldM> updateFieldM(@PathVariable(value = "id") String userId,
	      @Valid @RequestBody FieldM userDetails)
	      throws ResourceNotFoundException {

	    FieldM employee = fieldMRepository.findByFieldMValue(userId)
	        .orElseThrow(() -> new ResourceNotFoundException(" not found for this id :: " + userId));
	    ;

	    modelMapper.map(userDetails, employee);
	    final FieldM updatedEmployee = fieldMRepository.save(employee);
	    return ResponseEntity.ok(updatedEmployee);
	  }


	  @PostMapping("addFieldH")
	  public ResponseEntity<String> addFieldH(@Valid @RequestBody FieldH product) {
	    fieldHRepository.save(product);

	    return ResponseEntity.ok().body(gson.toJson("FieldH added successfully!"));

	  }

	  @GetMapping("getAllFieldH")
	  public List<FieldH> getAllFieldH() {
	    return fieldHRepository.findAll();
	  }


	@PostMapping("AllFieldHFiltred")
	public List<FieldH> AllFieldHFiltred(@RequestBody String libelle) {
		if(!libelle.equals("=")){
			return fieldHRepository.findAllByLibelle(libelle.trim());
		} else{
			return fieldHRepository.findAll();
		}
	}



	  @GetMapping("/FieldH/{id}")
	  public ResponseEntity<FieldH> getFieldHById(@PathVariable(value = "id") String atmHardFitnessId)
	      throws ResourceNotFoundException {
	    FieldH product = fieldHRepository.findById(atmHardFitnessId)
	        .orElseThrow(() -> new ResourceNotFoundException(
	            " FieldH found for this id :: " + atmHardFitnessId));
	    return ResponseEntity.ok().body(product);
	  }


	  @PutMapping("/updateFieldH/{id}")
	  public ResponseEntity<FieldH> updateFieldH(@PathVariable(value = "id") String userId,
	      @Valid @RequestBody FieldH userDetails) throws ResourceNotFoundException {

	    FieldH employee = fieldHRepository.findByCameraValue(userId).orElseThrow(() ->
	        new ResourceNotFoundException(" not found for this id :: " + userId));
	    ;

	    modelMapper.map(userDetails, employee);
	    final FieldH updatedEmployee = fieldHRepository.save(employee);
	    return ResponseEntity.ok(updatedEmployee);
	  }



	  @PostMapping("addAtmStatusConfiguration")
	  public ResponseEntity<String> addAtmStatusConfiguration(
	      @Valid @RequestBody AtmStatusConfiguration product) {
	    atmStatusConfigurationRepository.save(product);

	    return ResponseEntity.ok().body(gson.toJson("FieldH added successfully!"));

	  }


	@PostMapping("addAtmScreenValue")
	public ResponseEntity<String> addAtmScreenValue(
			@Valid @RequestBody AtmScreenValue product) {
		//product.setAtmScreenValueId(embdId);
		atmScreenValueRepository.save(product);

		return ResponseEntity.ok().body(gson.toJson("FieldH added successfully!"));

	}
	  

	  @DeleteMapping("deleteAtmStatusConfiguration/{id}")
	  public ResponseEntity<String> deleteAtmStatusConfiguration(@PathVariable(value = "id") Integer numStatusApplicationCode) {
	AtmStatusConfiguration  atmStatusConfiguration=atmStatusConfigurationRepository.findByNumStatusApplicationCode(numStatusApplicationCode).get();
	 atmStatusConfigurationRepository.delete(atmStatusConfiguration);

	    return ResponseEntity.accepted().body(gson.toJson("atmStatusConfiguration deleted successfully!"));

	  }


	@GetMapping("getAllAtmStatusConfiguration")
	public List<AtmStatusConfiguration> getAllAtmStatusConfiguration() {
		return atmStatusConfigurationRepository.findAll();
	}



	@PostMapping("AllAtmStatusConfigurationFiltred")
	public List<AtmStatusConfiguration> AllAtmStatusConfigurationFiltred(@RequestBody String libelleStatusApplication) {
		if(!libelleStatusApplication.equals("=")){
			return atmStatusConfigurationRepository.findAllBylibelleStatusApplication(libelleStatusApplication.trim());
		} else{
			return atmStatusConfigurationRepository.findAll();
		}
	}




	  @GetMapping("/AtmStatusConfiguration/{id}")
	  public ResponseEntity<AtmStatusConfiguration> getAtmStatusConfigurationById(
	      @PathVariable(value = "id") String atmHardFitnessId)
	      throws ResourceNotFoundException {
	    AtmStatusConfiguration product = atmStatusConfigurationRepository
	        .findById(Integer.parseInt(atmHardFitnessId))
	        .orElseThrow(() -> new ResourceNotFoundException(
	            " AtmStatusConfiguration found for this id :: " + atmHardFitnessId));
	    return ResponseEntity.ok().body(product);
	  }


	  @PutMapping("/updateAtmStatusConfiguration/{id}")
	  public ResponseEntity<String> updateAtmStatusConfiguration(
	      @PathVariable(value = "id") String userId,
	      @Valid @RequestBody AtmStatusConfiguration userDetails) throws ResourceNotFoundException {

	    AtmStatusConfiguration atmStatusConfiguration = atmStatusConfigurationRepository
	        .findByNumStatusApplicationCode(Integer.parseInt(userId))
	        .orElseThrow(() -> new ResourceNotFoundException(" not found for this id :: " + userId));
	    userDetails.setNumStatusApplicationCode(atmStatusConfiguration.getNumStatusApplicationCode());
	    mergeStateConf(userDetails,atmStatusConfiguration);
	    atmStatusConfigurationRepository.save(atmStatusConfiguration);


	    return ResponseEntity.ok().body(gson.toJson("AtmStatusConfiguration updated successfully!"));
	  }

	  public static <AtmStatusConfiguration> void mergeStateConf(AtmStatusConfiguration userDetails, AtmStatusConfiguration atmStatusConfiguration) {
	    ModelMapper modelMapper = new ModelMapper();
	    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
	    modelMapper.map(userDetails, atmStatusConfiguration);
	  }


	  @PostMapping("addAtmCassetteMapValue")
	  public ResponseEntity<String> addAtmCassetteMapValue(
	      @Valid @RequestBody AtmCassetteMapValue product) {
	    atmCassetteMapValueRepository.save(product);

	    return ResponseEntity.ok().body(gson.toJson("AtmCassetteMapValue added successfully!"));

	  }

	  @GetMapping("getAllAtmCassetteMapValue")
	  public List<AtmCassetteMapValue> getAllAtmCassetteMapValue() {
	    return atmCassetteMapValueRepository.findAll();
	  }
	  
	  
	  
	  
	  
	  
	  
//	  @PostMapping("getAllAtmCassetteMpFiltred")
//	    public ResponseEntity<Page<AtmCassetteMapValue>>  getAllAtmCassetteMpFiltred(
//	    		@RequestBody String acmpLibelle,
//	    	     @RequestParam(name = "page", defaultValue = "0") int page,
//	    	     @RequestParam(name = "size", defaultValue = "10") int size
//	    		) {
//		  
//		  Page<AtmCassetteMapValue> page1= null;
//	        if(!acmpLibelle.equals("=")){
//	        	page1=( atmCassetteMapValueRepository.findAllByacmpLibelleFiltred(PageRequest.of(page, size),acmpLibelle.trim()));
//	        } else{
//	        	page1=(atmCassetteMapValueRepository.findAll(PageRequest.of(page, size)));
//	        }
//	        
//	     
//	        return ResponseEntity.ok().body(page1);
//	        
//	    }

	  
	  @PostMapping(value ="reportingpdf")
		public @ResponseBody  List<AtmCassetteMapValue> reportingpdf(
				@RequestBody AtmcassMapvaluefiltresrequest atmcassMapvaluefiltresrequest) {
			 
		 List<AtmCassetteMapValue> atmCassetteMapValue = new ArrayList<>();
		 atmCassetteMapValue=atmCassetteMapValueRepository.findAllByacmpLibelle(atmcassMapvaluefiltresrequest.getAcmpLibelle());
			return atmCassetteMapValue;
		}

	 
	  
	  @PostMapping("getAllAtmCassetteMapValueFiltred")
	    public Page<AtmCassetteMapValue> getAllAtmCassetteMapValueFiltred(
	    		@RequestBody AtmcassMapvaluefiltresrequest atmcassMapvaluefiltresrequest,
	    		@RequestParam(name = "page", defaultValue = "0") int page,
			    @RequestParam(name = "size", defaultValue = "5") int size,
			    @RequestParam(name = "sortOn") String sortOn,
				@RequestParam(name = "dir") String dir
	    		) {
		  
			List<Order> orders = new ArrayList<Order>();
		  	Order order1=null;
		  	if (dir.equals("desc"))
		  		order1 = new Order(Sort.Direction.DESC, sortOn);
		  	else 
		  		order1 = new Order(Sort.Direction.ASC, sortOn);

		  	orders.add(order1);
		  	
		  	Page<AtmCassetteMapValue> Datapage = atmCassetteMapValueRepository.findAllByacmpLibelle(PageRequest.of(page, size,Sort.by(orders)), atmcassMapvaluefiltresrequest.getAcmpLibelle());
		  	
		  	
	        
	            return Datapage;
	           
	    }
	 


	  
	  
	  
	  @GetMapping("/AtmCassetteMapValue/{id}")
	  public ResponseEntity<AtmCassetteMapValue> getAtmCassetteMapValueById(
	      @PathVariable(value = "id") int atmHardFitnessId)
	      throws ResourceNotFoundException {
	    AtmCassetteMapValue product = atmCassetteMapValueRepository.findById(atmHardFitnessId)
	        .orElseThrow(() -> new ResourceNotFoundException(
	            "AtmCassetteMapValue not found for this id :: " + atmHardFitnessId));
	    return ResponseEntity.ok().body(product);
	  }


	  @PutMapping("/updateAtmCassetteMapValue/{id}")
	  public ResponseEntity<String> updateAtmCassetteMapValue(
	      @PathVariable(value = "id") String userId,
	      @Valid @RequestBody AtmCassetteMapValue userDetails)
	      throws ResourceNotFoundException {
		  
		  

	    AtmCassetteMapValue atmCassetteMapValue = atmCassetteMapValueRepository
	        .findByAcmpCode(Integer.parseInt(userId))
	        .orElseThrow(() -> new ResourceNotFoundException(
	            " AtmCassetteMapValue not found for this id :: " + userId));
	    ;
	     userDetails.setAcmpCode(atmCassetteMapValue.getAcmpCode());
	    mergeCassetteMapValue(userDetails,atmCassetteMapValue);
	     atmCassetteMapValueRepository.save(atmCassetteMapValue);
	    return ResponseEntity.ok().body(gson.toJson("AtmCassetteMapValue updated successfully!"));
	  }

	  public static <AtmCassetteMapValue> void mergeCassetteMapValue(AtmCassetteMapValue userDetails, AtmCassetteMapValue atmCassetteMapValue) {
	    ModelMapper modelMapper = new ModelMapper();
	    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
	    modelMapper.map(userDetails, atmCassetteMapValue);}




	  @PostMapping("addAtmConfiguration")
	  public ResponseEntity<String> addAtmConfiguration(@Valid @RequestBody AtmConfiguration product) {
	    atmConfigurationRepository.save(product);

	    return ResponseEntity.ok().body(gson.toJson("AtmConfiguration added successfully!"));

	  }


	  @GetMapping("getAllAtmConfiguration")
	  public List<AtmConfiguration> getAllAtmConfiguration() {
	    return atmConfigurationRepository.findAll();
	  }
	  @GetMapping("getAllAtmJournalConfig")
	  public List<AtmJournalConfig> getAllAtmJournalConfig() {
	    return atmJournalConfigRepository.findAll();
	  } 
	  @GetMapping("/AtmConfiguration/{id}")
	  public ResponseEntity<AtmConfiguration> getAtmConfigurationById(
	      @PathVariable(value = "id") int atmHardFitnessId)
	      throws ResourceNotFoundException {
	    AtmConfiguration product = atmConfigurationRepository.findById(atmHardFitnessId)
	        .orElseThrow(() -> new ResourceNotFoundException(
	            "AtmConfiguration not found for this id :: " + atmHardFitnessId));
	    return ResponseEntity.ok().body(product);
	  }



	@PostMapping("/updateAtmConfiguration/{id}")
	public ResponseEntity<AtmConfiguration> updateAtmConfiguration(@PathVariable(value = "id") int userId,
			@Valid @RequestBody AtmConfiguration userDetails) throws ResourceNotFoundException {

		AtmConfiguration employee = atmConfigurationRepository.findById(userId).orElseThrow(() ->

		new ResourceNotFoundException(" AtmConfiguration not found for this id :: " + userId));
//    modelMapper.map(userDetails, employee);

		final AtmConfiguration updatedEmployee = atmConfigurationRepository
				.save(employee.updateAttributes(userDetails));
		return ResponseEntity.ok(updatedEmployee);
	}



// 	@PostMapping("addEmvCurrencyValue")
// 	public ResponseEntity<String> addEmvCurrencyValue(@Valid @RequestBody EmvCurrencyValue product) {
// 		emvCurrencyValueRepository.save(product);
// 
// 		return ResponseEntity.ok().body(gson.toJson("EmvCurrencyValue added successfully!"));
// 
// 	} 
// 	
// 	@PostMapping("addAtmCurrencyValue")
// 	public ResponseEntity<String> addAtmCurrencyValue(@Valid @RequestBody AtmCurrencyValue product) {
// 		atmCurrencyValueRepository.save(product);
// 		return ResponseEntity.ok().body(gson.toJson("AtmEmvCurrencyValue added successfully!"));
// 	}

	@PostMapping("addAtmCurrencyValue")
	public ResponseEntity<String> addAtmCurrencyValue(@Valid @RequestBody AtmCurrencyValue product) {
		logger.info(product.toString());
		AtmCurrencyValue atmCurrencyValue = new AtmCurrencyValue();
		if (atmCurrencyValueRepository.existsBycurrCode(product.getCurrCode())) {
			return new ResponseEntity<String>("Fail -> currCode is already in use!", HttpStatus.BAD_REQUEST);
		}
		atmCurrencyValueRepository.save(product);
		logger.info(product.toString());
		return ResponseEntity.ok().body(gson.toJson("AtmCurrencyValue added successfully!"));
	}

//	@GetMapping("getAllEmvCurrencyValue")
//	public List<EmvCurrencyValue> getAllEmvCurrencyValue() {
//		return emvCurrencyValueRepostory.findAll();
//	}

	@GetMapping("getAllAtmCurrencyValues")
	public List<AtmCurrencyValue> getAllAtmCurrencyValues() {
		return atmCurrencyValueRepository.findAll();
	}
	
	
	
	@PostMapping("getAllAtmCurrencyValueFiltred")
    public List<AtmCurrencyValue> getAllAtmCurrencyValueFiltred(@RequestBody String libelleDevise) {
        if(!libelleDevise.equals("=")){
            return atmCurrencyValueRepository.findAllBylibelleDevise(libelleDevise.trim());
        } else{
            return atmCurrencyValueRepository.findAll();
        }
    }

// 	@GetMapping("/EmvCurrencyValue/{id}")
// 	public ResponseEntity<EmvCurrencyValue> getEmvCurrencyValueById(@PathVariable(value = "id") String atmHardFitnessId)
// 			throws ResourceNotFoundException {
// 		EmvCurrencyValue product = emvCurrencyValueRepository.findById(atmHardFitnessId).orElseThrow(
// 				() -> new ResourceNotFoundException("EmvCurrencyValue not found for this id :: " + atmHardFitnessId));
// 	return ResponseEntity.ok().body(product);
// 	}

	@GetMapping("/getAtmCurrencyValueById/{id}")
	public ResponseEntity<AtmCurrencyValue> getAtmCurrencyValueById(@PathVariable(value = "id") String currCode)
			throws ResourceNotFoundException {
		AtmCurrencyValue product = atmCurrencyValueRepository.findById(currCode).orElseThrow(
				() -> new ResourceNotFoundException("AtmCurrencyValue not found for this id :: " + currCode));
		return ResponseEntity.ok().body(product);
	}

//	@PutMapping("/updateEmvCurrencyValue/{id}")
//	public ResponseEntity<EmvCurrencyValue> updateEmvCurrencyValue(@PathVariable(value = "id") String userId,
//			@Valid @RequestBody EmvCurrencyValue userDetails) throws ResourceNotFoundException {
//
//		EmvCurrencyValue employee = emvCurrencyValueRepository.findByTransCurrCode(userId).orElseThrow(
//				() -> new ResourceNotFoundException(" EmvCurrencyValue not found for this id :: " + userId));
//		employee.setCurrLibelle(userDetails.getCurrLibelle());
//		employee.setTransCurExponent(userDetails.getTransCurExponent());
//		employee.setTransCurrCode(userDetails.getTransCurrCode());
//		// modelMapper.map(userDetails, employee);
//		final EmvCurrencyValue updatedEmployee = emvCurrencyValueRepository.save(employee);
//		return ResponseEntity.ok(updatedEmployee);
//	}

	@PutMapping("/updateAtmEmvCurrencyValue/{id}")
	public ResponseEntity<AtmCurrencyValue> updateAtmCurrencyValue(@PathVariable(value = "id") String userId,
			@Valid @RequestBody AtmCurrencyValue userDetails) throws ResourceNotFoundException {
		AtmCurrencyValue employee = atmCurrencyValueRepository.findByCurrCode(userId).orElseThrow(
				() -> new ResourceNotFoundException(" AtmCurrencyValue not found for this id :: " + userId));
		employee.setCurrCode(userDetails.getCurrCode());
		employee.setCurrExponenet(userDetails.getCurrExponenet());
		employee.setLibelleDevise(userDetails.getLibelleDevise());

		// modelMapper.map(userDetails, employee);
		final AtmCurrencyValue updatedEmployee = atmCurrencyValueRepository.save(employee);
		return ResponseEntity.ok(updatedEmployee);
	}

	

	
//	@GetMapping("ecm")
//	public List<AtmCurrencyMapping> ecm() {
////	    System.out.println("****** "+code);
//		atmCurrencyMappingRepository.findAll().forEach(v -> System.out.println("====== " + v.getEcmCode()));
//		emvCurrencyRepository.findAll().forEach(v -> System.out.println(">>>>>> " + v.getEcmCode()));
//		return atmCurrencyMappingRepository.findLibelleCurr();
//	}

	

	@GetMapping("getAtmCurrencyMapping/{id}")
	public ResponseEntity<AddAtmCurrencyMapping> getAtmCurrencyMapping(@PathVariable(value = "id") Integer ecmCode)
			throws ResourceNotFoundException {
		AtmCurrencyMapping currmapp = new AtmCurrencyMapping();
		currmapp = atmCurrencyMappingRepository.findByEcmCode(ecmCode);
		List<AtmEmvCurr> atmEmvCur = atmEmvCurrRepository.findByEcmCode(ecmCode);
		List<AddCurremv> addCurremv = new ArrayList<>();
		
		AddAtmCurrencyMapping addAtmCurrencyMapping = new AddAtmCurrencyMapping();
		addAtmCurrencyMapping.setEcmLibelle(currmapp.getEcmLibelle());

		for (AtmEmvCurr item : atmEmvCur) {
			AddCurremv atmEmvCurr = new AddCurremv();
			atmEmvCurr.setCurrCode(item.getCurrCode());
			atmEmvCurr.setCurrorder(item.getCurrorder());
			addCurremv.add(atmEmvCurr);
		}

		addAtmCurrencyMapping.setAddCurremv(addCurremv);

		return ResponseEntity.ok(addAtmCurrencyMapping);
	}
	
	@GetMapping("getAllAtmCurrencyMapping")
	public List<AtmCurrencyMapping> getAllAtmCurrencyMapping() {
		return atmCurrencyMappingRepository.findAll();
	}
	
	@PostMapping("getAllAtmCurrencyMappingFiltred")
    public List<AtmCurrencyMapping> getAllAtmCurrencyMappingFiltred(@RequestBody String ecmLibelle) {
        if(!ecmLibelle.equals("=")){
            return atmCurrencyMappingRepository.findAllByEcmLibelle(ecmLibelle.trim());
        } else{
            return atmCurrencyMappingRepository.findAll();
        }
    }
	
	
	@GetMapping("getAllAtmCurrencyMapp")
	public List<AddAtmCurrencyMappingList> getAllAtmCurrencyMapp() {
		 List<AddAtmCurrencyMappingList> AddAtmCurrencyMapp = new ArrayList<>();
		 
				
		 for(AtmCurrencyMapping AtmCurrencyMapp : atmCurrencyMappingRepository.findAll())
		 {
			 AddAtmCurrencyMappingList addAtmCurrencyMappingList=new AddAtmCurrencyMappingList();
			 addAtmCurrencyMappingList.setEcmCode(AtmCurrencyMapp.getEcmCode());
			 addAtmCurrencyMappingList.setEcmLibelle(AtmCurrencyMapp.getEcmLibelle());
			 addAtmCurrencyMappingList.setAtmEmvCurr(atmEmvCurrRepository.findByEcmCode(AtmCurrencyMapp.getEcmCode()));
			 
		
			 
			 AddAtmCurrencyMapp.add(addAtmCurrencyMappingList);
			 
			 
		 }
		 
 		return AddAtmCurrencyMapp;
	}
	
	
	@PostMapping("addAtmCurrencyMapping")
	public ResponseEntity<String> addAtmCurrencyMapping(
			@Valid @RequestBody AddAtmCurrencyMapping addatmCurrencyMapping) {
		AtmCurrencyMapping currmapp = new AtmCurrencyMapping();
		currmapp.setEcmLibelle(addatmCurrencyMapping.getEcmLibelle());
		currmapp = atmCurrencyMappingRepository.save(currmapp);

		for (AddCurremv item : addatmCurrencyMapping.getAddCurremv()) {
			AtmEmvCurr atmEmvCurr = new AtmEmvCurr();
			atmEmvCurr.setEcmCode(currmapp.getEcmCode());
			atmEmvCurr.setCurrCode(item.getCurrCode());
			atmEmvCurr.setCurrorder(item.getCurrorder());
			atmEmvCurrRepository.save(atmEmvCurr);
		}
		logger.info(addatmCurrencyMapping.toString());
		return ResponseEntity.ok().body(gson.toJson("addatmCurrencyMapping added successfully!"));

	}
	 

	 
	@PutMapping("/updateAtmCurrencyMappingById/{id}")
	public ResponseEntity<String> updateAtmCurrencyMappingById(@PathVariable(value = "id") String ecmCode,
			@Valid @RequestBody AddAtmCurrencyMapping addatmCurrencyMapping) throws ResourceNotFoundException {
		AtmCurrencyMapping currmapp = atmCurrencyMappingRepository.findByEcmCode(Integer.parseInt(ecmCode));
		currmapp.setEcmLibelle(addatmCurrencyMapping.getEcmLibelle());
		currmapp = atmCurrencyMappingRepository.save(currmapp);
		List<AtmEmvCurr> atm=atmEmvCurrRepository.findByEcmCode(Integer.parseInt(ecmCode));
		atmEmvCurrRepository.deleteAll(atm);
		
		
		for (AddCurremv item : addatmCurrencyMapping.getAddCurremv()) {
			AtmEmvCurr atmEmvCurr = new AtmEmvCurr();
			atmEmvCurr.setEcmCode(currmapp.getEcmCode());
			atmEmvCurr.setCurrCode(item.getCurrCode());
			atmEmvCurr.setCurrorder(item.getCurrorder());
			atmEmvCurrRepository.save(atmEmvCurr);
		}
 
		return ResponseEntity.ok().body(gson.toJson("addatmCurrencyMapping Updated successfully!"));
	}
	 
 
  @PostMapping("getStatConfig/{id}")
  public List<StatConfig> getStat(@PathVariable(value = "id") String atmHardFitnessId,
                                  @RequestBody StatusAtmFilter statusAtmFilter) {
    List<StatConfig> statConfigs = atmStatusValueRepository.findStatus(Integer.parseInt(atmHardFitnessId));



    statConfigs = statConfigs.stream()
            .filter(e -> Strings.isNullOrEmpty(statusAtmFilter.getStatusNum())
                    || e.getStatNum().equals(statusAtmFilter.getStatusNum()))



            .filter(e -> Strings.isNullOrEmpty(statusAtmFilter.getStatusType())
                    || e.getStatType().equals(statusAtmFilter.getStatusType()))



            .collect(Collectors.toList());



    return statConfigs;
  }

  @PostMapping("addAtmStatusValue")
  public ResponseEntity<String> addAtmStatusValue(@Valid @RequestBody AtmStatusValue atmStatusValue) {
    atmStatusValueRepository.save(atmStatusValue);


    return ResponseEntity.ok().body(gson.toJson("AtmConfiguration added successfully!"));

  }



  @PutMapping("/updateAtmStatusValue/{id}")
  public ResponseEntity<String> updateAtmStatusValue(
          @PathVariable(value = "id") String userId,
          @Valid @RequestBody AtmStatusValue stat) throws ResourceNotFoundException {
	  try {

 //AtmStatusValue statusValus = atmStatusValueRepository.findById(Integer.toString(userId)).get();
 
 AtmStatusValue statusValus = atmStatusValueRepository.getOne(userId);

System.out.println("statusValus" +statusValus.getAtmStatusCode());
//    AtmStatusValue stateValue = atmStatusValueRepository.findByAtmStatusCode(userId)
//   .orElseThrow(() -> new ResourceNotFoundException(" not found for this id :: " + userId));
    //enhanced.getEcplCode(employee.getEcplCode());
statusValus.setStatusNum(stat.getStatusNum());
statusValus.setAtmStatusCode(stat.getAtmStatusCode());
statusValus.setNumStatApplication(stat.getNumStatApplication());

    mergeStateValue(stat,statusValus);
    atmStatusValueRepository.save(statusValus);
    logger.info("doneeeeeeeeeeeeeeeeeeeee");
    return ResponseEntity.ok().body(gson.toJson("AtmStatusValue updated successfully!"));}
	  catch(Exception e) {
		  e.printStackTrace();
	  }
	  return null;

  }
  public static <AtmStatusValue> void mergeStateValue(AtmStatusValue stat, AtmStatusValue statusValus) {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    modelMapper.map(stat, statusValus);
  }



  @GetMapping("/AtmStatusTypeByType/{id}")
  public ResponseEntity<AtmStatusType> AtmStatusTypeByType(@PathVariable(value = "id") String userId)
          throws ResourceNotFoundException {



    AtmStatusType employee = atmStatusTypeRepository.findByStatusType(userId)
            .orElseThrow(() -> new ResourceNotFoundException(" AtmStatusType not found for this id :: " + userId));



    return ResponseEntity.ok(employee);
  }


//
//	@GetMapping("/AtmScreenValueByCode/{id}")
//	public ResponseEntity<AtmScreenValue> AtmScreenValueByCode(@PathVariable(value = "id") String userId)
//			throws ResourceNotFoundException {
//
//
//
//		List<AtmScreenValue> employee = atmScreenValueRepository.findByScreenConfigId(userId);
//
//
//	//	List<StatConfig> statConfigs = atmStatusValueRepository.findStatus(Integer.parseInt(atmHardFitnessId));
//
//		return ResponseEntity.ok(employee);
//	}

	@GetMapping("/getAllScreenValueByApp/{id}")
	public List<AtmScreenValue> getAllScreenValueByApp(
			@PathVariable(value = "id") String id){
		Vector<AtmScreenValue> atmModels = atmScreenValueRepository.findByScreenConfigId(id);
		AtmScreenConfig atmMarque = atmScreenConfigRepository.findById(Integer.valueOf(id)).get();
		List<AtmScreenValue> atmModelResults = new ArrayList<>();
		for (AtmScreenValue model : atmModels) {
			AtmScreenValueId AtmScreenValueId = new AtmScreenValueId();
			AtmScreenValueId.setAtmScreenConfigID(String.valueOf(atmMarque.getAtmScreenConfigCode()));
			//AtmScreenValueId.setAtmScreenConfigID(model.getAtmScreenValueId().getAtmScreenConfigID());
			AtmScreenValueId.setAtmScreenNumID(String.valueOf(model.getAtmScreenValueId().getAtmScreenNumID()));
			System.out.println("id"+model.getAtmScreenValueId().getAtmScreenConfigID());
			AtmScreenValue cassetteCountResult = new AtmScreenValue();
			cassetteCountResult.setAtmScreenValueId(AtmScreenValueId);
         	cassetteCountResult.setAtmScreenParam(model.getAtmScreenParam());
			atmModelResults.add(cassetteCountResult);
		}
		return atmModelResults;
	}



	@PostMapping("getAllScreenValueByAppFiltred")
	public List<AtmScreenValue> getAllScreenValueByAppFiltred(@RequestBody String atmScreenNumID) {
		if(!atmScreenNumID.equals("=")){
			return atmScreenValueRepository.findByatmScreenNumIDFilter(atmScreenNumID.trim());
		} else{
			return (List<AtmScreenValue>) atmScreenValueRepository.findAll();
		}
	}









	@PutMapping("/updateAtmScreenValue/{id}")
	public ResponseEntity<String> updateAtmScreenValue(
			@PathVariable(value = "id") String userId,
			@Valid @RequestBody AtmScreenValue stat) throws ResourceNotFoundException {

		AtmScreenValue statusValus = atmScreenValueRepository.findByatmScreenNumID(String.valueOf(userId));

//    AtmStatusValue stateValue = atmStatusValueRepository.findByAtmStatusCode(userId)
//   .orElseThrow(() -> new ResourceNotFoundException(" not found for this id :: " + userId));
		//enhanced.getEcplCode(employee.getEcplCode());
		stat.getAtmScreenValueId().setAtmScreenConfigID(statusValus.getAtmScreenValueId().getAtmScreenConfigID());

	/*	stat.setStatusNum(statusValus.getStatusNum());
		stat.setAtmStatusCode(statusValus.getAtmStatusCode());
		stat.setNumStatApplication(statusValus.getNumStatApplication());*/

		mergeScreenValue(stat,statusValus);
		atmScreenValueRepository.save(statusValus);
		logger.info("doneeeeeeeeeeeeeeeeeeeee");
		return ResponseEntity.ok().body(gson.toJson("AtmStatusValue updated successfully!"));
	}
	public static <AtmScreenValue> void mergeScreenValue(AtmScreenValue stat,AtmScreenValue statusValus) {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		modelMapper.map(stat, statusValus);
	}


  @GetMapping("/AtmStatusType")
  public ResponseEntity<AtmStatusType> AtmStatusType(@PathVariable(value = "id") String userId
  ) throws ResourceNotFoundException {

    AtmStatusType employee = atmStatusTypeRepository.findByStatusType(userId).orElseThrow(() ->
            new ResourceNotFoundException(" AtmStatusType not found for this id :: " + userId));
    ;

    return ResponseEntity.ok(employee);
  }




  @PostMapping("addEmvCurrencyValue")
  public ResponseEntity<String> addEmvCurrencyValue(@Valid @RequestBody EmvCurrencyValue product) {
    emvCurrencyValueRepository.save(product);

    return ResponseEntity.ok().body(gson.toJson("EmvCurrencyValue added successfully!"));

  }

  @GetMapping("getAllEmvCurrencyValue")
  public List<EmvCurrencyValue> getAllEmvCurrencyValue() {
    return emvCurrencyValueRepository.findAll();
  }

  @GetMapping("/EmvCurrencyValue/{id}")
  public ResponseEntity<EmvCurrencyValue> getEmvCurrencyValueById(
      @PathVariable(value = "id") String atmHardFitnessId)
      throws ResourceNotFoundException {
    EmvCurrencyValue product = emvCurrencyValueRepository.findById(atmHardFitnessId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "EmvCurrencyValue not found for this id :: " + atmHardFitnessId));
    return ResponseEntity.ok().body(product);
  }


  @PutMapping("/updateEmvCurrencyValue/{id}")
  public ResponseEntity<EmvCurrencyValue> updateEmvCurrencyValue(
      @PathVariable(value = "id") String userId,
      @Valid @RequestBody EmvCurrencyValue userDetails) throws ResourceNotFoundException {

    EmvCurrencyValue employee = emvCurrencyValueRepository.findByTransCurrCode(userId)
        .orElseThrow(() ->
            new ResourceNotFoundException(" EmvCurrencyValue not found for this id :: " + userId));

    modelMapper.map(userDetails, employee);
    final EmvCurrencyValue updatedEmployee = emvCurrencyValueRepository.save(employee);
    return ResponseEntity.ok(updatedEmployee);
  }


  @GetMapping("getAtmTransReplyById/{id}")
  public ResponseEntity<AtmTransactionReplyConfiguration> getAtmTransReplyById(
      @PathVariable(value = "id") Integer id)
      throws ResourceNotFoundException {

    AtmConfiguration product = atmConfigurationRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "AtmConfiguration not found for this id :: " + id));
    AtmTransactionReplyConfiguration atmTransactionReplyValue = atmTransactionReplyConfigurationRepository
        .findByAtmTransactionReplyConfigurationCode(product.getAtrcCode());
    return ResponseEntity.ok(atmTransactionReplyValue);
  }

  @GetMapping("getAllAtmTransReply")
  public List<AtmTransactionReplyConfiguration> getAllAtmTransReply()
      throws ResourceNotFoundException {
    return atmTransactionReplyConfigurationRepository.findAll();
  }

/*
  @GetMapping("getEmvTerminalDataById/{id}")
  public ResponseEntity<EmvTerminalData> getEmvTerminalDataById(
      @PathVariable(value = "id") Integer id)
      throws ResourceNotFoundException {

    AtmConfiguration product = atmConfigurationRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "AtmConfiguration not found for this id :: " + id));
    EmvTerminalData atmTransactionReplyValue = emvTerminalDataRepository
        .findByEmvTerminalDataId(product.getEtdCode());
    return ResponseEntity.ok(atmTransactionReplyValue);
  }*/

  @GetMapping("getAllEmvTerminalData")
  public List<EmvTerminalData> getAllEmvTerminalData()
      throws ResourceNotFoundException {
    return emvTerminalDataRepository.findAll();
  }



/*
	@GetMapping("getEmvServiceAppById/{id}")
  public ResponseEntity<EmvTerminalData> getEmvServiceAppById(
      @PathVariable(value = "id") Integer id)
      throws ResourceNotFoundException {

    AtmConfiguration product = atmConfigurationRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "AtmConfiguration not found for this id :: " + id));
    EmvTerminalData atmTransactionReplyValue = emvTerminalDataRepository
        .findByEmvTerminalDataId(product.getEtdCode());
    return ResponseEntity.ok(atmTransactionReplyValue);
  }*/

  @GetMapping("getAllEmvServiceApp")
  public List<EmvTerminalData> getAllEmvServiceApp()
      throws ResourceNotFoundException {
    return emvTerminalDataRepository.findAll();
  }

  @GetMapping("getCassetteMapValue/{id}")
  public ResponseEntity<AtmCassetteMapValue> getCassetteMapValueById(
      @PathVariable(value = "id") Integer id)
      throws ResourceNotFoundException {

    AtmConfiguration product = atmConfigurationRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "AtmConfiguration not found for this id :: " + id));
    AtmCassetteMapValue atmTransactionReplyValue = atmCassetteMapValueRepository
        .findByAcmpCode(product.getAcmpCode()).get();
    return ResponseEntity.ok(atmTransactionReplyValue);
  }

  @GetMapping("getAllConfParamLoad")
  public List<ConfigurationParaLoad> getAllConfParamLoad() {
    return confParamLoadRepository.findAll();
  }

  @GetMapping("getAllImpressionNumber")
  public List<AtmImpressionConfiguration> getAllImpressionNumber() {
    return atmImpressionConfigurationRepository.findAll();
  }

  @GetMapping("getAllAtmScreenConfig")
  public List<AtmScreenConfig> getAllAtmScreenConfig() {
    return atmScreenConfigRepository.findAll();
  }

	@PostMapping("getAllAtmScreenConfigFiltred")
	public List<AtmScreenConfig> getAllAtmScreenConfigFiltred(@RequestBody String libelle) {
		if(!libelle.equals("=")){
			return atmScreenConfigRepository.findAllByLibelle(libelle.trim());
		} else{
			return atmScreenConfigRepository.findAll();
		}
	}

	@GetMapping("/AtmScreenConfiguration/{id}")
	public ResponseEntity<AtmScreenConfig> AtmScreenConfiguration(
			@PathVariable(value = "id") String atmHardFitnessId)
			throws ResourceNotFoundException {
		AtmScreenConfig product = atmScreenConfigRepository
				.findById(Integer.parseInt(atmHardFitnessId))
				.orElseThrow(() -> new ResourceNotFoundException(
						" AtmStatusConfiguration found for this id :: " + atmHardFitnessId));
		return ResponseEntity.ok().body(product);
	}
	@PostMapping("addAtmScreenConfig")
	public ResponseEntity<String> addAtmScreenConfig(@Valid @RequestBody AtmScreenConfig product) {
		atmScreenConfigRepository.save(product);

		return ResponseEntity.ok().body(gson.toJson("Atm Marque added successfully!"));

	}

	@PutMapping("/updateAtmScreenConfig/{id}")
	public ResponseEntity<String> updateAtmScreenConfig(
			@PathVariable(value = "id") String userId,
			@Valid @RequestBody AtmScreenConfig userDetails) throws ResourceNotFoundException {

		AtmScreenConfig atmScreenConfig = atmScreenConfigRepository
				.findById(Integer.parseInt(userId))
				.orElseThrow(() -> new ResourceNotFoundException(" not found for this id :: " + userId));
		userDetails.setAtmScreenConfigCode(atmScreenConfig.getAtmScreenConfigCode());
		mergeScreenConf(userDetails,atmScreenConfig);
		atmScreenConfigRepository.save(atmScreenConfig);


		return ResponseEntity.ok().body(gson.toJson("AtmStatusConfiguration updated successfully!"));
	}
	public static <AtmScreenConfig> void mergeScreenConf(AtmScreenConfig userDetails, AtmScreenConfig atmScreenConfig) {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		modelMapper.map(userDetails, atmScreenConfig);
	}

	@GetMapping("/AtmScreenValue/{id}")
	public ResponseEntity<AtmScreenValue> AtmScreenValue(
			@PathVariable(value = "id") String atmHardFitnessId)
			throws ResourceNotFoundException {
	/*	AtmScreenValue product = atmScreenValueRepository
				.findByScrennNum(atmHardFitnessId);*/
		AtmScreenValue employee = atmScreenValueRepository.findByatmScreenNumID(atmHardFitnessId);

		return ResponseEntity.ok().body(employee);
	}



  @GetMapping("getAllEmvLangConfig")
  public List<EmvLangConfig> getAllEmvLangConfig() {
    return emvLangConfigRepository.findAll();
  }

  @PostMapping("addAtmModel")
  public ResponseEntity<String> addAtmModel(@Valid @RequestBody AtmModel product) {
    atmModelRepository.save(product);

    return ResponseEntity.ok().body(gson.toJson("Atm Model added successfully!"));

  }
  @PostMapping("addAtmMarque")
  public ResponseEntity<String> addAtmMarque(@Valid @RequestBody AtmMarque product) {
    atmMarqueRepository.save(product);

    return ResponseEntity.ok().body(gson.toJson("Atm Marque added successfully!"));

  }

  @GetMapping("getAllAtmMarque")
  public List<AtmMarque> getAllAtmMarque() {
    return atmMarqueRepository.findAll();
  }


  @GetMapping("getAllAtmModel")
  public List<AtmModel> getAllAtmModel() {
    return atmModelRepository.findAll();
  }

 
	@GetMapping("/getAllAtmModel/{id}")
	public List<AtmModel> getAllModelByMarque(
			@PathVariable(value = "id") String id){
		Optional<List<AtmModel>> atmModels = atmModelRepository.findByMarqueCode(Integer.valueOf(id));
		AtmMarque atmMarque = atmMarqueRepository.findById(Integer.valueOf(id)).get();
		List<AtmModel> atmModelResults = new ArrayList<>();
		for (AtmModel model : atmModels.get()) {
			AtmModel cassetteCountResult = new AtmModel();
			cassetteCountResult.setModelCode(model.getModelCode());
			cassetteCountResult.setLibelle(model.getLibelle());
			cassetteCountResult.setMarqueCode(atmMarque.getMarqueCode());
			atmModelResults.add(cassetteCountResult);
		}
		return atmModelResults;
	}

  @GetMapping("eac")
  public List<EmvAidConfig> eac(){
    return emvAidConfigRepository.findAll();
  }

  @GetMapping("esp")
  public List<EmvServiceApp> esp(){
    return emvServiceAppRepository.findAll();
  }

  @GetMapping("ecm")
  public List<AtmCurrencyMapping> ecm(){
//    System.out.println("****** "+code);
    atmCurrencyMappingRepository.findAll().forEach(v ->  System.out.println("====== "+v.getEcmCode()));
    emvCurrencyRepository.findAll().forEach(v ->  System.out.println(">>>>>> "+v.getEcmCode()));
    return atmCurrencyMappingRepository.findLibelleCurr();
  }


//  @PostMapping("addAtmCurrencyMapping")
//  public ResponseEntity<String> addAtmCurrMapping(@Valid @RequestBody AtmCurrencyMapping atmCurrencyMapping) {
//    atmCurrencyMappingRepository.save(atmCurrencyMapping);
//
//    return ResponseEntity.ok().body(gson.toJson("Atm Marque added successfully!"));
//
//  }
   


  @PutMapping("/updateAtmCurrencyMapping/{id}")
  public ResponseEntity<String> updateAtmCurrencyMapping(
          @PathVariable(value = "id") Integer userId,
          @RequestBody AtmCurrencyMapping curr) throws ResourceNotFoundException {

    AtmCurrencyMapping employee = atmCurrencyMappingRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(" not found for this id :: " + userId));
    //enhanced.getEcplCode(employee.getEcplCode());
    mergeCurr(curr,employee);
    atmCurrencyMappingRepository.save(employee);

    return ResponseEntity.ok().body(gson.toJson("EnhancedConfigParamLoad updated successfully!"));
  }
  public static <AtmCurrencyMapping> void mergeCurr( AtmCurrencyMapping curr, AtmCurrencyMapping employee) {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    modelMapper.map(curr, employee);
  }





  @PostMapping("addEmvTerminalData")
  public ResponseEntity<String> addEmvTerminalData(
          @Valid @RequestBody EmvTerminalData emvTerminalData) {
    emvTerminalDataRepository.save(emvTerminalData);

    return ResponseEntity.ok().body(gson.toJson("AtmCassetteMapValue added successfully!"));

  }

  @GetMapping("/emvTerminalData/{id}")
  public ResponseEntity<EmvTerminalData> getEmvTerminalDataById(
          @PathVariable(value = "id") int id)
          throws ResourceNotFoundException {
    EmvTerminalData emvTerminalData = emvTerminalDataRepository.findById(id)
            .orElseThrow(
                    () -> new ResourceNotFoundException(" not found for this id :: " + id));
    logger.info(emvTerminalData.toString());
    return ResponseEntity.ok().body(emvTerminalData);
  }

  @PutMapping("/updateEmvTerminalData/{id}")
  public ResponseEntity<String> updateEmvTerminalData(
          @PathVariable(value = "id") Integer userId,
          @RequestBody EmvTerminalData termData) throws ResourceNotFoundException {

    EmvTerminalData emvTerminalData = emvTerminalDataRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(" not found for this id :: " + userId));
    //enhanced.getEcplCode(employee.getEcplCode());
    termData.setEmvTerminalDataId(emvTerminalData.getEmvTerminalDataId());
    mergeTermData(termData,emvTerminalData);
    emvTerminalDataRepository.save(emvTerminalData);

    return ResponseEntity.ok().body(gson.toJson("EmvTerminalData updated successfully!"));
  }
  public static <EmvTerminalData> void mergeTermData(EmvTerminalData termData, EmvTerminalData emvTerminalData) {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    modelMapper.map(termData, emvTerminalData);
  }





	@PostMapping("getStatTypeFilter")
	public List<AtmStatusType> getStatusTypeFilter(@RequestBody AtmStatusTypeFilter statusAtmFilter) {
		List<AtmStatusType> statConfigs = atmStatusTypeRepository.findAll();



		statConfigs = statConfigs.stream()
				.filter(e -> Strings.isNullOrEmpty(statusAtmFilter.getStatusType())
						|| e.getStatusType().equals(statusAtmFilter.getStatusType()))



				.filter(e -> Strings.isNullOrEmpty(statusAtmFilter.getStatusLibelle())
						|| e.getStatusLibelle().equals(statusAtmFilter.getStatusLibelle()))



				.collect(Collectors.toList());



		return statConfigs;
	}


	@DeleteMapping("deleteAtmStatusType/{id}")
	public ResponseEntity<String> deleteAtmStatusType(@PathVariable(value = "id") Integer atmStatusTypeCode) {
		AtmStatusType  atmStatusType=atmStatusTypeRepository.findByAtmStatusTypeCode(atmStatusTypeCode).get();
		atmStatusTypeRepository.delete(atmStatusType);

		return ResponseEntity.accepted().body(gson.toJson("atmStatusType deleted successfully!"));

	}


	@PostMapping("getAtmConfigurationFilter")
	public List<AtmConfiguration> getAtmConfigurationFilter(@RequestBody AtmConfigurationFilter atmConfigurationFilter) {
		List<AtmConfiguration> statConfigs = atmConfigurationRepository.findAll();



		statConfigs = statConfigs.stream()
				.filter(e -> Strings.isNullOrEmpty(atmConfigurationFilter.getAmConfigLibelle())
						|| e.getAmConfigLibelle().equals(atmConfigurationFilter.getAmConfigLibelle()))

				.collect(Collectors.toList());
//		for (AtmConfiguration tr : statConfigs
//		){
//			AtmConfiguration tpeRequestDisplay = new AtmConfiguration();
//		}


		return statConfigs;
	}





	@PostMapping("getListCplFilter")
	public List<ConfigurationParaLoadDisplay> getListCplFilter(@RequestBody ConfigParamLoadFilter configParamLoadFilter) {

		List<ConfigurationParaLoad> tpeRequest = configurationParaLoadRepository.findAll();
		List<ConfigurationParaLoad> result = new ArrayList<>();

		List<ConfigurationParaLoadDisplay> tpeRequestDisplays = new ArrayList<>();

/*
 tpeRequest = tpeRequestRepository.findAll().stream().filter(
        p -> requestTpeFilter.getAgence().equals(String.valueOf(p.getAgence()))
            || requestTpeFilter.getNumCompte().equals(p.getAccountNumber()) || requestTpeFilter.getStatus().equals(String.valueOf(p.getStatus())))
        .collect(Collectors.toList());
*/

		tpeRequest = tpeRequest.stream()
				.filter(e -> Strings.isNullOrEmpty(configParamLoadFilter.getLibelle()) || String
						.valueOf(e.getLibelle())
						.equals(configParamLoadFilter.getLibelle()))
				.filter(e -> Strings.isNullOrEmpty(configParamLoadFilter.getCameraValue()) || e.getCameraValue()
						.equals(configParamLoadFilter.getCameraValue()))
				.filter(e -> Strings.isNullOrEmpty(configParamLoadFilter.getFieldMValue()) || String
						.valueOf(e.getFieldMValue())
						.equals(configParamLoadFilter.getFieldMValue()))


				.collect(Collectors.toList());
		for (ConfigurationParaLoad tr : tpeRequest
		) {
			ConfigurationParaLoadDisplay tpeRequestDisplay = new ConfigurationParaLoadDisplay();

			FieldH fieldH = fieldHRepository.findById(tr.getCameraValue()).get();
			tpeRequestDisplay.setCameraValue(fieldH.getLibelle());
			FieldM fieldM = fieldMRepository.findById(tr.getFieldMValue()).get();
			tpeRequestDisplay.setFieldMValue(fieldM.getLibelle());
			tpeRequestDisplay.setFieldI(tr.getFieldI());
			tpeRequestDisplay.setFieldJ(tr.getFieldJ());
			tpeRequestDisplay.setFieldK(tr.getFieldK());
			tpeRequestDisplay.setFieldL(tr.getFieldL());
			tpeRequestDisplay.setFieldN(tr.getFieldN());
			tpeRequestDisplay.setTimer00(tr.getTimer00());
			tpeRequestDisplay.setTimer01(tr.getTimer01());
			tpeRequestDisplay.setTimer02(tr.getTimer02());
			tpeRequestDisplay.setTimer03(tr.getTimer03());
			tpeRequestDisplay.setTimer04(tr.getTimer04());
			tpeRequestDisplay.setTimer05(tr.getTimer05());
			tpeRequestDisplay.setTimer06(tr.getTimer06());
			tpeRequestDisplay.setTimer07(tr.getTimer07());
			tpeRequestDisplay.setTimer08(tr.getTimer08());
			tpeRequestDisplay.setTimer09(tr.getTimer09());
			tpeRequestDisplay.setTimer10(tr.getTimer10());
			tpeRequestDisplay.setTimer60(tr.getTimer60());
			tpeRequestDisplay.setTimer61(tr.getTimer61());
			tpeRequestDisplay.setTimer63(tr.getTimer63());
			tpeRequestDisplay.setTimer68(tr.getTimer68());
			tpeRequestDisplay.setTimer69(tr.getTimer69());
			tpeRequestDisplay.setTimer72(tr.getTimer72());
			tpeRequestDisplay.setTimer77(tr.getTimer77());
			tpeRequestDisplay.setTimer78(tr.getTimer78());
			tpeRequestDisplay.setTimer87(tr.getTimer87());
			tpeRequestDisplay.setTimer91(tr.getTimer91());
			tpeRequestDisplay.setTimer92(tr.getTimer92());
			tpeRequestDisplay.setTimer94(tr.getTimer94());
			tpeRequestDisplay.setTimer95(tr.getTimer95());
			tpeRequestDisplay.setTimer96(tr.getTimer96());
			tpeRequestDisplay.setTimer97(tr.getTimer97());
			tpeRequestDisplay.setAtmConfigParamLoadCode(tr.getAtmConfigParamLoadCode());
			tpeRequestDisplay.setLibelle(tr.getLibelle());
//			tpeRequestDisplay.setCity(tr.getCity());
//			tpeRequestDisplay.setCodeZip(tr.getCodeZip());
//			tpeRequestDisplay.setCommissionInterNational(tr.getCommissionInterNational());
//			tpeRequestDisplay.setCommissionNational(tr.getCommissionNational());
//			tpeRequestDisplay.setCountry(tr.getCountry());
//			TpeRequestStatus tpeRequestStat = tpeRequestStatusRepository.findByStatusCode(tr.getStatus());
//			tpeRequestDisplay.setStatus(tpeRequestStat.getLibelle());
//			tpeRequestDisplay.setDateCreation(tr.getDateCreation());
//			tpeRequestDisplay.setDateDecision(tr.getDateDecision());
//			tpeRequestDisplay.setNombreTPE(tr.getNombreTPE());
//			tpeRequestDisplay.setCodeZip(tr.getCodeZip());
//			tpeRequestDisplay.setPhone(tr.getPhone());
//			tpeRequestDisplay.setRequestCode(tr.getRequestCode());
//			tpeRequestDisplay.setUserName(tr.getUserName());

			tpeRequestDisplays.add(tpeRequestDisplay);
		}
		return tpeRequestDisplays;

	}






	 





	@PostMapping("getAllPageAtmMarque")
	public Page<AtmMarque> getAllPageAtmMarque(@RequestBody PosRequestLibelle filter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, 
			@RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir

	){
		
	
		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);

		orders.add(order1);
		return atmMarqueRepository.getPageAtmMarque(PageRequest.of(page, size, Sort.by(orders)),
				filter.getLibelle().trim());
		
		

	}

	
	@PostMapping("getAllPageAtmModel")
	public Page<PosRequestModel> getAllPageAtmModel(@RequestBody PosRequestLibelle filter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, 
			@RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir

	){
		
		
		List<PosRequestModel> list = new ArrayList<>();

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);

		orders.add(order1);
		Page<AtmModel> page1 = atmModelRepository.getPageAtmModel(PageRequest.of(page, size, Sort.by(orders)),
				filter.getLibelle().trim());
		for (AtmModel tr : page1.getContent()) {
			PosRequestModel posRequestModel = new PosRequestModel();
			posRequestModel.setCodeModel(tr.getModelCode());
			AtmMarque atmMarque = atmMarqueRepository.getOne(tr.getMarqueCode());
			posRequestModel.setMarque(atmMarque.getLibelle());
			posRequestModel.setLibelle(tr.getLibelle());
			list.add(posRequestModel);

		}

		Page<PosRequestModel> pages = new PageImpl<PosRequestModel>(list, PageRequest.of(page, size, Sort.by(orders)),
				page1.getTotalElements());
		return pages;

	}
	
	
	@PostMapping("AddAtmMarque")
	public ResponseEntity<String> AddAtmMarque(@RequestBody AtmMarque atmMarque) {
		AtmMarque atmM = atmMarqueRepository.getAtmMarqueBylibelle(atmMarque.getLibelle());
		if (atmM == null) {
			atmMarqueRepository.save(atmMarque);
			return ResponseEntity.ok().body(gson.toJson("posMarque créé"));
		} else {
			return new ResponseEntity<String>(" Marque of ATM With label " + atmMarque.getLibelle() + " existed",
					HttpStatus.BAD_REQUEST);
		}

	}

	@PutMapping("updateAtmMarque/{id}")
	public ResponseEntity<String> updateAtmMarque(@PathVariable(value = "id") Integer idMarque,
			@RequestBody AtmMarque atmMarque) {

		AtmMarque atmMarques = atmMarqueRepository.getOne(idMarque);
		atmMarques.setLibelle(atmMarque.getLibelle());
		atmMarqueRepository.save(atmMarques);
		return ResponseEntity.ok().body(gson.toJson("atmMarque update"));

	}

	
	@PostMapping("AddAtmModel")
	public ResponseEntity<String> AddAtmModel(@RequestBody AtmModel atmModel) {
		AtmModel pm = atmModelRepository.getOneByLibelle(atmModel.getLibelle());
		if (pm == null) {

			atmModel = atmModelRepository.save(atmModel);

			return ResponseEntity.ok().body(gson.toJson("AtmModel créé"));
		} else {
			return new ResponseEntity<String>(" Model of ATM With label " + atmModel.getLibelle() + " existed",
					HttpStatus.BAD_REQUEST);
		}

	}

	@PutMapping("updateAtmModel/{id}")
	public ResponseEntity<String> updateAtmModel(@PathVariable(value = "id") Integer idModel,
			@RequestBody AtmModel atmModel) {

		AtmModel atmModels = atmModelRepository.getOne(idModel);
		atmModels.setLibelle(atmModel.getLibelle());
		atmModels.setMarqueCode(atmModel.getMarqueCode());
		atmModelRepository.save(atmModels);
		return ResponseEntity.ok().body(gson.toJson("atmModel update"));

	}
	
	@GetMapping("getAllAtmMarques")
	public List<AtmMarque> getAllAtmMarques(){

		return atmMarqueRepository.findAll();
	}
	
	@GetMapping("getOneAtmModel/{id}")
	public AtmModel getOneAtmModel(@PathVariable(value = "id") Integer idModel) {
		AtmModel atmModel = atmModelRepository.getOneByCode(idModel);

		return atmModel;
	}

}
