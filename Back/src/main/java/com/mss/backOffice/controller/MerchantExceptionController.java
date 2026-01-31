package com.mss.backOffice.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.google.gson.Gson;
import com.mss.unified.entities.Account;
import com.mss.unified.entities.DetailRiskManagment;
import com.mss.unified.entities.GlobalRiskManagement;
import com.mss.unified.entities.MerchantException;
import com.mss.unified.repositories.MerchantExceptionRepository;

@RestController
@RequestMapping("merchantException")
public class MerchantExceptionController {
	private static final Gson gson = new Gson();
	@Autowired
	MerchantExceptionRepository mer;

	@GetMapping("/getAllmerchantException")
	public Page<MerchantException> findAllMerchantException(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "sortOn", defaultValue = "merchantId") String sortOn,
			@RequestParam(name = "dir", defaultValue = "asc") String dir) {
		sortOn = sortOn.substring(0, 1).toLowerCase() + sortOn.substring(1);
		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);
		orders.add(order1);

		return mer.findAll(PageRequest.of(page, size, Sort.by(orders)));

	}

	@GetMapping("/findById")
	public ResponseEntity<?> findMerchandExceptionByID(@PathVariable(value = "id") Integer id) {
		return ResponseEntity.ok().body(mer.findById(id).get());

	}

	@PutMapping("/updateMerchandException")
	public ResponseEntity<?> updateMerchandException(@RequestBody MerchantException mex) {

		mer.save(mex);

		return null;

	}

	@DeleteMapping("/deletMerchandException/{id}")
	public ResponseEntity<String> deleteMerchandException(@PathVariable(value = "id") Integer id) {
		mer.deleteById(id);
		return ResponseEntity.ok().body(gson.toJson("Merchand Exceptino  deleted  successfully!"));

	}

	@PostMapping("/addMerchand")
	public ResponseEntity<MerchantException> addMerchandException(@RequestBody MerchantException mex) {
		MerchantException savedElement = mer.save(mex);
		return ResponseEntity.ok().body(savedElement);
	}

}
