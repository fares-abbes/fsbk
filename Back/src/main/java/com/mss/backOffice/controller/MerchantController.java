package com.mss.backOffice.controller;


import com.google.gson.Gson;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.unified.entities.Merchant;
import com.mss.unified.entities.MerchantStatus;
import com.mss.unified.entities.TpeRequest;
import com.mss.unified.repositories.MerchantRepository;
import com.mss.unified.repositories.MerchantStatusRepository;
import com.mss.backOffice.request.MerchantRequest;
import java.util.Date;
import java.util.List;
import javax.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("merchant")
public
class MerchantController {

  @Autowired
  MerchantRepository merchantRepository;
  @Autowired
  MerchantStatusRepository merchantStatusRepository;
  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  private ModelMapper modelMapper;
  private static final Gson gson = new Gson();
  @PostMapping("addMerchant")
  public ResponseEntity<String> addMerchant(@Valid @RequestBody MerchantRequest request) {
    logger.info(request.toString());
    Merchant merchant = new Merchant("1", request.getMerchantId(), request.getMerchantLibelle(),
        request.getCity(), request.getCountry(), request.getCodeZip(), request.getPhone(),
        new Date(),request.getAddress());
merchant.setCommissionNational(request.getCommissionNational());
merchant.setCommissionInternational(request.getCommissionInternational());
    merchantRepository.save(merchant);
logger.info(merchant.toString());
    return ResponseEntity.accepted().body(gson.toJson("Merchant added successfully!"));

  }


  @GetMapping("getAllMerchant")
  public List<Merchant> getAllMerchant() {
    return merchantRepository.findAll();
  }
  
  

  @DeleteMapping("/delete/{id}")
  public ResponseEntity<String>  delete(@PathVariable(value = "idMerchant") String idCustomer)  {

    Merchant customer =
        merchantRepository.findByMerchantCode(Integer.parseInt(idCustomer));

    merchantRepository.delete(customer);

    return ResponseEntity.accepted().body(gson.toJson("Merchant deleted successfully!"));


  }

  @GetMapping("/merchant/{id}")
  public ResponseEntity<Merchant> getMerchantById(
      @PathVariable(value = "idMerchant") String idCustomer)
      throws ResourceNotFoundException {
    Merchant user =
        merchantRepository.findByMerchantCode(Integer.parseInt(idCustomer));
logger.info(user.toString());
    return ResponseEntity.ok().body(user);
  }

  @PutMapping("/merchant/{id}")
  public ResponseEntity<Merchant> updateMerchant(
      @PathVariable(value = "idMerchant") String idCustomer,
      @Valid @RequestBody Merchant employeeDetails) throws ResourceNotFoundException {
    logger.info(employeeDetails.toString());
    Merchant customer = merchantRepository.findByMerchantCode(Integer.parseInt(idCustomer));
    modelMapper.map(employeeDetails, customer);

    final Merchant updatedEmployee = merchantRepository.save(customer);
    logger.info(updatedEmployee.toString());
    return ResponseEntity.ok(updatedEmployee);
  }

  @GetMapping("getMerchantStatus")
  public List<MerchantStatus> getMerchantStatus() {
    return merchantStatusRepository.findAll();
  }

}
