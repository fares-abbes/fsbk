package com.mss.backOffice.controller;

import com.google.gson.Gson;
import com.mss.unified.entities.Transaction;
import com.mss.unified.repositories.TransactionRepo;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("transaction")
public class TransactionController {


    private static final Gson gson = new Gson();


    @Autowired
    TransactionRepo transactionRepository;

    //creating a get mapping that brigs all transactions

    @GetMapping("/transaction")
    private List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    //creating a get mapping that brigs a specified transaction
    @GetMapping("/transaction/{transactionId}")
    private Transaction getTransaction(@PathVariable("transactionId") int transactionId) {

        return transactionRepository.findById(transactionId).get();

    }

    //creating a delete mapping that deletes a specified transaction
    @DeleteMapping("/transaction/{transactionId}")
    private void deleteTransaction(@PathVariable("transactionId") int transactionId) {
        try {
            transactionRepository.deleteById(transactionId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //creating post mapping that post the transaction detail in the database
    @ResponseBody
    @PostMapping("/saveTransaction")
    public Transaction saveTransaction(@RequestBody Transaction transaction) {
        try {
             return transactionRepository.save(transaction);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //creating put mapping that updates the transaction detail
    @PutMapping("/transaction")
    private Transaction update(@RequestBody Transaction transaction) {
        transactionRepository.save(transaction);
        return transaction;
    }

    @RequestMapping(value = "/transaction/{programId}", method = RequestMethod.GET)
    @ResponseBody
    public List<Transaction> findByTransactionProgId(@RequestParam("data") int programId) {
    	
    	 Optional<List<Transaction>> byProgramId = transactionRepository.findByProgramId(programId);
    	if (byProgramId.isPresent())
    		return byProgramId.get();
    	else return new ArrayList<Transaction>();
      
    }

}
