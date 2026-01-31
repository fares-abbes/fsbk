package com.mss.backOffice.specification;

import com.mss.unified.entities.UAP070FransaBankHistory;
import com.mss.unified.entities.UAP070INHistory;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UAP070HistorySpecification {
    
    public static Specification<UAP070FransaBankHistory> withDynamicQuery(Map<String, Object> filterParams) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (filterParams != null) {
                filterParams.forEach((key, value) -> {
                    if (value != null && !value.toString().isEmpty()) {
                        switch (key) {
                            case "codeBankEmetteur":
                            case "numRIBEmetteur":
                            case "numCartePorteur":
                            case "numRIBAcquereur":
                            case "codeBankAcquereur":
                            case "codeAgence":
                            case "typeTransaction":
                            case "dateTransaction":
                            case "numRefTransaction":
                            case "numAutorisation":
                            case "statusPaiement":
                            case "statusTechnique":
                            case "origin":
                                predicates.add(criteriaBuilder.like(
                                    criteriaBuilder.lower(root.get(key).as(String.class)),
                                    "%" + value.toString().toLowerCase() + "%"
                                ));
                                break;
                            case "montantTransactionFrom":
                                if (value instanceof Number) {
                                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                                        root.get("montantTransaction"), 
                                        new BigDecimal(value.toString())
                                    ));
                                }
                                break;
                            case "montantTransactionTo":
                                if (value instanceof Number) {
                                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                                        root.get("montantTransaction"), 
                                        new BigDecimal(value.toString())
                                    ));
                                }
                                break;
                        }
                    }
                });
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
