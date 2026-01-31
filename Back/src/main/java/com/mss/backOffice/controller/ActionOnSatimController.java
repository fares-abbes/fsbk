package com.mss.backOffice.controller;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Throwables;
import com.mss.backOffice.services.SatimService;
import com.mss.unified.entities.ActionOnSatim;
import com.mss.unified.repositories.ActionOnSatimRepository;

@RestController
@RequestMapping("actionOnSatim")
public class ActionOnSatimController {

	@Autowired
	private SatimService satimService;
	@Autowired
	private ActionOnSatimRepository actionOnSatimRepository;
	private static final Logger LOG = LoggerFactory.getLogger(ActionOnSatimController.class);

	@GetMapping("getAllActionsOnSatim")
	public List<ActionOnSatim> getAllActionsOnSatim() {

		return actionOnSatimRepository.findAll();
	}

	@GetMapping("signOn")
	public void signOn() {
		ActionOnSatim batch = actionOnSatimRepository.findByKeys("signOn").get();
		batch.setActionStatus(0);
		batch.setActionDate(new Date());
		batch.setError(null);
		batch.setErrorStackTrace(null);
		actionOnSatimRepository.save(batch);

		try {
			String res = satimService.sendMessageSignOn();
			LOG.info("res signOn => {}", res);
			if (res.equals("ok")) {
				actionOnSatimRepository.updateFinishBatch("signOn", 1, new Date());
			} else {
				actionOnSatimRepository.updateStatusAndErrorBatch("signOn", 2, "No response", new Date(),
						"No response");
			}
		} catch (Exception exception) {

			String stackTrace = Throwables.getStackTraceAsString(exception);
			LOG.info("Exception");
			LOG.info(stackTrace);

			if (stackTrace.length() > 4000)
				stackTrace = stackTrace.substring(0, 3999);

			actionOnSatimRepository.updateStatusAndErrorBatch("signOn", 2,
					exception.getMessage() == null ? exception.toString() : exception.getMessage(), new Date(),
					stackTrace);

		}

	}

	@GetMapping("signOff")
	public void signOff() {

		ActionOnSatim batch = actionOnSatimRepository.findByKeys("signOff").get();
		batch.setActionStatus(0);
		batch.setActionDate(new Date());
		batch.setError(null);
		batch.setErrorStackTrace(null);
		actionOnSatimRepository.save(batch);

		try {
			String res = satimService.sendMessageSignOff();
			LOG.info("res signOff => {}", res);
			if (res.equals("ok")) {
				actionOnSatimRepository.updateFinishBatch("signOff", 1, new Date());
			} else {
				actionOnSatimRepository.updateStatusAndErrorBatch("signOff", 2, "No response", new Date(),
						"No response");
			}
		} catch (Exception exception) {

			String stackTrace = Throwables.getStackTraceAsString(exception);
			LOG.info("Exception");
			LOG.info(stackTrace);

			if (stackTrace.length() > 4000)
				stackTrace = stackTrace.substring(0, 3999);

			actionOnSatimRepository.updateStatusAndErrorBatch("signOff", 2,
					exception.getMessage() == null ? exception.toString() : exception.getMessage(), new Date(),
					stackTrace);

		}

	}

}
