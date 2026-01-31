package com.mss.backOffice.controller;

import javax.validation.Valid;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.unified.entities.Periodicity;
import com.mss.unified.repositories.PeriodicityTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("periodicity")
public class risqPeriodicityController {

    @Autowired
    private PeriodicityTypeRepository typeRepository;

    /*@PostMapping("addPeriodicity")
    public ResponseEntity<String> addPeriodicity(@Valid @RequestBody RiskPeriodicity risq)
        throws ResourceNotFoundException {
        riskRepository.save(risq);

      return ResponseEntity.ok().body("Product added successfully!");

    }*/
    @PostMapping("addType")
    public ResponseEntity<String> addTypePeriodicity(@Valid @RequestBody Periodicity risq) throws ResourceNotFoundException {
        typeRepository.save(risq);
        return ResponseEntity.ok().body("Type added successfully!");
    }

}
