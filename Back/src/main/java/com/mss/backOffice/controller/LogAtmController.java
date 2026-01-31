package com.mss.backOffice.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mss.backOffice.request.AccountDisplay;
import com.mss.backOffice.request.AccountDisplayFilter;
import com.mss.backOffice.request.AddAtmImpressionValueRequest;
import com.mss.backOffice.request.AtmImpressionValueFilter;
import com.mss.backOffice.request.AtmLogDisplayFilter;
import com.mss.backOffice.request.AtmLogFilterRequest;
import com.mss.unified.entities.Account;
import com.mss.unified.entities.AtmImpressionConfiguration;
import com.mss.unified.entities.AtmJournalConfig;
import com.mss.unified.entities.AtmPrintJournal;
import com.mss.unified.entities.AtmPrintJournalId;
import com.mss.unified.entities.AtmPrintTicket;
import com.mss.unified.entities.AtmPrintTicketId;
import com.mss.unified.entities.AtmPrintTicketVariableText;
import com.mss.unified.entities.ElectoricJournal;
import com.mss.unified.entities.JournalGab;
import com.mss.unified.entities.JournalGabAtm;
import com.mss.unified.entities.LogAtm;
import com.mss.unified.entities.User;
import com.mss.unified.repositories.AtmImpressionConfigurationRepository;
import com.mss.unified.repositories.AtmJournalConfigRepository;
import com.mss.unified.repositories.AtmPrintJournalRepository;
import com.mss.unified.repositories.AtmPrintTicketRepository;
import com.mss.unified.repositories.AtmPrintTicketVariableTextRepository;
import com.mss.unified.repositories.ElectoricJournalRepository;
import com.mss.unified.repositories.JournalGabAtmRepository;

import com.mss.unified.repositories.JournalGabRepository;
import com.mss.unified.repositories.LogAtmRepository;


@RestController
@RequestMapping("logatm")
public class LogAtmController {
	  private static final Logger logger = LoggerFactory.getLogger(LogAtmController.class);

	 @Autowired
	 private LogAtmRepository logAtmRepository;
	 @Autowired
	 private ElectoricJournalRepository electoricJournalRepository;
	 @Autowired
	 private AtmPrintTicketRepository atmPrintTicketRepository;
	 @Autowired
	 private AtmPrintJournalRepository atmPrintJournalRepository;
	 @Autowired
	 private AtmPrintTicketVariableTextRepository atmPrintTicketVariableTextRepository;
	 
	 @Autowired
	 private JournalGabRepository journalGabRepository;
	 
	 @Autowired
	 private JournalGabAtmRepository journalGabAtmRepository;
	 
	 @Autowired
	 private AtmImpressionConfigurationRepository atmImpressionConfigurationRepository;
	 @Autowired
	 private AtmJournalConfigRepository atmJournalConfigRepository;
	 @PostMapping("getAtmLogs")
		public Page<LogAtm> getAllAtmLogs(@RequestBody AtmLogFilterRequest atmLogFilter,
				@RequestParam(name = "page", defaultValue = "0") int page,
				@RequestParam(name = "size", defaultValue = "10") int size,
				@RequestParam(name = "sortOn") String sortOn,
				@RequestParam(name = "dir") String dir

				) {
		  	logger.info(atmLogFilter.toString());
		  	
		  	List<Order> orders = new ArrayList<Order>();
		  	Order order1=null;
		  	if (dir.equals("desc"))
		  		order1 = new Order(Sort.Direction.DESC, sortOn);
		  	else 
		  		order1 = new Order(Sort.Direction.ASC, sortOn);

		  	orders.add(order1);

			if (atmLogFilter.getDate().equals("")) {
				return logAtmRepository.getLogAtmsWithouDate(PageRequest.of(page, size, Sort.by(orders)),
						atmLogFilter.getAtmNum()

				);

			} else {
				return logAtmRepository.getLogAtms(PageRequest.of(page, size, Sort.by(orders)),
						atmLogFilter.getAtmNum(), atmLogFilter.getDate()

				);
		  		
		  	}
		
		  	
	 }
	 
	  @PostMapping("addLog")
	  public LogAtm addLog(@RequestBody LogAtm logAtmRequest) {
		 
		  return logAtmRepository.save(logAtmRequest);
	  }
	  
	  
	  @GetMapping("getOneCasseteLog/{id}")
	  public JournalGabAtm getAllLogOne(@PathVariable(value = "id") int idGab) {
		
		  return journalGabAtmRepository.findById(idGab).get();
	  }
	  
	  @PostMapping("getLog")
	  public List<ElectoricJournal> getAllLog() {
		
		  return electoricJournalRepository.findAll();
	  }
	  
	  @PostMapping("getLogFsbk")
	  public Page<JournalGabAtm> getAllLogFsbk(@RequestBody AtmLogDisplayFilter atmLogDisplayFilter,
				@RequestParam(name = "page", defaultValue = "0") int page,
				@RequestParam(name = "size", defaultValue = "10") int size, 
				@RequestParam(name = "sortOn") String sortOn,
				@RequestParam(name = "dir") String dir) {
		 
			Order order=null;
		  	if (dir.equals("desc"))
		  		order = new Order(Sort.Direction.DESC, sortOn);
		  	else 
		  		order = new Order(Sort.Direction.ASC, sortOn);
	        
		  return journalGabAtmRepository.findPageJournalGabAtmFilter(PageRequest.of(page, size, Sort.by(order)), atmLogDisplayFilter.getGab(),atmLogDisplayFilter.getTrx(),atmLogDisplayFilter.getCard(), atmLogDisplayFilter.getDate(), atmLogDisplayFilter.getRrn(),atmLogDisplayFilter.getIsExtourne());
		  }
	  
	  
	  @PostMapping("getLogFsbk1")
	  public Page<JournalGab> getAllLogFsbk1(
				@RequestParam(name = "page", defaultValue = "0") int page,
				@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
				@RequestParam(name = "dir") String dir) {
		 
			Order order=null;
		  	if (dir.equals("desc"))
		  		order = new Order(Sort.Direction.DESC, sortOn);
		  	else 
		  		order = new Order(Sort.Direction.ASC, sortOn);

	       
	        
	        
		  return journalGabRepository.findPageJournalGabFilter(PageRequest.of(page, size, Sort.by(order)));
	  }
	  @PostMapping("getAllAtmImpressionValues")
	  public Page<AtmPrintTicket> getAllAtmImpressionValues(@RequestBody AtmImpressionValueFilter atmImpressionValueFilter,
				@RequestParam(name = "page", defaultValue = "0") int page,
				@RequestParam(name = "size", defaultValue = "10") int size,
				@RequestParam(name = "sortOn") String sortOn,
				@RequestParam(name = "dir") String dir) {
		  logger.info(atmImpressionValueFilter.toString());
		  	
		  	
		  	Order order=null;
		  	if (dir.equals("desc"))
		  		order = new Order(Sort.Direction.DESC, sortOn);
		  	else 
		  		order = new Order(Sort.Direction.ASC, sortOn);

			
				return atmPrintTicketRepository.getAtmImpressionValues(PageRequest.of(page, size, Sort.by(order)),
						atmImpressionValueFilter.getAtmConfig(),atmImpressionValueFilter.getTransactionCode()

				);
	  }
	  
	  
	  @PostMapping("getAllAtmJournalValues")
	  public Page<AtmPrintJournal> getAllAtmJounralValues(@RequestBody AtmImpressionValueFilter atmJounralValueFilter,
				@RequestParam(name = "page", defaultValue = "0") int page,
				@RequestParam(name = "size", defaultValue = "10") int size,
				@RequestParam(name = "sortOn") String sortOn,
				@RequestParam(name = "dir") String dir) {
		  logger.info(atmJounralValueFilter.toString());
		  	
		  	
		  	Order order=null;
		  	if (dir.equals("desc"))
		  		order = new Order(Sort.Direction.DESC, sortOn);
		  	else 
		  		order = new Order(Sort.Direction.ASC, sortOn);

			
				return atmPrintJournalRepository.getAtmImpressionValues(PageRequest.of(page, size, Sort.by(order)),
						atmJounralValueFilter.getAtmConfig(),atmJounralValueFilter.getTransactionCode()

				);
	  }
	  
	  
	  @PostMapping("editAtmImpressionValue")
	  public void editAtmImpressionValue(@RequestBody AtmPrintTicket atmPrintTicket) {
		  logger.info(atmPrintTicket.toString());
		  //System.out.println(atmPrintTicket.toString());
		  atmPrintTicketRepository.save(atmPrintTicket);
		  
	  }
	  
	  @PostMapping("editAtmJournalValue")
	  public void editAtmJournalValue(@RequestBody AtmPrintJournal atmPrintJournal) {
		  logger.info(atmPrintJournal.toString());
		  //System.out.println(atmPrintTicket.toString());
		  atmPrintJournalRepository.save(atmPrintJournal);
		  
	  }
	  @PostMapping("addAtmImpressionValue")
	  public void addAtmImpressionValue(@RequestBody AddAtmImpressionValueRequest addAtmImpressionValueRequest) {
		  logger.info(addAtmImpressionValueRequest.toString());
		  AtmPrintTicket atmPrintTicket=new AtmPrintTicket();
		  AtmPrintTicketId atmPrintTicketId=new AtmPrintTicketId();
		  atmPrintTicketId.setAtmPrintTicketConfigId(addAtmImpressionValueRequest.getConfigId());
		  atmPrintTicketId.setAtmPrintTicketTransId(addAtmImpressionValueRequest.getTransCode());
		  atmPrintTicketId.setAtmPrintTicketLineId(addAtmImpressionValueRequest.getLineNum());
		  
		  Optional<AtmPrintTicket> ticket= atmPrintTicketRepository.findByIds(addAtmImpressionValueRequest.getLineNum(),addAtmImpressionValueRequest.getTransCode(),addAtmImpressionValueRequest.getConfigId());
		  if (ticket.isPresent()) 
			  atmPrintTicketId.setAtmPrintTicketColumnId(ticket.get().getAtmPrintTicketId().getAtmPrintTicketColumnId()+1);
		  else 
			  atmPrintTicketId.setAtmPrintTicketColumnId(1);
		  
		  atmPrintTicket.setAtmPrintTicketId(atmPrintTicketId);
		  atmPrintTicket.setAtmPrintTicketStaticText(addAtmImpressionValueRequest.getStaticText());
		  atmPrintTicket.setAtmPrintTicketValue(addAtmImpressionValueRequest.getVariableText());
		  //System.out.println(atmPrintTicket.toString());
		  atmPrintTicketRepository.save(atmPrintTicket);
		  
	  }
	  
	  @PostMapping("addAtmJournalValue")
	  public void addAtmJournalValue(@RequestBody AddAtmImpressionValueRequest addAtmImpressionValueRequest) {
		  logger.info(addAtmImpressionValueRequest.toString());
		  AtmPrintJournal atmPrintJournal=new AtmPrintJournal();
		  AtmPrintJournalId atmPrintJournalId=new AtmPrintJournalId();
		  
		  atmPrintJournalId.setAtmPrintJournalConfigId(addAtmImpressionValueRequest.getConfigId());
		  atmPrintJournalId.setAtmPrintJournalTransId(addAtmImpressionValueRequest.getTransCode());
		  atmPrintJournalId.setAtmPrintJournalLineId(addAtmImpressionValueRequest.getLineNum());
		  
		  Optional<AtmPrintJournal> ticket= atmPrintJournalRepository.findByIds(addAtmImpressionValueRequest.getLineNum(),addAtmImpressionValueRequest.getTransCode(),addAtmImpressionValueRequest.getConfigId());
		  if (ticket.isPresent()) 
			  atmPrintJournalId.setAtmPrintJournalColumnId(ticket.get().getAtmPrintJournalId().getAtmPrintJournalColumnId()+1);
		  else 
			  atmPrintJournalId.setAtmPrintJournalColumnId(1);
		  
		  atmPrintJournal.setAtmPrintJournalId(atmPrintJournalId);
		  atmPrintJournal.setAtmPrintJournalStaticText(addAtmImpressionValueRequest.getStaticText());
		  atmPrintJournal.setAtmPrintJournalValue(addAtmImpressionValueRequest.getVariableText());
		  //System.out.println(atmPrintTicket.toString());
		  atmPrintJournalRepository.save(atmPrintJournal);
		  
	  }
	  
	  @GetMapping("getAllAtmPrintTicketVariableTexts")
	  public List<AtmPrintTicketVariableText> getAllAtmPrintTicketVariableTexts() {
		  
		 return atmPrintTicketVariableTextRepository.findAll();
		  
	  }
	  
	  @GetMapping("getAllAtmImpressionConf")
	  public List<AtmImpressionConfiguration> getAllAtmImpressionConf() {
		 return atmImpressionConfigurationRepository.findAll();  
	  }
	  
	  @GetMapping("getAllAtmJournalConf")
	  public List<AtmJournalConfig> getAllAtmJournalConf() {  
		 return atmJournalConfigRepository.findAll();  
	  }

	  @PostMapping("deleteAtmImpressionValue")
	  public void deleteAtmImpressionValue(@RequestBody AtmPrintTicket atmPrintTicket) {
		  atmPrintTicketRepository.delete(atmPrintTicket);
	  }
	  
	  @PostMapping("deleteAtmJournalValue")
	  public void deleteAtmImpressionValue(@RequestBody AtmPrintJournal atmPrintJournal) {
		  atmPrintJournalRepository.delete(atmPrintJournal);
	  }
	  
}





