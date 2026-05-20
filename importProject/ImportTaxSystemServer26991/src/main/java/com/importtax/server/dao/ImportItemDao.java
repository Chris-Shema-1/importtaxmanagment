package com.importtax.server.dao;

import com.importtax.server.model.ImportItem;

import java.util.List;
import java.util.Optional;

public interface ImportItemDao extends GenericDao<ImportItem> {

    ImportItem saveItem(ImportItem item);

    ImportItem updateItem(ImportItem item);

    void deleteItem(ImportItem item);

    List<ImportItem> findAllItems();

    Optional<ImportItem> findItemById(Long itemId);

    List<ImportItem> searchItemsByName(String itemName);

    List<ImportItem> findItemsByStatus(String status);

    List<ImportItem> findItemsByUser(Long userId);

    List<ImportItem> findByStatus(String status);

    List<ImportItem> searchByItemName(String itemName);

    long countAllItems();

    long countByStatus(String status);

    List<ImportItem> findRecentItems(int limit);
}
