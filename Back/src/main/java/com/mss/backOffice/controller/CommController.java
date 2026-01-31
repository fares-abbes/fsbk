package com.mss.backOffice.controller;

import com.google.gson.Gson;

import com.mss.unified.entities.Comm;
import com.mss.backOffice.exception.ResourceNotFoundException;
import com.mss.unified.entities.Comm;
import com.mss.unified.entities.EmvServiceValues;
import com.mss.unified.repositories.CommRepo;
import com.mss.unified.repositories.EmvServiceValuesRepository;
import com.mss.unified.repositories.ProgramReposiroty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("comm")
public class CommController {


    private static final Gson gson = new Gson();

    @Autowired
    private EmvServiceValuesRepository emvServiceValuesRepository;
    @Autowired
    CommRepo commRepository;
    @Autowired
    ProgramReposiroty programReposiroty;

    //creating a get mapping that brigs all comms

    @GetMapping("/comm")
    private List<Comm> getAllComms() {
        return commRepository.findAll();
    }

    //creating a get mapping that brigs a specified comm
    @GetMapping("/comm/{commId}")
    private Comm getComm(@PathVariable("commId") int commId) {

        return commRepository.findById(commId).get();

    }

    //creating a delete mapping that deletes a specified comm
    @DeleteMapping("/comm/{commId}")
    private void deleteComm(@PathVariable("commId") int commId) {
        commRepository.deleteById(commId);
    }

    //creating post mapping that post the comm detail in the database
    @ResponseBody
    @PostMapping("/saveComm")
    public ResponseEntity<String> saveComm(@RequestBody Comm comm) throws ResourceNotFoundException {

        comm.setProgram(programReposiroty.findById(comm.getProgram().getProgramCode()).get());


        Set<EmvServiceValues> transactions = comm.getEmvServiceValues();
        Set<EmvServiceValues> emvServiceValues = new HashSet<>();

        for (EmvServiceValues id : transactions

        ) {
            EmvServiceValues serviceValues = emvServiceValuesRepository.findByCodeTransaction(id.getCodeTransaction()).orElseThrow(() -> new ResourceNotFoundException(" EmvServiceValues not found for this id :: " + id));
            emvServiceValues.add(serviceValues);

        }

        comm.setEmvServiceValues(emvServiceValues);


        Set<EmvServiceValues> list = comm.getEmvServiceValues();

        for (EmvServiceValues transaction:list) {

            Comm rc=new Comm();
            rc.setCurrency(comm.getCurrency());
            rc.setEmvServiceValues(comm.getEmvServiceValues());
            rc.setFixedCommission(comm.getFixedCommission());
            rc.setLibelle(comm.getLibelle());
            rc.setProgram(comm.getProgram());
            rc.setTransactionSource(comm.getTransactionSource());
            rc.setVariableComission(comm.getVariableComission());
            rc.setId(0);

            rc.setTransaction(transaction);
            commRepository.save(rc);

        }


        //commRepository.save(comm);
        return ResponseEntity.accepted().body(gson.toJson("Comm saved successfully!"));
    }



    @GetMapping("/comm/{programId}")
    private Optional<List<Comm>> findByRevProgramId(@RequestParam("programId") int programId) {
        return commRepository.findByProgramId(programId);
    }





    //creating put mapping that updates the comm detail
    @PutMapping("/comm")
    private Comm update(@RequestBody Comm comm) {
        try {
            commRepository.save(comm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return comm;
    }
}
