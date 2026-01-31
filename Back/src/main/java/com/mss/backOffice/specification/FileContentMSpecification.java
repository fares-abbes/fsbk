package com.mss.backOffice.specification;

import com.mss.unified.entities.FileContentM;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Specification builder for FileContentM entity to support dynamic filtering
 */
public class FileContentMSpecification {

    /**
     * Build dynamic specification based on non-null fields in the filter object
     * 
     * @param filter FileContentM object containing filter criteria
     * @return Specification for querying FileContentM
     */
    public static Specification<FileContentM> buildSpecification(FileContentM filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by ID
            if (filter.getId() > 0) {
                predicates.add(criteriaBuilder.equal(root.get("id"), filter.getId()));
            }

            // Filter by dateCompensation
            if (filter.getDateCompensation() != null && !filter.getDateCompensation().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("dateCompensation"), "%" + filter.getDateCompensation() + "%"));
            }

            // Filter by summaryCode
            if (filter.getSummaryCode() != null) {
                predicates.add(criteriaBuilder.equal(root.get("summaryCode"), filter.getSummaryCode()));
            }

            // DONOR DATA FILTERS
            if (filter.getIdPlateformeMobileDonneur() != null && !filter.getIdPlateformeMobileDonneur().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("idPlateformeMobileDonneur"), "%" + filter.getIdPlateformeMobileDonneur() + "%"));
            }

            if (filter.getRibRipDonneur() != null && !filter.getRibRipDonneur().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("ribRipDonneur"), "%" + filter.getRibRipDonneur() + "%"));
            }

            if (filter.getNumTelDonneur() != null && !filter.getNumTelDonneur().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("numTelDonneur"), "%" + filter.getNumTelDonneur() + "%"));
            }

            if (filter.getMethodeAuthDonneur() != null && !filter.getMethodeAuthDonneur().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("methodeAuthDonneur"), filter.getMethodeAuthDonneur()));
            }

            // BENEFICIARY DATA FILTERS
            if (filter.getIdPlateformeMobileBeneficiaire() != null && !filter.getIdPlateformeMobileBeneficiaire().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("idPlateformeMobileBeneficiaire"), "%" + filter.getIdPlateformeMobileBeneficiaire() + "%"));
            }

            if (filter.getRibRipBeneficiaire() != null && !filter.getRibRipBeneficiaire().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("ribRipBeneficiaire"), "%" + filter.getRibRipBeneficiaire() + "%"));
            }

            if (filter.getNumTelBeneficiaire() != null && !filter.getNumTelBeneficiaire().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("numTelBeneficiaire"), "%" + filter.getNumTelBeneficiaire() + "%"));
            }

            // TRANSACTION DATA FILTERS
            if (filter.getTypeTransaction() != null && !filter.getTypeTransaction().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("typeTransaction"), "%" + filter.getTypeTransaction() + "%"));
            }

            if (filter.getDateTransaction() != null && !filter.getDateTransaction().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("dateTransaction"), "%" + filter.getDateTransaction() + "%"));
            }

            if (filter.getHeureTransaction() != null && !filter.getHeureTransaction().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("heureTransaction"), "%" + filter.getHeureTransaction() + "%"));
            }

            if (filter.getMontantTransaction() != null && !filter.getMontantTransaction().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("montantTransaction"), "%" + filter.getMontantTransaction() + "%"));
            }

            if (filter.getReferenceFactureCharge() != null && !filter.getReferenceFactureCharge().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("referenceFactureCharge"), "%" + filter.getReferenceFactureCharge() + "%"));
            }

            if (filter.getReferenceTransaction() != null && !filter.getReferenceTransaction().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("referenceTransaction"), "%" + filter.getReferenceTransaction() + "%"));
            }

            if (filter.getNumAutorisation() != null && !filter.getNumAutorisation().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("numAutorisation"), "%" + filter.getNumAutorisation() + "%"));
            }

            // COMMISSION FILTERS
            if (filter.getCommissionDonneurOrdre() != null && !filter.getCommissionDonneurOrdre().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("commissionDonneurOrdre"), "%" + filter.getCommissionDonneurOrdre() + "%"));
            }

            if (filter.getCommissionDestinataire() != null && !filter.getCommissionDestinataire().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("commissionDestinataire"), "%" + filter.getCommissionDestinataire() + "%"));
            }

            if (filter.getCommissionSwitchMobile() != null && !filter.getCommissionSwitchMobile().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("commissionSwitchMobile"), "%" + filter.getCommissionSwitchMobile() + "%"));
            }

            if (filter.getCommissionInterchange() != null && !filter.getCommissionInterchange().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("commissionInterchange"), "%" + filter.getCommissionInterchange() + "%"));
            }

            // CHARGEBACK FILTERS
            if (filter.getIdChargeback() != null && !filter.getIdChargeback().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("idChargeback"), "%" + filter.getIdChargeback() + "%"));
            }

            if (filter.getCodeMotifChargeback() != null && !filter.getCodeMotifChargeback().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("codeMotifChargeback"), "%" + filter.getCodeMotifChargeback() + "%"));
            }

            if (filter.getNumAutorisationOperationInitiale() != null && !filter.getNumAutorisationOperationInitiale().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("numAutorisationOperationInitiale"), "%" + filter.getNumAutorisationOperationInitiale() + "%"));
            }

            if (filter.getDateOperationInitiale() != null && !filter.getDateOperationInitiale().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("dateOperationInitiale"), "%" + filter.getDateOperationInitiale() + "%"));
            }

            if (filter.getRuf() != null && !filter.getRuf().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("ruf"), "%" + filter.getRuf() + "%"));
            }

            // Combine all predicates with AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
