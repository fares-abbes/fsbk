package com.mss.backOffice.request;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.HashMap;
import java.util.Map;

/**
 * Filter request DTO for UAP070IN and UAP070INHistory queries
 * Includes a historique flag to determine which table to query
 * Supports both nested filters and flat structure
 */
public class UAP070INFilterRequest {
    
    // Flag to determine which table to query
    private Boolean historique;
    
    // Filter criteria as a flexible map
    private Map<String, Object> filters;
    
    public UAP070INFilterRequest() {
        this.filters = new HashMap<>();
    }
    
    public UAP070INFilterRequest(Boolean historique, Map<String, Object> filters) {
        this.historique = historique;
        this.filters = filters != null ? filters : new HashMap<>();
    }
    
    public Boolean getHistorique() {
        return historique;
    }
    
    public void setHistorique(Boolean historique) {
        this.historique = historique;
    }
    
    public Map<String, Object> getFilters() {
        return filters;
    }
    
    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }
    
    /**
     * Catch-all setter for any additional properties not explicitly defined
     * This allows flat JSON structure like {"historique":false,"typeTransaction":"071"}
     * to automatically populate the filters map
     */
    @JsonAnySetter
    public void setFilterProperty(String key, Object value) {
        if (filters == null) {
            filters = new HashMap<>();
        }
        filters.put(key, value);
    }
    
    /**
     * Helper method to check if historique is true
     */
    public boolean isHistorique() {
        return historique != null && historique;
    }
}
