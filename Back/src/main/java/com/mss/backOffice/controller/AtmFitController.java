package com.mss.backOffice.controller;

import com.google.gson.Gson;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.backOffice.request.AddAtmFitApplication;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import com.mss.unified.entities.AtmFitsApplication;
import com.mss.unified.entities.AtmFitsValue;
import com.mss.unified.repositories.AtmFitsApplicationRepository;
import com.mss.unified.repositories.AtmFitsValueRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/atmFit")
public class AtmFitController {

	@Autowired
	AtmFitsApplicationRepository atmFitsApplicationRepository;

	private static final Gson gson = new Gson();
	private ModelMapper modelMapper;

	@Autowired
	AtmFitsValueRepository atmFitsValueRepository;
	private static final Logger logger = LoggerFactory.getLogger(BinController.class);

	@PostMapping("addAtmFitsApplication")
	public ResponseEntity<String> addAtmFitsApplication(@Valid @RequestBody AtmFitsApplication product) {
		//logger.info(product.toString());

		atmFitsApplicationRepository.save(product);

		return ResponseEntity.ok().body(gson.toJson("AtmFitsApplication added successfully!"));

	}

	@PostMapping("getAllatmfitapplicationFiltred")
	public List<AtmFitsApplication> getAllatmfitapplicationFiltred(@RequestBody String atmFitsAppLibelle) {
		if(!atmFitsAppLibelle.equals("=")){
			return atmFitsApplicationRepository.findAllByatmFitsAppLibelle(atmFitsAppLibelle.trim());
		} else{
			return atmFitsApplicationRepository.findAll();
		}
	}

	@PostMapping("getAllatmfitValueFiltred")
	public List<AtmFitsValue> getAllatmfitValueFiltred(@RequestBody String fitNumber) {
		if(!fitNumber.equals("=")){
			return atmFitsValueRepository.findAllByfitNumber(fitNumber.trim());
		} else{
			return atmFitsValueRepository.findAll();
		}
	}


	@GetMapping("getAllAtmFitsApplication")
	public List<AtmFitsApplication> getAllAtmFitsApplication() {
		return atmFitsApplicationRepository.findAll();
	}

	@GetMapping("/getAtmFitsApplicationById/{id}")
	public ResponseEntity<AtmFitsApplication> getAtmFitsApplicationById(
			@PathVariable(value = "id") int atmFitsApp) throws ResourceNotFoundException {
		AtmFitsApplication product = atmFitsApplicationRepository.findByAtmFitsApp(atmFitsApp)
				.orElseThrow(() -> new ResourceNotFoundException(
						"AtmFitsApplication not found for this id :: " + atmFitsApp));
		logger.info(product.toString());
		return ResponseEntity.ok().body(product);
	}


// 	@GetMapping("/getAtmfitapplicationvalue/{id}")
// 	public ResponseEntity<AtmFitsValue> getAtmfitapplicationvalue(@PathVariable(value = "id") int atmFitsApp)
//			throws ResourceNotFoundException {
//		AtmFitsValue product = atmFitsValueRepository.findByFitNumber(atmFitsApp).orElseThrow(
//				() -> new ResourceNotFoundException("AtmFitsValue not found for this id :: " + atmFitsApp));
//		logger.info(product.toString());
//		return ResponseEntity.ok().body(product);
//	}

	@PutMapping("/updateAtmFitsApplication/{id}")
	public ResponseEntity<AtmFitsApplication> updateAtmFitsApplication(@PathVariable(value = "id") int userId,
																	   @Valid @RequestBody AtmFitsApplication userDetails) throws ResourceNotFoundException {
		AtmFitsApplication employee = atmFitsApplicationRepository.findByAtmFitsApp(userId)
				// AtmFitsApplication employee =
				// atmFitsApplicationRepository.findByAtmFitsApp(Integer.parseInt(userId))
				.orElseThrow(() -> new ResourceNotFoundException(" not found for this id :: " + userId));
		// modelMapper.getConfiguration()
		// .setSourceNamingConvention(NamingConventions.NONE)
		// .setDestinationNamingConvention(NamingConventions.NONE);
		userDetails.setAtmFitsApp(employee.getAtmFitsApp());
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		modelMapper.map(userDetails, employee);
		// employee.setAtmFitsApp(userDetails.getAtmFitsApp());
		// employee.setAtmFitsAppLibelle(userDetails.getAtmFitsAppLibelle());

		final AtmFitsApplication updatedEmployee = atmFitsApplicationRepository.save(employee);
		logger.info(updatedEmployee.toString());
		return ResponseEntity.ok(updatedEmployee);
	}



//	@PostMapping("addAtmFitsValue")
//	public ResponseEntity<String> addAtmFitsValue(@Valid @RequestBody AtmFitsValue product) {
//		atmFitsValueRepository.save(product);
//		logger.info(product.toString());
//		return ResponseEntity.ok().body(gson.toJson("AtmFitsValue added successfully!"));
//
//	}

	@PostMapping("addAtmFitsValue")
	public ResponseEntity<String> addAtmFitsValue(@Valid @RequestBody AtmFitsValue product) {
		logger.info(product.toString());
		AtmFitsValue atmFitsValue = new AtmFitsValue();
		AtmFitsApplication atmFitsApplication =new AtmFitsApplication();


		if (atmFitsApplicationRepository.existsByAtmFitsApp(Integer.parseInt(product.getAtmFitsApp())) )

		{
			if(atmFitsValueRepository.existsByfitNumberAndAtmFitsApp(product.getFitNumber(), product.getAtmFitsApp()) )
			{
				return new ResponseEntity<String>("Fail -> fitNumber is already in use!",
						HttpStatus.BAD_REQUEST);

			}
		}

//	                    {
//
//	                return new ResponseEntity<String>("Fail -> fitNumber is already in use!",
//	                        HttpStatus.BAD_REQUEST);
//	                 }




		atmFitsValueRepository.save(product);
		logger.info(product.toString());
		return ResponseEntity.ok().body(gson.toJson("AtmFitsValue added successfully!"));

	}

	@GetMapping("getAllAtmFitsValue")
	public List<AtmFitsValue> getAllAtmFitsValue() {


		return atmFitsValueRepository.findAll();
	}


	@GetMapping("/AtmFitsValue/{id}")
	public ResponseEntity<AtmFitsValue> getAtmFitsValueById(@PathVariable(value = "id") Integer id)
			throws ResourceNotFoundException {

		AtmFitsValue product = atmFitsValueRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException("AtmFitsValue not found for this id :: " + id));
		logger.info(product.toString());
		return ResponseEntity.ok().body(product);
	}

	@GetMapping("/AtmFitsValueByAtmFitsApp/{id}")
	public List<AtmFitsValue> getAtmFitsValueByAtmFitsApp(@PathVariable(value = "id") int atmFitsApp)
			throws ResourceNotFoundException {
		List<AtmFitsValue> product = atmFitsValueRepository.findByAtmFitsApp(String.valueOf(atmFitsApp));
		//orElseThrow(
		//() -> new ResourceNotFoundException("AtmFitsValue not found for this id :: " + atmFitsApp));
		logger.info(product.toString());
		//return ResponseEntity.ok().body(product);
		return product;
	}


	@PutMapping("/updateAtmFitsValue/{id}")
	public ResponseEntity<AtmFitsValue> updateAtmFitsValue(@PathVariable(value = "id") Integer userId,
														   @Valid @RequestBody AtmFitsValue userDetails) throws ResourceNotFoundException {
		AtmFitsValue employee = atmFitsValueRepository.findById(userId)
				// AtmFitsValue employee =
				// atmFitsValueRepository.findByFitNumber(Integer.parseInt(userId)
				.orElseThrow(() -> new ResourceNotFoundException(" not found for this id :: " + userId));
		userDetails.setId(employee.getId());
		userDetails.setAtmFitsApp(employee.getAtmFitsApp());
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		modelMapper.map(userDetails, employee);
		final AtmFitsValue updatedEmployee = atmFitsValueRepository.save(employee);
		logger.info(updatedEmployee.toString());
		return ResponseEntity.ok(updatedEmployee);
	}

	@GetMapping("getAllFitApplication")
	public List<AddAtmFitApplication> getAllFitApplication() {
//	    System.out.println("****** "+code);
		List<AddAtmFitApplication> addAtmFitApp = new ArrayList<>();


		for(AtmFitsApplication atmfitapp : atmFitsApplicationRepository.findAll())
		{
			AddAtmFitApplication addAtmFitApplication = new AddAtmFitApplication();
			addAtmFitApplication.setAtmFitsApp(atmfitapp.getAtmFitsApp());
			addAtmFitApplication.setAtmFitsAppLibelle(atmfitapp.getAtmFitsAppLibelle());
			addAtmFitApplication.setAtmFitsValue(atmFitsValueRepository.findByAtmFitsApp(String.valueOf(atmfitapp.getAtmFitsApp())));
			addAtmFitApp.add(addAtmFitApplication);


		}

		return addAtmFitApp;
	}


//	@GetMapping("getAllFitApplication")
//	public List<AtmFitsApplication> getAllFitApplication() {
//		return atmFitsApplicationRepository.findAll();
//	}
//	@GetMapping("/AtmFitsApplication/{id}")
//	public ResponseEntity<AtmFitsApplication> findByAtmFitsApp(@PathVariable(value = "id") int code)
//			throws ResourceNotFoundException {
//		AtmFitsApplication product = atmFitsApplicationRepository.findByAtmFitsApp(code).orElseThrow(
//				() -> new ResourceNotFoundException("AtmFitsValue not found for this id :: " + code));
//		logger.info(product.toString());
//		return ResponseEntity.ok().body(product);
//	}
}