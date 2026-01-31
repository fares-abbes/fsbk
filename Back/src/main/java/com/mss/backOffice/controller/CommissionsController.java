package com.mss.backOffice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.mss.unified.entities.CommissionSwitch;
import com.mss.unified.repositories.CommissionSwitchRepository;

@RestController
@RequestMapping("commissions")
public class CommissionsController {
	@Autowired
	CommissionSwitchRepository csr;
	private static final Gson gson = new Gson();

	@GetMapping("getswitchCommissions")
	public List<CommissionSwitch> getSwitchCommissions() {
		return csr.findAll();
	}

	@PutMapping("updateCommissions")
	public ResponseEntity<?> updateSwitchCommissions(@RequestBody CommissionSwitch cs) {
		csr.save(cs);
		return ResponseEntity.accepted().body(gson.toJson("entity updated successfully!"));
	}

	@PutMapping("updateCommission")
	public ResponseEntity<?> updateSwitchCommission(@RequestBody CommissionSwitch cs) {
		csr.save(cs);
		return ResponseEntity.accepted().body(gson.toJson("entity updated successfully!"));
	}

}
