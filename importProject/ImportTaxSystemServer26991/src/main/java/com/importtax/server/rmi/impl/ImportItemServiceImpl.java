package com.importtax.server.rmi.impl;

import com.importtax.server.dao.ImportItemDao;
import com.importtax.server.dao.UserDao;
import com.importtax.server.dao.impl.ImportItemDaoImpl;
import com.importtax.server.dao.impl.UserDaoImpl;
import com.importtax.server.model.ImportItem;
import com.importtax.server.model.User;
import com.importtax.server.rmi.ImportItemService;
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

    public ImportItemServiceImpl() throws RemoteException {
        this(new ImportItemDaoImpl(), new UserDaoImpl());
    }

    public ImportItemServiceImpl(ImportItemDao importItemDao) throws RemoteException {
        this(importItemDao, new UserDaoImpl());
    }

    public ImportItemServiceImpl(ImportItemDao importItemDao, UserDao userDao) throws RemoteException {
        super(importItemDao, LOGGER, "ImportItemService");
        this.importItemDao = importItemDao;
        this.userDao = userDao;
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
            validateItem(item, false);
            item.setItemId(null);
            normalizeAndCalculate(item);
            item.setUser(resolveUser(userId));
            LOGGER.info("Saving import item '{}' for userId={}", item.getItemName(), userId);
            return sanitize(importItemDao.saveItem(item));
        });
    }

    @Override
    public ImportItem updateItem(ImportItem item, Long userId) throws RemoteException {
        return execute("updateItem", () -> {
            validateItem(item, true);
            ImportItem existingItem = importItemDao.findItemById(item.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Import item not found"));

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
            if (userId != null) {
                existingItem.setUser(resolveUser(userId));
            }

            LOGGER.info("Updating import item id={}", existingItem.getItemId());
            return sanitize(importItemDao.updateItem(existingItem));
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
            if ("CLEARED".equalsIgnoreCase(item.getStatus())) {
                LOGGER.warn("Delete rejected for cleared import item id={}", itemId);
                throw new IllegalArgumentException("CLEARED items cannot be deleted");
            }
            LOGGER.info("Deleting import item id={}", itemId);
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

    private void validateItem(ImportItem item, boolean requireId) {
        if (item == null) {
            throw new IllegalArgumentException("Import item data is required");
        }
        if (requireId && item.getItemId() == null) {
            throw new IllegalArgumentException("Import item ID is required");
        }
        if (isBlank(item.getItemName())) {
            throw new IllegalArgumentException("Item Name is required");
        }
        if (isBlank(item.getCategory())) {
            throw new IllegalArgumentException("Category is required");
        }
        if (isBlank(item.getImporterName())) {
            throw new IllegalArgumentException("Importer Name is required");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit Price must be greater than 0");
        }
        if (item.getTaxRate() == null
                || item.getTaxRate().compareTo(BigDecimal.ZERO) < 0
                || item.getTaxRate().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Tax Rate must be between 0 and 100");
        }
        if (item.getImportDate() == null) {
            throw new IllegalArgumentException("Import Date is required");
        }
        normalizeStatus(item.getStatus());
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
        BigDecimal totalTax = quantity
                .multiply(item.getUnitPrice())
                .multiply(item.getTaxRate())
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

    private ImportItem sanitize(ImportItem item) {
        if (item != null && item.getUser() != null) {
            item.getUser().setPassword(null);
            item.getUser().setImportItems(null);
        }
        return item;
    }

    private String normalizeStatus(String status) {
        String normalizedStatus = isBlank(status) ? "PENDING" : status.trim().toUpperCase();
        if (!"PENDING".equals(normalizedStatus)
                && !"CLEARED".equals(normalizedStatus)
                && !"HOLD".equals(normalizedStatus)) {
            throw new IllegalArgumentException("Status must be PENDING, CLEARED, or HOLD");
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
