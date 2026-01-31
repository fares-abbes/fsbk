package com.mss.backOffice.controller;

import com.mss.backOffice.services.DayOperationCardSequenceService;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.mss.unified.entities.*;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.backOffice.message.request.CardFormMobileActivationData;
import com.mss.unified.repositories.*;
import com.mss.backOffice.request.*;
import com.mss.backOffice.Response.*;
import com.mss.backOffice.services.PropertyService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;


import com.mss.unified.services.GenerateCardService;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.xwpf.usermodel.*;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Color;

import com.mss.backOffice.services.DateService;
@RestController
@RequestMapping("productManager")
public
class ProductController {
	@Autowired
	UserRepository userRepository;
    @Autowired
    BinOnUsRepository binOnUsRepository;
    @Autowired
    private
    ProductRepositpry productRepositpry;
	@Autowired
	OpposedCardRepository opposedCardRepository;
    @Autowired
    ControlListRepository controlListRepository;

    @Autowired
    PosEntryModeTypesRepository posEntryModeTypesRepository;
   
    @Autowired
    PeriodicityTypeRepository typeRepository;
    @Autowired
    ProgramReposiroty programReposiroty;
    @Autowired
    CardRepository cardRepository;
    @Autowired
    EmvServiceValuesRepository emvServiceValuesRepository;
    @Autowired
    CommissionRepository commissionRepository;
    @Autowired
    RangeRepository rangeRepository;
    @Autowired
   SourceRepository sourceRepository;
    @Autowired
    RoutageRepository routageRepository;
    @Autowired
    RequestCardRepository requestCardRepository;
    @Autowired
    CardStatusREpository cardStatusRepository;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    BinOnUsTypeRepository BinTypeBinRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    GlobalRiskManagementRepository globalRepository;
    @Autowired
    GenerateCardService generateCard;
    @Autowired
    SecondPositionRepository secondRepository;
    @Autowired
    private FirstPositionRepository firstRepository;
    @Autowired
    private DetailRiskManagmentRepository detailrepository;
    @Autowired
    private ThirdPositionRepository thirdRepository;
    @Autowired
    private IdentificationTypeRepository identificationRep;
    @Autowired
    private CustomerStatusRepository customerStatuRep;

    @Autowired
    private AccountStatusRepository AccountStatusRep;
    @Autowired
    private RequestStatusRepository RequestStatusRep;
    @Autowired
    private ProductPrepRepository prepRepository;
    @Autowired
    private TvrRepository tvrRepository;
    @Autowired
    private CvrRepository cvrRepository;

    @Autowired
    private CardStatusPersoRepository cardStatusPersoRepo;
    
    @Autowired
    private CountryRepository countryRepo;
    @Autowired
    private TransactionRepo allocatedTransactionRepo;
    @Autowired
    private DetailRiskManagmentRepository detailRMRepository;
    
    @Autowired
	CardHistoryRepository cardHistoryRepository;
    
    @Autowired
    private MotifOppositionRepository motifOppositionRepository;
    
    @Autowired
    private EpaymentInfoRepository epaymentInfoRepository;
    @Autowired
    private ContaclessControlRepository contaclessControlRepository;
    @Autowired
    private ActionCardRepository actionCardRepository;
    @Autowired
	private CurrencyFSBKRepository currencyFSBKRepository;
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private StockBinRepository stockBinRepository;

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
   
    private ModelMapper modelMapper = new ModelMapper();
    @Autowired
	private DateService  dateService;
    @Autowired
    private AgenceAdministrationRepository agenceAdministrationRepository;
    @Autowired
    private ZoneRepository zoneRepository;
    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private EligibleAccountRepository eligibleAccountRepository;
    private static final Gson gson = new Gson();
    @Autowired
    private MotifRenewelCardRepository motifRenewelCardRepository;
    @Autowired
    private MotifReditionPinCardRepository motifReditionPinCardRepository;
    @Autowired
    private CardComplaintRepository cardComplaintRepository;
	@Autowired
	private EmbossingSummaryRepository embossingSummaryRepository;
	@Autowired
	private EpanSummaryRepository epanSummaryRepository;
	@Autowired
	private BESummaryRepository bESummaryRepository;
	@Autowired
	private SettelementFransaBankRepository settlementRepo;
	@Autowired
	private DayOperationCardFransaBankRepository operationRepo;
	@Autowired
	private DayOperationCardSequenceService dayOperationCardSequenceService;
	@Autowired
	private TvaCommissionFransaBankRepository tvaRepo;
	
	@Autowired
    CodeBankBCRepository codeBankBCRepository;
    @Autowired
    DayOperationFransaBankRepository dayOperationFransaBankRepository;
    @Autowired
    MvbkSettlementRepository mvbkSettlementRepository;
    @Autowired
    BkmvtiFransaBankRepository bkmvtiFransaBankRepository;
	@Autowired
	private DemandeActionRepository demandeActionRepository;
	@Autowired
	private DemandeTypeRepository demandeTypeRepository;
	@Autowired
	private ProgramContractRepository programContractRepository;
	@Autowired
	private CardHolderInfoRepository cardHolderInfoRepository;
	@Autowired
	private AcsSummaryRepository acsSummaryRepository;
	@Autowired
	private OperationCodeRepository operationCodeInternationalRepository;

    public List<DetailRiskManagment> createGlobalAndDetailedRM(Program prog, GlobalRiskManagement globalSaved,RequestCard requestCard) {

 
        //------------------Create Detailed Risk----------------------

        if (allocatedTransactionRepo.findByProgramId(prog.getProgramCode()).isPresent()) {
            List<Transaction> allocatedTransactionList = allocatedTransactionRepo.findByProgramId(prog.getProgramCode()).get();
            List<DetailRiskManagment> detailRiskManagementList = new ArrayList<>();

            for (Transaction allocatedTransaction : allocatedTransactionList) {
            	
            	
                DetailRiskManagment detailRiskManagement = new DetailRiskManagment(
                        Long.parseLong(allocatedTransaction.getPlafondMin()!=null? Objects.requireNonNull(allocatedTransaction.getPlafondMin()) : String.valueOf(0)),
                        //todo verify where to get transType
                        allocatedTransaction.getType().getCodeTransaction(),
                        globalSaved.getGlobalRiskManagCode(),
                        Long.parseLong(allocatedTransaction.getPlafondMax()!=null? Objects.requireNonNull(allocatedTransaction.getPlafondMax()) : String.valueOf(0)),
                        allocatedTransaction.getPlafond() + "",
                        Long.parseLong(allocatedTransaction.getLimitNumber()!=null? Objects.requireNonNull(allocatedTransaction.getLimitNumber()) : String.valueOf(0)),
                        globalSaved.getGlStartDate(),
                        globalSaved.getGlEndDate(),
                        0, 0,
                        globalSaved.getPeriodicityType()
                );
            	if (requestCard.getCeilingsModificationRequest()==1 && requestCard.getCeilingsModificationResponse()==1) {
            		if (allocatedTransaction.getType().getCodeTransaction().equals("00")) {
            			
            			detailRiskManagement.setDrAmountLimit(String.valueOf(requestCard.getNewCprPurchaseMax()));
            			detailRiskManagement.setMaxAmount(requestCard.getNewCprPurchaseMax());
            			
            		}else if(allocatedTransaction.getType().getCodeTransaction().equals("50")) {
            			detailRiskManagement.setDrAmountLimit(String.valueOf(requestCard.getNewCprEcommerceMax()));
            			detailRiskManagement.setMaxAmount(requestCard.getNewCprEcommerceMax());
            		}
            		
     
            		else if(allocatedTransaction.getType().getCodeTransaction().equals("01")) {
            			detailRiskManagement.setDrAmountLimit(String.valueOf(requestCard.getNewCprWithdrawalMax()));
            			detailRiskManagement.setMaxAmount(requestCard.getNewCprWithdrawalMax());
            		}else {
            			detailRiskManagement.setDrAmountLimit(String.valueOf(requestCard.getNewCprRiskAmount()))  ;
                		detailRiskManagement.setMaxAmount(requestCard.getNewCprRiskAmountMax());

            		}
            		
            		detailRiskManagement.setDrNumberLimit(requestCard.getNewGlNumber());
            		detailRiskManagement.setMinAmount(requestCard.getNewCprRiskAmountMin());
            		
            		
            	}

            		
                detailRiskManagementList.add(detailRiskManagement);
            }

            return   detailRMRepository.saveAll(detailRiskManagementList);
        }

        return null;
    }

  
/*  @PostMapping("getListCard")
  public List<RequestCard> getListCard(@RequestBody SearchCard searchCard){
	  List<Card> cardRequest = cardRepository.findAll();
	  List<Card> cardDisplays = new ArrayList<>();
	 
	  cardRequest = cardRequest.stream()
		        

			        .filter(e -> Strings.isNullOrEmpty(String.valueOf(searchCard.accCode)) || String.valueOf(e.getAccCode())
			            .equals(String.valueOf(searchCard.accCode)))
			            
			 
			        
			         /*.filter(e -> Strings.isNullOrEmpty(String.valueOf(searchCard.getStatus()))|| String
			                 .valueOf(e.getCardStatus())
			         .equals(String.valueOf(requestCardFilter.getStatus())))
			         
			         .filter(e -> Strings.isNullOrEmpty(searchCard.cardNum)|| (e.getCardNum())
			         .equals(searchCard.cardNum))
			         
			         
			                    .collect(Collectors.toList());
	  for (Card tr : cardRequest
			    ) {
			      RequestCard RequestDisplay = new RequestCard();
			      RequestDisplay.setCardNum(tr.getAccCode());
			      
			      RequestDisplay.setCity(tr.getCity());
			      RequestDisplay.setZipCode(tr.getZipCode());
			      RequestDisplay.setCountry(tr.getCountry());
			      RequestDisplay.setCardStatus(tr.getCardStatus());
			      RequestDisplay.setCreationDate(tr.getCreationDate());
			      RequestDisplay.setPhone(tr.getPhone());
			      RequestDisplay.setCode(tr.getCode());
			      RequestDisplay.setUserName(tr.getUserName());
                  RequestDisplay.setProduct(tr.getProduct());
                  RequestDisplay.setNameInCard(tr.getNameInCard());
                  
			      requestRequestDisplays.add(RequestDisplay);
			    }
			    return requestRequestDisplays;
 
  }*/


//    @PostMapping("getListCardRequest")
//    public List<RequestCard> getListRequestCard(@RequestBody RequestCardFilter requestCardFilter) {
//
//        logger.info(requestCardFilter.toString());
//        List<RequestCard> cardRequest = requestCardRepository.findAll();
//        List<RequestCard> requestRequestDisplays = new ArrayList<>();
//
//        cardRequest = cardRequest.stream()
//
//                .filter(e -> Strings.isNullOrEmpty(String.valueOf(requestCardFilter.getAccountNumber()))
//                        || String.valueOf(e.getAccountNumber())
//                        .equals(String.valueOf(requestCardFilter.getAccountNumber())))
//
//                .filter(
//                        e -> Strings.isNullOrEmpty(requestCardFilter.getCreationDate()) || e.getCreationDate()
//                                .equals(requestCardFilter.getCreationDate()))
//
//                /*.filter(e -> Strings.isNullOrEmpty(String.valueOf(requestCardFilter.getStatus()))|| String
//                        .valueOf(e.getCardStatus())
//                .equals(String.valueOf(requestCardFilter.getStatus())))*/
//
//                .filter(e -> Strings.isNullOrEmpty(requestCardFilter.Cin) || String
//                        .valueOf(e.getCin())
//                        .equals(requestCardFilter.Cin))
//
//                .collect(Collectors.toList());
//        for (RequestCard tr : cardRequest
//        ) {
//            RequestCard RequestDisplay = new RequestCard();
//            RequestDisplay.setAccountNumber(tr.getAccountNumber());
//
//            RequestDisplay.setCity(tr.getCity());
//            RequestDisplay.setZipCode(tr.getZipCode());
//            RequestDisplay.setCountry(tr.getCountry());
//            RequestDisplay.setCardStatus(tr.getCardStatus());
//            RequestDisplay.setCreationDate(tr.getCreationDate());
//            RequestDisplay.setPhone(tr.getPhone());
//            RequestDisplay.setCode(tr.getCode());
//            RequestDisplay.setUserName(tr.getUserName());
//            RequestDisplay.setProduct(tr.getProduct());
//            RequestDisplay.setNameInCard(tr.getNameInCard());
//            RequestDisplay.setRequestStatus(tr.getRequestStatus());
//            RequestDisplay.setIdAgence(tr.getIdAgence());
//            requestRequestDisplays.add(RequestDisplay);
//        }
//        return requestRequestDisplays;
//
//    }
   // @PostMapping("ValidationCard")
    @Transactional
    public List<RequestCard> ValidateRequestCard(ValidationRequest validationRequest)
            throws ResourceNotFoundException {
    	//try {
    		  logger.info(validationRequest.toString());
    	        String name = SecurityContextHolder.getContext().getAuthentication().getName();
    			Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
    			if (!user.isPresent()) {
    				throw new RuntimeException("Error saving status !");
    			}
    	        List<String> ListCardValidation = validationRequest.RequestCard;
    	        List<RequestCard> ListRequestCard = new ArrayList<>();
    	        List<BinOnUsType> ListBinOnUs = new ArrayList<>();
    	        List<BinOnUs> ListBinOnUsProg = new ArrayList<>();
    	        List<Integer> ListCustomers = new ArrayList<>();
    	        List<Account> ListAccount = new ArrayList<>();
    	        Set<Program> ListPrograms = new HashSet<>();
    	        Customer custom;
    	        Account acc = new Account();
    	        List<Account> ListAccountNumber = new ArrayList<>();
    	        List<RequestCard> ListValidate = new ArrayList<>();
    	        boolean test = false;
    	        if (ListCardValidation.size() > 0) {
    	            for (String obj : ListCardValidation) {
    	                ListRequestCard.add(requestCardRepository.findById(Integer.parseInt(obj)).get());
    	            }
    	            for (RequestCard obj : ListRequestCard) {
    	                custom = new Customer();

    	                if (!(customerRepository.findByCustomerIdentidy(obj.getCin()).isPresent())) {
    	                    Customer customer = new Customer(
    	                    		obj.getIdentificationType(),
    	                    		null, 
    	                    		obj.getCustomerStatus(),
    	                            obj.getCin(),
    	                            obj.getNameInCard(),
    	                            obj.getAddress(),
    	                            String.valueOf(obj.getPhone()),
    	                            obj.getCity(),
    	                            obj.getCountry(), 
    	                            obj.getZipCode());
    	                    customer.setCustomerFirstName(obj.getFirstName());
    	                    customer.setCustomerLastName(obj.getLastName());
    	                    customer.setEmail(obj.getEmail());
    	                    customer.setBirthDate(obj.getBirthDate());
    	                    customer.setBirthPlace(obj.getBirthPlace());
    	                    customer.setGirlName(obj.getGirlName());
    	                    customer.setHomePhone(obj.getHomePhone());
    	                    customer.setRadical(obj.getRadical());
    	                    custom = customerRepository.save(customer);
    	                    ListCustomers.add(custom.getCustomerCode());
    	                } else if ((customerRepository.findByCustomerIdentidy(obj.getCin()).isPresent())) {
    	                	
    	                	
    	                	
    	                    ListCustomers
    	                            .add(customerRepository.findByCustomerIdentidy(obj.getCin()).get().getCustomerCode());
    	                }
    	               
    	                Set<Program> programs = new HashSet<>();
    	                programs = productRepositpry.findById(obj.getProduct()).get().getPrograms();
    	                ListPrograms = productRepositpry.findById(obj.getProduct()).get().getPrograms();
    	                for (Program prog : programs) {
    	                    if (binOnUsRepository.findById(prog.getBinOnUsCode()).isPresent()) {
    	                        BinOnUs bin = binOnUsRepository.findById(prog.getBinOnUsCode()).get();
    	                        ListBinOnUsProg.add(bin);
    	                        //stock
//    	                        StockBin stockbin = stockBinRepository.findBybinOnUsCode(prog.getBinOnUsCode());
//    	                       stockbin.setStockReserve(stockbin.getStockReserve()-1) ; 
//    	                       stockbin.setStockConsome(stockbin.getStockConsome()+1);
//    	                     //  stockbin.setStockDisponible(stockbin.getStockDisponible()-1);
//    	                       stockBinRepository.save(stockbin);
    	                        
    	                        ListBinOnUs.add(BinTypeBinRepository.findById(bin.getBinTypeCode()).get());
    	                    }

    	                }
    	                String devise=obj.getCurrency();
						CurrencyFSBK currencyFSBK = currencyFSBKRepository.getOne(devise);
						if (currencyFSBK!=null) {
							devise=currencyFSBK.getCodeDevise();	
						}
							
    			      
    			     if(ListBinOnUs.size()>0) {
    			    	
    	                for(BinOnUsType b:ListBinOnUs) {
    	                	
//    	                	if(b.getBtCode()==2) {
//    	                		
//    	                		int number=accountRepository.findPrep(obj.getAccountNumber(), b.getBtCode());
//    	                		//System.out.println((number));
//    	                		String NumAttached="0";
//    	                		if(number<10) {
//    	                			NumAttached=String.valueOf(obj.getAccountNumber())+"0"+String.valueOf(number+1);
//
//    	                		}
//    	                		else
//    	                		{
//    	                			NumAttached=String.valueOf(obj.getAccountNumber())+String.valueOf(number+1);
//
//    	                		}
//    	                		acc=new Account();
//    			    		        
//    				    			Account account=new Account();
//    				    			account.setAccountAuthorize(BigInteger.valueOf(0));
//    				    			account.setAccountAvailable(BigInteger.valueOf(0));
//    				    			account.setAccountBilling(BigInteger.valueOf(0));
//    				    			account.setAccountNum(String.valueOf(obj.getAccountNumber()));
//    				    			account.setAccountNumAttached(NumAttached);
//    				    			account.setAccountRevolvingLimit(BigInteger.valueOf(0));
//    				    			account.setAccountName(obj.getNameInCard());
//    				    			
//    				    			account.setCurrency(obj.getCurrency());
//    				    			account.setAstCode(obj.getAccountStatus());
//    				    			account.setCreationDate(new Date());
//    				    			account.setIdAgence(obj.getIdAgence());
//    				    			if(custom.getCustomerCode()!=0) {
//    					    		account.setCustomerCode(String.valueOf(custom.getCustomerCode()));
//    				    			}
//    				    			else
//    				    			{
//    				    			Customer customerFind=(customerRepository.findByCustomerIdentidy(obj.getCin())).get();
//    					    		account.setCustomerCode(String.valueOf(customerFind.getCustomerCode()));
//    				    			}
//    				    			account.setGlobalRiskMangCode(null);
//    				    			account.setAccountType(b.getBtCode());
//    						        acc=accountRepository.save(account);
//    						        ListAccount.add(acc); 
//    				    
//    	                	 	
//    	                	}
    	                	
    	                	//else {
    	                	  	 if(accountRepository.existsByAccountNum(obj.getAccountNumber())==false || accountRepository.existsByAccountType(b.getBtCode())== false ) {
    	 		    		        acc=new Account();
    	 		    		        
    	 			    			Account account=new Account();
    	 			    			account.setAccountAuthorize(BigInteger.valueOf(0));
    	 			    			account.setAccountAvailable(BigInteger.valueOf(0));
    	 			    			account.setAccountBilling(BigInteger.valueOf(0));
    	 			    			account.setAccountNum(String.valueOf(obj.getAccountNumber()));
    	 			    			account.setAccountNumAttached(obj.getAccountNumber());

    	 			    			if (!devise.equals("012")) {
    	 			    				if (obj.getAccountNumberAttached()!= null){
        	 			    				if (obj.getAccountNumberAttached().length()>19) {
        	    	 			    			account.setAccountNumAttached(obj.getAccountNumberAttached().substring(1));

        	 			    				}else {
        	    	 			    			account.setAccountNumAttached(obj.getAccountNumberAttached());
        	 			    				}
    	 			    				}
    	 			    			}
    	 			    			account.setAccountRevolvingLimit(BigInteger.valueOf(0));
    	 			    			account.setAccountName(obj.getFirstName()+ " " +obj.getLastName());
    	 			    			account.setAccountExceeding(BigInteger.valueOf(0));
    	 			    			account.setCurrency(devise);
    	 			    			account.setAstCode(obj.getAccountStatus());
    	 			    			account.setCreationDate(new Date());
    	 			    			account.setIdAgence(obj.getIdAgence());
    	 			    			if(custom.getCustomerCode()!=0) {
    	 				    		account.setCustomerCode(String.valueOf(custom.getCustomerCode()));
    	 			    			}
    	 			    			else
    	 			    			{
    	 			    			Customer customerFind=(customerRepository.findByCustomerIdentidy(obj.getCin())).get();
    	 				    		account.setCustomerCode(String.valueOf(customerFind.getCustomerCode()));
	                                custom=customerFind;

    	 			    			}
    	 			    			account.setGlobalRiskMangCode(null);

    	 			    			account.setAccountType(b.getBtCode());

    	 					        acc=accountRepository.save(account);
    	 					        ListAccount.add(acc); 
    	 			    			
    	 				}
    	                 	 else if(accountRepository.existsByAccountNum(obj.getAccountNumber())==true && accountRepository.existsByAccountType(b.getBtCode())== true)
    	 		    	 {        
    	 		    		       Account a=new Account();
    	 		    		       a=accountRepository.findByNumLib(obj.getAccountNumber());
    	 		    		       a.setAstCode(obj.getAccountStatus());
    	 		    		       if(custom.getCustomerCode()!=0) {
    	 		    		    	   	a.setCustomerCode(String.valueOf(custom.getCustomerCode()));
      	 			    			}
      	 			    			else{
      	 			    				Customer customerFind=(customerRepository.findByCustomerIdentidy(obj.getCin())).get();
	      	 				    		a.setCustomerCode(String.valueOf(customerFind.getCustomerCode()));
    	                                custom=customerFind;

      	 			    			}
    	 		    		       ListAccount.add(a);
    	 		    			  
    	 		    			        
    	 		    	 }
    	                		
    	                	//}
    	              
    	                	
    	                }
    			    	
    			    	 
    			    	
    			     }

    	                for (Program prog : ListPrograms) {

    	                    //Card card=new Card();
    	                    Account Globalaccount = new Account();
    	                    BinOnUs bin = binOnUsRepository.findById(prog.getBinOnUsCode()).get();
    	                    logger.info(bin.toString());
    	                    BinOnUsType binOnusType = BinTypeBinRepository.findById(bin.getBinTypeCode()).get();
    	                    if (binOnusType.getBtCode() == 2) {

    	                        int number = accountRepository
    	                                .findPrep(obj.getAccountNumber(), binOnusType.getBtCode());
    	                        //System.out.println((number));
    	                        String NumAttached = "0";
    	                        if (number < 10) {
    	                            NumAttached =
    	                                    String.valueOf(obj.getAccountNumber()) + "0" + String.valueOf(number + 1);

    	                        } else {
    	                            NumAttached = String.valueOf(obj.getAccountNumber()) + String.valueOf(number + 1);

    	                        }

    	                        acc = new Account();

    	                        Account account = new Account();
    	                        account.setAccountAuthorize(BigInteger.valueOf(0));
    	                        account.setAccountAvailable(BigInteger.valueOf(0));
    	                        account.setAccountBilling(BigInteger.valueOf(0));
    	                        account.setAccountNum(String.valueOf(obj.getAccountNumber()));
    	                        account.setAccountNumAttached(NumAttached);
    	                        account.setAccountRevolvingLimit(BigInteger.valueOf(0));
	 			    			account.setAccountName(obj.getFirstName()+ " " +obj.getLastName());


    	                        account.setCurrency(devise);
    	                        account.setAstCode(obj.getAccountStatus());
    	                        account.setCreationDate(new Date());
    	                        account.setIdAgence(obj.getIdAgence());
    	                        if (custom.getCustomerCode() != 0) {
    	                            account.setCustomerCode(String.valueOf(custom.getCustomerCode()));
    	                        } else {
    	                            Customer customerFind = (customerRepository.findByCustomerIdentidy(obj.getCin()))
    	                                    .get();
    	                            logger.info(customerFind.toString());
    	                            account.setCustomerCode(String.valueOf(customerFind.getCustomerCode()));
	                                custom=customerFind;

    	                        }
    	                        account.setGlobalRiskMangCode(null);
    	                        account.setAccountType(binOnusType.getBtCode());
    	                        acc = accountRepository.save(account);
    	                        //ListAccount.add(acc);
    	                        Globalaccount = acc;

    	                    } else {
    	                        if (accountRepository.existsByAccountNum(obj.getAccountNumber()) == false
    	                                || accountRepository.existsByAccountType(binOnusType.getBtCode()) == false) {
    	                            acc = new Account();

    	                            Account account = new Account();
    	                            account.setAccountAuthorize(BigInteger.valueOf(0));
    	                            account.setAccountAvailable(BigInteger.valueOf(0));
    	                            account.setAccountBilling(BigInteger.valueOf(0));
    	                            account.setAccountNum(String.valueOf(obj.getAccountNumber()));
    	                            
    	                            account.setAccountNumAttached(String.valueOf(obj.getAccountNumber()));
    	                            
//    	                            if (obj.getAccountNumberAttached()!=null) {
//    	                            	  if (!obj.getAccountNumberAttached().isBlank()) {
//    	                            		  
//    	      	                            account.setAccountNumAttached(obj.getAccountNumberAttached());
// 
//    	                            	  }
//    	                            	
//    	                            }
    	                           
    	                            if (!devise.equals("012")) {
    	 			    				if (obj.getAccountNumberAttached()!= null){
        	 			    				if (obj.getAccountNumberAttached().length()>19) {
        	    	 			    			account.setAccountNumAttached(obj.getAccountNumberAttached().substring(1));

        	 			    				}else {
        	    	 			    			account.setAccountNumAttached(obj.getAccountNumberAttached());
        	 			    				}
    	 			    				}
    	 			    			}
    	                            
    	                            
    	                            account.setAccountRevolvingLimit(BigInteger.valueOf(0));
    	 			    			account.setAccountName(obj.getFirstName()+ " " +obj.getLastName());


    	                            account.setCurrency(devise);
    	                            account.setAstCode(obj.getAccountStatus());
    	                            account.setCreationDate(new Date());
    	                            account.setIdAgence(obj.getIdAgence());
    	                            if (custom.getCustomerCode() != 0) {
    	                                account.setCustomerCode(String.valueOf(custom.getCustomerCode()));
    	                            } else {
    	                                Customer customerFind = (customerRepository.findByCustomerIdentidy(obj.getCin()))
    	                                        .get();
    	                                account.setCustomerCode(String.valueOf(customerFind.getCustomerCode()));
    	                                custom=customerFind;
    	                            }
    	                            account.setGlobalRiskMangCode(null);
    	                            account.setAccountType(binOnusType.getBtCode());
    	                            acc = accountRepository.save(account);
    	                            //ListAccount.add(acc);
    	                            Globalaccount = acc;

    	                        } else if (accountRepository.existsByAccountNum(obj.getAccountNumber()) == true
    	                                && accountRepository.existsByAccountType(binOnusType.getBtCode()) == true) {
    	                            //Account a=new Account();
    	                            acc = accountRepository.findByNumLib(obj.getAccountNumber());
    	                            //ListAccount.add(a);
    	                            Globalaccount = acc;

    	                        }

    	                    }

    	                    String numberCard = new String();
    	                    String LowBin = bin.getBouLowBin();

    	                    int CarteLength = bin.getBinLength();
    	                    Set<Range> ranges = new HashSet<>();
    	                    ranges = bin.getRanges();
    	                    if (ranges.size()==0) {
    	        				throw new RuntimeException("Error range size !");
    	        			}
    	                    for (Range rang : ranges) {
    	                        int count = 0;
    	                        if (rang.getRangeStatusCode() == 1) {
    	                            count++;
    	                            numberCard = GenerateCardService.completed_number(LowBin, CarteLength, rang.getBouLowRange(),
    	                                    rang.getBouHighRange());
    	                            System.out.println("numero" + numberCard);

    	                            while (cardRepository.findByCardNum(numberCard).isPresent()) {
    	                                numberCard = GenerateCardService
    	                                        .completed_number(LowBin, CarteLength, rang.getBouLowRange(),
    	                                                rang.getBouHighRange());
    	                                //System.out.println("numero" + numberCard);

    	                            }

    	                            break;
    	                        }


    	                    }
    	                    if (numberCard == null ) {
    	        				throw new RuntimeException("numberCard null !");
    	        			}
    	                    
    	                    if (numberCard.equals("")) {
    	        				throw new RuntimeException("numberCard empty !");
    	        			}
    	                    
    	                    //System.out.println("numero" + numberCard);

    	                    //if (Globalaccount.getAccountType() == binOnusType.getBtCode()) {
    	                        Card card = new Card();
    	                        card.setCardNum(numberCard);
    	                        card.setFirtPositionCode(prog.getFirstPosition());
    	                        card.setSecondPositionCode(prog.getSecondPosition());
    	                        card.setThirdPositionCode(prog.getThirdPosition());
    	                        card.setAccCode(Globalaccount.getAccountCode());
    	                        card.setProductCode(obj.getProduct());
    	                        card.setModifDate(new Date());
    	                        int years = prog.getCprLifeCycle();
    	                        Date date = new Date();
    	                        Calendar c = Calendar.getInstance();
    	                        c.setTime(date);
    	                        c.add(Calendar.YEAR, years);
    	                        Date currentDatePlusYears = c.getTime();
    	                        //card.setExpiryDate(currentDatePlusYears);
    	                        card.setExpiryDate(dateService.expireCard(currentDatePlusYears));
    	                        //card.setCardStatusCode(4);
    	                       
    	                        card.setCurrencyCode(Integer.parseInt(devise));
    	                        card.setCardName(obj.getNameInCard());
    	                        card.setGender(obj.getGender());
    	                        Optional<AgenceAdministration> agence=agenceAdministrationRepository.findByIdAgence(obj.getIdAgence());
    	                       if (agence.isPresent())
    	                        card.setAgencyCode(agence.get().getInitial());
    	                      
    	                        if (prog.isCprRenewl()) {
    	                        	LocalDate localDate = card.getExpiryDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    	                        	
    	                        	card.setPreDate(dateService.getPreDate(localDate));
    	                        }else  
    	                        	card.setPreDate(null);
    	                        card.setStartDate(new Date());
    	                        //GlobalRisque
    	                        GlobalRiskManagement globaManagement = new GlobalRiskManagement();
    	                        int typePeriodicity = prog.getRiskPeriodicity();
    	                        globaManagement.setPeriodicityType(typePeriodicity);
    	                        globaManagement.setGlCurrencyAmount(0);
    	                        globaManagement.setGlCurrencyNumber("0");
    	                        if (obj.getCeilingsModificationRequest()==1 && obj.getCeilingsModificationResponse()==1) {
    	                        	 globaManagement.setMaxAmount(obj.getNewCprRiskAmountMax()); 
    	                        	   globaManagement.setMinAmount(obj.getNewCprRiskAmountMin());
    	    	                        globaManagement.setGolbalAmount(String.valueOf(obj.getNewCprRiskAmount()));
    	    	                        globaManagement.setGlobalNumber(obj.getNewGlNumber());
    	                        	
    	                        }else {
    	                        	 globaManagement.setMaxAmount(prog.getCprRiskAmountMax());
    	                        	   globaManagement.setMinAmount(prog.getCprRiskAmountMin());
    	    	                        globaManagement.setGolbalAmount(String.valueOf(prog.getCprRiskAmount()));
    	    	                        globaManagement.setGlobalNumber(prog.glNumber);
    	                        }

    	                       
    	                        
    	                        Calendar calendar = Calendar.getInstance();
    	                        calendar.setTime(prog.getStartDate());
    	                        calendar.set(Calendar.HOUR_OF_DAY, 0);
    	                        calendar.set(Calendar.MINUTE, 0);
    	                        calendar.set(Calendar.SECOND, 0);
    	                        calendar.set(Calendar.MILLISECOND, 0);
    	                    
    	                        globaManagement.setGlStartDate(calendar.getTime());
    	                        Date endDate = new Date();
    	                        switch (typePeriodicity) {

    	                            case 24:

    	                                c.setTime(globaManagement.getGlStartDate());
    	                                c.add(Calendar.MONTH, 1);
    	                                endDate = c.getTime();
    	                                break;
    	                            case 22:
    	                                c.setTime(globaManagement.getGlStartDate());
    	                                c.add(Calendar.YEAR, 1);
    	                                endDate = c.getTime();
    	                                break;

    	                            case 21:
    	                                c.setTime(globaManagement.getGlStartDate());
    	                                c.add(Calendar.DAY_OF_WEEK, 7);
    	                                endDate = c.getTime();
    	                                System.out.println(endDate);
    	                                break;
    	                            case 23:
    	                                c.setTime(globaManagement.getGlStartDate());
    	                                c.add(Calendar.DAY_OF_WEEK, 1);
    	                                endDate = c.getTime();
    	                                break;

    	                        }
    	                        globaManagement.setGlEndDate(endDate);
    	                 
    	                        
    	                        
    	                        GlobalRiskManagement globalSaved = globalRepository.save(globaManagement);
    	                        List<DetailRiskManagment> detailRisk= createGlobalAndDetailedRM(prog, globalSaved,obj);
    	                        logger.info(globalSaved.toString());
    	                        card.setProgrameId(prog.getProgramCode());
    	                        card.setCardStatusCode(1);
    	                      
    	                        card.setGlobalRiskCode(String.valueOf(globalSaved.getGlobalRiskManagCode()));
    	                        
    	                        card.setSequenceNumber("00");
    	                        
    	                        if (obj.getRaisonSocial()!=null)
    	                        	card.setRaisonSocial(obj.getRaisonSocial());
    	                        
    	                        card.setIsFromMobile(obj.getIsFromMobile());
    	                        card.setRequestCardId(String.valueOf(obj.getCode()));
    	                        cardRepository.save(card);
    	                        boolean cardWithEcommerce=detailRisk.stream().anyMatch(risk-> risk.getTransactionCode().equals("50"));
    	                        if (cardWithEcommerce) {
	    	                        if (card.getIsFromMobile() !=null) {
	    	                        	 if (!card.getIsFromMobile()) {
	    	     	                        	if (devise.equals("012")) {
	    	     	                        		
	    	     	                        		EpaymentInfo epayement=createEpaymentInfoData(card,date,obj.getEpaymentphone(),user.get().getUserName()); 
		    	    	                        	epaymentInfoRepository.save(epayement);
		    	        	                        logger.info("after epayment save");	
	    	     	                        	}else {
	    	     	                        		CardHolderInfo epayement=createCardHolderData(card,date,obj.getEpaymentphone(),custom); 
		    	    	                        	cardHolderInfoRepository.save(epayement);
		    	        	                        logger.info("after CardHolderInfo save");		
	    	     	                        	}
	    	    	                        	
	    	    	                        }
	    	                        	
	    	                        }else {
	    	                        	if (devise.equals("012")) {
	    	                        		EpaymentInfo epayement=createEpaymentInfoData(card,date,obj.getEpaymentphone(),user.get().getUserName());
		    	                        	epaymentInfoRepository.save(epayement);
		        	                        logger.info("after epayment save");	
	    	                        	}else {
	    	                        		CardHolderInfo epayement=createCardHolderData(card,date,obj.getEpaymentphone(),custom); 
    	    	                        	cardHolderInfoRepository.save(epayement);
    	        	                        logger.info("after CardHolderInfo save");	
	    	                        	}
	    	                        
	    	                        }
    	                        }
    	    	                
    	                    	CardHistory cardHistory = new CardHistory();
    							cardHistory.setCardCode(card.getCardCode());
    							cardHistory.setOperation("Carte prête à personnaliser");
    							cardHistory.setOperation_date(new Date());
    							cardHistory.setEditedBy(user.get().getUserName());
    							cardHistoryRepository.save(cardHistory);
    							 logger.info("after history save");	
    	                    //}

    	                    test = true;


    	                }

    	                if (test == true) {
    	                    obj.setRequestStatus(1);
    	                    RequestCard requestUpdated = obj;
    	                    requestCardRepository.save(requestUpdated);
    	                    ListValidate.add(requestUpdated);
    	                }

    	            }

    	            return ListValidate;

    	        } else {
    	            return ListValidate;
    	        }
//    	}catch(Exception e) {
//    		e.printStackTrace();
//    		logger.info(Throwables.getStackTraceAsString(e));
//    		return null;
//    	}
      


    }
    private EpaymentInfo createEpaymentInfoData(Card card,Date creationgDate,String phoneNumber,String userName) {
    	
    	EpaymentInfo epayement=new EpaymentInfo();

        epayement.setCardCode(card.getCardCode());
        epayement.setStatus("A");
        epayement.setStatusFile(1);
        epayement.setCardNum(card.getCardNum());
        epayement.setPhoneNumber(phoneNumber);
        epayement.setCreationDate(creationgDate);
        epayement.setModifDate(creationgDate);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yy");
    	String year=dateFormat.format(creationgDate);
    	epayement.setYearInYy(year);
    	
    	Optional<EpaymentInfo> lastSummary=  epaymentInfoRepository.getRowMax(year);

    	if (lastSummary.isPresent()) {
    		
    		if(lastSummary.get().getSequenceNumber()>=99999) {
    			epayement.setSequenceNumber(0);
    		}else {
    			epayement.setSequenceNumber(lastSummary.get().getSequenceNumber()+1);
    		}
    	}else {
    		epayement.setSequenceNumber(0);
    	}

    	epayement.setEditedBy(userName);

    	epayement.setBranch(card.getAgencyCode());
    	epayement.setNumContrat("035"+card.getAgencyCode()+year+String.format("%05d", epayement.getSequenceNumber()));
    	 
    	logger.info(epayement.toString());
    	return epayement;
    	
    }
    
private CardHolderInfo createCardHolderData(Card card,Date creationDate,String phoneNumber,Customer customer) {

	CardHolderInfo cardHolderInfo = new CardHolderInfo();
	cardHolderInfo.setPan(card.getClearCardNum());
	cardHolderInfo.setAction("enroll");
	cardHolderInfo.setFirstName(customer.getCustomerFirstName());
	cardHolderInfo.setLastName(customer.getCustomerLastName());
	cardHolderInfo.setPhoneNumber(phoneNumber);
	cardHolderInfo.setCreationDate(creationDate);
	cardHolderInfo.setModifDate(creationDate);
	cardHolderInfo.setBranch(card.getAgencyCode());
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
	cardHolderInfo.setExpiry(sdf.format(card.getExpiryDate()));
	cardHolderInfo.setStatusGeneration(0);
	logger.info(cardHolderInfo.toString());

	return cardHolderInfo;
    }
    
    @PutMapping("cancel/{id}")
    public ResponseEntity<Card> cancelCard(@PathVariable(value = "id") Integer cardCode) {
    	String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving status !");
		}
      Card card = cardRepository.findByCardCode(cardCode).get();
      card.setCardStatusCode(8);
		card.setModifDate(new Date());

      cardRepository.save(card);
      CardHistory cardHistory = new CardHistory();
		cardHistory.setCardCode(card.getCardCode());
		cardHistory.setOperation("Changer le statut de la carte en Annulée");
		cardHistory.setOperation_date(card.getModifDate());
		cardHistory.setEditedBy(user.get().getUserName());
		cardHistoryRepository.save(cardHistory);
		
      return ResponseEntity.ok(card);
    }
    @PutMapping("sendToBranch")
    public void cancelCard(@RequestBody GenerateFileForSmtRequest cardCodes) {
    	String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving status !");
		}
		
		for (Integer code : cardCodes.getCardCodes()) {
			
			 Card card = cardRepository.findByCardCode(code).get();
			 
			 if (card.getCardStatusCode()==2 && card.getCardActionsStatus().equals("7")) 
				 card.setCardActionsStatus("9");
			 else 
				 card.setCardStatusCode(9);

			 
			 card.setModifDate(new Date());
			 
		      cardRepository.save(card);
		      
		      
		      CardHistory cardHistory = new CardHistory();
				cardHistory.setCardCode(card.getCardCode());
				cardHistory.setOperation("Carte envoyée à l'agence");
				cardHistory.setOperation_date(card.getModifDate());

				cardHistory.setEditedBy(user.get().getUserName());
				cardHistoryRepository.save(cardHistory);
				
				
		}
     
		
     
    }
    
    @PostMapping(value="generateFileForSmt", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] generateFileForSmt(@RequestBody GenerateFileForSmtRequest request) throws IOException {
    
 	  //File file = new File("C:/Users/Administrator/Downloads/porteur.txt");
 	   File file = new File(propertyService.getFilePath());
    	PrintWriter writer = new PrintWriter(file, "UTF-8");
 	    for (Integer cardCode  : request.getCardCodes()) {
 	    	
 		    Optional<Card> card=cardRepository.findById(cardCode);
 		    if (card.isPresent()) {
 		    	
 		    	
 		      
 		    	String operationType=request.getOperationType().equals("creation")?"1":"4";
 		    	String optionFabrication=request.getOperationType().equals("creation")?"D":"F";

 		    	String clientName="";
 		    	String customerAddress="";
 		    	String postalCode="";
 		    	String accountNumber="";
 		    	String pan=card.get().getClearCardNum();
 		    	//String dateNaissance="";
 		    			
 		    	Optional<Account> account=accountRepository.findByAccountCode(card.get().getAccCode());
 		        if(account.isPresent()) {
 		        	clientName=account.get().getAccountName();
 		        	accountNumber=account.get().getAccountNum();
 		        	Optional<Customer> customer=customerRepository.findByCustomerCode(Integer.parseInt(account.get().getCustomerCode()));
 		        	if (customer.isPresent()) {
 		        		customerAddress=customer.get().getCustomerAddress()!= null?customer.get().getCustomerAddress():"";
 		        		postalCode=customer.get().getCustomerPostal() != null?customer.get().getCustomerPostal():"";
 		        	}
 		        	
 		        }
 		        	
 		        	
 		        clientName=StringUtils.rightPad(clientName, 26);
 		        customerAddress=StringUtils.rightPad(customerAddress, 32);
 		        postalCode=StringUtils.leftPad(postalCode, 9,"0");
 		        accountNumber=StringUtils.leftPad(accountNumber, 24,"0");
 		        pan=StringUtils.rightPad(pan,19);
 		        DateFormat dateFormat = new SimpleDateFormat("MMyy");  
 		        String startDate = dateFormat.format(card.get().getStartDate());  
 		        String expDate=dateFormat.format(card.get().getExpiryDate());  
 		    	
 		    	writer.println("PO00"+pan+operationType+"32"+clientName+"                          "+"IHROT                     "+
 		    			customerAddress+"                                "+"                                "+postalCode+
 		    			"00000000000000000000000000"+accountNumber+"000000000000000000000000"+"00010"+startDate+expDate+optionFabrication+
 		    			"000000000000000000000000000000000000000000010000000000002030011999000000000000000078801000000000000000000000000000000009788360109075926   00000788000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
 		    			
 		    			
 		    			);
 		    	
 		    	card.get().setCardStatusCode(6);
 		    	cardRepository.save(card.get());
 		    	
 		    }
 	    	
 	    	
 	    }
 	   
 	    writer.close();
 	    InputStream in = new FileInputStream(file);

         return IOUtils.toByteArray(in);
    }
    
    
    @PostMapping("getListCardRequest")
    public Page<RequestCard> getListRequestCard(
    		@RequestBody RequestCardFilter requestCardFilter,
    		@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size
    		
    		) {
    	Order order=new Order(Sort.Direction.DESC, "code");
//	  	if (dir.equals("desc"))
//	  		order = new Order(Sort.Direction.DESC, sortOn);
//	  	else 
//	  		order = new Order(Sort.Direction.ASC, sortOn);
	  	Page<RequestCard> requests=null;
	  	 if (requestCardFilter.getStatus()!=0) {
	  		 if (requestCardFilter.getBranch().equals("")) {
	  			requests=requestCardRepository.getRequestsWithStatus(
		  				PageRequest.of(page, size, Sort.by(order)),
		  				requestCardFilter.getAccountNumber(),
		  				requestCardFilter.getCreationDate(), 
		  				
		  				requestCardFilter.getCin(),
		  				requestCardFilter.getStatus()
		  				);
	  		 }else {
	  			Optional<AgenceAdministration> agence=agenceAdministrationRepository.findByInitial(requestCardFilter.getBranch());

		  		requests=requestCardRepository.getRequestsWithStatusAndBranch(
		  				PageRequest.of(page, size, Sort.by(order)),
		  				requestCardFilter.getAccountNumber(),
		  				requestCardFilter.getCreationDate(), 
		  				
		  				requestCardFilter.getCin(),
		  				requestCardFilter.getStatus(),
		  				agence.get().getCodeAgence()
		  	
		  				);
	  			 
	  		 }
	  		
	  	 }else {
	  		 if (requestCardFilter.getBranch().equals("")) {
	  			requests=requestCardRepository.getRequests(
		  				PageRequest.of(page, size, Sort.by(order)),
		  				requestCardFilter.getAccountNumber(),
		  				requestCardFilter.getCreationDate(), 
		  				
		  				requestCardFilter.getCin()
		  			
		  	
		  				);
	  		 }else {
	  			Optional<AgenceAdministration> agence=agenceAdministrationRepository.findByInitial(requestCardFilter.getBranch());

		  		requests=requestCardRepository.getRequestsWithBranch(
		  				PageRequest.of(page, size, Sort.by(order)),
		  				requestCardFilter.getAccountNumber(),
		  				requestCardFilter.getCreationDate(), 
		  				
		  				requestCardFilter.getCin(),
		  				agence.get().getCodeAgence()
		  	
		  				);
	  			 
	  		 }
	     	
	  	 }

    
       
//        List<RequestCard> cardRequest = requestCardRepository.findAll();
//        List<RequestCard> requestRequestDisplays = new ArrayList<>();

//        cardRequest = cardRequest.stream()
//
//                .filter(e -> Strings.isNullOrEmpty(String.valueOf(requestCardFilter.getAccountNumber()))
//                        || String.valueOf(e.getAccountNumber())
//                        .equals(String.valueOf(requestCardFilter.getAccountNumber())))
//
//                .filter(
//                        e -> Strings.isNullOrEmpty(requestCardFilter.getCreationDate()) || e.getCreationDate()
//                                .equals(requestCardFilter.getCreationDate()))
//
//              
//
//                .filter(e -> Strings.isNullOrEmpty(requestCardFilter.Cin) || String
//                        .valueOf(e.getCin())
//                        .equals(requestCardFilter.Cin))
//                
//                .filter(e -> e.getPreValidation()==0
//                       )
//
//                .collect(Collectors.toList());
//        
//        if (requestCardFilter.getStatus()!=0) {
//        	
//        	 cardRequest = cardRequest.stream()
//
//        			.filter(e -> Strings.isNullOrEmpty(String.valueOf(requestCardFilter.getStatus()))|| String
//                             .valueOf(e.getRequestStatus())
//                     .equals(String.valueOf(requestCardFilter.getStatus())))
//                     .collect(Collectors.toList());
//
//        }
//        for (RequestCard tr : cardRequest
//        ) {
//
//        			  RequestCard RequestDisplay = new RequestCard();
//                      RequestDisplay.setAccountNumber(tr.getAccountNumber());
//
//                      RequestDisplay.setCity(tr.getCity());
//                      RequestDisplay.setZipCode(tr.getZipCode());
//                      RequestDisplay.setCountry(tr.getCountry());
//                      RequestDisplay.setCardStatus(tr.getCardStatus());
//                      RequestDisplay.setCreationDate(tr.getCreationDate());
//                      RequestDisplay.setPhone(tr.getPhone());
//                      RequestDisplay.setCode(tr.getCode());
//                      RequestDisplay.setUserName(tr.getUserName());
//                      RequestDisplay.setProduct(tr.getProduct());
//                      RequestDisplay.setNameInCard(tr.getNameInCard());
//                      RequestDisplay.setRequestStatus(tr.getRequestStatus());
//                      RequestDisplay.setIdAgence(tr.getIdAgence());
//                      RequestDisplay.setMotifRejet(tr.getMotifRejet());
//                      requestRequestDisplays.add(RequestDisplay);	  
//        			  
//        		  }
        return requests;

    }
    
    @PostMapping("validationZone")
    public Page<RequestCard> getListValidationZone(@RequestBody RequestCardFilter requestCardFilter,
    		@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size) {
	  	Page<RequestCard> requests=null;
    	Order order=new Order(Sort.Direction.DESC, "code");
    	
		 Optional<AgenceAdministration>  branch=agenceAdministrationRepository.findByInitial(requestCardFilter.getBranch());
		 if(branch.isPresent())

	  		requests=requestCardRepository.getRequestsForValidation1(
	  				PageRequest.of(page, size, Sort.by(order)),
	  				requestCardFilter.getAccountNumber(),
	  				requestCardFilter.getCreationDate(), 
	  				
	  				requestCardFilter.getCin(),
	  				branch.get().getCodeAgence()
	  	
	  				);
	  	 

       
//        List<RequestCard> cardRequest = requestCardRepository.findAll();
//        List<RequestCard> requestRequestDisplays = new ArrayList<>();
//
//        cardRequest = cardRequest.stream()
//
//                .filter(e -> Strings.isNullOrEmpty(String.valueOf(requestCardFilter.getAccountNumber()))
//                        || String.valueOf(e.getAccountNumber())
//                        .equals(String.valueOf(requestCardFilter.getAccountNumber())))
//
//                .filter(
//                        e -> Strings.isNullOrEmpty(requestCardFilter.getCreationDate()) || e.getCreationDate()
//                                .equals(requestCardFilter.getCreationDate()))
//
//                /*.filter(e -> Strings.isNullOrEmpty(String.valueOf(requestCardFilter.getStatus()))|| String
//                        .valueOf(e.getCardStatus())
//                .equals(String.valueOf(requestCardFilter.getStatus())))*/
//
//                .filter(e -> Strings.isNullOrEmpty(requestCardFilter.Cin) || String
//                        .valueOf(e.getCin())
//                        .equals(requestCardFilter.Cin))
//                
//                .filter(e -> e.getPreValidation()==1
//                        )
//
//                .collect(Collectors.toList());
//        for (RequestCard tr : cardRequest
//        ) {
//        
//        			  RequestCard RequestDisplay = new RequestCard();
//                      RequestDisplay.setAccountNumber(tr.getAccountNumber());
//
//                      RequestDisplay.setCity(tr.getCity());
//                      RequestDisplay.setZipCode(tr.getZipCode());
//                      RequestDisplay.setCountry(tr.getCountry());
//                      RequestDisplay.setCardStatus(tr.getCardStatus());
//                      RequestDisplay.setCreationDate(tr.getCreationDate());
//                      RequestDisplay.setPhone(tr.getPhone());
//                      RequestDisplay.setCode(tr.getCode());
//                      RequestDisplay.setUserName(tr.getUserName());
//                      RequestDisplay.setProduct(tr.getProduct());
//                      RequestDisplay.setNameInCard(tr.getNameInCard());
//                      RequestDisplay.setRequestStatus(tr.getRequestStatus());
//                      RequestDisplay.setIdAgence(tr.getIdAgence());
//                      RequestDisplay.setPreValidation(tr.getPreValidation());
//                      requestRequestDisplays.add(RequestDisplay);	  
//        			  
//       
//          
//        }
        return requests;

    }

    @PutMapping("changeRequestCardStatus/{codeRequest}/{limitsModificationsResponse}")
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public RequestCard changeRequestCardStatus(
    		@PathVariable(value = "codeRequest") Integer codeRequest,
    		@PathVariable(value = "limitsModificationsResponse") Integer limitsModificationsResponse,
    		@RequestBody ChangeRequestCardStatusRequest request
    		) throws ResourceNotFoundException {
    	 // Set<Program> programs = new HashSet<>();
    	
    	if (request.getGender()!=null && request.getGender().isEmpty()) {
			
			return null;

		}

    	Optional<RequestCard> requestcard=	requestCardRepository.findById(codeRequest);
    	if (requestcard.isPresent()) {
    		 requestcard.get().setPreValidation(0);
    		 if (requestcard.get().getCeilingsModificationRequest()==1)
    			 requestcard.get().setCeilingsModificationResponse(limitsModificationsResponse);
    		 
    		 requestcard.get().setGender(request.getGender());
    		 
    		 ValidationRequest validationRequest=new ValidationRequest();
    		 validationRequest.setCode(1);
    		 List<String> requests=new ArrayList<>();
    		 requests.add(String.valueOf(codeRequest));
    		 validationRequest.RequestCard= requests;
    		 requestCardRepository.save(requestcard.get());
    		// try {
				ValidateRequestCard(validationRequest);
				 return requestcard.get();
				
//			} catch (ResourceNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			
//	    		return null;
//			}
    		 
    	    
    	}else return null;
  			
    }
    @GetMapping("rejectRequestCard/{codeRequest}/{motif}")
    public RequestCard rejectRequestCard(@PathVariable(value = "codeRequest") Integer codeRequest,
    		@PathVariable(value = "motif") String motif) {
    	
    	Optional<RequestCard> requestcard=	requestCardRepository.findById(codeRequest);
    	if (requestcard.isPresent()) {
        
        	    Set<Program> programs= new HashSet<>();
        	 
                programs = productRepositpry.findById(requestcard.get().getProduct()).get().getPrograms();
                for (Program prog : programs) {
                	//BinOnUs bin = binOnUsRepository.findById(prog.getBinOnUsCode()).get();
                	StockBin stockbin = stockBinRepository.findBybinOnUsCode(prog.getBinOnUsCode());
                    stockbin.setStockReserve(stockbin.getStockReserve()-1) ; 
                    stockbin.setStockSatim(stockbin.getStockSatim()+1);
                    stockBinRepository.save(stockbin);
                }
	
    		 requestcard.get().setPreValidation(0);
    		 requestcard.get().setMotifRejet(motif);
    		 requestcard.get().setRequestStatus(3);
    		 
    	     return requestCardRepository.save(requestcard.get());
    	}else return null;
  			
    }
    @GetMapping("getCountriesToControl")
    public List<Country> getCountriesToControl() {
        return countryRepo.findAll();
    }

    @GetMapping("getAllFirst")
    public List<FirstPosition> getAllFirst() {
        return firstRepository.findAll();
    }

    @GetMapping("getAllIdentificationType")
    public List<IdentificationType> getAllIdentificationType() {
        return identificationRep.findAll();
    }

    @GetMapping("getAllCustomerStatus")
    public List<CustomerStatus> getAllCustomerStatus() {
    	 return customerStatuRep.findAll();
      
    }

    @GetMapping("getAllAccountStatus")
    public List<AccountStatus> getAllAccountStatus() {
        return AccountStatusRep.findAll();
    }

    @GetMapping("getAllSecond")
    public List<SecondPosition> getAllSecond() {
        return secondRepository.findAll();
    }

    @GetMapping("getAllThird")
    public List<ThirdPosition> getAllThird() {
        return thirdRepository.findAll();
    }

  
    
    @PostMapping(value = "generateCardContract", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public  byte[] generateCardContract(@RequestBody RequestCard requestcard) throws IOException, DocumentException {
    	try {

    		 PdfReader reader = new PdfReader(propertyService.getPosFilesPath()+"contrat_CIB.pdf");
      	   FileOutputStream file= new FileOutputStream(propertyService.getPosFilesPath()+"contrat_CIB_dest.pdf");
             PdfStamper stamper = new PdfStamper(reader, file);
             PdfContentByte page = stamper.getOverContent(1);
          
            Optional<Product> product= productRepositpry.findById(requestcard.getProduct());
             
             ////// Nom
             ColumnText ct = new ColumnText(page);
             ct.setSimpleColumn(100f, 485f, 250f, 0f);
             Font f = new Font();
             Font f2 = new Font();
             		f2.setSize(9.5f);
             Paragraph pz = new Paragraph(new Phrase(20, requestcard.getLastName(), f));
             ct.addElement(pz);
             ct.go();
             
             Optional<AgenceAdministration> agence=agenceAdministrationRepository.findByIdAgence(requestcard.getIdAgence());
            
             
             ////// chef agence
  	         ct = new ColumnText(page);
  	        ct.setSimpleColumn(87f, 589f, 700f, 0f);
  	    
  	         pz = new Paragraph(new Phrase(20, agence.get().getManager(), f));
  	        ct.addElement(pz);
  	        ct.go();
  	        
             ////// agence
  	         ct = new ColumnText(page);
  	        ct.setSimpleColumn(204f, 575f, 700f, 0f);
  	    
  	         pz = new Paragraph(new Phrase(20, agence.get().getLibelle()+"  "+agence.get().getInitial(), f));
  	        ct.addElement(pz);
  	        ct.go();
             
             ////// Nom fille
  	         ct = new ColumnText(page);
  	        ct.setSimpleColumn(374f, 485f, 700f, 0f);
  	       
  	         pz = new Paragraph(new Phrase(20, requestcard.getGirlName(), f));
  	        ct.addElement(pz);
  	        ct.go();
             
             ///////////// Prénom
             ct = new ColumnText(page);
             ct.setSimpleColumn(120f, 463f, 250f, 0f);
             pz = new Paragraph(new Phrase(20, requestcard.getFirstName(), f));
             ct.addElement(pz);
             ct.go();
             
             ///////////// date naissance
  	        ct = new ColumnText(page);
  	        ct.setSimpleColumn(190f, 440f, 700f, 0f);
  	      
  	        pz = new Paragraph(new Phrase(20, requestcard.getBirthDate()+", "+requestcard.getBirthPlace(), f));
  	        ct.addElement(pz);
  	        ct.go();
             
             ///////////// adresse
             ct = new ColumnText(page);
             ct.setSimpleColumn(170f, 418f, 700f, 0f);
           
             pz = new Paragraph(new Phrase(20, requestcard.getAddress(), f2));
             ct.addElement(pz);
             ct.go();
             
             ///////////// home phone
  	        ct = new ColumnText(page);
  	        ct.setSimpleColumn(170f, 395f, 700f, 0f);
  	        pz = new Paragraph(new Phrase(20, requestcard.getHomePhone(), f));
  	        ct.addElement(pz);
  	        ct.go();
  	        
             ///////////// mobile number
             ct = new ColumnText(page);
             ct.setSimpleColumn(370f, 395f, 700f, 0f);
             pz = new Paragraph(new Phrase(20, requestcard.getPhone(), f));
             ct.addElement(pz);
  			ct.go();

  			///////////// email
  			ct = new ColumnText(page);
  			ct.setSimpleColumn(145f, 373f, 700f, 0f);
  			pz = new Paragraph(new Phrase(20, requestcard.getEmail(), f));
  			ct.addElement(pz);
  			ct.go();

  			///////////// account number
  			ct = new ColumnText(page);
  			ct.setSimpleColumn(125f, 350f, 700f, 0f);
             pz = new Paragraph(new Phrase(20, requestcard.getAccountNumber(), f));
             ct.addElement(pz);
             ct.go();
             if (product.get().getLibelle().toLowerCase().contains("gold")) {
          	   ///////////// product gold
                 ct = new ColumnText(page);
                 ct.setSimpleColumn(253f, 241f, 700f, 0f);
                 pz = new Paragraph(new Phrase( "X", f));
                 ct.addElement(pz);
                 ct.go();
          	   
             }else {
          	   
          	   ///////////// product classique
                 ct = new ColumnText(page);
                 ct.setSimpleColumn(253f, 263f, 700f, 0f);
                 pz = new Paragraph(new Phrase( "X", f));
                 ct.addElement(pz);
                 ct.go();
                 
             }
          	  
             
         
             
          
             
             //////
             
             
             page = stamper.getOverContent(6);
             
             // city
             ct = new ColumnText(page);
             ct.setSimpleColumn(800f, 383f, 343f, 0f);
             pz = new Paragraph(new Phrase(20, agence.get().getCity(), f));
             ct.addElement(pz);
             ct.go();
             
             
             
             Date date = Calendar.getInstance().getTime();  
             DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");  
             String strDate = dateFormat.format(date); 
             
             
             // date
             ct = new ColumnText(page);
             ct.setSimpleColumn(800f, 383f, 408f, 0f);
             pz = new Paragraph(new Phrase(20, strDate, f));
             ct.addElement(pz);
             ct.go();

             stamper.close();
             reader.close();
             File fileDest = new File(propertyService.getPosFilesPath()+"contrat_CIB_dest.pdf");
      	InputStream in = new FileInputStream(fileDest);
      	   return IOUtils.toByteArray(in);	
    		
    	}catch(Exception e) {
    		logger.info("Exception");
    		logger.info(Throwables.getStackTraceAsString ( e ));
    		return null;
    	}
    	  
    	
    }
//	private void replaceTextaa(Object document, String searchText, String replacementText) {
//		
//		if (document instanceof XWPFDocument) {
//			 XWPFDocument doc = (XWPFDocument) document;
//			for (XWPFParagraph paragraph : doc.getParagraphs()) {
//				
//				for (XWPFRun run : paragraph.getRuns()) {
//					
//					
//					String text = run.getText(0);
//					
//					if (text != null && text.contains(searchText)) {
//						
//						text = text.replace(searchText, replacementText);
//						run.setText(text, 0);
//					}
//				}
//			}
//			
//		}else if (document instanceof HWPFDocument) {
//			HWPFDocument doc = (HWPFDocument) document;
//			org.apache.poi.hwpf.usermodel.Range range = doc.getRange();
//			
//			for (int i = 0; i < range.numParagraphs(); i++) {
//				org.apache.poi.hwpf.usermodel.Paragraph paragraph = range.getParagraph(i);
//	            for (int j = 0; j < paragraph.numCharacterRuns(); j++) {
//	                CharacterRun run = paragraph.getCharacterRun(j);
//	                String text = run.text();
//	                if (text.contains(searchText)) {
//	                    run.replaceText(searchText, replacementText);
//	                }
//	            }
//	        }
//			
//		}
//		
//	}
	
	private static void replaceText(XWPFParagraph paragraph, String searchText, String replaceText) {
        String paragraphText = paragraph.getText();
        if (paragraphText.contains(searchText)) {
            List<XWPFRun> runs = paragraph.getRuns();
            StringBuilder aggregatedText = new StringBuilder();
            
            for (XWPFRun run : runs) {
            	
            	logger.info(run.getText(0));
        		//logger.info(aggregatedText.toString());
            	if (run.getText(0)!=null)
            		aggregatedText.append(run.getText(0));
            	
//            	else
//            		aggregatedText.append(" ");
            	
         
            }

            String aggregatedString = aggregatedText.toString();
            aggregatedString = aggregatedString.replace(searchText, replaceText);
            
            // Capture the formatting of the first run
            XWPFRun firstRun = runs.get(0);
//            String fontFamily = firstRun.getFontFamily();
//            int fontSize = firstRun.getFontSize();
//            boolean isBold = firstRun.isBold();
//            boolean isItalic = firstRun.isItalic();
//            String color = firstRun.getColor();
            
            
            
            

           // int runIndex = 0;
            for (XWPFRun run : runs) {
                run.setText("", 0);  // Clear existing text
            }

            //XWPFRun firstRun = runs.get(runIndex);
            firstRun.setText(aggregatedString, 0);  // Set new text to the first run
            
            // Set new text to the first run with the captured formatting
//            firstRun.setText(aggregatedString, 0);
//            firstRun.setFontFamily(fontFamily);
//            firstRun.setFontSize(fontSize);
//            firstRun.setBold(isBold);
//            firstRun.setItalic(isItalic);
//            firstRun.setColor(color);

            for (int i = 1; i < runs.size(); i++) {
                paragraph.removeRun(i);  // Remove extra runs
            }
        }
    }
	
	@GetMapping(value = "generateContractCardMobile/{cardCode}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public  byte[] generateContractCardMobile(@PathVariable(value = "cardCode") Integer cardCode) throws IOException, DocumentException {
		Optional<Card> card=cardRepository.findByCardCode(cardCode);
		if (card.isPresent()) {
			
			Optional<RequestCard> requestCard= requestCardRepository.findById(Integer.parseInt(card.get().getRequestCardId()));
			return generateCardContractV2(requestCard.get());
		}
		
		return null;
	}
	
	
	
    @PostMapping(value = "generateCardContractV2", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public  byte[] generateCardContractV2(@RequestBody RequestCard requestcard) throws IOException, DocumentException {
    	try {
		
            Optional<Product> product= productRepositpry.findById(requestcard.getProduct());
            if (product.isPresent()) {
            	for (Program prog : product.get().getPrograms()) {
            		Optional<ProgramContract> fileOptional = programContractRepository
    						.findByProgramId(prog.getProgramCode());
            		if (fileOptional.isPresent()) {
            			
            			ProgramContract fileEntity = fileOptional.get();
            			
    					byte[] fileData = fileEntity.getData();
    					
//    					  Object document = null;
//    					  
//    					  
//    					  if (fileEntity.getFileName().endsWith(".doc")) {
//    						   document = new HWPFDocument(new ByteArrayInputStream(fileData));
//    						  
//    					  }
//    					  else if (fileEntity.getFileName().endsWith(".docx")) {
//    						  
//    	    				  document = new XWPFDocument(new ByteArrayInputStream(fileData));
//
//    						  
//    					  }
    					  
    					  
    					  XWPFDocument  document = new XWPFDocument(new ByteArrayInputStream(fileData));
    					  List<XWPFParagraph> paragraphs = document.getParagraphs();
    			            for (XWPFParagraph paragraph : paragraphs) {
    			            	
    			              
    			            	
    			            	
    			            	Optional<AgenceAdministration> agence=agenceAdministrationRepository.findByIdAgence(requestcard.getIdAgence());
    	    					
    	    					if(agence.isPresent()) {
    	    						
    	    					////// chef agence
    	    						replaceText(paragraph, "[CHEF_AGENCE]", agence.get().getManager());
    	    						
    	    					////// agence
    	    						replaceText(paragraph, "[LIB/NUM_AGENCE]", agence.get().getLibelle()+"  "+agence.get().getInitial());

    	    					//// city
    	    					replaceText(paragraph, "[LIEU_SIGNA]", agence.get().getCity());
    			
    	    						
    	    						
    	    					}
    	    					
    	    					else {
    	    					
    	    						replaceText(paragraph, "[CHEF_AGENCE]", "");
    	    						replaceText(paragraph, "[LIB/NUM_AGENCE]", "");
    	    						replaceText(paragraph, "[LIEU_SIGNA]", "");
    	    					}
    	    					
    	    					////// Nom
    	    					if (requestcard.getLastName()!=null)
    	    						replaceText(paragraph, "[NOM_CLIENT]", requestcard.getLastName());
    	    					else 
    	    						replaceText(paragraph, "[NOM_CLIENT]", "");
    	    					
    	    					
    	    						////// Nom fille
    	    					if (requestcard.getGirlName()!=null)
    	    						replaceText(paragraph, "[NJF]", requestcard.getGirlName());
    	    					else 
    	    						replaceText(paragraph, "[NJF]", "");

    	    					
    	    					////// Nom
    	    					if (requestcard.getFirstName()!=null) {
    	    						replaceText(paragraph, "[PRENOM_CLIENT]", requestcard.getFirstName());

    	    					}
    	    					else {
    	    						replaceText(paragraph, "[PRENOM_CLIENT]", "");

    	    					}
    	    					
    	    					
    	    					////// date naissance
    	    					String dateLieuNaissance="";
    	    					if (requestcard.getBirthDate()!=null) {
    	    						dateLieuNaissance=requestcard.getBirthDate();

    	    					}
    	    					if (requestcard.getBirthPlace()!=null) {
    	    						dateLieuNaissance=dateLieuNaissance+", "+requestcard.getBirthPlace();

    	    					}
    	    						
    	    					
    	    					replaceText(paragraph, "[DATE_NAISSANCE_CLIENT]", dateLieuNaissance);
    	    					
    	    					
    	    					////// adresse
    	    					if (requestcard.getAddress()!=null)
    	    						replaceText(paragraph, "[ADRESSE_CLIENT]",requestcard.getAddress());
    	    					else 
    	    						replaceText(paragraph, "[ADRESSE_CLIENT]", "");
    	    					
    	    					
    	    					////// home phone
    	    					if (requestcard.getHomePhone()!=null)
    	    						replaceText(paragraph, "[TEL_DOMICILE]",requestcard.getHomePhone());
    	    					else 
    	    						replaceText(paragraph, "[TEL_DOMICILE]", "");
    	    					
    	    					
    	    					////// mobile number
    	    					if (requestcard.getPhone()!=null)
    	    						replaceText(paragraph, "[GSM]",requestcard.getPhone());
    	    					else 
    	    						replaceText(paragraph, "[GSM]", "");
    	    					
    	    					////// email
    	    					if (requestcard.getEmail()!=null)
    	    						replaceText(paragraph, "[EMAIL]",requestcard.getEmail());
    	    					else 
    	    						replaceText(paragraph, "[EMAIL]", "");
    	    					
    	    					
    	    					////// account number // Account number in 978
    	    					if (requestcard.getAccountNumber()!=null) {
    	    						replaceText(paragraph, "[NUM_COMPTE]",requestcard.getAccountNumber());
    	    						replaceText(paragraph, "[NUM_C_DEVISE]",requestcard.getAccountNumber());

    	    					}
    	    					else {
    	    						replaceText(paragraph, "[NUM_COMPTE]", "");
    	    						replaceText(paragraph, "[NUM_C_DEVISE]", "");

    	    					}
    	    					
    	    					
    	    					Date date = Calendar.getInstance().getTime();  
    	    			        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");  
    	    			        String strDate = dateFormat.format(date); 
    	    			        
    	    			        
    	    			    	////// Date signature
    	    					if (requestcard.getAccountNumber()!=null)
    	    						replaceText(paragraph, "[DATE_SIGNA]",strDate);
    	    					else 
    	    						replaceText(paragraph, "[DATE_SIGNA]", "");  
    	    			        
    	    					
    	    					
    	    					//fix me
    	    					if(product.get().getLibelle().toLowerCase().contains("visa")) {
    	    						
    	    						if (product.get().getLibelle().toLowerCase().contains("gold")) { 	    						
        	    						replaceText(paragraph, "[G]"," X");
        	    						replaceText(paragraph, "[C]","");
        	    					}else if (product.get().getLibelle().toLowerCase().contains("classique") || 
        	    							product.get().getLibelle().toLowerCase().contains("classic")) {
        	    						replaceText(paragraph, "[G]","");
        	    						replaceText(paragraph, "[C]"," X");

        	    					}
    	    					}else {
    	    						
    	    						if (product.get().getLibelle().toLowerCase().contains("gold")) { 	    						
        	    						replaceText(paragraph, "[G]","  X");
        	    						replaceText(paragraph, "[C]","");
        	    					}else if (product.get().getLibelle().toLowerCase().contains("classique") || 
        	    							product.get().getLibelle().toLowerCase().contains("classic")) {
        	    						replaceText(paragraph, "[G]","");
        	    						replaceText(paragraph, "[C]","  X");

        	    					}
    	    					}
    	    					
//    	    					else if (product.get().getLibelle().toLowerCase().contains("platinum")) {
//    	    						replaceText(paragraph, "[G]","");
//    	    						replaceText(paragraph, "[P]","  X");
//    	    						replaceText(paragraph, "[C]","");
//
//    	    					}
    	    					
    	    					
    	    					////// Raison social
    	    					if (requestcard.getAccountNumber()!=null)
    	    						replaceText(paragraph, "[RAISON_SOCIAL]",requestcard.getRaisonSocial());
    	    					else 
    	    						replaceText(paragraph, "[RAISON_SOCIAL]", "");
    	    					
    	    					////// siege social
    	    					if (requestcard.getAccountNumber()!=null)
    	    						replaceText(paragraph, "[SIEGE_SOCIAL]",requestcard.getSiegeSocial());
    	    					else 
    	    						replaceText(paragraph, "[SIEGE_SOCIAL]", "");
    	    				
    	    					
    	    					////// capital social
    	    					if (requestcard.getCapital()!=null) {
    	    						DecimalFormat decimalFormat = new DecimalFormat("#0.00"); 
    	    						replaceText(paragraph, "[CAPITAL_SOCIAL]",
    	    								decimalFormat.format(Double.parseDouble(requestcard.getCapital())).replace(".", ",") + " DZD"
    	    								);
    	    					}
    	    					else 
    	    						replaceText(paragraph, "[CAPITAL_SOCIAL]", "");
    	    					
    	    					
    	    					////// code nif
    	    					if (requestcard.getCapital()!=null)
    	    						replaceText(paragraph, "[CODE_NIF]",requestcard.getCodeNif());
    	    					else 
    	    						replaceText(paragraph, "[CODE_NIF]", "");
    	    					
    	    					
    	    					
    	    					////// representé par
    	    					if (requestcard.getRepresenteePar()!=null)
    	    						replaceText(paragraph, "[REP_PAR]",requestcard.getRepresenteePar());
    	    					else 
    	    						replaceText(paragraph, "[REP_PAR]", "");
    	    					
    	    					
    	    					////// etablie le
    	    					if (requestcard.getEtablieLe()!=null)
    	    						replaceText(paragraph, "[ETABLIE_LE]",requestcard.getEtablieLe());
    	    					else 
    	    						replaceText(paragraph, "[ETABLIE_LE]", "");
    	    					
    	    					////// etablie par
    	    					if (requestcard.getEtabliePar()!=null)
    	    						replaceText(paragraph, "[ETABLIE_PAR]",requestcard.getEtabliePar());
    	    					else 
    	    						replaceText(paragraph, "[ETABLIE_PAR]", "");
    	    					
    	    					
    	    					////// registre de commerce
    	    					if (requestcard.getRegistreDeCommerce()!=null)
    	    						replaceText(paragraph, "[REGISTRE_DE_COMMERCE]",requestcard.getRegistreDeCommerce());
    	    					else 
    	    						replaceText(paragraph, "[REGISTRE_DE_COMMERCE]", "");
    	    					
    	    					
    	    					////// Account number in 978
    	    					if (requestcard.getAccountNumberAttached()!=null)
    	    						replaceText(paragraph, "[NUM_C_DINARS]",requestcard.getAccountNumberAttached().substring(1));
    	    					else 
    	    						replaceText(paragraph, "[NUM_C_DINARS]", "");
    	    					
    	    					
    	    					
    	    					
    			            }
    			            
    			            
    				
    					
    					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    					document.write(outputStream);
    					document.close();
    					byte[] modifiedFileData = outputStream.toByteArray();
    					return modifiedFileData;
            		}
            		
            	
            		
            		
            	}
            }
            return null;
    	}
    catch(Exception e) {
		logger.info("Exception");
		logger.info(Throwables.getStackTraceAsString ( e ));
		return null;
	}
}

    @PostMapping("verifDoublons")
    public ResponseEntity<String> verifDoublons(@RequestBody RequestCard requestcard) {
    	logger.info("begin verif doublons");
    	Account account= accountRepository.findByAccountNum(requestcard.getAccountNumber());
    	if (account!=null) {
    		List<Card> list=cardRepository.findCardsWithAccCodeAndProd(account.getAccountCode(),requestcard.getProduct());
    		logger.info("list card => {}",list.size());
        	if (list.size()>0) 
        		
        		return	ResponseEntity.ok().body(gson.toJson("account have card with same product!"));
        	
    		
    	}
    	
    	List<RequestCard> old = requestCardRepository.getByNumAccountAndProduct(requestcard.getAccountNumber(), requestcard.getProduct());
    	logger.info("old => {}",old.size());
        if (old.size()>0)
        	return	ResponseEntity.ok().body(gson.toJson("account have card with same product!"));
        
    	
    	
    	
    	return	ResponseEntity.ok().body(gson.toJson("ok"));
    	
    }

    @PostMapping("requestCard")
    public ResponseEntity<String> AddCard(@RequestBody RequestCard requestcard) {
    	
//    	Account account= accountRepository.findByAccountNum(requestcard.getAccountNumber());
//    	if (account!=null) {
//    		List<Card> list=cardRepository.findCardsWithAccCodeAndProd(account.getAccountCode(),requestcard.getProduct());
//        	
//        	if (list.size()>0) 
//        		
//        		return	ResponseEntity.ok().body(gson.toJson("account have card with same product!"));
//        	
//    		
//    	}
//    	
    
    	  Set<Program> programs = new HashSet<>();
    
        requestcard.setCardStatus(2);
        requestcard.setRequestStatus(2);

    	Optional<Product> product= productRepositpry.findById(requestcard.getProduct());
		if (product.isPresent()) {
			programs = product.get().getPrograms();
			for (Program p : programs) {
				if (binOnUsRepository.findById(p.getBinOnUsCode()).isPresent()) {
					StockBin stockbin = stockBinRepository.findBybinOnUsCode(p.getBinOnUsCode());
					if (stockbin.getStockSatim() > 0) {

						stockbin.setStockReserve(stockbin.getStockReserve() + 1);
						stockbin.setStockSatim(stockbin.getStockSatim() - 1);
						stockBinRepository.save(stockbin);
					} else
						throw new RuntimeException();
				}
			}

			if (product.get().getAutoValidation() == 1) {
				requestcard.setPreValidation(0);

			} else
				requestcard.setPreValidation(1);
		}
		
		requestCardRepository.save(requestcard);
		return ResponseEntity.ok().body(gson.toJson("request card added successfully!"));
	}
//    @PostMapping("requestCard")
//    public ResponseEntity<String> AddCard(@RequestBody RequestCard requestcard) {
//        
//        requestcard.setCardStatus(2);
//        requestcard.setRequestStatus(2);
//        requestCardRepository.save(requestcard);
//        return ResponseEntity.ok().body("request card added successfully!");
//    }

    @GetMapping("getRequestCard")
    public List<RequestCard> getRequestCard() {

        return requestCardRepository.findAll();
    }
    
    @GetMapping("getRequestCardById/{id}")
    public RequestDisplay getRequestCardById(@PathVariable(value = "id") Integer idRequest) {
    	try {
        	Optional<RequestCard> request=	requestCardRepository.findById(idRequest);
        	if (request.isPresent()) {
        		if (request.get().getPreValidation()==1) {
        			Optional<AgenceAdministration> agency= agenceAdministrationRepository.findByCodeAgence(request.get().getIdAgence());
        			Optional<Product> product = productRepositpry.findByProductCode(request.get().getProduct());
        			
        			String identificationLabel=identificationRep.findByIdentificationTypeCode(request.get().getIdentificationType()).getLibelle();
        			
        			
        			String accountStatus =AccountStatusRep.findById(String.valueOf(request.get().getAccountStatus())).get().getAstLibelle();
        			String customerStatus= customerStatuRep.findByIdentificationTypeCode(request.get().getCustomerStatus()).getLibelle();
        			
        			RequestDisplay requestDisplay= new RequestDisplay();
        			requestDisplay.setAccountNumber(request.get().getAccountNumber());
        			requestDisplay.setAddress(request.get().getAddress());
        			requestDisplay.setIdAgence(agency.get().getInitial());
        			requestDisplay.setProduct(product.get().getLibelle());
        			requestDisplay.setBirthDate(request.get().getBirthDate());
        			requestDisplay.setNameInCard(request.get().getNameInCard());
        			requestDisplay.setCin(request.get().getCin());
        			requestDisplay.setIdentificationType(identificationLabel);
        			requestDisplay.setAccountStatus(accountStatus);
        			requestDisplay.setCity(request.get().getCity());
        			requestDisplay.setCountry(request.get().getCountry());
        			requestDisplay.setAddress(request.get().getAddress());
        			requestDisplay.setZipCode(request.get().getZipCode());
        			requestDisplay.setPhone(request.get().getPhone());
        			requestDisplay.setGender(request.get().getGender());
        			requestDisplay.setCurrency(request.get().getCurrency());
        			requestDisplay.setCustomerStatus(customerStatus);
        			requestDisplay.setNewCprRiskAmount(request.get().getNewCprRiskAmount());
        			requestDisplay.setNewCprRiskAmountMax(request.get().getNewCprRiskAmountMax());
        			requestDisplay.setNewCprRiskAmountMin(request.get().getNewCprRiskAmountMin());
        			requestDisplay.setNewGlNumber(request.get().getNewGlNumber());
        			requestDisplay.setCeilingsModificationRequest(request.get().getCeilingsModificationRequest());
        			requestDisplay.setNewCprPurchaseMax(request.get().getNewCprPurchaseMax());
        			requestDisplay.setNewCprWithdrawalMax(request.get().getNewCprWithdrawalMax());
        			requestDisplay.setFirstName(request.get().getFirstName());
        			requestDisplay.setLastName(request.get().getLastName());
        			requestDisplay.setEpaymentphone(request.get().getEpaymentphone());
        			requestDisplay.setIncome(request.get().getIncome());
        			requestDisplay.setNewCprEcommerceMax(request.get().getNewCprEcommerceMax());
        			requestDisplay.setRadical(request.get().getRadical());
        			
        			requestDisplay.setRaisonSocial(request.get().getRaisonSocial());
        			requestDisplay.setSiegeSocial(request.get().getSiegeSocial());
        			requestDisplay.setCapital(request.get().getCapital());
        			requestDisplay.setRegistreDeCommerce(request.get().getRegistreDeCommerce());
        			requestDisplay.setEtablieLe(request.get().getEtablieLe());
        			requestDisplay.setEtabliePar(request.get().getEtabliePar());
        			requestDisplay.setCodeNif(request.get().getCodeNif());
        			requestDisplay.setRepresenteePar(request.get().getRepresenteePar());
        			requestDisplay.setAccountNumberAttached(request.get().getAccountNumberAttached());
        			
        			return requestDisplay;
        		}
        	}
    	}catch(Exception e) {
    		
    		logger.info("");
    	}

        return null;
    }


    @PostMapping("addCardStatus")
    public ResponseEntity<String> AddCardStatus(@RequestBody CardStatus card) {

        cardStatusRepository.save(card);
        return ResponseEntity.ok().body("status card added successfully!");
    }

    @PostMapping("generateCard")
    public ResponseEntity<String> generateCard(@RequestBody Card card) {
        return ResponseEntity.ok().body("Card added successfully!");
    }


    @PostMapping("transactionSource")
    public ResponseEntity<String> AddTransactionSource(@RequestBody TransactionSource source) {
        sourceRepository.save(source);
        return ResponseEntity.ok().body("source transaction added successfully!");
    }

    @GetMapping("getAllSource")
    public List<TransactionSource> getTransactionSource() {
        return sourceRepository.findAll();

    }


    @PostMapping("addControlList")
    public ResponseEntity<String> addControlList(@Valid @RequestBody ControlList controlList) {
        controlListRepository.save(controlList);

        return ResponseEntity.ok().body("Control List added successfully!");

    }

    @GetMapping("/controlList/{id}")
    public ResponseEntity<ControlList> getControlList(
            @PathVariable(value = "id") String atmHardFitnessId)
            throws ResourceNotFoundException {
        ControlList product = controlListRepository.findByControlNum(atmHardFitnessId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        " Contrl List not found for this id :: " + atmHardFitnessId));
        logger.info(product.toString());
        return ResponseEntity.ok().body(product);
    }

    @GetMapping("getAllControlList")
    public List<ControlList> getAllControlList() {
        return controlListRepository.findAll();
    }

    @GetMapping("getAllTransactionAllowed")
    public List<EmvServiceValues> getAllTransactionAllowed() {
        return emvServiceValuesRepository.findAll();
    }


   /* @GetMapping("getAllRiskPeriodicity")
    public List<RiskPeriodicity> getAllRiskPeriodicity() {
        return riskPeriodicityRepository.findAll();
    }*/

    @GetMapping("getAllType")
    public List<Periodicity> getAllType() {
        return typeRepository.findAll();
    }
  
  /*@GetMapping("getProgramPrep")
  public List<Program> getAllProgramPrep() {
	List<ProductPrepayer> productPrep=prepRepository.findAll();
	List<Program>programPrep=new ArrayList<Program>();
	if(productPrep.size()>0) {
	
	for(ProductPrepayer p:productPrep) {
		Set<Program> prog=p.getPrograms();
		for(Program program:prog) {
			programPrep.add(program);
		}
	}
	}
	
   /* List<Program> programs= programReposiroty.findAll();
    List<Program> programsPrep= new ArrayList<>();
    for(Program p:programs) {
    	BinOnUs bin=binOnUsRepository.findById(p.getBinOnUsCode()).get();
    	BinOnUsType type=BinTypeBinRepository.findById(bin.getBinTypeCode()).get();
    	System.out.println("typee"+type.getBtCode());
    	if(type.getBtCode()==2) {
    		
    		programsPrep.add(p);
    	}
    }
    return programsPrep;*/
	/*return  programPrep;
  }*/
  
  
  /*@GetMapping("getProgramRevolving")
  public List<Program> getAllProgramRevolving() {
	List<ProductRev> productRev=revRepository.findAll();
	List<Program>programRev=new ArrayList<Program>();
	if(productRev.size()>0) {
	
	for(ProductRev p:productRev) {
		Set<Program> prog=p.getPrograms();
		for(Program program:prog) {
			programRev.add(program);
		}
	}
	}
	
   /* List<Program> programs= programReposiroty.findAll();
    List<Program> programsPrep= new ArrayList<>();
    for(Program p:programs) {
    	BinOnUs bin=binOnUsRepository.findById(p.getBinOnUsCode()).get();
    	BinOnUsType type=BinTypeBinRepository.findById(bin.getBinTypeCode()).get();
    	System.out.println("typee"+type.getBtCode());
    	if(type.getBtCode()==2) {
    		
    		programsPrep.add(p);
    	}
    }
    return programsPrep;*/
    //return  programRev;
    // }

    @GetMapping("getProgramProductRevol")
    public List<Program> getAllProgramRevolProduct() {

        List<Program> programs = programReposiroty.findAll();
        List<Program> programsRev = new ArrayList<>();
        for (Program p : programs) {
            BinOnUs bin = binOnUsRepository.findById(p.getBinOnUsCode()).get();
            BinOnUsType type = BinTypeBinRepository.findById(bin.getBinTypeCode()).get();
            System.out.println("typee" + type.getBtCode());
            if (type.getBtCode() == 3) {

                programsRev.add(p);
            }
        }
        return programsRev;

    }

    @GetMapping("getProgramPrepProduct")
    public List<Program> getAllProgramPrepProduct() {

        List<Program> programs = programReposiroty.findAll();
        List<Program> programsPrep = new ArrayList<>();
        for (Program p : programs) {
            BinOnUs bin = binOnUsRepository.findById(p.getBinOnUsCode()).get();
            BinOnUsType type = BinTypeBinRepository.findById(bin.getBinTypeCode()).get();
            System.out.println("typee" + type.getBtCode());
            if (type.getBtCode() == 2) {

                programsPrep.add(p);
            }
        }
        return programsPrep;

    }

    @PostMapping("addProgram")
    public Program addProgram(@Valid @RequestBody ProgramRequest program)
            throws ResourceNotFoundException {
    	try {
    		 PosEntryModeTypes posEntryModeType = new PosEntryModeTypes();
    	       Country country = new Country();
    	        logger.info(program.toString());

    	        Program program1 = new Program(program.libelle, program.cprLifeCycle, program.cprRenewl,
    	                program.cprContacless, program.cprChip
    	                , program.cprRiskAmountMin, program.cprRiskAmountMax, program.cprRiskAmount,

    	                program.binOnUs, program.riskPeriodicity, program.FirstPosition, program.SecondPosition,
    	                program.ThirdPosition, program.GlNumber, program.StartDate);
    	        program1.setCmsCardType(program.getCmsCardType());
    	      program1.setCardvisualCode(program.getCardvisualCode());
    	      program1.setDaysNumberBeforeObliterate(program.getDaysNumberBeforeObliterate());
    	      program1.setCurrency(program.getCurrency());
    	        Set<String> controList = program.getControlLists();
    	        controList.remove("0");
    	        Set<String> transactions = program.getEmvServiceValues();
    	        Set<ControlList> controlLists = new HashSet<>();
    	        Set<EmvServiceValues> emvServiceValues = new HashSet<>();
    	        Set<PosEntryModeTypes> posEntryModeTypesList = new HashSet<>();
    	        Set<Country> countriesControlList = new HashSet<>();

    	        for (String id : controList

    	        ) {
    	            ControlList controlList = controlListRepository.findByControlNum(id)
    	                    .orElseThrow(
    	                            () -> new ResourceNotFoundException(" Contrl List not found for this id :: " + id));
    	            controlLists.add(controlList);
    	            if(controlList.getLibelleControl().equals("POS_ENTRY_MODE")){
    	                for(int code : program.getPosEntryModeType()){
    	                    posEntryModeType = posEntryModeTypesRepository.findByPosEntryModeCode(code);
    	                    posEntryModeTypesList.add(posEntryModeType);
    	                    program1.setPosEntryModeTypes(posEntryModeTypesList);
    	                }
    	            }
    	            
    	            if(controlList.getLibelleControl().equals("COUNTRY") && program.getCountriesControl().size()>0){
    	                for(int code : program.getCountriesControl()){
    	                	country = countryRepo.findByCountryId(code);
    	                	countriesControlList.add(country);
    	                	program1.setCountriesControl(countriesControlList);
    	                   
    	                }
    	            }

    	        }
    	        if (transactions != null)
    	            for (String id : transactions

    	            ) {
    	                EmvServiceValues serviceValues = emvServiceValuesRepository.findByCodeTransaction(id)
    	                        .orElseThrow(() -> new ResourceNotFoundException(
    	                                " EmvServiceValues not found for this id :: " + id));
    	                emvServiceValues.add(serviceValues);

    	            }
    	        program1.setControlLists(controlLists);
    	        
    	        if (program.getCvrValue() != null) {

    	            if (cvrRepository.findByCvrCodee(Integer.parseInt(program.getCvrValue())).isPresent()) {
    	                Cvr cvr = cvrRepository.findById(Integer.parseInt(program.getCvrValue())).get();
    	                program1.setCvr(cvr.getCvrCodee());

    	            }
    	        }
    	        if(program.getTvrValue() != null){
    	            if (tvrRepository.findByTvrCode(Integer.parseInt(program.getTvrValue())).isPresent()) {
    	                Tvr tvr = tvrRepository.findById(Integer.parseInt(program.getTvrValue())).get();
    	                program1.setTvr(tvr.getTvrCode());
    	            }
    	        }
    	       
    	        
    	        Calendar calendar = Calendar.getInstance();
    	        calendar.setTime(program1.getStartDate());
    	        calendar.set(Calendar.HOUR_OF_DAY, 0);
    	        calendar.set(Calendar.MINUTE, 0);
    	        calendar.set(Calendar.SECOND, 0);
    	        calendar.set(Calendar.MILLISECOND, 0);
    	        program1.setStartDate(calendar.getTime());
    	        logger.info(program1.toString());
    	        program1=programReposiroty.save(program1);
    	        
    	        if (program.getMinAmountContactless()==null) {
    	        	program.setMinAmountContactless(0);
    	        }
    	        if (program.getMaxAmountContactless()==null) {
    	        	program.setMaxAmountContactless(0);
    	        }
    	        
    	        if (program.cprContacless && program.getMinAmountContactless()!=null && program.getMaxAmountContactless()!=null) {
    	        	ContaclessControl contaclessControl=new ContaclessControl();
    	        	contaclessControl.setProgramId(program1.getProgramCode());	
    	        	contaclessControl.setMinAmount(program.getMinAmountContactless());
    	        	contaclessControl.setMaxAmount(program.getMaxAmountContactless());
    	        	contaclessControl.setPin(program.getContactlessWithPin());
    	        	contaclessControlRepository.save(contaclessControl);
    	        }
    	     
    	        return program1;
    		
    	}catch(Exception e) {
    		
    		
    	e.printStackTrace();
			logger.info("Exception is=>{}", Throwables.getStackTraceAsString(e));
			return null;
    	}
      

    }
    @GetMapping("getAllPosEntryModeTypes")
    public List<PosEntryModeTypes> getAllPosEntryModeTypes() {
        return posEntryModeTypesRepository.findAll();
    }
    @GetMapping("getAllProduct")
    public List<Product> getAllProduct()  {
        return productRepositpry.findAll();
    }
    
    @GetMapping("getEligibleProducts/{chapter}")
    public List<Product> getEligibleProducts(@PathVariable(value = "chapter") String chapter)  {
    	List<Product> all=productRepositpry.findAll();
    	List<Product> products= new ArrayList<Product>();
    	for(Product product :all) {
    		
    		for (Program prog : product.getPrograms()) {
    			if (prog.getCurrency().equals("012")) {
    				
    				if (product.getEligibleAccounts()!=null) {
    	    			for(EligibleAccount eligibleAccount:product.getEligibleAccounts()) {
    	        			if (eligibleAccount.getChapter().equals(chapter))
    	        				products.add(product);
    	        		}
    	    		}
    			}
    			
    		}
  
    		
    	}
        return products;
    }

    @GetMapping("getProductPrep")
    public List<ProductPrepayer> getProductPrep() {
        return prepRepository.findAll();
    }

    @GetMapping("getAllProgram")
    public List<Program> getAllProgram() {
        return programReposiroty.findAll();
    }

    @PostMapping("getAllProgramFiltred")
    public List<Program> getAllProgramFiltred(@RequestBody String libelle) {
        if(!libelle.equals("=")){
            return programReposiroty.findAllByProgramLibelle(libelle.trim());
        } else{
            return programReposiroty.findAll();
        }
    }
    
    
    @GetMapping("/motifOppostion")
    public List<MotifOpposition> getAllMotifOpposition(){
        return motifOppositionRepository.findAll();
    }




    //get program
    @GetMapping("/program/{id}")
    public ResponseEntity<Program> getProgramById(@PathVariable(value = "id") String id)
            throws ResourceNotFoundException {
        Program program = programReposiroty.findById(Integer.parseInt(id))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "program not found for this id :: " + id));
        logger.info(program.toString());
        return ResponseEntity.ok().body(program);
    }

	@GetMapping("/allEligibleAccounts")
	public List<EligibleAccount> getEligibleAccounts() {
		return eligibleAccountRepository.findAll();
	}
	@GetMapping("/getMotifRenewelCard")
	public List<MotifRenewelCard> getMotifCloseCard() {
		return motifRenewelCardRepository.findAll();
	}
	
	@GetMapping("/getMotifReeditionPin")
	public List<MotifReeditionPinCard> getMotifReeditionPin() {
		return motifReditionPinCardRepository.findAll();
	}
	
	
	
    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable(value = "id") String atmHardFitnessId)
            throws ResourceNotFoundException {
        Product product = productRepositpry.findByProductCode(Integer.parseInt(atmHardFitnessId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found for this id :: " + atmHardFitnessId));
        logger.info(product.toString());
        return ResponseEntity.ok().body(product);
    }
    @GetMapping("getContactlessControlByProg/{id}")
    public ContaclessControl getContactlessControlByProg(@PathVariable(value = "id") Integer id) {
    	Optional<ContaclessControl> control =contaclessControlRepository.findByProgramId(id);
     return  control.isPresent()? control.get():null;

    }
    @PutMapping("removeOpposition/{id}")
    public ResponseEntity<Card> removeOpposition(@PathVariable(value = "id") Integer cardCode) {
      Optional<OpposedCard>	opposedCard=opposedCardRepository.findByCardCode(cardCode);
		Card card = cardRepository.findByCardCode(cardCode).get();
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving status !");
		}
      
      if (opposedCard.isPresent()) {
    	  if (opposedCard.get().getMotifOpposition().equals("LOST")) {
    		  card.setCardStatusCode(2);
    			card.setModifDate(new Date());
    			cardRepository.save(card);
    		  opposedCardRepository.deleteByCardCode(cardCode);
    		  
    		  CardHistory cardHistory = new CardHistory();
    			cardHistory.setCardCode(card.getCardCode());
    			cardHistory.setOperation("Changer le statut de la carte en Actif");
    			cardHistory.setOperation_date(card.getModifDate());
    			cardHistory.setEditedBy(user.get().getUserName());
    			cardHistoryRepository.save(cardHistory);
    			
    			return ResponseEntity.ok(card);
    	  }
    		 
    		
      }
    	  
      return null;
    }
	@PutMapping("activate/{id}")
	public ResponseEntity<Card> activate(@PathVariable(value = "id") Integer cardCode) {
		Card card = cardRepository.findByCardCode(cardCode).get();
		
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving status !");
		}
		card.setCardStatusCode(2);
		card.setModifDate(new Date());
		cardRepository.save(card);

		CardHistory cardHistory = new CardHistory();
		cardHistory.setCardCode(card.getCardCode());
		cardHistory.setOperation("Changer le statut de la carte en Actif");
		cardHistory.setOperation_date(card.getModifDate());
		cardHistory.setEditedBy(user.get().getUserName());
		cardHistoryRepository.save(cardHistory);
		
		return ResponseEntity.ok(card);
	}
	
	@PutMapping("activateCardFromMobile/{id}")
	public ResponseEntity<Card> activateCardFromMobile(@PathVariable(value = "id") Integer cardCode,@RequestBody CardFormMobileActivationData request) {
		Card card = cardRepository.findByCardCode(cardCode).get();
		
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving status !");
		}
		 Date currentDate = new Date();
		 
			Optional<DetailRiskManagment> detailRiskManagment = detailRMRepository
					.findByGlobalCodeAndTransactionCode(Integer.parseInt(card.getGlobalRiskCode()) ,
							"50");
		 if (detailRiskManagment.isPresent()) {
				
				if (card.getIsFromMobile()) {
		             
		        	EpaymentInfo epayement=createEpaymentInfoData(card,currentDate,request.getEpaymentphone(),user.get().getUserName()); 
		        	epaymentInfoRepository.save(epayement);
		            logger.info("after epayment save");	
		        }	
		}
		
		
		card.setCardStatusCode(2);
		card.setModifDate(currentDate);
		
		cardRepository.save(card);

		CardHistory cardHistory = new CardHistory();
		cardHistory.setCardCode(card.getCardCode());
		cardHistory.setOperation("Changer le statut de la carte en Actif");
		cardHistory.setOperation_date(card.getModifDate());
		cardHistory.setEditedBy(user.get().getUserName());
		cardHistoryRepository.save(cardHistory);
		
		return ResponseEntity.ok(card);
	}
	
	
	@PutMapping("unblockPin/{id}")
	public ResponseEntity<Card> unblockPin(@PathVariable(value = "id") Integer cardCode) {
		Card card = cardRepository.findByCardCode(cardCode).get();
		
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving status !");
		}
		card.setCardStatusCode(2);
		card.setPinTryLimit(0);
		card.setModifDate(new Date());
		cardRepository.save(card);

		CardHistory cardHistory = new CardHistory();
		cardHistory.setCardCode(card.getCardCode());
		cardHistory.setOperation("Débloquer la limite d'essai de PIN de carte");
		cardHistory.setOperation_date(card.getModifDate());
		cardHistory.setEditedBy(user.get().getUserName());
		cardHistoryRepository.save(cardHistory);
		
		return ResponseEntity.ok(card);
	}
	
	@PutMapping("deliverCard/{id}")
	public ResponseEntity<Card> deliverCard(@PathVariable(value = "id") Integer cardCode) {
		Card card = cardRepository.findByCardCode(cardCode).get();
		
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving status !");
		}
		card.setCardActionsStatus(null);
		card.setModifDate(new Date());
		card.setAtc("0");
		cardRepository.save(card);

		CardHistory cardHistory = new CardHistory();
		cardHistory.setCardCode(card.getCardCode());
		cardHistory.setOperation("Carte délivrée au client");
		cardHistory.setOperation_date(card.getModifDate());
		cardHistory.setEditedBy(user.get().getUserName());
		cardHistoryRepository.save(cardHistory);
		
		return ResponseEntity.ok(card);
	}
	
	@PutMapping("ackCard/{id}")
	public ResponseEntity<Card> ackCard(@PathVariable(value = "id") Integer cardCode) {
		Card card = cardRepository.findByCardCode(cardCode).get();
		
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving status !");
		}
		if (card.getCardStatusCode()==2 && card.getCardActionsStatus().equals("9"))
			card.setCardActionsStatus("10");
		else
			card.setCardStatusCode(10);
		
		card.setModifDate(new Date());
		cardRepository.save(card);

		CardHistory cardHistory = new CardHistory();
		cardHistory.setCardCode(card.getCardCode());
		cardHistory.setOperation("Carte reçue en agence");
		cardHistory.setOperation_date(card.getModifDate());
		cardHistory.setEditedBy(user.get().getUserName());
		cardHistoryRepository.save(cardHistory);
		
		return ResponseEntity.ok(card);
	}
	
	@PutMapping("ackListCard")
	public void ackListCard(@RequestBody List<Integer> cards) {
		
		for (Integer cardCode:cards) {
			Optional<Card> foundedCard=cardRepository.findByCardCode(cardCode);
			if (foundedCard.isPresent()) {
				Card card = foundedCard.get();
				
				String name = SecurityContextHolder.getContext().getAuthentication().getName();
				Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
				if (!user.isPresent()) {
					throw new RuntimeException("Error saving status !");
				}
				if (card.getCardStatusCode()==2 && card.getCardActionsStatus().equals("9"))
					card.setCardActionsStatus("10");
				else
					card.setCardStatusCode(10);
				
				card.setModifDate(new Date());
				cardRepository.save(card);

				CardHistory cardHistory = new CardHistory();
				cardHistory.setCardCode(card.getCardCode());
				cardHistory.setOperation("Carte reçue en agence");
				cardHistory.setOperation_date(card.getModifDate());
				cardHistory.setEditedBy(user.get().getUserName());
				cardHistoryRepository.save(cardHistory);
				
			}
			
		}

	}
	
	@PostMapping("getAllCardForObliteration")
    public Page<Card> getAllCardForObliteration(@RequestBody BranchFilter branchFilter,
     @RequestParam(name = "page", defaultValue = "0") int page,
     @RequestParam(name = "size", defaultValue = "10") int size
	    ) {
		
		return cardRepository.getCardsForObliteration(PageRequest.of(page, size), branchFilter.getBranch());
		
	}
	
	
	@PostMapping("getEmbossingSummaries")
    public Page<EmbossingSummary> getEmbossingSummaries(@RequestBody DateFilter dateFilter,
     @RequestParam(name = "page", defaultValue = "0") int page,
     @RequestParam(name = "size", defaultValue = "10") int size
	    ) {
	return	embossingSummaryRepository.getAll(PageRequest.of(page, size),dateFilter.getDate());
		
		
	}
	
	@PostMapping("getEpanSummaries")
    public Page<EpanSummary> getEpanSummaries(@RequestBody DateFilter dateFilter,
     @RequestParam(name = "page", defaultValue = "0") int page,
     @RequestParam(name = "size", defaultValue = "10") int size
	    ) {
	return	epanSummaryRepository.getAll(PageRequest.of(page, size),dateFilter.getDate());
		
		
	}
	
	@PostMapping("getBeSummaries")
    public Page<BESummary> getBeSummaries(@RequestBody DateFilter dateFilter,
     @RequestParam(name = "page", defaultValue = "0") int page,
     @RequestParam(name = "size", defaultValue = "10") int size
	    ) {
	return	bESummaryRepository.getAll(PageRequest.of(page, size),dateFilter.getDate());
		
		
	}
	
	@PostMapping("getAcsSummaries")
    public Page<AcsSummary> getAcsSummaries(@RequestBody DateFilter dateFilter,
     @RequestParam(name = "page", defaultValue = "0") int page,
     @RequestParam(name = "size", defaultValue = "10") int size
	    ) {
	return	acsSummaryRepository.getAll(PageRequest.of(page, size),dateFilter.getDate());
		
		
	}
	
	@PostMapping("getAllCardForReeditionPin")
    public Page<Card> getAllCardForReeditionPin(@RequestBody BranchFilter branchFilter,
     @RequestParam(name = "page", defaultValue = "0") int page,
     @RequestParam(name = "size", defaultValue = "10") int size
	    ) {
		
		return cardRepository.getCardsForReeditionPin(PageRequest.of(page, size), branchFilter.getBranch());
		
	}
	
	

	@PutMapping("disable/{id}")
	public ResponseEntity<Card> disable(@PathVariable(value = "id") Integer cardCode) {
		Card card = cardRepository.findByCardCode(cardCode).get();
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving status !");
		}
		card.setCardStatusCode(3);
		card.setModifDate(new Date());
		cardRepository.save(card);

		CardHistory cardHistory = new CardHistory();
		cardHistory.setCardCode(card.getCardCode());
		cardHistory.setOperation("Changer le statut de la carte en Désactivé");
		cardHistory.setOperation_date(card.getModifDate());

		cardHistory.setEditedBy(user.get().getUserName());
		cardHistoryRepository.save(cardHistory);
		
		return ResponseEntity.ok(card);
	}

//	@PutMapping("block/{id}/{motif}/{blockedBy}")
//	public ResponseEntity<Card> block(
//			@PathVariable(value = "id") Integer cardCode,
//			@PathVariable(value = "motif") String motif,
//			@PathVariable(value = "blockedBy") String blockedBy) {
//		Card card = cardRepository.findByCardCode(cardCode).get();
//		Optional<Account> account = accountRepository.findByAccountCode(card.getAccCode());
//		Optional<Product> product =productRepositpry.findByProductCode(card.getProductCode());
//		List<DayOperationCardFransaBank> dayOperations = new ArrayList<DayOperationCardFransaBank>();
//
//		
//		String name = SecurityContextHolder.getContext().getAuthentication().getName();
//		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
//		if (!user.isPresent()) {
//			throw new RuntimeException("Error saving status !");
//		}
//		card.setCardStatusCode(4);
//		card.setModifDate(new Date());
//		cardRepository.save(card);
//		CardHistory cardHistory = new CardHistory();
//		cardHistory.setCardCode(card.getCardCode());
//		String volonte="";
//		if (blockedBy.equals("CUSTOMER"))
//			volonte="Volonté client";
//		else volonte="Volonté banque";
//				
//		cardHistory.setOperation("Changer le statut de la carte à Résiliée ("+volonte+"). Motif: "+motif);
//		cardHistory.setOperation_date(card.getModifDate());
//
//		cardHistory.setEditedBy(user.get().getUserName());
//		
//		cardHistory.setBlockedBy(name);
//		cardHistoryRepository.save(cardHistory);
//		
//		
//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
//		SimpleDateFormat filedateFormat = new SimpleDateFormat("yyMMdd");
//
//		SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
//		SimpleDateFormat datePieceComptableFormat = new SimpleDateFormat("ddMMyy");
//
//		Date date=new Date();
//		String transactionDate=dateFormat.format(date);
//		String transactionTime=timeFormat.format(date);
//		
//		
//		DayOperationFeesCardSequence  seq=dayOperationCardSequenceService.getSequence();
//		DayOperationFeesCardSequence  seqPieceComptable=dayOperationCardSequenceService.getSequencePieceComptable();
//		TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
//		float tvaBase=Float.parseFloat(tvaCommissionFransaBank.getTva())  /100;
//		
//		float tva=tvaBase+1;
//		logger.info("TVA is => {}",tva);
//	
//		
//		
//		int settlementAmountHt=Math.round(product.get().getCpCancelation()/tva);
//		logger.info("settlementAmountHt is => {}",settlementAmountHt);
//		
//		int settlementAmountTva=product.get().getCpCancelation()-settlementAmountHt;
//		
//		logger.info("settlementAmountTva is => {}",settlementAmountTva);
//		
//		int transactionAmount=product.get().getCpCancelation();
//		logger.info("transactionAmount is => {}",transactionAmount);
//		
//		String montantTrans=String.format("%014d",transactionAmount);
//		montantTrans=montantTrans.substring(0,12)+"."+montantTrans.substring(12);
//		
//		if (product.get().getLibelle().toLowerCase().contains("gold")) {
//			SettelementFransaBank settlc009 =settlementRepo.findByIdentificationbh("C009");
//			SettelementFransaBank settlc010 =settlementRepo.findByIdentificationbh("C010");
//
//			
//			DayOperationCardFransaBank C009 = new DayOperationCardFransaBank();
//			C009.setCodeAgence(card.getAgencyCode());
//			C009.setCodeBankAcquereur("035");
//			C009.setCodeBank("035");
//			C009.setCompteDebit("0"+account.get().getAccountNum());
//			C009.setDateTransaction(transactionDate);
//			C009.setHeureTransaction(transactionTime);
//			C009.setCompteCredit(settlc009.getCreditAccount());
//			C009.setNumCartePorteur(card.getCardNum());
//			C009.setNumtransaction(	String.format("%012d", seq.getSequence()));
//			C009.setIdenfication(settlc009.getIdentificationbh());
//			
//			
//
//			C009.setMontantSettlement(settlementAmountHt);
//			
//			
//			C009.setMontantTransaction(montantTrans);
//			C009.setLibelleCommercant("ANNULATIONMB00432");
//			
//		    //bkmvtiFransaBank.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));
//			
//			
//			C009.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
//			C009.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
//			C009.setFileDate(filedateFormat.format(date));
//			C009.setNumAutorisation(C009.getNumtransaction());
//			C009.setNumRefTransaction(C009.getNumtransaction());
//			C009.setCardProduct("gold");
//			
//
//			//seq = dayOperationCardSequenceService.incrementSequence(seq);
//
//			// ecriture tva
//			DayOperationCardFransaBank C010 = new DayOperationCardFransaBank();
//			C010.setCodeAgence(card.getAgencyCode());
//			C010.setCodeBankAcquereur("035");
//			C010.setCodeBank("035");
//			C010.setCompteDebit("0" + account.get().getAccountNum());
//			C010.setDateTransaction(transactionDate);
//			C010.setHeureTransaction(transactionTime);
//			C010.setCompteCredit(settlc010.getCreditAccount());
//			C010.setNumCartePorteur(card.getCardNum());
//			C010.setNumtransaction(String.format("%012d", seq.getSequence()));
//			C010.setIdenfication(settlc010.getIdentificationbh());
//
//			C010.setMontantSettlement(settlementAmountTva);
//
//			C010.setMontantTransaction(montantTrans);
//			C010.setLibelleCommercant("ANNULATIONMB00432");
//			
//			C010.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
//			C010.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
//			C010.setFileDate(filedateFormat.format(date));
//			C010.setNumAutorisation(C010.getNumtransaction());
//			C010.setNumRefTransaction(C010.getNumtransaction());
//			C010.setCardProduct("gold");
//		
//				
//			
//			
//				
//		
//			dayOperations.add(C009);
//			dayOperations.add(C010);
//			
//		}else {
//			SettelementFransaBank settlc017 =settlementRepo.findByIdentificationbh("C017");
//			SettelementFransaBank settlc018 =settlementRepo.findByIdentificationbh("C018");
//
//			
//			DayOperationCardFransaBank C017 = new DayOperationCardFransaBank();
//			C017.setCodeAgence(card.getAgencyCode());
//			C017.setCodeBankAcquereur("035");
//			C017.setCodeBank("035");
//			C017.setCompteDebit("0"+account.get().getAccountNum());
//			C017.setDateTransaction(transactionDate);
//			C017.setHeureTransaction(transactionTime);
//			C017.setCompteCredit(settlc017.getCreditAccount());
//			C017.setNumCartePorteur(card.getCardNum());
//			C017.setNumtransaction(	String.format("%012d", seq.getSequence()));
//			C017.setIdenfication(settlc017.getIdentificationbh());
//			C017.setMontantSettlement(settlementAmountHt);	
//			C017.setMontantTransaction(montantTrans);
//			C017.setLibelleCommercant("ANNULATIONMB00432");
//			
//		    //bkmvtiFransaBank.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));
//			
//			
//			C017.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
//			C017.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
//			C017.setFileDate(filedateFormat.format(date));
//			C017.setNumAutorisation(C017.getNumtransaction());
//			C017.setNumRefTransaction(C017.getNumtransaction());
//			C017.setCardProduct("classic");
//			
//
//			//seq = dayOperationCardSequenceService.incrementSequence(seq);
//
//			// ecriture tva
//			DayOperationCardFransaBank C018 = new DayOperationCardFransaBank();
//			C018.setCodeAgence(card.getAgencyCode());
//			C018.setCodeBankAcquereur("035");
//			C018.setCodeBank("035");
//			C018.setCompteDebit("0" + account.get().getAccountNum());
//			C018.setDateTransaction(transactionDate);
//			C018.setHeureTransaction(transactionTime);
//			C018.setCompteCredit(settlc018.getCreditAccount());
//			C018.setNumCartePorteur(card.getCardNum());
//			C018.setNumtransaction(String.format("%012d", seq.getSequence()));
//			C018.setIdenfication(settlc018.getIdentificationbh());
//
//			C018.setMontantSettlement(settlementAmountTva);
//
//			C018.setMontantTransaction(montantTrans);
//			C018.setLibelleCommercant("ANNULATIONMB00432");
//			
//			C018.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
//			C018.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
//			C018.setFileDate(filedateFormat.format(date));
//			C018.setNumAutorisation(C018.getNumtransaction());
//			C018.setNumRefTransaction(C018.getNumtransaction());
//			C018.setCardProduct("classic");
//
//			dayOperations.add(C017);
//			dayOperations.add(C018);
//			
//			
//		}
//		operationRepo.saveAll(dayOperations);
//		seq = dayOperationCardSequenceService.incrementSequence(seq);
//		seqPieceComptable=dayOperationCardSequenceService.incrementSequencePieceComptable(seqPieceComptable);
//
//
//		return ResponseEntity.ok(card);
//	}
	
	
	@PutMapping("demandeBlockCard/{id}/{motif}/{blockedBy}")
	public ResponseEntity<?> demandeBlockCard(@PathVariable(value = "id") Integer cardCode,
			@PathVariable(value = "motif") String motif, @PathVariable(value = "blockedBy") String blockedBy) {

		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving status !");
		}
		Card card = cardRepository.findByCardCode(cardCode).get();
		Optional<DemandeAction> olddemandeAction = demandeActionRepository
				.findProcessingRequestByCardNum(card.getCardNum());

		if (olddemandeAction.isPresent()) {

			return ResponseEntity.ok().body(gson.toJson("this card has already a request to validate!"));
		}
		Optional<Account> account=accountRepository.findByAccountCode(card.getAccCode());
		DemandeAction demandeAction = new DemandeAction(card.getCardNum(), 5, 2, card.getAgencyCode(),
				user.get().getUserName(),account.get().getAccountNum());
	
		CardHistory cardHistory = new CardHistory();
		cardHistory.setCardCode(card.getCardCode());
		cardHistory.setOperation("Demande de résiliation de le carte");
		cardHistory.setOperation_date(new Date());
		cardHistory.setEditedBy(user.get().getUserName());
		cardHistoryRepository.save(cardHistory);
		demandeAction.setMotifBlock(motif);
		demandeAction.setBlockBy(blockedBy);
		demandeActionRepository.save(demandeAction);
		
		
		

		return ResponseEntity.ok().body(gson.toJson("Request block card added successfully!"));
	}

	@PutMapping("validateBlockCard/{id}")
	public ResponseEntity<?> validateBlockCard(@PathVariable(value = "id") Integer demandeID) {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving status !");
		}
		Optional<DemandeAction> demandeAction = demandeActionRepository.findById(demandeID);
		Card card = cardRepository.findByCardNum(demandeAction.get().getCardNum()).get();
		card.setCardStatusCode(4);
		card.setModifDate(new Date());
		card=cardRepository.save(card);
		CardHistory cardHistory = new CardHistory();
		cardHistory.setCardCode(card.getCardCode());
		String volonte = "";
		if (demandeAction.get().getBlockBy().equals("CUSTOMER"))
			volonte = "Volonté client";
		else
			volonte = "Volonté banque";

		cardHistory.setOperation("Changer le statut de la carte à Résiliée (" + volonte + "). Motif: "
				+ demandeAction.get().getMotifBlock());
		cardHistory.setOperation_date(card.getModifDate());

		cardHistory.setEditedBy(user.get().getUserName());

		cardHistoryRepository.save(cardHistory);

		demandeAction.get().setDemandeStatusCode(1);
		demandeActionRepository.save(demandeAction.get());
		Optional<Product> product =productRepositpry.findByProductCode(card.getProductCode());
		if (product.get().getCpCancelation()>0) {
			
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat filedateFormat = new SimpleDateFormat("yyMMdd");

		SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
		SimpleDateFormat datePieceComptableFormat = new SimpleDateFormat("ddMMyy");

		Date date=new Date();
		String transactionDate=dateFormat.format(date);
		String transactionTime=timeFormat.format(date);
		
		
		DayOperationFeesCardSequence  seq=dayOperationCardSequenceService.getSequence();
		DayOperationFeesCardSequence  seqPieceComptable=dayOperationCardSequenceService.getSequencePieceComptable();
		TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
		float tvaBase=Float.parseFloat(tvaCommissionFransaBank.getTva())  /100;
		
		float tva=tvaBase+1;
		logger.info("TVA is => {}",tva);
		List<DayOperationCardFransaBank> dayOperations = new ArrayList<DayOperationCardFransaBank>();
	
		Optional<Account> account=accountRepository.findByAccountCode(card.getAccCode());

		
		
		int settlementAmountHt=Math.round(product.get().getCpCancelation()/tva);
		logger.info("settlementAmountHt is => {}",settlementAmountHt);
		
		int settlementAmountTva=product.get().getCpCancelation()-settlementAmountHt;
		
		logger.info("settlementAmountTva is => {}",settlementAmountTva);
		
		int transactionAmount=product.get().getCpCancelation();
		logger.info("transactionAmount is => {}",transactionAmount);
		
		String montantTrans=String.format("%014d",transactionAmount);
		montantTrans=montantTrans.substring(0,12)+"."+montantTrans.substring(12);
		if (product.get().getLibelle().toLowerCase().contains("visa")) {

			OperationCodeCommision settlc027 = operationCodeInternationalRepository.findByIdentification("C027").get();
			OperationCodeCommision settlc028 = operationCodeInternationalRepository.findByIdentification("C028").get();
			DayOperationCardFransaBank C027 = new DayOperationCardFransaBank();
			C027.setCodeAgence(card.getAgencyCode());
			C027.setCodeBankAcquereur("035");
			C027.setCodeBank("035");
			C027.setCompteDebit("0"+(account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
			C027.setDateTransaction(transactionDate);
			C027.setHeureTransaction(transactionTime);
			C027.setCompteCredit(settlc027.getCreditAccount());
			C027.setNumCartePorteur(card.getCardNum());
			C027.setNumtransaction(	String.format("%012d", seq.getSequence()));
			C027.setIdenfication(settlc027.getIdentification());
			
			

			C027.setMontantSettlement(settlementAmountHt);
			
			
			C027.setMontantTransaction(montantTrans);
			C027.setLibelleCommercant("ANNULATIONMB00432");
			
		    //bkmvtiFransaBank.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));
			
			
			C027.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
			C027.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
			C027.setFileDate(filedateFormat.format(date));
			C027.setNumAutorisation(C027.getNumtransaction());
			C027.setNumRefTransaction(C027.getNumtransaction());
			C027.setCardProduct("visa");
			

			//seq = dayOperationCardSequenceService.incrementSequence(seq);

			// ecriture tva
			DayOperationCardFransaBank C028 = new DayOperationCardFransaBank();
			C028.setCodeAgence(card.getAgencyCode());
			C028.setCodeBankAcquereur("035");
			C028.setCodeBank("035");
			C028.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
			C028.setDateTransaction(transactionDate);
			C028.setHeureTransaction(transactionTime);
			C028.setCompteCredit(settlc028.getCreditAccount());
			C028.setNumCartePorteur(card.getCardNum());
			C028.setNumtransaction(String.format("%012d", seq.getSequence()));
			C028.setIdenfication(settlc028.getIdentification());

			C028.setMontantSettlement(settlementAmountTva);

			C028.setMontantTransaction(montantTrans);
			C028.setLibelleCommercant("ANNULATIONMB00432");
			
			C028.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
			C028.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
			C028.setFileDate(filedateFormat.format(date));
			C028.setNumAutorisation(C028.getNumtransaction());
			C028.setNumRefTransaction(C028.getNumtransaction());
			C028.setCardProduct("visa");
		
				
			
			
				
		
			dayOperations.add(C027);
			dayOperations.add(C028);
		}
		else if (product.get().getLibelle().toLowerCase().contains("gold")) {
			SettelementFransaBank settlc009 =settlementRepo.findByIdentificationbh("C009");
			SettelementFransaBank settlc010 =settlementRepo.findByIdentificationbh("C010");

			
			DayOperationCardFransaBank C009 = new DayOperationCardFransaBank();
			C009.setCodeAgence(card.getAgencyCode());
			C009.setCodeBankAcquereur("035");
			C009.setCodeBank("035");
			C009.setCompteDebit("0"+(account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
			C009.setDateTransaction(transactionDate);
			C009.setHeureTransaction(transactionTime);
			C009.setCompteCredit(settlc009.getCreditAccount());
			C009.setNumCartePorteur(card.getCardNum());
			C009.setNumtransaction(	String.format("%012d", seq.getSequence()));
			C009.setIdenfication(settlc009.getIdentificationbh());
			
			

			C009.setMontantSettlement(settlementAmountHt);
			
			
			C009.setMontantTransaction(montantTrans);
			C009.setLibelleCommercant("ANNULATIONMB00432");
			
		    //bkmvtiFransaBank.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));
			
			
			C009.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
			C009.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
			C009.setFileDate(filedateFormat.format(date));
			C009.setNumAutorisation(C009.getNumtransaction());
			C009.setNumRefTransaction(C009.getNumtransaction());
			C009.setCardProduct("gold");
			

			//seq = dayOperationCardSequenceService.incrementSequence(seq);

			// ecriture tva
			DayOperationCardFransaBank C010 = new DayOperationCardFransaBank();
			C010.setCodeAgence(card.getAgencyCode());
			C010.setCodeBankAcquereur("035");
			C010.setCodeBank("035");
			C010.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
			C010.setDateTransaction(transactionDate);
			C010.setHeureTransaction(transactionTime);
			C010.setCompteCredit(settlc010.getCreditAccount());
			C010.setNumCartePorteur(card.getCardNum());
			C010.setNumtransaction(String.format("%012d", seq.getSequence()));
			C010.setIdenfication(settlc010.getIdentificationbh());

			C010.setMontantSettlement(settlementAmountTva);

			C010.setMontantTransaction(montantTrans);
			C010.setLibelleCommercant("ANNULATIONMB00432");
			
			C010.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
			C010.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
			C010.setFileDate(filedateFormat.format(date));
			C010.setNumAutorisation(C010.getNumtransaction());
			C010.setNumRefTransaction(C010.getNumtransaction());
			C010.setCardProduct("gold");
		
				
			
			
				
		
			dayOperations.add(C009);
			dayOperations.add(C010);
			
		}else {
			SettelementFransaBank settlc017 =settlementRepo.findByIdentificationbh("C017");
			SettelementFransaBank settlc018 =settlementRepo.findByIdentificationbh("C018");

			
			DayOperationCardFransaBank C017 = new DayOperationCardFransaBank();
			C017.setCodeAgence(card.getAgencyCode());
			C017.setCodeBankAcquereur("035");
			C017.setCodeBank("035");
			C017.setCompteDebit("0"+(account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
			C017.setDateTransaction(transactionDate);
			C017.setHeureTransaction(transactionTime);
			C017.setCompteCredit(settlc017.getCreditAccount());
			C017.setNumCartePorteur(card.getCardNum());
			C017.setNumtransaction(	String.format("%012d", seq.getSequence()));
			C017.setIdenfication(settlc017.getIdentificationbh());
			C017.setMontantSettlement(settlementAmountHt);	
			C017.setMontantTransaction(montantTrans);
			C017.setLibelleCommercant("ANNULATIONMB00432");
			
		    //bkmvtiFransaBank.setPieceComptable("DB" + "***" + String.format("%06d", indexPieceComptable));
			
			
			C017.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
			C017.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
			C017.setFileDate(filedateFormat.format(date));
			C017.setNumAutorisation(C017.getNumtransaction());
			C017.setNumRefTransaction(C017.getNumtransaction());
			C017.setCardProduct("classic");
			

			//seq = dayOperationCardSequenceService.incrementSequence(seq);

			// ecriture tva
			DayOperationCardFransaBank C018 = new DayOperationCardFransaBank();
			C018.setCodeAgence(card.getAgencyCode());
			C018.setCodeBankAcquereur("035");
			C018.setCodeBank("035");
			C018.setCompteDebit("0" + (account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
			C018.setDateTransaction(transactionDate);
			C018.setHeureTransaction(transactionTime);
			C018.setCompteCredit(settlc018.getCreditAccount());
			C018.setNumCartePorteur(card.getCardNum());
			C018.setNumtransaction(String.format("%012d", seq.getSequence()));
			C018.setIdenfication(settlc018.getIdentificationbh());

			C018.setMontantSettlement(settlementAmountTva);

			C018.setMontantTransaction(montantTrans);
			C018.setLibelleCommercant("ANNULATIONMB00432");
			
			C018.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
			C018.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
			C018.setFileDate(filedateFormat.format(date));
			C018.setNumAutorisation(C018.getNumtransaction());
			C018.setNumRefTransaction(C018.getNumtransaction());
			C018.setCardProduct("classic");

			dayOperations.add(C017);
			dayOperations.add(C018);
			
			
		}
		operationRepo.saveAll(dayOperations);
		seq = dayOperationCardSequenceService.incrementSequence(seq);
		seqPieceComptable=dayOperationCardSequenceService.incrementSequencePieceComptable(seqPieceComptable);

		}
		

		return ResponseEntity.ok().body(gson.toJson("Request validated successfully!"));
	}

	@PutMapping("rejetDemandeAction/{id}")
	public ResponseEntity<?> rejetDemandeAction(@PathVariable(value = "id") Integer demandeID) {
		try {
			Optional<DemandeAction> demandeAction = demandeActionRepository.findById(demandeID);
			String name = SecurityContextHolder.getContext().getAuthentication().getName();
			Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
			if (!user.isPresent()) {
				throw new RuntimeException("Error saving status !");
			}
			demandeAction.get().setDemandeStatusCode(3);
			demandeActionRepository.save(demandeAction.get());
			Card card = cardRepository.findByCardNum(demandeAction.get().getCardNum()).get();

			CardHistory cardHistory = new CardHistory();
			cardHistory.setCardCode(card.getCardCode());
			
			cardHistory.setOperation_date(new Date());
			cardHistory.setEditedBy(user.get().getUserName());
			if (demandeAction.get().getDemandeType()==5) 
				cardHistory.setOperation("Demande de résiliation refusée");
			else if (demandeAction.get().getDemandeType()==4) 
				cardHistory.setOperation("Demande de changement des plafonds refusée");

			cardHistoryRepository.save(cardHistory);
			
			
			return ResponseEntity.ok().body(gson.toJson("request rejected successfully!"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok().body(gson.toJson("request not rejected successfully!"));
		}
	}
	
	@PutMapping("receiveCardsForReeditionPin")
	public ResponseEntity<Card> receiveCardsForReeditionPin(@RequestBody GenerateFileForSmtRequest cardCodes){
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(username, username);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving risk !");
		}
		for (Integer code : cardCodes.getCardCodes()) {
			Optional<Card> card = cardRepository.findByCardCode(code);
			
			 Optional<Program> foundedProgram = programReposiroty.findByProgramCode(card.get().getProgrameId());
			   if (foundedProgram.isPresent()) {
				  
//				   StockBin stockbin = stockBinRepository.findBybinOnUsCode(foundedProgram.get().getBinOnUsCode());
//	             stockbin.setStockReserve(stockbin.getStockReserve()-1) ; 
//	             
//	             stockbin.setStockConsome(stockbin.getStockConsome()+1);
	             card.get().setCardActionsStatus("7");
	             
	             CardHistory cardHistory = new CardHistory();
					cardHistory.setCardCode(card.get().getCardCode());
					cardHistory.setOperation("Carte reçue au BO");
					cardHistory.setOperation_date(new Date());
					cardHistory.setEditedBy(user.get().getUserName());
					
					cardRepository.save(card.get());
				//	stockBinRepository.save(stockbin);
					cardHistoryRepository.save(cardHistory);
					
					return ResponseEntity.ok(card.get());
			   }
			
		}
		
		
		   return null;
		
	}

	@PutMapping("opposition/{id}/{motif}")
	public ResponseEntity<Card> opposition(@PathVariable(value = "id") Integer cardCode,
			@PathVariable(value = "motif") String motif) {
		Card card = cardRepository.findByCardCode(cardCode).get();
		
		Optional<Account> account = accountRepository.findByAccountCode(card.getAccCode());
		Optional<Product> product =productRepositpry.findByProductCode(card.getProductCode());

		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving status !");
		}
		
		Optional<MotifOpposition> motifOpp= motifOppositionRepository.findByMotifOpposition(motif);
		List<DayOperationCardFransaBank> dayOperations = new ArrayList<DayOperationCardFransaBank>();

		card.setCardStatusCode(5);
		card.setModifDate(new Date());
		
		CardHistory cardHistory = new CardHistory();
		cardHistory.setCardCode(card.getCardCode());
		cardHistory.setOperation("Mettre la carte en opposition. Motif: "+motifOpp.get().getMotifFr());
		cardHistory.setOperation_date(card.getModifDate());
		cardHistory.setEditedBy(user.get().getUserName());
		

		OpposedCard opposedCard = new OpposedCard();
		opposedCard.setCardNumber(card.getCardNum());
		opposedCard.setDateOpposition(card.getModifDate());
		opposedCard.setEditedBy(user.get().getUserName());
		opposedCard.setMotifOpposition(motif);
		opposedCard.setMotifOppositionFr(motifOpp.get().getMotifFr());
		opposedCard.setCardCode(card.getCardCode());
		opposedCard.setBranch(card.getAgencyCode());
	
		if (product.get().getCpCommissionOpposition()>0) {
			
			
	
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat filedateFormat = new SimpleDateFormat("yyMMdd");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
		SimpleDateFormat datePieceComptableFormat = new SimpleDateFormat("ddMMyy");
		Date date=new Date();
		String transactionDate=dateFormat.format(date);
		String transactionTime=timeFormat.format(date);
		
		
		DayOperationFeesCardSequence  seq=dayOperationCardSequenceService.getSequence();
		DayOperationFeesCardSequence  seqPieceComptable=dayOperationCardSequenceService.getSequencePieceComptable();

		
		TvaCommissionFransaBank tvaCommissionFransaBank = tvaRepo.findTva();
		float tvaBase=Float.parseFloat(tvaCommissionFransaBank.getTva())  /100;
		
		float tva=tvaBase+1;
		logger.info("TVA is => {}",tva);
	
		
		
		int settlementAmountHt=Math.round(product.get().getCpCommissionOpposition()/tva);
		logger.info("settlementAmountHt is => {}",settlementAmountHt);
		
		int settlementAmountTva=product.get().getCpCommissionOpposition()-settlementAmountHt;
		
		logger.info("settlementAmountTva is => {}",settlementAmountTva);
		
		int transactionAmount=product.get().getCpCommissionOpposition();
		logger.info("transactionAmount is => {}",transactionAmount);
		
		String montantTrans=String.format("%014d",transactionAmount);
		montantTrans=montantTrans.substring(0,12)+"."+montantTrans.substring(12);
		if (product.get().getLibelle().toLowerCase().contains("visa")) {

			OperationCodeCommision settlc025 = operationCodeInternationalRepository.findByIdentification("C023").get();
			OperationCodeCommision settlc026 = operationCodeInternationalRepository.findByIdentification("C024").get();
	
			
			DayOperationCardFransaBank C025 = new DayOperationCardFransaBank();
			C025.setCodeAgence(card.getAgencyCode());
			C025.setCodeBankAcquereur("035");
			C025.setCodeBank("035");
			C025.setCompteDebit("0"+(account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
			C025.setDateTransaction(transactionDate);
			C025.setHeureTransaction(transactionTime);
			C025.setCompteCredit(settlc025.getCreditAccount());
			C025.setNumCartePorteur(card.getCardNum());
			C025.setNumtransaction(String.format("%012d", seq.getSequence()));
			C025.setIdenfication(settlc025.getIdentification());
			
			

			C025.setMontantSettlement(settlementAmountHt);
			
			

			C025.setMontantTransaction(montantTrans);
			C025.setLibelleCommercant("OPPOSITIONMB00432");
			
			
			C025.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
			C025.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
			C025.setFileDate(filedateFormat.format(date));
			C025.setNumAutorisation(C025.getNumtransaction());
			C025.setNumRefTransaction(C025.getNumtransaction());
			C025.setCardProduct("visa");
			// ecriture tva
			DayOperationCardFransaBank C026 = new DayOperationCardFransaBank();
			C026.setCodeAgence(card.getAgencyCode());
			C026.setCodeBankAcquereur("035");
			C026.setCodeBank("035");
			C026.setCompteDebit("0"+(account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
			C026.setDateTransaction(transactionDate);
			C026.setHeureTransaction(transactionTime);
			C026.setCompteCredit(settlc026.getCreditAccount());
			C026.setNumCartePorteur(card.getCardNum());
			C026.setNumtransaction(	String.format("%012d", seq.getSequence()));
			C026.setIdenfication(settlc026.getIdentification());
			
			
			C026.setMontantSettlement(settlementAmountTva);
			
			
			
			C026.setMontantTransaction(montantTrans);
			C026.setLibelleCommercant("OPPOSITIONMB00432");
			
		
			C026.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
			C026.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
			C026.setFileDate(filedateFormat.format(date));
			C026.setNumAutorisation(C026.getNumtransaction());
			C026.setNumRefTransaction(C026.getNumtransaction());
			
			
			C026.setCardProduct("visa");
			
			
			dayOperations.add(C025);
			dayOperations.add(C026);
		}
		
		else if (product.get().getLibelle().toLowerCase().contains("gold")) {
			
			SettelementFransaBank settlc001 =settlementRepo.findByIdentificationbh("C001");
			SettelementFransaBank settlc002 =settlementRepo.findByIdentificationbh("C002");

			DayOperationCardFransaBank C001 = new DayOperationCardFransaBank();
			C001.setCodeAgence(card.getAgencyCode());
			C001.setCodeBankAcquereur("035");
			C001.setCodeBank("035");
			C001.setCompteDebit("0"+(account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
			C001.setDateTransaction(transactionDate);
			C001.setHeureTransaction(transactionTime);
			C001.setCompteCredit(settlc001.getCreditAccount());
			C001.setNumCartePorteur(card.getCardNum());
			C001.setNumtransaction(String.format("%012d", seq.getSequence()));
			C001.setIdenfication(settlc001.getIdentificationbh());
			
			

			C001.setMontantSettlement(settlementAmountHt);
			
			

			C001.setMontantTransaction(montantTrans);
			C001.setLibelleCommercant("OPPOSITIONMB00432");
			
			
			C001.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
			C001.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
			C001.setFileDate(filedateFormat.format(date));
			C001.setNumAutorisation(C001.getNumtransaction());
			C001.setNumRefTransaction(C001.getNumtransaction());
			C001.setCardProduct("gold");
			// ecriture tva
			DayOperationCardFransaBank C002 = new DayOperationCardFransaBank();
			C002.setCodeAgence(card.getAgencyCode());
			C002.setCodeBankAcquereur("035");
			C002.setCodeBank("035");
			C002.setCompteDebit("0"+(account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
			C002.setDateTransaction(transactionDate);
			C002.setHeureTransaction(transactionTime);
			C002.setCompteCredit(settlc002.getCreditAccount());
			C002.setNumCartePorteur(card.getCardNum());
			C002.setNumtransaction(	String.format("%012d", seq.getSequence()));
			C002.setIdenfication(settlc002.getIdentificationbh());
			
			
			C002.setMontantSettlement(settlementAmountTva);
			
			
			
			C002.setMontantTransaction(montantTrans);
			C002.setLibelleCommercant("OPPOSITIONMB00432");
			
		
			C002.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
			C002.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
			C002.setFileDate(filedateFormat.format(date));
			C002.setNumAutorisation(C002.getNumtransaction());
			C002.setNumRefTransaction(C002.getNumtransaction());
			
			
			C002.setCardProduct("gold");
			
			
			dayOperations.add(C001);
			dayOperations.add(C002);
			
		}else {
			
			SettelementFransaBank settlc003 =settlementRepo.findByIdentificationbh("C003");
			SettelementFransaBank settlc004 =settlementRepo.findByIdentificationbh("C004");

			DayOperationCardFransaBank C003 = new DayOperationCardFransaBank();
			C003.setCodeAgence(card.getAgencyCode());
			C003.setCodeBankAcquereur("035");
			C003.setCodeBank("035");
			C003.setCompteDebit("0"+(account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
			C003.setDateTransaction(transactionDate);
			C003.setHeureTransaction(transactionTime);
			C003.setCompteCredit(settlc003.getCreditAccount());
			C003.setNumCartePorteur(card.getCardNum());
			C003.setNumtransaction(String.format("%012d", seq.getSequence()));
			C003.setIdenfication(settlc003.getIdentificationbh());
			
			

			C003.setMontantSettlement(settlementAmountHt);
			
			

			C003.setMontantTransaction(montantTrans);
			C003.setLibelleCommercant("OPPOSITIONMB00432");
			
			
			C003.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
			C003.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
			C003.setFileDate(filedateFormat.format(date));
			C003.setNumAutorisation(C003.getNumtransaction());
			C003.setNumRefTransaction(C003.getNumtransaction());
			C003.setCardProduct("classic");
			// ecriture tva
			DayOperationCardFransaBank C004 = new DayOperationCardFransaBank();
			C004.setCodeAgence(card.getAgencyCode());
			C004.setCodeBankAcquereur("035");
			C004.setCodeBank("035");
			C004.setCompteDebit("0"+(account.get().getCurrency().equals("012")? account.get().getAccountNum(): account.get().getAccountNumAttached()));
			C004.setDateTransaction(transactionDate);
			C004.setHeureTransaction(transactionTime);
			C004.setCompteCredit(settlc004.getCreditAccount());
			C004.setNumCartePorteur(card.getCardNum());
			C004.setNumtransaction(	String.format("%012d", seq.getSequence()));
			C004.setIdenfication(settlc004.getIdentificationbh());
			
			
			C004.setMontantSettlement(settlementAmountTva);
			
			
			
			C004.setMontantTransaction(montantTrans);
			C004.setLibelleCommercant("OPPOSITIONMB00432");
			
		
			C004.setPieceComptable(String.format("%06d", seqPieceComptable.getSequence()));
			C004.setRefernceLettrage(datePieceComptableFormat.format(date)+card.getCardNum());
			C004.setFileDate(filedateFormat.format(date));
			C004.setNumAutorisation(C004.getNumtransaction());
			C004.setNumRefTransaction(C004.getNumtransaction());
			
			
			C004.setCardProduct("classic");
			
			
			dayOperations.add(C003);
			dayOperations.add(C004);
			
			
			
		}

		
		seq=dayOperationCardSequenceService.incrementSequence(seq);
		seqPieceComptable=dayOperationCardSequenceService.incrementSequencePieceComptable(seqPieceComptable);
		
		
		operationRepo.saveAll(dayOperations);
		}
		cardHistoryRepository.save(cardHistory);
		opposedCardRepository.save(opposedCard);
		cardRepository.save(card);

		return ResponseEntity.ok(card);
	}

    @GetMapping("getAllCard/{userType}")
    public ResponseEntity<List<CardDisplay>> getAllCardByType(@PathVariable(value = "userType") int userType) {

        int f;
        int s;
        int t;
/*
    for (Card c : cardRepository.findAll()
    ) {
      logger.info(c.toString());
    }


*/
        List<CardDisplay> cardDisplays = new ArrayList<>();
        List<Card> all = cardRepository.findAll();
        for (Card c : all
        ) {
            CardDisplay cd = new CardDisplay();
            if (firstRepository.findById(c.getFirtPositionCode()).isPresent()) {
                FirstPosition firstPosition = firstRepository.findById(c.getFirtPositionCode()).get();
                f = firstPosition.getFpscNum();
            } else
                f = 0;


            if (secondRepository.findById(c.getSecondPositionCode()).isPresent()) {
                SecondPosition secondPosition = secondRepository.findById(c.getSecondPositionCode()).get();
                s = secondPosition.getSpscNum();
            } else
                s = 0;
            if (thirdRepository.findById(c.getThirdPositionCode()).isPresent()) {
                ThirdPosition thirdPosition = thirdRepository.findById(c.getThirdPositionCode()).get();
                t = thirdPosition.getTpscNum();
            } else
                t = 0;
            if(c.getProgrameId()==null)
            {
                cd.setProgram(null);
            }
            else {
                if (programReposiroty.findByProgramCode(c.getProgrameId())
                    .isPresent()) {
                    Program program = programReposiroty
                        .findByProgramCode(c.getProgrameId())
                        .get();
                    cd.setProgram(program.getLibelle());

                } else {
                    cd.setProgram(null);
                }
            }
            if(c.getProductCode()==null)
            {
                cd.setProductCode(null);
            }
            else {
                if (productRepositpry.findByProductCode(c.getProductCode()).isPresent()
                   ) {
                    Product product = productRepositpry.findByProductCode(c.getProductCode()).get();
                    cd.setProductCode(product.getLibelle());

                } else {
                    cd.setProductCode(null);

                }
            }
            Account account = accountRepository.findByAccountCode(c.getAccCode()).get();
            CardStatus cardStatus = cardStatusRepository.findById(c.getCardStatusCode()).get();
            cd.setCardNum(c.getCardNum());
            cd.setAccCode(account.getAccountName());
            cd.setAtc(c.getAtc());
            cd.setCardCode(c.getCardCode());
            cd.setCardStatusCode(cardStatus.getStatus());
            cd.setCurrencyCode(c.getCurrencyCode());
            cd.setExpiryDate(c.getExpiryDate());
            cd.setFirtPositionCode(String.valueOf(f) + String.valueOf(s) + String.valueOf(t));

            cd.setGlobalRiskCode(c.getGlobalRiskCode());
            cd.setPreDate(c.getPreDate());
            cd.setRev_program(c.getRevProgram());
            cd.setRevProductCode(String.valueOf(c.getRevProductCode()));
            cd.setStartDate(c.getStartDate());
            cardDisplays.add(cd);
            System.out.println(cd.toString());


        }
        System.out.println(all.size());

        return ResponseEntity.ok().body(cardDisplays);
    }

	@GetMapping("getAllDemandeType")
	public List<DemandeType> getAllDemandeType() {
		return demandeTypeRepository.findAll();
	}
	
    @GetMapping("getAllCard")
    public ResponseEntity<List<CardDisplay>> getAllCard() {

        int f;
        int s;
        int t;
/*
    for (Card c : cardRepository.findAll()
    ) {
      logger.info(c.toString());
    }


*/
      String name = SecurityContextHolder.getContext().getAuthentication().getName();
  	  User user = userRepository.findByUserNameOrUserEmail(name, name).get();
  	  List<Account> accounts=new ArrayList<Account>();
  	
        List<CardDisplay> cardDisplays = new ArrayList<>();
        //List<Card> all = cardRepository.findAll();
        List<Card> all = new ArrayList<Card>();
    	  if(user.getUserType()==1) {
    		  accounts=accountRepository.findAllByAgence(user.getIdAgence());
    	      for(Account acc:accounts) {
    	    	all.addAll(cardRepository.findByAccCode(acc.getAccountCode()));  
    	      }
    	  }
    	  if(user.getUserType()==3) {
    		  accounts=accountRepository.findAllByZone(user.getIdAgence());
    		  for(Account acc:accounts) {
      	    	all.addAll(cardRepository.findByAccCode(acc.getAccountCode()));  
      	      }
    	  }
    	  if(user.getUserType()==4) {
    		  accounts=accountRepository.findAllByRegion(user.getIdAgence());
    		  for(Account acc:accounts) {
      	    	all.addAll(cardRepository.findByAccCode(acc.getAccountCode()));  
      	      }
    	  }
    	  if(user.getUserType()==5) {
    		  all=cardRepository.findAll();
    	  }
        for (Card c : all
        ) {
            CardDisplay cd = new CardDisplay();
            if (firstRepository.findById(c.getFirtPositionCode()).isPresent()) {
                FirstPosition firstPosition = firstRepository.findById(c.getFirtPositionCode()).get();
                f = firstPosition.getFpscNum();
            } else
                f = 0;


            if (secondRepository.findById(c.getSecondPositionCode()).isPresent()) {
                SecondPosition secondPosition = secondRepository.findById(c.getSecondPositionCode()).get();
                s = secondPosition.getSpscNum();
            } else
                s = 0;
            if (thirdRepository.findById(c.getThirdPositionCode()).isPresent()) {
                ThirdPosition thirdPosition = thirdRepository.findById(c.getThirdPositionCode()).get();
                t = thirdPosition.getTpscNum();
            } else
                t = 0;
            if(c.getProgrameId()==null)
            {
                cd.setProgram(null);
            }
            else {
                if (programReposiroty.findByProgramCode(c.getProgrameId())
                    .isPresent()) {
                    Program program = programReposiroty
                        .findByProgramCode(c.getProgrameId())
                        .get();
                    cd.setProgram(program.getLibelle());

                } else {
                    cd.setProgram(null);
                }
            }
            if(c.getProductCode()==null)
            {
                cd.setProductCode(null);
            }
            else {
                if (productRepositpry.findByProductCode(c.getProductCode()).isPresent()
                   ) {
                    Product product = productRepositpry.findByProductCode(c.getProductCode()).get();
                    cd.setProductCode(product.getLibelle());

                } else {
                    cd.setProductCode(null);

                }
            }
            Account account = accountRepository.findByAccountCode(c.getAccCode()).get();
            CardStatus cardStatus = cardStatusRepository.findById(c.getCardStatusCode()).get();
            cd.setCardNum(c.getCardNum());
            cd.setAccCode(account.getAccountName());
            cd.setAtc(c.getAtc());
            cd.setCardCode(c.getCardCode());
            cd.setCardStatusCode(cardStatus.getStatus());
            cd.setCurrencyCode(c.getCurrencyCode());
            cd.setExpiryDate(c.getExpiryDate());
            cd.setFirtPositionCode(String.valueOf(f) + String.valueOf(s) + String.valueOf(t));

            cd.setGlobalRiskCode(c.getGlobalRiskCode());
            cd.setPreDate(c.getPreDate());
            cd.setRev_program(c.getRevProgram());
            cd.setRevProductCode(String.valueOf(c.getRevProductCode()));
            cd.setStartDate(c.getStartDate());
            cd.setStatusCode(c.getCardStatusCode());
            cardDisplays.add(cd);
            System.out.println(cd.toString());


        }
        System.out.println(all.size());

        return ResponseEntity.ok().body(cardDisplays);
    }
    
	@PostMapping("/opposedCards")
	public Page<OpposedCard> getOpposedCards(@RequestBody OpposedCardsFilter opposedCardsFilter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
		    @RequestParam(name = "sortOn") String sortOn,
		    @RequestParam(name = "dir") String dir) {
		
		Order order=null;
	  	if (dir.equals("desc"))
	  		order = new Order(Sort.Direction.DESC, sortOn);
	  	else 
	  		order = new Order(Sort.Direction.ASC, sortOn);
	  	
		if (opposedCardsFilter.getStartDate()!="" && opposedCardsFilter.getEndDate()!="") {
			return opposedCardRepository.getOpposedCardsWithInterval(
					PageRequest.of(page, size, Sort.by(order)),
					opposedCardsFilter.getPan(),
					opposedCardsFilter.getMotif(),
					opposedCardsFilter.getStartDate(),
					opposedCardsFilter.getEndDate(),opposedCardsFilter.getAgencyCode());
			
		}else {
			return opposedCardRepository.getOpposedCards(
					PageRequest.of(page, size, Sort.by(order)),
					opposedCardsFilter.getPan(),
					opposedCardsFilter.getMotif(),
					opposedCardsFilter.getStartDate(),opposedCardsFilter.getAgencyCode());
					

		}
		
	}

	 @PostMapping("getAllCardFiltred")
	    public ResponseEntity<Page<Object[]>> getAllCardFiltred(@RequestBody CardReportingRequest cardReportingRequest,
	     @RequestParam(name = "page", defaultValue = "0") int page,
	     @RequestParam(name = "size", defaultValue = "10") int size,
		    @RequestParam(name = "sortOn") String sortOn,
		    @RequestParam(name = "dir") String dir) {


	        String name = SecurityContextHolder.getContext().getAuthentication().getName();
	        User user = userRepository.findByUserNameOrUserEmail(name, name).get();
	        //List<Account> accounts=new ArrayList<Account>();
	        Order order=null;
		  	if (dir.equals("desc"))
		  		order = new Order(Sort.Direction.DESC, sortOn);
		  	else 
		  		order = new Order(Sort.Direction.ASC, sortOn);
	        Page<Object[]> all = null;
	        if (user.getUserType()!=5) {
	        	List<String> agencyCode=new ArrayList<String>();
	        	   if(user.getUserType()==1) {
	        		   
	                 //  accounts=accountRepository.findAllByAgence(user.getIdAgence());
	                   Optional<AgenceAdministration> agence=  agenceAdministrationRepository.findByCodeAgence(user.getIdAgence());
	                   if (agence.isPresent()) {
	                	   agencyCode.add(agence.get().getInitial());

	                   }
	                   
	                   
	        	   }else if (user.getUserType()==3) {
	        		   
	        		   Optional<Zone> zone=zoneRepository.findByCodeZone(user.getIdAgence());
	        		   if (zone.isPresent()) {
	        			   
	                       List<AgenceAdministration> agencies=  agenceAdministrationRepository.findByCodeZone(zone.get().getCodeZone());
	                       for (AgenceAdministration agency : agencies) {
	                    	   agencyCode.add(agency.getInitial());
	                       }
	   
	        		   }
	        		   //accounts=accountRepository.findAllByZone(user.getIdAgence()); 
	        	   }
	        	   else if (user.getUserType()==4) {
	        		   Optional<Region> region=regionRepository.findByCodeRegion(user.getIdAgence());
	        		   if (region.isPresent()) {
	        			   List<Zone> zones=zoneRepository.findByCodeRegion(region.get().getCodeRegion());
	                       for (Zone zone : zones) {
	                           List<AgenceAdministration> agencies=  agenceAdministrationRepository.findByCodeZone(zone.getCodeZone());
	                           for (AgenceAdministration agency : agencies) {
	                        	   agencyCode.add(agency.getInitial());
	                           }
	                       }

	        		   }
	        		   
	        		   
	        	   }
	        	   
	        	   if (!cardReportingRequest.getStartDate().equals("")
	      					&& !cardReportingRequest.getEndDate().equals("")) {
	      				all = cardRepository.reportinWithInervalWithAgenciesList(PageRequest.of(page, size, Sort.by(order)),
	      						cardReportingRequest.getName(), cardReportingRequest.getPan(),
	      						cardReportingRequest.getCardStatus(), cardReportingRequest.getAddress(),
	      						agencyCode   , cardReportingRequest.getAccountNum(),
	      						cardReportingRequest.getStartDate(), cardReportingRequest.getExpiryDate(),
	      						cardReportingRequest.getModifDate(), cardReportingRequest.getEndDate(),cardReportingRequest.getRadical(),
	      	            		cardReportingRequest.getIsFromMobile()
	      				);

	      			} else {

	      				all = cardRepository.reportingWithAgenciesList(PageRequest.of(page, size, Sort.by(order)),
	      						cardReportingRequest.getName(), cardReportingRequest.getPan(),
	      						cardReportingRequest.getCardStatus(), cardReportingRequest.getAddress(),
	      						agencyCode, cardReportingRequest.getAccountNum(),
	      						cardReportingRequest.getStartDate(), cardReportingRequest.getExpiryDate(),
	      						cardReportingRequest.getModifDate(),cardReportingRequest.getRadical(),
	      	            		cardReportingRequest.getIsFromMobile());
	      			}

	        }else {
				if (!cardReportingRequest.getStartDate().equals("")
						&& !cardReportingRequest.getEndDate().equals("")) {
					all = cardRepository.reportinWithInerval(PageRequest.of(page, size, Sort.by(order)),
							cardReportingRequest.getName(), cardReportingRequest.getPan(),
							cardReportingRequest.getCardStatus(), cardReportingRequest.getAddress(),
							cardReportingRequest.getAgencyCode(), cardReportingRequest.getAccountNum(),
							cardReportingRequest.getStartDate(), cardReportingRequest.getExpiryDate(),
							cardReportingRequest.getModifDate(), cardReportingRequest.getEndDate(),cardReportingRequest.getRadical(),
		            		cardReportingRequest.getIsFromMobile()
					);

				} else {

					all = cardRepository.reporting(PageRequest.of(page, size, Sort.by(order)),
							cardReportingRequest.getName(), cardReportingRequest.getPan(),
							cardReportingRequest.getCardStatus(), cardReportingRequest.getAddress(),
							cardReportingRequest.getAgencyCode(), cardReportingRequest.getAccountNum(),
							cardReportingRequest.getStartDate(), cardReportingRequest.getExpiryDate(),
							cardReportingRequest.getModifDate(),cardReportingRequest.getRadical(),cardReportingRequest.getIsFromMobile());
				}
	        	
	        }
	        return ResponseEntity.ok().body(all);
	    }


    @GetMapping("/card/{id}")
    public ResponseEntity<Card> getCard(@PathVariable(value = "id") String atmHardFitnessId)
            throws ResourceNotFoundException {
        Card product = cardRepository.findByCardCode(Integer.parseInt(atmHardFitnessId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        " Card not found for this id :: " + atmHardFitnessId));
        logger.info(product.toString());
        return ResponseEntity.ok().body(product);
    }

    @PutMapping("changeControlList/{id}/{idProgram}")
    public ResponseEntity<Card> changeControlList(@PathVariable(value = "id") String cardCode, @Valid
    @RequestBody Set<String> controlLists, @PathVariable(value = "idProgram") String idProgram)
            throws ResourceNotFoundException {
        Card card = cardRepository.findByCardCode(Integer.parseInt(cardCode)).orElseThrow(
                () -> new ResourceNotFoundException("Card not found for this id :: " + cardCode));
        Product product = productRepositpry.findByProductCode(card.getProductCode()).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Product not found for this id :: " + card.getProductCode()));
        Program program = programReposiroty.findByProgramCode(Integer.parseInt(idProgram))
                .orElseThrow(
                        () -> new ResourceNotFoundException("Program not found for this id :: " + idProgram));
        Set<ControlList> controlLists1 = new HashSet<>();
        for (String id : controlLists

        ) {
            ControlList serviceValues = controlListRepository.findByControlNum(id)
                    .orElseThrow(
                            () -> new ResourceNotFoundException(
                                    " EmvServiceValues not found for this id :: " + id));
            controlLists1.add(serviceValues);

        }
        program.setControlLists(controlLists1);
        programReposiroty.save(program);
        product.getPrograms().add(program);
        productRepositpry.save(product);
        cardRepository.save(card);
        logger.info(product.toString());
        logger.info(program.toString());
      //  logger.info(card.toString());
        return ResponseEntity.ok().body(card);
    }
    
    @GetMapping("getProductCeilings/{id}")
    public ProductCeilings getProductCeilings(@PathVariable(value = "id") Integer idProduct) {
    	Optional<Product> product=productRepositpry.findById(idProduct);
    	ProductCeilings productCeilings=new ProductCeilings();
    	if (product.isPresent()) {
    		Set<Program> programs=	product.get().getPrograms();
    		   for (Program prog : programs) {
    			 //  Program 		programReposiroty.findByProgramCode(porg.getProgramCode());
    			   
    			   productCeilings.setCprRiskAmount(prog.getCprRiskAmount()); 
    			   productCeilings.setCprRiskAmountMax(prog.getCprRiskAmountMax());
    			   productCeilings.setCprRiskAmountMin(prog.getCprRiskAmountMin());
    			   productCeilings.setGlNumber(prog.getGlNumber());
    			   
    			   Optional<Transaction> purchase = allocatedTransactionRepo.findByProgramIdAndTransactionType(prog.getProgramCode(),"00");

    			   Optional<Transaction> withdrawal = allocatedTransactionRepo.findByProgramIdAndTransactionType(prog.getProgramCode(),"01");
    			   Optional<Transaction> ecommerce = allocatedTransactionRepo.findByProgramIdAndTransactionType(prog.getProgramCode(),"50");

    			   if (purchase.isPresent())
    				   productCeilings.setCprPurchaseMax(Integer.parseInt(purchase.get().getPlafond()));
    			   else productCeilings.setCprPurchaseMax(-1);
    			   
    			   
    			   if ( ecommerce.isPresent())
    				   productCeilings.setCprEcommerceMax(Integer.parseInt(ecommerce.get().getPlafond()));
    			   else productCeilings.setCprEcommerceMax(-1);
    			   
    			   if (withdrawal.isPresent())
    				   productCeilings.setCprWithdrawalMax(Integer.parseInt(withdrawal.get().getPlafond()));
    			   else productCeilings.setCprWithdrawalMax(-1);

    			   break;
    		   }
    		   return productCeilings;
    		
    	}
    	return null;
    	
    }

    @PostMapping("addProduct")
    public ResponseEntity<String> addProduct(@Valid @RequestBody ProductRequest productRequest)
            throws ResourceNotFoundException {
        logger.info(productRequest.toString());
        Product product = new Product(
        		productRequest.libelle,
        		productRequest.cpCommissionNew,
                productRequest.cpCommissionRemplacement,
                productRequest.cpCommissionPin,
                productRequest.cpCommissionAnniversary,
                productRequest.cpCommissionOpposition,
                productRequest.getCpCommissionModifPlafond(),
                productRequest.getCpCancelation(),
                productRequest.getCpCapturedCard(),
                productRequest.isAutoValidation()?1:0,
                productRequest.getCpCommissionCreation(),
                productRequest.getCpCommissionAnniversaryAndCreation(),
                productRequest.getCpTypeCommissionAnniversary()		
                );
        Set<String> controList = productRequest.getPrograms();

        Set<Program> programs = new HashSet<>();
        Set<EligibleAccount> eligibleAccounts = new HashSet<>();
        for (String id : controList

        ) {
            Program controlList = programReposiroty.findByProgramCode(Integer.parseInt(id)).orElseThrow(
                    () -> new ResourceNotFoundException(" Contrl List not found for this id :: " + id));
            programs.add(controlList);

        }

        product.setPrograms(programs);
        
        for (Integer id : productRequest.getEligibleAccounts()

                ) {
        	EligibleAccount eligibleAccount=eligibleAccountRepository.findById(id).get();

        	eligibleAccounts.add(eligibleAccount);
                }
        product.setEligibleAccounts(eligibleAccounts);
        productRepositpry.save(product);
        logger.info(product.toString());
        return ResponseEntity.ok().body("Product added successfully!");

    }

    @PostMapping("addProductPrep")
    public ResponseEntity<String> addProductPrep(@Valid @RequestBody ProductPrepayer productRequest)
            throws ResourceNotFoundException {
        logger.info(productRequest.toString());
        prepRepository.save(productRequest);

        return ResponseEntity.ok().body("Product added successfully!");

    }


    @GetMapping("getProgramByCard/{id}")
    public Set<Program> getProgramByCard(@PathVariable(value = "id") String cardCode)
            throws ResourceNotFoundException {
        Card card = cardRepository.findByCardCode(Integer.parseInt(cardCode)).orElseThrow(
                () -> new ResourceNotFoundException("Card not found for this id :: " + cardCode));
       // logger.info(card.toString());
        Product product = productRepositpry.findByProductCode(card.getProductCode()).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Product not found for this id :: " + card.getProductCode()));
        logger.info(product.toString());
        return product.getPrograms();
    }


    @GetMapping("getControlListByProgram/{id}")
    public Set<ControlList> getControlListByProgram(@PathVariable(value = "id") String cardCode)
            throws ResourceNotFoundException {

        Program product = programReposiroty.findByProgramCode(Integer.parseInt(cardCode)).orElseThrow(
                () -> new ResourceNotFoundException("Program not found for this id :: " + cardCode));

        return product.getControlLists();
    }


    /*@GetMapping("getRiskPeriodicityProgram/{id}")
    public List<RiskPeriodicity> getListPeriodByProgram(@PathVariable(value = "id") String programId)
            throws ResourceNotFoundException {
        List<RiskPeriodicity> Listrisq = riskPeriodicityRepository
                .findByProgCode(Integer.parseInt(programId));
        return Listrisq;

    }*/

/*
    @PostMapping("addRiskPeriodicity")
    public RiskPeriodicity addRiskPeriodicity(
            @Valid @RequestBody PeriodicityRequest riskPeriodicity) throws ResourceNotFoundException {
        logger.info(riskPeriodicity.toString());
        RiskPeriodicity risqSaved = new RiskPeriodicity(
                riskPeriodicity.typeCode, riskPeriodicity.riskPeriodicityStartDate,
                riskPeriodicity.getRiskPeriodicityEndDate, riskPeriodicity.progCode,
                riskPeriodicity.AmountMaxPeriod, riskPeriodicity.AmountMinPeriod,
                riskPeriodicity.AmountLimitPeriod, riskPeriodicity.NumberLimitPeriod);
        Set<String> transactions = riskPeriodicity.getEmvServiceValues();
        Set<EmvServiceValues> emvServiceValues = new HashSet<>();
        for (String id : transactions

        ) {
            EmvServiceValues serviceValues = emvServiceValuesRepository.findByCodeTransaction(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            " EmvServiceValues not found for this id :: " + id));

            emvServiceValues.add(serviceValues);

        }

        risqSaved.setEmvServiceValues(emvServiceValues);
        logger.info(risqSaved.toString());
        return riskPeriodicityRepository.save(risqSaved);

    }
*/
    @GetMapping("getAllCardStatus")
    public List<CardStatus> getAllCardStatus() {
        return cardStatusRepository.findAll();
    }
    @GetMapping("getAllStatusReq")
    public List<RequestCardStatus> getAllStatusReq() {
        return  RequestStatusRep.findAll();
    }

    @PostMapping("addCommission")
    public ResponseEntity<String> addComission(@Valid @RequestBody AddComission addCommission)
            throws ResourceNotFoundException {
        logger.info(addCommission.toString());
        Commission commission = new Commission(addCommission.getTransactionSource(),
                addCommission.getFixedCommission(),
                addCommission.getVariableComission(), addCommission.getLibelle(),
                addCommission.getCurrency());
        logger.info(commission.toString());
        Set<String> transactions = addCommission.getEmvServiceValues();
        Set<EmvServiceValues> emvServiceValues = new HashSet<>();
        for (String id : transactions

        ) {
            EmvServiceValues serviceValues = emvServiceValuesRepository.findByCodeTransaction(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            " EmvServiceValues not found for this id :: " + id));
            emvServiceValues.add(serviceValues);

        }
        commission.setEmvServiceValues(emvServiceValues);
        
        if (commission.getCurrency().length()<3) {
        	commission.setCurrency("0"+commission.getCurrency());	
        }
        commissionRepository.save(commission);

        return ResponseEntity.ok().body("Commission added successfully!");

    }

  /*@GetMapping("getAllCommmission")
  public List<DisplayCommission> getAllCommmission() {

    List<Commission> commissionList = commissionRepository.findAll();
    List<DisplayCommission> displayCommissions = new ArrayList<>();
    for (Commission c : commissionList
    ) {
      DisplayCommission displayCommission = new DisplayCommission();
      Routage routage = routageRepository.findByRoutageCode(c.getTransactionSource());
      EmvServiceValues serviceValues = emvServiceValuesRepository
          .findByCodeTransaction(c.getTransactionType()).get();
      displayCommission.setFixedCommission(c.getFixedCommission());
      displayCommission.setVariableComission(c.getVariableComission());
      displayCommission.setTransactionSource(routage.getLibelle());
      displayCommission.setTransactionType(serviceValues.getLibelle());

      displayCommissions.add(displayCommission);
    }

    return displayCommissions;
  }*/

    @DeleteMapping("deleteCommission/{id}")
    public ResponseEntity<String> deleteCommission(@PathVariable(value = "id") Integer type) {
        Commission commission = commissionRepository.findById(type).get();
        commissionRepository.delete(commission);

        return ResponseEntity.ok().body("Commission deleted  successfully!");

    }

    //delete product
    @DeleteMapping("deleteProduct/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable(value = "id") Integer type) {
        Product product = productRepositpry.findById(type).get();
        productRepositpry.delete(product);

        return ResponseEntity.ok().body("Product deleted  successfully!");

    }

    @PutMapping("updateCommission/{id}")
    public ResponseEntity<Commission> getCommission(@PathVariable(value = "id") Integer type,
                                                    @RequestBody AddCommission addCommission) {
        logger.info(addCommission.toString());
        Commission bin = commissionRepository.findById(type).get();
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.map(addCommission, bin);
        if (bin.getCurrency().length()<3)
        	bin.setCurrency("0"+bin.getCurrency());
        final Commission updatedEmployee = commissionRepository.save(bin);
        logger.info(updatedEmployee.toString());
        return ResponseEntity.ok(updatedEmployee);

    }
    
    
    @GetMapping("getListBins_Products/{produit}")
    public Set<BinOnUs> getBinfromproduct(@PathVariable(value ="produit") int produit){//libelle
    	System.out.println("produit is: "+produit);
    	Set<BinOnUs> bins=new HashSet<BinOnUs>();
    Optional<Product> p = productRepositpry.findByProductCode(produit);
    
    Set<Program> programs=p.get().getPrograms();
    System.out.println("programs: "+programs);
    for (Program prog : programs) {
    	System.out.println(prog.getBinOnUsCode());
bins.add(binOnUsRepository.findByBinOnUsCode(prog.getBinOnUsCode())); }
 return bins; }

    //update product
    @PutMapping("updateProduct/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable(value = "id") String productId,
                                                 @Valid @RequestBody ProductRequest productRequest)
            throws NumberFormatException, ResourceNotFoundException {
        logger.info(productRequest.toString());
        Product product = productRepositpry.findByProductCode(Integer.parseInt(productId)).get();
        Set<String> controList = productRequest.getPrograms();

        Set<Program> programs = new HashSet<>();
        Set<EligibleAccount> eligibleAccounts = new HashSet<>();

        for (String id : controList

        ) {
            Program controlList = programReposiroty.findByProgramCode(Integer.parseInt(id)).orElseThrow(
                    () -> new ResourceNotFoundException(" Contrl List not found for this id :: " + id));
            programs.add(controlList);

        }

        product.setPrograms(programs);
        for (Integer id : productRequest.getEligibleAccounts()) {
        	EligibleAccount eligibleAccount=eligibleAccountRepository.findById(id).get();

        	eligibleAccounts.add(eligibleAccount);
                }
     
        modelMapper.map(productRequest, product);
        product.setAutoValidation(productRequest.isAutoValidation()?1:0);
        product.setEligibleAccounts(eligibleAccounts);
        final Product updatedProduct = productRepositpry.save(product);
        logger.info(updatedProduct.toString());
        return ResponseEntity.ok(updatedProduct);


    }
	
    @PutMapping("processComplaint/{id}/{comment}")
    public void processComplaint(
    		@PathVariable(value = "id") Integer complaintCode,
    		@PathVariable(value = "comment") String comment
    		) {
    	Optional<CardComplaint> complaint =cardComplaintRepository.findById(complaintCode);
    	if (complaint.isPresent()) {
    		complaint.get().setBoComment(comment);
    		complaint.get().setStatus(0);
    		complaint.get().setResponseDate(new Date());
    		cardComplaintRepository.save(complaint.get());
    		
    	}
    }
    
    @GetMapping("getCardComplaints/{id}")
    public List<CardComplaint> getCardComplaints(
    		@PathVariable(value = "id") Integer id
    	
    		) {
    	
    	  return cardComplaintRepository.findByCardCode(id);
    	
    	
    }
    	
    @PostMapping("addCardsComplaint")
    public void addCardComplaint(@RequestBody CardComplaintRequest request){
    	
    	String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		if (!user.isPresent()) {
			throw new RuntimeException("Error saving status !");
		}
		Optional<Card> card=cardRepository.findByCardCode(request.getCardCode());
    	
    	CardComplaint complaint=new CardComplaint();
    	
    	complaint.setCardCode(request.getCardCode());
    	complaint.setComplaintDate(new Date());
    	complaint.setComment(request.getComment());
    	complaint.setIncident(request.getIncident());
    	complaint.setStatus(1);
    	complaint.setAddedBy(user.get().getUserName());
    	complaint.setBranch(request.getBranch());
    	complaint.setPan(card.get().getCardNum());
    	cardComplaintRepository.save(complaint);
    	
    	//return cardRepository.findCardsForComplaints(filter.getPan(), filter.getAgence(), filter.getAccountNumber());
    }
    
    @PostMapping("getEpaymentInfo")
    public Page<EpaymentInfo> getEpaymentInfo(
    		@RequestBody CardComplaintsFilter filter,
    		@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size
    		){
//    	Order order=null;
//	  	if (dir.equals("desc"))
//	  		order = new Order(Sort.Direction.DESC, sortOn);
//	  	else 
//	  		order = new Order(Sort.Direction.ASC, sortOn);
	  	return epaymentInfoRepository.getInfos(PageRequest.of(page, size), filter.getAgence(), filter.getPan());
	  //	return cardComplaintRepository.getComplaints(PageRequest.of(page, size, Sort.by(order)), filter.getPan(), filter.getAgence());
    }
    
    
    @PutMapping("updateEpaymentInfo/{code}/{phoneNumber}")
    public EpaymentInfo updateEpaymentInfo(
    		@PathVariable(value = "code") Integer code,
    		@PathVariable(value = "phoneNumber") String phoneNumber) {
    	
    	Optional<EpaymentInfo> info =epaymentInfoRepository.findById(code);
    	if (info.isPresent()) {
    		 String name = SecurityContextHolder.getContext().getAuthentication().getName();
 			Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
 			if (!user.isPresent()) {
 				throw new RuntimeException("Error saving status !");
 			}
 			
            info.get().setModifDate(new Date());
            info.get().setEditedBy(user.get().getUserName());
            info.get().setStatus("M");
            info.get().setStatusFile(1);
            info.get().setPhoneNumber(phoneNumber);

            return epaymentInfoRepository.save(info.get());

    	}
    	return null;
    	
    	
    	
    }
    
    
    @PostMapping("getComplaints")
    public Page<CardComplaint> getComplaints(
    		@RequestBody CardComplaintsFilter filter,
    		@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
		    @RequestParam(name = "sortOn") String sortOn,
		    @RequestParam(name = "dir") String dir
    		
    		){
    	Order order=null;
	  	if (dir.equals("desc"))
	  		order = new Order(Sort.Direction.DESC, sortOn);
	  	else 
	  		order = new Order(Sort.Direction.ASC, sortOn);
	  	
	  	return cardComplaintRepository.getComplaints(PageRequest.of(page, size, Sort.by(order)), filter.getPan(), filter.getAgence());
    }
    @PostMapping("getCardsForComplaints")
    public List<CardComplaintDisplay> getCardsForComplaints(@RequestBody CardComplaintsFilter filter){
    	List<CardComplaintDisplay> cards=new ArrayList<CardComplaintDisplay>();
    	Boolean test=false;
    	  for (Card card : cardRepository.findCardsForComplaints(filter.getPan(), filter.getAgence(), filter.getAccountNumber())) {
    		  CardComplaintDisplay display=new CardComplaintDisplay();
    		  display.setCard(card);
    		  List<CardComplaint> complaints=cardComplaintRepository.findByCardCode(card.getCardCode());
    		  for (CardComplaint comp : complaints) {
    			  if (comp.getStatus()==1) {
    				  test=true;
    				  display.setOpenedComplaint(comp);
    				  break;
    			  }
    		  }
    		  
    		  display.setComplaints(complaints);
    		  display.setOpenedComplaint(test);
    		  cards.add(display);
    	  }
    	return cards;
    }

    @PostMapping("getAllActiveCards")
    public ResponseEntity<Page<CardDisplay>> getAllActiveCards(@RequestBody CardFilterDisplay cardFilterDisplay,
     @RequestParam(name = "page", defaultValue = "0") int page,
     @RequestParam(name = "size", defaultValue = "10") int size
    ) {

        int f;
        int s;
        int t;

        String pan = "";
        int status = 0;

        if(cardFilterDisplay.getPan() !=null){
            pan = cardFilterDisplay.getPan().trim();
        }
      
            status = 2;
        
/*
    for (Card c : cardRepository.findAll()
    ) {
      logger.info(c.toString());
    }


*/
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNameOrUserEmail(name, name).get();
        List<Account> accounts=new ArrayList<Account>();

        List<CardDisplay> cardDisplays = new ArrayList<CardDisplay>();
        //List<Card> all = cardRepository.findAll();
        Page<Card> all = null;
        Page<CardDisplay> page2 = null;
//        if(user.getUserType()==1) {
//            accounts=accountRepository.findAllByAgence(user.getIdAgence());
//            for (Account acc : accounts) {
//                if (cardFilterDisplay.getStatus() == 0) {
//                    all = (cardRepository.findByAccCodeAndPanAndwithoutStatus(PageRequest.of(page, size),acc.getAccountCode(), pan));
//                } else {
//                    all = (cardRepository.findByAccCodeAndPanAndStatus(PageRequest.of(page, size),acc.getAccountCode(), status, pan));
//                }
//            }
//        }
//        if(user.getUserType()==3) {
//            accounts=accountRepository.findAllByZone(user.getIdAgence());
//            for(Account acc:accounts) {
//                if (cardFilterDisplay.getStatus() == 0) {
//                    all = (cardRepository.findByAccCodeAndPanAndwithoutStatus(PageRequest.of(page, size),acc.getAccountCode(), pan));
//                } else {
//                    all = (cardRepository.findByAccCodeAndPanAndStatus(PageRequest.of(page, size),acc.getAccountCode(), status, pan));
//                }
//            }
//        }
//        if(user.getUserType()==4) {
//            accounts=accountRepository.findAllByRegion(user.getIdAgence());
//            for(Account acc:accounts) {
//                if (cardFilterDisplay.getStatus() == 0) {
//                    all = (cardRepository.findByAccCodeAndPanAndwithoutStatus(PageRequest.of(page, size),acc.getAccountCode(), pan));
//                } else {
//                    all = (cardRepository.findByAccCodeAndPanAndStatus(PageRequest.of(page, size),acc.getAccountCode(), status, pan));
//                }            }
//        }
//        if(user.getUserType()==5) {
//            if (cardFilterDisplay.getStatus() == 0) {
//                all = cardRepository.findByPanAndwithoutStatus(PageRequest.of(page, size),pan);
//            } else {
//                all=cardRepository.findByPanAndStatus(PageRequest.of(page, size),status, pan);
//            }
//        }
//        
        
        
        if (cardFilterDisplay.getStatus() == 0) {
            all = cardRepository.findByPanAndwithoutStatus(PageRequest.of(page, size),pan,cardFilterDisplay.getAgencyCode());
        } else {
            all=cardRepository.findByPanAndStatus(PageRequest.of(page, size),status, pan,cardFilterDisplay.getAgencyCode());
        }

        for (Card c : all
        ) {
        	if (!actionCardRepository.findByCardCode(c.getCardCode()).isPresent()) {
        		
        	
            CardDisplay cd = new CardDisplay();
            if (firstRepository.findById(c.getFirtPositionCode()).isPresent()) {
                FirstPosition firstPosition = firstRepository.findById(c.getFirtPositionCode()).get();
                f = firstPosition.getFpscNum();
            } else
                f = 0;


            if (secondRepository.findById(c.getSecondPositionCode()).isPresent()) {
                SecondPosition secondPosition = secondRepository.findById(c.getSecondPositionCode()).get();
                s = secondPosition.getSpscNum();
            } else
                s = 0;
            if (thirdRepository.findById(c.getThirdPositionCode()).isPresent()) {
                ThirdPosition thirdPosition = thirdRepository.findById(c.getThirdPositionCode()).get();
                t = thirdPosition.getTpscNum();
            } else
                t = 0;
            if(c.getProgrameId()==null)
            {
                cd.setProgram(null);
            }
            else {
                if (programReposiroty.findByProgramCode(c.getProgrameId())
                        .isPresent()) {
                    Program program = programReposiroty
                            .findByProgramCode(c.getProgrameId())
                            .get();
                    cd.setProgram(program.getLibelle());

                } else {
                    cd.setProgram(null);
                }
            }
            if(c.getProductCode()==null)
            {
                cd.setProductCode(null);
            }
            else {
                if (productRepositpry.findByProductCode(c.getProductCode()).isPresent()
                ) {
                    Product product = productRepositpry.findByProductCode(c.getProductCode()).get();
                    cd.setProductCode(product.getLibelle());

                } else {
                    cd.setProductCode(null);

                }
            }
            Account account = accountRepository.findByAccountCode(c.getAccCode()).get();
            CardStatus cardStatus = cardStatusRepository.findById(c.getCardStatusCode()).get();
            cd.setCardNum(c.getCardNum());
            cd.setAccCode(account.getAccountName());
            cd.setAtc(c.getAtc());
            cd.setCardCode(c.getCardCode());
            cd.setCardStatusCode(cardStatus.getStatus());
            cd.setCurrencyCode(c.getCurrencyCode());
            cd.setExpiryDate(c.getExpiryDate());
            cd.setFirtPositionCode(String.valueOf(f) + String.valueOf(s) + String.valueOf(t));
            cd.setGlobalRiskCode(c.getGlobalRiskCode());
            cd.setPreDate(c.getPreDate());
            cd.setRev_program(c.getRevProgram());
            cd.setRevProductCode(String.valueOf(c.getRevProductCode()));
            cd.setStartDate(c.getStartDate());
            cd.setStatusCode(c.getCardStatusCode());
            cardDisplays.add(cd);
            System.out.println(cd.toString());
//            page2 = new PageImpl<>(cardDisplays.subList(page, size), PageRequest.of(page, size), cardDisplays.size());
           

        	}}
        page2 = new PageImpl<>(cardDisplays,PageRequest.of(page, size), all.getTotalElements());
        System.out.println(all.getSize());

        return ResponseEntity.ok().body(page2);
    }
    //getPeriodicity
   /* @GetMapping("getPeriodicity/{id}")
    public RiskPeriodicity getPeriodicityById(@PathVariable(value = "id") int id) {
        return riskPeriodicityRepository.findByRiskPeriodicityCode(id);
    }
*/
    //update program
    /*@PutMapping("updatePeriod/{id}")
    public ResponseEntity<RiskPeriodicity> updatePeriod(@PathVariable(value = "id") String periodId,
                                                        @Valid @RequestBody PeriodicityRequest risqPeriodicity) throws NumberFormatException, ResourceNotFoundException {

       // RiskPeriodicity risq = riskPeriodicityRepository.findByRiskPeriodicityCode(Integer.parseInt(periodId));

        Set<String> emvServiceValues = risqPeriodicity.getEmvServiceValues();

        Set<EmvServiceValues> listEmv = new HashSet<>();


        for (String id : emvServiceValues
        ) {
            EmvServiceValues emv = emvServiceValuesRepository.findByCodeTransaction(id).orElseThrow(
                    () -> new ResourceNotFoundException(" Contrl List not found for this id :: " + id));
            listEmv.add(emv);

        }


        /*risq.setEmvServiceValues(listEmv);
        risq.AmountLimitPeriod = risqPeriodicity.AmountLimitPeriod;
        risq.AmountMaxPeriod = risqPeriodicity.AmountMaxPeriod;
        risq.AmountMinPeriod = risqPeriodicity.AmountMinPeriod;
        risq.NumberLimitPeriod = risqPeriodicity.NumberLimitPeriod;
        risq.riskPeriodicityStartDate = risqPeriodicity.riskPeriodicityStartDate;
        risq.getRiskPeriodicityEndDate = risqPeriodicity.getRiskPeriodicityEndDate;
        risq.setTypeCode(risqPeriodicity.getTypeCode());
*/
        // modelMapper.map(risqPeriodicity, risq);

        //final RiskPeriodicity updatedRisq = riskPeriodicityRepository.save(risq);
        /*return ResponseEntity.ok(updatedRisq);

    }*/


    //update program
    @PutMapping("updateProgram/{id}")
    public ResponseEntity<Program> updateProgram(@PathVariable(value = "id") String programId,
                                                 @Valid @RequestBody ProgramRequest programRequest)
            throws NumberFormatException, ResourceNotFoundException {
    	try {
    		
    		
    	
        PosEntryModeTypes posEntryModeType = new PosEntryModeTypes();
        logger.info(programRequest.toString());
        
        Program program = programReposiroty.findByProgramCode(Integer.parseInt(programId)).get();
        logger.info(program.toString());
        Integer codee=program.getProgramCode();
        Set<String> controList = programRequest.getControlLists();
        controList.remove("0");
        
        logger.info(controList.toString());
        
        Set<PosEntryModeTypes> posEntryModeTypesList = new HashSet<>();
        Country country = new Country();

        Set<ControlList> listControls = new HashSet<>();
        Set<Country> countriesControlList = new HashSet<>();

        modelMapper.map(programRequest, program);
        program.setProgramCode(codee);
        for (String id : controList

        ) {
        
            ControlList controlList = controlListRepository.findByControlNum(id)
                    .orElseThrow(
                            () -> new ResourceNotFoundException(" Contrl List not found for this id :: " + id));
         
            listControls.add(controlList);
            if(controlList.getLibelleControl().equals("POS_ENTRY_MODE")){
                for(int code : programRequest.getPosEntryModeType()){
                    posEntryModeType = posEntryModeTypesRepository.findByPosEntryModeCode(code);
                    posEntryModeTypesList.add(posEntryModeType);
                    program.setPosEntryModeTypes(posEntryModeTypesList);
                }
            }
         
            if(controlList.getLibelleControl().equals("COUNTRY") && programRequest.getCountriesControl().size()>0){
                for(int code : programRequest.getCountriesControl()){
                	country = countryRepo.findByCountryId(code);
                	countriesControlList.add(country);
                	program.setCountriesControl(countriesControlList);
                   
                }
            }
         
        }

       
        if (programRequest.getCvrValue() != null && programRequest.getCvrValue() != "") {

            if (cvrRepository.findByCvrCodee(Integer.parseInt(programRequest.getCvrValue())).isPresent()) {
                Cvr cvr = cvrRepository.findById(Integer.parseInt(programRequest.getCvrValue())).get();
                program.setCvr(cvr.getCvrCodee());

            }
        }
       

        if(programRequest.getTvrValue() != null && programRequest.getTvrValue() != "" ){
            if (tvrRepository.findByTvrCode(Integer.parseInt(programRequest.getTvrValue())).isPresent()) {
                Tvr tvr = tvrRepository.findById(Integer.parseInt(programRequest.getTvrValue())).get();
                program.setTvr(tvr.getTvrCode());
            }
        }
       
        program.setControlLists(listControls);
        //prod
        if (!listControls.contains(controlListRepository.findByControlNum("90").get()))
        	program.setPosEntryModeTypes(new HashSet<>());
        //preprod
//      if (!listControls.contains(controlListRepository.findByControlNum("10").get()))
//    	program.setPosEntryModeTypes(new HashSet<>());   
        
//        if (!listControls.contains(controlListRepository.findByControlNum("9").get()))
//        	program.setCountriesControl(new HashSet<>());
        
        program.setBinOnUsCode(programRequest.getBinOnUs());
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(program.getStartDate());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        program.setStartDate(calendar.getTime());
        logger.info(program.toString());
        
        final Program updatedProgram = programReposiroty.save(program);
        
        logger.info(updatedProgram.toString());


        if (programRequest.getMinAmountContactless()==null) {
        	programRequest.setMinAmountContactless(0);
        }
        if (programRequest.getMaxAmountContactless()==null) {
        	programRequest.setMaxAmountContactless(0);
        }
        		


        if (programRequest.cprContacless && programRequest.getMinAmountContactless()!=null && programRequest.getMaxAmountContactless()!=null) {
        	Optional<ContaclessControl> control =contaclessControlRepository.findByProgramId(updatedProgram.getProgramCode());
        	if (control.isPresent()) {
        		control.get().setMinAmount(programRequest.getMinAmountContactless());
        		control.get().setMaxAmount(programRequest.getMaxAmountContactless());
        		control.get().setPin(programRequest.getContactlessWithPin());
            	contaclessControlRepository.save(control.get());

        	}else {
        			ContaclessControl contaclessControl=new ContaclessControl();
                	contaclessControl.setProgramId(updatedProgram.getProgramCode());	
                	contaclessControl.setMinAmount(programRequest.getMinAmountContactless());
                	contaclessControl.setMaxAmount(programRequest.getMaxAmountContactless());
                	contaclessControl.setPin(programRequest.getContactlessWithPin());
                	contaclessControlRepository.save(contaclessControl);
        		
        		
        		
        	}
        	
        }else {
        	Optional<ContaclessControl> control =contaclessControlRepository.findByProgramId(updatedProgram.getProgramCode());
        	if (control.isPresent()) 
        		contaclessControlRepository.deleteById(control.get().getContaclessControlCode());
        		
        	
        }
        return ResponseEntity.ok(updatedProgram);
}catch(Exception e) {
	
logger.info(Throwables.getStackTraceAsString ( e )) ;
return null;

    	}
       

    }
    
    @PostMapping("getAccountByCard")
    public UpdateAccountDisplay getAccountByCard(@RequestBody RequestFilterDomiciliation requestFilterDomiciliation ) throws Exception {
    	UpdateAccountDisplay updateAccountDisplay=new UpdateAccountDisplay();

    	if(requestFilterDomiciliation.getPan()!="") {
    		
        	Optional<Card> card = cardRepository.findByCardNumAndBranch(requestFilterDomiciliation.getPan(),requestFilterDomiciliation.getAgence());
        	if (card.isPresent()) {
        		if (card.get().getCardStatusCode()!=4 && 
        				card.get().getCardStatusCode()!=5 && 
        				card.get().getCardStatusCode()!=8 
        				) {
            		Optional<Account> account= accountRepository.findByAccountCode(card.get().getAccCode());
            		if (account.isPresent()) {
            			if (account.get().getAstCode()!=9) {
            				
            				if (account.get().getAccountNum().substring(7,13).equals("220200"))
            					throw new Exception("CHAPITRE INVALIDE!");
            				
            				
            				updateAccountDisplay.setAccount(account.get());
                			Optional<Customer> customer=customerRepository.findByCustomerCode(Integer.parseInt(account.get().getCustomerCode()));
                			if (customer.isPresent()) {
                				updateAccountDisplay.setCustomer(customer.get());
                			}
                			List<CardsToUpdateRequest> cards = new ArrayList<CardsToUpdateRequest>();
                			
                			
                			Optional<Product> product = productRepositpry.findById(card.get().getProductCode());
                			CardsToUpdateRequest cardsToUpdateRequest= new CardsToUpdateRequest(
                					card.get().getCardCode(),
                					card.get().getCardNum(),
                					card.get().getExpiryDate(),
                					product.get().getLibelle());
                			cards.add(cardsToUpdateRequest);
                			updateAccountDisplay.setCard(cards);
                			
                			
            			}
            			else {
                			throw new Exception("STATUT COMPTE INVALIDE!");
                		}
            			
            		}else {
            			throw new Exception("COMPTE INVALIDE!");
            		}
            	
        			
        		}else {
        			throw new Exception("STATUT CARTE INVALIDE!");
        		}

        		return updateAccountDisplay;
        	}
    	}
    	else if(requestFilterDomiciliation.getAccountNumber()!="") {
    		Optional<AgenceAdministration> agence = agenceAdministrationRepository.findByInitial(requestFilterDomiciliation.getAgence());
    		
    		
    		
    		Optional<Account> account= accountRepository.findByAccountNumAndBranch(
    			"35"	+requestFilterDomiciliation.getAgence()+requestFilterDomiciliation.getAccountNumber() 
    				,agence.get().getCodeAgence());
    		
    		if (account.isPresent()) {
    			
    			if (account.get().getAstCode()!=9) {

    				if (account.get().getAccountNum().substring(7,13).equals("220200"))
    					throw new Exception("CHAPITRE INVALIDE!");
    				
    				updateAccountDisplay.setAccount(account.get());
        			Optional<Customer> customer=customerRepository.findByCustomerCode(Integer.parseInt(account.get().getCustomerCode()));
        			if (customer.isPresent()) {
        				updateAccountDisplay.setCustomer(customer.get());
        			}
        			List<CardsToUpdateRequest> cards = new ArrayList<CardsToUpdateRequest>();

        			for(Card card :cardRepository.findByAccCode(account.get().getAccountCode())) {
        				if (card.getCardStatusCode()!=4 && 
                				card.getCardStatusCode()!=5 && 
                				card.getCardStatusCode()!=8 
                				) {
        					
                			Optional<Product> product = productRepositpry.findById(card.getProductCode());
                			System.out.println("cards size "+product.get().getLibelle());
        					
        					CardsToUpdateRequest cardsToUpdateRequest= new CardsToUpdateRequest(
        							card.getCardCode(),
        							card.getCardNum(),
                					card.getExpiryDate(),
                					product.get().getLibelle());
                			cards.add(cardsToUpdateRequest);
        				}
        			}
        			System.out.println("cards size "+cards.size());
        			updateAccountDisplay.setCard(cards);
        			
    			}else {
        			throw new Exception("STATUT COMPTE INVALIDE!");
        		}
    			return updateAccountDisplay;
    		}else {
    			throw new Exception("COMPTE INVALIDE!");
    		}
    	}
    	
        return null;
    }
    
    @PostMapping("updateAccount")
    public ResponseEntity<String> changeAccountNumber(@RequestBody ChangeAccountRequest accountChangeRequest) {
    	logger.info("old account num => {}",accountChangeRequest.getOldAccountNumber());
    	logger.info("new account num => {}",accountChangeRequest.getAccountNumber());
    	  String name = SecurityContextHolder.getContext().getAuthentication().getName();
			Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
			if (!user.isPresent()) {
				throw new RuntimeException("Error saving status !");
			}
    	Account account= accountRepository.findByAccountNum(accountChangeRequest.getOldAccountNumber());
    	if (account !=null) {
    		Account newAccount= accountRepository.findByAccountNum(accountChangeRequest.getAccountNumber());
			if (newAccount == null) {
				logger.info("Creating new account");
				newAccount=new Account();
				newAccount.setAccountNum(accountChangeRequest.getAccountNumber());
				newAccount.setAccountNumAttached(accountChangeRequest.getAccountNumber());
				String devise = accountChangeRequest.getCurrency();
				CurrencyFSBK currencyFSBK = currencyFSBKRepository.getOne(devise);
				if (currencyFSBK != null) {
					devise = currencyFSBK.getCodeDevise();
				}

				newAccount.setCurrency(devise);
				newAccount.setCustomerCode(account.getCustomerCode());
				newAccount.setIdAgence(accountChangeRequest.getBranchCode());

				newAccount.setAccountAuthorize(account.getAccountAuthorize());
				newAccount.setAccountAvailable(account.getAccountAvailable());
				newAccount.setAccountBilling(account.getAccountBilling());
				newAccount.setAccountExceeding(
						account.getAccountExceeding() != null ? account.getAccountExceeding() : BigInteger.valueOf(0)

				);
				newAccount.setPreauthAmount(
						account.getPreauthAmount() != null ? account.getPreauthAmount() : BigInteger.valueOf(0));
				newAccount.setRefundAmount(
						account.getRefundAmount() != null ? account.getRefundAmount() : BigInteger.valueOf(0));
				newAccount.setAccountBalance(
						account.getAccountBalance() != null ? account.getAccountBalance() : BigInteger.valueOf(0));
				newAccount.setCreationDate(new Date());
				
				newAccount.setAccountRevolvingLimit(
						account.getAccountRevolvingLimit() != null ? account.getAccountRevolvingLimit() : BigInteger.valueOf(0));
				newAccount.setAccountType(account.getAccountType());
				newAccount.setAccountName(accountChangeRequest.getAccountName());
				newAccount.setGlobalRiskMangCode(null);
				newAccount.setAstCode(1);

				accountRepository.save(newAccount);
				
				
			}else 
				logger.info("new account already exist");
			
			
			Optional<AgenceAdministration> agence=agenceAdministrationRepository.findByIdAgence(newAccount.getIdAgence());

			logger.info("updating cards...");
			if (accountChangeRequest.getAllCards()==1) {
				for(int code:accountChangeRequest.getCards()) {
					Optional<Card> card=cardRepository.findByCardCode(code);
					if (card.isPresent()) {
						card.get().setAccCode(newAccount.getAccountCode());
		                 if (agence.isPresent())
		                   card.get().setAgencyCode(agence.get().getInitial());
		                 cardRepository.save(card.get());
		                 
		                 CardHistory cardHistory = new CardHistory();
		 				cardHistory.setCardCode(card.get().getCardCode());
		 				cardHistory.setOperation("Compte changé de " + accountChangeRequest.getOldAccountNumber() + " à "
		 						+ accountChangeRequest.getAccountNumber());
		 				cardHistory.setOperation_date(new Date());
		 				cardHistory.setEditedBy(user.get().getUserName());
		 				cardHistoryRepository.save(cardHistory);
					}
				}
			}else {
				Optional<Card> card=cardRepository.findByCardCode(Integer.parseInt(accountChangeRequest.getCardCode()));
				if (card.isPresent()) {
					card.get().setAccCode(newAccount.getAccountCode());
					if (agence.isPresent())
		                card.get().setAgencyCode(agence.get().getInitial());
		            cardRepository.save(card.get());
		            
		            CardHistory cardHistory = new CardHistory();
					cardHistory.setCardCode(card.get().getCardCode());
					cardHistory.setOperation("Compte changé de " + accountChangeRequest.getOldAccountNumber() + " à "
							+ accountChangeRequest.getAccountNumber());
					cardHistory.setOperation_date(new Date());
					cardHistory.setEditedBy(user.get().getUserName());
					cardHistoryRepository.save(cardHistory);
				}
			}
			
			  	return ResponseEntity.ok().body(gson.toJson("Account updated successfully!"));
    	}else 		 
    			return ResponseEntity.badRequest().body(gson.toJson("Invalid account!"));
    }
    
    @GetMapping("getDemandeActionStatusById/{id}")
	public Integer getDemandeActionById( @PathVariable(value = "id") Integer id){
    	Optional<DemandeAction>  request = demandeActionRepository.findById(id);
    	if (request.isPresent()) {
    		return request.get().getDemandeStatusCode();
    	}
    	return null;
    }

    
	
	@PostMapping("getAllDemandeActions")
	public Page<DemandeAction> getAllDemandeActions(@RequestBody DemandeActionsFilter demandeActionsFilter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir)  {
         
		// List<Account> accounts=new ArrayList<Account>();
		Order order = null;
		if (dir.equals("desc"))
			order = new Order(Sort.Direction.DESC, sortOn);
		else
			order = new Order(Sort.Direction.ASC, sortOn);
		Page<DemandeAction> page1 = null;
		

		page1 =  demandeActionRepository.getAllDemandeAction(PageRequest.of(page, size, Sort.by(order)),
				demandeActionsFilter.getPan(), demandeActionsFilter.getActionType(),
				demandeActionsFilter.getActionStatus(), demandeActionsFilter.getBranchCode()
				,demandeActionsFilter.getAccountNumber());
		

//		page1.forEach(element -> {
//			element.setCardNum(maskCardNumber(element.getCardNum()));
//			
//			
//
//		});

		return page1;
	
	}
    
    @PostMapping("getPageEligibleAccount")
	public Page<EligibleAccount> getPageEligibleAccount(@RequestBody PosRequestLibelle filter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir

	) {

		List<Order> orders = new ArrayList<Order>();
		Order order1 = null;
		if (dir.equals("desc"))
			order1 = new Order(Sort.Direction.DESC, sortOn);
		else
			order1 = new Order(Sort.Direction.ASC, sortOn);

		orders.add(order1);
		return eligibleAccountRepository.getPageEligibleAccount(PageRequest.of(page, size, Sort.by(orders)),
				filter.getLibelle().trim());

	}
	@PostMapping("AddEligibleAccount")
	public ResponseEntity<String> AddEligibleAccount(@RequestBody EligibleAccount eligibleAccount) {
	
		eligibleAccountRepository.save(eligibleAccount);
			return ResponseEntity.ok().body(gson.toJson("Eligible Account créé"));
		

	}

	@PutMapping("updateEligibleAccount/{id}")
	public ResponseEntity<String> updateEligibleAccount(@PathVariable(value = "id") Integer eligibleAccountCode,
			@RequestBody EligibleAccount eligibleAccount) {

		EligibleAccount EligibleAccountExist = eligibleAccountRepository.getOne(eligibleAccountCode);
		EligibleAccountExist.setLabel(eligibleAccount.getLabel());
		EligibleAccountExist.setType(eligibleAccount.getType());
		EligibleAccountExist.setChapter(eligibleAccount.getChapter());
		eligibleAccountRepository.save(EligibleAccountExist);
		return ResponseEntity.ok().body(gson.toJson("Eligible Account update"));

	}
	
	@PostMapping("/uploadProgramContract/{id}")
	public ResponseEntity<String> uploadProgramContract(@RequestParam("file") MultipartFile file,
			@PathVariable(value = "id") Integer programId) {
  
		try {
			Optional<ProgramContract> programContract = programContractRepository.findByProgramId(programId);

			if (programContract.isPresent()) {
				programContract.get().setContentType(null);
				programContract.get().setFileName(file.getOriginalFilename());
				programContract.get().setContentType(file.getContentType());
				programContract.get().setData(file.getBytes());
				programContractRepository.save(programContract.get());
				return ResponseEntity.ok("File uploaded successfully");
				} else {
				ProgramContract fileEntity = new ProgramContract();
				fileEntity.setFileName(file.getOriginalFilename());
				fileEntity.setContentType(file.getContentType());
				fileEntity.setProgramId(programId);

				fileEntity.setData(file.getBytes());
				 
				
				programContractRepository.save(fileEntity);
				return ResponseEntity.ok("File uploaded successfully");
			}

		} catch (Exception e) {
			logger.info("Exception is=>{}", Throwables.getStackTraceAsString(e));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
		}
	}
	
	@GetMapping(value = "/getProgramContract/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public byte[] getProgramContract(@PathVariable(value = "id") Integer programId)
			throws IOException, DocumentException {

		//System.out.println("xxxxxxxxxxxxx");
		Optional<ProgramContract> fileOptional = programContractRepository.findByProgramId(programId);

		if (fileOptional.isPresent()) {
		//	System.out.println("yyyyyyyyy");
			ProgramContract fileEntity = fileOptional.get();
			byte[] fileData = fileEntity.getData();
			return fileData;

		} else {

			return null;

		}
	}

	@GetMapping("/getProgramContratName/{id}")
	public ResponseEntity<String> getProgramContratName(@PathVariable(value = "id") Integer programId)
			throws IOException {
		Optional<ProgramContract> fileOptional = programContractRepository.findByProgramId(programId);

		if (fileOptional.isPresent()) {
			ProgramContract fileEntity = fileOptional.get();

			String fileName = fileEntity.getFileName();
			logger.info("contract name is {},",fileName);
			return ResponseEntity.accepted().body(gson.toJson(fileName));

		} else {

			return null;

		}
	}
	
	@GetMapping("/getFileNameProgramContract/{id}")
	public ResponseEntity<String> getFileNameProgramContract(@PathVariable Integer id) throws IOException {
		Optional<Product> product = productRepositpry.findById(id);

		if (product.isPresent()) {
			Set<Program> programs = product.get().getPrograms();

			for (Program prog : programs) {
				Optional<ProgramContract> fileOptional = programContractRepository
						.findByProgramId(prog.getProgramCode());
				if (fileOptional.isPresent()) {
					ProgramContract fileEntity = fileOptional.get();
					String fileName = fileEntity.getFileName();
					
					return ResponseEntity.ok().body(gson.toJson(fileName));
				}
			}
		}

		return ResponseEntity.notFound().build();
	}
	
	
	@GetMapping("/getContractFileNameByCard/{cardCode}")
	public ResponseEntity<String> getContractFileNameByCard(@PathVariable(value = "cardCode") Integer cardCode) throws IOException {
		
		Optional<Card> card=cardRepository.findByCardCode(cardCode);
		if (card.isPresent()) {
			
			Optional<Product> product = productRepositpry.findById(card.get().getProductCode());

			if (product.isPresent()) {
				Set<Program> programs = product.get().getPrograms();

				for (Program prog : programs) {
					Optional<ProgramContract> fileOptional = programContractRepository
							.findByProgramId(prog.getProgramCode());
					if (fileOptional.isPresent()) {
						ProgramContract fileEntity = fileOptional.get();
						String fileName = fileEntity.getFileName().replace(".docx", "_"+card.get().getCardName()+".docx");
						
						return ResponseEntity.ok().body(gson.toJson(fileName));
					}
				}
			}

		}
		

		return ResponseEntity.notFound().build();
	}
	
	@GetMapping("/getAllCurrencies")
	public List<CurrencyDto> getAllCurrencies() throws IOException {
		
		List<CurrencyDto> 	currenciesDto = new ArrayList<CurrencyDto>();
		List<CurrencyFSBK> currencies = currencyFSBKRepository.findAll();
		
		for (CurrencyFSBK currency : currencies) {
			
			currenciesDto.add(new CurrencyDto(currency.getLibelle(),currency.getCodeDevise()));
		}
		
		
		

		return currenciesDto;
	}
	
	@GetMapping("getEligibleInternationalProducts/{chapter}")
    public List<Product> getEligibleInternationalProducts(@PathVariable(value = "chapter") String chapter)  {
    	List<Product> all=productRepositpry.findAll();
    	List<Product> products= new ArrayList<Product>();
    	for(Product product :all) {
    		
    		for (Program prog : product.getPrograms()) {
    			if (!prog.getCurrency().equals("012")) {
    				
    				if (product.getEligibleAccounts()!=null) {
    	    			for(EligibleAccount eligibleAccount:product.getEligibleAccounts()) {
    	        			if (eligibleAccount.getChapter().equals(chapter))
    	        				products.add(product);
    	        		}
    	    		}
    			}
    			
    		}
    	}
        return products;
    }
	
	@PostMapping("getCardHolderInfo")
	public Page<CardHolderInfo> getCardHolderInfo(@RequestBody CardComplaintsFilter filter,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "sortOn") String sortOn,
			@RequestParam(name = "dir") String dir

	) {

				Sort.Direction direction=dir.equals("desc")?Sort.Direction.DESC:Sort.Direction.ASC;
				Order defaultOrderDirection = new Order(direction, sortOn);
				Order uniqueOrderDirection = new Order(direction, "CardHolderInfoCode");

		//Page<CardHolderInfo> page1 = null;

		return cardHolderInfoRepository.getCardHolderInfo(PageRequest.of(page, size, Sort.by(defaultOrderDirection,uniqueOrderDirection)),
				filter.getAgence(), filter.getPan(), filter.getAccountNumber());

//		page1.forEach(element -> {
//			element.setPan(maskCardNumber(element.getPan()));
//
//		});

	//	return page1;

	}
	
	@PutMapping("updateCardHolderInfo/{code}/{phoneNumber}")
	public CardHolderInfo updateCardHolderInfo(@PathVariable(value = "code") Integer code,
			@PathVariable(value = "phoneNumber") String phoneNumber) {

		Optional<CardHolderInfo> info = cardHolderInfoRepository.findById(code);
		if (info.isPresent()) {
			String name = SecurityContextHolder.getContext().getAuthentication().getName();
			Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
			if (!user.isPresent()) {
				throw new RuntimeException("Error saving status !");
			}

			info.get().setModifDate(new Date());
			info.get().setAction("update");
			info.get().setStatusGeneration(0);
			info.get().setPhoneNumber(phoneNumber);

			return cardHolderInfoRepository.save(info.get());

		}
		return null;

	}

	@GetMapping("deleteCardHolderInfo/{code}")
	public CardHolderInfo deleteCardHolderInfo(@PathVariable(value = "code") Integer code) {

		Optional<CardHolderInfo> info = cardHolderInfoRepository.findById(code);
		if (info.isPresent()) {
			String name = SecurityContextHolder.getContext().getAuthentication().getName();
			Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
			if (!user.isPresent()) {
				throw new RuntimeException("Error saving status !");
			}

			info.get().setModifDate(new Date());
			info.get().setAction("delete");
			info.get().setStatusGeneration(0);

			return cardHolderInfoRepository.save(info.get());

		}
		return null;

	}
}
