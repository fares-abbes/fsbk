package com.mss.backOffice.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.backOffice.request.UpdateExchangeRate;
import com.mss.unified.entities.Account;
import com.mss.unified.entities.Customer;
import com.mss.unified.entities.ExchangeRate;
import com.mss.unified.repositories.ExchangeRateRepository;

@RestController
@RequestMapping("exchangeRate")
public class ExchangeRateController {
	private static final Logger logger = LoggerFactory.getLogger(ExchangeRateController.class);

	@Autowired
	private ExchangeRateRepository exchangeRateRepository;
	private static final Gson gson = new Gson();

	@PostMapping("addExchangeRate")
	public ResponseEntity<ExchangeRate> addExchangeRate(@RequestBody ExchangeRate exchangeRate) {
		exchangeRateRepository.save(exchangeRate);
		return ResponseEntity.ok().body(exchangeRate);
	}

	@GetMapping("/getAll")
	public List<ExchangeRate> getAll() {

		return exchangeRateRepository.findAll();

	}

	@GetMapping("/getExchangeRateById/{id}")
	public ResponseEntity<ExchangeRate> getExchangeRate(@PathVariable(value = "id") Integer id) {
		Optional<ExchangeRate> exchangeRate = exchangeRateRepository.findById(id);
		if (exchangeRate.isPresent())
			return ResponseEntity.ok().body(exchangeRate.get());
			
		else
			return null;
	}

	@PutMapping("editExchangeRate/{id}")
	public ResponseEntity<String> editExchangeRate(@RequestBody ExchangeRate updateRequest,
			@PathVariable(value = "id") Integer id) {
		Optional<ExchangeRate> exchangeRate = exchangeRateRepository.findById(id);

		if (exchangeRate.isPresent()) {
			exchangeRate.get().setCost(updateRequest.getCost());
			exchangeRate.get().setCurrencyBeginCode(updateRequest.getCurrencyBeginCode());
			exchangeRate.get().setCurrencyBeginLabel(updateRequest.getCurrencyBeginLabel());
			exchangeRate.get().setCurrencyEndCode(updateRequest.getCurrencyEndCode());
			exchangeRate.get().setCurrencyEndLabel(updateRequest.getCurrencyEndLabel());
			exchangeRate.get().setExpo(updateRequest.getExpo());

			exchangeRateRepository.save(exchangeRate.get());
			return ResponseEntity.ok().body(gson.toJson("ExchangeRate updated successfully!"));
		}
		return ResponseEntity.badRequest().body(gson.toJson("ExchangeRate Not Found!"));
	}
	
	
	@PostMapping("exchange-rate")
	public ResponseEntity<String> exchangeRate(@RequestBody List<UpdateExchangeRate> exchangeRates) {
		
		List<ExchangeRate> rates=new ArrayList<ExchangeRate>();
		for(UpdateExchangeRate rate : exchangeRates) {
			logger.info("currency code : "+rate.getCurrencyCode() + " label : "+rate.getLabel());
			Optional<ExchangeRate> exchangeRateOpt = exchangeRateRepository.getExchangeRate( rate.getCurrencyCode(), "012");
			if (exchangeRateOpt.isPresent()) {
				exchangeRateOpt.get().setCost(rate.getCost());
				rates.add(exchangeRateOpt.get());
				
				return ResponseEntity.ok().body(gson.toJson("ExchangeRate updated successfully!"));

			}else {
				ExchangeRate exchange= new ExchangeRate();
				exchange.setCost(rate.getCost());
				exchange.setCurrencyBeginCode(rate.getCurrencyCode());
				exchange.setCurrencyBeginLabel(rate.getLabel());
				exchange.setCurrencyEndCode("012");
				exchange.setCurrencyEndLabel("DZD");
				exchange.setExpo(2);
				rates.add(exchange);
				

			}
			
			
		}
		if (rates.size()>0) {
			exchangeRateRepository.saveAll(rates);
		}
		
		return ResponseEntity.ok().body(gson.toJson("ExchangeRate updated successfully!"));
		
	
	}

}
