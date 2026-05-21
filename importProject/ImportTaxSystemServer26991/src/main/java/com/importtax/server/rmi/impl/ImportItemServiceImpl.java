package com.importtax.server.rmi.impl;

import com.importtax.server.constants.ImportItemStatus;
import com.importtax.server.dao.ImportItemDao;
import com.importtax.server.dao.UserDao;
import com.importtax.server.dao.impl.ImportItemDaoImpl;
import com.importtax.server.dao.impl.UserDaoImpl;
import com.importtax.server.model.ImportItem;
import com.importtax.server.model.User;
import com.importtax.server.rmi.ImportItemService;
import com.importtax.server.service.NotificationWorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

public class ImportItemServiceImpl extends AbstractRemoteCrudService<ImportItem> implements ImportItemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportItemServiceImpl.class);

    private final ImportItemDao importItemDao;
    private final UserDao userDao;
    private final NotificationWorkflowService notificationWorkflow;

    public ImportItemServiceImpl() throws RemoteException {
        this(new ImportItemDaoImpl(), new UserDaoImpl(), new NotificationWorkflowService());
    }

    public ImportItemServiceImpl(ImportItemDao importItemDao) throws RemoteException {
        this(importItemDao, new UserDaoImpl(), new NotificationWorkflowService());
    }

    public ImportItemServiceImpl(ImportItemDao importItemDao, UserDao userDao) throws RemoteException {
        this(importItemDao, userDao, new NotificationWorkflowService());
    }

    public ImportItemServiceImpl(ImportItemDao importItemDao, UserDao userDao,
                                 NotificationWorkflowService notificationWorkflow) throws RemoteException {
        super(importItemDao, LOGGER, "ImportItemService");
        this.importItemDao = importItemDao;
        this.userDao = userDao;
        this.notificationWorkflow = notificationWorkflow;
    }

    @Override
    public ImportItem save(ImportItem entity) throws RemoteException {
        Long userId = entity != null && entity.getUser() != null ? entity.getUser().getUserId() : null;
        return saveItem(entity, userId);
    }

    @Override
    public ImportItem update(ImportItem entity) throws RemoteException {
        Long userId = entity != null && entity.getUser() != null ? entity.getUser().getUserId() : null;
        return updateItem(entity, userId);
    }

    @Override
    public void delete(ImportItem entity) throws RemoteException {
        Long itemId = entity != null ? entity.getItemId() : null;
        deleteItem(itemId);
    }

    @Override
    public ImportItem findById(Long id) throws RemoteException {
        return findItemById(id);
    }

    @Override
    public List<ImportItem> findAll() throws RemoteException {
        return findAllItems();
    }

    @Override
    public ImportItem saveItem(ImportItem item, Long userId) throws RemoteException {
        return execute("saveItem", () -> {
            User caller = resolveUser(userId);
            if (caller != null && "FINANCE_OFFICER".equalsIgnoreCase(caller.getRole())) {
                throw new SecurityException("Access denied. Finance Officer cannot create import items.");
            }
            validateItem(item, false);
            item.setItemId(null);
            normalizeAndCalculate(item);
            item.setUser(caller);
            LOGGER.info("Saving import item '{}' for userId={}", item.getItemName(), userId);
            ImportItem persisted = importItemDao.saveItem(item);
            return sanitize(reloadItem(persisted.getItemId()).orElse(persisted));
        });
    }

    @Override
    public ImportItem updateItem(ImportItem item, Long userId) throws RemoteException {
        return execute("updateItem", () -> {
            User caller = resolveUser(userId);
            if (caller != null && "FINANCE_OFFICER".equalsIgnoreCase(caller.getRole())) {
                throw new SecurityException("Access denied. Finance Officer cannot modify import items.");
            }
            validateItem(item, true);
            ImportItem existingItem = importItemDao.findItemById(item.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Import item not found"));

            String previousStatus = ImportItemStatus.canonicalFromDatabase(existingItem.getStatus());
            String nextStatus = normalizeStatus(item.getStatus());
            enforceStatusTransition(previousStatus, nextStatus);

            existingItem.setItemName(item.getItemName());
            existingItem.setCategory(item.getCategory());
            existingItem.setDescription(item.getDescription());
            existingItem.setQuantity(item.getQuantity());
            existingItem.setUnitPrice(item.getUnitPrice());
            existingItem.setCountryOfOrigin(item.getCountryOfOrigin());
            existingItem.setImporterName(item.getImporterName());
            existingItem.setTaxRate(item.getTaxRate());
            existingItem.setImportDate(item.getImportDate());
            existingItem.setStatus(item.getStatus());
            normalizeAndCalculate(existingItem);
            if (caller != null) {
                existingItem.setUser(caller);
            }

            LOGGER.info("Updating import item id={}, status transition {} -> {}",
                    existingItem.getItemId(), previousStatus, nextStatus);
            importItemDao.updateItem(existingItem);
            ImportItem updated = sanitize(reloadItem(existingItem.getItemId()).orElse(existingItem));
            if (ImportItemStatus.PAID.equals(previousStatus) && ImportItemStatus.CLEARED.equals(nextStatus)) {
                notificationWorkflow.handleClearance(updated);
            }
            return updated;
        });
    }

    @Override
    public void deleteItem(Long itemId) throws RemoteException {
        execute("deleteItem", () -> {
            if (itemId == null) {
                throw new IllegalArgumentException("Import item ID is required");
            }
            ImportItem item = importItemDao.findItemById(itemId)
                    .orElseThrow(() -> new IllegalArgumentException("Import item not found"));
            if (ImportItemStatus.CLEARED.equals(ImportItemStatus.canonicalFromDatabase(item.getStatus()))) {
                LOGGER.warn("Delete rejected for cleared import item id={}", itemId);
                throw new IllegalArgumentException("CLEARED items cannot be deleted");
            }
            LOGGER.info("Deleting import item id={}", itemId);
            importItemDao.deleteItem(item);
            return null;
        });
    }

    @Override
    public void deleteItemSecure(Long itemId, Long callerUserId) throws RemoteException {
        execute("deleteItemSecure", () -> {
            User caller = resolveUser(callerUserId);
            if (caller == null || !"ADMIN".equalsIgnoreCase(caller.getRole())) {
                throw new SecurityException("Access denied. Administrator privileges required.");
            }
            if (itemId == null) {
                throw new IllegalArgumentException("Import item ID is required");
            }
            ImportItem item = importItemDao.findItemById(itemId)
                    .orElseThrow(() -> new IllegalArgumentException("Import item not found"));
            if (ImportItemStatus.CLEARED.equals(ImportItemStatus.canonicalFromDatabase(item.getStatus()))) {
                LOGGER.warn("Delete rejected for cleared import item id={}", itemId);
                throw new IllegalArgumentException("CLEARED items cannot be deleted");
            }
            LOGGER.info("Deleting import item id={} securely via callerUserId={}", itemId, callerUserId);
            importItemDao.deleteItem(item);
            return null;
        });
    }

    @Override
    public List<ImportItem> findAllItems() throws RemoteException {
        return execute("findAllItems", () -> sanitize(importItemDao.findAllItems()));
    }

    @Override
    public ImportItem findItemById(Long itemId) throws RemoteException {
        return execute("findItemById", () -> {
            if (itemId == null) {
                throw new IllegalArgumentException("Import item ID is required");
            }
            return sanitize(importItemDao.findItemById(itemId).orElse(null));
        });
    }

    @Override
    public List<ImportItem> searchItemsByName(String itemName) throws RemoteException {
        return execute("searchItemsByName", () -> sanitize(importItemDao.searchItemsByName(itemName)));
    }

    @Override
    public List<ImportItem> findItemsByStatus(String status) throws RemoteException {
        return execute("findItemsByStatus", () -> {
            String normalizedStatus = normalizeStatus(status);
            return sanitize(importItemDao.findItemsByStatus(normalizedStatus));
        });
    }

    @Override
    public List<ImportItem> findItemsByUser(Long userId) throws RemoteException {
        return execute("findItemsByUser", () -> {
            if (userId == null) {
                throw new IllegalArgumentException("User ID is required");
            }
            return sanitize(importItemDao.findItemsByUser(userId));
        });
    }

    @Override
    public List<ImportItem> findByStatus(String status) throws RemoteException {
        return findItemsByStatus(status);
    }

    @Override
    public List<ImportItem> searchByItemName(String itemName) throws RemoteException {
        return searchItemsByName(itemName);
    }

    private static final int MAX_QUANTITY = 1_000_000;

    private void validateItem(ImportItem item, boolean requireId) {
        if (item == null) {
            throw new IllegalArgumentException("Import item data is required");
        }
        if (requireId && item.getItemId() == null) {
            throw new IllegalArgumentException("Import item ID is required");
        }

        String itemName = trimToEmpty(item.getItemName());
        if (itemName.isEmpty()) {
            throw new IllegalArgumentException("Item name is required");
        }
        if (itemName.length() < 3) {
            throw new IllegalArgumentException("Item name must be at least 3 characters");
        }

        String category = trimToEmpty(item.getCategory());
        if (category.isEmpty()) {
            throw new IllegalArgumentException("Category is required");
        }
        if ("Select Category".equalsIgnoreCase(category)) {
            throw new IllegalArgumentException("Please select a valid category");
        }

        String importerName = trimToEmpty(item.getImporterName());
        if (importerName.isEmpty()) {
            throw new IllegalArgumentException("Importer name is required");
        }
        if (importerName.length() < 3) {
            throw new IllegalArgumentException("Importer name must be at least 3 characters");
        }

        if (isBlank(item.getCountryOfOrigin())) {
            throw new IllegalArgumentException("Country of origin is required");
        }

        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (item.getQuantity() > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity cannot exceed " + MAX_QUANTITY);
        }

        if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than zero");
        }

        if (item.getTaxRate() == null
                || item.getTaxRate().compareTo(BigDecimal.ZERO) < 0
                || item.getTaxRate().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Tax rate must be between 0 and 100");
        }

        if (item.getImportDate() == null) {
            throw new IllegalArgumentException("Import date is required");
        }
        if (item.getImportDate().isAfter(LocalDate.now())) {
            LOGGER.warn("Import date validation failed: future date {}", item.getImportDate());
            throw new IllegalArgumentException("Import date cannot be in the future");
        }
        normalizeStatus(item.getStatus());
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Allowed: PENDING→PAID|HOLD, PAID→CLEARED, HOLD→PENDING.
     * PAID/CLEARED cannot regress per specification.
     */
    private void enforceStatusTransition(String previousCanonical, String nextCanonical) {
        if (previousCanonical.equals(nextCanonical)) {
            return;
        }

        if (ImportItemStatus.CLEARED.equals(nextCanonical)
                && !ImportItemStatus.PAID.equals(previousCanonical)) {
            LOGGER.warn("Rejected clearance without payment: current status {}", previousCanonical);
            throw new IllegalArgumentException(
                    "Import item cannot be cleared before payment is completed.");
        }

        boolean allowed = switch (previousCanonical) {
            case ImportItemStatus.PENDING ->
                    ImportItemStatus.PAID.equals(nextCanonical) || ImportItemStatus.HOLD.equals(nextCanonical);
            case ImportItemStatus.PAID -> ImportItemStatus.CLEARED.equals(nextCanonical);
            case ImportItemStatus.HOLD -> ImportItemStatus.PENDING.equals(nextCanonical);
            case ImportItemStatus.CLEARED -> false;
            default -> false;
        };

        if (!allowed) {
            LOGGER.warn("Rejected invalid status transition {} -> {}", previousCanonical, nextCanonical);
            throw new IllegalArgumentException(
                    String.format("Invalid status transition from %s to %s.", previousCanonical, nextCanonical));
        }
    }

    private void normalizeAndCalculate(ImportItem item) {
        item.setItemName(item.getItemName().trim());
        item.setCategory(item.getCategory().trim());
        item.setDescription(trimToNull(item.getDescription()));
        item.setCountryOfOrigin(trimToNull(item.getCountryOfOrigin()));
        item.setImporterName(item.getImporterName().trim());
        item.setStatus(normalizeStatus(item.getStatus()));
        if (item.getImportDate() == null) {
            item.setImportDate(LocalDate.now());
        }

        BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
        // Single source of truth: scalar tax_rate on the item (not M:M appliedTaxes).
        BigDecimal effectiveRate = item.getTaxRate();
        BigDecimal totalTax = quantity
                .multiply(item.getUnitPrice())
                .multiply(effectiveRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        item.setUnitPrice(item.getUnitPrice().setScale(2, RoundingMode.HALF_UP));
        item.setTaxRate(item.getTaxRate().setScale(2, RoundingMode.HALF_UP));
        item.setTotalTax(totalTax);
    }

    private User resolveUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Logged-in user was not found"));
    }

    private List<ImportItem> sanitize(List<ImportItem> items) {
        items.forEach(this::sanitize);
        return items;
    }

    private java.util.Optional<ImportItem> reloadItem(Long itemId) {
        if (itemId == null) {
            return java.util.Optional.empty();
        }
        return importItemDao.findItemById(itemId);
    }

    private ImportItem sanitize(ImportItem item) {
        if (item != null) {
            if (item.getUser() != null) {
                item.getUser().setPassword(null);
                item.getUser().setImportItems(null);
            }
            // Avoid lazy init / circular graphs over RMI — client uses tax_rate + total_tax columns only.
            item.setAppliedTaxes(null);
        }
        return item;
    }

    private String normalizeStatus(String status) {
        String normalizedStatus = isBlank(status) ? ImportItemStatus.PENDING : status.trim().toUpperCase();
        if (!ImportItemStatus.isKnown(normalizedStatus)) {
            throw new IllegalArgumentException("Status must be one of: PENDING, PAID, CLEARED, HOLD.");
        }
        return normalizedStatus;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
