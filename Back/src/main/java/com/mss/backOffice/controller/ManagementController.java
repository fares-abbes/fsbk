package com.mss.backOffice.controller;

import com.google.gson.Gson;

import com.mss.backOffice.request.AgenceFilter;
import com.mss.backOffice.request.ZoneFilter;
import com.mss.unified.entities.AgenceAdministration;
import com.mss.unified.entities.AgencyStatus;
import com.mss.unified.entities.Region;
import com.mss.unified.entities.UAP050IN;
import com.mss.unified.entities.User;
import com.mss.unified.entities.Zone;
import com.mss.unified.repositories.AgenceAdministrationRepository;
import com.mss.unified.repositories.AgencyStatusRepository;
import com.mss.unified.repositories.RegionRepository;
import com.mss.unified.repositories.UserRepository;
import com.mss.unified.repositories.ZoneRepository;

import org.apache.xmlbeans.SchemaAnnotation;
import org.hibernate.boot.jaxb.hbm.spi.EntityInfo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@RestController
@RequestMapping("/management")
public class ManagementController {
    private static final Gson gson = new Gson();
    @Autowired
    AgenceAdministrationRepository agenceAdministrationRepository;
    @Autowired
    ZoneRepository zoneRepository;
    @Autowired
    RegionRepository regionRepository;
    
    @Autowired
    AgencyStatusRepository agencyStatusRepository;

    ModelMapper modelMapper;

    @Autowired
    UserRepository userRepository;

    @GetMapping("AllRegions")
    public List<Region> getAllRegions() {
        return regionRepository.findAll();
    }

    @PostMapping("AllRegionFiltred")
    public List<com.mss.unified.entities.Region> getAlFiltredlRegions(@RequestBody String initial) {
        if(!initial.equals("=")){
            return regionRepository.findAllByInitial(initial.trim());
        } else{
            return regionRepository.findAll();
        }
    }
    @PostMapping("AllZoneFiltred")
    public List<Zone> getAllFiltredZones(@RequestBody ZoneFilter zoneFilter) {
        String initial = "";
        String initialRegion = "";
        if (zoneFilter.getInitial() != null) {
            initial = initial + zoneFilter.getInitial().trim();
        }
        if (zoneFilter.getInitialRegion() != null) {
            initialRegion = initialRegion + zoneFilter.getInitialRegion().trim();
        }
        return zoneRepository.findZoneByInitialAndInitialRegion(initial,initialRegion);
    }
    @PostMapping("AllAgenceFiltred")
    public List<AgenceAdministration> AllAgenceFiltred(@RequestBody AgenceFilter agenceFilter) {
        String initial = "";
        String initialRegion = "";
        String initialZone = "";
        if (agenceFilter.getInitial() != null) {
            initial = initial + agenceFilter.getInitial().trim();
        }
        if (agenceFilter.getInitialRegion() != null) {
            initialRegion = initialRegion + agenceFilter.getInitialRegion().trim();
        }
        if (agenceFilter.getInitialZone() != null) {
            initialZone = initialZone + agenceFilter.getInitialZone().trim();
        }
        return agenceAdministrationRepository.findByInitialAndInitialZoneAndInitialRegion(initial,initialZone,initialRegion);
    }

    @PostMapping("addRegion")
    public ResponseEntity<?> addRegion(@RequestBody Region region) {
        Optional<Region> regionFound = regionRepository.findByInitial(region.getInitial());
        if (regionFound.isPresent()) {
            return ResponseEntity.status(409).body(gson.toJson("Initial Already exist"));
        } else {
            regionRepository.save(region);
            return ResponseEntity.status(200).body(gson.toJson(region));
        }
    }

    @PostMapping("addZone")
    public ResponseEntity<?> addZone(@RequestBody Zone zone) {
        Optional<Zone> zoneFound = zoneRepository.findByInitial(zone.getInitial());
        if (zoneFound.isPresent()) {
            return ResponseEntity.status(409).body(gson.toJson("Initial Already exist"));
        } else {
            Optional<Region> regionFound = regionRepository.findByInitial(zone.getInitialRegion());
            if (!regionFound.isPresent()) {
                return ResponseEntity.status(404).body(gson.toJson("Initial Region does not exist"));
            } else {
                zone.setCodeRegion(regionFound.get().getCodeRegion());
                zoneRepository.save(zone);
                return ResponseEntity.status(200).body(gson.toJson(zone));
            }
        }
    }

    @PostMapping("addAgence")
    public ResponseEntity<?> addAgence(@RequestBody AgenceAdministration agence) {
        Optional<AgenceAdministration> agenceFound = agenceAdministrationRepository.findByInitial(agence.getInitial());
        if (agenceFound.isPresent()) {
            return ResponseEntity.status(409).body(gson.toJson("Initial Already exist"));
        } else {
            Optional<Zone> zoneFound = zoneRepository.findByInitial(agence.getInitialZone());
            if (!zoneFound.isPresent()) {
                return ResponseEntity.status(404).body(gson.toJson("Initial Zone does not exist"));
            } else {
                agence.setCodeZone(zoneFound.get().getCodeZone());
                agenceAdministrationRepository.save(agence);
                return ResponseEntity.status(200).body(gson.toJson(agence));
            }
        }
    }

    @PutMapping("updateRegion/{id}")
    public ResponseEntity<?> updateRegion(@Valid @RequestBody Region region, @PathVariable(value = "id") Integer id) {
        Region regionFound = regionRepository.findByCodeRegion(id).get();
        regionFound.setLibelle(region.getLibelle());
        regionRepository.save(regionFound);
        return ResponseEntity.status(200).body(gson.toJson(regionFound));
    }

    @PutMapping("updateZone/{id}")
    public ResponseEntity<?> updateZone(@RequestBody Zone zone, @PathVariable(value = "id") Integer id) {
        Optional<Zone> zoneFound = zoneRepository.findByCodeZone(id);
        if (zoneFound.isPresent()) {
        Optional<Region> region = regionRepository.findByInitial(zone.getInitialRegion());
        if (region.isPresent()) {
            zoneFound.get().setInitialRegion(zone.getInitialRegion());
            zoneFound.get().setLibelle(zone.getLibelle());
            zoneFound.get().setCodeRegion(region.get().getCodeRegion());
            zoneFound.get().setEmail(zone.getEmail());
            zoneRepository.save(zoneFound.get());
            return ResponseEntity.status(200).body(gson.toJson(zoneFound.get()));
        } else {
            return ResponseEntity.status(404).body(gson.toJson("Region not found"));
        }
        }else {
            return ResponseEntity.status(404).body(gson.toJson("Region not found"));
        }
    }

    @PutMapping("updateAgence/{id}")
    public ResponseEntity<?> updateAgence(@RequestBody AgenceAdministration agence, @PathVariable(value = "id") Integer id) {
        AgenceAdministration agenceFound = agenceAdministrationRepository.findByCodeAgence(id).get();
        Optional<Zone> zone = zoneRepository.findByInitial(agence.getInitialZone());
        if (zone.isPresent()) {
            agenceFound.setCodeZone(zone.get().getCodeZone());
            agenceFound.setInitialZone(agence.getInitialZone());
            agenceFound.setAdresse(agence.getAdresse());
            agenceFound.setLibelle(agence.getLibelle());
            agenceFound.setStatus(agence.getStatus());
            agenceFound.setManager(agence.getManager());
            agenceFound.setEmail(agence.getEmail());
            agenceFound.setCity(agence.getCity());
            
            agenceAdministrationRepository.save(agenceFound);
            return ResponseEntity.status(200).body(gson.toJson(agenceFound));
        } else {
            return ResponseEntity.status(404).body(gson.toJson("Zone not found"));
        }
    }

//    public static <Region> void merge(Region region, Region regionFound) {
//        ModelMapper modelMapper = new ModelMapper();
//        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
//        modelMapper.map(region, regionFound);}

    @DeleteMapping("deleteRegion/{id}")
    public ResponseEntity<?> deleteRegion(@PathVariable(value = "id") Integer id) {
        List<Zone> zoneFound = zoneRepository.findByCodeRegion(regionRepository.findByCodeRegion(id).get().getCodeRegion());
        if (zoneFound.isEmpty()) {
            List<User> usersList = userRepository.findByUserTypeAndIdAgence(4,id);
            if(usersList.isEmpty()){
                regionRepository.delete(regionRepository.findByCodeRegion(id).get());
                return ResponseEntity.status(200).body(gson.toJson("deleted succesfully"));
            }else{
                return ResponseEntity.status(404).body(gson.toJson("REGION ALLREADY HAS USERS ATTACHED"));
            }
        } else {
            return ResponseEntity.status(409).body(gson.toJson("REGION ALLREADY HAVE ZONES ATTACHED"));
        }
    }

    @DeleteMapping("deleteZone/{id}")
    public ResponseEntity<?> deleteZone(@PathVariable(value = "id") Integer zoneId) {
        List<AgenceAdministration> agenceFound = agenceAdministrationRepository.findByCodeZone(zoneRepository.findByCodeZone(zoneId).get().getCodeZone());
        if (agenceFound.isEmpty()) {
            List<User> usersList = userRepository.findByUserTypeAndIdAgence(3,zoneId);
            if(usersList.isEmpty()){
                zoneRepository.delete(zoneRepository.findByCodeZone(zoneId).get());
                return ResponseEntity.status(200).body(gson.toJson("deleted succesfully"));
            }else{
                return ResponseEntity.status(404).body(gson.toJson("ZONE ALLREADY HAS USERS ATTACHED"));
            }
        } else {
            return ResponseEntity.status(409).body(gson.toJson("ZONE ALLREADY HAVE AGENCES ATTACHED"));
        }
    }

    @DeleteMapping("deleteAgence/{id}")
    public ResponseEntity<?> deleteAgence(@PathVariable(value = "id") Integer agenceId) {

        List<User> usersList = userRepository.findByUserTypeAndIdAgence(1,agenceId);
        if(usersList.isEmpty()){
            agenceAdministrationRepository.delete(agenceAdministrationRepository.findByCodeAgence(agenceId).get());
            return ResponseEntity.status(200).body(gson.toJson("agence deleted succesfully"));
        } else{
            return ResponseEntity.status(404).body(gson.toJson("Agence already has users attached"));
        }
    }

    @GetMapping("AllZones")
    public List<Zone> getAllZones() {
        return zoneRepository.findAll();
    }


    @GetMapping("AllAgences")
    public List<AgenceAdministration> getAllAgences() {
        return agenceAdministrationRepository.findAll();
    }
    
    @GetMapping("allAgencyStatus")
    public List<AgencyStatus> allAgencyStatus() {
        return agencyStatusRepository.findAll();
    }

    @GetMapping("AllAgencesType")
    public List<AgenceAdministration> getAllAgencesByType() {

        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User ConnectUser = userRepository.findByUserNameOrUserEmail(name, name).get();
        List<AgenceAdministration> agences = new ArrayList<AgenceAdministration>();

        if (ConnectUser.getUserType() == 1) {
            agences = agenceAdministrationRepository.findAllByAgence(ConnectUser.getIdAgence());
        }
        if (ConnectUser.getUserType() == 3) {
            agences = agenceAdministrationRepository.findAllByZone(ConnectUser.getIdAgence());
        }
        if (ConnectUser.getUserType() == 4) {
            agences = agenceAdministrationRepository.findAllByRegion(ConnectUser.getIdAgence());
        }
        if (ConnectUser.getUserType() == 5) {
            agences = agenceAdministrationRepository.findAll();
        }
        return agences;
    }

    @GetMapping("allRegionsInitial")
    public List<String> getAllInitialRegions() {
        ArrayList<String> list = new ArrayList<>();
        for (Region region : regionRepository.findAll()) {
            list.add(region.getInitial());
        }
        return list;
    }

    private  JdbcTemplate jdbcTemplate = new JdbcTemplate();

    @Autowired
    public void SqlQueryController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/api/query")
    public List<Map<String, Object>> executeSqlQuery(@RequestBody Map<String, String> request) {
    	 if (!isValidSqlQuery(request.get("sqlQuery") ) || request.get("sqlQuery").toLowerCase().contains("update") ||  request.get("sqlQuery").toLowerCase().contains("truncate") || request.get("sqlQuery").toLowerCase().contains("delete")  || request.get("sqlQuery").toLowerCase().contains("merge") ) {
             throw new IllegalArgumentException("Invalid SQL query.");
         }
        // Perform basic input validation to avoid potential SQL injection.
        if (!isValidSqlQuery(request.get("sqlQuery")  )) {
            throw new IllegalArgumentException("Invalid SQL query.");
        }

        return jdbcTemplate.queryForList(request.get("sqlQuery") );
    }

    private boolean isValidSqlQuery(String sqlQuery) {
        // Implement basic validation logic here based on your requirements.
        // You may also consider using third-party libraries for more sophisticated validation.
        return !sqlQuery.trim().isEmpty();
    }

}
