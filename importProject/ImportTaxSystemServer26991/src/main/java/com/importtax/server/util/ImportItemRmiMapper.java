package com.importtax.server.util;

import com.importtax.server.model.ImportItem;
import com.importtax.server.model.User;

import java.util.ArrayList;

/**
 * Builds plain Serializable import-item graphs for RMI (no Hibernate proxies or cycles).
 */
public final class ImportItemRmiMapper {

    private ImportItemRmiMapper() {
    }

    /**
     * Copies scalar fields only. Call while the Hibernate session is still open.
     */
    public static ImportItem toSafeImportItem(ImportItem managed) {
        if (managed == null) {
            return null;
        }
        ImportItem safe = new ImportItem(
                managed.getItemName(),
                managed.getCategory(),
                managed.getDescription(),
                managed.getQuantity(),
                managed.getUnitPrice(),
                managed.getCountryOfOrigin(),
                managed.getImporterName(),
                managed.getTaxRate(),
                managed.getTotalTax(),
                managed.getImportDate(),
                managed.getStatus(),
                toSafeUser(managed.getUser()));
        safe.setItemId(managed.getItemId());
        safe.setAppliedTaxes(new ArrayList<>());
        return safe;
    }

    /**
     * Minimal user snapshot for display/RBAC; no password or import-item backrefs.
     */
    public static User toSafeUser(User managed) {
        if (managed == null) {
            return null;
        }
        User safe = new User(
                managed.getFullName(),
                managed.getEmail(),
                managed.getUsername(),
                null,
                managed.getRole(),
                managed.getCreatedAt(),
                managed.getStatus());
        safe.setUserId(managed.getUserId());
        safe.setImportItems(new ArrayList<>());
        return safe;
    }
}
