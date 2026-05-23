package com.importtax.server.dao.impl;

import com.importtax.server.dao.ImportItemDao;
import com.importtax.server.model.ImportItem;
import com.importtax.server.util.ImportItemRmiMapper;
import jakarta.persistence.TypedQuery;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ImportItemDaoImpl extends GenericDaoImpl<ImportItem> implements ImportItemDao {

    private static final String ITEM_LIST_FETCH =
            "select distinct i from ImportItem i left join fetch i.user u ";

    public ImportItemDaoImpl() {
        super(ImportItem.class);
    }

    public ImportItemDaoImpl(SessionFactory sessionFactory) {
        super(ImportItem.class, sessionFactory);
    }

    @Override
    public ImportItem saveItem(ImportItem item) {
        return save(item);
    }

    @Override
    public ImportItem updateItem(ImportItem item) {
        return update(item);
    }

    @Override
    public void deleteItem(ImportItem item) {
        delete(item);
    }

    @Override
    public List<ImportItem> findAllItems() {
        return executeReadOnly(session -> {
            List<ImportItem> loaded = session.createQuery(
                            ITEM_LIST_FETCH + "order by i.importDate desc, i.itemId desc",
                            ImportItem.class)
                    .getResultList();
            return toSafeList(loaded);
        }, "findAllItems");
    }

    @Override
    public Optional<ImportItem> findItemById(Long itemId) {
        return executeReadOnly(session -> {
            List<ImportItem> list = session.createQuery(
                            ITEM_LIST_FETCH + "where i.itemId = :itemId",
                            ImportItem.class)
                    .setParameter("itemId", itemId)
                    .setMaxResults(1)
                    .getResultList();
            if (list.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(ImportItemRmiMapper.toSafeImportItem(list.get(0)));
        }, "findItemById");
    }

    @Override
    public List<ImportItem> searchItemsByName(String itemName) {
        return searchByItemName(itemName);
    }

    @Override
    public List<ImportItem> findItemsByStatus(String status) {
        return findByStatus(status);
    }

    @Override
    public List<ImportItem> findItemsByUser(Long userId) {
        return executeReadOnly(session -> {
            TypedQuery<ImportItem> query = session.createQuery(
                    ITEM_LIST_FETCH + "where u.userId = :userId "
                            + "order by i.importDate desc, i.itemId desc",
                    ImportItem.class);
            query.setParameter("userId", userId);
            return toSafeList(query.getResultList());
        }, "findItemsByUser");
    }

    @Override
    public List<ImportItem> findByStatus(String status) {
        return executeReadOnly(session -> {
            TypedQuery<ImportItem> query = session.createQuery(
                    ITEM_LIST_FETCH + "where i.status = :status "
                            + "order by i.importDate desc, i.itemId desc",
                    ImportItem.class);
            query.setParameter("status", status);
            return toSafeList(query.getResultList());
        }, "findByStatus");
    }

    @Override
    public List<ImportItem> searchByItemName(String itemName) {
        String searchTerm = itemName == null ? "" : itemName.trim().toLowerCase();
        return executeReadOnly(session -> {
            TypedQuery<ImportItem> query = session.createQuery(
                    ITEM_LIST_FETCH + "where lower(i.itemName) like :itemName "
                            + "order by i.itemName",
                    ImportItem.class);
            query.setParameter("itemName", "%" + searchTerm + "%");
            return toSafeList(query.getResultList());
        }, "searchByItemName");
    }

    @Override
    public long countAllItems() {
        return executeReadOnly(session -> session.createQuery(
                "select count(i) from ImportItem i", Long.class).getSingleResult(), "countAllItems");
    }

    @Override
    public long countByStatus(String status) {
        return executeReadOnly(session -> session.createQuery(
                        "select count(i) from ImportItem i where upper(i.status) = upper(:status)", Long.class)
                .setParameter("status", status)
                .getSingleResult(), "countByStatus");
    }

    @Override
    public List<ImportItem> findRecentItems(int limit) {
        int max = Math.max(1, Math.min(limit, 20));
        return executeReadOnly(session -> {
            List<ImportItem> loaded = session.createQuery(
                            ITEM_LIST_FETCH + "order by i.importDate desc, i.itemId desc",
                            ImportItem.class)
                    .setMaxResults(max)
                    .getResultList();
            return toSafeList(loaded);
        }, "findRecentItems");
    }

    private static List<ImportItem> toSafeList(List<ImportItem> loaded) {
        List<ImportItem> safe = new ArrayList<>(loaded.size());
        for (ImportItem item : loaded) {
            safe.add(ImportItemRmiMapper.toSafeImportItem(item));
        }
        return safe;
    }
}
